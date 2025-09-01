package de.flori.chestShop.utils;

import de.flori.chestShop.ChestShopPlugin;
import de.flori.chestShop.models.Shop;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HologramUtil {
    
    private static final Map<String, ArmorStand> textHolograms = new HashMap<>();
    private static final Map<String, Item> itemHolograms = new HashMap<>();
    private static final Map<String, ArmorStand> priceHolograms = new HashMap<>();
    
    /**
     * Creates or updates a holographic display for a shop
     */
    public static void createShopHologram(Shop shop, ChestShopPlugin plugin) {
        if (!plugin.getConfigManager().getConfig().getBoolean("holograms.enabled", true)) {
            return;
        }
        
        removeShopHologram(shop.getId());
        
        Location chestLoc = shop.getChestLocation().clone().add(0.5, 1.5, 0.5);
        
        // Create text hologram (shop info)
        if (plugin.getConfigManager().getConfig().getBoolean("holograms.show-text", true)) {
            ArmorStand textStand = createTextHologram(chestLoc.clone().add(0, 0.5, 0), 
                formatShopText(shop, plugin), shop.getId() + "_text");
            textHolograms.put(shop.getId(), textStand);
        }
        
        // Create item hologram (floating item)
        if (plugin.getConfigManager().getConfig().getBoolean("holograms.show-item", true)) {
            Item itemDisplay = createItemHologram(chestLoc.clone(), 
                new ItemStack(shop.getItem(), shop.getAmount()), shop.getId() + "_item");
            itemHolograms.put(shop.getId(), itemDisplay);
        }
        
        // Create price hologram
        if (plugin.getConfigManager().getConfig().getBoolean("holograms.show-prices", true)) {
            ArmorStand priceStand = createTextHologram(chestLoc.clone().add(0, -0.5, 0),
                formatPriceText(shop, plugin), shop.getId() + "_price");
            priceHolograms.put(shop.getId(), priceStand);
        }
    }
    
    /**
     * Removes holographic display for a shop
     */
    public static void removeShopHologram(String shopId) {
        // Remove text hologram
        ArmorStand textStand = textHolograms.remove(shopId);
        if (textStand != null && !textStand.isDead()) {
            textStand.remove();
        }
        
        // Remove item hologram
        Item itemDisplay = itemHolograms.remove(shopId);
        if (itemDisplay != null && !itemDisplay.isDead()) {
            itemDisplay.remove();
        }
        
        // Remove price hologram
        ArmorStand priceStand = priceHolograms.remove(shopId);
        if (priceStand != null && !priceStand.isDead()) {
            priceStand.remove();
        }
    }
    
    /**
     * Updates holographic display for a shop
     */
    public static void updateShopHologram(Shop shop, ChestShopPlugin plugin) {
        if (!plugin.getConfigManager().getConfig().getBoolean("holograms.enabled", true)) {
            return;
        }
        
        // Update text hologram
        ArmorStand textStand = textHolograms.get(shop.getId());
        if (textStand != null && !textStand.isDead()) {
            textStand.setCustomName(formatShopText(shop, plugin));
        }
        
        // Update item hologram
        Item itemDisplay = itemHolograms.get(shop.getId());
        if (itemDisplay != null && !itemDisplay.isDead()) {
            itemDisplay.setItemStack(new ItemStack(shop.getItem(), shop.getAmount()));
        }
        
        // Update price hologram
        ArmorStand priceStand = priceHolograms.get(shop.getId());
        if (priceStand != null && !priceStand.isDead()) {
            priceStand.setCustomName(formatPriceText(shop, plugin));
        }
    }
    
    /**
     * Removes all holograms (for plugin disable)
     */
    public static void removeAllHolograms() {
        // Remove all text holograms
        for (ArmorStand stand : textHolograms.values()) {
            if (stand != null && !stand.isDead()) {
                stand.remove();
            }
        }
        textHolograms.clear();
        
        // Remove all item holograms
        for (Item item : itemHolograms.values()) {
            if (item != null && !item.isDead()) {
                item.remove();
            }
        }
        itemHolograms.clear();
        
        // Remove all price holograms
        for (ArmorStand stand : priceHolograms.values()) {
            if (stand != null && !stand.isDead()) {
                stand.remove();
            }
        }
        priceHolograms.clear();
    }
    
    /**
     * Recreates all holograms (for config reload)
     */
    public static void recreateAllHolograms(ChestShopPlugin plugin) {
        removeAllHolograms();
        
        if (!plugin.getConfigManager().getConfig().getBoolean("holograms.enabled", true)) {
            return;
        }
        
        for (Shop shop : plugin.getShopManager().getAllShops()) {
            createShopHologram(shop, plugin);
        }
    }
    
    private static ArmorStand createTextHologram(Location location, String text, String name) {
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCanPickupItems(false);
        armorStand.setCustomName(text);
        armorStand.setCustomNameVisible(true);
        armorStand.setSmall(true);
        armorStand.setMarker(true);
        armorStand.setInvulnerable(true);
        armorStand.setPersistent(false);
        
        return armorStand;
    }
    
    private static Item createItemHologram(Location location, ItemStack itemStack, String name) {
        Item item = location.getWorld().dropItem(location, itemStack);
        
        item.setVelocity(new Vector(0, 0, 0));
        item.setPickupDelay(Integer.MAX_VALUE);
        item.setGravity(false);
        item.setInvulnerable(true);
        item.setPersistent(false);
        
        return item;
    }
    
    private static String formatShopText(Shop shop, ChestShopPlugin plugin) {
        String template = plugin.getConfigManager().getConfig().getString("holograms.text-format", 
            "&6[ChestShop] &b%owner%");
        
        return template
            .replace("%owner%", shop.getOwnerName())
            .replace("%item%", shop.getItem().name())
            .replace("%amount%", String.valueOf(shop.getAmount()))
            .replace("%stock%", String.valueOf(shop.getStock()))
            .replace("&", "§");
    }
    
    private static String formatPriceText(Shop shop, ChestShopPlugin plugin) {
        String buyText = shop.hasBuyPrice() ? 
            plugin.getEconomyManager().formatSimple(shop.getBuyPrice()) : "---";
        String sellText = shop.hasSellPrice() ? 
            plugin.getEconomyManager().formatSimple(shop.getSellPrice()) : "---";
            
        String template = plugin.getConfigManager().getConfig().getString("holograms.price-format", 
            "&aBuy: %buy% &cSell: %sell%");
        
        return template
            .replace("%buy%", buyText)
            .replace("%sell%", sellText)
            .replace("&", "§");
    }
}
