package skye.emcbaubles.proxy;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class ClientProxy implements IProxy {

    public void registerItemRenderer(Item item, int meta, String name) {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation("emcbaubles:" + name, "inventory"));
    }

    public void registerItemRenderer(Item item) {
        String name = ForgeRegistries.ITEMS.getKey(item).toString();
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(name, "inventory"));
    }
}
