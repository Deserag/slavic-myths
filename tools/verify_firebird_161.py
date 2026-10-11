from pathlib import Path
import json, zipfile, hashlib, subprocess
from PIL import Image
R=Path(__file__).resolve().parents[1];J=R/'build/libs/slavicmyths-1.6.1.jar';checks=0
def need(b,s):
 global checks;checks+=1
 if not b:raise AssertionError(s)
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
log=(R/'work/firebird/build.log').read_text(encoding='utf8',errors='replace');need('BUILD SUCCESSFUL' in log,'clean build');need('Firebird offline PASS: 200020' in log,'offline checks')
old=zipfile.ZipFile(R/'work/firebird/baseline-1.6.0.jar');new=zipfile.ZipFile(J)
changed={'org/slavicmyths/SlavicMyths.class','org/slavicmyths/registry/ModItemGroup.class','META-INF/neoforge.mods.toml','META-INF/MANIFEST.MF','META-INF/accesstransformer.cfg'}
counts={'classes':0,'resources':0}
for name in old.namelist():
 if name.endswith('/') or name in changed:continue
 need(name in new.namelist(),'preserve '+name)
 if name=='data/minecraft/tags/damage_type/bypasses_armor.json':
  before=json.loads(old.read(name));after=json.loads(new.read(name));need(before.get('replace',False)==after.get('replace',False) and all(v in after['values'] for v in before['values']),'preserve existing damage tag')
 elif name in ['assets/slavicmyths/lang/ru_ru.json','assets/slavicmyths/lang/en_us.json','assets/slavicmyths/sounds.json']:
  before=json.loads(old.read(name));after=json.loads(new.read(name));need(all(after.get(k)==v for k,v in before.items()),'additive '+name)
 else:need(old.read(name)==new.read(name),'unchanged '+name)
 counts['classes' if name.endswith('.class') else 'resources']+=1
for name in new.namelist():
 need(not ('FirebirdChecks' in name or '/smoke/' in name),'no test code '+name)
 if name.endswith('.json'):json.loads(new.read(name));checks+=1
 if name.endswith('.class'):need(int.from_bytes(new.read(name)[6:8],'big')==65,'Java21 '+name)
for name in ['firebird_feather','smoldering_feather','firebird_down']:
 p=R/f'src/main/resources/assets/slavicmyths/textures/item/{name}.png';need(Image.open(p).size==(64,64),'icon '+name);need(new.read(f'assets/slavicmyths/textures/item/{name}.png')==p.read_bytes(),'packaged icon')
manifest=json.loads((R/'docs/firebird/audio-manifest.json').read_text());need(len(manifest)==28,'28 variants')
for entry in manifest:
 p=R/'src/main/resources/assets/slavicmyths'/entry['file'];need(sha(p)==entry['sha256'],'audio hash')
 info=json.loads(subprocess.check_output(['ffprobe','-v','error','-show_streams','-show_format','-of','json',str(p)]));stream=info['streams'][0];need(stream['codec_name']=='vorbis' and stream['channels']==1 and stream['sample_rate']=='44100','audio encoding')
 pcm=subprocess.check_output(['ffmpeg','-v','error','-i',str(p),'-f','s16le','-']);need(len(pcm)>8000 and any(pcm),'decoded audible data')
mask=Image.open(R/'src/main/resources/assets/slavicmyths/textures/entity/firebird_emissive.png');need(mask.size==(256,256) and mask.getbbox()!=None,'real emissive atlas')
for p in (R/'src/main/resources').rglob('*'):
 if p.is_file() and ('firebird' in p.as_posix() or p.name.startswith('smoldering_feather')):
  name=p.relative_to(R/'src/main/resources').as_posix();need(name in new.namelist() and new.read(name)==p.read_bytes(),'current packaged resource '+name)
damage=json.loads(new.read('data/minecraft/tags/damage_type/bypasses_armor.json'));need('slavicmyths:firebird_retaliation' in damage['values'],'armor bypass tag')
need(not any('firebird' in n and '/recipe/' in n for n in new.namelist()),'no recipes');need(not any('firebird_spawn_egg' in n for n in new.namelist()),'no egg')
report={'version':'1.6.1','checks':checks,'clean_build':'PASS','offline_assertions':200020,'baseline_preserved':counts,'audio_variants':28,'jar':str(J),'sha256':sha(J),'client_launches':0,'server_launches':0,'runtime':'NOT TESTED'}
p=R/'docs/verification/firebird-1.6.1/resource-gates.json';p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report))
