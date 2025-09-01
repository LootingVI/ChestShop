package de.flori.chestShop;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class ChestShopBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        // Hier können frühe Initialisierungen durchgeführt werden
        // wie z.B. das Registrieren von DataComponents oder andere Paper-spezifische Features
        context.getLogger().info("ChestShop Plugin is initializing...");
    }

    @Override
    public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) {
        return new ChestShopPlugin();
    }
}
