package me.coordshud.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(net.minecraft.class_329.class)
public abstract class InGameHudMixin {

    @ModifyVariable(
        method = "method_1752", 
        at = @At(value = "STORE"), 
        ordinal = 1
    )
    private int lockArmorBarY(int originalY) {
        return -49;
    }
}
