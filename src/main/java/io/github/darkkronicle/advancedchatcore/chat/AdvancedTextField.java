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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.render.*;
import net.minecraft.command.CommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class AdvancedTextField extends TextFieldWidget {

    private final static int MAX_HISTORY = 50;

    /**
     * Stores the last saved snapshot of the box. This ensures that not every character update is
     * put in, but instead groups.
     */
    private String lastSaved = "";

    /** Snapshots of chat box */
    private final List<String> history = new ArrayList<>();

    private int focusedTicks = 0;
    private List<Text> renderLines = new ArrayList<>();
    private TextRenderer textRenderer;
    private String suggestion = null;
    private int maxLength = 32;
    private int selectionEnd;
    private int selectionStart;
    // TODO Split?
    private BiFunction<String, Integer, OrderedText> renderTextProvider = (string, firstCharacterIndex) -> {
        // Check if the string starts with "/" to enable command highlighting
        if (string.startsWith("/")) {
            return highlightCommand(string);
        }

        // Convert & color codes to § section symbols only when followed by valid formatting character
        // Valid characters: 0-9, a-f, k-o, r (color codes and formatting codes)
        String converted = string.replaceAll("&([0-9a-fk-or])", "§$1");
        Text text = Text.literal(converted);
        Text formatted = StyleFormatter.formatText(text);
        return formatted.asOrderedText();
    };

    private int historyIndex = -1;

    public AdvancedTextField(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        this(textRenderer, x, y, width, height, null, text);
    }

    public AdvancedTextField(
            TextRenderer textRenderer,
            int x,
            int y,
            int width,
            int height,
            @Nullable TextFieldWidget copyFrom,
            Text text) {
        super(textRenderer, x, y, width, height, copyFrom, text);
        history.add("");
        this.textRenderer = textRenderer;
        updateRender();
    }

    /**
     * Highlights command syntax based on Brigadier parsing
     * - Red for invalid commands
     * - Light reddish for valid commands
     * - Blue for arguments
     */
    private OrderedText highlightCommand(String input) {
        MinecraftClient client = MinecraftClient.getInstance();

        // If we don't have a network handler or command dispatcher, fall back to basic formatting
        if (client.getNetworkHandler() == null || client.getNetworkHandler().getCommandDispatcher() == null) {
            return Text.literal(input).asOrderedText();
        }

        // Create text builder for the result
        Text result;

        try {
            // Remove the leading "/" for parsing
            String command = input.substring(1);

            // Parse the command using Brigadier
            StringReader reader = new StringReader(command);
            ParseResults<ClientCommandSource> parseResults = client.getNetworkHandler()
                    .getCommandDispatcher()
                    .parse(reader, client.getNetworkHandler().getCommandSource());

            // Check if there are any parsed nodes (valid command path)
            boolean hasValidCommand = !parseResults.getContext().getNodes().isEmpty();

            // Colors for different parts
            int errorColor = 0xFF5555;      // Red for errors
            int commandColor = 0xFAB4B4;    // Light reddish for valid commands
            int argumentColor = 0x6EFAE0;   // Blue for arguments

            if (!hasValidCommand) {
                // Invalid command - color everything in red
                result = Text.literal(input).styled(style -> style.withColor(TextColor.fromRgb(errorColor)));
            } else {
                // Valid command - highlight parts differently
                MutableText mutableText = Text.literal("/").styled(style -> style.withColor(TextColor.fromRgb(commandColor)));

                // Get the command name (first word)
                int firstSpaceIndex = command.indexOf(' ');
                String commandName = firstSpaceIndex > 0 ? command.substring(0, firstSpaceIndex) : command;

                // Color the command name
                mutableText.append(Text.literal(commandName).styled(style -> style.withColor(TextColor.fromRgb(commandColor))));

                // Color the rest (arguments) in blue
                if (firstSpaceIndex > 0 && firstSpaceIndex < command.length()) {
                    String arguments = command.substring(firstSpaceIndex);
                    mutableText.append(Text.literal(arguments).styled(style -> style.withColor(TextColor.fromRgb(argumentColor))));
                }

                result = mutableText;
            }
        } catch (Exception e) {
            // If parsing fails, color as error
            int errorColor = 0xFF5555;
            result = Text.literal(input).styled(style -> style.withColor(TextColor.fromRgb(errorColor)));
        }

        return result.asOrderedText();
    }

    //@Override
    public void tick() {
        focusedTicks++;
    }

    @Override
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        super.setMaxLength(maxLength);
    }

    public static boolean isUndo(KeyInput input) {
        // Undo (Ctrl + Z)
        return input.key() == KeyCodes.KEY_Z && input.hasCtrl() && !input.hasAlt();
    }

    /** Triggers undo for the text box */
    public void undo() {
        // Save the current snapshot if it's been edited
        if (!this.lastSaved.equals(this.getText()) && historyIndex < 0) {
            addToHistory(getText());
        }
        // History index < 0 means not in the middle of undoing
        if (historyIndex < 0) {
            historyIndex = history.size() - 1;
        }
        // Check that we're not at index 0
        if (historyIndex != 0) {
            historyIndex--;
        }
        // Set the text but don't update
        setText(history.get(historyIndex), false);
    }

    public void redo() {
        if (historyIndex < 0 || historyIndex >= history.size() - 1) {
            // No stuff to redo...
            return;
        }
        historyIndex++;
        setText(history.get(historyIndex), false);
    }

    @Override
    public void write(String text) {
        super.write(text);
        updateHistory();
        updateRender();
    }

    @Override
    public void eraseCharacters(int characterOffset) {
        super.eraseCharacters(characterOffset);
        updateHistory();
        updateRender();
    }

    @Override
    public void setSuggestion(@Nullable String suggestion) {
        this.suggestion = suggestion;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {

        int renderY = getY() - (renderLines.size() - 1) * (textRenderer.fontHeight + 2);
        if (click.button() < renderY - 2 || click.y() > getY() + height + 2 || click.x() < getX() - 2 || click.x() > getX() + width + 4) {
            return false;
        }
        return super.mouseClicked(click, doubled);
    }


    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int color = 0xFFE0E0E0;
        int cursor = getCursor();
        int cursorRow = renderLines.size() - 1;
        boolean renderCursor = this.isFocused() && focusedTicks / 6 % 2 == 0;
        int renderY = getY() - (renderLines.size() - 1) * (textRenderer.fontHeight + 2);
        int endX = 0;
        int charCount = 0;
        int cursorX = -1;
        boolean selection = selectionStart != selectionEnd;
        boolean started = false;
        boolean ended = false;
        int selStart;
        int selEnd;
        if (this.selectionStart < this.selectionEnd) {
            selStart = this.selectionStart;
            selEnd = this.selectionEnd;
        } else {
            selStart = this.selectionEnd;
            selEnd = this.selectionStart;
        }
        int x = getX();
        int y = getY()    ;
        context.fill(getX() - 2, renderY - 2, getX() + width + 4, getY() + height + 4, ConfigStorage.ChatScreen.COLOR.config.getIntegerValue());
        for (int line = 0; line < renderLines.size(); line++) {
            Text text = renderLines.get(line);
            if (cursor >= charCount && cursor < text.getString().length() + charCount) {
                cursorX = textRenderer.getWidth(text.getString().substring(0, cursor - charCount));
                cursorRow = line;
            }
            endX = x + textRenderer.getWidth(text);
            context.drawTextWithShadow(textRenderer, text, x, renderY, color);
            if (selection) {
                if (!started && selStart >= charCount && selStart <= text.getString().length() + charCount) {
                    started = true;
                    int startX = textRenderer.getWidth(TextUtil.truncate(text, new StringMatch("", 0, selStart - charCount)));
                    if (selEnd > charCount && selEnd <= text.getString().length() + charCount) {
                        ended = true;
                        int sEndX = textRenderer.getWidth(TextUtil.truncate(text, new StringMatch("", 0, selEnd - charCount)));
                        drawSelectionHighlight(context, x + startX, renderY - 1, x + sEndX, renderY + textRenderer.fontHeight);
                    } else {
                        int sEndX = textRenderer.getWidth(text);
                        drawSelectionHighlight(context, x + startX, renderY - 1, x + sEndX, renderY + textRenderer.fontHeight);
                    }
                } else if (started && !ended) {
                    if (selEnd >= charCount && selEnd <= text.getString().length() + charCount) {
                        ended = true;
                        int sEndX = textRenderer.getWidth(TextUtil.truncate(text, new StringMatch("", 0, selEnd - charCount)));
                        drawSelectionHighlight(context, x, renderY - 1, x + sEndX, renderY + textRenderer.fontHeight);
                    } else {
                        int sEndX = textRenderer.getWidth(text);
                        drawSelectionHighlight(context, x, renderY - 1, x + sEndX, renderY + textRenderer.fontHeight);
                    }
                }
            }
            renderY += textRenderer.fontHeight + 2;
            charCount += text.getString().length();
        }
        if (cursorX < 0) {
            cursorX = endX;
        }
        boolean cursorAtEnd = getCursor() == getText().length();
        if (!cursorAtEnd && this.suggestion != null) {
            context.drawTextWithShadow(textRenderer, this.suggestion, endX - 1, y, -8355712);
        }
        if (renderCursor) {
            int cursorY = y - (renderLines.size() - 1 - cursorRow) * (textRenderer.fontHeight + 2);
            if (cursorAtEnd) {
                context.fill(cursorX, cursorY - 1, cursorX + 1, cursorY + 1 + this.textRenderer.fontHeight, -3092272);
            } else {
                context.drawTextWithShadow(textRenderer, "_", x + cursorX, cursorY, color);
            }
        }
    }

    private void drawSelectionHighlight(DrawContext context, int x1, int y1, int x2, int y2) {
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

        context.fill(RenderPipelines.GUI_TEXT_HIGHLIGHT, x1, y1, x2, y2, 0xFF0000FF);
    }

    @Override
    public void setSelectionStart(int cursor) {
        this.selectionStart = MathHelper.clamp(cursor, 0, getText().length());
        super.setSelectionStart(cursor);
    }

    @Override
    public void setSelectionEnd(int index) {
        int i = getText().length();
        this.selectionEnd = MathHelper.clamp(index, 0, i);
        super.setSelectionEnd(index);
    }

    @Override
    public SelectionType getType() {
        return super.getType();
    }

    /**
     * Sets the text for the text field
     *
     * @param text Text to set
     * @param update Updates the history
     */
    public void setText(String text, boolean update) {
        // Wrapper class for setText
        super.setText(text);
        if (update) {
            updateHistory();
        }
        updateRender();
    }

    @Override
    public void setText(String text) {
        setText(text, true);
    }

    private void updateRender() {
        OrderedText formatted = renderTextProvider.apply(getText(), 0);
        renderLines = StyleFormatter.wrapText(textRenderer, getWidth(), new TextBuilder().append(formatted).build());
    }

    private void updateHistory() {
        if (historyIndex >= 0) {
            // Remove all history after what has gone back
            pruneHistory(historyIndex + 1);
            historyIndex = -1;
        }
        // Check to see if it should log
        int dif = getText().length() - lastSaved.length();
        double sim = TextUtil.similarity(getText(), lastSaved);
        if (sim >= .3 && (dif < 5 && dif * -1 < 5) || (sim >= .9)) {
            return;
        }
        addToHistory(getText());
    }

    private void addToHistory(String text) {
        this.lastSaved = text;
        this.history.add(text);
        while (this.history.size() > MAX_HISTORY) {
            this.history.removeFirst();
        }
    }

    /**
     * Remove's all history past a certain index
     *
     * @param index Index to prune from. Non-inclusive.
     */
    private void pruneHistory(int index) {
        if (index == 0) {
            history.clear();
            return;
        }
        while (history.size() > index) {
            history.removeLast();
        }
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (!this.isActive()) {
            return false;
        }
        if (!isUndo(input)) {
            return super.keyPressed(input);
        }
        if (input.hasShift()) {
            redo();
        } else {
            undo();
        }
        return true;
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        // Crashes here because Text is null
    }

    @Override
    public void eraseWords(int wordOffset) {
        if (!this.getText().isEmpty()) {
            if (this.selectionEnd != this.selectionStart) {
                this.setText("");
            } else {
                this.eraseCharacters(this.getWordSkipPosition(wordOffset) - this.selectionStart);
            }
        }
    }
}
