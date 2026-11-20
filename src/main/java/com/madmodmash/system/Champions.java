package com.madmodmash.system;

import com.madmodmash.registry.ModEffects;
import com.madmodmash.registry.ModGameRules;
import com.madmodmash.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * Champions & Alphas.
 * HostileBuffs ("Enhanced" mobs dropping coins), Illager Heroes (random names), Dark Adventures
 * (mutations, Alpha animals) and ModJelly (bounties) all describe "special" versions of normal mobs.
 * Here they are one system: any hostile can rise as a named Champion carrying a Trait mutation;
 * any wild animal can be an Alpha.
 */
public final class Champions {
    public static final String CHAMPION = "mm_champion", ALPHA = "mm_alpha", TRAIT = "mm_trait", WANTED = "mm_wanted", ROLLED = "mm_rolled";

    public static final String[] NAMES = {"Vigo", "Gimlon", "Freman", "Khole", "Murek", "Tolin", "Azash", "Urgo", "Laim", "Khefan", "Dalos", "Jovun",
            "Bralf", "Anar", "Orman", "Eleazar", "Ralston", "Moritz", "Aurelius", "Yegor", "Pumal", "Ragin", "Edom", "Azod", "Duncan",
            "Tarmel", "Arnole", "Ruthangren", "Dlev", "Besman", "Povu", "Tuuks", "Welld", "Skai", "Nomis", "Iccelerox", "Cragor", "Kredann", "Ghane", "Phanarax"};
    static final String[] TITLES = {"the Brute", "the Outlaw", "the Warlord", "the Marauder", "the Scoundrel", "the Gladiator", "the Berserker",
            "the Pit-Fighter", "the Overseer", "the Interrogator", "the Smuggler", "the Mercenary", "the Reaper", "the Seeker", "the Arbiter",
            "the Sentinel", "the Hermit", "the Outcast", "the Duelist", "the Myth", "the Legend", "the Rogue", "the Elite", "the Champion"};

    public static boolean isChampion(LivingEntity e) {
        return e.getPersistentData().getBooleanOr(CHAMPION, false);
    }

    public static boolean isAlpha(LivingEntity e) {
        return e.getPersistentData().getBooleanOr(ALPHA, false);
    }

    public static Trait trait(LivingEntity e) {
        String s = e.getPersistentData().getStringOr(TRAIT, "");
        if (s.isEmpty()) return null;
        try { return Trait.valueOf(s); } catch (IllegalArgumentException ex) { return null; }
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || event.loadedFromDisk() || !(event.getEntity() instanceof Mob mob)) return;
        CompoundTag pd = mob.getPersistentData();
        if (pd.getBooleanOr(ROLLED, false)) return;
        pd.putBoolean(ROLLED, true);
        ServerLevel level = (ServerLevel) event.getLevel();
        RandomSource r = mob.getRandom();
        if (mob instanceof Enemy && mob.getType().canSerialize() && !mob.is(net.neoforged.neoforge.common.Tags.EntityTypes.BOSSES)) {
            Faction f = Faction.of(mob);
            Wrath wrath = Wrath.get(level);
            double chance = MashConfig.CHAMPION_CHANCE.get() + (f == null ? 0 : wrath.tier(f) * 0.012);
            if (r.nextDouble() < chance) makeChampion(level, mob, null);
        } else if (mob instanceof Animal && r.nextDouble() < MashConfig.ALPHA_CHANCE.get()) {
            makeAlpha(mob);
        }
    }

    public static void makeChampion(ServerLevel level, Mob mob, Trait forced) {
        RandomSource r = mob.getRandom();
        CompoundTag pd = mob.getPersistentData();
        pd.putBoolean(CHAMPION, true);
        Faction f = Faction.of(mob);
        int tier = f == null ? 0 : Wrath.get(level).tier(f);
        boolean mutate = forced != null || tier >= 2 || r.nextFloat() < 0.35F || level.getGameRules().get(ModGameRules.COMMON_MUTATIONS.get());
        Trait trait = forced != null ? forced : (mutate ? Trait.values()[r.nextInt(Trait.values().length)] : null);
        if (trait != null) pd.putString(TRAIT, trait.name());
        String name = NAMES[r.nextInt(NAMES.length)] + " " + TITLES[r.nextInt(TITLES.length)];
        Component display = Component.literal(name).withStyle(trait == null ? ChatFormatting.GOLD : trait.color, ChatFormatting.BOLD);
        mob.setCustomName(display);
        mob.setCustomNameVisible(true);
        mob.setPersistenceRequired();
        boost(mob, Attributes.MAX_HEALTH, 1.0 + 0.2 * tier, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        boost(mob, Attributes.ATTACK_DAMAGE, 0.5 + 0.1 * tier, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        boost(mob, Attributes.MOVEMENT_SPEED, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        boost(mob, Attributes.ARMOR, 4 + tier, AttributeModifier.Operation.ADD_VALUE);
        boost(mob, Attributes.SCALE, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        boost(mob, Attributes.KNOCKBACK_RESISTANCE, 0.4, AttributeModifier.Operation.ADD_VALUE);
        mob.setHealth(mob.getMaxHealth());
        if (trait == Trait.FIRE) mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, -1));
        if (trait == Trait.LIFE) mob.addEffect(new MobEffectInstance(MobEffects.REGENERATION, -1, 0));
        if (trait == Trait.STONE) mob.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, -1, 0));
        if (trait == Trait.AIR) mob.addEffect(new MobEffectInstance(MobEffects.SPEED, -1, 0));
        if (trait == Trait.LIGHT) mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, -1, 0));
        if (trait == Trait.EARTH) mob.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, -1, 1));
        if (trait == Trait.POISON) mob.addEffect(new MobEffectInstance(MobEffects.POISON, 1, 0));
        if (trait == Trait.MIGHT) mob.addEffect(new MobEffectInstance(MobEffects.STRENGTH, -1, 0));
    }

    public static void makeAlpha(Mob mob) {
        mob.getPersistentData().putBoolean(ALPHA, true);
        boost(mob, Attributes.SCALE, 0.35, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        boost(mob, Attributes.MAX_HEALTH, 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        mob.setHealth(mob.getMaxHealth());
        mob.setCustomName(Component.translatable("msg.madmodmash.alpha", mob.getType().getDescription()).withStyle(ChatFormatting.DARK_AQUA));
    }

    private static void boost(Mob mob, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, double amount, AttributeModifier.Operation op) {
        AttributeInstance inst = mob.getAttribute(attr);
        if (inst == null) return;
        Identifier id = com.madmodmash.MadModMash.id("champion");
        inst.removeModifier(id);
        inst.addPermanentModifier(new AttributeModifier(id, amount, op));
    }

    /** Loot for a slain Champion: trophy, a trait core, gildans and maybe an engram. */
    public static void dropLoot(ServerLevel level, LivingEntity champion, net.minecraft.world.entity.player.Player killer) {
        Faction f = Faction.of(champion);
        int tier = f == null ? 0 : Wrath.get(level).tier(f);
        ItemStack trophy = new ItemStack(ModItems.CHAMPION_TROPHY.get());
        trophy.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                Component.translatable("item.madmodmash.champion_trophy.named", champion.getName()).withStyle(ChatFormatting.GOLD));
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, trophy,
                t -> t.putBoolean("wanted", champion.getPersistentData().getBooleanOr(WANTED, false)));
        Util2.drop(level, champion.getX(), champion.getY() + 0.5, champion.getZ(), trophy);
        Trait t = trait(champion);
        if (t != null) {
            int lvl = tier >= 6 ? 3 : tier >= 3 ? 2 : 1;
            if (champion.getRandom().nextFloat() < 0.15F) lvl = Math.min(3, lvl + 1);
            Util2.drop(level, champion.getX(), champion.getY() + 0.5, champion.getZ(), TraitCores.core(t, lvl));
        }
        Economy.drop(level, champion.getX(), champion.getY() + 0.5, champion.getZ(), 9 + champion.getRandom().nextInt(30) + tier * 20L);
        if (champion.getRandom().nextFloat() < 0.35F + tier * 0.05F) Engrams.spawn(level, champion.position(), Math.min(21, 3 + tier * 2 + champion.getRandom().nextInt(4)));
        if (killer instanceof net.minecraft.server.level.ServerPlayer sp) {
            Renown.add(sp, 15 + tier * 3, "renown.madmodmash.champion");
            Quests.onChampionKilled(sp, champion);
        }
        if (f != null) Wrath.get(level).add(level.getServer(), f, -10);
    }

    static { ModEffects.class.getName(); }

    private Champions() {}
}
