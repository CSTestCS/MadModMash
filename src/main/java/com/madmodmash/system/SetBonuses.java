package com.madmodmash.system;

import com.madmodmash.MadModMash;
import com.madmodmash.item.HaloItem;
import com.madmodmash.registry.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/** Armor set perks, drawn from "__RPG Item Uses.txt" and the MobMats armor list. */
public final class SetBonuses {
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public static String setOf(ItemStack s) {
        if (s.isEmpty()) return "";
        Identifier id = BuiltInRegistries.ITEM.getKey(s.getItem());
        if (!id.getNamespace().equals(MadModMash.MODID)) return "";
        String p = id.getPath();
        int i = p.lastIndexOf('_');
        return i < 0 ? "" : p.substring(0, i);
    }

    public static int count(LivingEntity e, String set) {
        int n = 0;
        for (EquipmentSlot s : ARMOR) if (setOf(e.getItemBySlot(s)).equals(set)) n++;
        return n;
    }

    /** Full set: four pieces, or three for the three-piece sets (wetsuit, full plates). */
    public static boolean full(LivingEntity e, String set) {
        int need = (set.equals("wetsuit") || set.endsWith("full_plate")) ? 3 : 4;
        return count(e, set) >= need;
    }

    public static boolean wearing(LivingEntity e, String set, EquipmentSlot slot) {
        return setOf(e.getItemBySlot(slot)).equals(set);
    }

    private static void eff(LivingEntity e, Holder<MobEffect> effect, int amp) {
        e.addEffect(new MobEffectInstance(effect, 50, amp, true, false, true));
    }

    private static void attr(Player p, Holder<Attribute> a, String key, double amount, AttributeModifier.Operation op, boolean on) {
        AttributeInstance inst = p.getAttribute(a);
        if (inst == null) return;
        Identifier id = MadModMash.id("set_" + key);
        boolean has = inst.getModifier(id) != null;
        if (on && !has) inst.addTransientModifier(new AttributeModifier(id, amount, op));
        else if (!on && has) inst.removeModifier(id);
    }

    /** Runs every 20 ticks. */
    public static void tick(ServerPlayer p) {
        ServerLevel level = (ServerLevel) p.level();
        BlockPos pos = p.blockPosition();
        boolean night = !level.isBrightOutside();
        if (p.getInventory().getSelectedItem().isEmpty() && false) return;
        for (String plate : new String[]{"copper_full_plate", "iron_full_plate", "gold_full_plate", "diamond_full_plate", "netherite_full_plate"})
            if (full(p, plate)) { eff(p, MobEffects.RESISTANCE, 0); attr(p, Attributes.MOVEMENT_SPEED, "plate", -0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, true); }
        boolean anyPlate = false;
        for (String plate : new String[]{"copper_full_plate", "iron_full_plate", "gold_full_plate", "diamond_full_plate", "netherite_full_plate"}) anyPlate |= full(p, plate);
        if (!anyPlate) attr(p, Attributes.MOVEMENT_SPEED, "plate", 0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, false);
        if (full(p, "alpha_leather") && night) eff(p, MobEffects.STRENGTH, 0);
        if (full(p, "ruby")) eff(p, MobEffects.HEALTH_BOOST, 0);
        if (full(p, "bedrock")) { eff(p, MobEffects.RESISTANCE, 1); eff(p, MobEffects.SLOWNESS, 0); }
        if (full(p, "herobrine")) {
            eff(p, MobEffects.NIGHT_VISION, 0);
            if (p.isShiftKeyDown()) eff(p, MobEffects.INVISIBILITY, 0);
            for (Mob m : Util2.near(level, Mob.class, p, 12, m -> m instanceof net.minecraft.world.entity.monster.Enemy)) m.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, true, false));
        }
        if ((full(p, "village_copper") || full(p, "village_iron")) && level.isVillage(pos)) eff(p, MobEffects.HERO_OF_THE_VILLAGE, 0);
        if (full(p, "magma")) eff(p, MobEffects.FIRE_RESISTANCE, 0);
        if (full(p, "wetsuit") && p.isInWater()) { eff(p, MobEffects.WATER_BREATHING, 0); eff(p, MobEffects.DOLPHINS_GRACE, 0); eff(p, MobEffects.CONDUIT_POWER, 0); }
        if (count(p, "wolfpelt") > 0) p.removeEffect(MobEffects.SLOWNESS);
        if (full(p, "lich")) p.removeEffect(MobEffects.WITHER);
        if (full(p, "dragonscale")) { eff(p, MobEffects.FIRE_RESISTANCE, 0); eff(p, MobEffects.RESISTANCE, 0); }
        if (full(p, "guardian") && p.isInWater()) { eff(p, MobEffects.DOLPHINS_GRACE, 0); eff(p, MobEffects.WATER_BREATHING, 0); }
        if (full(p, "elder_guardian")) { p.removeEffect(MobEffects.MINING_FATIGUE); if (p.isInWater()) eff(p, MobEffects.CONDUIT_POWER, 0); }
        if (full(p, "arctic")) {
            p.setTicksFrozen(0);
            if (level.getBlockState(pos.below()).is(BlockTags.ICE) || level.getBlockState(pos).is(Blocks.SNOW) || level.getBlockState(pos.below()).is(Blocks.SNOW_BLOCK)) eff(p, MobEffects.SPEED, 1);
        }
        if (full(p, "wizard")) {
            Holder<Biome> b = level.getBiome(pos);
            if (b.is(BiomeTags.IS_FOREST)) eff(p, MobEffects.REGENERATION, 0);
            else if (b.is(BiomeTags.IS_MOUNTAIN) || b.is(BiomeTags.IS_HILL)) eff(p, MobEffects.JUMP_BOOST, 1);
            else if (b.is(net.neoforged.neoforge.common.Tags.Biomes.IS_SWAMP)) { eff(p, MobEffects.WATER_BREATHING, 0); p.removeEffect(MobEffects.POISON); }
            else if (b.is(net.neoforged.neoforge.common.Tags.Biomes.IS_DESERT)) eff(p, MobEffects.FIRE_RESISTANCE, 0);
            else eff(p, MobEffects.SPEED, 0);
        }
        if (full(p, "fox_hunter")) { eff(p, MobEffects.NIGHT_VISION, 0); if (p.isShiftKeyDown()) eff(p, MobEffects.INVISIBILITY, 0); }
        if (full(p, "blazebone")) {
            eff(p, MobEffects.FIRE_RESISTANCE, 0);
            for (Mob m : Util2.near(level, Mob.class, p, 3, m -> m instanceof net.minecraft.world.entity.monster.Enemy)) m.igniteForSeconds(2);
        }
        if (full(p, "bat")) eff(p, MobEffects.NIGHT_VISION, 0);
        if (full(p, "hoglord") && p.isSprinting()) eff(p, MobEffects.STRENGTH, 0);
        if (full(p, "witch")) p.removeEffect(MobEffects.POISON);
        boolean astro = full(p, "astroite");
        attr(p, Attributes.GRAVITY, "astroite", -0.45, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, astro);
        attr(p, Attributes.SAFE_FALL_DISTANCE, "astroite", 8, AttributeModifier.Operation.ADD_VALUE, astro);
        attr(p, Attributes.KNOCKBACK_RESISTANCE, "fossil", 1.0, AttributeModifier.Operation.ADD_VALUE, full(p, "fossil") || full(p, "bedrock"));
        mysteryPerks(p);
        haloTick(p, level);
        // Mark V energy shield
        PlayerData d = ModAttachments.get(p);
        if (full(p, "mark_v")) {
            if (d.shieldCooldown > 0) d.shieldCooldown -= 20;
            else if (p.getAbsorptionAmount() < 6) p.setAbsorptionAmount(Math.min(6, p.getAbsorptionAmount() + 2));
        }
    }

    /** Runs every tick for movement-based perks. */
    public static void fastTick(ServerPlayer p) {
        ServerLevel level = (ServerLevel) p.level();
        boolean airborne = !p.onGround() && !p.isInWater() && !p.getAbilities().flying;
        boolean jump = p.getLastClientInput().jump();
        if (full(p, "hive") && airborne && jump) {
            Vec3 v = p.getDeltaMovement();
            p.setDeltaMovement(v.x, Math.max(v.y, 0.12), v.z);
            p.syncVelocity = true;
            p.fallDistance = 0;
        }
        if ((full(p, "plumage") || full(p, "bat")) && airborne && p.isShiftKeyDown()) {
            Vec3 v = p.getDeltaMovement();
            if (v.y < -0.12) { p.setDeltaMovement(v.x * 1.02, -0.12, v.z * 1.02); p.syncVelocity = true; }
            p.fallDistance = 0;
        }
        if (wearing(p, "endermite_slides", EquipmentSlot.FEET) && p.onGround() && p.isSprinting()) {
            Vec3 v = p.getDeltaMovement();
            if (v.horizontalDistanceSqr() < 0.5) { p.setDeltaMovement(v.x * 1.06, v.y, v.z * 1.06); p.syncVelocity = true; }
        }
        if (wearing(p, "magma", EquipmentSlot.FEET) && p.onGround()) {
            BlockPos c = p.blockPosition().below();
            for (BlockPos b : BlockPos.betweenClosed(c.offset(-1, 0, -1), c.offset(1, 0, 1)))
                if (level.getBlockState(b).is(Blocks.LAVA) && level.getFluidState(b).isSource()) level.setBlockAndUpdate(b, Blocks.OBSIDIAN.defaultBlockState());
        }
    }

    private static final Holder<MobEffect>[] MYSTERY = new Holder[]{MobEffects.SPEED, MobEffects.HASTE, MobEffects.JUMP_BOOST, MobEffects.NIGHT_VISION,
            MobEffects.LUCK, MobEffects.FIRE_RESISTANCE, MobEffects.WATER_BREATHING, MobEffects.REGENERATION, MobEffects.RESISTANCE, MobEffects.STRENGTH};

    private static void mysteryPerks(ServerPlayer p) {
        for (EquipmentSlot s : ARMOR) {
            ItemStack st = p.getItemBySlot(s);
            if (!setOf(st).equals("mystery")) continue;
            CustomData cd = st.get(DataComponents.CUSTOM_DATA);
            int perk = cd == null ? -1 : cd.copyTag().getIntOr("mm_perk", -1);
            if (perk < 0) {
                int roll = p.getRandom().nextInt(MYSTERY.length);
                CustomData.update(DataComponents.CUSTOM_DATA, st, t -> t.putInt("mm_perk", roll));
                perk = roll;
            }
            eff(p, MYSTERY[perk], 0);
        }
    }

    public static Holder<MobEffect> mysteryPerk(int idx) {
        return MYSTERY[Math.max(0, Math.min(MYSTERY.length - 1, idx))];
    }

    private static void haloTick(ServerPlayer p, ServerLevel level) {
        if (!(p.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof HaloItem halo)) return;
        switch (halo.kind) {
            case POWER -> eff(p, MobEffects.STRENGTH, 1);
            case FIRE -> eff(p, MobEffects.FIRE_RESISTANCE, 0);
            case LIGHT -> { eff(p, MobEffects.NIGHT_VISION, 0); for (var e : p.getActiveEffects().stream().filter(e -> !e.getEffect().value().isBeneficial()).toList()) p.removeEffect(e.getEffect()); }
            case HEALING -> eff(p, MobEffects.REGENERATION, 1);
            case INVISIBILITY -> eff(p, MobEffects.INVISIBILITY, 0);
            case WATER -> eff(p, MobEffects.WATER_BREATHING, 0);
            case SWIFTNESS -> { eff(p, MobEffects.SPEED, 1); eff(p, MobEffects.HASTE, 0); }
            case GRAVITY -> { eff(p, MobEffects.JUMP_BOOST, 2); p.fallDistance = 0; }
            case MIGHT -> eff(p, MobEffects.RESISTANCE, 1);
            case LIFE -> eff(p, MobEffects.HEALTH_BOOST, 2);
            case POISON -> { eff(p, MobEffects.SATURATION, 0); for (Mob m : Util2.near(level, Mob.class, p, 6, m -> m instanceof net.minecraft.world.entity.monster.Enemy)) m.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0)); }
            case DARKNESS -> { for (Mob m : Util2.near(level, Mob.class, p, 10, m -> m instanceof net.minecraft.world.entity.monster.Enemy)) { m.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60)); m.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60)); } }
            case DEATH -> { for (Mob m : Util2.near(level, Mob.class, p, 2.5, m -> m instanceof net.minecraft.world.entity.monster.Enemy)) m.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1)); }
            case THORNS -> { for (Mob m : Util2.near(level, Mob.class, p, 4, m -> m instanceof net.minecraft.world.entity.monster.Enemy)) m.hurtServer(level, level.damageSources().thorns(p), 1); eff(p, MobEffects.REGENERATION, 0); }
            case EXPLOSIONS -> eff(p, MobEffects.RESISTANCE, 0);
        }
    }

    private SetBonuses() {}
}
