package skye.emcbaubles.mixin;

import invtweaks.InvTweaksHandlerAutoRefill;
import invtweaks.InvTweaksObfuscation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static skye.emcbaubles.eventhandler.ItemRefillHandler.cancelEmptyContainerReplacement;
import static skye.emcbaubles.util.MiscUtils.isEmptyContainer;

@Mixin(InvTweaksHandlerAutoRefill.class)
public class MixinCancelInvTweaksAutoReplaceForEmptyContainer {

    @Inject(
            method = "autoRefillSlot",
            at = @At(value = "INVOKE",
                    target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;)V"),
            cancellable = true
            /*
            method = "autoRefillSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Linvtweaks/InvTweaks;addScheduledTask(Ljava/lang/Runnable;)V"
            ),
            cancellable = true
             */
    )
    private void emcbaubles$cancelInventorySlotRefill(int slot, String wantedId, int wantedDamage, CallbackInfo ci,
                                                      @Local(ordinal = 1) ItemStack replacementStack) {
        EntityPlayer player = ((InvTweaksObfuscation) (Object) this).mc.player;

        if (cancelEmptyContainerReplacement.getOrDefault(player, false)) {
            cancelEmptyContainerReplacement.remove(player);
            if (isEmptyContainer(replacementStack)) ci.cancel();
        }
    }
}