package de.flori.chestShop.utils;

import de.flori.chestShop.ChestShopPlugin;
import de.flori.chestShop.models.Shop;
import org.bukkit.block.Sign;

public class SignUtil {

    public static void updateShopSign(Shop shop, ChestShopPlugin plugin) {
        Sign sign = shop.getSign();
        if (sign == null) {
            return;
        }

        String colorCode = getStatusColor(shop, plugin);
        
        // Check if this is a trading shop
        boolean itemTradingEnabled = plugin.getConfigManager().getConfig().getBoolean("item-trading.enabled", false);
        if (itemTradingEnabled && shop.isItemTradingShop()) {
            updateTradingShopSign(shop, plugin, sign, colorCode);
        } else {
            updateNormalShopSign(shop, plugin, sign, colorCode);
        }
        
        sign.update();
        
        // Update hologram
        HologramUtil.updateShopHologram(shop, plugin);
    }
    
    private static void updateNormalShopSign(Shop shop, ChestShopPlugin plugin, Sign sign, String colorCode) {
        String line1 = plugin.getConfigManager().getConfig().getString("signs.format.line1", "&9[ChestShop]");
        String line2 = plugin.getConfigManager().getConfig().getString("signs.format.line2", "&b%owner%");
        String line3 = plugin.getConfigManager().getConfig().getString("signs.format.line3", "&a%amount% %item%");
        String line4 = plugin.getConfigManager().getConfig().getString("signs.format.line4", "&eB: %buy% S: %sell%");

        // Platzhalter ersetzen
        line1 = colorCode + line1.replace("&", "§");
        line2 = colorCode + line2.replace("&", "§")
            .replace("%owner%", shop.getOwnerName());
        line3 = colorCode + line3.replace("&", "§")
            .replace("%amount%", String.valueOf(shop.getAmount()))
            .replace("%item%", getItemDisplayName(shop.getItem()));
        line4 = colorCode + line4.replace("&", "§")
            .replace("%buy%", shop.hasBuyPrice() ? plugin.getEconomyManager().formatSimple(shop.getBuyPrice()) : "-")
            .replace("%sell%", shop.hasSellPrice() ? plugin.getEconomyManager().formatSimple(shop.getSellPrice()) : "-");

        sign.setLine(0, line1);
        sign.setLine(1, line2);
        sign.setLine(2, line3);
        sign.setLine(3, line4);
    }
    
    private static void updateTradingShopSign(Shop shop, ChestShopPlugin plugin, Sign sign, String colorCode) {
        // Get trading shop sign format
        String header = plugin.getConfigManager().getMessage("item-trading.sign.format-header");
        String owner = plugin.getConfigManager().getMessage("item-trading.sign.format-owner");
        String trade = plugin.getConfigManager().getMessage("item-trading.sign.format-trade");
        String amounts = plugin.getConfigManager().getMessage("item-trading.sign.format-amounts");
        
        // Replace placeholders for trading shop
        String line1 = colorCode + header.replace("&", "§");
        String line2 = colorCode + owner.replace("&", "§")
            .replace("%owner%", shop.getOwnerName());
        String line3 = colorCode + trade.replace("&", "§")
            .replace("%buy_item%", getShortItemName(shop.getBuyItemType()))
            .replace("%sell_item%", getShortItemName(shop.getSellItemType()));
        String line4 = colorCode + amounts.replace("&", "§")
            .replace("%buy_amount%", String.valueOf(shop.getBuyItemAmount()))
            .replace("%sell_amount%", String.valueOf(shop.getSellItemAmount()));

        sign.setLine(0, line1);
        sign.setLine(1, line2);
        sign.setLine(2, line3);
        sign.setLine(3, line4);
    }

    private static String getStatusColor(Shop shop, ChestShopPlugin plugin) {
        String colorKey;
        switch (shop.getStatus()) {
            case ACTIVE:
                colorKey = "signs.colors.active";
                break;
            case INACTIVE:
                colorKey = "signs.colors.inactive";
                break;
            case OUT_OF_STOCK:
                colorKey = "signs.colors.out-of-stock";
                break;
            case OUT_OF_SPACE:
                colorKey = "signs.colors.out-of-space";
                break;
            default:
                colorKey = "signs.colors.active";
                break;
        }
        
        return plugin.getConfigManager().getConfig().getString(colorKey, "&a").replace("&", "§");
    }

    private static String getItemDisplayName(org.bukkit.Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        
        return result.toString();
    }
    
    /**
     * Get a shortened version of an item name for signs (limited space)
     */
    private static String getShortItemName(org.bukkit.Material material) {
        String fullName = getItemDisplayName(material);
        
        // If the name is already short enough, return it
        if (fullName.length() <= 8) {
            return fullName;
        }
        
        // Try to abbreviate common words
        String abbreviated = fullName
            .replace("Diamond", "Dia")
            .replace("Iron", "Fe")
            .replace("Golden", "Au")
            .replace("Stone", "St")
            .replace("Wooden", "Wd")
            .replace("Leather", "Lea")
            .replace("Enchanted", "Ench")
            .replace(" Of ", " ")
            .replace(" The ", " ")
            .replace("Block", "Bl");
        
        // If still too long, take first 8 characters
        if (abbreviated.length() > 8) {
            abbreviated = abbreviated.substring(0, 8);
        }
        
        return abbreviated;
    }
}
