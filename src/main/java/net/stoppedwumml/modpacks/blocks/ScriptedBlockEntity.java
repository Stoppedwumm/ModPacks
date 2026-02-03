package net.stoppedwumml.modpacks.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.stoppedwumml.modpacks.RegisterProcess;
import net.stoppedwumml.modpacks.scripting.ScriptHelper;
import net.stoppedwumml.modpacks.scripting.ScriptManager;

public class ScriptedBlockEntity extends BlockEntity {
    public final String scriptId;

    public ScriptedBlockEntity(BlockPos pos, BlockState state, String scriptId) {
        // We reference the static BLOCK_ENTITY_TYPE from RegisterProcess
        super(RegisterProcess.SCRIPTED_BE_TYPE.get(), pos, state);
        this.scriptId = scriptId;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ScriptedBlockEntity be) {
        // Run every 20 ticks (1 second)
        if (level.getGameTime() % 20 == 0) {
            ScriptHelper ctx = new ScriptHelper(level, null, pos);
            ScriptManager.runFunction(be.scriptId, "onBlockTick", ctx);
        }
    }
}