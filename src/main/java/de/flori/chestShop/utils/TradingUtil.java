package de.flori.chestShop.utils;

import de.flori.chestShop.ChestShopPlugin;
import de.flori.chestShop.models.Shop;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class TradingUtil {
    
    private static final Map<String, Long> tradingCooldowns = new HashMap<>();
    private static final Map<String, Long> confirmationTimeouts = new HashMap<>();
    
    /**
     * Handles item trading transaction
     */
    public static boolean handleItemTrade(Player player, Shop shop, ChestShopPlugin plugin) {
        // Check if item trading is enabled
        if (!plugin.getConfigManager().getConfig().getBoolean("item-trading.enabled", false)) {
            player.sendMessage(plugin.getConfigManager().getMessage("item-trading.creation.feature-disabled"));
            return false;
        }
        
        // Check if shop has item trading
        if (!shop.hasItemTrading()) {
            player.sendMessage(plugin.getConfigManager().getMessage("item-trading.trading.not-trading-shop"));
            return false;
        }
        
        // Check shop status
        if (!shop.isActive()) {
            player.sendMessage(plugin.getConfigManager().getMessage("item-trading.trading.shop-inactive"));
            return false;
        }
        
        // Check if player is shop owner
        if (shop.getOwnerId().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getMessage("item-trading.trading.self-trade-blocked"));
            return false;
        }
        
        // Check trading cooldown
        if (hasTradingCooldown(player, plugin)) {
            long remainingTime = getRemainingCooldown(player, plugin);
            player.sendMessage(plugin.getConfigManager().getMessage("item-trading.trading.cooldown-active", 
                "%seconds%", String.valueOf(remainingTime)));
            return false;
        }
        
        // Check distance if configured
        int maxDistance = plugin.getConfigManager().getConfig().getInt("item-trading.behavior.max-trading-distance", 0);
        if (maxDistance > 0) {
            double distance = player.getLocation().distance(shop.getChestLocation());
            if (distance > maxDistance) {
                player.sendMessage(plugin.getConfigManager().getMessage("item-trading.trading.too-far-away",
                    "%distance%", String.valueOf(maxDistance)));
                return false;
            }
        }
        
        // Validate player has required items
        if (!hasRequiredItems(player, shop, plugin)) {
            return false;
        }
        
        // Validate shop has items to give
        if (!shopHasItemsToGive(shop, player, plugin)) {
            return false;
        }
        
        // Validate player has inventory space
        if (!playerHasInventorySpace(player, shop, plugin)) {
            return false;
        }
        
        // Validate shop has space for received items
        if (!shopHasSpaceForItems(shop, player, plugin)) {
            return false;
        }
        
        // Execute the trade
        return executeItemTrade(player, shop, plugin);
    }
    
    /**
     * Shows item trading preview to player
     */
    public static void showTradingPreview(Player player, Shop shop, ChestShopPlugin plugin) {
        if (!shop.hasItemTrading()) {
            player.sendMessage(plugin.getConfigManager().getMessage("item-trading.trading.not-trading-shop"));
            return;
        }
        
        player.sendMessage(plugin.getConfigManager().getMessage("item-trading.trading.preview-header"));
        player.sendMessage(plugin.getConfigManager().getMessage("item-trading.trading.preview-required",
            "%amount%", String.valueOf(shop.getBuyItemAmount()),
            "%item%", getItemDisplayName(shop.getBuyItemType())));
        player.sendMessage(plugin.getConfigManager().getMessage("item-trading.trading.preview-given",
            "%amount%", String.valueOf(shop.getSellItemAmount()),
            "%item%", getItemDisplayName(shop.getSellItemType())));
        player.sendMessage(plugin.getConfigManager().getMessage("item-trading.trading.preview-footer"));
        player.sendMessage(plugin.getConfigManager().getMessage("item-trading.trading.preview-confirm"));
    }
    
    /**
     * Executes the trade from listener interaction
     */
    public static boolean executeTrade(Player player, Shop shop, ChestShopPlugin plugin) {
        return handleItemTrade(player, shop, plugin);
    }
    
    /**
     * Validates if the player has the required items for trading
     */
    private static boolean hasRequiredItems(Player player, Shop shop, ChestShopPlugin plugin) {
        int requiredAmount = shop.getBuyItemAmount();
        Material requiredItem = shop.getBuyItemType();
        
        int playerItemCount = getPlayerItemCount(player, requiredItem);
        
        if (playerItemCount < requiredAmount) {
            player.sendMessage(plugin.getConfigManager().getMessage("item-trading.trading.insufficient-items",
                "%needed%", String.valueOf(requiredAmount),
                "%item%", getItemDisplayName(requiredItem)));
            return false;
        }
        
        return true;
    }
    
    /**
     * Validates if shop has items to give
     */
    private static boolean shopHasItemsToGive(Shop shop, Player player, ChestShopPlugin plugin) {
        int requiredStock = shop.getSellItemAmount();
        int availableStock = shop.getItemStock(shop.getSellItemType());
        
        if (availableStock < requiredStock) {
            player.sendMessage(plugin.getConfigManager().getMessage("item-trading.trading.insufficient-stock",
                "%available%", String.valueOf(availableStock),
                "%item%", getItemDisplayName(shop.getSellItemType())));
            return false;
        }
        
        return true;
    }
    
    /**
     * Validates if player has inventory space for received items
     */
    private static boolean playerHasInventorySpace(Player player, Shop shop, ChestShopPlugin plugin) {
        int spaceNeeded = shop.getSellItemAmount();
        int availableSpace = getPlayerInventorySpace(player, shop.getSellItemType());
        
        if (availableSpace < spaceNeeded) {
            player.sendMessage(plugin.getConfigManager().getMessage("item-trading.trading.insufficient-space",
                "%amount%", String.valueOf(spaceNeeded),
                "%item%", getItemDisplayName(shop.getSellItemType())));
            return false;
        }
        
        return true;
    }
    
    /**
     * Validates if shop has space for received items
     */
    private static boolean shopHasSpaceForItems(Shop shop, Player player, ChestShopPlugin plugin) {
        int spaceNeeded = shop.getBuyItemAmount();
        int availableSpace = shop.getItemSpace(shop.getBuyItemType());
        
        if (availableSpace < spaceNeeded) {
            player.sendMessage(plugin.getConfigManager().getMessage("item-trading.trading.shop-insufficient-space",
                "%amount%", String.valueOf(spaceNeeded),
                "%item%", getItemDisplayName(shop.getBuyItemType())));
            return false;
        }
        
        return true;
    }
    
    /**
     * Executes the actual item trade
     */
    private static boolean executeItemTrade(Player player, Shop shop, ChestShopPlugin plugin) {
        try {
            // Remove items from player
            if (!removeItemsFromPlayer(player, shop.getBuyItemType(), shop.getBuyItemAmount())) {
                player.sendMessage(plugin.getConfigManager().getMessage("item-trading.errors.conversion-failed",
                    "%reason%", "Could not remove items from inventory"));
                return false;
            }
            
            // Add items to shop
            if (!addItemsToShop(shop, shop.getBuyItemType(), shop.getBuyItemAmount(), plugin)) {
                // Rollback - give items back to player
                giveItemsToPlayer(player, shop.getBuyItemType(), shop.getBuyItemAmount());
                player.sendMessage(plugin.getConfigManager().getMessage("item-trading.trading.shop-insufficient-space",
                    "%amount%", String.valueOf(shop.getBuyItemAmount()),
                    "%item%", getItemDisplayName(shop.getBuyItemType())));
                return false;
            }
            
            // Remove items from shop
            if (!removeItemsFromShop(shop, shop.getSellItemType(), shop.getSellItemAmount(), plugin)) {
                // Rollback
                removeItemsFromShop(shop, shop.getBuyItemType(), shop.getBuyItemAmount(), plugin);
                giveItemsToPlayer(player, shop.getBuyItemType(), shop.getBuyItemAmount());
                player.sendMessage(plugin.getConfigManager().getMessage("item-trading.trading.insufficient-stock",
                    "%available%", String.valueOf(shop.getItemStock(shop.getSellItemType())),
                    "%item%", getItemDisplayName(shop.getSellItemType())));
                return false;
            }
            
            // Give items to player
            giveItemsToPlayer(player, shop.getSellItemType(), shop.getSellItemAmount());
            
            // Update shop last used
            shop.updateLastUsed();
            
            // Set trading cooldown
            setTradingCooldown(player, plugin);
            
            // Log trade if enabled
            boolean logTrades = plugin.getConfigManager().getConfig().getBoolean("item-trading.behavior.log-item-trades", true);
            if (logTrades) {
                plugin.getLogger().info(String.format("Item trade: %s traded %dx %s for %dx %s at shop %s",
                    player.getName(), 
                    shop.getBuyItemAmount(), shop.getBuyItemType().name(),
                    shop.getSellItemAmount(), shop.getSellItemType().name(),
                    shop.getId()));
            }
            
            // Send success message to player
            player.sendMessage(plugin.getConfigManager().getMessage("item-trading.trading.success",
                "%buy_amount%", String.valueOf(shop.getBuyItemAmount()),
                "%buy_item%", getItemDisplayName(shop.getBuyItemType()),
                "%sell_amount%", String.valueOf(shop.getSellItemAmount()),
                "%sell_item%", getItemDisplayName(shop.getSellItemType())));
            
            // Notify shop owner if enabled
            boolean notifyOwner = plugin.getConfigManager().getConfig().getBoolean("item-trading.notifications.notify-owner", true);
            if (notifyOwner) {
                Player owner = plugin.getServer().getPlayer(shop.getOwnerId());
                if (owner != null && owner.isOnline()) {
                    owner.sendMessage(plugin.getConfigManager().getMessage("item-trading.trading.owner-notification",
                        "%player%", player.getName(),
                        "%buy_amount%", String.valueOf(shop.getBuyItemAmount()),
                        "%buy_item%", getItemDisplayName(shop.getBuyItemType()),
                        "%sell_amount%", String.valueOf(shop.getSellItemAmount()),
                        "%sell_item%", getItemDisplayName(shop.getSellItemType())));
                }
            }
            
            // Update sign
            SignUtil.updateShopSign(shop, plugin);
            
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().warning("Error during item trade: " + e.getMessage());
            return false;
        }
    }
    
    // Helper Methods
    
    /**
     * Checks if player has trading cooldown
     */
    private static boolean hasTradingCooldown(Player player, ChestShopPlugin plugin) {
        int cooldownTime = plugin.getConfigManager().getConfig().getInt("item-trading.behavior.trading-cooldown", 0);
        if (cooldownTime <= 0) return false;
        
        String playerId = player.getUniqueId().toString();
        if (!tradingCooldowns.containsKey(playerId)) return false;
        
        long lastTrade = tradingCooldowns.get(playerId);
        long currentTime = System.currentTimeMillis();
        
        return (currentTime - lastTrade) < (cooldownTime * 1000L);
    }
    
    /**
     * Gets remaining cooldown time in seconds
     */
    private static long getRemainingCooldown(Player player, ChestShopPlugin plugin) {
        int cooldownTime = plugin.getConfigManager().getConfig().getInt("item-trading.behavior.trading-cooldown", 0);
        if (cooldownTime <= 0) return 0;
        
        String playerId = player.getUniqueId().toString();
        if (!tradingCooldowns.containsKey(playerId)) return 0;
        
        long lastTrade = tradingCooldowns.get(playerId);
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastTrade;
        long cooldownMillis = cooldownTime * 1000L;
        
        return Math.max(0, (cooldownMillis - elapsedTime) / 1000);
    }
    
    /**
     * Sets trading cooldown for player
     */
    private static void setTradingCooldown(Player player, ChestShopPlugin plugin) {
        int cooldownTime = plugin.getConfigManager().getConfig().getInt("item-trading.behavior.trading-cooldown", 0);
        if (cooldownTime > 0) {
            tradingCooldowns.put(player.getUniqueId().toString(), System.currentTimeMillis());
        }
    }
    
    /**
     * Counts player's items of a specific type
     */
    private static int getPlayerItemCount(Player player, Material material) {
        Inventory inventory = player.getInventory();
        int count = 0;
        
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        
        return count;
    }
    
    /**
     * Gets available inventory space for a specific item type
     */
    private static int getPlayerInventorySpace(Player player, Material material) {
        Inventory inventory = player.getInventory();
        int space = 0;
        int maxStackSize = material.getMaxStackSize();
        
        for (ItemStack item : inventory.getStorageContents()) {
            if (item == null) {
                space += maxStackSize;
            } else if (item.getType() == material) {
                space += maxStackSize - item.getAmount();
            }
        }
        
        return space;
    }
    
    /**
     * Removes items from player inventory
     */
    private static boolean removeItemsFromPlayer(Player player, Material material, int amount) {
        Inventory inventory = player.getInventory();
        int remaining = amount;
        
        // First pass: count available items
        int available = getPlayerItemCount(player, material);
        if (available < amount) {
            return false;
        }
        
        // Second pass: remove items
        for (int i = 0; i < inventory.getSize() && remaining > 0; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == material) {
                if (item.getAmount() <= remaining) {
                    remaining -= item.getAmount();
                    inventory.setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - remaining);
                    remaining = 0;
                }
            }
        }
        
        return remaining == 0;
    }
    
    /**
     * Gives items to player
     */
    private static void giveItemsToPlayer(Player player, Material material, int amount) {
        ItemStack itemStack = new ItemStack(material, amount);
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(itemStack);
        
        // Drop leftover items if inventory is full
        if (!leftover.isEmpty()) {
            for (ItemStack item : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }
    }
    
    /**
     * Adds items to shop chest
     */
    private static boolean addItemsToShop(Shop shop, Material material, int amount, ChestShopPlugin plugin) {
        org.bukkit.block.Chest chest = shop.getChest();
        if (chest == null) return false;
        
        ItemStack itemStack = new ItemStack(material, amount);
        Map<Integer, ItemStack> leftover = chest.getInventory().addItem(itemStack);
        
        return leftover.isEmpty();
    }
    
    /**
     * Removes items from shop chest
     */
    private static boolean removeItemsFromShop(Shop shop, Material material, int amount, ChestShopPlugin plugin) {
        org.bukkit.block.Chest chest = shop.getChest();
        if (chest == null) return false;
        
        Inventory inventory = chest.getInventory();
        int remaining = amount;
        
        // First pass: check availability
        int available = shop.getItemStock(material);
        if (available < amount) {
            return false;
        }
        
        // Second pass: remove items
        for (int i = 0; i < inventory.getSize() && remaining > 0; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == material) {
                if (item.getAmount() <= remaining) {
                    remaining -= item.getAmount();
                    inventory.setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - remaining);
                    remaining = 0;
                }
            }
        }
        
        return remaining == 0;
    }
    
    /**
     * Gets display name for material
     */
    public static String getItemDisplayName(Material material) {
        if (material == null) return "Unknown";
        
        String name = material.name().toLowerCase().replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (result.length() > 0) {
                result.append(" ");
            }
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1));
                }
            }
        }
        
        return result.toString();
    }
    
    // Public validation methods for commands
    
    /**
     * Checks if an item is allowed for trading
     */
    public static boolean isItemAllowedForTrading(Material material, ChestShopPlugin plugin) {
        if (material == null) return false;
        
        // Check if item is banned from trading
        var bannedItems = plugin.getConfigManager().getConfig().getStringList("item-trading.restrictions.banned-trading-items");
        if (bannedItems.contains(material.name())) {
            return false;
        }
        
        // Check general banned items
        var generalBannedItems = plugin.getConfigManager().getConfig().getStringList("shop.creation.banned-items");
        if (generalBannedItems.contains(material.name())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Validates trading amount
     */
    public static boolean isValidTradingAmount(int amount) {
        return amount > 0 && amount <= 64;
    }
    
    /**
     * Converts a normal shop to an item trading shop
     */
    public static boolean convertToItemTradingShop(Shop shop, Material buyItem, int buyAmount, 
                                                  Material sellItem, int sellAmount, ChestShopPlugin plugin) {
        if (shop == null) return false;
        
        // Validate items
        if (!isItemAllowedForTrading(buyItem, plugin) || !isItemAllowedForTrading(sellItem, plugin)) {
            return false;
        }
        
        // Validate amounts
        if (!isValidTradingAmount(buyAmount) || !isValidTradingAmount(sellAmount)) {
            return false;
        }
        
        // Prevent same item trading
        if (buyItem == sellItem) {
            return false;
        }
        
        // Convert shop
        shop.setItemTradingShop(true);
        shop.setBuyItemType(buyItem);
        shop.setBuyItemAmount(buyAmount);
        shop.setSellItemType(sellItem);
        shop.setSellItemAmount(sellAmount);
        
        // Clear money prices to avoid confusion
        shop.setBuyPrice(0);
        shop.setSellPrice(0);
        
        // Update shop sign
        SignUtil.updateShopSign(shop, plugin);
        
        return true;
    }
    
    /**
     * Cleanup expired trading confirmations (called periodically)
     */
    public static void cleanupExpiredTrades(ChestShopPlugin plugin) {
        int timeoutMinutes = plugin.getConfigManager().getConfig().getInt("item-trading.advanced.cleanup-expired-trades", 5);
        long timeoutMillis = timeoutMinutes * 60 * 1000L;
        long currentTime = System.currentTimeMillis();
        
        confirmationTimeouts.entrySet().removeIf(entry -> 
            (currentTime - entry.getValue()) > timeoutMillis);
        
        // Also cleanup old cooldowns (older than 1 hour)
        tradingCooldowns.entrySet().removeIf(entry -> 
            (currentTime - entry.getValue()) > 3600000L);
    }
}
