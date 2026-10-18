package me.coordshud.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** Purely client-side: reads the local player's position, so it works on any server. */
public class CoordsHudClient implements ClientModInitializer {
    private static boolean enabled = true;
    private static int corner = 0; // 0 TL, 1 TR, 2 BL, 3 BR

    @Override
    public void onInitializeClient() {
        KeyMapping toggle = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.coordshud.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, KeyMapping.Category.MISC));
        KeyMapping cycle = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.coordshud.cycle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, KeyMapping.Category.MISC));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (toggle.consumeClick()) enabled = !enabled;
            while (cycle.consumeClick()) corner = (corner + 1) % 4;
        });

        HudElementRegistry.addLast(
                ResourceLocation.fromNamespaceAndPath("coordshud", "hud"),
                (graphics, tickCounter) -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (!enabled || mc.player == null || mc.level == null
                            || mc.options.hideGui || mc.getDebugOverlay().showDebugScreen()) return;

                    List<String> lines = new ArrayList<>();
                    lines.add(String.format("XYZ: %.1f / %.1f / %.1f",
                            mc.player.getX(), mc.player.getY(), mc.player.getZ()));
                    String dir = mc.player.getDirection().getName();
                    lines.add("Facing: " + dir.substring(0, 1).toUpperCase() + dir.substring(1));
                    mc.level.getBiome(mc.player.blockPosition()).unwrapKey().ifPresent(k -> {
                        ResourceLocation id = k.location();
                        lines.add("Biome: " + Component.translatable(
                                "biome." + id.getNamespace() + "." + id.getPath()).getString());
                    });
                    lines.add("Dim: " + mc.level.dimension().location().getPath());

                    int lh = mc.font.lineHeight + 2, pad = 4;
                    int w = 0;
                    for (String s : lines) w = Math.max(w, mc.font.width(s));
                    int sw = graphics.guiWidth(), sh = graphics.guiHeight();
                    int x = (corner % 2 == 0) ? pad : sw - w - pad;
                    int y = (corner < 2) ? pad : sh - lines.size() * lh - pad;
                    for (String s : lines) {
                        graphics.drawString(mc.font, s, x, y, 0xFFFFFFFF, true);
                        y += lh;
                    }
                });
    }
}
