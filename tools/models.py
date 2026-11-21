"""Recover entity model geometry from compiled model classes (javap bytecode)
and emit a JSON description: {tex:[w,h], parts:[{name,pose,cubes,children}]}.
Usage: python3 -I models.py <classfile> [<classfile>...]  -> prints JSON list
"""
import subprocess, sys, re, json
class SL(list):
    def pop(self, *a):
        return super().pop(*a) if self else None


def javap(path):
    return subprocess.run(['javap', '-c', '-p', path], capture_output=True, text=True).stdout.splitlines()

def methods(lines):
    out = {}; cur = None
    for l in lines:
        m = re.match(r'^  (?:[\w.<>\[\], $]+ )?([\w$<>.]+)\((.*)\)[^;]*;$', l)
        if m and not l.startswith('    '):
            cur = m.group(1)
            if '.' in cur: cur = '<init>'
            out.setdefault(cur, []); continue
        if cur and re.match(r'^\s+\d+: ', l):
            out[cur].append(l.strip())
    return out

class Part:
    def __init__(self, name):
        self.name = name; self.pose = [0.0]*6; self.cubes = []; self.children = []
        self.u = 0; self.v = 0; self.mirror = False
    def js(self):
        return {'name': self.name, 'pose': self.pose, 'cubes': self.cubes,
                'children': [c.js() for c in self.children]}

class CLB:  # CubeListBuilder
    def __init__(self): self.u = 0; self.v = 0; self.mirror = False; self.cubes = []

def const(op, arg):
    if op.startswith('fconst_'): return float(op[-1])
    if op.startswith('iconst_m1'): return -1
    if op.startswith('iconst_'): return int(op[-1])
    if op in ('bipush', 'sipush'): return int(arg)
    return None

SRG = {'m_171558_':'create','m_171514_':'texOffs','m_171555_':'mirror','m_171480_':'mirror','m_171599_':'addOrReplaceChild',
       'm_171423_':'offsetAndRotation','m_171419_':'offset','m_171430_':'rotation','m_171576_':'getRoot','m_171565_':'create',
       'f_171404_':'ZERO','f_171458_':'NONE'}
def norm(l):
    def rep(m):
        n=m.group(1); d=m.group(2)
        if n in SRG: return '.'+SRG[n]+':'+d
        if d.startswith('(FFFFFF') or d.startswith('(Ljava/lang/String;FFFFFF') or d.startswith('(FFFIII'): return '.addBox:'+d
        return '.'+n+':'+d
    l = re.sub(r'\.(m_\d+_):(\S+)', rep, l)
    l = re.sub(r'\.(f_\d+_):', lambda m: '.'+SRG.get(m.group(1),m.group(1))+':', l)
    return l
def run_new(code):
    """1.17+ LayerDefinition createBodyLayer."""
    code = [norm(x) for x in code]
    st = SL(); loc = {}; tex = [64, 64]; root = Part('root'); defs = {}
    for l in code:
        m = re.match(r'\d+: (\w+)\s*(.*)$', l); op, arg = m.group(1), m.group(2)
        c = const(op, arg.split()[0] if arg else '')
        if c is not None: st.append(c); continue
        cm = re.search(r'// (\w+) (.*)$', arg)
        kind, ref = (cm.group(1), cm.group(2)) if cm else (None, None)
        if op in ('ldc', 'ldc_w'):
            if kind == 'float': st.append(float(ref.rstrip('f')))
            elif kind == 'int': st.append(int(ref))
            elif kind == 'String': st.append(ref)
            else: st.append(ref)
        elif op.startswith('astore'): loc[op.split('_')[-1] if '_' in op else arg.split()[0]] = st.pop()
        elif op.startswith('aload'): st.append(loc.get(op.split('_')[-1] if '_' in op else arg.split()[0]))
        elif op == 'new':
            st.append({'new': ref})
        elif op == 'dup': st.append(st[-1])
        elif op == 'pop': st.pop()
        elif op == 'getstatic':
            if 'PartPose.ZERO' in ref: st.append([0.0]*6)
            elif 'CubeDeformation.NONE' in ref: st.append(0.0)
            else: st.append(None)
        elif op in ('invokespecial',):
            if 'CubeDeformation."<init>"' in ref:
                v = st.pop(); st.pop(); st.pop(); st.append(('def', v))
            elif 'MeshDefinition."<init>"' in ref:
                st.pop(); st.pop(); st.append({'mesh': root})
            else:
                n = ref.count('F') if '(' in ref else 0
        elif op in ('invokestatic', 'invokevirtual'):
            sig = ref
            if 'CubeListBuilder.create' in sig: st.append(CLB())
            elif 'CubeListBuilder.texOffs' in sig:
                v = st.pop(); u = st.pop(); b = st.pop(); b.u, b.v = u, v; st.append(b)
            elif 'CubeListBuilder.mirror:(Z)' in sig:
                z = st.pop(); b = st.pop(); b.mirror = bool(z); st.append(b)
            elif 'CubeListBuilder.mirror:()' in sig:
                b = st[-1]; b.mirror = True
            elif 'CubeListBuilder.addBox' in sig:
                desc = sig.split('addBox:(')[1].split(')')[0]
                args = []
                d = desc
                # parse descriptor tokens
                toks = re.findall(r'L[^;]+;|[FIZ]', d)
                vals = [st.pop() for _ in toks][::-1]
                b = st.pop()
                infl = 0.0; mir = b.mirror; fl = []
                for t, v in zip(toks, vals):
                    if t == 'F' or t == 'I': fl.append(float(v))
                    elif t == 'Z': mir = bool(v)
                    elif 'CubeDeformation' in t:
                        infl = v[1] if isinstance(v, tuple) else (v or 0.0)
                    elif 'String' in t: pass
                box = fl[:6]
                if len(fl) > 6: infl = fl[6]
                b.cubes.append({'uv': [b.u, b.v], 'box': box, 'infl': infl, 'mirror': mir})
                st.append(b)
            elif 'PartPose.offsetAndRotation' in sig:
                vals = [st.pop() for _ in range(6)][::-1]; st.append([float(x) for x in vals])
            elif 'PartPose.offset' in sig:
                vals = [st.pop() for _ in range(3)][::-1]; st.append([float(x) for x in vals] + [0.0, 0.0, 0.0])
            elif 'PartPose.rotation' in sig:
                vals = [st.pop() for _ in range(3)][::-1]; st.append([0.0, 0.0, 0.0] + [float(x) for x in vals])
            elif 'MeshDefinition.getRoot' in sig:
                st.pop(); st.append(root)
            elif 'PartDefinition.addOrReplaceChild' in sig:
                pose = st.pop(); b = st.pop(); name = st.pop(); parent = st.pop()
                p = Part(name); p.pose = pose if isinstance(pose, list) else [0.0]*6
                p.cubes = b.cubes if isinstance(b, CLB) else []
                if isinstance(parent, Part):
                    parent.children = [c for c in parent.children if c.name != name] + [p]
                st.append(p)
            elif 'LayerDefinition.create' in sig:
                h = st.pop(); w = st.pop(); st.pop(); tex = [w, h]; st.append(None)
            else:
                # unknown call: pop args conservatively
                desc = sig.split(':(')[1].split(')')[0] if ':(' in sig else ''
                toks = re.findall(r'L[^;]+;|\[?[FIZDJBSC]', desc)
                for _ in toks: st.pop() if st else None
                if op == 'invokevirtual' and st: st.pop()
                ret = sig.split(')')[-1]
                if ret != 'V': st.append(None)
        elif op == 'areturn': break
    return {'tex': tex, 'parts': [c.js() for c in root.children]}

ROT = {'field_78795_f': 3, 'field_78796_g': 4, 'field_78808_h': 5}

def run_old(code, rendercode):
    st = SL(); parts = {}; tex = [64, 32]; children = set()
    def P(name):
        if name not in parts: parts[name] = Part(name)
        return parts[name]
    for l in code:
        m = re.match(r'\d+: (\w+)\s*(.*)$', l); op, arg = m.group(1), m.group(2)
        c = const(op, arg.split()[0] if arg else '')
        if c is not None: st.append(c); continue
        cm = re.search(r'// (\w+) (.*)$', arg)
        kind, ref = (cm.group(1), cm.group(2)) if cm else (None, None)
        if op in ('ldc', 'ldc_w'):
            st.append(float(ref.rstrip('f')) if kind == 'float' else (int(ref) if kind == 'int' else ref))
        elif op == 'aload_0': st.append('THIS')
        elif op.startswith('aload') or op.startswith('astore'):
            if op.startswith('aload'): st.append(None)
            else: st.pop()
        elif op == 'new': st.append({'new': ref})
        elif op == 'dup': st.append(st[-1])
        elif op == 'pop': st.pop()
        elif op == 'putfield':
            fld = ref.split(':')[0].split('.')[-1]
            v = st.pop(); obj = st.pop()
            if fld == 'field_78090_t' or fld == 'textureWidth': tex[0] = v
            elif fld == 'field_78089_u' or fld == 'textureHeight': tex[1] = v
            elif 'ModelRenderer' in ref and obj == 'THIS':
                p = P(fld)
            elif isinstance(obj, Part) and fld in ROT: obj.pose[ROT[fld]] = float(v)
            elif isinstance(obj, Part) and fld in ('field_78809_i', 'mirror'): obj.mirror = bool(v)
        elif op == 'getfield':
            fld = ref.split(':')[0].split('.')[-1]
            obj = st.pop()
            if 'ModelRenderer' in ref: st.append(P(fld))
            else: st.append(None)
        elif op == 'invokespecial':
            desc = ref.split(':(')[1].split(')')[0]
            toks = re.findall(r'L[^;]+;|\[?[FIZDJBSC]', desc)
            for _ in toks: st.pop()
            st.pop()  # uninit ref (dup consumed)
        elif op == 'invokevirtual':
            desc = ref.split(':(')[1].split(')')[0]; ret = ref.split(')')[-1]
            toks = re.findall(r'L[^;]+;|\[?[FIZDJBSC]', desc)
            vals = [st.pop() for _ in toks][::-1]; obj = st.pop()
            if 'ModelRenderer.' in ref and isinstance(obj, Part):
                if desc == 'FFF':
                    obj.pose[0:3] = [float(x) for x in vals]
                elif desc == 'II':
                    obj.u, obj.v = vals
                elif 'ModelRenderer;' in desc and len(toks) == 1:
                    if isinstance(vals[0], Part):
                        obj.children.append(vals[0]); children.add(vals[0].name)
                elif desc.startswith('FFFFFF') or desc.startswith('FFFIII') or desc.startswith('Ljava/lang/String;FFFIII'):
                    nums = [v for t, v in zip(toks, vals) if t in ('F', 'I')]
                    zs = [v for t, v in zip(toks, vals) if t == 'Z']
                    infl = float(nums[6]) if len(nums) > 6 else 0.0
                    mir = bool(zs[0]) if zs else obj.mirror
                    obj.cubes.append({'uv': [obj.u, obj.v], 'box': [float(x) for x in nums[:6]], 'infl': infl, 'mirror': mir})
                if ret.startswith('Lnet/minecraft/client/renderer/model/ModelRenderer'): st.append(obj)
            elif obj == 'THIS' and len(vals) == 4 and isinstance(vals[0], Part):
                vals[0].pose[3:6] = [float(x) for x in vals[1:]]
            else:
                if ret != 'V': st.append(None)
        elif op == 'return': break
    # roots: rendered in render method
    rendered = []
    for l in rendercode:
        m = re.search(r'getfield .*// Field (\w+):Lnet/minecraft/client/renderer/model/ModelRenderer', l)
        if m and m.group(1) in parts and m.group(1) not in rendered: rendered.append(m.group(1))
    roots = [n for n in rendered if n not in children] or [n for n in parts if n not in children]
    return {'tex': tex, 'parts': [parts[n].js() for n in roots]}

def convert(path):
    lines = javap(path); ms = methods(lines)
    if 'createBodyLayer' in ms:
        return run_new(ms['createBodyLayer'])
    init = ms.get('<init>') or next((v for k, v in ms.items() if k.endswith('$Model') or '<init>' in k), [])
    if not init:
        for k, v in ms.items():
            if any('ModelRenderer."<init>"' in x for x in v): init = v; break
    render = []
    for k, v in ms.items():
        if k.startswith('func_225598_a_') or k == 'render' or k.startswith('func_78088_a'): render = v
    return run_old(init, render)

if __name__ == '__main__':
    res = {}
    for p in sys.argv[1:]:
        try: res[p] = convert(p)
        except Exception as e: sys.stderr.write(p + ' ' + repr(e) + '\n')
    print(json.dumps(res, indent=1))
