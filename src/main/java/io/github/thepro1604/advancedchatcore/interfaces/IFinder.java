/*
 * Copyright (C) 2021 thepro1604
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.thepro1604.advancedchatcore.interfaces;

import io.github.thepro1604.advancedchatcore.util.StringMatch;
import net.minecraft.network.chat.Component;

import java.util.List;

public interface IFinder {

    boolean isMatch(String input, String toMatch);

    default boolean isMatch(Component input, String toMatch) {
        return isMatch(input.getString(), toMatch);
    }

    List<StringMatch> getMatches(String input, String toMatch);

    default List<StringMatch> getMatches(Component input, String toMatch) {
        return getMatches(input.getString(), toMatch);
    }
}
