/*
 * Copyright (c) 2018.
 *
 * This file is part of av.
 *
 * av is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * av is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with av.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package com.avbot.scheduler.tasks;

import com.avbot.av;
import com.avbot.contracts.scheduler.Task;
import com.avbot.factories.RequestFactory;
import com.avbot.metrics.Metrics;
import com.avbot.requests.Response;
import com.avbot.time.Carbon;
import com.avbot.utilities.NumberUtil;
import com.avbot.vote.VoteCacheEntity;
import com.avbot.vote.VoteEntity;
import com.avbot.vote.VoteMetricType;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Consumer;

public class DrainVoteQueueTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(DrainVoteQueueTask.class);

    @Override
    public void handle(av av) {
        if (av.getVoteManager() == null || av.getVoteManager().getQueue().isEmpty()) {
            return;
        }

        VoteEntity entity = av.getVoteManager().getQueue().poll();
        if (entity == null) {
            return;
        }

        if (av.getConfig().getBoolean("vote-lock.sync-with-public-bot", false)) {
            RequestFactory.makeGET("http://api.avbot.com/v1/votes/" + entity.getUserId())
                .send((Consumer<Response>) response -> acceptViaPublicSync(av, response, entity));
        }

        String apiToken = av.getConfig().getString("vote-lock.vote-sync-token");
        if (apiToken == null || apiToken.trim().length() == 0) {
            return;
        }

        if (apiToken.equalsIgnoreCase("ReplaceThisWithYourAPITokenForDBL")) {
            return;
        }

        log.info("Checking vote requests for {} with the DBL API...", entity.getUserId());

        RequestFactory.makeGET("https://discordbots.org/api/bots/275270122082533378/check")
            .addParameter("userId", entity.getUserId())
            .addHeader("Authorization", av.getConfig().getString("vote-lock.vote-sync-token"))
            .send((Consumer<Response>) response -> acceptViaDBL(av, response, entity));
    }

    private void acceptViaPublicSync(av av, Response response, VoteEntity entity) {
        if (response.getResponse().code() != 200) {
            return;
        }

        Object obj = response.toService(Map.class);
        if (!(obj instanceof Map)) {
            return;
        }

        Map<String, Boolean> data = (Map<String, Boolean>) obj;
        if (data.isEmpty()) {
            return;
        }

        Boolean result = data.getOrDefault(String.valueOf(entity.getUserId()), false);
        if (result == null || !result) {
            return;
        }

        Metrics.dblVotes.labels(VoteMetricType.COMMAND.getName()).inc();

        Carbon expiresIn = new Carbon(response.getResponse().header("Date")).addHours(12);

        log.info("Vote record for {} was found, registering vote that expires on {}", entity.getUserId(), expiresIn.toDateTimeString());

        User user = av.getShardManager().getUserById(entity.getUserId());
        if (user == null) {
            return;
        }

        handleRegisteringVote(av, user, expiresIn, entity);
    }

    private void acceptViaDBL(av av, Response response, VoteEntity entity) {
        if (response.getResponse().code() != 200) {
            return;
        }

        Object obj = response.toService(Map.class);
        if (!(obj instanceof Map)) {
            return;
        }

        Map<String, Object> data = (Map<String, Object>) obj;
        if (data.isEmpty()) {
            return;
        }

        int voted = NumberUtil.parseInt(data.getOrDefault("voted", "0.0")
            .toString().split("\\.")[0]);

        if (voted != 1) {
            return;
        }

        Metrics.dblVotes.labels(VoteMetricType.COMMAND.getName()).inc();

        Carbon expiresIn = new Carbon(response.getResponse().header("Date")).addHours(12);

        log.info("Vote record for {} was found, registering vote that expires on {}", entity.getUserId(), expiresIn.toDateTimeString());

        User user = av.getShardManager().getUserById(entity.getUserId());
        if (user == null) {
            return;
        }

        handleRegisteringVote(av, user, expiresIn, entity);
    }

    private void handleRegisteringVote(av av, User user, Carbon expiresIn, VoteEntity entity) {
        VoteCacheEntity voteEntity = av.getVoteManager().getVoteEntityWithFallback(user);
        voteEntity.setCarbon(expiresIn);

        av.getVoteManager().registerVoteFor(user.getIdLong(), 1);

        log.info("Vote has been registered by {} ({})",
            user.getName() + "#" + user.getDiscriminator(), user.getId()
        );

        TextChannel textChannel = av.getShardManager().getTextChannelById(entity.getChannelId());
        if (textChannel == null || !textChannel.canTalk()) {
            if (voteEntity.isOptIn()) {
                av.getVoteManager().getMessenger()
                    .SendThanksForVotingMessageInDM(user, voteEntity.getVotePoints());
            }
            return;
        }

        textChannel.sendMessage(
            av.getVoteManager().getMessenger().buildThanksForVotingMessage(
                "Your vote has been registered!", voteEntity.getVotePoints()
            )
        ).queue(null, error -> {
            if (voteEntity.isOptIn()) {
                av.getVoteManager().getMessenger()
                    .SendThanksForVotingMessageInDM(user, voteEntity.getVotePoints());
            }
        });
    }
}
