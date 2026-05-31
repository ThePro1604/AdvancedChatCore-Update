package io.github.darkkronicle.advancedchatcore.gui;

import fi.dy.masa.malilib.render.GuiContext;
import fi.dy.masa.malilib.render.RenderUtils;
import net.minecraft.client.input.MouseButtonEvent;
import io.github.darkkronicle.advancedchatcore.util.Color;
import io.github.darkkronicle.advancedchatcore.util.Colors;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
// TODO: RenderPipelines import - verify 26.1 equivalent
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

public class IconButton extends CleanButton {

    @Setter
    @Getter
    private int padding;

    @Setter
    @Getter
    private Identifier icon;

    @Setter
    @Getter
    private int iconWidth;

    @Setter
    @Getter
    private int iconHeight;

    @Setter
    @Getter
    private Consumer<IconButton> onClick;

    @Getter
    @Setter
    private String onHover;

    public IconButton(int x, int y, int sideLength, int iconLength, Identifier icon, Consumer<IconButton> mouseClick) {
        this(x, y, sideLength, sideLength, iconLength, iconLength, icon, mouseClick);
    }

    public IconButton(int x, int y, int width, int height, int iconWidth, int iconHeight, Identifier icon, Consumer<IconButton> mouseClick) {
        this(x, y, width, height, 2, iconWidth, iconHeight, icon, mouseClick, null);
    }

    public IconButton(int x, int y, int width, int height, int padding, int iconWidth, int iconHeight, Identifier icon, Consumer<IconButton> mouseClick, String onHover) {
        super(x, y, width, height, null, null);
        this.padding = padding;
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
        this.icon = icon;
        this.onClick = mouseClick;
        this.onHover = onHover;
    }

    @Override
    public void render(GuiContext context, int mouseX, int mouseY, boolean unused) {
        GuiGraphicsExtractor drawContext = (GuiGraphicsExtractor) (Object) context.getGuiGraphics();
        int relMX = mouseX - x;
        int relMY = mouseY - y;
        hovered = relMX >= 0 && relMX <= width && relMY >= 0 && relMY <= height;

        Color plusBack = Colors.getInstance().getColorOrWhite("background").withAlpha(100);
        if (hovered) {
            plusBack = Colors.getInstance().getColorOrWhite("hover").withAlpha(plusBack.alpha());
        }

        RenderUtils.drawRect(x, y, width, height, plusBack.color());

        // TODO: blit API changed in 26.1 - needs RenderPipeline;

        if (hovered && onHover != null) {
            // TODO: drawCenteredString API changed in 26.1 - stub
        }
    }

    @Override
    protected boolean onMouseClickedImpl(MouseButtonEvent click, boolean doubled) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        onClick.accept(this);
        return true;
    }

}
