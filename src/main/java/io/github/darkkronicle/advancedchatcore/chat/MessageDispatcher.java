/*
 * Copyright (C) 2021-2022 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatcore.chat;

import io.github.darkkronicle.advancedchatcore.interfaces.IMessageFilter;
import io.github.darkkronicle.advancedchatcore.interfaces.IMessageProcessor;
import io.github.darkkronicle.advancedchatcore.util.FindType;
import io.github.darkkronicle.advancedchatcore.util.SearchResult;
import io.github.darkkronicle.advancedchatcore.util.SearchUtils;
import io.github.darkkronicle.advancedchatcore.util.StringInsert;
import io.github.darkkronicle.advancedchatcore.util.StringMatch;
import io.github.darkkronicle.advancedchatcore.util.StyleFormatter;
import io.github.darkkronicle.advancedchatcore.util.TextUtil;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MutableComponent;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

/**
 * A class to handle chat events.
 *
 * <p>Different events and hooks can be registered in here.
 */
@Environment(EnvType.CLIENT)
public class MessageDispatcher {

    private static final MessageDispatcher INSTANCE = new MessageDispatcher();
    private ArrayList<IMessageProcessor> processors = new ArrayList<>();
    private ArrayList<IMessageFilter> preFilters = new ArrayList<>();

    public static MessageDispatcher getInstance() {
        return INSTANCE;
    }

    private MessageDispatcher() {
        registerPreFilter(msg -> Optional.of(StyleFormatter.formatText(msg)), -1);

        registerPreFilter(
                msg -> {
                    String string = msg.getString();
                    if (string.isEmpty()) {
                        return Optional.empty();
                    }
                    SearchResult search =
                            SearchResult.searchOf(
                                    string,
                                    "(http(s)?:\\/\\/.)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&\\/=]*)",
                                    FindType.REGEX);
                    if (search.size() == 0) {
                        return Optional.empty();
                    }
                    Map<StringMatch, StringInsert> insert = new HashMap<>();
                    for (StringMatch match : search.getMatches()) {
                        insert.put(
                                match,
                                (current, match1) -> {
                                    String url = match1.match;
                                    if (!SearchUtils.isMatch(
                                            match1.match, "(http(s)?:\\/\\/.)", FindType.REGEX)) {
                                        url = "https://" + url;
                                    }
                                    return Component.literal(match1.match).withStyle(current.getStyle().withClickEvent(new ClickEvent.OpenUrl(URI.create(url))));
                                });
                    }
                    Component result = TextUtil.replaceStrings(msg, insert);
                    return Optional.of(result);
                },
                -1);
        registerPreFilter(
                (IMessageProcessor)
                        (msg, orig) -> {
                            LogManager.getLogger()
                                    .info(
                                            "[CHAT] {}",
                                            msg.getString()
                                                    .replaceAll("\r", "\\\\r")
                                                    .replaceAll("\n", "\\\\n"));
                            return true;
                        },
                -1);
    }

    public void handleText(Component text, @Nullable MessageSignature signature, @Nullable GuiMessageTag tag) {
        boolean previouslyBlank = text.getString().isEmpty();
        text = preFilter(text, signature, tag);
        if (text.getString().isEmpty() && !previouslyBlank) {
            return;
        }
        process(text, signature, tag);
    }

    /**
     * Applies preFilters only and returns the result.
     * Used by Fabric message events (MODIFY_CHAT/MODIFY_GAME) to transform the message
     * before vanilla displays it. Returns the original if preFilters produce an empty result.
     */
    public Component applyPreFilters(Component text) {
        Component result = preFilter(text, null, null);
        return result.getString().isEmpty() ? text : result;
    }

    /**
     * Runs processors (history, etc.) on an already-displayed message.
     * Used by Fabric message events (CHAT/GAME) after vanilla has shown the message.
     */
    public void runProcessors(Component text, @Nullable GuiMessageTag tag) {
        process(text, null, tag);
    }

    private Component preFilter(Component text, @Nullable MessageSignature signature, @Nullable GuiMessageTag tag) {
        for (IMessageFilter f : preFilters) {
            Optional<Component> t = f.filter(text);
            if (t.isPresent()) {
                text = t.get();
            }
        }
        return text;
    }

    private void process(Component text, @Nullable MessageSignature signature, @Nullable GuiMessageTag tag) {
        for (IMessageFilter f : processors) {
            f.filter(text);
        }
    }

    public void registerPreFilter(IMessageFilter processor, int index) {
        if (index < 0) {
            index = preFilters.size();
        }
        if (!preFilters.contains(processor)) {
            preFilters.add(index, processor);
        }
    }

    public void register(IMessageProcessor processor, int index) {
        if (index < 0) {
            index = processors.size();
        }
        if (!processors.contains(processor)) {
            processors.add(index, processor);
        }
    }
}
