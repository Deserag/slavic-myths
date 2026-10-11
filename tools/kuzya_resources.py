"""Deterministic original native rig, pixel atlas, manifest keyframes and supplied audio.
Does not overwrite existing translation/sound/tag keys or use the reference as texture.
"""
from pathlib import Path
import csv,json,random,math,shutil,subprocess,array
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1]
S=R/'docs/kuzya/spec/Slavic_Myths_1.6.4_Kuzya_Codex_TZ'
A=R/'src/main/resources/assets/slavicmyths'
J=R/'src/main/java/org/slavicmyths'
def write(p,s):
 p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s,encoding='utf8',newline='\n')
def dump(p,v):write(p,json.dumps(v,ensure_ascii=False,indent=2)+'\n')
def rows(n):return list(csv.DictReader((S/'manifests'/n).read_text(encoding='utf-8-sig').splitlines()))
def merge(p,v):
 old=json.loads(p.read_text(encoding='utf8'));old.update(v);dump(p,old)
clips=rows('animation_clips.csv');keys=rows('animation_keyframes.csv');events=rows('animation_events.csv')
sounds=json.loads((S/'resources_overlay/assets/slavicmyths/sounds.json').read_text(encoding='utf8'))
audio=json.loads((S/'manifests/AUDIO_FILES.json').read_text(encoding='utf8'));dialogue=json.loads((S/'manifests/dialogue_ru.json').read_text(encoding='utf8'))
film_manifest=R/'docs/kuzya/film-voices.json'
film=json.loads(film_manifest.read_text(encoding='utf8')) if film_manifest.exists() else None
if film:
 dialogue=film['voices'];audio=[{'file':v['file']} for v in dialogue]
 for v in dialogue:
  sounds['kuzya.'+v['line_id']]={'subtitle':'subtitles.slavicmyths.kuzya.'+v['line_id'],'sounds':[{'name':'slavicmyths:kuzya/film/'+v['line_id'],'stream':False}]}
 for cue,val in list(sounds.items()):
  if cue.startswith('kuzya.voice_') and cue[6:] not in [v['line_id'] for v in dialogue]:
   replacement=next(v for v in dialogue if v['line_id']==film['legacy_aliases'][cue[6:]])
   val['sounds']=sounds['kuzya.'+replacement['line_id']]['sounds']
merge(A/'sounds.json',sounds)
for p in (S/'resources_overlay/assets/slavicmyths/sounds/kuzya').rglob('*.ogg'):
 if film and 'voice_draft' in p.parts:continue
 target=A/'sounds/kuzya'/p.relative_to(S/'resources_overlay/assets/slavicmyths/sounds/kuzya');target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(p,target)
if film:
 for v in dialogue:
  target=A/'sounds/kuzya/film'/f"{v['line_id']}.ogg";target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(R/v['file'],target)
 # Only obsolete generated draft files, within this module's exact directory.
 draft=(A/'sounds/kuzya/voice_draft').resolve()
 assert draft.is_relative_to((A/'sounds/kuzya').resolve())
 for p in draft.glob('voice_*.ogg'):p.unlink()
english=['Who has come to visit us?','Close the door behind you.','A tidy home does not happen by itself.','Everything is out of place again.','I could use pancakes with sour cream!','The porridge has gone cold. Ah!','I like a full belly and a warm home.','Let me doze.','Let me sleep a little longer.','Are you staying long?','What are you looking around for?','A guest should know when to leave.','How much longer are you staying?','Strangers may not spend the night here.','All this walking about, and no rest.','Off you go; I have plenty to do.','Grandmother, where are the pies?','I am keeping the house tidy.','What do you need?','We can talk after lunch.']
sfx={'step_wood':('Кузя шлёпает по полу','Kuzya pads across the floor'),'stove_climb':('Кузя забирается на печь','Kuzya climbs the stove'),'cloth':('Рубаха Кузи шуршит','Kuzya’s shirt rustles'),'stove_settle':('Кузя устраивается на печи','Kuzya settles on the stove'),'sleep_breath':('Кузя дышит во сне','Kuzya breathes in his sleep'),'snore':('Кузя похрапывает','Kuzya snores'),'wake':('Кузя просыпается','Kuzya wakes'),'sit':('Кузя садится','Kuzya sits down'),'bowl':('Миска Кузи звякает','Kuzya’s bowl clinks'),'chew':('Кузя жуёт','Kuzya chews'),'swallow':('Кузя глотает','Kuzya swallows'),'sweep':('Кузя подметает','Kuzya sweeps'),'huff':('Кузя недовольно фыркает','Kuzya huffs'),'grumble':('Кузя ворчит','Kuzya grumbles'),'happy':('Кузя напевает','Kuzya hums'),'trunk':('Скрип дерева','Wood creaks'),'sleepy_sigh':('Кузя сонно вздыхает','Kuzya sighs sleepily')}
for li,lang in enumerate(['ru_ru','en_us']):
 v={'entity.slavicmyths.kuzya':('Домовёнок Кузя','Kuzya')[li], 'kuzya.repair.unloaded':('Подойдите к избушке: все её чанки должны быть загружены.','Approach the hut; all its chunks must be loaded.')[li],'kuzya.repair.exists':('Кузя уже живёт в избушке.','Kuzya already lives in the hut.')[li],'kuzya.repair.blocked':('Внутри нет безопасного свободного места для Кузи.','There is no safe free place for Kuzya inside.')[li],'kuzya.repair.done':('Кузя поселился в избушке: %s. Старые блоки сохранены.','Kuzya now lives in the hut at %s. Existing blocks were preserved.')[li]}
 for k,val in sounds.items():
  cue=k.removeprefix('kuzya.');v[val['subtitle']]=sfx.get(cue,('Кузя шуршит','Kuzya rustles'))[li]
 for i,line in enumerate(dialogue):v['subtitles.slavicmyths.kuzya.'+line['line_id']]=line['text_ru'] if li==0 else line.get('text_en',english[i])
 if film:
  for alias,active in film['legacy_aliases'].items():
   line=next(d for d in dialogue if d['line_id']==active)
   v['subtitles.slavicmyths.kuzya.'+alias]=line['text_ru'] if li==0 else line['text_en']
 merge(A/'lang'/f'{lang}.json',v)
for name in ['combat_protected','control_immune']:
 p=R/f'src/main/resources/data/slavicmyths/tags/entity_type/{name}.json';v=json.loads(p.read_text());
 if 'slavicmyths:kuzya' not in v['values']:v['values'].append('slavicmyths:kuzya')
 dump(p,v)
loot=[]
for row in rows('HUT_BARREL_LOOT.csv'):
 if row['item_category']!='vanilla':continue
 loot.append({'type':'minecraft:item','name':row['registry_id_or_constraint'],'weight':int(row['roll_chance_percent']),'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':int(row['min_count']),'max':int(row['max_count'])}}]})
dump(R/'src/main/resources/data/slavicmyths/loot_table/chests/baba_yaga_household_supplies.json',{'type':'minecraft:chest','pools':[{'rolls':{'type':'minecraft:uniform','min':4,'max':7},'entries':loot}]})
def floats(v):return '{'+','.join(f'{float(x):.5f}F' for x in v)+'}'
def strings(v):return '{'+','.join(json.dumps(x) for x in v)+'}'
names=[c['clip'] for c in clips];voice_names=[d['line_id'] for d in dialogue]
envelopes=[];voice_ticks=[]
for name in voice_names:
 p=(R if film else S)/next(a['file'] for a in audio if Path(a['file']).stem==name)
 raw=subprocess.check_output(['ffmpeg','-v','error','-i',str(p),'-f','s16le','-ac','1','-ar','16000','-'])
 samples=array.array('h',raw);voice_ticks.append(math.ceil(len(samples)/800));env=[]
 for start in range(0,len(samples),320):
  chunk=samples[start:start+320];env.append(math.sqrt(sum(v*v for v in chunk)/len(chunk)))
 peak=max(env) or 1;envelopes.append([min(1,v/peak) for v in env])
src=['package org.slavicmyths.kuzya;','/** Generated from the supplied frame and audio contracts by tools/kuzya_resources.py. */','public final class KuzyaAssets {',f'public static final String[] CLIPS={strings(names)};',f'public static final int[] TICKS={{{",".join(str(math.ceil(float(c["seconds"])*20)) for c in clips)}}};',f'public static final boolean[] LOOPS={{{",".join("true" if c["loop"]=="1" else "false" for c in clips)}}};',f'public static final String[] CUES={strings([k.removeprefix("kuzya.") for k in sounds])};',f'public static final String[] VOICES={strings(voice_names)};',f'public static final int[] VOICE_TICKS={{{",".join(map(str,voice_ticks))}}};','public record Event(int tick,String cue){}','public static final Event[][] EVENTS={']
for name in names:src.append('{'+','.join('new Event('+str(round(float(e['time_s'])*20))+','+json.dumps(e['sound_event'].removeprefix('kuzya.'))+')' for e in events if e['clip']==name)+'},')
src+=['};','public static final float[][][] KEYS={']
for name in names:src.append('{'+','.join(floats([row[k] for k in row if k not in ['clip','normalized_t']]) for row in keys if row['clip']==name)+'},')
voice_method='public static int[] voices(String category){return java.util.stream.IntStream.range(0,VOICES.length).filter(i->VOICES[i].startsWith(category+"_")).toArray();}'
if film:
 voice_method='public static int[] voices(String category){return switch(category){'+''.join('case '+json.dumps(c)+' -> new int[]{'+','.join(str(voice_names.index(n)) for n in ns)+'};' for c,ns in film['categories'].items())+'default -> new int[0];};}'
src+=['};','public static final float[][] ENVELOPES={'+','.join(floats(v) for v in envelopes)+'};','public static int index(String name){for(int i=0;i<CLIPS.length;i++)if(CLIPS[i].equals(name))return i;throw new IllegalArgumentException(name);}',voice_method,'private KuzyaAssets(){}','}']
write(J/'kuzya/KuzyaAssets.java','\n'.join(src)+'\n')
# Atlas islands use 4 texels per model pixel, padding and original stepped pixel materials.
im=Image.new('RGBA',(256,256),(38,27,25,255));draw=ImageDraw.Draw(im);rng=random.Random(1645)
tiles={'face':(2,2,78,58,['#211717','#382621','#483027','#654439']),'tunic':(2,66,62,118,['#763128','#843c2e','#9f4d38','#693028']),'sleeve':(66,66,110,110,['#763128','#843c2e','#9f4d38']),'pants':(114,66,150,98,['#28201e','#362820','#463329']),'shoe':(154,66,198,98,['#292523','#3c3028','#4e3a2b']),'hand':(82,2,114,34,['#211717','#382621','#654439']),'white':(202,66,234,98,['#d5cec0','#b8afa1']),'pupil':(202,102,234,134,['#191818','#433d37']),'nose':(118,2,138,22,['#191818','#211717']),'mouth':(142,2,162,22,['#63362f','#8a4b3b']),'food':(2,126,46,158,['#b67b39','#d4a35b','#8c582a']),'bowl':(50,126,98,158,['#624532','#8a6147']),'broom':(102,126,134,158,['#65503a','#927044'])}
tiles['hair_cap']=(164,2,254,62,['#261b19','#38261f','#4b3126','#69452f'])
for i in range(12):
 x=(i%6)*40+2;y=166+(i//6)*40;tiles[f'hair{i}']=(x,y,x+34,y+32,['#261b19','#38261f','#4b3126','#69452f'])
clean_palettes={'face':['#714c38','#79543e','#825b43','#6c4735'],'hand':['#714c38','#79543e','#825b43'],'tunic':['#913c32','#984236','#a34a3b','#88392f'],'sleeve':['#913c32','#984236','#a34a3b'],'pants':['#39312c','#3d352f','#443b33'],'shoe':['#574533','#604c37','#69523b'],'white':['#eee7d7','#e4ddcd'],'pupil':['#191818'],'nose':['#28201d','#302620'],'mouth':['#aa5a4b','#bd6d5a']}
for name,(x,y,x1,y1,palette) in tiles.items():
 palette=clean_palettes.get(name,['#39281f','#423025','#4b372a','#513c2d'] if name.startswith('hair') else palette)
 # Broad coherent material areas; no independent high-contrast noise per texel.
 draw.rectangle((x-2,y-2,x1+1,y1+1),fill=palette[0])
 for yy in range(y,y1,4):
  for xx in range(x,x1,4):
   shade=palette[min(len(palette)-1,1+(xx//12+yy//16)%max(1,len(palette)-1))] if (xx//4+yy//4)%7==0 else palette[0]
   draw.rectangle((xx,yy,min(xx+3,x1-1),min(yy+3,y1-1)),fill=shade)
 if name.startswith('hair'):
  for xx in range(x+4,x1-2,12):draw.rectangle((xx,y+2,xx+2,y1-3),fill=palette[1])
 if name in ['tunic','sleeve']:
  for xx in range(x+3,x1-3,7):draw.rectangle((xx,y1-6,xx+2,y1-5),fill='#c59f71')
  draw.rectangle((x+7,y+9,x+11,y+15),fill='#6a3c30');draw.line((x+7,y+9,x+11,y+9),fill='#c59f71',width=1)
  if name=='tunic':
   for cx,cy in [(18,82),(27,86)]:
    draw.rectangle((cx-1,cy-3,cx+1,cy+3),fill='#d8bf91')
    draw.rectangle((cx-3,cy-1,cx+3,cy+1),fill='#d8bf91')
im.save(A/'textures/entity/kuzya.png')
dump(R/'docs/kuzya/atlas.json',{'size':[256,256],'texels_per_model_pixel':4,'padding':2,'filter':'nearest','islands':tiles})
nodes=[];lines=['package org.slavicmyths.client;','import net.minecraft.client.model.geom.*;','import net.minecraft.client.model.geom.builders.*;','public final class KuzyaMesh {public static ModelPart create(){var mesh=new MeshDefinition();var root=mesh.getRoot().addOrReplaceChild("root",CubeListBuilder.create(),PartPose.offset(0,24,0));']
def f(x):return f'{float(x):.5f}F'
def node(name,parent,pos,boxes=(),rot=(0,0,0)):
 b='CubeListBuilder.create()'
 texscale=8 if name.startswith(('eye_','pupil_','eyelid_')) else 4
 posscale=8 if parent.startswith('eye_') else 4
 for x,y,z,w,h,d,mat in boxes:
  u,v,*_=tiles[mat];b+=f'.texOffs({u},{v}).addBox({",".join(f(t*texscale) for t in [x,y,z,w,h,d])})'
 lines.append(f'var {name}={parent}.addOrReplaceChild("{name}",{b},PartPose.offsetAndRotation({",".join(f(t*posscale) for t in pos)},{",".join(f(t) for t in rot)}));');nodes.append({'name':name,'parent':parent,'position':pos,'rotation':rot,'boxes':boxes,'texels_per_model_pixel':texscale})
node('pelvis','root',(0,-3.3,0),[(-1.7,-.7,-1.15,3.4,1.5,2.3,'pants')])
node('torso','pelvis',(0,-1.8,0),[(-2.15,-1.55,-1.4,4.3,3.1,2.8,'tunic'),(-2.35,.85,-1.55,4.7,.7,3.1,'tunic')])
node('neck','torso',(0,-1.85,0),[(-.95,-.4,-.9,1.9,.8,1.8,'face')])
node('head','neck',(0,-1.1,-.1),[(-2.5,-1.85,-1.825,5,3.7,3.65,'face'),(-2.35,-.5,-2.02,1.6,1.3,.3,'face'),(.75,-.5,-2.02,1.6,1.3,.3,'face')])
node('jaw','head',(0,1.05,-1.8),[(-.53,0,-.21,1.06,.22,.12,'mouth')])
node('nose','head',(0,.1,-2.1),[(-.4,-.2,-.28,.8,.45,.55,'nose')])
for side,sgn in [('L',1),('R',-1)]:
 node('eye_'+side,'head',(sgn*1.15,-.6,-1.96),[(-.68,-.61,-.16,1.36,1.22,.18,'face'),(-.56,-.5,-.22,1.12,1,.15,'white')])
 node('pupil_'+side,'eye_'+side,(.08,-.02,-.24),[(-.26,-.32,-.03,.52,.64,.06,'pupil'),(.08,-.2,-.05,.12,.12,.035,'white')])
 node('eyelid_'+side,'eye_'+side,(0,-.52,-.32),[(-.57,0,-.025,1.14,1.04,.06,'face')])
 node('brow_'+side,'head',(sgn*1.15,-1.35,-2.12),[(-.65,-.12,-.05,1.3,.24,.14,'hair0')])
 node('shoulder_'+side,'torso',(sgn*2.15,-1.1,0),[(-.72,0,-.9,1.44,1.7,1.8,'sleeve')],(0,0,-sgn*.08))
 node('elbow_'+side,'shoulder_'+side,(0,1.55,0),[(-.65,0,-.8,1.3,1.25,1.6,'sleeve')])
 node('hand_'+side,'elbow_'+side,(0,1.1,0),[(-.53,0,-.5,1.06,.75,1,'hand')]+[(x,.55,-.55,.25,.45,.6,'hand') for x in [-.48,-.12,.24]])
 node('hip_'+side,'pelvis',(sgn*1.08,.25,0),[(-.775,0,-.95,1.55,1.3,1.9,'pants')])
 node('knee_'+side,'hip_'+side,(0,1.15,0),[(-.73,0,-.88,1.46,1.35,1.76,'pants')])
 node('foot_'+side,'knee_'+side,(0,1.35,-.45),[(-.95,-.45,-1.33,1.9,1,2.65,'shoe')])
node('food_prop','hand_R',(0,.2,-.8),[(-.5,-.5,-.4,1,.6,.8,'food')])
node('bowl_prop','hand_L',(0,.25,-.8),[(-.7,-.45,-.7,1.4,.25,1.4,'bowl'),(-.7,-.2,-.7,.15,.35,1.4,'bowl'),(.55,-.2,-.7,.15,.35,1.4,'bowl'),(-.55,-.2,-.7,1.1,.35,.15,'bowl'),(-.55,-.2,.55,1.1,.35,.15,'bowl')])
node('broom_prop','hand_R',(0,.1,0),[(-.12,-3.5,-.1,.24,6.5,.2,'broom'),(-.85,2.8,-.3,1.7,1.1,.6,'broom')],(.28,0,.48))
node('hair_root','head',(0,-.3,0),[(-3.2,-2.2,-2.35,6.4,.9,4.7,'hair_cap')])
# 36 articulated clusters, each three staggered cubic fibers; eye openings remain clear.
for i in range(36):
 a=i*math.tau/28;top=i>=28
 if top: x=(i-31.5)*.72;z=math.sin(i)*1.6;y=-2.15+rng.uniform(-.3,.2);length=1.1
 else:x=math.cos(a)*3.18;z=math.sin(a)*2.5;y=-1.4;length=1.6+(.8 if z>0 else .2)+rng.random()*.45
 if z<-.9 and abs(x)<2.0:length=.7
 boxes=[(-.38,-.15,-.38,.76,length,.76,f'hair{i%12}'),(-.30,length-.2,-.3,.6,.55,.6,f'hair{(i+3)%12}'),(-.44,.1,-.51,.88,.5,.35,f'hair{(i+7)%12}')]
 node(f'hair_{i}','hair_root',(x,y,z),boxes,(rng.uniform(-.15,.15),rng.uniform(-.3,.3),rng.uniform(-.38,.38)))
lines+=['return LayerDefinition.create(mesh,256,256).bakeRoot().getChild("root");}','public static java.util.Map<String,ModelPart> index(ModelPart r){var m=new java.util.LinkedHashMap<String,ModelPart>();m.put("root",r);']
for n in nodes:lines.append(f'm.put("{n["name"]}",m.get("{n["parent"]}").getChild("{n["name"]}"));')
lines+=['return m;}','}']
write(J/'client/KuzyaMesh.java','\n'.join(lines)+'\n');dump(R/'docs/kuzya/rig.json',nodes)
print('Kuzya resources:',len(names),'clips,',len(keys),'frames,',len(nodes),'bones,',sum(len(n['boxes']) for n in nodes),'cubes,',len(list((A/'sounds/kuzya').rglob('*.ogg'))),'OGG')
