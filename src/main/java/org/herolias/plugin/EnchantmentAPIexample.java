package org.herolias.plugin;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import org.herolias.plugin.command.EnchantmentCommand;
import org.herolias.plugin.api.EnchantmentApi;
import org.herolias.plugin.api.EnchantmentApiProvider;
import org.herolias.plugin.enchantment.EnchantmentType;
import org.herolias.plugin.enchantment.ItemCategory;

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

            // Register Stick as enchantable (demo)
            api.registerItemToCategory("Ingredient_Stick", "MELEE_WEAPON");
            LOGGER.atInfo().log("Registered Ingredient_Stick as MELEE_WEAPON");

            // ─── Register custom Shovel crafting category ───
            api.registerCraftingCategory("Enchanting_Shovel", "Shovel", "Icons/CraftingCategories/ShovelTab.png");
            LOGGER.atInfo().log("Registered crafting category: Enchanting_Shovel");

            // ─── Register new HOES category with registerCategoryByItems ───
            ItemCategory hoesCategory = api.registerCategoryByItems("HOES", "Tool_Hoe_Crude", "Tool_Hoe_Iron", "Tool_Hoe_Copper", "Tool_Hoe_Thorium");
            LOGGER.atInfo().log("Registered custom item category: HOES");

            // ─── Register the Gold Digger enchantment ───
            EnchantmentType goldDigger = api.registerEnchantment("example:gold_digger", "Gold Digger")
                .description("Chance to find gold ore when digging dirt")
                .maxLevel(3)
                .multiplierPerLevel(0.10)
                .bonusDescription("Mined dirt has a {amount}% chance to drop gold ore instead")
                .modDisplayName("Enchantment API Example")
                .walkthrough("While digging dirt blocks with a shovel or hoe, there is a chance "
                    + "the block will drop gold ore instead of dirt. Each level increases "
                    + "the chance by {amount}%.")
                .appliesTo(ItemCategory.SHOVEL, hoesCategory)
                .craftingCategory("Enchanting_Shovel")
                .scroll(1)
                    .quality("Uncommon")
                    .craftingTier(1)
                    .ingredient("Ingredient_Crystal_Yellow", 5)
                    .ingredient("Soil_Dirt", 50)
                    .ingredient("Ore_Gold", 10)
                    .end()
                .scroll(2)
                    .quality("Rare")
                    .craftingTier(2)
                    .ingredient("Ingredient_Crystal_Yellow", 10)
                    .ingredient("Soil_Dirt", 100)
                    .ingredient("Ore_Gold", 20)
                    .end()
                .scroll(3)
                    .quality("Epic")
                    .craftingTier(3)
                    .ingredient("Ingredient_Crystal_Yellow", 15)
                    .ingredient("Soil_Dirt", 150)
                    .ingredient("Ore_Gold", 30)
                    .end()
                .build();

            LOGGER.atInfo().log("Registered enchantment: " + goldDigger.getDisplayName() 
                + " (id=" + goldDigger.getId() + ", maxLevel=" + goldDigger.getMaxLevel() + ")");

        } catch (NoClassDefFoundError | Exception e) {
             LOGGER.atSevere().log("Failed to setup EnchantmentAPIexample: " + e.getMessage());
        }

        this.getEntityStoreRegistry().registerSystem(new org.herolias.plugin.enchantment.GoldDiggerSystem());
        LOGGER.atInfo().log("Registered GoldDiggerSystem with ECS");

        this.getCommandRegistry().registerCommand(new EnchantmentCommand());
    }
}
