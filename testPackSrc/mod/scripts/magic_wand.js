function onRightClick(ctx) {
    if (ctx.isCrouching()) {
        // Mode: Flight
        ctx.actionBar("§bLaunching...");
        ctx.launch(0, 1.5, 0); // Throw player into the air
        ctx.spawnParticle("minecraft:cloud", ctx.getX(), ctx.getY(), ctx.getZ(), 20, 0.5, 0.1, 0.5, 0.1);
        ctx.playSound("minecraft:entity.firework_rocket.launch", 1.0, 1.2);
    } else {
        // Mode: Chaos
        ctx.actionBar("§cBOOM!");
        ctx.explode(3.0, true); // 3.0 power, causes fire
        ctx.addEffect("minecraft:resistance", 100, 4); // Shield player from their own blast
    }
}