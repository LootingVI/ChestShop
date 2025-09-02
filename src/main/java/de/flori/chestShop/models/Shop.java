package de.flori.chestShop.models;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Shop {
    
    private String id;
    private UUID ownerId;
    private String ownerName;
    private Location chestLocation;
    private Location signLocation;
    private Material item;
    private int amount;
    private double buyPrice;
    private double sellPrice;
    private boolean active;
    private long created;
    private long lastUsed;
    
    // Item Trading fields
    private boolean isItemTradingShop;
    private Material buyItemType;
    private int buyItemAmount;
    private Material sellItemType;
    private int sellItemAmount;
    private Map<String, Object> buyItemMeta;
    private Map<String, Object> sellItemMeta;

    public Shop(String id, UUID ownerId, String ownerName, Location chestLocation, 
                Location signLocation, Material item, int amount, double buyPrice, double sellPrice) {
        this.id = id;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.chestLocation = chestLocation;
        this.signLocation = signLocation;
        this.item = item;
        this.amount = amount;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.active = true;
        this.created = System.currentTimeMillis();
        this.lastUsed = System.currentTimeMillis();
    }

    // Getters
    public String getId() { return id; }
    public UUID getOwnerId() { return ownerId; }
    public String getOwnerName() { return ownerName; }
    public Location getChestLocation() { return chestLocation; }
    public Location getSignLocation() { return signLocation; }
    public Material getItem() { return item; }
    public int getAmount() { return amount; }
    public double getBuyPrice() { return buyPrice; }
    public double getSellPrice() { return sellPrice; }
    public boolean isActive() { return active; }
    public long getCreated() { return created; }
    public long getLastUsed() { return lastUsed; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public void setChestLocation(Location chestLocation) { this.chestLocation = chestLocation; }
    public void setSignLocation(Location signLocation) { this.signLocation = signLocation; }
    public void setItem(Material item) { this.item = item; }
    public void setAmount(int amount) { this.amount = amount; }
    public void setBuyPrice(double buyPrice) { this.buyPrice = buyPrice; }
    public void setSellPrice(double sellPrice) { this.sellPrice = sellPrice; }
    public void setActive(boolean active) { this.active = active; }
    public void setCreated(long created) { this.created = created; }
    public void setLastUsed(long lastUsed) { this.lastUsed = lastUsed; }
    
    // Item Trading getters and setters
    public boolean isItemTradingShop() { return isItemTradingShop; }
    public void setItemTradingShop(boolean itemTradingShop) { this.isItemTradingShop = itemTradingShop; }
    
    public Material getBuyItemType() { return buyItemType; }
    public void setBuyItemType(Material buyItemType) { this.buyItemType = buyItemType; }
    
    public int getBuyItemAmount() { return buyItemAmount; }
    public void setBuyItemAmount(int buyItemAmount) { this.buyItemAmount = buyItemAmount; }
    
    public Material getSellItemType() { return sellItemType; }
    public void setSellItemType(Material sellItemType) { this.sellItemType = sellItemType; }
    
    public int getSellItemAmount() { return sellItemAmount; }
    public void setSellItemAmount(int sellItemAmount) { this.sellItemAmount = sellItemAmount; }
    
    public Map<String, Object> getBuyItemMeta() { 
        if (buyItemMeta == null) buyItemMeta = new HashMap<>();
        return buyItemMeta; 
    }
    public void setBuyItemMeta(Map<String, Object> buyItemMeta) { this.buyItemMeta = buyItemMeta; }
    
    public Map<String, Object> getSellItemMeta() { 
        if (sellItemMeta == null) sellItemMeta = new HashMap<>();
        return sellItemMeta; 
    }
    public void setSellItemMeta(Map<String, Object> sellItemMeta) { this.sellItemMeta = sellItemMeta; }

    // Utility Methods
    public boolean hasBuyPrice() {
        return buyPrice > 0;
    }

    public boolean hasSellPrice() {
        return sellPrice > 0;
    }

    public Chest getChest() {
        if (chestLocation == null || chestLocation.getWorld() == null) {
            return null;
        }
        Block block = chestLocation.getBlock();
        if (block.getState() instanceof Chest) {
            return (Chest) block.getState();
        }
        return null;
    }

    public Sign getSign() {
        if (signLocation == null || signLocation.getWorld() == null) {
            return null;
        }
        Block block = signLocation.getBlock();
        if (block.getState() instanceof Sign) {
            return (Sign) block.getState();
        }
        return null;
    }

    public int getStock() {
        Chest chest = getChest();
        if (chest == null) {
            return 0;
        }
        
        Inventory inventory = chest.getInventory();
        int stock = 0;
        
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null && itemStack.getType() == item) {
                stock += itemStack.getAmount();
            }
        }
        
        return stock;
    }

    public int getAvailableSpace() {
        Chest chest = getChest();
        if (chest == null) {
            return 0;
        }
        
        Inventory inventory = chest.getInventory();
        int space = 0;
        int maxStackSize = item.getMaxStackSize();
        
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null) {
                space += maxStackSize;
            } else if (itemStack.getType() == item) {
                space += maxStackSize - itemStack.getAmount();
            }
        }
        
        return space;
    }

    public boolean canBuy(int requestedAmount) {
        return hasBuyPrice() && isActive() && getStock() >= requestedAmount;
    }

    public boolean canSell(int requestedAmount) {
        return hasSellPrice() && isActive() && getAvailableSpace() >= requestedAmount;
    }

    public ShopStatus getStatus() {
        if (!active) {
            return ShopStatus.INACTIVE;
        }
        
        if (hasBuyPrice() && getStock() == 0) {
            return ShopStatus.OUT_OF_STOCK;
        }
        
        if (hasSellPrice() && getAvailableSpace() == 0) {
            return ShopStatus.OUT_OF_SPACE;
        }
        
        return ShopStatus.ACTIVE;
    }

    public void updateLastUsed() {
        this.lastUsed = System.currentTimeMillis();
    }
    
    // Item Trading Utility Methods
    public boolean hasItemTrading() {
        return isItemTradingShop && buyItemType != null && sellItemType != null;
    }
    
    public boolean canItemTrade(int requestedAmount) {
        if (!hasItemTrading() || !isActive()) {
            return false;
        }
        
        // Check if shop has enough sellItems to give
        return getItemStock(sellItemType) >= (sellItemAmount * requestedAmount);
    }
    
    public int getItemStock(Material itemType) {
        Chest chest = getChest();
        if (chest == null) {
            return 0;
        }
        
        Inventory inventory = chest.getInventory();
        int stock = 0;
        
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null && itemStack.getType() == itemType) {
                stock += itemStack.getAmount();
            }
        }
        
        return stock;
    }
    
    public int getItemSpace(Material itemType) {
        Chest chest = getChest();
        if (chest == null) {
            return 0;
        }
        
        Inventory inventory = chest.getInventory();
        int space = 0;
        int maxStackSize = itemType.getMaxStackSize();
        
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null) {
                space += maxStackSize;
            } else if (itemStack.getType() == itemType) {
                space += maxStackSize - itemStack.getAmount();
            }
        }
        
        return space;
    }
    
    // Additional methods for Item Trading compatibility
    public Material getBuyItem() {
        return buyItemType;
    }
    
    public Material getSellItem() {
        return sellItemType;
    }
    
    public int getTradingStockForGiving() {
        if (!hasItemTrading()) return 0;
        return getItemStock(sellItemType);
    }
    
    public int getTradingStockForReceiving() {
        if (!hasItemTrading()) return 0;
        return getItemSpace(buyItemType);
    }

    public enum ShopStatus {
        ACTIVE, INACTIVE, OUT_OF_STOCK, OUT_OF_SPACE
    }

    @Override
    public String toString() {
        return "Shop{" +
                "id='" + id + '\'' +
                ", owner='" + ownerName + '\'' +
                ", item=" + item +
                ", amount=" + amount +
                ", buyPrice=" + buyPrice +
                ", sellPrice=" + sellPrice +
                ", active=" + active +
                '}';
    }
}
