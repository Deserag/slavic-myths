"""Documentation consistency gate; no build, game, resource generation or fixes."""
from pathlib import Path
import csv,json,re,hashlib
ROOT=Path(__file__).resolve().parents[1];OUT=ROOT/'docs/audit/creatures-1.9.8.1'
def rows(name):
    with (OUT/name).open(encoding='utf-8-sig',newline='') as f:return list(csv.DictReader(f))
creatures=rows('CREATURES.csv');attacks=rows('ATTACKS.csv')
primary=[r for r in creatures if r['group']!='auxiliary']
assert len(primary)==69
for r in primary:
    assert r['danger_level'] in {'0','I','II','III','IV','V','VI','VII','VIII'}
    assert len(r['types'].split(';'))>=2
    assert all(r[f] and 'UNVERIFIED' not in r[f] for f in ['target_hp','proposed_base_damage','current_max_health','current_base_damage'])
    assert r['target_hp_status'].startswith(('VERIFIED','PROPOSED'))
    assert r['base_damage_status'].startswith('PROPOSED')
    assert r['danger_status'].startswith(('VERIFIED','PROPOSED'))
by_id={r['entity_type']:r for r in creatures}
assert float(by_id['slavicmyths:zmey_gorynych']['target_hp'])==2800
assert float(by_id['slavicmyths:likho_one_eyed']['target_hp'])==350
assert float(by_id['slavicmyths:firebird']['target_hp'])==3000
assert float(by_id['slavicmyths:cursed_volkolak']['target_hp'])==700
assert by_id['slavicmyths:cursed_volkolak']['target_hp_status'].startswith('PROPOSED')
decisions=[r for r in rows('UNVERIFIED.csv') if r['blocks_implementation']=='YES']
assert {r['field'] for r in decisions}=={'B1','B2','B3','B4'}
for r in attacks:
    assert r['action_category'] in {'DAMAGE','PERIODIC_DAMAGE','HEALING','SUMMON','CONTROL','NO_DIRECT_DAMAGE','UNREACHABLE'}
    assert all(r[f] for f in ['reachability','reachability_evidence','target_status','damage_budget','formula_status','multihit_risk'])
    if r['proposed_category']=='N/A':assert float(r['target_direct_damage'])==0
assert sum(r['action_category']=='UNREACHABLE' for r in attacks)==3
index=json.loads((OUT/'registry-source-index.json').read_text(encoding='utf-8'))
assert {r['id'] for r in index}=={r['entity_type'] for r in creatures}
assert len(creatures)==len({r['entity_type'] for r in creatures})==111
assert all(r['entity_type'] in {c['entity_type'] for c in creatures} for r in attacks)
assert len(attacks)==len({(r['entity_type'],r['form'],r['attack']) for r in attacks}), 'Duplicate semantic attack row'
cache={};refs=set()
for name in ['CREATURES.csv','ATTACKS.csv','FORMS.md','TECHNICAL_RISKS.md']:
    content=(OUT/name).read_text(encoding='utf-8-sig')
    for p,n in re.findall(r'(src/[\w/.-]+\.(?:java|json)):(\d+)',content):
        if p not in cache:cache[p]=(ROOT/p).read_text(encoding='utf-8').splitlines()
        assert 1<=int(n)<=len(cache[p]),(p,n)
        refs.add((p,n))
coeff={'A1':.65,'A2':.8,'A3':.7,'A4':1.,'A5':1.15,'A6':1.35,'A7':1.5}
for r in attacks:
    if r['proposed_category'] in coeff:
        assert float(r['coefficient'])==coeff[r['proposed_category']]
        if r['target_base_damage']!='UNVERIFIED':
            assert float(r['target_direct_damage'])==round(float(r['target_base_damage'])*coeff[r['proposed_category']],2)
    else:assert r['proposed_category']=='N/A'
for r in creatures:
    if r['implementation']!='UNVERIFIED':
        p=ROOT/'src/main/java'/Path(*r['implementation'].split('.')).with_suffix('.java')
        assert p.is_file(),r['implementation']
jar=ROOT/'build/libs/slavicmyths-1.9.7.jar'
receipt={'status':'PASS','entity_types':len(creatures),'creatures':sum(r['group']!='auxiliary' for r in creatures),'attack_rows':len(attacks),'references_exist':len(refs),'source_files_referenced':len(cache),'citation_semantics':'line existence only; not full interpretation','runtime_runs':0,'build':'NOT_REPEATED: documentation only','production_sha256':hashlib.sha256(jar.read_bytes()).hexdigest() if jar.exists() else 'UNVERIFIED'}
receipt.update(audit_status='COMPLETE_WITH_LIMITATIONS',blocking_decision_groups=len(decisions),all_primary_classification_fields_present=True,all_known_action_rows_processed=True,reachability_semantics='static selectable/candidate/unverified producer/unreachable are separate; no runtime certification',historical_sha256='0f1e76dd6bef952350a67d83da5357cc26bcc8dcb290b7eb281c8e46d2290e0e',production_changed_by_this_audit=False)
(OUT/'table-checks.json').write_text(json.dumps(receipt,indent=2)+'\n',encoding='utf-8')
print(json.dumps(receipt))
