/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatcore.gui;

import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import fi.dy.masa.malilib.gui.widgets.WidgetListBase;
import fi.dy.masa.malilib.gui.wrappers.TextFieldWrapper;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public abstract class WidgetConfigList<TYPE, WIDGET extends WidgetConfigListEntry<TYPE>>
        extends WidgetListBase<TYPE, WIDGET> {

    protected List<TextFieldWrapper<GuiTextFieldGeneric>> textFields = new ArrayList<>();

    public WidgetConfigList(
            int x,
            int y,
            int width,
            int height,
            ISelectionListener<TYPE> selectionListener,
            Screen parent) {
        super(x, y, width, height, selectionListener);
        this.browserEntryHeight = 22;
        this.setParent(parent);
    }

    public void resize(Minecraft mc, int width, int height) {
        this.width = width;
        this.height = height;
        this.clearElements();
        // TODO: clearAndInit malilib 26.1;
    }

    @Override
    protected void reCreateListEntryWidgets() {
        textFields.clear();
        super.reCreateListEntryWidgets();
    }

    public void addTextField(TextFieldWrapper<GuiTextFieldGeneric> Component) {
        textFields.add(Component);
    }

    protected void clearTextFieldFocus() {
        for (TextFieldWrapper<GuiTextFieldGeneric> field : this.textFields) {
            GuiTextFieldGeneric textField = field.textField();

            if (textField.isFocused()) {
                textField.setFocused(false);
                break;
            }
        }
    }

    @Override
    public boolean onMouseClicked(MouseButtonEvent click, boolean doubled) {
        clearTextFieldFocus();
        return super.onMouseClicked(click, doubled);
    }

    @Override
    public boolean onKeyTyped(KeyEvent input) {
        for (WidgetConfigListEntry<TYPE> widget : this.listWidgets) {
            if (widget.onKeyTyped(input)) {
                return true;
            }
        }
        return super.onKeyTyped(input);
    }
}
