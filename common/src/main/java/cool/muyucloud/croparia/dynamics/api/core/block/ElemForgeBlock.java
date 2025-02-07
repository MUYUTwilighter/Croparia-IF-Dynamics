package cool.muyucloud.croparia.dynamics.api.core.block;

import cool.muyucloud.croparia.dynamics.api.core.block.entity.ElemForgeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class ElemForgeBlock extends BaseEntityBlock {
    public static final BooleanProperty RUNNING = BooleanProperty.create("running");

    @NotNull
    private final BlockEntityType<? extends ElemForgeBlockEntity<?>> blockEntityType;

    protected ElemForgeBlock(@NotNull Properties properties, @NotNull BlockEntityType<? extends ElemForgeBlockEntity<?>> blockEntityType) {
        super(properties);
        this.blockEntityType = blockEntityType;
        this.registerDefaultState(this.defaultBlockState().setValue(RUNNING, false));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return blockEntityType.create(blockPos, blockState);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RUNNING);
    }
}
