package re.domi.dispenserautomation;

import net.fabricmc.fabric.api.tool.attribute.v1.DynamicAttributeTool;
import net.fabricmc.fabric.api.tool.attribute.v1.ToolManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ShearsItem;
import net.minecraft.item.ToolItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public abstract class Behavior
{
    private static final List<Behavior> behaviors = new ArrayList<>();

    public static Behavior get(ItemStack stack, boolean stoneCutter)
    {
        return behaviors.stream().filter(x -> x.predicate.test(stack, stoneCutter)).findFirst().orElse(null);
    }

    private static Vec3d getHitCoords(BlockPos p, Direction offsetDirection)
    {
        return switch (offsetDirection)
            {
                case UP -> new Vec3d(p.getX() + 0.5, p.getY() + 1, p.getZ() + 0.5);
                case DOWN -> new Vec3d(p.getX() + 0.5, p.getY(), p.getZ() + 0.5);
                case NORTH -> new Vec3d(p.getX() + 0.5, p.getY() + 0.5, p.getZ());
                case EAST -> new Vec3d(p.getX() + 1, p.getY() + 0.5, p.getZ() + 0.5);
                case SOUTH -> new Vec3d(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 1);
                case WEST -> new Vec3d(p.getX(), p.getY() + 0.5, p.getZ() + 0.5);
            };
    }

    static
    {
        behaviors.add(new Behavior((stack, stoneCutter) -> stoneCutter
            && (stack.getItem() instanceof ToolItem || stack.getItem() instanceof ShearsItem || stack.getItem() instanceof DynamicAttributeTool)
            && (stack.getDamage() < stack.getMaxDamage() - 1 || EnchantmentHelper.getLevel(Enchantments.MENDING, stack) == 0))
        {
            @Override
            public boolean run(ServerWorld w, BlockPos actionPos, BlockPos dispenserPos, Direction dispenserFacing, DispenserBlockEntity dispenser, int slot, ItemStack stack)
            {
                DispenserTicker.TaskHolder taskHolder = (DispenserTicker.TaskHolder)dispenser;

                if (taskHolder.dispAuto_get() != null)
                {
                    return false;
                }

                BlockState s = w.getBlockState(actionPos);
                float hardness = s.getHardness(w, actionPos);

                if (hardness < 0 || (s.isToolRequired() && !ToolManager.handleIsEffectiveOn(s, stack, null)))
                {
                    return false;
                }

                float breakingSpeed = ToolManager.handleBreakingSpeed(s, stack, null);
                int efficiencyLevel = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);

                if (breakingSpeed > 0)
                {
                    if (breakingSpeed > 1 && efficiencyLevel > 0)
                    {
                        breakingSpeed += (efficiencyLevel * efficiencyLevel + 1);
                    }

                    long breakTime = Math.max((int)Math.ceil(30 * hardness / breakingSpeed), 1);

                    DispenserTicker.Task.createAndSchedule(w, w.getTime(), breakTime, actionPos, dispenserPos, taskHolder, slot, stack);
                }

                return true;
            }
        });

        behaviors.add(new Behavior((stack, stoneCutter) -> true)
        {
            @Override
            public boolean run(ServerWorld world, BlockPos actionPos, BlockPos dispenserPos, Direction dispenserFacing, DispenserBlockEntity dispenser, int slot, ItemStack stack)
            {
                BlockState state = world.getBlockState(actionPos);
                BlockHitResult hitResult = new BlockHitResult(Behavior.getHitCoords(actionPos, dispenserFacing), dispenserFacing.getOpposite(), actionPos, false);
                FakePlayer player = new FakePlayer(world, actionPos, dispenserFacing, stack);
                ItemUsageContext ctx = new DumbItemUsageContext(world, player, Hand.MAIN_HAND, stack, hitResult);

                if (stack.getItem().useOnBlock(ctx).isAccepted() ||
                    state.onUse(world, player, Hand.MAIN_HAND, hitResult).isAccepted())
                {
                    dispenser.setStack(slot, player.getStackInHand(Hand.MAIN_HAND));
                    player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                    player.getInventory().dropAll();

                    return true;
                }

                return false;
            }
        });
    }

    public BiPredicate<ItemStack, Boolean> predicate;

    public Behavior(BiPredicate<ItemStack, Boolean> p)
    {
        this.predicate = p;
    }

    public abstract boolean run(ServerWorld w, BlockPos actionPos, BlockPos dispenserPos, Direction dispenserFacing, DispenserBlockEntity dispenser, int slot, ItemStack stack);
}
