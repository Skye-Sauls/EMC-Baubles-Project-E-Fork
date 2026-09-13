package skye.emcbaubles.proxy;

import net.minecraft.item.Item;

public interface IProxy {
    void registerItemRenderer(Item item, int meta, String name);
    void registerItemRenderer(Item item);
}
