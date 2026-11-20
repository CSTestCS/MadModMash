package com.madmodmash.system;

import com.madmodmash.registry.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Elemental traits ("___Traits.txt" / "Attributes.txt" / the Abilities list).
 * A Trait is carried by the twelve Mystery Mineral families and by mutated Champions,
 * and can be infused into any weapon (right-click a core onto the weapon in your inventory).
 */
public enum Trait {
    VOID("Void", ChatFormatting.DARK_PURPLE), AIR("Air", ChatFormatting.WHITE), FIRE("Fire", ChatFormatting.GOLD),
    EARTH("Earth", ChatFormatting.DARK_GREEN), POISON("Poison", ChatFormatting.GREEN), STONE("Stone", ChatFormatting.GRAY),
    ICE("Ice", ChatFormatting.AQUA), MIGHT("Might", ChatFormatting.DARK_GRAY), LIGHTNING("Lightning", ChatFormatting.YELLOW),
    LIFE("Life", ChatFormatting.RED), SHADOW("Shadow", ChatFormatting.BLACK), LIGHT("Light", ChatFormatting.YELLOW);

    public final String title;
    public final ChatFormatting color;

    Trait(String title, ChatFormatting color) {
        this.title = title;
        this.color = color;
    }

    public static final String KEY = "mm_trait", LEVEL = "mm_trait_level";

    public static Trait of(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        if (cd == null) return null;
        String s = cd.copyTag().getStringOr(KEY, "");
        if (s.isEmpty()) return null;
        try { return valueOf(s); } catch (IllegalArgumentException e) { return null; }
    }

    public static int level(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd == null ? 0 : cd.copyTag().getIntOr(LEVEL, 0);
    }

    public static void infuse(ItemStack stack, Trait trait, int level) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putString(KEY, trait.name());
            tag.putInt(LEVEL, level);
        });
    }

    /** Extra damage dealt by this trait (applied in the damage event). */
    public float bonusDamage(LivingEntity target, int lvl, float damage) {
        return switch (this) {
            case LIGHT -> target.is(EntityTypeTags.UNDEAD) ? 2.0F * lvl : 0;
            case MIGHT -> target.getArmorValue() > 6 ? damage * 0.15F * lvl : damage * 0.05F * lvl;
            case STONE -> target.is(com.madmodmash.util.MashTags.CONSTRUCTS) ? 2.5F * lvl : 0;
            default -> 0;
        };
    }

    public void onHit(ServerLevel level, LivingEntity attacker, LivingEntity target, int lvl, float damage) {
        var rand = attacker.getRandom();
        switch (this) {
            case VOID -> {
                target.addEffect(new MobEffectInstance(ModEffects.VOID_FIRE, 40 * lvl, 0), attacker);
                if (rand.nextFloat() < 0.15F * lvl)
                    target.randomTeleport(target.getX() + rand.nextInt(9) - 4, target.getY(), target.getZ() + rand.nextInt(9) - 4, true, (BlockState s) -> false);
            }
            case AIR -> {
                Vec3 push = target.position().subtract(attacker.position()).normalize().scale(0.5 * lvl);
                target.push(push.x, 0.25 + 0.2 * lvl, push.z);
                target.syncVelocity = true;
            }
            case FIRE -> target.igniteForSeconds(3.0F * lvl);
            case EARTH -> {
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40 * lvl, lvl - 1), attacker);
                target.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 40 * lvl, 128), attacker);
                if (lvl >= 3 && rand.nextFloat() < 0.2F)
                    level.explode(attacker, target.getX(), target.getY(), target.getZ(), 1.2F, net.minecraft.world.level.Level.ExplosionInteraction.NONE);
            }
            case POISON -> target.addEffect(new MobEffectInstance(MobEffects.POISON, 60 * lvl, lvl - 1), attacker);
            case STONE -> {
                target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 80 * lvl, lvl - 1), attacker);
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40 * lvl, 0), attacker);
            }
            case ICE -> {
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50 * lvl, lvl), attacker);
                target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 40, target.getTicksFrozen() + 50 * lvl));
                if (lvl >= 3 && rand.nextFloat() < 0.25F) target.addEffect(new MobEffectInstance(ModEffects.FROZEN, 40), attacker);
            }
            case MIGHT -> Util2.knock(target, 0.4 * lvl, attacker.getX() - target.getX(), attacker.getZ() - target.getZ());
            case LIGHTNING -> {
                if (rand.nextFloat() < 0.1F * lvl && level.canSeeSky(target.blockPosition())) {
                    LightningBolt bolt = net.minecraft.world.entity.EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
                    if (bolt != null) {
                        bolt.snapTo(target.getX(), target.getY(), target.getZ());
                        bolt.setVisualOnly(false);
                        level.addFreshEntity(bolt);
                    }
                } else {
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 1), attacker);
                    level.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1, target.getZ(), 10, 0.3, 0.5, 0.3, 0.1);
                }
            }
            case LIFE -> attacker.heal(Mth.clamp(damage * 0.08F * lvl, 0.5F, 4F));
            case SHADOW -> {
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30 * lvl, 0), attacker);
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 30 * lvl, lvl - 1), attacker);
            }
            case LIGHT -> target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100 * lvl, 0), attacker);
        }
    }

    public static CompoundTag tagOf(ItemStack stack) {
        CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
        return cd == null ? new CompoundTag() : cd.copyTag();
    }
}
