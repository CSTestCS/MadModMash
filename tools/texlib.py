"""Texture sourcing / generation helpers for the MadModMash asset pipeline."""
import os, glob, colorsys
from PIL import Image

# Working folder holding src/mods/x/<mod>/ (the extracted original jars). Override with MASH_WORK.
S = os.environ.get('MASH_WORK', os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), 'work'))
X = os.path.join(S, 'src', 'mods', 'x')
V = os.path.join(S, 'vanilla', 'assets', 'minecraft', 'textures')

MODS = {
    'AA': 'AdventureAwaits', 'CSA': 'CSA_1.0.0', 'DUCK': 'Duckobrine', 'FLUB': 'Flub',
    'HALO': 'HalocraftEvolved_1.0.0', 'HMM': 'HmmMod', 'HB': 'HostileBuffs1.0.6',
    'IH': 'IllagerHeroesBeta1.0.1', 'KWIK': 'KwikMod', 'MJ23': 'ModJamJuly23', 'JELLY': 'ModJelly',
    'MJAMO': 'Modjamo', 'MONKEY': 'MonkeyBusiness', 'NAY': 'Nay', 'NEW': 'NewMobs', 'QUACK': 'Quack',
    'QOL': 'QuackOfLegends2', 'QTH': 'Quackthstone', 'RPG': 'RPGModPre1.0.4b', 'PLAGUE8': 'TheEighthPlague',
    'WL': 'WildLoot1.0.0', 'WLM': 'WildLootMod', 'WUT': 'Wutpak', 'ZP': 'ZombiePlagueMod_1_0_4',
    'DA': 'darkadventures-1.0.9-neoforge-1.21.4', 'LEGO': 'legomc', 'UNOB': 'unobtainables-1.0.1',
}

_index = None
def _build_index():
    global _index
    _index = {}
    for k, d in MODS.items():
        for f in glob.glob(os.path.join(X, d, 'assets', '*', 'textures', '**', '*.png'), recursive=True):
            rel = f.split('/textures/', 1)[1][:-4]
            _index[(k, rel)] = f
            _index.setdefault((k, os.path.basename(rel)), f)

def path(mod, rel):
    if _index is None: _build_index()
    if mod == 'V':
        p = os.path.join(V, rel + '.png')
        if not os.path.exists(p): raise FileNotFoundError(rel)
        return p
    for cand in (rel, rel.replace('item/', 'items/'), rel.replace('items/', 'item/'), rel.replace('block/', 'blocks/'),
                 rel.replace('blocks/', 'block/'), os.path.basename(rel)):
        if (mod, cand) in _index: return _index[(mod, cand)]
    raise FileNotFoundError(f'{mod}:{rel}')

def load(spec):
    """spec: 'MOD:rel/path' or Image"""
    if isinstance(spec, Image.Image): return spec.copy()
    mod, rel = spec.split(':', 1)
    return Image.open(path(mod, rel)).convert('RGBA')

def hexrgb(h):
    h = h.lstrip('#'); return tuple(int(h[i:i+2], 16) for i in (0, 2, 4))

def tint(spec, color, keep=0.0, bright=1.0):
    """Map luminance of image onto a target color (keeps alpha)."""
    im = load(spec); c = hexrgb(color) if isinstance(color, str) else color
    px = im.load()
    for y in range(im.size[1]):
        for x in range(im.size[0]):
            r, g, b, a = px[x, y]
            if a == 0: continue
            l = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
            l = min(1.0, l * 1.25 * bright)
            nr, ng, nb = (int(min(255, cc * l * 1.15)) for cc in c)
            px[x, y] = (int(nr * (1 - keep) + r * keep), int(ng * (1 - keep) + g * keep), int(nb * (1 - keep) + b * keep), a)
    return im

def hue(spec, deg, sat=1.0, val=1.0):
    im = load(spec); px = im.load()
    for y in range(im.size[1]):
        for x in range(im.size[0]):
            r, g, b, a = px[x, y]
            if a == 0: continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            h = (h + deg / 360.0) % 1.0; s = min(1, s * sat); v = min(1, v * val)
            r2, g2, b2 = colorsys.hsv_to_rgb(h, s, v)
            px[x, y] = (int(r2 * 255), int(g2 * 255), int(b2 * 255), a)
    return im

def tint_where(spec, color, pred):
    """Tint only pixels for which pred(r,g,b) is true (e.g. the metal head of a tool)."""
    im = load(spec); c = hexrgb(color); px = im.load()
    for y in range(im.size[1]):
        for x in range(im.size[0]):
            r, g, b, a = px[x, y]
            if a == 0 or not pred(r, g, b): continue
            l = min(1.0, (0.299 * r + 0.587 * g + 0.114 * b) / 255.0 * 1.3)
            px[x, y] = (int(min(255, c[0] * l * 1.1)), int(min(255, c[1] * l * 1.1)), int(min(255, c[2] * l * 1.1)), a)
    return im

def is_grayish(r, g, b):
    return max(r, g, b) - min(r, g, b) < 28

def overlay(base, top):
    b = load(base); t = load(top)
    if t.size != b.size: t = t.resize(b.size, Image.NEAREST)
    b.alpha_composite(t); return b

# ---------------------------------------------------------------- pixel templates
# Weapon templates: '#'=head (material), 'h'=handle(wood), 'g'=grip/guard (dark), '.'=empty
TEMPLATES = {
 'club': [
  "................", "...........###..", "..........#####.", ".........######.", "........######..",
  ".......#####....", "......h###......", ".....hh.........", "....hh..........", "...hh...........",
  "..hh............", ".gg.............", "gg..............", "g...............", "................", "................"],
 'hammer': [
  "................", ".........##.....", "........####....", ".......######...", "......####h##...",
  ".......##h.##...", "........h..#....", ".......h........", "......h.........", ".....h..........",
  "....h...........", "...h............", "..g.............", ".gg.............", "................", "................"],
 'battleaxe': [
  "..........#.....", ".........###....", "........#####...", ".......###h###..", "........##h##...",
  ".........h#.....", "........h.......", ".......h........", "......h.........", ".....h..........",
  "....h...........", "...h............", "..h.............", ".g..............", "g...............", "................"],
 'katana': [
  "...............#", "..............##", ".............##.", "............##..", "...........##...",
  "..........##....", ".........##.....", "........##......", ".......##.......", "....g.##........",
  ".....gg.........", "....hgg.........", "...hh..g........", "..hh............", ".hh.............", "................"],
 'truncheon': [
  "................", "................", "............##..", "...........###..", "..........###...",
  ".........###....", "........###.....", ".......###......", "......g##.......", ".....gg.........",
  "....hg..........", "...hh...........", "..hh............", ".hh.............", "................", "................"],
 'knife': [
  "................", "................", "................", "................", "...........#....",
  "..........##....", ".........##.....", "........##......", ".......##.......", "....g.##........",
  ".....gg.........", "....hgg.........", "...hh..g........", "..hh............", "................", "................"],
 'dagger': [
  "................", "................", "................", "............#...", "...........##...",
  "..........##....", ".........##.....", "........##......", ".......##.......", ".....g##........",
  "......g.........", ".....hg.........", "....h..g........", "...h............", "................", "................"],
 'chisel': [
  "................", "................", "................", "................", "...........#....",
  "..........###...", ".........###....", "........#h#.....", ".......hh.......", "......hh........",
  ".....hh.........", "....hh..........", "...hh...........", "................", "................", "................"],
 'halo': [
  "................", "................", "................", "................", "....########....",
  "...#........#...", "..#..........#..", "..#..........#..", "...#........#...", "....########....",
  "................", "................", "................", "................", "................", "................"],
 'card': [
  "................", "....########....", "....#hhhhhh#....", "....#h####h#....", "....#h#..#h#....",
  "....#h#..#h#....", "....#h####h#....", "....#h#..#h#....", "....#h#..#h#....", "....#h####h#....",
  "....#hhhhhh#....", "....########....", "................", "................", "................", "................"],
 'coin': [
  "................", "................", "................", "......####......", ".....#hhhh#.....",
  "....#hh##hh#....", "....#h#hhhh#....", "....#hh##hh#....", "....#hhhh#h#....", "....#hh##hh#....",
  ".....#hhhh#.....", "......####......", "................", "................", "................", "................"],
 'scroll': [
  "................", "..gggggggggg....", ".g#########g....", "..#hhhhhhh#.....", "..#h#####h#.....",
  "..#hhhhhhh#.....", "..#h####hh#.....", "..#hhhhhhh#.....", "..#h#####h#.....", "..#hhhhhhh#.....",
  "..#hh###hh#.....", "..#hhhhhhh#.....", ".g#########g....", "..gggggggggg....", "................", "................"],
 'orb': [
  "................", "................", "................", "......####......", ".....######.....",
  "....###hh###....", "....##hh####....", "....########....", "....########....", ".....######.....",
  "......####......", "................", "................", "................", "................", "................"],
 'can': [
  "................", "................", "......gggg......", ".....g####g.....", ".....######.....",
  ".....#hhhh#.....", ".....#hhhh#.....", ".....#hhhh#.....", ".....#hhhh#.....", ".....#hhhh#.....",
  ".....######.....", ".....g####g.....", "......gggg......", "................", "................", "................"],
 'ball': [
  "................", "................", "................", "......####......", ".....#hhhh#.....",
  "....#hhhhhh#....", "....gggggggg....", "....gg#hh#gg....", "....#######.....", ".....######.....",
  "......####......", "................", "................", "................", "................", "................"],
 'module': [
  "................", "................", "....gggggggg....", "...g########g...", "...g#hhhhhh#g...",
  "...g#h####h#g...", "...g#h#hh#h#g...", "...g#h#hh#h#g...", "...g#h####h#g...", "...g#hhhhhh#g...",
  "...g########g...", "....gggggggg....", "................", "................", "................", "................"],
}

def shade(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c)

def from_template(name, head, handle='#7a5230', grip='#3b2a1a'):
    t = TEMPLATES[name]; H = hexrgb(head); Hd = hexrgb(handle); G = hexrgb(grip)
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); px = im.load()
    for y, row in enumerate(t):
        for x, ch in enumerate(row):
            if ch == '.': continue
            if ch == '#':
                # simple bevel shading: lighter on top-left edges
                up = y > 0 and t[y - 1][x] == '#'; left = x > 0 and t[y][x - 1] == '#'
                f = 1.0 if (up and left) else 1.25
                down = y < 15 and t[y + 1][x] == '#'
                if not down: f = 0.75
                px[x, y] = shade(H, f) + (255,)
            elif ch == 'h': px[x, y] = shade(Hd, 1.0 if (x + y) % 3 else 0.85) + (255,)
            elif ch == 'g': px[x, y] = G + (255,)
    return im

def blank(w=16, h=16):
    return Image.new('RGBA', (w, h), (0, 0, 0, 0))

def save(im, dest):
    os.makedirs(os.path.dirname(dest), exist_ok=True)
    im.save(dest)
