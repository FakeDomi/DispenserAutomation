package re.domi.dispenserautomation.mixin;

import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import re.domi.dispenserautomation.DispenserTicker;

@Mixin(value = DispenserBlockEntity.class, priority = 1500)
public abstract class DispenserBlockEntityMixin extends LootableContainerBlockEntity implements DispenserTicker.TaskHolder
{
    @Unique
    @SuppressWarnings("ConstantConditions")
    private final int entityNetworkID = EntityMixin.currentEntityNetId().incrementAndGet();

    @Unique
    private DispenserTicker.Task task;

    @Unique
    private NbtCompound serializedTask;

    @Inject(method = "readNbt", at = @At(value = "RETURN"))
    private void readNbt(NbtCompound nbt, CallbackInfo ci)
    {
        this.serializedTask = nbt.getCompound("DispenserAutomationTask");
    }

    @Inject(method = "writeNbt", at = @At(value = "RETURN"))
    private void writeNbt(NbtCompound nbt, CallbackInfo ci)
    {
        if (this.task != null && this.world != null)
        {
            nbt.put("DispenserAutomationTask", this.task.serialize(new NbtCompound(), this.world));
        }
    }

    @Override
    @Unique(silent = true)
    public void setWorld(World world)
    {
        super.setWorld(world);
    }

    @Inject(method = { "setWorld", "method_31662" }, at = @At("RETURN"), remap = false)
    public void dispAuto_setWorld(World world, CallbackInfo ci)
    {
        if (world instanceof ServerWorld serverWorld)
        {
            DispenserTicker.Task.deserializeAndSchedule(this.serializedTask, serverWorld, this);
        }

        this.serializedTask = null;
    }

    @Override
    public DispenserTicker.Task dispAuto_get()
    {
        return this.task;
    }

    @Override
    public void dispAuto_set(DispenserTicker.Task task)
    {
        this.task = task;
    }

    @Override
    public int dispAuto_getNetworkId()
    {
        return this.entityNetworkID;
    }

    @Override
    public ItemStack dispAuto_getStackInSlot(int slot)
    {
        return this.getStack(slot);
    }

    protected DispenserBlockEntityMixin()
    {
        //noinspection ConstantConditions
        super(null, null, null);
    }
}
