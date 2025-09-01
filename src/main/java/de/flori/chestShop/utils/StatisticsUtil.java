package de.flori.chestShop.utils;

import de.flori.chestShop.ChestShopPlugin;
import de.flori.chestShop.models.Shop;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class StatisticsUtil {
    
    private static final Map<String, Integer> itemSales = new HashMap<>();
    private static final Map<String, Integer> itemPurchases = new HashMap<>();
    private static final Map<UUID, Double> playerSpending = new HashMap<>();
    private static final Map<UUID, Double> playerEarnings = new HashMap<>();
    private static final Map<String, Long> shopTransactions = new HashMap<>();
    
    /**
     * Records a purchase transaction
     */
    public static void recordPurchase(String item, int amount, UUID buyerId, double price, String shopId) {
        itemPurchases.merge(item, amount, Integer::sum);
        playerSpending.merge(buyerId, price, Double::sum);
        shopTransactions.merge(shopId, 1L, Long::sum);
    }
    
    /**
     * Records a sale transaction
     */
    public static void recordSale(String item, int amount, UUID sellerId, double price, String shopId) {
        itemSales.merge(item, amount, Integer::sum);
        playerEarnings.merge(sellerId, price, Double::sum);
        shopTransactions.merge(shopId, 1L, Long::sum);
    }
    
    /**
     * Gets the most popular items by transaction volume
     */
    public static List<Map.Entry<String, Integer>> getMostPopularItems(int limit) {
        Map<String, Integer> totalTransactions = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : itemSales.entrySet()) {
            totalTransactions.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
        
        for (Map.Entry<String, Integer> entry : itemPurchases.entrySet()) {
            totalTransactions.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
        
        return totalTransactions.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets shop statistics for a specific shop
     */
    public static Map<String, Object> getShopStatistics(Shop shop) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("transactions", shopTransactions.getOrDefault(shop.getId(), 0L));
        stats.put("currentStock", shop.getStock());
        stats.put("availableSpace", shop.getAvailableSpace());
        stats.put("daysSinceCreation", (System.currentTimeMillis() - shop.getCreated()) / (1000 * 60 * 60 * 24));
        stats.put("daysSinceLastUsed", (System.currentTimeMillis() - shop.getLastUsed()) / (1000 * 60 * 60 * 24));
        
        return stats;
    }
    
    /**
     * Gets global server statistics
     */
    public static Map<String, Object> getGlobalStatistics(ChestShopPlugin plugin) {
        Map<String, Object> stats = new HashMap<>();
        
        List<Shop> allShops = plugin.getShopManager().getAllShops();
        
        stats.put("totalShops", allShops.size());
        stats.put("activeShops", allShops.stream().mapToInt(shop -> shop.isActive() ? 1 : 0).sum());
        stats.put("totalTransactions", shopTransactions.values().stream().mapToLong(Long::longValue).sum());
        stats.put("uniqueItems", getUniqueItemsInShops(allShops));
        stats.put("averageShopAge", calculateAverageShopAge(allShops));
        
        return stats;
    }
    
    /**
     * Gets the most successful shop owners
     */
    public static List<Map.Entry<UUID, Double>> getTopEarners(int limit) {
        return playerEarnings.entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets the biggest spenders
     */
    public static List<Map.Entry<UUID, Double>> getTopSpenders(int limit) {
        return playerSpending.entrySet().stream()
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Searches for shops by item
     */
    public static List<Shop> searchShopsByItem(Material item, ChestShopPlugin plugin) {
        return plugin.getShopManager().getAllShops().stream()
                .filter(shop -> shop.getItem() == item && shop.isActive())
                .collect(Collectors.toList());
    }
    
    /**
     * Searches for shops by price range
     */
    public static List<Shop> searchShopsByPriceRange(double minPrice, double maxPrice, boolean isBuyPrice, ChestShopPlugin plugin) {
        return plugin.getShopManager().getAllShops().stream()
                .filter(shop -> {
                    if (!shop.isActive()) return false;
                    
                    double price = isBuyPrice ? shop.getBuyPrice() : shop.getSellPrice();
                    return price >= minPrice && price <= maxPrice && price > 0;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Searches for shops by owner
     */
    public static List<Shop> searchShopsByOwner(String ownerName, ChestShopPlugin plugin) {
        return plugin.getShopManager().getAllShops().stream()
                .filter(shop -> shop.getOwnerName().toLowerCase().contains(ownerName.toLowerCase()) && shop.isActive())
                .collect(Collectors.toList());
    }
    
    /**
     * Gets shops sorted by best buy prices for an item
     */
    public static List<Shop> getBestBuyPrices(Material item, int limit, ChestShopPlugin plugin) {
        return plugin.getShopManager().getAllShops().stream()
                .filter(shop -> shop.getItem() == item && shop.isActive() && shop.hasBuyPrice() && shop.getStock() > 0)
                .sorted(Comparator.comparingDouble(Shop::getBuyPrice))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets shops sorted by best sell prices for an item
     */
    public static List<Shop> getBestSellPrices(Material item, int limit, ChestShopPlugin plugin) {
        return plugin.getShopManager().getAllShops().stream()
                .filter(shop -> shop.getItem() == item && shop.isActive() && shop.hasSellPrice() && shop.getAvailableSpace() > 0)
                .sorted(Comparator.comparingDouble(Shop::getSellPrice).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Clears all statistics (for reset)
     */
    public static void clearStatistics() {
        itemSales.clear();
        itemPurchases.clear();
        playerSpending.clear();
        playerEarnings.clear();
        shopTransactions.clear();
    }
    
    private static int getUniqueItemsInShops(List<Shop> shops) {
        return (int) shops.stream()
                .map(Shop::getItem)
                .distinct()
                .count();
    }
    
    private static double calculateAverageShopAge(List<Shop> shops) {
        if (shops.isEmpty()) return 0;
        
        long currentTime = System.currentTimeMillis();
        double totalAge = shops.stream()
                .mapToLong(shop -> currentTime - shop.getCreated())
                .average()
                .orElse(0);
                
        return totalAge / (1000 * 60 * 60 * 24); // Convert to days
    }
    
    /**
     * Gets performance metrics for a shop
     */
    public static Map<String, Object> getShopPerformance(Shop shop) {
        Map<String, Object> performance = new HashMap<>();
        
        long transactions = shopTransactions.getOrDefault(shop.getId(), 0L);
        long daysSinceCreation = Math.max(1, (System.currentTimeMillis() - shop.getCreated()) / (1000 * 60 * 60 * 24));
        
        performance.put("transactionsPerDay", (double) transactions / daysSinceCreation);
        performance.put("totalTransactions", transactions);
        performance.put("efficiency", calculateShopEfficiency(shop));
        performance.put("status", shop.getStatus().name());
        
        return performance;
    }
    
    private static double calculateShopEfficiency(Shop shop) {
        // Efficiency based on stock utilization and activity
        int maxCapacity = 27 * shop.getItem().getMaxStackSize(); // Assuming single chest
        int currentStock = shop.getStock();
        
        if (shop.hasBuyPrice() && shop.hasSellPrice()) {
            // Buy/Sell shop - optimal stock is around 50%
            double optimalStock = maxCapacity * 0.5;
            return Math.max(0, 100 - Math.abs(currentStock - optimalStock) / optimalStock * 100);
        } else if (shop.hasBuyPrice()) {
            // Buy only shop - more stock is better
            return Math.min(100, (double) currentStock / maxCapacity * 100);
        } else if (shop.hasSellPrice()) {
            // Sell only shop - more space is better
            int availableSpace = shop.getAvailableSpace();
            return Math.min(100, (double) availableSpace / maxCapacity * 100);
        }
        
        return 0;
    }
    
    /**
     * Gets player statistics
     */
    public static Map<String, Object> getPlayerStatistics(UUID playerId, ChestShopPlugin plugin) {
        Map<String, Object> stats = new HashMap<>();
        
        List<Shop> playerShops = plugin.getShopManager().getShopsByOwner(playerId);
        
        stats.put("totalShops", playerShops.size());
        stats.put("activeShops", playerShops.stream().mapToInt(shop -> shop.isActive() ? 1 : 0).sum());
        stats.put("totalEarnings", playerEarnings.getOrDefault(playerId, 0.0));
        stats.put("totalSpending", playerSpending.getOrDefault(playerId, 0.0));
        
        // Calculate total transactions for this player's shops
        long playerTransactions = playerShops.stream()
                .mapToLong(shop -> shopTransactions.getOrDefault(shop.getId(), 0L))
                .sum();
        stats.put("totalTransactions", playerTransactions);
        
        // Find most sold item by this player
        Map<String, Integer> playerItemSales = new HashMap<>();
        for (Shop shop : playerShops) {
            String itemName = shop.getItem().name();
            int transactions = shopTransactions.getOrDefault(shop.getId(), 0L).intValue();
            playerItemSales.merge(itemName, transactions, Integer::sum);
        }
        
        if (!playerItemSales.isEmpty()) {
            Map.Entry<String, Integer> mostSold = playerItemSales.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);
            if (mostSold != null) {
                stats.put("mostSoldItem", mostSold.getKey());
                stats.put("mostSoldCount", mostSold.getValue());
            }
        }
        
        return stats;
    }
    
    /**
     * Gets server-wide statistics for admin view
     */
    public static Map<String, Object> getServerStatistics(ChestShopPlugin plugin) {
        Map<String, Object> stats = new HashMap<>();
        
        List<Shop> allShops = plugin.getShopManager().getAllShops();
        
        stats.put("totalShops", allShops.size());
        stats.put("activeShops", allShops.stream().mapToInt(shop -> shop.isActive() ? 1 : 0).sum());
        stats.put("totalTransactions", shopTransactions.values().stream().mapToLong(Long::longValue).sum());
        
        // Total money flow
        double totalEarnings = playerEarnings.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalSpending = playerSpending.values().stream().mapToDouble(Double::doubleValue).sum();
        stats.put("totalMoneyFlow", totalEarnings);
        
        // Most popular items
        List<Map.Entry<String, Integer>> popularItems = getMostPopularItems(5);
        List<String> itemNames = popularItems.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        stats.put("popularItems", String.join(", ", itemNames));
        
        // Top sellers
        List<Map.Entry<UUID, Double>> topEarners = getTopEarners(5);
        List<String> topSellerNames = new ArrayList<>();
        for (Map.Entry<UUID, Double> entry : topEarners) {
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player != null) {
                topSellerNames.add(player.getName());
            }
        }
        stats.put("topSellers", String.join(", ", topSellerNames));
        
        return stats;
    }
    
    /**
     * Resets statistics for a specific player
     */
    public static void resetPlayerStatistics(UUID playerId) {
        playerEarnings.remove(playerId);
        playerSpending.remove(playerId);
    }
    
    /**
     * Gets statistics for shops within a certain distance of a location
     */
    public static List<Shop> getNearbyShops(org.bukkit.Location location, double maxDistance, ChestShopPlugin plugin) {
        return plugin.getShopManager().getAllShops().stream()
                .filter(shop -> {
                    if (!shop.isActive()) return false;
                    if (!shop.getChestLocation().getWorld().equals(location.getWorld())) return false;
                    
                    double distance = shop.getChestLocation().distance(location);
                    return distance <= maxDistance;
                })
                .sorted(Comparator.comparingDouble(shop -> shop.getChestLocation().distance(location)))
                .collect(Collectors.toList());
    }
    
    /**
     * Gets the best shops for a specific item (best prices)
     */
    public static Map<String, List<Shop>> getBestDeals(Material item, ChestShopPlugin plugin) {
        Map<String, List<Shop>> deals = new HashMap<>();
        
        deals.put("bestBuyPrices", getBestBuyPrices(item, 5, plugin));
        deals.put("bestSellPrices", getBestSellPrices(item, 5, plugin));
        
        return deals;
    }
    
    /**
     * Calculates profit margin for shops that both buy and sell
     */
    public static double calculateAverageProfitMargin(ChestShopPlugin plugin) {
        List<Shop> tradingShops = plugin.getShopManager().getAllShops().stream()
                .filter(shop -> shop.hasBuyPrice() && shop.hasSellPrice() && shop.getBuyPrice() > 0 && shop.getSellPrice() > 0)
                .collect(Collectors.toList());
        
        if (tradingShops.isEmpty()) return 0;
        
        double totalMargin = tradingShops.stream()
                .mapToDouble(shop -> {
                    double buyPrice = shop.getBuyPrice();
                    double sellPrice = shop.getSellPrice();
                    return ((sellPrice - buyPrice) / buyPrice) * 100;
                })
                .average()
                .orElse(0);
        
        return totalMargin;
    }
    
    /**
     * Gets trending items (items with increasing sales)
     */
    public static List<String> getTrendingItems(int limit) {
        // Simple implementation - could be enhanced with time-based tracking
        return getMostPopularItems(limit).stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets shop activity summary for a player
     */
    public static Map<String, Object> getPlayerActivity(UUID playerId, ChestShopPlugin plugin) {
        Map<String, Object> activity = new HashMap<>();
        
        List<Shop> playerShops = plugin.getShopManager().getShopsByOwner(playerId);
        
        int lowStockShops = 0;
        int fullShops = 0;
        int totalStock = 0;
        
        for (Shop shop : playerShops) {
            if (!shop.isActive()) continue;
            
            int stock = shop.getStock();
            int space = shop.getAvailableSpace();
            
            totalStock += stock;
            
            // Check for low stock (less than 5 items or less than 10% of capacity)
            int maxCapacity = 27 * shop.getItem().getMaxStackSize();
            if (stock < 5 || stock < maxCapacity * 0.1) {
                lowStockShops++;
            }
            
            // Check for full shops (less than 5% available space)
            if (space < maxCapacity * 0.05) {
                fullShops++;
            }
        }
        
        activity.put("activeShops", playerShops.stream().mapToInt(shop -> shop.isActive() ? 1 : 0).sum());
        activity.put("lowStockShops", lowStockShops);
        activity.put("fullShops", fullShops);
        activity.put("totalStock", totalStock);
        activity.put("averageStock", playerShops.isEmpty() ? 0 : totalStock / playerShops.size());
        
        return activity;
    }
}
