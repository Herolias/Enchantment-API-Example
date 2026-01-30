package org.herolias.plugin.command;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import org.herolias.plugin.api.EnchantmentApi;
import org.herolias.plugin.api.EnchantmentApiProvider;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Map;

public class EnchantmentCommand extends CommandBase {

    public EnchantmentCommand() {
        super("enchantment", "Manages enchantments on held items.");
        this.setPermissionGroup(GameMode.Adventure);
        this.setAllowsExtraArguments(true);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        if (!ctx.isPlayer()) {
            ctx.sendMessage(Message.raw("This command is for players only."));
            return;
        }

        Player player = (Player) ctx.sender();
        String input = ctx.getInputString();
        String[] parts = input.trim().split("\\s+");
        String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

        if (args.length < 1) {
            ctx.sendMessage(Message.raw("Usage: /enchantment <add|remove|check|get_level|has_enchant> [args]"));
            return;
        }

        EnchantmentApi api = EnchantmentApiProvider.get();
        if (api == null) {
            ctx.sendMessage(Message.raw("Enchantment API is not available."));
            return;
        }

        Inventory inventory = player.getInventory();
        ItemStack heldItem = inventory.getItemInHand();
        
        if (heldItem == null || heldItem.isEmpty()) {
            ctx.sendMessage(Message.raw("You must be holding an item."));
            return;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "add": {
                if (args.length < 3) {
                    ctx.sendMessage(Message.raw("Usage: /enchantment add <enchantment> <level>"));
                    return;
                }
                String enchantName = args[1];
                int level;
                try {
                    level = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    ctx.sendMessage(Message.raw("Level must be a valid number."));
                    return;
                }

                try {
                    ItemStack newItem = api.addEnchantment(heldItem, enchantName, level);
                    updateHeldItem(inventory, newItem);
                    ctx.sendMessage(Message.raw("Added " + enchantName + " level " + level + " to item."));
                } catch (Exception e) {
                    ctx.sendMessage(Message.raw("Failed to add enchantment: " + e.getMessage()));
                    e.printStackTrace();
                }
                break;
            }
            case "remove": {
                if (args.length < 2) {
                    ctx.sendMessage(Message.raw("Usage: /enchantment remove <enchantment>"));
                    return;
                }
                String enchantName = args[1];
                try {
                    ItemStack newItem = api.removeEnchantment(heldItem, enchantName);
                    updateHeldItem(inventory, newItem);
                    ctx.sendMessage(Message.raw("Removed " + enchantName + " from item."));
                } catch (Exception e) {
                    ctx.sendMessage(Message.raw("Failed to remove enchantment: " + e.getMessage()));
                }
                break;
            }
            case "get_level": {
                if (args.length < 2) {
                    ctx.sendMessage(Message.raw("Usage: /enchantment get_level <enchantment>"));
                    return;
                }
                String enchantName = args[1];
                try {
                    int level = api.getEnchantmentLevel(heldItem, enchantName);
                    ctx.sendMessage(Message.raw("Enchantment " + enchantName + " level: " + level));
                } catch (Exception e) {
                    ctx.sendMessage(Message.raw("Failed to get enchantment level: " + e.getMessage()));
                }
                break;
            }
            case "has_enchant": {
                if (args.length < 2) {
                    ctx.sendMessage(Message.raw("Usage: /enchantment has_enchant <enchantment>"));
                    return;
                }
                String enchantName = args[1];
                try {
                    boolean has = api.hasEnchantment(heldItem, enchantName);
                    ctx.sendMessage(Message.raw("Item has " + enchantName + ": " + has));
                } catch (Exception e) {
                    ctx.sendMessage(Message.raw("Failed to check enchantment: " + e.getMessage()));
                }
                break;
            }
            case "check":
            case "list": {
                Map<String, Integer> enchants = api.getEnchantments(heldItem);
                if (enchants == null || enchants.isEmpty()) {
                    ctx.sendMessage(Message.raw("No enchantments on this item."));
                } else {
                    ctx.sendMessage(Message.raw("Enchantments:"));
                    for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
                        ctx.sendMessage(Message.raw("- " + entry.getKey() + ": " + entry.getValue()));
                    }
                }
                break;
            }
            default:
                ctx.sendMessage(Message.raw("Unknown subcommand: " + subCommand));
                break;
        }
    }

    private void updateHeldItem(Inventory inventory, ItemStack newItem) {
     
        int activeHotbarSlot = inventory.getActiveHotbarSlot();
        if (activeHotbarSlot != -1) {
             ItemContainer hotbar = inventory.getHotbar();
             hotbar.setItemStackForSlot((short)activeHotbarSlot, newItem);
        }
    }
}
