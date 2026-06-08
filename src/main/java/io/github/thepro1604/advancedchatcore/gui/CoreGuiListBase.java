/*
 * Copyright (C) 2021 thepro1604
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.thepro1604.advancedchatcore.gui;

import fi.dy.masa.malilib.gui.GuiListBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;
import fi.dy.masa.malilib.gui.widgets.WidgetListBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListEntryBase;
import io.github.thepro1604.advancedchatcore.interfaces.IClosable;
import net.minecraft.client.Minecraft;

public abstract class CoreGuiListBase<
                TYPE,
                WIDGET extends WidgetListEntryBase<TYPE>,
                WIDGETLIST extends WidgetListBase<TYPE, WIDGET>>
        extends GuiListBase<TYPE, WIDGET, WIDGETLIST>
        implements ISelectionListener<TYPE>, IClosable {

    public CoreGuiListBase(int listX, int listY) {
        super(listX, listY);
    }

    public void resize(Minecraft mc, int width, int height) {
        this.width = width;
        this.height = height;
        this.clearElements();
        // TODO: clearAndInit renamed in malilib 26.1;
    }

    @Override
    protected WIDGETLIST createListWidget(int listX, int listY) {
        return null;
    }

    @Override
    protected int getBrowserWidth() {
        return this.width - 20;
    }

    @Override
    protected int getBrowserHeight() {
        return this.height - 6 - this.getListY();
    }

    @Override
    public void onSelectionChange(TYPE entry) {}

    @Override
    public void close(ButtonBase button) {
        this.closeGui(true);
    }
}
