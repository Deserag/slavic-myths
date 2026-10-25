"""Current package guards and exact production packaging; no game starts."""
from pathlib import Path
import hashlib, json, shutil, subprocess, tempfile, zipfile
import accessories278_resources as overlay

R=Path(__file__).resolve().parents[1]
RES=R/'src/main/resources'
OUT=R/'docs/implementation/accessories278-20261025'
checks=[]
def check(ok,label):
    if not ok: raise AssertionError(label)
    checks.append(label)
def old(path): return subprocess.check_output(['git','show','6fa28fe9:'+path],cwd=R).decode('utf8').replace('\r\n','\n')

slot_paths=[f'data/slavicmyths/curios/slots/{slot}.json' for slot in ['head','necklace','ring','belt','charm']]
for path in slot_paths+['data/slavicmyths/curios/entities/accessories.json']:
    check((RES/path).read_text()==old('src/main/resources/'+path),'existing player slot topology unchanged '+path)
for slot in ['head','necklace','ring','belt','charm']:
    path=f'src/main/resources/data/curios/tags/item/{slot}.json'
    before=json.loads(old(path));after=json.loads((R/path).read_text())
    expected=before['values']+['slavicmyths:'+id for id in overlay.IDS] if slot=='charm' else before['values']
    check(after['values']==expected,'exact slot membership '+slot)
for lang,entries in overlay.LABELS.items():
    path=f'src/main/resources/assets/slavicmyths/lang/{lang}.json'
    before=json.loads(old(path));after=json.loads((R/path).read_text(encoding='utf8'))
    check({k for k in before if before[k]!=after.get(k)}=={'tooltip.slavicmyths.'+id for id in overlay.IDS}|set(overlay.TEXT[lang]),'only scoped tooltips, book and talent text changed '+lang)
    check(before.keys()==after.keys(),'no names/IDs removed '+lang)
with tempfile.TemporaryDirectory() as folder:
    root=Path(folder)
    paths=[f'src/main/resources/assets/slavicmyths/lang/{l}.json' for l in overlay.LABELS]+['src/main/resources/data/curios/tags/item/charm.json']
    for path in paths:
        p=root/path;p.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(R/path,p)
    overlay.apply(root);overlay.apply(root)
    for path in paths:check((root/path).read_bytes()==(R/path).read_bytes(),'scoped generator idempotence '+path)
path='src/main/java/org/slavicmyths/item/StormProtection.java'
expected=old(path).replace('if (!(event.getEntity() instanceof Player)) return;','if (event.getEntity().level().isClientSide || !(event.getEntity() instanceof Player)) return;')
for id in overlay.IDS:
    expected=expected.replace('player.getOffhandItem().getItem() == ModItems.'+id.upper()+'.get()', 'PassiveCharm.protects(player, ModItems.'+id.upper()+'.get())')
check(expected==(R/path).read_text(),'all damage types, talent and armor multipliers preserved exactly')
source=(R/'src/main/java/org/slavicmyths/item/PassiveCharm.java').read_text()
check('if (entity.getOffhandItem().is(item)) return true;' in source and 'inventory.isSlotActive(slot, index)' in source and 'findCurios(' not in source,'single boolean protection, current active slots without item search cache')
for path in ['item/PassiveCharm.java','item/StormProtection.java']:
    check('net.minecraft.client' not in (R/'src/main/java/org/slavicmyths'/path).read_text(),'common client isolation '+path)
jar=R/'build/libs/slavicmyths-2.0.1-HELL.jar'
with zipfile.ZipFile(jar) as z:
    for name in ['PassiveCharm','AmberCharmItem','PerunCharmItem','SimpleCharmItem','StormProtection']:
        check('org/slavicmyths/item/'+name+'.class' in z.namelist(),'packaged class '+name)
    for path in ['data/curios/tags/item/charm.json','assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json']:
        check(z.read(path)==(RES/path).read_bytes(),'exact packaged resource '+path)
    check(not any('Accessories278Offline' in name for name in z.namelist()),'offline fixture excluded')
report={'checks':checks,'count':len(checks),'jar':str(jar.resolve()),'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'game_launches':0,'gameplay':'NOT_RUN'}
(OUT/'source-jar.json').write_text(json.dumps(report,indent=2)+'\n')
print('ACCESSORIES278_SOURCE_JAR PASS checks='+str(len(checks))+' SHA256='+report['sha256'])
