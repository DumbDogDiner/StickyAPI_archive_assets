/**
 * Copyright (c) 2020 DumbDogDiner <dumbdogdiner.com>. All rights reserved.
 * Licensed under the MIT license, see LICENSE for more information...
 */
package com.dumbdogdiner.stickyapi.common.util.url;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class URLUtil {

    private static final Pattern urlPattern = Pattern.compile(
            "(https:\\/\\/|http:\\ /\\/)((?:[-;:&=\\+\\$,\\w]+@)?[A-Za-z0-9.-]+|(www.|[-;:&=\\+\\$,\\w]+@)[A-Za-z0-9.-]+)((?:\\/[\\+~%\\/.\\w-_]*)?\\??(?:[-\\+=&;%@.\\w_]*)#?(?:[\\w]*))?");

    /**
     * Find the first URL in a given Text.
     * <p>
     * Returns a URLPair object which stores the full URL as well as a shortened
     * version (e.g. www.github.com)
     * 
     * @param text The text that should be checked for URLs
     * @return {@link URLPair}
     */
    public static URLPair findURL(@NotNull String text) {
        Matcher matcher = urlPattern.matcher(text);

        if (matcher.find()) {
            return new URLPair(matcher.group(0), matcher.group(2));
        }
        return null;
    }

    /**
     * Converts URLs in a preformatted String to clickable JSON components.
     * <p>
     * Returns a TextComponent containing formatted and clickable URLs.
     * 
     * @param text The text that should be converted into a TextComponent with
     *             formatted URLs.
     * @return {@link TextComponent}
     */
    public static TextComponent convertURLs(@NotNull String text) {
        TextComponent finalComp = new TextComponent();
        for (String s : text.split(" ")) {
            URLPair url = findURL(s + " ");
            if ((url) == null) {
                finalComp.addExtra(s + " ");
            } else {
                TextComponent urlComponent = new TextComponent(url.getShortened() + " ");
                urlComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url.getFullPath()));
                urlComponent.setBold(true);
                finalComp.addExtra(urlComponent);
            }
        }

        return finalComp;
    }

}
