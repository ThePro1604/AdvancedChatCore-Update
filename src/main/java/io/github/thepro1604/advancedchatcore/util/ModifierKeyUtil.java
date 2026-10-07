package io.github.thepro1604.advancedchatcore.util;

import com.mojang.blaze3d.platform.InputConstants;

public class ModifierKeyUtil {
    public static boolean hasControlDown() {
        if (net.minecraft.util.Util.getPlatform() == net.minecraft.util.Util.OS.OSX) {
            return InputConstants.isKeyDown(InputConstants.KEY_LGUI)
                    || InputConstants.isKeyDown(InputConstants.KEY_RGUI);
        } else {
            return InputConstants.isKeyDown(InputConstants.KEY_LCONTROL)
                    || InputConstants.isKeyDown(InputConstants.KEY_RCONTROL);
        }
    }

    public static boolean hasShiftDown() {
        return InputConstants.isKeyDown(InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(InputConstants.KEY_RSHIFT);
    }

    public static boolean hasAltDown() {
        return InputConstants.isKeyDown(InputConstants.KEY_LALT)
                || InputConstants.isKeyDown(InputConstants.KEY_RALT);
    }
}
