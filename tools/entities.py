"""Entity roster: registration, textures, recovered model geometry and renderer wiring."""
import os, json
from PIL import Image
from texlib import tint, hue, load, hexrgb, S

MODELS = json.load(open(os.path.join(os.path.dirname(os.path.abspath(__file__)), 'models.json')))
def M(suffix):
    for k in MODELS:
        if k.endswith(suffix): return MODELS[k]
    raise KeyError(suffix)

# model id -> recovered geometry
MODEL_SRC = {
    'golden_eagle': 'ModelgiantEagle.class', 'monkey': 'Modelmonkey.class', 'monkey_sword': 'ModelmonkeyWithSword.class',
    'monkey_pickaxe': 'ModelmonkeyWithPickaxe.class', 'monkey_crossbow': 'ModelmonkeyWithCrossbow.class', 'monkey_chest': 'ModelmonkeyWithChest.class',
    'ram': 'ModelThaRam.class', 'boar': 'ModelBoar.class', 'zephyr': 'WildLoot1.0.0/net/mcreator/wildloot/client/model/Modelzephyr.class',
    'locust': 'Modellocust.class', 'redstone_golem': 'RedstoneGolemEntity$Modelcustom_model.class', 'steel_golem': 'SteelGolemEntity$Modelcustom_model.class',
    'stone_golem': 'LesserStoneGolemEntity$Modelblockgolem.class', 'summoner': 'SummonerEntity$Modelblockmob.class',
    'illager_butcher': 'IllagerButcherEntity$Modelillagerbutcher.class', 'illager_priest': 'IllagerPriestEntity$Modelillagerpriest.class',
    'illager_farmer': 'IllagerFarmerEntity$ModelillagerFarmer.class', 'illager_nitwit': 'IllagerNitwitEntity$Modelillagernitwit.class',
    'atlas_zombie': 'AtlasZombieEntity$Modelzombietrudger.class', 'zombie_trudger': 'ZombiePlagueMod_1_0_4/net/mcreator/plaguemod/client/model/ModelzombieTrudger.class',
    'redstone_minion': 'darkadventures/client/model/Modelredstoneminion.class', 'redstone_turret': 'darkadventures/client/model/ModelredstoneTurret.class',
    'nexus': 'RedstoneNexusEntity$Modelnexus.class', 'villager_minion': 'VillagerMinionEntity$Modelvillagertotem.class',
    'llama_turret': 'LlamaTurretEntity$ModelllamaTurret.class', 'mortis_zombie': 'darkadventures/client/model/ModelmortisZombie.class',
    'mortis_arm': 'darkadventures/client/model/ModelmortisArm.class', 'mortis_chicken': 'ModelmortisChicken.class', 'mortis_cow': 'ModelmortisCow.class',
    'mortis_sheep': 'ModelmortisSheep.class', 'mortis_egg': 'ModelmortisEgg.class', 'headless_horseman': 'ModelHeadlessHorseman.class',
    'living_head': 'Nay/net/mcreator/modjammarch/client/model/ModelLivingHead.class', 'combat_dummy': 'ModelBipedStatue.class',
    'mutant_dummy': 'ModelMutatedDummy.class', 'bonfire': 'darkadventures/client/model/Modelbonfire.class', 'effigy': 'Modeleffigy.class',
    'loot_engram': 'WildLoot1.0.0/net/mcreator/wildloot/client/model/ModelLootEngram.class', 'slash': 'ModelSlashProjectile.class',
}
MANUAL_MODELS = {
    'iron_mimic': {'tex': [64, 32], 'parts': [{'name': 'body', 'pose': [0, 24, 0, 0, 0, 0], 'cubes': [{'uv': [0, 0], 'box': [-8, -16, -8, 16, 16, 16], 'infl': 0, 'mirror': False}], 'children': []}]},
}

def J(e): return e['id'].upper()

ENTITIES = []
def ent(id, name, cls, cat, size, egg=None, attrs=None, rend=None, tex=None, extra='', track=10, lore=None):
    ENTITIES.append(dict(id=id, name=name, cls=cls, cat=cat, size=size, egg=egg, attrs=attrs, rend=rend, tex=tex or {}, extra=extra, track=track, lore=lore))

def R(model, tex, shadow=0.5, scale=1.0, glow=None):
    g = f'"{glow}"' if glow else 'null'
    return f'ctx -> new MashMobRenderer<>(ctx, GeneratedModels.{model.upper()}, "{tex}", {g}, {shadow}f, {scale}f)'

# ---------------------------------------------------------------- the Wilds
SLIMES = [('stone', 'slimestone', 'Stone'), ('sand', 'slimesand', 'Sand'), ('wood', 'slimewood', 'Wood'), ('earth', 'slimeearth', 'Earth'),
          ('coal', 'slimecoal', 'Coal'), ('iron', 'slimeiron', 'Iron'), ('gold', 'slimegold', 'Gold'), ('redstone', 'slimeredstone', 'Redstone'),
          ('lapis', 'slimelapis', 'Lapis'), ('diamond', 'slimediamond', 'Diamond'), ('emerald', 'slimeemerald', 'Emerald'),
          ('obsidian', 'slimeobsidian', 'Obsidian'), ('ice', 'slimeice', 'Ice'), ('lava', 'slimelava', 'Lava'), ('honey', 'slimehoney', 'Honey'),
          ('glass', 'slimeglass', 'Glass'), ('glowstone', 'slimeglowstone', 'Glowstone'), ('netherrack', 'slimenetherrack', 'Netherrack'),
          ('soul', 'slimesoulfire', 'Soul Fire'), ('prismarine', 'slimeprismarine', 'Prismarine'), ('netherite', 'slimenetherite', 'Netherite')]
SLIME_EGG = {'stone': ('#8a8a8a', '#5a5a5a'), 'sand': ('#e0d4a0', '#b8a870'), 'wood': ('#a07a40', '#6a4a20'), 'earth': ('#7a5a3a', '#4a8a3a'),
             'coal': ('#3a3a3a', '#1a1a1a'), 'iron': ('#d8d8d8', '#a08070'), 'gold': ('#f4d03f', '#c09020'), 'redstone': ('#c02020', '#600000'),
             'lapis': ('#2a50b0', '#183080'), 'diamond': ('#4fe6d6', '#2a9a90'), 'emerald': ('#2ad06a', '#108a3a'), 'obsidian': ('#2a1a40', '#100820'),
             'ice': ('#a8d0ff', '#7aa0e0'), 'lava': ('#ff7a20', '#c02a00'), 'honey': ('#f0b030', '#b07010'), 'glass': ('#e0f0f0', '#a0c0c0'),
             'glowstone': ('#f8e080', '#c0a040'), 'netherrack': ('#7a2a2a', '#4a1010'), 'soul': ('#40d0e0', '#4a3a2a'),
             'prismarine': ('#5aa090', '#2a6a60'), 'netherite': ('#4a4246', '#2a2226')}
for sk, stex, sname in SLIMES:
    ent(f'{sk}_slime', f'{sname} Slime', 'MaterialSlime', 'MONSTER', (0.52, 0.52), SLIME_EGG[sk], 'MaterialSlime::createAttributes',
        f'ctx -> new MaterialSlimeRenderer(ctx, "{sk}_slime")', {f'entity/slime/{sk}_slime': f'FLUB:{stex}'},
        extra='.fireImmune()' if sk in ('lava', 'netherrack', 'soul', 'obsidian', 'netherite') else '')
ent('monkey', 'Monkey', 'Monkey', 'CREATURE', (0.6, 0.9), ('#7a4a22', '#e0c090'), 'Monkey::createAttributes', 'MonkeyRenderer::new',
    {'entity/monkey/monkey': 'MONKEY:entities/monkey', 'entity/monkey/monkey_chest': 'MONKEY:entities/monkeywchest', 'entity/monkey/monkey_pickaxe': 'MONKEY:entities/monkeywdiamond',
     'entity/monkey/monkey_boots': 'MONKEY:entities/monkeywithboots', 'entity/monkey/monkey_crossbow': 'MONKEY:entities/monkeywithcrossbow', 'entity/monkey/monkey_sword': 'MONKEY:entities/monkeywithsword'})
ent('golden_eagle', 'Golden Eagle', 'GoldenEagle', 'CREATURE', (1.2, 1.0), ('#7a5a2a', '#e2b33a'), 'GoldenEagle::createAttributes',
    R('golden_eagle', 'entity/golden_eagle', 0.6, 1.0, 'entity/golden_eagle_glow'), {'entity/golden_eagle': 'AA:goldeneagle', 'entity/golden_eagle_glow': 'AA:goldeneagleglow'})
ent('ram', 'Ram', 'Ram', 'CREATURE', (0.9, 1.3), ('#e8e4d4', '#7a6a5a'), 'Ram::createAttributes', R('ram', 'entity/ram', 0.7), {'entity/ram': 'DA:entities/ram'})
ent('boar', 'Boar', 'Boar', 'CREATURE', (0.9, 0.9), ('#6a4a3a', '#f0c0b0'), 'Boar::createAttributes', 'BoarRenderer::new',
    {'entity/boar': 'DA:entities/boar', 'entity/boar_king': 'DA:entities/boartechnobladecrown'})
ent('zephyr', 'Zephyr', 'Zephyr', 'MONSTER', (1.4, 2.6), ('#d8f6ff', '#9ac0d0'), 'Zephyr::createAttributes', R('zephyr', 'entity/zephyr', 0.0, 1.0), {'entity/zephyr': 'WL:entities/zephyrtornado'})
ent('locust', 'Locust', 'Locust', 'MONSTER', (0.4, 0.3), ('#b5a040', '#5a4a20'), 'Locust::createAttributes', R('locust', 'entity/locust', 0.2), {'entity/locust': 'PLAGUE8:locust'})
ent('locust_swarm', 'Locust Swarm', 'Locust', 'MONSTER', (0.9, 0.6), ('#b5a040', '#3a3010'), 'Locust::createSwarmAttributes', R('locust', 'entity/locust', 0.4, 2.2))
ent('locust_horde', 'Locust Horde', 'Locust', 'MONSTER', (1.6, 1.1), ('#9a8a30', '#2a2008'), 'Locust::createHordeAttributes', R('locust', 'entity/locust', 0.8, 4.0))

# ---------------------------------------------------------------- Golems & constructs
ent('redstone_golem', 'Redstone Golem', 'RedstoneGolem', 'MISC', (1.4, 2.7), ('#c02020', '#ff7a40'), 'RedstoneGolem::createAttributes',
    R('redstone_golem', 'entity/redstone_golem', 1.0, 1.0, 'entity/redstone_golem_glow'),
    {'entity/redstone_golem': 'KWIK:redstonegolem', 'entity/redstone_golem_glow': 'KWIK:redstonegolememissive'})
ent('steel_golem', 'Steel Golem', 'SteelGolem', 'MISC', (1.4, 2.7), ('#9fb4c8', '#5a6a7a'), 'SteelGolem::createAttributes',
    R('steel_golem', 'entity/steel_golem', 1.0), {'entity/steel_golem': 'QTH:greatersteelgolem'})
ent('stone_golem', 'Lesser Stone Golem', 'LesserStoneGolem', 'MONSTER', (0.9, 1.5), ('#8a8a8a', '#3a3a3a'), 'LesserStoneGolem::createAttributes',
    R('stone_golem', 'entity/stone_golem', 0.6), {'entity/stone_golem': 'MJAMO:stonemonster'})
ent('iron_mimic', 'Iron Mimic', 'IronMimic', 'MONSTER', (1.0, 1.0), ('#d8d8d8', '#c03030'), 'IronMimic::createAttributes',
    R('iron_mimic', 'entity/iron_mimic', 0.5), {'entity/iron_mimic': 'QUACK:angryiron'})

# ---------------------------------------------------------------- the Illager Host
HERO_OUTFITS = ['pillager', 'vindicator', 'evoker', 'illusioner', 'illagerking', 'illagerrook']
HERO_TEX = {f'entity/illager_hero/{o}': f'IH:entities/{o}' for o in HERO_OUTFITS}
HERO_TEX['entity/illager_hero/technomancer'] = tint('IH:entities/evoker', '#b03020', keep=0.45)
HERO_TEX['entity/illager_hero/cultist'] = tint('IH:entities/evoker', '#40205a', keep=0.35)
ent('illager_hero', 'Illager Hero', 'IllagerHero', 'MONSTER', (0.6, 1.95), ('#5a5a5a', '#2a4a7a'), 'IllagerHero::createAttributes', 'IllagerHeroRenderer::new', HERO_TEX)
ent('technomancer', 'Technomancer', 'Technomancer', 'MONSTER', (0.6, 1.95), ('#b03020', '#3a3a3a'), 'Technomancer::createAttributes', 'IllagerHeroRenderer::new')
ent('herobrine_cultist', 'Herobrine Cultist', 'HerobrineCultist', 'MONSTER', (0.6, 1.95), ('#40205a', '#ffffff'), 'HerobrineCultist::createAttributes', 'IllagerHeroRenderer::new')
CIVS = [('butcher', 'illager_butcher', 'illagerbutcherfixed'), ('smith', 'illager_butcher', 'illagersmith'), ('farmer', 'illager_farmer', 'illagerfarmer'),
        ('priest', 'illager_priest', 'illagerpriest'), ('librarian', 'illager_nitwit', 'illagerlibrarian'), ('nitwit', 'illager_nitwit', 'illagernitwit')]
for ck, cmodel, ctex in CIVS:
    ent(f'illager_{ck}', f'Illager {ck.title()}', 'IllagerCivilian', 'CREATURE', (0.6, 1.95), ('#959b9b', {'butcher': '#a03030', 'smith': '#303030', 'farmer': '#6a9a3a',
        'priest': '#8a3aa0', 'librarian': '#e0e0e0', 'nitwit': '#3a8a3a'}[ck]), 'IllagerCivilian::createAttributes', R(cmodel, f'entity/illager_civilian/{ck}', 0.5),
        {f'entity/illager_civilian/{ck}': f'NEW:{ctex}'})
ent('summoner', 'Summoner', 'Summoner', 'MONSTER', (0.8, 0.8), ('#6a6a6a', '#40a0ff'), 'Summoner::createAttributes',
    R('summoner', 'entity/summoner', 0.5), {'entity/summoner': 'MJAMO:blockminion'})
ent('redstone_minion', 'Redstone Minion', 'RedstoneMinion', 'MONSTER', (0.7, 0.9), ('#8a8a8a', '#c02020'), 'RedstoneMinion::createAttributes',
    R('redstone_minion', 'entity/redstone_minion', 0.4), {'entity/redstone_minion': 'DA:entities/redstoneminion'})
ent('redstone_turret', 'Redstone Turret', 'RedstoneTurret', 'MONSTER', (0.9, 1.1), ('#6a6a6a', '#ff3a20'), 'RedstoneTurret::createAttributes',
    R('redstone_turret', 'entity/redstone_turret', 0.5), {'entity/redstone_turret': 'DA:entities/redstoneturret'})
ent('redstone_nexus', 'Redstone Nexus', 'Nexus', 'MONSTER', (1.4, 2.0), ('#c02020', '#3a3a3a'), 'Nexus::createAttributes',
    R('nexus', 'entity/nexus/redstone', 0.8), {'entity/nexus/redstone': 'QOL:nexusredstone', 'entity/nexus/emerald': 'QOL:nexusemerald'})
ent('emerald_nexus', 'Emerald Nexus', 'Nexus', 'MISC', (1.4, 2.0), ('#2ad06a', '#3a3a3a'), 'Nexus::createAttributes', R('nexus', 'entity/nexus/emerald', 0.8))
ent('villager_minion', 'Villager Minion', 'VillagerMinion', 'MISC', (0.6, 1.0), ('#a07a50', '#2ad06a'), 'VillagerMinion::createAttributes',
    R('villager_minion', 'entity/villager_minion', 0.4), {'entity/villager_minion': 'QOL:villagertotem'})
ent('llama_turret', 'Llama Turret', 'LlamaTurret', 'MISC', (0.9, 1.6), ('#e0d0b0', '#2ad06a'), 'LlamaTurret::createAttributes',
    R('llama_turret', 'entity/llama_turret', 0.5), {'entity/llama_turret': 'QOL:llamaturret'})

# ---------------------------------------------------------------- the Mortis Blight
ent('mortis_zombie', 'Mortis Zombie', 'MortisZombie', 'MONSTER', (0.6, 1.95), ('#5a7a3a', '#2a3a1a'), 'MortisZombie::createAttributes',
    R('mortis_zombie', 'entity/mortis/zombie', 0.5, 1.0, 'entity/mortis/zombie_e'),
    {'entity/mortis/zombie': 'DA:entities/mortiszombie', 'entity/mortis/zombie_e': 'DA:entities/mortiszombie_e',
     'entity/mortis/zombie_pigman': 'ZP:entities/mortiszombiepigman', 'entity/mortis/zombie_pigman_e': 'ZP:entities/mortiszombiepigman_e',
     'entity/mortis/arm': 'DA:entities/mortisarm', 'entity/mortis/arm_pigman': 'ZP:entities/mortisarmzpig',
     'entity/mortis/chicken': 'ZP:entities/mortischicken', 'entity/mortis/chicken_e': 'ZP:entities/mortischicken_e',
     'entity/mortis/cow': 'ZP:entities/mortiscow', 'entity/mortis/cow_e': 'ZP:entities/mortiscow_e',
     'entity/mortis/pig': 'ZP:entities/mortispig', 'entity/mortis/pig_e': 'ZP:entities/mortispig_e',
     'entity/mortis/sheep': 'ZP:entities/mortissheep', 'entity/mortis/sheep_e': 'ZP:entities/mortissheep_e',
     'entity/mortis/egg': 'ZP:entities/mortisegg', 'entity/mortis/trudger': 'ZP:entities/mortiszombietrudger',
     'entity/mortis/trudger_e': 'ZP:entities/mortiszombietrudger_e', 'entity/mortis/atlas': 'NEW:atlaszombie'})
ent('mortis_zombie_pigman', 'Mortis Zombie Pigman', 'MortisZombie', 'MONSTER', (0.6, 1.95), ('#c08080', '#2a3a1a'), 'MortisZombie::createAttributes',
    R('mortis_zombie', 'entity/mortis/zombie_pigman', 0.5, 1.0, 'entity/mortis/zombie_pigman_e'), extra='.fireImmune()')
ent('mortis_arm', 'Mortis Arm', 'MortisArm', 'MONSTER', (0.5, 0.4), ('#5a7a3a', '#8a6a5a'), 'MortisArm::createAttributes', R('mortis_arm', 'entity/mortis/arm', 0.3))
ent('mortis_chicken', 'Mortis Chicken', 'MortisAnimal', 'MONSTER', (0.4, 0.7), ('#c0c0a0', '#5a7a3a'), 'MortisAnimal::createAttributes',
    R('mortis_chicken', 'entity/mortis/chicken', 0.3, 1.0, 'entity/mortis/chicken_e'))
ent('mortis_cow', 'Mortis Cow', 'MortisAnimal', 'MONSTER', (0.9, 1.4), ('#4a3a2a', '#5a7a3a'), 'MortisAnimal::createAttributes',
    R('mortis_cow', 'entity/mortis/cow', 0.7, 1.0, 'entity/mortis/cow_e'))
ent('mortis_pig', 'Mortis Pig', 'MortisAnimal', 'MONSTER', (0.9, 0.9), ('#e0a0a0', '#5a7a3a'), 'MortisAnimal::createAttributes', 'MortisPigRenderer::new')
ent('mortis_sheep', 'Mortis Sheep', 'MortisAnimal', 'MONSTER', (0.9, 1.3), ('#d0d0c0', '#5a7a3a'), 'MortisAnimal::createAttributes',
    R('mortis_sheep', 'entity/mortis/sheep', 0.7, 1.0, 'entity/mortis/sheep_e'))
ent('mortis_egg', 'Mortis Egg', 'MortisEgg', 'MONSTER', (0.4, 0.5), ('#d0d0b0', '#3a5a2a'), 'MortisEgg::createAttributes', R('mortis_egg', 'entity/mortis/egg', 0.2))
ent('zombie_trudger', 'Zombie Trudger', 'ZombieTrudger', 'MONSTER', (2.4, 5.0), ('#3a5a2a', '#1a1a1a'), 'ZombieTrudger::createAttributes',
    R('zombie_trudger', 'entity/mortis/trudger', 1.5, 1.0, 'entity/mortis/trudger_e'), track=12)
ent('atlas_zombie', 'Atlas Zombie', 'ZombieTrudger', 'MONSTER', (2.4, 5.0), ('#4a6a3a', '#7a5a4a'), 'ZombieTrudger::createAtlasAttributes',
    R('atlas_zombie', 'entity/mortis/atlas', 1.5), track=12)

# ---------------------------------------------------------------- Unholy / bosses
ent('herobrine', 'Herobrine', 'Herobrine', 'MONSTER', (0.6, 1.95), ('#00aaaa', '#ffffff'), 'Herobrine::createAttributes', 'HerobrineRenderer::new',
    {'entity/herobrine': None}, track=16)
ent('headless_horseman', 'Headless Horseman', 'HeadlessHorseman', 'MONSTER', (1.4, 2.8), ('#2a2a2a', '#f08020'), 'HeadlessHorseman::createAttributes',
    R('headless_horseman', 'entity/headless_horseman', 1.0, 1.0, 'entity/headless_horseman_glow'),
    {'entity/headless_horseman': 'NAY:entities/headlesshorseman', 'entity/headless_horseman_glow': 'NAY:entities/headlesshorsemanglow',
     'entity/horseless_headman': 'NAY:entities/downloadedheadless', 'entity/living_head': 'NAY:entities/headlesshorsemanhead',
     'entity/living_head_glow': 'NAY:entities/headlesshorsemanheadglow'}, track=12)
ent('horseless_headman', 'Horseless Headman', 'HorselessHeadman', 'MONSTER', (0.6, 1.95), ('#3a3a3a', '#f08020'), 'HorselessHeadman::createAttributes',
    'ctx -> new SimpleHumanoidRenderer<>(ctx, "entity/horseless_headman", false)')
ent('living_head', 'Living Head', 'LivingHead', 'MONSTER', (0.5, 0.5), ('#f08020', '#2a2a2a'), 'LivingHead::createAttributes',
    R('living_head', 'entity/living_head', 0.2, 1.0, 'entity/living_head_glow'))
ent('overworld_piglin', 'Overworld Piglin', 'OverworldPiglin', 'MONSTER', (0.6, 1.95), ('#e0a080', '#f4d03f'), 'OverworldPiglin::createAttributes',
    'OverworldPiglinRenderer::new', {'entity/overworld_piglin': 'DA:entities/overworldpiglin'})

# ---------------------------------------------------------------- Emerald Empire folk
TRAVELERS = [('alchemist', 'villageralchemist'), ('armorer', 'villagerarmorerwanderer'), ('armsman', 'villagerarmsman'),
             ('artificer', 'villagerartificer'), ('banker', 'villagerbanker'), ('locksmith', 'villagerlocksmith')]
for tk, ttex in TRAVELERS:
    ent(f'traveling_{tk}', f'Traveling {tk.title()}', 'TravelingMerchant', 'CREATURE', (0.6, 1.95), ('#3a6a9a', '#e0c090'), 'TravelingMerchant::createAttributes',
        f'ctx -> new TravelerRenderer(ctx, "entity/traveler/{tk}")', {f'entity/traveler/{tk}': f'RPG:entities/{ttex}'})
ent('villager_mourner', 'Villager Mourner', 'VillagerMourner', 'CREATURE', (0.6, 1.95), ('#2a2a2a', '#e0e0e0'), 'VillagerMourner::createAttributes',
    'ctx -> new TravelerRenderer(ctx, "entity/villager_mourner")', {'entity/villager_mourner': 'HMM:villagermourner'})
ent('statue_carver', 'Statue Carver', 'StatueCarver', 'CREATURE', (0.6, 1.95), ('#8a8a8a', '#e0c090'), 'StatueCarver::createAttributes',
    'ctx -> new TravelerRenderer(ctx, "entity/statue_carver")', {'entity/statue_carver': 'HMM:statuecarver'})
ent('companion', 'Companion', 'Companion', 'CREATURE', (0.6, 1.8), ('#e0c090', '#3a6a3a'), 'Companion::createAttributes', 'CompanionRenderer::new')
ent('proxy', 'Proxy', 'Proxy', 'MONSTER', (0.6, 1.95), ('#bd8b72', '#959b9b'), 'Proxy::createAttributes', 'ProxyRenderer::new')

# ---------------------------------------------------------------- objects
ent('bonfire', 'Bonfire', 'Bonfire', 'MISC', (1.0, 0.9), None, 'Bonfire::createAttributes', 'BonfireRenderer::new',
    {'entity/bonfire/tier1': 'DA:entities/bonfiretier1', 'entity/bonfire/tier2': 'DA:entities/bonfiretier2', 'entity/bonfire/tier3': 'DA:entities/bonfiretier3'})
ent('effigy', 'Effigy', 'Effigy', 'MISC', (0.6, 1.4), None, 'Effigy::createAttributes', R('effigy', 'entity/effigy', 0.4),
    {'entity/effigy': 'RPG:entities/effigy', 'entity/effigy_true': 'RPG:entities/effigytrue'})
ent('combat_dummy', 'Combat Dummy', 'CombatDummy', 'MISC', (0.6, 1.9), None, 'CombatDummy::createAttributes', R('combat_dummy', 'entity/combat_dummy', 0.5),
    {'entity/combat_dummy': 'DA:entities/statuebipedoak'})
ent('mutant_dummy', 'Mutant Dummy', 'CombatDummy', 'MISC', (0.9, 2.2), None, 'CombatDummy::createAttributes', R('mutant_dummy', 'entity/mutant_dummy', 0.6),
    {'entity/mutant_dummy': 'DA:entities/mutateddummy'})
ENGRAMS = ['wood', 'stone', 'copper', 'ice', 'amethyst', 'redstone', 'honey', 'iron', 'anvil', 'prismarine', 'gold', 'lapis', 'emerald', 'slime',
           'diamond', 'obsidian', 'crying_obsidian', 'ancient_debris', 'purpur', 'netherite', 'void', 'bedrock']
ENGRAM_SRC = {'crying_obsidian': 'cryingobsidian', 'ancient_debris': 'ancientdebris'}
ent('loot_engram', 'Loot Engram', 'LootEngram', 'MISC', (0.6, 0.6), None, None, 'LootEngramRenderer::new',
    {f'entity/engram/{e}': f'WL:entities/lootengram{ENGRAM_SRC.get(e, e)}' for e in ENGRAMS} | {'entity/engram/wolf': 'WL:entities/lootengramwolf', 'entity/engram/wither': 'WL:entities/lootengramwither'})

# projectiles (no attrs)
for pid, pname, prend in [('grapple_hook', 'Grapple Hook', 'item'), ('thrown_knife', 'Thrown Knife', 'item'), ('rock', 'Rock', 'item'),
                          ('pop_rock', 'Pop Rock', 'item'), ('magic_card', 'Magic Card', 'item'), ('lucky_spell', 'Lucky Spell', 'item'),
                          ('throwing_star', 'Throwing Star', 'item'), ('grenade', 'Grenade', 'item'), ('pocketball', 'Pocketball', 'item'),
                          ('turret_bolt', 'Turret Bolt', 'item'), ('rifle_round', 'Rifle Round', 'item'), ('meteor', 'Meteor', 'item'),
                          ('slash', 'Claymore Slash', 'slash'), ('llama_spit_shot', 'Llama Spit', 'item'), ('bow_charge', 'Elemental Charge', 'item')]:
    cls = {'grapple_hook': 'GrappleHook', 'thrown_knife': 'ThrownKnife', 'slash': 'SlashProjectile', 'meteor': 'Meteor'}.get(pid, 'MashProjectile')
    size = (0.25, 0.25) if pid not in ('meteor', 'slash') else ((1.5, 1.5) if pid == 'meteor' else (1.2, 0.4))
    rend = 'ctx -> new ThrownItemRenderer<>(ctx, ' + ('2.5f' if pid == 'meteor' else '1.0f') + ', true)' if prend == 'item' else 'SlashRenderer::new'
    ent(pid, pname, cls, 'MISC', size, None, None, rend, {'entity/slash': 'DA:entities/slashprojectile'} if pid == 'slash' else None, track=6)

def egg_texture(colors):
    base = load('V:item/egg'); px = base.load()
    c1 = hexrgb(colors[0]); c2 = hexrgb(colors[1])
    im = base.copy(); q = im.load()
    spots = {(6, 5), (7, 5), (9, 8), (10, 8), (10, 9), (5, 9), (6, 10), (8, 12), (9, 12), (11, 5), (7, 8)}
    for y in range(16):
        for x in range(16):
            r, g, b, a = px[x, y]
            if not a: continue
            l = (0.299 * r + 0.587 * g + 0.114 * b) / 255
            c = c2 if (x, y) in spots else c1
            f = 0.55 + 0.6 * l
            q[x, y] = (min(255, int(c[0] * f)), min(255, int(c[1] * f)), min(255, int(c[2] * f)), a)
    return im

def herobrine_skin():
    im = load('V:entity/player/wide/steve'); px = im.load()
    # eyes on the face (front of head: x 8..15, y 8..15); steve's eyes sit at row 12
    for x in (9, 10, 13, 14):
        px[x, 12] = (255, 255, 255, 255)
    return im

# ---------------------------------------------------------------- codegen
def fmt(v): return f'{float(v)}f'

def part_code(p, parent, out, indent='        '):
    var = 'p_' + str(len(out))
    cubes = ''.join(f'.texOffs({int(c["uv"][0])}, {int(c["uv"][1])})' + ('.mirror(true)' if c['mirror'] else '.mirror(false)') +
                    f'.addBox({fmt(c["box"][0])}, {fmt(c["box"][1])}, {fmt(c["box"][2])}, {fmt(c["box"][3])}, {fmt(c["box"][4])}, {fmt(c["box"][5])}, new CubeDeformation({fmt(c["infl"])}))'
                    for c in p['cubes'])
    pose = p['pose']
    out.append(f'{indent}PartDefinition {var} = {parent}.addOrReplaceChild("{p["name"]}", CubeListBuilder.create(){cubes}, '
               f'PartPose.offsetAndRotation({", ".join(fmt(x) for x in pose)}));')
    for c in p['children']: part_code(c, var, out, indent)

def emissive_alpha(spec):
    """Older mods drew glow layers on an opaque black background because their eyes layer blended
    additively, so black added nothing. 26.3's eyes layer honours alpha, so the black would paint the whole
    mob. Turn brightness into alpha (and rescale the colour) to keep the additive look."""
    im = load(spec)
    px = im.load()
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            m = max(r, g, b)
            if a == 0 or m < 8: px[x, y] = (0, 0, 0, 0); continue
            if a < 255: continue  # already uses real transparency
            px[x, y] = (r * 255 // m, g * 255 // m, b * 255 // m, m)
    return im

EMISSIVE_SUFFIXES = ('_e', '_glow', '_eyes')

def generate(put_tex, wjson, lang, A, D, Jdir):
    # textures
    for e in ENTITIES:
        for dest, src in e['tex'].items():
            if e['id'] == 'herobrine': src = herobrine_skin()
            if dest.endswith(EMISSIVE_SUFFIXES): src = emissive_alpha(src)
            put_tex(src, dest)
        lang[f'entity.madmodmash.{e["id"]}'] = e['name']
    eyes = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    for x in (9, 10, 13, 14): eyes.putpixel((x, 12), (255, 255, 255, 255))
    put_tex(eyes, 'entity/herobrine_eyes')
    # professions overlays
    put_tex('DA:entity/villager/profession/banker', 'entity/villager/profession/banker')
    put_tex('DA:entity/zombie_villager/profession/banker', 'entity/zombie_villager/profession/banker')
    put_tex('MJ23:entity/villager/profession/bounty_buyer', 'entity/villager/profession/bounty_buyer')
    put_tex('MJ23:entity/zombie_villager/profession/bounty_buyer', 'entity/zombie_villager/profession/bounty_buyer')
    put_tex(tint('V:entity/villager/profession/armorer', '#2a9a4a', keep=0.5), 'entity/villager/profession/quartermaster')
    put_tex(tint('V:entity/villager/profession/armorer', '#2a9a4a', keep=0.5), 'entity/zombie_villager/profession/quartermaster')
    put_tex(tint('V:entity/villager/profession/cartographer', '#8a6a3a', keep=0.5), 'entity/villager/profession/contractor')
    put_tex(tint('V:entity/villager/profession/cartographer', '#8a6a3a', keep=0.5), 'entity/zombie_villager/profession/contractor')
    # models
    meth = []; layers = []; regs = []
    allm = {k: M(v) for k, v in MODEL_SRC.items()}
    allm.update(MANUAL_MODELS)
    for mid, m in allm.items():
        out = []
        for p in m['parts']: part_code(p, 'root', out)
        meth.append(f'    public static LayerDefinition {mid}() {{\n        MeshDefinition mesh = new MeshDefinition();\n        PartDefinition root = mesh.getRoot();\n'
                    + '\n'.join(out) + f'\n        return LayerDefinition.create(mesh, {int(m["tex"][0])}, {int(m["tex"][1])});\n    }}\n')
        layers.append(f'    public static final ModelLayerLocation {mid.upper()} = new ModelLayerLocation(MadModMash.id("{mid}"), "main");')
        regs.append(f'        event.registerLayerDefinition({mid.upper()}, GeneratedModels::{mid});')
    src = GM_TEMPLATE.replace('%LAYERS%', '\n'.join(layers)).replace('%METHODS%', '\n'.join(meth)).replace('%REGS%', '\n'.join(regs))
    cdir = Jdir.replace('/registry', '/client')
    os.makedirs(cdir, exist_ok=True)
    open(f'{cdir}/GeneratedModels.java', 'w').write(src)
    # ModEntities
    lines = []; attrs = []; rends = []
    for e in ENTITIES:
        w, h = e['size']
        lines.append(f'    public static final DeferredHolder<EntityType<?>, EntityType<{e["cls"]}>> {J(e)} = ENTITY_TYPES.register("{e["id"]}", () -> '
                     f'EntityType.Builder.<{e["cls"]}>of({e["cls"]}::new, MobCategory.{e["cat"]}).sized({w}f, {h}f).clientTrackingRange({e["track"]}){e["extra"]}'
                     f'.build(ResourceKey.create(Registries.ENTITY_TYPE, MadModMash.id("{e["id"]}"))));')
        if e['attrs']: attrs.append(f'        event.put({J(e)}.get(), {e["attrs"].replace("::", ".")}().build());')
        rends.append(f'        event.registerEntityRenderer(ModEntities.{J(e)}.get(), {e["rend"]});')
    open(f'{Jdir}/ModEntities.java', 'w').write(ME_TEMPLATE.replace('%LINES%', '\n'.join(lines)).replace('%ATTRS%', '\n'.join(attrs)))
    open(f'{cdir}/GeneratedRenderers.java', 'w').write(GR_TEMPLATE.replace('%RENDS%', '\n'.join(rends)))

GM_TEMPLATE = '''// GENERATED by scripts/entities.py from geometry recovered from the original mods' model classes.
package com.madmodmash.client;

import com.madmodmash.MadModMash;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class GeneratedModels {
%LAYERS%

%METHODS%
    public static void register(EntityRenderersEvent.RegisterLayerDefinitions event) {
%REGS%
    }

    private GeneratedModels() {}
}
'''
ME_TEMPLATE = '''// GENERATED by scripts/entities.py - do not edit by hand.
package com.madmodmash.registry;

import com.madmodmash.MadModMash;
import com.madmodmash.entity.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MadModMash.MODID);

%LINES%

    public static void attributes(EntityAttributeCreationEvent event) {
%ATTRS%
    }

    private ModEntities() {}
}
'''
GR_TEMPLATE = '''// GENERATED by scripts/entities.py - do not edit by hand.
package com.madmodmash.client;

import com.madmodmash.registry.ModEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class GeneratedRenderers {
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
%RENDS%
    }

    private GeneratedRenderers() {}
}
'''
