"""Offline production evidence for #110; never starts Minecraft or full legacy generators."""
from pathlib import Path
import ast
import hashlib
import itertools
import json
import tempfile
import zipfile
from PIL import Image, ImageDraw
from trophy110_art import IDS, sprite, ROOT

A = ROOT / 'src/main/resources/assets/slavicmyths'
OUT = ROOT / 'docs/implementation/trophy110'
sha = lambda b: hashlib.sha256(b).hexdigest()
checks = []

def check(ok, label):
    checks.append({'requirement': label, 'status': 'PASS' if ok else 'FAIL'})
    assert ok, label

def components(mask):
    points = {(x, y) for y in range(16) for x in range(16) if mask[y * 16 + x]}
    sizes = []
    while points:
        todo = [points.pop()]; count = 0
        while todo:
            x, y = todo.pop(); count += 1
            for p in [(x-1,y),(x+1,y),(x,y-1),(x,y+1)]:
                if p in points: points.remove(p); todo.append(p)
        sizes.append(count)
    return sorted(sizes, reverse=True)

def strip(names, path):
    # Actual production pixels, same 18px frame, native 1x and nearest 3x.
    im = Image.new('RGB', (720, 50 + 104 * len(names)), '#25282e')
    d = ImageDraw.Draw(im)
    d.text((12, 10), 'OFFLINE PNG comparison: BEFORE / AFTER / silhouette. Not a game capture.', fill='white')
    for i, name in enumerate(names):
        y = 40 + i * 104
        d.text((12,y),name,fill='white')
        old = Image.open(OUT/'before'/f'{name}.png').convert('RGBA')
        new = Image.open(A/'textures/item'/f'{name}.png').convert('RGBA')
        for j, pic in enumerate([old, new]):
            for scale, x in [(1, 20+j*220), (3, 64+j*220)]:
                for bg_index, bg in enumerate(['#8b8b8b','#303038']):
                    frame = Image.new('RGBA', (18,18), '#d0d0d0')
                    frame.paste(bg,(1,1,17,17)); frame.alpha_composite(pic,(1,1))
                    im.paste(frame.resize((18*scale,18*scale),Image.Resampling.NEAREST),(x,y+18+bg_index*28 if scale==1 else y+18))
        mask = Image.new('RGBA',(16,16)); mask.putdata([(240,240,240,255) if a else (0,0,0,0) for a in new.getchannel('A').getdata()])
        im.paste(mask.resize((48,48),Image.Resampling.NEAREST),(500,y+20),mask.resize((48,48),Image.Resampling.NEAREST))
    im.save(path)

def main():
    manifest = []; masks = {}
    baseline=json.loads((OUT/'workspace-before.json').read_text())
    whitelist={f'src/main/resources/assets/slavicmyths/textures/item/{n}.png' for n in IDS}
    whitelist.update(f'tools/{n}.py' for n in ['hunt_090','hunt_091','hunt_092','kurgan_086','vanilla_item_style'])
    whitelist.update(['docs/PROJECT_STATUS.md','docs/ROADMAP.md','docs/ARCHITECTURE.md'])
    changed=[]
    for path,old_hash in baseline.items():
        p=ROOT/path
        if not p.is_file() or sha(p.read_bytes())!=old_hash: changed.append(path)
    check(set(changed)<=whitelist,'baseline byte parity outside seven PNG/five generator hooks/three status docs')
    (OUT/'workspace-diff.json').write_text(json.dumps(changed,indent=2)+'\n',encoding='utf8')
    catalog = (A/'creative_catalog.tsv').read_text(encoding='utf8')
    registration = (ROOT/'src/main/java/org/slavicmyths/registry/ModItems.java').read_text(encoding='utf8')
    for name in IDS:
        p = A/'textures/item'/f'{name}.png'; model = A/'models/item'/f'{name}.json'
        data = json.loads(model.read_text(encoding='utf8'))
        check(data == {'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/{name}'}}, name+' model/layer/anchors')
        check(name in catalog and f'"{name}"' in registration, name+' actual catalog/registry')
        im = Image.open(p).convert('RGBA'); old = Image.open(OUT/'before'/p.name).convert('RGBA')
        check(im.size == old.size == (16,16), name+' native resolution')
        check(im.tobytes() == sprite(name).tobytes(),name+' deterministic generator')
        alphas = list(im.getchannel('A').getdata()); masks[name] = [bool(a) for a in alphas]
        check(set(alphas) == {0,255}, name+' crisp binary alpha')
        check(im.getbbox()[0]>0 and im.getbbox()[2]<16 and im.getbbox()[1]>0 if name!='iskra_ovinnika' else True,name+' bounds')
        manifest.append({'id':name,'action':'FIX','model':model.relative_to(ROOT).as_posix(),'texture':p.relative_to(ROOT).as_posix(),
                         'old_sha256':sha((OUT/'before'/p.name).read_bytes()),'new_sha256':sha(p.read_bytes()),
                         'size':im.size,'bbox':im.getbbox(),'components':components(masks[name])})
    distances = []
    for group in [IDS[:3],IDS[3:5],IDS[5:]]:
        for a,b in itertools.combinations(group,2):
            union = sum(x or y for x,y in zip(masks[a],masks[b]))
            delta = sum(x != y for x,y in zip(masks[a],masks[b]))
            check(delta/union > .30, f'{a}/{b}: material silhouette difference >30%')
            distances.append({'a':a,'b':b,'changed_mask_pixels':delta,'union_pixels':union,'difference_fraction':delta/union})
    check(len(components(masks['ovinnaya_zola']))>=3,'ash detached flakes')
    check(len(components(masks['iskra_ovinnika']))==4,'ember plus three detached sparks')
    check(len(set(Image.open(A/'textures/item/ovinnaya_zola.png').getdata()))==4,'ash exactly three opaque tones')
    # Run only historical art functions against a temporary output root.
    with tempfile.TemporaryDirectory() as temp:
        target=Path(temp); (target/'textures/item').mkdir(parents=True)
        for module, function, names in [('kurgan_086','sprite',IDS[:1]),('hunt_090','art',IDS[1:5]),('hunt_091','art',IDS[5:6]),('hunt_092','art',IDS[6:])]:
            tree=ast.parse((ROOT/'tools'/f'{module}.py').read_text(encoding='utf8'))
            node=next(n for n in tree.body if isinstance(n,ast.FunctionDef) and n.name==function)
            # Execute the real function alone: legacy top-level imports have side effects.
            namespace={'A':target}
            exec(compile(ast.Module(body=[node],type_ignores=[]),module,'exec'),namespace)
            for name in names:
                namespace[function](name)
                check((target/'textures/item'/f'{name}.png').read_bytes()==(A/'textures/item'/f'{name}.png').read_bytes(),module+': '+name+' exact regeneration')
        import vanilla_item_style as vanilla
        for kind,names in vanilla.FAMILIES.items():
            for name in names:
                if name in IDS: check(vanilla.sprite(kind,name).tobytes()==sprite(name).tobytes(),'final style delegation: '+name)
    for p in (ROOT/'src/main/resources').rglob('*.json'):
        json.loads(p.read_text(encoding='utf-8-sig'))
    check(True,'all production resource JSON parse')
    png_count=0
    for p in (ROOT/'src/main/resources').rglob('*.png'):
        with Image.open(p) as im: im.verify()
        png_count+=1
    check(True,f'all {png_count} production PNG decode')
    for label,group in [('T1-A',IDS[:3]),('T1-B',IDS[3:5]),('T1-C',IDS[5:])]: strip(group,OUT/f'{label}.png')
    strip(IDS,OUT/'before-after.png')
    jars=[p for p in (ROOT/'build/libs').glob('*.jar') if not any(s in p.name for s in ['sources','javadoc'])]
    check(len(jars)==1,'one production JAR')
    jar=jars[0]
    with zipfile.ZipFile(jar) as z:
        resource_count=0
        for p in (ROOT/'src/main/resources').rglob('*'):
            if p.is_file():
                relative=p.relative_to(ROOT/'src/main/resources').as_posix()
                expected=(ROOT/'build/resources/main'/relative) if relative=='META-INF/neoforge.mods.toml' else p
                check(z.read(relative)==expected.read_bytes(),'production resource parity: '+relative)
                resource_count+=1
        for name in IDS:
            for relative in [f'assets/slavicmyths/textures/item/{name}.png',f'assets/slavicmyths/models/item/{name}.json']:
                check(z.read(relative)==(ROOT/'src/main/resources'/relative).read_bytes(),'JAR exact bytes: '+relative)
        check(not any(n.startswith(('tools/','org/slavicmyths/verify/')) or 'Trophy110' in n for n in z.namelist()),'no smoke/tools fixtures in production')
    result={'assets':manifest,'silhouette_distances':distances,'checks':checks,'resource_count':resource_count,'jar':{'path':str(jar),'sha256':sha(jar.read_bytes()),'bytes':jar.stat().st_size},'minecraft_launches':0}
    (OUT/'verification.json').write_text(json.dumps(result,indent=2)+'\n',encoding='utf8')
    print(json.dumps({'checks':len(checks),'jar':result['jar']},indent=2))

if __name__=='__main__': main()
