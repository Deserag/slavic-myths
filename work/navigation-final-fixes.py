from pathlib import Path
root=Path('src/main/java/org/slavicmyths/navigation')
for name in ('NavigationEvents.java','NavigationManager.java'):
 p=root/name;s=p.read_text(encoding='utf-8')
 s=s.replace('List.copyOf(data.assignments.values()))if(a.player().equals(p.getUUID())&&','data.assignmentsFor(p.getUUID()))if(')
 s=s.replace('List.copyOf(data.assignments.values()))if(assignment.player().equals(p.getUUID())&&','data.assignmentsFor(p.getUUID()))if(')
 s=s.replace('List.copyOf(data.assignments.values()))if(assignment.player().equals(p.getUUID()))','data.assignmentsFor(p.getUUID()))')
 s=s.replace('data.assignments.values().stream().anyMatch(a->a.player().equals(p.getUUID())&&a.quest().equals(quest))','data.assignmentsFor(p.getUUID()).stream().anyMatch(a->a.quest().equals(quest))')
 p.write_text(s,encoding='utf-8')
p=root/'client/NavigationScreen.java';s=p.read_text(encoding='utf-8');s=s.replace('"navigation.slavicmyths.state."+(m.area()==null?', '"navigation.slavicmyths.state."+(!NavigationClient.state.markers.containsKey(m.id())?"ended":m.area()==null?');p.write_text(s,encoding='utf-8')
import json
for lang,text in [('en_us','Completed / expired'),('ru_ru','Завершено / истекло')]:
 p=Path('src/main/resources/assets/slavicmyths/lang')/(lang+'.json');data=json.loads(p.read_text(encoding='utf-8'));data['navigation.slavicmyths.state.ended']=text;p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
