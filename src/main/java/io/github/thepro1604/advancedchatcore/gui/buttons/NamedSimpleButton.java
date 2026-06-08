/*
 * Copyright (C) 2021 thepro1604
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.thepro1604.advancedchatcore.gui.buttons;
import net.minecraft.network.chat.Component;

import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.util.StringUtils;

public class NamedSimpleButton extends ButtonGeneric {

    public NamedSimpleButton(int x, int y, String text) {
        this(x, y, text, true);
    }

    public NamedSimpleButton(int x, int y, String text, boolean alineLeft) {
        super(x, y, 5, 20, text);
        setWidth(StringUtils.getStringWidth(text) + 10);
        if (!alineLeft) {
            setX(this.x - this.width);
        }
    }
}
