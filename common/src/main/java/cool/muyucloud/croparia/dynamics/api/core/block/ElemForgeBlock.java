package cool.muyucloud.croparia.dynamics.api.core.block;

import cool.muyucloud.croparia.dynamics.api.core.block.entity.ElemForgeBlockEntity;
import cool.muyucloud.croparia.dynamics.api.core.item.ForgeUpgrade;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused", "deprecation"})
public class ElemForgeBlock extends BaseEntityBlock {
    public static final BooleanProperty RUNNING = BooleanProperty.create("running");
    public static final IntegerProperty TIER = IntegerProperty.create("tier", 0, 3);

    @NotNull
    private final BlockEntityType<? extends ElemForgeBlockEntity<?>> blockEntityType;

    protected ElemForgeBlock(@NotNull Properties properties, @NotNull BlockEntityType<? extends ElemForgeBlockEntity<?>> blockEntityType) {
        super(properties);
        this.blockEntityType = blockEntityType;
        this.registerDefaultState(this.defaultBlockState().setValue(RUNNING, false));
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
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return blockEntityType.create(blockPos, blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RUNNING);
    }
}
