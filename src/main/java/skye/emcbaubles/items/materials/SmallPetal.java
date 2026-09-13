package skye.emcbaubles.items.materials;

import moze_intel.projecte.gameObjs.ObjHandler;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public final class SmallPetal extends Item {
    public final int tier;

    public SmallPetal(int tier) {
        this.tier = tier;
        this.setCreativeTab(ObjHandler.cTab);
        this.setTranslationKey("small_petal_mk" + tier);
        this.setRegistryName(new ResourceLocation("emcbaubles", "small_petal_mk" + tier));
    }
}
