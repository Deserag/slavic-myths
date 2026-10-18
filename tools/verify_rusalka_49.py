"""#49 scoped source/resources/production JAR checks. No game/client/server launch."""
from pathlib import Path
import ast,hashlib,io,json,struct,subprocess,sys,zipfile
from PIL import Image
R=Path(__file__).resolve().parents[1];BASE='b4fb788e';checks=[]
def check(ok,label):
 if not ok:raise AssertionError(label)
 checks.append(label)
def old(path):return subprocess.check_output(['git','show',f'{BASE}:{path}'],cwd=R)
def read(path):return (R/path).read_text(encoding='utf-8-sig')
def main():
 assets=R/'src/main/resources/assets/slavicmyths';texture=assets/'textures/entity/rusalka.png'
 with Image.open(texture) as im:
  im.load();check(im.size==(128,64) and im.mode=='RGB','Native atlas size / mode')
  check(len(im.getcolors(8192))==10,'Approved ten-color palette, no noise/rescaling')
 import rusalka_resources
 digest=hashlib.sha256(texture.read_bytes()).hexdigest();rusalka_resources.generate();check(hashlib.sha256(texture.read_bytes()).hexdigest()==digest,'Scoped texture generator byte reproducibility')
 # Exercise the exact legacy animals branch in isolation, with other resource writes stubbed.
 tree=ast.parse(read('tools/water_070.py'));generate=next(x for x in tree.body if isinstance(x,ast.FunctionDef) and x.name=='generate')
 animals=next(x for x in generate.body if isinstance(x,ast.Assign) and any(isinstance(t,ast.Name) and t.id=='animals' for t in x.targets))
 loop=next(x for x in generate.body if isinstance(x,ast.For) and isinstance(x.iter,ast.Call) and isinstance(x.iter.func,ast.Attribute) and isinstance(x.iter.func.value,ast.Name) and x.iter.func.value.id=='animals')
 calls=[];env=dict(R=R,A=R/'build/rusalka-49/legacy-isolation',D=R/'build/rusalka-49/legacy-data',names={},material=lambda path,*args:calls.append(path.stem),write=lambda *args:None)
 exec(compile(ast.fix_missing_locations(ast.Module(body=[animals,loop],type_ignores=[])),'water_070.py:animals','exec'),env)
 check('rusalka' not in calls and len(calls)==4,'Actual legacy animal loop delegates Rusalka and preserves other four')
 check(hashlib.sha256(texture.read_bytes()).hexdigest()==digest,'Legacy delegation preserves authored skin bytes')
 soundgen=next(x for x in tree.body if isinstance(x,ast.FunctionDef) and x.name=='sounds')
 who=next(x for x in soundgen.body if isinstance(x,ast.For) and isinstance(x.target,ast.Name) and x.target.id=='who')
 check(ast.literal_eval(who.iter)==('vodyanoy',),'Legacy generator cannot synthesize/rewrite Rusalka vocals')
 unchanged=['src/main/java/org/slavicmyths/client/WaterSpiritModel.java','src/main/java/org/slavicmyths/client/WaterSpiritRenderer.java','src/main/java/org/slavicmyths/water/WaterSpirit.java','src/main/java/org/slavicmyths/water/VodyanoyEntity.java','src/main/java/org/slavicmyths/registry/ModEntities.java','src/main/java/org/slavicmyths/registry/ModSounds.java','src/main/resources/assets/slavicmyths/sounds.json','gradle.properties']
 for path in unchanged:check((R/path).read_bytes().replace(b'\r\n',b'\n')==old(path).replace(b'\r\n',b'\n'),'Unchanged '+path)
 setup=read('src/main/java/org/slavicmyths/client/ClientSetup.java');check('ModEntities.RUSALKA.get(),RusalkaRenderer::new' in setup,'Dedicated Rusalka renderer wired')
 common=read('src/main/java/org/slavicmyths/water/RusalkaEntity.java');song=read('src/main/java/org/slavicmyths/water/RusalkaSong.java');sound=read('src/main/java/org/slavicmyths/client/RusalkaSound.java');model=read('src/main/java/org/slavicmyths/client/RusalkaModel.java')
 check('net.minecraft.client' not in common+song,'No client imports in common Rusalka code')
 check('SynchedEntityData.defineId' in common and 'visualAction()' in model and 'getTarget()' not in model,'Server synchronized action drives pose, no client target inference')
 check('Map<UUID,Blend>' in model and 'blends.size()>=64' in model,'Shared renderer cannot blend different entity poses')
 check('action==RusalkaActions.GRAB||action==RusalkaActions.BREAK' in model,'Actual-hit and interruption poses bypass delayed crossfade')
 check('hasChunkAt(p)||!level().getFluidState' in common and 'noCollision(this,body)' in common and 'move(MoverType.SELF,step)' in common and 'teleportTo(' not in common,'Static audit: loaded water, collision-aware physical dive, no teleport')
 check('nextPower=level().getGameTime()+100' in common and '.add(0,-.07,0)' in common,'Original grab impulse / cooldown')
 for token in ['amount*=.25F','amount*=.5F','strength*=.7','charm>=25','charm>=50','charm>=75','charm>=90','hasLineOfSight(p)']:
  check(token in song,'Charm baseline / LOS: '+token)
 for token in ['MAX_LOOPS=4','ACTIVE.containsKey(r.getUUID())','ACTIVE.size()>=MAX_LOOPS','ClientTickEvent.Post','sound.isStopped()','!manager.isActive(sound)','mc.level==singer.level()','!singer.isRemoved()','singer.singing()','distanceToSqr(mc.player)<=400','singer.hasLineOfSight(mc.player)','ACTIVE.remove(singer.getUUID(),this)']:
  check(token in sound,'Static loop lifecycle: '+token)
 # Decode original source-baseline without claiming a listening test or vocal acceptance.
 sys.path.insert(0,str(R/'.tools/audio-libs'));import soundfile as sf
 audio=[]
 for path in sorted((assets/'sounds/water').glob('rusalka_*.ogg')):
  data,rate=sf.read(path,dtype='float32');check(len(data)>rate/10,'OGG decodes '+path.name)
  rel=path.relative_to(R).as_posix();check(path.read_bytes()==old(rel),'Baseline OGG preserved '+path.name)
  audio.append(dict(file=path.name,seconds=round(len(data)/rate,3),rate=rate,peak=round(float(abs(data).max()),4),sha256=hashlib.sha256(path.read_bytes()).hexdigest(),status='LEGACY_NOT_ACCEPTED_ON_HEARING'))
 check(len(audio)==5,'All five legacy voices preserved')
 for path in sorted((R/'art/references/rusalka-2.0/audio').glob('*.wav')):
  data,rate=sf.read(path);check(len(data)>0,'Reference WAV decodes '+path.name)
 loopdata,rate=sf.read(assets/'sounds/water/rusalka_song.ogg');loopgap=float(abs(loopdata[-1]-loopdata[0]).max())
 check(loopgap<.02,'Legacy loop endpoint difference bounded (not listening acceptance)')
 jars=[p for p in (R/'build/libs').glob('*.jar') if not any(x in p.name for x in ['sources','javadoc'])];check(len(jars)==1,'One production JAR')
 with zipfile.ZipFile(jars[0]) as z:
  check(z.testzip() is None,'JAR CRC integrity');check('META-INF/neoforge.mods.toml' in z.namelist(),'NeoForge production metadata')
  for path in (R/'src/main/resources').rglob('*'):
   if path.is_file():
    rel=path.relative_to(R/'src/main/resources').as_posix()
    if rel=='META-INF/neoforge.mods.toml':continue
    check(z.read(rel)==path.read_bytes(),'JAR resource parity '+rel)
  check(not any('org/slavicmyths/verify/' in n or '/smoke/' in n for n in z.namelist()),'No offline/smoke fixture in production')
  for name in ['client/RusalkaModel','client/RusalkaRenderer','client/RusalkaSound','water/RusalkaEntity','water/RusalkaActions']:
   data=z.read('org/slavicmyths/'+name+'.class');check(struct.unpack('>H',data[6:8])[0]==65,'Java 21 class '+name)
 report=dict(status='PARTIAL_NEEDS_AUDIO_SOURCE',checks=len(checks),source_checks='STATIC, NOT LIVE CLIENT ACCEPTANCE',jar=jars[0].name,jar_sha256=hashlib.sha256(jars[0].read_bytes()).hexdigest(),atlas_sha256=digest,audio=audio,legacy_loop_endpoint_delta=loopgap,runtime_launches=0,manual_playback='NOT_RUN',vocal='NEEDS_AUDIO_SOURCE')
 out=R/'docs/implementation/rusalka-49/offline-gates.json';out.write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
if __name__=='__main__':main()
