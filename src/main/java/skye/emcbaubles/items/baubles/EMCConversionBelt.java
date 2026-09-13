package skye.emcbaubles.items.baubles;

import moze_intel.projecte.gameObjs.ObjHandler;

import baubles.api.BaubleType;
import baubles.api.IBauble;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;

import org.jspecify.annotations.NonNull;

import skye.emcbaubles.EMCBaubles;

public final class EMCConversionBelt extends Item implements IBauble {
    public String name = "emc_conversion_belt";

    public EMCConversionBelt() {
        this.setTranslationKey(this.name);
        this.setRegistryName(this.name);
        this.setCreativeTab(ObjHandler.cTab);
        this.setMaxStackSize(1);
    }

    public void registerItemModel() {
        EMCBaubles.proxy.registerItemRenderer(this, 0, this.name);
    }

    @Override
    public @NonNull BaubleType getBaubleType(@NonNull ItemStack itemstack) {
        return BaubleType.BELT;
    }

    public void onEquipped(ItemStack itemstack, EntityLivingBase player) {
        player.addTag("auto_emc_convert");
    }

    public void onUnequipped(ItemStack itemstack, EntityLivingBase player) {
        if (player.getTags().contains("auto_emc_convert")) {
            player.removeTag("auto_emc_convert");
        }
    }
}
