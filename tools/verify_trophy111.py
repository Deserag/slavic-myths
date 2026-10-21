"""#111 offline resource audit; no legacy module imports, no game launches."""
from pathlib import Path
import ast,hashlib,json,zipfile,tempfile
from PIL import Image,ImageDraw
from trophy111_art import IDS,FIX,KEEP,BLOCKED,sprite,ROOT

A=ROOT/'src/main/resources/assets/slavicmyths';OUT=ROOT/'docs/implementation/trophy111-refinement'
sha=lambda b:hashlib.sha256(b).hexdigest()
checks=[]
def check(ok,label):
    checks.append({'requirement':label,'status':'PASS' if ok else 'FAIL'})
    assert ok,label

def preview(names,path,before_root=None):
    im=Image.new('RGB',(740,46+90*len(names)),'#282b30');d=ImageDraw.Draw(im)
    d.text((12,10),'OFFLINE textures: before / after / alpha masks; 1x + 3x nearest. NOT game captures.',fill='white')
    for i,n in enumerate(names):
        y=42+i*90;d.text((12,y),n+' ['+('FIX' if n in FIX else 'KEEP')+']',fill='white')
        for j,root in enumerate([before_root or OUT/'before',A/'textures/item']):
            input_path=(OUT/('pearl-gui-before.png' if j==0 else 'pearl-gui-after.png')) if n=='pearl_fragment' else root/f'{n}.png'
            pic=Image.open(input_path).convert('RGBA').resize((16,16),Image.Resampling.NEAREST)
            for scale,x in [(1,14+j*230),(3,60+j*230)]:
                frame=Image.new('RGBA',(18,18),'#d0d0d0');frame.paste('#888888',(1,1,17,17));frame.alpha_composite(pic,(1,1))
                im.paste(frame.resize((18*scale,18*scale),Image.Resampling.NEAREST),(x,y+20))
            mask=Image.new('RGBA',(16,16));mask.putdata([(230,230,230,255) if v else (0,0,0,0) for v in pic.getchannel('A').getdata()])
            mask=mask.resize((48,48),Image.Resampling.NEAREST);im.paste(mask,(520+j*80,y+22),mask)
    im.save(path)

def execute(nodes,ns):exec(compile(ast.Module(body=nodes,type_ignores=[]),'isolated-real-generator','exec'),ns)

def main():
    baseline=json.loads((OUT/'workspace-before.json').read_text());changed=[]
    tools=['trophy111_art','verify_trophy111','water_070']
    allowed={f'tools/{n}.py' for n in tools}|{f'src/main/resources/assets/slavicmyths/textures/item/{n}.png' for n in ['pearl_fragment','ataman_sign','serpent_seal']}|{'src/main/resources/assets/slavicmyths/models/item/pearl_fragment.json','docs/PROJECT_STATUS.md','docs/ROADMAP.md','docs/ARCHITECTURE.md'}
    for p,h in baseline.items():
        file=ROOT/p
        if not file.is_file() or sha(file.read_bytes())!=h:changed.append(p)
    check(set(changed)<=allowed,'source byte parity outside three PNG/pearl model/canonical tools/status docs')
    (OUT/'workspace-diff.json').write_text(json.dumps(changed,indent=2)+'\n')
    manifest=[]
    for n in IDS:
        path=A/'textures/item'/f'{n}.png';old=OUT/'before'/path.name;im=Image.open(path).convert('RGBA');model=A/'models/item'/f'{n}.json';m=json.loads(model.read_text())
        if n=='pearl_fragment':
            check(m=={'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/pearl_fragment'}},'approved pearl flat generated model with no old UV/display')
        else:check(sha(model.read_bytes())==baseline[model.relative_to(ROOT).as_posix()],n+' unchanged actual model/geometry/UV/display')
        check(im.size==Image.open(old).size,n+' preserved native resolution')
        if n in FIX:
            check(im.tobytes()==sprite(n).tobytes(),n+' exact canonical master')
            check(set(im.getchannel('A').getdata())=={0,255},n+' no blur/partial alpha')
            original=ROOT/'docs/implementation/trophy111/before'/path.name
            check(path.read_bytes()!=original.read_bytes(),n+' production differs from original pre-T2 baseline')
        else:check(path.read_bytes()==old.read_bytes(),n+' KEEP/BLOCKED exact baseline')
        manifest.append({'id':n,'action':'KEEP' if n in KEEP else 'REFINE' if n in ['ataman_sign','serpent_seal'] else 'FIX' if n=='pearl_fragment' else 'RETAIN_PRIOR_FIX','model':model.relative_to(ROOT).as_posix(),'texture':path.relative_to(ROOT).as_posix(),'size':im.size,'sha_old':sha(old.read_bytes()),'sha_new':sha(path.read_bytes())})
    with tempfile.TemporaryDirectory() as td:
        tmp=Path(td);(tmp/'textures/item').mkdir(parents=True)
        import vanilla_item_style as v
        for n in FIX:
            check(v.sprite('unused-for-owned-id',n).tobytes()==sprite(n).tobytes(),'final sprite delegation '+n)
        old_a,old_f=v.A,v.FAMILIES
        try:
            v.A=tmp;v.FAMILIES={};v.apply()
            for n in FIX:check((tmp/'textures/item'/f'{n}.png').read_bytes()==(A/'textures/item'/f'{n}.png').read_bytes(),'final overlay writes exact production '+n)
            check((tmp/'models/item/pearl_fragment.json').read_bytes()==(A/'models/item/pearl_fragment.json').read_bytes(),'final overlay preserves pearl generated model')
        finally:v.A,v.FAMILIES=old_a,old_f
        tree=ast.parse((ROOT/'tools/water_070.py').read_text(encoding='utf8'))
        node=next(n for n in ast.walk(tree) if isinstance(n,ast.If) and ast.unparse(n.test)=="name == 'pearl_fragment'")
        loop=ast.parse("for name in ['pearl_fragment']:\n pass").body[0];loop.body=[node];execute([loop],{'A':tmp})
        check((tmp/'textures/item/pearl_fragment.png').read_bytes()==(A/'textures/item/pearl_fragment.png').read_bytes(),'legacy water guarded pearl sprite')
        check((tmp/'models/item/pearl_fragment.json').read_bytes()==(A/'models/item/pearl_fragment.json').read_bytes(),'legacy water does not restore cuboid')
        tree=ast.parse((ROOT/'tools/rare_weapon_resources_1322.py').read_text(encoding='utf8'));node=next(n for n in tree.body if isinstance(n,ast.FunctionDef) and n.name=='icon');ns={};execute([node],ns)
        check(ns['icon']('ataman_sign').tobytes()==sprite('ataman_sign').tobytes(),'ataman icon native32 exact master')
        # Real legacy FunctionDef, isolated without importing finalize_065.
        tree=ast.parse((ROOT/'tools/kurgan_086.py').read_text(encoding='utf8'));node=next(n for n in tree.body if isinstance(n,ast.FunctionDef) and n.name=='sprite');ns={'A':tmp};execute([node],ns)
        for name in ['shield_boss_fragment','voevoda_insignia','princely_seal']:
            ns['sprite'](name);check((tmp/'textures/item'/f'{name}.png').read_bytes()==(A/'textures/item'/f'{name}.png').read_bytes(),'legacy kurgan entrypoint '+name)
        for module in ['generate_045','generate_046']:
            tree=ast.parse((ROOT/'tools'/f'{module}.py').read_text(encoding='utf8'));fn=next(n for n in tree.body if isinstance(n,ast.FunctionDef) and n.name=='generate');execute(fn.body[-2:],{'res':tmp})
            check((tmp/'textures/item/ancient_coin.png').read_bytes()==(A/'textures/item/ancient_coin.png').read_bytes(),module+' final coin overlay')
        for module in ['gorynych_assets','generate_milestone']:
            tree=ast.parse((ROOT/'tools'/f'{module}.py').read_text(encoding='utf8'))
            node=next(n for n in ast.walk(tree) if isinstance(n,ast.If) and 'write_trophy' in ast.unparse(n.test))
            from trophy111_art import write_owned
            path=tmp/'textures/item'/('serpent_seal.png' if module=='gorynych_assets' else 'ancient_coin.png')
            ns={'A':tmp,'name':'serpent_seal','p':path,'write_trophy':write_owned}
            execute([node],ns);check(path.read_bytes()==(A/'textures/item'/path.name).read_bytes(),module+' actual scoped write guard')
    resource_count=png_count=json_count=0
    jar=next(p for p in (ROOT/'build/libs').glob('*.jar') if not any(x in p.name for x in ['sources','javadoc']))
    with zipfile.ZipFile(jar) as z:
        for p in (ROOT/'src/main/resources').rglob('*'):
            if not p.is_file():continue
            rel=p.relative_to(ROOT/'src/main/resources').as_posix();expected=ROOT/'build/resources/main'/rel if rel=='META-INF/neoforge.mods.toml' else p
            check(z.read(rel)==expected.read_bytes(),'JAR parity '+rel);resource_count+=1
            if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8-sig'));json_count+=1
            if p.suffix=='.png':
                with Image.open(p) as image:image.verify()
                png_count+=1
        check(not any(n.startswith(('tools/','org/slavicmyths/verify/')) for n in z.namelist()),'no smoke/tools fixtures in JAR')
    for label,names in [('T2-A',IDS[:3]),('T2-B',IDS[3:6]),('T2-C',IDS[6:])]:preview(names,OUT/f'{label}.png')
    preview(IDS,OUT/'before-after.png')
    preview(IDS,OUT/'original-before-after.png',ROOT/'docs/implementation/trophy111/before')
    data={'assets':manifest,'checks':checks,'resources':resource_count,'json':json_count,'png':png_count,'jar':{'path':str(jar),'bytes':jar.stat().st_size,'sha256':sha(jar.read_bytes())},'minecraft_launches':0,'issue_status':'OWNER_IN_GAME_PENDING','recommended_project_status':'Тестирование','blocked':list(BLOCKED)}
    (OUT/'verification.json').write_text(json.dumps(data,indent=2)+'\n',encoding='utf8');print(json.dumps({k:v for k,v in data.items() if k not in ['checks','assets']},indent=2))

if __name__=='__main__':main()
