package re.domi.dispenserautomation;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

public class DispenserAutomation implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        ServerTickEvents.START_WORLD_TICK.register(w -> ((DispenserTicker)w).dispAuto_tick());
    }

    public static int getEnchantmentLevel(World world, ItemStack stack, RegistryKey<Enchantment> enchantment)
    {
        return world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getEntry(enchantment.getValue()).map(e -> EnchantmentHelper.getLevel(e, stack)).orElse(0);
    }
}
