/*
 * Copyright (C) 2021 thepro1604
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.thepro1604.advancedchatcore.interfaces;

import fi.dy.masa.malilib.util.StringUtils;

/** An interface for a translatable object */
public interface Translatable {
    /**
     * Translation key of the object
     *
     * @return Translation key
     */
    String getTranslationKey();

    /**
     * Translates the object
     *
     * @return Translated string
     */
    default String translate() {
        return StringUtils.translate(getTranslationKey());
    }
}
