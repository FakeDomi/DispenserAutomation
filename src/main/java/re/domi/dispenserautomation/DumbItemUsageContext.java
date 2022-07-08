package re.domi.dispenserautomation;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DumbItemUsageContext extends ItemUsageContext
{
    public DumbItemUsageContext(World world, @Nullable PlayerEntity player, Hand hand, ItemStack stack, BlockHitResult hit)
    {
        super(world, player, hand, stack, hit);
    }
}
