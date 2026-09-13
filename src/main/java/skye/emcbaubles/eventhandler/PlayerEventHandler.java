package skye.emcbaubles.eventhandler;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;

import skye.emcbaubles.Reference;
import skye.emcbaubles.items.baubles.AutoRefillerRing;
import skye.emcbaubles.items.baubles.EMCConversionBelt;

import static skye.emcbaubles.util.ItemTrackingUtils.stopTrackingInventory;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class PlayerEventHandler {

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer player)) return;
        if (player.world.isRemote) return;

        stopTrackingInventory(player);
    }

    @SubscribeEvent
    public static void onLogin(PlayerLoggedInEvent event) {
        stopTrackingInventory(event.player);
        reapplyTag(event.player);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerRespawnEvent event) { reapplyTag(event.player); }

    @SubscribeEvent
    public static void onDimensionChange(PlayerChangedDimensionEvent event) { reapplyTag(event.player); }

    private static void reapplyTag(EntityPlayer player) {
        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
        for (int i = 0; i < baubles.getSlots(); i++) {
            ItemStack stack = baubles.getStackInSlot(i);

            if (!stack.isEmpty() && stack.getItem() instanceof EMCConversionBelt) {
                if (!player.getTags().contains("auto_emc_convert")) player.addTag("auto_emc_convert");
                continue;
            }

            if (!stack.isEmpty() && stack.getItem() instanceof AutoRefillerRing) {
                if (!player.getTags().contains("emc_refill_items")) player.addTag("emc_refill_items");
                // If more bauble checks are added uncomment.
                //continue;
            }
        }
    }
}