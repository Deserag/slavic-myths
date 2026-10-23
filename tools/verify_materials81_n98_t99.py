"""#98/#99 exact production scope, native pixels, old writer guards and JAR."""
from pathlib import Path
import ast,hashlib,json,struct,zlib,tempfile,sys,zipfile
from PIL import Image
import materials81_n98_t99 as art

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs/implementation/materials-81/N98-T99'
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def load_defs(path,names,env):
    tree=ast.parse((ROOT/path).read_text(encoding='utf-8'))
    selected=[n for n in tree.body if isinstance(n,(ast.FunctionDef,ast.Import,ast.ImportFrom)) and (not isinstance(n,ast.FunctionDef) or n.name in names)]
    # Execute only imports and requested definitions, never historical top-level
    # generators (which also write unrelated assets, recipes and entities).
    exec(compile(ast.Module(body=selected,type_ignores=[]),str(path),'exec'),env)
    return env

def main():
    baseline=json.loads((OUT/'production-baseline.json').read_text())
    allowed={f'src/main/resources/{art.PREFIX}{n}.png' for n in art.OWNED}
    changed={p for p,h in baseline.items() if sha(ROOT/p)!=h}
    preservation='--preserve' in sys.argv
    if preservation:
        prior=json.loads((OUT/'verification.json').read_text())
        assert prior['source_scope']=='PASS' and len(prior['assets'])==12
        for asset in prior['assets']:assert sha(ROOT/asset['texture'])==asset['after_sha256'],asset['id']
    else:
        assert changed==allowed,(changed-allowed,allowed-changed)
        assert set(p.relative_to(ROOT).as_posix() for p in (ROOT/'src/main').rglob('*') if p.is_file())==set(baseline)-{'gradle.properties'}
    catalog=(ROOT/'src/main/resources/assets/slavicmyths/creative_catalog.tsv').read_text(encoding='utf-8')
    assets=[]
    for name in art.OWNED:
        path=ROOT/f'src/main/resources/{art.PREFIX}{name}.png'
        before=OUT/'before'/path.relative_to(ROOT)
        with Image.open(path) as im:
            assert im.size==Image.open(before).size
            rgba=im.convert('RGBA');assert set(rgba.getchannel('A').getdata())=={0,255}
            assert rgba.tobytes()==art.render(name).tobytes()
        model=ROOT/f'src/main/resources/assets/slavicmyths/models/item/{name}.json'
        assert json.loads(model.read_text())['textures']['layer0']=='slavicmyths:item/'+name
        assert name in catalog
        assets.append(dict(id=name,model=model.relative_to(ROOT).as_posix(),texture=path.relative_to(ROOT).as_posix(),before_sha256=sha(before),after_sha256=sha(path)))
    # Actual alpha masks: ordinary/light components must differ substantially,
    # and materials must not all be one recoloured silhouette.
    masks=[]
    for material in ('stone','gold','iron','silver'):
        regular=art.render(material+'_spearhead').getchannel('A')
        light=art.render(material+'_light_spearhead').getchannel('A')
        heavy_count=sum(a>0 for a in regular.getdata());light_count=sum(a>0 for a in light.getdata())
        assert heavy_count>light_count*1.25,(material,heavy_count,light_count)
        masks.append(regular.tobytes())
    assert len(set(masks))==4
    with tempfile.TemporaryDirectory(prefix='materials81-N98-T99-') as folder:
        root=Path(folder);art.apply(root)
        first={p.relative_to(root).as_posix():sha(p) for p in root.rglob('*.png')}
        art.apply(root);assert first=={p.relative_to(root).as_posix():sha(p) for p in root.rglob('*.png')}
        assert set(first)==allowed
        # Exercise actual writer functions from their AST in the sandbox.
        textile=load_defs(Path('tools/textile_resources.py'),{'save'},{})
        oldbone=load_defs(Path('tools/generate_065.py'),{'png'},{})
        oldbark=load_defs(Path('tools/generate_textures.py'),{'png'},{})
        for name in art.OWNED:
            target=root/f'src/main/resources/{art.PREFIX}{name}.png'
            if name in ('animal_fat','goat_hide'):textile['save'](target,Image.new('RGBA',(1,1),'red'))
            elif name=='large_animal_bone':oldbone['png'](target,[[(255,0,0,255)]])
            else:oldbark['png'](target,[[(255,0,0,255)]],1,1)
            assert Image.open(target).convert('RGBA').tobytes()==art.render(name).tobytes(),name
        oldtips=load_defs(Path('tools/weapon_resources_1321.py'),{'asset','icon','write'},{'ROOT':root,'RES':root/'src/main/resources','TYPES':{}})
        for name in art.TIPS:
            kind='light_spearhead' if '_light_' in name else 'spearhead'
            oldtips['asset'](name,kind,'#ff0000','#00ff00')
            target=root/f'src/main/resources/{art.PREFIX}{name}.png'
            assert Image.open(target).convert('RGBA').tobytes()==art.render(name).tobytes(),name
        neighbours=root/'src/main/resources/assets/slavicmyths/textures/item/linen_thread.png'
        assert not art.write_owned(neighbours) and not neighbours.exists()
    jar_info=None
    if '--jar' in sys.argv:
        jars=list((ROOT/'build/libs').glob('slavicmyths-*.jar'));assert len(jars)==1,jars
        count=0
        with zipfile.ZipFile(jars[0]) as jar:
            # Earlier verify_issue217.py and verify_current_resources.py own
            # whole-JAR validation. This package verifies only its 12 owned
            # pixel sprites, after exercising unrelated legacy generators in a
            # temporary sandbox; those source generators are not JAR baselines.
            for name in art.OWNED:
                rel=art.PREFIX+name+'.png'
                expected=ROOT/'src/main/resources'/rel
                assert jar.read(rel)==expected.read_bytes(),rel
                count+=1
            assert not any('HeadPoseExport' in n or 'Heads140Offline' in n or n.startswith('tools/smoke/') for n in jar.namelist())
        jar_info=dict(path=jars[0].relative_to(ROOT).as_posix(),sha256=sha(jars[0]),resources=count)
    receipt=dict(issues=[98,99],assets=assets,source_scope='PASS',generators='PASS',jar=jar_info,runtime_launches=0,owner_acceptance='PENDING',project_status='NOT_CHANGED: project scope missing',version_unchanged=True,polymc='NOT_UPDATED')
    if preservation:
        receipt.update(source_scope='PRESERVED_12_SPRITES: original package scope receipt retained; later weapon scope checked independently',version_unchanged=False,project_status='NOT_CHANGED_BY_THIS_CHECK')
    (OUT/('verification-preservation.json' if preservation else 'verification.json')).write_text(json.dumps(receipt,indent=2)+'\n')
    print('N98/T99 PASS: 12 exact sprites, natural/weapon-component silhouettes, original dimensions, isolated deterministic generators; JAR='+str(jar_info)+'; runtime NOT_RUN')

if __name__=='__main__':main()
