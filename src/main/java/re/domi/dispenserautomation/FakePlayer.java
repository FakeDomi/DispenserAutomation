package re.domi.dispenserautomation;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;
import java.util.UUID;

public class FakePlayer extends ServerPlayerEntity
{
    private static final GameProfile fakeGameProfile = new GameProfile(new UUID(1634839249521798066L, 6112050954690944932L), "[DispenserAutomation]");

    public FakePlayer(ServerWorld world, BlockPos pos, Direction facing, ItemStack heldItem)
    {
        super(world.getServer(), world, fakeGameProfile, SyncedClientOptions.createDefault());

        switch (facing)
        {
            case UP -> this.setPitch(-90);
            case DOWN -> this.setPitch(90);
            case NORTH -> {
                this.setYaw(180);
                this.setBodyYaw(180);
                this.setHeadYaw(180);
            }
            case EAST -> {
                this.setYaw(-90);
                this.setBodyYaw(-90);
                this.setHeadYaw(-90);
            }
            case WEST -> {
                this.setYaw(90);
                this.setBodyYaw(90);
                this.setHeadYaw(90);
            }
        }

        this.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        this.setStackInHand(Hand.MAIN_HAND, heldItem);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt)
    {
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt)
    {
    }

    @Override
    public void onSpawn()
    {
    }

    @Override
    public void enterCombat()
    {
    }

    @Override
    public void endCombat()
    {
    }

    @Override
    public void tick()
    {
    }

    @Override
    public void playerTick()
    {
    }

    @Override
    public void onDeath(DamageSource source)
    {
    }

    @Override
    public void updateKilledAdvancementCriterion(Entity killer, int score, DamageSource damageSource)
    {
    }

    @Override
    public boolean damage(DamageSource source, float amount)
    {
        return false;
    }

    @Override
    public void openEditSignScreen(SignBlockEntity sign, boolean front)
    {
    }

    public OptionalInt openHandledScreen(@Nullable NamedScreenHandlerFactory factory)
    {
        return OptionalInt.empty();
    }
}
