package re.domi.dispenserautomation.mixin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(Entity.class)
public interface EntityMixin
{
    @Accessor("CURRENT_ID")
    static AtomicInteger currentEntityNetId() { return null; }
}
