package skye.emcbaubles.eventhandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemLead;
import net.minecraft.item.ItemStack;

import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import skye.emcbaubles.Reference;
import skye.emcbaubles.event.PlayerShotArrowPickupEvent;

import static skye.emcbaubles.util.EMCUtils.refillEMCStorageFromStackButRefillPartialStacksFirst;
import static skye.emcbaubles.util.ItemTrackingUtils.stopTrackingStack;
import static skye.emcbaubles.util.MiscUtils.stackIsDamageableOrTool;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class ItemPickupHandler {

    private static boolean failsStandardItemPickupEventChecks(EntityPlayer player) {
        if (player.getEntityWorld().isRemote) return true;
        return !player.getTags().contains("auto_emc_convert");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerPickUpArrow(PlayerShotArrowPickupEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (failsStandardItemPickupEventChecks(player)) return;

        ItemStack arrowStack = event.getArrowStack();
        if (arrowStack.isEmpty()) return;
        if (stackIsDamageableOrTool(arrowStack)) return;

        refillEMCStorageFromStackButRefillPartialStacksFirst(arrowStack, player);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPickUpItem(EntityItemPickupEvent event) {
        ItemStack pickUpStack = event.getItem().getItem();

        // Clean nbt tags from my mod for picked up item (basically only needed for thrown eye of ender)
        stopTrackingStack(pickUpStack);

        if (!(event.getEntityLiving() instanceof EntityPlayer player)) return;
        if (failsStandardItemPickupEventChecks(player)) return;

        if (pickUpStack.isEmpty()) return;
        if (stackIsDamageableOrTool(pickUpStack)) return;
        if (pickUpStack.getItem() instanceof ItemLead) return;
        refillEMCStorageFromStackButRefillPartialStacksFirst(pickUpStack, player);
    }
}
