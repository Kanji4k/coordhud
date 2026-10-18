package me.coordshud.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net.minecraft.client.gui.hud.InGameHud")
public abstract class InGameHudMixin {

    @ModifyVariable(
        method = "renderStatusBars", 
        at = @At(value = "STORE"), 
        ordinal = 1
    )
    private int lockArmorBarY(int originalY) {
        return -49;
    }
}
