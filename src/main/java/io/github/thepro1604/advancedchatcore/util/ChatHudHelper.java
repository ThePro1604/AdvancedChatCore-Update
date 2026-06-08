/*
 * Copyright (C) 2026 thepro1604
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.thepro1604.advancedchatcore.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Helper class to interact with ChatComponent in Minecraft 26.1.
 */
public class ChatHudHelper {

    /**
     * Render the hover tooltip for a Style containing a HoverEvent.
     * Handles ShowText (text tooltip), ShowEntity (entity info), and ShowItem (item tooltip).
     * Splits on embedded newlines so they render as proper line breaks.
     */
    public static void renderHoverTooltip(GuiGraphicsExtractor drawContext, Style style, int mouseX, int mouseY) {
        if (style == null) return;
        HoverEvent hoverEvent = style.getHoverEvent();
        if (hoverEvent == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (hoverEvent instanceof HoverEvent.ShowText showText) {
            List<Component> lines = splitOnNewlines(showText.value());
            drawContext.setComponentTooltipForNextFrame(mc.font, lines, mouseX, mouseY);
        } else if (hoverEvent instanceof HoverEvent.ShowEntity showEntity) {
            drawContext.setComponentTooltipForNextFrame(mc.font, showEntity.entity().getTooltipLines(), mouseX, mouseY);
        } else if (hoverEvent instanceof HoverEvent.ShowItem showItem) {
            drawContext.setTooltipForNextFrame(mc.font, showItem.item().create(), mouseX, mouseY);
        }
    }

    /**
     * Split a Component on embedded newline characters into a list of line Components,
     * preserving all style information on each segment.
     */
    private static List<Component> splitOnNewlines(Component text) {
        List<Component> lines = new ArrayList<>();
        // currentLine accumulates styled segments for the current line
        final MutableComponent[] currentLine = {Component.empty()};

        text.visit((style, string) -> {
            int start = 0;
            int idx;
            while ((idx = string.indexOf('\n', start)) >= 0) {
                // Add text before the newline to the current line
                if (idx > start) {
                    currentLine[0].append(Component.literal(string.substring(start, idx)).withStyle(style));
                }
                // Finish this line and start a new one
                lines.add(currentLine[0]);
                currentLine[0] = Component.empty();
                start = idx + 1;
            }
            // Remaining text after last newline
            if (start < string.length()) {
                currentLine[0].append(Component.literal(string.substring(start)).withStyle(style));
            }
            return Optional.empty();
        }, Style.EMPTY);

        // Always add the last accumulated line (even if empty, to avoid empty tooltip)
        lines.add(currentLine[0]);

        return lines.isEmpty() ? List.of(text) : lines;
    }

    @Nullable
    public static Style getTextStyleAt(ChatComponent chatComponent, double mouseX, double mouseY) {
        if (chatComponent == null) {
            return null;
        }
        Minecraft mc = Minecraft.getInstance();
        ActiveTextCollector.ClickableStyleFinder finder =
                new ActiveTextCollector.ClickableStyleFinder(mc.font, (int) mouseX, (int) mouseY);
        chatComponent.captureClickableText(
                finder,
                mc.getWindow().getGuiScaledHeight(),
                mc.gui.getGuiTicks(),
                ChatComponent.DisplayMode.FOREGROUND);
        return finder.result();
    }
}
