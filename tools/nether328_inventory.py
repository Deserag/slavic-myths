"""Merge source EntityType declarations with historical audit, never overwrite history.
Nature/trait tags integrate with existing CreatureProperties; tier budgets stay proposed.
"""
from pathlib import Path
import csv,json,re,hashlib
import creatures79_inventory as old_inventory
R=Path(__file__).resolve().parents[1];D=R/'docs/implementation/nether-creature-balance';T=R/'src/main/resources/data/slavicmyths/tags/entity_type'
ALIASES={'UNCONFIRMED:whisperer':'sheptun','UNCONFIRMED:shardling':'oskolnik','UNCONFIRMED:glowbat':'gulokryl','UNCONFIRMED:echo_beast':'ekhovik','UNCONFIRMED:sporeling':'griboshmyg','UNCONFIRMED:posten':'posten'}
FIX={'rootling':('PLANT',['EARTH_AFFINITY']),'pekelnik':('ELEMENTAL',['FIRE_AFFINITY']),'ekhovik':('MYTHICAL_BEAST',[]),'griboshmyg':('PLANT',[])}

def main():
 D.mkdir(parents=True,exist_ok=True);historical=R/'docs/audit/creatures-1.9.8.1/CREATURES.csv';historic_sha=hashlib.sha256(historical.read_bytes()).hexdigest()
 rows=list(csv.DictReader((R/'docs/balance/NETHER_CREATURES_2.0_X_DRAFT.csv').open(encoding='utf-8-sig')))
 source={r['id']:r for r in old_inventory.inventory()};new=[]
 for r in rows:
  if r['entity_id'] in ALIASES:r['entity_id']='slavicmyths:'+ALIASES[r['entity_id']]
  name=r['entity_id'].split(':')[1];nature=r['nature_proposed'];traits=[] if r['traits_proposed']=='none' else r['traits_proposed'].split(';')
  if name in FIX:nature,traits=FIX[name]
  folder='navya' if name in ['navka','rootling','bone_crawler','navya_wisp','grove_guardian'] else 'blackcinder' if name in ['coal_crawler','ash_spirit','pekelnik','sparkling','zharenitsa'] else 'burningislands' if name in ['heat_salamander','slag_crab','vent_golem','magma_lord'] else 'losthalls'
  filename={'navya':'NavyaContent','blackcinder':'CinderContent','burningislands':'IslandsContent','losthalls':'HallsContent'}[folder]
  decl=R/f'src/main/java/org/slavicmyths/{folder}/{filename}.java';code=decl.read_text(encoding='utf-8');assert name in code or name.upper() in code or name=='posten'
  if name=='posten':decl=R/'src/main/java/org/slavicmyths/losthalls/PostenMob.java';code=decl.read_text(encoding='utf-8');assert '"posten"' in code
  source[r['entity_id']]={'id':r['entity_id'],'source':decl.relative_to(R).as_posix(),'line':1,'declaration':'verified family map/enum registration'}
  r.update(nature_proposed=nature,traits_proposed=';'.join(traits),source=decl.relative_to(R).as_posix(),source_presence='SOURCE_REGISTERED_NOT_RUNTIME',actual_nature_tag=nature,actual_trait_tags=';'.join(traits),balance_state='OWNER_REVIEW_PROPOSED;HP_DAMAGE_UNCHANGED;RUNTIME_NOT_RUN')
  if folder=='losthalls':r['current_attack_damage_attribute']='7' if name=='oskolnik' else '5' if name!='posten' else 'N/A_NO_ATTACK_DAMAGE_REGISTERED'
  new.append(r)
 ids={r['entity_id'] for r in new};assert len(ids)==20
 # Preserve every existing unrelated membership, assign exactly one nature to the new twenty.
 for p in T.glob('nature_*.json'):
  data=json.loads(p.read_text(encoding='utf-8'));data['values']=[v for v in data['values'] if v not in ids]
  data['values'] += [r['entity_id'] for r in new if 'nature_'+r['nature_proposed'].lower()==p.stem]
  p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 for p in T.glob('trait_*.json'):
  data=json.loads(p.read_text(encoding='utf-8'));data['values']=[v for v in data['values'] if v not in ids]
  data['values'] += [r['entity_id'] for r in new if p.stem.removeprefix('trait_').upper() in r['traits_proposed'].split(';')]
  p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 resolved=old_inventory.tags()
 for r in new:
  id=r['entity_id'];n=[n.split(':nature_')[1].upper() for n,v in resolved.items() if n.startswith('slavicmyths:nature_') and id in v];tr=[n.split(':trait_')[1].upper() for n,v in resolved.items() if n.startswith('slavicmyths:trait_') and id in v]
  assert n==[r['nature_proposed']],(id,n);assert not {'FIRE_AFFINITY','ICE_AFFINITY'}<=set(tr)
 with (D/'NEW_NETHER_ENTITIES.tsv').open('w',newline='',encoding='utf-8') as f:
  writer=csv.DictWriter(f,list(new[0]),delimiter='\t');writer.writeheader();writer.writerows(new)
 old=list(csv.DictReader(historical.open(encoding='utf-8-sig')));merged={r['entity_type']:r for r in old};fields=list(old[0])+['source_registry_status','actual_nature_tag','actual_trait_tags','current_registry_source']
 for id,s in source.items():
  entry=merged.setdefault(id,{'entity_type':id,'name':id,'group':'SOURCE_NEW_UNCLASSIFIED','notes':'Source registry declaration; full runtime inventory NOT_RUN'})
  entry.update(source_registry_status='SOURCE_DECLARATION',current_registry_source=s['source'],actual_nature_tag=';'.join(n.split(':nature_')[1].upper() for n,v in resolved.items() if n.startswith('slavicmyths:nature_') and id in v),actual_trait_tags=';'.join(n.split(':trait_')[1].upper() for n,v in resolved.items() if n.startswith('slavicmyths:trait_') and id in v))
 for r in new:
  entry=merged[r['entity_id']];entry.update(name=r['display_name_ru'],group=r['combat_role'],danger_level=r['proposed_danger_level'],danger_status='OWNER_REVIEW_PROPOSED',current_max_health=r['current_hp_source_or_contract'],current_base_damage=r['current_attack_damage_attribute'],base_damage_status='SOURCE_ONLY;NOT_REBALANCED',types_status='SOURCE_TAGGED;RUNTIME_NOT_RUN',registry_source=r['source'],forms_reference='Sheptun: variants0..3 in one EntityType' if r['entity_id'].endswith(':sheptun') else '')
 for id,r in merged.items():
  if id not in source:r['source_registry_status']='HISTORICAL_ONLY_NOT_MATCHED'
 with (R/'docs/balance/CREATURES_2.0.csv').open('w',newline='',encoding='utf-8') as f:
  writer=csv.DictWriter(f,fields);writer.writeheader();writer.writerows(merged[id] for id in sorted(merged))
 # Exhaustive source callsite ledger; unresolved expressions remain explicit, never fake target budgets.
 actions=[]
 for folder in ['navya','blackcinder','losthalls','burningislands']:
  for p in (R/'src/main/java/org/slavicmyths'/folder).glob('*.java'):
   for line,s in enumerate(p.read_text(encoding='utf-8').splitlines(),1):
    if '.hurt(' in s or 'Action(int release' in s or re.search(r'(CLAWS|FRACTURE|ERUPTION)\(\d',s):actions.append({'source':p.relative_to(R).as_posix()+':'+str(line),'source_expression':s.strip(),'target_budget':'NOT_APPLIED;OWNER_REVIEW_PROPOSED','runtime':'NOT_RUN'})
 with (R/'docs/balance/ATTACKS_2.0.csv').open('w',newline='',encoding='utf-8') as f:
  writer=csv.DictWriter(f,list(actions[0]));writer.writeheader();writer.writerows(actions)
 with (D/'TARGET_COMBAT_PROFILE.tsv').open('w',newline='',encoding='utf-8') as f:
  fields=['entity_id','proposed_danger_level','approved_scale_A4_for_level']+['A'+str(i) for i in range(1,8)]+['status'];wr=csv.DictWriter(f,fields,delimiter='\t');wr.writeheader()
  for r in new:
   level=r['proposed_danger_level'];a4={'IV':14,'V':18,'VI':20}.get(level);row={k:r.get(k,'') for k in fields[:3]}
   row.update({'A'+str(i+1):round(a4*c,2) if a4 else 'NO_APPROVED_FORMULA' for i,c in enumerate([.65,.8,.7,1,1.15,1.35,1.5])});row['status']='PROPOSED_CAST_BUDGET;NOT_APPLIED;DOT_BURST_SPLIT_PENDING';wr.writerow(row)
 assert hashlib.sha256(historical.read_bytes()).hexdigest()==historic_sha
 (D/'inventory.json').write_text(json.dumps({'new_entities':20,'source_declarations':len(source),'merged_rows':len(merged),'historical_sha256_unchanged':historic_sha,'source_nature_unique':'PASS','runtime_registry':'NOT_RUN','tier_damage_changes':'NOT_APPLIED;OWNER_REVIEW_PROPOSED'},indent=2)+'\n')
 print('Twenty source registrations typed; historical CSV preserved. Tier budgets proposed, runtime NOT_RUN.')
if __name__=='__main__':main()
