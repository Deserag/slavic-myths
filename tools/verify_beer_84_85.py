"""Combined #84/#85 gates: production resources, existing routes and actual JVM rules."""
from pathlib import Path
import hashlib,json,re,subprocess,tomllib,zipfile
from PIL import Image
import beer_models_125 as art
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';O=R/'docs/implementation/drinks-19726';JDK=Path('C:/Program Files/Java/jdk-21.0.12/bin')

def hashes(root):return {p.relative_to(root).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in root.rglob('*') if p.is_file()}
def main():
 O.mkdir(parents=True,exist_ok=True)
 # Exact regeneration; report any collateral writes, even outside the beer set.
 before=hashes(R/'src/main/resources');art.main();assert hashes(R/'src/main/resources')==before,'beer generator changed production bytes'
 out=R/'.tools/beer8485-jvm';out.mkdir(parents=True,exist_ok=True)
 subprocess.run([str(JDK/'javac.exe'),'-encoding','UTF-8','-d',str(out),str(R/'src/main/java/org/slavicmyths/brewing/BrewRules.java'),str(R/'tools/brewing-tests/IntoxicationChecks.java')],check=True)
 result=subprocess.check_output([str(JDK/'java.exe'),'-cp',str(out),'IntoxicationChecks'],text=True);assert 'INTOXICATION_OFFLINE PASS' in result
 (O/'jvm-tests.txt').write_text(result,encoding='utf-8')
 brewing=(R/'src/main/java/org/slavicmyths/brewing/Brewing.java').read_text(encoding='utf-8')
 actual=re.search(r'BEERS=\{([^}]+)\}',brewing).group(1);assert re.findall(r'"([^"]+)"',actual)==list(art.BEER_ART)
 item=(R/'src/main/java/org/slavicmyths/brewing/BeverageItem.java').read_text(encoding='utf-8')
 assert 'container.equals("dark_bottle")&&mugAllowed(b)' in item and 'new net.minecraft.world.item.component.CustomModelData(1)' in item
 assert 'Set.of("kvass","mead","apple_cider","berry_mors")' in item # No beer pitchers exist.
 assert 'p.setData(Brewing.LOAD,n)' in item and 'int stage=BrewRules.stage(points)' in item
 assert 'amp=BrewRules.amplifier(points)' in item
 assert 'if(magic&&stage>=3)' not in item
 assert 'negative(p,MobEffects.POISON,8,amp,factor)' in item
 assert 'negative(p,MobEffects.MOVEMENT_SLOWDOWN,stage==4?30:45,amp,factor)' in item
 # #84 only changes resource generation. #85 already changed effect threshold.
 beforeitem=subprocess.check_output(['git','show','f07225da6f4c24efcda38730995e08bd7b2c70ba:src/main/java/org/slavicmyths/brewing/BeverageItem.java'],cwd=R).decode().replace('\r\n','\n')
 for name in ('beverage','pitcherAllowed','mugAllowed','load','quality','filled','empty'):
  pattern=r' public static [^\n]+ '+name+r'\([^\n]+'
  assert re.search(pattern,item).group(0)==re.search(pattern,beforeitem).group(0),name
 # Owner requested kvass and mors to share the intact beer vessel and colored top.
 for name in ('kvass','berry_mors'):
  path=A/f'models/item/{name}_mug.json';model=json.loads(path.read_text())
  template=json.loads((A/'models/item/dark_beer_mug.json').read_text())
  assert model['elements']==template['elements'] and model['display']==template['display']
  assert model['textures']['foam']==f'slavicmyths:item/{name}_foam'
  assert model['overrides']==[{'predicate':{'custom_model_data':1},'model':f'slavicmyths:item/{name}_bottled'}]
  for variant in ('mug','bottled','liquid','foam'):
   im=Image.open(A/f'textures/item/{name}_{variant}.png').convert('RGBA');assert im.size==(16,16) and set(im.getchannel('A').getdata())<={0,255}
 # Existing eight beer models/textures must stay byte-for-byte as accepted for this revision.
 for beer in art.BEER_ART:
  for folder,suffixes in [('models/item',('_mug.json','_bottled.json')),('textures/item',('_mug.png','_bottled.png','_liquid.png','_foam.png'))]:
   for suffix in suffixes:
    path='src/main/resources/assets/slavicmyths/'+folder+'/'+beer+suffix
    before=subprocess.check_output(['git','show','48ed68246c5073b581c6d9ef2fe6e0ed8e401254:'+path],cwd=R)
    assert (before.decode().replace('\r\n','\n')==(R/path).read_text() if suffix.endswith('.json') else before==(R/path).read_bytes()),path
 evidence=[];marks=set();foams=set()
 vanilla=zipfile.ZipFile(R/'build/moddev/artifacts/neoforge-21.1.255-client-extra-aka-minecraft-resources.jar')
 for beer in art.BEER_ART:
  paths=[]
  for variant in ('mug','bottled'):
   modelpath=A/f'models/item/{beer}_{variant}.json';model=json.loads(modelpath.read_text());paths.append(str(modelpath.relative_to(R)))
   if variant=='mug':
    assert model['overrides']==[{'predicate':{'custom_model_data':1},'model':f'slavicmyths:item/{beer}_bottled'}]
    mask=[(e['from'],e['to']) for e in model['elements'] if any(f['texture']=='#foam' for f in e['faces'].values())]
    signature=json.dumps(mask);foams.add(signature)
    assert len(mask)==1 and mask[0]==([5,9.65,5],[10,9.72,10])
    base=json.loads(subprocess.check_output(['git','show','f07225da6f4c24efcda38730995e08bd7b2c70ba:src/main/resources/assets/slavicmyths/models/item/'+beer+'_mug.json'],cwd=R))
    structure=lambda m:[e for e in m['elements'] if not any(f['texture'] in ('#liquid','#foam') for f in e['faces'].values())]
    assert structure(base)==structure(model),'body/handle/bottom changed'
    assert base['display']==model['display'],'display changed'
   assert 'mark' not in model['textures']
   assert not any(f['texture']=='#mark' for e in model['elements'] for f in e['faces'].values())
   if variant=='bottled':
    base=json.loads(subprocess.check_output(['git','show','f07225da6f4c24efcda38730995e08bd7b2c70ba:src/main/resources/assets/slavicmyths/models/item/'+beer+'_bottled.json'],cwd=R))
    assert base==model,'existing bottle model changed'
   for e in model['elements']:
    assert all(-16<=a<b<=32 for a,b in zip(e['from'],e['to']))
    for f in e['faces'].values():
     assert f['texture'][1:] in model['textures']
     if 'uv' in f:assert len(f['uv'])==4 and all(0<=v<=16 for v in f['uv'])
   for ref in model['textures'].values():
    ns,path=ref.split(':');key=f'assets/{ns}/textures/{path}.png'
    assert (R/'src/main/resources'/key).exists() or key in vanilla.namelist(),key
  for suffix in ('mug','bottled','liquid','foam'):
   p=A/f'textures/item/{beer}_{suffix}.png';im=Image.open(p).convert('RGBA');assert im.size==(16,16)
   assert set(im.getchannel('A').getdata())<={0,255};paths.append(str(p.relative_to(R)))
  surface=Image.open(A/f'textures/item/{beer}_foam.png').tobytes();assert surface not in marks;marks.add(surface)
  evidence.append(dict(id='slavicmyths:'+beer+'_mug',bottled='custom_model_data=1',pitcher='NOT_SUPPORTED_BY_EXISTING_RULES',paths=paths))
 assert len(foams)==1,'mug surface shape must be identical'
 props=dict(l.split('=',1) for l in (R/'gradle.properties').read_text().splitlines() if '=' in l)
 jar=R/f"build/libs/slavicmyths-{props['mod_version']}.jar";resources=classes=0
 with zipfile.ZipFile(jar) as z:
  assert z.testzip() is None
  meta=tomllib.loads(z.read('META-INF/neoforge.mods.toml').decode());assert any(m['modId']=='slavicmyths' and m['version']==props['mod_version'] for m in meta['mods'])
  assert not any(('IntoxicationChecks' in n or 'IntoxicationNbtChecks' in n) or n.startswith(('tools/','smoke/')) or '/verify/' in n for n in z.namelist())
  for p in (R/'src/main/resources').rglob('*'):
   if not p.is_file():continue
   key=p.relative_to(R/'src/main/resources').as_posix()
   if p.suffix=='.json':json.loads(p.read_text(encoding='utf-8'))
   if key!='META-INF/neoforge.mods.toml':assert z.read(key)==p.read_bytes(),key
   resources+=1
  for p in (R/'build/classes/java/main').rglob('*.class'):
   assert z.read(p.relative_to(R/'build/classes/java/main').as_posix())==p.read_bytes()
   assert int.from_bytes(p.read_bytes()[6:8],'big')==65;classes+=1
 langs={l:json.loads((A/f'lang/{l}.json').read_text(encoding='utf-8')) for l in ('ru_ru','en_us')}
 for key in langs['ru_ru']:
  if key.startswith('brewing.slavicmyths.'):
   assert key in langs['en_us'];assert re.findall(r'%(?:\d+\$)?[sd]',langs['ru_ru'][key])==re.findall(r'%(?:\d+\$)?[sd]',langs['en_us'][key]),key
 log=(R/'.tools/drinks19726-build.log').read_text(errors='replace');assert 'BUILD SUCCESSFUL' in log and 'INTOXICATION_NATIVE PASS' in log
 report=dict(issues=[84,85],version=props['mod_version'],status='IN_PROGRESS / OWNER_PENDING',jar=str(jar),sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),resources=resources,java21_classes=classes,build='PASS',source_base_commit=subprocess.check_output(['git','rev-parse','HEAD'],cwd=R,text=True).strip(),source='uncommitted working tree',generator='PASS byte-identical',jvm_rules='PASS',runtime='NOT_RUN',minecraft_launches=0,polymc='POLYMC_NOT_INSTALLED',owner_acceptance=False,beer_routes=evidence)
 report['source_sha256']={str(p.relative_to(R)):hashlib.sha256(p.read_bytes()).hexdigest() for p in [R/'gradle.properties',R/'src/main/java/org/slavicmyths/brewing/BeverageItem.java',R/'src/main/java/org/slavicmyths/brewing/BrewRules.java',R/'tools/beer_models_125.py']}
 (O/'checks.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps({k:v for k,v in report.items() if k not in ('beer_routes','source_sha256')},ensure_ascii=False))
if __name__=='__main__':main()
