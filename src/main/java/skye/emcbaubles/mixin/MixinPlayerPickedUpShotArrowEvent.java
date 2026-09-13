package skye.emcbaubles.mixin;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.MinecraftForge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import skye.emcbaubles.event.PlayerShotArrowPickupEvent;

@Mixin(EntityArrow.class)
public abstract class MixinPlayerPickedUpShotArrowEvent {

    @Unique
    private EntityPlayer emcbaubles$collidingPlayer;

    @Inject(method = "onCollideWithPlayer", at = @At("HEAD"))
    private void emcbaubles$captureCollidingPlayer(EntityPlayer entityIn, CallbackInfo ci) {
        this.emcbaubles$collidingPlayer = entityIn;
    }

    @Redirect(
            method = "onCollideWithPlayer",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/InventoryPlayer;addItemStackToInventory(Lnet/minecraft/item/ItemStack;)Z")
    )
    private boolean emcbaubles$onArrowPickup(InventoryPlayer inventoryPlayer, ItemStack itemStackIn) {
        MinecraftForge.EVENT_BUS.post(new PlayerShotArrowPickupEvent(itemStackIn, this.emcbaubles$collidingPlayer));

        if (itemStackIn.isEmpty()) return true;
        return inventoryPlayer.addItemStackToInventory(itemStackIn);
    }
}
