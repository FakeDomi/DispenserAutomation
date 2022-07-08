package re.domi.dispenserautomation.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointerImpl;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import re.domi.dispenserautomation.DispenserFireHook;
import re.domi.dispenserautomation.DispenserTicker;

@Mixin(DispenserBlock.class)
public abstract class DispenserBlockMixin extends AbstractBlock
{
    @Inject(method = "onStateReplaced", at = @At("HEAD"))
    protected void dispAuto_onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved, CallbackInfo ci)
    {
        if (!state.isOf(newState.getBlock()) && world.getBlockEntity(pos) instanceof DispenserTicker.TaskHolder dispenser)
        {
            DispenserTicker.Task t = dispenser.dispAuto_get();

            if (t != null)
            {
                t.cancel();
            }
        }
    }

    @Inject(method = "dispense", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/DispenserBlock;getBehaviorForItem(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/block/dispenser/DispenserBehavior;", shift = At.Shift.BY, by = 3), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectDispense(ServerWorld world, BlockPos pos, CallbackInfo ci, BlockPointerImpl b, DispenserBlockEntity d, int slot, ItemStack stack, DispenserBehavior vanillaBehavior)
    {
        DispenserFireHook.injectDispense(world, pos, ci, b, d, slot, stack, vanillaBehavior);
    }

    public DispenserBlockMixin()
    {
        //noinspection ConstantConditions
        super(null);
    }
}
