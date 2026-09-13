package skye.emcbaubles.items.baubles;

import baubles.api.BaubleType;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.jspecify.annotations.NonNull;

public final class CollectorNecklace extends EMCCollectingBauble {

    public CollectorNecklace(long EMCPerSecond, int tier) {
        super(EMCPerSecond, tier);
        this.setTranslationKey("collector_mk" + tier + "_necklace");
        this.setRegistryName(new ResourceLocation("emcbaubles", "collector_mk" + tier + "_necklace"));
    }

    @Override
    public @NonNull BaubleType getBaubleType(@NonNull ItemStack itemstack) {
        return BaubleType.AMULET;
    }
}
