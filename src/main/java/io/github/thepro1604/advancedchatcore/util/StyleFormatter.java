/*
 * Copyright (C) 2021-2022 thepro1604
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.thepro1604.advancedchatcore.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Unit;

/** Class to format Component without losing data */
@Environment(EnvType.CLIENT)
public class StyleFormatter {

    /**
     * An interface to take multiple inputs from a string that has the Section Symbol formatting
     * combined with standard {@link Component} ChatFormatting.
     */
    public interface FormattingVisitable {
        /**
         * Accepts a character with information about current formatting
         *
         * @param c Current character
         * @param currentIndex The current index of the raw string
         * @param realIndex The current index without formatting symbols
         * @param textStyle The style that the Component currently has
         * @param formattingStyle The style that combines Component formatting and formatting symbols
         * @return Whether to continue
         */
        boolean accept(
                char c, int currentIndex, int realIndex, Style textStyle, Style formattingStyle);
    }

    private int currentIndex;
    private int realIndex;
    private int skipBy = 0;
    private Style currentStyle;
    private Style lastTextStyle = null;
    private final FormattingVisitable visitor;
    private final int length;
    private final boolean keepCodes;

    /** Results of different parts of formatting */
    private enum Result {
        /** Go up a character */
        INCREMENT,

        /** Go to the next {@link FormattedText} */
        SKIP,

        /** STOP */
        TERMINATE,
    }

    /**
     * Creates a StyleFormatter for a given length and a {@link FormattingVisitable}
     *
     * <p>This class is meant to be updated with a {@link FormattedText.StyledVisitor}
     *
     * @param visitor {@link FormattingVisitable} to get updated with each visible character
     * @param length Length of the string
     */
    public StyleFormatter(FormattingVisitable visitor, int length) {
        this(visitor, length, false);
    }

    /**
     * Creates a StyleFormatter for a given length and a {@link FormattingVisitable}
     *
     * @param visitor {@link FormattingVisitable} to get updated with each visible character
     * @param length Length of the string
     * @param keepCodes Whether the raw formatting code characters (e.g. {@code §a} or {@code §#RRGGBB})
     *                  should also be sent to the visitor -- styled with the formatting they apply --
     *                  instead of being consumed silently
     */
    public StyleFormatter(FormattingVisitable visitor, int length, boolean keepCodes) {
        this.visitor = visitor;
        this.currentIndex = 0;
        this.realIndex = 0;
        this.currentStyle = Style.EMPTY;
        this.length = length;
        this.keepCodes = keepCodes;
    }

    /** Sends the current character with the current information to the visitor. */
    private boolean sendToVisitor(char c, Style textStyle) {
        return visitor.accept(c, currentIndex, realIndex, textStyle, currentStyle);
    }

    /** Handles how section symbols get processed */
    private Result updateSection(Style textStyle, Character nextChar, String rest) {
        if (nextChar == null) {
            return Result.SKIP;
        }
        if (nextChar == '#') {
            if (rest.length() > 6) {
                String format = rest.substring(1, 7);
                if (!SearchUtils.isMatch(format, "^[0-9a-fA-F]{6}", FindType.REGEX)) {
                    currentIndex++;
                    return Result.INCREMENT;
                }
                int red = Integer.parseInt(format.substring(0, 2), 16);
                int green = Integer.parseInt(format.substring(2, 4), 16);
                int blue = Integer.parseInt(format.substring(4, 6), 16);
                TextColor color = TextColor.fromRgb(new Color(red, green, blue, 255).color());
                if (currentStyle.equals(Style.EMPTY) || currentStyle.equals(textStyle)) {
                    // If it's empty or different rely on just the current Component style
                    // Arbitrary color
                    currentStyle = textStyle.applyFormat(ChatFormatting.BLACK);
                } else {
                    // Styles are different so we take what happened before. This allows us to chain
                    // formatting symbols.

                    // Arbitrary color to reset
                    currentStyle = currentStyle.applyFormat(ChatFormatting.BLACK);
                }
                currentStyle = currentStyle.withColor(color);
                currentIndex += 7;
                skipBy = 6;
            }
            return Result.INCREMENT;
        }
        ChatFormatting formatting = ChatFormatting.getByCode(nextChar);
        if (formatting != null) {
            if (formatting == ChatFormatting.RESET) {
                // If it resets, just go to what the current Component is.
                currentStyle = textStyle;
            } else {
                if (currentStyle.equals(Style.EMPTY) || currentStyle.equals(textStyle)) {
                    // If it's empty or different rely on just the current Component style
                    currentStyle = textStyle.applyFormat(formatting);
                } else {
                    // Styles are different so we take what happened before. This allows us to chain
                    // formatting symbols.
                    currentStyle = currentStyle.applyFormat(formatting);
                }
            }
            if (currentStyle.equals(Style.EMPTY)) {
                currentStyle = textStyle;
            }
        }
        currentIndex++;
        return Result.INCREMENT;
    }

    /**
     * Updates current visitable data as well as signifies whether to end.
     *
     * <p>Calling this method will result in each 'visible' character being sent to the {@link
     * FormattingVisitable}
     *
     * @param textStyle Style of the current string
     * @param string The current string
     * @return Value to terminate. Follows {@link FormattedText.StyledContentConsumer} return values.
     */
    public Optional<Optional<Unit>> updateStyle(Style textStyle, String string) {
        if (lastTextStyle == null) {
            lastTextStyle = textStyle;
        }
        currentStyle = textStyle;
        int stringLength = string.length();
        for (int i = 0; i < stringLength; i++) {
            char c = string.charAt(i);
            Character nextChar = null;
            if (i + 1 < stringLength) {
                nextChar = string.charAt(i + 1);
            }
            if (c == '§') {
                int codeStart = i;
                skipBy = 0;
                switch (updateSection(textStyle, nextChar, string.substring(i + 1))) {
                    case SKIP:
                        return Optional.empty();
                    case TERMINATE:
                        return Optional.of(Optional.of(net.minecraft.util.Unit.INSTANCE));
                    case INCREMENT:
                        i++;
                }
                i += skipBy;
                if (keepCodes) {
                    // Emit the raw code characters (e.g. "§a" or "§#RRGGBB") themselves, styled
                    // with the formatting they just applied, instead of consuming them silently.
                    for (int j = codeStart; j <= i; j++) {
                        sendToVisitor(string.charAt(j), textStyle);
                        realIndex++;
                    }
                }
            } else if (sendToVisitor(c, textStyle)) {
                realIndex++;
            } else {
                return Optional.of(Optional.of(net.minecraft.util.Unit.INSTANCE));
            }
            currentIndex++;
        }
        lastTextStyle = textStyle;
        return Optional.empty();
    }

    /**
     * Formats Component that contains styling data as well as formatting symbols
     *
     * <p>This method is used to remove section symbols while maintaining previous formatting as
     * well as new ChatFormatting.
     *
     * @param Component text to reformat
     * @return Formatted Component
     */
    public static MutableComponent formatText(Component text) {
        String originalString = text.getString();
        MutableComponent t = Component.empty();
        int length = originalString.length();

        StyleFormatter formatter =
                new StyleFormatter(
                        (c, index, formattedIndex, style, formattedStyle) -> {
                            t.append(Component.literal(String.valueOf(c)).withStyle(formattedStyle));
                            return true;
                        },
                        length);
        text.visit(formatter::updateStyle, Style.EMPTY);

        return flattenText(t);
    }

    /**
     * Formats Component that contains styling data as well as formatting symbols, same as {@link
     * #formatText(Component)}, but keeps the raw formatting code characters (e.g. {@code &a} converted
     * to {@code §a}, or {@code §#RRGGBB}) visible in the output instead of stripping them, while still
     * applying the color/formatting they specify.
     *
     * @param text Component to reformat
     * @return Formatted Component with formatting codes still visible
     */
    public static MutableComponent formatTextKeepCodes(Component text) {
        String originalString = text.getString();
        MutableComponent t = Component.empty();
        int length = originalString.length();

        StyleFormatter formatter =
                new StyleFormatter(
                        (c, index, formattedIndex, style, formattedStyle) -> {
                            t.append(Component.literal(String.valueOf(c)).withStyle(formattedStyle));
                            return true;
                        },
                        length,
                        true);
        text.visit(formatter::updateStyle, Style.EMPTY);

        return flattenText(t);
    }

    public static MutableComponent flattenText(Component text) {
        TextBuilder builder = new TextBuilder();
        builder.append(text);

        MutableComponent newText = Component.empty();
        Style lastStyle = null;
        StringBuilder accumulated = new StringBuilder();

        for (RawText raw : builder.getTexts()) {
            if (lastStyle == null) {
                lastStyle = raw.getStyle();
                accumulated.append(raw.getString());
            } else if (raw.getStyle().equals(lastStyle)) {
                accumulated.append(raw.getString());
            } else {
                if (accumulated.length() > 0) {
                    newText.append(Component.literal(accumulated.toString()).withStyle(lastStyle));
                }
                accumulated = new StringBuilder(raw.getString());
                lastStyle = raw.getStyle();
            }
        }

        // Add any remaining accumulated Component
        if (accumulated.length() > 0 && lastStyle != null) {
            newText.append(Component.literal(accumulated.toString()).withStyle(lastStyle));
        }

        return newText;
    }

    /**
     * Wraps Component into multiple lines
     *
     * @param font Font to handle Component
     * @param scaledWidth Maximum width before the line breaks
     * @param Component text to break up
     * @return List of Component of the new lines
     * TODO: verify ComponentRenderUtils.wrapComponents is the correct class/method in 26.1
     */
    public static List<Component> wrapText(Font font, int scaledWidth, Component text) {
        ArrayList<Component> lines = new ArrayList<>();
        for (FormattedCharSequence line : ComponentRenderUtils.wrapComponents(text, scaledWidth, font)) {
            lines.add(new TextBuilder().append(line).build());
        }
        return lines;
    }
}
