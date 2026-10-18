package me.coordshud.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "net.minecraft.client.gui.hud.InGameHud")
public abstract class InGameHudMixin {
}
