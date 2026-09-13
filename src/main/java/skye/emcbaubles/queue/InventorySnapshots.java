package skye.emcbaubles.queue;

import moze_intel.projecte.gameObjs.items.MercurialEye;
import moze_intel.projecte.gameObjs.items.tools.PEToolBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;

import java.util.*;

import static skye.emcbaubles.util.ItemTrackingUtils.*;
import static skye.emcbaubles.util.MiscUtils.stackIsDamageableOrTool;

public final class InventorySnapshots {
    public static final Map<EntityPlayer, InventorySnapshot> INVENTORY_SNAPSHOTS = new WeakHashMap<>();

    public static class InventorySnapshot extends ExpirablePlayer implements ITickable {
        private final ItemStack[] INVENTORY_STACKS;
        private final UUID[] TRACK_IDS;
        private static final int TICKS_TO_DESTROY = 1;
        private int ticksLeft;

        public InventorySnapshot(ItemStack[] inventoryStacks, EntityPlayer player, UUID[] trackIds) {
            super(player);
            INVENTORY_SNAPSHOTS.put(player, this);
            this.INVENTORY_STACKS = inventoryStacks;
            this.TRACK_IDS = trackIds;
            setTicks(TICKS_TO_DESTROY);
        }

        public ItemStack getItemStackAt(int i) { return INVENTORY_STACKS[i]; }
        public UUID getTrackIdAt(int i) { return TRACK_IDS[i]; }
        public int getTicks() { return ticksLeft; }
        public void setTicks(int ticks) { ticksLeft = ticks; }

        public void removeSnapshot() {
            INVENTORY_SNAPSHOTS.remove(getPlayer());
            stopTrackingInventory(getPlayer());
        }
    }

    public static boolean shouldSnapshotInventory(ItemStack stack, Item item) {
        return (stack.getMaxStackSize() == 1 &&
                (!(item instanceof ItemSpade) && !(item instanceof ItemHoe) && !(item instanceof ItemFlintAndSteel)
                && !(item instanceof PEToolBase) && !(item instanceof ItemBucket) && !(item instanceof ItemBow)
                && !(item instanceof MercurialEye)));
    }

    public static void snapshotInventory(EntityPlayer player) {
        UUID[] inventoryToRestoreUUIDs = new UUID[36];
        ItemStack[] inventoryToRestoreStacks = new ItemStack[36];
        for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
            ItemStack stackAtSlot = player.inventory.mainInventory.get(i);
            if (stackIsDamageableOrTool(stackAtSlot)) continue;
            if (stackAtSlot.isEmpty()) continue;

            UUID trackId = UUID.randomUUID();
            inventoryToRestoreUUIDs[i] = trackId;
            inventoryToRestoreStacks[i] = stackAtSlot.copy();
            beginTrackingStack(stackAtSlot, trackId);
        }
        new InventorySnapshot(inventoryToRestoreStacks, player, inventoryToRestoreUUIDs);
    }
}
