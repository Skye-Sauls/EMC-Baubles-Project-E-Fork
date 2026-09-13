package skye.emcbaubles.items.baubles;

import baubles.api.BaubleType;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.jspecify.annotations.NonNull;

public final class PowerFlowerCharm extends EMCCollectingBauble {

    public PowerFlowerCharm(long EMCPerSecond, int tier) {
        super(EMCPerSecond, tier);
        this.setTranslationKey("flower_mk" + tier + "_charm");
        this.setRegistryName(new ResourceLocation("emcbaubles", "flower_mk" + tier + "_charm"));
    }

    @Override
    public @NonNull BaubleType getBaubleType(@NonNull ItemStack itemstack) {
        return BaubleType.CHARM;
    }
}
