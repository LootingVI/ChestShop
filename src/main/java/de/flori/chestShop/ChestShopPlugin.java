package de.flori.chestShop;

import de.flori.chestShop.commands.ChestShopCommand;
import de.flori.chestShop.config.ConfigManager;
import de.flori.chestShop.config.ConfigMigrator;
import de.flori.chestShop.listeners.ChestShopListener;
import de.flori.chestShop.managers.ShopManager;
import de.flori.chestShop.utils.EconomyManager;
import de.flori.chestShop.utils.HologramUtil;
import de.flori.chestShop.utils.NotificationUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestShopPlugin extends JavaPlugin {
    private static ChestShopPlugin instance;
    private ConfigManager configManager;
    private ShopManager shopManager;
    private EconomyManager economyManager;
    private Economy economy;

    @Override
    public void onEnable() {
        instance = this;

        // Check if Vault is available
        if (!setupEconomy()) {
            getLogger().severe("Vault or an Economy Plugin is not available! Plugin will be disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Load configuration
        configManager = new ConfigManager(this);
        configManager.loadConfigs();
        
        // Check and perform configuration migration if needed
        ConfigMigrator migrator = new ConfigMigrator(this, configManager);
        if (migrator.needsMigration()) {
            getLogger().info("Configuration migration required. Creating backup and migrating...");
            migrator.createBackup();
            migrator.migrateConfigs();
            // Reload configs after migration
            configManager.loadConfigs();
        }

        // Initialize economy manager first
        economyManager = new EconomyManager(economy);
        
        // Delay shop manager initialization until worlds are loaded
        getServer().getScheduler().runTaskLater(this, () -> {
            getLogger().info("Initializing shops after world loading...");
            shopManager = new ShopManager(this);
            
            // Initialize holograms after ShopManager is ready
            getServer().getScheduler().runTaskLater(this, () -> {
                getLogger().info("Initializing holograms...");
                HologramUtil.initializeAllHolograms(this);
            }, 20L); // Wait another 1 second for ShopManager to fully initialize
        }, 20L); // Wait 1 second (20 ticks)

        // Register commands (modern Paper method)
        ChestShopCommand commandExecutor = new ChestShopCommand(this);

        // Register main command and aliases
        registerCommand("chestshop", commandExecutor, commandExecutor);
        registerCommand("cs", commandExecutor, commandExecutor);
        registerCommand("shop", commandExecutor, commandExecutor);
        registerCommand("cshop", commandExecutor, commandExecutor);

        // Register events
        getServer().getPluginManager().registerEvents(new ChestShopListener(this), this);

        // Start notification system
        NotificationUtil.startPeriodicCheck(this);

        getLogger().info("ChestShop Plugin successfully loaded!");
    }

    @Override
    public void onDisable() {
        // Remove all holograms first
        HologramUtil.removeAllHolograms();
        getLogger().info("Holograms removed");
        
        if (shopManager != null) {
            shopManager.saveAllShops();
        }
        
        getLogger().info("ChestShop Plugin disabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    /**
     * Registers a command with executor and TabCompleter
     * Modern Paper Plugin method
     */
    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor,
                                 org.bukkit.command.TabCompleter tabCompleter) {
        Command command = new Command(name) {
            @Override
            public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                return executor.onCommand(sender, this, commandLabel, args);
            }

            @Override
            public java.util.List<String> tabComplete(CommandSender sender, String alias, String[] args) {
                if (tabCompleter != null) {
                    return tabCompleter.onTabComplete(sender, this, alias, args);
                }
                return super.tabComplete(sender, alias, args);
            }
        };

        command.setDescription("ChestShop Plugin Commands");
        command.setUsage("/chestshop <create|remove|info|list|toggle|reload|help>");
        command.setPermission("chestshop.use");

        // Register command
        getServer().getCommandMap().register(getName().toLowerCase(), command);
    }

    public static ChestShopPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public Economy getEconomy() {
        return economy;
    }
}
