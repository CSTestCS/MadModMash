"""MadModMash master content table.

Every item/block is declared once here.  gen.py turns this into:
  * Java registration classes (ModItems/ModBlocks/ModArmorMaterials generated sources)
  * textures (copied from the original mods or generated), models, item definitions,
    blockstates, loot tables, lang entries (names + lore lines).
"""
from texlib import tint, hue, load, from_template, tint_where, is_grayish, overlay, blank

ITEMS = []    # dicts: id,name,tex,model,ctor,props,tab,desc
BLOCKS = []   # dicts: id,name,kind,tex,ctor,props,tab,desc,loot,item

def item(id, name, tex=None, ctor='Item::new', props='p', tab='materials', desc=None, model='generated', **kw):
    d = dict(id=id, name=name, tex=tex, ctor=ctor, props=props, tab=tab, desc=desc, model=model)
    d.update(kw); ITEMS.append(d); return d

def block(id, name, kind='cube', tex=None, ctor='Block::new', props='p', tab='blocks', desc=None, loot='self', item=True, **kw):
    d = dict(id=id, name=name, kind=kind, tex=tex, ctor=ctor, props=props, tab=tab, desc=desc, loot=loot, item=item)
    d.update(kw); BLOCKS.append(d); return d

def mat(id, name, tex, desc=None, tab='materials', props='p'):
    return item(id, name, tex, desc=desc, tab=tab, props=props)

# ============================================================== ECONOMY (Emerald Empire)
GILDANS = [  # id, name, texture, value, colour
    ('copper_gildan', 'Copper Gildan', 'DA:item/coppercoin', 1),
    ('silver_gildan', 'Silver Gildan', 'DA:item/silvercoin', 9),
    ('gold_gildan', 'Gold Gildan', 'DA:item/gold_coin', 81),
    ('emerald_gildan', 'Emerald Gildan', 'DA:item/emeraldcoin', 729),
    ('amethyst_gildan', 'Amethyst Gildan', 'DA:item/amethystcoin', 6561),
    ('diamond_gildan', 'Diamond Gildan', 'DA:item/diamondcoin', 59049),
    ('netherite_gildan', 'Netherite Gildan', 'DA:item/netheritecoin', 531441),
]
for gid, gname, gtex, gval in GILDANS:
    item(gid, gname, gtex, ctor=f'p -> new GildanItem(p, {gval})', tab='empire',
         desc=f'Imperial coinage worth {gval} copper. Nine of a kind exchange for one of the next tier.')
item('coin_pouch', 'Coin Pouch', 'V:item/bundle', ctor='CoinPouchItem::new', props='p.stacksTo(1)', tab='empire',
     desc='Gildans you pick up are counted into the pouch. Use to withdraw, sneak-use to deposit.')
item('imperial_charter', 'Imperial Charter', tint('UNOB:item/questparchment', '#4fd16b', keep=0.4), ctor='ImperialCharterItem::new',
     props='p.stacksTo(1).rarity(Rarity.EPIC)', tab='empire', desc="Unlocks the Emperor's Track of the Imperial Path.")
item('renown_ledger', 'Renown Ledger', 'V:item/writable_book', ctor='RenownLedgerItem::new', props='p.stacksTo(1)', tab='empire',
     desc='Shows your Imperial rank, Path level, faction Wrath and active events.')
item('quest_journal', 'Quest Journal', 'UNOB:item/questparchment', ctor='QuestJournalItem::new', props='p.stacksTo(1)', tab='empire',
     desc='Tracks the five Unobtainable questlines.')
item('recall_token', 'Recall Token', 'RPG:items/recalltoken', ctor='RecallTokenItem::new', props='p.stacksTo(16)', tab='empire',
     desc='Bind to a Bonfire, then use anywhere to return to it.')
item('return_token', 'Return Token', 'RPG:items/returntoken', ctor='ReturnTokenItem::new', props='p.stacksTo(16)', tab='empire',
     desc='Returns you to where you last used a Recall Token.')

# contracts / bounties
item('blank_contract', 'Blank Contract', 'DA:item/huntcontractbasenew', ctor='p -> new ContractItem(p, 0)', props='p.stacksTo(16)', tab='empire',
     desc='Use to draw up a random common contract.')
item('common_hunt_contract', 'Common Hunt Contract', 'DA:item/generichuntcontracttier1', ctor='p -> new ContractItem(p, 1)', props='p.stacksTo(1)', tab='empire')
item('rare_hunt_contract', 'Rare Hunt Contract', 'DA:item/generichuntcontracttier2', ctor='p -> new ContractItem(p, 2)', props='p.stacksTo(1).rarity(Rarity.RARE)', tab='empire')
item('legendary_hunt_contract', 'Legendary Hunt Contract', 'DA:item/generichuntcontracttier3', ctor='p -> new ContractItem(p, 3)', props='p.stacksTo(1).rarity(Rarity.EPIC)', tab='empire')
item('gather_contract', 'Gather Contract', 'DA:item/huntcontractthickwoolnew', ctor='p -> new ContractItem(p, 4)', props='p.stacksTo(1)', tab='empire')
item('wanted_poster', 'Wanted Poster', 'DA:item/ramhornbounty', ctor='WantedPosterItem::new', props='p.stacksTo(1)', tab='empire',
     desc='Names a Champion roaming the land. Kill it and bring its trophy to a Bounty Buyer.')
item('champion_trophy', 'Champion Trophy', 'MJ23:item/bountybuyeritem', ctor='ChampionTrophyItem::new', props='p.stacksTo(16).rarity(Rarity.UNCOMMON)', tab='empire',
     desc='Proof of a slain Champion. Bounty Buyers pay well for these.')
item('scanner', 'Bounty Scanner', 'JELLY:items/scanner', ctor='ScannerItem::new', props='p.stacksTo(1)', tab='empire',
     desc='Scan a creature to reveal Champions, bounties, Spies and disguises.')
item('capture_card', 'Capture Target Card', 'JELLY:items/bountycard', props='p.stacksTo(1)', tab='empire', ctor='p -> new TargetCardItem(p, true)',
     desc='Subdue the marked target with a truncheon and lead it to a Bounty Buyer.')
item('kill_card', 'Kill Target Card', 'JELLY:items/bountycardkill', props='p.stacksTo(1)', tab='empire', ctor='p -> new TargetCardItem(p, false)')
item('req_card', 'Requisition Card', 'HALO:items/reqcard', ctor='ReqCardItem::new', props='p.stacksTo(16)', tab='empire',
     desc='Imperial requisition. Use to receive a random field supply crate of gear and ammunition.')

# keys & crates
KEYS = [('rusted_key', 'Rusted Key', 'RPG:items/rusty_key', 0), ('copper_key', 'Copper Key', 'DA:item/copper_key_new', 1),
        ('iron_key', 'Iron Key', 'DA:item/iron_key_new', 2), ('gold_key', 'Gold Key', 'DA:item/gold_key_new', 3),
        ('emerald_key', 'Emerald Key', 'DA:item/emeraldkey', 4), ('amethyst_key', 'Amethyst Key', 'DA:item/amethystkey', 5),
        ('diamond_key', 'Diamond Key', 'DA:item/diamondkey', 6), ('netherite_key', 'Netherite Key', 'DA:item/netheritekey', 7),
        ('master_key', 'Master Key', 'RPG:items/master_key', 8)]
for kid, kname, ktex, tier in KEYS:
    item(kid, kname, ktex, ctor=f'p -> new CrateKeyItem(p, {tier})', props='p.stacksTo(16)', tab='empire',
         desc='Opens a crate of the same tier or lower.' if tier < 8 else 'Opens any crate, even the Master Crate.')
item('lock_pick', 'Lock Pick', 'RPG:items/lock_pick', ctor='p -> new CrateKeyItem(p, -1)', props='p.stacksTo(16)', tab='empire',
     desc='Has a chance to open any crate up to Gold tier. Often snaps.')
item('magical_key', 'Magical Key', 'RPG:items/magical_key', ctor='p -> new CrateKeyItem(p, -2)', props='p.stacksTo(16).rarity(Rarity.RARE)', tab='empire',
     desc='Opens any crate below Master tier and rerolls its loot one tier higher.')
item('key_of_dimensions', 'Key of Dimensions', 'RPG:items/key_of_dimensions', ctor='DimensionKeyItem::new', props='p.stacksTo(1).rarity(Rarity.EPIC)', tab='empire',
     desc='Opens a way to the Alphalands, and back home again.')

# ============================================================== MYSTERY / ALCHEMY
item('wooden_magnifier', 'Wooden Magnifier', 'DA:item/magnifyingglass', ctor='p -> new MagnifierItem(p, 0)', props='p.durability(32)', tab='arcana',
     desc='Appraises Mystery Ores and Mystery Plants.')
item('copper_magnifier', 'Copper Magnifier', 'DA:item/magnifyingglasscopper', ctor='p -> new MagnifierItem(p, 1)', props='p.durability(96)', tab='arcana')
item('iron_magnifier', 'Iron Magnifier', 'DA:item/magnifyingglassiron', ctor='p -> new MagnifierItem(p, 2)', props='p.durability(192)', tab='arcana')
item('golden_magnifier', 'Golden Magnifier', 'DA:item/magnifyingglassgold', ctor='p -> new MagnifierItem(p, 3)', props='p.durability(64)', tab='arcana')
item('netherite_magnifier', 'Netherite Magnifier', 'DA:item/magnifyingglassnetherite', ctor='p -> new MagnifierItem(p, 4)', props='p.durability(1024).fireResistant()', tab='arcana')
item('bee_magnifier', 'Bee Magnifier', 'DA:item/magnifyingglassbee', ctor='p -> new MagnifierItem(p, 5)', props='p.durability(128)', tab='arcana',
     desc='Never loses durability on plants.')
item('village_magnifier', 'Village Magnifier', 'RPG:items/villagemagnifyingglass', ctor='p -> new MagnifierItem(p, 1)', props='p.durability(128)', tab='arcana')
item('village_sickle', 'Village Sickle', 'RPG:items/villagesickle', ctor='SickleItem::new', props='p.durability(160)', tab='arcana', model='handheld',
     desc='Harvests revealed mystery plants for powder, and reaps crops in a wide arc.')

# 12 mystery mineral families x 3 forms -> each a Trait Core (see Trait.java)
MINERALS = [  # family key, trait, (ingot, shard, gem) source names, display names
    ('liliota', 'void', ('00925ostrum', '00925morium', '00925liliota'), ('Ostrum Ingot', 'Morium Shard', 'Liliota Gem')),
    ('limona', 'air', ('01614calcius', '01614misket', '01614limona'), ('Calcius Ingot', 'Misket Shard', 'Limona Gem')),
    ('lutum', 'fire', ('01921lutum', '01921zawist', '01921korkak'), ('Lutum Ingot', 'Zawist Shard', 'Korkak Gem')),
    ('brunius', 'earth', ('01929brunius', '01929karamak', '01929brazite'), ('Brunius Ingot', 'Karamak Shard', 'Brazite Gem')),
    ('viridius', 'poison', ('01968viridius', '01968vert', '01968taz'), ('Viridius Ingot', 'Vert Shard', 'Taz Gem')),
    ('tawnium', 'stone', ('02238tawnium', '02238tawnium', '02238tawnium'), ('Tawnium Ingot', 'Tawnium Shard', 'Tawnium Gem')),
    ('carulem', 'ice', ('03848carulem', '03848mavium', '03848sapphire'), ('Carulem Ingot', 'Mavium Shard', 'Sapphire')),
    ('szarite', 'might', ('04185karite', '04185szarite', '04185gri'), ('Karite Ingot', 'Szarite Shard', 'Gri Gem')),
    ('flavum', 'lightning', ('06424flavum', '06424zoltite', '06424sari'), ('Flavum Ingot', 'Zoltite Shard', 'Sari Gem')),
    ('rubrum', 'life', ('07357rubrum', '07357krasny', '07357ruby'), ('Rubrum Ingot', 'Krasny Shard', 'Ruby')),
    ('aterum', 'shadow', ('11028aterum', '11028aterum', '11028siyah'), ('Aterum Ingot', 'Aterum Shard', 'Siyah Gem')),
    ('albumin', 'light', ('13506albumingot', '13506beyaz', '13506biel'), ('Albumin Ingot', 'Beyaz Shard', 'Biel Gem')),
]
MINERAL_IDS = []
for fam, trait, srcs, names in MINERALS:
    for lvl, (src, nm) in enumerate(zip(srcs, names), start=1):
        iid = nm.lower().replace(' ', '_')
        tex = f'RPG:items/{src}'
        if fam == 'tawnium' and lvl == 2: tex = hue(tex, 0, 1.2, 1.15)
        if fam == 'tawnium' and lvl == 3: tex = hue(tex, -8, 1.4, 1.3)
        if fam == 'aterum' and lvl == 2: tex = hue(tex, 0, 1.0, 1.4)
        item(iid, nm, tex, ctor=f'p -> new TraitCoreItem(p, Trait.{trait.upper()}, {lvl})',
             props='p' if lvl < 3 else 'p.rarity(Rarity.RARE)', tab='arcana')
        MINERAL_IDS.append((iid, fam, trait, lvl))
item('crushed_coal', 'Crushed Coal', 'RPG:items/crushedcoal', desc='Smelts with iron into steel.')

# Powders (bonfire infusions) - name, bonfire effect key, texture
POWDERS = [
    ('vistren', 'Vistren Powder', 'strength', 'powderstrength'), ('celerus', 'Celerus Powder', 'speed', 'powderspeed'),
    ('armis', 'Armis Powder', 'resistance', 'powdergold'), ('sanatia', 'Sanatia Powder', 'regeneration', 'powderregeneration'),
    ('aquarius', 'Aquarius Powder', 'water_breathing', 'powderwaterbreathing'), ('alsus', 'Alsus Powder', 'leaping', 'powderforest'),
    ('patet', 'Patet Powder', 'glowing', 'powderglowing'), ('illusio', 'Illusio Powder', 'invisibility', 'powderinvisibility'),
    ('luxine', 'Luxine Powder', 'heroism', 'powderhero'), ('visus', 'Visus Powder', 'night_vision', 'powdernightvision'),
    ('fortuna', 'Fortuna Powder', 'luck', 'powderluck'), ('volare', 'Volare Powder', 'flight', 'powderflight'),
    ('arescet', 'Arescet Powder', 'fire_resistance', 'powderfireresist'), ('rulsevia', 'Rulsevia Powder', 'repulsion', 'powderrepulsion'),
    ('felirus', 'Felirus Powder', 'command_animals', 'powderforest'), ('lumia', 'Lumia Powder', 'protective_light', 'powderprotectivelight'),
    ('statia', 'Statia Powder', 'stasis', 'powderstasis'), ('tempesta', 'Tempesta Powder', 'storm', 'powderstorms'),
    ('serenus', 'Serenus Powder', 'clear_weather', 'powderclarity'), ('sumonia', 'Sumonia Powder', 'summon_visitor', 'powdersummoning'),
    ('arcanus', 'Arcanus Powder', 'cleansing', 'powderarcane'), ('animus', 'Animus Powder', 'possession', 'powderpossession'),
    ('gravitas', 'Gravitas Powder', 'gravity', 'powdergravity'), ('mortem', 'Mortem Powder', 'smiting', 'powdersoul'),
    ('viridia', 'Viridia Powder', 'growth', 'powderglistening'), ('venenum', 'Venenum Powder', 'poison', 'powderpoison'),
    ('chroma', 'Chroma Powder', 'elementalism', 'powderrainbow'), ('dissolvo', 'Dissolvo Powder', 'dissolution', 'powderdissolution'),
]
for pid, pname, effect, ptex in POWDERS:
    tex = f'RPG:items/{ptex}'
    if pid == 'felirus': tex = hue(tex, 40, 1.3)
    item(f'{pid}_powder', pname, tex, ctor=f'p -> new PowderItem(p, BonfireEffect.{effect.upper()})', tab='arcana')

# ============================================================== MOB MATERIALS
MOBMATS = [
    ('wolf_fur', 'Wolf Fur', 'RPG:items/wolffur'), ('alpha_wolf_fur', 'Alpha Wolf Fur', 'RPG:items/alphawolffur'),
    ('creeper_leaves', 'Creeper Leaves', 'RPG:items/creeperleaves'), ('charged_leaves', 'Charged Leaves', hue('RPG:items/creeperleaves', 70, 1.2, 1.2)),
    ('magma_scales', 'Magma Scales', 'RPG:items/magmascales'), ('unholy_cloth', 'Unholy Cloth', 'RPG:items/unholycloth'),
    ('herobrine_charm', 'Herobrine Charm', 'RPG:items/herobrinecharm'), ('spectral_scales', 'Spectral Scales', 'RPG:items/spectralscales'),
    ('enderman_scales', 'Enderman Scales', 'RPG:items/endermanscales'), ('stiff_feather', 'Stiff Feather', 'RPG:items/stifffeather'),
    ('squid_skin', 'Squid Skin', 'RPG:items/squidskin'), ('squid_eye', 'Squid Eye', 'RPG:items/squideye'),
    ('squid_tentacle', 'Squid Tentacle', 'RPG:items/squidtenticle'), ('fox_pelt', 'Fox Pelt', 'RPG:items/foxpelt'),
    ('fox_tail', 'Fox Tail', 'RPG:items/foxtail'), ('endermite_mucus', 'Endermite Mucus', 'RPG:items/endermitemucus'),
    ('endermite_softshell', 'Endermite Softshell', 'RPG:items/endermitesoftshell'), ('bee_chitin', 'Bee Chitin', 'RPG:items/beechitin'),
    ('bee_lense', 'Bee Lense', 'RPG:items/beelense'), ('bee_wing', 'Bee Wing', 'RPG:items/beewing'),
    ('elder_guardian_fin', 'Elder Guardian Fin', 'RPG:items/elderguardianfin'), ('elder_guardian_scales', 'Elder Guardian Scales', 'RPG:items/elderguardianscales'),
    ('guardian_fin', 'Guardian Fin', 'RPG:items/guardianfin'), ('guardian_scales', 'Guardian Scales', 'RPG:items/guardianscales'),
    ('horse_hair', 'Horse Hair', 'RPG:items/horsehair'), ('horse_leather', 'Horse Leather', 'RPG:items/horseleather'),
    ('fibrous_mist', 'Fibrous Mist', 'RPG:items/fibrousmist'), ('fibrous_flame', 'Fibrous Flame', 'RPG:items/fibrousflame'),
    ('shroomhide', 'Shroomhide', 'RPG:items/shroomhide'), ('dragon_scale', 'Dragon Scale', 'RPG:items/dragonscale'),
    ('dragon_skin', 'Dragon Skin', 'RPG:items/dragonskin'), ('bat_fur', 'Bat Fur', 'RPG:items/batfur'), ('bat_wing', 'Bat Wing', 'RPG:items/batwing'),
    ('thick_leather', 'Thick Leather', 'RPG:items/thickleather'), ('toxic_string', 'Toxic String', 'RPG:items/toxicstring'),
    ('wither_bone', 'Wither Bone', 'RPG:items/witherbone'), ('hoglin_leather', 'Hoglin Leather', 'RPG:items/hoglinleather'),
    ('hoglin_bristle', 'Hoglin Bristle', 'RPG:items/hoglinbristle'), ('hoglin_tusk', 'Hoglin Tusk', 'RPG:items/hoglintusk'),
    ('illusory_cloth', 'Illusory Cloth', 'RPG:items/illusorycloth'), ('bizarre_star', 'Bizarre Star', 'RPG:items/bizzarestar'),
    ('phantom_hide', 'Phantom Hide', 'RPG:items/phantomleather'), ('salmon_scale', 'Salmon Scale', 'RPG:items/salmonscale'),
    ('strong_salmon_scale', 'Strong Salmon Scale', 'RPG:items/strongsalmonscale'), ('shulker_chitin', 'Shulker Chitin', 'RPG:items/shulkerchitin'),
    ('ram_horn', 'Ram Horn', 'RPG:items/ramhornt1'), ('alpha_leather', 'Alpha Leather', 'DA:item/alphaleather'),
    ('chainmail_sheet', 'Chainmail Sheet', 'RPG:items/chainmailsheet'),
    ('black_cloth', 'Black Cloth', 'HMM:items/black_fabric'), ('silver_cloth', 'Silver Cloth', 'HMM:items/silver_fabric'),
    ('yellow_cloth', 'Yellow Cloth', 'WL:item/yellow_fabric'), ('fabric', 'Fabric', tint('HMM:items/silver_fabric', '#e8dcc0')),
    ('steel_nugget', 'Steel Nugget', tint('V:item/iron_nugget', '#9fb4c8')), ('steel_ingot', 'Steel Ingot', 'QUACK:items/steelingot'),
    ('polar_pelt', 'Polar Pelt', tint('RPG:items/wolffur', '#f4f4ff', bright=1.2)), ('pigskin', 'Pigskin', tint('RPG:items/hoglinleather', '#f0a0a8')),
    ('strider_hair', 'Strider Hair', tint('RPG:items/horsehair', '#c0303a')), ('silverfish_chitin', 'Silverfish Chitin', tint('RPG:items/beechitin', '#a8adb5')),
    ('withered_soul', 'Withered Soul', tint('RPG:items/fibrousmist', '#4a3a55')), ('corrupt_leather', 'Corrupt Leather', tint('RPG:items/thickleather', '#5b3a7a')),
    ('ancient_cloth', 'Ancient Cloth', tint('RPG:items/unholycloth', '#b9b08a')), ('diamond_pendant', 'Diamond Pendant', tint('RPG:items/herobrinecharm', '#5ee8e0')),
    ('nasty_cloth', 'Nasty Cloth', tint('RPG:items/unholycloth', '#5d8a3a')), ('bloodied_cloth', 'Bloodied Cloth', tint('RPG:items/unholycloth', '#a0241e')),
    ('broken_axehead', 'Broken Axehead', tint_where('V:item/iron_axe', '#8a8f96', lambda r, g, b: is_grayish(r, g, b))),
    ('cat_fur', 'Cat Fur', tint('RPG:items/wolffur', '#e0a050')), ('sickly_leather', 'Sickly Leather', tint('RPG:items/thickleather', '#7a9a5a')),
    ('redstone_core', 'Redstone Core', tint('RPG:items/beelense', '#ff2a1a', bright=1.3)),
    ('embers', 'Embers', 'RPG:items/firepuff'), ('pop_rock', 'Pop Rock', 'RPG:items/poprock'),
    ('mortis_flesh', 'Mortis Flesh', tint('V:item/rotten_flesh', '#7aa05a')),
    ('locust_wing', 'Locust Wing', tint('RPG:items/beewing', '#b5a040')),
    ('golden_eagle_feather', 'Golden Eagle Feather', tint('RPG:items/stifffeather', '#e2b33a')),
    ('monkey_fur', 'Monkey Fur', tint('RPG:items/wolffur', '#7a4a22')),
    ('zephyr_wisp', 'Zephyr Wisp', tint('RPG:items/fibrousmist', '#d8f6ff', bright=1.3)),
    ('astroite_chunk', 'Astroite Chunk', tint('V:item/raw_iron', '#6a5a8a')),
    ('plastic_gem', 'Plastic Gem', 'QUACK:items/legoplastic_gem'), ('ruby_shard', 'Ruby Dust', 'QUACK:items/bedrock_dust'),
    ('bedrock_dust', 'Bedrock Dust', 'QUACK:items/bedrock_dust'),
    ('command_block_fragment', 'Command Block Fragment', 'DA:item/commandblockfragment'),
    ('flawless_lapis', 'Flawless Lapis Lazuli', 'DA:item/perfectlapislazuli'),
    ('tissue', 'Tissue', tint('HMM:items/silver_fabric', '#ffffff', bright=1.4)),
    ('warden_soul', 'Warden Soul', tint('RPG:items/fibrousmist', '#0e6b74', bright=1.2)),
]
for mid, mname, mtex in MOBMATS:
    if mid == 'ruby_shard': continue
    mat(mid, mname, mtex)

# ============================================================== FOOD & DRINK
item('alpha_beef', 'Alpha Beef', 'DA:item/alphasteakraw', props='p.food(Foods.ALPHA_BEEF)', tab='materials')
item('alpha_steak', 'Alpha Steak', 'DA:item/alphasteakcooked', props='p.food(Foods.ALPHA_STEAK)', tab='materials')
item('banana', 'Banana', from_template('orb', '#f2d23a', '#c9a520', '#5a4a10'), props='p.food(Foods.BANANA)', tab='materials',
     desc='Monkeys go bananas for these.')
item('air_bubble', 'Air Bubble', 'DA:item/airbubble', ctor='AirBubbleItem::new', props='p.stacksTo(16)', tab='arcana', desc='Pop it underwater for a breath of air.')
SODAS = [('creeper_cola', 'Creeper Cola', 'WUT:blocks/cancocacolatex', 'A fizzy green blast. Speed and Jump Boost.'),
         ('emerald_fizz', 'Emerald Fizz', 'WUT:blocks/canpepsitex', 'Imperial favourite. Luck and Hero of the Village.'),
         ('doctor_bubbly', 'Doctor Bubbly', 'WUT:blocks/candrpeppertex', '23 secret ingredients. Regeneration and Saturation.')]
for sid, sname, stex, sdesc in SODAS:
    item(sid, sname, from_template('can', {'creeper_cola': '#3fbf3a', 'emerald_fizz': '#1f8f5a', 'doctor_bubbly': '#8a1f2a'}[sid], '#e8e8e8', '#9a9a9a'),
         ctor=f'p -> new SodaItem(p, "{sid}")', props='p.stacksTo(16)', tab='materials', desc=sdesc)

# ============================================================== MAGIC CARDS & ARCANA
CARDS = [('fire', 'Fire Card', 'magiccardfire'), ('ice', 'Ice Card', 'magiccardice'), ('toxin', 'Toxin Card', 'magiccardtoxicslime'),
         ('void_fire', 'Void Fire Card', 'magiccardvoidfire'), ('xp', 'Experience Card', 'magiccardxp'), ('healing', 'Healing Card', 'magiccardheal'),
         ('golem', 'Golem Card', 'magiccardgolem')]
for cid, cname, ctex in CARDS:
    item(f'{cid}_card', cname, f'QTH:items/{ctex}', ctor=f'p -> new MagicCardItem(p, MagicCardItem.Kind.{cid.upper()})', props='p.stacksTo(16)', tab='arcana')
item('magic_card_deck', 'Magic Card Deck', 'QTH:items/magiccardback', ctor='CardDeckItem::new', props='p.stacksTo(1).durability(24)', tab='arcana',
     desc='Draw a random magic card. Wizards draw for free more often.')
item('enchanted_quill', 'Enchanted Quill', 'RPG:items/enchantedquill', ctor='EnchantedQuillItem::new', props='p.durability(16)', tab='arcana',
     desc='With paper in your other hand, writes a Lucky Spell.')
item('lucky_spell', 'Lucky Spell', 'RPG:items/luckyspell', ctor='LuckySpellItem::new', props='p.stacksTo(16)', tab='arcana',
     desc='Hurls a spell with a random effect.')
item('lucky_shuriken', 'Lucky Shuriken', from_template('orb', '#c9b6ff', '#ffffff', '#6a4ab0'), ctor='LuckyShurikenItem::new', props='p.stacksTo(16)', tab='arcana')
item('wolf_totem', 'Wolf Totem', 'RPG:items/wolftotem', ctor='WolfTotemItem::new', props='p.stacksTo(1).durability(8)', tab='arcana',
     desc='Howls for the pack: slays nearby skeletons and rallies your wolves.')
item('creeper_totem', 'Blast Totem', 'RPG:items/creepertotem', ctor='BlastTotemItem::new', props='p.stacksTo(4)', tab='arcana',
     desc='Plant it; moments later everything nearby but you is blasted.')
item('effigy', 'Effigy', 'RPG:items/effigyit', ctor='EffigyItem::new', props='p.stacksTo(4)', tab='arcana',
     desc='A cursed doll with a deep health pool that wounds all nearby enemies.')
item('antipotion', 'Splash Antipotion', 'RPG:items/splashpotionofantipotion', ctor='AntipotionItem::new', props='p.stacksTo(16)', tab='arcana',
     desc='Shatters every effect, good or bad, on all creatures it splashes.')
item('pocketball', 'Pocketball', from_template('ball', '#d8402a', '#f0f0f0', '#202020'), ctor='PocketballItem::new', props='p.stacksTo(16)', tab='arcana',
     desc='Throw at a weakened creature to capture it. Throw again to release.')
item('enchancer', 'Enchancer', hue('RPG:items/powderenchanted', 0, 1.4, 1.2), ctor='EnchancerItem::new', props='p.rarity(Rarity.RARE)', tab='arcana',
     desc='Raises one enchantment on your off-hand item beyond its normal limit (up to X).')
item('seer_draught', "Seer's Draught", hue('V:item/honey_bottle', 180), ctor='SeerDraughtItem::new', props='p.stacksTo(16)', tab='arcana',
     desc='Reveals nearby ores through the stone for a short while.')
item('mortasia_bloom', 'Mortasia Bloom', 'ZP:blocks/mortisflower', tab='arcana', desc='A pale flower that smells of the grave.')
item('zombie_plague_vial', 'Vial of Zombie Plague', tint('V:item/potion', '#557a2f'), ctor='PlagueVialItem::new', props='p.stacksTo(16)', tab='arcana')
item('mortis_cure', 'Mortis Cure', tint('V:item/potion', '#f2e6a0'), ctor='MortisCureItem::new', props='p.stacksTo(16)', tab='arcana',
     desc='Cures the Zombie Plague and Mortis Infection.')
item('midas_draught', 'Midas Draught', tint('V:item/potion', '#f2c02a'), ctor='MidasDraughtItem::new', props='p.stacksTo(16)', tab='arcana',
     desc='Everything you slay for a while drops gildans.')
item('chisel_bag', "Mason's Satchel", 'V:item/bundle', props='p.stacksTo(1)', tab='tools')

# ============================================================== TOOLS / GADGETS
item('grapple_hook', 'Grapple Hook', 'AA:items/grapplehook', ctor='GrappleHookItem::new', props='p.durability(256)', tab='tools',
     desc='Fire a hook into a block and reel yourself in. Sneak to drop a rope line.')
item('air_hook', 'Air Hook', tint('AA:items/grapplehook', '#cfefff'), ctor='AirHookItem::new', props='p.durability(128)', tab='tools',
     desc='Use against a block to be thrust high into the air.')
item('ancient_dagger', 'Ancient Dagger', 'DA:item/ancient_dagger_2', ctor='AncientDaggerItem::new', props='p.sword(ModTiers.ANCIENT, 2f, -2f).rarity(Rarity.EPIC)', tab='tools', model='handheld',
     desc='Cuts through stone like butter. Use on a block to turn it to stone.')
item('wooden_dagger', 'Wooden Dagger', 'IH:items/woodendagger', ctor='p -> new MashWeaponItem(p, WeaponClass.DAGGER)', props='p.sword(ToolMaterial.WOOD, 1.5f, -1.8f)', tab='arsenal', model='handheld')
item('brick_blade', 'Brick Blade', 'LEGO:items/sword', ctor='BrickBladeItem::new', props='p.sword(ModTiers.PLASTIC, 3f, -2.4f)', tab='arsenal', model='handheld',
     desc='Snaps toy bricks into place when used on a block. Hurts to step on.')
item('legendary_diamond_sword', 'Legendary Diamond Sword', 'DA:item/classicdiamondsword', ctor='LegendarySwordItem::new',
     props='p.sword(ToolMaterial.DIAMOND, 5f, -2.4f).rarity(Rarity.EPIC)', tab='arsenal', model='handheld',
     desc='Grows stronger with every kill.')
item('ramhorn_mallet', 'Ramhorn Mallet', 'DA:item/ramhornmallet', ctor='p -> new MashWeaponItem(p, WeaponClass.HAMMER)', props='p.sword(ToolMaterial.IRON, 6f, -3.2f)', tab='arsenal', model='handheld')
item('fireball_bow', 'Fireball Bow', 'DUCK:items/phantomfirebow', ctor='p -> new ElementBowItem(p, ElementBowItem.Kind.FIRE)', props='p.durability(384)', tab='arsenal')
item('lightning_bow', 'Lightning Bow', 'DUCK:items/phantomarcbow', ctor='p -> new ElementBowItem(p, ElementBowItem.Kind.LIGHTNING)', props='p.durability(384)', tab='arsenal')
item('freezing_bow', 'Freezing Bow', 'DUCK:items/phantomicebow', ctor='p -> new ElementBowItem(p, ElementBowItem.Kind.ICE)', props='p.durability(384)', tab='arsenal')
item('enchanted_flint_and_steel', 'Enchanted Flint and Steel', 'DUCK:items/enchantedflintandsteel', ctor='EnchantedFlintItem::new', props='p.durability(64)', tab='tools',
     desc='Lights soul fire that never spreads. Something answers when you light a Herobrine shrine...')
item('phantom_shovel', 'Phantom Shovel', 'DUCK:items/phantomshovel', ctor='PhantomShovelItem::new', props='p.shovel(ToolMaterial.DIAMOND, 1.5f, -3f)', tab='tools', model='handheld',
     desc='Blocks it digs fade away and return ten seconds later.')
item('rock', 'Rock', 'MJ23:item/rock', ctor='RockItem::new', props='p.stacksTo(16)', tab='arsenal')
item('pop_rock_throwable', 'Molten Pop Rock', hue('RPG:items/poprock', 10, 1.3, 1.2), ctor='PopRockItem::new', props='p.stacksTo(16)', tab='arsenal',
     desc='A molten pebble that bursts on impact.')
item('throwing_star', 'Throwing Star', from_template('orb', '#b0b8c0', '#e8e8e8', '#505860'), ctor='ThrowingStarItem::new', props='p.stacksTo(32)', tab='arsenal')
item('frag_grenade', 'Frag Grenade', from_template('orb', '#4a5a3a', '#2a2a2a', '#9a9a9a'), ctor='GrenadeItem::new', props='p.stacksTo(16)', tab='arsenal')
item('village_carving_knife', 'Village Carving Knife', 'RPG:items/villagecarvingknife', ctor='p -> new CarvingKnifeItem(p, 1)', props='p.sword(ToolMaterial.COPPER, 1f, -2f)', tab='tools', model='handheld')
for kid, kname, ktex, tier, mat_ in [('flint_carving_knife', 'Flint Carving Knife', 'carvingknifeflint', 0, 'WOOD'),
                                     ('copper_carving_knife', 'Copper Carving Knife', 'carvingknifecopper', 1, 'COPPER'),
                                     ('iron_carving_knife', 'Iron Carving Knife', 'carvingknife', 2, 'IRON'),
                                     ('salmon_carving_knife', 'Salmon Carving Knife', 'carvingknifesalmon', 2, 'IRON'),
                                     ('golden_carving_knife', 'Golden Carving Knife', 'carvingknifegold', 3, 'GOLD'),
                                     ('diamond_carving_knife', 'Diamond Carving Knife', 'carvingknifediamond', 4, 'DIAMOND'),
                                     ('netherite_carving_knife', 'Netherite Carving Knife', 'carvingknifenetherite', 5, 'NETHERITE')]:
    item(kid, kname, f'DA:item/{ktex}', ctor=f'p -> new CarvingKnifeItem(p, {tier})', props=f'p.sword(ToolMaterial.{mat_}, 1f, -2f)', tab='tools', model='handheld',
         desc='Creatures slain with a carving knife yield their rare materials.' if tier == 0 else None)

CHISELS = [('andesite', 'DA:item/andesitechisel'), ('calcite', 'DA:item/calcitechisel'), ('diorite', None), ('granite', None), ('tuff', None),
           ('sandstone', None), ('deepslate', None), ('blackstone', None), ('basalt', None), ('quartz', None), ('obsidian', None),
           ('end_stone', None), ('netherrack', None), ('stone', None), ('mossy_cobblestone', None), ('diamond', None)]
CHISEL_COL = {'diorite': '#e0e0dc', 'granite': '#a8705a', 'tuff': '#6c6d66', 'sandstone': '#dccf9c', 'deepslate': '#4a4a50',
              'blackstone': '#2c272c', 'basalt': '#555459', 'quartz': '#efe9e2', 'obsidian': '#2a1a40', 'end_stone': '#e6e8aa',
              'netherrack': '#7a2a2a', 'stone': '#8a8a8a', 'mossy_cobblestone': '#5f7a4a', 'diamond': '#4fe0d8'}
for cname, ctex in CHISELS:
    tex = ctex or from_template('chisel', CHISEL_COL[cname])
    nm = cname.replace('_', ' ').title()
    item(f'{cname}_chisel', f'{nm} Chisel', tex, ctor=f'p -> new ChiselItem(p, "{cname}")', props='p.durability(64)' if cname != 'diamond' else 'p.durability(512)', tab='tools',
         desc=f'Use on stone-like blocks to transmute them into {nm.lower()}. Sneak-use on a solid block to carve a villager statue.' if cname != 'diamond'
         else 'Carves villager statues from any precious block.')

# Tech (Halocraft + ability modules)
item('assault_rifle', 'Assault Rifle', 'HALO:items/assault_rifle', ctor='p -> new RifleItem(p, RifleItem.Kind.ASSAULT)', props='p.stacksTo(1).durability(1500)', tab='arsenal', model='handheld',
     desc='Hold use to fire. Reloads from Standard Ammunition.')
item('battle_rifle', 'Battle Rifle', 'HALO:items/battle_rifle', ctor='p -> new RifleItem(p, RifleItem.Kind.BATTLE)', props='p.stacksTo(1).durability(1500)', tab='arsenal', model='handheld',
     desc='Fires precise three-round bursts.')
item('beam_rifle', 'Beam Rifle', tint('HALO:items/battle_rifle', '#7fd6ff'), ctor='p -> new RifleItem(p, RifleItem.Kind.BEAM)', props='p.stacksTo(1).durability(600).rarity(Rarity.RARE)', tab='arsenal', model='handheld',
     desc='A piercing beam powered by Plasma Cells.')
item('standard_ammunition', 'Standard Ammunition', 'HALO:items/ammunition', tab='arsenal')
item('plasma_cell', 'Plasma Cell', 'HALO:items/plasmabattery', tab='arsenal')
item('ammo_case', 'Ammo Case', 'HALO:items/ammocrate', ctor='AmmoCaseItem::new', tab='arsenal', desc='Crack open for a stack of ammunition.')
MODULES = [('active_camo', 'Active Camouflage Module', '#3a6a8a', 'Turns you invisible for 10 seconds.'),
           ('armor_lock', 'Armor Lock Module', '#c0c040', 'Become invulnerable for 4 seconds, then release an EMP blast.'),
           ('drop_shield', 'Drop Shield Module', '#40b0e0', 'Deploys a healing bubble.'),
           ('jet_pack', 'Jet Pack Module', '#e07030', 'Hold jump while airborne to thrust upward.'),
           ('sprint', 'Sprint Module', '#50d050', 'A burst of speed.')]
for mid, mname, mcol, mdesc in MODULES:
    tex = 'JELLY:active_camo_gen2' if mid == 'active_camo' else from_template('module', mcol, '#202428', '#5a6068')
    item(f'{mid}_module', mname, tex, ctor=f'p -> new AbilityModuleItem(p, AbilityModuleItem.Kind.{mid.upper()})', props='p.stacksTo(1).durability(64)', tab='arsenal', desc=mdesc)

# ============================================================== SCROLLS OF CALLING (HG kits) and quest rewards
CALLINGS = ['lumberjack', 'jackhammer', 'fireman', 'poseidon', 'werewolf', 'frosty', 'worm', 'cultivator', 'fletcher', 'necro',
            'stomper', 'turtle', 'vampire', 'viper', 'snail', 'kangaroo', 'boxer', 'berserker', 'cannibal', 'beastmaster', 'miner', 'monster']
for cname in CALLINGS:
    item(f'{cname}_calling', f'Scroll of the {cname.title()}', from_template('scroll', '#e8dcb0', '#cbbf90', '#7a5a30'),
         ctor=f'p -> new CallingScrollItem(p, Calling.{cname.upper()})', props='p.stacksTo(1).rarity(Rarity.UNCOMMON)', tab='empire')

# ============================================================== HALOS
HALOS = [('explosions', '#ff7a30'), ('thorns', '#3a8a3a'), ('power', '#d02020'), ('fire', '#ff9a20'), ('poison', '#6ac040'), ('light', '#fff6a0'),
         ('darkness', '#3a2a50'), ('healing', '#ff70a0'), ('invisibility', '#c8c8d8'), ('water', '#3070e0'), ('swiftness', '#80e0ff'),
         ('gravity', '#a070ff'), ('might', '#a0a0a8'), ('life', '#50ff70'), ('death', '#202020')]
for hid, hcol in HALOS:
    item(f'halo_of_{hid}', f'Halo of {hid.title()}', from_template('halo', hcol), ctor=f'p -> new HaloItem(p, HaloItem.Kind.{hid.upper()})',
         props='p.humanoidArmor(ModArmorMaterials.HALO, ArmorType.HELMET).rarity(Rarity.EPIC)', tab='arcana')

# ============================================================== WEAPON CLASSES x MATERIALS
WMATS = [('wooden', 'WOOD', '#8a6a3a'), ('stone', 'STONE', '#8a8a8a'), ('copper', 'COPPER', '#d8774a'), ('iron', 'IRON', '#d8d8d8'),
         ('golden', 'GOLD', '#f4d03f'), ('diamond', 'DIAMOND', '#4fe6d6'), ('netherite', 'NETHERITE', '#4a4246'), ('steel', 'ModTiers.STEEL', '#9fb4c8')]
WCLASSES = {  # class: (name, damage baseline, speed baseline)
    'club': ('Club', 4.5, -3.0), 'hammer': ('Hammer', 6.0, -3.2), 'battleaxe': ('Battleaxe', 6.5, -3.1), 'katana': ('Katana', 3.5, -2.0),
    'truncheon': ('Truncheon', 2.0, -2.2), 'knife': ('Knife', 2.0, -1.6), 'claymore': ('Claymore', 7.0, -3.3), 'scythe': ('Scythe', 5.0, -2.8),
}
SCYTHE_TEX = {'wooden': 'scythewood', 'stone': 'scythestone', 'copper': 'scythecopper', 'iron': 'scytheiron', 'golden': 'scythegold', 'diamond': 'scythediamond'}
CLAY_TEX = {'copper': 'copperclaymore', 'iron': 'ironclaymore', 'golden': 'goldclaymore', 'diamond': 'diamondclaymore', 'netherite': 'netheriteclaymore'}
for wcls, (wname, wdmg, wspd) in WCLASSES.items():
    for mkey, mtier, mcol in WMATS:
        tier = mtier if '.' in mtier else f'ToolMaterial.{mtier}'
        handle = '#5a3a20' if mkey != 'wooden' else '#6a4a2a'
        if wcls == 'scythe':
            tex = f'RPG:items/{SCYTHE_TEX[mkey]}' if mkey in SCYTHE_TEX else tint_where('RPG:items/scytheiron', mcol, lambda r, g, b: is_grayish(r, g, b) and r > 90)
        elif wcls == 'claymore':
            tex = f'DA:block/{CLAY_TEX[mkey]}' if mkey in CLAY_TEX else tint_where('DA:block/ironclaymore', mcol, lambda r, g, b: is_grayish(r, g, b) and r > 90)
        else:
            tex = from_template(wcls, mcol, handle)
        mname = {'wooden': 'Wooden', 'golden': 'Golden'}.get(mkey, mkey.title())
        item(f'{mkey}_{wcls}', f'{mname} {wname}', tex, ctor=f'p -> new MashWeaponItem(p, WeaponClass.{wcls.upper()})',
             props=f'p.sword({tier}, {wdmg}f, {wspd}f)' + ('.fireResistant()' if mkey == 'netherite' else ''), tab='arsenal', model='handheld')
for sid, sname, stex in [('emerald_scythe', 'Emerald Scythe', 'scytheemerald'), ('ruby_scythe', 'Ruby Scythe', 'scytheruby')]:
    item(sid, sname, f'RPG:items/{stex}', ctor='p -> new MashWeaponItem(p, WeaponClass.SCYTHE)',
         props=f'p.sword(ModTiers.{sid.split("_")[0].upper()}, 5f, -2.8f)', tab='arsenal', model='handheld')

# Village (Empire-issue) gear from RPG
for vm, vtier in [('copper', 'COPPER'), ('iron', 'IRON')]:
    for vcls, vtex, vprops, vctor in [('sword', f'village{vm}sword', f'p.sword(ToolMaterial.{vtier}, 3f, -2.4f)', 'p -> new MashWeaponItem(p, WeaponClass.SWORD)'),
                                      ('axe', f'village{vm}axe', f'p.axe(ToolMaterial.{vtier}, 6f, -3.1f)', 'Item::new'),
                                      ('hammer', f'village{vm}hammer', f'p.sword(ToolMaterial.{vtier}, 6f, -3.2f)', 'p -> new MashWeaponItem(p, WeaponClass.HAMMER)'),
                                      ('knife', f'villageknife{vm}', f'p.sword(ToolMaterial.{vtier}, 2f, -1.6f)', 'p -> new MashWeaponItem(p, WeaponClass.KNIFE)'),
                                      ('scythe', f'scythe{vm}village', f'p.sword(ToolMaterial.{vtier}, 5f, -2.8f)', 'p -> new MashWeaponItem(p, WeaponClass.SCYTHE)')]:
        item(f'village_{vm}_{vcls}', f'Village {vm.title()} {vcls.title()}', f'RPG:items/{vtex}', ctor=vctor, props=vprops, tab='arsenal', model='handheld')

# Ore tool sets: steel / ruby / bedrock / astroite
TOOLSETS = [('steel', 'STEEL', 'QUACK', {'sword': 'steel_sword', 'pickaxe': 'steel_pickaxe', 'axe': 'steel_axe', 'shovel': 'steel_shovel', 'hoe': 'steel_hoe'}),
            ('ruby', 'RUBY', 'QUACK', {'sword': 'ruby_sword', 'pickaxe': 'ruby_pickaxe', 'axe': 'ruby_axe', 'shovel': 'ruby_shovel', 'hoe': 'ruby_hoe'}),
            ('bedrock', 'BEDROCK', 'DA', {'sword': 'bedrocksword', 'pickaxe': 'bedrockpickaxe', 'axe': 'bedrockaxe', 'shovel': 'bedrockshovel', 'hoe': 'bedrockhoe'}),
            ('astroite', 'ASTROITE', None, {})]
TOOLTYPES = {'sword': ('Sword', 'sword', 3, -2.4, 'MashWeaponItem::sword'), 'pickaxe': ('Pickaxe', 'pickaxe', 1, -2.8, 'Item::new'),
             'axe': ('Axe', 'axe', 6, -3.1, 'Item::new'), 'shovel': ('Shovel', 'shovel', 1.5, -3.0, 'Item::new'), 'hoe': ('Hoe', 'hoe', 0, -1, 'Item::new')}
for tk, tmat, tmod, texs in TOOLSETS:
    for tt, (tname, tmeth, tdmg, tspd, tctor) in TOOLTYPES.items():
        if tmod: tex = f'{tmod}:{"items/" if tmod == "QUACK" else "item/"}{texs[tt]}'
        else: tex = tint_where(f'V:item/iron_{tt}', '#7a64b0', lambda r, g, b: is_grayish(r, g, b) and r > 60)
        ctor = tctor
        if tk == 'bedrock' and tt == 'pickaxe': ctor = 'BedrockPickaxeItem::new'
        item(f'{tk}_{tt}', f'{tk.title()} {tname}', tex, ctor=ctor, props=f'p.{tmeth}(ModTiers.{tmat}, {tdmg}f, {tspd}f)' + ('.fireResistant()' if tk == 'bedrock' else ''),
             tab='tools', model='handheld', desc='Can break bedrock.' if (tk == 'bedrock' and tt == 'pickaxe') else None)
item('ruby_ring', 'Ruby Ring', 'QUACK:items/red_ring', ctor='RubyRingItem::new', props='p.stacksTo(1).rarity(Rarity.RARE)', tab='arcana',
     desc='Held in the off-hand: slowly mends your wounds.')
for chid, chname, chtex, chdesc in [('emerald_charm', 'Emerald Charm', 'DA:item/emeraldcharm', 'Off-hand: villagers treat you as a hero.'),
                                    ('diamond_charm', 'Diamond Charm', 'DA:item/diamondcharm', 'Off-hand: steadfast resistance.'),
                                    ('amethyst_charm', 'Amethyst Charm', 'DA:item/amethystcharm', 'Off-hand: absorb more experience.')]:
    item(chid, chname, chtex, ctor=f'p -> new CharmItem(p, "{chid}")', props='p.stacksTo(1).rarity(Rarity.RARE)', tab='arcana', desc=chdesc)

# ============================================================== ARMOR SETS
# key, display, pieces(subset of HCLB), layer source, icon sources dict, stats(dur, [boots,legs,chest,helm], ench, tough, kb, repair), perk desc
def vlayer(kind, color, base='leather'):
    """generate (layer1, layer2) from vanilla armor textures, tinted."""
    l1 = tint(f'V:entity/equipment/humanoid/{base}', color); l2 = tint(f'V:entity/equipment/humanoid_leggings/{base}', color)
    return (l1, l2)
def vicons(color, base='leather', pieces='HCLB'):
    names = {'H': 'helmet', 'C': 'chestplate', 'L': 'leggings', 'B': 'boots'}
    return {p: tint(f'V:item/{base}_{names[p]}', color) for p in pieces}

ARMOR = []
def armor(key, name, pieces, layers, icons, stats, perk, names=None):
    ARMOR.append(dict(key=key, name=name, pieces=pieces, layers=layers, icons=icons, stats=stats, perk=perk, names=names or {}))

def mlayers(mod, base):  # from mod armor textures
    return (f'{mod}:models/armor/{base}_layer_1', f'{mod}:models/armor/{base}_layer_2')

armor('tattered', 'Tattered', 'HCLB', vlayer('x', '#b8a888'), vicons('#b8a888'), (4, [1, 1, 2, 1], 10, 0, 0, 'madmodmash:fabric'),
      'Humble clothes. Repair them with Fabric.', {'H': 'Tattered Hood', 'C': 'Tattered Shirt', 'L': 'Tattered Trousers', 'B': 'Tattered Shoes'})
armor('gambeson', 'Gambeson', 'HCLB', mlayers('DA', 'gambeson'), vicons('#e8e8e8'), (12, [1, 3, 4, 2], 12, 0, 0, 'madmodmash:thick_wool'),
      'Cannot burn. Halves fire damage.')
for fp, fcol, fstats in [('copper', '#d8774a', (12, [2, 4, 5, 2], 10, 0, 0.05, 'minecraft:copper_block')),
                         ('iron', '#d8d8d8', (18, [2, 5, 7, 3], 9, 1, 0.1, 'minecraft:iron_block')),
                         ('gold', '#f4d03f', (9, [2, 4, 6, 3], 25, 0, 0.05, 'minecraft:gold_block')),
                         ('diamond', '#4fe6d6', (36, [3, 6, 9, 4], 10, 2.5, 0.15, 'minecraft:diamond_block')),
                         ('netherite', '#4a4246', (40, [3, 7, 9, 4], 15, 3.5, 0.2, 'minecraft:netherite_block'))]:
    armor(f'{fp}_full_plate', f'{fp.title()} Full Plate', 'HCB', mlayers('DA', f'{fp}fullplate'),
          {'H': f'DA:item/{fp}fullplatehelmetitem', 'C': f'DA:item/{fp}fullplatechestitem', 'B': f'DA:item/{fp}fullplatebootsitem'}, fstats,
          'Full plate: knockback resistance and extra armor, but you move a little slower.')
armor('alpha_leather', 'Alpha Leather', 'HCLB', mlayers('DA', 'alphaleather'),
      {'H': 'DA:item/alphaleatherhelm', 'C': 'DA:item/alphaleatherchest', 'L': 'DA:item/alphaleatherlegs', 'B': 'DA:item/alphaleatherboots'},
      (16, [2, 4, 5, 2], 15, 1, 0, 'madmodmash:alpha_leather'), 'Full set: Strength at night, and wild animals respect you.')
armor('amethyst', 'Amethyst', 'HCLB', mlayers('DA', 'amethyst'),
      {'H': 'DA:item/amethysthelmet', 'C': 'DA:item/amethystchestplate', 'L': 'DA:item/amethystleggings', 'B': 'DA:item/amethystboots'},
      (20, [2, 5, 6, 2], 22, 1, 0, 'minecraft:amethyst_shard'), 'Full set: reduces magic damage and boosts experience gained.')
armor('steel', 'Steel', 'HCLB', mlayers('QUACK', 'steel'),
      {'H': 'QUACK:items/steel_head', 'C': 'QUACK:items/steel_body', 'L': 'QUACK:items/steel_leggings', 'B': 'QUACK:items/steel_boots'},
      (22, [2, 6, 7, 2], 10, 1.5, 0.1, 'madmodmash:steel_ingot'), 'Full set: immune to knockback from explosions.')
armor('ruby', 'Ruby', 'HCLB', mlayers('QUACK', 'ruby'),
      {'H': 'QUACK:items/ruby_head', 'C': 'QUACK:items/ruby_body', 'L': 'QUACK:items/ruby_leggings', 'B': 'QUACK:items/ruby_boots'},
      (26, [3, 5, 7, 3], 18, 1, 0, 'madmodmash:ruby'), 'Full set: +4 max health.')
armor('bedrock', 'Bedrock', 'HCLB', mlayers('QUACK', 'bedrock'),
      {'H': 'QUACK:items/bedrock_head', 'C': 'QUACK:items/bedrock_body', 'L': 'QUACK:items/bedrock_leggings', 'B': 'QUACK:items/bedrock_boots'},
      (200, [4, 7, 9, 4], 5, 4, 0.4, 'madmodmash:bedrock_dust'), 'Nearly unbreakable. Full set: immovable, but slow.')
armor('herobrine', 'Herobrine', 'HCLB', mlayers('DUCK', 'herobrine'),
      {'H': 'DUCK:items/herobrine_head', 'C': 'DUCK:items/herobrine_body', 'L': 'DUCK:items/herobrine_leggings', 'B': 'DUCK:items/herobrine_boots'},
      (33, [3, 6, 8, 3], 20, 2, 0, 'madmodmash:herobrine_charm'),
      'Become Herobrine. Full set: night vision, glowing gaze, sneak to vanish, and Herobrine\'s haunting no longer targets you.')
armor('village_copper', 'Village Copper', 'HCLB', mlayers('RPG', 'villagecopper_'),
      {'H': 'RPG:items/villagecopperhelmet', 'C': 'RPG:items/villagecopperchestplate', 'L': 'RPG:items/villagecopperleggings', 'B': 'RPG:items/villagecopperboots'},
      (11, [1, 3, 4, 2], 12, 0, 0, 'minecraft:copper_ingot'), 'Empire issue. Full set: villagers rally to you; Hero of the Village while in a village.')
armor('village_iron', 'Village Iron', 'HCLB', mlayers('RPG', 'villageiron_'),
      {'H': 'RPG:items/villageironhelmet', 'C': 'RPG:items/villageironchestplate', 'L': 'RPG:items/villageironleggings', 'B': 'RPG:items/villageironboots'},
      (16, [2, 5, 6, 2], 12, 0.5, 0, 'minecraft:iron_ingot'), 'Empire issue. Full set: villagers rally to you; Hero of the Village while in a village.')
armor('magma', 'Magma Mail', 'HCLB', mlayers('RPG', 'magmaarmor_'),
      {'H': 'RPG:items/magmahelmet', 'C': 'RPG:items/magmachestplate', 'L': 'RPG:items/magmaleggings', 'B': 'RPG:items/magmaboots'},
      (20, [2, 5, 6, 2], 12, 1, 0, 'madmodmash:magma_scales'), 'Full set: fire immunity, attackers burst into flame, and your boots cool lava into obsidian.')
armor('wetsuit', 'Wetsuit', 'HCB', mlayers('RPG', 'wetsuit_'), {'H': tint('RPG:items/wetsuit', '#2a4a6a'), 'C': 'RPG:items/wetsuit', 'B': tint('RPG:items/wetsuit', '#1a3a5a')},
      (10, [1, 2, 3, 1], 12, 0, 0, 'madmodmash:squid_skin'), 'Perfect movement underwater: water breathing, dolphin\'s grace and fast mining.')
armor('endermite_slides', 'Endermite Slides', 'HCLB', mlayers('RPG', 'endermiteslides_'),
      {'H': tint('RPG:items/endermiteslides', '#7a4aa0'), 'C': tint('RPG:items/endermitesoftshell', '#6a3a90'), 'L': tint('RPG:items/endermitesoftshell', '#5a2a80'), 'B': 'RPG:items/endermiteslides'},
      (12, [2, 3, 4, 2], 15, 0, 0, 'madmodmash:endermite_softshell'), 'Boots: glide across the ground as if on ice. Full set: endermen ignore you.')
armor('mystery', 'Mystery', 'HCLB', mlayers('RPG', 'mysteryarmortemp_'),
      {'H': 'RPG:items/mysteryhelmetbaseicon', 'C': 'RPG:items/mysterychestplateicon', 'L': 'RPG:items/mysteryleggingsicon', 'B': 'RPG:items/mysterybootsicon'},
      (18, [2, 5, 6, 2], 18, 1, 0, 'madmodmash:bizarre_star'), 'Each piece rolls a hidden perk the first time it is worn. Appraise with a magnifier.')
GEN_ARMOR = [  # key, name, colour, base, stats, perk
    ('evocation', 'Evocation Robe', '#2a2a3a', 'leather', (18, [2, 4, 5, 2], 20, 0, 0, 'madmodmash:unholy_cloth'), 'Full set: vexes answer when you are struck; undead will not target you.'),
    ('ghillie', 'Ghillie', '#4a7a2a', 'leather', (10, [1, 3, 4, 1], 12, 0, 0, 'madmodmash:creeper_leaves'), 'Full set: monsters cannot see you unless you strike first or come too close.'),
    ('wolfpelt', 'Wolfpelt Cloak', '#9a9a9a', 'leather', (14, [2, 3, 5, 2], 12, 0, 0, 'madmodmash:wolf_fur'), 'Negates Slowness. Wolves will not attack you.'),
    ('bone', 'Bone', '#e8e4d4', 'chainmail', (14, [1, 4, 5, 2], 12, 0, 0, 'minecraft:bone'), 'Full set: skeletons ignore you.'),
    ('lich', 'Lich', '#3a3a3a', 'chainmail', (24, [3, 5, 7, 3], 18, 1, 0, 'madmodmash:wither_bone'), 'Full set: immune to Wither; your strikes wither foes.'),
    ('dragonscale', 'Dragonscale', '#3a2a5a', 'diamond', (40, [3, 7, 9, 4], 15, 3, 0.1, 'madmodmash:dragon_scale'), 'Chestplate glides like elytra. Full set: fire immunity and resistance.'),
    ('hive', 'Hive', '#e0b030', 'chainmail', (14, [2, 4, 5, 2], 15, 0, 0, 'madmodmash:bee_chitin'), 'Full set: bees defend you; hold jump to hover on bee wings.'),
    ('plumage', 'Plumage', '#f0f0f0', 'leather', (10, [1, 2, 3, 1], 15, 0, 0, 'madmodmash:stiff_feather'), 'Full set: no fall damage, slow descent while sneaking in the air.'),
    ('spectre', 'Spectre', '#2a1a3a', 'chainmail', (22, [2, 5, 6, 2], 18, 1, 0, 'madmodmash:spectral_scales'), 'Full set: endermen stay calm; when hit, you sometimes blink away.'),
    ('guardian', 'Guardian Mail', '#5a9a8a', 'chainmail', (18, [2, 5, 6, 2], 12, 0.5, 0, 'madmodmash:guardian_scales'), 'Full set: Dolphin\'s Grace and long breath.'),
    ('elder_guardian', 'Elder Guardian Mail', '#c8c8b0', 'diamond', (28, [3, 6, 7, 3], 15, 1.5, 0, 'madmodmash:elder_guardian_scales'), 'Full set: Conduit Power and immunity to Mining Fatigue.'),
    ('phantom', 'Phantom', '#3a4a7a', 'leather', (14, [1, 3, 4, 2], 15, 0, 0, 'madmodmash:phantom_hide'), 'Chestplate glides. Full set: phantoms leave you be.'),
    ('arctic', 'Arctic', '#f4f8ff', 'leather', (16, [2, 4, 5, 2], 12, 0, 0, 'madmodmash:polar_pelt'), 'Full set: immune to freezing; run fast on snow and ice.'),
    ('mark_v', 'Mark V', '#6a7a4a', 'iron', (30, [3, 6, 8, 3], 10, 2, 0.05, 'madmodmash:steel_ingot'), 'Full set: an energy shield recharges when you avoid damage for 5 seconds.'),
    ('wizard', 'Wizard', '#3a3ab0', 'leather', (12, [1, 3, 4, 2], 30, 0, 0, 'minecraft:lapis_lazuli'), 'Full set: magic cards are often free; the land itself lends you its power.'),
    ('slime', 'Slime', '#6ad05a', 'leather', (12, [1, 3, 4, 2], 12, 0, 0, 'minecraft:slime_ball'), 'Full set: bounce instead of taking fall damage; slimes are friendly.'),
    ('shulker', 'Shulker', '#9a6a9a', 'iron', (24, [2, 5, 7, 3], 15, 1, 0, 'madmodmash:shulker_chitin'), 'Full set: your strikes levitate foes.'),
    ('fox_hunter', 'Fox Hunter', '#e07a2a', 'leather', (12, [1, 3, 4, 2], 15, 0, 0, 'madmodmash:fox_pelt'), 'Full set: night vision; sneaking makes you invisible.'),
    ('blazebone', 'Blazebone', '#f0a020', 'iron', (22, [2, 5, 6, 3], 15, 1, 0, 'madmodmash:fibrous_flame'), 'Full set: fire resistance; nearby enemies catch fire.'),
    ('bat', 'Bat Suit', '#2a2a2a', 'leather', (14, [2, 3, 5, 2], 15, 0, 0, 'madmodmash:bat_wing'), 'Full set: night vision and slow falling; sneak-fall to glide.'),
    ('hoglord', 'Hoglord', '#a06a4a', 'iron', (22, [2, 5, 7, 3], 12, 1, 0.1, 'madmodmash:hoglin_tusk'), 'Full set: piglins think you are one of them; charge harder.'),
    ('astroite', 'Astroite', '#6a5a9a', 'iron', (28, [3, 6, 7, 3], 15, 2, 0, 'madmodmash:astroite_chunk'), 'Full set: low gravity and safe landings.'),
    ('fossil', 'Fossil', '#d8d0b0', 'iron', (20, [2, 5, 6, 2], 10, 1, 0.25, 'minecraft:bone_block'), 'Full set: unshakeable knockback resistance.'),
    ('silk', 'Silk Robe', '#f0f0f8', 'leather', (8, [1, 2, 3, 1], 22, 0, 0, 'minecraft:string'), 'Full set: spiders are friendly and cobwebs do not slow you.'),
    ('witch', 'Witch Robe', '#4a2a5a', 'leather', (12, [1, 3, 4, 2], 25, 0, 0, 'madmodmash:nasty_cloth'), 'Full set: harmful potions last half as long; witches are friendly.'),
    ('vile', 'Vile Leather', '#4a3a5a', 'leather', (20, [2, 5, 6, 2], 10, 1, 0.1, 'madmodmash:corrupt_leather'), 'Full set: ravagers and illager beasts ignore you.'),
]
for gk, gname, gcol, gbase, gstats, gperk in GEN_ARMOR:
    armor(gk, gname, 'HCLB', vlayer('x', gcol, gbase), vicons(gcol, 'leather' if gbase == 'leather' else ('chainmail' if gbase == 'chainmail' else 'iron')), gstats, gperk)
# halo equipment asset (gold circlet drawn on head area)
HALO_LAYER = None

# ============================================================== BLOCKS
from texlib import hexrgb
from PIL import Image, ImageDraw

def toy_brick(color):
    c = hexrgb(color); im = Image.new('RGBA', (16, 16), c + (255,)); d = ImageDraw.Draw(im)
    dark = tuple(int(v * 0.72) for v in c); light = tuple(min(255, int(v * 1.25 + 20)) for v in c)
    d.rectangle([0, 0, 15, 15], outline=dark)
    for sx, sy in [(4, 4), (11, 4), (4, 11), (11, 11)]:
        d.ellipse([sx - 3, sy - 3, sx + 2, sy + 2], fill=light, outline=dark)
    return im

STONE_P = 'p.mapColor(MapColor.STONE).strength(1.5f, 6f).requiresCorrectToolForDrops()'
ORE_P = 'p.mapColor(MapColor.STONE).strength(3f, 3f).requiresCorrectToolForDrops()'
DEEP_P = 'p.mapColor(MapColor.DEEPSLATE).strength(4.5f, 3f).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)'
METAL_P = 'p.mapColor(MapColor.METAL).strength(5f, 6f).requiresCorrectToolForDrops().sound(SoundType.METAL)'
WOOL_P = 'p.mapColor(MapColor.WOOL).strength(0.8f).sound(SoundType.WOOL)'
WOOD_P = 'p.mapColor(MapColor.WOOD).strength(2f, 3f).sound(SoundType.WOOD)'

# ores
block('ruby_ore', 'Ruby Ore', tex='QUACK:blocks/ruby_ore', props=ORE_P, loot=('ore', 'madmodmash:ruby', 1, 1), tool='pickaxe', level='iron')
block('deepslate_ruby_ore', 'Deepslate Ruby Ore', tex=tint('QUACK:blocks/ruby_ore', '#4a4a50', keep=0.35), props=DEEP_P, loot=('ore', 'madmodmash:ruby', 1, 1), tool='pickaxe', level='iron')
block('plastic_ore', 'Plastic Ore', tex='QUACK:blocks/legoplastic_ore', props=ORE_P, loot=('ore', 'madmodmash:plastic_gem', 1, 3), tool='pickaxe', level='stone',
      desc='Bright chunks of plastic: the stuff toy bricks are made of.')
block('bedrock_ore', 'Bedrock Ore', tex='QUACK:blocks/bedrock_ore', props='p.mapColor(MapColor.STONE).strength(50f, 1200f).requiresCorrectToolForDrops()',
      loot=('ore', 'madmodmash:bedrock_dust', 1, 2), tool='pickaxe', level='diamond', desc='Found just above the bedrock floor.')
block('mystery_ore', 'Mystery Ore', tex='DA:block/mysteryore', ctor='p -> new MysteryOreBlock(p)', props=ORE_P + '.lightLevel(s -> 3)', loot='none', tool='pickaxe', level='stone',
      desc='A shimmering ore. Appraise it with a magnifier to learn what it holds.')
block('deepslate_mystery_ore', 'Deepslate Mystery Ore', tex=tint('DA:block/mysteryore', '#5a5a66', keep=0.4), ctor='p -> new MysteryOreBlock(p)', props=DEEP_P + '.lightLevel(s -> 3)', loot='none', tool='pickaxe', level='stone')
block('astroite', 'Astroite', tex='DA:block/asteroid_stone', props='p.mapColor(MapColor.COLOR_PURPLE).strength(3f, 9f).requiresCorrectToolForDrops()',
      loot=('ore', 'madmodmash:astroite_chunk', 1, 2), tool='pickaxe', level='iron', desc='Meteoric stone, light as a whisper.')
block('rough_astroite', 'Rough Astroite', tex='DA:block/craggy_asteroid_stone', props=STONE_P, tool='pickaxe', level='wood')
block('polished_astroite', 'Polished Astroite', tex=hue('DA:block/asteroid_stone', 0, 0.6, 1.2), props=STONE_P, tool='pickaxe', level='wood')
block('crumbling_stone', 'Crumbling Stone', tex='QUACK:blocks/crumblingstone', ctor='CrumblingStoneBlock::new', props='p.mapColor(MapColor.STONE).strength(0.6f).randomTicks()',
      loot='none', tool='pickaxe', desc='Gives way beneath your feet. Cave-ins are made of this.')
# storage
block('ruby_block', 'Block of Ruby', tex='QUACK:blocks/ruby_block', props=METAL_P, tool='pickaxe', level='iron')
block('sapphire_block', 'Block of Sapphire', tex=hue('QUACK:blocks/ruby_block', 220), props=METAL_P, tool='pickaxe', level='iron')
block('steel_block', 'Block of Steel', tex=tint('V:block/iron_block', '#9fb4c8'), props=METAL_P, tool='pickaxe', level='stone')
block('blood_ruby_block', 'Blood Ruby Block', kind='glass', tex='DA:block/transruby', ctor='BloodRubyBlock::new',
      props='p.mapColor(MapColor.COLOR_RED).strength(1.5f).noOcclusion().sound(SoundType.GLASS)', tool='pickaxe', desc='Drinks the blood of those who walk on it, and heals its owner.')
block('wither_bone_block', 'Wither Bone Block', kind='column', tex=('WL:block/witherboneblockside', 'WL:block/witherboneblocktop'),
      props='p.mapColor(MapColor.COLOR_BLACK).strength(2f).sound(SoundType.BONE_BLOCK).requiresCorrectToolForDrops()', ctor='RotatedPillarBlock::new', tool='pickaxe')
block('thick_wool', 'Thick Wool', tex='DA:block/thickwool2', props='p.mapColor(MapColor.WOOL).strength(1.6f).sound(SoundType.WOOL)', desc='Harder to break, and it will not burn.')
block('fur_block', 'Fur Block', tex='RPG:block/furblock', ctor='FurBlock::new', props=WOOL_P, desc='Softens any landing.')
block('fur_carpet', 'Fur Carpet', kind='carpet', tex='RPG:block/furblock', ctor='CarpetBlock::new', props='p.mapColor(MapColor.WOOL).strength(0.1f).sound(SoundType.WOOL)')
block('magma_carpet', 'Ember Carpet', kind='carpet', tex='RPG:block/magma', ctor='EmberCarpetBlock::new', props='p.mapColor(MapColor.NETHER).strength(0.1f).lightLevel(s -> 6)',
      desc='Warms whatever walks over it. Mobs do not care for it.')
block('evocation_carpet', 'Unholy Carpet', kind='carpet', tex='RPG:block/evokercarpet', ctor='CarpetBlock::new', props='p.mapColor(MapColor.COLOR_BLACK).strength(0.1f).sound(SoundType.WOOL)')
block('endermite_mucus_layer', 'Endermite Mucus', kind='carpet', tex='RPG:block/endermitemucuslayer', ctor='CarpetBlock::new',
      props='p.mapColor(MapColor.COLOR_PURPLE).strength(0.1f).friction(0.98f).sound(SoundType.SLIME_BLOCK)', desc='Slippery as ice.')
block('padding', 'Padding', tex='RPG:block/padding', ctor='FurBlock::new', props=WOOL_P, desc='Negates fall damage.')
block('wise_hedge', 'Wise Hedge', kind='leaves', tex='RPG:block/wisenedhedge', ctor='HedgeBlock::new',
      props='p.mapColor(MapColor.PLANT).strength(0.3f).noOcclusion().sound(SoundType.GRASS).dynamicShape()', desc='Players and villagers pass through. Monsters do not.')
block('spike_mat', 'Spike Mat', kind='carpet', tex=tint('V:block/pointed_dripstone_up_tip', '#9a9aa8'), ctor='SpikeMatBlock::new',
      props='p.mapColor(MapColor.METAL).strength(1f).noOcclusion()', desc='Cuts any monster that steps on it, leaving it carved and bleeding.')
block('toxic_web', 'Toxic Web', kind='cross', tex=tint('V:block/cobweb', '#9adf6a'), ctor='ToxicWebBlock::new',
      props='p.mapColor(MapColor.PLANT).strength(4f).noCollision().forceSolidOn()', loot=('item', 'madmodmash:toxic_string'), desc='Poisons and snares anything caught in it.')
block('toxic_sludge', 'Toxic Sludge', tex='QTH:blocks/slimestill', ctor='ToxicSludgeBlock::new', props='p.mapColor(MapColor.COLOR_LIGHT_GREEN).strength(0.5f).sound(SoundType.SLIME_BLOCK).speedFactor(0.4f)',
      loot='none', desc='Spilt from toxin cards. Poisonous and sticky; fades in time.', frames=True)
block('charred_log', 'Charred Log', kind='column', tex=(tint('V:block/oak_log', '#3a3533'), tint('V:block/oak_log_top', '#3a3533')), ctor='CharredLogBlock::new',
      props='p.mapColor(MapColor.COLOR_BLACK).strength(2f).sound(SoundType.WOOD)', tool='axe', desc='Like netherrack: fire on top of it burns forever.')
block('charred_planks', 'Charred Planks', tex=tint('V:block/oak_planks', '#3a3533'), props=WOOD_P, tool='axe')
block('bog_oak_log', 'Bog Oak Log', kind='column', tex=(tint('V:block/oak_log', '#4a3a2a'), tint('V:block/oak_log_top', '#5a4a30')), ctor='RotatedPillarBlock::new', props=WOOD_P, tool='axe')
block('bog_oak_planks', 'Bog Oak Planks', tex=tint('V:block/oak_planks', '#5a4a30'), props=WOOD_P, tool='axe')
block('packed_reeds', 'Packed Reeds', kind='column', tex=(tint('V:block/hay_block_side', '#8a9a5a'), tint('V:block/hay_block_top', '#8a9a5a')),
      ctor='RotatedPillarBlock::new', props='p.mapColor(MapColor.PLANT).strength(0.5f).sound(SoundType.GRASS)')
block('asteroid_bricks', 'Astroite Bricks', tex=tint('V:block/stone_bricks', '#6a5a8a'), props=STONE_P, tool='pickaxe', level='wood')
TOY = [('white', '#f2f2f2'), ('light_gray', '#a0a5a8'), ('dark_gray', '#5a5f62'), ('black', '#1e1e22'), ('red', '#c4281c'), ('dark_red', '#7b2e2f'),
       ('orange', '#da8541'), ('yellow', '#f5cd2f'), ('lime', '#a4bd47'), ('green', '#287f46'), ('sand_green', '#789082'), ('cyan', '#55a5af'),
       ('light_blue', '#6c96bf'), ('blue', '#0d69ab'), ('purple', '#6b327b'), ('pink', '#e4adc8'), ('nougat', '#cc8e68'), ('reddish_brown', '#694027')]
for tk, tc in TOY:
    block(f'{tk}_toy_brick', f'{tk.replace("_", " ").title()} Toy Brick', tex=toy_brick(tc), ctor='ToyBrickBlock::new',
          props='p.mapColor(MapColor.SNOW).strength(0.8f).sound(SoundType.BAMBOO_WOOD)', tab='bricks')
block('glow_toy_brick', 'Glow-in-the-Dark Toy Brick', tex=toy_brick('#d8f0b0'), ctor='ToyBrickBlock::new',
      props='p.mapColor(MapColor.SNOW).strength(0.8f).sound(SoundType.BAMBOO_WOOD).lightLevel(s -> 10)', tab='bricks')

# crates (opened with keys)
CRATES = [('village_crate', 'Village Crate', 'RPG', 'villagecrate', 0), ('copper_crate', 'Copper Crate', 'DA', 'coppercrate', 1),
          ('village_copper_crate', 'Village Copper Crate', 'RPG', 'villagecoppercrate', 1), ('iron_crate', 'Iron Crate', 'DA', 'ironcrate', 2),
          ('gold_crate', 'Gold Crate', 'DA', 'goldcrate', 3), ('emerald_crate', 'Emerald Crate', 'DA', 'emeraldcrate', 4),
          ('amethyst_crate', 'Amethyst Crate', 'DA', 'amethystcrate', 5), ('diamond_crate', 'Diamond Crate', 'DA', 'diamondcrate', 6),
          ('netherite_crate', 'Netherite Crate', 'DA', 'netheritecrate', 7), ('master_crate', 'Master Crate', 'UNOB', 'mastercrate', 8),
          ('alpha_loot_box', 'Alpha Loot Box', 'DA', 'alphachest', 3)]
for cid, cname, cmod, cbase, ctier in CRATES:
    sub = 'block/' if cmod != 'RPG' else 'block/'
    block(cid, cname, kind='crate', tex=(f'{cmod}:{sub}{cbase}front', f'{cmod}:{sub}{cbase}side', f'{cmod}:{sub}{cbase}top'),
          ctor=f'p -> new CrateBlock(p, {ctier})', props='p.mapColor(MapColor.WOOD).strength(2.5f, 1200f).sound(SoundType.WOOD)' if ctier < 8 else 'p.mapColor(MapColor.METAL).strength(-1f, 3600000f)',
          loot='self', tab='empire', tool='axe',
          desc=('Needs a key of the same tier (or a lock pick).' if ctier < 8 else 'Holds the Unobtainables. Only a Master Key will do.'))
block('coin_barrel', "Banker's Barrel", kind='crate', tex=('DA:block/dungeons_barrel_side', 'DA:block/dungeons_barrel_side', 'DA:block/coinbarreltop', 'DA:block/dungeons_barrel_bottom'),
      ctor='CoinBarrelBlock::new', props='p.mapColor(MapColor.WOOD).strength(2.5f).sound(SoundType.WOOD)', tab='empire', tool='axe',
      desc="A shared imperial strongbox. Deposit gildans to bank them; they're safe even if you die.")
block('vending_machine', 'Vending Machine', kind='crate', tex=('WUT:blocks/vendingmachine', tint('V:block/iron_block', '#c03030'), tint('V:block/iron_block', '#c03030')),
      ctor='VendingMachineBlock::new', props='p.mapColor(MapColor.COLOR_RED).strength(3f).sound(SoundType.METAL).lightLevel(s -> 7)', tab='empire', tool='pickaxe',
      desc='Insert a Silver Gildan for a cold soda.')
block('redstone_activator', 'Redstone Activator', kind='crate', tex=('DA:block/activatorfront', 'DA:block/activatorside', 'DA:block/activatortop', 'DA:block/activatorbottom'),
      ctor='RedstoneActivatorBlock::new', props='p.mapColor(MapColor.STONE).strength(3.5f).requiresCorrectToolForDrops()', tab='blocks', tool='pickaxe',
      desc='Pulses redstone on its own when a creature stands before it. Technomancers love these.')
block('continuum_block', 'Continuum Block', kind='column', tex=('DA:block/continuumtop', 'DA:block/continuumtop'), ctor='ContinuumBlock::new',
      props='p.mapColor(MapColor.COLOR_PURPLE).strength(5f, 1200f).lightLevel(s -> 12)', tab='empire', desc='Step on it to return to where you last recalled from.')
block('redstone_core_block', 'Redstone Core', tex='KWIK:blocks/magmaredstone', ctor='RedstoneCoreBlock::new',
      props='p.mapColor(MapColor.FIRE).strength(3f).lightLevel(s -> 9).requiresCorrectToolForDrops()', tool='pickaxe',
      desc='Place atop a T of redstone blocks to build a Redstone Golem.')
block('rope', 'Rope', kind='rope', tex='AA:blocks/rope', ctor='RopeBlock::new', props='p.mapColor(MapColor.WOOL).strength(0.3f).noCollision().sound(SoundType.WOOL)',
      tab='tools', desc='Climbable. Hangs down from the block above.')
block('hooked_grapple', 'Hooked Grapple', kind='cross', tex='AA:blocks/anviltexturenobevel', ctor='HookedGrappleBlock::new',
      props='p.mapColor(MapColor.METAL).strength(0.5f).noCollision()', loot=('item', 'madmodmash:grapple_hook'), item=False)
block('hellrift_patch', 'Hellrift Patch', tex='ZP:blocks/hellrift', ctor='HellriftBlock::new', props='p.mapColor(MapColor.NETHER).strength(0.4f).randomTicks().lightLevel(s -> 5)',
      loot='none', desc='A wound in the world left by Mortis pigs.')

# plants
PLANTS = [('strength', 'vistren', 'Vistren'), ('speed', 'celerus', 'Celerus'), ('resistance', 'armis', 'Armis'), ('regeneration', 'sanatia', 'Sanatia'),
          ('nightvision', 'visus', 'Visus'), ('glowing', 'patet', 'Patet'), ('fireresist', 'arescet', 'Arescet'), ('stasis', 'statia', 'Statia'),
          ('storm', 'tempesta', 'Tempesta'), ('clarity', 'serenus', 'Serenus'), ('poison', 'venenum', 'Venenum'), ('wither', 'mortem', 'Mortem')]
for ptex, pkey, pname in PLANTS:
    block(f'{pkey}_plant', pname, kind='plant', tex=(f'DA:block/plant{ptex}', f'DA:block/plant{ptex}picked'),
          ctor=f'p -> new MysticPlantBlock(p, () -> ModItems.{pkey.upper()}_POWDER.get())', props='p.mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS).randomTicks().offsetType(BlockBehaviour.OffsetType.XZ)',
          loot='self', tab='arcana')
block('emerald_plant', 'Luxine', kind='plant', tex=('DA:block/plantemerald', 'DA:block/plantemerald'), ctor='p -> new MysticPlantBlock(p, () -> ModItems.LUXINE_POWDER.get())',
      props='p.mapColor(MapColor.EMERALD).noCollision().instabreak().sound(SoundType.GRASS).randomTicks().offsetType(BlockBehaviour.OffsetType.XZ)', tab='arcana')
block('shining_plant', 'Fortuna', kind='plant', tex=('RPG:block/shiningplant', 'RPG:block/shiningplant'), ctor='p -> new MysticPlantBlock(p, () -> ModItems.FORTUNA_POWDER.get())',
      props='p.mapColor(MapColor.GOLD).noCollision().instabreak().sound(SoundType.GRASS).randomTicks().lightLevel(s -> 6).offsetType(BlockBehaviour.OffsetType.XZ)', tab='arcana')
block('mystery_plant', 'Mystery Plant', kind='cross', tex='DA:block/mysteryplant', ctor='MysteryPlantBlock::new',
      props='p.mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS).lightLevel(s -> 4).offsetType(BlockBehaviour.OffsetType.XZ)', loot='none', tab='arcana',
      desc='A shimmering plant. Appraise it with a magnifier to reveal its true nature.')
block('mortasia_flower', 'Mortasia', kind='cross', tex='ZP:blocks/mortisflower', ctor='MortasiaFlowerBlock::new',
      props='p.mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS)', loot=('item', 'madmodmash:mortasia_bloom'), tab='arcana',
      desc='Picking it is a very bad idea.')
block('banana_bunch', 'Banana Bunch', kind='cross', tex=from_template('orb', '#f2d23a', '#c9a520', '#5a4a10'), ctor='BananaBunchBlock::new',
      props='p.mapColor(MapColor.PLANT).noCollision().instabreak().sound(SoundType.GRASS)', loot=('item', 'madmodmash:banana', 2), tab='materials')

# statues (villager statues, HmmMod)
STATUES = [('stone', 'HMM:blocks/stone'), ('smooth_stone', 'HMM:blocks/smoothstone'), ('granite', 'HMM:blocks/granite'), ('polished_granite', 'HMM:blocks/smoothgranite'),
           ('diorite', 'HMM:blocks/diorite'), ('polished_diorite', 'HMM:blocks/smoothdiorite'), ('andesite', 'HMM:blocks/andesite'),
           ('polished_andesite', 'HMM:blocks/smoothandesite'), ('mossy', 'HMM:blocks/smoothmossyandesite'), ('sandstone', 'HMM:blocks/sandstone_top'),
           ('red_sandstone', 'HMM:blocks/red_sandstone_top'), ('prismarine', 'HMM:blocks/prismarine'), ('dark_prismarine', 'HMM:blocks/smoothprismarine'),
           ('quartz', 'HMM:blocks/smoothquartz'), ('obsidian', 'HMM:blocks/obsidian'), ('snow', 'HMM:blocks/snow'), ('iron', 'HMM:blocks/smoothiron'),
           ('gold', 'HMM:blocks/smoothgold'), ('lapis', 'HMM:blocks/smoothlapis'), ('emerald', 'HMM:blocks/smoothemerald'),
           ('diamond', 'HMM:blocks/smoothdiamond'), ('bedrock', 'HMM:blocks/bedrock'), ('tuff', 'V:block/tuff'), ('deepslate', 'V:block/polished_deepslate'),
           ('blackstone', 'V:block/polished_blackstone'), ('calcite', 'V:block/calcite'), ('end_stone', 'V:block/end_stone'), ('netherrack', 'V:block/netherrack'),
           ('astroite', 'DA:block/asteroid_stone'), ('ruby', 'QUACK:blocks/ruby_block')]
for sk, stex in STATUES:
    block(f'{sk}_villager_statue', f'{sk.replace("_", " ").title()} Villager Statue', kind='statue', tex=stex, ctor='VillagerStatueBlock::new',
          props='p.mapColor(MapColor.STONE).strength(1.5f, 6f).noOcclusion()', tab='statues', tool='pickaxe')

# illager trophy heads
HEADS = ['base', 'armorer', 'armorer2', 'bandit', 'bearded', 'brodie', 'butcher', 'cartographer', 'chale', 'drunk', 'evil', 'fletcher', 'grin', 'hansley',
         'helmet', 'herobrine', 'illager', 'masquerader', 'mumbomoustache', 'moustache', 'odd', 'old', 'pirate', 'scarred', 'scratchedup', 'serene',
         'shepherd', 'skull', 'sleepy', 'spectacles', 'steve', 'thief', 'villager', 'witch', 'zal']
for hk in HEADS:
    src = 'IH:blocks/pillagerhead' if hk == 'base' else f'IH:blocks/pillagerhead{hk}'
    block(f'illager_head_{hk}', f'{hk.title()} Illager Head', kind='head', tex=src, ctor='TrophyHeadBlock::new',
          props='p.mapColor(MapColor.COLOR_GRAY).strength(1f).noOcclusion()', tab='statues')

# ============================================================== placeables that spawn entities
item('bonfire_kit', 'Bonfire', 'RPG:items/bonfireitemunlit', ctor='BonfireKitItem::new', props='p.stacksTo(4)', tab='empire',
     desc='Place to light a Bonfire: a camp that heals, takes powder infusions, binds recall tokens and draws in travelling merchants.')
item('combat_dummy', 'Combat Dummy', 'DA:item/statuebipedoakitem', ctor='p -> new PlaceEntityItem(p, () -> ModEntities.COMBAT_DUMMY.get())', props='p.stacksTo(16)', tab='tools',
     desc='Shows the damage of every hit. Feed it a Mortem powder and it mutates.')
item('emerald_nexus_core', 'Emerald Nexus Core', tint('V:item/heart_of_the_sea', '#2ad06a'), ctor='p -> new PlaceEntityItem(p, () -> ModEntities.EMERALD_NEXUS.get())',
     props='p.stacksTo(1).rarity(Rarity.RARE)', tab='empire', desc='Raises an Emerald Nexus that musters villager minions to hold the line.')
item('llama_turret_kit', 'Llama Turret', tint('V:item/lead', '#e0d0b0'), ctor='p -> new PlaceEntityItem(p, () -> ModEntities.LLAMA_TURRET.get())',
     props='p.stacksTo(4)', tab='empire', desc='A loyal spitting sentry. Right-click it with an empty hand to pick it back up.')
item('cursed_pumpkin', 'Cursed Pumpkin', tint('V:block/carved_pumpkin', '#f08020', keep=0.6), ctor='CursedPumpkinItem::new', props='p.stacksTo(1).rarity(Rarity.EPIC)', tab='arcana',
     desc='Set it down at night and wait for the hoofbeats.')
item('ritual_candle', 'Ritual Candle', 'V:item/black_candle', ctor='RitualCandleItem::new', props='p.stacksTo(16)', tab='arcana',
     desc='Light one at an Unholy Ritual site to disrupt it.')

# ============================================================== job sites for Empire professions
def board(icon):
    base = load('V:block/spruce_planks'); ic = load(icon).resize((12, 12), Image.NEAREST)
    base.alpha_composite(ic, (2, 2)); return base
block('contract_board', 'Contract Board', kind='crate', tex=(board('DA:item/huntcontractbasenew'), 'V:block/spruce_planks', 'V:block/spruce_planks'),
      ctor='JobSiteBlock::new', props='p.mapColor(MapColor.WOOD).strength(2f).sound(SoundType.WOOD)', tab='empire', tool='axe',
      desc='Job site of the Contractor, who deals in hunt and gather contracts.')
block('bounty_board', 'Bounty Board', kind='crate', tex=(board('MJ23:creeperwanted'), 'V:block/dark_oak_planks', 'V:block/dark_oak_planks'),
      ctor='JobSiteBlock::new', props='p.mapColor(MapColor.WOOD).strength(2f).sound(SoundType.WOOD)', tab='empire', tool='axe',
      desc='Job site of the Bounty Buyer, who pays for Champion trophies.')
block('imperial_supply_crate', 'Imperial Supply Crate', kind='crate', tex=(tint('RPG:block/villagecratefront', '#3a9a5a', keep=0.5),
      tint('RPG:block/villagecrateside', '#3a9a5a', keep=0.5), tint('RPG:block/villagecratetop', '#3a9a5a', keep=0.5)),
      ctor='JobSiteBlock::new', props='p.mapColor(MapColor.EMERALD).strength(2.5f).sound(SoundType.WOOD)', tab='empire', tool='axe',
      desc="Job site of the Quartermaster, who sells the Emerald Empire's supplies for emeralds.")
