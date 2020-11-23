/**
 * Copyright (c) 2020 DumbDogDiner <a href="dumbdogdiner.com">&lt;dumbdogdiner.com&gt;</a>. All rights reserved.
 * Licensed under the MIT license, see LICENSE for more information...
 */
package com.dumbdogdiner.stickyapi.bukkit.gui;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import lombok.Getter;

/**
 * This class is for creating new slots in an inventory GUI
*/
public class ClickableSlot {
    
    @Getter
    private ItemStack item;

    @Getter
    private final int slot;

    public ClickableSlot(ItemStack item, int slot){
        this.item = item;
        this.slot = slot;
    }

    public ClickableSlot(Material material, String name, int amount, int slot) {
        item = new ItemStack(material, amount);

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        //FIXME Do we actually need to re-set it tho? Didnt we get by ref??
        item.setItemMeta(meta);

        this.slot = slot;
    }

    public void execute(Runnable r) {
        new Thread(r).start();
    }

    public void setName(String s) {
        ItemMeta isM = this.item.getItemMeta();
        isM.setDisplayName(s);
        //FIXME Do we actually need to re-set it tho?
        item.setItemMeta(isM);
    }

    public String getName() {
        return item.getItemMeta().getDisplayName();
    }
}