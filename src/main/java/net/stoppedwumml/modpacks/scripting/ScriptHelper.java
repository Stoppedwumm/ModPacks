package net.stoppedwumml.modpacks.scripting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * The Bridge class between JavaScript and Minecraft.
 * Every method here is accessible in JS via the 'ctx' variable.
 */
public class ScriptHelper {
    private final Level level;
    private final Player player;
    private final Object eventData;

    public ScriptHelper(Level level, Player player, Object eventData) {
        this.level = level;
        this.player = player;
        this.eventData = eventData;
    }

    // --- GETTERS & METADATA ---

    public Player getPlayer() { return player; }
    public Level getLevel() { return level; }

    /**
     * Returns event-specific data.
     * For onBlockBreak/onBlockTick, this is a BlockPos.
     */
    public Object getEventData() { return eventData; }

    // --- PLAYER STATE CHECKS ---

    public boolean isCrouching() {
        return player != null && player.isCrouching();
    }

    public boolean isSprinting() {
        return player != null && player.isSprinting();
    }

    public boolean isInWater() {
        return player != null && player.isInWater();
    }

    public float getHealth() {
        return player != null ? player.getHealth() : 0;
    }

    public double getX() { return player != null ? player.getX() : 0; }
    public double getY() { return player != null ? player.getY() : 0; }
    public double getZ() { return player != null ? player.getZ() : 0; }

    // --- MESSAGING ---

    public void chat(String message) {
        if (player != null) player.sendSystemMessage(Component.literal(message));
    }

    public void actionBar(String message) {
        if (player != null) player.displayClientMessage(Component.literal(message), true);
    }

    public void broadcast(String message) {
        if (!level.isClientSide && level.getServer() != null) {
            level.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message), false);
        }
    }

    // --- PLAYER STATS & EFFECTS ---

    public void heal(float amount) {
        if (player != null) player.heal(amount);
    }

    public void damage(float amount) {
        if (player != null) player.hurt(player.damageSources().magic(), amount);
    }

    public void addFood(int level, float modifier) {
        if (player != null) player.getFoodData().eat(level, modifier);
    }

    public void setOnFire(int seconds) {
        if (player != null) player.igniteForSeconds(seconds);
    }

    /**
     * @param effectId e.g. "minecraft:speed"
     */
    public void addEffect(String effectId, int duration, int amplifier) {
        Optional<Holder.Reference<MobEffect>> holder = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse(effectId));
        if (player != null) {
            holder.ifPresent(h -> player.addEffect(new MobEffectInstance(h, duration, amplifier)));
        }
    }

    public void removeEffect(String effectId) {
        Optional<Holder.Reference<MobEffect>> holder = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse(effectId));
        if (player != null) {
            holder.ifPresent(player::removeEffect);
        }
    }

    public void clearEffects() {
        if (player != null) player.removeAllEffects();
    }

    // --- MOVEMENT ---

    public void teleport(double x, double y, double z) {
        if (player != null) player.teleportTo(x, y, z);
    }

    public void launch(double x, double y, double z) {
        if (player != null) {
            player.setDeltaMovement(new Vec3(x, y, z));
            player.hurtMarked = true; // Syncs to client
        }
    }

    // --- WORLD INTERACTION ---

    public void setBlock(int x, int y, int z, String blockId) {
        if (!level.isClientSide) {
            Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockId));
            level.setBlockAndUpdate(new BlockPos(x, y, z), block.defaultBlockState());
        }
    }

    public String getBlock(int x, int y, int z) {
        return BuiltInRegistries.BLOCK.getKey(level.getBlockState(new BlockPos(x, y, z)).getBlock()).toString();
    }

    public void breakBlock(int x, int y, int z, boolean dropItems) {
        if (!level.isClientSide) {
            level.destroyBlock(new BlockPos(x, y, z), dropItems);
        }
    }

    public void explode(float power, boolean causesFire) {
        if (!level.isClientSide) {
            double ex = player != null ? player.getX() : 0;
            double ey = player != null ? player.getY() : 0;
            double ez = player != null ? player.getZ() : 0;

            // Handle block-based explosions if player is null
            if (player == null && eventData instanceof BlockPos pos) {
                ex = pos.getX(); ey = pos.getY(); ez = pos.getZ();
            }

            level.explode(null, ex, ey, ez, power, causesFire, Level.ExplosionInteraction.MOB);
        }
    }

    // --- ENVIRONMENT & WEATHER ---

    public boolean isRaining() {
        return level.isRaining();
    }

    public boolean canSeeSky(int x, int y, int z) {
        return level.canSeeSky(new BlockPos(x, y, z));
    }

    public boolean isPlayerInRain() {
        if (player == null) return false;
        return level.isRaining() && level.canSeeSky(player.blockPosition());
    }

    public void setTime(long time) {
        if (!level.isClientSide && level.getServer() != null) {
            level.getServer().getLevel(Level.OVERWORLD).setDayTime(time);
        }
    }

    // --- INVENTORY ---

    public void giveItem(String itemId, int count) {
        if (!level.isClientSide && player != null) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId));
            player.getInventory().add(new ItemStack(item, count));
        }
    }

    // --- EFFECTS & SOUNDS ---

    public void playSound(String soundId, float volume, float pitch) {
        double sx = player != null ? player.getX() : 0;
        double sy = player != null ? player.getY() : 0;
        double sz = player != null ? player.getZ() : 0;

        level.playSound(null, sx, sy, sz,
                BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(soundId)),
                SoundSource.PLAYERS, volume, pitch);
    }

    public void spawnParticle(String particleId, double x, double y, double z, int count, double dx, double dy, double dz, double speed) {
        if (level instanceof ServerLevel serverLevel) {
            ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.get(ResourceLocation.parse(particleId));
            if (type instanceof ParticleOptions options) {
                serverLevel.sendParticles(options, x, y, z, count, dx, dy, dz, speed);
            }
        }
    }
}