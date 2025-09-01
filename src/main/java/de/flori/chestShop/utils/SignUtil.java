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
        
        String line1 = plugin.getConfigManager().getConfig().getString("signs.format.line1", "&9[ChestShop]");
        String line2 = plugin.getConfigManager().getConfig().getString("signs.format.line2", "&b%owner%");
        String line3 = plugin.getConfigManager().getConfig().getString("signs.format.line3", "&a%amount% %item%");
        String line4 = plugin.getConfigManager().getConfig().getString("signs.format.line4", "&eB: %buy% S: %sell%");

        // Platzhalter ersetzen
        line1 = colorCode + line1.replace("&", "§");
        line2 = colorCode + line2.replace("&", "§").replace("%owner%", shop.getOwnerName());
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
        sign.update();
        
        // Update hologram
        HologramUtil.updateShopHologram(shop, plugin);
    }

    private static String getStatusColor(Shop shop, ChestShopPlugin plugin) {
        String colorKey = switch (shop.getStatus()) {
            case ACTIVE -> "signs.colors.active";
            case INACTIVE -> "signs.colors.inactive";
            case OUT_OF_STOCK -> "signs.colors.out-of-stock";
            case OUT_OF_SPACE -> "signs.colors.out-of-space";
        };
        
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
}
