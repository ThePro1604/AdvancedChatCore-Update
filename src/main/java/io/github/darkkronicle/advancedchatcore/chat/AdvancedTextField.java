/*
 * Copyright (C) 2021 DarkKronicle
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.darkkronicle.advancedchatcore.chat;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import fi.dy.masa.malilib.util.KeyCodes;
import io.github.darkkronicle.advancedchatcore.config.ConfigStorage;
import io.github.darkkronicle.advancedchatcore.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.commands.CommandSource;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class AdvancedTextField extends EditBox {

    private final static int MAX_HISTORY = 50;

    private String lastSaved = "";

    private final List<String> history = new ArrayList<>();

    private int focusedTicks = 0;
    private List<Component> renderLines = new ArrayList<>();
    private Font font;
    private String suggestion = null;
    private int maxLength = 32;
    private int selectionEnd;
    private int selectionStart;
    private BiFunction<String, Integer, FormattedCharSequence> renderTextProvider = (string, firstCharacterIndex) -> {
        // Check if the string starts with "/" to enable command highlighting
        if (string.startsWith("/")) {
            return highlightCommand(string);
        }

        // Convert & color codes to § section symbols only when followed by valid formatting character
        String converted = string.replaceAll("&([0-9a-fk-or])", "§$1");
        Component text = Component.literal(converted);
        Component formatted = StyleFormatter.formatText(text);
        return formatted.getVisualOrderText();
    };

    private int historyIndex = -1;

    public AdvancedTextField(Font font, int x, int y, int width, int height, Component text) {
        this(font, x, y, width, height, null, text);
    }

    public AdvancedTextField(
            Font font,
            int x,
            int y,
            int width,
            int height,
            @Nullable EditBox copyFrom,
            Component text) {
        super(font, x, y, width, height, copyFrom, text);
        history.add("");
        this.font = font;
        updateRender();
    }

    /**
     * Highlights command syntax based on Brigadier parsing
     * - Red for invalid commands
     * - Light reddish for valid commands
     * - Blue for arguments
     */
    private FormattedCharSequence highlightCommand(String input) {
        Minecraft client = Minecraft.getInstance();

        if (client.getConnection() == null || client.getConnection().getCommands() == null) {
            return Component.literal(input).getVisualOrderText();
        }

        Component result;

        try {
            String command = input.substring(1);

            StringReader reader = new StringReader(command);
            ParseResults<ClientSuggestionProvider> parseResults = client.getConnection()
                    .getCommands()
                    .parse(reader, client.getConnection().getSuggestionsProvider());

            boolean hasValidCommand = !parseResults.getContext().getNodes().isEmpty();

            int invalidColor = ConfigStorage.ChatScreen.COMMAND_SYNTAX_INVALID.config.getIntegerValue();
            int validColor = ConfigStorage.ChatScreen.COMMAND_SYNTAX_VALID.config.getIntegerValue();
            int argumentColor = ConfigStorage.ChatScreen.COMMAND_SYNTAX_ARGUMENTS.config.getIntegerValue();

            if (!hasValidCommand) {
                result = Component.literal(input).withStyle(style -> style.withColor(TextColor.fromRgb(invalidColor)));
            } else {
                MutableComponent mutableText = Component.literal("/").withStyle(style -> style.withColor(TextColor.fromRgb(validColor)));

                int firstSpaceIndex = command.indexOf(' ');
                String commandName = firstSpaceIndex > 0 ? command.substring(0, firstSpaceIndex) : command;

                mutableText.append(Component.literal(commandName).withStyle(style -> style.withColor(TextColor.fromRgb(validColor))));

                if (firstSpaceIndex > 0 && firstSpaceIndex < command.length()) {
                    String arguments = command.substring(firstSpaceIndex);
                    mutableText.append(Component.literal(arguments).withStyle(style -> style.withColor(TextColor.fromRgb(argumentColor))));
                }

                result = mutableText;
            }
        } catch (Exception e) {
            int invalidColor = ConfigStorage.ChatScreen.COMMAND_SYNTAX_INVALID.config.getIntegerValue();
            result = Component.literal(input).withStyle(style -> style.withColor(TextColor.fromRgb(invalidColor)));
        }

        return result.getVisualOrderText();
    }

    public void tick() {
        focusedTicks++;
    }

    @Override
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        super.setMaxLength(maxLength);
    }

    public static boolean isUndo(KeyEvent input) {
        return input.key() == KeyCodes.KEY_Z && (input.modifiers() & org.lwjgl.glfw.GLFW.GLFW_MOD_CONTROL) != 0 && (input.modifiers() & org.lwjgl.glfw.GLFW.GLFW_MOD_ALT) == 0;
    }

    public void undo() {
        if (!this.lastSaved.equals(this.getText()) && historyIndex < 0) {
            addToHistory(getText());
        }
        if (historyIndex < 0) {
            historyIndex = history.size() - 1;
        }
        if (historyIndex != 0) {
            historyIndex--;
        }
        setText(history.get(historyIndex), false);
    }

    public void redo() {
        if (historyIndex < 0 || historyIndex >= history.size() - 1) {
            return;
        }
        historyIndex++;
        setText(history.get(historyIndex), false);
    }

    @Override
    public void insertText(String text) {
        super.insertText(text);
        updateHistory();
        updateRender();
    }

    @Override
    public void deleteChars(int num) {
        super.deleteChars(num);
        updateHistory();
        updateRender();
    }

    @Override
    public void setSuggestion(@Nullable String suggestion) {
        this.suggestion = suggestion;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        double mouseX = event.x();
        double mouseY = event.y();
        int renderY = getY() - (renderLines.size() - 1) * (font.lineHeight + 2);
        if (mouseY < renderY - 2 || mouseY > getY() + height + 2 || mouseX < getX() - 2 || mouseX > getX() + width + 4) {
            return false;
        }
        return super.mouseClicked(event, doubled);
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int color = 0xFFE0E0E0;
        int cursor = getCursorPosition();
        int cursorRow = renderLines.size() - 1;
        boolean renderCursor = this.isFocused() && focusedTicks / 6 % 2 == 0;
        int renderY = getY() - (renderLines.size() - 1) * (font.lineHeight + 2);
        int endX = 0;
        int charCount = 0;
        int cursorX = -1;
        // selectionStart = highlight anchor (shadow of EditBox highlightPos)
        // cursor         = current cursor position (from getCursorPosition())
        // Only draw a selection rectangle when they differ (i.e. user has selected text)
        int selStart = Math.min(selectionStart, cursor);
        int selEnd   = Math.max(selectionStart, cursor);
        boolean selection = selStart != selEnd;
        boolean started = false;
        boolean ended = false;
        int x = getX();
        int y = getY();
        context.fill(getX() - 2, renderY - 2, getX() + width + 4, getY() + height + 4, ConfigStorage.ChatScreen.COLOR.config.getIntegerValue());
        for (int line = 0; line < renderLines.size(); line++) {
            Component text = renderLines.get(line);
            if (cursor >= charCount && cursor < text.getString().length() + charCount) {
                cursorX = font.width(text.getString().substring(0, cursor - charCount));
                cursorRow = line;
            }
            endX = x + font.width(text);
            context.text(font, text, x, renderY, color);
            if (selection) {
                if (!started && selStart >= charCount && selStart <= text.getString().length() + charCount) {
                    started = true;
                    int startX = font.width(TextUtil.truncate(text, new StringMatch("", 0, selStart - charCount)));
                    if (selEnd > charCount && selEnd <= text.getString().length() + charCount) {
                        ended = true;
                        int sEndX = font.width(TextUtil.truncate(text, new StringMatch("", 0, selEnd - charCount)));
                        drawSelectionHighlight(context, x + startX, renderY - 1, x + sEndX, renderY + font.lineHeight);
                    } else {
                        int sEndX = font.width(text);
                        drawSelectionHighlight(context, x + startX, renderY - 1, x + sEndX, renderY + font.lineHeight);
                    }
                } else if (started && !ended) {
                    if (selEnd >= charCount && selEnd <= text.getString().length() + charCount) {
                        ended = true;
                        int sEndX = font.width(TextUtil.truncate(text, new StringMatch("", 0, selEnd - charCount)));
                        drawSelectionHighlight(context, x, renderY - 1, x + sEndX, renderY + font.lineHeight);
                    } else {
                        int sEndX = font.width(text);
                        drawSelectionHighlight(context, x, renderY - 1, x + sEndX, renderY + font.lineHeight);
                    }
                }
            }
            renderY += font.lineHeight + 2;
            charCount += text.getString().length();
        }
        if (cursorX < 0) {
            cursorX = endX;
        }
        boolean cursorAtEnd = getCursorPosition() == getValue().length();
        if (!cursorAtEnd && this.suggestion != null) {
            context.text(font, this.suggestion, endX - 1, y, -8355712);
        }
        if (renderCursor) {
            int cursorY = y - (renderLines.size() - 1 - cursorRow) * (font.lineHeight + 2);
            if (cursorAtEnd) {
                context.fill(cursorX, cursorY - 1, cursorX + 1, cursorY + 1 + this.font.lineHeight, -3092272);
            } else {
                context.text(font, "_", x + cursorX, cursorY, color);
            }
        }
    }

    private void drawSelectionHighlight(GuiGraphicsExtractor context, int x1, int y1, int x2, int y2) {
        int x = getX();
        int y = getY();
        int i;
        if (x1 < x2) {
            i = x1;
            x1 = x2;
            x2 = i;
        }
        if (y1 < y2) {
            i = y1;
            y1 = y2;
            y2 = i;
        }
        if (x2 > x + this.width) {
            x2 = x + this.width;
        }
        if (x1 > x + this.width) {
            x1 = x + this.width;
        }
        // TODO: verify fill with Component highlight in GuiGraphicsExtractor 26.1 (was RenderPipelines.GUI_TEXT_HIGHLIGHT)
        context.fillGradient(x1, y1, x2, y2, 0xFF0000FF, 0xFF0000FF);
    }

    @Override
    public void setHighlightPos(int cursor) {
        this.selectionStart = Mth.clamp(cursor, 0, getValue().length());
        super.setHighlightPos(cursor);
    }

    public void setSelectionEnd(int index) {
        int i = getValue().length();
        this.selectionEnd = Mth.clamp(index, 0, i);
    }


    public void setText(String text, boolean update) {
        super.setValue(text);
        if (update) {
            updateHistory();
        }
        updateRender();
    }

    @Override
    public void setValue(String text) {
        setText(text, true);
    }

    public String getText() {
        return getValue();
    }

    public void setText(String text) {
        setText(text, true);
    }

    private void updateRender() {
        FormattedCharSequence formatted = renderTextProvider.apply(getValue(), 0);
        renderLines = StyleFormatter.wrapText(font, getWidth(), new TextBuilder().append(formatted).build());
    }

    private void updateHistory() {
        if (historyIndex >= 0) {
            pruneHistory(historyIndex + 1);
            historyIndex = -1;
        }
        int dif = getValue().length() - lastSaved.length();
        double sim = TextUtil.similarity(getValue(), lastSaved);
        if (sim >= .3 && (dif < 5 && dif * -1 < 5) || (sim >= .9)) {
            return;
        }
        addToHistory(getValue());
    }

    private void addToHistory(String text) {
        this.lastSaved = text;
        this.history.add(text);
        while (this.history.size() > MAX_HISTORY) {
            this.history.removeFirst();
        }
    }

    private void pruneHistory(int index) {
        if (index == 0) {
            history.clear();
            return;
        }
        while (history.size() > index) {
            history.removeLast();
        }
    }

public boolean keyPressed(KeyEvent input) {
        if (!this.isActive()) {
            return false;
        }
        if (!isUndo(input)) {
            return super.keyPressed(input);
        }
        if ((input.modifiers() & org.lwjgl.glfw.GLFW.GLFW_MOD_SHIFT) != 0) {
            redo();
        } else {
            undo();
        }
        return true;
    }
@Override
    public void deleteWords(int wordOffset) {
        if (!this.getValue().isEmpty()) {
            if (this.selectionEnd != this.selectionStart) {
                this.setValue("");
            } else {
                this.deleteChars(this.getWordPosition(wordOffset) - this.selectionStart);
            }
        }
    }
}
