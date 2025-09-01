package de.flori.chestShop.models;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
