package re.domi.dispenserautomation;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.util.math.Direction.*;

public class DispenserFireHook
{
    public static void injectDispense(ServerWorld world, BlockPos pos, CallbackInfo ci, DispenserBlockEntity d, int slot, ItemStack stack)
    {
        boolean nextToStoneCutter = isNextToBlock(world, pos, Blocks.STONECUTTER);

        if (nextToStoneCutter || isNextToBlock(world, pos, Blocks.SMITHING_TABLE))
        {
            Behavior behavior = Behavior.get(stack, nextToStoneCutter);

            if (behavior != null)
            {
                Direction dispenserFacing = world.getBlockState(pos).get(DispenserBlock.FACING);

                if (!behavior.run(world, pos.offset(dispenserFacing), pos, dispenserFacing, d, slot, stack))
                {
                    world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0f, 1.2f);
                }
            }

            ci.cancel();
        }
    }

    public static boolean isNextToBlock(ServerWorld world, BlockPos pos, Block block)
    {
        return world.getBlockState(pos.offset(UP)).getBlock().equals(block) ||
            world.getBlockState(pos.offset(DOWN)).getBlock().equals(block) ||
            world.getBlockState(pos.offset(NORTH)).getBlock().equals(block) ||
            world.getBlockState(pos.offset(EAST)).getBlock().equals(block) ||
            world.getBlockState(pos.offset(SOUTH)).getBlock().equals(block) ||
            world.getBlockState(pos.offset(WEST)).getBlock().equals(block);
    }
}
