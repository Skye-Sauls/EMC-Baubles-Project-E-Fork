package skye.emcbaubles.util;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class MiscUtils {

    public static boolean stackIsDamageableOrTool(ItemStack stackToCheck) {
        return stackToCheck.getMaxStackSize() == 1 || stackToCheck.getItem().isDamageable();
    }

    public static boolean isEmptyContainer(ItemStack stack) {
        if (stack.isEmpty()) return false;

        Item item = stack.getItem();
        return item == Items.BOWL || item == Items.GLASS_BOTTLE || item == Items.BUCKET;
    }
}
