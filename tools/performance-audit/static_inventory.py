from pathlib import Path
import re,json,hashlib
R=Path.cwd();J=R/'src/main/java';O=R/'docs/implementation/performance-audit';events=[];totals={}
for p in J.rglob('*.java'):
 s=p.read_text(encoding='utf-8-sig');rel=p.relative_to(R).as_posix()
 for m in re.finditer(r'@SubscribeEvent(?:\([^\n]*?\))?\s*(?:public\s+)?static\s+void\s+(\w+)\s*\(([^)]*)\)',s):
  arg=m.group(2).split(',')[0].strip().split();events.append({'file':rel,'line':s.count('\n',0,m.start())+1,'method':m.group(1),'argument':arg[0] if arg else ''})
 totals[rel]={'getEntitiesOfClass_text_occurrences':s.count('getEntitiesOfClass('),'setDirty_text_occurrences':s.count('setDirty('),'getChunk_text_occurrences':len(re.findall(r'\.getChunk\(',s))}
(O/'static-inventory.json').write_text(json.dumps({'warning':'Regex inventory, not call graph or runtime counts; fully qualified annotations may be missed. No costs or leaks inferred.','handlers':events,'files_with_queries_dirty_or_chunk_calls':{k:v for k,v in totals.items() if any(v.values())}},indent=2),encoding='utf-8')
print('regex_handlers',len(events));print('event_types',len(set(x['argument'] for x in events)));print('radius16 sphere cells',sum(x*x+y*y+z*z<=256 for x in range(-16,17) for y in range(-16,17) for z in range(-16,17)))
