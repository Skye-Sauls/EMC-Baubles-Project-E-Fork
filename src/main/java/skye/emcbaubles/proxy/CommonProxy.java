package skye.emcbaubles.proxy;

import net.minecraft.item.Item;

public class CommonProxy implements IProxy {

    @Override
    public void registerItemRenderer(Item item, int meta, String name) {
        // Left blank intentionally for server side safety
    }

    @Override
    public void registerItemRenderer(Item item) {
        // Left blank intentionally for server side safety
    }
}
