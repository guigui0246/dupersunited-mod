package com.vinzy.cataddons.features;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import static com.vinzy.cataddons.commands.CommandCat.sendMessage;

public class SetHand {
    public static void setHand(ItemStack stack) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int slot = mc.player.getInventory().getSelectedSlot();
        mc.player.getInventory().setStack(slot, stack);
        sendMessage("§aSet your hand to " + stack.getCount() + "x " + stack.getItem().getName().getString() + ".", true);
    }
}
