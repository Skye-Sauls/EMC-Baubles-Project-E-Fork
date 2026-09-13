package skye.emcbaubles.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import net.minecraftforge.event.entity.player.PlayerEvent;

public final class PlayerShotArrowPickupEvent extends PlayerEvent {

    private final ItemStack arrowStack;

    public PlayerShotArrowPickupEvent(ItemStack arrowStack, EntityPlayer player) {
        super(player);
        this.arrowStack = arrowStack;
    }

    public ItemStack getArrowStack() {
        return arrowStack;
    }
}
