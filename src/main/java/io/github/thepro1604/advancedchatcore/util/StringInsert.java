package io.github.thepro1604.advancedchatcore.util;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;

/**
 * An interface to provide a way to get the Component that should be replaced based off of the
 * current {@link Component} and the current {@link StringMatch}
 */
public interface StringInsert {
    /**
     * Return's the {@link MutableText} that should be inserted.
     *
     * @param current The current {@link Component}
     * @param match The current {@link StringMatch}
     * @return
     */
    MutableComponent getText(Component current, StringMatch match);
}