"""Data pack generator for MadModMash: loot tables, trades, tags, recipes, brewing, worldgen, dimension, loot modifiers."""
import os, sys, json, shutil
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from content import ITEMS, BLOCKS, ARMOR, MINERAL_IDS, POWDERS, WMATS, WCLASSES, CHISELS, CALLINGS, HALOS, TOY, STATUES, KEYS
import entities

RES = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..') + '/src/main/resources'
D = RES + '/data/madmodmash'
MC = RES + '/data/minecraft'
NS = 'madmodmash'
M = 'madmodmash:'

def w(path, obj):
    if '/loot_table/' in path:
        import lootfmt; obj = lootfmt.conv(obj)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w') as f: json.dump(obj, f, indent=2)

ITEM_IDS = {i['id'] for i in ITEMS} | {b['id'] for b in BLOCKS if b['item']}
for a in ARMOR:
    for p in a['pieces']: ITEM_IDS.add(a['key'] + '_' + {'H': 'helmet', 'C': 'chestplate', 'L': 'leggings', 'B': 'boots'}[p])
def mm(i):
    assert i in ITEM_IDS, f'unknown item {i}'
    return M + i

# ------------------------------------------------------------------ loot tables
def entry(item, weight=1, lo=1, hi=1):
    e = {'type': 'minecraft:item', 'name': item, 'weight': weight}
    if hi > 1 or lo > 1:
        e['functions'] = [{'function': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': lo, 'max': hi} if hi != lo else lo}]
    return e

def table(pools):
    return {'type': 'minecraft:chest', 'pools': pools}

def pool(entries, rolls=1, bonus=0):
    r = rolls if isinstance(rolls, (int, float)) else {'type': 'minecraft:uniform', 'min': rolls[0], 'max': rolls[1]}
    return {'rolls': r, 'bonus_rolls': bonus, 'entries': entries}

# value-ranked loot pool (0..21, matching the engram ladder)
V = 'minecraft:'
VALUED = [
    (0, V + 'stick', 1, 8), (0, V + 'bread', 2, 6), (0, V + 'oak_log', 4, 12), (0, mm('wooden_club'), 1, 1), (0, mm('wooden_knife'), 1, 1),
    (0, mm('copper_gildan'), 3, 9), (0, mm('rock'), 2, 6), (1, V + 'cobblestone', 8, 24), (1, mm('stone_hammer'), 1, 1), (1, V + 'torch', 4, 16),
    (2, V + 'copper_ingot', 2, 6), (2, mm('copper_battleaxe'), 1, 1), (2, mm('wolf_fur'), 1, 3), (2, mm('creeper_leaves'), 1, 3), (2, mm('copper_key'), 1, 1),
    (3, V + 'ice', 2, 6), (3, mm('carulem_ingot'), 1, 1), (3, mm('air_bubble'), 1, 3),
    (4, V + 'amethyst_shard', 2, 6), (4, mm('silver_gildan'), 1, 4), (4, mm('vistren_powder'), 1, 2), (4, mm('celerus_powder'), 1, 2), (4, mm('rusted_key'), 1, 1),
    (5, V + 'redstone', 4, 12), (5, mm('redstone_core'), 1, 1), (5, mm('blank_contract'), 1, 2),
    (6, V + 'honeycomb', 2, 5), (6, mm('lock_pick'), 1, 3), (6, mm('sanatia_powder'), 1, 2), (6, mm('pocketball'), 1, 2),
    (7, V + 'iron_ingot', 2, 6), (7, mm('iron_key'), 1, 1), (7, mm('iron_katana'), 1, 1), (7, mm('steel_ingot'), 1, 3), (7, mm('iron_magnifier'), 1, 1),
    (8, V + 'anvil', 1, 1), (8, mm('karite_ingot'), 1, 1), (8, mm('village_iron_chestplate'), 1, 1), (8, mm('magic_card_deck'), 1, 1),
    (9, V + 'prismarine_shard', 3, 8), (9, mm('guardian_scales'), 1, 2), (9, mm('standard_ammunition'), 2, 6),
    (10, mm('gold_gildan'), 1, 3), (10, V + 'gold_ingot', 2, 5), (10, mm('gold_key'), 1, 1), (10, mm('golden_claymore'), 1, 1), (10, mm('mystery_helmet'), 1, 1),
    (10, mm('misket_shard'), 1, 1), (10, mm('zawist_shard'), 1, 1),
    (11, V + 'lapis_lazuli', 4, 12), (11, mm('flawless_lapis'), 1, 1), (11, mm('wizard_helmet'), 1, 1),
    (12, V + 'emerald', 2, 6), (12, mm('emerald_key'), 1, 1), (12, mm('rare_hunt_contract'), 1, 1), (12, mm('emerald_scythe'), 1, 1),
    (12, mm('lumberjack_calling'), 1, 1), (12, mm('kangaroo_calling'), 1, 1), (12, mm('werewolf_calling'), 1, 1), (12, mm('sprint_module'), 1, 1),
    (13, V + 'slime_block', 1, 2), (13, mm('slime_chestplate'), 1, 1),
    (14, V + 'diamond', 1, 3), (14, mm('diamond_key'), 1, 1), (14, mm('diamond_battleaxe'), 1, 1), (14, mm('sapphire'), 1, 1), (14, mm('ruby'), 1, 1),
    (14, mm('active_camo_module'), 1, 1), (14, mm('assault_rifle'), 1, 1),
    (15, V + 'obsidian', 2, 6), (15, mm('enchancer'), 1, 1), (15, mm('halo_of_swiftness'), 1, 1), (15, mm('wanted_poster'), 1, 1),
    (16, V + 'crying_obsidian', 2, 4), (16, mm('legendary_hunt_contract'), 1, 1), (16, mm('liliota_gem'), 1, 1), (16, mm('armor_lock_module'), 1, 1),
    (17, V + 'netherite_scrap', 1, 2), (17, mm('amethyst_gildan'), 1, 1), (17, mm('ancient_dagger'), 1, 1),
    (18, V + 'shulker_shell', 1, 2), (18, mm('shulker_chitin'), 1, 2), (18, mm('legendary_diamond_sword'), 1, 1), (18, mm('halo_of_life'), 1, 1),
    (19, V + 'netherite_ingot', 1, 1), (19, mm('netherite_key'), 1, 1), (19, mm('imperial_charter'), 1, 1), (19, mm('diamond_gildan'), 1, 1),
    (20, V + 'echo_shard', 2, 5), (20, mm('spectral_scales'), 1, 3), (20, mm('warden_soul'), 1, 1),
    (21, mm('netherite_gildan'), 1, 1), (21, mm('master_key'), 1, 1), (21, mm('bedrock_pickaxe'), 1, 1), (21, mm('halo_of_death'), 1, 1), (21, mm('master_crate'), 1, 1),
]
TIERS = entities.ENGRAMS

def valued_pool(tier, rolls):
    ents = []
    for v, item, lo, hi in VALUED:
        if tier - 7 <= v <= tier + 1:
            weight = max(1, 12 - abs(tier - v) * 2) if v <= tier else 2
            ents.append(entry(item, weight, lo, hi))
    return pool(ents, rolls)

def gen_loot():
    for t, name in enumerate(TIERS):
        w(f'{D}/loot_table/engram/{name}.json', table([valued_pool(t, (2, 3 + t // 5))]))
    for t in range(8):
        w(f'{D}/loot_table/crates/tier_{t}.json', table([valued_pool(min(21, t * 3), (3, 5 + t // 2)),
            pool([entry(mm(['copper_gildan', 'silver_gildan', 'silver_gildan', 'gold_gildan', 'gold_gildan', 'emerald_gildan', 'emerald_gildan', 'amethyst_gildan'][t]), 1, 2, 6)])]))
    w(f'{D}/loot_table/crates/village_crate.json', table([valued_pool(2, (3, 5)), pool([entry(V + 'wheat', 4, 4, 12), entry(V + 'carrot', 3, 2, 8), entry(mm('banana'), 2, 1, 4),
        entry(mm('fabric'), 2, 1, 4), entry(mm('village_magnifier'), 1), entry(mm('village_sickle'), 1)])]))
    w(f'{D}/loot_table/crates/village_copper_crate.json', table([valued_pool(5, (3, 5)), pool([entry(mm('village_copper_sword'), 2), entry(mm('village_copper_helmet'), 2),
        entry(mm('village_copper_chestplate'), 1), entry(mm('village_copper_knife'), 2), entry(mm('village_carving_knife'), 1), entry(mm('copper_gildan'), 3, 6, 18)])]))
    w(f'{D}/loot_table/crates/alpha_loot_box.json', table([valued_pool(10, (3, 5)), pool([entry(mm('alpha_leather'), 3, 1, 3), entry(mm('alpha_beef'), 3, 2, 5),
        entry(mm('alpha_wolf_fur'), 2, 1, 2), entry(mm('alpha_leather_chestplate'), 1), entry(mm('ram_horn'), 2, 1, 3)])]))
    # Unobtainables master crate: the creative-only stuff
    w(f'{D}/loot_table/crates/master_crate.json', table([pool([entry(V + 'budding_amethyst', 3), entry(V + 'bedrock', 3, 8, 16), entry(V + 'spawner', 2), entry(V + 'debug_stick', 1),
        entry(V + 'barrier', 2, 8, 16), entry(V + 'end_portal_frame', 2, 4, 12), entry(V + 'reinforced_deepslate', 2, 4, 12), entry(V + 'command_block', 1),
        entry(V + 'light', 2, 8, 16), entry(V + 'structure_void', 1, 8, 16), entry(V + 'trial_spawner', 1), entry(V + 'vault', 1), entry(mm('netherite_gildan'), 2),
        entry(mm('halo_of_life'), 1), entry(mm('halo_of_death'), 1)], (3, 5))]))
    w(f'{D}/loot_table/gameplay/req_card.json', table([pool([entry(mm('standard_ammunition'), 6, 4, 10), entry(mm('plasma_cell'), 3, 1, 3), entry(mm('frag_grenade'), 4, 2, 4),
        entry(mm('assault_rifle'), 1), entry(mm('battle_rifle'), 1), entry(mm('mark_v_helmet'), 1), entry(mm('sprint_module'), 1), entry(mm('drop_shield_module'), 1),
        entry(mm('steel_ingot'), 3, 2, 5), entry(mm('ammo_case'), 3)], (2, 4))]))
    w(f'{D}/loot_table/gameplay/magic_card_deck.json', table([pool([entry(mm('fire_card'), 5), entry(mm('ice_card'), 5), entry(mm('toxin_card'), 4), entry(mm('void_fire_card'), 3),
        entry(mm('xp_card'), 3), entry(mm('healing_card'), 4), entry(mm('golem_card'), 1)])]))
    # extra loot in vanilla chests via global loot modifiers
    w(f'{D}/loot_table/inject/dungeon.json', table([pool([entry(mm('copper_key'), 3), entry(mm('iron_key'), 2), entry(mm('blank_contract'), 3), entry(mm('silver_gildan'), 3, 2, 6),
        entry(mm('magic_card_deck'), 1), entry(mm('lock_pick'), 2), entry(mm('recall_token'), 1), {'type': 'minecraft:empty', 'weight': 6}], 2)]))
    w(f'{D}/loot_table/inject/village.json', table([pool([entry(mm('village_crate'), 2), entry(mm('fabric'), 3, 1, 4), entry(mm('copper_gildan'), 4, 3, 12), entry(mm('banana'), 2, 1, 3),
        entry(mm('blank_contract'), 2), {'type': 'minecraft:empty', 'weight': 6}])]))
    w(f'{D}/loot_table/inject/treasure.json', table([pool([entry(mm('gold_key'), 2), entry(mm('emerald_key'), 1), entry(mm('wanted_poster'), 2), entry(mm('gold_gildan'), 3, 1, 3),
        entry(mm('enchancer'), 1), entry(mm('ancient_dagger'), 1), entry(mm('req_card'), 2), {'type': 'minecraft:empty', 'weight': 8}], 2)]))
    glm = []
    for name, tables in [('dungeon', ['chests/simple_dungeon', 'chests/abandoned_mineshaft', 'chests/stronghold_corridor', 'chests/ruined_portal']),
                         ('village', ['chests/village/village_plains_house', 'chests/village/village_taiga_house', 'chests/village/village_savanna_house',
                                      'chests/village/village_desert_house', 'chests/village/village_snowy_house', 'chests/village/village_weaponsmith']),
                         ('treasure', ['chests/desert_pyramid', 'chests/jungle_temple', 'chests/woodland_mansion', 'chests/end_city_treasure',
                                       'chests/bastion_treasure', 'chests/ancient_city', 'chests/buried_treasure', 'chests/pillager_outpost'])]:
        for t in tables:
            key = f'inject_{name}_{t.replace("/", "_")}'
            w(f'{D}/loot_modifiers/{key}.json', {'type': 'neoforge:add_table', 'condition': {'type': 'neoforge:loot_table_id', 'loot_table_id': 'minecraft:' + t}, 'table': f'{M}inject/{name}'})
            glm.append(M + key)
    shutil.rmtree(f'{RES}/data/neoforge', ignore_errors=True)

# ------------------------------------------------------------------ villager trades
def trade(wants, gives, wcount=1, gcount=1, uses=8, xp=5, wants2=None, w2count=1):
    t = {'wants': {'id': wants, 'count': wcount}, 'gives': {'id': gives, 'count': gcount}, 'max_uses': uses, 'xp': xp, 'reputation_discount': 0.05}
    if wants2: t['additional_wants'] = {'id': wants2, 'count': w2count}
    return t

def trade_set(path, trades, amount):
    """path like 'quartermaster/level_1'."""
    ids = []
    for i, tr in enumerate(trades):
        tid = f'{path}/{i}'
        w(f'{D}/villager_trade/{tid}.json', tr)
        ids.append(M + tid)
    w(f'{D}/tags/villager_trade/{path}.json', {'replace': False, 'values': ids})
    w(f'{D}/trade_set/{path}.json', {'amount': min(amount, len(trades)), 'random_sequence': f'{M}trade_set/{path}', 'trades': f'#{M}{path}'})

E = V + 'emerald'
def gen_trades():
    # Quartermaster: the Emerald Empire supply list (EmeraldEmpireplans.txt), five items per level.
    qm = [('torch', 64, 1), ('chest', 4, 1), ('furnace', 4, 1), ('ladder', 32, 1), ('jukebox', 1, 4),
          ('item_frame', 8, 1), ('armor_stand', 2, 1), ('loom', 1, 1), ('composter', 2, 1), ('barrel', 4, 1),
          ('smoker', 1, 2), ('blast_furnace', 1, 3), ('cartography_table', 1, 2), ('fletching_table', 1, 2), ('grindstone', 1, 2),
          ('smithing_table', 1, 3), ('stonecutter', 1, 2), ('lantern', 16, 1), ('campfire', 4, 1), ('beehive', 1, 5),
          ('dispenser', 2, 2), ('observer', 2, 3), ('lectern', 1, 3), ('rail', 64, 2), ('cauldron', 1, 2)]
    for lvl in range(5):
        trade_set(f'quartermaster/level_{lvl + 1}', [trade(E, V + it, cost, cnt, 16, 2 + lvl * 3) for it, cnt, cost in qm[lvl * 5:lvl * 5 + 5]], 5)
    S, G, EG, AG, DG = mm('silver_gildan'), mm('gold_gildan'), mm('emerald_gildan'), mm('amethyst_gildan'), mm('diamond_gildan')
    trade_set('banker/level_1', [trade(E, S, 1, 1, 32, 1), trade(S, E, 2, 1, 32, 1), trade(S, mm('coin_pouch'), 4, 1, 4, 5)], 3)
    trade_set('banker/level_2', [trade(V + 'gold_ingot', S, 2, 1, 16, 5), trade(G, V + 'emerald', 1, 7, 16, 5), trade(S, mm('recall_token'), 3, 1, 8, 5)], 3)
    trade_set('banker/level_3', [trade(V + 'diamond', G, 1, 2, 12, 10), trade(G, mm('return_token'), 1, 2, 8, 10), trade(S, mm('coin_barrel'), 6, 1, 4, 10)], 3)
    trade_set('banker/level_4', [trade(EG, mm('imperial_charter'), 2, 1, 1, 20), trade(V + 'emerald_block', G, 1, 1, 16, 15)], 2)
    trade_set('banker/level_5', [trade(AG, mm('netherite_key'), 1, 1, 2, 30), trade(DG, mm('master_key'), 1, 1, 1, 30)], 2)
    trade_set('contractor/level_1', [trade(S, mm('blank_contract'), 1, 1, 16, 2), trade(mm('wolf_fur'), mm('copper_gildan'), 2, 5, 16, 2),
                                      trade(mm('creeper_leaves'), mm('copper_gildan'), 2, 5, 16, 2), trade(mm('thick_wool'), mm('copper_gildan'), 1, 3, 16, 2)], 3)
    trade_set('contractor/level_2', [trade(G, mm('rare_hunt_contract'), 2, 1, 6, 10), trade(S, mm('gather_contract'), 2, 1, 8, 5), trade(S, mm('flint_carving_knife'), 3, 1, 4, 5)], 3)
    trade_set('contractor/level_3', [trade(G, mm('wanted_poster'), 1, 1, 6, 15), trade(G, mm('scanner'), 2, 1, 2, 15), trade(G, mm('iron_carving_knife'), 1, 1, 4, 10)], 3)
    trade_set('contractor/level_4', [trade(EG, mm('legendary_hunt_contract'), 1, 1, 3, 20), trade(G, mm('iron_truncheon'), 2, 1, 4, 15)], 2)
    trade_set('contractor/level_5', [trade(EG, mm('golden_carving_knife'), 1, 1, 2, 30), trade(EG, mm('diamond_truncheon'), 1, 1, 2, 30)], 2)
    trade_set('bounty_buyer/level_1', [trade(mm('champion_trophy'), G, 1, 2, 16, 10), trade(S, mm('kill_card'), 2, 1, 8, 5)], 2)
    trade_set('bounty_buyer/level_2', [trade(mm('champion_trophy'), EG, 4, 1, 8, 15), trade(G, mm('wanted_poster'), 1, 1, 8, 10)], 2)
    trade_set('bounty_buyer/level_3', [trade(G, mm('iron_truncheon'), 2, 1, 4, 15), trade(G, mm('pocketball'), 1, 4, 8, 10), trade(G, mm('capture_card'), 1, 1, 8, 10)], 3)
    trade_set('bounty_buyer/level_4', [trade(EG, mm('midas_draught'), 1, 2, 4, 20), trade(EG, mm('req_card'), 1, 2, 4, 20)], 2)
    trade_set('bounty_buyer/level_5', [trade(EG, mm('cursed_pumpkin'), 3, 1, 1, 30), trade(EG, mm('golden_truncheon'), 1, 1, 2, 30)], 2)
    # Travelling merchants (bonfire visitors)
    pw = [mm(p[0] + '_powder') for p in POWDERS]
    trade_set('traveler/alchemist', [trade(S, p, 2, 1, 6, 1) for p in pw[:14]] + [trade(G, mm('seer_draught'), 1, 2, 4, 1), trade(G, mm('midas_draught'), 2, 1, 2, 1),
        trade(S, mm('antipotion'), 3, 1, 4, 1), trade(G, mm('mortis_cure'), 1, 1, 4, 1), trade(S, mm('recall_token'), 3, 1, 4, 1)], 7)
    trade_set('traveler/armorer', [trade(G, mm(f'{s}_{p}'), c, 1, 2, 1) for s, p, c in [('mystery', 'helmet', 2), ('mystery', 'boots', 2), ('steel', 'chestplate', 3),
        ('magma', 'helmet', 2), ('wetsuit', 'chestplate', 2), ('village_iron', 'chestplate', 1), ('ghillie', 'chestplate', 2), ('fox_hunter', 'helmet', 2),
        ('arctic', 'boots', 1), ('plumage', 'boots', 2), ('gambeson', 'chestplate', 1)]], 5)
    trade_set('traveler/armsman', [trade(G, mm(i), c, 1, 2, 1) for i, c in [('iron_katana', 2), ('iron_battleaxe', 2), ('iron_claymore', 2), ('steel_hammer', 2),
        ('iron_scythe', 2), ('iron_truncheon', 1)]] + [trade(S, mm('throwing_star'), 3, 8, 4, 1), trade(S, mm('standard_ammunition'), 2, 4, 6, 1),
        trade(EG, mm('assault_rifle'), 1, 1, 1, 1), trade(G, mm('frag_grenade'), 1, 3, 4, 1)], 6)
    trade_set('traveler/artificer', [trade(G, mm(c + '_calling'), 3, 1, 1, 1) for c in CALLINGS] + [trade(G, mm('iron_magnifier'), 1, 1, 2, 1),
        trade(G, mm('grapple_hook'), 2, 1, 2, 1), trade(EG, mm('jet_pack_module'), 1, 1, 1, 1), trade(G, mm('enchanted_quill'), 2, 1, 2, 1),
        trade(G, mm('magic_card_deck'), 2, 1, 2, 1), trade(EG, mm('halo_of_light'), 3, 1, 1, 1)], 6)
    trade_set('traveler/banker', [trade(E, S, 1, 1, 32, 1), trade(S, E, 2, 1, 32, 1), trade(S, mm('coin_pouch'), 4, 1, 2, 1), trade(G, mm('return_token'), 1, 2, 4, 1),
        trade(EG, mm('imperial_charter'), 3, 1, 1, 1)], 5)
    trade_set('traveler/locksmith', [trade(S, mm('lock_pick'), 1, 2, 8, 1), trade(S, mm('copper_key'), 2, 1, 6, 1), trade(G, mm('iron_key'), 1, 1, 4, 1),
        trade(G, mm('gold_key'), 2, 1, 2, 1), trade(G, mm('village_crate'), 1, 1, 2, 1), trade(G, mm('copper_crate'), 2, 1, 2, 1),
        trade(EG, mm('magical_key'), 1, 1, 1, 1), trade(EG, mm('emerald_key'), 1, 1, 2, 1)], 6)

# ------------------------------------------------------------------ tags
def tag(kind, name, values, ns=NS):
    base = D if ns == NS else f'{RES}/data/{ns}'
    w(f'{base}/tags/{kind}/{name}.json', {'replace': False, 'values': values})

def gen_tags():
    E_ = lambda *n: [M + x for x in n]
    tag('entity_type', 'faction/illager_host', ['#minecraft:illager', 'minecraft:ravager', 'minecraft:vex', 'minecraft:witch'] + E_('illager_hero', 'technomancer', 'summoner',
        'stone_golem', 'redstone_minion', 'redstone_turret', 'redstone_nexus'))
    tag('entity_type', 'faction/mortis_blight', ['#minecraft:undead'] + E_('mortis_zombie', 'mortis_zombie_pigman', 'mortis_arm', 'mortis_chicken', 'mortis_cow', 'mortis_pig',
        'mortis_sheep', 'mortis_egg', 'zombie_trudger', 'atlas_zombie', 'locust', 'locust_swarm', 'locust_horde'))
    tag('entity_type', 'faction/unholy', ['minecraft:phantom', 'minecraft:enderman', 'minecraft:endermite'] + E_('herobrine', 'herobrine_cultist', 'headless_horseman', 'horseless_headman',
        'living_head', 'proxy'))
    tag('entity_type', 'faction/nether', ['minecraft:piglin', 'minecraft:piglin_brute', 'minecraft:blaze', 'minecraft:magma_cube', 'minecraft:hoglin', 'minecraft:ghast',
        'minecraft:wither_skeleton', 'minecraft:zoglin'] + E_('overworld_piglin', 'lava_slime', 'netherrack_slime', 'soul_slime', 'netherite_slime'))
    tag('entity_type', 'faction/wilds', ['minecraft:spider', 'minecraft:cave_spider', 'minecraft:creeper', 'minecraft:slime', 'minecraft:silverfish', 'minecraft:breeze'] +
        E_('zephyr', 'iron_mimic') + [M + s[0] + '_slime' for s in entities.SLIMES if s[0] not in ('lava', 'netherrack', 'soul', 'netherite')])
    tag('entity_type', 'constructs', ['minecraft:iron_golem', 'minecraft:snow_golem', 'minecraft:copper_golem'] + E_('redstone_golem', 'steel_golem', 'stone_golem', 'iron_mimic',
        'redstone_minion', 'redstone_turret', 'redstone_nexus', 'emerald_nexus', 'llama_turret', 'summoner'))
    tag('entity_type', 'bosses', E_('zombie_trudger', 'atlas_zombie', 'herobrine', 'headless_horseman'), 'c')
    tag('block', 'climbable', [M + 'rope'], 'minecraft')
    tag('block', 'chiselable', ['#minecraft:base_stone_overworld', 'minecraft:cobblestone', 'minecraft:cobbled_deepslate', 'minecraft:sandstone', 'minecraft:red_sandstone',
        'minecraft:calcite', 'minecraft:blackstone', 'minecraft:basalt', 'minecraft:netherrack', 'minecraft:end_stone', 'minecraft:obsidian', 'minecraft:quartz_block',
        'minecraft:mossy_cobblestone', 'minecraft:stone_bricks', 'minecraft:smooth_stone'])
    tag('block', 'infiniburn_overworld', [M + 'charred_log'], 'minecraft')
    tag('item', 'gildans', [M + g for g in ('copper_gildan', 'silver_gildan', 'gold_gildan', 'emerald_gildan', 'amethyst_gildan', 'diamond_gildan', 'netherite_gildan')])
    tag('item', 'powders', [M + p[0] + '_powder' for p in POWDERS])
    tag('item', 'carving_knives', [M + i['id'] for i in ITEMS if 'CarvingKnifeItem' in i['ctor']])
    # tool materials
    tag('item', 'steel_tool_materials', [M + 'steel_ingot']); tag('item', 'ruby_tool_materials', [M + 'ruby'])
    tag('item', 'emerald_tool_materials', ['minecraft:emerald']); tag('item', 'bedrock_tool_materials', [M + 'bedrock_dust'])
    tag('item', 'astroite_tool_materials', [M + 'astroite_chunk']); tag('item', 'plastic_tool_materials', [M + 'plastic_gem'])
    tag('item', 'ancient_tool_materials', ['minecraft:netherite_scrap'])
    # make our gear enchantable / recognisable
    weapons = [i['id'] for i in ITEMS if 'MashWeaponItem' in i['ctor'] or i['id'].endswith('_sword')]
    tag('item', 'swords', [M + x for x in weapons], 'minecraft')
    tag('item', 'pickaxes', [M + i['id'] for i in ITEMS if i['id'].endswith('_pickaxe')], 'minecraft')
    tag('item', 'axes', [M + i['id'] for i in ITEMS if i['id'].endswith('_axe')], 'minecraft')
    tag('item', 'shovels', [M + i['id'] for i in ITEMS if i['id'].endswith('_shovel')], 'minecraft')
    tag('item', 'hoes', [M + i['id'] for i in ITEMS if i['id'].endswith('_hoe')], 'minecraft')
    pieces = {'H': 'head_armor', 'C': 'chest_armor', 'L': 'leg_armor', 'B': 'foot_armor'}
    for p, t in pieces.items():
        vals = [M + a['key'] + '_' + {'H': 'helmet', 'C': 'chestplate', 'L': 'leggings', 'B': 'boots'}[p] for a in ARMOR if p in a['pieces']]
        if p == 'H': vals += [M + 'halo_of_' + h[0] for h in HALOS]
        tag('item', t, vals, 'minecraft')
    tag('item', 'piglin_safe_armor', [M + 'hoglord_' + x for x in ('helmet', 'chestplate', 'leggings', 'boots')] + [M + 'gold_full_plate_' + x for x in ('helmet', 'chestplate', 'boots')], 'minecraft')
    tag('item', 'piglin_loved', [M + 'gold_gildan'], 'minecraft')
    tag('point_of_interest_type', 'acquirable_job_site', [M + 'banker', M + 'contractor', M + 'bounty_buyer', M + 'quartermaster'], 'minecraft')

# ------------------------------------------------------------------ recipes
R = {}
def shaped(name, pattern, key, result, count=1, cat='misc'):
    k = {c: v for c, v in key.items()}
    R[name] = {'type': 'minecraft:crafting_shaped', 'category': cat, 'pattern': pattern, 'key': k, 'result': {'id': result, 'count': count}}
def shapeless(name, ingredients, result, count=1, cat='misc'):
    R[name] = {'type': 'minecraft:crafting_shapeless', 'category': cat, 'ingredients': ingredients, 'result': {'id': result, 'count': count}}
def smelt(name, ing, result, xp=0.2, kind='smelting', time=200):
    R[name] = {'type': f'minecraft:{kind}', 'cookingtime': time, 'experience': xp, 'ingredient': ing, 'result': {'id': result}}
def smithing(name, base, addition, result, template='minecraft:netherite_upgrade_smithing_template'):
    R[name] = {'type': 'minecraft:smithing_transform', 'template': template, 'base': base, 'addition': addition, 'result': {'id': result}}

def gen_recipes():
    g = ['copper_gildan', 'silver_gildan', 'gold_gildan', 'emerald_gildan', 'amethyst_gildan', 'diamond_gildan', 'netherite_gildan']
    for a, b in zip(g, g[1:]):
        shapeless(f'{b}_from_{a}', [M + a] * 9, M + b)
        shapeless(f'{a}_from_{b}', [M + b], M + a, 9)
    shaped('coin_pouch', [' S ', 'L L', ' L '], {'S': 'minecraft:string', 'L': 'minecraft:leather'}, M + 'coin_pouch')
    shapeless('crushed_coal', ['minecraft:coal'], M + 'crushed_coal', 2)
    shapeless('steel_ingot', ['minecraft:iron_ingot', M + 'crushed_coal', M + 'crushed_coal'], M + 'steel_ingot')
    shapeless('steel_ingot_from_nuggets', [M + 'steel_nugget'] * 9, M + 'steel_ingot')
    shapeless('steel_nugget', [M + 'steel_ingot'], M + 'steel_nugget', 9)
    for blk, ing in [('steel_block', 'steel_ingot'), ('ruby_block', 'ruby'), ('sapphire_block', 'sapphire'), ('wither_bone_block', 'wither_bone')]:
        shaped(blk, ['XXX', 'XXX', 'XXX'], {'X': M + ing}, M + blk)
        shapeless(f'{ing}_from_{blk}', [M + blk], M + ing, 9)
    # mystery mineral refinement: three of a form make the next form of the same family
    fams = {}
    for iid, fam, trait, lvl in MINERAL_IDS: fams.setdefault(fam, {})[lvl] = iid
    for fam, forms in fams.items():
        for lvl in (1, 2):
            if forms[lvl] != forms[lvl + 1]: shapeless(f'refine_{forms[lvl + 1]}', [M + forms[lvl]] * 3, M + forms[lvl + 1])
    # weapon classes
    mats = {'wooden': '#minecraft:planks', 'stone': '#minecraft:stone_tool_materials', 'copper': 'minecraft:copper_ingot', 'iron': 'minecraft:iron_ingot',
            'golden': 'minecraft:gold_ingot', 'diamond': 'minecraft:diamond', 'steel': M + 'steel_ingot'}
    pats = {'club': [' XX', ' XX', 'S  '], 'hammer': ['XXX', 'XXX', ' S '], 'battleaxe': ['XSX', 'XSX', ' S '], 'katana': ['  X', ' X ', 'L  '],
            'truncheon': ['  X', ' S ', 'L  '], 'knife': [' X', 'S '], 'claymore': [' X ', ' X ', 'XSX'], 'scythe': ['XXS', '  S', '  S']}
    for cls, pat in pats.items():
        for mk, mi in mats.items():
            key = {'X': mi, 'S': 'minecraft:stick'}
            if 'L' in ''.join(pat): key['L'] = 'minecraft:leather'
            key = {k: v for k, v in key.items() if k in ''.join(pat)}
            shaped(f'{mk}_{cls}', pat, key, mm(f'{mk}_{cls}'), cat='equipment')
        smithing(f'netherite_{cls}', mm(f'diamond_{cls}'), '#minecraft:netherite_tool_materials', mm(f'netherite_{cls}'))
    shaped('emerald_scythe', ['XXS', '  S', '  S'], {'X': 'minecraft:emerald', 'S': 'minecraft:stick'}, M + 'emerald_scythe', cat='equipment')
    shaped('ruby_scythe', ['XXS', '  S', '  S'], {'X': M + 'ruby', 'S': 'minecraft:stick'}, M + 'ruby_scythe', cat='equipment')
    # ore tool sets
    tp = {'sword': [' X ', ' X ', ' S '], 'pickaxe': ['XXX', ' S ', ' S '], 'axe': ['XX ', 'XS ', ' S '], 'shovel': [' X ', ' S ', ' S '], 'hoe': ['XX ', ' S ', ' S ']}
    for tk, tm in [('steel', M + 'steel_ingot'), ('ruby', M + 'ruby'), ('astroite', M + 'astroite_chunk'), ('bedrock', M + 'bedrock_dust')]:
        for tt, pat in tp.items():
            shaped(f'{tk}_{tt}', pat, {'X': tm, 'S': 'minecraft:stick' if tk != 'bedrock' else 'minecraft:blaze_rod'}, mm(f'{tk}_{tt}'), cat='equipment')
    # armor
    ap = {'H': ['XXX', 'X X'], 'C': ['X X', 'XXX', 'XXX'], 'L': ['XXX', 'X X', 'X X'], 'B': ['X X', 'X X']}
    skip = {'village_copper', 'village_iron', 'mystery', 'herobrine'}
    names = {'H': 'helmet', 'C': 'chestplate', 'L': 'leggings', 'B': 'boots'}
    for a in ARMOR:
        if a['key'] in skip: continue
        mat = a['stats'][5]
        for p in a['pieces']:
            shaped(f"{a['key']}_{names[p]}", ap[p], {'X': mat}, mm(f"{a['key']}_{names[p]}"), cat='equipment')
    for p in 'HCLB':
        hp = {'H': ['XXX', 'XOX'], 'C': ['XOX', 'XXX', 'XXX'], 'L': ['XXX', 'XOX', 'X X'], 'B': ['XOX', 'X X']}[p]
        shaped(f'herobrine_{names[p]}', hp, {'X': 'minecraft:obsidian', 'O': M + 'herobrine_charm'}, mm(f'herobrine_{names[p]}'), cat='equipment')
        shaped(f'mystery_{names[p]}', ap[p], {'X': M + 'chainmail_sheet'}, mm(f'mystery_{names[p]}'), cat='equipment')
    shaped('chainmail_sheet', ['NN', 'NN'], {'N': 'minecraft:iron_nugget'}, M + 'chainmail_sheet', 2)
    halo_items = {'explosions': 'minecraft:tnt', 'thorns': 'minecraft:cactus', 'power': 'minecraft:blaze_powder', 'fire': M + 'magma_scales', 'poison': M + 'toxic_string',
                  'light': 'minecraft:glowstone', 'darkness': 'minecraft:ink_sac', 'healing': 'minecraft:ghast_tear', 'invisibility': M + 'illusory_cloth',
                  'water': 'minecraft:prismarine_crystals', 'swiftness': 'minecraft:sugar', 'gravity': 'minecraft:phantom_membrane', 'might': 'minecraft:iron_block',
                  'life': 'minecraft:golden_apple', 'death': M + 'withered_soul'}
    for h, it in halo_items.items():
        shaped(f'halo_of_{h}', [' G ', 'GXG', ' G '], {'G': 'minecraft:gold_ingot', 'X': it}, mm(f'halo_of_{h}'), cat='equipment')
    # knives, magnifiers, keys, tools
    for kid, k_mat in [('flint_carving_knife', 'minecraft:flint'), ('copper_carving_knife', 'minecraft:copper_ingot'), ('iron_carving_knife', 'minecraft:iron_ingot'),
                       ('salmon_carving_knife', M + 'strong_salmon_scale'), ('golden_carving_knife', 'minecraft:gold_ingot'), ('diamond_carving_knife', 'minecraft:diamond')]:
        shaped(kid, [' X', 'S '], {'X': k_mat, 'S': 'minecraft:bone'}, mm(kid), cat='equipment')
    smithing('netherite_carving_knife', mm('diamond_carving_knife'), '#minecraft:netherite_tool_materials', mm('netherite_carving_knife'))
    for mid, mmat in [('wooden_magnifier', '#minecraft:planks'), ('copper_magnifier', 'minecraft:copper_ingot'), ('iron_magnifier', 'minecraft:iron_ingot'),
                      ('golden_magnifier', 'minecraft:gold_ingot'), ('bee_magnifier', M + 'bee_lense')]:
        shaped(mid, [' X ', 'XGX', 'S  '], {'X': mmat, 'G': 'minecraft:glass_pane', 'S': 'minecraft:stick'}, mm(mid), cat='equipment')
    smithing('netherite_magnifier', mm('golden_magnifier'), '#minecraft:netherite_tool_materials', mm('netherite_magnifier'))
    shaped('village_sickle', [' XX', '  X', ' S '], {'X': 'minecraft:iron_ingot', 'S': 'minecraft:stick'}, M + 'village_sickle', cat='equipment')
    for kid, kmat in [('rusted_key', 'minecraft:iron_nugget'), ('copper_key', 'minecraft:copper_ingot'), ('iron_key', 'minecraft:iron_ingot'), ('gold_key', 'minecraft:gold_ingot'),
                      ('emerald_key', 'minecraft:emerald'), ('amethyst_key', 'minecraft:amethyst_shard'), ('diamond_key', 'minecraft:diamond')]:
        shaped(kid, ['XX', 'X ', 'X '], {'X': kmat}, mm(kid), cat='equipment')
    smithing('netherite_key', mm('diamond_key'), '#minecraft:netherite_tool_materials', mm('netherite_key'))
    shaped('lock_pick', ['N', 'S'], {'N': 'minecraft:iron_nugget', 'S': 'minecraft:stick'}, M + 'lock_pick', 2, cat='equipment')
    shaped('grapple_hook', [' IT', ' SI', 'S  '], {'I': 'minecraft:iron_ingot', 'T': 'minecraft:tripwire_hook', 'S': 'minecraft:string'}, M + 'grapple_hook', cat='equipment')
    shapeless('air_hook', [M + 'grapple_hook', M + 'zephyr_wisp', 'minecraft:feather'], M + 'air_hook', cat='equipment')
    shaped('rope', ['S', 'S', 'S'], {'S': 'minecraft:string'}, M + 'rope', 3)
    shaped('brick_blade', [' P ', ' P ', ' S '], {'P': M + 'plastic_gem', 'S': 'minecraft:stick'}, M + 'brick_blade', cat='equipment')
    shapeless('phantom_shovel', ['minecraft:diamond_shovel', 'minecraft:phantom_membrane', 'minecraft:phantom_membrane'], M + 'phantom_shovel', cat='equipment')
    shapeless('enchanted_flint_and_steel', ['minecraft:flint_and_steel', M + 'herobrine_charm'], M + 'enchanted_flint_and_steel', cat='equipment')
    shapeless('fireball_bow', ['minecraft:bow', M + 'fibrous_flame', 'minecraft:fire_charge'], M + 'fireball_bow', cat='equipment')
    shapeless('lightning_bow', ['minecraft:bow', 'minecraft:lightning_rod', M + 'flavum_ingot'], M + 'lightning_bow', cat='equipment')
    shapeless('freezing_bow', ['minecraft:bow', 'minecraft:packed_ice', M + 'carulem_ingot'], M + 'freezing_bow', cat='equipment')
    shaped('wooden_dagger', ['X', 'S'], {'X': '#minecraft:planks', 'S': 'minecraft:stick'}, M + 'wooden_dagger', cat='equipment')
    for cname, ctex in CHISELS:
        cm = {'andesite': 'minecraft:andesite', 'calcite': 'minecraft:calcite', 'diorite': 'minecraft:diorite', 'granite': 'minecraft:granite', 'tuff': 'minecraft:tuff',
              'sandstone': 'minecraft:sandstone', 'deepslate': 'minecraft:cobbled_deepslate', 'blackstone': 'minecraft:blackstone', 'basalt': 'minecraft:basalt',
              'quartz': 'minecraft:quartz', 'obsidian': 'minecraft:obsidian', 'end_stone': 'minecraft:end_stone', 'netherrack': 'minecraft:netherrack',
              'stone': 'minecraft:stone', 'mossy_cobblestone': 'minecraft:mossy_cobblestone', 'diamond': 'minecraft:diamond'}[cname]
        shaped(f'{cname}_chisel', ['XI', 'S '], {'X': cm, 'I': 'minecraft:iron_nugget', 'S': 'minecraft:stick'}, mm(f'{cname}_chisel'), cat='equipment')
    # tech
    shaped('assault_rifle', ['SSS', 'RCG', '  S'], {'S': M + 'steel_ingot', 'R': 'minecraft:redstone', 'C': M + 'redstone_core', 'G': 'minecraft:gunpowder'}, M + 'assault_rifle', cat='equipment')
    shaped('battle_rifle', ['SSS', 'GCS', 'S  '], {'S': M + 'steel_ingot', 'G': 'minecraft:spyglass', 'C': M + 'redstone_core'}, M + 'battle_rifle', cat='equipment')
    shaped('beam_rifle', ['PPP', 'GCS', 'S  '], {'P': M + 'plasma_cell', 'S': M + 'steel_ingot', 'G': 'minecraft:spyglass', 'C': M + 'redstone_core'}, M + 'beam_rifle', cat='equipment')
    shapeless('standard_ammunition', [M + 'steel_nugget', 'minecraft:gunpowder'], M + 'standard_ammunition', 4)
    shapeless('plasma_cell', [M + 'steel_ingot', 'minecraft:redstone', 'minecraft:glowstone_dust'], M + 'plasma_cell', 2)
    shapeless('frag_grenade', [M + 'steel_nugget', 'minecraft:gunpowder', 'minecraft:gunpowder'], M + 'frag_grenade', 2)
    for mod, extra in [('active_camo', M + 'illusory_cloth'), ('armor_lock', 'minecraft:obsidian'), ('drop_shield', 'minecraft:glass'), ('jet_pack', 'minecraft:firework_rocket'), ('sprint', 'minecraft:sugar')]:
        shaped(f'{mod}_module', ['SRS', 'SXS', 'SSS'], {'S': M + 'steel_ingot', 'R': M + 'redstone_core', 'X': extra}, mm(f'{mod}_module'), cat='equipment')
    # arcana
    pmap = {'aquarius': ['sanatia', 'minecraft:prismarine_crystals'], 'alsus': ['celerus', 'minecraft:feather'], 'illusio': ['patet', 'minecraft:fermented_spider_eye'],
            'volare': ['alsus', 'minecraft:phantom_membrane', 'minecraft:diamond'], 'felirus': ['vistren', 'minecraft:wheat'], 'lumia': ['patet', 'minecraft:glowstone_dust'],
            'rulsevia': ['statia', 'minecraft:piston'], 'sumonia': ['luxine', 'minecraft:emerald'], 'arcanus': ['serenus', 'minecraft:milk_bucket'],
            'animus': ['mortem', 'minecraft:soul_sand'], 'gravitas': ['statia', 'minecraft:iron_ingot', 'minecraft:redstone'], 'viridia': ['sanatia', 'minecraft:bone_meal'],
            'chroma': ['vistren', 'celerus', 'armis'], 'dissolvo': ['mortem', 'minecraft:fermented_spider_eye']}
    for p, ing in pmap.items():
        shapeless(f'{p}_powder', [i if ':' in i else M + i + '_powder' for i in ing], mm(f'{p}_powder'), 2 if p != 'volare' else 1)
    for cname, extra in [('fire', 'minecraft:blaze_powder'), ('ice', 'minecraft:packed_ice'), ('toxin', M + 'toxic_string'), ('void_fire', 'minecraft:ender_pearl'),
                         ('xp', 'minecraft:experience_bottle'), ('healing', 'minecraft:glistering_melon_slice'), ('golem', M + 'steel_ingot')]:
        shapeless(f'{cname}_card', ['minecraft:paper', 'minecraft:lapis_lazuli', extra], mm(f'{cname}_card'), 2)
    shaped('magic_card_deck', ['PPP', 'PLP', 'PPP'], {'P': 'minecraft:paper', 'L': 'minecraft:lapis_block'}, M + 'magic_card_deck')
    shapeless('enchanted_quill', [M + 'stiff_feather', 'minecraft:ink_sac', 'minecraft:amethyst_shard'], M + 'enchanted_quill')
    shapeless('lucky_shuriken', [M + 'bizarre_star', 'minecraft:iron_ingot'], M + 'lucky_shuriken', 4)
    shapeless('throwing_star', [M + 'steel_nugget', M + 'steel_nugget', M + 'steel_nugget', M + 'steel_nugget'], M + 'throwing_star', 4)
    shapeless('pop_rock_throwable', [M + 'pop_rock', 'minecraft:magma_cream'], M + 'pop_rock_throwable', 2)
    shaped('pocketball', [' R ', 'NBN', ' W '], {'R': 'minecraft:red_dye', 'N': 'minecraft:iron_nugget', 'B': 'minecraft:stone_button', 'W': 'minecraft:white_dye'}, M + 'pocketball', 2)
    shaped('wolf_totem', [' F ', 'BGB', ' F '], {'F': M + 'alpha_wolf_fur', 'B': 'minecraft:bone', 'G': 'minecraft:gold_ingot'}, M + 'wolf_totem')
    shapeless('creeper_totem', [M + 'charged_leaves', 'minecraft:tnt', 'minecraft:stick'], M + 'creeper_totem')
    shaped('effigy', [' C ', 'CHC', ' S '], {'C': M + 'unholy_cloth', 'H': 'minecraft:hay_block', 'S': 'minecraft:string'}, M + 'effigy')
    shapeless('antipotion', ['minecraft:splash_potion', 'minecraft:milk_bucket', M + 'arcanus_powder'], M + 'antipotion')
    shapeless('seer_draught', ['minecraft:glass_bottle', M + 'bee_lense', 'minecraft:glowstone_dust'], M + 'seer_draught')
    shapeless('midas_draught', ['minecraft:glass_bottle', 'minecraft:gold_block', M + 'fortuna_powder'], M + 'midas_draught')
    shapeless('mortis_cure', ['minecraft:glass_bottle', 'minecraft:glistering_melon_slice', M + 'mortis_flesh', M + 'sanatia_powder'], M + 'mortis_cure')
    shapeless('zombie_plague_vial', ['minecraft:glass_bottle', M + 'mortis_flesh', M + 'mortis_flesh'], M + 'zombie_plague_vial')
    shapeless('air_bubble', ['minecraft:glass_bottle', 'minecraft:prismarine_crystals'], M + 'air_bubble', 2)
    shapeless('ritual_candle', ['minecraft:candle', M + 'herobrine_charm'], M + 'ritual_candle', 4)
    shapeless('cursed_pumpkin', ['minecraft:carved_pumpkin', M + 'withered_soul', 'minecraft:soul_torch'], M + 'cursed_pumpkin')
    for cname in CALLINGS:
        extra = {'lumberjack': 'minecraft:iron_axe', 'jackhammer': 'minecraft:iron_pickaxe', 'fireman': 'minecraft:water_bucket', 'poseidon': 'minecraft:prismarine_shard',
                 'werewolf': M + 'wolf_fur', 'frosty': 'minecraft:packed_ice', 'worm': 'minecraft:dirt', 'cultivator': 'minecraft:wheat_seeds', 'fletcher': 'minecraft:flint',
                 'necro': 'minecraft:bone', 'stomper': 'minecraft:iron_block', 'turtle': 'minecraft:turtle_scute', 'vampire': M + 'bloodied_cloth', 'viper': 'minecraft:spider_eye',
                 'snail': 'minecraft:slime_ball', 'kangaroo': 'minecraft:rabbit_foot', 'boxer': 'minecraft:leather', 'berserker': 'minecraft:blaze_powder',
                 'cannibal': 'minecraft:rotten_flesh', 'beastmaster': 'minecraft:lead', 'miner': 'minecraft:apple', 'monster': M + 'creeper_leaves'}[cname]
        shapeless(f'{cname}_calling', ['minecraft:paper', 'minecraft:feather', 'minecraft:ink_sac', 'minecraft:emerald', extra], mm(f'{cname}_calling'))
    # empire
    shapeless('recall_token', ['minecraft:ender_pearl', 'minecraft:gold_nugget', M + 'embers'], M + 'recall_token', 2)
    shapeless('return_token', ['minecraft:ender_pearl', 'minecraft:lapis_lazuli', M + 'embers'], M + 'return_token', 2)
    shapeless('embers', ['minecraft:blaze_powder', 'minecraft:coal'], M + 'embers', 2)
    shaped('bonfire_kit', [' E ', 'LCL', 'LLL'], {'E': M + 'embers', 'L': '#minecraft:logs', 'C': 'minecraft:campfire'}, M + 'bonfire_kit')
    shapeless('blank_contract', ['minecraft:paper', 'minecraft:ink_sac', M + 'copper_gildan'], M + 'blank_contract')
    shaped('scanner', ['IGI', 'IRI', 'IBI'], {'I': 'minecraft:iron_ingot', 'G': 'minecraft:glass_pane', 'R': 'minecraft:redstone', 'B': 'minecraft:stone_button'}, M + 'scanner')
    shaped('renown_ledger', ['E', 'B'], {'E': 'minecraft:emerald', 'B': 'minecraft:book'}, M + 'renown_ledger')
    shaped('quest_journal', ['A', 'B'], {'A': 'minecraft:amethyst_shard', 'B': 'minecraft:book'}, M + 'quest_journal')
    shaped('combat_dummy', [' H ', 'SAS', ' S '], {'H': 'minecraft:hay_block', 'S': 'minecraft:stick', 'A': 'minecraft:armor_stand'}, M + 'combat_dummy')
    shaped('emerald_nexus_core', ['EGE', 'GCG', 'EGE'], {'E': 'minecraft:emerald_block', 'G': M + 'gold_gildan', 'C': M + 'redstone_core'}, M + 'emerald_nexus_core')
    shaped('llama_turret_kit', ['HLH', 'HDH'], {'H': 'minecraft:hay_block', 'L': 'minecraft:lead', 'D': 'minecraft:dispenser'}, M + 'llama_turret_kit')
    shaped('contract_board', ['PPP', 'PCP', 'PPP'], {'P': 'minecraft:spruce_planks', 'C': M + 'blank_contract'}, M + 'contract_board')
    shaped('bounty_board', ['PPP', 'PSP', 'PPP'], {'P': 'minecraft:dark_oak_planks', 'S': 'minecraft:iron_sword'}, M + 'bounty_board')
    shaped('imperial_supply_crate', ['PEP', 'EBE', 'PEP'], {'P': '#minecraft:planks', 'E': 'minecraft:emerald', 'B': 'minecraft:barrel'}, M + 'imperial_supply_crate')
    shaped('coin_barrel', [' G ', 'GBG', ' G '], {'G': M + 'gold_gildan', 'B': 'minecraft:barrel'}, M + 'coin_barrel')
    shaped('vending_machine', ['IGI', 'IRI', 'ISI'], {'I': 'minecraft:iron_ingot', 'G': 'minecraft:glass', 'R': 'minecraft:redstone_block', 'S': M + 'silver_gildan'}, M + 'vending_machine')
    shaped('redstone_activator', ['CCC', 'ROR', 'CCC'], {'C': 'minecraft:cobblestone', 'R': 'minecraft:redstone', 'O': 'minecraft:observer'}, M + 'redstone_activator')
    shaped('continuum_block', ['SOS', 'OEO', 'SOS'], {'S': M + 'spectral_scales', 'O': 'minecraft:obsidian', 'E': 'minecraft:ender_eye'}, M + 'continuum_block')
    shaped('redstone_core_block', ['RRR', 'RCR', 'RRR'], {'R': 'minecraft:redstone', 'C': M + 'redstone_core'}, M + 'redstone_core_block')
    # blocks
    shaped('fur_block', ['FF', 'FF'], {'F': M + 'wolf_fur'}, M + 'fur_block')
    shaped('fur_carpet', ['FF'], {'F': M + 'fur_block'}, M + 'fur_carpet', 3)
    shaped('magma_carpet', ['MM'], {'M': M + 'magma_scales'}, M + 'magma_carpet', 3)
    shaped('evocation_carpet', ['UU'], {'U': M + 'unholy_cloth'}, M + 'evocation_carpet', 3)
    shaped('endermite_mucus_layer', ['MM'], {'M': M + 'endermite_mucus'}, M + 'endermite_mucus_layer', 3)
    shaped('padding', ['FWF', 'WFW', 'FWF'], {'F': 'minecraft:feather', 'W': '#minecraft:wool'}, M + 'padding', 4)
    shaped('wise_hedge', ['LCL', 'C C', 'LCL'], {'L': '#minecraft:leaves', 'C': M + 'creeper_leaves'}, M + 'wise_hedge', 4)
    shaped('spike_mat', ['NSN'], {'N': 'minecraft:iron_nugget', 'S': M + 'salmon_scale'}, M + 'spike_mat', 3)
    shapeless('toxic_web', ['minecraft:cobweb', M + 'toxic_string'], M + 'toxic_web')
    shaped('thick_wool', ['WW', 'WW'], {'W': '#minecraft:wool'}, M + 'thick_wool')
    smelt('charred_log', '#minecraft:logs_that_burn', M + 'charred_log', 0.1, 'smoking', 200)
    shapeless('charred_planks', [M + 'charred_log'], M + 'charred_planks', 4)
    shapeless('bog_oak_planks', [M + 'bog_oak_log'], 'minecraft:oak_planks', 4)
    shapeless('bog_oak_planks_dark', [M + 'bog_oak_log', 'minecraft:mud'], M + 'bog_oak_planks', 4)
    shaped('bog_oak_log', ['LM'], {'L': 'minecraft:oak_log', 'M': 'minecraft:mud'}, M + 'bog_oak_log')
    shaped('packed_reeds', ['RRR', 'RRR', 'RRR'], {'R': 'minecraft:sugar_cane'}, M + 'packed_reeds')
    shaped('asteroid_bricks', ['AA', 'AA'], {'A': M + 'astroite'}, M + 'asteroid_bricks', 4)
    smelt('polished_astroite', M + 'rough_astroite', M + 'polished_astroite')
    shaped('blood_ruby_block', ['RGR', 'GBG', 'RGR'], {'R': M + 'ruby', 'G': 'minecraft:glass', 'B': 'minecraft:redstone_block'}, M + 'blood_ruby_block')
    shaped('crumbling_stone', ['CG', 'GC'], {'C': 'minecraft:cobblestone', 'G': 'minecraft:gravel'}, M + 'crumbling_stone', 4)
    dyes = {'white': 'white', 'light_gray': 'light_gray', 'dark_gray': 'gray', 'black': 'black', 'red': 'red', 'dark_red': 'brown', 'orange': 'orange', 'yellow': 'yellow',
            'lime': 'lime', 'green': 'green', 'sand_green': 'cyan', 'cyan': 'cyan', 'light_blue': 'light_blue', 'blue': 'blue', 'purple': 'purple', 'pink': 'pink',
            'nougat': 'orange', 'reddish_brown': 'brown'}
    for tk, tc in TOY:
        extra = ['minecraft:brown_dye'] if tk in ('nougat', 'sand_green', 'dark_red') else []
        shapeless(f'{tk}_toy_brick', [M + 'plastic_gem', f'minecraft:{dyes[tk]}_dye'] + extra, mm(f'{tk}_toy_brick'), 4)
    shapeless('glow_toy_brick', [M + 'plastic_gem', 'minecraft:glow_ink_sac'], M + 'glow_toy_brick', 4)
    # food & smelting
    smelt('alpha_steak', M + 'alpha_beef', M + 'alpha_steak', 0.35)
    smelt('alpha_steak_smoking', M + 'alpha_beef', M + 'alpha_steak', 0.35, 'smoking', 100)
    smelt('ruby_from_ore', M + 'ruby_ore', M + 'ruby', 1.0); smelt('ruby_from_deepslate_ore', M + 'deepslate_ruby_ore', M + 'ruby', 1.0)
    smelt('steel_from_blasting', M + 'crushed_coal', M + 'steel_nugget', 0.1, 'blasting', 100)
    shapeless('fabric', [M + 'black_cloth'], M + 'fabric', 2)
    shapeless('fabric_from_wool', ['#minecraft:wool', 'minecraft:string'], M + 'fabric', 2)
    for name, r in R.items():
        w(f'{D}/recipe/{name}.json', r)
    return len(R)

# ------------------------------------------------------------------ brewing
def gen_brewing():
    potions = [('midas_touch', 'minecraft:gold_block'), ('frozen', 'minecraft:packed_ice'), ('void_fire', M + 'spectral_scales'),
               ('zombie_plague', M + 'mortis_flesh'), ('health_penalty', M + 'wither_bone'), ('detect_ores', M + 'bee_lense'), ('undeath', M + 'unholy_cloth')]
    n = 0
    for pot, reagent in potions:
        for kind in ('potion', 'splash_potion', 'lingering_potion'):
            w(f'{D}/recipe/brewing/{kind}_{pot}.json', {'type': 'minecraft:brewing',
                'input': {'item': f'minecraft:{kind}', 'potion_contents': {'potions': 'minecraft:awkward'}},
                'output': {'components': {'minecraft:potion_contents': {'potion': M + pot}}, 'id': f'minecraft:{kind}'},
                'reagent': {'item': reagent}})
            n += 1
    return n

# ------------------------------------------------------------------ worldgen
def ore_feature(name, block, deep_block, size):
    targets = [{'state': {'id': block}, 'target': {'predicate_type': 'minecraft:tag_match', 'tag': 'minecraft:stone_ore_replaceables'}}]
    if deep_block: targets.append({'state': {'id': deep_block}, 'target': {'predicate_type': 'minecraft:tag_match', 'tag': 'minecraft:deepslate_ore_replaceables'}})
    w(f'{D}/worldgen/feature/{name}.json', {'type': 'minecraft:ore', 'discard_chance_on_air_exposure': 0.0, 'size': size, 'targets': targets})

def ore_placed(name, count, lo, hi, shape='uniform'):
    h = {'type': f'minecraft:{shape}', 'min_inclusive': lo, 'max_inclusive': hi}
    w(f'{D}/worldgen/placed_feature/{name}.json', {'feature': M + name, 'placement': [
        {'type': 'minecraft:count', 'count': count}, {'type': 'minecraft:in_square'}, {'type': 'minecraft:height_range', 'height': h}, {'type': 'minecraft:biome'}]})

def patch(name, block, rarity, tries, spread=5):
    w(f'{D}/worldgen/feature/{name}.json', {'type': 'minecraft:simple_block', 'to_place': {'id': block}})
    w(f'{D}/worldgen/placed_feature/{name}.json', {'feature': M + name, 'placement': [
        {'type': 'minecraft:rarity_filter', 'chance': rarity}, {'type': 'minecraft:in_square'}, {'type': 'minecraft:heightmap', 'heightmap': 'MOTION_BLOCKING_NO_LEAVES'},
        {'type': 'minecraft:biome'}, {'type': 'minecraft:count', 'count': tries},
        {'type': 'minecraft:offset', 'x': {'type': 'minecraft:trapezoid', 'max': spread, 'min': -spread, 'plateau': 0}, 'y': {'type': 'minecraft:trapezoid', 'max': 2, 'min': -2, 'plateau': 0},
         'z': {'type': 'minecraft:trapezoid', 'max': spread, 'min': -spread, 'plateau': 0}},
        {'type': 'minecraft:block_predicate_filter', 'predicate': {'type': 'minecraft:all_of', 'predicates': [
            {'type': 'minecraft:matching_block_tag', 'tag': 'minecraft:air'},
            {'type': 'minecraft:matching_block_tag', 'tag': 'minecraft:dirt', 'offset': [0, -1, 0]}]}}]})

def cave_block(name, block, rarity):
    w(f'{D}/worldgen/feature/{name}.json', {'type': 'minecraft:simple_block', 'to_place': {'id': block}})
    w(f'{D}/worldgen/placed_feature/{name}.json', {'feature': M + name, 'placement': [
        {'type': 'minecraft:rarity_filter', 'chance': rarity}, {'type': 'minecraft:in_square'},
        {'type': 'minecraft:height_range', 'height': {'type': 'minecraft:uniform', 'min_inclusive': {'absolute': -50}, 'max_inclusive': {'absolute': 50}}},
        {'type': 'minecraft:count', 'count': 24},
        {'type': 'minecraft:block_predicate_filter', 'predicate': {'type': 'minecraft:all_of', 'predicates': [
            {'type': 'minecraft:matching_block_tag', 'tag': 'minecraft:air'},
            {'type': 'minecraft:matching_block_tag', 'tag': 'minecraft:base_stone_overworld', 'offset': [0, -1, 0]}]}},
        {'type': 'minecraft:biome'}]})

def modifier(name, biomes, features, step):
    w(f'{D}/neoforge/biome_modifier/{name}.json', {'type': 'neoforge:add_features', 'biomes': biomes, 'features': features, 'step': step})

def spawns(name, biomes, entries):
    w(f'{D}/neoforge/biome_modifier/{name}.json', {'type': 'neoforge:add_spawns', 'biomes': biomes,
        'spawners': [{'type': M + t, 'count': {'type': 'minecraft:uniform', 'min_inclusive': lo, 'max_inclusive': hi}, 'weight': wt} for t, wt, lo, hi in entries]})

def gen_worldgen():
    ore_feature('ore_ruby', M + 'ruby_ore', M + 'deepslate_ruby_ore', 5); ore_placed('ore_ruby', 4, {'absolute': -40}, {'absolute': 32}, 'trapezoid')
    ore_feature('ore_mystery', M + 'mystery_ore', M + 'deepslate_mystery_ore', 3); ore_placed('ore_mystery', 6, {'above_bottom': 8}, {'absolute': 96})
    ore_feature('ore_plastic', M + 'plastic_ore', None, 7); ore_placed('ore_plastic', 5, {'absolute': 0}, {'absolute': 80})
    w(f'{D}/worldgen/feature/ore_bedrock.json', {'type': 'minecraft:ore', 'discard_chance_on_air_exposure': 0.0, 'size': 3,
        'targets': [{'state': {'id': M + 'bedrock_ore'}, 'target': {'predicate_type': 'minecraft:tag_match', 'tag': 'minecraft:deepslate_ore_replaceables'}}]})
    ore_placed('ore_bedrock', 2, {'above_bottom': 1}, {'above_bottom': 8})
    modifier('overworld_ores', '#minecraft:is_overworld', [M + 'ore_ruby', M + 'ore_mystery', M + 'ore_plastic', M + 'ore_bedrock'], 'underground_ores')
    patch('patch_mystery_plant', M + 'mystery_plant', 24, 6)
    patch('patch_mortasia', M + 'mortasia_flower', 160, 2)
    patch('patch_banana', M + 'banana_bunch', 6, 6)
    modifier('overworld_plants', '#minecraft:is_overworld', [M + 'patch_mystery_plant', M + 'patch_mortasia'], 'vegetal_decoration')
    modifier('jungle_bananas', '#minecraft:is_jungle', [M + 'patch_banana'], 'vegetal_decoration')
    cave_block('cave_village_crate', M + 'village_crate', 30)
    cave_block('cave_copper_crate', M + 'copper_crate', 45)
    cave_block('cave_crumbling_stone', M + 'crumbling_stone', 8)
    modifier('cave_crates', '#minecraft:is_overworld', [M + 'cave_village_crate', M + 'cave_copper_crate', M + 'cave_crumbling_stone'], 'underground_decoration')
    # spawns
    spawns('spawn_monkeys', '#minecraft:is_jungle', [('monkey', 12, 2, 4)])
    spawns('spawn_rams', '#minecraft:is_mountain', [('ram', 8, 2, 4), ('golden_eagle', 4, 1, 2)])
    spawns('spawn_boars', '#minecraft:is_forest', [('boar', 6, 2, 4)])
    spawns('spawn_taiga_boars', '#minecraft:is_taiga', [('boar', 6, 2, 4), ('illager_butcher', 1, 1, 1), ('illager_farmer', 1, 1, 1), ('illager_nitwit', 1, 1, 1)])
    spawns('spawn_dark_forest', 'minecraft:dark_forest', [('summoner', 6, 1, 1), ('illager_priest', 2, 1, 1), ('illager_librarian', 2, 1, 1), ('illager_smith', 2, 1, 1)])
    spawns('spawn_zephyr', '#minecraft:is_hill', [('zephyr', 3, 1, 1)])
    spawns('spawn_plains', 'minecraft:plains', [('locust', 4, 2, 5), ('zephyr', 2, 1, 1)])
    for biome_tag, slimes in [('#minecraft:is_overworld', [('stone', 10), ('earth', 8), ('coal', 8), ('iron', 6), ('gold', 3), ('redstone', 4), ('lapis', 3), ('diamond', 1),
                                                         ('emerald', 1), ('obsidian', 2), ('glass', 2), ('glowstone', 2)]),
                              ('#minecraft:is_beach', [('sand', 10), ('prismarine', 4)]), ('#minecraft:is_forest', [('wood', 10), ('honey', 4)]),
                              ('#c:is_snowy', [('ice', 10)]), ('#minecraft:is_nether', [('lava', 10), ('netherrack', 10), ('soul', 6), ('netherite', 1)])]:
        name = 'spawn_slimes_' + biome_tag.split(':')[1].replace('/', '_')
        spawns(name, biome_tag, [(f'{s}_slime', wt, 1, 2) for s, wt in slimes])
    spawns('spawn_iron_mimic', '#minecraft:is_overworld', [('iron_mimic', 3, 1, 1), ('mortis_zombie', 1, 1, 2)])
    # The Alphalands dimension (Dark Adventures "The Alphalands" + "Meteor Fields")
    base = json.load(open(os.environ.get('MASH_VANILLA', 'work/vanilla') + '/data/minecraft/worldgen/biome/plains.json'))
    for bname, sky, fog, water, grass, extra_spawns, extra_features in [
        ('alphalands', '#7ab8ff', '#c0e0ff', '#3f76e4', '#7fd64a', [('ram', 12, 2, 4), ('boar', 10, 2, 4), ('golden_eagle', 4, 1, 1)], [M + 'patch_mystery_plant']),
        ('meteor_fields', '#2a1a4a', '#5a4a7a', '#5a3a8a', '#6a7a5a', [('zephyr', 6, 1, 1), ('iron_mimic', 4, 1, 1)], [M + 'ore_mystery']),
        ('bog', '#6a7a5a', '#8a9a7a', '#4a5a3a', '#5a6a2a', [('mortis_zombie', 10, 1, 3), ('locust', 8, 2, 4)], [M + 'patch_mortasia'])]:
        b = json.loads(json.dumps(base))
        b['attributes']['minecraft:visual/sky_color'] = sky
        b['effects']['water_color'] = water
        b['effects']['grass_color'] = grass
        b['effects']['foliage_color'] = grass
        nm = b['attributes']['minecraft:gameplay/natural_mob_spawns']['argument']['spawns_by_category']
        nm['creature'] = nm.get('creature', []) + [{'type': M + t, 'count': {'type': 'minecraft:uniform', 'min_inclusive': lo, 'max_inclusive': hi}, 'weight': wt}
                                                    for t, wt, lo, hi in extra_spawns if t in ('ram', 'boar', 'golden_eagle')]
        nm['monster'] = nm.get('monster', []) + [{'type': M + t, 'count': {'type': 'minecraft:uniform', 'min_inclusive': lo, 'max_inclusive': hi}, 'weight': wt}
                                                  for t, wt, lo, hi in extra_spawns if t not in ('ram', 'boar', 'golden_eagle')]
        b['features'][9] = list(b['features'][9]) + extra_features if len(b['features']) > 9 else b['features']
        w(f'{D}/worldgen/biome/{bname}.json', b)
    w(f'{D}/dimension_type/alphalands.json', json.load(open(os.environ.get('MASH_VANILLA', 'work/vanilla') + '/data/minecraft/dimension_type/overworld.json')))
    w(f'{D}/dimension/alphalands.json', {'type': M + 'alphalands', 'generator': {'type': 'minecraft:noise', 'settings': 'minecraft:overworld', 'biome_source': {
        'type': 'minecraft:multi_noise', 'biomes': [
            {'biome': M + 'alphalands', 'parameters': {'temperature': [-1.0, 0.2], 'humidity': [-1.0, 1.0], 'continentalness': [-0.1, 1.0], 'erosion': [-1.0, 1.0], 'weirdness': [-1.0, 0.0], 'depth': 0.0, 'offset': 0.0}},
            {'biome': M + 'meteor_fields', 'parameters': {'temperature': [0.2, 1.0], 'humidity': [-1.0, 0.3], 'continentalness': [-0.1, 1.0], 'erosion': [-1.0, 1.0], 'weirdness': [-1.0, 1.0], 'depth': 0.0, 'offset': 0.0}},
            {'biome': M + 'bog', 'parameters': {'temperature': [0.2, 1.0], 'humidity': [0.3, 1.0], 'continentalness': [-1.0, 1.0], 'erosion': [-1.0, 1.0], 'weirdness': [-1.0, 1.0], 'depth': 0.0, 'offset': 0.0}},
            {'biome': 'minecraft:ocean', 'parameters': {'temperature': [-1.0, 0.2], 'humidity': [-1.0, 1.0], 'continentalness': [-1.0, -0.1], 'erosion': [-1.0, 1.0], 'weirdness': [-1.0, 1.0], 'depth': 0.0, 'offset': 0.0}},
            {'biome': 'minecraft:dark_forest', 'parameters': {'temperature': [-1.0, 0.2], 'humidity': [-1.0, 1.0], 'continentalness': [-0.1, 1.0], 'erosion': [-1.0, 1.0], 'weirdness': [0.0, 1.0], 'depth': 0.0, 'offset': 0.0}}]}}})

def main():
    for d in ('loot_table', 'villager_trade', 'trade_set', 'tags', 'recipe', 'worldgen', 'neoforge', 'dimension', 'dimension_type', 'loot_modifiers'):
        shutil.rmtree(f'{D}/{d}', ignore_errors=True)
    for d in ('tags/item', 'tags/entity_type', 'tags/point_of_interest_type'):
        shutil.rmtree(f'{MC}/{d}', ignore_errors=True)
    shutil.rmtree(f'{RES}/data/c', ignore_errors=True)
    import gen
    gen.lang.clear()
    # block tags (mineable etc.) and repair tags are written by gen.py - regenerate them first
    gen.gen_blocks(); gen.gen_armor()
    gen_loot(); gen_trades(); gen_tags(); n = gen_recipes(); b = gen_brewing(); gen_worldgen()
    print('recipes', n, 'brewing', b)

if __name__ == '__main__':
    main()
