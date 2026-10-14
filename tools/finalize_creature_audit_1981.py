"""Finalize existing CSVs only. No indexing, gameplay, build or installation."""
from pathlib import Path
import csv, json, re
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs/audit/creatures-1.9.8.1'
def read(name):
    with (OUT/name).open(encoding='utf-8-sig',newline='') as f:return list(csv.DictReader(f))
def write(name,rows):
    fields=list(dict.fromkeys(k for row in rows for k in row))
    temporary=OUT/(name+'.tmp')
    with temporary.open('w',encoding='utf-8-sig',newline='') as f:
        w=csv.DictWriter(f,fieldnames=fields);w.writeheader();w.writerows(rows)
    temporary.replace(OUT/name)
C=read('CREATURES.csv'); A=read('ATTACKS.csv')
if 'danger_status' in C[0]:
    raise SystemExit('Tables already finalized. Edit reviewed rows explicitly; do not reinterpret prior proposals as approved.')
levels={}
for level,ids in {
 '0':'bear_cub doe pike carp crayfish domovoy kuzya baba_yaga sivka_burka kalin_war_horse',
 'I':'igosha stag', 'II':'forest_wolf boar nav bandit_fighter bandit_archer rusalka',
 'III':'brown_bear bandit_heavy bandit_senior kikimora polevik bolotnik vodyanoy upyr nochnitsa podzemnik kalin_common_soldier pesigolovets',
 'IV':'leshy poludnitsa bannik ataman kurgan_druzhinnik kalin_elite_soldier',
 'V':'ovinnik volkolak fire_serpent podvey kurgan_voevoda buried_volkhv kladovik elder_vodyanoy finist alyosha_popovich',
 'VI':'nightingale cursed_volkolak grey_wolf large_podzemnik unresting_prince tugarin_zmey likho_one_eyed dobrynya_nikitich ilya_muromets',
 'VII':'extinguished_flame_guardian defiled_spring_keeper morozko karachun earth_spirit idolishche_poganoe marinka skim_beast kartaus',
 'VIII':'zmey_gorynych great_poloz kalin_tsar kot_bayun firebird',
}.items():
    levels.update({x:level for x in ids.split()})
bases={'0':0,'I':4,'II':6,'III':10,'IV':14,'V':18,'VI':20,'VII':24,'VIII':26}
hp={'ataman':100,'tugarin_zmey':400,'nightingale':350,'likho_one_eyed':350,'cursed_volkolak':700,'extinguished_flame_guardian':900,'defiled_spring_keeper':800,'zmey_gorynych':2800,'firebird':3000}
personal={'grey_wolf':13,'cursed_volkolak':15,'firebird':20,'kot_bayun':26}
by={r['entity_type']:r for r in C}
for r in C:
    id=r['entity_type'].split(':')[1]
    r.update(danger_status='N/A auxiliary',target_hp_status='UNVERIFIED auxiliary',base_damage_status='UNVERIFIED auxiliary',current_hp_status='SOURCE_VERIFIED' if 'UNVERIFIED' not in r['current_max_health'] else 'UNVERIFIED',current_damage_status='SOURCE_EXPRESSION; equipment/events/runtime not measured',forms_reference='FORMS.md')
    if r['group']=='auxiliary':continue
    assert id in levels,id
    old=r['danger_level'];r['danger_level']=levels[id]
    r['danger_status']='VERIFIED: prior agreed scale' if old in {'VII','VIII'} else 'PROPOSED: analytical progression assignment'
    if id in hp:
        r['target_hp']=hp[id];r['target_hp_status']='PROPOSED: explicit user confirmation required' if id=='cursed_volkolak' else 'VERIFIED: user approved target'
    elif 'UNVERIFIED' in r['target_hp']:
        s=r['current_max_health']
        # Preserve existing form-specific maxima instead of flattening variants.
        r['target_hp']={'karachun':'1200 default; config retained','great_poloz':3000,'kalin_tsar':1800}.get(id,s)
        r['target_hp_status']='PROPOSED: retain current HP and variant maxima'
    else:r['target_hp_status']='VERIFIED: retained previous target'
    r['proposed_base_damage']=personal.get(id,bases[levels[id]])
    r['base_damage_status']='PROPOSED: preserve personal base' if id in personal else 'PROPOSED: level base; IV..VIII coefficients agreed, individual assignment pending'
    if id=='kot_bayun':r['base_damage_status']='PROPOSED: nominal base26; narrative lethal formula is separate, do not replace automatically'
    types=[t for t in r['types'].split(';') if t and t!='UNVERIFIED']
    if not types:types=['мифическое существо']
    types+=({'NPC':['сюжетный','нейтральный'],'ally':['союзник','сюжетный'],'boss':['босс','сюжетный'],'mini_boss':['элитный','боевой'],'ordinary':['фауна' if 'животное' in types else 'локальный противник']}[r['group']])
    r['types']=';'.join(dict.fromkeys(types));r['types_status']='PROPOSED semantic classification; not runtime tags'
    if id=='likho_one_eyed':r['notes']+='; normal HP350 VERIFIED; mor HP700 VERIFIED same EntityType; see FORMS'
    if id=='firebird':r['notes']+='; REQUIRED narrative immortality; DEFECT: hurt->super.hurt and die->super.die'
    if id=='zmey_gorynych':r['notes']+='; VERIFIED logical target2800, transform1500, floor75; not proportional thresholds'
    if id in {'pike','carp','crayfish','baba_yaga','kuzya'}:r['current_base_damage']='N/A: no autonomous damaging action in existing audit; factory omission is not numerical damage0'
    attacks=[a for a in A if a['entity_type']==r['entity_type']]
    r['combat_inventory_status']='KNOWN_ACTIONS_PROCESSED; call-chain/runtime limitations in UNVERIFIED' if attacks else 'NO_KNOWN_AUTONOMOUS_ATTACK; attribute alone not proof; passenger damage belongs to commander'

coeff={'A1':.65,'A2':.8,'A3':.7,'A4':1.,'A5':1.15,'A6':1.35,'A7':1.5}
for a in A:
    c=by[a['entity_type']];id=a['entity_type'].split(':')[1];name=a['attack'];text=(name+' '+a['effects_and_duration_ticks']).lower()
    base=c['proposed_base_damage']
    if base=='UNVERIFIED':base=6 # concrete auxiliary proposal, never an approved creature base
    a['target_base_damage']=base
    cat=a['proposed_category']
    a['target_direct_damage']=round(float(base)*coeff[cat],2) if cat in coeff else 0
    a['target_status']='PROPOSED: category/base/budget; no production change'
    a['action_category']='DAMAGE' if cat in coeff else 'HEALING' if name=='HEAL' else 'SUMMON' if any(t in text for t in ['summon','clone','herd','shadow','decoy','howl_summon']) else 'CONTROL' if any(t in text for t in ['pull','slowness','weakness','fear','hunger','capture','curse','snare','pressure','blizzard','storm','flood']) else 'NO_DIRECT_DAMAGE'
    periodic=cat=='A1' and any(t in (a['hits']+' '+text).lower() for t in ['every','cloud','trail','ticks','aftermath','ground','ember rune'])
    if periodic:a['action_category']='PERIODIC_DAMAGE'
    a['secondary_categories']='CONTROL/EFFECT' if any(t in text for t in ['slowness','weakness','blindness','pull','fear','capture','hold','knockback']) else 'NONE'
    if any(t in text for t in ['bleed','wither','fire','poison','cold','waning']):a['secondary_categories']+='; EFFECT_DAMAGE_OR_ACCUMULATION: separate from direct budget'
    hits=a['hits'];multi=hits!='1' and not hits.startswith('1 per UUID') and not hits.startswith('1 UUID')
    a['damage_budget']='PROPOSED whole action/window per victim; components share parent budget, never sum component targets' if multi or periodic or any(t in name for t in ['cloud','shot','landing','aftermath','throw','grab ticks']) else 'PROPOSED one accepted hit per victim'
    a['target_unit']='whole action/window per victim' if 'whole' in a['damage_budget'] else 'one accepted hit per victim'
    a['multihit_risk']='YES: i-frames/UUID gates/components must be checked; split total target among actual hits' if multi or periodic else 'NO_KNOWN_MULTIHIT in this row; event modifiers remain'
    a['formula_status']='CUSTOM_RAW_NET_OR_EQUIPMENT: preserve formula pipeline; target is proposed calibration budget' if any(t in a['current_direct_damage'].lower() for t in ['raw','diamond','snapshot','enchant','equipment','velocity','attack_damage','maxhp','budget','phase','exposure']) else 'CURRENT_SOURCE_LITERAL; net not measured'
    a['reachability']='STATIC_CANDIDATE: cited handler/context; full producer chain not certified, runtime NOT_RUN'
    a['reachability_evidence']=a['source']
    if id=='tugarin_zmey':
        a['reachability']='VERIFIED_STATIC_SELECTABLE; runtime NOT_RUN'
        a['reachability_evidence']='src/main/java/org/slavicmyths/hunt/WorldBoss.java:99; src/main/java/org/slavicmyths/hunt/TugarinCombat.java:137'
    if id=='hot_stone':a['reachability']='UNVERIFIED_PRODUCER: not selected by Bannik scheduler'
    if 'vanilla' in name.lower() or 'equipment' in a['current_direct_damage']:
        a['formula_status']+='; INHERITED Mob.doHurtTarget/native projectile + equipment/events'
    if id=='player_ice_double':a['notes']+='; proposed auxiliary base6 is nominal only; actual mirror remains player snapshot formula'
    if id=='kot_bayun':a['notes']+='; target39 is nominal A7 only; narrative lethal response remains separate blocking design exception'
    a['status']='CURRENT_SOURCE_EVIDENCE; TARGET_PROPOSED; reachability explicitly separate'

def ref(file,token):
    p='src/main/java/org/slavicmyths/'+file+'.java'
    lines=(ROOT/p).read_text(encoding='utf-8').splitlines()
    return p+':'+str(next(i for i,s in enumerate(lines,1) if token in s))
for id,name,file,token,reason in [
 ('tugarin_zmey','legacy WorldBoss attack scheduler','hunt/WorldBoss','public void tick(){if(kind()==BossKind.TUGARIN)','early return to TugarinCombat: legacy SLAM/SPIN/BREATH/CHARGE/STONE/PUNCH not live alternatives'),
 ('tugarin_zmey','STONE unselected','hunt/TugarinCombat','case STONE ->','choose never returns STONE; handler alone is insufficient'),
 ('igosha','late think Weakness/Blindness','entity/IgoshaEntity','if(getTarget()!=null &&','early return when target exists'),
]:
    if any(a['attack']==name for a in A):continue
    r=dict(A[0]);r.update(entity_type='slavicmyths:'+id,form='normal',attack=name,handler=reason,source=ref(file,token),current_direct_damage='N/A unreachable branch',range_blocks='N/A',windup_ticks='N/A',cooldown_ticks='N/A',effects_and_duration_ticks=reason,hits='0',proposed_category='N/A',coefficient='N/A',target_base_damage=0,target_direct_damage=0,target_unit='N/A excluded',status='VERIFIED_STATIC_UNREACHABLE',notes=reason,target_status='N/A excluded',action_category='UNREACHABLE',secondary_categories='NONE',damage_budget='N/A excluded',multihit_risk='N/A',formula_status='N/A',reachability='VERIFIED_STATIC_UNREACHABLE',reachability_evidence=ref(file,token));A.append(r)
write('CREATURES.csv',C);write('ATTACKS.csv',A)
pending=[]
def pending_row(table,entity,attack,field,value,kind,blocking):
    pending.append(dict(table=table,entity_type=entity,attack=attack,field=field,value=value,classification=kind,blocks_audit='NO',blocks_implementation=blocking))
for table,data in [('CREATURES',C),('ATTACKS',A)]:
    for r in data:
        for field,value in r.items():
            if field in {'status','notes','resistances_immunities','spawn_conditions'}:continue
            if 'UNVERIFIED' in str(value) or 'STATIC_CANDIDATE' in str(value):pending_row(table,r['entity_type'],r.get('attack',''),field,value,'IMPLEMENTATION_TEST_OR_SOURCE_CHECK','NO')
for key,value in {
 'B1':'Confirm cursed_volkolak target700 (PROPOSED); do not implement before user confirmation',
 'B2':'Approve all PROPOSED levels/HP/bases/categories, lower I..III bases4/6/10; nominal Bayun39 must not silently replace lethal narrative formula',
 'B3':'Approve save migration: proposed proportional HP with idempotent schema, preserve completed phases; Gorynych target thresholds1500/75 are absolute',
 'B4':'Approve silver eligible types and one compound rule: proposed one maximum bonus per hit, preserve current +1.5 until approved',
}.items():pending_row('DECISION','', '',key,value,'USER_DESIGN_DECISION','YES')
write('UNVERIFIED.csv',pending)
summary={'status':'COMPLETE_WITH_LIMITATIONS','entity_types':len(C),'creatures':sum(r['group']!='auxiliary' for r in C),'auxiliary':sum(r['group']=='auxiliary' for r in C),'attack_rows':len(A),'processed_known_action_rows':len(A),'blocking_questions':4,'runtime_runs':0,'scope':'existing inventory; not proof of exhaustive runtime reachability'}
(OUT/'completion-summary.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps(summary))
