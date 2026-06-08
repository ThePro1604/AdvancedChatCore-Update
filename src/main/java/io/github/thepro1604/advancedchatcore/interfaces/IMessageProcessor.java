/*
 * Copyright (C) 2021-2022 thepro1604
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.thepro1604.advancedchatcore.interfaces;

import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.Component;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

/** An interface for taking a Component and processing it. */
public interface IMessageProcessor extends IMessageFilter {
    @Deprecated
    @Override
    default Optional<Component> filter(Component text) {
        process(text, null);
        return Optional.empty();
    }

    boolean process(Component text, @Nullable Component unfiltered);

    default boolean process(Component text, @Nullable Component unfiltered, @Nullable MessageSignature signature, @Nullable GuiMessageTag tag) {
        return process(text, unfiltered);
    }
}
