package me.coordshud.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class CoordsHudClient implements ClientModInitializer {
    private static boolean enabled = true;
    private static int corner = 0;

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
                Identifier.fromNamespaceAndPath("coordshud", "hud"),
                (graphics, tickCounter) -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (!enabled || mc.player == null || mc.level == null || mc.options.hideGui) return;

                    List<String> lines = new ArrayList<>();
                    lines.add(String.format("XYZ: %.1f / %.1f / %.1f",
                            mc.player.getX(), mc.player.getY(), mc.player.getZ()));
                    String dir = mc.player.getDirection().getName();
                    lines.add("Facing: " + dir.substring(0, 1).toUpperCase() + dir.substring(1));

                    String biome = mc.level.getBiome(mc.player.blockPosition()).getRegisteredName();
                    int c = biome.indexOf(':');
                    if (c > 0) {
                        lines.add("Biome: " + Component.translatable(
                                "biome." + biome.substring(0, c) + "." + biome.substring(c + 1)).getString());
                    }

                    String dim = mc.level.dimension().toString();
                    dim = dim.substring(dim.lastIndexOf(':') + 1).replace("]", "");
                    lines.add("Dim: " + dim);

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
