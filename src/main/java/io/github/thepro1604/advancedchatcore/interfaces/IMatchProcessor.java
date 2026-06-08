/*
 * Copyright (C) 2021-2022 thepro1604
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.thepro1604.advancedchatcore.interfaces;

import io.github.thepro1604.advancedchatcore.util.SearchResult;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * An interface to receive Component and matches to process.
 *
 * <p>Similar to {@link IMessageProcessor} but it takes matches and can return a {@link Result}
 */
public interface IMatchProcessor extends IMessageProcessor {
    /** Different outcome's the processor can have */
    enum Result {
        FAIL(false, true, false),
        PROCESSED(true, false, false),
        FORCE_FORWARD(true, true, true),
        FORCE_STOP(true, false, true);

        public final boolean success;
        public final boolean forward;
        public final boolean force;

        Result(boolean success, boolean forward, boolean force) {
            this.success = success;
            this.forward = forward;
            this.force = force;
        }

        public static Result getFromBool(boolean success) {
            if (!success) {
                return FAIL;
            }
            return PROCESSED;
        }
    }

    @Override
    default boolean process(Component text, Component unfiltered) {
        return processMatches(text, unfiltered, null).success;
    }

    /**
     * Process specific matches and return how the rest of the processors should be handled
     *
     * @param Component Final Component
     * @param unfiltered Unfiltered version of text. If not available null.
     * @param search {@link SearchResult} matches
     * @return The {@link Result} that the method performed
     */
    Result processMatches(
            Component text, @Nullable Component unfiltered, @Nullable SearchResult search);

    /**
     * Whether or not this processor should only trigger when matches are present. If false {@link
     * SearchResult} can be null.
     *
     * @return If this processor should only trigger when matches are present
     */
    default boolean matchesOnly() {
        return true;
    }
}
