"""Current source registration + resolved static tag inventory, not a launched runtime registry."""
from pathlib import Path
import csv,json,re,hashlib,zipfile
R=Path(__file__).resolve().parents[1];J=R/'src/main/java/org/slavicmyths';O=R/'docs/implementation/creatures-79';T=R/'src/main/resources/data/slavicmyths/tags/entity_type'
LEGACY=['bleed_immune','class_bloodless','silver_vulnerable','class_mythical','class_control_resistant','control_immune','combat_protected','village_hostiles','ominous_aura_immune']
def inventory():
 rows=[]
 for p in sorted(J.rglob('*.java')):
  s=p.read_text(encoding='utf-8')
  for m in re.finditer(r'(?:ModEntities\.)?ENTITIES\.register\(\s*"([a-z0-9_]+)"',s):
   d=s[m.start():s.find(';',m.end())]
   if 'EntityType.Builder' in d:rows.append(dict(id='slavicmyths:'+m[1],source=p.relative_to(R).as_posix(),line=s.count('\n',0,m.start())+1,declaration=d))
  if p.name=='ModEntities.java':
   for m in re.finditer(r'=\s*wildlife\("([a-z_]+)"\s*,\s*([^;]+)',s):rows.append(dict(id='slavicmyths:'+m[1],source=p.relative_to(R).as_posix(),line=s.count('\n',0,m.start())+1,declaration=m[0]))
  if p.name=='ChudPeople.java':
   for m in re.finditer(r'=type\("([a-z_]+)"\)',s):rows.append(dict(id='slavicmyths:'+m[1],source=p.relative_to(R).as_posix(),line=s.count('\n',0,m.start())+1,declaration='ChudPeople.type: '+m[0]))
 assert len(rows)==len({r['id'] for r in rows}),'Duplicate source declarations'
 return sorted(rows,key=lambda r:r['id'])
def tags():
 out={}
 # Resolve the installed vanilla tag definitions as data, without booting a world or game.
 jars=list((R/'build/moddev/artifacts').glob('*client-extra*jar'))
 if jars:
  with zipfile.ZipFile(jars[0]) as z:
   for name in z.namelist():
    if name.startswith('data/minecraft/tags/entity_type/') and name.endswith('.json'):out['minecraft:'+name.rsplit('/',1)[-1][:-5]]=json.loads(z.read(name))['values']
 for p in (R/'src/main/resources/data').glob('*/tags/entity_type/*.json'):
  key=p.parents[2].name+':'+p.stem;data=json.loads(p.read_text(encoding='utf-8'))
  if data.get('replace',False) or key not in out:out[key]=[]
  out[key]+=data['values']
 def resolve(name,stack=()):
  assert name not in stack,'Tag cycle '+name
  result=set()
  for v in out.get(name,[]):
   if isinstance(v,dict):v=v['id']
   if v.startswith('#'):result|=resolve(v[1:],(*stack,name))
   else:result.add(v)
  return result
 return {n:resolve(n) for n in out}
def validation_error(id,natures,traits,expected=None,is_new=False):
 if len(natures)>1 or 'FIRE_AFFINITY' in traits and 'ICE_AFFINITY' in traits:return 'CONFLICT'
 if expected and natures!=[expected]:return 'PRIMARY_MISMATCH'
 if is_new and len(natures)!=1:return 'UNCLASSIFIED_NEW_ID'
 return None

def main():
 rows=inventory();resolved=tags();old={r['entity_type']:r for r in csv.DictReader((R/'docs/audit/creatures-1.9.8.1/CREATURES.csv').open(encoding='utf-8-sig'))};approved=json.loads((O/'approved-primary.json').read_text(encoding='utf-8'));primary={"slavicmyths:"+i:n for n,ids in approved.items() for i in ids};assert len(primary)==69
 assert not(set(old)-{r['id'] for r in rows}),'Old registration missing'
 typed=[];fail=[]
 for row in rows:
  id=row['id'];natures=[n for n in approved if id in resolved.get('slavicmyths:nature_'+n.lower(),set())];traits=[t.split(':trait_')[1].upper() for t,v in resolved.items() if t.startswith('slavicmyths:trait_') and id in v]
  error=validation_error(id,natures,traits,primary.get(id),id not in old)
  if error:fail.append([id,error])
  row.update(nature=natures[0] if len(natures)==1 else 'PROJECTION' if 'PROJECTION' in traits else 'NO_PHYSIOLOGY',traits=traits,old_group=old.get(id,{}).get('group','ABSENT_FROM_OLD_AUDIT'),implementation=old.get(id,{}).get('implementation','org.slavicmyths.industry.ChudPerson'),runtime_registry='NOT_RUN')
  typed.append(row)
 (O/'source-inventory.json').write_text(json.dumps(typed,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 calls=[]
 aliases={'bleed_immune':{'CutEffect.IMMUNE','CutEffect:is(IMMUNE)'},'class_bloodless':{'CreatureProperties:is(OLD_BLOODLESS)'},'silver_vulnerable':{'SilverCombat:is(VULNERABLE)','CreatureProperties:is(SILVER)'},'class_mythical':{'ClassControl.MYTHICAL','ClassControl:is(MYTHICAL)'},'class_control_resistant':{'ClassControl.RESISTANT','ClassControl:is(RESISTANT)','CreatureProperties:is(CONTROL)'},'control_immune':{'CombatTargeting.CONTROL_IMMUNE','CombatTargeting:is(CONTROL_IMMUNE)'},'combat_protected':{'CombatTargeting.PROTECTED','CombatTargeting:is(PROTECTED)'},'village_hostiles':{'Military.HOSTILES','CombatTargeting.HOSTILE','CombatTargeting:is(HOSTILE)'},'ominous_aura_immune':{'OminousAura.IMMUNE','OminousAura:is(IMMUNE)'}}
 for tag in LEGACY:
  uses=[]
  for p in J.rglob('*.java'):
   for line,text in enumerate(p.read_text(encoding='utf-8').splitlines(),1):
    if '"'+tag+'"' in text or any((token.split(':',1)[0]==p.stem and token.split(':',1)[1] in text) if ':' in token else token in text for token in aliases.get(tag,set())):uses.append(p.relative_to(R).as_posix()+':'+str(line))
  calls.append(dict(tag=tag,entries=sorted(resolved.get('slavicmyths:'+tag,set())),callsites=uses,sha256=hashlib.sha256((T/(tag+'.json')).read_bytes()).hexdigest()))
 undead_calls=[p.relative_to(R).as_posix()+':'+str(i) for p in J.rglob('*.java') for i,line in enumerate(p.read_text(encoding='utf-8').splitlines(),1) if 'EntityTypeTags.UNDEAD' in line or '"minecraft:undead"' in line]
 calls.append(dict(tag='minecraft:undead',entries=sorted(resolved.get('minecraft:undead',set())),callsites=undead_calls,source='installed Minecraft resources, no vanilla tag rewritten'))
 (O/'legacy-tags.json').write_text(json.dumps(calls,indent=2)+'\n',encoding='utf-8')
 assert validation_error('slavicmyths:unclassified_new',[],[],is_new=True)=='UNCLASSIFIED_NEW_ID'
 assert validation_error('slavicmyths:conflicting',['SPIRIT','ANIMAL'],[])=='CONFLICT'
 assert validation_error('slavicmyths:conflicting',['SPIRIT'],['FIRE_AFFINITY','ICE_AFFINITY'])=='CONFLICT'
 assert 'slavicmyths:leshy' in resolved['slavicmyths:silver_vulnerable'] and 'slavicmyths:leshy' not in resolved['slavicmyths:nature_mythical_beast']
 result=dict(source_registrations=len(rows),approved_primary=69,old_auxiliary=sum(r['group']=='auxiliary' for r in old.values()),absent_from_old_audit=[r['id'] for r in rows if r['id'] not in old],failures=fail,runtime_registry='NOT_RUN',status='PASS_SOURCE_ONLY' if not fail else 'FAIL')
 (O/'inventory-check.json').write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8');print(json.dumps(result));assert not fail
 return typed,resolved
if __name__=='__main__':main()
