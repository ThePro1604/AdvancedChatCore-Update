/*
 * Copyright (C) 2021-2022 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatcore.mixin;

import io.github.darkkronicle.advancedchatcore.chat.AdvancedChatScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public class MixinMinecraftClient {

    // Runtime confirmed signature: openChatScreen(ChatComponent$ChatMethod)
    @Inject(method = "openChatScreen",
            at = @At(value = "HEAD"), cancellable = true)
    public void openChatScreen(ChatComponent.ChatMethod method, CallbackInfo ci) {
        // method.prefix() returns "/" for COMMAND (slash key) or "" for MESSAGE (T key)
        Minecraft.getInstance().setScreen(new AdvancedChatScreen(method.prefix()));
        ci.cancel();
    }
}
