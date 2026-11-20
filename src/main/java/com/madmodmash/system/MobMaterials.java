package com.madmodmash.system;

import com.madmodmash.item.CarvingKnifeItem;
import com.madmodmash.registry.ModEffects;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * "____MobMats.txt": every creature has a common and a rare material. Common materials drop on a plain kill
 * now and then; rare materials need a carving knife (or a creature left Carved and bleeding).
 * Alphas always give up their rare material.
 */
public final class MobMaterials {
    record Mats(String common, String rare) {}
    private static final Map<String, Mats> TABLE = new HashMap<>();
    private static void put(String mob, String common, String rare) { TABLE.put(mob, new Mats(common, rare)); }
    private static final String M = "madmodmash:";

    static {
        put("minecraft:sheep", "minecraft:white_wool", M + "thick_wool");
        put("minecraft:iron_golem", "minecraft:iron_ingot", "minecraft:iron_block");
        put("minecraft:skeleton", "minecraft:bone", "minecraft:bone_block");
        put("minecraft:spider", "minecraft:string", "minecraft:cobweb");
        put("minecraft:slime", "minecraft:slime_ball", "minecraft:slime_block");
        put("minecraft:shulker", "minecraft:shulker_shell", M + "shulker_chitin");
        put("minecraft:wither_skeleton", M + "wither_bone", M + "wither_bone_block");
        put("minecraft:zombie", M + "fabric", M + "sickly_leather");
        put("minecraft:creeper", M + "creeper_leaves", M + "charged_leaves");
        put("minecraft:ender_dragon", M + "dragon_scale", M + "dragon_skin");
        put("minecraft:wolf", M + "wolf_fur", M + "alpha_wolf_fur");
        put("minecraft:bee", M + "bee_wing", M + "bee_chitin");
        put("minecraft:horse", M + "horse_hair", M + "horse_leather");
        put("minecraft:turtle", "minecraft:turtle_scute", "minecraft:turtle_scute");
        put("minecraft:chicken", "minecraft:feather", M + "stiff_feather");
        put("minecraft:salmon", M + "salmon_scale", M + "strong_salmon_scale");
        put("minecraft:squid", M + "squid_skin", M + "squid_tentacle");
        put("minecraft:glow_squid", M + "squid_eye", M + "squid_tentacle");
        put("minecraft:enderman", M + "enderman_scales", M + "spectral_scales");
        put("minecraft:blaze", "minecraft:blaze_rod", M + "fibrous_flame");
        put("minecraft:bat", M + "bat_fur", M + "bat_wing");
        put("minecraft:cat", M + "cat_fur", M + "cat_fur");
        put("minecraft:cave_spider", "minecraft:string", M + "toxic_string");
        put("minecraft:cow", "minecraft:leather", M + "thick_leather");
        put("minecraft:drowned", "minecraft:rotten_flesh", M + "squid_skin");
        put("minecraft:elder_guardian", M + "elder_guardian_fin", M + "elder_guardian_scales");
        put("minecraft:endermite", M + "endermite_mucus", M + "endermite_softshell");
        put("minecraft:evoker", M + "unholy_cloth", M + "herobrine_charm");
        put("minecraft:fox", M + "fox_pelt", M + "fox_tail");
        put("minecraft:ghast", "minecraft:ghast_tear", M + "fibrous_mist");
        put("minecraft:guardian", M + "guardian_fin", M + "guardian_scales");
        put("minecraft:hoglin", M + "hoglin_leather", M + "hoglin_tusk");
        put("minecraft:zoglin", M + "hoglin_bristle", M + "hoglin_tusk");
        put("minecraft:illusioner", M + "illusory_cloth", M + "bizarre_star");
        put("minecraft:magma_cube", "minecraft:magma_cream", M + "magma_scales");
        put("minecraft:mooshroom", "minecraft:leather", M + "shroomhide");
        put("minecraft:phantom", "minecraft:phantom_membrane", M + "phantom_hide");
        put("minecraft:pig", M + "pigskin", M + "pigskin");
        put("minecraft:pillager", M + "steel_nugget", M + "steel_ingot");
        put("minecraft:polar_bear", M + "polar_pelt", M + "polar_pelt");
        put("minecraft:ravager", M + "thick_leather", M + "corrupt_leather");
        put("minecraft:silverfish", M + "silverfish_chitin", M + "silverfish_chitin");
        put("minecraft:stray", "minecraft:bone", M + "ancient_cloth");
        put("minecraft:bogged", "minecraft:bone", M + "toxic_string");
        put("minecraft:strider", M + "strider_hair", M + "strider_hair");
        put("minecraft:vindicator", M + "bloodied_cloth", M + "broken_axehead");
        put("minecraft:witch", M + "nasty_cloth", M + "emerald_charm");
        put("minecraft:wither", M + "wither_bone_block", M + "withered_soul");
        put("minecraft:goat", M + "ram_horn", M + "ram_horn");
        put("minecraft:husk", "minecraft:rotten_flesh", M + "ancient_cloth");
        put("minecraft:warden", "minecraft:echo_shard", M + "warden_soul");
        put("minecraft:piglin", "minecraft:gold_nugget", M + "hoglin_leather");
        put(M + "ram", M + "ram_horn", M + "ram_horn");
        put(M + "boar", "minecraft:porkchop", M + "hoglin_bristle");
        put(M + "monkey", M + "monkey_fur", M + "banana");
        put(M + "golden_eagle", M + "golden_eagle_feather", M + "stiff_feather");
        put(M + "zephyr", M + "zephyr_wisp", M + "fibrous_mist");
        put(M + "locust", M + "locust_wing", M + "locust_wing");
        put(M + "locust_swarm", M + "locust_wing", M + "bee_chitin");
        put(M + "locust_horde", M + "locust_wing", M + "bizarre_star");
        put(M + "mortis_zombie", M + "mortis_flesh", M + "sickly_leather");
        put(M + "zombie_trudger", M + "mortis_flesh", M + "withered_soul");
        put(M + "illager_hero", M + "steel_nugget", M + "bloodied_cloth");
        put(M + "technomancer", "minecraft:redstone", M + "redstone_core");
        put(M + "herobrine_cultist", M + "unholy_cloth", M + "herobrine_charm");
        put(M + "redstone_golem", "minecraft:redstone", M + "redstone_core");
        put(M + "steel_golem", M + "steel_nugget", M + "steel_ingot");
        put(M + "iron_mimic", "minecraft:iron_nugget", "minecraft:iron_block");
        put(M + "overworld_piglin", "minecraft:gold_nugget", M + "hoglin_leather");
        put(M + "headless_horseman", "minecraft:carved_pumpkin", M + "withered_soul");
    }

    public static void onKill(ServerLevel level, LivingEntity victim, Player killer, java.util.function.Consumer<ItemStack> drop) {
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType());
        Mats mats = TABLE.get(id.toString());
        if (mats == null) return;
        var r = victim.getRandom();
        ItemStack held = killer.getMainHandItem();
        int knife = held.getItem() instanceof CarvingKnifeItem k ? k.tier + 1 : 0;
        boolean carved = victim.hasEffect(ModEffects.CARVING);
        boolean alpha = Champions.isAlpha(victim) || Champions.isChampion(victim);
        if (r.nextFloat() < 0.30F + knife * 0.08F) drop.accept(Util2.stack(mats.common(), 1 + (knife >= 3 ? r.nextInt(2) : 0)));
        float rare = knife > 0 ? 0.06F + knife * 0.06F : 0.0F;
        if (carved) rare += 0.12F;
        if (alpha || r.nextFloat() < rare) drop.accept(Util2.stack(mats.rare(), 1));
        if (alpha && victim instanceof net.minecraft.world.entity.animal.Animal) {
            if (victim.getType() == net.minecraft.world.entity.EntityTypes.COW) { drop.accept(Util2.stack(M + "alpha_beef", 2)); drop.accept(Util2.stack(M + "alpha_leather", 1)); }
            if (victim.getType() == net.minecraft.world.entity.EntityTypes.WOLF) drop.accept(Util2.stack(M + "alpha_wolf_fur", 1));
        }
    }

    private MobMaterials() {}
}
