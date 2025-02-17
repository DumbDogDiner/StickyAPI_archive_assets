/*
 * Copyright (c) 2020-2021 DumbDogDiner <dumbdogdiner.com>. All rights reserved.
 * Licensed under the MIT license, see LICENSE for more information...
 */
package com.dumbdogdiner.stickyapi.common.util;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Preconditions;

import net.md_5.bungee.api.ChatColor;

import org.jetbrains.annotations.NotNull;

/**
 * Operations on {@link java.lang.String}
 */
public final class StringUtil {
    private StringUtil() {
    }

    private static HashMap<String, String> leetReplace = new HashMap<>();

    // createProgressBar - format double percentage to no decimal places to avoid it
    // showing as '100.0000%' or something
    private static DecimalFormat percentageFormatter = new DecimalFormat("#");

    static {
        leetReplace.put("0", "o");
        leetReplace.put("1", "l");
        leetReplace.put("3", "e");
        leetReplace.put("4", "a");
        leetReplace.put("5", "s");
        leetReplace.put("6", "d");
        leetReplace.put("7", "t");
        leetReplace.put("_", "");
    }

    /**
     * Create a horizontal progress bar, similar to how htop does it.
     * 
     * <p>
     * Returns a progress bar
     * 
     * @param size              The size of the bar (inside)
     * @param percentage        The percentage to fill the bar to
     * @param monospace         If false, the bars will be a character with the
     *                          equivalent amount of pixels as a whitespace
     *                          character
     * @param includePercentage If true, the percentage will be appended inside of
     *                          outside of the bar, depending on how much whitespace
     *                          is available
     * @param includeBrackets   If true, the progress bar will return wrapped in
     *                          snuggly brackets
     * @return {@link String}
     */
    public static String createProgressBar(@NotNull double size, @NotNull double percentage, @NotNull boolean monospace,
            @NotNull boolean includePercentage, @NotNull boolean includeBrackets) {
        double barCount = ((percentage / 100) * size);
        StringBuilder barBuilder = new StringBuilder();
        for (double i = 0; i < size; i++) {
            if (i < barCount) {
                if (!monospace)
                    barBuilder.append("\u258D"); // ...
                else
                    barBuilder.append("|");
            } else {
                barBuilder.append(" ");
            }
        }

        if (includeBrackets) {
            barBuilder.insert(0, "[");
            barBuilder.append("]");
        }

        if (includePercentage)
            barBuilder.append(String.format(" %s%%", percentageFormatter.format(percentage)));

        return barBuilder.toString();
    }

    /**
     * Create a horizontal progress bar, similar to how htop does it.
     * <p>
     * Returns a progress bar
     * 
     * @param size       The size of the bar
     * @param percentage The percentage to fill the bar to
     * @return {@link String}
     */
    public static String createProgressBar(@NotNull double size, @NotNull double percentage) {
        return createProgressBar(size, percentage, false, false, true);
    }

    /**
     * Capitalise every letter after whitespace.
     * <p>
     * This will also lowercase any uppercase characters.
     * <p>
     * Example: "hello world" == "Hello World"
     * <p>
     * Example: "hello WORLD" == "Hello World" || "Hello WORLD" depending on the
     * `keepCase` input
     * <p>
     * Returns a message with capital letters after every whitespace
     * <p>
     * See {@link #capitaliseSentenceKeepUpperCase(String)}
     * 
     * @param string   The string to capitalise
     * @param keepCase Whether or not to keep the uppercase characters
     * @return {@link String}
     */
    public static String capitaliseSentence(@NotNull String string, @NotNull Boolean keepCase) {
        StringBuilder sb = new StringBuilder();
        boolean cnl = true;
        for (char c : string.toCharArray()) {
            if (cnl && Character.isLetter(c)) {
                sb.append(Character.toUpperCase(c));
                cnl = false;
            } else {
                sb.append(keepCase ? c : Character.toLowerCase(c));
            }
            if (Character.isWhitespace(c)) {
                cnl = true;
            }
        }
        return sb.toString();
    }

    /**
     * Capitalise every letter after whitespace.
     * <p>
     * This will also lowercase any uppercase characters.
     * <p>
     * Example: "hello world" == "Hello World"
     * <p>
     * Example: "HELLO WORLD" == "Hello World"}
     * <p>
     * Returns a message with capital letters after every whitespace
     * <p>
     * See Alternate (keeping uppercase):
     * {@link #capitaliseSentenceKeepUpperCase(String)}
     * 
     * @param string The string to capitalise
     * @return {@link String}
     */
    public static String capitaliseSentence(@NotNull String string) {
        return capitaliseSentence(string, false);
    }

    /**
     * Capitalise every letter after whitespace.
     * <p>
     * Will keep uppercase letters uppercase.
     * <p>
     * Example: "hello world" == "Hello World"
     * <p>
     * Example: "hello WORLD" == "Hello WORLD"
     * 
     * <p>
     * See Alternate (not keeping uppercase): {@link #capitaliseSentence(String)}
     * 
     * <p>
     * Returns a message with capital letters after every whitespace
     * 
     * @since 2.0
     * @param string The string to capitalise
     * @return {@link String}
     */
    public static String capitaliseSentenceKeepUpperCase(@NotNull String string) {
        return capitaliseSentence(string, true);
    }

    /**
     * Replace a word with asterisks.
     * <p>
     * The censored word
     * 
     * @param word  The word to censor
     * @param regex The characters to not censor
     * @return {@link String}
     */
    public static String censorWord(@NotNull String word, @NotNull String regex) {
        StringBuilder asterisks = new StringBuilder();

        for (int i = 0; i < word.length(); i++) {
            if (String.valueOf(word.charAt(i)).matches(regex)) {
                asterisks.append(word.charAt(i));
            } else {
                asterisks.append("*");
            }
        }

        return asterisks.toString();
    }

    /**
     * Replace a word with asterisks.
     *
     * <p>
     * The censored word
     * 
     * @param word The word to censor
     * @return {@link String}
     */
    public static String censorWord(@NotNull String word) {
        return censorWord(word, "[ -/:-@\\[-`{-~¡-¿]");
    }

    /**
     * Filter "Leet Speak" out of a message
     * <p>
     * Example:
     * 
     * <pre>
     * Translation.replaceLeet("50m3 1337 5p34k h3r3") = "some leet speak here"
     * </pre>
     * 
     * <p>
     * The filtered message
     * 
     * @param message The message to filter
     * @return {@link String}
     */
    public static String replaceLeet(@NotNull String message) {
        if (message.trim().isEmpty())
            return message;

        for (Map.Entry<String, String> entry : leetReplace.entrySet())
            message = message.replaceAll(entry.getKey(), entry.getValue());

        return message;
    }

    /**
     * Check if many strings equal a single comparison string
     * 
     * <p>
     * Whether something matches.
     * 
     * @param haystack the string to compare to
     * @param needles  things that may match the comparison string
     * @return {@link Boolean}
     */
    public static boolean compareMany(@NotNull String haystack, @NotNull String[] needles) {
        for (String needle : needles) {
            if (haystack.equalsIgnoreCase(needle))
                return true;
        }

        return false;
    }

    /**
     * This method uses a region to check case-insensitive equality. This means the
     * internal array does not need to be copied like a toLowerCase() call would.
     *
     * @param string String to check
     * @param prefix Prefix of string to compare
     * @return {@link Boolean}
     * @throws NullPointerException if prefix or string is null
     */
    public static boolean startsWithIgnoreCase(@NotNull final String string, @NotNull final String prefix) {
        if (string.length() < prefix.length()) {
            return false;
        }
        return string.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    /**
     * This method uses a region to check case-sensitive equality.
     *
     * @param string String to check
     * @param prefix Prefix of string to compare
     * @return {@link Boolean}
     * @throws NullPointerException if prefix or string is null
     */
    public static boolean startsWith(@NotNull final String string, @NotNull final String prefix) {
        if (string.length() < prefix.length()) {
            return false;
        }
        return string.regionMatches(false, 0, prefix, 0, prefix.length());
    }

    /**
     * Put hyphens into a uuid
     * <p>
     * e.x. de8c89e12f25424d8078c6ff58db7d6e &gt;
     * de8c89e1-2f25-424d-8078-c6ff58db7d6e
     * 
     * @param uuid to hyphenate
     * @return {@link UUID}
     * @throws NullPointerException     if uuid string is null
     * @throws IllegalArgumentException if uuid is not 32 characters and is invalid
     */
    public static UUID hyphenateUUID(@NotNull String uuid) {
        if (uuid.length() == 32) {
            return UUID.fromString(uuid.replaceFirst( // https://stackoverflow.com/a/19399768
                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                    "$1-$2-$3-$4-$5"));
        } else {
            return UUID.fromString(uuid);
        }
    }

    /**
     * Creates a random string of "magic" characters of characters
     *
     * @param min               the minimum length of the string (inclusive)
     * @param max               the maximum length of the string (inclusive)
     * @param minRunBeforeSpace Minimum number of characters before a space can
     *                          appear, set to 0 to disable space
     * @return A string of random characters, where the length
     */
    public static String randomObfuscatedString(int min, int max, int minRunBeforeSpace) {
        Preconditions.checkArgument(max >= min, "Max cannot be less than min");
        // Placeholder chars by width
        char[] choices = new char[] {
                // Space is boring
                // ' ', // 1 px
                'i', // 2 px
                'l', // 3 px
                't', // 4 px
                'f', // 5 px
                'a', // 6 px
                '@' // 7 px
        };
        StringBuilder obfuscated = new StringBuilder();

        int len = MathUtil.randomInt(min, max);
        int charsSinceSpace = 0;
        while (obfuscated.length() < len) {
            if (minRunBeforeSpace > 0 && charsSinceSpace > minRunBeforeSpace &&
            // Set a 5% probability of the character being a space
                    MathUtil.randomInt(1, 100) <= 5) {
                obfuscated.append(' ');
                charsSinceSpace = 0;
            } else {
                obfuscated.append(MathUtil.randomElement(choices));
                charsSinceSpace++;
            }
        }
        obfuscated.insert(0, ChatColor.MAGIC);
        obfuscated.append(ChatColor.RESET);
        return obfuscated.toString();
    }

    /**
     * Replaces &amp; followed by any valid minecraft format code (matching the
     * regex
     * 
     * <pre>
     * (?=([a-f]|[0-9]|[klmnor]))
     * </pre>
     * 
     * with &#x00a7;
     *
     * @param input The input string
     * @return A string where the relevant ampersands are replaced with section
     *         symbols
     */
    public static String formatChatCodes(String input) {
        return input.replaceAll("&(?=([a-f]|[0-9]|[klmnor]))", Character.toString(ChatColor.COLOR_CHAR));
    }

}