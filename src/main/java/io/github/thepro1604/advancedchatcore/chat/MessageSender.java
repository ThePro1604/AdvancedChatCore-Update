/*
 * Copyright (C) 2022 thepro1604
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.thepro1604.advancedchatcore.chat;

import io.github.thepro1604.advancedchatcore.AdvancedChatCore;
import io.github.thepro1604.advancedchatcore.interfaces.IStringFilter;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MessageSender {

    private static final MessageSender INSTANCE = new MessageSender();
    private final Minecraft client = Minecraft.getInstance();

    public static MessageSender getInstance() {
        return INSTANCE;
    }

    private MessageSender() {}

    private final List<IStringFilter> filters = new ArrayList<>();

    public void addFilter(IStringFilter filter) {
        filters.add(filter);
    }

    public void addFilter(IStringFilter filter, int index) {
        filters.add(index, filter);
    }

    public void sendMessage(String string) {
        String unfiltered = string;
        for (IStringFilter filter : filters) {
            Optional<String> filtered = filter.filter(string);
            if (filtered.isPresent()) {
                string = filtered.get().strip();
            }
        }
        if (string.length() > 256) {
            string = string.substring(0, 256);
        }
        // TODO: verify method name "addRecentChat" in ChatComponent 26.1
        this.client.gui.getChat().addRecentChat(unfiltered);

        if (string.isEmpty()) {
            AdvancedChatCore.LOGGER.log(Level.WARN, "Blank message was attempted to be sent. " + unfiltered);
            return;
        }

        if (client.player != null) {
            if (string.startsWith("/")) {
                // TODO: verify method name "sendCommand" on ClientPacketListener in 26.1
                this.client.getConnection().sendCommand(string.substring(1));
            } else {
                // TODO: verify method name "sendChat" on ClientPacketListener in 26.1
                this.client.getConnection().sendChat(string);
            }
        }
    }
}
