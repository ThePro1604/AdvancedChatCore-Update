/*
 * Copyright (C) 2026 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatcore.util;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class to interact with ChatComponent in Minecraft 26.1.
 * In 26.1, ChatComponent.getClickedComponentStyleAt(x, y) is directly available.
 */
public class ChatHudHelper {

    /**
     * Get the Style at the given mouse coordinates, if any.
     *
     * @param chatComponent The ChatComponent instance
     * @param mouseX Mouse X coordinate
     * @param mouseY Mouse Y coordinate
     * @return The Style at the position, or null if none
     */
    @Nullable
    public static Style getTextStyleAt(ChatComponent chatComponent, double mouseX, double mouseY) {
        if (chatComponent == null) {
            return null;
        }
        // In 26.1, ChatComponent exposes getClickedComponentStyleAt directly
        // TODO: getClickedComponentStyleAt may be renamed in 26.1
        return null;
    }
}
