package skye.emcbaubles.eventhandler;

import moze_intel.projecte.api.item.IItemEmc;
import moze_intel.projecte.config.ProjectEConfig;

import moze_intel.projecte.gameObjs.items.KleinStar;
import moze_intel.projecte.utils.Constants;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import skye.emcbaubles.Reference;
import skye.emcbaubles.items.ItemList;
import skye.emcbaubles.items.baubles.EMCCollectingBauble;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = {Side.CLIENT})
public final class TooltipEventHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void tooltipEvent(ItemTooltipEvent event) {
        if (!(Loader.isModLoaded("projecte") && ProjectEConfig.misc.statToolTips)) return;

        ItemStack current = event.getItemStack();
        Item currentItem = current.getItem();
        String rate = I18n.format("pe.emc.rate");

        // Tweak to base ProjectE: add a max emc storage tooltip to Klein Stars.
        if (currentItem instanceof IItemEmc) {
            String tooltipLabel = I18n.format("item.klein_star_max_storage.tooltip");
            String maxEMCStorage = Constants.EMC_FORMATTER.format(((KleinStar) currentItem).getMaximumEmc(current));

            String formattedMaxEMCTooltip = TextFormatting.DARK_PURPLE + tooltipLabel
                    + TextFormatting.BLUE + " " + maxEMCStorage;
            event.getToolTip().add(formattedMaxEMCTooltip);
        }

        if (currentItem instanceof EMCCollectingBauble) {
            String tooltipLabel = I18n.format("pe.emc.maxgenrate_tooltip");
            String emcRate = Constants.EMC_FORMATTER.format(((EMCCollectingBauble) currentItem).getEMCPerSecond());

            String emcCollectingBaubleDesc
                    = I18n.format("item.emc_collecting_bauble.desc");
            event.getToolTip().add(emcCollectingBaubleDesc);

            String formattedEMCRateTooltip = TextFormatting.DARK_PURPLE + tooltipLabel
                    + TextFormatting.BLUE + " " + emcRate + " " + rate;
            event.getToolTip().add(formattedEMCRateTooltip);
        }

        if (currentItem == ItemList.getEMCConversionBelt()) {
            String conversionBeltDesc
                    = I18n.format("item.emc_conversion_belt.desc");
            event.getToolTip().add(conversionBeltDesc);
        }

        if (currentItem == ItemList.getAutoRefillerRing()) {
            String autoRefillerRingDesc
                    = I18n.format("item.emc_refiller_ring.desc");
            event.getToolTip().add(autoRefillerRingDesc);
        }
    }
}
