package de.flori.chestShop.commands;

import de.flori.chestShop.ChestShopPlugin;
import de.flori.chestShop.models.Shop;
import de.flori.chestShop.utils.SignUtil;
import de.flori.chestShop.utils.StatisticsUtil;
import de.flori.chestShop.utils.HologramUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.io.PrintWriter;
import java.util.StringJoiner;

public class ChestShopCommand implements CommandExecutor, TabCompleter {

    private final ChestShopPlugin plugin;

    public ChestShopCommand(ChestShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                return handleCreate(sender, args);
            case "remove":
                return handleRemove(sender, args);
            case "info":
                return handleInfo(sender, args);
            case "list":
                return handleList(sender, args);
            case "reload":
                return handleReload(sender, args);
            case "toggle":
                return handleToggle(sender, args);
            case "refill":
                return handleRefill(sender, args);
            case "search":
                return handleSearch(sender, args);
            case "stats":
                return handleStats(sender, args);
            case "price":
                return handlePrice(sender, args);
            case "admin":
                return handleAdmin(sender, args);
            case "help":
                sendHelp(sender);
                return true;
            default:
                sender.sendMessage(plugin.getConfigManager().getMessage("errors.unknown-command"));
                return true;
        }
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("general.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("chestshop.create")) {
            player.sendMessage(plugin.getConfigManager().getMessage("general.no-permission"));
            return true;
        }

        if (args.length < 4) {
            player.sendMessage(plugin.getConfigManager().getMessage("commands.usage.create"));
            return true;
        }

        try {
            String itemName = args[1].toUpperCase();
            Material material = Material.valueOf(itemName);
            int amount = Integer.parseInt(args[2]);
            double buyPrice = Double.parseDouble(args[3]);
            double sellPrice = args.length > 4 ? Double.parseDouble(args[4]) : 0.0;

            // Validierungen
            if (!isItemAllowed(material)) {
                player.sendMessage(plugin.getConfigManager().getMessage("shop.creation.item-banned"));
                return true;
            }

            if (!isWorldAllowed(player.getWorld().getName())) {
                player.sendMessage(plugin.getConfigManager().getMessage("shop.creation.world-not-allowed"));
                return true;
            }

            if (!isValidPrice(buyPrice, "buy") || !isValidPrice(sellPrice, "sell")) {
                double minBuy = plugin.getConfigManager().getConfig().getDouble("shop.price-limits.min-buy-price");
                double maxBuy = plugin.getConfigManager().getConfig().getDouble("shop.price-limits.max-buy-price");
                player.sendMessage(plugin.getConfigManager().getMessage("shop.creation.invalid-price", 
                    "%min%", String.valueOf(minBuy), "%max%", String.valueOf(maxBuy)));
                return true;
            }

            if (amount < 1 || amount > 64) {
                player.sendMessage(plugin.getConfigManager().getMessage("errors.invalid-amount"));
                return true;
            }

            // Shop-Limit prüfen
            int maxShops = plugin.getConfigManager().getConfig().getInt("general.max-shops-per-player");
            if (maxShops > 0) {
                int currentShops = plugin.getShopManager().getShopCount(player.getUniqueId());
                if (currentShops >= maxShops) {
                    player.sendMessage(plugin.getConfigManager().getMessage("shop.creation.max-shops-reached",
                        "%current%", String.valueOf(currentShops), "%max%", String.valueOf(maxShops)));
                    return true;
                }
            }

            // Erstellungskosten prüfen
            double creationCost = plugin.getConfigManager().getConfig().getDouble("shop.creation.creation-cost");
            if (creationCost > 0 && !plugin.getEconomyManager().hasEnough(player, creationCost)) {
                player.sendMessage(plugin.getConfigManager().getMessage("shop.creation.insufficient-funds",
                    "%cost%", plugin.getEconomyManager().format(creationCost)));
                return true;
            }

            // Truhe finden
            RayTraceResult rayTrace = player.rayTraceBlocks(5);
            if (rayTrace == null || rayTrace.getHitBlock() == null) {
                player.sendMessage(plugin.getConfigManager().getMessage("shop.creation.chest-not-found"));
                return true;
            }

            Block targetBlock = rayTrace.getHitBlock();
            if (!(targetBlock.getState() instanceof Chest)) {
                player.sendMessage(plugin.getConfigManager().getMessage("shop.creation.chest-not-found"));
                return true;
            }

            Location chestLocation = targetBlock.getLocation();

            // Prüfen ob bereits Shop
            if (plugin.getShopManager().isChestShop(chestLocation)) {
                player.sendMessage(plugin.getConfigManager().getMessage("shop.creation.already-exists"));
                return true;
            }

            // Schild finden
            Location signLocation = findNearbySign(chestLocation);
            if (signLocation == null) {
                player.sendMessage(plugin.getConfigManager().getMessage("shop.creation.failed"));
                return true;
            }

            // Erstellungskosten abziehen
            if (creationCost > 0) {
                plugin.getEconomyManager().withdraw(player, creationCost);
            }

            // Shop erstellen
            String shopId = plugin.getShopManager().generateShopId();
            Shop shop = plugin.getShopManager().createShop(shopId, player.getUniqueId(), player.getName(),
                    chestLocation, signLocation, material, amount, buyPrice, sellPrice);

            // Schild aktualisieren
            SignUtil.updateShopSign(shop, plugin);

            player.sendMessage(plugin.getConfigManager().getMessage("shop.creation.success"));

        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getConfigManager().getMessage("general.invalid-number", "%input%",
                Arrays.toString(Arrays.copyOfRange(args, 2, args.length))));
        } catch (IllegalArgumentException e) {
            player.sendMessage(plugin.getConfigManager().getMessage("errors.item-not-found", "%item%", args[1]));
        }

        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("general.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("chestshop.remove")) {
            player.sendMessage(plugin.getConfigManager().getMessage("general.no-permission"));
            return true;
        }

        RayTraceResult rayTrace = player.rayTraceBlocks(5);
        if (rayTrace == null || rayTrace.getHitBlock() == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("shop.removal.not-shop"));
            return true;
        }

        Block targetBlock = rayTrace.getHitBlock();
        Shop shop = plugin.getShopManager().getShopByLocation(targetBlock.getLocation());

        if (shop == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("shop.removal.not-shop"));
            return true;
        }

        if (!shop.getOwnerId().equals(player.getUniqueId()) && !player.hasPermission("chestshop.admin")) {
            player.sendMessage(plugin.getConfigManager().getMessage("shop.removal.not-owner"));
            return true;
        }

        plugin.getShopManager().removeShop(shop.getId());
        
        // Schild leeren
        Sign sign = shop.getSign();
        if (sign != null) {
            sign.setLine(0, "");
            sign.setLine(1, "");
            sign.setLine(2, "");
            sign.setLine(3, "");
            sign.update();
        }

        player.sendMessage(plugin.getConfigManager().getMessage("shop.removal.success"));
        return true;
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("general.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("chestshop.info")) {
            player.sendMessage(plugin.getConfigManager().getMessage("general.no-permission"));
            return true;
        }

        RayTraceResult rayTrace = player.rayTraceBlocks(5);
        if (rayTrace == null || rayTrace.getHitBlock() == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("shop.removal.not-shop"));
            return true;
        }

        Block targetBlock = rayTrace.getHitBlock();
        Shop shop = plugin.getShopManager().getShopByLocation(targetBlock.getLocation());

        if (shop == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("shop.removal.not-shop"));
            return true;
        }

        sendShopInfo(player, shop);
        return true;
    }

    private boolean handleList(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("general.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("chestshop.list")) {
            player.sendMessage(plugin.getConfigManager().getMessage("general.no-permission"));
            return true;
        }

        List<Shop> shops;
        String headerKey;
        boolean isAdmin = false;

        if (args.length > 1 && player.hasPermission("chestshop.admin")) {
            Player targetPlayer = plugin.getServer().getPlayer(args[1]);
            if (targetPlayer == null) {
                player.sendMessage(plugin.getConfigManager().getMessage("general.player-not-found", "%player%", args[1]));
                return true;
            }
            shops = plugin.getShopManager().getShopsByOwner(targetPlayer.getUniqueId());
            headerKey = "shop.admin-list.header";
            isAdmin = true;
        } else {
            shops = plugin.getShopManager().getShopsByOwner(player.getUniqueId());
            headerKey = "shop.list.header";
        }

        if (shops.isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getMessage(isAdmin ? "shop.admin-list.no-shops" : "shop.list.no-shops"));
            return true;
        }

        player.sendMessage(plugin.getConfigManager().getMessage(headerKey, "%count%", String.valueOf(shops.size())));
        
        for (int i = 0; i < shops.size(); i++) {
            Shop shop = shops.get(i);
            String entryKey = isAdmin ? "shop.admin-list.entry" : "shop.list.entry";
            
            String message = plugin.getConfigManager().getMessage(entryKey,
                "%id%", String.valueOf(i + 1),
                "%owner%", shop.getOwnerName(),
                "%item%", shop.getItem().name(),
                "%buy%", plugin.getEconomyManager().formatSimple(shop.getBuyPrice()),
                "%sell%", plugin.getEconomyManager().formatSimple(shop.getSellPrice()),
                "%world%", shop.getChestLocation().getWorld().getName(),
                "%x%", String.valueOf(shop.getChestLocation().getBlockX()),
                "%y%", String.valueOf(shop.getChestLocation().getBlockY()),
                "%z%", String.valueOf(shop.getChestLocation().getBlockZ()));
            
            player.sendMessage(message);
        }

        player.sendMessage(plugin.getConfigManager().getMessage("shop.list.footer"));
        return true;
    }

    private boolean handleReload(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chestshop.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("general.no-permission"));
            return true;
        }

        plugin.getConfigManager().reloadConfigs();
        plugin.getShopManager().loadShops();
        sender.sendMessage(plugin.getConfigManager().getMessage("general.config-reloaded"));
        return true;
    }

    private boolean handleToggle(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("general.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("chestshop.toggle")) {
            player.sendMessage(plugin.getConfigManager().getMessage("general.no-permission"));
            return true;
        }

        RayTraceResult rayTrace = player.rayTraceBlocks(5);
        if (rayTrace == null || rayTrace.getHitBlock() == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("shop.removal.not-shop"));
            return true;
        }

        Block targetBlock = rayTrace.getHitBlock();
        Shop shop = plugin.getShopManager().getShopByLocation(targetBlock.getLocation());

        if (shop == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("shop.removal.not-shop"));
            return true;
        }

        if (!shop.getOwnerId().equals(player.getUniqueId()) && !player.hasPermission("chestshop.admin")) {
            player.sendMessage(plugin.getConfigManager().getMessage("shop.removal.not-owner"));
            return true;
        }

        shop.setActive(!shop.isActive());
        SignUtil.updateShopSign(shop, plugin);

        String status = shop.isActive() ? 
            plugin.getConfigManager().getMessage("shop.status.active") : 
            plugin.getConfigManager().getMessage("shop.status.inactive");
        
        player.sendMessage(plugin.getConfigManager().getMessage("shop.status-changed", "%status%", status));
        return true;
    }

    private boolean handleRefill(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("general.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("chestshop.refill")) {
            player.sendMessage(plugin.getConfigManager().getMessage("general.no-permission"));
            return true;
        }

        // Shop finden
        RayTraceResult rayTrace = player.rayTraceBlocks(5);
        if (rayTrace == null || rayTrace.getHitBlock() == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("shop.removal.not-shop"));
            return true;
        }

        Block targetBlock = rayTrace.getHitBlock();
        Shop shop = plugin.getShopManager().getShopByLocation(targetBlock.getLocation());

        if (shop == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("shop.removal.not-shop"));
            return true;
        }

        // Prüfen ob Spieler Shop-Besitzer ist
        if (!shop.getOwnerId().equals(player.getUniqueId()) && !player.hasPermission("chestshop.admin")) {
            player.sendMessage(plugin.getConfigManager().getMessage("shop.refill.not-owner"));
            return true;
        }

        // Refill-Menge bestimmen
        int refillAmount = shop.getAmount();
        if (args.length > 1) {
            try {
                refillAmount = Integer.parseInt(args[1]);
                if (refillAmount <= 0) {
                    player.sendMessage(plugin.getConfigManager().getMessage("errors.invalid-amount"));
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getConfigManager().getMessage("general.invalid-number", "%input%", args[1]));
                return true;
            }
        }

        // Prüfen ob Spieler genug Items hat
        int playerItemCount = 0;
        for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == shop.getItem()) {
                playerItemCount += item.getAmount();
            }
        }

        if (playerItemCount < refillAmount) {
            player.sendMessage(plugin.getConfigManager().getMessage("shop.refill.not-enough-items",
                "%needed%", String.valueOf(refillAmount),
                "%have%", String.valueOf(playerItemCount),
                "%item%", shop.getItem().name()));
            return true;
        }

        // Prüfen ob genug Platz im Shop ist
        int availableSpace = shop.getAvailableSpace();
        if (availableSpace < refillAmount) {
            player.sendMessage(plugin.getConfigManager().getMessage("shop.refill.not-enough-space",
                "%needed%", String.valueOf(refillAmount),
                "%space%", String.valueOf(availableSpace)));
            return true;
        }

        // Items aus Player-Inventar entfernen
        int remainingToRemove = refillAmount;
        for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == shop.getItem() && remainingToRemove > 0) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remainingToRemove) {
                    remainingToRemove -= itemAmount;
                    item.setAmount(0);
                } else {
                    item.setAmount(itemAmount - remainingToRemove);
                    remainingToRemove = 0;
                }
            }
        }

        // Items zur Shop-Truhe hinzufügen
        Chest chest = shop.getChest();
        if (chest != null) {
            org.bukkit.inventory.ItemStack shopItem = new org.bukkit.inventory.ItemStack(shop.getItem(), refillAmount);
            chest.getInventory().addItem(shopItem);
        }

        // Schild aktualisieren
        SignUtil.updateShopSign(shop, plugin);

        player.sendMessage(plugin.getConfigManager().getMessage("shop.refill.success",
            "%amount%", String.valueOf(refillAmount),
            "%item%", shop.getItem().name(),
            "%stock%", String.valueOf(shop.getStock())));

        return true;
    }

    private boolean handleSearch(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("general.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("chestshop.search")) {
            player.sendMessage(plugin.getConfigManager().getMessage("general.no-permission"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getMessage("commands.usage.search"));
            return true;
        }

        String searchType = args[1].toLowerCase();
        List<Shop> results = new ArrayList<>();

        switch (searchType) {
            case "item":
                if (args.length < 3) {
                    player.sendMessage(plugin.getConfigManager().getMessage("commands.usage.search"));
                    return true;
                }
                try {
                    Material item = Material.valueOf(args[2].toUpperCase());
                    results = StatisticsUtil.searchShopsByItem(item, plugin);
                } catch (IllegalArgumentException e) {
                    player.sendMessage(plugin.getConfigManager().getMessage("errors.item-not-found", "%item%", args[2]));
                    return true;
                }
                break;
                
            case "owner":
                if (args.length < 3) {
                    player.sendMessage(plugin.getConfigManager().getMessage("commands.usage.search"));
                    return true;
                }
                results = StatisticsUtil.searchShopsByOwner(args[2], plugin);
                break;
                
            case "price":
                if (args.length < 5) {
                    player.sendMessage(plugin.getConfigManager().getMessage("commands.usage.search"));
                    return true;
                }
                try {
                    double minPrice = Double.parseDouble(args[2]);
                    double maxPrice = Double.parseDouble(args[3]);
                    boolean isBuyPrice = args[4].equalsIgnoreCase("buy");
                    results = StatisticsUtil.searchShopsByPriceRange(minPrice, maxPrice, isBuyPrice, plugin);
                } catch (NumberFormatException e) {
                    player.sendMessage(plugin.getConfigManager().getMessage("general.invalid-number", "%input%", args[2] + ", " + args[3]));
                    return true;
                }
                break;
                
            default:
                player.sendMessage(plugin.getConfigManager().getMessage("commands.usage.search"));
                return true;
        }

        if (results.isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getMessage("search.no-results"));
            return true;
        }

        player.sendMessage(plugin.getConfigManager().getMessage("search.results-header", "%count%", String.valueOf(results.size())));
        
        int maxResults = plugin.getConfigManager().getConfig().getInt("search.max-results", 10);
        for (int i = 0; i < Math.min(results.size(), maxResults); i++) {
            Shop shop = results.get(i);
            String message = plugin.getConfigManager().getMessage("search.result-entry",
                "%id%", String.valueOf(i + 1),
                "%owner%", shop.getOwnerName(),
                "%item%", shop.getItem().name(),
                "%amount%", String.valueOf(shop.getAmount()),
                "%buy%", plugin.getEconomyManager().formatSimple(shop.getBuyPrice()),
                "%sell%", plugin.getEconomyManager().formatSimple(shop.getSellPrice()),
                "%world%", shop.getChestLocation().getWorld().getName(),
                "%x%", String.valueOf(shop.getChestLocation().getBlockX()),
                "%y%", String.valueOf(shop.getChestLocation().getBlockY()),
                "%z%", String.valueOf(shop.getChestLocation().getBlockZ()));
            
            player.sendMessage(message);
        }

        if (results.size() > maxResults) {
            player.sendMessage(plugin.getConfigManager().getMessage("search.too-many-results", 
                "%max%", String.valueOf(maxResults)));
        }

        return true;
    }

    private boolean handleStats(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chestshop.stats")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("general.no-permission"));
            return true;
        }

        if (args.length > 1 && args[1].equalsIgnoreCase("global")) {
            // Global statistics
            Map<String, Object> stats = StatisticsUtil.getGlobalStatistics(plugin);
            
            sender.sendMessage(plugin.getConfigManager().getMessage("stats.global.header"));
            sender.sendMessage(plugin.getConfigManager().getMessage("stats.global.total-shops", 
                "%count%", stats.get("totalShops").toString()));
            sender.sendMessage(plugin.getConfigManager().getMessage("stats.global.active-shops", 
                "%count%", stats.get("activeShops").toString()));
            sender.sendMessage(plugin.getConfigManager().getMessage("stats.global.total-transactions", 
                "%count%", stats.get("totalTransactions").toString()));
            sender.sendMessage(plugin.getConfigManager().getMessage("stats.global.unique-items", 
                "%count%", stats.get("uniqueItems").toString()));
            sender.sendMessage(plugin.getConfigManager().getMessage("stats.global.average-age", 
                "%days%", String.format("%.1f", (Double) stats.get("averageShopAge"))));
                
        } else if (args.length > 1 && sender.hasPermission("chestshop.admin")) {
            // Admin viewing specific player stats
            Player targetPlayer = plugin.getServer().getPlayer(args[1]);
            if (targetPlayer == null) {
                sender.sendMessage(plugin.getConfigManager().getMessage("general.player-not-found", "%player%", args[1]));
                return true;
            }
            
            displayDetailedPlayerStats(sender, targetPlayer, plugin);
                
        } else if (sender instanceof Player) {
            // Player statistics
            Player player = (Player) sender;
            displayDetailedPlayerStats(sender, player, plugin);
        } else {
            sender.sendMessage(plugin.getConfigManager().getMessage("general.player-only"));
        }

        return true;
    }

    private boolean handlePrice(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("general.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("chestshop.price")) {
            player.sendMessage(plugin.getConfigManager().getMessage("general.no-permission"));
            return true;
        }

        if (args.length < 3) {
            player.sendMessage(plugin.getConfigManager().getMessage("commands.usage.price"));
            return true;
        }

        try {
            double newBuyPrice = Double.parseDouble(args[1]);
            double newSellPrice = Double.parseDouble(args[2]);

            // Find shop
            RayTraceResult rayTrace = player.rayTraceBlocks(5);
            if (rayTrace == null || rayTrace.getHitBlock() == null) {
                player.sendMessage(plugin.getConfigManager().getMessage("shop.removal.not-shop"));
                return true;
            }

            Block targetBlock = rayTrace.getHitBlock();
            Shop shop = plugin.getShopManager().getShopByLocation(targetBlock.getLocation());

            if (shop == null) {
                player.sendMessage(plugin.getConfigManager().getMessage("shop.removal.not-shop"));
                return true;
            }

            if (!shop.getOwnerId().equals(player.getUniqueId()) && !player.hasPermission("chestshop.admin")) {
                player.sendMessage(plugin.getConfigManager().getMessage("shop.removal.not-owner"));
                return true;
            }

            // Validate prices
            if (!isValidPrice(newBuyPrice, "buy") || !isValidPrice(newSellPrice, "sell")) {
                double minBuy = plugin.getConfigManager().getConfig().getDouble("shop.price-limits.min-buy-price");
                double maxBuy = plugin.getConfigManager().getConfig().getDouble("shop.price-limits.max-buy-price");
                player.sendMessage(plugin.getConfigManager().getMessage("shop.creation.invalid-price", 
                    "%min%", String.valueOf(minBuy), "%max%", String.valueOf(maxBuy)));
                return true;
            }

            // Update prices
            shop.setBuyPrice(newBuyPrice);
            shop.setSellPrice(newSellPrice);

            // Update sign
            SignUtil.updateShopSign(shop, plugin);

            player.sendMessage(plugin.getConfigManager().getMessage("shop.price-updated"));

        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getConfigManager().getMessage("general.invalid-number", "%input%", args[1] + ", " + args[2]));
        }

        return true;
    }

    private boolean handleAdmin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("chestshop.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("general.no-permission"));
            return true;
        }

        if (args.length < 2) {
            sendAdminHelp(sender);
            return true;
        }

        String adminCommand = args[1].toLowerCase();

        switch (adminCommand) {
            case "removeall":
                if (args.length < 3) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("commands.usage.admin-removeall"));
                    return true;
                }
                return handleAdminRemoveAll(sender, args[2]);
                
            case "holograms":
            case "hologram":
                if (args.length < 3) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("commands.usage.admin-holograms"));
                    return true;
                }
                return handleAdminHolograms(sender, args[2]);
                
            case "cleanup":
                return handleAdminCleanup(sender);
                
            case "stats":
                return handleAdminStats(sender);
                
            case "reset":
                if (args.length < 3) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("commands.usage.admin-reset"));
                    return true;
                }
                return handleAdminReset(sender, args[2]);
                
            case "maintenance":
                return handleAdminMaintenance(sender);
                
            case "backup":
                return handleAdminBackup(sender);
                
            case "reload":
                return handleReload(sender, args);
                
            default:
                sendAdminHelp(sender);
                return true;
        }
    }

    private boolean handleAdminRemoveAll(CommandSender sender, String playerName) {
        Player targetPlayer = plugin.getServer().getPlayer(playerName);
        UUID targetId;
        
        if (targetPlayer != null) {
            targetId = targetPlayer.getUniqueId();
        } else {
            // Try to find offline player - simplified approach
            sender.sendMessage(plugin.getConfigManager().getMessage("general.player-not-found", "%player%", playerName));
            return true;
        }

        List<Shop> playerShops = plugin.getShopManager().getShopsByOwner(targetId);
        int removedCount = 0;
        
        for (Shop shop : new ArrayList<>(playerShops)) {
            plugin.getShopManager().removeShop(shop.getId());
            removedCount++;
        }

        sender.sendMessage(plugin.getConfigManager().getMessage("admin.shops-removed", 
            "%count%", String.valueOf(removedCount), "%player%", playerName));
        return true;
    }

    private boolean handleAdminHolograms(CommandSender sender, String action) {
        switch (action.toLowerCase()) {
            case "reload":
                HologramUtil.recreateAllHolograms(plugin);
                sender.sendMessage(plugin.getConfigManager().getMessage("admin.holograms-reloaded"));
                break;
                
            case "remove":
                HologramUtil.removeAllHolograms();
                sender.sendMessage(plugin.getConfigManager().getMessage("admin.holograms-removed"));
                break;
                
            default:
                sender.sendMessage(plugin.getConfigManager().getMessage("commands.usage.admin-holograms"));
                return true;
        }
        return true;
    }

    private boolean handleAdminCleanup(CommandSender sender) {
        int removedCount = 0;
        List<Shop> allShops = new ArrayList<>(plugin.getShopManager().getAllShops());
        
        for (Shop shop : allShops) {
            // Remove shops with invalid chests or signs
            if (shop.getChest() == null || shop.getSign() == null) {
                plugin.getShopManager().removeShop(shop.getId());
                removedCount++;
            }
        }

        sender.sendMessage(plugin.getConfigManager().getMessage("admin.cleanup-complete", 
            "%count%", String.valueOf(removedCount)));
        return true;
    }

    private boolean handleAdminStats(CommandSender sender) {
        Map<String, Object> stats = StatisticsUtil.getGlobalStatistics(plugin);
        
        sender.sendMessage(plugin.getConfigManager().getMessage("admin.stats.header"));
        sender.sendMessage(plugin.getConfigManager().getMessage("admin.stats.total-shops", 
            "%count%", stats.get("totalShops").toString()));
        sender.sendMessage(plugin.getConfigManager().getMessage("admin.stats.active-shops", 
            "%count%", stats.get("activeShops").toString()));
        sender.sendMessage(plugin.getConfigManager().getMessage("admin.stats.total-transactions", 
            "%count%", stats.get("totalTransactions").toString()));
        
        // Most popular items
        List<Map.Entry<String, Integer>> popularItems = StatisticsUtil.getMostPopularItems(5);
        if (!popularItems.isEmpty()) {
            sender.sendMessage(plugin.getConfigManager().getMessage("admin.stats.popular-items"));
            for (int i = 0; i < popularItems.size(); i++) {
                Map.Entry<String, Integer> entry = popularItems.get(i);
                sender.sendMessage(plugin.getConfigManager().getMessage("admin.stats.popular-item-entry",
                    "%rank%", String.valueOf(i + 1),
                    "%item%", entry.getKey(),
                    "%count%", String.valueOf(entry.getValue())));
            }
        }
        
        return true;
    }

    private boolean handleAdminReset(CommandSender sender, String playerName) {
        Player targetPlayer = plugin.getServer().getPlayer(playerName);
        if (targetPlayer == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("general.player-not-found", "%player%", playerName));
            return true;
        }

        // Reset player statistics using StatisticsUtil
        StatisticsUtil.resetPlayerStatistics(targetPlayer.getUniqueId());
        
        sender.sendMessage(plugin.getConfigManager().getMessage("admin.player-data-reset", "%player%", playerName));
        plugin.getLogger().info("Player data reset for " + playerName + " by " + sender.getName());
        
        return true;
    }

    private boolean handleAdminMaintenance(CommandSender sender) {
        // Toggle maintenance mode
        boolean currentMode = plugin.getConfigManager().getConfig().getBoolean("general.maintenance-mode", false);
        boolean newMode = !currentMode;
        
        // Update config (in memory)
        plugin.getConfigManager().getConfig().set("general.maintenance-mode", newMode);
        
        // Notify
        if (newMode) {
            sender.sendMessage(plugin.getConfigManager().getMessage("admin.maintenance-mode-enabled"));
            // Notify all online players about maintenance
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (!player.hasPermission("chestshop.admin")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("admin.maintenance-mode-enabled"));
                }
            }
        } else {
            sender.sendMessage(plugin.getConfigManager().getMessage("admin.maintenance-mode-disabled"));
            // Notify all online players that shops are active again
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (!player.hasPermission("chestshop.admin")) {
                    player.sendMessage(plugin.getConfigManager().getMessage("admin.maintenance-mode-disabled"));
                }
            }
        }
        
        plugin.getLogger().info("Maintenance mode " + (newMode ? "enabled" : "disabled") + " by " + sender.getName());
        return true;
    }

    private boolean handleAdminBackup(CommandSender sender) {
        try {
            sender.sendMessage(plugin.getConfigManager().getMessage("backup.started"));
            
            // Create backup directory if it doesn't exist
            File backupDir = new File(plugin.getDataFolder(), "backups");
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
            
            // Generate backup filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String filename = "shops_backup_" + timestamp + ".json";
            File backupFile = new File(backupDir, filename);
            
            // Collect all shop data
            List<Shop> allShops = plugin.getShopManager().getAllShops();
            
            // Write to JSON file using manual serialization to avoid Gson reflection issues
            try (PrintWriter writer = new PrintWriter(new FileWriter(backupFile))) {
                writer.println("{");
                writer.println("  \"timestamp\": \"" + timestamp + "\",");
                writer.println("  \"version\": \"" + plugin.getDescription().getVersion() + "\",");
                writer.println("  \"shop_count\": " + allShops.size() + ",");
                writer.println("  \"shops\": [");
                
                for (int i = 0; i < allShops.size(); i++) {
                    Shop shop = allShops.get(i);
                    writer.println("    {");
                    writer.println("      \"id\": \"" + escapeJson(shop.getId()) + "\",");
                    writer.println("      \"owner_id\": \"" + shop.getOwnerId().toString() + "\",");
                    writer.println("      \"owner_name\": \"" + escapeJson(shop.getOwnerName()) + "\",");
                    writer.println("      \"item\": \"" + shop.getItem().name() + "\",");
                    writer.println("      \"amount\": " + shop.getAmount() + ",");
                    writer.println("      \"buy_price\": " + shop.getBuyPrice() + ",");
                    writer.println("      \"sell_price\": " + shop.getSellPrice() + ",");
                    writer.println("      \"active\": " + shop.isActive() + ",");
                    writer.println("      \"created\": " + shop.getCreated() + ",");
                    writer.println("      \"last_used\": " + shop.getLastUsed() + ",");
                    
                    // Location data
                    Location chest = shop.getChestLocation();
                    writer.println("      \"chest_location\": {");
                    writer.println("        \"world\": \"" + chest.getWorld().getName() + "\",");
                    writer.println("        \"x\": " + chest.getBlockX() + ",");
                    writer.println("        \"y\": " + chest.getBlockY() + ",");
                    writer.println("        \"z\": " + chest.getBlockZ());
                    writer.println("      },");
                    
                    Location sign = shop.getSignLocation();
                    writer.println("      \"sign_location\": {");
                    writer.println("        \"world\": \"" + sign.getWorld().getName() + "\",");
                    writer.println("        \"x\": " + sign.getBlockX() + ",");
                    writer.println("        \"y\": " + sign.getBlockY() + ",");
                    writer.println("        \"z\": " + sign.getBlockZ());
                    writer.println("      }");
                    
                    writer.print("    }");
                    if (i < allShops.size() - 1) {
                        writer.println(",");
                    } else {
                        writer.println();
                    }
                }
                
                writer.println("  ]");
                writer.println("}");
            }
            
            sender.sendMessage(plugin.getConfigManager().getMessage("backup.completed", "%filename%", filename));
            plugin.getLogger().info("Shop backup created: " + filename + " by " + sender.getName());
            
        } catch (IOException e) {
            sender.sendMessage(plugin.getConfigManager().getMessage("backup.failed", "%reason%", e.getMessage()));
            plugin.getLogger().severe("Backup failed: " + e.getMessage());
        }
        
        return true;
    }
    
    /**
     * Escapes special characters in JSON strings
     */
    private String escapeJson(String input) {
        if (input == null) {
            return "null";
        }
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    private void sendAdminHelp(CommandSender sender) {
        sender.sendMessage(plugin.getConfigManager().getMessage("commands.admin-help.header"));
        sender.sendMessage(plugin.getConfigManager().getMessage("commands.admin-help.removeall"));
        sender.sendMessage(plugin.getConfigManager().getMessage("commands.admin-help.holograms"));
        sender.sendMessage(plugin.getConfigManager().getMessage("commands.admin-help.cleanup"));
        sender.sendMessage(plugin.getConfigManager().getMessage("commands.admin-help.stats"));
        sender.sendMessage(plugin.getConfigManager().getMessage("commands.admin-help.reset-command"));
        sender.sendMessage(plugin.getConfigManager().getMessage("commands.admin-help.maintenance-command"));
        sender.sendMessage(plugin.getConfigManager().getMessage("commands.admin-help.backup-command"));
        sender.sendMessage(plugin.getConfigManager().getMessage("commands.admin-help.reload-command"));
        sender.sendMessage(plugin.getConfigManager().getMessage("commands.admin-help.footer"));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.getConfigManager().getMessage("commands.help.header"));
        sender.sendMessage(plugin.getConfigManager().getMessage("commands.help.create"));
        sender.sendMessage(plugin.getConfigManager().getMessage("commands.help.remove"));
        sender.sendMessage(plugin.getConfigManager().getMessage("commands.help.info"));
        sender.sendMessage(plugin.getConfigManager().getMessage("commands.help.list"));
        sender.sendMessage(plugin.getConfigManager().getMessage("commands.help.toggle"));
        sender.sendMessage(plugin.getConfigManager().getMessage("commands.help.refill"));
        
        if (sender.hasPermission("chestshop.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("commands.help.reload"));
        }
        
        sender.sendMessage(plugin.getConfigManager().getMessage("commands.help.footer"));
    }

    private void sendShopInfo(Player player, Shop shop) {
        player.sendMessage(plugin.getConfigManager().getMessage("shop.info.header"));
        player.sendMessage(plugin.getConfigManager().getMessage("shop.info.owner", "%owner%", shop.getOwnerName()));
        player.sendMessage(plugin.getConfigManager().getMessage("shop.info.item", "%item%", shop.getItem().name()));
        player.sendMessage(plugin.getConfigManager().getMessage("shop.info.amount", "%amount%", String.valueOf(shop.getAmount())));
        
        if (shop.hasBuyPrice()) {
            player.sendMessage(plugin.getConfigManager().getMessage("shop.info.buy-price", 
                "%price%", plugin.getEconomyManager().format(shop.getBuyPrice())));
        }
        
        if (shop.hasSellPrice()) {
            player.sendMessage(plugin.getConfigManager().getMessage("shop.info.sell-price", 
                "%price%", plugin.getEconomyManager().format(shop.getSellPrice())));
        }
        
        player.sendMessage(plugin.getConfigManager().getMessage("shop.info.stock", "%stock%", String.valueOf(shop.getStock())));
        
        String statusKey = "shop.status." + shop.getStatus().name().toLowerCase().replace("_", "-");
        player.sendMessage(plugin.getConfigManager().getMessage("shop.info.status", 
            "%status%", plugin.getConfigManager().getMessage(statusKey)));
        
        Location loc = shop.getChestLocation();
        player.sendMessage(plugin.getConfigManager().getMessage("shop.info.location",
            "%world%", loc.getWorld().getName(),
            "%x%", String.valueOf(loc.getBlockX()),
            "%y%", String.valueOf(loc.getBlockY()),
            "%z%", String.valueOf(loc.getBlockZ())));
        
        player.sendMessage(plugin.getConfigManager().getMessage("shop.info.footer"));
    }

    private Location findNearbySign(Location chestLocation) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    
                    Location loc = chestLocation.clone().add(x, y, z);
                    Block block = loc.getBlock();
                    
                    if (block.getState() instanceof Sign) {
                        return loc;
                    }
                }
            }
        }
        return null;
    }

    private boolean isItemAllowed(Material material) {
        List<String> bannedItems = plugin.getConfigManager().getConfig().getStringList("shop.creation.banned-items");
        if (bannedItems.contains(material.name())) {
            return false;
        }
        
        List<String> allowedItems = plugin.getConfigManager().getConfig().getStringList("shop.creation.allowed-items");
        return allowedItems.isEmpty() || allowedItems.contains(material.name());
    }

    private boolean isWorldAllowed(String worldName) {
        List<String> allowedWorlds = plugin.getConfigManager().getConfig().getStringList("general.allowed-worlds");
        return allowedWorlds.isEmpty() || allowedWorlds.contains(worldName);
    }

    private boolean isValidPrice(double price, String type) {
        if (price <= 0) return true; // 0 bedeutet deaktiviert
        
        String minKey = "shop.price-limits.min-" + type + "-price";
        String maxKey = "shop.price-limits.max-" + type + "-price";
        
        double min = plugin.getConfigManager().getConfig().getDouble(minKey);
        double max = plugin.getConfigManager().getConfig().getDouble(maxKey);
        
        return price >= min && price <= max;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("create", "remove", "info", "list", "toggle", "refill", "search", "stats", "price", "help"));
            if (sender.hasPermission("chestshop.admin")) {
                completions.addAll(Arrays.asList("reload", "admin"));
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            // Material-Namen vorschlagen
            return Arrays.stream(Material.values())
                    .filter(m -> m.isItem() && !m.isAir())
                    .map(Material::name)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            // Admin subcommands
            if (sender.hasPermission("chestshop.admin")) {
                completions.addAll(Arrays.asList("removeall", "hologram", "cleanup", "stats", "reload", "reset", "maintenance", "backup"));
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("list")) {
            if (sender.hasPermission("chestshop.admin")) {
                return plugin.getServer().getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("stats")) {
            // Stats can show global stats or specific player stats if admin
            completions.add("global");
            if (sender.hasPermission("chestshop.admin")) {
                completions.addAll(plugin.getServer().getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList()));
            }
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("search")) {
            // Search types
            completions.addAll(Arrays.asList("item", "owner", "price"));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("admin")) {
            // Admin subcommand arguments
            if (sender.hasPermission("chestshop.admin")) {
                if (args[1].equalsIgnoreCase("removeall") || args[1].equalsIgnoreCase("reset")) {
                    // Player names for removeall and reset commands
                    return plugin.getServer().getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());
                } else if (args[1].equalsIgnoreCase("hologram")) {
                    // Hologram subcommands
                    completions.addAll(Arrays.asList("reload", "remove"));
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("search") && args[1].equalsIgnoreCase("owner")) {
            // Player names for owner search
            return plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("search") && args[1].equalsIgnoreCase("item")) {
            // Material names for item search
            return Arrays.stream(Material.values())
                    .filter(m -> m.isItem() && !m.isAir())
                    .map(Material::name)
                    .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
    
    /**
     * Displays detailed player statistics
     */
    private void displayDetailedPlayerStats(CommandSender sender, Player targetPlayer, ChestShopPlugin plugin) {
        Map<String, Object> stats = StatisticsUtil.getPlayerStatistics(targetPlayer.getUniqueId(), plugin);
        Map<String, Object> activity = StatisticsUtil.getPlayerActivity(targetPlayer.getUniqueId(), plugin);
        
        sender.sendMessage(plugin.getConfigManager().getMessage("statistics.header"));
        sender.sendMessage(plugin.getConfigManager().getMessage("statistics.player-stats", "%player%", targetPlayer.getName()));
        sender.sendMessage(plugin.getConfigManager().getMessage("statistics.total-shops", 
            "%count%", stats.get("totalShops").toString()));
        sender.sendMessage(plugin.getConfigManager().getMessage("statistics.active-shops", 
            "%count%", stats.get("activeShops").toString()));
        sender.sendMessage(plugin.getConfigManager().getMessage("statistics.transactions", 
            "%count%", stats.get("totalTransactions").toString()));
        sender.sendMessage(plugin.getConfigManager().getMessage("statistics.money-earned", 
            "%amount%", plugin.getEconomyManager().format((Double) stats.get("totalEarnings"))));
        sender.sendMessage(plugin.getConfigManager().getMessage("statistics.money-spent", 
            "%amount%", plugin.getEconomyManager().format((Double) stats.get("totalSpending"))));
        
        // Show most sold item if available
        if (stats.containsKey("mostSoldItem")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("statistics.most-sold", 
                "%item%", stats.get("mostSoldItem").toString(),
                "%count%", stats.get("mostSoldCount").toString()));
        }
        
        // Activity summary with proper message keys
        sender.sendMessage(plugin.getConfigManager().getMessage("statistics.low-stock-shops", 
            "%count%", activity.get("lowStockShops").toString()));
        sender.sendMessage(plugin.getConfigManager().getMessage("statistics.full-shops", 
            "%count%", activity.get("fullShops").toString()));
        sender.sendMessage(plugin.getConfigManager().getMessage("statistics.total-stock", 
            "%count%", activity.get("totalStock").toString()));
        
        // Calculate profit if both earnings and spending exist
        double earnings = (Double) stats.get("totalEarnings");
        double spending = (Double) stats.get("totalSpending");
        double profit = earnings - spending;
        String profitColor = profit >= 0 ? "&a+" : "&c";
        sender.sendMessage(plugin.getConfigManager().getMessage("statistics.net-profit", 
            "%color%", profitColor, "%amount%", plugin.getEconomyManager().format(Math.abs(profit))));
    }
}
