/*
 * Copyright (C) 2026 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatcore.util;

import io.github.darkkronicle.advancedchatcore.mixin.MixinChatHudInvoker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.Style;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Helper class to interact with ChatHud in Minecraft 1.21.11+
 * since the old API methods were removed.
 */
public class ChatHudHelper {

    /**
     * Get the Style at the given mouse coordinates, if any.
     * This replaces the removed ChatHud.getTextStyleAt() method.
     *
     * @param chatHud The ChatHud instance
     * @param mouseX Mouse X coordinate
     * @param mouseY Mouse Y coordinate
     * @return The Style at the position, or null if none
     */
    @Nullable
    public static Style getTextStyleAt(ChatHud chatHud, double mouseX, double mouseY) {
        if (chatHud == null) {
            return null;
        }

        MixinChatHudInvoker accessor = (MixinChatHudInvoker) chatHud;
        
        // Convert screen coordinates to chat line coordinates
        double chatLineX = toChatLineX(chatHud, mouseX);
        double chatLineY = toChatLineY(chatHud, mouseY);

        // Check if coordinates are valid
        if (chatLineX < 0 || chatLineY < 0) {
            return null;
        }

        // Get the visible messages
        List<ChatHudLine.Visible> visibleMessages = accessor.getVisibleMessages();
        
        // Calculate which line we're hovering over
        int lineHeight = accessor.invokeGetLineHeight();
        int lineIndex = (int) (chatLineY / lineHeight);

        if (lineIndex < 0 || lineIndex >= visibleMessages.size()) {
            return null;
        }

        // Get the line at this position
        ChatHudLine.Visible line = visibleMessages.get(lineIndex);

        // Get the style at the x position within the line
        // We need to use a visitor to extract the style from OrderedText
        MinecraftClient client = MinecraftClient.getInstance();
        StyleHolder styleHolder = new StyleHolder();
        int targetX = (int) chatLineX;
        int[] currentX = {0};

        line.content().accept((index, style, codePoint) -> {
            // Get the width of this character using the text renderer
            String charString = new String(Character.toChars(codePoint));
            int width = client.textRenderer.getWidth(charString);
            if (currentX[0] <= targetX && targetX < currentX[0] + width) {
                styleHolder.style = style;
                return false; // Stop iteration
            }
            currentX[0] += width;
            return true; // Continue iteration
        });

        return styleHolder.style;
    }

    /**
     * Helper class to hold a style reference from inside a lambda
     */
    private static class StyleHolder {
        Style style = null;
    }

    /**
     * Convert screen X coordinate to chat line X coordinate.
     * This replaces the removed ChatHud.toChatLineX() method.
     *
     * @param chatHud The ChatHud instance
     * @param x Screen X coordinate
     * @return Chat line X coordinate
     */
    public static double toChatLineX(ChatHud chatHud, double x) {
        MixinChatHudInvoker accessor = (MixinChatHudInvoker) chatHud;
        MinecraftClient client = MinecraftClient.getInstance();
        
        double chatScale = accessor.invokeGetChatScale();
        double scaledX = x - 2.0;
        
        return scaledX / chatScale;
    }

    /**
     * Convert screen Y coordinate to chat line Y coordinate.
     * This replaces the removed ChatHud.toChatLineY() method.
     *
     * @param chatHud The ChatHud instance
     * @param y Screen Y coordinate
     * @return Chat line Y coordinate
     */
    public static double toChatLineY(ChatHud chatHud, double y) {
        MixinChatHudInvoker accessor = (MixinChatHudInvoker) chatHud;
        MinecraftClient client = MinecraftClient.getInstance();
        
        double chatScale = accessor.invokeGetChatScale();
        boolean focused = accessor.invokeIsChatFocused();
        
        // Calculate the Y offset
        double scaledY = y - 40.0;
        
        // If chat is focused, account for the input field height
        if (focused) {
            scaledY -= 14.0;
        }
        
        scaledY = -scaledY;
        
        return scaledY / chatScale;
    }

    /**
     * Check if a click on the chat should be handled.
     * This replaces the removed ChatHud.mouseClicked() method.
     *
     * @param chatHud The ChatHud instance
     * @param mouseX Mouse X coordinate
     * @param mouseY Mouse Y coordinate
     * @return true if the click is within chat bounds
     */
    public static boolean isWithinChatBounds(ChatHud chatHud, double mouseX, double mouseY) {
        if (chatHud == null) {
            return false;
        }

        MixinChatHudInvoker accessor = (MixinChatHudInvoker) chatHud;
        
        double chatLineX = toChatLineX(chatHud, mouseX);
        double chatLineY = toChatLineY(chatHud, mouseY);

        if (chatLineX < 0 || chatLineY < 0) {
            return false;
        }

        List<ChatHudLine.Visible> visibleMessages = accessor.getVisibleMessages();
        if (visibleMessages.isEmpty()) {
            return false;
        }

        int chatWidth = accessor.invokeGetWidth();
        int lineHeight = accessor.invokeGetLineHeight();
        int totalHeight = visibleMessages.size() * lineHeight;

        return chatLineX >= 0 && chatLineX <= chatWidth && chatLineY >= 0 && chatLineY <= totalHeight;
    }
}

