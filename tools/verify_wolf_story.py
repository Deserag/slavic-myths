"""1.4.2 resources, preserved baseline and real production JAR checks. No gameplay claim."""
from pathlib import Path
from PIL import Image
import hashlib,json,re,subprocess,zipfile
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';OUT=ROOT/'docs/verification/grey_wolf_1.4.2';checks=0
def check(condition,message):
 global checks
 checks+=1
 if not condition:raise AssertionError(message)
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
version=re.search(r'^mod_version=(.+)$',(ROOT/'gradle.properties').read_text(),re.M).group(1)
jar=ROOT/f'build/libs/slavicmyths-{version}.jar'
allowed={
 'build.gradle','gradle.properties','docs/ARCHITECTURE.md','docs/PROJECT_STATUS.md','docs/ROADMAP.md',
 'tools/finalize_065.py','tools/grey-wolf-tests/org/slavicmyths/verify/GreyWolfGameTests.java'}
allowed|={'src/main/java/org/slavicmyths/'+s for s in [
 'client/ClientSetup.java','client/CuriosAccessories.java','client/FlightClient.java','flight/FlyingVessel.java',
 'entity/WildlifeEntity.java','item/ItemState.java','kurgan/KurganEncounters.java','kurgan/KurganInstance.java',
 'registry/ModEntities.java','registry/ModItemGroup.java','registry/ModItems.java','registry/ModSounds.java','wolf/GreyWolf.java']}
allowed|={'src/main/resources/'+s for s in [
 'data/minecraft/tags/item/enchantable/bow.json','data/minecraft/tags/item/enchantable/durability.json',
 'data/curios/tags/item/belt.json','data/curios/tags/item/ring.json',
 'assets/slavicmyths/sounds.json','assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json']}
base=json.loads((ROOT/'work/grey-wolf-142/baseline.json').read_text(encoding='utf-8'));changed=[];preserved=0
for name,sha in base.items():
 p=ROOT/name;check(p.is_file(),'Missing baseline '+name)
 if digest(p)!=sha:check(name in allowed,'Unexpected baseline mutation '+name);changed.append(name)
 else:preserved+=1
for p in RES.rglob('*.json'):json.loads(p.read_text(encoding='utf-8'));check(True,'JSON')
for p in RES.rglob('*.png'):
 with Image.open(p) as im:im.verify()
 check(True,'PNG')
assets=RES/'assets/slavicmyths';owned=json.loads((ROOT/'tools/grey_wolf_142_text.json').read_text(encoding='utf-8'))
for locale in ['ru_ru','en_us']:
 actual=json.loads((assets/f'lang/{locale}.json').read_text(encoding='utf-8'))
 for key,value in owned[locale].items():check(actual.get(key)==value,'Localization '+key);check('???'not in value,'Invalid text '+key)
for name in ['ring_of_grey_pack','finist_bow','finist_bow_pulling_0','finist_bow_pulling_1','finist_bow_pulling_2','finist_quiver']:
 im=Image.open(assets/f'textures/item/{name}.png').convert('RGBA');check(im.size==(32,32),'Icon dimensions');check(set(im.getchannel('A').getdata())=={0,255},'Hard pixel alpha');check(len(set(im.getdata()))<=15,'Palette');check(im.getpixel((0,0))[3]==0,'Transparent background')
 model=json.loads((assets/f'models/item/{name}.json').read_text(encoding='utf-8'));check(model['textures']['layer0']=='slavicmyths:item/'+name,'Model texture')
for tag in ['bow','durability']:
 data=json.loads((RES/f'data/minecraft/tags/item/enchantable/{tag}.json').read_text());check(not data['replace'] and all('slavicmyths:'+s in data['values']for s in ['short_bow','heavy_bow','finist_bow']),'Preserved enchantments '+tag)
for folder in ['recipe','loot_table']:
 for p in (RES/'data/slavicmyths'/folder).rglob('*.json'):check('slavicmyths:finist_bow'not in p.read_text(encoding='utf-8')and'slavicmyths:finist_quiver'not in p.read_text(encoding='utf-8'),'Reserved reward leaked '+str(p))
audio=[];catalog=json.loads((assets/'sounds.json').read_text(encoding='utf-8'))
for event,value in owned['sounds'].items():
 check(catalog.get(event)==value,'Sound catalog '+event)
 for sound in value['sounds']:
  name=sound['name'].split(':',1)[-1];path=assets/f'sounds/{name}.ogg';check(path.is_file(),'Missing audio '+name)
  probe=json.loads(subprocess.check_output(['ffprobe','-v','error','-show_streams','-show_format','-of','json',str(path)]));s=probe['streams'][0];duration=float(probe['format']['duration']);check(s['channels']==1 and s['codec_name']=='vorbis'and 0<duration<=1.5,'Sound format '+name);audio.append({'event':event,'duration':duration,'mono':True,'sha256':digest(path)})
for folder in ['finist','military','wolf']:
 for p in (ROOT/f'src/main/java/org/slavicmyths/{folder}').glob('*.java'):check('net.minecraft.client'not in p.read_text(encoding='utf-8'),'Common client dependency '+str(p))
with zipfile.ZipFile(jar) as z:
 names=set(z.namelist());check(z.testzip()is None,'JAR CRC');check(f'version="{version}"'in z.read('META-INF/neoforge.mods.toml').decode(),'Production version');check(not any('verify/'in n or'slavicmyths_wolf_story/'in n or'smoke/'in n for n in names),'Fixture leak')
 for p in RES.rglob('*'):
  if p.is_file():n=p.relative_to(RES).as_posix();check(n in names,'Missing resource '+n);check(n.endswith('neoforge.mods.toml')or z.read(n)==p.read_bytes(),'Stale packaged resource '+n)
 for n in names:
  if n.startswith('org/slavicmyths/')and n.endswith('.class'):check(int.from_bytes(z.read(n)[6:8],'big')==65,'Java21 '+n)
 for n in ['org/slavicmyths/finist/FinistArrow.class','org/slavicmyths/client/WolfStoryScreen.class','org/slavicmyths/client/FinistQuiverRenderer.class','data/slavicmyths/wolf_story/quests.json']:check(n in names,'Production missing '+n)
report={'version':version,'checks':checks,'jar':str(jar),'sha256':digest(jar),'preserved_baseline_files':preserved,'changed_baseline_files':changed,'audio':audio,'client_launches':0,'manual_gameplay_acceptance':'pending','parallel_hotfix_preserved':True}
OUT.mkdir(parents=True,exist_ok=True);(OUT/'resource-gates.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps({'checks':checks,'version':version,'sha256':report['sha256']}))
