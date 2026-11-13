package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class ExampleMod implements ClientModInitializer {

    private static KeyBinding menuKey;
    private static KeyBinding triggerKey;
    private static KeyBinding fullbrightKey;

    public static boolean triggerEnabled = false;
    public static boolean fullbrightEnabled = false;

    private final Random random = new Random();
    private long nextAttackDelay = 0;

    @Override
    public void onInitializeClient() {
        // Menyu ochish tugmasi ('M')
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.artic.menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "category.artic"
        ));

        // TriggerBot tezkor tugmasi ('R')
        triggerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.artic.triggerbot",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.artic"
        ));

        // Fullbright tezkor tugmasi ('B')
        fullbrightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.artic.fullbright",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.artic"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // 'M' bosilganda Artic Client menyusini ochish
            while (menuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ArticMenuScreen());
                }
            }

            // 'R' bosilganda TriggerBotni almashtirish
            while (triggerKey.wasPressed()) {
                triggerEnabled = !triggerEnabled;
                sendStatus(client, "TriggerBot", triggerEnabled);
            }

            // 'B' bosilganda Fullbrightni almashtirish
            while (fullbrightKey.wasPressed()) {
                fullbrightEnabled = !fullbrightEnabled;
                sendStatus(client, "Fullbright", fullbrightEnabled);
            }

            // Fullbright ishlashi
            if (client.options != null) {
                if (fullbrightEnabled) {
                    client.options.getGamma().setValue(100.0D);
                } else if (client.options.getGamma().getValue() > 1.0D) {
                    client.options.getGamma().setValue(1.0D);
                }
            }

            // TriggerBot ishlashi
            if (triggerEnabled && client.player != null && client.world != null) {
                executeTriggerBot(client);
            }
        });
    }

    private void sendStatus(MinecraftClient client, String name, boolean state) {
        if (client.player != null) {
            String status = state ? "§aON" : "§cOFF";
            client.player.sendMessage(Text.of("§b[Artic Client] §f" + name + ": " + status), true);
        }
    }

    private void executeTriggerBot(MinecraftClient client) {
        if (client.currentScreen != null) return;

        HitResult hitResult = client.crosshairTarget;
        if (hitResult == null || hitResult.getType() != HitResult.Type.ENTITY) return;

        Entity target = ((EntityHitResult) hitResult).getEntity();
        if (!(target instanceof PlayerEntity) || !target.isAlive() || target == client.player) return;
        if (client.player.distanceTo(target) > 3.0f) return;

        float requiredCooldown = 0.92f + (random.nextFloat() * 0.08f);
        if (client.player.getAttackCooldownProgress(0.5f) < requiredCooldown) return;
        if (System.currentTimeMillis() < nextAttackDelay) return;

        if (client.interactionManager != null) {
            client.interactionManager.attackEntity(client.player, target);
            client.player.swingHand(Hand.MAIN_HAND);
            nextAttackDelay = System.currentTimeMillis() + 30 + random.nextInt(60);
        }
    }

    // Artic Client Menyu oynasi (GUI)
    public static class ArticMenuScreen extends Screen {
        public ArticMenuScreen() {
            super(Text.of("Artic Client"));
        }

        @Override
        protected void init() {
            int centerX = this.width / 2;
            int centerY = this.height / 2;

            // TriggerBot tugmasi
            this.addDrawableChild(ButtonWidget.builder(getTriggerText(), button -> {
                triggerEnabled = !triggerEnabled;
                button.setMessage(getTriggerText());
            }).dimensions(centerX - 100, centerY - 25, 200, 20).build());

            // Fullbright tugmasi
            this.addDrawableChild(ButtonWidget.builder(getFullbrightText(), button -> {
                fullbrightEnabled = !fullbrightEnabled;
                button.setMessage(getFullbrightText());
            }).dimensions(centerX - 100, centerY + 5, 200, 20).build());

            // Yopish tugmasi
            this.addDrawableChild(ButtonWidget.builder(Text.of("§7Yopish (ESC)"), button -> {
                this.close();
            }).dimensions(centerX - 100, centerY + 40, 200, 20).build());
        }

        private Text getTriggerText() {
            return Text.of("TriggerBot [R]: " + (triggerEnabled ? "§aYOQILGAN" : "§cO'CHIRILGAN"));
        }

        private Text getFullbrightText() {
            return Text.of("Fullbright [B]: " + (fullbrightEnabled ? "§aYOQILGAN" : "§cO'CHIRILGAN"));
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            this.renderBackground(context, mouseX, mouseY, delta);
            super.render(context, mouseX, mouseY, delta);
            // Yuqorida Artic Client sarlavhasini chizish
            context.drawCenteredTextWithShadow(this.textRenderer, "§b§lARTIC CLIENT §7v1.0", this.width / 2, (this.height / 2) - 55, 0xFFFFFF);
        }

        @Override
        public boolean shouldPause() {
            return false; // Serverda o'yin to'xtab qolmasligi uchun
        }
    }
}
