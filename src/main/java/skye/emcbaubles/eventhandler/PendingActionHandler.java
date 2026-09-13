package skye.emcbaubles.eventhandler;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

import skye.emcbaubles.Reference;
import skye.emcbaubles.queue.PendingActions.*;
import skye.emcbaubles.queue.InventorySnapshots.InventorySnapshot;

import static skye.emcbaubles.queue.PendingActions.PENDING_ACTIONS;
import static skye.emcbaubles.queue.InventorySnapshots.*;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public final class PendingActionHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!PENDING_ACTIONS.isEmpty()) {
            Iterator<PendingAction> actionIterator = PENDING_ACTIONS.iterator();
            while (actionIterator.hasNext()) {
                PendingAction deferredPendingAction = actionIterator.next();
                if (deferredPendingAction.getTicksThenDecrement() > 0) continue;
                actionIterator.remove();
                deferredPendingAction.resolve();
            }
        }

        for (InventorySnapshot snapshot : INVENTORY_SNAPSHOTS.values())
            if (snapshot.getTicksThenDecrement() < 0) snapshot.removeSnapshot();
    }
}