"""Ingredient intersection audit, including recursively expanded vanilla/mod item tags.
Shaped recipes are trimmed and mirrored; shapeless overlap uses bipartite matching.
No Minecraft process is started. Special serializers are reported separately.
"""
import json,zipfile,itertools
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources'
JAR=Path.home()/'.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar'
recipes={};tags={}
for archive in (Path.home()/'.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/21.1.255').rglob('*universal.jar'):
 with zipfile.ZipFile(archive) as z:
  for n in z.namelist():
   if n.startswith('data/') and '/tags/item/' in n and n.endswith('.json'):
    ns=n.split('/')[1];tags[ns+':'+n.split('/tags/item/')[1][:-5]]=json.loads(z.read(n))['values']
with zipfile.ZipFile(JAR) as z:
 for n in z.namelist():
  if n.startswith('data/minecraft/recipe/') and n.endswith('.json'):recipes['minecraft:'+n.split('recipe/')[1][:-5]]=json.loads(z.read(n))
  if n.startswith('data/minecraft/tags/item/') and n.endswith('.json'):tags['minecraft:'+n.split('tags/item/')[1][:-5]]=json.loads(z.read(n))['values']
for p in RES.glob('data/*/tags/item/**/*.json'):
 ns=p.parts[p.parts.index('data')+1];key=ns+':'+p.relative_to(RES/'data'/ns/'tags/item').as_posix()[:-5];d=json.loads(p.read_text());tags[key]=([] if d.get('replace') else tags.get(key,[]))+d['values']
for p in (RES/'data/slavicmyths/recipe').rglob('*.json'):recipes['slavicmyths:'+p.relative_to(RES/'data/slavicmyths/recipe').as_posix()[:-5]]=json.loads(p.read_text())
unresolved=set()
def expand(key,seen=frozenset()):
 if key in seen:return set()
 if key not in tags:unresolved.add(key);return {'?'+key}
 result=set()
 for v in tags[key]:
  if isinstance(v,dict) and not v.get('required',True) and v['id'].startswith('#') and v['id'][1:] not in tags:continue
  v=v if isinstance(v,str) else v['id'];result.update(expand(v[1:],seen|{key}) if v.startswith('#') else [v])
 return result
def ingredient(v):
 if not v:return frozenset([''])
 if isinstance(v,list):return frozenset().union(*(ingredient(x) for x in v))
 return frozenset([v['item']]) if 'item' in v else frozenset(expand(v['tag']))
def shape(r):
 rows=r['pattern'];rows=[row for row in rows if row.strip()];lo=min(i for row in rows for i,c in enumerate(row) if c!=' ');hi=max(i for row in rows for i,c in enumerate(row) if c!=' ')
 return [[ingredient(r['key'].get(c)) for c in row[lo:hi+1]] for row in rows]
def grid_overlap(a,b):return len(a)==len(b) and len(a[0])==len(b[0]) and all(x&y for ar,br in zip(a,b) for x,y in zip(ar,br))
def matching(a,b):
 if len(a)!=len(b):return False
 def visit(i,used):
  return i==len(a) or any(a[i]&b[j] and visit(i+1,used|{j}) for j in range(len(b)) if j not in used)
 return visit(0,set())
supported={k:v for k,v in recipes.items() if v.get('type') in ['minecraft:crafting_shaped','minecraft:crafting_shapeless','slavicmyths:armorer_shaped','slavicmyths:armorer_shapeless']}
conflicts=[]
for (ka,a),(kb,b) in itertools.combinations(supported.items(),2):
 if not (ka.startswith('slavicmyths:') or kb.startswith('slavicmyths:')):continue
 if a['type'].split(':')[0]!=b['type'].split(':')[0]:continue
 ash='pattern' in a;bsh='pattern' in b
 if ash and bsh:
  aa=shape(a);bb=shape(b);overlap=grid_overlap(aa,bb) or grid_overlap(aa,[row[::-1] for row in bb])
 else:
  aa=[x for row in shape(a) for x in row if x!=frozenset([''])] if ash else list(map(ingredient,a['ingredients']))
  bb=[x for row in shape(b) for x in row if x!=frozenset([''])] if bsh else list(map(ingredient,b['ingredients']))
  overlap=matching(aa,bb)
 if overlap:conflicts.append([ka,kb])
process_conflicts=[]
for (ka,a),(kb,b) in itertools.combinations(recipes.items(),2):
 if not (ka.startswith('slavicmyths:') or kb.startswith('slavicmyths:')) or a.get('type')!=b.get('type') or a.get('result')==b.get('result'):continue
 typ=a.get('type');overlap=False
 if typ in ['minecraft:smelting','minecraft:smoking','minecraft:campfire_cooking','slavicmyths:drying','slavicmyths:chud_bloomery','slavicmyths:refining_hearth']:
  overlap=bool(ingredient(a['ingredient'])&ingredient(b['ingredient'])) and bool(ingredient(a.get('flux'))&ingredient(b.get('flux')))
 elif typ=='slavicmyths:rune_crafting':
  aa=a['inputs'];bb=b['inputs'];overlap=len(aa)==len(bb) and all(ingredient(x['ingredient'])&ingredient(y['ingredient']) and (not x.get('base') or not y.get('base') or x['base']==y['base']) for x,y in zip(aa,bb))
 elif typ in ['slavicmyths:vat','slavicmyths:kitchen']:
  if a.get('mode')!=b.get('mode') or not ingredient(a.get('catalyst',a.get('tool')))&ingredient(b.get('catalyst',b.get('tool'))):continue
  aa=[ingredient(v['ingredient']) for v in a['ingredients']];bb=[ingredient(v['ingredient']) for v in b['ingredients']]
  overlap=all(any(x&y for y in bb) for x in aa) and all(any(y&x for x in aa) for y in bb)
 if overlap:process_conflicts.append([ka,kb])
report={'recipes':len(recipes),'supported':len(supported),'conflicts':conflicts,'process_overlap_candidates':process_conflicts,'unresolved_tags':sorted(unresolved),'other_serializers':sorted({r.get('type','') for k,r in recipes.items() if k.startswith('slavicmyths:') and k not in supported})}
p=ROOT/'docs/implementation/feedback-20261024/recipe-audit.json';p.write_text(json.dumps(report,indent=2));print(json.dumps(report,indent=2))
