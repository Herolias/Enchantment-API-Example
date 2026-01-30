package org.herolias.plugin;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import org.herolias.plugin.command.EnchantmentCommand;
import org.herolias.plugin.api.EnchantmentApi;
import org.herolias.plugin.api.EnchantmentApiProvider;

import javax.annotation.Nonnull;

public class EnchantmentAPIexample extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public EnchantmentAPIexample(@Nonnull JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("EnchantmentAPIexample initialized!");
    }

    protected void setup() {
        LOGGER.atInfo().log("Setting up EnchantmentAPIexample...");
        
        try {
            EnchantmentApi api = EnchantmentApiProvider.get();
            api.registerItemToCategory("Ingredient_Stick", "MELEE_WEAPON");
            LOGGER.atInfo().log("Registered Ingredient_Stick as MELEE_WEAPON");
        } catch (NoClassDefFoundError | Exception e) {
             LOGGER.atSevere().log("Failed to register Stick enchantment category: " + e.getMessage());
        }

        this.getCommandRegistry().registerCommand(new EnchantmentCommand());
    }
}
