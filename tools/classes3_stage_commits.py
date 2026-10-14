"""Publish reviewable dependent stages in the isolated worktree only."""
from pathlib import Path
import subprocess,re,json,ast
W=Path(r'C:\Users\pavel\.codex\worktrees\classes-3-review\slavic-myths')
def git(*args,input=None):return subprocess.check_output(['git',*args],cwd=W,input=input).decode().strip()
def stage_content(path,text):
 sha=git('hash-object','-w','--stdin',input=text.encode('utf-8'));git('update-index','--add','--cacheinfo','100644,'+sha+','+path)
def add(*paths):git('add','--',*paths)
def commit(message):print(git('commit','-m',message))
C='src/main/java/org/slavicmyths/rpg/classes/'
assert git('rev-parse','HEAD')=='2389b876e72d38ddcef93b7032026a74a64c40ac','Unexpected review baseline'
assert not git('diff','--cached','--name-only'),'Review index is not empty'
git('branch','-m','codex/classes-3-xp')
add(C+'ClassXpService.java',C+'ClassXpEvents.java','src/main/java/org/slavicmyths/tales/TaleEvents.java','src/main/java/org/slavicmyths/ritual/AltarTileEntity.java')
for language in ['ru_ru','en_us']:
 path='src/main/resources/assets/slavicmyths/lang/'+language+'.json';old=json.loads(git('show','HEAD:'+path));current=json.loads((W/path).read_text(encoding='utf-8'));old['classes.slavicmyths.xp_gain']=current['classes.slavicmyths.xp_gain'];stage_content(path,json.dumps(old,ensure_ascii=False,indent=2)+'\n')
commit('feat(classes): add survival XP sources and persistent reward deduplication')
git('switch','-c','codex/classes-3-passives')
for name in ['ClassDefinitions','ClassBalance','ClassPassiveEvents']:add(C+name+'.java')
path=C+'ClassState.java';s=(W/path).read_text(encoding='utf-8');a=s.index('        var ultimate=ClassUltimates.ALL.get(id);');b=s.index('        var b=ClassDefinitions.BRANCHES.get(id);',a);stage_content(path,s[:a]+s[b:])
path=C+'ClassEvents.java';s=(W/path).read_text(encoding='utf-8');s=s.replace('ClassUltimateRuntime.clear(p);','').replace('ClassUltimateRuntime.tick(p);','').replace('||ClassUltimateRuntime.secondary(p)','');s=re.sub(r'ClassTrialService\.block\(p,enemy,block\.damage\(\)\.getBlockedDamage\(\)\);','',s);s=s.replace('if(hits>=2)ClassTrialService.progress(p,"bogatyr",1,null,true);','');stage_content(path,s)
path=C+'ClassRuntime.java';s=(W/path).read_text(encoding='utf-8');s=re.sub(r'ClassTrialService\.cast\(p,id,t\);','',s);s=s.replace('ClassTrialService.escaped(p,"naletchik",escaped.get());','').replace('if(pursuers>=2)ClassTrialService.escaped(p,"ataman",escapedEnemy);','');stage_content(path,s)
add('src/main/resources/META-INF/accesstransformer.cfg')
path='src/main/java/org/slavicmyths/client/ClassScreen.java';s=(W/path).read_text(encoding='utf-8');s=re.sub(r'  button\(left\+Math\.min\(318,panel-88\),top\+62,80,tr\("mastery"\),b->minecraft\.setScreen\(new ClassMasteryScreen\(this\)\)\);\n','',s);stage_content(path,s)
path='src/main/java/org/slavicmyths/client/ClassClient.java';s=git('show','HEAD:'+path);final=(W/path).read_text(encoding='utf-8');a=final.index('String passive2=');b=final.index('\n        var ultimate=',a);s=s.replace('g.pose().popPose();',final[a:b]+'g.pose().popPose();');stage_content(path,s)
for language in ['ru_ru','en_us']:
 path='src/main/resources/assets/slavicmyths/lang/'+language+'.json';old=json.loads(git('show','HEAD:'+path));current=json.loads((W/path).read_text(encoding='utf-8'))
 for id in ['battle_discipline','iron_will','spiritual_fortitude','sign_mastery','ambush_master','survival_instinct']:
  for suffix in ['','.description']:old['classskill.slavicmyths.'+id+suffix]=current['classskill.slavicmyths.'+id+suffix]
  add('src/main/resources/assets/slavicmyths/textures/gui/classes/'+id+'.png')
 for id in ['passive_second','passive_locked','passive_open']:old['classes.slavicmyths.'+id]=current['classes.slavicmyths.'+id]
 stage_content(path,json.dumps(old,ensure_ascii=False,indent=2)+'\n')
commit('feat(classes): preserve v2 progress and add two equipped passive slots')
git('switch','-c','codex/classes-3-ultimates')
add(C+'ClassUltimates.java',C+'ClassUltimateRuntime.java',C+'ClassTrialService.java',C+'ClassState.java',C+'ClassEvents.java',C+'ClassRuntime.java',C+'ClassNetwork.java',C+'ClassCommands.java','src/main/java/org/slavicmyths/rpg/RpgNetwork.java','src/main/java/org/slavicmyths/military/GuardBehavior.java','src/main/resources/data/slavicmyths/tags/entity_type/class_bloodless.json')
commit('feat(classes): implement twelve trial-gated higher classes and finite combat effects')
git('switch','-c','codex/classes-3-ui-qa')
add('src/main/java/org/slavicmyths/client/ClassClient.java','src/main/java/org/slavicmyths/client/ClassScreen.java','src/main/java/org/slavicmyths/client/ClassMasteryScreen.java','src/main/resources/assets/slavicmyths/lang/ru_ru.json','src/main/resources/assets/slavicmyths/lang/en_us.json','src/main/resources/assets/slavicmyths/textures/gui/classes','tools/classes3_assets.py','tools/class_resources_131.py','tools/verify_classes3.py','tools/classes3.init.gradle','tools/classes3-tests','tools/class-tests/org/slavicmyths/verify/ClassGameTests.java')
commit('feat(classes): integrate fourth-stage cards, controls, icons and native QA')
print(git('log','-4','--format=%h %s'))
