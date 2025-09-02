package de.flori.chestShop.listeners;

import de.flori.chestShop.ChestShopPlugin;
import de.flori.chestShop.models.Shop;
import de.flori.chestShop.utils.SignUtil;
import de.flori.chestShop.utils.TransactionUtil;
import de.flori.chestShop.utils.TradingUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class ChestShopListener implements Listener {

    private final ChestShopPlugin plugin;

    public ChestShopListener(ChestShopPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        Shop shop = plugin.getShopManager().getShopByLocation(clickedBlock.getLocation());
        if (shop == null) {
            return;
        }

        Player player = event.getPlayer();
        event.setCancelled(true);

        // Informationen anzeigen bei Shift-Klick
        if (player.isSneaking()) {
            showShopPreview(player, shop);
            return;
        }

        // Shop-Owner Zugriff prüfen
        if (shop.getOwnerId().equals(player.getUniqueId())) {
            boolean ownerFreeAccess = plugin.getConfigManager().getConfig().getBoolean("shop.behavior.owner-free-access");
            if (ownerFreeAccess) {
                // Owner kann frei auf Chest zugreifen
                return;
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage("shop.transaction.cannot-buy-own"));
                return;
            }
        }

        // Wartungsmodus prüfen
        boolean maintenanceMode = plugin.getConfigManager().getConfig().getBoolean("general.maintenance-mode", false);
        if (maintenanceMode && !player.hasPermission("chestshop.admin")) {
            player.sendMessage(plugin.getConfigManager().getMessage("admin.maintenance-mode-enabled"));
            return;
        }
        
        // Shop-Status prüfen
        if (!shop.isActive()) {
            player.sendMessage(plugin.getConfigManager().getMessage("shop.transaction.shop-inactive"));
            return;
        }

        // Prüfen ob Item-Trading aktiviert ist
        boolean itemTradingEnabled = plugin.getConfigManager().getConfig().getBoolean("item-trading.enabled", false);
        
        switch (event.getAction()) {
            case LEFT_CLICK_BLOCK:
                // Kaufen (normal) oder Trading-Vorschau
                if (itemTradingEnabled && shop.isItemTradingShop()) {
                    // Item-Trading: Linksklick = Vorschau anzeigen
                    TradingUtil.showTradingPreview(player, shop, plugin);
                } else if (shop.hasBuyPrice()) {
                    TransactionUtil.handleBuyTransaction(player, shop, plugin);
                } else {
                    player.sendMessage(plugin.getConfigManager().getMessage("interaction.no-buy-price"));
                }
                break;
                
            case RIGHT_CLICK_BLOCK:
                // Verkaufen (normal) oder Trading durchführen
                if (itemTradingEnabled && shop.isItemTradingShop()) {
                    // Item-Trading: Rechtsklick = Tauschen
                    TradingUtil.executeTrade(player, shop, plugin);
                } else if (shop.hasSellPrice()) {
                    TransactionUtil.handleSellTransaction(player, shop, plugin);
                } else {
                    player.sendMessage(plugin.getConfigManager().getMessage("interaction.no-sell-price"));
                }
                break;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        Shop shop = plugin.getShopManager().getShopByLocation(block.getLocation());
        if (shop == null) {
            return;
        }

        // Prüfen ob Protection aktiviert ist
        if (!plugin.getConfigManager().getConfig().getBoolean("protection.enabled")) {
            return;
        }

        // Check if only owner can break shop blocks
        boolean onlyOwnerBreak = plugin.getConfigManager().getConfig().getBoolean("protection.only-owner-break");
        if (onlyOwnerBreak && !shop.getOwnerId().equals(player.getUniqueId()) && !player.hasPermission("chestshop.admin")) {
            event.setCancelled(true);
            player.sendMessage(plugin.getConfigManager().getMessage("protection.shop-protected"));
            return;
        }

        // Shop automatisch entfernen wenn Chest oder Schild abgebaut wird
        plugin.getShopManager().removeShop(shop.getId());
        
        // Owner benachrichtigen wenn Admin den Shop entfernt
        if (!shop.getOwnerId().equals(player.getUniqueId())) {
            Player owner = plugin.getServer().getPlayer(shop.getOwnerId());
            if (owner != null) {
                owner.sendMessage(plugin.getConfigManager().getMessage("shop.removal.admin-removed"));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("protection.hopper-protection")) {
            return;
        }

        // Prüfen ob ein Hopper Items aus einem ChestShop entnimmt
        if (event.getSource().getHolder() instanceof Chest) {
            Chest chest = (Chest) event.getSource().getHolder();
            Shop shop = plugin.getShopManager().getShopByLocation(chest.getLocation());
            
            if (shop != null && event.getDestination().getHolder() instanceof Hopper) {
                event.setCancelled(true);
            }
        }

        // Prüfen ob ein Hopper Items in einen ChestShop einlegt
        if (event.getDestination().getHolder() instanceof Chest) {
            Chest chest = (Chest) event.getDestination().getHolder();
            Shop shop = plugin.getShopManager().getShopByLocation(chest.getLocation());
            
            if (shop != null && event.getSource().getHolder() instanceof Hopper) {
                // Erlaube das Einlegen von Items des Shop-Typs
                Material shopItem = shop.getItem();
                Material movingItem = event.getItem().getType();
                
                if (!movingItem.equals(shopItem)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        
        // Prüfen ob es sich um eine Shop-Truhe handelt
        if (event.getInventory().getHolder() instanceof Chest) {
            Chest chest = (Chest) event.getInventory().getHolder();
            Shop shop = plugin.getShopManager().getShopByLocation(chest.getLocation());
            
            if (shop != null) {
                // Nur Shop-Besitzer und Admins dürfen Truhe öffnen
                boolean allowOwnerAccess = plugin.getConfigManager().getConfig().getBoolean("protection.allow-owner-inventory-access", true);
                
                if (!allowOwnerAccess || (!shop.getOwnerId().equals(player.getUniqueId()) && !player.hasPermission("chestshop.admin"))) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.getConfigManager().getMessage("protection.no-chest-access"));
                    return;
                }
                
                // Warnung an Owner dass dies ein Shop ist
                if (shop.getOwnerId().equals(player.getUniqueId())) {
                    player.sendMessage(plugin.getConfigManager().getMessage("protection.owner-chest-warning"));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        // Prüfen ob Block neben einem Shop platziert wird
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    
                    Location checkLoc = block.getLocation().clone().add(x, y, z);
                    Shop shop = plugin.getShopManager().getShopByLocation(checkLoc);
                    
                    if (shop != null) {
                        // Prüfen ob Protection für Block-Platzierung aktiviert ist
                        boolean protectNearby = plugin.getConfigManager().getConfig().getBoolean("protection.protect-nearby-blocks", true);
                        
                        if (protectNearby && !shop.getOwnerId().equals(player.getUniqueId()) && !player.hasPermission("chestshop.admin")) {
                            // Bestimmte Blöcke sind gefährlich (wie Hopper)
                            if (block.getType() == Material.HOPPER || 
                                block.getType() == Material.DROPPER || 
                                block.getType() == Material.DISPENSER ||
                                block.getType() == Material.PISTON ||
                                block.getType() == Material.STICKY_PISTON ||
                                block.getType() == Material.TNT) {
                                
                                event.setCancelled(true);
                                player.sendMessage(plugin.getConfigManager().getMessage("protection.no-place-near-shop"));
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private void showShopPreview(Player player, Shop shop) {
        boolean itemTradingEnabled = plugin.getConfigManager().getConfig().getBoolean("item-trading.enabled", false);
        
        if (itemTradingEnabled && shop.isItemTradingShop()) {
            // Trading Shop Preview
            player.sendMessage(plugin.getConfigManager().getMessage("item-trading.shop-info.header"));
            player.sendMessage(plugin.getConfigManager().getMessage("item-trading.shop-info.owner", "%owner%", shop.getOwnerName()));
            player.sendMessage(plugin.getConfigManager().getMessage("item-trading.shop-info.buy-item", 
                "%amount%", String.valueOf(shop.getBuyItemAmount()),
                "%item%", shop.getBuyItem().name()));
            player.sendMessage(plugin.getConfigManager().getMessage("item-trading.shop-info.sell-item", 
                "%amount%", String.valueOf(shop.getSellItemAmount()),
                "%item%", shop.getSellItem().name()));
            
            // Stock information
            int stockGiving = shop.getTradingStockForGiving();
            int stockReceiving = shop.getTradingStockForReceiving();
            player.sendMessage(plugin.getConfigManager().getMessage("item-trading.shop-info.stock-buy", 
                "%stock%", String.valueOf(stockGiving)));
            player.sendMessage(plugin.getConfigManager().getMessage("item-trading.shop-info.stock-sell", 
                "%stock%", String.valueOf(stockReceiving)));
            
            // Trading rate
            player.sendMessage(plugin.getConfigManager().getMessage("item-trading.shop-info.trading-rate", 
                "%buy_amount%", String.valueOf(shop.getBuyItemAmount()),
                "%buy_item%", shop.getBuyItem().name(),
                "%sell_amount%", String.valueOf(shop.getSellItemAmount()),
                "%sell_item%", shop.getSellItem().name()));
            
            // Interaction hints
            player.sendMessage(plugin.getConfigManager().getMessage("item-trading.interaction.left-click-info",
                "%buy_amount%", String.valueOf(shop.getBuyItemAmount()),
                "%buy_item%", shop.getBuyItem().name(),
                "%sell_amount%", String.valueOf(shop.getSellItemAmount()),
                "%sell_item%", shop.getSellItem().name()));
            player.sendMessage(plugin.getConfigManager().getMessage("item-trading.interaction.right-click-trade"));
            
            player.sendMessage(plugin.getConfigManager().getMessage("item-trading.shop-info.status", "%status%", getStatusMessage(shop)));
            player.sendMessage(plugin.getConfigManager().getMessage("item-trading.shop-info.footer"));
            
        } else {
            // Normal Shop Preview
            player.sendMessage(plugin.getConfigManager().getMessage("shop.preview.header"));
            player.sendMessage(plugin.getConfigManager().getMessage("shop.preview.owner", "%owner%", shop.getOwnerName()));
            player.sendMessage(plugin.getConfigManager().getMessage("shop.preview.item", 
                "%amount%", String.valueOf(shop.getAmount()),
                "%item%", shop.getItem().name()));
            
            if (shop.hasBuyPrice() && shop.canBuy(shop.getAmount())) {
                player.sendMessage(plugin.getConfigManager().getMessage("interaction.left-click-buy",
                    "%amount%", String.valueOf(shop.getAmount()),
                    "%item%", shop.getItem().name(),
                    "%price%", plugin.getEconomyManager().format(shop.getBuyPrice())));
            }
            
            if (shop.hasSellPrice() && shop.canSell(shop.getAmount())) {
                player.sendMessage(plugin.getConfigManager().getMessage("interaction.right-click-sell",
                    "%amount%", String.valueOf(shop.getAmount()),
                    "%item%", shop.getItem().name(),
                    "%price%", plugin.getEconomyManager().format(shop.getSellPrice())));
            }
            
            player.sendMessage(plugin.getConfigManager().getMessage("shop.preview.stock", "%stock%", String.valueOf(shop.getStock())));
            player.sendMessage(plugin.getConfigManager().getMessage("shop.preview.status", "%status%", getStatusMessage(shop)));
            player.sendMessage(plugin.getConfigManager().getMessage("shop.preview.footer"));
        }
    }

    private String getStatusMessage(Shop shop) {
        String statusKey = "shop.status." + shop.getStatus().name().toLowerCase().replace("_", "-");
        return plugin.getConfigManager().getMessage(statusKey);
    }
}
