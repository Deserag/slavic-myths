from pathlib import Path
import re,collections
s=Path('docs/port/finalize-compile.log').read_text(encoding='utf-8',errors='replace');print(s[-250:])
errors=collections.defaultdict(lambda:collections.defaultdict(set))
for m in re.finditer(r'^(D:\\slavic-myths\\src\\main\\java\\[^\n:]+):(\d+): error: (\w+) has (?:private|protected) access in (\w+)',s,re.M):
 errors[Path(m.group(1))][int(m.group(2))-1].add(m.group(3))
protected=re.compile(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\n]*|/\*[\s\S]*?\*/|net\.minecraft(?:\.\w+)+|\b[A-Za-z_$][\w$]*\b')
getters={'level':'level()','inventory':'getInventory()','abilities':'getAbilities()','yRot':'getYRot()','xRot':'getXRot()','uuid':'getUUID()'}
changes=collections.Counter()
for p,lines in errors.items():
 source=p.read_text(encoding='utf-8').splitlines(keepends=True)
 for i,fields in lines.items():
  if i>=len(source):raise ValueError((p,i))
  field_get={k:getters[k] for k in fields if k in getters}
  if 'client' in p.parts:
   field_get={k:v for k,v in field_get.items() if k not in ['level','xRot','yRot']}
  # Setter sites are kept for manual migration; do not turn assignments into invalid getters.
  field_get={k:v for k,v in field_get.items() if not re.search(r'\b'+k+r'\s*(?:[+\-*/]?=|\+\+|--)',source[i])}
  old=source[i];source[i]=protected.sub(lambda m:field_get.get(m.group(),m.group()),source[i])
  if old!=source[i]:changes[p.name]+=1
 p.write_text(''.join(source),encoding='utf-8')
print('Migrated compiler-confirmed private field reads:',sum(changes.values()),'lines in',len(changes),'files')
print(changes.most_common(12))
