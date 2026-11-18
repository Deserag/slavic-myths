"""Evidence index only: extraction is not gameplay/visual acceptance."""
from pathlib import Path
import json,re,csv,subprocess
R=Path(__file__).resolve().parents[2];S=R/'src/main/java';OUT=R/'docs/qa/boss-audit-20261106';OUT.mkdir(parents=True,exist_ok=True)
registry=R/'work/boss-audit/evidence/registry.json'
if not registry.exists():registry=OUT/'REGISTRY.json'
raw=json.loads(registry.read_text(encoding='utf-8'))
palette=(S/'org/slavicmyths/combat/BossBarPalette.java').read_text(encoding='utf-8');known=set(re.findall(r'Map.entry\("([^"\n]+)"',palette));known.add('nine_mor_serpent')
all_sources=list(S.rglob('*.java'));bosses=[];matrix=[];scales=[]
for row in raw['bosses']:
 identifier=row['entity_id'].split(':')[1]
 if identifier not in known:continue
 path=S/Path(*row['entity_class'].split('.'));path=path.with_suffix('.java');text=path.read_text(encoding='utf-8')
 simple=path.stem;renderers=[];models=[]
 for source in all_sources:
  if 'client' not in source.parts and 'Model' not in source.name and 'Client' not in source.name:continue
  body=source.read_text(encoding='utf-8')
  if re.search(r'\b'+re.escape(simple)+r'\b',body) and ('Renderer' in source.name or 'Client' in source.name):
   renderers.append(source.relative_to(R).as_posix());models+=re.findall(r'new\s+(\w+Model)\s*\(',body)
 decls=[]
 for m in re.finditer(r'public\s+static\s+final\s+int\s+([^;]+);',text):
  if any(k in m[1] for k in ['IDLE=','IDLE =','CIRCLE =','CIRCLE=','GRAB =','FIST=']):
   decls += [(n,v) for n,v in re.findall(r'(\w+)\s*=\s*(-?\d+)',m[1])]
 for m in re.finditer(r'(?:public|private)\s+enum\s+(\w+)\s*\{([^;]+);',text):
  for name in re.findall(r'(?:^|,)\s*([A-Z][A-Z_0-9]*)\s*(?:\([^)]*\))?',m[2]):decls.append((name,'enum:'+m[1]))
 row.update(source=path.relative_to(R).as_posix(),renderer_candidates=';'.join(sorted(set(renderers))),model_candidates=';'.join(sorted(set(models))),states=';'.join(n for n,_ in decls),visual='NOT_RUN',hitboxes='NOT_RUN',animations='NOT_RUN',attacks='NOT_RUN',behaviour='NOT_RUN')
 bosses.append(row)
 for name,value in dict(decls).items():
  for phase in ['WINDUP','ACTIVE','RECOVERY','TRANSITION']:
   for damage in ['sword','critical','projectile','rapid_hits','knockback','two_players','mob','effect_area','shield','lethal']:
    matrix.append(dict(entity_id=row['entity_id'],state=name,value=value,phase=phase,damage=damage,status='NOT_RUN',evidence='',note='State/phase eligibility must be verified against source controller; extraction does not prove attack coverage.'))
for path in all_sources:
 if 'Model' not in path.name:continue
 for n,line in enumerate(path.read_text(encoding='utf-8').splitlines(),1):
  if re.search(r'\.(?:xScale|yScale|zScale)\s*=',line):scales.append(dict(file=path.relative_to(R).as_posix(),line=n,expression=line.strip(),status='STATIC_OCCURRENCE_NOT_DEFECT'))
def write(name,rows):
 if not rows:return
 keys=list(dict.fromkeys(k for r in rows for k in r))
 with (OUT/name).open('w',encoding='utf-8',newline='') as f:w=csv.DictWriter(f,fieldnames=keys);w.writeheader();w.writerows(rows)
# Preserve the dated audit's checked-in scoped evidence when regenerating its indexes.
actual=OUT/'ACTUAL_MODELS.csv'
if actual.exists():
 models={r['entity_id']:r for r in csv.DictReader(actual.open(encoding='utf-8'))}
 for row in bosses:
  m=models.get(row['entity_id'])
  if not m:continue
  row.update(actual_renderer=m['renderer'],actual_model=m['model'],actual_cuboids=m['cuboids'],scoped_evidence='actual_model_metadata;hosted_views')
  if row['entity_id'].split(':')[1] in {'gneteya','tryaseya','agneya','ledeya','lomeya','neveya','viy'}:row['scoped_evidence']+=';native_arrow_at_3_collider_heights;real_dedicated_restart'
  if row['entity_id']=='slavicmyths:morovoy_raven':row['behaviour']='FAIL';row['scoped_evidence']+=';BQA-003_health_contract'
poses_file=OUT/'SYNCHRONIZED_POSES.json';pressure_file=OUT/'dedicated-legal-pressure.json'
if poses_file.exists() and pressure_file.exists():
 poses=json.loads(poses_file.read_text())['frames'];pressure={r['id']:r for r in json.loads(pressure_file.read_text())};retry=OUT/'dedicated-tryaseya-loaded.json'
 if retry.exists():pressure['tryaseya']=json.loads(retry.read_text())[0]
 for row in matrix:
  identifier=row['entity_id'].split(':')[1];row.update(source_eligibility='UNRESOLVED_GENERIC_CONTROLLER',controlled_pose_frames='',natural_pressure_state_observed='NOT_RUN')
  if identifier not in {'gneteya','tryaseya','agneya','ledeya','lomeya','neveya','viy'}:continue
  row['source_eligibility']='DECLARED_ACTION_ELIGIBILITY_REQUIRES_PHASE_CONDITIONS'
  if row['state']=='IDLE':row['source_eligibility']='NON_ATTACK_IDLE'
  if (identifier,row['state']) in {('lomeya','GIANT_WINDUP'),('viy','OPEN_EYES')}:row['source_eligibility']='DECLARED_BUT_NOT_SELECTED_BY_CURRENT_NATIVE_CONTROLLER'
  if row['value'].lstrip('-').isdigit():
   row['controlled_pose_frames']=';'.join(x['file'] for x in poses if x['id']==identifier and int(x['mode'].split('_')[1])==int(row['value']) and x['client_pose_matches_requested'])
   row['natural_pressure_state_observed']=str(('action:'+row['value']) in pressure[identifier]['states'].split(';')).upper()
  row['note']+=' Controlled pose/synchronous damage evidence does not certify requested damage-window/phase cell.'
write('BOSSES.csv',bosses);write('ATTACK_MATRIX.csv',matrix);write('MODEL_SCALES.csv',scales)
(OUT/'BASELINE.json').write_text(json.dumps({'commit':'5738a4038f615dc292403cd6308d794cbc2915cc','qa_inventory_commit':'31badc2f','registry_candidates':len(raw['bosses']),'selected_ranked_encounters':len(bosses),'matrix_rows':len(matrix),'construction_errors':raw['construction_errors'],'coverage_warning':'Not complete attack coverage; generic inherited controllers and environmental eligibility require further runtime cases. All gameplay cells initially NOT_RUN.'},indent=2)+'\n')
print('Selected',len(bosses),'ranked encounters;',len(matrix),'pending state/damage cells;',len(scales),'scale occurrences')
