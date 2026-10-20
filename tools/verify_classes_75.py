"""Issue 75 offline rules, source-layout illustration and production parity. No game launch."""
from pathlib import Path
import hashlib,json,re,subprocess,tomllib,zipfile
from PIL import Image,ImageDraw,ImageFont
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/classes-75'
JAVA=Path('C:/Program Files/Java/jdk-21.0.12/bin');T=R/'.tools/class-ui-checks'
def main():
 O.mkdir(parents=True,exist_ok=True);T.mkdir(parents=True,exist_ok=True)
 sources=['src/main/java/org/slavicmyths/rpg/classes/ClassDefinitions.java','src/main/java/org/slavicmyths/rpg/classes/ClassLearningRules.java','src/main/java/org/slavicmyths/client/ClassTreeLayout.java','src/main/java/org/slavicmyths/client/ClassTreeViewport.java','tools/class-ui-tests/ClassUiChecks.java']
 subprocess.run([str(JAVA/'javac.exe'),'-encoding','UTF-8','-d',str(T),*[str(R/p) for p in sources]],check=True)
 lines=subprocess.check_output([str(JAVA/'java.exe'),'-cp',str(T),'ClassUiChecks','--nodes'],text=True).splitlines();checks=json.loads(lines[-1]);nodes={}
 for line in lines[:-1]:
  _,base,id,x,y,parent=line.split('\t');nodes.setdefault(base,[]).append(dict(id=id,x=int(x),y=int(y),parent=parent))
 (O/'layout.json').write_text(json.dumps(nodes,indent=2)+'\n',encoding='utf-8')
 langs={lang:json.loads((R/f'src/main/resources/assets/slavicmyths/lang/{lang}.json').read_text(encoding='utf-8')) for lang in ('ru_ru','en_us')}
 screen=(R/'src/main/java/org/slavicmyths/client/ClassScreen.java').read_text(encoding='utf-8')
 for key in re.findall(r'tr\("([a-z_]+)"\s*[,)]',screen):
  for lang in langs:assert 'classes.slavicmyths.'+key in langs[lang],key
 # Full logical tree from actual Java positions/resources, not a game screenshot.
 font=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',13);title=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',18)
 width=max(max(n['x'] for n in group)+70 for group in nodes.values());heights=[max(n['y'] for n in group)+115 for group in nodes.values()]
 image=Image.new('RGB',(width+40,sum(heights)+55),'#211a14');draw=ImageDraw.Draw(image);draw.text((15,10),'SOURCE LAYOUT — not a Minecraft screenshot',font=title,fill='#d6b46d');offset=50
 for (base,group),height in zip(nodes.items(),heights):
  draw.text((15,offset),langs['ru_ru']['class.slavicmyths.'+base],font=title,fill='#f1e7d0');lookup={n['id']:n for n in group};oy=offset+32
  for n in group:
   if n['parent'] in lookup:
    parent=lookup[n['parent']];ax=parent['x']+37;ay=parent['y']+oy+44;bx=n['x']+37;by=n['y']+oy;mid=(ay+by)//2
    draw.line([(ax,ay),(ax,mid),(bx,mid),(bx,by)],fill='#80623d',width=2)
  for n in group:
   x=n['x']+15;y=n['y']+oy;draw.rectangle((x,y,x+44,y+44),outline='#d6b46d')
   icon=Image.open(R/f"src/main/resources/assets/slavicmyths/textures/gui/classes/{n['id']}.png").convert('RGBA').resize((36,36),Image.Resampling.NEAREST);image.paste(icon,(x+4,y+4),icon)
   # IDs identify exact source nodes without obscuring adjacent art.
   draw.text((x,y+46),n['id'][:9],font=font,fill='#f1e7d0')
  offset+=height
 image.save(O/'source-layout.png')
 props=dict(l.split('=',1) for l in (R/'gradle.properties').read_text().splitlines() if '=' in l);jar=R/f"build/libs/slavicmyths-{props['mod_version']}.jar";resources=classes=0
 with zipfile.ZipFile(jar) as z:
  assert z.testzip() is None
  meta=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode());assert any(m['modId']=='slavicmyths' and m['version']==props['mod_version'] for m in meta['mods'])
  assert not any('/verify/' in n or n.startswith(('tools/','smoke/')) or 'ClassUiChecks' in n for n in z.namelist())
  for p in (R/'src/main/resources').rglob('*'):
   if not p.is_file():continue
   key=p.relative_to(R/'src/main/resources').as_posix()
   if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8'))
   if key!='META-INF/neoforge.mods.toml':assert z.read(key)==p.read_bytes(),key
   resources+=1
  for p in (R/'build/classes/java/main').rglob('*.class'):
   assert z.read(p.relative_to(R/'build/classes/java/main').as_posix())==p.read_bytes();assert int.from_bytes(p.read_bytes()[6:8],'big')==65;classes+=1
 log=(R/'.tools/issue-75-build.log').read_text(errors='replace');assert all(s in log for s in ('BUILD SUCCESSFUL','COMMANDS_OFFLINE PASS','FROST_STAFF_OFFLINE PASS'))
 checks.update(version=props['mod_version'],jar=str(jar),sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),resources=resources,classes=classes,build='PASS',runtime='NOT_RUN',polymc='NOT_INSTALLED_THIS_TASK')
 (O/'checks.json').write_text(json.dumps(checks,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(checks))
if __name__=='__main__':main()
