/*
 * Copyright (C) 2021 thepro1604
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.thepro1604.advancedchatcore.config.gui.widgets;

import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import io.github.thepro1604.advancedchatcore.util.FindType;
import io.github.thepro1604.advancedchatcore.util.SearchUtils;
import io.github.thepro1604.advancedchatcore.util.StringMatch;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.Font;

public class WidgetIntBox extends GuiTextFieldGeneric {

    @Setter @Getter private Runnable apply = null;

    public WidgetIntBox(int x, int y, int width, int height, Font font) {
        // TODO: verify GuiTextFieldGeneric constructor in malilib 26.1
        super(x, y, width, height, font);
        // TODO: verify setTextPredicate in malilib 26.1 (may be renamed or removed)
        // TODO: verify setDrawsBackground in malilib 26.1
    }

    public Integer getInt() {
        String text = "" /* TODO: getText() malilib 26.1 */;
        if (text == null || text.length() == 0) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            Optional<List<StringMatch>> omatches =
                    SearchUtils.findMatches(text, "[0-9]+", FindType.REGEX);
            if (!omatches.isPresent()) {
                return null;
            }
            for (StringMatch m : omatches.get()) {
                try {
                    return Integer.parseInt(m.match);
                } catch (NumberFormatException err) {
                    return null;
                }
            }
        }
        return null;
    }
}
