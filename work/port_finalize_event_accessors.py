from pathlib import Path
import re,collections
log=Path('docs/port/finalize-compile.log').read_text(encoding='utf-8',errors='replace').split('FAILURE:')[0];edits={}
for m in re.finditer(r'([^\r\n]+\.java):(\d+): error: ([\s\S]*?)(?=\n[^\n]+\.java:\d+: (?:error|warning):|\Z)',log):
 body=m[3];pairs=[]
 for old,new in [('getPlayer','getEntity'),('getWorld','getLevel')]:
  if 'symbol:   method '+old+'()' in body:pairs.append(('.'+old+'()','.'+new+'()'))
 if 'symbol:   method getLevel()' in body and 'type ServerPlayer' in body:pairs.append(('.getLevel()','.serverLevel()'))
 if 'symbol:   method getTileData()' in body:pairs.append(('.getTileData()','.getPersistentData()'))
 if pairs:edits.setdefault(m[1],{}).setdefault(int(m[2])-1,[]).extend(pairs)
for name,lines in edits.items():
 p=Path(name);s=p.read_text(encoding='utf-8').splitlines(keepends=True)
 for n,pairs in lines.items():
  for old,new in pairs:s[n]=s[n].replace(old,new)
 p.write_text(''.join(s),encoding='utf-8')
print('Compiler-confirmed event accessors:',sum(len(v) for v in edits.values()))
# Source command messages now defer component creation through the required Supplier.
for p in Path('src/main/java').rglob('*.java'):
 s=p.read_text(encoding='utf-8');old=s;pos=0
 while m:=re.search(r'\.sendSuccess\(',s[pos:]):
  start=pos+m.end();i=start;depth=0;quoted=False;escape=False
  while i<len(s):
   c=s[i]
   if quoted:
    if escape:escape=False
    elif c=='\\':escape=True
    elif c=='"':quoted=False
   elif c=='"':quoted=True
   elif c in '([{':depth+=1
   elif c in ')]}':depth-=1
   elif c==',' and depth==0:break
   i+=1
  arg=s[start:i].strip()
  if not arg.startswith('()->') and not arg.startswith('() ->'):
   s=s[:start]+'()->'+s[start:];i+=4
  pos=i+1
 if s!=old:p.write_text(s,encoding='utf-8')
