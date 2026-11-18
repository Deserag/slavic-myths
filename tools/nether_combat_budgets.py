"""Source-derived current action budgets. No gameplay balance application or simulated DPS claim."""
from pathlib import Path
import csv,json,re,hashlib,math
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/2026-10-29-hell-remaining';J=R/'src/main/java/org/slavicmyths';B=R/'docs/balance'
def rows(p):return list(csv.DictReader(p.open(encoding='utf-8-sig'),delimiter='\t' if p.suffix=='.tsv' else ','))
def save(p,data):
 with p.open('w',newline='',encoding='utf-8') as f:w=csv.DictWriter(f,list(data[0]));w.writeheader();w.writerows(data)
entities=rows(R/'docs/implementation/nether-creature-balance/NEW_NETHER_ENTITIES.tsv');actions=[]
def add(id,action,raw,attempts,dot,control,range,timing,src,anchor,notes=''):
 p=J/src;text=p.read_text(encoding='utf-8');line=next((i for i,l in enumerate(text.splitlines(),1) if anchor in l),None);assert line,(src,anchor)
 actions.append(dict(entity_id='slavicmyths:'+id,action=action,raw_per_attempt_HP=raw,max_attempts_per_victim=attempts,raw_burst_upper_HP=raw*attempts,dot_upper_HP=dot,control=control,range_blocks=range,timing_ticks=timing,source=p.relative_to(R).as_posix()+':'+str(line),source_sha256=hashlib.sha256(p.read_bytes()).hexdigest(),assumptions=notes or 'Normal difficulty; raw before armor/effects/runes; upper attempts not guaranteed hits; vanilla invulnerability/LOS/geometry apply',status='SOURCE_BUDGET;RUNTIME_NOT_RUN;TARGET_A1_A7_NOT_APPLIED'))
for n,raw in [('navka',6),('rootling',9),('bone_crawler',10),('grove_guardian',14)]:add(n,'MELEE',raw,1,0,'knockback','2.8/3.2/6','12 or26 windup;18 recovery+18/26 idle','navya/NavyaMob.java','case MELEE ->')
add('navka','SHOT',7,1,0,'SlowI50','4..26','20 windup;70 ICD','navya/NavyaShot.java','target.hurt(')
add('bone_crawler','SHOT',9,1,0,'none','4..26','20 windup;110 ICD','navya/NavyaShot.java','target.hurt(')
for n,raw,count in [('rootling',6,1),('grove_guardian',10,3)]:add(n,'ROOTS',raw,count,0,'SlowI45','<16;root boxes','28 release+20 root warning;180 ICD','navya/NavyaRoots.java','target.hurt(', 'Rootling1/guardian3 separate root entities; same-tick overlap usually native immunity-limited;30 is emitted upper, not measured damage')
add('grove_guardian','THROW',18,1,0,'knockback1','7..28','48 windup;220 ICD','navya/NavyaShot.java','target.hurt(')
for n,action,range,time in [('bone_crawler','DASH','4..10','16;160 ICD'),('grove_guardian','SUMMON','local;max2 rootlings','40;500 ICD'),('grove_guardian','PHASE','self','60+20'),('navya_wisp','AMBIENT','none','no autonomous damaging cast')]:add(n,action,0,0,0,'movement/summon/ambient',range,time,'navya/NavyaMob.java','private void impact(')
add('coal_crawler','BITE',9,1,0,'push.5','1.8','22+15;40/24 recovery','blackcinder/CinderMob.java','private void bite()')
add('ash_spirit','PULSE',6,1,0,'push.5','3.5','24+18;40 recovery','blackcinder/CinderMob.java','case PULSE->')
add('ash_spirit','SMOKE',0,0,0,'movement','bounded path','16+18','blackcinder/CinderMob.java','case SMOKE,SHIFT')
add('pekelnik','CLAW',11,1,0,'push.5','3.2 front','26+18','blackcinder/CinderMob.java','case CLAW->')
add('pekelnik','COAL',8,1,0,'heat damage;not Ignite','<=32','26+18;projectile life80','blackcinder/CinderCoal.java','living.hurt(')
add('zharenitsa','RING',12,1,0,'heat;once/cast','2..16.28','36..70;40/24 recovery','blackcinder/CinderMob.java','private void ring()')
add('zharenitsa','SPIKES',10,5,0,'separate boxes','5spikes around aimed radius3','36+5 entityticks','blackcinder/CinderSpike.java','e.hurt(', '50 emitted upper across5 separate hazards; native iframes/placement make this not guaranteed50 per victim')
add('zharenitsa','VORTEX',3,7,0,'pull.06','8','phase2 only;pulses40,50,60,70,80,90,100','blackcinder/CinderMob.java','private void vortex()')
for n,a in [('zharenitsa','SHIFT'),('zharenitsa','PHASE'),('sparkling','AMBIENT')]:add(n,a,0,0,0,'movement/ambient','no damage','bounded cast','blackcinder/CinderMob.java','private void impact()')
for mode,dot,control in [(0,3,'fire60'),(1,3,'PoisonI80'),(2,1,'WitherI60'),(3,0,'indirect magic')]:add('sheptun','SHOT_VARIANT_'+str(mode),5,1,dot,control,'<=16','release16;cast30;recovery50','losthalls/HallsBolt.java','int mode=entityData.get(MODE);', 'POISON floor80/25=3 (cannot kill below1HP); WITHER floor60/40=1; fire<=3x1; no effects when direct hurt fails')
add('oskolnik','SHOT',7,1,0,'none','<=16','16+14;50 recovery','losthalls/HallsBolt.java','int mode=entityData.get(MODE);')
add('oskolnik','DOUBLE_SHOT',7,2,0,'two independent bolts','<=16','even sequence;80 recovery','losthalls/HallsMob.java','else if(kind!=Kind.GRIBOSHMYG', '14 emitted upper; simultaneous equal hits may be iframe-limited to7')
add('gulokryl','DEFEND_SHOT',5,1,3,'fire60','<=16','16+14;65 recovery','losthalls/HallsBolt.java','int mode=entityData.get(MODE);')
add('ekhovik','ECHO',6,1,0,'push.65','8','release16;cast30;50 recovery','losthalls/HallsMob.java','if(kind==Kind.EKHOVIK&&cast()==2)')
add('ekhovik','CHARGE',7,1,0,'safe dry path','collision<3','ticks17..24;struck guard','losthalls/HallsMob.java','if(kind==Kind.EKHOVIK&&cast()==3&&castAge>16')
for n,a in [('ekhovik','IDLE_ECHO_HARVEST'),('griboshmyg','PEACEFUL_SPORES')]:add(n,a,0,0,0,'non-combat','local','200 echo;140 spores','losthalls/HallsMob.java','if(kind==Kind.GRIBOSHMYG&&tickCount%140')
add('posten','SLOW_MASK',0,0,0,'SlowI50','10 LOS','release+20;50 recovery','losthalls/PostenMob.java','if(mask()==0)e.addEffect')
add('posten','FIRE_MASK',5,3,3,'fire60','<24 target gate','one three-bolt volley','losthalls/PostenMob.java','if(mask()==1){', '15 emitted direct upper; fire timer refresh does not independently stack three60t burns')
add('posten','POISON_MASK',0,0,4,'PoisonI40 refreshed5t over40t','aim radius3','field40+tail<=40','losthalls/PostenMob.java','if(poisonUntil>', 'Conservative4 HP DoT ceiling over80t; refresh/phase/biological immunity can reduce it, not measured DPS')
add('posten','WITHER_MASK',6,1,1,'WitherI40','10 frontal dot>.8','release+20;50 recovery','losthalls/PostenMob.java','if(mask()==3&&e.position()')
add('posten','TRANSIT_COPIES',0,0,0,'shared encounter/controller;copies use same masks','safe loaded arena','no direct damage','losthalls/PostenMob.java','private void reconcile(')
for n,raw in [('heat_salamander',6),('slag_crab',7),('vent_golem',12)]:add(n,'CLAWS',raw,1,0,'neutral until provoked','body widths+1','23 release40 total+30 recovery','burningislands/IslandsMob.java','float raw=boss()?')
for a,raw in [('CLAWS',14),('FRACTURE',18),('HANDS',16),('ERUPTION',18),('RING',20)]:add('magma_lord',a,raw,1,0,'one hit UUID/cast','5/22/adaptive128/three zones/ring','MagmaCombat.Action shared timeline+40 recovery','burningislands/MagmaCombat.java',a+'(', 'ERUPTION3 pulses and RINGmany samples share struck UUID; upper one raw hit; no hidden burn')
for a in ['PULL','WARN','WAKE','PHASE']:add('magma_lord',a,0,0,0,'movement/warning;no direct damage','loaded safe route','shared timeline','burningislands/MagmaCombat.java',a+'(')
assert {x['entity_id'] for x in entities}=={x['entity_id'] for x in actions}
save(B/'NETHER_CURRENT_ACTION_BUDGETS.csv',actions)
# An exhaustive current callsite ledger catches newly added attacks for manual budget review.
ledger=[]
for folder in ['navya','blackcinder','losthalls','burningislands']:
 for p in (J/folder).glob('*.java'):
  for i,line in enumerate(p.read_text(encoding='utf-8').splitlines(),1):
   if '.hurt(' in line or 'MobEffects.' in line or 'igniteForSeconds' in line or 'setRemainingFireTicks' in line:
    ledger.append(dict(source=p.relative_to(R).as_posix()+':'+str(i),expression=line.strip(),classification='CURRENT_SOURCE_CALLSITE; combat family/action budgets in NETHER_CURRENT_ACTION_BUDGETS.csv; player/rune/environment callsites retained separately',runtime='NOT_RUN'))
save(O/'nether-all-damage-effect-callsites.csv',ledger)
# Update living master only; historical audit remains byte-for-byte unchanged.
master=rows(B/'CREATURES_2.0.csv');by={e['entity_id']:e for e in entities}
for row in master:
 n=row['entity_type'];e=by.get(n);aa=[a for a in actions if a['entity_id']==n]
 row.update(current_action_budget_source='NETHER_CURRENT_ACTION_BUDGETS.csv#'+n if aa else 'historical ATTACKS.csv; no new budget applied',current_max_emitted_burst_HP=str(max(a['raw_burst_upper_HP'] for a in aa)) if aa else 'HISTORICAL_NOT_REMEASURED',current_max_dot_upper_HP=str(max(a['dot_upper_HP'] for a in aa)) if aa else 'HISTORICAL_NOT_REMEASURED',action_budget_status='SOURCE_BUDGET;RUNTIME_NOT_RUN' if aa else 'HISTORICAL_NOT_REMEASURED')
 row.update(current_follow_range='HISTORICAL_NOT_REMEASURED',natural_spawn_source='HISTORICAL_NOT_REMEASURED',loot_and_looting_source='HISTORICAL_NOT_REMEASURED',current_model_source='HISTORICAL_NOT_REMEASURED',immunity_source='CreatureProperties + native entity flags; runtime not measured',last_checked='2026-10-29',owner_balance_decision='PROPOSED levels/A1-A7; current production unchanged')
 if e:
  row['current_base_damage']=e['current_attack_damage_attribute'];row['current_max_health']=e['current_hp_source_or_contract'];name=n.split(':')[1];folder=e['source'].split('/')[-2]
  row['current_follow_range']='24' if folder=='losthalls' and name!='posten' else '32'
  biome={'navya':'navya_grove','blackcinder':'black_cinder','losthalls':'lost_halls','burningislands':'burning_islands'}[folder]
  row['natural_spawn_source']='src/main/resources/data/slavicmyths/worldgen/biome/'+biome+'.json; '+e['source']
  if name in ['grove_guardian','zharenitsa']:row['natural_spawn_source']+='; '+folder+' arena structure/terrain validator'
  if name=='posten':row['natural_spawn_source']='losthalls/HallsWorldgen.java + PostenMob.java canonical natural journal; manual mode has no rewards'
  if name=='magma_lord':row['natural_spawn_source']='burningislands/IslandsWorldgen.java validated loaded arena + MagmaEncounters journal'
  loot=R/'src/main/resources/data/slavicmyths/loot_table/entities'/f'{name}.json'
  row['loot_and_looting_source']=loot.relative_to(R).as_posix() if loot.is_file() else 'NO_ENTITY_LOOT_TABLE; inspect '+e['source']
  if name in ['posten','magma_lord']:row['loot_and_looting_source']='losthalls/BossRewardInbox.java + BossRewardAuthority.java; fixed receipt/batch no Looting multiplication'
  if name in ['sparkling','navya_wisp']:row['loot_and_looting_source']='Ambient no kill reward; family shouldDropLoot/XP guards'
  model_folder={'navya':'navya_rigs','blackcinder':'cinder_rigs','losthalls':'lost_halls_rigs','burningislands':'burning_islands_rigs'}[folder]
  row['current_model_source']='src/main/resources/assets/slavicmyths/'+model_folder+'/'+name+'.json; physical reference ART_BLOCKED_REFERENCE'
  row['immunity_source']='nature_'+e['actual_nature_tag'].lower()+'.json; traits '+e['actual_trait_tags']+'; CreatureProperties.java + native flags in '+e['source']

save(B/'CREATURES_2.0.csv',master)
report={'twenty_entities':len(entities),'current_actions':len(actions),'damage_effect_callsites':len(ledger),'master_rows':len(master),'status':'SOURCE_BUDGET_COMPLETE;NO_HP_DAMAGE_REBALANCE','runtime':'NOT_RUN','historical_sha256':hashlib.sha256((R/'docs/audit/creatures-1.9.8.1/CREATURES.csv').read_bytes()).hexdigest()};(O/'combat-budgets.json').write_text(json.dumps(report,indent=2)+'\n');print(report)
