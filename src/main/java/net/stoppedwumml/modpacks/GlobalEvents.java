package net.stoppedwumml.modpacks;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.stoppedwumml.modpacks.scripting.ScriptHelper;
import net.stoppedwumml.modpacks.scripting.ScriptManager;

@EventBusSubscriber(modid = ModPacks.MODID)
public class GlobalEvents {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide && player.level().getGameTime() % 20 == 0) {
            ScriptHelper ctx = new ScriptHelper(player.level(), player, null);
            ScriptManager.runGlobalFunction("onPlayerTick", ctx);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        ScriptHelper ctx = new ScriptHelper((net.minecraft.world.level.Level) event.getLevel(), event.getPlayer(), event.getPos());
        ScriptManager.runGlobalFunction("onBlockBreak", ctx);
    }
}