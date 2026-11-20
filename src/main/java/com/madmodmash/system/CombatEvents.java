package com.madmodmash.system;

import com.madmodmash.entity.*;
import com.madmodmash.item.*;
import com.madmodmash.registry.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerSpawnPhantomsEvent;

public final class CombatEvents {
    public static final String PROVOKED = "mm_provoked";

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level)) return;
        DamageSource src = event.getSource();
        float amount = event.getAmount();
        if (victim.hasEffect(ModEffects.ARMOR_LOCK)) { event.setCanceled(true); return; }

        // ---- defender perks
        if (victim instanceof ServerPlayer p) {
            PlayerData d = ModAttachments.get(p);
            d.shieldCooldown = 100;
            if (src.is(DamageTypeTags.IS_FIRE)) amount *= 1F - 0.125F * SetBonuses.count(p, "gambeson");
            if (SetBonuses.full(p, "amethyst") && src.is(DamageTypeTags.WITCH_RESISTANT_TO)) amount *= 0.6F;
            if (src.is(DamageTypeTags.IS_EXPLOSION)) {
                if (SetBonuses.full(p, "steel")) amount *= 0.7F;
                if (p.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof HaloItem h && h.kind == HaloItem.Kind.EXPLOSIONS) amount *= 0.5F;
            }
            Calling c = Calling.byName(d.calling);
            if (c == Calling.TURTLE && p.isShiftKeyDown()) amount *= 0.4F;
            if (c == Calling.BOXER) amount *= 0.9F;
            if (c == Calling.FIREMAN && (src.is(DamageTypeTags.IS_FIRE) || src.is(DamageTypeTags.IS_LIGHTNING))) { event.setCanceled(true); return; }
            if (src.getEntity() instanceof LivingEntity att && att != p) {
                if (SetBonuses.full(p, "magma")) att.igniteForSeconds(4);
                if (p.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof HaloItem h && h.kind == HaloItem.Kind.THORNS) att.hurtServer(level, level.damageSources().thorns(p), 2);
                if (SetBonuses.full(p, "evocation") && p.getRandom().nextFloat() < 0.25F)
                    level.addFreshEntity(new EvokerFangs(level, att.getX(), att.getY(), att.getZ(), p.getYRot(), 0, p));
                if (SetBonuses.full(p, "spectre") && p.getRandom().nextFloat() < 0.25F)
                    p.randomTeleport(p.getX() + p.getRandom().nextInt(13) - 6, p.getY(), p.getZ() + p.getRandom().nextInt(13) - 6, true, (BlockState s) -> false);
            }
        }

        // ---- attacker side
        if (src.getEntity() instanceof LivingEntity attacker && src.getDirectEntity() == attacker) {
            ItemStack weapon = attacker.getMainHandItem();
            if (attacker instanceof ServerPlayer p) {
                Calling c = Calling.byName(ModAttachments.get(p).calling);
                if (c == Calling.TURTLE && p.isShiftKeyDown()) { event.setCanceled(true); return; }
                if (c == Calling.BOXER && weapon.isEmpty()) amount += 4;
                if (c == Calling.NECRO && victim instanceof AbstractSkeleton) amount = Math.max(amount, victim.getHealth() + 1);
                if (victim instanceof Mob m) m.getPersistentData().putLong(PROVOKED, level.getGameTime());
                if (weapon.getItem() instanceof MashWeaponItem w && w.cls == WeaponClass.TRUNCHEON && victim instanceof Mob
                        && amount >= victim.getHealth() && !(victim instanceof Player)) {
                    amount = Math.max(0, victim.getHealth() - 1);
                    victim.addEffect(new MobEffectInstance(ModEffects.SUBDUED, 2400, 0), p);
                    if (victim instanceof Mob m) m.setTarget(null);
                }
            }
            Trait t = Trait.of(weapon);
            if (t != null) amount += t.bonusDamage(victim, Trait.level(weapon), amount);
            Trait ct = Champions.trait(attacker);
            if (ct != null) amount += ct.bonusDamage(victim, 2, amount);
        }
        event.setAmount(amount);
    }

    @SubscribeEvent
    public static void onDamagePost(LivingDamageEvent.Post event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level)) return;
        DamageSource src = event.getSource();
        if (!(src.getEntity() instanceof LivingEntity attacker) || src.getDirectEntity() != attacker) return;
        float dealt = event.getOriginalDamage();
        ItemStack weapon = attacker.getMainHandItem();
        Trait t = Trait.of(weapon);
        if (t != null) t.onHit(level, attacker, victim, Trait.level(weapon), dealt);
        Trait ct = Champions.trait(attacker);
        if (ct != null) ct.onHit(level, attacker, victim, 2, dealt);
        if (attacker instanceof ServerPlayer p) {
            Calling c = Calling.byName(ModAttachments.get(p).calling);
            var r = p.getRandom();
            if (c == Calling.VIPER && r.nextInt(3) == 0) victim.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0), p);
            if (c == Calling.SNAIL && r.nextInt(3) == 0) victim.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 1), p);
            if (c == Calling.CANNIBAL) { victim.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 0), p); p.getFoodData().eat(1, 0.2F); }
            if (SetBonuses.full(p, "lich")) victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0), p);
            if (SetBonuses.full(p, "shulker")) victim.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 20, 0), p);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level)) return;
        Entity killerEntity = event.getSource().getEntity();
        // Mortis spread (Zombie Plague + Dark Adventures Mortis Infection)
        boolean mortisKill = killerEntity instanceof MortisZombie || killerEntity instanceof MortisAnimal || victim.hasEffect(ModEffects.ZOMBIE_PLAGUE);
        if (mortisKill && level.getGameRules().get(ModGameRules.MORTIS_SPREAD.get())) Mortis.convert(level, victim);
        // Villager mourning (HmmMod)
        if (victim instanceof Villager) VillagerMourner.onVillagerDeath(level, victim);
        Faction f = Faction.of(victim);
        if (f != null && killerEntity instanceof Player) Wrath.get(level).add(level.getServer(), f, Champions.isChampion(victim) ? 5 : 2);
        if (!(killerEntity instanceof ServerPlayer p)) return;
        ContractItem.onKill(p, victim);
        Quests.onKill(p, victim);
        if (p.hasEffect(ModEffects.MIDAS_TOUCH)) Economy.drop(level, victim.getX(), victim.getY() + 0.5, victim.getZ(), 1 + (long) (victim.getMaxHealth() / 4));
        Calling c = Calling.byName(ModAttachments.get(p).calling);
        if (c == Calling.VAMPIRE) p.heal(3);
        if (c == Calling.BERSERKER) { p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 160, 0)); p.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0)); }
        if (p.getMainHandItem().getItem() instanceof LegendarySwordItem) LegendarySwordItem.addKill(p.getMainHandItem(), victim);
        if (victim.is(net.neoforged.neoforge.common.Tags.EntityTypes.BOSSES) || victim instanceof ZombieTrudger || victim instanceof HeadlessHorseman || victim instanceof Herobrine)
            Renown.add(p, 100, "renown.madmodmash.boss");
        else if (victim instanceof Enemy && p.getRandom().nextInt(4) == 0) Renown.add(p, 1, "renown.madmodmash.kill");
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level) || !(event.getSource().getEntity() instanceof Player p)) return;
        MobMaterials.onKill(level, victim, p, s -> event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(level, victim.getX(), victim.getY() + 0.5, victim.getZ(), s)));
        if (Champions.isChampion(victim)) Champions.dropLoot(level, victim, p);
        Calling c = Calling.byName(ModAttachments.get(p).calling);
        if (c == Calling.FLETCHER && victim.getType() == net.minecraft.world.entity.EntityTypes.CHICKEN)
            event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(level, victim.getX(), victim.getY(), victim.getZ(), new ItemStack(net.minecraft.world.item.Items.FEATHER, 2)));
        if (c == Calling.NECRO && victim instanceof AbstractSkeleton)
            event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(level, victim.getX(), victim.getY(), victim.getZ(), new ItemStack(net.minecraft.world.item.Items.BONE, 2)));
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer p)) return;
        ServerLevel level = (ServerLevel) p.level();
        if (SetBonuses.full(p, "plumage")) { event.setDamageMultiplier(0); return; }
        if (SetBonuses.full(p, "slime") && event.getDistance() > 2) {
            event.setDamageMultiplier(0);
            p.setDeltaMovement(p.getDeltaMovement().x, Math.min(1.2, event.getDistance() * 0.08), p.getDeltaMovement().z);
            p.syncVelocity = true;
            return;
        }
        Calling c = Calling.byName(ModAttachments.get(p).calling);
        if (c == Calling.WORM && level.getBlockState(p.blockPosition().below()).is(net.minecraft.tags.BlockTags.DIRT)) event.setDamageMultiplier(0);
        if (c == Calling.STOMPER && event.getDistance() > 3) {
            float dmg = (float) event.getDistance() - 3;
            for (LivingEntity e : Util2.near(level, LivingEntity.class, p, 2.5, e -> e != p)) e.hurtServer(level, level.damageSources().playerAttack(p), dmg);
            event.setDamageMultiplier(Math.min(1F, 4F / Math.max(1F, dmg)));
        }
        if (c == Calling.KANGAROO) event.setDistance(Math.max(0, event.getDistance() - 3));
    }

    @SubscribeEvent
    public static void onTarget(LivingChangeTargetEvent event) {
        if (!(event.getNewAboutToBeSetTarget() instanceof ServerPlayer p) || !(event.getEntity() instanceof Mob mob)) return;
        long provoked = mob.getPersistentData().getLongOr(PROVOKED, -100000L);
        boolean wasProvoked = mob.level().getGameTime() - provoked < 600;
        if (wasProvoked) return;
        if (mob instanceof Herobrine && SetBonuses.full(p, "herobrine")) { event.setCanceled(true); return; }
        String type = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getPath();
        boolean ignore =
                (SetBonuses.full(p, "ghillie") && mob instanceof Enemy && mob.distanceTo(p) > 4)
                || (Calling.byName(ModAttachments.get(p).calling) == Calling.MONSTER && mob instanceof Enemy)
                || ((SetBonuses.full(p, "evocation") || p.hasEffect(ModEffects.UNDEATH)) && mob.is(EntityTypeTags.UNDEAD))
                || (SetBonuses.full(p, "bone") && mob instanceof AbstractSkeleton)
                || ((SetBonuses.full(p, "wolfpelt") || SetBonuses.full(p, "alpha_leather")) && mob.getType() == net.minecraft.world.entity.EntityTypes.WOLF)
                || (SetBonuses.full(p, "silk") && (type.equals("spider") || type.equals("cave_spider")))
                || (SetBonuses.full(p, "slime") && mob instanceof net.minecraft.world.entity.monster.cubemob.Slime)
                || (SetBonuses.full(p, "hive") && mob.getType() == net.minecraft.world.entity.EntityTypes.BEE)
                || (SetBonuses.full(p, "witch") && mob.getType() == net.minecraft.world.entity.EntityTypes.WITCH)
                || (SetBonuses.full(p, "vile") && (mob.getType() == net.minecraft.world.entity.EntityTypes.RAVAGER || mob.getType() == net.minecraft.world.entity.EntityTypes.HOGLIN))
                || (SetBonuses.full(p, "phantom") && mob.getType() == net.minecraft.world.entity.EntityTypes.PHANTOM)
                || (SetBonuses.full(p, "hoglord") && (mob.getType() == net.minecraft.world.entity.EntityTypes.PIGLIN || mob instanceof OverworldPiglin));
        if (ignore) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEndermanAnger(EndermanAngerEvent event) {
        Player p = event.getPlayer();
        if (SetBonuses.full(p, "spectre") || SetBonuses.full(p, "endermite_slides")) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPhantoms(PlayerSpawnPhantomsEvent event) {
        if (SetBonuses.full(event.getEntity(), "phantom")) event.setResult(PlayerSpawnPhantomsEvent.Result.DENY);
    }

    private CombatEvents() {}
}
