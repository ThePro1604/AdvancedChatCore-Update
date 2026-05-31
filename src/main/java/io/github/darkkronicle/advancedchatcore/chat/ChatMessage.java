/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatcore.chat;

import io.github.darkkronicle.advancedchatcore.util.Color;
import io.github.darkkronicle.advancedchatcore.util.StyleFormatter;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.jetbrains.annotations.Nullable;

/** A message from chat with data stored within it. */
@Environment(EnvType.CLIENT)
@Data
public class ChatMessage {

    protected int creationTick;
    protected Component displayText;
    protected Component originalText;
    protected int id;
    protected LocalTime time;
    protected Color backgroundColor;
    protected int stacks;
    protected UUID uuid;
    @Nullable protected MessageOwner owner;
    protected List<AdvancedChatLine> lines;
    @Nullable protected MessageSignature signature;
    protected GuiMessageTag indicator;

    public void setDisplayText(Component text, int width) {
        this.displayText = text;
        formatChildren(width);
    }

    public ChatMessage shallowClone(int width) {
        ChatMessage message =
                new ChatMessage(
                        creationTick, displayText, originalText, id, time, backgroundColor,
                        width, owner, signature, indicator);
        message.setStacks(getStacks());
        return message;
    }

    @Data
    public static class AdvancedChatLine {

        private Component text;
        private final ChatMessage parent;
        private int width;

        private AdvancedChatLine(ChatMessage parent, Component text) {
            this.parent = parent;
            this.text = text;
            // TODO: verify "font" field name in Minecraft 26.1
            this.width = Minecraft.getInstance().font.width(text);
        }

        @Override
        public String toString() {
            return ("AdvancedChatLine{" + "text=" + text + ", width=" + width + '}');
        }
    }

    @Builder
    protected ChatMessage(
            int creationTick,
            Component displayText,
            Component originalText,
            int id,
            LocalTime time,
            Color backgroundColor,
            int width,
            MessageOwner owner,
            @Nullable MessageSignature signature,
            @Nullable GuiMessageTag indicator) {
        this.creationTick = creationTick;
        this.displayText = displayText;
        this.id = id;
        this.time = time;
        this.backgroundColor = backgroundColor;
        this.stacks = 0;
        this.uuid = UUID.randomUUID();
        this.owner = owner;
        this.originalText = originalText == null ? displayText : originalText;
        this.signature = signature;
        // TODO: verify GuiMessageTag.system() method in 26.1
        this.indicator = indicator == null ? GuiMessageTag.system() : indicator;
        formatChildren(width);
    }

    public void formatChildren(int width) {
        this.lines = new ArrayList<>();
        if (width == 0) {
            this.lines.add(new AdvancedChatLine(this, displayText));
        } else {
            // TODO: verify "font" field name in Minecraft 26.1
            for (Component t : StyleFormatter.wrapText(Minecraft.getInstance().font, width, displayText)) {
                this.lines.add(new AdvancedChatLine(this, t));
            }
        }
    }

    public boolean isSimilar(ChatMessage message) {
        return message.getOriginalText().getString().equals(this.getOriginalText().getString());
    }

    public int getLineCount() {
        return this.lines.size();
    }
}
