package io.github.darkkronicle.advancedchatcore.mixin;

// MixinClientPlayerEntity disabled in 26.1: tickNausea was renamed/removed in LocalPlayer.
// The original purpose was to prevent the chat screen closing during nausea/portal effects.
// TODO: find the new method name in LocalPlayer and re-enable.
public class MixinClientPlayerEntity {
}
