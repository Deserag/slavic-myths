"""Scoped offline batch verification; never executes Minecraft or its server."""
from pathlib import Path
import json,sys,hashlib,subprocess,zipfile
R=Path(__file__).resolve().parents[1];sys.path.insert(0,str(R/'.tools/audio-libs'));sys.path.insert(0,str(R/'tools'))
import soundfile as sf,numpy as np
from creature_audio_batch_60 import restore_selected
P=R/'art/audio/pack-a-60/voice-rework';A=R/'src/main/resources/assets/slavicmyths';checks=0
def check(ok,label):
 global checks;checks+=1
 if not ok:raise AssertionError(label)
def old(file):return subprocess.check_output(['git','show','037f80ac:'+file],cwd=R).decode('utf-8').replace('\r\n','\n')
def main():
 # New delegated mappings are independently checked against exact source/JAR contracts.
 from verify_audio_groups_61 import main as verify_project
 verify_project()
 from audio_groups_61 import EVENTS as PROJECT_EVENTS
 from audio_groups_61_plan import NEW as PROJECT_NEW
 rows=json.loads((P/'batch-selection.json').read_text(encoding='utf-8'));check(len(rows)==17,'14 performed vocals + 3 movement foley files')
 before={r['file']:hashlib.sha256((A/'sounds'/r['file']).read_bytes()).hexdigest() for r in rows};restore_selected();check(before=={r['file']:hashlib.sha256((A/'sounds'/r['file']).read_bytes()).hexdigest() for r in rows},'Entire selected batch regenerates byte-for-byte')
 events=json.loads((A/'sounds.json').read_text(encoding='utf-8'));pcm=[];sources=[]
 for row in rows:
  path=A/'sounds'/row['file'];x,sr=sf.read(path);info=sf.info(path)
  check(info.subtype=='VORBIS' and sr==44100 and x.ndim==1 and np.isfinite(x).all() and np.max(abs(x))<.95,'Full decode/mono/rate/peak '+row['event'])
  check(abs(x[0])<.03 and abs(x[-1])<.03,'Short edge fade '+row['event'])
  check(hashlib.sha256(path.read_bytes()).hexdigest()==row['sha256'],'Selected hash '+row['event']);pcm.append(hashlib.sha256(x.tobytes()).hexdigest())
  check(events[row['event']]['sounds'][0]['name']=='slavicmyths:'+row['file'][:-4],'Actual event -> recorded asset '+row['event'])
  check(not row['owner_approved'] and row['status']=='AGENT_SELECTED_UNDER_OWNER_DELEGATION','No invented hearing approval '+row['event'])
  if row['layer']=='recorded voice':sources.append(row['source_sha256'])
  if row['event'].startswith('skim.hurt'):check(len(x)/sr/.96<14/20,'Skim hurt fits pre-existing audio cooldown14')
  if row['event'].startswith('idolishche') and '.hurt' in row['event']:check(len(x)/sr<32/20,'Giant hurt fits pre-existing audio cooldown32')
  if row['event']=='ovinnik_hurt':check(len(x)/sr<.5,'Ovinnik hurt shorter than damage immunity window')
 check(len(set(pcm))==17 and len(set(sources))==14,'Distinct decoded outputs and distinct performed voice sources')
 check(events['grey_wolf.hurt']['sounds'][0]['name']=='minecraft:entity.wolf.hurt','Pain no longer shares growl alias')
 for key in ['pounce','dash','claw']:check(all('wolf.growl' not in r.get('name','') for r in events['grey_wolf.'+key]['sounds']),'Movement is foley, not another growl '+key)
 for event in ['grey_wolf.summon_howl','grey_wolf.pack_answer']:
  name=events[event]['sounds'][0]['name'].removeprefix('slavicmyths:');file='src/main/resources/assets/slavicmyths/sounds/'+name+'.ogg'
  check((R/file).read_bytes()==subprocess.check_output(['git','show','037f80ac:'+file],cwd=R),'Existing unique howl/pack recording unchanged '+event)
 file='src/main/java/org/slavicmyths/hunt/HuntMob.java';s=old(file)
 s=s.replace(' private void cue(SoundEvent sound){playSound(sound,.85F,.65F);}',' private void cue(SoundEvent sound){playSound(sound,.85F,.65F);}\n private long nextOvenVoiceTick;\n private void ovenVoice(boolean power){long now=level().getGameTime();if(power||now>=nextOvenVoiceTick){playSound(power?org.slavicmyths.registry.ModSounds.OVINNIK_POWER.get():org.slavicmyths.registry.ModSounds.OVINNIK_ANGRY.get(),power?.9F:.75F,1);nextOvenVoiceTick=now+(power?28:18);}}')
 s=s.replace('entityData.set(RAGE,true);cue(SoundEvents.RAVAGER_ROAR);','entityData.set(RAGE,true);if(oven())ovenVoice(true);else cue(SoundEvents.RAVAGER_ROAR);')
 s=s.replace('  cue(move==Move.HOWL?SoundEvents.RAVAGER_ROAR:move==Move.ASH?SoundEvents.FIRE_EXTINGUISH:oven()?SoundEvents.BLAZE_AMBIENT:SoundEvents.WOLF_GROWL);','  if(oven()){if(move==Move.ASH)playSound(SoundEvents.FIRE_EXTINGUISH,.85F,1);else ovenVoice(false);}else cue(move==Move.HOWL?SoundEvents.RAVAGER_ROAR:move==Move.ASH?SoundEvents.FIRE_EXTINGUISH:oven()?SoundEvents.BLAZE_AMBIENT:SoundEvents.WOLF_GROWL);')
 check((R/file).read_text(encoding='utf-8')==s,'Entire HuntMob unchanged except scoped oven audio; non-oven/Mor/AI/damage preserved')
 file='src/main/java/org/slavicmyths/entity/OvinnikEntity.java';s=old(file).replace(' @Override public boolean oven(){return true;}',' @Override public boolean oven(){return true;}\n @Override public float getVoicePitch(){return morState()==0?1:super.getVoicePitch();}')
 check((R/file).read_text(encoding='utf-8')==s,'Only normal Ovinnik voice pitch changed; Mor retains superclass pitch')
 for file in ['wolf/GreyWolf.java','bogatyr/IdolishcheEntity.java','skim/SkimBeastEntity.java']:
  path='src/main/java/org/slavicmyths/'+file;check((R/path).read_text(encoding='utf-8')==old(path),'Full AI/quest/HP/cooldown/NBT unchanged '+file)
 baseline=__import__('audio_resource_names_61').qualified(json.loads(old('src/main/resources/assets/slavicmyths/sounds.json')))
 for k,v in baseline.items():
  if not v.get('sounds') and k not in PROJECT_EVENTS:check(events[k]==v,'Quarantine remains disabled '+k)
 for lang in ['ru_ru','en_us']:
  d=json.loads((A/f'lang/{lang}.json').read_text(encoding='utf-8'))
  for row in rows:check(events[row['event']]['subtitle'] in d,'Subtitle exists '+lang+' '+row['event'])
  check(d['subtitles.slavicmyths.ovinnik_ambient'] in ['Овинник тяжело выдыхает','Ovinnik breathes harshly'],'Ambient subtitle now describes voice, not fire')
 version=next(line.split('=',1)[1].strip() for line in (R/'gradle.properties').read_text().splitlines() if line.startswith('mod_version='));jar=R/f'build/libs/slavicmyths-{version}.jar'
 with zipfile.ZipFile(jar) as z:
  check(z.testzip() is None,'JAR CRC')
  for row in rows:check(z.read('assets/slavicmyths/sounds/'+row['file'])==(A/'sounds'/row['file']).read_bytes(),'JAR selected bytes '+row['event'])
  for name in ['hunt/HuntMob','entity/OvinnikEntity']:
   key='org/slavicmyths/'+name+'.class';check(z.read(key)==(R/'build/classes/java/main'/key).read_bytes(),'Actual compiled audio dispatch '+name)
  check('assets/slavicmyths/audio_licenses/creature-batch-60.txt' in z.namelist(),'New source attribution included')
 report=dict(status='IN_PROGRESS',checks=checks,new_recorded_files=17,changed_existing_events=18,voice_takes=14,movement_foley=3,owner_accepted=False,runtime_launches=0,poly_mc_updated=hashlib.sha256(jar.read_bytes()).hexdigest()==json.loads((R/'docs/implementation/audio-60/POLYMC_INSTALLATION.json').read_text())['sha256'],jar_sha256=hashlib.sha256(jar.read_bytes()).hexdigest())
 (P/'batch-gates.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
if __name__=='__main__':main()
