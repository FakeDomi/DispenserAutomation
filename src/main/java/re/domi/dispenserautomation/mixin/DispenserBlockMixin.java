package re.domi.dispenserautomation.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
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
    protected void cancelOngoingTask(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved, CallbackInfo ci)
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

    @Inject(method = "dispense", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/block/DispenserBlock;getBehaviorForItem(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/block/dispenser/DispenserBehavior;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void handleCustomBehavior(ServerWorld world, BlockState state, BlockPos pos, CallbackInfo ci, DispenserBlockEntity d, BlockPointer bp, int slot, ItemStack stack)
    {
        DispenserFireHook.injectDispense(world, pos, ci, d, slot, stack);
    }

    public DispenserBlockMixin()
    {
        //noinspection ConstantConditions
        super(null);
    }
}
