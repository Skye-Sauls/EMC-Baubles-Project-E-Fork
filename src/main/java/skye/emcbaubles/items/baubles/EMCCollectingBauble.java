package skye.emcbaubles.items.baubles;

import moze_intel.projecte.api.item.IItemEmc;
import moze_intel.projecte.gameObjs.ObjHandler;

import baubles.api.BaubleType;
import baubles.api.IBauble;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import net.minecraft.world.EnumSkyBlock;
import org.jspecify.annotations.NonNull;

import skye.emcbaubles.util.EMCUtils;

public abstract class EMCCollectingBauble extends Item implements IBauble {
    public final int tier;
    public long EMCPerSecond;

    public long getEMCPerSecond() {
        return EMCPerSecond;
    }

    public EMCCollectingBauble(long EMCPerSecond, int tier) {
        this.tier = tier;
        this.EMCPerSecond = EMCPerSecond;
        this.setCreativeTab(ObjHandler.cTab);
        this.setMaxStackSize(1);
    }

    @Override
    public abstract @NonNull BaubleType getBaubleType(@NonNull ItemStack itemstack);

    private long distributeCollectedEMCToEMCStorage(ItemStack candidate, long emcLeftToDistribute) {
        long spaceInItem = EMCUtils.getAvailableEMCSpace(candidate);
        if (spaceInItem <= 0) return 0L;

        Item item = candidate.getItem();
        IItemEmc emcItem = (IItemEmc) item;

        long toAdd = Math.min(emcLeftToDistribute, spaceInItem);
        return emcItem.addEmc(candidate, toAdd);
    }

    public void onWornTick(ItemStack itemstack, EntityLivingBase entityLiving) {
        // Only run on the SERVER side, and only once every 20 ticks (1 second)
        if (entityLiving.world.isRemote || entityLiving.ticksExisted % 20 != 0) return;
        if (!(entityLiving instanceof EntityPlayer player)) return;

        BlockPos pos = new BlockPos(player.posX, player.posY, player.posZ);

        int skyLight = player.world.getLightFor(EnumSkyBlock.SKY, pos);
        int blockLight = player.world.getLightFor(EnumSkyBlock.BLOCK, pos);

        int light = Math.max(skyLight - player.world.getSkylightSubtracted(), blockLight);

        double efficiencyMultiplier = 0.0625D + (0.9375D * ((double) light / 15.0D));
        long emcLeftToDistribute = Math.round((double) this.EMCPerSecond * efficiencyMultiplier);
        if (emcLeftToDistribute <= 0) return;

        for (ItemStack candidate : player.inventory.mainInventory) {
            if (emcLeftToDistribute <= 0) break;
            emcLeftToDistribute -= distributeCollectedEMCToEMCStorage(candidate, emcLeftToDistribute);
        }

        ItemStack candidate = player.inventory.offHandInventory.getFirst();
        if (emcLeftToDistribute <= 0) return;
        distributeCollectedEMCToEMCStorage(candidate, emcLeftToDistribute);
    }
}
