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
                Location chestLocation = deserializeLocation(shopSection.getConfigurationSection("chest-location"));
                Location signLocation = deserializeLocation(shopSection.getConfigurationSection("sign-location"));
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

                shops.put(shopId, shop);
                locationToShopId.put(locationToString(chestLocation), shopId);
                locationToShopId.put(locationToString(signLocation), shopId);

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
        Map<String, Object> map = new HashMap<>();
        map.put("world", location.getWorld().getName());
        map.put("x", location.getBlockX());
        map.put("y", location.getBlockY());
        map.put("z", location.getBlockZ());
        return map;
    }

    private Location deserializeLocation(ConfigurationSection section) {
        if (section == null) return null;
        
        String worldName = section.getString("world");
        int x = section.getInt("x");
        int y = section.getInt("y");
        int z = section.getInt("z");
        
        return new Location(plugin.getServer().getWorld(worldName), x, y, z);
    }
}
