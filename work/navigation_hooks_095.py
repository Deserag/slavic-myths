from pathlib import Path
import shutil
root=Path(__file__).resolve().parents[1]
archive=root/'.tools/port-backups/slavicmyths-0.9.4-final.jar'
if not archive.exists(): shutil.copy2(root/'build/libs/slavicmyths-0.9.4.jar',archive)
p=root/'src/main/java/org/slavicmyths/yaga/YagaUtilityItem.java'
s=p.read_text(encoding='utf-8')
a=s.index('YagaHut.prepare(overworld);');b=s.index('p.getCooldowns().addCooldown(this,80);',a)
s=s[:a]+'if(!org.slavicmyths.navigation.NavigationManager.thread(p))return InteractionResultHolder.fail(s);'+s[b:]
p.write_text(s,encoding='utf-8')
p=root/'src/main/java/org/slavicmyths/yaga/YagaMenu.java';s=p.read_text(encoding='utf-8')
s=s.replace('if(result){data().setDirty();','if(result){if(action==120)org.slavicmyths.navigation.NavigationManager.contractAccepted(server);data().setDirty();')
s=s.replace('if(!progress.completeContract(progress.contract,p.level().getGameTime()))','String navigationQuest=progress.active;if(!progress.completeContract(progress.contract,p.level().getGameTime()))')
s=s.replace('YagaServices.give(p,e.output());return true;}','YagaServices.give(p,e.output());org.slavicmyths.navigation.NavigationManager.endQuest(p,navigationQuest,true);return true;}',1)
p.write_text(s,encoding='utf-8')
p=root/'src/main/java/org/slavicmyths/hunt/HuntItems.java';s=p.read_text(encoding='utf-8')
s=s.replace('records.setDirty();w.playSound','records.setDirty();org.slavicmyths.navigation.NavigationManager.remember(mob);var navigationEncounter=org.slavicmyths.navigation.NavigationRecords.get(w).encounters.get(mob.getUUID());if(navigationEncounter!=null)org.slavicmyths.navigation.NavigationManager.assign(p,navigationEncounter,"hunt:"+mob.getUUID(),null,0,false);w.playSound')
p.write_text(s,encoding='utf-8')
p=root/'src/main/java/org/slavicmyths/hunt/BossRitualItem.java';s=p.read_text(encoding='utf-8')
s=s.replace('d.bossOwners.put(p.getUUID(),key);d.setDirty();','d.bossOwners.put(p.getUUID(),key);d.setDirty();var entity=w.getEntity(b.target);if(entity!=null)org.slavicmyths.navigation.NavigationManager.remember(entity);var nav=org.slavicmyths.navigation.NavigationRecords.get(w).encounters.get(b.target);if(nav!=null)org.slavicmyths.navigation.NavigationManager.assign(p,nav,key,null,0,false);')
p.write_text(s,encoding='utf-8')
p=root/'gradle.properties';p.write_text(p.read_text().replace('mod_version=0.9.4','mod_version=0.9.5'),encoding='utf-8')
