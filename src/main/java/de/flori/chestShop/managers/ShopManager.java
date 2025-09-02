package de.flori.chestShop.managers;

import de.flori.chestShop.ChestShopPlugin;
import de.flori.chestShop.models.Shop;
import de.flori.chestShop.utils.HologramUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ShopManager {

    private final ChestShopPlugin plugin;
    private final Map<String, Shop> shops;
    private final Map<String, String> locationToShopId;

    public ShopManager(ChestShopPlugin plugin) {
        this.plugin = plugin;
        this.shops = new ConcurrentHashMap<>();
        this.locationToShopId = new ConcurrentHashMap<>();
        
        // Load shops after initialization
        plugin.getLogger().info("ShopManager initialized, now loading shops...");
        loadShops();
    }

    public void loadShops() {
        shops.clear();
        locationToShopId.clear();
        
        FileConfiguration config = plugin.getConfigManager().getShops();
        ConfigurationSection shopsSection = config.getConfigurationSection("shops");
        
        if (shopsSection == null) {
            return;
        }

        for (String shopId : shopsSection.getKeys(false)) {
            try {
                ConfigurationSection shopSection = shopsSection.getConfigurationSection(shopId);
                if (shopSection == null) continue;

                UUID ownerId = UUID.fromString(shopSection.getString("owner-id"));
                String ownerName = shopSection.getString("owner-name");
                Location chestLocation = deserializeLocation(shopSection, "chest-location");
                Location signLocation = deserializeLocation(shopSection, "sign-location");
                
                // Skip shop if locations could not be deserialized
                if (chestLocation == null || signLocation == null) {
                    plugin.getLogger().warning("Skipping shop " + shopId + " due to invalid locations (chest: " + 
                        (chestLocation != null) + ", sign: " + (signLocation != null) + ")");
                    continue;
                }
                Material item = Material.valueOf(shopSection.getString("item"));
                int amount = shopSection.getInt("amount");
                double buyPrice = shopSection.getDouble("buy-price");
                double sellPrice = shopSection.getDouble("sell-price");
                boolean active = shopSection.getBoolean("active", true);
                long created = shopSection.getLong("created");
                long lastUsed = shopSection.getLong("last-used");

                Shop shop = new Shop(shopId, ownerId, ownerName, chestLocation, signLocation, item, amount, buyPrice, sellPrice);
                shop.setActive(active);
                shop.setCreated(created);
                shop.setLastUsed(lastUsed);
                
                // Load item trading data if available
                boolean isItemTradingShop = shopSection.getBoolean("item-trading.enabled", false);
                if (isItemTradingShop) {
                    plugin.getLogger().info("Loading item trading shop " + shopId);
                    shop.setItemTradingShop(true);
                    
                    String buyItemName = shopSection.getString("item-trading.buy-item");
                    if (buyItemName != null) {
                        try {
                            shop.setBuyItemType(Material.valueOf(buyItemName));
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid buy item type for shop " + shopId + ": " + buyItemName);
                        }
                    }
                    
                    int buyItemAmount = shopSection.getInt("item-trading.buy-amount", 1);
                    shop.setBuyItemAmount(buyItemAmount);
                    
                    String sellItemName = shopSection.getString("item-trading.sell-item");
                    if (sellItemName != null) {
                        try {
                            shop.setSellItemType(Material.valueOf(sellItemName));
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid sell item type for shop " + shopId + ": " + sellItemName);
                        }
                    }
                    
                    int sellItemAmount = shopSection.getInt("item-trading.sell-amount", 1);
                    shop.setSellItemAmount(sellItemAmount);
                    
                    plugin.getLogger().info("Loaded item trading shop " + shopId + ": " + 
                        buyItemAmount + "x " + buyItemName + " -> " + sellItemAmount + "x " + sellItemName);
                    
                    // Load item meta if available (for future use)
                    ConfigurationSection buyItemMetaSection = shopSection.getConfigurationSection("item-trading.buy-item-meta");
                    if (buyItemMetaSection != null) {
                        Map<String, Object> buyItemMeta = new HashMap<>();
                        for (String key : buyItemMetaSection.getKeys(false)) {
                            buyItemMeta.put(key, buyItemMetaSection.get(key));
                        }
                        shop.setBuyItemMeta(buyItemMeta);
                    }
                    
                    ConfigurationSection sellItemMetaSection = shopSection.getConfigurationSection("item-trading.sell-item-meta");
                    if (sellItemMetaSection != null) {
                        Map<String, Object> sellItemMeta = new HashMap<>();
                        for (String key : sellItemMetaSection.getKeys(false)) {
                            sellItemMeta.put(key, sellItemMetaSection.get(key));
                        }
                        shop.setSellItemMeta(sellItemMeta);
                    }
                }

                shops.put(shopId, shop);
                
                // Only add to location mapping if locations are valid
                if (chestLocation != null) {
                    locationToShopId.put(locationToString(chestLocation), shopId);
                }
                if (signLocation != null) {
                    locationToShopId.put(locationToString(signLocation), shopId);
                }
                
                plugin.getLogger().info("Successfully loaded shop " + shopId + " for " + ownerName + 
                    (shop.isItemTradingShop() ? " (Item Trading)" : " (Regular)"));

            } catch (Exception e) {
                plugin.getLogger().warning("Error loading shop " + shopId + ": " + e.getMessage());
            }
        }

        plugin.getLogger().info("Shops loaded: " + shops.size());
    }

    public void saveAllShops() {
        FileConfiguration config = plugin.getConfigManager().getShops();
        config.set("shops", null); // Clear existing shops
        
        for (Shop shop : shops.values()) {
            saveShop(shop, config);
        }
        
        plugin.getConfigManager().saveShops();
    }

    private void saveShop(Shop shop, FileConfiguration config) {
        String path = "shops." + shop.getId();
        
        config.set(path + ".owner-id", shop.getOwnerId().toString());
        config.set(path + ".owner-name", shop.getOwnerName());
        config.set(path + ".chest-location", serializeLocation(shop.getChestLocation()));
        config.set(path + ".sign-location", serializeLocation(shop.getSignLocation()));
        config.set(path + ".item", shop.getItem().name());
        config.set(path + ".amount", shop.getAmount());
        config.set(path + ".buy-price", shop.getBuyPrice());
        config.set(path + ".sell-price", shop.getSellPrice());
        config.set(path + ".active", shop.isActive());
        config.set(path + ".created", shop.getCreated());
        config.set(path + ".last-used", shop.getLastUsed());
        
        // Save item trading data if this is an item trading shop
        if (shop.isItemTradingShop()) {
            plugin.getLogger().info("Saving item trading shop " + shop.getId() + " with items: " + shop.getBuyItemType() + " -> " + shop.getSellItemType());
            config.set(path + ".item-trading.enabled", true);
            
            if (shop.getBuyItemType() != null) {
                config.set(path + ".item-trading.buy-item", shop.getBuyItemType().name());
            }
            config.set(path + ".item-trading.buy-amount", shop.getBuyItemAmount());
            
            if (shop.getSellItemType() != null) {
                config.set(path + ".item-trading.sell-item", shop.getSellItemType().name());
            }
            config.set(path + ".item-trading.sell-amount", shop.getSellItemAmount());
            
            // Save item meta if available
            Map<String, Object> buyItemMeta = shop.getBuyItemMeta();
            if (buyItemMeta != null && !buyItemMeta.isEmpty()) {
                for (Map.Entry<String, Object> entry : buyItemMeta.entrySet()) {
                    config.set(path + ".item-trading.buy-item-meta." + entry.getKey(), entry.getValue());
                }
            }
            
            Map<String, Object> sellItemMeta = shop.getSellItemMeta();
            if (sellItemMeta != null && !sellItemMeta.isEmpty()) {
                for (Map.Entry<String, Object> entry : sellItemMeta.entrySet()) {
                    config.set(path + ".item-trading.sell-item-meta." + entry.getKey(), entry.getValue());
                }
            }
        } else {
            config.set(path + ".item-trading.enabled", false);
        }
    }

    public Shop createShop(String id, UUID ownerId, String ownerName, Location chestLocation,
                           Location signLocation, Material item, int amount, double buyPrice, double sellPrice) {
        Shop shop = new Shop(id, ownerId, ownerName, chestLocation, signLocation, item, amount, buyPrice, sellPrice);
        shops.put(id, shop);
        locationToShopId.put(locationToString(chestLocation), id);
        locationToShopId.put(locationToString(signLocation), id);
        
        // Create hologram
        HologramUtil.createShopHologram(shop, plugin);
        
        return shop;
    }

    public boolean removeShop(String shopId) {
        Shop shop = shops.remove(shopId);
        if (shop != null) {
            locationToShopId.remove(locationToString(shop.getChestLocation()));
            locationToShopId.remove(locationToString(shop.getSignLocation()));
            
            // Remove hologram
            HologramUtil.removeShopHologram(shopId);
            
            return true;
        }
        return false;
    }

    public Shop getShop(String shopId) {
        return shops.get(shopId);
    }

    public Shop getShopByLocation(Location location) {
        String locationString = locationToString(location);
        String shopId = locationToShopId.get(locationString);
        return shopId != null ? shops.get(shopId) : null;
    }

    public List<Shop> getShopsByOwner(UUID ownerId) {
        return shops.values().stream()
                .filter(shop -> shop.getOwnerId().equals(ownerId))
                .collect(Collectors.toList());
    }

    public List<Shop> getAllShops() {
        return new ArrayList<>(shops.values());
    }

    public int getShopCount() {
        return shops.size();
    }

    public int getShopCount(UUID ownerId) {
        return (int) shops.values().stream()
                .filter(shop -> shop.getOwnerId().equals(ownerId))
                .count();
    }

    public boolean isChestShop(Location location) {
        return getShopByLocation(location) != null;
    }

    public String generateShopId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String locationToString(Location location) {
        if (location == null || location.getWorld() == null) {
            return "";
        }
        return location.getWorld().getName() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
    }

    private Map<String, Object> serializeLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            plugin.getLogger().warning("Attempting to serialize null location or location with null world!");
            Map<String, Object> map = new HashMap<>();
            map.put("world", "world");
            map.put("x", 0);
            map.put("y", 0);
            map.put("z", 0);
            return map;
        }
        
        Map<String, Object> map = new HashMap<>();
        map.put("world", location.getWorld().getName());
        map.put("x", location.getBlockX());
        map.put("y", location.getBlockY());
        map.put("z", location.getBlockZ());
        return map;
    }

    private Location deserializeLocation(ConfigurationSection parentSection, String locationKey) {
        if (parentSection == null) {
            plugin.getLogger().warning("Parent section is null during location deserialization!");
            return null;
        }
        
        // Try to get as ConfigurationSection first
        ConfigurationSection section = parentSection.getConfigurationSection(locationKey);
        
        String worldName;
        int x, y, z;
        
        if (section != null) {
            // Standard ConfigurationSection approach
            worldName = section.getString("world");
            x = section.getInt("x");
            y = section.getInt("y");
            z = section.getInt("z");
        } else {
            // Try to get as raw object (for Map-based configurations)
            Object locationObj = parentSection.get(locationKey);
            if (locationObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> locationMap = (Map<String, Object>) locationObj;
                worldName = (String) locationMap.get("world");
                x = ((Number) locationMap.get("x")).intValue();
                y = ((Number) locationMap.get("y")).intValue();
                z = ((Number) locationMap.get("z")).intValue();
            } else {
                plugin.getLogger().warning("Location " + locationKey + " is neither ConfigurationSection nor Map!");
                return null;
            }
        }
        
        if (worldName == null || worldName.isEmpty()) {
            plugin.getLogger().warning("World name is null or empty during location deserialization for " + locationKey + "!");
            return null;
        }
        
        org.bukkit.World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("World '" + worldName + "' not found when loading shop location!");
            // Try to find any available world
            if (!plugin.getServer().getWorlds().isEmpty()) {
                world = plugin.getServer().getWorlds().get(0);
                plugin.getLogger().warning("Using fallback world: " + world.getName());
            } else {
                plugin.getLogger().severe("No worlds available! Cannot deserialize location.");
                return null;
            }
        }
        
        Location location = new Location(world, x, y, z);
        plugin.getLogger().info("Successfully deserialized " + locationKey + ": " + worldName + " (" + x + ", " + y + ", " + z + ")");
        return location;
    }
}
