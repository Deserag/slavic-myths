"""Refresh target mapping statuses without mutating immutable source evidence."""
from pathlib import Path
import copy, json

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs/port'
baseline=json.loads((OUT/'baseline-0.9.3.json').read_text(encoding='utf-8'))
entries=copy.deepcopy(baseline['entries'])
helper=json.loads((OUT/'baseline-helper-entries-0.9.3.json').read_text(encoding='utf-8'))
entries.extend(copy.deepcopy(helper['entries']))
resource_kinds={'advancement','recipe','loot_table','structure_template','loot_modifiers'}
changed=[]
logs=[(OUT/name).read_text(encoding='utf-8') for name in
      ('finalize-runtime-nojei.log','finalize-runtime-companions.log')]
verified=all('PORT_RUNTIME_REGISTRIES_PASS checked=794 entities=47' in log and
             'All 4 required tests passed' in log and 'BUILD SUCCESSFUL' in log for log in logs)
if not verified:
    raise SystemExit('Cannot mark parity verified without passing actual loader tests in both configurations')
for row in entries:
    if row['kind'] in resource_kinds or row['kind'].startswith('tag/'):
        row['status']='DONE'
        row['notes']='Resource ID/schema preserved; actual loader data reload passed. Gameplay remains manual.'
        if row['kind']=='structure_template':
            row['notes']='Original binary template SHA-256 preserved; natural placement/template decoding remains manual.'
        if row['old_id'].startswith('forge:'):
            namespace='neoforge' if row['kind']=='loot_modifiers' else 'c'
            row['target_id']=namespace+':'+row['old_id'].split(':',1)[1]
            row['notes']='NeoForge global loot modifier namespace / common tag namespace compatibility'
            changed.append({k:row[k] for k in ('kind','old_id','target_id','notes')})
    else:
        row['status']='DONE'
        row['notes']='Existing registration ID verified under actual target loader; behavior/visual QA remains manual.'
        if row['kind'] in {'command','saved_data','jei_category'}:
            row['status']='COMPILES'
            row['notes']='Complete production code compiles and common bootstrap passed; end-to-end interaction/persistence remains manual.'
        elif row['kind']=='curios_slot':
            row['notes']='Actual player slots verified by Curios API: head/necklace/belt/charm=1, ring=2; gameplay remains manual.'
report={'source':'0.9.3+yaga-placement-hotfix','target':'0.9.4',
        'baseline_counts':baseline['counts'],'additional_verified_legacy_entries':helper['entries'],'runtime_registry_verified':True,
        'verification_scope':'IDs/registration, target data reload and Curios slots; not in-game AI, art or client interaction',
        'changed_external_data_ids':changed,'changed_slavicmyths_ids':[],
        'removed_slavicmyths_ids':[],
        'technical_new_registries':{'data_component_type':['slavicmyths:runes','slavicmyths:hunt_target','slavicmyths:cloth_ready','slavicmyths:hunt_trophies','slavicmyths:flight_cargo'],
            'armor_material':['slavicmyths:perunite','slavicmyths:gambeson','slavicmyths:plate'],
            'creative_mode_tab':['slavicmyths:slavicmyths']},
        'reference_corrections':[{'old':'minecraft:grass','target':'minecraft:short_grass','reason':'Vanilla target rename'},
            {'old':'slavicmyths:pustoy_sosud','target':'slavicmyths:pustoy_ritualny_sosud','reason':'Pre-existing dangling loot reference; existing registry IDs unchanged'}],
        'entries':entries}
(OUT/'registry-parity-0.9.4.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
lines=['# Target registry/data parity — loader verified','',
       'Actual loader tests verify registry IDs, data reload and player Curios slots. DONE here describes this registration/data scope, not complete gameplay QA.',
       'No Slavic Myths public IDs intentionally changed/removed. External Forge data namespaces',
       'use the modern common/NeoForge equivalents listed below.','',
       '| Category | OLD ID | TARGET ID | Status |', '|---|---|---|---|']
for row in entries:
    lines.append('| '+' | '.join(row[k] for k in ('kind','old_id','target_id','status'))+' |')
(OUT/'REGISTRY_PARITY_0.9.4.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')
print(f'{len(entries)} mappings; {len(changed)} external namespace updates; target registration/data verified under loader; client/gameplay QA manual.')
