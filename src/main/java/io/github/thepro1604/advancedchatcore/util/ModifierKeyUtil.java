package io.github.thepro1604.advancedchatcore.util;

import net.minecraft.client.Minecraft;

import com.mojang.blaze3d.platform.InputConstants;

public class ModifierKeyUtil {
    public static boolean hasControlDown() {
        if (net.minecraft.util.Util.getPlatform() == net.minecraft.util.Util.OS.OSX) {
            return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 343)
                    || InputConstants.isKeyDown(
                    Minecraft.getInstance().getWindow(),
                    347
            );
        } else {
            return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 341)
                    || InputConstants.isKeyDown(
                    Minecraft.getInstance().getWindow(),
                    345
            );
        }
    }

    public static boolean hasShiftDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 340)
                || InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 344);
    }

    public static boolean hasAltDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 342)
                || InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 346);
    }
}
