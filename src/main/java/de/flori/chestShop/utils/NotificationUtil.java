package de.flori.chestShop.utils;

import de.flori.chestShop.ChestShopPlugin;
import de.flori.chestShop.models.Shop;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NotificationUtil {
    
    private static final Map<String, Long> lastNotifications = new HashMap<>();
    private static final long NOTIFICATION_COOLDOWN = 300000; // 5 Minuten in Millisekunden
    
    /**
     * Prüft ob ein Shop einen niedrigen Lagerbestand hat und benachrichtigt den Besitzer
     */
    public static void checkLowStock(Shop shop, ChestShopPlugin plugin) {
        if (!plugin.getConfigManager().getConfig().getBoolean("notifications.low-stock.enabled", true)) {
            return;
        }
        
        int currentStock = shop.getStock();
        int lowStockThreshold = plugin.getConfigManager().getConfig().getInt("notifications.low-stock.threshold", 5);
        
        if (currentStock <= lowStockThreshold && shop.hasBuyPrice()) {
            String notificationKey = shop.getId() + "_lowstock";
            long now = System.currentTimeMillis();
            
            // Cooldown prüfen
            if (lastNotifications.containsKey(notificationKey)) {
                long lastNotification = lastNotifications.get(notificationKey);
                if (now - lastNotification < NOTIFICATION_COOLDOWN) {
                    return; // Zu früh für neue Benachrichtigung
                }
            }
            
            // Owner benachrichtigen
            Player owner = plugin.getServer().getPlayer(shop.getOwnerId());
            if (owner != null && owner.isOnline()) {
                owner.sendMessage(plugin.getConfigManager().getMessage("notifications.low-stock.message",
                    "%item%", shop.getItem().name(),
                    "%stock%", String.valueOf(currentStock),
                    "%threshold%", String.valueOf(lowStockThreshold),
                    "%world%", shop.getChestLocation().getWorld().getName(),
                    "%x%", String.valueOf(shop.getChestLocation().getBlockX()),
                    "%y%", String.valueOf(shop.getChestLocation().getBlockY()),
                    "%z%", String.valueOf(shop.getChestLocation().getBlockZ())));
                
                lastNotifications.put(notificationKey, now);
            }
        }
    }
    
    /**
     * Prüft ob ein Shop voll ist und benachrichtigt den Besitzer
     */
    public static void checkFullShop(Shop shop, ChestShopPlugin plugin) {
        if (!plugin.getConfigManager().getConfig().getBoolean("notifications.full-shop.enabled", true)) {
            return;
        }
        
        int availableSpace = shop.getAvailableSpace();
        
        if (availableSpace == 0 && shop.hasSellPrice()) {
            String notificationKey = shop.getId() + "_fullshop";
            long now = System.currentTimeMillis();
            
            // Cooldown prüfen
            if (lastNotifications.containsKey(notificationKey)) {
                long lastNotification = lastNotifications.get(notificationKey);
                if (now - lastNotification < NOTIFICATION_COOLDOWN) {
                    return; // Zu früh für neue Benachrichtigung
                }
            }
            
            // Owner benachrichtigen
            Player owner = plugin.getServer().getPlayer(shop.getOwnerId());
            if (owner != null && owner.isOnline()) {
                owner.sendMessage(plugin.getConfigManager().getMessage("notifications.full-shop.message",
                    "%item%", shop.getItem().name(),
                    "%world%", shop.getChestLocation().getWorld().getName(),
                    "%x%", String.valueOf(shop.getChestLocation().getBlockX()),
                    "%y%", String.valueOf(shop.getChestLocation().getBlockY()),
                    "%z%", String.valueOf(shop.getChestLocation().getBlockZ())));
                
                lastNotifications.put(notificationKey, now);
            }
        }
    }
    
    /**
     * Startet einen periodischen Task zur Überprüfung aller Shops
     */
    public static void startPeriodicCheck(ChestShopPlugin plugin) {
        boolean enablePeriodicCheck = plugin.getConfigManager().getConfig().getBoolean("notifications.periodic-check.enabled", false);
        if (!enablePeriodicCheck) {
            return;
        }
        
        int checkInterval = plugin.getConfigManager().getConfig().getInt("notifications.periodic-check.interval-minutes", 30);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Shop shop : plugin.getShopManager().getAllShops()) {
                    if (shop.isActive()) {
                        checkLowStock(shop, plugin);
                        checkFullShop(shop, plugin);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L * 60 * checkInterval, 20L * 60 * checkInterval); // Konvertiere Minuten zu Ticks
    }
    
    /**
     * Benachrichtigt alle Shop-Besitzer über ihre niedrigen Lagerbestände
     */
    public static void notifyAllLowStockShops(ChestShopPlugin plugin, UUID playerId) {
        for (Shop shop : plugin.getShopManager().getShopsByOwner(playerId)) {
            if (shop.isActive()) {
                checkLowStock(shop, plugin);
            }
        }
    }
    
    /**
     * Löscht alle Benachrichtigungs-Cooldowns (für Reload)
     */
    public static void clearNotificationCooldowns() {
        lastNotifications.clear();
    }
}
