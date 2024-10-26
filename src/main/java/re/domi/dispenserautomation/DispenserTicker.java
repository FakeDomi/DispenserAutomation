package re.domi.dispenserautomation;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public interface DispenserTicker
{
    void dispAuto_add(Task task);
    void dispAuto_tick();

    class Task
    {
        public final int entityNetworkID;

        public final long startTime;
        public final long breakTime;
        public final BlockPos breakPos;

        public final BlockPos dispenserPos;
        public final int selectedSlot;
        public final ItemStack stack;

        private final TaskHolder taskHolder;

        private boolean isCanceled;

        public Task(long startTime, long breakTime, BlockPos breakPos, BlockPos dispenserPos, TaskHolder taskHolder, int slot, ItemStack stack)
        {
            this.entityNetworkID = taskHolder.dispAuto_getNetworkId();
            this.taskHolder = taskHolder;

            this.startTime = startTime;
            this.breakTime = breakTime;
            this.breakPos = breakPos;

            this.dispenserPos = dispenserPos;
            this.selectedSlot = slot;
            this.stack = stack.copy();
        }

        public void cancel()
        {
            this.isCanceled = true;
        }

        public boolean tick(World w)
        {
            ItemStack nowInSlot = this.taskHolder.dispAuto_getStackInSlot(this.selectedSlot);

            if (!this.isCanceled && (nowInSlot == this.stack || ItemStack.areEqual(nowInSlot, this.stack)))
            {
                long delta = w.getTime() - this.startTime;

                if (delta < this.breakTime)
                {
                    int progress = (int)(10.0 * delta / this.breakTime);

                    if (progress > (int)(10.0 * (delta - 1) / this.breakTime))
                    {
                        w.setBlockBreakingInfo(this.entityNetworkID, this.breakPos, progress);
                    }

                    return false;
                }

                BlockState s = w.getBlockState(this.breakPos);
                Block.dropStacks(s, w, this.breakPos, w.getBlockEntity(this.breakPos), null, this.stack);

                if (s.getHardness(w, this.breakPos) > 0)
                {
                    nowInSlot.damage(1, (ServerWorld) w, null, item ->
                        w.playSound(null, this.dispenserPos, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, 0.8F, 0.8F + w.random.nextFloat() * 0.4F));
                }

                w.breakBlock(this.breakPos, false);
            }

            w.setBlockBreakingInfo(this.entityNetworkID, this.breakPos, -1);
            this.taskHolder.dispAuto_set(null);
            return true;
        }

        public NbtCompound serialize(NbtCompound nbt, World world, RegistryWrapper.WrapperLookup registryLookup)
        {
            nbt.putLong("StartTime", this.startTime);
            nbt.putLong("BreakTime", this.breakTime);
            nbt.putLong("WorldTime", world.getTime());
            nbt.put("BreakPos", NbtHelper.fromBlockPos(this.breakPos));
            nbt.put("DispenserPos", NbtHelper.fromBlockPos(this.dispenserPos));
            nbt.putInt("SelectedSlot", this.selectedSlot);
            nbt.put("Stack", this.stack.toNbt(registryLookup));

            return nbt;
        }

        public static void deserializeAndSchedule(NbtCompound nbt, ServerWorld world, TaskHolder taskHolder)
        {
            if (nbt != null && !nbt.isEmpty())
            {
                Optional<BlockPos> breakPos = NbtHelper.toBlockPos(nbt, "BreakPos");
                Optional<BlockPos> dispenserPos = NbtHelper.toBlockPos(nbt, "DispenserPos");
                Optional<ItemStack> stack = ItemStack.fromNbt(world.getRegistryManager(), nbt.getCompound("Stack"));

                if (breakPos.isPresent() && dispenserPos.isPresent() && stack.isPresent())
                {
                    createAndSchedule(world,
                            nbt.getLong("StartTime") + world.getTime() - nbt.getLong("WorldTime"),
                            nbt.getLong("BreakTime"),
                            breakPos.get(),
                            dispenserPos.get(),
                            taskHolder,
                            nbt.getInt("SelectedSlot"),
                            stack.get());
                }
            }
        }

        public static void createAndSchedule(ServerWorld world, long startTime, long breakTime, BlockPos breakPos, BlockPos dispenserPos, TaskHolder taskHolder, int slot, ItemStack stack)
        {
            Task task = new Task(startTime, breakTime, breakPos, dispenserPos, taskHolder, slot, stack);

            ((DispenserTicker)world).dispAuto_add(task);
            taskHolder.dispAuto_set(task);
        }
    }

    interface TaskHolder
    {
        Task dispAuto_get();
        void dispAuto_set(Task task);
        int dispAuto_getNetworkId();
        ItemStack dispAuto_getStackInSlot(int slot);
    }
}
