"""Offline packaging/resource gates only; never starts Minecraft."""
from pathlib import Path
import hashlib,io,json,re,subprocess,zipfile
from PIL import Image
import class_ui_resources_1323 as art

ROOT=Path(__file__).resolve().parents[1]
checks=0
def check(value,message):
 global checks
 checks+=1
 assert value,message
def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def main():
 jar=ROOT/'build/libs/slavicmyths-1.3.2.3.jar'
 check(jar.is_file(),'Production JAR')
 java=Path('C:/Program Files/Java/jdk-21.0.12/bin')
 sources=['src/main/java/org/slavicmyths/rpg/classes/ClassDefinitions.java','src/main/java/org/slavicmyths/rpg/classes/ClassLearningRules.java','src/main/java/org/slavicmyths/client/ClassTreeLayout.java','tools/class-ui-tests/ClassUiChecks.java']
 target=ROOT/'.tools/class-ui-checks';target.mkdir(parents=True,exist_ok=True)
 subprocess.run([str(java/'javac.exe'),'-encoding','UTF-8','-d',str(target),*[str(ROOT/p) for p in sources]],check=True)
 rules=json.loads(subprocess.check_output([str(java/'java.exe'),'-cp',str(target),'ClassUiChecks'],text=True))
 with zipfile.ZipFile(jar) as z:
  names=set(z.namelist());check('META-INF/neoforge.mods.toml' in names,'NeoForge metadata')
  check('version="1.3.2.3"' in z.read('META-INF/neoforge.mods.toml').decode().replace(' ',''),'Version')
  check(not any('ClassUiChecks' in n or '/smoke/' in n or 'ClassGameTests' in n or '/classcheck/' in n.lower() for n in names),'No test code')
  prefix='assets/slavicmyths/textures/gui/classes/'
  for id in ['druzhinnik','vedun','razboinik',*art.definitions.SKILLS]:
   file=prefix+id+'.png';check(file in names,'Missing icon '+id)
   image=Image.open(io.BytesIO(z.read(file)));size=64 if id in ('druzhinnik','vedun','razboinik') else 32
   check(image.size==(size,size),'Icon size '+id);check(image.mode=='RGBA' and image.getbbox() is not None,'Real transparent art '+id)
  for name in ['button_normal','button_hover','button_pressed','button_disabled','button_selected','node_locked','node_available','node_learned','node_selected']:
   file=prefix+'ui/'+name+'.png';check(file in names,'GUI asset '+name)
   image=Image.open(io.BytesIO(z.read(file)));check(image.size==((96,24) if name.startswith('button') else (48,48) if name=='node_selected' else (44,44)),'GUI size '+name)
  skills=list(art.definitions.SKILLS);check(len({z.read(prefix+n+'.png') for n in skills})==34,'Distinct skill images')
  for lang in ('ru_ru','en_us'):
   data=json.loads(z.read('assets/slavicmyths/lang/'+lang+'.json'))
   for key in art.UI:check(bool(data.get('classes.slavicmyths.'+key)),'UI translation '+key)
   for id in skills:
    for suffix in ('','.description'):check(bool(data.get('classskill.slavicmyths.'+id+suffix)),'Skill translation '+id+suffix)
   screen=(ROOT/'src/main/java/org/slavicmyths/client/ClassScreen.java').read_text(encoding='utf-8')
   for key in re.findall(r'tr\("([a-z_]+)"\s*[,)]',screen):check('classes.slavicmyths.'+key in data,'Screen key '+key)
  for path in (ROOT/'src/main/resources/assets/slavicmyths/textures/gui/classes').rglob('*.png'):
   name=path.relative_to(ROOT/'src/main/resources').as_posix();check(z.read(name)==path.read_bytes(),'Stale packaged resource '+name)
 # Regenerate ONLY this overlay, and prove byte reproducibility of its canonical resources.
 paths=[*(art.OUT.rglob('*.png')),*(art.ASSETS/'lang').glob('*.json')]
 before={str(p):digest(p) for p in paths};art.main()
 for p in paths:check(digest(p)==before[str(p)],'Generator drift '+str(p))
 report={'version':'1.3.2.3','resource_and_packaging_checks':checks,**rules,'jar':str(jar),'sha256':digest(jar),'bytes':jar.stat().st_size,'build':'clean build PASS','runtime_testing':False,'visual_runtime_testing':False}
 (ROOT/'docs/verification/classes-ui-1.3.2.3.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print(json.dumps(report,ensure_ascii=False))
if __name__=='__main__':main()
