"""Convert pre-26.x loot table JSON (functions/conditions) to the 26.3 format (modifier/condition)."""
def cond(c):
    if c.get('condition') == 'minecraft:match_tool' and 'silk_touch' in str(c): return 'minecraft:tool/can_silk_touch'
    c = dict(c); c['type'] = c.pop('condition'); return c
def conv(o):
    if isinstance(o, list): return [conv(x) for x in o]
    if not isinstance(o, dict): return o
    o = {k: conv(v) for k, v in o.items()}
    if 'functions' in o:
        mods = []
        for f in o.pop('functions'):
            f = dict(f); f['type'] = f.pop('function'); mods.append(f)
        o['modifier'] = mods[0] if len(mods) == 1 else mods
    if 'conditions' in o:
        cs = [cond(c) for c in o.pop('conditions')]
        o['condition'] = cs[0] if len(cs) == 1 else {'type': 'minecraft:all_of', 'terms': cs}
    o.pop('bonus_rolls', None)
    return o
