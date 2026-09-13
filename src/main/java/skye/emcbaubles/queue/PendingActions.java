package skye.emcbaubles.queue;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.*;

import skye.emcbaubles.queue.InventorySnapshots.InventorySnapshot;

import static skye.emcbaubles.queue.InventorySnapshots.INVENTORY_SNAPSHOTS;
import static skye.emcbaubles.util.EMCUtils.refillDepletedHeldStackFromEMCStorage;
import static skye.emcbaubles.util.ItemTrackingUtils.*;

public final class PendingActions {
    public static final List<PendingAction> PENDING_ACTIONS = new ArrayList<>();
    private static final Map<EntityPlayer, Long> LAST_QUEUED_RESTORE_TICK = new WeakHashMap<>();

    public abstract static class PendingAction extends ExpirablePlayer implements ITickable{
        private static final int TICKS_TO_DEFER = 1;
        protected int ticksLeft;

        PendingAction(EntityPlayer player) {
            super(player);
            PENDING_ACTIONS.add(this);
            setTicks(TICKS_TO_DEFER);
        }

        public abstract void resolve();
        public int getTicks() { return ticksLeft; }
        public void setTicks(int ticks) { ticksLeft = ticks; }
    }

    public final static class PendingGive extends PendingAction {
        private final ItemStack STACK;

        public PendingGive(ItemStack stack, EntityPlayer player) {
            super(player);
            this.STACK = stack;
        }

        public void resolve() {
            if (playerReferenceInvalid()) return;

            refillDepletedHeldStackFromEMCStorage(STACK, getPlayer());
        }
    }

    public final static class PendingAddToSlot extends PendingAction {
        private final int HOTBAR_SLOT;
        private final ItemStack STACK;

        public PendingAddToSlot(int hotbarSlot, ItemStack stack, EntityPlayer player) {
            super(player);
            this.HOTBAR_SLOT = hotbarSlot;
            this.STACK = stack;
        }

        public void resolve() {
            if (playerReferenceInvalid()) return;

            // Cancel deferred item stack replacement if slot is now filled
            EntityPlayer player = getPlayer();
            if (inventorySlotOccupied(HOTBAR_SLOT, player)) return;

            refillDepletedHeldStackFromEMCStorage(HOTBAR_SLOT, STACK, player);
        }
    }

    public final static class PendingCheckToReplace extends PendingAction {
        private final int INVENTORY_SLOT;
        private final ItemStack STACK;
        private final UUID TRACK_ID;

        public PendingCheckToReplace(int inventorySlot, ItemStack stack, EntityPlayer player, UUID trackId) {
            super(player);
            this.INVENTORY_SLOT = inventorySlot;
            this.STACK = stack;
            this.TRACK_ID = trackId;
        }

        public void resolve() {
            if (playerReferenceInvalid()) return;

            // Cancel deferred item stack replacement if slot is now filled
            EntityPlayer player = getPlayer();

            if (!inventorySlotOccupied(INVENTORY_SLOT, player))
                restoreStackIfDepletedToSlot(INVENTORY_SLOT, STACK, player, TRACK_ID);
            else {
                // stop tracking non-depleted stack now
                if (INVENTORY_SLOT == -1) stopTrackingStack(player.inventory.offHandInventory.getFirst());
                else stopTrackingStack(player.inventory.mainInventory.get(INVENTORY_SLOT));
            }
        }
    }

    public final static class PendingRestoreInventory extends PendingAction {
        private static final int TICKS_TO_DEFER = 0;
        private final InventorySnapshot INVENTORY_SNAPSHOT;

        public PendingRestoreInventory(EntityPlayer player) {
            super(player);
            setTicks(TICKS_TO_DEFER);
            long currentTick = player.getEntityWorld().getTotalWorldTime();
            LAST_QUEUED_RESTORE_TICK.put(player, currentTick);
            INVENTORY_SNAPSHOT = INVENTORY_SNAPSHOTS.get(player);
        }

        public void resolve() {
            if (playerReferenceInvalid()) return;

            EntityPlayer player = getPlayer();
            for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
                // empty stacks are not copied into snapshot
                ItemStack stack = INVENTORY_SNAPSHOT.getItemStackAt(i);
                if (stack == null || stack.isEmpty()) continue;

                // Don't check if slot now occupied for each stack to replace w/ inventorySlotOccupied
                // if (inventorySlotOccupied(INVENTORY_SLOT, player)) return false;

                restoreStackIfDepletedToSlot(i, stack, player, INVENTORY_SNAPSHOT.getTrackIdAt(i));
            }
        }
    }

    public static boolean withinSameTickAsLastQueuedRestoreInventory(EntityPlayer player) {
        long currentTick = player.getEntityWorld().getTotalWorldTime();
        Long lastQueuedTick = LAST_QUEUED_RESTORE_TICK.get(player);
        if (lastQueuedTick == null || lastQueuedTick != currentTick) return false;
        LAST_QUEUED_RESTORE_TICK.put(player, currentTick);
        return true;
    }

    private static boolean inventorySlotOccupied(int inventorySlot, EntityPlayer player) {
        ItemStack stack;
        if (inventorySlot == -1) stack = player.getHeldItemOffhand();
        else stack = player.inventory.getStackInSlot(inventorySlot);

        return !stack.isEmpty();
    }

    private static boolean trackedStackMatchFound(ItemStack stack, UUID trackId) {
        if (stack.isEmpty()) return false;
        if (!trackIdMatchesStack(stack, trackId)) {
            return false;
        }
        else {
            stopTrackingStack(stack);
            return true;
        }
    }

    public static void restoreStackIfDepletedToSlot(int inventorySlot, ItemStack stack, EntityPlayer player, UUID trackId) {
        for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
            ItemStack candidateStack = player.inventory.mainInventory.get(i);
            if (trackedStackMatchFound(candidateStack, trackId)) return;
        }

        ItemStack candidateStack = player.inventory.offHandInventory.getFirst();
        if (trackedStackMatchFound(candidateStack, trackId)) return;

        // Item stack fully depleted
        refillDepletedHeldStackFromEMCStorage(inventorySlot, stack, player);
    }
}
