package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
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

    private static KeyBinding toggleKey;
    private boolean isEnabled = false;
    private final Random random = new Random();
    private long nextAttackDelay = 0;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.triggerbot.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.triggerbot"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                isEnabled = !isEnabled;
                if (client.player != null) {
                    String status = isEnabled ? "§aYoqildi (ON)" : "§cO'chirildi (OFF)";
                    client.player.sendMessage(Text.of("§7[TriggerBot] " + status), true);
                }
            }

            if (isEnabled && client.player != null && client.world != null) {
                executeTriggerBot(client);
            }
        });
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
}
