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

package com.avbot.commands.utility;

import com.avbot.AppInfo;
import com.avbot.av;
import com.avbot.audio.AudioHandler;
import com.avbot.chat.MessageType;
import com.avbot.commands.CommandMessage;
import com.avbot.contracts.commands.Command;
import com.avbot.contracts.commands.CommandGroup;
import com.avbot.contracts.commands.CommandGroups;
import com.avbot.language.I18n;
import com.avbot.metrics.Metrics;
import com.avbot.utilities.NumberUtil;
import io.prometheus.client.Collector;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import javax.annotation.Nonnull;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StatsCommand extends Command {

    public StatsCommand(av av) {
        super(av);
    }

    @Override
    public String getName() {
        return "Stats Command";
    }

    @Override
    public String getDescription() {
        return "Displays information about Ava and some related stats.";
    }

    @Override
    public List<String> getUsageInstructions() {
        return Collections.singletonList("`:command` - Shows some stats about the bot.");
    }

    @Override
    public List<String> getTriggers() {
        return Arrays.asList("stats", "about");
    }

    @Nonnull
    @Override
    public List<CommandGroup> getGroups() {
        return Collections.singletonList(CommandGroups.BOT_INFORMATION);
    }

    @Override
    public boolean onCommand(CommandMessage context, String[] args) {
        context.makeEmbeddedMessage(MessageType.INFO,
            new MessageEmbed.Field(context.i18n("fields.author"), "[Senither#0001](https://senither.com/)", true),
            new MessageEmbed.Field(context.i18n("fields.website"), "[avbot.com](https://avbot.com/)", true),
            new MessageEmbed.Field(context.i18n("fields.library"), "[JDA](https://github.com/DV8FromTheWorld/JDA)", true),
            new MessageEmbed.Field(context.i18n("fields.database"), getDatabaseQueriesStats(context), true),
            new MessageEmbed.Field(context.i18n("fields.messages"), getMessagesReceivedStats(context), true),
            new MessageEmbed.Field(context.i18n("fields.commands"), getCommandExecutedStats(context), true),
            new MessageEmbed.Field(context.i18n("fields.shard"), "" + context.getJDA().getShardInfo().getShardId(), true),
            new MessageEmbed.Field(context.i18n("fields.memory"), memoryUsage(context), true),
            new MessageEmbed.Field(context.i18n("fields.uptime"), applicationUptime(), true),
            new MessageEmbed.Field(context.i18n("fields.members"), NumberUtil.formatNicely(av.getShardEntityCounter().getUsers()), true),
            new MessageEmbed.Field(context.i18n("fields.channels"), NumberUtil.formatNicely(av.getShardEntityCounter().getChannels()), true),
            new MessageEmbed.Field(context.i18n("fields.servers"), NumberUtil.formatNicely(av.getShardEntityCounter().getGuilds()), true)
        )
            .setTitle(context.i18n("title"), "https://avbot.com/support")
            .setAuthor("av v" + AppInfo.getAppInfo().version, "https://avbot.com/support", av.getSelfUser().getEffectiveAvatarUrl())
            .setFooter(context.i18n("footer",
                NumberUtil.formatNicely(AudioHandler.getDefaultAudioHandler().getTotalListenersSize()),
                NumberUtil.formatNicely(AudioHandler.getDefaultAudioHandler().getTotalQueueSize())
            ))
            .queue();

        return true;
    }

    private String applicationUptime() {
        RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
        long seconds = rb.getUptime() / 1000;
        long d = (long) Math.floor(seconds / 86400);
        long h = (long) Math.floor((seconds % 86400) / 3600);
        long m = (long) Math.floor(((seconds % 86400) % 3600) / 60);
        long s = (long) Math.floor(((seconds % 86400) % 3600) % 60);

        if (d > 0) {
            return String.format("%sd %sh %sm %ss", d, h, m, s);
        }

        if (h > 0) {
            return String.format("%sh %sm %ss", h, m, s);
        }

        if (m > 0) {
            return String.format("%sm %ss", m, s);
        }
        return String.format("%ss", s);
    }

    private String getDatabaseQueriesStats(CommandMessage context) {
        return formatDynamicValue(context, getTotalsFrom(Metrics.databaseQueries.collect()));
    }

    private String getMessagesReceivedStats(CommandMessage context) {
        return formatDynamicValue(context, (int) Metrics.jdaEvents.labels(MessageReceivedEvent.class.getSimpleName()).get());
    }

    private String getCommandExecutedStats(CommandMessage context) {
        return formatDynamicValue(context, getTotalsFrom(Metrics.commandsReceived.collect()));
    }

    private String memoryUsage(CommandMessage context) {
        return context.i18n("formats.memory",
            (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024),
            Runtime.getRuntime().totalMemory() / (1024 * 1024)
        );
    }

    private String formatDynamicValue(CommandMessage context, int rawValue) {
        double value = rawValue / ((double) ManagementFactory.getRuntimeMXBean().getUptime() / 1000D);
        return I18n.format(
            context.i18n(value < 1.5D ? "formats.perMinute" : "formats.perSecond"),
            NumberUtil.formatNicely(rawValue),
            NumberUtil.formatNicelyWithDecimals(value < 1.5D ? value * 60D : value)
        );
    }

    private int getTotalsFrom(List<Collector.MetricFamilySamples> familySamples) {
        double total = 0.0D;
        for (Collector.MetricFamilySamples family : familySamples) {
            for (Collector.MetricFamilySamples.Sample sample : family.samples) {
                total += sample.value;
            }
        }
        return (int) total;
    }
}
