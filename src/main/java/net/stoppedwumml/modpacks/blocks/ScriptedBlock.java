package net.stoppedwumml.modpacks.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ScriptedBlock extends Block implements EntityBlock {
    private final String scriptId;
    private final boolean ticks;

    public ScriptedBlock(Properties properties, String scriptId, boolean ticks) {
        super(properties);
        this.scriptId = scriptId;
        this.ticks = ticks;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        // We only create a BlockEntity if the block is marked as "ticking" in the JSON
        if (ticks) {
            return new ScriptedBlockEntity(pos, state, scriptId);
        }
        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (ticks && !level.isClientSide) {
            return (lvl, pos, st, be) -> {
                if (be instanceof ScriptedBlockEntity scriptedBe) {
                    ScriptedBlockEntity.tick(lvl, pos, st, scriptedBe);
                }
            };
        }
        return null;
    }
}