package net.stoppedwumml.modpacks.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.stoppedwumml.modpacks.scripting.ScriptHelper;
import net.stoppedwumml.modpacks.scripting.ScriptManager;

public class ScriptedItem extends Item {
    private final String scriptId;

    public ScriptedItem(Properties properties, String scriptId) {
        super(properties);
        this.scriptId = scriptId;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND) {
            // FIXED: Added the 3rd argument (null) for eventData
            ScriptHelper helper = new ScriptHelper(level, player, null);
            ScriptManager.runFunction(scriptId, "onRightClick", helper);
        }
        return super.use(level, player, hand);
    }
}