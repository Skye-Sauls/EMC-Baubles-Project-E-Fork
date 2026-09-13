package skye.emcbaubles.items.materials;

import moze_intel.projecte.gameObjs.ObjHandler;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public final class BigPetal extends Item {
    public final int tier;

    public BigPetal(int tier) {
        this.tier = tier;
        this.setCreativeTab(ObjHandler.cTab);
        this.setTranslationKey("big_petal_mk" + tier);
        this.setRegistryName(new ResourceLocation("emcbaubles", "big_petal_mk" + tier));
    }
}
