package me.coordshud.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffects;

public class HeartsRow implements ClientModInitializer {
    private static Identifier sp(String name) {
        return Identifier.withDefaultNamespace("hud/heart/" + name);
    }

    @Override
    public void onInitializeClient() {
        HudElementRegistry.replaceElement(VanillaHudElements.HEALTH_BAR, original -> (graphics, tick) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.gameMode == null || !mc.gameMode.canHurtPlayer()) {
                original.render(graphics, tick);
                return;
            }

            float health = Math.max(0f, mc.player.getHealth());
            float max = Math.max(1f, mc.player.getMaxHealth());
            float abs = Math.max(0f, mc.player.getAbsorptionAmount());

            int redHearts = (int) Math.ceil(max / 2f);
            int goldHearts = (int) Math.ceil(abs / 2f);
            int total = redHearts + goldHearts;

            int left = graphics.guiWidth() / 2 - 91;
            int y = graphics.guiHeight() - 39;
            int step = total <= 10 ? 8 : Math.max(2, (81 - 9) / (total - 1));

            String prefix = "";
            if (mc.player.hasEffect(MobEffects.POISON)) prefix = "poisoned_";
            else if (mc.player.hasEffect(MobEffects.WITHER)) prefix = "withered_";

            for (int i = 0; i < redHearts; i++) {
                int x = left + i * step;
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sp("container"), x, y, 9, 9);
                if (health >= 2 * (i + 1)) {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sp(prefix + "full"), x, y, 9, 9);
                } else if (health >= 2 * i + 1) {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sp(prefix + "half"), x, y, 9, 9);
                }
            }

            for (int j = 0; j < goldHearts; j++) {
                int x = left + (redHearts + j) * step;
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sp("container"), x, y, 9, 9);
                if (abs >= 2 * (j + 1)) {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sp("absorbing_full"), x, y, 9, 9);
                } else if (abs >= 2 * j + 1) {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sp("absorbing_half"), x, y, 9, 9);
                }
            }
        });
    }
}
