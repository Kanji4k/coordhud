package me.coordshud.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.minecraft.class_329")
public abstract class InGameHudMixin {

    @Inject(
        method = "renderArmor",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void fixArmorY(DrawContext context, Object player, int i, int j, int k, int x, int y, CallbackInfo ci) {
        // Keeps armor bar fixed at default height
    }
}
