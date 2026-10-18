package me.coordshud.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class CoordsHudClient implements ClientModInitializer {
    private static boolean enabled = true;
    private static boolean editMode = false;
    private static int offX = 0, offY = 0;
    private static int hold = 0;
    private static Path cfg;

    private static final String[] NAMES  = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
    private static final String[] ARROWS = {"\u2191", "\u2197", "\u2192", "\u2198", "\u2193", "\u2199", "\u2190", "\u2196"};

    private static final int GOLD = 0xFFFFAA00;
    private static final int WHITE = 0xFFFFFFFF;

    private static KeyMapping key(String name, int code) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping(
                name, InputConstants.Type.KEYSYM, code, KeyMapping.Category.MISC));
    }

    @Override
    public void onInitializeClient() {
        cfg = FabricLoader.getInstance().getConfigDir().resolve("coordshud.properties");
        load();

        KeyMapping toggle = key("key.coordshud.toggle", GLFW.GLFW_KEY_H);
        KeyMapping edit = key("key.coordshud.edit", GLFW.GLFW_KEY_K);
        KeyMapping reset = key("key.coordshud.reset", GLFW.GLFW_KEY_HOME);
        KeyMapping up = key("key.coordshud.up", GLFW.GLFW_KEY_UP);
        KeyMapping down = key("key.coordshud.down", GLFW.GLFW_KEY_DOWN);
        KeyMapping left = key("key.coordshud.left", GLFW.GLFW_KEY_LEFT);
        KeyMapping right = key("key.coordshud.right", GLFW.GLFW_KEY_RIGHT);

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (toggle.consumeClick()) enabled = !enabled;
            while (edit.consumeClick()) {
                editMode = !editMode;
                hold = 0;
                if (editMode) enabled = true; else save();
            }
            while (reset.consumeClick()) {
                if (editMode) { offX = 0; offY = 0; }
            }
            while (up.consumeClick()) { }
            while (down.consumeClick()) { }
            while (left.consumeClick()) { }
            while (right.consumeClick()) { }

            if (editMode && mc.screen == null) {
                int dx = 0, dy = 0;
                if (left.isDown()) dx -= 1;
                if (right.isDown()) dx += 1;
                if (up.isDown()) dy -= 1;
                if (down.isDown()) dy += 1;
                if (dx != 0 || dy != 0) {
                    hold++;
                    int step = 1 + hold / 6;
                    offX = Math.max(-3000, Math.min(3000, offX + dx * step));
                    offY = Math.max(-3000, Math.min(3000, offY + dy * step));
                } else {
                    hold = 0;
                }
            }
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
                    int sw = graphics.guiWidth(), sh = graphics.guiHeight();
                    int px = (sw - total) / 2 + offX;
                    int py = sh - 64 + offY;
                    px = Math.max(0, Math.min(sw - total, px));
                    py = Math.max(0, Math.min(sh - mc.font.lineHeight, py));

                    if (editMode) {
                        graphics.fill(px - 3, py - 3, px + total + 3, py + mc.font.lineHeight + 2, 0x66000000);
                        String hint = "Arrows: move   Home: reset   K: save";
                        graphics.drawString(mc.font, hint, (sw - mc.font.width(hint)) / 2, 6, GOLD, true);
                    }

                    int cx = px;
                    graphics.drawString(mc.font, label, cx, py, GOLD, true);
                    cx += mc.font.width(label);
                    graphics.drawString(mc.font, coords, cx, py, WHITE, true);
                    cx += mc.font.width(coords) + mc.font.width(gap);
                    graphics.drawString(mc.font, arrow, cx, py, GOLD, true);
                    cx += mc.font.width(arrow);
                    graphics.drawString(mc.font, dir, cx, py, WHITE, true);
                });
    }

    private static void load() {
        try {
            if (Files.exists(cfg)) {
                Properties p = new Properties();
                try (InputStream in = Files.newInputStream(cfg)) { p.load(in); }
                offX = Integer.parseInt(p.getProperty("offsetX", "0"));
                offY = Integer.parseInt(p.getProperty("offsetY", "0"));
            }
        } catch (Exception ignored) { }
    }

    private static void save() {
        try {
            Properties p = new Properties();
            p.setProperty("offsetX", Integer.toString(offX));
            p.setProperty("offsetY", Integer.toString(offY));
            try (OutputStream out = Files.newOutputStream(cfg)) { p.store(out, "Coords HUD position"); }
        } catch (Exception ignored) { }
    }
}
