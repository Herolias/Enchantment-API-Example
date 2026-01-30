# Simple Enchantments API

This project serves as an example implementation and documentation for the **Simple Enchantments API** of the mod `SimpleEnchantments` for Hytale.

## Requirements

To use this API, you must have the latest version of the mod (`SimpleEnchantments.jar`) in your project's `lib` folder.

You also need to add it to your `build.gradle` dependencies:

```gradle
dependencies {
    compileOnly files('lib/SimpleEnchantments-0.5.0.jar')
}
```

## Getting Started

To access the API, retrieve the instance from the provider:

```java
import org.herolias.plugin.api.EnchantmentApi;
import org.herolias.plugin.api.EnchantmentApiProvider;

EnchantmentApi api = EnchantmentApiProvider.get();
if (api == null) {
    // API is not available (dependency missing or not loaded)
}
```

## API Methods

### Enchantment Management

#### `addEnchantment`
Adds a specific enchantment to an item.

```java
/**
 * @param item The item to enchant
 * @param enchantmentId The ID of the enchantment (e.g. "sharpness")
 * @param level The level of the enchantment
 * @return A new ItemStack with the enchantment applied
 */
ItemStack newItem = api.addEnchantment(itemStack, "sharpness", 5);
```

#### `removeEnchantment`
Removes a specific enchantment from an item.

```java
/**
 * @param item The item to modify
 * @param enchantmentId The ID of the enchantment to remove
 * @return A new ItemStack with the enchantment removed
 */
ItemStack newItem = api.removeEnchantment(itemStack, "sharpness");
```

#### `getEnchantmentLevel`
Gets the level of a specific enchantment on an item. Returns 0 if not present.

```java
/**
 * @param item The item to check
 * @param enchantmentId The ID of the enchantment
 * @return The level of the enchantment, or 0 if not present
 */
int level = api.getEnchantmentLevel(itemStack, "sharpness");
```

#### `hasEnchantment`
Checks if an item has a specific enchantment.

```java
/**
 * @param item The item to check
 * @param enchantmentId The ID of the enchantment
 * @return True if the item has the enchantment, false otherwise
 */
boolean hasIt = api.hasEnchantment(itemStack, "sharpness");
```

#### `getEnchantments`
Retrieves all enchantments on an item as a Map.

```java
/**
 * @param item The item to check
 * @return Map of enchantment IDs to their levels
 */
Map<String, Integer> enchants = api.getEnchantments(itemStack);
```

### Configuration

#### `registerItemToCategory`
Registers a specific item ID to a category (e.g., "MELEE_WEAPON"). This is useful for defining which items can accept certain enchantments.

```java
/**
 * @param itemName The ID of the item (e.g. "Ingredient_Stick")
 * @param category The category to register it to
 */
api.registerItemToCategory("Ingredient_Stick", "MELEE_WEAPON");
```

## Example Usage

See `src/main/java/org/herolias/plugin/command/EnchantmentCommand.java` for a full command implementation using these methods, and `src/main/java/org/herolias/plugin/HeroliasMod.java` for registration examples.
