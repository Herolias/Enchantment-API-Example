# Simple Enchantments API

This project serves as an example implementation and documentation for the **Simple Enchantments API** — a public API that allows mods to interact with the enchantment system of the `SimpleEnchantments` mod for Hytale as well as create their own enchantments and scrolls.
Most of this API is added in the `SimpleEnchantments` mod version 0.9.0, so you cannot publish enchantment addons before this version is officially released.

---

## Table of Contents

- [Requirements](#requirements)
- [Getting Started](#getting-started)
- [API Reference](#api-reference)
  - [EnchantmentApi](#enchantmentapi)
  - [EnchantmentBuilder](#enchantmentbuilder)
  - [ScrollBuilder](#scrollbuilder)
  - [ScaleType](#scaletype)
  - [ItemCategory](#itemcategory)
  - [Events](#events)
  - [EnchantmentType](#enchantmenttype)
- [Full Example](#full-example)

---

## Requirements

To use this API, you must have the latest version of the mod (`SimpleEnchantments.jar`) in your project's `lib` folder.

### Project Types

There are two ways to integrate with Simple Enchantments, depending on your mod's purpose:

#### Enchantment Add-on (Full Dependency)

Your mod **exists specifically to add new enchantments** and has no purpose without Simple Enchantments. The mod will not load if Simple Enchantments is missing.

**`build.gradle`** — use `compileOnly` (SE is loaded at runtime by Hytale, not bundled):
```gradle
dependencies {
    compileOnly files('lib/SimpleEnchantments-0.9.0.jar')
}
```

**`manifest.json`** — declare as a full dependency to ensure SE loads first:
```json
{
    "Main": "com.example.plugin.MyEnchantmentAddon",
    "Dependencies": {
        "org.herolias:SimpleEnchantments": "0.9.0"
    }
}
```

> With a full dependency, Hytale guarantees Simple Enchantments is loaded before your mod. You can safely call the API directly in `setup()` without null-checks on `EnchantmentApiProvider.get()`.

#### Standalone Mod (Optional Dependency)

Your mod **has its own core functionality** but can optionally integrate with Simple Enchantments when it's installed. The mod works fine without it.

**`build.gradle`** — use `compileOnly` so your mod still compiles without SE at runtime:
```gradle
dependencies {
    compileOnly files('lib/SimpleEnchantments-0.9.0.jar')
}
```

**`manifest.json`** — declare as an optional dependency so SE loads first *if present*:
```json
{
    "Main": "com.example.plugin.MyStandaloneMod",
    "OptionalDependencies": {
        "org.herolias:SimpleEnchantments": "0.9.0"
    }
}
```

> With an optional dependency, Simple Enchantments may not be installed. You **must** guard all API calls with a null-check and wrap them in a `try/catch` for `NoClassDefFoundError`:

```java
protected void setup() {
    // Your mod's core setup always runs
    registerMyCoreSystems();

    // Optional SE integration — only runs if SE is installed
    try {
        EnchantmentApi api = EnchantmentApiProvider.get();
        if (api != null) {
            api.registerItemToCategory("My_Custom_Sword", "MELEE_WEAPON");
        }
    } catch (NoClassDefFoundError e) {
        // Simple Enchantments is not installed — skip enchantment features
    }
}
```

### Summary

| | Enchantment Add-on | Standalone Mod |
|---|---|---|
| **Purpose** | Only adds new enchantments to SE | Independent mod with optional enchantment features |
| **Works without SE?** | ❌ No | ✅ Yes |
| **`build.gradle`** | `compileOnly` | `compileOnly` |
| **`manifest.json`** | `"Dependencies"` | `"OptionalDependencies"` |
| **Null-check needed?** | No | Yes |
| **NoClassDefFoundError guard?** | No | Yes |

> This example project is an **Enchantment Add-on** — it registers the "Gold Digger" enchantment and has no purpose without Simple Enchantments.

---

## Getting Started

To access the API, retrieve the singleton instance from the provider inside your plugin's `setup()` method:

```java
import org.herolias.plugin.api.EnchantmentApi;
import org.herolias.plugin.api.EnchantmentApiProvider;

protected void setup() {
    EnchantmentApi api = EnchantmentApiProvider.get(); //guard if optional dependency
    if (api == null) {
        // Simple Enchantments is not loaded — handle gracefully
        return;
    }
}
```

> **Note:** `EnchantmentApiProvider.get()` returns `null` if the Simple Enchantments plugin has not been initialized yet. Always null-check before using the API.

---

## API Reference

### EnchantmentApi

The main interface for interacting with the enchantment system. Retrieved via `EnchantmentApiProvider.get()`.

---

#### `addEnchantment`

Adds an enchantment to an item.

```java
@Nonnull
ItemStack addEnchantment(@Nonnull ItemStack item, @Nonnull String enchantmentId, int level)
```

| Parameter | Type | Description |
|---|---|---|
| `item` | `ItemStack` | The item to enchant |
| `enchantmentId` | `String` | The ID of the enchantment (e.g. `"sharpness"`, `"example:gold_digger"`) |
| `level` | `int` | The enchantment level to apply |

**Returns:** A new `ItemStack` with the enchantment applied, or the original item if application failed (e.g. conflicts, max enchantment limit reached).

**Throws:** `IllegalArgumentException` if `enchantmentId` is not a registered enchantment.

```java
ItemStack enchantedSword = api.addEnchantment(itemStack, "sharpness", 3);
```

---

#### `removeEnchantment`

Removes a specific enchantment from an item.

```java
@Nonnull
ItemStack removeEnchantment(@Nonnull ItemStack item, @Nonnull String enchantmentId)
```

| Parameter | Type | Description |
|---|---|---|
| `item` | `ItemStack` | The item to modify |
| `enchantmentId` | `String` | The ID of the enchantment to remove |

**Returns:** A new `ItemStack` with the enchantment removed, or the original item if the enchantment was not present.

```java
ItemStack cleaned = api.removeEnchantment(itemStack, "sharpness");
```

---

#### `getEnchantmentLevel`

Gets the level of a specific enchantment on an item.

```java
int getEnchantmentLevel(@Nullable ItemStack item, @Nonnull String enchantmentId)
```

| Parameter | Type | Description |
|---|---|---|
| `item` | `ItemStack` | The item to check (nullable — returns `0` if null) |
| `enchantmentId` | `String` | The ID of the enchantment |

**Returns:** The level of the enchantment, or `0` if not present.

```java
int level = api.getEnchantmentLevel(tool, "example:gold_digger");
```

---

#### `hasEnchantment`

Checks if an item has a specific enchantment.

```java
boolean hasEnchantment(@Nullable ItemStack item, @Nonnull String enchantmentId)
```

| Parameter | Type | Description |
|---|---|---|
| `item` | `ItemStack` | The item to check (nullable — returns `false` if null) |
| `enchantmentId` | `String` | The ID of the enchantment |

**Returns:** `true` if the item has the enchantment, `false` otherwise.

```java
if (api.hasEnchantment(heldItem, "sharpness")) {
    // item is sharp
}
```

---

#### `getEnchantments`

Retrieves all enchantments on an item.

```java
@Nonnull
Map<String, Integer> getEnchantments(@Nullable ItemStack item)
```

| Parameter | Type | Description |
|---|---|---|
| `item` | `ItemStack` | The item to check (nullable — returns empty map if null) |

**Returns:** A `Map<String, Integer>` of enchantment IDs to their levels. Empty map if no enchantments.

```java
Map<String, Integer> enchants = api.getEnchantments(heldItem);
for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
    System.out.println(entry.getKey() + ": " + entry.getValue());
}
```

---

#### `equippedItemEnchantments`

Gets all enchantments across a player's equipped items (armor slots, utility/off-hand, and main-hand). If the same enchantment appears on multiple items, the highest level is kept.

```java
@Nonnull
Map<String, Integer> equippedItemEnchantments(@Nonnull Player player)
```

| Parameter | Type | Description |
|---|---|---|
| `player` | `Player` | The player to inspect |

**Returns:** A `Map<String, Integer>` of enchantment IDs to their highest levels across all equipped slots.

```java
Map<String, Integer> equipped = api.equippedItemEnchantments(player);
if (equipped.containsKey("feather_falling")) {
    int level = equipped.get("feather_falling");
}
```

---

#### `registerItemToCategory`

Registers a specific item ID to a category, making it enchantable. This has higher priority than configuration files. Use this to make modded items or vanilla items enchantable that aren't automatically detected.

```java
void registerItemToCategory(@Nonnull String itemId, @Nonnull String categoryId)
```

| Parameter | Type | Description |
|---|---|---|
| `itemId` | `String` | The item ID (e.g. `"Ingredient_Stick"`) |
| `categoryId` | `String` | The category ID (see [ItemCategory](#itemcategory)) |

**Throws:** `IllegalArgumentException` if the category does not exist.

```java
api.registerItemToCategory("Ingredient_Stick", "MELEE_WEAPON");
```

---

#### `registerCategoryByFamily`

Registers a custom item category based on an item family. Returns the registered `ItemCategory`, which can be used to specify which items an enchantment applies to.

```java
@Nonnull
ItemCategory registerCategoryByFamily(@Nonnull String categoryId, @Nonnull String family)
```

| Parameter | Type | Description |
|---|---|---|
| `categoryId` | `String` | Unique ID for the new or existing category (e.g. `"MY_MOD_WEAPON"`) |
| `family` | `String` | The item tag family (e.g. `"katana"`) |

**Returns:** The registered `ItemCategory`.

```java
ItemCategory myCategory = api.registerCategoryByFamily("MY_MOD_WEAPON", "katana");
```

---

#### `registerCategoryByItems`

Registers a custom item category and explicitly assigns an array of item IDs to it. Returns the registered `ItemCategory`, which can be used to specify which items an enchantment applies to.

```java
@Nonnull
ItemCategory registerCategoryByItems(@Nonnull String categoryId, @Nonnull String... itemIds)
```

| Parameter | Type | Description |
|---|---|---|
| `categoryId` | `String` | Unique ID for the new or existing category (e.g. `"MY_MOD_TOOLS"`) |
| `itemIds` | `String...` | The item IDs to associate with this category |

**Returns:** The registered `ItemCategory`.

```java
ItemCategory magicWands = api.registerCategoryByItems("MAGIC_WANDS", "Wand_Fire", "Wand_Ice");
```

---

#### `getCategory`

Retrieves a registered item category by its ID.

```java
@Nullable
ItemCategory getCategory(@Nonnull String categoryId)
```

| Parameter | Type | Description |
|---|---|---|
| `categoryId` | `String` | The category ID |

**Returns:** The `ItemCategory`, or `null` if not found.

```java
ItemCategory melee = api.getCategory("MELEE_WEAPON");
```

---

#### `registerEnchantment`

Starts building a new addon enchantment. Returns an `EnchantmentBuilder` for fluent configuration.

```java
@Nonnull
EnchantmentBuilder registerEnchantment(@Nonnull String id, @Nonnull String displayName)
```

| Parameter | Type | Description |
|---|---|---|
| `id` | `String` | Namespaced enchantment ID — **must** contain `:` (e.g. `"my_mod:lightning"`) |
| `displayName` | `String` | Human-readable name (e.g. `"Lightning Strike"`) |

**Returns:** An [`EnchantmentBuilder`](#enchantmentbuilder) to configure and register the enchantment.

**Throws:** `IllegalArgumentException` if the ID does not contain `:`.

```java
EnchantmentType lightning = api.registerEnchantment("my_mod:lightning", "Lightning Strike")
    .description("Chance to strike enemies with lightning")
    .maxLevel(3)
    .multiplierPerLevel(0.15)
    .modDisplayName("My Awesome Mod")
    .appliesTo(ItemCategory.MELEE_WEAPON)
    .build();
```

---

#### `getRegisteredEnchantment`

Gets a registered enchantment by its ID.

```java
@Nullable
EnchantmentType getRegisteredEnchantment(@Nonnull String id)
```

| Parameter | Type | Description |
|---|---|---|
| `id` | `String` | The enchantment ID (e.g. `"sharpness"` or `"my_mod:lightning"`) |

**Returns:** The `EnchantmentType` instance, or `null` if not registered.

```java
EnchantmentType type = api.getRegisteredEnchantment("example:gold_digger");
if (type != null) {
    double multiplier = type.getScaledMultiplier(level);
}
```

---

#### `isEnchantmentRegistered`

Checks if an enchantment ID is registered.

```java
boolean isEnchantmentRegistered(@Nonnull String id)
```

| Parameter | Type | Description |
|---|---|---|
| `id` | `String` | The enchantment ID |

**Returns:** `true` if registered, `false` otherwise.

```java
if (api.isEnchantmentRegistered("my_mod:lightning")) {
    // enchantment exists
}
```

---

#### `addConflict`

Declares two enchantments as conflicting — they cannot coexist on the same item.

```java
void addConflict(@Nonnull String enchantmentId1, @Nonnull String enchantmentId2)
```

| Parameter | Type | Description |
|---|---|---|
| `enchantmentId1` | `String` | First enchantment ID |
| `enchantmentId2` | `String` | Second enchantment ID |

```java
api.addConflict("sharpness", "my_mod:lightning");
```

---

#### `registerCraftingCategory`

Registers a new crafting category (tab) in the Enchanting Table UI. Use this when your mod's scrolls don't fit into the existing built-in categories.

```java
void registerCraftingCategory(@Nonnull String categoryId, @Nonnull String displayName,
                              @Nullable String iconPath)
```

| Parameter | Type | Description |
|---|---|---|
| `categoryId` | `String` | Unique ID for the category (e.g. `"Enchanting_Magic"`) |
| `displayName` | `String` | Displayed name in the Enchanting Table UI |
| `iconPath` | `String` | Path to the tab icon relative to mod assets, or `null` for the default icon |

**Throws:** `IllegalArgumentException` if the category ID is already registered.

**Built-in categories:**
| Category ID | Display Name |
|---|---|
| `Enchanting_Melee` | Melee |
| `Enchanting_Ranged` | Ranged |
| `Enchanting_Armor` | Armor |
| `Enchanting_Shield` | Shield |
| `Enchanting_Staff` | Staff |
| `Enchanting_Tools` | Tools |

```java
api.registerCraftingCategory("Enchanting_Shovel", "Shovel", "Icons/CraftingCategories/ShovelTab.png");
```

---

### EnchantmentBuilder

Fluent builder for configuring and registering addon enchantments. Obtained via [`api.registerEnchantment()`](#registerenchantment).

After calling `.build()`, the enchantment is:
- Registered in the global `EnchantmentRegistry`
- Available via the `/enchant` command
- Visible in the config UI with an enable/disable toggle
- Given a configurable multiplier slider (if `multiplierPerLevel > 0`)
- Auto-generated scroll items (if scrolls are defined)

---

#### `description(String description)`

Sets the enchantment description shown in UI tooltips.

| Parameter | Type | Default |
|---|---|---|
| `description` | `String` | `""` |

```java
.description("Chance to find gold ore when digging dirt")
```

---

#### `maxLevel(int maxLevel)`

Sets the maximum enchantment level.

| Parameter | Type | Default | Constraints |
|---|---|---|---|
| `maxLevel` | `int` | `1` | Must be ≥ 1 |

```java
.maxLevel(3)
```

---

#### `requiresDurability(boolean requiresDurability)`

Sets whether this enchantment requires the item to have durability.

| Parameter | Type | Default |
|---|---|---|
| `requiresDurability` | `boolean` | `false` |

```java
.requiresDurability(true)
```

---

#### `legendary(boolean isLegendary)`

Marks this enchantment as legendary — only one legendary enchantment can be on an item at a time.

| Parameter | Type | Default |
|---|---|---|
| `isLegendary` | `boolean` | `false` |

```java
.legendary(true)
```

---

#### `multiplierPerLevel(double multiplier)`
#### `multiplierPerLevel(double multiplier, String label)`

Sets the primary effect multiplier per level. When greater than `0`, the enchantment gets a configurable multiplier slider in the config UI. When `0`, the enchantment is treated as binary (on/off).

The second variation allows you to optionally set a custom label to display next to the slider in the config UI. If not set, it defaults to the translated name for your enchantment (or an empty string if missing).

| Parameter | Type | Default |
|---|---|---|
| `multiplier` | `double` | `0.0` |
| `label` | `String` | Falls back to namespace translation |

```java
.multiplierPerLevel(0.10, "Damage Per Level")
```

---

#### `addMultiplier(String key, double defaultValue, String labelKey)`

Adds a secondary configurable multiplier that will appear as an additional slider in the in-game configuration UI underneath the primary one. Useful for enchantments with multiple dynamic effects (e.g. chance + duration).

| Parameter | Type | Description |
|---|---|---|
| `key` | `String` | The unique suffix for this multiplier (e.g. `"duration"`). The final config key will be prefixed with your enchantment ID (e.g. `"my_mod:lightning:duration"`). |
| `defaultValue` | `double` | The default value for the multiplier. |
| `labelKey` | `String` | A translation key or raw text for the label displayed next to the slider in the UI. |

```java
.addMultiplier("duration", 5.0, "config.multiplier.lightning.duration")
```

---

#### `scale(ScaleType scaleType)`

Sets the scaling curve using a predefined [`ScaleType`](#scaletype). Defaults to `LINEAR` if not called.

| Parameter | Type |
|---|---|
| `scaleType` | `ScaleType` |

```java
.scale(ScaleType.DIMINISHING)
```

---

#### `scale(double exponent)`

Sets the scaling curve using a power exponent. Formula: `level^exponent * multiplierPerLevel`.

| Parameter | Type | Examples |
|---|---|---|
| `exponent` | `double` | `1.0` = linear, `2.0` = quadratic, `0.5` = diminishing |

```java
.scale(0.5) // diminishing returns (square root)
```

---

#### `scale(IntToDoubleFunction function)`

Sets a fully custom scaling function. The function receives the enchantment level and must return the **total** scaled multiplier for that level (not per-level).

| Parameter | Type |
|---|---|
| `function` | `IntToDoubleFunction` (level → total multiplier) |

```java
.scale(level -> level * level * 0.05) // custom quadratic
```

---

#### `bonusDescription(String template)`

Sets the bonus description template shown in tooltips and the walkthrough. Use `{amount}` as a placeholder for the calculated per-level value.

| Parameter | Type | Default |
|---|---|---|
| `template` | `String` | `""` |

```java
.bonusDescription("Mined dirt has a {amount}% chance to drop gold ore instead")
```

---

#### `modDisplayName(String modName)`

Sets the display name of the mod that owns this enchantment. This name is shown in the scroll description and the walkthrough page. If not set, it falls back to the mod ID (the namespace part of the enchantment ID).

| Parameter | Type | Default |
|---|---|---|
| `modName` | `String` | Falls back to namespace of ID |

```java
.modDisplayName("My Awesome Mod")
```

---

#### `walkthrough(String text)`

Sets custom walkthrough text for the `/enchanting` page. Use `{amount}` as a placeholder. If not set, defaults to the bonus description template.

| Parameter | Type | Default |
|---|---|---|
| `text` | `String` | Falls back to `bonusDescription` |

```java
.walkthrough("While digging dirt blocks with a shovel, there is a chance "
    + "the block will drop gold ore instead of dirt. Each level increases "
    + "the chance by {amount}%.")
```

---

#### `appliesTo(ItemCategory... categories)`

Adds item categories this enchantment can be applied to. At least one category is **required** before calling `build()`.

| Parameter | Type | Constraints |
|---|---|---|
| `categories` | `ItemCategory...` | At least one required; see [ItemCategory](#itemcategory) |

```java
.appliesTo(ItemCategory.MELEE_WEAPON, ItemCategory.RANGED_WEAPON)
```

---

#### `craftingCategory(String category)`

Sets which tab in the Enchanting Table this enchantment's scrolls appear under. If not set, auto-derived from the primary item category.

| Parameter | Type | Default |
|---|---|---|
| `category` | `String` | Auto-derived from `appliesTo()` categories |

**Auto-derivation rules:**
| Item Category | Default Crafting Category |
|---|---|
| `MELEE_WEAPON` | `Enchanting_Melee` |
| `RANGED_WEAPON` | `Enchanting_Ranged` |
| `ARMOR`, `BOOTS` | `Enchanting_Armor` |
| `SHIELD` | `Enchanting_Shield` |
| `STAFF`, `STAFF_MANA`, `STAFF_ESSENCE` | `Enchanting_Staff` |
| `PICKAXE`, `AXE`, `SHOVEL`, `TOOL` | `Enchanting_Tools` |

```java
.craftingCategory("Enchanting_Shovel") // custom category
```

---

#### `scroll(int level)`

Starts building a scroll definition for a specific enchantment level. Returns a [`ScrollBuilder`](#scrollbuilder) that chains back to this builder via `.end()`.

| Parameter | Type | Constraints |
|---|---|---|
| `level` | `int` | Must be ≥ 1 |

```java
.scroll(1)
    .quality("Uncommon")
    .craftingTier(1)
    .ingredient("Ingredient_Crystal_Yellow", 5)
    .end()
```

---

#### `build()`

Builds and registers the enchantment. After this call, the enchantment is fully registered and active.

**Returns:** The registered `EnchantmentType` instance.

**Throws:**
- `IllegalStateException` if no item categories were specified via `appliesTo()`
- `IllegalStateException` if an enchantment with this ID already exists

```java
EnchantmentType myEnchant = builder.build();
```

---

### ScrollBuilder

Fluent builder for configuring a single scroll level within an enchantment. Obtained via [`EnchantmentBuilder.scroll(int level)`](#scrollint-level).

---

#### `quality(String quality)`

Sets the rarity/quality of this scroll level.

| Parameter | Type | Default | Valid Values |
|---|---|---|---|
| `quality` | `String` | `"Uncommon"` | `"Common"`, `"Uncommon"`, `"Rare"`, `"Epic"`, `"Legendary"` |

```java
.quality("Rare")
```

---

#### `craftingTier(int tier)`

Sets the crafting tier required to craft this scroll. Corresponds to the Enchanting Table upgrade level.

| Parameter | Type | Default | Constraints |
|---|---|---|---|
| `tier` | `int` | `1` | Must be 1–4 |

```java
.craftingTier(2)
```

---

#### `craftingCategory(String category)`

Overrides which tab this scroll appears under in the Enchanting Table. If not set, inherits from the parent `EnchantmentBuilder`.

| Parameter | Type | Default |
|---|---|---|
| `category` | `String` | Inherited from enchantment |

```java
.craftingCategory("Enchanting_Melee")
```

---

#### `ingredient(String itemId, int quantity)`

Adds a crafting ingredient for this scroll level. Call multiple times to add multiple ingredients.

| Parameter | Type | Constraints |
|---|---|---|
| `itemId` | `String` | The item ID (e.g. `"Ingredient_Crystal_Blue"`) |
| `quantity` | `int` | Must be ≥ 1 |

```java
.ingredient("Ingredient_Crystal_Yellow", 5)
.ingredient("Soil_Dirt", 50)
.ingredient("Ore_Gold", 10)
```

---

#### `icon(String iconPath)`

Overrides the scroll icon for this level. If not set, uses the default Simple Enchantments scroll icon.

| Parameter | Type | Default |
|---|---|---|
| `iconPath` | `String` | SE default scroll icon |

```java
.icon("Icons/MyScroll.png")
```

---

#### `model(String modelPath)`

Overrides the scroll 3D model for this level.

| Parameter | Type | Default |
|---|---|---|
| `modelPath` | `String` | SE default scroll model |

```java
.model("Items/MyScroll.blockymodel")
```

---

#### `texture(String texturePath)`

Overrides the scroll texture for this level.

| Parameter | Type | Default |
|---|---|---|
| `texturePath` | `String` | SE default scroll texture |

```java
.texture("Items/MyScroll.png")
```

---

#### `iconProperties(float scale, float translationX, float translationY, float rotationX, float rotationY, float rotationZ)`

Overrides the scroll icon rendering properties (size, offset, and 3D rotation) in the UI and ground drops.

| Parameter | Type | Default |
|---|---|---|
| `scale` | `float` | `0.84f` |
| `translationX`, `translationY`| `float` | `5f`, `15f` |
| `rotationX`, `rotationY`, `rotationZ` | `float` | `90f`, `45f`, `0f` |

```java
.iconProperties(0.9f, 6f, 16f, 90f, 45f, 0f)
```

---

#### `end()`

Returns to the parent `EnchantmentBuilder`. Only available when the `ScrollBuilder` was created via `EnchantmentBuilder.scroll()`.

**Throws:** `IllegalStateException` if called on a standalone `ScrollBuilder`.

```java
.scroll(1)
    .quality("Uncommon")
    .craftingTier(1)
    .ingredient("My_Crystal", 3)
    .end() // returns to EnchantmentBuilder
```

---

#### `build()`

Builds the `ScrollDefinition` (standalone usage only — not needed when chaining via `.end()`).

**Returns:** The `ScrollDefinition` instance.

---

### ScaleType

Predefined scaling curves for enchantment multipliers. Used with [`EnchantmentBuilder.scale(ScaleType)`](#scalescaletype-scaletype).

| Value | Formula | Description |
|---|---|---|
| `LINEAR` | `level × multiplier` | Equal gains per level **(default)** |
| `QUADRATIC` | `level² × multiplier` | Accelerating returns — higher levels are disproportionately stronger |
| `DIMINISHING` | `√level × multiplier` | Front-loaded gains — first levels matter most, later levels taper off |
| `EXPONENTIAL` | `(2^level − 1) × multiplier` | Extreme late-game power spikes |
| `LOGARITHMIC` | `ln(level + 1) × multiplier` | Soft cap — gains slow down significantly at higher levels |

```java
import org.herolias.plugin.api.ScaleType;

// Diminishing returns: level 1 → 100%, level 2 → 141%, level 3 → 173%
.multiplierPerLevel(0.15)
.scale(ScaleType.DIMINISHING)
```

---

### ItemCategory

Categorizes items for enchantment applicability. Used with [`EnchantmentBuilder.appliesTo()`](#appliestocategory-categories) and [`api.registerItemToCategory()`](#registeritemtocategory).

| Constant | ID | Description |
|---|---|---|
| `ItemCategory.MELEE_WEAPON` | `"MELEE_WEAPON"` | Swords, axes, maces, etc. |
| `ItemCategory.RANGED_WEAPON` | `"RANGED_WEAPON"` | Bows, crossbows, etc. |
| `ItemCategory.TOOL` | `"TOOL"` | Hoes, scythes, sickles, shears, etc. |
| `ItemCategory.PICKAXE` | `"PICKAXE"` | Pickaxes (mining tools) |
| `ItemCategory.SHOVEL` | `"SHOVEL"` | Shovels (digging tools) |
| `ItemCategory.AXE` | `"AXE"` | Hatchets and axes (not battleaxes, which are `MELEE_WEAPON`) |
| `ItemCategory.SHIELD` | `"SHIELD"` | Shields |
| `ItemCategory.BOOTS` | `"BOOTS"` | Boots (foot armor) |
| `ItemCategory.HELMET` | `"HELMET"` | Helmets (head armor) |
| `ItemCategory.ARMOR` | `"ARMOR"` | General armor (chestplates, leggings, etc.) |
| `ItemCategory.GLOVES` | `"GLOVES"` | Gloves (hand armor) |
| `ItemCategory.STAFF` | `"STAFF"` | Staffs (magic weapons) |
| `ItemCategory.STAFF_MANA` | `"STAFF_MANA"` | Mana staffs (consume mana) |
| `ItemCategory.STAFF_ESSENCE` | `"STAFF_ESSENCE"` | Essence staffs (consume items) |

```java
import org.herolias.plugin.enchantment.ItemCategory;

.appliesTo(ItemCategory.SHOVEL)
```

---

### Events

Simple Enchantments fires events via the Hytale `EventBus` that addon mods can listen to.

---

#### `ItemEnchantedEvent`

Fired when an item is successfully enchanted.

```java
import org.herolias.plugin.api.event.ItemEnchantedEvent;
```

| Method | Return Type | Description |
|---|---|---|
| `getPlayerRef()` | `@Nullable PlayerRef` | The player who enchanted the item, or `null` if done via command/console |
| `getItem()` | `@Nonnull ItemStack` | The item that was enchanted |
| `getEnchantment()` | `@Nonnull EnchantmentType` | The enchantment that was applied |
| `getLevel()` | `int` | The level of the enchantment applied |

---

#### `EnchantmentActivatedEvent`

Fired when an enchantment successfully activates its effect (e.g. Fortune triggers extra drops, Sharpness deals extra damage).

```java
import org.herolias.plugin.api.event.EnchantmentActivatedEvent;
```

| Method | Return Type | Description |
|---|---|---|
| `getPlayerRef()` | `@Nullable PlayerRef` | The player whose enchantment activated, or `null` for non-player entities |
| `getItem()` | `@Nonnull ItemStack` | The item that triggered the enchantment effect |
| `getEnchantment()` | `@Nonnull EnchantmentType` | The enchantment that activated |
| `getLevel()` | `int` | The current level of the activated enchantment |

---

#### Firing Events (for addon enchantments)

When implementing custom enchantment logic, use `EnchantmentEventHelper` to fire the activation event:

```java
import org.herolias.plugin.enchantment.EnchantmentEventHelper;

// Inside your ECS system or event handler:
EnchantmentEventHelper.fireActivated(playerRef, item, enchantmentType, level);
```

---

### EnchantmentType

The `EnchantmentType` class represents a registered enchantment instance. Returned by [`EnchantmentBuilder.build()`](#build) and [`api.getRegisteredEnchantment()`](#getregisteredenchantment).

Key methods available on `EnchantmentType`:

| Method | Return Type | Description |
|---|---|---|
| `getId()` | `String` | The enchantment ID (e.g. `"sharpness"`, `"example:gold_digger"`) |
| `getDisplayName()` | `String` | The human-readable name |
| `getDescription()` | `String` | The enchantment description |
| `getMaxLevel()` | `int` | The maximum level |
| `requiresDurability()` | `boolean` | Whether the item needs durability |
| `isLegendary()` | `boolean` | Whether this is a legendary enchantment |
| `isBuiltIn()` | `boolean` | Whether this is a built-in SE enchantment |
| `getOwnerModId()` | `String` | The namespace/mod ID (e.g. `"example"`) |
| `getApplicableCategories()` | `Set<ItemCategory>` | Item categories this enchantment applies to |
| `getEffectMultiplier()` | `double` | The current multiplier per level (from config) |
| `getDefaultMultiplierPerLevel()` | `double` | The default multiplier per level |
| `getScaledMultiplier(int level)` | `double` | The total scaled multiplier for a given level |
| `canApplyTo(ItemCategory category)` | `boolean` | Whether this can apply to the given category |
| `conflictsWith(EnchantmentType other)` | `boolean` | Whether this conflicts with another enchantment |
| `getFormattedName(int level)` | `String` | Display name with Roman numeral level (e.g. `"Sharpness III"`) |
| `getBonusDescription(int level)` | `String` | Formatted bonus text for a specific level |

---

## Full Example

The following is the full example from this project, demonstrating how to register a custom crafting category, register a custom enchantment with scrolls, and implement the enchantment's ECS logic.

### Plugin Entry Point (`EnchantmentAPIexample.java`)

```java
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
            api.registerCraftingCategory("Enchanting_Shovel", "Shovel",
                "Icons/CraftingCategories/ShovelTab.png");
            LOGGER.atInfo().log("Registered crafting category: Enchanting_Shovel");

            // ─── Register the Gold Digger enchantment ───
            EnchantmentType goldDigger = api.registerEnchantment("example:gold_digger",
                    "Gold Digger")
                .description("Chance to find gold ore when digging dirt")
                .maxLevel(3)
                .multiplierPerLevel(0.10)
                .bonusDescription(
                    "Mined dirt has a {amount}% chance to drop gold ore instead")
                .walkthrough("While digging dirt blocks with a shovel, there is a "
                    + "chance the block will drop gold ore instead of dirt. Each "
                    + "level increases the chance by {amount}%.")
                .appliesTo(ItemCategory.SHOVEL)
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
                + " (id=" + goldDigger.getId()
                + ", maxLevel=" + goldDigger.getMaxLevel() + ")");

        } catch (NoClassDefFoundError | Exception e) {
             LOGGER.atSevere().log(
                 "Failed to setup EnchantmentAPIexample: " + e.getMessage());
        }

        this.getEntityStoreRegistry().registerSystem(
            new org.herolias.plugin.enchantment.GoldDiggerSystem());
        LOGGER.atInfo().log("Registered GoldDiggerSystem with ECS");

        this.getCommandRegistry().registerCommand(new EnchantmentCommand());
    }
}
```

### Custom Enchantment ECS System (`GoldDiggerSystem.java`)

This system listens for `BreakBlockEvent` and applies the Gold Digger effect — replacing dirt drops with gold ore based on the enchantment level and its scaled multiplier.

```java
// Key logic inside the event handler:
EnchantmentApi api = EnchantmentApiProvider.get();

int level = api.getEnchantmentLevel(tool, "example:gold_digger");
if (level <= 0) return;

EnchantmentType type = api.getRegisteredEnchantment("example:gold_digger");
double chance = type.getScaledMultiplier(level);

if (ThreadLocalRandom.current().nextDouble() < chance) {
    // Cancel the original drop, spawn gold ore instead
    event.setCancelled(true);
    ItemStack goldDrop = new ItemStack("Ore_Gold", 1);
    // ... spawn the gold drop at the block position

    // Fire the activation event for listeners
    EnchantmentEventHelper.fireActivated(playerRef, tool, type, level);
}
```

### Command Example (`EnchantmentCommand.java`)

A command that demonstrates using the API to manage enchantments on held items:

```
/enchantment add <enchantment> <level>    — Adds an enchantment to the held item
/enchantment remove <enchantment>         — Removes an enchantment from the held item
/enchantment get_level <enchantment>      — Gets the level of an enchantment on the held item
/enchantment has_enchant <enchantment>    — Checks if the held item has an enchantment
/enchantment check                        — Lists all enchantments on the held item
```

See `src/main/java/org/herolias/plugin/command/EnchantmentCommand.java` for the full implementation.
