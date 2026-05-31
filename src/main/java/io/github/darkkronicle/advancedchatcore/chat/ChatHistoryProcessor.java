/*
 * Copyright (C) 2021-2022 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatcore.chat;

import fi.dy.masa.malilib.util.data.Color4f;
import io.github.darkkronicle.advancedchatcore.AdvancedChatCore;
import io.github.darkkronicle.advancedchatcore.config.ConfigStorage;
import io.github.darkkronicle.advancedchatcore.interfaces.IMessageProcessor;
import io.github.darkkronicle.advancedchatcore.util.SearchUtils;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ChatHistoryProcessor implements IMessageProcessor {

    @Override
    public boolean process(Component text, @Nullable Component unfiltered) {
        return process(text, unfiltered, null, GuiMessageTag.system());
    }

    @Override
    public boolean process(Component text, @Nullable Component unfiltered, @Nullable MessageSignature signature, @Nullable GuiMessageTag tag) {
        if (unfiltered == null) {
            unfiltered = text;
        }

        LocalTime time = LocalTime.now();
        boolean showtime = ConfigStorage.General.SHOW_TIME.config.getBooleanValue();
        Component original = text.copy();
        if (showtime) {
            DateTimeFormatter format =
                    DateTimeFormatter.ofPattern(
                            ConfigStorage.General.TIME_FORMAT.config.getStringValue());
            String replaceFormat =
                    ConfigStorage.General.TIME_TEXT_FORMAT.config.getStringValue().replaceAll("&", "§");
            Color4f color = ConfigStorage.General.TIME_COLOR.config.getColor();
            Style style = Style.EMPTY.withColor(TextColor.fromRgb(color.getIntValue()));
            text.getSiblings().addFirst(Component.literal(replaceFormat.replaceAll("%TIME%", time.format(format))).withStyle(style));
        }

        MessageOwner player = SearchUtils.getAuthor(Minecraft.getInstance().getConnection(), unfiltered.getString());
        ChatMessage line = ChatMessage.builder()
                .displayText(text)
                .originalText(original)
                .owner(player)
                .id(0)
                .width(0)
                .creationTick(Minecraft.getInstance().gui.getGuiTicks())
                .time(time)
                .backgroundColor(null)
                .build();
        // In 26.1 vanilla handles HUD display via Fabric Message API.
        // We only track messages in ChatHistory here.
        ChatHistory.getInstance().add(line);
        return true;
    }
}
