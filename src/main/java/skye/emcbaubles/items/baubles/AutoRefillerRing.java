package skye.emcbaubles.items.baubles;

import baubles.api.BaubleType;
import baubles.api.IBauble;

import moze_intel.projecte.gameObjs.ObjHandler;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.jspecify.annotations.NonNull;

import skye.emcbaubles.EMCBaubles;

public final class AutoRefillerRing extends Item implements IBauble {
    public String name = "emc_refiller_ring";

    public AutoRefillerRing() {
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
        return BaubleType.RING;
    }

    public void onEquipped(ItemStack itemstack, EntityLivingBase player) {
        player.addTag("emc_refill_items");
    }

    public void onUnequipped(ItemStack itemstack, EntityLivingBase player) {
        if (player.getTags().contains("emc_refill_items")) player.removeTag("emc_refill_items");
    }
}
