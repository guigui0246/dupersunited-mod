package com.vinzy.cataddons.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import static com.vinzy.cataddons.SharedVariables.randomQuote;

public class SoundUtils {
    public static void alertSound() {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(
                SoundEvents.BLOCK_AMETHYST_BLOCK_HIT, 10f, 10f //does sound even do anything above 1 vro
        ));
    }

    public static void alertMessage(String message) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            player.sendMessage(Text.literal(message), true);
            alertSound();
        }
    }
}
