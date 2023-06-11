package re.domi.dispenserautomation.mixin;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import re.domi.dispenserautomation.DispenserTicker;

import java.util.ArrayList;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements DispenserTicker
{
    @Unique
    private final ArrayList<Task> tasks = new ArrayList<>(10);

    @Override
    public void dispAuto_add(Task task)
    {
        this.tasks.add(task);
    }

    @Override
    public void dispAuto_tick()
    {
        this.tasks.removeIf(current -> current.tick(this));
    }

    protected ServerWorldMixin()
    {
        //noinspection ConstantConditions
        super(null, null, null, null, null, false, false, 0, 0);
    }
}
