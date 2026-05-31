/*
 * Copyright (C) 2021-2022 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatcore.util;

import fi.dy.masa.malilib.util.StringUtils;

import java.util.*;
import java.util.function.BiFunction;
import lombok.experimental.UtilityClass;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

@UtilityClass
public class TextUtil {

    private final char[] SUPERSCRIPTS =
            new char[] {
                    '⁰', '¹', '²', '³', '⁴', '⁵', '⁶', '⁷',
                    '⁸', '⁹'
            };

    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) {
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0;
        }
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    public int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0) costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

    public String toSuperscript(int num) {
        StringBuilder sb = new StringBuilder();
        do {
            sb.append(SUPERSCRIPTS[num % 10]);
        } while ((num /= 10) > 0);
        return sb.reverse().toString();
    }

    public int getMaxLengthTranslation(Collection<String> translations) {
        return getMaxLengthTranslation(translations.toArray(new String[0]));
    }

    public int getMaxLengthTranslation(String... translations) {
        List<String> translated = new ArrayList<>();
        for (String translation : translations) {
            translated.add(StringUtils.translate(translation));
        }
        return getMaxLengthString(translated);
    }

    public int getMaxLengthString(Collection<String> strings) {
        return getMaxLengthString(strings.toArray(new String[0]));
    }

    public int getMaxLengthString(String... strings) {
        int max = 0;
        for (String str : strings) {
            int width = StringUtils.getStringWidth(str);
            if (width > max) {
                max = width;
            }
        }
        return max;
    }

    private TreeMap<StringMatch, StringInsert> filterMatches(
            Map<StringMatch, StringInsert> matches) {
        TreeMap<StringMatch, StringInsert> map = new TreeMap<>(matches);
        Iterator<StringMatch> search = new TreeMap<>(map).keySet().iterator();
        int lastEnd = 0;
        while (search.hasNext()) {
            StringMatch m = search.next();
            if (m.start < lastEnd) {
                map.remove(m);
            } else {
                lastEnd = m.end;
            }
        }
        return map;
    }

    public Component replaceStrings(Component input, Map<StringMatch, StringInsert> matches) {
        if (matches.size() == 0) {
            return input;
        }
        Iterator<Map.Entry<StringMatch, StringInsert>> sortedMatches =
                filterMatches(matches).entrySet().iterator();
        if (!sortedMatches.hasNext()) {
            return input;
        }
        TextBuilder newSiblings = new TextBuilder();
        Map.Entry<StringMatch, StringInsert> match = sortedMatches.next();

        int totalchar = 0;
        boolean inMatch = false;
        for (RawText rawText : new TextBuilder().append(input).getTexts()) {

            if (rawText.getString() == null || rawText.getString().length() <= 0) {
                continue;
            }
            if (match == null) {
                newSiblings.append(rawText);
                continue;
            }
            int length = rawText.getString().length();
            int last = 0;
            while (true) {
                if (length + totalchar <= match.getKey().start) {
                    newSiblings.append(rawText.getString().substring(last), rawText.getStyle());
                    break;
                }
                int start = match.getKey().start - totalchar;
                int end = match.getKey().end - totalchar;
                if (inMatch) {
                    if (end <= length) {
                        inMatch = false;
                        newSiblings.append(rawText.getString().substring(end), rawText.getStyle());
                        last = end;
                        if (!sortedMatches.hasNext()) {
                            match = null;
                            break;
                        }
                        match = sortedMatches.next();
                    } else {
                        break;
                    }
                } else if (start < length) {
                    if (start > 0) {
                        newSiblings.append(rawText.getString().substring(last, start), rawText.getStyle());
                    }
                    if (end >= length) {
                        newSiblings.append(match.getValue().getText(rawText, match.getKey()));
                        if (end == length) {
                            if (!sortedMatches.hasNext()) {
                                match = null;
                                break;
                            }
                            match = sortedMatches.next();
                        } else {
                            inMatch = true;
                        }
                        break;
                    }
                    newSiblings.append(match.getValue().getText(rawText, match.getKey()));
                    if (!sortedMatches.hasNext()) {
                        match = null;
                    } else {
                        match = sortedMatches.next();
                    }
                    last = end;
                    if (match == null || match.getKey().start - totalchar > length) {
                        newSiblings.append(rawText.getString().substring(end), rawText.getStyle());
                        break;
                    }
                } else {
                    break;
                }
                if (match == null) {
                    break;
                }
            }
            totalchar = totalchar + length;
        }

        return newSiblings.build();
    }

    public static MutableComponent truncate(Component input, StringMatch match) {
        ArrayList<Component> newSiblings = new ArrayList<>();
        boolean start = false;
        int totalchar = 0;
        List<Component> siblings = input.getSiblings();
        // TODO: verify MutableComponent.create() API in 26.1
        siblings.add(0, MutableComponent.create(input.getContents()).setStyle(input.getStyle()));
        for (Component sib : siblings) {
            if (sib.getContents() == null || sib.getString().length() <= 0) {
                continue;
            }

            int length = sib.getString().length();

            if (totalchar + length > match.start) {
                if (totalchar + length >= match.end) {
                    if (!start) {
                        newSiblings.add(
                                Component.literal(
                                        sib.getString()
                                                .substring(
                                                        match.start - totalchar,
                                                        match.end - totalchar)).withStyle(sib.getStyle()));
                    } else {
                        newSiblings.add(
                                Component.literal(
                                        sib.getString().substring(0, match.end - totalchar)).withStyle(sib.getStyle()));
                    }
                    MutableComponent newtext = Component.empty();
                    for (Component sibling : newSiblings) {
                        newtext.append(sibling);
                    }
                    return newtext;
                } else {
                    if (!start) {
                        newSiblings.add(
                                Component.literal(
                                        sib.getString().substring(match.start - totalchar)).withStyle(sib.getStyle()));
                        start = true;
                    } else {
                        newSiblings.add(sib);
                    }
                }
            }

            totalchar = totalchar + length;
        }

        MutableComponent newtext = Component.empty();
        for (Component sibling : newSiblings) {
            newtext.append(sibling);
        }
        return newtext;
    }

    public static boolean styleChanges(Component component) {
        Style style = null;
        if (component.getSiblings().size() == 1) {
            return false;
        }
        for (Component raw : component.getSiblings()) {
            if (style == null) {
                style = raw.getStyle();
            } else if (!style.equals(raw.getStyle())) {
                return true;
            }
        }
        return false;
    }

    public static boolean styleChanges(Component component, BiFunction<Style, Style, Boolean> predicate) {
        Style previous = null;
        if (component.getSiblings().size() == 1) {
            return !predicate.apply(component.getSiblings().get(0).getStyle(), component.getSiblings().get(0).getStyle());
        }
        for (Component raw : component.getSiblings()) {
            if (previous == null) {
                previous = raw.getStyle();
            } else if (!previous.equals(raw.getStyle())) {
                if (!predicate.apply(previous, raw.getStyle())) {
                    return true;
                }
                previous = raw.getStyle();
            }
        }
        return false;
    }

    public static String getContent(ComponentContents content) {
        StringBuilder builder = new StringBuilder();
        content.visit((s) -> {
            builder.append(s);
            return Optional.empty();
        });
        return builder.toString();
    }
}
