package skye.emcbaubles.util;

import moze_intel.projecte.api.item.IItemEmc;
import moze_intel.projecte.utils.EMCHelper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public final class EMCUtils {

    public static IItemEmc getAsEmcStorage(ItemStack candidate) {
        if (candidate == null || candidate.isEmpty()) return null;
        if (candidate.getItem() instanceof IItemEmc) return (IItemEmc) candidate.getItem();

        return null;
    }

    public static long getAvailableEMCSpace(ItemStack candidate) {
        IItemEmc emcItem = getAsEmcStorage(candidate);
        if (emcItem == null) return 0L;

        long spaceInItem = emcItem.getMaximumEmc(candidate) - emcItem.getStoredEmc(candidate);
        if (spaceInItem <= 0) return 0L;
        else return spaceInItem;
    }

    public static long getStoredEMC(ItemStack candidate) {
        IItemEmc emcItem = getAsEmcStorage(candidate);
        if (emcItem == null) return 0L;
        else return emcItem.getStoredEmc(candidate);
    }

    public static long getEMCValueOfSingularItem(ItemStack stackToCheck) {
        ItemStack singleItemToCheck = stackToCheck.copy();
        singleItemToCheck.setCount(1);
        return EMCHelper.getEmcValue(singleItemToCheck);
    }

    public static void reduceStoredEMC(ItemStack emcItem, long emcToRemove) {
        IItemEmc emcStorage = getAsEmcStorage(emcItem);

        // Removes emc from the emcITem
        emcStorage.extractEmc(emcItem, emcToRemove);
    }

    public static class EMCStorage {
        private long totalStoredEMC = 0L;
        private final List<ItemStack> emcStorageItems = new ArrayList<>();

        private EMCStorage() {}

        private void processEMCStorageCandidate(ItemStack emcStorageCandidate) {
            long storedEMC = getStoredEMC(emcStorageCandidate);
            if (storedEMC > 0) {
                this.totalStoredEMC += storedEMC;
                this.emcStorageItems.add(emcStorageCandidate);
            }
        }
    }

    public static EMCStorage getEMCStorage(EntityPlayer player) {
        EMCStorage emcStorage = new EMCStorage();

        for (ItemStack emcStorageCandidate : player.inventory.mainInventory) {
            emcStorage.processEMCStorageCandidate(emcStorageCandidate);
        }

        ItemStack emcStorageCandidate = player.inventory.offHandInventory.getFirst();
        emcStorage.processEMCStorageCandidate(emcStorageCandidate);

        return emcStorage;
    }

    public static void reduceEMCStorage(List<ItemStack> emcStorageItems, long emcToRemove) {
        for (ItemStack emcStorageItem : emcStorageItems) {
            if (emcToRemove <= 0) break;

            long storedEMC = getStoredEMC(emcStorageItem);
            long emcCost = Math.min(emcToRemove, storedEMC);
            reduceStoredEMC(emcStorageItem, emcCost);
            emcToRemove -= emcCost;
        }
    }

    public static boolean refillStackFromEMCStorage(ItemStack stack, EntityPlayer player) {
        long emcValueOfSingleItem = getEMCValueOfSingularItem(stack);
        if (emcValueOfSingleItem <= 0) return false;

        int stackMaxCount = stack.getMaxStackSize();

        EMCStorage emcStorage = getEMCStorage(player);

        int itemsToRefill = (int) Math.min(emcStorage.totalStoredEMC / emcValueOfSingleItem, stackMaxCount);
        if (itemsToRefill == 0) return false;

        long totalEMCCost = emcValueOfSingleItem * itemsToRefill;
        reduceEMCStorage(emcStorage.emcStorageItems, totalEMCCost);

        stack.setCount(itemsToRefill);

        // don't inherit the old tracking tag
        if (stack.hasTagCompound() && stack.getTagCompound() != null) {
            stack.getTagCompound().removeTag("emcbaubles");
            if (!stack.hasTagCompound()) stack.setTagCompound(null);
        }
        return true;
    }

    public static void refillDepletedHeldStackFromEMCStorage(int inventorySlot, ItemStack stack, EntityPlayer player) {
        boolean successful = refillStackFromEMCStorage(stack, player);
        if (!successful) return;

        if (inventorySlot == -1) player.inventory.offHandInventory.set(0, stack);
        else ItemHandlerHelper.giveItemToPlayer(player, stack, inventorySlot);
    }

    public static void refillDepletedHeldStackFromEMCStorage(ItemStack stack, EntityPlayer player) {
        boolean successful = refillStackFromEMCStorage(stack, player);
        if (!successful) return;

        ItemHandlerHelper.giveItemToPlayer(player, stack);
    }

    private static void fillPartialInventoryStack(ItemStack inventoryStack, ItemStack stackToCheck) {
        int space = inventoryStack.getMaxStackSize() - inventoryStack.getCount();
        if (space <= 0) return;

        int amountToMove = Math.min(stackToCheck.getCount(), space);
        inventoryStack.grow(amountToMove);
        stackToCheck.shrink(amountToMove);

    }

    private static void fillPartialInventoryStacksFirst(ItemStack stackToCheck, EntityPlayer player) {
        for (ItemStack inventoryStack : player.inventory.mainInventory) {
            if (inventoryStack.isEmpty()) continue;
            if (!ItemStack.areItemsEqual(inventoryStack, stackToCheck)) continue;

            fillPartialInventoryStack(inventoryStack, stackToCheck);

            if (stackToCheck.isEmpty()) return;
        }

        ItemStack inventoryStack = player.getHeldItemOffhand();
        if (inventoryStack.isEmpty()) return;
        if (!ItemStack.areItemsEqual(inventoryStack, stackToCheck)) return;

        fillPartialInventoryStack(inventoryStack, stackToCheck);
    }

    private static void refillEMCStorageCandidate(ItemStack stack, ItemStack candidate, long emcValueOfSingleItem) {
        int stackCount = stack.getCount();

        long spaceInItem = getAvailableEMCSpace(candidate);
        if (spaceInItem <= 0) return;

        int itemCountToAdd = (int) (spaceInItem / emcValueOfSingleItem);
        if (itemCountToAdd <= 0) return;

        int maxItemsToConsume = Math.min(stackCount, itemCountToAdd);

        Item item = candidate.getItem();
        IItemEmc emcItem = (IItemEmc) item;

        long emcToAdd = maxItemsToConsume * emcValueOfSingleItem;
        emcItem.addEmc(candidate, emcToAdd);
        stack.shrink(maxItemsToConsume);
    }

    public static void refillEMCStorageFromStackButRefillPartialStacksFirst
            (ItemStack stackToCheck, EntityPlayer player) {
        fillPartialInventoryStacksFirst(stackToCheck, player);
        if(stackToCheck.isEmpty()) return;

        refillEMCStorageFromStack(stackToCheck, player);
    }

    public static void refillEMCStorageFromStack(ItemStack stackToCheck, EntityPlayer player) {
        long emcValueOfSingleItem = getEMCValueOfSingularItem(stackToCheck);
        if (emcValueOfSingleItem <= 0) return;

        for (ItemStack emcStorageCandidate : player.inventory.mainInventory) {
            refillEMCStorageCandidate(stackToCheck, emcStorageCandidate, emcValueOfSingleItem);
        }

        ItemStack emcStorageCandidate = player.inventory.offHandInventory.getFirst();
        refillEMCStorageCandidate(stackToCheck, emcStorageCandidate, emcValueOfSingleItem);
    }
}
