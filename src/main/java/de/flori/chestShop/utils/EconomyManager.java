package de.flori.chestShop.utils;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class EconomyManager {

    private final Economy economy;
    private final DecimalFormat formatter;

    public EconomyManager(Economy economy) {
        this.economy = economy;
        this.formatter = new DecimalFormat("#,##0.00");
    }

    public boolean hasEnough(Player player, double amount) {
        return economy.has(player, amount);
    }

    public boolean withdraw(Player player, double amount) {
        if (!hasEnough(player, amount)) {
            return false;
        }
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public boolean deposit(Player player, double amount) {
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    public double getBalance(Player player) {
        return economy.getBalance(player);
    }

    public String format(double amount) {
        return economy.format(amount);
    }

    public String formatSimple(double amount) {
        return formatter.format(amount);
    }

    public String getCurrencyNameSingular() {
        return economy.currencyNameSingular();
    }

    public String getCurrencyNamePlural() {
        return economy.currencyNamePlural();
    }

    public boolean isEnabled() {
        return economy != null;
    }
}
