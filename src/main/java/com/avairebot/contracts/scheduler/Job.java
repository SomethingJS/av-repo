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

package com.avbot.contracts.scheduler;

import com.avbot.av;
import com.avbot.contracts.reflection.Reflectional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public abstract class Job extends TimerTask implements Reflectional {

    private static final Logger log = LoggerFactory.getLogger(Job.class);

    /**
     * The av class instance, this is used to access
     * and interact with the rest of the application.
     */
    protected final av av;

    /**
     * The amount of time the job should be delayed before starting,
     * the {@link #unit} of time is determined by {@link #unit}.
     */
    private final long delay;

    /**
     * The amount of time in between each time the job should be executed.
     */
    private final long period;

    /**
     * The unit of time that the job should be scaled after.
     *
     * @see TimeUnit
     */
    private final TimeUnit unit;

    /**
     * Instantiates the job instance with the given av application instance, with
     * a delay of 0, period of 1, and time unit of {@link TimeUnit#MINUTES}.
     *
     * @param av The av application instance.
     */
    public Job(av av) {
        this(av, 0);
    }

    /**
     * Instantiates the job instance with the given av application instance and delay,
     * setting the default period to 1 along with a time unit of {@link TimeUnit#MINUTES}.
     *
     * @param av The av application instance.
     * @param delay  The delay before the command should be executed for the first time.
     */
    public Job(av av, long delay) {
        this(av, delay, 1);
    }

    /**
     * Instantiates the job instance with the given av application instance, delay,
     * and period, setting the default time unit to {@link TimeUnit#MINUTES}.
     *
     * @param av The av application instance.
     * @param delay  The delay before the command should be executed for the first time.
     * @param period The time in between executions after the job has already run once.
     */
    public Job(av av, long delay, long period) {
        this(av, delay, period, TimeUnit.MINUTES);
    }

    /**
     * Instantiates the job instance with the given av application
     * instance, delay, and, period and time unit.
     *
     * @param av The av application instance.
     * @param delay  The delay before the command should be executed for the first time.
     * @param period The time in between executions after the job has already run once.
     * @param unit   The unit of time the job should measure the delay and periods in.
     */
    public Job(av av, long delay, long period, TimeUnit unit) {
        this.av = av;

        this.delay = delay;
        this.period = period;
        this.unit = unit;
    }

    /**
     * Gets the job delay.
     *
     * @return The job delay.
     */
    public long getDelay() {
        return delay;
    }

    /**
     * Gets the job period.
     *
     * @return The job period.
     */
    public long getPeriod() {
        return period;
    }

    /**
     * Gets the time unit.
     *
     * @return The time unit.
     */
    public TimeUnit getUnit() {
        return unit;
    }

    /**
     * Handles the given tasks by invoking them one by one within a try-catch
     * statement, if a exception is thrown nothing should fail.
     *
     * @param tasks The tasks that should be handled.
     */
    protected void handleTask(Task... tasks) {
        for (Task task : tasks) {
            try {
                log.trace("Invoking {}#handle(av)", task.getClass().getName());
                task.handle(av);
            } catch (Exception ex) {
                log.error("An error occurred while running the {} class, message: {}",
                    task.getClass().getSimpleName(), ex.getMessage(), ex
                );
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(av, delay, period, unit);
    }
}
