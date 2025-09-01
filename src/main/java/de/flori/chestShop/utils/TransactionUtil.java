package de.flori.chestShop.utils;

import de.flori.chestShop.ChestShopPlugin;
import de.flori.chestShop.models.Shop;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class TransactionUtil {

    public static void handleBuyTransaction(Player buyer, Shop shop, ChestShopPlugin plugin) {
        int amount = shop.getAmount();
        double price = shop.getBuyPrice();

        // Prüfungen
        if (!shop.canBuy(amount)) {
            buyer.sendMessage(plugin.getConfigManager().getMessage("shop.transaction.insufficient-stock"));
            return;
        }

        if (!plugin.getEconomyManager().hasEnough(buyer, price)) {
            buyer.sendMessage(plugin.getConfigManager().getMessage("shop.transaction.insufficient-money",
                "%price%", plugin.getEconomyManager().format(price)));
            return;
        }

        // Check if player has inventory space
        if (!hasInventorySpace(buyer, shop.getItem(), amount)) {
            buyer.sendMessage(plugin.getConfigManager().getMessage("shop.transaction.insufficient-space"));
            return;
        }

        // Transaktion durchführen
        if (removeItemsFromChest(shop, amount)) {
            // Geld abziehen
            plugin.getEconomyManager().withdraw(buyer, price);
            
            // Geld an Owner geben
            Player owner = plugin.getServer().getPlayer(shop.getOwnerId());
            if (owner != null) {
                plugin.getEconomyManager().deposit(owner, price);
                owner.sendMessage(plugin.getConfigManager().getMessage("shop.transaction.owner-notification-buy",
                    "%player%", buyer.getName(),
                    "%amount%", String.valueOf(amount),
                    "%item%", shop.getItem().name(),
                    "%price%", plugin.getEconomyManager().format(price)));
            }

            // Items geben
            ItemStack itemStack = new ItemStack(shop.getItem(), amount);
            buyer.getInventory().addItem(itemStack);

            buyer.sendMessage(plugin.getConfigManager().getMessage("shop.transaction.buy-success",
                "%amount%", String.valueOf(amount),
                "%item%", shop.getItem().name(),
                "%price%", plugin.getEconomyManager().format(price)));

            // Shop aktualisieren
            shop.updateLastUsed();
            SignUtil.updateShopSign(shop, plugin);
            
            // Benachrichtigungen prüfen
            NotificationUtil.checkLowStock(shop, plugin);
            
            // Statistiken aktualisieren
            if (plugin.getConfigManager().getConfig().getBoolean("statistics.enabled", true)) {
                StatisticsUtil.recordPurchase(shop.getItem().name(), amount, buyer.getUniqueId(), price, shop.getId());
            }

            // Logging
            if (plugin.getConfigManager().getConfig().getBoolean("logging.log-transactions")) {
                plugin.getLogger().info(String.format("PURCHASE: %s bought %dx %s for %s from %s",
                    buyer.getName(), amount, shop.getItem().name(), 
                    plugin.getEconomyManager().format(price), shop.getOwnerName()));
            }
        } else {
            buyer.sendMessage(plugin.getConfigManager().getMessage("shop.transaction.insufficient-stock"));
        }
    }

    public static void handleSellTransaction(Player seller, Shop shop, ChestShopPlugin plugin) {
        int amount = shop.getAmount();
        double price = shop.getSellPrice();

        // Prüfungen
        if (!shop.canSell(amount)) {
            seller.sendMessage(plugin.getConfigManager().getMessage("shop.transaction.insufficient-space"));
            return;
        }

        // Prüfen ob Spieler genügend Items hat
        if (!hasEnoughItems(seller, shop.getItem(), amount)) {
            seller.sendMessage(plugin.getConfigManager().getMessage("shop.transaction.insufficient-items"));
            return;
        }

        // Check if owner has enough money
        Player owner = plugin.getServer().getPlayer(shop.getOwnerId());
        if (owner != null && !plugin.getEconomyManager().hasEnough(owner, price)) {
            seller.sendMessage(plugin.getConfigManager().getMessage("shop.transaction.owner-insufficient-funds"));
            return;
        }

        // Items aus Spieler-Inventar entfernen
        if (removeItemsFromPlayer(seller, shop.getItem(), amount)) {
            // Items in Chest legen
            addItemsToChest(shop, shop.getItem(), amount);

            // Geld an Verkäufer geben
            plugin.getEconomyManager().deposit(seller, price);
            
            // Geld von Owner abziehen
            if (owner != null) {
                plugin.getEconomyManager().withdraw(owner, price);
                owner.sendMessage(plugin.getConfigManager().getMessage("shop.transaction.owner-notification-sell",
                    "%player%", seller.getName(),
                    "%amount%", String.valueOf(amount),
                    "%item%", shop.getItem().name(),
                    "%price%", plugin.getEconomyManager().format(price)));
            }

            seller.sendMessage(plugin.getConfigManager().getMessage("shop.transaction.sell-success",
                "%amount%", String.valueOf(amount),
                "%item%", shop.getItem().name(),
                "%price%", plugin.getEconomyManager().format(price)));

            // Shop aktualisieren
            shop.updateLastUsed();
            SignUtil.updateShopSign(shop, plugin);
            
            // Benachrichtigungen prüfen
            NotificationUtil.checkFullShop(shop, plugin);
            
            // Statistiken aktualisieren
            if (plugin.getConfigManager().getConfig().getBoolean("statistics.enabled", true)) {
                StatisticsUtil.recordSale(shop.getItem().name(), amount, seller.getUniqueId(), price, shop.getId());
            }

            // Logging
            if (plugin.getConfigManager().getConfig().getBoolean("logging.log-transactions")) {
                plugin.getLogger().info(String.format("SALE: %s sold %dx %s for %s to %s",
                    seller.getName(), amount, shop.getItem().name(), 
                    plugin.getEconomyManager().format(price), shop.getOwnerName()));
            }
        } else {
            seller.sendMessage(plugin.getConfigManager().getMessage("shop.transaction.insufficient-items"));
        }
    }

    private static boolean hasInventorySpace(Player player, org.bukkit.Material material, int amount) {
        Inventory inventory = player.getInventory();
        int space = 0;
        int maxStackSize = material.getMaxStackSize();

        for (ItemStack item : inventory.getContents()) {
            if (item == null) {
                space += maxStackSize;
            } else if (item.getType() == material) {
                space += maxStackSize - item.getAmount();
            }
        }

        return space >= amount;
    }

    private static boolean hasEnoughItems(Player player, org.bukkit.Material material, int amount) {
        int totalAmount = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                totalAmount += item.getAmount();
            }
        }
        return totalAmount >= amount;
    }

    private static boolean removeItemsFromChest(Shop shop, int amount) {
        Inventory inventory = shop.getChest().getInventory();
        int remaining = amount;

        for (int i = 0; i < inventory.getSize() && remaining > 0; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == shop.getItem()) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) {
                    inventory.clear(i);
                    remaining -= itemAmount;
                } else {
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                }
            }
        }

        return remaining == 0;
    }

    private static boolean removeItemsFromPlayer(Player player, org.bukkit.Material material, int amount) {
        Inventory inventory = player.getInventory();
        int remaining = amount;

        for (int i = 0; i < inventory.getSize() && remaining > 0; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == material) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) {
                    inventory.clear(i);
                    remaining -= itemAmount;
                } else {
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                }
            }
        }

        return remaining == 0;
    }

    private static void addItemsToChest(Shop shop, org.bukkit.Material material, int amount) {
        Inventory inventory = shop.getChest().getInventory();
        ItemStack itemStack = new ItemStack(material, amount);
        
        HashMap<Integer, ItemStack> leftover = inventory.addItem(itemStack);
        
        // If items are left over, this shouldn't happen as we check beforehand
        if (!leftover.isEmpty()) {
            ChestShopPlugin.getInstance().getLogger().warning(
                "Items could not be completely placed in shop " + shop.getId() + "!");
        }
    }
}
