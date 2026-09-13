package skye.emcbaubles.eventhandler;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.EnumHand;

import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Finish;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Method;
import java.util.*;

import skye.emcbaubles.Reference;

import static skye.emcbaubles.EMCBaubles.LOGGER;
import static skye.emcbaubles.queue.InventorySnapshots.*;
import static skye.emcbaubles.util.EMCUtils.*;
import static skye.emcbaubles.util.ItemTrackingUtils.isTrackingStack;
import static skye.emcbaubles.util.ItemTrackingUtils.beginTrackingStack;
import static skye.emcbaubles.queue.PendingActions.*;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class ItemRefillHandler {
    public static Map<EntityPlayer, Boolean> cancelEmptyContainerReplacement = new WeakHashMap<>();

    private static final Method GET_ARROW_STACK_METHOD
        = ObfuscationReflectionHelper.findMethod(EntityArrow.class, "func_184550_j", ItemStack.class);

    private static boolean failsStandardRefillEventChecks(EntityPlayer player) {
        if (player.getEntityWorld().isRemote) return true;
        return !player.getTags().contains("emc_refill_items");
    }

    private static void resetToolItemStack(ItemStack tool) {
        tool.setItemDamage(0);
        EnchantmentHelper.setEnchantments(new HashMap<>(), tool);
        tool.setRepairCost(0);
        tool.clearCustomName();
    }

    private static int getHotbarSlot(EntityPlayer player, EnumHand hand) {
        // Value of -1 indicates the item was held in the off-hand
        if (hand == EnumHand.OFF_HAND) return -1;
        return player.inventory.currentItem;
    }

    private static void attemptSnapshotOrQueueCheckToReplace(PlayerInteractEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (failsStandardRefillEventChecks(player)) return;

        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();

        if (item.getMaxItemUseDuration(stack) > 0) return;

        if (shouldSnapshotInventory(stack, item)) snapshotInventory(player);
        else {
            if (item instanceof ItemRecord || item instanceof ItemBucket) return;

            EnumHand hand = event.getHand();

            // If stack is tracked, it's associated resolve restore inventory/check to replace has not run yet
            if (isTrackingStack(stack)) return;

            UUID trackId = UUID.randomUUID();
            beginTrackingStack(stack, trackId);

            int hotbarSlot = getHotbarSlot(player, hand);
            ItemStack stackCopy = stack.copy();

            if (stackCopy.isItemStackDamageable() && stackCopy.getMaxStackSize() == 1) return;

            new PendingCheckToReplace(hotbarSlot, stackCopy, player, trackId);
        }
    }

    private static int findOneSingleArrowInInventorySlot(EntityPlayer player, ItemStack arrowStack) {
        int matchingStackCount = 0;
        int matchingTotalCount = 0;
        int matchingSlot = -2;
        ItemStack lastMatchingArrowStack = null;

        for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
            ItemStack stack = player.inventory.mainInventory.get(i);
            if (stack.isEmpty()) continue;
            if (ItemStack.areItemsEqual(stack, arrowStack)
                    && ItemStack.areItemStackTagsEqual(stack, arrowStack)) {
                matchingStackCount++;
                matchingTotalCount += stack.getCount();
                matchingSlot = i;
                lastMatchingArrowStack = stack;
            }
        }

        ItemStack offHandStack = player.inventory.offHandInventory.getFirst();
        if (!offHandStack.isEmpty()
                && ItemStack.areItemsEqual(offHandStack, arrowStack)
                && ItemStack.areItemStackTagsEqual(offHandStack, arrowStack)) {
            matchingStackCount++;
            matchingTotalCount += offHandStack.getCount();
            matchingSlot = -1;
            lastMatchingArrowStack = offHandStack;
        }

        // exactly one matching stack, and that stack has exactly 1 item
        if (matchingStackCount == 1 && matchingTotalCount == 1) {
            lastMatchingArrowStack.setCount(0);
            return matchingSlot;
        }
        return -2;
    }

    // Handle broken tool and weapon replacement (does not handle broken bow replacement)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemBreak(PlayerDestroyItemEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (failsStandardRefillEventChecks(player)) return;

        // Exit if item destroyed is not a damageable tool w/ a maximum stack size of 1.
        ItemStack originalTool = event.getOriginal();
        if (!(originalTool.isItemStackDamageable() && originalTool.getMaxStackSize() == 1)) return;

        // Tool broken by a different means then using the tool
        EnumHand hand = event.getHand();
        if (hand == null) return;

        // Wipe special characteristics of the original tool & fully repair it
        ItemStack toolCopy = originalTool.copy();
        resetToolItemStack(toolCopy);

        int hotbarSlot = getHotbarSlot(player, hand);
        new PendingAddToSlot(hotbarSlot, toolCopy, player);
    }

    // Handle replacement of broken bow
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onArrowLoose(ArrowLooseEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (failsStandardRefillEventChecks(player)) return;

        EnumHand hand = player.getActiveHand();
        int hotbarSlot = getHotbarSlot(player, hand);

        // Held item is of type bow (not archangel's smite or something similar)
        ItemStack heldItemStack = player.getHeldItem(hand);

        if (!(heldItemStack.getItem() instanceof ItemBow)) return;

        UUID trackId = UUID.randomUUID();
        beginTrackingStack(heldItemStack, trackId);

        // Wipe special characteristics of the original tool & fully repair it
        ItemStack bowCopy = heldItemStack.copy();
        resetToolItemStack(bowCopy);

        new PendingCheckToReplace(hotbarSlot, bowCopy, player, trackId);
    }

    // Handle replacement of emptied arrow stack from shooting (only replaces if there are zero item stacks of the kind
    // of arrow shot left in the player's inventory)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityJoin(EntityJoinWorldEvent event) {
        if (!(event.getEntity() instanceof EntityArrow arrow)) return;
        if (!(arrow.shootingEntity instanceof EntityPlayer player)) return;
        if (failsStandardRefillEventChecks(player)) return;

        EnumHand hand = player.getActiveHand();

        // Check that held item is a type of bow (not archangel's smite or something similar)
        ItemStack heldItemStack = player.getHeldItem(hand);
        if (!(heldItemStack.getItem() instanceof ItemBow || heldItemStack.getItem() instanceof ItemAir)) return;

        try {
            ItemStack arrowStack = (ItemStack) GET_ARROW_STACK_METHOD.invoke(arrow);
            if (arrowStack.isEmpty()) return;

            // Do not trigger arrow replacement on bow w/ infinity enchantment using regular arrows
            boolean hasInfinity = EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, heldItemStack) > 0;
            Item arrowItem = arrowStack.getItem();
            if (hasInfinity && arrowItem == Items.ARROW) return;

            int arrowSlot = findOneSingleArrowInInventorySlot(player, arrowStack);
            // Only refills if inventory has only single arrow left of the type of arrow shot.
            if (arrowSlot == -2) return;

            ItemStack arrowStackCopy = arrowStack.copy();
            new PendingAddToSlot(arrowSlot, arrowStackCopy, player);
        }
        catch (java.lang.reflect.InvocationTargetException e) {
            // This happens if the getArrowStack() method itself crashed internally while running
            LOGGER.error("EMCBaubles - Failed to read arrow item stack context. " +
                    "The entity's internal getArrowStack method threw an error.", e.getCause());
        }
        catch (java.lang.IllegalAccessException e) {
            // This happens if another mod/coremod locked access to the method or modified the class structure
            LOGGER.error("EMCBaubles - Reflection access denied for getArrowStack. " +
                    "This is usually caused by a severe mod conflict or aggressive coremod modifications.");
        }
        catch (Exception e) {
            // Catch-all for unexpected issues like NullPointerExceptions
            LOGGER.error("EMCBaubles - Unexpected error resolving " +
                    "projectile data during EntityJoinWorldEvent.", e);
        }
    }

    // Handle consuming food, drinking potions, and other usable items with durations
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemUseFinish(Finish event) {
        // Make sure it's the player triggering the event and not something else.
        if (!(event.getEntityLiving() instanceof EntityPlayer player)) return;
        if (failsStandardRefillEventChecks(player)) return;

        ItemStack stack = event.getItem();
        if (stack.getCount() != 1) return;
        // If the player holds the bow fire for an hour and the finish event triggers for it (very rare case).
        // Arrow firing already handled by a different event so just exit this event.
        if (stack.getItem() instanceof ItemBow) return;

        ItemStack resultStack = event.getResultStack();
        // The stack the player just finished using is not yet empty.
        if (stack == resultStack && !resultStack.isEmpty()) return;

        ItemStack stackCopy = stack.copy();

        refillStackFromEMCStorage(stackCopy, player);

        EnumHand hand = player.getActiveHand();
        int hotbarSlot = getHotbarSlot(player, hand);

        // checks if there is a result stack (emptied bowl/bucket/bottle)
        if (stack != resultStack && !resultStack.isEmpty()) {
            // try to refill emc storage w/ emc from emptied bowl/bucket/bottle if emc conversion belt equipped
            if (player.getTags().contains("auto_emc_convert")) {
                // attempts to refill emc with result stack & shrink to 0 (IGNORE PARTIAL STACKS)
                refillEMCStorageFromStack(resultStack, player);
                // if result stack now empty, add pre-consumption stack at maximum count EMC can afford to same slot
                if (resultStack.isEmpty()) {
                    cancelEmptyContainerReplacement.put(player, Boolean.TRUE);
                    new PendingAddToSlot(hotbarSlot, stackCopy, player);
                }
                // else drop the pre-consumption stack replacement in a free slot or drop on ground at player
                else new PendingGive(stackCopy, player);
            }
            // if no conversion belt just add pre-consumption stack replacement in a free slot or drop on ground
            else new PendingGive(stackCopy, player);
        }
        // if no result stack just add pre-consumption stack replacement to the exact slot consumed from
        else new PendingAddToSlot(hotbarSlot, stackCopy, player);
    }

    // Handle items consumed instantly by right-clicking like ender pearls
    // & snapshotting inventory state for modded midair block placing w/ tool
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(RightClickItem event) {
        attemptSnapshotOrQueueCheckToReplace(event);
    }


    // Handle regular block placement & snapshotting inventory state for modded block placing w/ tool
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(RightClickBlock event) {
        attemptSnapshotOrQueueCheckToReplace(event);
    }

    // Handle replacing saddles placed on pigs, leads purposefully excluded from this & item pickup emc conversion
    // Does not replace armor placed on armor stand either because that is a slightly different event
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickEntity(EntityInteract event) {
        EntityPlayer player = event.getEntityPlayer();
        if (failsStandardRefillEventChecks(player)) return;

        ItemStack stack = event.getItemStack();

        Item item = stack.getItem();
        if (item.getMaxItemUseDuration(stack) > 0) return;
        if (item instanceof ItemLead || item instanceof ItemBucket) return;

        // If stack is tracked, it's associated resolve restore inventory/check to replace has not run yet
        if (isTrackingStack(stack)) return;

        UUID trackId = UUID.randomUUID();
        beginTrackingStack(stack, trackId);

        ItemStack stackCopy = stack.copy();

        EnumHand hand = event.getHand();
        int hotbarSlot = getHotbarSlot(player, hand);
        new PendingCheckToReplace(hotbarSlot, stackCopy, player, trackId);
    }

    //Handle block replacement for modded block placing w/ tool
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockPlace(PlaceEvent event) {
        EntityPlayer player = event.getPlayer();
        if (failsStandardRefillEventChecks(player)) return;

        ItemStack stack = event.getItemInHand();
        if (stack.getCount() > 1) return;

        Item item = stack.getItem();

        // don't replace buckets or music discs
        if (item instanceof ItemBucket || item instanceof ItemRecord) return;

        // Check if block placed by tool & restore inventory has not already been queued once this tick
        if (!shouldSnapshotInventory(stack, item) || withinSameTickAsLastQueuedRestoreInventory(player)) return;
        new PendingRestoreInventory(player);
    }
}
