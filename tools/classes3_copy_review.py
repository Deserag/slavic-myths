"""Copy only this task's files to its isolated Git worktree, preserving all neighbouring work."""
from pathlib import Path
import ast,json,shutil,subprocess
root=Path(__file__).resolve().parents[1]
dest=Path(r'C:\Users\pavel\.codex\worktrees\classes-3-review\slavic-myths')
files=[]
files += ['src/main/java/org/slavicmyths/rpg/classes/'+x+'.java' for x in ['ClassBalance','ClassCommands','ClassDefinitions','ClassEvents','ClassNetwork','ClassRuntime','ClassState','ClassPassiveEvents','ClassTrialService','ClassUltimateRuntime','ClassUltimates','ClassXpEvents','ClassXpService']]
files += ['src/main/java/org/slavicmyths/client/'+x+'.java' for x in ['ClassClient','ClassScreen','ClassMasteryScreen']]
files += ['src/main/java/org/slavicmyths/rpg/RpgNetwork.java','src/main/java/org/slavicmyths/tales/TaleEvents.java','src/main/java/org/slavicmyths/military/GuardBehavior.java','src/main/resources/META-INF/accesstransformer.cfg','src/main/resources/data/slavicmyths/tags/entity_type/class_bloodless.json','tools/classes3_assets.py','tools/classes3.init.gradle','tools/class_resources_131.py','tools/classes3-tests/org/slavicmyths/verify/Classes3GameTests.java','tools/class-tests/org/slavicmyths/verify/ClassGameTests.java']
tree=ast.parse((root/'tools/classes3_assets.py').read_text(encoding='utf-8'));values={}
for node in tree.body:
 if isinstance(node,ast.Assign) and len(node.targets)==1 and isinstance(node.targets[0],ast.Name) and node.targets[0].id in ['names','passives','trials','ui']:values[node.targets[0].id]=ast.literal_eval(node.value)
keys={'key.slavicmyths.class_ultimate'}
keys|={'classes.slavicmyths.'+key for key in values['ui']}
for cls,skill,*_ in values['names']:
 keys|={'class.slavicmyths.'+cls,'classskill.slavicmyths.'+skill,'classskill.slavicmyths.'+skill+'.description'}
 for id in [cls,skill]:files.append('src/main/resources/assets/slavicmyths/textures/gui/classes/'+id+'.png')
for skill,*_ in values['passives']:
 keys|={'classskill.slavicmyths.'+skill,'classskill.slavicmyths.'+skill+'.description'}
 files.append('src/main/resources/assets/slavicmyths/textures/gui/classes/'+skill+'.png')
keys|={'classtrial.slavicmyths.'+row[0] for row in values['trials']}
for lang in ['ru_ru','en_us']:
 rel='src/main/resources/assets/slavicmyths/lang/'+lang+'.json'
 original=json.loads(subprocess.check_output(['git','show','2389b876:'+rel],cwd=root).decode('utf-8'))
 current=json.loads((root/rel).read_text(encoding='utf-8'));original.update({k:current[k] for k in keys})
 (dest/rel).write_text(json.dumps(original,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 files.append(rel)
for rel in files:
 if '/lang/' in rel:continue
 target=dest/rel;target.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(root/rel,target)
out=dest/'docs/implementation/classes3';out.mkdir(parents=True,exist_ok=True)
for p in (root/'docs/implementation/classes3').glob('*'):
 if p.is_file():shutil.copy2(p,out/p.name)
(root/'docs/implementation/classes3/OWNED_FILES.json').write_text(json.dumps(files,indent=2)+'\n',encoding='utf-8')
print(dest);print('Copied',len(files),'owned files; neighbouring changes excluded')
