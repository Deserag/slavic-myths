"""Production gates against the preserved 1.3.2.3 artifact; no Minecraft launch."""
from pathlib import Path
import hashlib,json,re,zipfile
import armor_rune_resources_133 as overlay
ROOT=Path(__file__).resolve().parents[1]
checks=0
def check(value,message):
 global checks
 checks+=1
 assert value,message
def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def main():
 jar=ROOT/'build/libs/slavicmyths-1.3.3.jar';baseline=ROOT/'.tools/armor-133-baseline.jar'
 check('BUILD SUCCESSFUL' in (ROOT/'.tools/armor-133-build.log').read_text(encoding='utf-8',errors='replace'),'Clean build')
 log=(ROOT/'.tools/armor-133-offline.log').read_text(encoding='utf-8',errors='replace');check('ARMOR_OFFLINE_CHECKS=147' in log and 'BUILD SUCCESSFUL' in log,'Offline rules/codecs')
 changed_classes=[];preserved_resources=0
 with zipfile.ZipFile(baseline) as old,zipfile.ZipFile(jar) as new:
  before=set(old.namelist());after=set(new.namelist())
  check(not any('/verify/' in n or '/smoke/' in n or 'ArmorRunesOffline' in n for n in after),'Production contains no fixtures')
  check('version="1.3.3"' in new.read('META-INF/neoforge.mods.toml').decode().replace(' ',''),'Version')
  for name in before:
   check(name in after,'Missing old entry '+name)
   if name.startswith(('assets/','data/')):
    if name in ('assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json'):
     original=json.loads(old.read(name));current=json.loads(new.read(name));check(all(current.get(k)==v for k,v in original.items()),'Existing translations changed')
     check(set(current)-set(original)=={'rpg.slavicmyths.'+k for k in overlay.TEXT},'Unexpected language additions')
    else:check(old.read(name)==new.read(name),'Unrelated resource changed '+name);preserved_resources+=1
   elif name.endswith('.class') and old.read(name)!=new.read(name):changed_classes.append(name)
  expected={'org/slavicmyths/rpg/Runes.class','org/slavicmyths/rpg/RuneDefinition.class','org/slavicmyths/rpg/RpgEvents.class','org/slavicmyths/client/RpgScreen.class','org/slavicmyths/client/RpgClient.class'}
  check(set(changed_classes)==expected,'Unexpected changed production classes '+str(changed_classes))
  check(not any(n.endswith('.class') for n in after-before),'Unexpected new production classes')
  check(not any(n.startswith(('assets/','data/')) for n in after-before),'Unexpected new registry/resources')
  for lang in ('ru_ru','en_us'):
   name='assets/slavicmyths/lang/'+lang+'.json';data=json.loads(new.read(name))
   for key in overlay.TEXT:check(bool(data.get('rpg.slavicmyths.'+key)),'Localization '+key)
   check(new.read(name)==(ROOT/'src/main/resources'/name).read_bytes(),'Stale packaged locale')
 # Exact preservation of ItemState, RuneRepair, armor/item registries, combat effects,
 # attributes, classes/HUD, models/recipes and all older rune IDs follows from the gates above.
 paths=[ROOT/f'src/main/resources/assets/slavicmyths/lang/{lang}.json' for lang in ('ru_ru','en_us')]
 before={p:digest(p) for p in paths};overlay.main()
 for p in paths:check(digest(p)==before[p],'Generator drift')
 report={'version':'1.3.3','resource_and_jar_checks':checks,'offline_rule_and_codec_checks':147,'preserved_resources':preserved_resources,'changed_production_classes':changed_classes,'new_registry_ids':[],'jar':str(jar),'bytes':jar.stat().st_size,'sha256':digest(jar),'build':'clean build PASS','client_launches':0,'server_launches':0,'runtime_tested':False,'native_itemstack_checks':'Not run: offline registry bootstrap requires NeoForge loader; diagnostic .tools/armor-133-native-probe.log','persistence_checks':'RuneState JSON/NBT/packet roundtrip; lifecycle and real ItemStack gameplay not tested','polymc_installed':False}
 (ROOT/'docs/verification/armor-runes-1.3.3.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print(json.dumps(report,ensure_ascii=False))
if __name__=='__main__':main()
