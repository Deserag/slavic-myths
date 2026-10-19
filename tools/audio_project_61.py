"""Join the complete registry, state routes, event definitions, provenance and JAR.

This is static evidence, never a runtime/hearing assertion. Unresolved dynamic
dispatch remains explicit rather than being silently converted into COVERED.
"""
from pathlib import Path
import csv,json,re,hashlib,subprocess,zipfile,collections
R=Path(__file__).resolve().parents[1];O=R/'docs/audio-review';A=R/'src/main/resources/assets/slavicmyths'
def readcsv(name):return list(csv.DictReader((O/name).open(encoding='utf-8-sig')))
def write(name,rows):
 fields=list(dict.fromkeys(k for row in rows for k in row))
 with (O/name).open('w',encoding='utf-8-sig',newline='') as f:
  w=csv.DictWriter(f,fieldnames=fields);w.writeheader();w.writerows(rows)

# Explicit shared-kind selectors verified against constructor/dispatch source.
ALIASES={'brown_bear':['bear'], 'bear_cub':['cub'], 'forest_wolf':['wolf'],
 'baba_yaga':['yaga'], 'kot_bayun':['bayun'], 'elder_vodyanoy':['elder'],
 'tugarin_zmey':['tugarin'], 'likho_one_eyed':['likho'], 'skim_beast':['skim'],
 'ilya_muromets':['ilya_muromets'], 'alyosha_popovich':['alyosha'], 'dobrynya_nikitich':['dobrynya'],
 'bandit_fighter':['bandit'],'bandit_archer':['bandit'],'bandit_heavy':['bandit'],'bandit_senior':['bandit'],'ataman':['bandit'],
 'pesigolovets':['doghead'],'great_poloz':['poloz'], 'sivka_burka':['sivka'], 'zmey_gorynych':['gorynych'],
 'extinguished_flame_guardian':['guardians.fire','guardians.amulet'], 'defiled_spring_keeper':['guardians.spring','guardians.amulet'],
 'kladovik':['underground.kladovik'],'large_podzemnik':['underground.large_podzemnik'],'podzemnik':['underground.podzemnik'],
 'finist':['entity.finist','entity.falcon'],'kartaus':['entity.kartaus'],
 'kalin_tsar':['kalin'],'kalin_common_soldier':['kalin'],'kalin_elite_soldier':['kalin'],'kalin_war_horse':['kalin'],
 'karachun_raven':['karachun.raven'], 'karachun_blade':['karachun.black_harvest'],
 'morozko_ice_bolt':['morozko.spike_break'], 'morozko_icicle':['morozko.staff_icicles','morozko.spike_break','morozko.wave'],
 'morozko_ice_double':['morozko.clone','morozko.staff_hit','morozko.ice_bolt'], 'player_ice_double':['morozko.mirror_break'],
 'grey_wolf_support':['quest185','wolf_story'], 'earth_servant':['earth_spirit'],
 'guardian_water_echo':['guardians.spring'], 'guardian_water_prison':['guardians.spring_prison'],
 'marinka_tur_minion':['marinka'], 'marinka_bolt':['marinka'], 'marinka_rune':['marinka'],
 'kartaus_banner':['entity.kartaus.banner','entity.finist.banner'], 'kartaus_curse_anchor':['entity.kartaus'],
 'gorynych_breath_cloud':['gorynych'],'gorynych_breath_shot':['gorynych'],
 'poloz_glob':['poloz'],'poloz_stone_spike':['poloz'],
 'mor_bolt':['mor'],'mor_zone':['mor'],'mor_flask_projectile':['mor'],
 'fire_serpent':['fire_serpent','mor'],'podvey':['podvey'], 'volkolak':['volkolak'], 'cursed_volkolak':['volkolak'],
 'bolotnik':['swamp','bolotnik'], 'nochnitsa':['nochnitsa'],
}
NO_VOICE={'carcass':'Inert harvested carcass; textile processing sound belongs to tool interaction.',
 'furniture_seat':'Invisible seating carrier; furniture interaction owns any cue.',
 'rune_visual':'Client visual marker; no body/voice or own audible action.',
 'earth_encounter_actor':'Encounter visual/controller; EarthSpirit owns encounter cues.',
 'marinka_hex_focus':'Spell anchor; Marinka owns cast telegraph, no autonomous voice.',
 'flying_broom':'Vehicle, no voice; movement/interaction foley reviewed separately.',
 'flying_mortar':'Vehicle, no voice; movement/interaction foley reviewed separately.'}

def main():
 base=subprocess.check_output(['git','rev-parse','--short','HEAD'],cwd=R,text=True).strip()
 entities=readcsv('ENTITY_COVERAGE.csv');events=json.loads((A/'sounds.json').read_text(encoding='utf-8'))
 assets={r['path']:r for r in readcsv('ASSETS.csv')};licenses={r['asset']:r for r in readcsv('SOURCE_LICENSES.csv')}
 java={p.relative_to(R).as_posix():p.read_text(encoding='utf-8') for p in (R/'src/main/java').rglob('*.java')}
 classes={Path(p).stem:(p,s) for p,s in java.items()};constant={}
 for text in java.values():
  for c,e in re.findall(r'\b([A-Z][A-Z_0-9]+)\s*=\s*(?:sound|register)\("([a-z0-9_.]+)"',text):constant[c]=e
 selected={}
 for path in [R/'art/audio/pack-a-60/voice-rework/batch-selection.json',R/'art/audio/pack-a-60/frost-foley/selection.json']:
  for row in json.loads(path.read_text(encoding='utf-8')):selected[row['event']]=row
 for event in ['tugarin_phase1_aggro','tugarin_phase1_idle','tugarin_phase1_attack','skim.roar']:selected[event]={'status':'OWNER_TIMBRE_APPROVED_GAME_PENDING'}
 for event in ['tugarin_spin_prepare','skim.rage',*[f'tugarin_spin_blade_{n}' for n in range(1,4)]]:selected[event]={'status':'AGENT_SELECTED_OWNER_PENDING'}
 if (R/'art/audio/kurgan-65/selection.json').exists():
  for row in json.loads((R/'art/audio/kurgan-65/selection.json').read_text(encoding='utf-8')):selected[row['event']]=row
 if (R/'art/audio/project-61/selection.json').exists():
  for row in json.loads((R/'art/audio/project-61/selection.json').read_text(encoding='utf-8')):selected[row['event']]=row
 actions=[];coverage=[];forms=[]
 for entity in entities:
  ident=entity['id'].split(':')[1];impl=entity['implementation'].split('.')[-1];chain=[];seen=set()
  while impl in classes and impl not in seen:
   seen.add(impl);path,text=classes[impl];chain.append(path)
   match=re.search(r'\bclass\s+\w+(?:<[^>]*>)?\s+extends\s+([\w.]+)',text);impl=match[1].split('.')[-1] if match else ''
  assert chain,entity
  package=str(Path(chain[0]).parent);associated={p for p in java if str(Path(p).parent)==package and not p.endswith(('Content.java','Sounds.java'))}
  # Only actual inherited class paths are direct entity evidence. Package helpers
  # are retained separately because their target/skill guards require inspection.
  prefixes=ALIASES.get(ident,[ident]);owned={e for e in events if any(e==q or e.startswith(q+'_') or e.startswith(q+'.') or ('.' in q and e.startswith(q)) for q in prefixes)}
  if ident=='forest_wolf':owned={e for e in owned if not e.startswith('wolf_story.')}
  related={};direct_vanilla=[]
  for path in sorted(set(chain)|associated):
   text=java[path]
   units=[(m[0],text.count('\n',0,m.start())+1) for m in re.finditer(r'[^;]+(?:;|$)',text)]
   units.extend((line,n) for n,line in enumerate(text.splitlines(),1))
   for line,number in units:
    if not re.search(r'playSound\(|\bcue\(|\bvoice\(|\bsound\(|getAmbientSound\(|getHurtSound\(|getDeathSound\(|SoundInstance|ModSounds\.|String cue\s*=|new Sound\(',line) and not path.endswith('KuzyaAssets.java'):continue
    literals=re.findall(r'"([a-z0-9_.]+)"',line)
    found={e for e in owned if e in literals}
    found|={constant[c] for c in re.findall(r'ModSounds\.([A-Z_0-9]+)',line) if constant.get(c) in owned}
    for value in literals:
     found|={e for e in owned if any(e==prefix+'_'+value or e==prefix+'.'+value for prefix in prefixes)}
     found|={e for e in owned if '.' in e and e==e.split('.')[0]+'.'+value}
     if value.endswith('_') and ('nextInt' in line or '?' in line):found|={e for e in owned if re.fullmatch(re.escape(value)+r'\d+',e)}
    for event in found:related.setdefault(event,[]).append((path,number,line.strip(),'ENTITY_OR_INHERITED_DISPATCH' if path in chain else 'DOMAIN_HELPER_REQUIRES_GUARD_REVIEW'))
    if path in chain:
     for value in re.findall(r'SoundEvents\.([A-Z_0-9]+)',line):direct_vanilla.append((path,number,value,line.strip()))
   if path in chain:
    for match in re.finditer(r'\benum\s+(\w+)\s*\{([^}]+)',text):
     names=re.findall(r'\b([A-Z][A-Z_0-9]+)\s*(?:\(|,|;|$)',match[2])
     forms.append(dict(registry_id=entity['id'],enum=match[1],states=';'.join(dict.fromkeys(names)),source=f'{path}:{text.count(chr(10),0,match.start())+1}',interpretation='SOURCE_STATE_ENUM_NOT_ALL_STATES_AUDIBLE',runtime='NOT_RUN'))
  ownrows=[]
  for event in sorted(owned):
   definition=events[event];sites=related.get(event,[]);items=definition.get('sounds',[]);source_names=[s if isinstance(s,str) else s['name'] for s in items]
   sample=[];license_names=[];jar_results=[]
   for name in source_names:
    key=name.removeprefix('slavicmyths:');path='src/main/resources/assets/slavicmyths/sounds/'+key+'.ogg'
    if name.startswith('minecraft:'):license_names.append('VANILLA_REFERENCE_NOT_REDISTRIBUTED');continue
    metric=assets.get(path,{});sample.append(metric);license_names.append(licenses.get(path,{}).get('license','UNKNOWN'));jar_results.append(metric.get('jar_parity')=='True')
   muted=not items;bad_alias=any(any(t in name for t in ['ravager.roar','player.attack.sweep','ender_dragon.growl']) for name in source_names)
   decision='BLOCKED_NEEDS_AUDIO_SOURCE' if muted else 'AGENT_SELECTED_OWNER_PENDING' if event in selected else 'REPLACE_P0_SHARED_SIGNATURE' if bad_alias else 'KEEP_PENDING_HEARING'
   legacy_vocal=any('PROJECT_OUTPUT_RIGHTS_DECLARATION' in x for x in license_names) and bool(re.search(r'ambient|idle|breath|hurt|death|roar|growl|grunt|taunt|shout|voice|song|laugh|mew|purr',event))
   if legacy_vocal and not muted and event not in selected:decision='REVIEW_P0_LEGACY_PROCEDURAL_VOCAL_ROLE'
   if ident=='kuzya':decision='KEEP_PROTECTED_OWNER_DIRECTION'
   priority='P0' if muted or bad_alias or legacy_vocal else 'P1' if 'UNKNOWN' in license_names else 'P2'
   # Prefix ownership alone must never assert a reachable callsite.
   row=dict(registry_id=entity['id'],mob=entity['name'],group=entity['group'],event=event,action=event[len(next((q for q in prefixes if event.startswith(q)),'')):].lstrip('._'),phase_or_form='See FORMS.csv / source guard',callsite=';'.join(f'{p}:{n}' for p,n,_,_ in sites),source_guard=' | '.join(s for _,_,s,_ in sites),dispatch=';'.join(sorted({c for *_,c in sites})) or 'UNRESOLVED_DYNAMIC_ROUTE',active=not muted,definition=json.dumps(definition,ensure_ascii=False),actual_sources=';'.join(source_names),jar_parity=all(jar_results) if jar_results else 'VANILLA_REFERENCE' if items else 'MUTED',license=';'.join(sorted(set(license_names))) or 'NONE_ACTIVE',duration_s=';'.join(m.get('seconds','') for m in sample),peak_dbfs=';'.join(m.get('decoded_peak_dbfs','') for m in sample),priority=priority,decision=decision,owner_hearing='NOT_RUN',approval=selected.get(event,{}).get('status','OWNER_PENDING'),source_base=base)
   ownrows.append(row)
  for path,n,event,line in direct_vanilla:
   ownrows.append(dict(registry_id=entity['id'],mob=entity['name'],group=entity['group'],event='SoundEvents.'+event,action='DIRECT_VANILLA_CALL',callsite=f'{path}:{n}',source_guard=line,dispatch='ENTITY_OR_INHERITED_DIRECT_CALL',active=True,actual_sources='minecraft built-in constant '+event,jar_parity='VANILLA_REFERENCE',license='VANILLA_REFERENCE_NOT_REDISTRIBUTED',priority='P0' if event in ['RAVAGER_ROAR','PLAYER_ATTACK_SWEEP'] else 'P2',decision='REVIEW_DIRECT_ALIAS' if event in ['RAVAGER_ROAR','PLAYER_ATTACK_SWEEP'] else 'KEEP_SHARED_FOLEY_OR_CALLBACK_REVIEW',owner_hearing='NOT_RUN',approval='OWNER_PENDING'))
  if not ownrows:
   ownrows.append(dict(registry_id=entity['id'],mob=entity['name'],group=entity['group'],event='',dispatch='NO_EXPLICIT_MOD_AUDIO_ROUTE',priority='P2' if entity['group']=='auxiliary' else 'P1',decision='NO_VOICE_REQUIRED' if ident in NO_VOICE else 'MISSING_AUDIO_ROUTE_REVIEW',source_guard=NO_VOICE.get(ident,'Review inherited vanilla behaviour / owner spell source; no exemption inferred'),owner_hearing='NOT_RUN',approval='OWNER_PENDING'))
  actions.extend(ownrows)
  blockers=[r for r in ownrows if r.get('decision')=='BLOCKED_NEEDS_AUDIO_SOURCE'];missing=[r for r in ownrows if r.get('dispatch') in ['UNRESOLVED_DYNAMIC_ROUTE','NO_EXPLICIT_MOD_AUDIO_ROUTE']]
  coverage.append(dict(registry_id=entity['id'],mob=entity['name'],group=entity['group'],implementation=entity['implementation'],registry=entity['registry'],source_chain=';'.join(chain),event_rows=len(ownrows),muted_rows=len(blockers),unresolved_routes=len(missing),status='BLOCKED_NEEDS_AUDIO_SOURCE' if blockers else 'NO_VOICE_REQUIRED' if ident in NO_VOICE else 'IN_PROGRESS_OWNER_PENDING',reason=NO_VOICE.get(ident,'Every known profile/direct call classified; unresolved routing remains explicit'),owner_issue=59 if ident=='rusalka' else 50 if ident in ['vodyanoy','elder_vodyanoy','bolotnik'] else 63 if any('/entity/LandSpiritEntity.java' in p for p in chain) or ident in ['baba_yaga','kot_bayun','kuzya'] else 64 if entity['group'] in ['boss','mini_boss','ally'] else 65,hearing='NOT_RUN'))
 write('PROJECT_EVENT_COVERAGE.csv',actions);write('PROJECT_ENTITY_COVERAGE.csv',coverage);write('FORMS.csv',forms)
 summary=dict(status='IN_PROGRESS',source_base=subprocess.check_output(['git','rev-parse','HEAD'],cwd=R,text=True).strip(),registry_count=len(entities),independent=sum(e['group']!='auxiliary' for e in entities),auxiliary=sum(e['group']=='auxiliary' for e in entities),action_rows=len(actions),profile_event_rows=sum(r.get('event') in events for r in actions),source_guard_pending_rows=sum('REQUIRES_GUARD_REVIEW' in r.get('dispatch','') for r in actions),unresolved_routes=sum(r.get('dispatch')=='UNRESOLVED_DYNAMIC_ROUTE' for r in actions),quarantined_events=sum(not d.get('sounds') for d in events.values()),hearing='NOT_LISTENED',runtime='NOT_RUN',method='Conservative static route join; inherited versus domain helper evidence distinguished. No runtime reachability or quality inferred from prefix alone.')
 (O/'PROJECT_GATES.json').write_text(json.dumps(summary,indent=2)+'\n');print(json.dumps(summary,indent=2))
if __name__=='__main__':main()
