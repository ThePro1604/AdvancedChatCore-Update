package io.github.thepro1604.advancedchatcore.mixin;

import io.github.thepro1604.advancedchatcore.config.ConfigStorage;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiMessageTag.class)
public class MixinMessageIndicator {

    // TODO: verify method name "indicatorColor" in GuiMessageTag 26.1 (was GuiMessageTag in 1.21.11)
    @Inject(method = "indicatorColor", at = @At("HEAD"), cancellable = true)
    private void getColor(CallbackInfoReturnable<Integer> ci) {
        GuiMessageTag indicator = ((GuiMessageTag) (Object) this);
        String name = "System" /* TODO: verify loggedName/name method on GuiMessageTag 26.1 */;
        ci.setReturnValue(switch (name) {
            case "Modified" -> ConfigStorage.ChatScreen.MODIFIED.config.getColor().intValue;
            case "Filtered" -> ConfigStorage.ChatScreen.FILTERED.config.getColor().intValue;
            case "Not Secure" -> ConfigStorage.ChatScreen.NOT_SECURE.config.getColor().intValue;
            default -> // And "System"
                    ConfigStorage.ChatScreen.SYSTEM.config.getColor().intValue;
        });

    }

    // TODO: verify method name "icon" in GuiMessageTag 26.1
    @Inject(method = "icon", at = @At("HEAD"), cancellable = true)
    private void getIcon(CallbackInfoReturnable<GuiMessageTag.Icon> ci) {
        if (!ConfigStorage.ChatScreen.SHOW_CHAT_ICONS.config.getBooleanValue()) {
            ci.setReturnValue(null);
        }
    }

}
