package skye.emcbaubles;

import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import skye.emcbaubles.items.ItemList;
import skye.emcbaubles.proxy.IProxy;

@Mod(
    modid = Reference.MOD_ID,
    name = Reference.MOD_NAME,
    version = Reference.VERSION,
    dependencies = "required-after:projecte;required-after:baubles",
    acceptedMinecraftVersions = "[1.12.2]"
)
public final class EMCBaubles {

    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_NAME);

    @SidedProxy(
        modId = Reference.MOD_ID,
        clientSide = "skye.emcbaubles.proxy.ClientProxy",
        serverSide = "skye.emcbaubles.proxy.CommonProxy"
    )
    public static IProxy proxy;

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID)
    public static class RegistrationHandler {

        @SubscribeEvent
        public static void registerItems(RegistryEvent.Register<Item> event) {
            if (!Loader.isModLoaded("projecte")) return;
            if (!Loader.isModLoaded("baubles")) return;

            event.getRegistry().register(ItemList.getEMCConversionBelt());
            event.getRegistry().register(ItemList.getAutoRefillerRing());

            for (int i = 0; i < 3; ++i) {
                event.getRegistry().register(ItemList.getCollectorNecklace(i));
                event.getRegistry().register(ItemList.getPowerFlowerCharm(i));
                event.getRegistry().register(ItemList.getBigPetal(i));
                event.getRegistry().register(ItemList.getSmallPetal(i));
            }
            LOGGER.log(Level.INFO, "EMCBaubles items registered.");
        }

        @SubscribeEvent
        public static void registerModels(ModelRegistryEvent event) {
            if (!Loader.isModLoaded("projecte")) return;
            if (!Loader.isModLoaded("baubles")) return;

            ItemList.getEMCConversionBelt().registerItemModel();
            ItemList.getAutoRefillerRing().registerItemModel();

            for (int i = 0; i < 3; ++i) {
                EMCBaubles.proxy.registerItemRenderer(ItemList.getCollectorNecklace(i));
                EMCBaubles.proxy.registerItemRenderer(ItemList.getPowerFlowerCharm(i));
                EMCBaubles.proxy.registerItemRenderer(ItemList.getBigPetal(i));
                EMCBaubles.proxy.registerItemRenderer(ItemList.getSmallPetal(i));
            }
        }
    }
}
