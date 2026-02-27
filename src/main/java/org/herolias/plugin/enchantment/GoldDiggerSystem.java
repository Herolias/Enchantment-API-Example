package org.herolias.plugin.enchantment;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.modules.interaction.BlockInteractionUtils;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import org.herolias.plugin.api.EnchantmentApi;
import org.herolias.plugin.api.EnchantmentApiProvider;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ECS system that applies the Gold Digger enchantment to block break events.
 */
public class GoldDiggerSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public GoldDiggerSystem() {
        super(BreakBlockEvent.class);
        LOGGER.atInfo().log("GoldDiggerSystem initialized");
    }

    @Override
    @Nonnull
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }

    @Override
    public void handle(int index,
                       @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                       @Nonnull Store<EntityStore> store,
                       @Nonnull CommandBuffer<EntityStore> commandBuffer,
                       @Nonnull BreakBlockEvent event) {
        if (event.isCancelled()) {
            return;
        }

        ItemStack tool = event.getItemInHand();
        if (tool == null || tool.isEmpty()) {
            return;
        }

        EnchantmentApi api = EnchantmentApiProvider.get();
        if (api == null) {
            return;
        }

        int level = api.getEnchantmentLevel(tool, "example:gold_digger");
        if (level <= 0) {
            return;
        }

        EnchantmentType type = api.getRegisteredEnchantment("example:gold_digger");
        if (type == null) {
            return;
        }

        BlockType blockType = event.getBlockType();
        if (blockType == null || blockType.getId() == null) {
            return;
        }

        // Target specifically Soil_* 
        if (!blockType.getId().startsWith("Soil_")) {
            return;
        }

        // Drop chance based on dynamic scale (from config or scale function).
        double chance = type.getScaledMultiplier(level);
        if (ThreadLocalRandom.current().nextDouble() >= chance) {
            return;
        }

        Vector3i targetBlock = event.getTargetBlock();
        World world = store.getExternalData().getWorld();
        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
        Ref<ChunkStore> chunkRef = chunkStore.getExternalData().getChunkReference(
            ChunkUtil.indexChunkFromBlock(targetBlock.getX(), targetBlock.getZ())
        );
        if (chunkRef == null || !chunkRef.isValid()) {
            return;
        }

        BlockChunk blockChunk = chunkStore.getComponent(chunkRef, BlockChunk.getComponentType());
        if (blockChunk == null) {
            return;
        }

        BlockSection blockSection = blockChunk.getSectionAtBlockY(targetBlock.getY());
        int filler = blockSection.getFiller(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());

        Ref<EntityStore> breakerRef = archetypeChunk.getReferenceTo(index);
        boolean naturalAction = breakerRef != null && breakerRef.isValid()
            ? BlockInteractionUtils.isNaturalAction(breakerRef, store)
            : BlockInteractionUtils.isNaturalAction(null, store);

        int setBlockSettings = 0;
        setBlockSettings |= 0x100;
        if (!naturalAction) {
            setBlockSettings |= 0x800;
        }

        // Cancel the original event so that dirt doesn't drop.
        event.setCancelled(true);
        BlockHarvestUtils.naturallyRemoveBlock(targetBlock, blockType, filler, 0, null, null, setBlockSettings, chunkRef, store, chunkStore);

        // Drop Ore_Gold
        ItemStack goldDrop = new ItemStack("Ore_Gold", 1);
        Vector3d dropPosition = new Vector3d(targetBlock.getX() + 0.5, targetBlock.getY(), targetBlock.getZ() + 0.5);
        Holder<EntityStore>[] itemEntities = ItemComponent.generateItemDrops(commandBuffer, List.of(goldDrop), dropPosition, Vector3f.ZERO);
        if (itemEntities.length > 0) {
            commandBuffer.addEntities(itemEntities, AddReason.SPAWN);
        }

        com.hypixel.hytale.server.core.universe.PlayerRef playerRef = null;
        if (breakerRef != null && breakerRef.isValid()) {
            playerRef = store.getComponent(breakerRef, com.hypixel.hytale.server.core.universe.PlayerRef.getComponentType());
        }

        EnchantmentEventHelper.fireActivated(playerRef, tool, type, level);
    }
}
