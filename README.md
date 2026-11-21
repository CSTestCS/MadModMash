# MadModMash: The Emerald Empire

A NeoForge mod for **Minecraft 26.3** (NeoForge 26.3.0.115, Java 25) that fuses 27 small mods and
a decade of idea notebooks into one connected adventure.

**Download:** [`dist/madmodmash-1.0.0.jar`](dist/madmodmash-1.0.0.jar). Put it in your `mods` folder.

## How it all fits together

You're an adventurer at the edge of the **Emerald Empire** (the villagers). Four threats keep it
under pressure: the **Illager Host**, the **Mortis Blight**, the **Herobrine Cult** and the
**Nether Legion**, plus the restless **Wilds**. Every piece of content from the source mods plugs
into a handful of shared systems, so loot from one feeds progress in another.

| System | What it does | Built from |
|---|---|---|
| **Renown & Imperial ranks** | Contracts, bounties, events, quests, crates and boss kills earn Renown. It takes you from Peasant up through 12 ranks to Emperor, with a gift at each high rank. | Attributes.txt title ladder |
| **Imperial Path** | A 100-level reward track with a free track and an "Emperor's Track", unlocked by an Imperial Charter (bought with gildans). | `__BattlePass.txt` (paid track → in-game charter, trims → vanilla templates) |
| **Gildans** | One currency (copper → netherite, 9:1). Coin Pouch, Banker's Barrel (banked coins survive death), vending machines, every merchant and contract. | DA Gildans + RPG coins/tokens + HostileBuffs coins |
| **Faction Wrath** | Killing a faction's mobs raises its Wrath. Higher Wrath means more Champions, more mutations and more world events against you. Winning an event cools it down. | HostileBuffs "The undead grow stronger!" |
| **Champions & Alphas** | Any hostile can spawn as a named Champion with a **Trait** mutation, and any animal can spawn as an Alpha. Champions drop trophies, Trait Cores, gildans and engrams. | HostileBuffs, Illager Heroes names, DA mutations/Alphas |
| **Traits** | 12 elements (Void, Air, Fire, Earth, Poison, Stone, Ice, Might, Lightning, Life, Shadow, Light). Right-click a core onto **any** weapon to infuse it. | ___Traits.txt / Abilities.txt + the RPG mod's 36 numbered gems/ingots/shards |
| **Mystery Ores & Plants** | Shimmering blocks you appraise with a magnifier. Ores reveal Trait minerals; plants become 14 named herbs that you harvest with a sickle for powders. | CSA, RPG, DA magnifiers |
| **Bonfires** | Your camp. Throw in one of 28 powders to set an area effect, bind Recall Tokens to it, upgrade its tier, rest to heal, and it draws in 6 kinds of travelling merchant. | RPG + DA bonfires, "Bonfire Effects" list |
| **Mob Materials** | Every vanilla and modded creature has a common and a rare material. Rare drops need a carving knife (or a bleeding "Carved" target). There are 45 armor sets with set bonuses. | `____MobMats.txt`, `__RPG Item Uses.txt` |
| **Contracts & bounties** | Hunt and gather contracts, Wanted Posters that spawn a named Champion for you to track down, a Bounty Scanner, and truncheon "capture alive" bounties. | DA hunt contracts, RPG bounties, ModJelly scanner, ModJam wanted posters |
| **World events** | Pillager Caravan, Technomancer Siege, Mortis Outbreak, Unholy Ritual, Headless Night, Nether Invasion, Locust Swarm, Rogue Golem, Wooflin Ambush, Meteor Shower, Cave-In. Each has a boss bar and either a reward or a consequence. | DA "Public Events", Wartime questline |
| **Unobtainables questlines** | Traitor, Beastmaster, Bounty Hunter, Wartime and Darkest Depths, 4 stages each. The final reward is the Master Crate. | `__UnobtainablesQuestlines.txt` |
| **Callings** | 22 permanent classes (Lumberjack, Kangaroo double-jump, Werewolf, Stomper, Vampire...). | Mc-HG kits |
| **Entry events** | New players arrive Shipwrecked, Knocked Out, Camping or Captive. | DA "Entry Events" |
| **Emerald Empire villagers** | Quartermaster (sells exactly the **EmeraldEmpireplans.txt** list for emeralds), Banker, Contractor, Bounty Buyer, each with its own job-site block. | EmeraldEmpireplans.txt, DA banker/contractor, ModJam bounty buyer |

## Content at a glance

* **~590 items:** 7 gildans, contracts, 10 keys, 7 magnifiers, 36 trait minerals, 28 powders,
  ~70 mob materials, 64 class weapons (club, hammer, battleaxe, katana, truncheon, throwable knife,
  claymore with slash, scythe sweep, each in 8 materials), ore tool sets (steel, ruby, astroite,
  bedrock that can pry up bedrock), Halocraft rifles with ammo and reload, 5 armor-ability modules,
  magic cards and decks, 15 Halos, 22 Calling scrolls, 16 transmutation chisels, a grapple hook,
  pocketballs, enchancers, sodas and more.
* **~170 armor pieces:** 45 sets, each with a set bonus, for example: Magma Mail turns lava to
  obsidian, Mark V has a recharging shield, Hive lets you hover, Ghillie hides you from monsters,
  and Herobrine lets you vanish.
* **155 blocks:** ruby, plastic, bedrock and mystery ores; astroite (meteors); 18 toy-brick colours;
  30 villager statues; 35 illager trophy heads; 11 crate tiers including the Master Crate; Banker's
  Barrel; vending machine; redstone activator; continuum block; rope; hedges; spike, ember and
  slime mats; mystic plants; and Mortasia (picking it starts a plague).
* **94 entity types** using geometry recovered from the original mods' compiled model classes:
  21 material slimes, monkeys with gear, golden eagles, rams, boars (and the Boar King), zephyrs,
  locusts → swarms → hordes, redstone and steel golems, iron mimics, Illager Heroes (14 roles ×
  35 faces), Technomancers, cultists, illager civilians, the whole Mortis family, Zombie Trudger,
  Atlas Zombie, Herobrine, the Headless Horseman, travelling merchants, mourners, statue carvers,
  companions, spies, nexuses and turrets, bonfires, effigies, dummies and loot engrams.
* **The Alphalands:** a dimension (Key of Dimensions) with Alphalands, Meteor Fields and Bog biomes.
* **Gamerules:** `madmodmash:herobrine_haunting` (once Herobrine is summoned, only turning this
  off stops him), `mortis_spread`, `common_mutations`, `border_control`, `world_events` and
  `entry_events`.
* **Commands:** `/mash status`, `/mash wrath [faction amount]`, `/mash event list|start <type>`,
  `/mash renown <player> <n>`, `/mash calling <name>`.

Named characters from other franchises in the idea notes (Star Wars, Halo, LOTR, Bionicle and so
on) became original equivalents: "Mark V" armor, Halos, Spies, Technomancers, toy bricks instead
of LEGO, and Creeper Cola instead of real soda brands.

## Building

```bash
./gradlew build          # needs a Java 25 toolchain; produces build/libs/madmodmash-1.0.0.jar
```

`tools/` holds the Python pipeline (Pillow required) that generated the assets, data and registry
sources: `content.py` (item and block table), `entities.py` (entity table), `models.json` (geometry
recovered from the original mods' compiled model classes), `gen.py` (assets and registries),
`data.py` (loot, trades, tags, recipes, worldgen) and `lang.py`. Run `lang.py`, `data.py`, then `gen.py`.
They read the original mod jars extracted to `work/src/mods/x/<mod>/` (set `MASH_WORK` to use another
folder) and vanilla data in `work/vanilla` (`MASH_VANILLA`). Neither is included here.
