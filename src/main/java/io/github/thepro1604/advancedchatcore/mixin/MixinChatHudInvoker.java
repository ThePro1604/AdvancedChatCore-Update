/*
 * Copyright (C) 2021 thepro1604
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.thepro1604.advancedchatcore.mixin;

import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(ChatComponent.class)
public interface MixinChatHudInvoker {
    // TODO: verify internal method names in ChatComponent 26.1
    // In 26.1 "addVisibleMessage" may be renamed; verify before use
    @Invoker("addVisibleMessage")
    void invokeAddVisibleMessage(GuiMessage message);

    @Invoker("addMessage")
    void invokeAddMessage(GuiMessage message);

    // In 26.1 the field is "trimmedMessages" of type List<GuiMessage.Line>
    @Accessor("trimmedMessages")
    List<GuiMessage.Line> getVisibleMessages();

    @Invoker("getWidth")
    int invokeGetWidth();

    @Invoker("getLineHeight")
    int invokeGetLineHeight();

    @Invoker("isChatFocused")
    boolean invokeIsChatFocused();

    @Invoker("getChatScale")
    double invokeGetChatScale();
}
