from pathlib import Path
import json,csv,zipfile,hashlib,re
from PIL import Image,ImageDraw
R=Path('src/main/resources');O=Path('docs/audit/runtime-1.3.4.3');O.mkdir(parents=True,exist_ok=True)
items=json.loads(Path('.tools/runtime-audit/server/registry-export.json').read_text());tabs=json.loads(Path('.tools/runtime-audit/server/creative-export.json').read_text())
(O/'REGISTRY_EXPORT.json').write_text(json.dumps(items,ensure_ascii=False,indent=2));(O/'CREATIVE_CURRENT.json').write_text(json.dumps(tabs,ensure_ascii=False,indent=2))
errors=[];jsons=0;pngs=0
for p in R.rglob('*.json'):
 try: d=json.loads(p.read_text(encoding='utf-8'));jsons+=1
 except Exception as e:errors.append(str(p)+': '+str(e));continue
 if '/models/' in p.as_posix():
  refs=[]
  if isinstance(d.get('parent'),str):refs.append(('model',d['parent']))
  refs.extend(('texture',v) for v in d.get('textures',{}).values() if isinstance(v,str) and not v.startswith('#'))
  refs.extend(('model',v['model']) for v in d.get('overrides',[]) if 'model'in v)
  for kind,ref in refs:
   if not ref.startswith('slavicmyths:'):continue
   q=R/'assets/slavicmyths'/('models'if kind=='model'else'textures')/(ref.split(':')[1]+('.json'if kind=='model'else'.png'))
   if not q.exists():errors.append(str(p)+': missing '+ref)
for p in R.rglob('*.png'):
 try:
  with Image.open(p) as im:im.verify();pngs+=1
 except Exception as e:errors.append(str(p)+': '+str(e))
langs={l:json.loads((R/f'assets/slavicmyths/lang/{l}.json').read_text(encoding='utf-8'))for l in ['ru_ru','en_us']}
missing=[]
for it in items:
 id=it['item'].split(':')[1]
 if not(R/f'assets/slavicmyths/models/item/{id}.json').exists():missing.append(id)
# ItemNameBlockItem can intentionally use block descriptions.
lang_missing={l:[i['item']for i in items if 'item.slavicmyths.'+i['item'].split(':')[1]not in langs[l] and 'block.slavicmyths.'+i['item'].split(':')[1]not in langs[l]]for l in langs}
jar=Path('build/libs/slavicmyths-1.3.4.3.jar')
with zipfile.ZipFile(jar)as z:
 fixture=[n for n in z.namelist()if '/verify/'in n or n.startswith('org/slavicmyths/smoke/')]
 report=dict(json_files=jsons,png_files=pngs,registered_items=len(items),errors=errors,missing_item_models=missing,missing_translations=lang_missing,production_test_classes=fixture,jar_sha256=hashlib.sha256(jar.read_bytes()).hexdigest())
(O/'RESOURCE_AUDIT.json').write_text(json.dumps(report,ensure_ascii=False,indent=2))
fields='item,source,damage,attack_speed,dps,durability,reach,rune_slots,special_mechanic,comparison,verdict'.split(',')
special={'tugarin_sword':'Pair: x1.4 charged damage; native bleed 0.5 HP/stack, max3','likho_staff':'Heal1HP/180ticks; morok120ticks+stun25ticks; cooldown560; RMB ray12','ataman_flail':'Fourth chained attack bonus; pause resets chain','victor_spear':'Far strike opens second native attack','storm_staff':'RMB magic cone:3HP;7blocks;80ticks cooldown','veles_staff':'Guardian60sec;cooldown120sec;48uses','vodyanoy_net':'RMB projectile slowdown3sec land/5sec water;cooldown8sec','seven_league_boots':'LEATHER armor artifact, not flying boots'}
texts={p:p.read_text(encoding='utf-8')for p in (R/'data/slavicmyths').rglob('*.json')}
with (O/'BALANCE_TABLE.csv').open('w',encoding='utf-8-sig',newline='')as f:
 w=csv.DictWriter(f,fieldnames=fields);w.writeheader()
 for i in items:
  id=i['item'].split(':')[1];cls=i['class'];weapon=i['damage']>1 or any(t in cls for t in ['Staff','Shield','Bow','Net'])or id=='seven_league_boots'
  if not weapon:continue
  sources=[p.relative_to(R).as_posix()for p,s in texts.items()if i['item']in s and ('/recipe/'in p.as_posix()or'/loot_table/'in p.as_posix())]
  melee=i['damage']>1;dps=round(i['damage']*i['attack_speed'],3)if melee else ''
  verdict='Compare special utility separately; nominal melee DPS ignores armor/crit/charge/bleed'if melee else 'Utility/ranged item: melee DPS not applicable'
  w.writerow(dict(item=i['item'],source='; '.join(sources)or'No static recipe/loot reference found; dynamic source needs review',damage=i['damage']if melee else '',attack_speed=i['attack_speed']if melee else '',dps=dps,durability=i['durability'],reach=i['reach'],rune_slots=i['rune_slots'],special_mechanic=special.get(id,''),comparison='Iron sword6/1.6/9.6; diamond7/1.6/11.2; netherite8/1.6/12.8',verdict=verdict))
# Reference rows explicitly separated from measured registry values.
with (O/'BALANCE_TABLE.csv').open('a',encoding='utf-8-sig',newline='')as f:
 w=csv.DictWriter(f,fieldnames=fields)
 for id,d,dur in [('iron_sword',6,250),('diamond_sword',7,1561),('netherite_sword',8,2031)]:w.writerow(dict(item='minecraft:'+id,source='Vanilla recipe',damage=d,attack_speed=1.6,dps=d*1.6,durability=dur,reach=3,rune_slots='iron1/diamond2/netherite2',special_mechanic='',comparison='Vanilla baseline; not captured by mod-item export',verdict='Reference'))
thumbs=[]
for id in ['short_bow','heavy_bow','heavy_bow_pulling_0','heavy_bow_pulling_1','heavy_bow_pulling_2','storm_staff','tugarin_sword','likho_staff','throwing_knife','folk_materials']:
 p=R/f'assets/slavicmyths/textures/item/{id}.png'
 if p.exists():thumbs.append((id,Image.open(p).convert('RGBA')))
out=Image.new('RGB',(800,240*((len(thumbs)+3)//4)), '#333333');dr=ImageDraw.Draw(out)
for n,(name,im)in enumerate(thumbs):
 x=(n%4)*200;y=(n//4)*240;im.thumbnail((160,160));im=im.resize((160,160),Image.Resampling.NEAREST);out.paste(im,(x+20,y+30),im);dr.text((x+5,y+205),name,fill='white')
out.save(O/'screenshots/ASSET_REFERENCE_NOT_GAME.png')
print(json.dumps(report,ensure_ascii=False))
