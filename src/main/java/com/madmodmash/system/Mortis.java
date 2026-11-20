package com.madmodmash.system;

import com.madmodmash.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

/** The Mortis Blight (Zombie Plague mod "Rebirth" + Dark Adventures Mortis Infection). */
public final class Mortis {
    public static void convert(ServerLevel level, LivingEntity victim) {
        EntityType<?> t = victim.getType();
        EntityType<? extends Mob> into = null;
        if (victim instanceof Player || t == net.minecraft.world.entity.EntityTypes.VILLAGER || t == net.minecraft.world.entity.EntityTypes.ZOMBIE || t == net.minecraft.world.entity.EntityTypes.WANDERING_TRADER || t == net.minecraft.world.entity.EntityTypes.PILLAGER
                || t == net.minecraft.world.entity.EntityTypes.VINDICATOR || t == ModEntities.ILLAGER_HERO.get()) into = ModEntities.MORTIS_ZOMBIE.get();
        else if (t == net.minecraft.world.entity.EntityTypes.PIG) into = ModEntities.MORTIS_PIG.get();
        else if (t == net.minecraft.world.entity.EntityTypes.COW) into = ModEntities.MORTIS_COW.get();
        else if (t == net.minecraft.world.entity.EntityTypes.CHICKEN) into = ModEntities.MORTIS_CHICKEN.get();
        else if (t == net.minecraft.world.entity.EntityTypes.SHEEP) into = ModEntities.MORTIS_SHEEP.get();
        else if (t == net.minecraft.world.entity.EntityTypes.PIGLIN || t == net.minecraft.world.entity.EntityTypes.ZOMBIFIED_PIGLIN || t == ModEntities.OVERWORLD_PIGLIN.get()) into = ModEntities.MORTIS_ZOMBIE_PIGMAN.get();
        if (into == null) return;
        Mob m = Util2.spawn(level, into, victim.blockPosition());
        if (m != null && victim.hasCustomName()) m.setCustomName(victim.getCustomName());
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.SPORE_BLOSSOM_AIR, victim.getX(), victim.getY() + 1, victim.getZ(), 30, 0.4, 0.6, 0.4, 0.0);
    }

    private Mortis() {}
}
