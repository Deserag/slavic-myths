"""Current resources, preserved entities, actual sound decoding and production packaging. No client claim."""
from pathlib import Path
from PIL import Image
import hashlib,json,re,subprocess,zipfile
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';A=RES/'assets/slavicmyths';OUT=ROOT/'docs/verification/volkolak_1.4.3';OUT.mkdir(parents=True,exist_ok=True);checks=0
def check(ok,message):
 global checks
 checks+=1
 if not ok:raise AssertionError(message)
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
version=re.search(r'^mod_version=(.+)$',(ROOT/'gradle.properties').read_text(),re.M).group(1);jar=ROOT/f'build/libs/slavicmyths-{version}.jar'
check(version=='1.4.3','Expected final archive version')
base=json.loads((ROOT/'work/volkolak-143/baseline.json').read_text(encoding='utf-8'));preserved=[]
for name,digest in base.items():
 protected=any(name.startswith('src/main/java/org/slavicmyths/'+folder+'/') for folder in ['finist','hunt']) or any(name.startswith('src/main/java/org/slavicmyths/client/'+p) for p in ['GreyWolf','Finist','Hunt']) or any(s in name for s in ['/textures/entity/grey_wolf','/textures/item/finist_','/textures/armor/finist_','/models/item/finist_','/sounds/grey_wolf/']) or name=='src/main/java/org/slavicmyths/wolf/GreyWolf.java'
 if protected:check((ROOT/name).is_file() and sha(ROOT/name)==digest,'Protected baseline '+name);preserved.append(name)
for p in RES.rglob('*.json'):json.loads(p.read_text(encoding='utf-8'));check(True,'JSON '+str(p))
for p in RES.rglob('*.png'):
 with Image.open(p)as im:im.verify()
 check(True,'PNG '+str(p))
owned=json.loads((ROOT/'tools/volkolak_143_text.json').read_text(encoding='utf-8'));langs={}
for locale in ['ru_ru','en_us']:
 langs[locale]=json.loads((A/f'lang/{locale}.json').read_text(encoding='utf-8'))
 for key,value in owned[locale].items():check(langs[locale].get(key)==value and '???'not in value,'Localization '+key)
for name in ['cursed_volkolak_spawn_egg','cursed_volkolak_skull']:
 im=Image.open(A/f'textures/item/{name}.png').convert('RGBA');check(im.size==(32,32),'32px icon '+name);check(set(im.getchannel('A').getdata())=={0,255},'Hard pixel alpha '+name)
 model=json.loads((A/f'models/item/{name}.json').read_text());check(model['textures']['layer0']=='slavicmyths:item/'+name,'Icon model '+name)
for name in ['cursed_volkolak','cursed_volkolak_eyes']:check(Image.open(A/f'textures/entity/{name}.png').size==(256,256),'Entity atlas '+name)
check(Image.open(A/'textures/mob_effect/bad_blood.png').size==(32,32),'Effect icon')
for block in ['plach_trava','cursed_skull_cache']:
 for path in [A/f'blockstates/{block}.json',A/f'models/block/{block}.json',A/f'models/item/{block}.json',RES/f'data/slavicmyths/loot_table/blocks/{block}.json']:check(path.is_file(),'Block resource '+str(path))
catalog=json.loads((A/'sounds.json').read_text(encoding='utf-8'));audio=json.loads((ROOT/'work/volkolak-143/audio/manifest.json').read_text());check(len(audio)==28 and len({r['event']for r in audio})==23,'Real sound count')
for row in audio:
 p=A/'sounds/volkolak'/row['file'];check(p.is_file()and sha(p)==row['sha256'],'Sound bytes '+str(p));probe=json.loads(subprocess.check_output(['ffprobe','-v','error','-show_streams','-show_format','-of','json',str(p)]));s=probe['streams'][0];check(s['channels']==1 and s['codec_name']=='vorbis'and s['sample_rate']=='44100','Mono Vorbis '+str(p));check(abs(float(probe['format']['duration'])-row['duration'])<.06 and row['peak_dbfs']<=-1,'Sound measurement '+str(p));event=catalog['volkolak.'+row['event']];check(any(v['name']=='slavicmyths:volkolak/'+p.stem for v in event['sounds']),'Sound event '+str(p))
 for loc in langs:check(event['subtitle']in langs[loc],'Subtitle '+loc)
for folder in ['recipe','loot_table']:
 for p in (RES/f'data/slavicmyths/{folder}').rglob('*.json'):check(not any('slavicmyths:'+s in p.read_text(encoding='utf-8')for s in ['finist_bow','finist_quiver','cursed_volkolak_skull']),'Quest reward recipe/loot leak '+str(p))
for p in (ROOT/'src/main/java/org/slavicmyths/volkolak').glob('*.java'):check('net.minecraft.client'not in p.read_text(),'Client common dependency '+str(p))
with zipfile.ZipFile(jar)as z:
 names=set(z.namelist());check(z.testzip()is None,'JAR CRC');check(f'version="{version}"'in z.read('META-INF/neoforge.mods.toml').decode(),'Production version');check(not any('/verify/'in n or '/smoke/'in n or 'slavicmyths_volkolak/'in n or 'slavicmyths_wolf_story/'in n for n in names),'Test fixture leak')
 for p in RES.rglob('*'):
  if p.is_file():n=p.relative_to(RES).as_posix();check(n in names,'Missing resource '+n);check(n=='META-INF/neoforge.mods.toml'or z.read(n)==p.read_bytes(),'Stale resource '+n)
 for n in names:
  if n.endswith('.class'):check(int.from_bytes(z.read(n)[6:8],'big')==65,'Java 21 class '+n)
 for n in ['org/slavicmyths/volkolak/CursedVolkolak.class','org/slavicmyths/entity/VolkolakEntity.class','org/slavicmyths/client/CursedVolkolakModel.class','org/slavicmyths/client/CursedVolkolakRenderer.class','org/slavicmyths/equipment/mixin/BadBloodFoodMixin.class','org/slavicmyths/equipment/mixin/BadBloodInstantMixin.class']:check(n in names,'Required production class '+n)
report={'status':'PASS','checks':checks,'version':version,'jar':str(jar),'sha256':sha(jar),'protected_baseline_files':preserved,'audio':audio,'limitations':['No GUI client launch, survival playthrough, real relog/restart or measured MSPT.','Concurrent Chud changes are preserved; this gate does not claim Chud gameplay validation.']};(OUT/'resource_gates.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(f'PASS: {checks} resources/production checks; {len(preserved)} protected files; SHA256 {sha(jar)}')
