"""Authored pixel geometry, sprites and mono procedural sounds; no external audio samples."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,math,random,wave,array,subprocess,tempfile
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources';A=RES/'assets/slavicmyths'
def write(p,data):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def sprites():
 glob=Image.new('RGBA',(32,32));rng=random.Random('poloz_glob')
 for y in range(32):
  for x in range(32):glob.putpixel((x,y),rng.choice([(32,23,29,255),(175,35,51,255),(91,25,35,255),(227,69,80,255)]))
 (A/'textures/entity').mkdir(parents=True,exist_ok=True);glob.save(A/'textures/entity/poloz_glob.png')
 for name in ['mor_dust','eye_of_mor','ashen_heart','poloz_core','poloz_scale','mor_horn']:
  size=32 if name in ['mor_horn','poloz_core'] else 16;im=Image.new('RGBA',(size,size));d=ImageDraw.Draw(im)
  if name=='mor_dust':
   d.polygon([(2,12),(5,6),(7,9),(9,3),(13,9),(14,13),(8,15)],fill='#342127',outline='#20191F');d.line([(5,10),(8,11),(9,7),(11,12)],fill='#B51F30');d.point((9,5),fill='#ED4350')
  elif name=='eye_of_mor':
   d.ellipse((1,3,14,13),fill='#651923',outline='#20191F');d.ellipse((4,4,11,12),fill='#342127');d.rectangle((7,5,8,11),fill='#ED4350');d.point((4,5),fill='#D8C4A2')
  elif name=='ashen_heart':
   d.polygon([(2,3),(5,2),(8,5),(11,2),(14,4),(14,8),(8,14),(2,9)],fill='#20191F',outline='#651923');d.line([(8,5),(6,7),(9,9),(8,12)],fill='#E34550')
  elif name=='poloz_scale':
   d.polygon([(2,3),(8,1),(14,4),(13,9),(8,14),(2,10)],fill='#A18136',outline='#5D4C31');d.line([(4,4),(7,6),(6,9),(9,12)],fill='#AF2333');d.line([(6,2),(11,4)],fill='#C39F51')
  elif name=='poloz_core':
   d.polygon([(8,4),(23,2),(29,13),(23,27),(12,30),(3,20)],fill='#20171D',outline='#A18136');d.line([(21,5),(14,13),(19,18),(13,27)],fill='#E34550',width=2);d.line([(6,17),(11,9),(19,5)],fill='#C39F51',width=2)
  else:
   d.polygon([(3,27),(5,22),(10,20),(14,16),(17,11),(18,5),(25,2),(29,6),(26,15),(21,22),(13,27),(7,29)],fill='#C8B69B',outline='#5D4C31')
   d.polygon([(19,4),(25,2),(29,6),(24,8)],fill='#342127');d.line([(6,23),(9,26),(14,24),(20,19),(23,12)],fill='#5A392B',width=4)
   d.line([(15,17),(20,21)],fill='#20191F',width=3);d.line([(21,9),(27,12)],fill='#20191F',width=3);d.line([(11,23),(16,20),(18,14),(23,8)],fill='#B51F30')
   d.line([(15,21),(13,30)],fill='#651923',width=2);d.line([(17,20),(19,28)],fill='#20191F',width=2)
  p=A/f'textures/item/{name}.png';p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
  write(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/{name}'}})
 # Inventory uses the separate alpha icon; in-hand uses 14 solid stepped cuboids.
 elements=[]
 def box(start,end,uv):elements.append({'from':start,'to':end,'faces':{face:{'texture':'#horn','uv':uv} for face in ['up','down','north','south','east','west']}})
 box([2,0,6],[4,4,10],[0,0,2,4]);box([3,3,5],[6,7,11],[2,0,5,4]);box([5,6,5],[9,10,11],[5,0,9,4]);box([8,9,5],[12,14,11],[9,0,13,5]);
 for x in [7.5,11.5]:box([x,13,4.5],[x+1,17.5,11.5],[0,8,1,12.5])
 for z in [4.5,10.5]:box([8.5,13,z],[11.5,17.5,z+1],[0,8,3,12.5])
 box([7.5,11,4.5],[12.5,12,11.5],[8,8,13,9]);box([4.5,6,4.5],[9.5,7,11.5],[8,8,13,9]);box([4,3,4.5],[6,6,5],[4,8,6,11]);box([8,7,5],[9,12,5.4],[14,8,15,13]);box([6,1,5],[7,7,5.5],[14,8,15,14]);box([9,4,5],[10,8,5.5],[12,8,13,12])
 atlas=Image.new('RGBA',(32,32),'#C8B69B');ad=ImageDraw.Draw(atlas);ad.rectangle((0,16,14,31),fill='#D2BFA2');ad.rectangle((16,16,27,20),fill='#20191F');ad.rectangle((8,16,13,23),fill='#5A392B');ad.rectangle((28,16,31,31),fill='#AF2333');atlas.save(A/'textures/item/mor_horn_held.png')
 write(A/'models/item/mor_horn_held.json',{'textures':{'horn':'slavicmyths:item/mor_horn_held','particle':'slavicmyths:item/mor_horn_held'},'elements':elements,'display':{'firstperson_righthand':{'rotation':[0,-90,20],'translation':[1,1,0],'scale':[1,1,.8]},'thirdperson_righthand':{'rotation':[0,0,15],'translation':[0,1,0],'scale':[1,1,.8]}}})
 write(A/'models/item/mor_horn.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/mor_horn'},'overrides':[{'predicate':{'slavicmyths:held':1},'model':'slavicmyths:item/mor_horn_held'}]})
 for name in ['great_poloz_body','great_poloz_head']:
  im=Image.new('RGBA',(64,64));rng=random.Random(name)
  for y in range(64):
   for x in range(64):
    base=(161,129,54) if (x//4+y//4)%2==0 else (139,122,90);noise=rng.randrange(-18,19);im.putpixel((x,y),tuple(max(0,min(255,c+noise)) for c in base)+(255,))
  d=ImageDraw.Draw(im)
  for y in range(3,48,8):d.line([(0,y),(63,y)],fill='#5D4C31')
  for x in range(3,44,11):d.line([(x,0),(x+2,16),(x-1,24),(x+1,42)],fill='#AF2333')
  d.rectangle((48,48,63,63),fill='#AF2333');d.line([(49,50),(55,54),(52,59),(61,62)],fill='#E34550',width=2);d.rectangle((58,0,63,8),fill='#E34550');d.rectangle((58,12,63,18),fill='#D8C4A2')
  p=A/f'textures/entity/{name}.png';p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
def sounds():
 durations={'mor_horn_call':6,'mor_horn_charge':3,'poloz_distant_reply':3,'poloz_earth_rumble':3,'poloz_ambient_rumble':2,'poloz_surface_roar':2.3,'poloz_move_crush':.7,'poloz_armor_deflect':.5,'poloz_weakspot_hit':.8,'poloz_tail_slam':1.5,'poloz_mor_rain':2,'poloz_burrow':2,'poloz_emerge':1.4,'poloz_phase2_cracks':3,'poloz_phase3_roar':2,'poloz_minion_call':1.2,'poloz_death':5}
 data=json.loads((A/'sounds.json').read_text(encoding='utf-8'));rate=44100;target=A/'sounds/poloz';target.mkdir(parents=True,exist_ok=True)
 with tempfile.TemporaryDirectory() as tmp:
  for name,duration in durations.items():
   rng=random.Random(name);samples=array.array('h');phase=0;low=0
   for i in range(int(rate*duration)):
    t=i/rate;env=min(1,t/.1)*min(1,(duration-t)/.4)
    if name=='mor_horn_call':
     frequency=55 if t<1 else 110 if t<2.1 else 98 if t<3 else 82 if t<4 else 65+8*math.sin(t*4)
     env*=.25 if t<1 else 1 if t<5.6 else .25
    else:frequency=47+16*math.sin(t*2.3)+8*math.sin(t*5)
    phase+=2*math.pi*frequency/rate;low=low*.96+rng.uniform(-1,1)*.04
    signal=(math.sin(phase)*.42+math.sin(phase*2)*.24+math.sin(phase*3)*.14+math.sin(phase*4)*.07+low*.8)*env
    samples.append(round(math.tanh(signal*1.7)*.82*32767))
   wav=Path(tmp)/f'{name}.wav'
   with wave.open(str(wav),'wb') as w:w.setnchannels(1);w.setsampwidth(2);w.setframerate(rate);w.writeframes(samples.tobytes())
   subprocess.run(['ffmpeg','-v','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','5',str(target/f'{name}.ogg')],check=True)
   data[name]={'subtitle':f'subtitles.slavicmyths.{name}','sounds':[{'name':f'slavicmyths:poloz/{name}','attenuation_distance':16}]}
 write(A/'sounds.json',data)
def text():
 ru={'poloz.warning':'Великий Полоз разрушает постройки, деревья и землю. Последствия сохраняются. Повторите действие в течение 10 секунд и удерживайте рог 3 секунды для призыва.','poloz.irreversible':'ВНИМАНИЕ: разрушение мира необратимо','poloz.horn_lore':'Глас, пробуждающий Великого Полоза','poloz.repeat':'Повторный призыв: 4 моровых праха','poloz.first':'Первый призыв: без дополнительного праха','poloz.altar':'Для призыва используйте у действующего капища','poloz.busy':'Мировой мор уже пробуждён','poloz.griefing':'Включите mobGriefing для полного мирового события','poloz.cooldown':'До следующего призыва: %s сек.','poloz.no_spawn':'Не найдено подходящее место в 200–300 блоках. Ничего не потрачено.','poloz.awakened':'Хворь пробудилась. Великий Полоз пришёл в мир!','poloz.failed':'Призыв не удался; стоимость возвращена.','poloz.direction':'Полоз: %s, %s блока','poloz.defeated':'Полоз повержен','poloz.armor':'Чешуя непробиваема — бейте по шрамам, голове и хвосту','poloz.phase2':'Мор расколол чешую: теперь уязвимо всё тело','poloz.phase3':'Последняя ярость Великого Полоза!','poloz.cancel_warning':'Событие Полоза меняет мир необратимо. Отмена НЕ восстанавливает разрушения. Используйте только тестовый мир.','poloz.need_water':'Для водного теста встаньте у воды.'}
 en={'poloz.warning':'Great Poloz destroys buildings, trees and terrain permanently. Repeat within 10 seconds and hold the horn for 3 seconds to summon.','poloz.irreversible':'WARNING: world destruction is permanent','poloz.horn_lore':'The voice that awakens Great Poloz','poloz.repeat':'Repeat summon: 4 Mor Dust','poloz.first':'First summon: no additional dust','poloz.altar':'Use near an existing ritual altar','poloz.busy':'World corruption is already awake','poloz.griefing':'Enable mobGriefing for the full world event','poloz.cooldown':'Next summon in %s seconds','poloz.no_spawn':'No suitable location 200–300 blocks away; nothing spent.','poloz.awakened':'The blight awakens. Great Poloz has entered the world!','poloz.failed':'Summoning failed; the cost was refunded.','poloz.direction':'Poloz: %s, %s blocks','poloz.defeated':'Poloz is defeated','poloz.armor':'Armor is impenetrable: strike the scars, head and tail','poloz.phase2':'The blight shattered the scales: the whole body is vulnerable','poloz.phase3':'Great Poloz enters its final rage!','poloz.cancel_warning':'Poloz changes terrain permanently. Cancel does NOT restore destruction. Use a test world.','poloz.need_water':'Stand near water for the water test.'}
 names={'mor_horn':('Моровой рог','Mor Horn'),'mor_dust':('Моровой прах','Mor Dust'),'eye_of_mor':('Око мора','Eye of Mor'),'ashen_heart':('Пепельное сердце','Ashen Heart'),'poloz_core':('Сердцевина Полоза','Poloz Core'),'poloz_scale':('Чешуя Великого Полоза','Great Poloz Scale')}
 tales={'mor_block_trace':('След мора','Trace of Mor','Тёмные прожилки в земле опасны: контакт на 3 секунды ослабляет или замедляет, на 5 секунд начинает вредить. Поражение остаётся после победы; заменяйте блоки вручную.','Dark veins in the ground are dangerous: three seconds of contact weakens or slows, five seconds may hurt. Corruption remains after victory; replace blocks manually.'),'mor_horn':('Моровой рог','Mor Horn','Рецепт 3×3: Око мора / золото / Пепельное сердце; прах / козий рог / прах; прах / прах / прах. Око и сердце выпадают из высших Лиха и Овинника. У капища подтвердите необратимую катастрофу и удерживайте рог 3 секунды. Первый призыв без дополнительного праха, повторный стоит 4.','Recipe: eye / gold / heart; dust / goat horn / dust; dust / dust / dust. Eye and heart drop from higher Likho and Ovinnik. Confirm permanent destruction at an existing altar, then hold for three seconds. First call is free; repeats cost four dust.'),'great_poloz':('Великий Полоз','Great Poloz','Пробуждение: ищите Полоза по рогу. Сначала уязвимы голова, кончик хвоста и три шрама. На 70% здоровья чешуя раскалывается; уязвимо всё тело. После падения остаются руины и поражённые блоки. Сердцевина и чешуя — трофеи. Уральский сказовый образ хранителя земли; мор и бой — игровая интерпретация.','Awakening: follow the horn compass. Initially attack the head, tail tip and three scars. At 70% health armor cracks and the whole body becomes vulnerable. Ruins and corruption remain after victory. The core and scales are trophies. Inspired by the Ural tale guardian; corruption and combat are game interpretations.')}
 for lang,index,extra in [('ru_ru',0,ru),('en_us',1,en)]:
  p=A/f'lang/{lang}.json';data=json.loads(p.read_text(encoding='utf-8'));data.update(extra)
  for n,values in names.items():data[f'item.slavicmyths.{n}']=values[index]
  data['entity.slavicmyths.great_poloz']=['Великий Полоз','Great Poloz'][index];data['entity.slavicmyths.poloz_glob']=['Моровой сгусток','Mor Glob'][index]
  for i,direction in enumerate(['С','СВ','В','ЮВ','Ю','ЮЗ','З','СЗ'] if index==0 else ['N','NE','E','SE','S','SW','W','NW']):data[f'poloz.compass.{i}']=direction
  for name in ['mor_horn_call','mor_horn_charge','poloz_distant_reply','poloz_earth_rumble','poloz_ambient_rumble','poloz_surface_roar','poloz_move_crush','poloz_armor_deflect','poloz_weakspot_hit','poloz_tail_slam','poloz_mor_rain','poloz_burrow','poloz_emerge','poloz_phase2_cracks','poloz_phase3_roar','poloz_minion_call','poloz_death']:data['subtitles.slavicmyths.'+name]=('Моровой рог гудит' if name.startswith('mor_horn') else 'Великий Полоз: '+name.removeprefix('poloz_').replace('_',' ')) if index==0 else name.replace('_',' ').title()
  for n,values in tales.items():
   data[f'tales.entry.{n}.title']=values[index]
   for stage in ['known','rumor','seen','verified','discovered','studied']:data[f'tales.entry.{n}.{stage}']=values[index+2]
  write(p,data)
 for n in tales:write(RES/f'data/slavicmyths/tales/{n}.json',{'part':'creatures' if n=='great_poloz' else 'crafts','chapter':'forest' if n=='great_poloz' else 'materials','section':'inhabitants' if n=='great_poloz' else 'catalogue','title':f'tales.entry.{n}.title','icon':'slavicmyths:'+('poloz_core' if n=='great_poloz' else 'mor_horn' if n=='mor_horn' else 'mor_dirt'),'initial':0,'subjects':['recipe/slavicmyths:mor_horn'] if n=='mor_horn' else ['item/slavicmyths:poloz_core','item/slavicmyths:poloz_scale'] if n=='great_poloz' else [],'advancements':[],'links':[]})
 write(RES/'data/slavicmyths/recipe/mor_horn.json',{'type':'minecraft:crafting_shaped','pattern':['EGH','MRM','MMM'],'key':{'E':{'item':'slavicmyths:eye_of_mor'},'G':{'item':'minecraft:gold_ingot'},'H':{'item':'slavicmyths:ashen_heart'},'M':{'item':'slavicmyths:mor_dust'},'R':{'item':'minecraft:goat_horn'}},'result':{'id':'slavicmyths:mor_horn','count':1}})
if __name__=='__main__':
 sprites();sounds();text()
 import poloz_text_finish
 poloz_text_finish.finish()
 import poloz_full_assets
 poloz_full_assets.main()

# Preserve the user-authorized 1.9.1 summon rules after legacy text generation.
from summon_access_policy import apply as apply_summon_access
apply_summon_access()
