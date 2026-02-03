// Runs every second for every player
function onPlayerTick(ctx) {

    // Use the new helper method we just added
    if (ctx.isPlayerInRain()) {
        ctx.damage(1.0);
        ctx.actionBar("§4The acid rain burns!");
        ctx.spawnParticle("minecraft:smoke", ctx.getX(), ctx.getY() + 1, ctx.getZ(), 2, 0.2, 0.2, 0.2, 0.01);
    }

    // Example of using the other new helpers
    if (ctx.isSprinting()) {
        ctx.spawnParticle("minecraft:cloud", ctx.getX(), ctx.getY(), ctx.getZ(), 1, 0.1, 0.1, 0.1, 0.01);
    }
}

function onBlockBreak(ctx) {
    // getEventData() returns the BlockPos object.
    // In Nashorn, you can call .getX() directly on Java objects
    // passed through the bridge without needing the "net" package prefix.
    var pos = ctx.getEventData();
    if (pos != null) {
        var x = pos.getX();
        var y = pos.getY();
        var z = pos.getZ();

        var blockName = ctx.getBlock(x, y, z);
        if (blockName.indexOf("ore") !== -1) {
            ctx.chat("§6Mining expert boost!");
            ctx.addEffect("minecraft:haste", 200, 1);
        }
    }
}