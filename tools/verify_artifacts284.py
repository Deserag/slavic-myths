"""Scoped source/resource/production JAR checks. Game results are separate evidence."""
from pathlib import Path
import hashlib,json,subprocess,zipfile,tempfile,shutil
import artifacts284_resources as overlay
R=Path(__file__).resolve().parents[1];OUT=R/'docs/implementation/artifacts284-20261025';checks=[]
def check(ok,label):
 if not ok:raise AssertionError(label)
 checks.append(label)
def source(file):return (R/'src/main/java/org/slavicmyths'/file).read_text(encoding='utf8')
def before(file):return (OUT/'before'/file).read_text(encoding='utf8')
file='mor/MorFlaskItem.java'
check(source(file)==before(file).replace('level.addFreshEntity(flask);','if(!level.addFreshEntity(flask))return InteractionResultHolder.fail(stack);'),'flask only adds native spawn acceptance; cooldown10/sound/creative behavior unchanged')
file='quest185/Quest185Item.java';old=before(file)
old=old.replace('p.addEffect(new MobEffectInstance(Quest185Content.WARD_EFFECT,24000,0,false,true,true));','if(!p.addEffect(new MobEffectInstance(Quest185Content.WARD_EFFECT,24000,0,false,true,true)))return InteractionResultHolder.fail(stack);')
old=old.replace('n.putLong("TrackerReady",now+12000);n.putLong("TrackerUntil",now+3600);p.addEffect(new MobEffectInstance(Quest185Content.TRACKER_EFFECT,3600,0,false,true,true));','if(!p.addEffect(new MobEffectInstance(Quest185Content.TRACKER_EFFECT,3600,0,false,true,true)))return InteractionResultHolder.fail(stack);n.putLong("TrackerReady",now+12000);n.putLong("TrackerUntil",now+3600);')
check(source(file)==old,'quest only native effect acceptance before item/CD commit; durations/gates/special GuardianWard unchanged')
file='sanctuary/SanctuaryItem.java';old=before(file)
old=old.replace('s.shrink(1);p.addEffect(new MobEffectInstance(SanctuaryContent.GUARD,SanctuaryConfig.WARD_SECONDS.get()*20));','if(!p.addEffect(new MobEffectInstance(SanctuaryContent.GUARD,SanctuaryConfig.WARD_SECONDS.get()*20)))return InteractionResultHolder.fail(s);s.shrink(1);')
old=old.replace('n.putLong("Sanctuary193TearReady",now(p)+SanctuaryConfig.TEAR_COOLDOWN.get()*20);p.addEffect(new MobEffectInstance(SanctuaryContent.TEAR,160));','if(!p.addEffect(new MobEffectInstance(SanctuaryContent.TEAR,160)))return stack;n.putLong("Sanctuary193TearReady",now(p)+SanctuaryConfig.TEAR_COOLDOWN.get()*20);')
check(source(file)==old,'sanctuary only accepted effect before commit; water/flame vial/other behavior unchanged')
changed=subprocess.check_output(['git','diff','--name-only','4fba8ac1','--','src/main'],cwd=R,text=True).splitlines()
expected={'src/main/java/org/slavicmyths/'+f for f in ['mor/MorFlaskItem.java','quest185/Quest185Item.java','sanctuary/SanctuaryItem.java']}
expected_resources={f'src/main/resources/assets/slavicmyths/lang/{lang}.json' for lang in overlay.LABELS}
check(set(changed)==expected|expected_resources,'only three production Java files and two book language files; recipes/art/IDs/loot/armor/weapons unchanged')
for file in expected:check('net.minecraft.client' not in (R/file).read_text(encoding='utf8'),'common isolation '+file)
for lang,labels in overlay.LABELS.items():
 path=f'src/main/resources/assets/slavicmyths/lang/{lang}.json';old=json.loads(subprocess.check_output(['git','show','4fba8ac1:'+path],cwd=R).decode('utf8'));current=json.loads((R/path).read_text(encoding='utf8'))
 check(old.keys()==current.keys() and {key for key in old if old[key]!=current[key]}==set(labels),'only two existing book texts changed '+lang)
with tempfile.TemporaryDirectory() as folder:
 root=Path(folder)
 for lang in overlay.LABELS:
  path=f'src/main/resources/assets/slavicmyths/lang/{lang}.json';p=root/path;p.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(R/path,p)
 overlay.apply(root);overlay.apply(root)
 for lang in overlay.LABELS:
  path=f'src/main/resources/assets/slavicmyths/lang/{lang}.json';check((root/path).read_bytes()==(R/path).read_bytes(),'book overlay idempotence '+lang)
catalog=json.loads((OUT/'catalog.json').read_text(encoding='utf8'));check(catalog['required_catalog_count']==24 and catalog['total_entries']==34,'all24 required and10 additional real IDs')
check({e['id'] for e in catalog['items'] if e['status']=='BLOCKED_NEEDS_ACQUISITION_APPROVAL'}=={'gusli','skatert','kartaus_black_seal','kalin_battle_seal'},'honest four unresolved acquisition decisions')
jar=R/'build/libs/slavicmyths-2.0.1-HELL.jar'
with zipfile.ZipFile(jar) as z:
 for name in ['mor/MorFlaskItem','quest185/Quest185Item','sanctuary/SanctuaryItem']:
  path='org/slavicmyths/'+name+'.class';check(z.read(path)==(R/'build/classes/java/main'/path).read_bytes(),'exact final class '+name)
 check(not any('Artifacts284' in path or 'Accessories278GameTests' in path for path in z.namelist()),'no test fixtures in production JAR')
report={'checks':checks,'count':len(checks),'jar':str(jar.resolve()),'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'scope':'source/resource/package only; runtime in runtime.json'}
(OUT/'source-jar.json').write_text(json.dumps(report,indent=2)+'\n');print('ARTIFACTS284_SOURCE_JAR PASS checks='+str(len(checks))+' SHA256='+report['sha256'])
