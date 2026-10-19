"""Deterministic native Bayun rig, authored CSV curves, pixel surfaces and original audio."""
from pathlib import Path
from PIL import Image,ImageDraw
import csv,json,math,random,shutil,collections
R=Path(__file__).resolve().parents[1];S=R/'docs/bayun/spec/Slavic_Myths_1.6_Final_Bayun_Book_Codex_TZ';A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data';J=R/'src/main/java/org/slavicmyths'
def write(p,s):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s,encoding='utf8')
def dump(p,j):write(p,json.dumps(j,ensure_ascii=False,indent=2)+'\n')
def merge(p,j):old=json.loads(p.read_text(encoding='utf8')) if p.exists() else {};old.update(j);dump(p,old)
rows=list(csv.DictReader((S/'09_ASSETS/animation/BAYUN_KEYFRAMES.csv').open(encoding='utf-8-sig')))
curves=collections.defaultdict(lambda:collections.defaultdict(list));durations={}
for r in rows:
 c=r['clip'];durations[c]=int(r['total_ticks']);curves[c][r['bone']].append([float(r[k]) for k in ['key_tick','pitch_deg','yaw_deg','roll_deg','translate_x_blocks','translate_y_blocks','translate_z_blocks']])
loops={'idle_breath','idle_sit','sleep_curl_loop','walk','hypnosis_song'}
events={'idle_breath':[(0,'breath',.18)],'idle_sit':[(0,'purr.soft',.24)],'sleep_curl_in':[(13,'settle',.3)],'sleep_curl_loop':[(0,'purr.soft',.2)],'wake_up':[(20,'mew.soft',.35)],'stretch_front':[(23,'stretch',.44)],'groom_paw':[(20,'groom',.2),(37,'groom',.2),(52,'groom',.2)],'walk':[(0,'pawstep',.25),(12,'pawstep',.25)],'loaf_rest':[(21,'settle',.3)],'eat_milk':[(22,'milk',.5),(33,'milk',.5),(44,'milk',.5),(55,'milk',.5)],'eat_fish':[(26,'fish',.43),(35,'fish',.43),(45,'fish',.43)],'pet_rub':[(9,'purr.happy',.55)],'threat_warn':[(13,'warn',.82)],'hypnosis_song':[(4,'song.intro',.72)],'pounce_kill':[(9,'pounce',.85),(18,'claw',.9)],'recover':[(20,'breath',.28)]}
code=['package org.slavicmyths.bayun;','public final class BayunAnimations {','public record Sound(int tick,String cue,float volume){}','public record Curve(String bone,float[][] keys){}','public static Curve[] curves(String clip){return switch(clip){']
for c,bones in curves.items():
 v=[]
 for bone,keys in bones.items():v.append('new Curve('+json.dumps(bone)+',new float[][]{'+','.join('{'+','.join(str(x)+'F' for x in k)+'}' for k in keys)+'})')
 code.append('case '+json.dumps(c)+' -> new Curve[]{'+','.join(v)+'};')
code+=['default -> new Curve[0];};}','public static int duration(String clip){return switch(clip){']
code += ['case '+json.dumps(c)+' -> '+str(n)+';' for c,n in durations.items()]
code+=['default -> 80;};}','public static boolean loop(String c){return java.util.Set.of('+','.join(json.dumps(c) for c in sorted(loops))+').contains(c);}','public static Sound[] sounds(String c){return switch(c){']
for c,items in events.items():code.append('case '+json.dumps(c)+' -> new Sound[]{'+','.join(f'new Sound({t},{json.dumps(cue)},{vol}F)' for t,cue,vol in items)+'};')
code+=['default -> new Sound[0];};}','}'];write(J/'bayun/BayunAnimations.java','\n'.join(code)+'\n')
files=['bayun_purr_soft','bayun_purr_happy','bayun_mew_soft','bayun_mew_annoyed','bayun_breath','bayun_settle','bayun_stretch_exhale','bayun_groom','bayun_lap_milk','bayun_chew_fish','bayun_pawstep','bayun_spell_warning','bayun_sleep_song','bayun_song_loop','bayun_pounce','bayun_claw_swoosh','bayun_book_page','book_open','book_discovery']
cues=['purr.soft','purr.happy','mew.soft','mew.annoyed','breath','settle','stretch','groom','milk','fish','pawstep','warn','song.intro','song.loop','pounce','claw','book.page','book.open','book.discovery'];sounds={}
for cue,name in zip(cues,files):
 dst=A/'sounds/bayun'/f'{name}.ogg';dst.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(S/'09_ASSETS/audio'/f'{name}.ogg',dst);sounds['bayun.'+cue]={'subtitle':'subtitles.slavicmyths.bayun.'+cue,'sounds':[{'name':'slavicmyths:bayun/'+name,'stream':cue.startswith('song.'),'attenuation_distance':16 if cue.startswith('song.') or cue=='warn' else 10}]}
merge(A/'sounds.json',sounds)
ru={'entity.slavicmyths.kot_bayun':'Кот Баюн','item.slavicmyths.kot_bayun_spawn_egg':'Яйцо призыва Кота Баюна','tales.received':'Вы получили Книгу сказаний. В ней собраны предания о мире и ваши открытия.','tales.title':'Книга сказаний','tales.back':'Назад','tales.next':'Далее','tales.contents':'Оглавление','tales.count':'Открыто записей: %s из %s','tales.page':'Страница %s из %s','tales.locked':'Сведения пока неизвестны. Подробности откроются после настоящей встречи.','tales.search':'Поиск по книге','tales.map':'Карта слухов','tales.no_hint':'Пока нет достоверных сведений о местонахождении.','tales.stale':'Сведения устарели. Попросите новый слух.','tales.other_dimension':'Этот слух относится к другому измерению.','tales.schematic':'Схематичная область — рельеф не показан','tales.region':'Возможное место','tales.your_position':'Ваша позиция','tales.north':'Север','tales.quest.none':'Сейчас нет принятого поручения Яги.','tales.quest.progress':'Поручений Яги завершено: %s из 3','tales.done':'Получено','tales.available':'Доступно','tales.unearned':'Ещё не получено','tales.stage.1':'Услышано','tales.stage.2':'Встречено','tales.stage.3':'Изучено','tales.stage.4':'Проверено','tales.defeated':'Победа подтверждена','bayun.notice':'Кот Баюн пристально смотрит на вас. Угостите его или отойдите.','bayun.warning':'Баюн выгнул спину. Уходите, пока он не запел!','bayun.song':'Кот Баюн начал свою чарующую песнь!','bayun.charmed':'Чарующий голос тянет вас всё ближе…','bayun.released':'Песня стихла. Вы снова владеете собой.','bayun.milk':'Кот Баюн лакает молоко. Подношение принято.','bayun.fish':'Баюн принял рыбу и прикрыл янтарные глаза.','bayun.invalid':'Кот даже не посмотрел на подношение.','bayun.far':'Подойдите поближе, но не забывайте об осторожности.','bayun.fed':'Кот уже сыт. Не докучайте ему бесконечными подношениями.','bayun.pet':'Баюн довольно жмурится и трётся о вашу ладонь.','bayun.pet_wait':'Баюн уже принял вашу ласку. Дайте ему отдохнуть.','bayun.danger':'Сейчас лучше не протягивать к нему руку.','bayun.site.created':'Лесное пристанище Баюна создано.','bayun.site.blocked':'Здесь нет безопасного места для пристанища Баюна.','bayun.dialogue.greeting':'Садись подальше от когтей да слушай, путник.','bayun.dialogue.lore':'Расскажи предание','bayun.dialogue.who':'Как меня зовут?','bayun.dialogue.where':'Что ты знаешь о здешних местах?','bayun.dialogue.bye':'Попрощаться','bayun.dialogue.identity':'Твоё имя — %s. Я запомнил тебя. Я — Кот Баюн. Одни мои сказки усыпляют тревоги, другие — самих путников.','bayun.dialogue.no_lore':'Пока у меня нет для тебя нового сказания.','bayun.dialogue.places':'Мои корни и камни настоящие. Я отметил в твоей книге широкую область вокруг этого места — точной дороги не обещаю.','bayun.trust':'Доверие: %s — %s','bayun.bond.0':'Незнакомец','bayun.bond.1':'Знакомый','bayun.bond.2':'Доверие','bayun.bond.3':'Друг Баюна','death.attack.slavicmyths.bayun_claws':'%1$s был растерзан Котом Баюном.','death.attack.slavicmyths.bayun_claws.player':'%1$s был растерзан Котом Баюном, спасаясь от %2$s.'}
en={'entity.slavicmyths.kot_bayun':'Kot Bayun','item.slavicmyths.kot_bayun_spawn_egg':'Kot Bayun Spawn Egg','tales.received':'You received the Book of Tales. It records legends and your discoveries.','tales.title':'Book of Tales','tales.back':'Back','tales.next':'Next','tales.contents':'Contents','tales.count':'Entries discovered: %s of %s','tales.page':'Page %s of %s','tales.locked':'These details remain unknown. A real encounter will reveal more.','tales.search':'Search the book','tales.map':'Rumor map','tales.no_hint':'No reliable location is known yet.','tales.stale':'This information is stale. Ask for another rumor.','tales.other_dimension':'This rumor belongs to another dimension.','tales.schematic':'Schematic region — no terrain is shown','tales.region':'Possible location','tales.your_position':'Your position','tales.north':'North','tales.quest.none':'No Yaga task is currently accepted.','tales.quest.progress':'Yaga tasks completed: %s of 3','tales.done':'Earned','tales.available':'Available','tales.unearned':'Not yet earned','tales.stage.1':'Heard','tales.stage.2':'Encountered','tales.stage.3':'Studied','tales.stage.4':'Verified','tales.defeated':'Victory confirmed','bayun.notice':'Kot Bayun watches you. Offer a gift or step away.','bayun.warning':'Bayun arches his back. Leave before he sings!','bayun.song':'Kot Bayun begins his enchanting song!','bayun.charmed':'The enchanting voice draws you closer…','bayun.released':'The song fades. You control yourself again.','bayun.milk':'Kot Bayun laps milk. Your offering is accepted.','bayun.fish':'Bayun accepts the fish and closes his amber eyes.','bayun.invalid':'The cat ignores your offering.','bayun.far':'Come closer, but stay cautious.','bayun.fed':'The cat is full. Do not keep bothering him with gifts.','bayun.pet':'Bayun rubs his cheek against your hand.','bayun.pet_wait':'Bayun has already accepted your affection. Let him rest.','bayun.danger':'Keep your hand away for now.','bayun.site.created':'Bayun\'s forest nook was created.','bayun.site.blocked':'This terrain cannot safely support Bayun\'s nook.','bayun.dialogue.greeting':'Sit beyond my claws and listen, traveler.','bayun.dialogue.lore':'Tell me a legend','bayun.dialogue.who':'What is my name?','bayun.dialogue.where':'What do you know of this place?','bayun.dialogue.bye':'Say goodbye','bayun.dialogue.identity':'Your name is %s. I remember you. I am Kot Bayun. Some tales soothe worries; others put travelers to sleep.','bayun.dialogue.no_lore':'I have no new legend for you yet.','bayun.dialogue.places':'These roots and stones are real. I marked a broad area around my home in your book; I promise no exact route.','bayun.trust':'Trust: %s — %s','bayun.bond.0':'Stranger','bayun.bond.1':'Familiar','bayun.bond.2':'Trusted','bayun.bond.3':'Friend of Bayun','death.attack.slavicmyths.bayun_claws':'%1$s was torn apart by Kot Bayun.','death.attack.slavicmyths.bayun_claws.player':'%1$s was torn apart by Kot Bayun while escaping %2$s.'}
parts=[('tales','Предания','Tales'),('creatures','Существа и духи','Creatures and spirits'),('people','Люди и поселения','People and settlements'),('places','Места и тайны','Places and secrets'),('crafts','Ремёсла и еда','Crafts and food'),('nature','Звери и растения','Animals and plants'),('quests','Поручения и поиски','Tasks and searches'),('deeds','Деяния и достижения','Deeds and achievements')]
for key,a,b in parts:ru['tales.part.'+key]=a;en['tales.part.'+key]=b
for i,(a,b) in enumerate(zip(['север','северо-восток','восток','юго-восток','юг','юго-запад','запад','северо-запад'],['north','northeast','east','southeast','south','southwest','west','northwest'])):ru['tales.direction.'+str(i)]=a;en['tales.direction.'+str(i)]=b
for i,(a,b) in enumerate(zip(['поблизости','неподалёку','вдалеке'],['nearby','not far away','far away'])):ru['tales.distance.'+str(i)]=a;en['tales.distance.'+str(i)]=b
subtitle_ru=['Кот Баюн тихо мурлычет','Кот Баюн довольно мурлычет','Кот Баюн мяукает','Кот Баюн недовольно рычит','Кот Баюн дышит','Кот Баюн укладывается','Кот Баюн потягивается','Кот Баюн умывается','Кот Баюн лакает молоко','Кот Баюн ест рыбу','Мягкие шаги Кота Баюна','Кот Баюн предупреждает','Кот Баюн начинает петь','Чарующая песнь Кота Баюна','Кот Баюн прыгает','Когти Кота Баюна рассекают воздух','Шелестят страницы Книги сказаний','Открывается Книга сказаний','Открылось новое сказание']
subtitle_en=['Bayun purrs softly','Bayun purrs happily','Bayun mews','Bayun growls','Bayun breathes','Bayun settles','Bayun stretches','Bayun grooms','Bayun laps milk','Bayun eats fish','Bayun pads softly','Bayun warns','Bayun begins singing','Bayun\'s enchanting song','Bayun pounces','Bayun\'s claws slash','Tale pages rustle','Book of Tales opens','A tale is discovered']
for c,a,b in zip(cues,subtitle_ru,subtitle_en):ru['subtitles.slavicmyths.bayun.'+c]=a;en['subtitles.slavicmyths.bayun.'+c]=b
merge(A/'lang/ru_ru.json',ru);merge(A/'lang/en_us.json',en)
dump(D/'slavicmyths/damage_type/bayun_claws.json',{'message_id':'slavicmyths.bayun_claws','scaling':'never','exhaustion':0.1})
for tag in ['bypasses_armor']:
 p=D/'minecraft/tags/damage_type'/f'{tag}.json';n=json.loads(p.read_text(encoding='utf8'));v='slavicmyths:bayun_claws'
 if v not in n['values']:n['values'].append(v)
 dump(p,n)
dump(D/'slavicmyths/worldgen/configured_feature/bayun_nook.json',{'type':'slavicmyths:bayun_nook','config':{}})
dump(D/'slavicmyths/worldgen/placed_feature/bayun_nook.json',{'feature':'slavicmyths:bayun_nook','placement':[{'type':'minecraft:rarity_filter','chance':3200},{'type':'minecraft:in_square'},{'type':'minecraft:heightmap','heightmap':'MOTION_BLOCKING_NO_LEAVES'},{'type':'minecraft:biome'}]})
dump(D/'slavicmyths/neoforge/biome_modifier/bayun_nook.json',{'type':'neoforge:add_features','biomes':'#minecraft:is_forest','features':'slavicmyths:bayun_nook','step':'surface_structures'})
for tag in ['combat_protected','control_immune']:
 p=D/'slavicmyths/tags/entity_type'/f'{tag}.json';n=json.loads(p.read_text(encoding='utf8'))
 if 'slavicmyths:kot_bayun' not in n['values']:n['values'].append('slavicmyths:kot_bayun')
 dump(p,n)
dump(A/'models/item/kot_bayun_spawn_egg.json',{'parent':'minecraft:item/template_spawn_egg'})
# UV surfaces: body 16 model pixels/block; head subtree 64 texels/block with scale .25.
# Overlap is intentional only for shared fur materials. Each eye has a distinct 12x8 island.
skin=Image.new('RGBA',(256,256),'#302E36');d=ImageDraw.Draw(skin)
for rect,color in [((0,0,155,79),'#5B585E'),((160,0,255,95),'#302E36'),((0,96,127,159),'#44424B'),((128,96,255,175),'#302E36'),((0,160,159,223),'#44424B'),((160,176,255,239),'#5B585E'),((0,240,255,255),'#BDBBC0')]:d.rectangle(rect,fill=color)
for y in range(0,240,8):
 for x in range(0,256,8):
  if y in range(80,96):continue
  c=skin.getpixel((x,y));shade=7 if (x//8+y//8)%4==0 else -3
  d.rectangle((x+1,y+1,x+5,y+3),fill=tuple(max(0,min(255,v+shade)) for v in c[:3])+(255,))
for x in [0,32]:
 d.rectangle((x,80,x+23,95),fill='#44424B');d.rectangle((x+1,81,x+12,88),fill='#EAA23F');d.rectangle((x+6,81,x+7,88),fill='#202027');d.rectangle((x+2,81,x+3,82),fill='#F9D98C')
d.rectangle((64,80,95,95),fill='#684A34');d.rectangle((96,80,127,95),fill='#AE9E8E');d.rectangle((128,80,159,95),fill='#A78A82')
(A/'textures/entity/kot_bayun').mkdir(parents=True,exist_ok=True);skin.save(A/'textures/entity/kot_bayun/skin.png')
nodes=[]
def node(name,parent,pivot,boxes=(),dense=False):
 nodes.append({'name':name,'parent':parent,'pivot':pivot,'unit':64 if dense else 16,'boxes':boxes})
def box(x,y,z,w,h,depth,uv=(160,0)):return [x,y,z,w,h,depth,*uv]
node('root',None,(0,0,0));node('spine','root',(0,.90,0),[box(-.43,-.29,-.60,.86,.67,1.30),box(-.46,-.23,-.54,.92,.5,1.18),box(-.36,-.35,-.48,.72,.63,1.10)])
node('chest','spine',(0,.88,-.42),[box(-.42,-.30,-.22,.84,.71,.50,(0,96))]);node('ruff','chest',(0,1.02,-.58))
node('head','chest',(0,1.28,-.83),[box(-.32,-.22,-.28,.64,.5,.56,(0,0)),box(-.32,-.18,-.36,.27,.27,.14,(96,80)),box(.05,-.18,-.36,.27,.27,.14,(96,80)),box(-.10,-.11,-.4,.20,.11,.07,(64,80))],True)
node('jaw','head',(0,1.14,-1.04),[box(-.18,-.065,-.15,.36,.13,.27,(96,80))],True)
for side,sign in [('L',-1),('R',1)]:
 node('ear_'+side,'head',(sign*.29,1.5,-.86),[box(-.10,0,-.06,.20,.10,.14,(128,80)),box(-.07,.10,-.035,.14,.10,.10,(128,80)),box(-.04,.20,-.015,.08,.06,.065,(0,0))],True)
 node('eye_'+side,'head',(sign*.17,1.37,-1.114),[box(-.094,-.0625,-.012,.1875,.125,.025,(0 if side=='L' else 32,80))],True)
 node('eyelid_'+side,'eye_'+side,(sign*.17,1.37,-1.132),[box(-.094,-.0625,-.006,.1875,.125,.014,(0,96))],True)
 for j in range(3):node('whisker_'+side+str(j),'head',(sign*.19,1.19+j*.045,-1.18),[box(0 if sign>0 else -.35,0,0,.35,.009,.014,(0,240))],True)
 node('shoulder_'+side,'chest',(sign*.35,.92,-.43),[box(-.13,-.36,-.13,.26,.39,.28,(0,96))]);node('foreleg_'+side,'shoulder_'+side,(sign*.35,.55,-.46),[box(-.10,-.31,-.10,.20,.34,.22,(0,96))]);node('paw_F'+side,'foreleg_'+side,(sign*.35,.14,-.60),[box(-.13,-.065,-.16,.26,.13,.29,(0,96))]);node('claws_'+side,'paw_F'+side,(sign*.35,.14,-.75))
 for j in range(3):node('claw_'+side+str(j),'claws_'+side,(sign*.35+(j-1)*.075,.10,-.75),[box(-.015,-.02,-.10,.03,.045,.12,(0,240)),box(-.014,-.035,-.12,.028,.03,.045,(0,240))])
 node('haunch_'+side,'spine',(sign*.38,.77,.47),[box(-.19,-.34,-.20,.38,.51,.42,(128,96))]);node('hindleg_'+side,'haunch_'+side,(sign*.38,.43,.50),[box(-.11,-.25,-.11,.22,.3,.23,(0,96))]);node('paw_B'+side,'hindleg_'+side,(sign*.38,.12,.58),[box(-.14,-.065,-.18,.28,.13,.32,(0,96))])
for i,length in enumerate([.22,.22,.20,.19,.16]):
 z=.66+sum([.22,.22,.20,.19,.16][:i]);size=[.25,.29,.34,.28,.20][i];node('tail_0'+str(i+1),'spine' if i==0 else 'tail_0'+str(i),(0,.98,z),[box(-size/2,-size/2,0,size,size,length+.04,(0,160))])
rng=random.Random(169)
for i in range(100):
 angle=(i%20)/20*math.tau;layer=i//20
 x=math.cos(angle)*(.48+(layer%3)*.045);y=.76+layer*.11;z=-.61+math.sin(angle)*.16
 if i>=60:x=math.cos(angle)*.49;y=.5+(i%3)*.12;z=-.35+(i-60)/40*1.05
 node('fur_'+str(i),'ruff' if i<60 else 'spine',(x,y,z),[box(-.055,-.16,-.04,.11,.29,.10,(160,176)),box(-.035,-.20,-.02,.07,.09,.065,(160,176))])
mapped={n['name']:n for n in nodes};lines=['package org.slavicmyths.client;','import net.minecraft.client.model.geom.*;','import net.minecraft.client.model.geom.builders.*;','public final class BayunMesh {','public static ModelPart create(){MeshDefinition mesh=new MeshDefinition();PartDefinition root=mesh.getRoot();']
for n in nodes:
 parent=mapped.get(n['parent']);unit=parent['unit'] if parent else 16;px,py,pz=n['pivot'];bx,by,bz=parent['pivot'] if parent else (0,0,0);cubes='CubeListBuilder.create()'
 for x,y,z,w,h,dep,u,v in n['boxes']:
  f=n['unit'];cubes+=f'.texOffs({u},{v}).addBox({x*f:.4f}F,{-(y+h)*f:.4f}F,{z*f:.4f}F,{w*f:.4f}F,{h*f:.4f}F,{dep*f:.4f}F)'
 y=24 if n['parent'] is None else -(py-by)*unit
 lines.append(f'PartDefinition n{len(lines)} = '+('root' if not parent else 'p_'+parent['name'])+f'.addOrReplaceChild("{n["name"]}",{cubes},PartPose.offset({(px-bx)*unit:.4f}F,{y:.4f}F,{(pz-bz)*unit:.4f}F));')
 lines[-1]=lines[-1].replace('n'+str(len(lines)-1),'p_'+n['name'],1)
lines+=['return LayerDefinition.create(mesh,256,256).bakeRoot().getChild("root");}','public static java.util.Map<String,ModelPart> index(ModelPart root){var m=new java.util.LinkedHashMap<String,ModelPart>();m.put("root",root);']
for n in nodes[1:]:lines.append(f'm.put("{n["name"]}",m.get("{n["parent"]}").getChild("{n["name"]}"));')
lines+=['return m;}','}'];write(J/'client/BayunMesh.java','\n'.join(lines)+'\n');dump(R/'docs/bayun/rig.json',nodes)
# Closed book inventory sprite with spine, clasp and a large tree; no borrowed vanilla sprite.
im=Image.new('RGBA',(64,64));d=ImageDraw.Draw(im);d.polygon([(10,7),(49,3),(56,9),(56,53),(17,61),(8,55)],fill='#201916');d.polygon([(16,10),(50,6),(53,10),(53,51),(17,57)],fill='#3A2926');d.polygon([(8,10),(15,10),(16,57),(10,54)],fill='#704A36');d.line([(19,14),(47,11),(48,48),(20,53),(19,14)],fill='#A87C3E',width=2);d.rectangle((43,27,54,35),fill='#A87C3E');d.rectangle((47,29,54,32),fill='#E3C47F');d.line([(32,44),(32,23)],fill='#C49D61',width=4)
for end in [(21,23),(23,16),(31,15),(42,16),(44,22)]:d.line([(32,31),end],fill='#C49D61',width=3);d.rectangle((end[0]-2,end[1]-2,end[0]+2,end[1]+2),fill='#E1BE75')
for x,y in [(21,15),(45,13),(21,49),(46,46)]:d.rectangle((x,y,x+3,y+3),fill='#7B2C22')
im.save(A/'textures/item/lore_book.png');dump(A/'models/item/lore_book.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/lore_book'}})
print('Bayun assets:',len(nodes),'bones,',sum(len(n['boxes']) for n in nodes),'cubes,',len(curves),'clips,',len(rows),'keys,',len(files),'original OGG')

# Preserve the delegated recorded animal identity after legacy resource generation.
from audio_groups_61 import restore_selected as restore_project_audio
restore_project_audio()
