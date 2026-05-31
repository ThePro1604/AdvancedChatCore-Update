/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatcore.mixin;

import io.github.darkkronicle.advancedchatcore.chat.AdvancedChatScreen;
import io.github.darkkronicle.advancedchatcore.chat.MessageDispatcher;
import io.github.darkkronicle.advancedchatcore.config.ConfigStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChatComponent.class, priority = 1050)
public class MixinChatHud {

    @Shadow @Final private Minecraft minecraft;

    // Prevent re-entering our injections when we re-call after processing
    private static final ThreadLocal<Boolean> PROCESSING = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private Component processMessage(Component message, @Nullable MessageSignature sig, @Nullable GuiMessageTag tag) {
        // applyPreFilters always returns a new MutableComponent (StyleFormatter is first filter)
        Component modified = MessageDispatcher.getInstance().applyPreFilters(message);
        // runProcessors mutates modified (prepends timestamp to siblings) and adds to ChatHistory
        MessageDispatcher.getInstance().runProcessors(modified, tag);
        return modified;
    }

    // Confirmed signature from Minecraft 26.1.2 jar: addPlayerMessage(Component, MessageSignature, GuiMessageTag)
    @Inject(
            method = "addPlayerMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void onAddPlayerMessage(Component message, @Nullable MessageSignature sig, @Nullable GuiMessageTag tag, CallbackInfo ci) {
        if (PROCESSING.get()) return;
        PROCESSING.set(true);
        try {
            Component modified = processMessage(message, sig, tag);
            ((ChatComponent)(Object)this).addPlayerMessage(modified, sig, tag);
            ci.cancel();
        } finally {
            PROCESSING.set(false);
        }
    }

    // Confirmed signature from Minecraft 26.1.2 jar: addServerSystemMessage(Component) — one param
    @Inject(
            method = "addServerSystemMessage(Lnet/minecraft/network/chat/Component;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void onAddServerSystemMessage(Component message, CallbackInfo ci) {
        if (PROCESSING.get()) return;
        PROCESSING.set(true);
        try {
            Component modified = processMessage(message, null, null);
            ((ChatComponent)(Object)this).addServerSystemMessage(modified);
            ci.cancel();
        } finally {
            PROCESSING.set(false);
        }
    }

    // Confirmed signature from Minecraft 26.1.2 jar: addClientSystemMessage(Component) — one param
    @Inject(
            method = "addClientSystemMessage(Lnet/minecraft/network/chat/Component;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void onAddClientSystemMessage(Component message, CallbackInfo ci) {
        if (PROCESSING.get()) return;
        PROCESSING.set(true);
        try {
            Component modified = processMessage(message, null, null);
            ((ChatComponent)(Object)this).addClientSystemMessage(modified);
            ci.cancel();
        } finally {
            PROCESSING.set(false);
        }
    }

    // clearMessages is the real method name in 26.1 (not "clear")
    @Inject(method = "clearMessages", at = @At("HEAD"), cancellable = true, require = 0)
    private void clearMessages(boolean clearTextHistory, CallbackInfo ci) {
        if (!clearTextHistory) {
            return;
        }
        if (!ConfigStorage.General.CLEAR_ON_DISCONNECT.config.getBooleanValue()) {
            ci.cancel();
        }
    }

    @Inject(method = "isChatFocused", at = @At("HEAD"), cancellable = true, require = 0)
    private void isChatFocused(CallbackInfoReturnable<Boolean> ci) {
        ci.setReturnValue(AdvancedChatScreen.PERMANENT_FOCUS || minecraft.screen instanceof AdvancedChatScreen);
    }
}
