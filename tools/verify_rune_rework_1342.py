"""Production resource/preservation gates. Does not launch Minecraft."""
from pathlib import Path
import hashlib,json,re,zipfile
from PIL import Image
import rune_rework_resources_1342 as overlay
ROOT=Path(__file__).resolve().parents[1]; RES=ROOT/'src/main/resources'
checks=0
def check(ok,message):
 global checks
 checks+=1
 if not ok: raise AssertionError(message)
def main():
 global checks
 log=(ROOT/'.tools/rune-1342/build.log').read_text(errors='replace')
 check('BUILD SUCCESSFUL' in log,'clean build')
 for marker in ['RUNE_REWORK_OFFLINE_CHECKS=355','ARMOR_OFFLINE_CHECKS=147','RUNE_OFFLINE_CHECKS=75064']:check(marker in log,marker)
 jar=ROOT/'build/libs/slavicmyths-1.3.4.2.jar'
 ids=[d[0] for d in overlay.DEFS]+overlay.LEGACY
 allowed={f'assets/slavicmyths/{kind}/rune_{id}.{ext}' for id in ids for kind,ext in [('textures/item','png'),('models/item','json')]}|{f'assets/slavicmyths/lang/{lang}.json' for lang in ['ru_ru','en_us']}
 changed=[];preserved=0
 with zipfile.ZipFile(ROOT/'.tools/rune-1342/baseline.jar') as old,zipfile.ZipFile(jar) as new:
  before=set(old.namelist());after=set(new.namelist())
  check(not any('/verify/' in n or '/smoke/' in n for n in after),'test code excluded')
  check('1.3.4.2' in new.read('META-INF/neoforge.mods.toml').decode(),'production version')
  for n in before:
   check(n in after,'removed old entry '+n)
   if n.startswith(('assets/','data/')):
    if n not in allowed:check(old.read(n)==new.read(n),'unrelated resource changed '+n);preserved+=1
    elif '/lang/' in n:
     original=json.loads(old.read(n));current=json.loads(new.read(n));renames={'item.slavicmyths.rune_'+d[0] for d in overlay.DEFS}
     for k,v in original.items():
      if k not in renames:check(current.get(k)==v,'old translation '+k)
   if n.endswith('.class') and old.read(n)!=new.read(n):changed.append(n)
  prefixes=('org/slavicmyths/SlavicMyths','org/slavicmyths/item/ItemState','org/slavicmyths/rpg/Rune','org/slavicmyths/rpg/Runes','org/slavicmyths/rpg/RpgMenu','org/slavicmyths/rpg/RpgEvents','org/slavicmyths/client/RpgClient','org/slavicmyths/client/RuneAnvilScreen','org/slavicmyths/compat/JeiRunes','org/slavicmyths/combat/RareBleedEffect')
  check(all(n.startswith(prefixes) for n in changed),'unexpected code changes '+str(changed))
  for n in after:
   if n.endswith('/'):continue
   if n.endswith('.json'):json.loads(new.read(n));checks+=1
   if n.startswith(('assets/','data/')):check(new.read(n)==(RES/n).read_bytes(),'stale production resource '+n)
   if n.startswith(('org/slavicmyths/rpg/Rune','org/slavicmyths/combat/RareBleedEffect')) and n.endswith('.class'):
    check(b'net/minecraft/client' not in new.read(n) and b'mezz/jei' not in new.read(n),'common client dependency '+n)
 recipes=sorted((RES/'data/slavicmyths/recipe').glob('rune_rework_*.json'));check(len(recipes)==8,'eight recipes')
 generated=recipes+[RES/f'assets/slavicmyths/lang/{l}.json' for l in ['ru_ru','en_us']]+[RES/'data/slavicmyths/tags/item/rune_melee_weapons.json']
 for id,tier,base,ru,en in overlay.DEFS:
  r=json.loads((RES/f'data/slavicmyths/recipe/rune_rework_{id}.json').read_text())
  check(r['type']=='slavicmyths:rune_crafting' and len(r['inputs'])==3 and not r['chisel'],'anvil recipe '+id)
  check(r['inputs'][0]=={'ingredient':{'item':f'slavicmyths:blank_rune_{tier}'},'count':1,'base':{'tier':tier,'material':base}},'component-sensitive base '+id)
  check(r['result']=={'id':'slavicmyths:rune_'+id,'count':1},'result '+id)
 for id in ids:
  p=RES/f'assets/slavicmyths/textures/item/rune_{id}.png';model=RES/f'assets/slavicmyths/models/item/rune_{id}.json';generated.extend([p,model])
  im=Image.open(p);check(im.size==(32,32) and im.mode=='RGBA' and im.getchannel('A').getextrema()==(0,255),'physical sprite '+id)
  check(json.loads(model.read_text())['textures']['layer0']=='slavicmyths:item/rune_'+id,'model '+id)
 check(len({hashlib.sha256((RES/f'assets/slavicmyths/textures/item/rune_{id}.png').read_bytes()).hexdigest() for id in ids})==14,'distinct sprites')
 for lang in ['ru_ru','en_us']:
  data=json.loads((RES/f'assets/slavicmyths/lang/{lang}.json').read_text(encoding='utf-8'))
  for id in ids:check(bool(data.get('item.slavicmyths.rune_'+id)),'localization '+id)
  for key in overlay.TEXT:check(bool(data.get('rune2.slavicmyths.'+key)),'effect localization '+key)
 tag=json.loads((RES/'data/slavicmyths/tags/item/rune_melee_weapons.json').read_text())
 for v in tag['values']:
  if v.startswith('#slavicmyths:'):check((RES/('data/slavicmyths/tags/item/'+v.split(':')[1]+'.json')).exists(),'tag reference '+v)
  elif v.startswith('slavicmyths:'):check((RES/('assets/slavicmyths/models/item/'+v.split(':')[1]+'.json')).exists(),'item model '+v)
 hashes={p:hashlib.sha256(p.read_bytes()).hexdigest() for p in generated};overlay.main()
 for p,d in hashes.items():check(hashlib.sha256(p.read_bytes()).hexdigest()==d,'reproducible '+p.name)
 result={'version':'1.3.4.2','resource_checks':checks,'preserved_old_resources':preserved,'changed_old_classes':changed,'rune_offline_checks':355,'armor_offline_checks':147,'foundation_offline_checks':75064,'real_client_runs':0,'real_server_runs':0,'runtime_verified':False,'jar_bytes':jar.stat().st_size,'jar_sha256':hashlib.sha256(jar.read_bytes()).hexdigest()}
 out=ROOT/'docs/verification/runes-rework-1.3.4.2.json';out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(result,ensure_ascii=False))
if __name__=='__main__':main()
