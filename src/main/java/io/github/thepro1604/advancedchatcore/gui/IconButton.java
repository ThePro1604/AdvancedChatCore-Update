package io.github.thepro1604.advancedchatcore.gui;

import fi.dy.masa.malilib.render.GuiContext;
import io.github.thepro1604.advancedchatcore.util.Color;
import io.github.thepro1604.advancedchatcore.util.Colors;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;

import java.util.function.Consumer;

public class IconButton extends CleanButton {

    @Setter @Getter private int padding;
    /**
     * Sprite ID for the icon — must be registered in the GUI atlas.
     * In 26.1, place the texture at assets/<namespace>/textures/gui/sprites/<path>.png
     * and use Identifier.fromNamespaceAndPath(namespace, path) as the sprite ID.
     */
    @Setter @Getter private Identifier icon;
    @Setter @Getter private int iconWidth;
    @Setter @Getter private int iconHeight;
    @Setter @Getter private Consumer<IconButton> onClick;
    @Getter @Setter private String onHover;

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
        GuiGraphicsExtractor drawContext = (GuiGraphicsExtractor)(Object) context.getGuiGraphics();
        int relMX = mouseX - x;
        int relMY = mouseY - y;
        hovered = relMX >= 0 && relMX <= width && relMY >= 0 && relMY <= height;

        Color bg = Colors.getInstance().getColorOrWhite("background").withAlpha(150);
        if (hovered) {
            bg = Colors.getInstance().getColorOrWhite("hover").withAlpha(200);
        }

        // Background fill
        drawContext.fill(x, y, x + width, y + height, bg.color());

        // Icon sprite via blitSprite — in 26.1, sprites must be in textures/gui/sprites/.
        // toSpriteId() converts legacy full-path identifiers to sprite IDs automatically.
        if (icon != null) {
            int destSize = Math.max(1, width - 2 * padding);
            drawContext.blitSprite(RenderPipelines.GUI_TEXTURED, toSpriteId(icon),
                    x + padding, y + padding,
                    destSize, destSize);
        }
    }

    /**
     * Converts a legacy full-texture-path Identifier to a sprite ID for 26.1's GUI atlas.
     * In 26.1, sprites are auto-loaded from assets/<ns>/textures/gui/sprites/<path>.png
     * and referenced by sprite ID <ns>:<path> (no prefix, no extension).
     *
     * Examples:
     *   "advancedchatcore:textures/gui/settings.png" → "advancedchatcore:settings"
     *   "advancedchatfilters:textures/gui/filter.png" → "advancedchatfilters:filter"
     *   "mod:settings" → "mod:settings" (already a sprite ID, unchanged)
     */
    private static Identifier toSpriteId(Identifier id) {
        String path = id.getPath();
        if (!path.endsWith(".png")) {
            return id; // Already a sprite ID
        }
        // Strip .png extension
        path = path.substring(0, path.length() - 4);
        // Strip common texture path prefixes
        if (path.startsWith("textures/gui/sprites/")) {
            path = path.substring("textures/gui/sprites/".length());
        } else if (path.startsWith("textures/gui/")) {
            path = path.substring("textures/gui/".length());
        } else if (path.startsWith("textures/")) {
            path = path.substring("textures/".length());
        }
        return Identifier.fromNamespaceAndPath(id.getNamespace(), path);
    }

    @Override
    protected boolean onMouseClickedImpl(MouseButtonEvent click, boolean doubled) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        onClick.accept(this);
        return true;
    }
}
