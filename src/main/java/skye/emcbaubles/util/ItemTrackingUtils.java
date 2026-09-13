package skye.emcbaubles.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Objects;
import java.util.UUID;

public final class ItemTrackingUtils {
    private static final String TRACKED_ID_KEY = "trackedId";

    public static void beginTrackingStack(ItemStack stack, UUID trackId) {
        NBTTagCompound nbt = stack.getOrCreateSubCompound("emcbaubles");
        nbt.setUniqueId(TRACKED_ID_KEY, trackId);
    }

    public static boolean isTrackingStack(ItemStack stack) {
        if (!stack.hasTagCompound()) return false;
        NBTTagCompound rootNbt = stack.getTagCompound();

        return rootNbt.hasKey("emcbaubles", 10);
    }

    public static boolean trackIdMatchesStack(ItemStack stack, UUID trackId) {
        NBTTagCompound nbt = stack.getSubCompound("emcbaubles");
        if (nbt == null) return false;

        return Objects.equals(nbt.getUniqueId(TRACKED_ID_KEY), trackId);
    }

    public static void stopTrackingStack(ItemStack stack) {
        if (!stack.hasTagCompound()) return;
        NBTTagCompound rootNbt = stack.getTagCompound();

        if (rootNbt.hasKey("emcbaubles", 10)) rootNbt.removeTag("emcbaubles");
        if (rootNbt.isEmpty()) stack.setTagCompound(null);
    }

    public static void stopTrackingInventory(EntityPlayer player) {
        for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
            ItemStack stackAtSlot = player.inventory.mainInventory.get(i);
            if (stackAtSlot.isEmpty()) continue;

            stopTrackingStack(stackAtSlot);
        }
    }
}
