from pathlib import Path
p=Path('tools/verify_kurgan_096.py');s=p.read_text(encoding='utf-8-sig');s=s.replace('changed=[];preserved=0','changed=[];preserved=0;baseline_assets={}');s=s.replace("  p=ROOT/name;require(p.is_file()", "  if name.startswith('src/main/resources/assets/') and name.endswith('.json'):baseline_assets[name]=json.loads(z.read(name))\n  p=ROOT/name;require(p.is_file()")
s=s.replace('refs=0\ndef resource', 'refs=0;baseline_issues=[];active_file=None\ndef resource');s=s.replace(" require((RES/name).is_file() or name in vanilla_names,'Missing resource '+name);refs+=1",''' if ns=='minecraft' and name in ['assets/minecraft/models/builtin/generated.json','assets/minecraft/models/builtin/entity.json']:return
 if not ((RES/name).is_file() or name in vanilla_names):
  relative='src/main/resources/'+active_file.relative_to(RES).as_posix();original=baseline_assets.get(relative)
  if original is None and relative.endswith('_held.json'):original=baseline_assets.get(relative.replace('_held.json','.json'))
  require(original is not None and json.dumps(value) in json.dumps(original),'Missing NEW resource '+name+' in '+relative)
  baseline_issues.append({'model':relative,'missingReference':name,'scope':'unchanged 0.9.5 resource; outside burial rework'})
 refs+=1''');s=s.replace("for p in (RES/'assets/slavicmyths'/folder).rglob('*.json'):walk(json.loads(p.read_text(encoding='utf-8')))","for p in (RES/'assets/slavicmyths'/folder).rglob('*.json'):\n  active_file=p;walk(json.loads(p.read_text(encoding='utf-8')))");s=s.replace("'changedExistingResources':changed,", "'changedExistingResources':changed,'existingBaselineAssetIssues':baseline_issues,")
s=s.replace("'; client not launched')", "'; pre-existing missing model references='+str(len(baseline_issues))+'; client not launched')");p.write_text(s,encoding='utf-8')
