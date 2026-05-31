/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatcore.chat;
import io.github.darkkronicle.advancedchatcore.AdvancedChatCore;

import io.github.darkkronicle.advancedchatcore.interfaces.AdvancedChatScreenSection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

/**
 * Handles the CommandSuggestions for the chat.
 * TODO: CommandSuggestions API changed in 26.1 - setWindowActive/refresh/render may be renamed
 */
@Environment(EnvType.CLIENT)
public class DefaultChatSuggestor extends AdvancedChatScreenSection {

    private CommandSuggestions commandSuggestor;

    public DefaultChatSuggestor(AdvancedChatScreen screen) {
        super(screen);
    }

    @Override
    public void onChatFieldUpdate(String chatText, String text) {
        // TODO: verify setWindowActive in CommandSuggestions 26.1
        // this.commandSuggestor.setWindowActive(!text.equals(getScreen().getOriginalChatText()));
        // this.commandSuggestor.refresh();
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        // TODO: verify CommandSuggestions.keyPressed in 26.1
        return false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTicks) {
        // TODO: verify CommandSuggestions.render/extractRenderState in 26.1
        // this.commandSuggestor.render(context, mouseX, mouseY);
    }

    @Override
    public void setChatFromHistory(String hist) {
        // TODO: verify setWindowActive in CommandSuggestions 26.1
        // this.commandSuggestor.setWindowActive(false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        // TODO: verify mouseScrolled in CommandSuggestions 26.1
        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        // TODO: verify mouseClicked in CommandSuggestions 26.1
        return false;
    }

    @Override
    public void resize(int width, int height) {
        // TODO: verify refresh in CommandSuggestions 26.1
        // this.commandSuggestor.refresh();
    }

    @Override
    public void initGui() {
        if (!AdvancedChatCore.CREATE_SUGGESTOR) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        AdvancedChatScreen screen = getScreen();
        // TODO: verify CommandSuggestions constructor signature in 26.1
        /*
        this.commandSuggestor =
                new CommandSuggestions(
                        client,
                        screen,
                        screen.chatField,
                        client.font,
                        false,
                        false,
                        1,
                        10,
                        true,
                        -805306368);
        this.commandSuggestor.refresh();
        */
    }
}
