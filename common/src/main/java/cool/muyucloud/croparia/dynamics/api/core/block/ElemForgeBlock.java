package cool.muyucloud.croparia.dynamics.api.core.block;

import cool.muyucloud.croparia.dynamics.api.core.block.entity.ElemForgeBlockEntity;
import cool.muyucloud.croparia.dynamics.api.core.item.ForgeUpgrade;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@SuppressWarnings({"unused", "deprecation"})
public class ElemForgeBlock extends BaseEntityBlock {
    public static final BooleanProperty RUNNING = BooleanProperty.create("running");
    public static final IntegerProperty TIER = IntegerProperty.create("tier", 0, 3);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    @NotNull
    private final Supplier<BlockEntityType<? extends ElemForgeBlockEntity<?>>> blockEntityType;

    public ElemForgeBlock(@NotNull Properties properties, @NotNull Supplier<BlockEntityType<? extends ElemForgeBlockEntity<?>>> blockEntityType) {
        super(properties);
        this.blockEntityType = blockEntityType;
        this.registerDefaultState(this.defaultBlockState().setValue(RUNNING, false).setValue(TIER, 0).setValue(FACING, Direction.NORTH));
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @Nullable BlockHitResult blockHitResult) {
        if (level.isClientSide) return InteractionResult.PASS;
        ItemStack stack = player.getItemInHand(hand);
        Item item = stack.getItem();
        if (item instanceof ForgeUpgrade upgrade) {
            int tier = state.getValue(TIER);
            if (tier >= upgrade.getTier()) {
                return InteractionResult.PASS;
            } else {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof ElemForgeBlockEntity<?> forge) {
                    int result = forge.tryUpgrade(upgrade.getTier());
                    if (result == 0) {
                        return InteractionResult.PASS;
                    } else {
                        state.setValue(TIER, upgrade.getTier());
                        stack.shrink(1);
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos blockPos, BlockState future, boolean bl) {
        super.onRemove(state, level, blockPos, future, bl);
        if (state.getBlock() != future.getBlock()) {
            BlockEntity be = level.getBlockEntity(blockPos);
            if (be instanceof ElemForgeBlockEntity<?> forge) {
                forge.onRemove();
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return blockEntityType.get().create(blockPos, blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(RUNNING);
        builder.add(TIER);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type) {
        return createTickerHelper(
            type, this.blockEntityType.get(),
            (world, pos, state, be) -> {
                if (!world.isClientSide) {
                    be.tick(world.getServer(), state);
                }
            }
        );
    }
}
