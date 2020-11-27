/**
 * Copyright (c) 2020 DumbDogDiner <dumbdogdiner.com>. All rights reserved.
 * Licensed under the MIT license, see LICENSE for more information...
 */
package com.dumbdogdiner.stickyapi.common.util;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Utility methods for dealing with time and duration parsing.
 */
public class TimeUtil {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d yyyy HH:mm:ss");

    private static final HashMap<Character, Long> DURATION_CHARS = new HashMap<>();
    // TODO Why are both upper and lower put in here??
    static {
        DURATION_CHARS.put('Y', 31536000L);
        DURATION_CHARS.put('y', 31536000L);
        DURATION_CHARS.put('M', 2592000L);
        DURATION_CHARS.put('W', 604800L);
        DURATION_CHARS.put('w', 604800L);
        DURATION_CHARS.put('D', 86400L);
        DURATION_CHARS.put('d', 86400L);
        DURATION_CHARS.put('H', 3600L);
        DURATION_CHARS.put('h', 3600L);
        DURATION_CHARS.put('m', 60L);
        DURATION_CHARS.put('S', 1L);
        DURATION_CHARS.put('s', 1L);
    }

    /**
     * Convert milliseconds into a compact human readable time string
     * <p>
     * Returns human readable duration
     * 
     * @param time in milliseconds (e.x. 1000 == 0.02/m)
     * @return {@link java.lang.String}
     */
    public static String significantDurationString(final long time) {
        StringBuilder message = new StringBuilder();
        double timeSince = (double) (System.currentTimeMillis() / 1000L)
                - ((double) (System.currentTimeMillis() / 1000L) - ((double)time / 1000L) + 0.0);
        if ((timeSince /= 60.0) < 60.0) {
            message.append(new DecimalFormat("0.00").format(timeSince)).append("/m");
        }
        if (message.length() == 0 && (timeSince /= 60.0) < 24.0) {
            message.append(new DecimalFormat("0.00").format(timeSince)).append("/h");
        }
        if (message.length() == 0 && (timeSince /= 24.0) < 30) {
            message.append(new DecimalFormat("0.00").format(timeSince)).append("/d");
        }
        if (message.length() == 0) {
            message.append(new DecimalFormat("0.00").format(timeSince /= 30.0)).append("/mo");
        }
        return message.toString();
    }

    /**
     * Convert milliseconds into a human readable time string
     * <p>
     * Returns human readable duration
     * 
     * @param time in milliseconds (e.x. 1000 == 1 second)
     * @return {@link java.lang.String}
     */
    public static String durationString(final long time) {
        var t = time / 1000L;
        long years = t / 31449600;
        long weeks = (t / 604800) % 52;
        long days = (t / 86400) % 7;
        long hours = (t / 3600) % 24;
        long minutes = (t / 60) % 60;
        long seconds = t % 60;

        List<String> components = new ArrayList<>();

        if (years != 0) {
            components.add(String.format("%d %s", years, years != 1 ? "years" : "year"));
        }
        if (weeks != 0) {
            components.add(String.format("%d %s", weeks, weeks != 1 ? "weeks" : "week"));
        }
        if (days != 0) {
            components.add(String.format("%d %s", days, days != 1 ? "days" : "day"));
        }
        if (hours != 0) {
            components.add(String.format("%d %s", hours, hours != 1 ? "hours" : "hour"));
        }
        if (minutes != 0) {
            components.add(String.format("%d %s", minutes, minutes != 1 ? "minutes" : "minute"));
        }
        if (seconds != 0) {
            components.add(String.format("%d %s", seconds, seconds != 1 ? "seconds" : "second"));
        }
        StringBuilder sb = new StringBuilder();
        for (String str : components) {
            if (components.get(components.size() - 1) == str) {
                if (components.size() == 1) {
                    sb.append(str);
                    break;
                }
                sb.append("and " + str);
                break;
            }
            sb.append(str + ", ");
        }
        return sb.toString();
    }

    /**
     * Convert milliseconds into a human readable time string
     * <p>
     * Returns human readable duration
     * 
     * @param timestamp to convert to a duration string
     * @return {@link java.lang.String}
     */
    public static String durationString(@NotNull Timestamp timestamp) {
        return durationString(System.currentTimeMillis() - timestamp.getTime());
    }

    /**
     * Convert milliseconds into a compact human readable time string
     * <p>
     * Returns human readable duration
     * 
     * @param timestamp to convert to a significant duration string
     * @return {@link java.lang.String}
     */
    public static String significantDurationString(@NotNull Timestamp timestamp) {
        return significantDurationString(System.currentTimeMillis() - timestamp.getTime());
    }

    private static String expTime(@NotNull long time, @NotNull boolean compact) {
        long currentTime = System.currentTimeMillis();
        if (time == 0)
            return "never expires";
        else if (time < currentTime)
            return String.format("%s ago",
                    compact ? TimeUtil.significantDurationString(time) : TimeUtil.durationString(time));
        else
            return String.format("%s from now",
                    compact ? TimeUtil.significantDurationString(time) : TimeUtil.durationString(time));
    }

    /**
     * Convert amount of time in milliseconds since unix epoch into a human readable
     * time string time
     * <p>
     * Returns a string stating the duration forward or backward in time.
     * 
     * @param time in milliseconds since unix epoch
     * @return {@link java.lang.String}
     */
    public static String expirationTime(@NotNull long time) {
        return expTime(time, false);
    }

    /**
     * Convert a timestamp into a human readable time string
     * <p>
     * Returns a string stating the duration forward or backward in time.
     * 
     * @param timestamp to convert
     * @return {@link java.lang.String}
     */
    public static String expirationTime(@NotNull Timestamp timestamp) {
        return expirationTime(System.currentTimeMillis() - timestamp.getTime());
    }

    /**
     * Convert amount of time in milliseconds since unix epoch into a human readable
     * time string time
     * <p>
     * Returns a string stating the duration forward or backward in time.
     * 
     * @param time in milliseconds since unix epoch
     * @return {@link java.lang.String}
     */
    public static String significantExpirationTime(@NotNull long time) {
        return expTime(time, true);
    }

    /**
     * Convert a timestamp into a human readable time string
     * <p>
     * Returns a string stating the duration forward or backward in time.
     * 
     * @param timestamp to convert
     * @return {@link java.lang.String}
     */
    public static String significantExpirationTime(@NotNull Timestamp timestamp) {
        return significantExpirationTime(System.currentTimeMillis() - timestamp.getTime());
    }

    /**
     * Parse a duration string to a length of time in seconds.
     * <p>
     * Returns the duration converted to seconds
     * 
     * @param string the string of characters to convert to a quantity of seconds
     * @return {@link java.lang.String}
     */
    public static Optional<Long> duration(@NotNull String string) {
        var total = 0L;
        var subtotal = 0L;

        for (var i = 0; i < string.length(); ++i) {
            char ch = string.charAt(i);
            if ((ch >= '0') && (ch <= '9'))
                subtotal = (subtotal * 10) + (ch - '0');
            else {
                // Found something thats not a number, find out how much it multiplies the built
                // up number by, multiply the total and reset the built up number.

                Long multiplier = TimeUtil.DURATION_CHARS.get(ch);
                if (multiplier == null)
                    return Optional.empty();

                total += subtotal * multiplier;

                // Next subtotal please!
                subtotal = 0;
            }
        }
        return Optional.of(total);
    }

    /**
     * Convert a unix timestamp to a human readable date string
     * 
     * @param t Number of seconds since unix epoch
     * @return A string with the format "EEE, MMM d yyyy HH:mm:ss"
     * @deprecated Use the
     *             {@link com.dumbdogdiner.stickyapi.common.translation.Translation}
     *             functions for converting timestamps to human readable strings.
     *             {@link java.text.SimpleDateFormat}
     */
    @Deprecated
    public static String timeString(@Nullable long t) {
        return TimeUtil.timeString(new Timestamp(t));
    }

    /**
     * Convert a SQL Timestamp to a human readable date string
     * 
     * @param ts The time as a Timestamp object
     * @return A string with the format "EEE, MMM d yyyy HH:mm:ss"
     * @deprecated Use the
     *             {@link com.dumbdogdiner.stickyapi.common.translation.Translation}
     *             functions for converting timestamps to human readable strings.
     *             {@link java.text.SimpleDateFormat}
     */
    @Deprecated
    public static String timeString(@Nullable Timestamp ts) {
        if (ts == null)
            return "";
        return sdf.format(ts);
    }

    /**
     * Convert a human-readable duration to a SQL Timestamp object
     * 
     * @param timePeriod A duration string (eg, "2y1w10d40m6s")
     * @return {@link java.sql.Timestamp}
     */
    public static Timestamp toTimestamp(@NotNull String timePeriod) {
        boolean compare = StringUtil.compareMany(timePeriod, new String[] { "*", "0" });
        if (compare)
            return null;

        // If it's numeric, lets do some extra checks!
        if (NumberUtil.isNumeric(timePeriod)) {
            // Return null if it's greater 12 characters long
            if (timePeriod.length() > 12)
                return null;
            return _toTimestamp(timePeriod);
        }
        if (!compare) {
            return _toTimestamp(timePeriod);
        }

        return null;
    }

    private static Timestamp _toTimestamp(String timePeriod) {
        Optional<Long> dur = TimeUtil.duration(timePeriod);
        if (dur.isPresent())
            return ((TimeUtil.getUnixTime() + dur.get()) * 1000L) >= (253402261199L * 1000L) ? null
                    : new Timestamp((TimeUtil.getUnixTime() + dur.get()) * 1000L);
        return null;
    }

    /**
     * Get the current system time as a Unix timestamp
     * 
     * @return Current number of seconds since Unix Epoch
     */
    public static long getUnixTime() {
        return System.currentTimeMillis() / 1000L;
    }

    /**
     * Get the current system time as a SQL Timestamp object
     * 
     * @return {@link java.sql.Timestamp}
     */
    public static Timestamp now() {
        return new Timestamp(TimeUtil.getUnixTime() * 1000L);
    }
}
