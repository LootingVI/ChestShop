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
        // Check if ShopManager is initialized
        if (plugin.getShopManager() == null) {
            plugin.getLogger().info("Skipping hologram creation: ShopManager not yet initialized");
            return;
        }
        
        if (!plugin.getConfigManager().getConfig().getBoolean("holograms.enabled", true)) {
            return;
        }
        
        // Null safety check for shop location
        if (shop.getChestLocation() == null) {
            plugin.getLogger().warning("Cannot create hologram for shop " + shop.getId() + ": chest location is null");
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
        
        // Check if ShopManager is initialized
        if (plugin.getShopManager() == null) {
            plugin.getLogger().info("Cannot recreate holograms: ShopManager not yet initialized");
            return;
        }
        
        if (!plugin.getConfigManager().getConfig().getBoolean("holograms.enabled", true)) {
            return;
        }
        
        for (Shop shop : plugin.getShopManager().getAllShops()) {
            try {
                // Additional null safety check
                if (shop.getChestLocation() != null) {
                    createShopHologram(shop, plugin);
                } else {
                    plugin.getLogger().warning("Skipping hologram creation for shop " + shop.getId() + ": invalid location");
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to create hologram for shop " + shop.getId() + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Initialize all holograms after plugin startup (when ShopManager is ready)
     */
    public static void initializeAllHolograms(ChestShopPlugin plugin) {
        // This method is specifically for initial hologram creation after startup
        if (plugin.getShopManager() == null) {
            plugin.getLogger().warning("Cannot initialize holograms: ShopManager not available");
            return;
        }
        
        if (!plugin.getConfigManager().getConfig().getBoolean("holograms.enabled", true)) {
            plugin.getLogger().info("Holograms are disabled in config");
            return;
        }
        
        int successCount = 0;
        int errorCount = 0;
        
        for (Shop shop : plugin.getShopManager().getAllShops()) {
            try {
                if (shop.getChestLocation() != null) {
                    createShopHologram(shop, plugin);
                    successCount++;
                } else {
                    plugin.getLogger().warning("Skipping hologram for shop " + shop.getId() + ": invalid location");
                    errorCount++;
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to create hologram for shop " + shop.getId() + ": " + e.getMessage());
                errorCount++;
            }
        }
        
        if (successCount > 0 || errorCount > 0) {
            plugin.getLogger().info("Hologram initialization complete: " + successCount + " created, " + errorCount + " errors");
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
        boolean itemTradingEnabled = plugin.getConfigManager().getConfig().getBoolean("item-trading.enabled", false);
        
        if (itemTradingEnabled && shop.isItemTradingShop()) {
            // Trading shop text format
            String template = plugin.getConfigManager().getConfig().getString("holograms.trading-text-format", 
                "&9[ItemShop] &b%owner%");
                
            return template
                .replace("%owner%", shop.getOwnerName())
                .replace("%buy_item%", shop.getBuyItemType().name())
                .replace("%buy_amount%", String.valueOf(shop.getBuyItemAmount()))
                .replace("%sell_item%", shop.getSellItemType().name())
                .replace("%sell_amount%", String.valueOf(shop.getSellItemAmount()))
                .replace("%stock_giving%", String.valueOf(shop.getTradingStockForGiving()))
                .replace("%stock_receiving%", String.valueOf(shop.getTradingStockForReceiving()))
                .replace("&", "§");
        } else {
            // Normal shop text format
            String template = plugin.getConfigManager().getConfig().getString("holograms.text-format", 
                "&6[ChestShop] &b%owner%");
            
            return template
                .replace("%owner%", shop.getOwnerName())
                .replace("%item%", shop.getItem().name())
                .replace("%amount%", String.valueOf(shop.getAmount()))
                .replace("%stock%", String.valueOf(shop.getStock()))
                .replace("&", "§");
        }
    }
    
    private static String formatPriceText(Shop shop, ChestShopPlugin plugin) {
        boolean itemTradingEnabled = plugin.getConfigManager().getConfig().getBoolean("item-trading.enabled", false);
        
        if (itemTradingEnabled && shop.isItemTradingShop()) {
            // Trading shop price format (shows trading ratio)
            String template = plugin.getConfigManager().getConfig().getString("holograms.trading-price-format", 
                "&e%buy_amount%x %buy_item% &6-> &a%sell_amount%x %sell_item%");
                
            return template
                .replace("%buy_amount%", String.valueOf(shop.getBuyItemAmount()))
                .replace("%buy_item%", getShortItemName(shop.getBuyItem()))
                .replace("%sell_amount%", String.valueOf(shop.getSellItemAmount()))
                .replace("%sell_item%", getShortItemName(shop.getSellItem()))
                .replace("&", "§");
        } else {
            // Normal shop price format
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
    
    /**
     * Get a shortened version of an item name for holograms
     */
    private static String getShortItemName(org.bukkit.Material material) {
        String fullName = material.name().toLowerCase().replace("_", " ");
        String[] words = fullName.split(" ");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        
        String displayName = result.toString();
        
        // If the name is already short enough, return it
        if (displayName.length() <= 12) {
            return displayName;
        }
        
        // Try to abbreviate common words
        String abbreviated = displayName
            .replace("Diamond", "Dia")
            .replace("Iron", "Fe")
            .replace("Golden", "Au")
            .replace("Stone", "St")
            .replace("Wooden", "Wd")
            .replace("Leather", "Lea")
            .replace("Enchanted", "Ench")
            .replace(" Of ", " ")
            .replace(" The ", " ")
            .replace("Block", "Bl");
        
        // If still too long, take first 12 characters
        if (abbreviated.length() > 12) {
            abbreviated = abbreviated.substring(0, 12);
        }
        
        return abbreviated;
    }
}
