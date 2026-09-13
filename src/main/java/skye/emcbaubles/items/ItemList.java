package skye.emcbaubles.items;

import skye.emcbaubles.items.baubles.AutoRefillerRing;
import skye.emcbaubles.items.baubles.CollectorNecklace;
import skye.emcbaubles.items.baubles.EMCConversionBelt;
import skye.emcbaubles.items.baubles.PowerFlowerCharm;
import skye.emcbaubles.items.materials.BigPetal;
import skye.emcbaubles.items.materials.SmallPetal;

import net.minecraft.item.Item;

public final class ItemList {
    public static CollectorNecklace[] collector_necklaces;
    public static PowerFlowerCharm[] flower_charms;
    public static BigPetal[] big_petals;
    public static SmallPetal[] small_petals;
    public static EMCConversionBelt emc_conversion_belt;
    public static AutoRefillerRing auto_refiller_ring;
    private static boolean isInitialized = false;

    public static void init() {
        collector_necklaces = new CollectorNecklace[3];
        flower_charms = new PowerFlowerCharm[3];
        big_petals = new BigPetal[3];
        small_petals = new SmallPetal[3];

        collector_necklaces[0] = new CollectorNecklace(4L, 1);
        collector_necklaces[1] = new CollectorNecklace(12L, 2);
        collector_necklaces[2] = new CollectorNecklace(40L, 3);

        flower_charms[0] = new PowerFlowerCharm(73L, 1);
        flower_charms[1] = new PowerFlowerCharm(219L, 2);
        flower_charms[2] = new PowerFlowerCharm(730L, 3);

        big_petals[0] = new BigPetal(1);
        big_petals[1] = new BigPetal(2);
        big_petals[2] = new BigPetal(3);

        small_petals[0] = new SmallPetal(1);
        small_petals[1] = new SmallPetal(2);
        small_petals[2] = new SmallPetal(3);

        emc_conversion_belt = new EMCConversionBelt();
        auto_refiller_ring = new AutoRefillerRing();

        isInitialized = true;
    }

    public static EMCConversionBelt getEMCConversionBelt() {
        if (!isInitialized) init();

        return emc_conversion_belt;
    }

    public static AutoRefillerRing getAutoRefillerRing() {
        if (!isInitialized) init();

        return auto_refiller_ring;
    }

    public static CollectorNecklace getCollectorNecklace(int i) {
        if (!isInitialized) init();

        return i >= 0 && i <= collector_necklaces.length ? collector_necklaces[i] : null;
    }

    public static Item getPowerFlowerCharm(int i) {
        if (!isInitialized) init();

        return i >= 0 && i <= flower_charms.length ? flower_charms[i] : null;
    }

    public static Item getBigPetal(int i) {
        if (!isInitialized) init();

        return i >= 0 && i <= big_petals.length ? big_petals[i] : null;
    }

    public static Item getSmallPetal(int i) {
        if (!isInitialized) init();

        return i >= 0 && i <= small_petals.length ? small_petals[i] : null;
    }
}
