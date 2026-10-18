package me.coordshud.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class CoordsHudClient implements ClientModInitializer {
    private static boolean enabled = true;

    private static final String[] NAMES  = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
    private static final String[] ARROWS = {"\u2191", "\u2197", "\u2192", "\u2198", "\u2193", "\u2199", "\u2190", "\u2196"};

    private static final int GOLD = 0xFFFFAA00;
    private static final int WHITE = 0xFFFFFFFF;

    @Override
    public void onInitializeClient() {
        KeyMapping toggle = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.coordshud.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, KeyMapping.Category.MISC));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (toggle.consumeClick()) enabled = !enabled;
        });

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("coordshud", "hud"),
                (graphics, tickCounter) -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (!enabled || mc.player == null || mc.level == null || mc.options.hideGui) return;

                    int x = (int) Math.floor(mc.player.getX());
                    int y = (int) Math.floor(mc.player.getY());
                    int z = (int) Math.floor(mc.player.getZ());

                    float yaw = ((mc.player.getYRot() % 360f) + 360f) % 360f;
                    int idx = Math.round(((yaw + 180f) % 360f) / 45f) % 8;

                    String label = "XYZ: ";
                    String coords = x + " " + y + " " + z;
                    String arrow = ARROWS[idx];
                    String dir = " " + NAMES[idx];
                    String gap = "   ";

                    int total = mc.font.width(label) + mc.font.width(coords) + mc.font.width(gap)
                            + mc.font.width(arrow) + mc.font.width(dir);
                    int px = (graphics.guiWidth() - total) / 2;
                    int py = graphics.guiHeight() - 64;

                    graphics.drawString(mc.font, label, px, py, GOLD, true);
                    px += mc.font.width(label);
                    graphics.drawString(mc.font, coords, px, py, WHITE, true);
                    px += mc.font.width(coords) + mc.font.width(gap);
                    graphics.drawString(mc.font, arrow, px, py, GOLD, true);
                    px += mc.font.width(arrow);
                    graphics.drawString(mc.font, dir, px, py, WHITE, true);
                });
    }
}
