/*
 * Copyright (C) 2021 thepro1604
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.thepro1604.advancedchatcore.mixin;

import io.github.thepro1604.advancedchatcore.chat.ChatHistory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(KeyboardHandler.class)
public class MixinKeyboard {

    @Inject(
            method = "processF3",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;clear(Z)V"),
            require = 0)
    public void processF3Chat(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {
        // Make it so that history can still be cleared
        ChatHistory.getInstance().clearAll();
    }
}
