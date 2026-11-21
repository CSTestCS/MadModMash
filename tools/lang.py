"""Builds lang_extra.json: every non-item/block translation the mod uses."""
import json, os
from content import CALLINGS, HALOS
import entities

L = {}
def t(k, v): L[k] = v
M = 'madmodmash'

for tab, name in [('empire', 'MadModMash: Emerald Empire'), ('arsenal', 'MadModMash: Arsenal'), ('tools', 'MadModMash: Tools & Gadgets'),
                  ('arcana', 'MadModMash: Arcana'), ('materials', 'MadModMash: Materials'), ('blocks', 'MadModMash: Blocks'), ('creatures', 'MadModMash: Creatures')]:
    t(f'itemGroup.{M}.{tab}', name)

for p, n in [('banker', 'Banker'), ('contractor', 'Contractor'), ('bounty_buyer', 'Bounty Buyer'), ('quartermaster', 'Quartermaster')]:
    t(f'entity.{M}.villager.{p}', n)
t(f'entity.{M}.boar_king', 'Boar King'); t(f'entity.{M}.proxy.revealed', 'Unmasked Spy')

effects = {'carving': 'Carved', 'frozen': 'Frozen', 'void_fire': 'Void Fire', 'health_penalty': 'Health Penalty', 'midas_touch': 'Midas Touch',
           'zombie_plague': 'Zombie Plague', 'seer_sight': "Seer's Sight", 'undeath': 'Undeath', 'armor_lock': 'Armor Lock', 'stasis': 'Stasis',
           'subdued': 'Subdued', 'bonfire_flight': 'Bonfire Flight'}
for k, v in effects.items(): t(f'effect.{M}.{k}', v)
for pot, nm in [('midas_touch', 'Midas Touch'), ('frozen', 'Freezing'), ('void_fire', 'Void Fire'), ('health_penalty', 'Health Penalty'),
                ('detect_ores', 'Ore Detection'), ('zombie_plague', 'Zombie Plague'), ('undeath', 'Undeath')]:
    t(f'item.minecraft.potion.effect.{pot}', f'Potion of {nm}')
    t(f'item.minecraft.splash_potion.effect.{pot}', f'Splash Potion of {nm}')
    t(f'item.minecraft.lingering_potion.effect.{pot}', f'Lingering Potion of {nm}')
    t(f'item.minecraft.tipped_arrow.effect.{pot}', f'Arrow of {nm}')

rules = {'herobrine_haunting': ('Herobrine haunting', 'Once Herobrine has been summoned he stalks the nights. Only turning this off ends it.'),
         'mortis_spread': ('Mortis infection spreads', 'Creatures slain by the Mortis Blight rise as Mortis.'),
         'common_mutations': ('Common mutations', 'Champions can mutate regardless of faction Wrath.'),
         'border_control': ('Border control', 'The world border grows with the total Renown of the realm.'),
         'world_events': ('World events', 'Faction Wrath spills over into public world events.'),
         'entry_events': ('Entry events', 'New players arrive through a random entry event.')}
for k, (n, d) in rules.items():
    for key in (f'gamerule.{M}.{k}', f'gamerule.{M}:{k}', f'gamerule.{k}'):
        t(key, n); t(key + '.description', d)

for s, v in [('rifle_fire', 'Rifle fires'), ('beam_fire', 'Beam rifle fires'), ('rifle_equip', 'Rifle reloads'), ('sad_song', 'Mourner sings')]:
    t(f'subtitles.{M}.{s}', v)

cfg = {'championChance': 'Champion chance', 'alphaChance': 'Alpha chance', 'eventIntervalMinutes': 'Event interval (minutes)',
       'eventChance': 'Event chance', 'visitorIntervalTicks': 'Visitor interval (ticks)', 'renownPerPathLevel': 'Renown per Path level'}
for k, v in cfg.items(): t(f'{M}.configuration.{k}', v)
t(f'{M}.configuration.title', 'MadModMash Configuration')

traits = {'void': 'Void', 'air': 'Air', 'fire': 'Fire', 'earth': 'Earth', 'poison': 'Poison', 'stone': 'Stone', 'ice': 'Ice', 'might': 'Might',
          'lightning': 'Lightning', 'life': 'Life', 'shadow': 'Shadow', 'light': 'Light'}
for k, v in traits.items(): t(f'trait.{M}.{k}', v)

bonfire = {'none': 'Warmth', 'strength': 'Strength', 'speed': 'Speed', 'resistance': 'Resistance', 'regeneration': 'Regeneration', 'water_breathing': 'Water Breathing',
           'leaping': 'Leaping', 'glowing': 'Revealing Glow', 'invisibility': 'Invisibility', 'heroism': 'Heroism', 'night_vision': 'Night Vision',
           'luck': 'Luck', 'flight': 'Flight', 'fire_resistance': 'Fire Resistance', 'repulsion': 'Repulsion', 'command_animals': 'Command Animals',
           'protective_light': 'Protective Light', 'stasis': 'Stasis', 'storm': 'Stormy Weather', 'clear_weather': 'Clear Weather',
           'summon_visitor': 'Summon Visitor', 'cleansing': 'Cleansing', 'possession': 'Possession', 'gravity': 'Item Magnet', 'smiting': 'Smiting',
           'growth': 'Plant Growth', 'poison': 'Poison', 'elementalism': 'Elementalism', 'dissolution': 'Dissolution'}
for k, v in bonfire.items(): t(f'bonfire.{M}.{k}', v)

calling_desc = {
    'lumberjack': 'Fell a whole tree by chopping a single log.', 'jackhammer': 'Breaking stone breaks the stone above it as well.',
    'fireman': 'Immune to fire and lightning.', 'poseidon': 'Strength and Speed in water; you never drown while swimming.',
    'werewolf': 'Stronger and faster at night, weaker by day.', 'frosty': 'Run with the speed of the blizzard on snow and ice.',
    'worm': 'Dig dirt instantly and land softly on it.', 'cultivator': 'Saplings and crops grow the instant you plant them.',
    'fletcher': 'Gravel always gives flint; chickens give extra feathers.', 'necro': 'Skeletons fall in one hit and drop extra bones.',
    'stomper': 'Land on enemies to pass your fall damage on to them.', 'turtle': 'Crouch to take far less damage, but you cannot attack while crouched.',
    'vampire': 'Heal whenever you kill.', 'viper': 'Your hits may poison.', 'snail': 'Your hits may slow.', 'kangaroo': 'Jump again in mid-air.',
    'boxer': 'Bare fists hit like a stone sword; you take a little less damage.', 'berserker': 'Each kill gives you Strength (and a little Nausea).',
    'cannibal': 'Your hits starve foes and feed you.', 'beastmaster': 'Tamed beasts grow strong; pocketballs always catch.',
    'miner': 'Mine faster; an apple on a full stomach grants Haste.', 'monster': 'Monsters will not attack you unless you attack first.'}
for c in CALLINGS:
    t(f'calling.{M}.{c}', 'The ' + c.title()); t(f'calling.{M}.{c}.desc', calling_desc[c])

halo_desc = {'explosions': 'Halves explosion damage.', 'thorns': 'Enemies who strike you are pricked; slow regeneration.', 'power': 'Strength II.',
             'fire': 'Fire resistance.', 'poison': 'Never hungry; enemies around you are poisoned.', 'light': 'Night vision, and harmful effects fade.',
             'darkness': 'Enemies around you are blinded and slowed.', 'healing': 'Regeneration II.', 'invisibility': 'Permanent invisibility.',
             'water': 'Water breathing.', 'swiftness': 'Speed II and Haste.', 'gravity': 'Jump Boost III and no fall damage.', 'might': 'Resistance II.',
             'life': 'Health Boost III.', 'death': 'Anything that comes too close withers.'}
for h, _ in HALOS: t(f'halo.{M}.{h}', halo_desc[h])

for e in entities.ENGRAMS + ['wolf', 'wither']: t(f'engram.{M}.{e}', e.replace('_', ' ').title() + ' Engram')

quests = {
    'traitor': ('The Traitor', ['Find the missing villager\'s mourner in a village at night', 'Question three villagers with a Bounty Scanner',
                                 'Scan the villagers until you unmask the Spy', 'Kill the unmasked Spy']),
    'beastmaster': ('The Beastmaster', ['Tame any creature', 'Tame three different kinds of creature', 'Capture a creature in a Pocketball',
                                         'Tame a Golden Eagle after taming four kinds of beast']),
    'bounty_hunter': ('The Bounty Hunter', ['Trade with a Contractor or Bounty Buyer', 'Scan a Champion with a Bounty Scanner',
                                             'Slay a Wanted Champion', 'Subdue a Champion with a truncheon and deliver it alive to a Bounty Buyer']),
    'wartime': ('Wartime', ['Win one battle against the Illager Host', 'Win two battles against the Illager Host', 'Win three battles against the Illager Host',
                            'Win four battles against the Illager Host']),
    'darkest_depths': ('Darkest Depths', ['Descend below Y=0', 'Find a Deep Dark', 'Stand near a Warden', 'Slay a Warden with a carving knife and take its soul'])}
for q, (name, stages) in quests.items():
    t(f'quest.{M}.{q}', name)
    for i, s in enumerate(stages, 1): t(f'quest.{M}.{q}.{i}', s)

events = {'pillager_caravan': 'Pillager Caravan', 'technomancer_siege': 'Technomancer Siege', 'mortis_outbreak': 'Mortis Outbreak', 'unholy_ritual': 'Unholy Ritual',
          'headless_night': 'Night of the Headless Horseman', 'nether_invasion': 'Nether Invasion', 'locust_swarm': 'Plague of Locusts',
          'rogue_golem': 'Rogue Golem', 'wooflin_ambush': 'Wooflin Ambush', 'meteor_shower': 'Meteor Shower', 'cave_in': 'Cave-In'}
lost = {'pillager_caravan': 'The caravan escaped with its captives.', 'technomancer_siege': 'The Technomancer\'s machines dig in.',
        'mortis_outbreak': 'The plague spreads to the villages...', 'unholy_ritual': 'The ritual is complete. Something answers.',
        'headless_night': 'The Horseman rides on into the night.', 'nether_invasion': 'The Nether Legion entrenches itself.',
        'locust_swarm': 'The fields lie stripped bare.', 'rogue_golem': 'The rogue golem wanders off, sparking.', 'wooflin_ambush': 'The pack melts back into the woods.',
        'meteor_shower': 'The shower has passed.', 'cave_in': 'The rumbling stops.'}
for k, v in events.items():
    t(f'event.{M}.{k}', v)
    t(f'msg.{M}.event_lost.{k}', lost[k])

wrath = {'illager_host': 'The Illager Host grows stronger!', 'mortis_blight': 'The undead grow stronger!', 'unholy': 'The Herobrine Cult grows bolder!',
         'nether': 'The Nether Legion gathers!', 'wilds': 'The wilds grow restless!'}
for k, v in wrath.items(): t(f'msg.{M}.wrath_rises.{k}', v)

for k, v in {'shipwrecked': 'You wake on the shore amid splintered planks. The ship is gone.', 'knocked_out': 'Your head throbs. Someone left you here in rags.',
             'camping': 'You wake beside a crackling bonfire. A fine place to start an empire.', 'captive': 'You wake in a pen. The pillagers are not far. Find a way out.'}.items():
    t(f'entry.{M}.{k}', v)

for k, v in {'advancement': 'advancement', 'boss': 'boss slain', 'champion': 'Champion slain', 'command': 'decree', 'contract': 'contract fulfilled',
             'crate': 'crate opened', 'cure': 'villager saved', 'event': 'event won', 'kill': 'monster slain', 'quest': 'quest stage', 'trade': 'Empire trade'}.items():
    t(f'renown.{M}.{k}', v)

msgs = {
    'alpha': 'Alpha %s', 'appraised': 'Appraised: %s', 'bank_deposit': 'Deposited %s copper. Your imperial account holds %s.', 'bank_withdraw': 'Withdrew %s copper.',
    'bonfire_info': 'Bonfire tier %s - %s (%ss left)', 'bonfire_infused': 'The flames turn: %s', 'calling_set': 'You answer the calling of %s.',
    'charter_already': 'Your charter is already signed.', 'charter_no': 'no charter', 'charter_yes': "Emperor's Track", 'charter_signed': "Charter signed! The Emperor's Track is open to you.",
    'companion_hired': '%s joins you.', 'companion_stats': '%s: %s', 'contract_done': 'Contract fulfilled! You are paid %s copper.',
    'contract_gather_short': 'You still need %s more %s.', 'contract_progress': 'Contract: %s / %s', 'crate_locked': 'Locked. Needs a tier %s key.',
    'engram': 'Decrypted: %s', 'event_start': '%s near %s, %s!', 'event_won': 'Victory: %s!', 'herobrine_returns': 'He will return.',
    'herobrine.0': 'I see you.', 'herobrine.1': 'You are not alone.', 'herobrine.2': 'Behind you.', 'herobrine.3': 'This world was mine first.',
    'herobrine.4': 'Turn back.', 'journal_line': '%s - stage %s/4 - next: %s', 'key_too_weak': 'That key is too weak for this crate.',
    'no_return': 'You have nowhere to return to.', 'none': 'none', 'not_alone': 'You are not alone.', 'only_night': 'It only works at night.',
    'path_level': 'Imperial Path level %s reached!', 'path_next': 'Next Path level %s in %s renown. Free: %s | Emperor: %s',
    'pick_too_weak': 'This lock is too fine for a lock pick.', 'plague_begins': 'The Plague has begun...', 'quest_complete': '%s: %s - complete!',
    'quest_done': 'complete', 'quest_next': 'Next: %s', 'rank_gift': 'The Empire rewards your rank: %s', 'rank_up': 'Imperial rank attained',
    'recall_bound': 'Recall tokens now return you here.', 'recall_unbound': 'Bind your recall tokens to a Bonfire first.', 'renown_gain': '+%s Renown (%s)',
    'scan_champion': 'CHAMPION', 'scan_disguise': 'Something is wrong with this one...', 'scan_faction': 'Allegiance: %s', 'scan_head': '%s - %s/%s HP',
    'scan_trait': 'Mutation: %s', 'scan_wanted': 'WANTED', 'status_bank': 'Imperial account: %s copper', 'status_calling': 'Calling: %s',
    'status_quest': '%s: stage %s/4', 'status_rank': 'Rank %s - %s renown - Path level %s (%s)', 'thief': 'A thief snatched your %s!',
    'traitor_start': 'A villager has gone missing. A mourner weeps by the bell...', 'vending_poor': 'The machine wants a Silver Gildan.',
    'visitor': '%s has arrived at your bonfire.', 'wanted_lost': '%s was last seen near %s, %s.', 'wanted_new': 'WANTED: %s, last seen near %s, %s.',
    'wanted_seen': '%s is at %s, %s - %s blocks away.', 'welcome': 'Welcome to the Emerald Empire! Read your Renown Ledger and Quest Journal to begin.',
    'wrath_line': '%s: Wrath %s (tier %s)'}
for k, v in msgs.items(): t(f'msg.{M}.{k}', v)

tips = {'ammo': 'Ammo: %s / %s', 'capture_help': 'Subdue it with a truncheon and lead it to a Bounty Buyer.', 'coins': '%s copper inside',
        'contract_blank': 'Use to draw up the contract.', 'contract_gather': 'Gather %s x %s', 'contract_hunt': 'Hunt %s: %s / %s', 'core': 'Trait: %s %s',
        'core_help': 'Right-click onto a weapon in your inventory to infuse it.', 'kill_help': 'Slay the marked creature.', 'kills': '%s kills (+%s damage)',
        'pocketball': 'Holds: %s', 'powder': 'Bonfire infusion: %s', 'target': 'Target: %s', 'trait': 'Trait: %s %s'}
for k, v in tips.items(): t(f'tooltip.{M}.{k}', v)
t(f'item.{M}.champion_trophy.named', 'Trophy of %s')
t(f'item.{M}.wanted_poster.named', 'Wanted: %s')

json.dump(L, open(os.path.join(os.path.dirname(os.path.abspath(__file__)), 'lang_extra.json'), 'w'), indent=1)
print('extra lang keys', len(L))
