"""Exact armor-only source/artifact gates; no game, performance or relog claims."""
from pathlib import Path
import hashlib, json, re, struct, subprocess, sys, zipfile
from PIL import Image
from armor_revision_assets import ROOT, A, IDS
from check_event_subscribers import check as subscribers

O=ROOT/'docs/armor-revision'; W=ROOT/'work/armor-revision'; checks=0
JAVA={f'src/main/java/org/slavicmyths/client/{name}.java' for name in
      ('DragonArmor','PeruniteArmor','RewardArmor','UniqueArmorModels')}
TOOLS={f'tools/{name}' for name in ('unique_armor_resources.py','bandits_080.py','folk_equipment.py',
       'generate_045.py','gorynych_assets.py','poloz_full_assets.py','rewards_models.py')}

def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def need(ok,message):
    global checks
    checks+=1
    if not ok:raise AssertionError(message)

def main():
    props=dict(line.split('=',1) for line in (ROOT/'gradle.properties').read_text().splitlines() if '=' in line)
    jar=ROOT/f'build/libs/slavicmyths-{props["mod_version"]}.jar'
    log=(W/'clean-build.log').read_text(encoding='utf-8',errors='replace')
    need('BUILD SUCCESSFUL' in log,'clean build')
    need('ARMOR_MESH_PASS' in log,'actual baked model/UV checks')
    need(props['minecraft_version']=='1.21.1' and props['neo_version']=='21.1.255','pinned platform')
    before={p.relative_to(ROOT).as_posix():sha(p) for p in (ROOT/'src').rglob('*') if p.is_file()}
    subprocess.run([sys.executable,str(ROOT/'tools/armor_revision_assets.py')],check=True)
    need(before=={p.relative_to(ROOT).as_posix():sha(p) for p in (ROOT/'src').rglob('*') if p.is_file()},'asset generator is byte reproducible')
    baseline=json.loads((W/'baseline.json').read_text())
    changed=[];concurrent=[]
    for name,digest in baseline.items():
        p=ROOT/name;need(p.exists(),'no deleted existing file: '+name)
        if sha(p)!=digest:
            changed.append(name)
            owned=(name in JAVA or name in TOOLS or name.startswith(('src/main/resources/assets/slavicmyths/textures/armor/',
                 'src/main/resources/assets/slavicmyths/textures/models/armor/')) or
                 name in {f'src/main/resources/assets/slavicmyths/{kind}/{id}.{ext}' for id in IDS
                          for kind,ext in [('models/item','json'),('textures/item','png')]})
            if not owned:
                if name in {'tools/check_creature_audit_tables_1981.py','tools/complete_creature_audit_1981.py',
                            'tools/finalize_creature_audit_1981.py','tools/finalize_creature_audit_docs_1981.py'}:
                    concurrent.append(name)
                    continue
                # A separate weapon patch was committed while this task was running.
                # Accept only files exactly matching its committed state, never arbitrary edits.
                actual=subprocess.check_output(['git','hash-object',name],cwd=ROOT,text=True).strip()
                committed=subprocess.check_output(['git','rev-parse','HEAD:'+name],cwd=ROOT,text=True).strip()
                need(actual==committed,'uncommitted change outside scoped revision: '+name)
                concurrent.append(name)
    alpha_masks=set();icons=set()
    for id in IDS:
        p=A/f'textures/item/{id}.png'
        with Image.open(p) as im:
            need(im.mode=='RGBA' and im.size==(32,32),'icon dimensions '+id)
            need(im.getextrema()[3]==(0,255),'transparent background '+id)
            need(im.getbbox() is not None,'nonempty icon '+id)
            # No smoothing: every 2x2 block is a single authored pixel.
            need(im.resize((16,16),Image.Resampling.NEAREST).resize((32,32),Image.Resampling.NEAREST).tobytes()==im.tobytes(),'16px authored grid '+id)
            alpha_masks.add(im.getchannel('A').tobytes());icons.add(im.tobytes())
        need(json.loads((A/f'models/item/{id}.json').read_text())=={'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/{id}'}},'native inventory model '+id)
        for locale in ('ru_ru','en_us'):
            need('item.slavicmyths.'+id in json.loads((A/f'lang/{locale}.json').read_text(encoding='utf-8')),'localized '+id)
    need(len(icons)==22,'all icon designs distinct');need(len(alpha_masks)>=12,'multiple silhouettes, not just recolors')
    for p in (ROOT/'src/main/resources').rglob('*.json'):
        json.loads(p.read_text(encoding='utf-8'));need(True,'valid JSON')
    for p in (ROOT/'src/main/resources').rglob('*.png'):
        with Image.open(p) as im:im.verify()
        need(True,'valid PNG')
    baseline_jar=W/'latest-weapon-production.jar';changed_entries=[]
    classes={n.removeprefix('src/main/java/').removesuffix('.java')+'.class' for n in JAVA}
    with zipfile.ZipFile(jar) as z,zipfile.ZipFile(baseline_jar) as old:
        names=set(z.namelist());need(set(old.namelist())<=names,'old production entries retained')
        for n in old.namelist():
            if old.read(n)!=z.read(n):
                changed_entries.append(n)
                need(n in classes or n.startswith(('assets/slavicmyths/textures/armor/','assets/slavicmyths/textures/models/armor/')) or
                     n in {f'assets/slavicmyths/{kind}/{id}.{ext}' for id in IDS for kind,ext in [('models/item','json'),('textures/item','png')]},'production behavior changed outside client armor: '+n)
        for n in names:
            need(not any(token in n.lower() for token in ('gametest','tools/smoke','org/slavicmyths/verify/','armorrevisionmeshes','rewardmodelchecks')),'test fixture excluded '+n)
            blob=z.read(n)
            if n.endswith('.class'):
                need(struct.unpack('>H',blob[6:8])[0]==65,'Java21 class '+n)
                need(blob==(ROOT/'build/classes/java/main'/n).read_bytes(),'current compiled class '+n)
            source=ROOT/'src/main/resources'/n
            if source.is_file() and n!='META-INF/neoforge.mods.toml':need(blob==source.read_bytes(),'exact source resource '+n)
        for p in (ROOT/'src/main/resources').rglob('*'):
            if p.is_file():need(p.relative_to(ROOT/'src/main/resources').as_posix() in names,'resource packaged '+str(p))
        need(f'version="{props["mod_version"]}"' in z.read('META-INF/neoforge.mods.toml').decode().replace(' ',''),'technical version preserved')
    result=dict(status='PASS',label=props['mod_version']+' + патч оружия — ревизия брони',version=props['mod_version'],
                checks=checks,sha256=sha(jar),baseline_sha256=sha(baseline_jar),changed_existing_files=changed,
                concurrent_committed_files_preserved=concurrent,
                changed_production_entries=changed_entries,unique_icons=len(icons),unique_alpha_masks=len(alpha_masks),
                offline_native_mesh_checks=int(re.search(r'ARMOR_MESH_PASS checks=(\d+)',log).group(1)),
                event_subscribers=subscribers(jar),client_starts=0,server_starts=0,gametest_starts=0,
                runtime='NOT_RUN',MSPT='NOT_MEASURED',gameplay_changes=0)
    O.mkdir(parents=True,exist_ok=True);(O/'resource-gates.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(f'ARMOR_PRODUCTION_PASS checks={checks} sha256={result["sha256"]} silhouettes={len(alpha_masks)}')

if __name__=='__main__':main()
