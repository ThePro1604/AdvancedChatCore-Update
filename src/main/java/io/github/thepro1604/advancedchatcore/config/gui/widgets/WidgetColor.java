/*
 * Copyright (C) 2021 thepro1604
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.thepro1604.advancedchatcore.config.gui.widgets;

import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.render.RenderUtils;
import fi.dy.masa.malilib.util.StringUtils;
import io.github.thepro1604.advancedchatcore.util.Color;
import io.github.thepro1604.advancedchatcore.util.Colors;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class WidgetColor extends GuiTextFieldGeneric {

    private int colorX;
    private Color currentColor;

    public WidgetColor(
            int x, int y, int width, int height, Color color, Font font) {
        super(x, y, width - 22, height, font);
        this.colorX = x + width - 20;
        this.currentColor = color;
        this.setValue(this.currentColor.getString());
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractWidgetRenderState(context, mouseX, mouseY, delta);
        int y = this.y;
        RenderUtils.drawRect(this.colorX, y, 19, 19, 0xFFFFFFFF);
        RenderUtils.drawRect(this.colorX + 1, y + 1, 17, 17, 0xFF000000);
        RenderUtils.drawRect(this.colorX + 2, y + 2, 15, 15, this.currentColor.color());
    }

    public void onWrite(String text) {
        getAndRefreshColor4f();
    }

    @Override
    public int getWidth() {
        return super.getWidth() + 22;
    }

    public Color getAndRefreshColor4f() {
        Optional<Color> color = Colors.getInstance().getColor(this.getValue());
        if (color.isPresent()) {
            this.currentColor = color.get();
            return this.currentColor;
        }
        this.currentColor = new Color(StringUtils.getColor(this.getValue(), 0));
        return this.currentColor;
    }
}
