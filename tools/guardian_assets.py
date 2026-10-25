"""Reference-guided original blocky rigs, UV pixels, matching fragments and original audio."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,math,random,wave,struct,subprocess,shutil
from morozko_assets import Rig,write,merge
import karachun_assets as export
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data/slavicmyths';O=R/'docs/verification/guardians-1.9.4'
FIRE={'wood':['65432D','3B2622','815C3A'],'bark':['241C19','3B2622','4F3329'],'mor':['721D25','211E29','C1392B'],'cloth':['A64B38','653024','D6C099'],'rune':['D6C099','AA803D','FFCB55'],'glow':['FFB43B','ED641F','FFF1B0'],'eye':['FA6732','C1392B','FFCB55'],'metal':['584533','33291F','A8884B']}
WATER={'skin':['C8D8D6','8CABB0','E0E5DC'],'cloth':['B3CCD0','779EAD','E0E5DC'],'hair':['1D444B','28585D','236C7A'],'weed':['526746','354839','798250'],'rune':['B49D65','897543','D6C099'],'water':['39B6DC','236C7A','9AE8FF'],'mor':['58192F','211E29','BD2544'],'morcloth':['58192F','211E29','F04B62'],'morskin':['58192F','211E29','BD2544'],'glow':['9AE8FF','39B6DC','E0E5DC'],'red_eye':['F04B62','BD2544','FFBAC9']}
def fire():
 r=Rig();r.bone('pelvis',None,0,22,0);r.bone('torso','pelvis',0,43,0)
 r.box('pelvis',0,20,0,22,4,13,'bark')
 # Real cavity: four wood rails and back logs, not a flat chest emblem.
 for x in [-8,8]:r.box('torso',x,24,0,6,20,14,'wood')
 r.box('torso',0,24,0,12,5,14,'bark');r.box('torso',0,40,0,12,5,14,'wood')
 for x in [-7,-2,3,8]:r.box('torso',x,24,6,4.5,20,3,'bark')
 for x in [-6,6]:r.box('torso',x,29,-7.5,2,11,2,'rune')
 r.bone('core','torso',0,35,-4)
 for x,y,z,w,h,d in [(0,30,-4,7,3,6),(-2,33,-3,3,5,3),(2,34,-4,3,7,3),(0,36,-6,2,6,2)]:r.box('core',x,y,z,w,h,d,'glow')
 r.bone('head','torso',0,47,0);r.box('head',0,44,-1,12,12,11,'wood');r.box('head',0,48,-7,2,6,3,'wood');r.box('head',0,46,-7,5,1,1,'bark')
 for sign in [-1,1]:
  r.box('head',sign*3.4,51,-6.8,3.5,1.6,1,'bark');r.box('head',sign*3.4,51.3,-7.4,2.3,.7,.5,'eye');r.box('head',sign*4.5,47,-6.7,2.5,3,1.7,'wood')
  tag='left' if sign>0 else 'right';mat='mor' if sign>0 else 'wood'
  r.bone('leg_'+tag,'pelvis',sign*6,22,0);r.box('leg_'+tag,sign*6,5,0,9,17,9,mat);r.box('leg_'+tag,sign*6,0,-1,11,6,12,'bark')
  r.bone('arm_'+tag,'torso',sign*14,43,0);r.box('arm_'+tag,sign*14,28,0,9,15,10,mat)
  for layer in range(3):r.box('arm_'+tag,sign*(12+layer),40-layer*2,0,11,3,13,'bark')
  r.bone('forearm_'+tag,'arm_'+tag,sign*14,29,0);r.box('forearm_'+tag,sign*14,16,-1,8,12,10,mat);r.box('forearm_'+tag,sign*14,17,-1,10,3,12,'bark')
  r.bone('hand_'+tag,'forearm_'+tag,sign*14,17,-1);r.box('hand_'+tag,sign*14,10,-2,10,7,10,mat)
  for finger in range(3):r.box('hand_'+tag,sign*14-3+finger*3,8,-4,2.5,5,4,'wood')
  for yy in [8,15,27,34,42]:r.box('leg_'+tag if yy<20 else 'arm_'+tag,sign*(6 if yy<20 else 15),yy,-5,.9,4,.6,'mor' if sign>0 else 'rune')
  for i in range(4):r.box('head',sign*(4+i*2),55+i%2,1,2,2+i%3,3,'wood')
  r.box('head',sign*7,56,1,2,4 if sign<0 else 3,2,'wood')
  r.bone('apron_'+tag,'pelvis',sign*3,23,-7.2);r.box('apron_'+tag,sign*3,12,-7.2,6,11,1.2,'cloth');r.box('apron_'+tag,sign*3,10,-7.4,6,3,1.3,'rune')
  for j in range(4):r.box('apron_'+tag,sign*3+(-1 if j%2 else 1),14+j*1.7,-8,.8,1,.3,'rune')
 for i in range(12):r.box('torso',-8+i%4*5,26+i//4*5,7.8,.8,4,.5,'mor' if i%3 else 'rune')
 return r
def water():
 r=Rig();r.bone('pelvis',None,0,20,0);r.bone('torso','pelvis',0,34,0)
 for sign in [-1,1]:
  tag='left' if sign>0 else 'right';skin='morskin' if sign>0 else 'skin';cloth='morcloth' if sign>0 else 'cloth'
  r.box('torso',sign*3,20,0,6,15,7,cloth)
  r.bone('leg_'+tag,'pelvis',sign*3,20,0);r.box('leg_'+tag,sign*3,2,0,4,18,4,cloth)
  r.bone('arm_'+tag,'torso',sign*8,34,0);r.box('arm_'+tag,sign*8,23,0,4,11,5,skin)
  r.bone('forearm_'+tag,'arm_'+tag,sign*8,24,0);r.box('forearm_'+tag,sign*8,14,-1,4,10,5,skin);r.box('forearm_'+tag,sign*8,12,-1,4,3,5,cloth)
  r.bone('hand_'+tag,'forearm_'+tag,sign*8,14,-1);r.box('hand_'+tag,sign*8,10,-1,3,4,4,skin)
  for j in range(4):
   n='skirt_'+tag+'_'+str(j);r.bone(n,'pelvis',sign*(4+j*.7),20,0);r.box(n,sign*(4+j*.7),3+j*2,0,5,18-j*3,8+j,cloth);r.box(n,sign*(4+j*.7),2+j*2,-5.3,5,2,1,'rune')
  n='stream_'+tag;r.bone(n,'pelvis',sign*7,13,0)
  for j in range(5):r.box(n,sign*(7+j*.7),j*.6,-2+j,2,6+j,3,'water' if sign<0 else 'mor')
  r.box('torso',sign*5,31,-4,2,3,1,'rune')
  parent='hand_'+tag
  for j in range(5):
   n='whip_'+tag+'_'+str(j);r.bone(n,parent,sign*8,12,-1-j*16);r.box(n,sign*8,11,-9-j*16,2,2,16,'water' if sign<0 else 'mor');parent=n
 r.bone('head','torso',0,39,-.5)
 r.box('head',-3,36,-1,6,11,10,'skin');r.box('head',3,36,-1,6,11,10,'morskin');r.box('head',0,39,-6.5,1.5,3,1.5,'skin')
 for sign in [-1,1]:r.box('head',sign*3,42,-6.3,1.5,1,.5,'glow' if sign<0 else 'red_eye');r.box('head',sign*4.5,37,-6,2,2,1,'skin' if sign<0 else 'morskin')
 r.box('head',0,38,-6.3,3,.6,.4,'hair')
 for side in ['left','right','back']:
  sx=1 if side=='left' else -1;n='hair_'+side;r.bone(n,'head',sx*5 if side!='back' else 0,47,3)
  for j in range(7 if side=='back' else 4):
   x=-6+j*2 if side=='back' else sx*(5+j*.8);z=5.3 if side=='back' else -5+j*3
   r.box(n,x,15+j%3*2,z,2,30-j%3*2,2.5,'hair' if x<1 else 'mor')
   r.box(n,x,18+j%2*4,z-1.3,.8,20-j%3*3,.5,'weed');r.box(n,x,40-j%2,z-1.5,1.1,2,.6,'rune')
 for j in range(8):r.box('head',-6+j*1.7,46,-2+(j%2),2,2,5,'hair');r.box('head',-6+j*1.7,47,-4+(j%3),1,1+(j%2),1,'rune')
 return r
def emit(r,name,cls,palette,clean):
 export.PAL=palette;export.O=O;export.emit(r,name+'_corrupted',cls,512)
 pure=dict(palette)
 if clean=='fire':pure['mor']=palette['wood'];pure['eye']=palette['glow'];pure['bark']=['65432D','4F3329','815C3A']
 else:
  pure['mor']=palette['water'];pure['morskin']=palette['skin'];pure['morcloth']=palette['cloth'];pure['red_eye']=palette['glow']
 export.PAL=pure;export.emit(r,name+'_cleansed',cls,512)
 for state in ['corrupted','cleansed']:
  texture=A/f'textures/entity/{name}_{state}.png';im=Image.open(texture);glow=Image.open(A/f'textures/entity/{name}_{state}_glow.png')
  for b in r.boxes:
   u,v,w,h=b['uv']
   if b['material'] in ['eye','red_eye']:glow.paste(im.crop((u,v,u+w,v+h)),(u,v))
   if clean=='water' and b['material']=='water':
    for yy in range(v,v+h):
     for xx in range(u,u+w):im.putpixel((xx,yy),im.getpixel((xx,yy))[:3]+(175,))
  im.save(texture);glow.save(A/f'textures/entity/{name}_{state}_glow.png')
 if clean=='fire':
  im=Image.open(A/f'textures/entity/{name}_corrupted.png');glow=Image.open(A/f'textures/entity/{name}_corrupted_glow.png')
  for b in r.boxes:
   if b['material']!='glow':continue
   u,v,w,h=b['uv']
   for yy in range(v,v+h):
    for xx in range(u,u+w):im.putpixel((xx,yy),(250,70 if (xx+yy)%5 else 160,50,255));glow.putpixel((xx,yy),im.getpixel((xx,yy)))
  im.save(A/f'textures/entity/{name}_charged.png');glow.save(A/f'textures/entity/{name}_charged_glow.png')
 for stage in [1,2]:
  infected=Image.open(A/f'textures/entity/{name}_corrupted.png');cleaned=Image.open(A/f'textures/entity/{name}_cleansed.png');ig=Image.open(A/f'textures/entity/{name}_corrupted_glow.png');cg=Image.open(A/f'textures/entity/{name}_cleansed_glow.png')
  for b in r.boxes:
   if b['bone'] in ['torso','head','core'] or stage==2 and not b['bone'].startswith('hair_'):
    u,v,w,h=b['uv'];infected.paste(cleaned.crop((u,v,u+w,v+h)),(u,v));ig.paste(cg.crop((u,v,u+w,v+h)),(u,v))
  infected.save(A/f'textures/entity/{name}_purifying_{stage}.png');ig.save(A/f'textures/entity/{name}_purifying_{stage}_glow.png')
def icons():
 for spring in [False,True]:
  name='mor_water_amulet_fragment' if spring else 'mor_fire_amulet_fragment';im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
  # Shared central zigzag seam, opposite halves of one medallion.
  seam=[(16,3),(14,7),(17,11),(15,16),(18,20),(15,25),(16,29)]
  outer=[(16,3),(8,4),(3,10),(2,19),(7,26),(16,29)] if not spring else [(16,3),(24,5),(29,11),(29,21),(23,27),(16,29)]
  points=outer+list(reversed(seam));base='#8B6335' if not spring else '#9BAAB2';edge='#D6AC55' if not spring else '#DDE8E9';d.polygon(points,fill=base);d.line(outer,fill=edge,width=2);d.line(seam,fill=edge,width=1)
  if spring:d.polygon([(23,9),(20,16),(21,20),(24,21),(26,18)],fill='#39B6DC');d.line([(20,24),(23,22),(27,24)],fill='#9AE8FF',width=1)
  else:d.polygon([(9,10),(6,16),(7,20),(12,22),(14,17),(11,16),(11,12)],fill='#FFB43B');d.rectangle((8,18,10,20),fill='#FFF1B0')
  im.save(A/f'textures/item/{name}.png');write(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+name}})
def blocks():
 for name,valve in [('guardian_relic_pedestal',False),('spring_valve',True)]:
  for lit in [False,True]:
   im=Image.new('RGBA',(32,32),'#52616A');d=ImageDraw.Draw(im)
   for y in range(0,32,4):d.line((0,y,31,y),fill='#77858A')
   color='#39B6DC' if lit else '#897543';d.rectangle((7,7,24,24),outline=color,width=2);d.line((16,6,16,26),fill=color,width=3)
   suffix='_lit' if lit else '';im.save(A/f'textures/block/{name}{suffix}.png')
   elements=[{'from':[1,0,1],'to':[15,3,15]},{'from':[4,3,4],'to':[12,11,12]},{'from':[2,11,2],'to':[14,14,14]}] if not valve else [{'from':[3,0,3],'to':[13,4,13]},{'from':[6,4,6],'to':[10,13,10]},{'from':[2,11,5],'to':[14,14,11]}]
   for e in elements:e['faces']={f:{'texture':'#all'} for f in ['north','south','east','west','up','down']}
   write(A/f'models/block/{name}{suffix}.json',{'textures':{'all':'slavicmyths:block/'+name+suffix,'particle':'slavicmyths:block/'+name+suffix},'elements':elements})
  write(A/f'blockstates/{name}.json',{'variants':{f'facing={f},lit={str(lit).lower()}':{'model':'slavicmyths:block/'+name+('_lit' if lit else ''),'y':i*90} for i,f in enumerate(['north','east','south','west']) for lit in [False,True]}})
  write(A/f'models/item/{name}.json',{'parent':'slavicmyths:block/'+name})
  write(D/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'minecraft:stone_bricks'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
SOUNDS=['fire_guardian_idle','fire_guardian_awaken','fire_guardian_step','fire_guardian_hurt','fire_guardian_swing','fire_pulse_charge','fire_pulse_burst','fire_lockdown_warning','fire_lockdown_ignite','fire_shield_break','fire_guardian_ground_impact','fire_guardian_purify','spring_keeper_idle','spring_keeper_emerge','spring_keeper_step','spring_keeper_hurt','spring_whip_windup','spring_whip_impact','spring_whirlpool_warning','spring_whirlpool_loop','spring_prison_form','spring_prison_break','spring_flood_rise','spring_heal_begin','spring_heal_interrupt','spring_valve_switch','spring_keeper_purify','amulet_fragment_claim']
SOUND_RU=['Тлеет древнее дерево','Пробуждается обугленный идол','Тяжёлая деревянная поступь','Треск поражённого дерева','Размах тяжёлой руки','Разгорается грудной очаг','Вспышка осквернённого пламени','Предупреждение огненной полосы','Зажигается огненная полоса','Рассыпается огненный щит','Ладони ударяют о камень','Очищается защитное пламя','Тихое течение источника','Поднимается хранительница','Водяная поступь','Надлом водной стражницы','Натягивается водяная плеть','Хлещет водяная плеть','Закручивается водоворот','Вода вращается в воронке','Смыкается водяная темница','Рассыпается водяная темница','Поднимается разлив','Источник питает хранительницу','Прерывается исцеляющий поток','Поворачивается древний затвор','Очищается хранительница','Получена часть оберега']
def sounds():
 ff=shutil.which('ffmpeg');assert ff
 work=R/'.tools/guardian-audio';work.mkdir(parents=True,exist_ok=True);events={}
 for idx,name in enumerate(SOUNDS):
  duration=.3 if 'step' in name or 'hurt' in name else 1.2 if 'purify' in name else .8;rate=22050;rng=random.Random(194+idx);samples=[];water=name.startswith('spring');last=0
  for j in range(int(rate*duration)):
   t=j/rate;noise=rng.uniform(-1,1);last=last*.75+noise*.25;env=math.sin(math.pi*j/(rate*duration))**2
   tone=math.sin(2*math.pi*(140+idx*9+(80*t if 'warning' in name else 0))*t)
   v=(last*.35+tone*.1+math.sin(2*math.pi*(440+idx*8)*t)*.04) if water else (noise*.06+tone*.16+math.sin(2*math.pi*70*t)*.1)
   samples.append(struct.pack('<h',int(max(-.8,min(.8,v*env))*32767)))
  wav=work/(name+'.wav')
  with wave.open(str(wav),'wb') as out:out.setnchannels(1);out.setsampwidth(2);out.setframerate(rate);out.writeframes(b''.join(samples))
  target=A/f'sounds/guardians/{name}.ogg';target.parent.mkdir(parents=True,exist_ok=True)
  subprocess.run([ff,'-y','-loglevel','error','-i',str(wav),'-c:a','libvorbis','-q:a','3','-flags:a','+bitexact','-fflags','+bitexact',str(target)],check=True)
  events['guardians.'+name]={'subtitle':'subtitles.slavicmyths.guardians.'+name,'sounds':[{'name':'slavicmyths:guardians/'+name}]}
 merge(A/'sounds.json',events)
def language():
 common={
 'fire_boss':('Страж погасшего пламени','Guardian of the Extinguished Flame'),'water_boss':('Осквернённая хранительница источника','Defiled Spring Keeper'),
 'open_fire_hint':('Зажги центральную чашу Священным углём из обычных сундуков. После этого откроется дверь к хранителю.', 'Light the central brazier with Sacred Ember from the sanctuary chests, then return to this door.'),'open_water_hint':('Очистите оба заражённых бассейна.','Purify both tainted pools.'),
 'purify_fire_hint':('Страж склоняется. Коснитесь очага в зале или центральной чаши.','The guardian kneels. Touch the ritual hearth or central brazier.'),'purify_water_hint':('Хранительница ждёт очищения. Коснитесь чистого источника.','The keeper awaits purification. Touch the clean spring.'),
 'fire_cleansed':('Пламя очищено. Заберите свою деталь на постаменте.','The flame is purified. Claim your fragment at the pedestal.'),'water_cleansed':('Источник вновь чист. Заберите свою деталь на постаменте.','The spring is pure again. Claim your fragment at the pedestal.'),
 'pedestal_hint':('Постамент отдаёт деталь лишь после очищения хранителя.','The pedestal offers its fragment after the guardian is purified.'),
 'already_claimed':('Эта часть оберега уже получена вами.','You have already claimed this fragment type.'),'inventory_full':('Освободите место: право на деталь сохранено.','Make room in your inventory; your claim remains available.'),
 'blocked_spawn':('Место хранителя занято. Освободите пространство в зале.','The guardian anchor is blocked. Clear space in the chamber.'),
 'return_guardian':('Вернитесь в зал хранителя, чтобы завершить очищение.','Return to the guardian chamber to finish purification.'),
 'valve_idle':('Затвор действует во время боя с хранительницей.','This valve operates during the keeper encounter.'),
 'valve_cooldown':('Потоки перекрыты на 20 секунд.','The channels are closed for 20 seconds.'),'valve_turn':('Поворачиваю затвор…','Turning the valve…'),
 'healing_hint':('Закройте оба затвора: один лишь ослабляет поток!','Close both valves; one only weakens the stream!')}
 names={'mor_fire_amulet_fragment':('Пламенная часть защитного амулета','Flame Fragment of the Protective Amulet'),'mor_water_amulet_fragment':('Водная часть защитного амулета','Water Fragment of the Protective Amulet')}
 descriptions=[('Часть древнего оберега. Несёт тепло защитного пламени. Нужна в будущей истории Змеиной горы.','Part of an ancient ward, holding protective warmth. Needed in the future Serpent Mountain story.'),('Часть древнего оберега. Сохраняет память очищающего источника. Нужна в будущей истории Змеиной горы.','Part of an ancient ward, remembering the cleansing spring. Needed in the future Serpent Mountain story.')]
 for locale,index in [('ru_ru',0),('en_us',1)]:
  values={'guardians194.'+k:v[index] for k,v in common.items()}
  for i,(name,v) in enumerate(names.items()):values['item.slavicmyths.'+name]=v[index];values['item.slavicmyths.'+name+'.desc']=descriptions[i][index]
  values.update({'entity.slavicmyths.extinguished_flame_guardian':common['fire_boss'][index],'entity.slavicmyths.defiled_spring_keeper':common['water_boss'][index],'entity.slavicmyths.guardian_water_echo':('Водное воплощение','Water Manifestation')[index],'entity.slavicmyths.guardian_water_prison':('Водяная темница','Water Prison')[index],'block.slavicmyths.spring_valve':('Древний затвор','Ancient Valve')[index],'block.slavicmyths.guardian_relic_pedestal':('Реликтовый постамент','Relic Pedestal')[index]})
  for number,name in enumerate(SOUNDS):values['subtitles.slavicmyths.guardians.'+name]=SOUND_RU[number] if index==0 else name.replace('_',' ').capitalize()
  values['death.attack.guardian_fire']=('%1$s сгорел в осквернённом пламени' if index==0 else '%1$s burned in corrupted guardian fire')
  values['death.attack.guardian_fire.player']=('%1$s сгорел в пламени %2$s' if index==0 else '%1$s burned in %2$s\'s guardian fire')
  for water in [False,True]:
   tale='cleansed_spring_keeper' if water else 'cleansed_flame_guardian';title=common['water_boss' if water else 'fire_boss'][index];body=('Древняя хранительница защищала чистый ключ. Мор расколол её облик. Два затвора разрывают исцеляющий поток. После очищения она остаётся беречь источник.' if water else 'Резной идол охранял очистительный огонь. Мор погасил его обет. Во время огненного заточения ищите безопасные полосы, после боя коснитесь очага.') if index==0 else ('The ancient keeper guarded the spring. Corruption divided her form. Both valves interrupt healing; purification restores her duty.' if water else 'A carved idol guarded the cleansing flame. Seek safe strips during lockdown, then touch the hearth to restore its vow.')
   values['tales.entry.'+tale+'.title']=title
   for stage in ['rumor','known','studied','completed','legend','observed']:values['tales.entry.'+tale+'.'+stage]=body
   adv='claimed_water_fragment' if water else 'claimed_fire_fragment';values['advancements.slavicmyths.'+adv+'.title']=('Источник вновь чист' if water else 'Пламя очищено') if index==0 else ('The Spring Is Pure' if water else 'Flame Purified');values['advancements.slavicmyths.'+adv+'.description']=('Получите личную половину древнего оберега. Обе детали составят один оберег в будущей истории; рецепт пока недоступен.' if index==0 else 'Claim your half of the ancient ward. Both halves belong together; its recipe comes in a future story.')
  merge(A/f'lang/{locale}.json',values)
  fragment_lore={'tales.entry.mor_guardian_fragments.title':('Две части древнего оберега','Two Parts of an Ancient Ward')[index]}
  body=('Защитное пламя и чистый источник хранят две сопряжённые половины одного оберега. Они не связаны с двумя зимними реликвиями. Готовый оберег потребует будущего знания после заражённого Лиха; сейчас его рецепт недоступен.' if index==0 else 'Protective flame and the clean spring hold matching halves of one ward. They are separate from the winter relics. A future revelation after the blighted Likho is still needed; there is no assembled ward recipe yet.')
  for stage in ['rumor','known','studied','completed','legend','observed']:fragment_lore['tales.entry.mor_guardian_fragments.'+stage]=body
  merge(A/f'lang/{locale}.json',fragment_lore)
 for water in [False,True]:
  tale='cleansed_spring_keeper' if water else 'cleansed_flame_guardian';name='mor_water_amulet_fragment' if water else 'mor_fire_amulet_fragment';adv='claimed_water_fragment' if water else 'claimed_fire_fragment'
  write(D/f'tales/{tale}.json',{'part':'creatures','chapter':'legends','section':'inhabitants','title':'tales.entry.'+tale+'.title','icon':'slavicmyths:'+name,'initial':0,'subjects':['entity/slavicmyths:'+('defiled_spring_keeper' if water else 'extinguished_flame_guardian')],'advancements':['slavicmyths:'+adv],'links':[]})
  write(D/f'advancement/{adv}.json',{'parent':'slavicmyths:found_ancient_sanctuary','display':{'icon':{'id':'slavicmyths:'+name},'title':{'translate':'advancements.slavicmyths.'+adv+'.title'},'description':{'translate':'advancements.slavicmyths.'+adv+'.description'},'frame':'goal','show_toast':True,'announce_to_chat':False,'hidden':False},'criteria':{'claim':{'trigger':'minecraft:impossible'}}})
 write(D/'damage_type/guardian_fire.json',{'message_id':'guardian_fire','scaling':'when_caused_by_living_non_player','exhaustion':.1,'effects':'burning'})
 write(D/'tales/mor_guardian_fragments.json',{'part':'creatures','chapter':'legends','section':'inhabitants','title':'tales.entry.mor_guardian_fragments.title','icon':'slavicmyths:mor_fire_amulet_fragment','initial':0,'subjects':[],'advancements':['slavicmyths:claimed_fire_fragment','slavicmyths:claimed_water_fragment'],'links':[]})
 for name in ['extinguished_flame_guardian','defiled_spring_keeper','guardian_water_echo','guardian_water_prison']:write(D/f'loot_table/entities/{name}.json',{'type':'minecraft:entity','pools':[]})
 tag=R/'src/main/resources/data/minecraft/tags/damage_type/is_fire.json'
 old=json.loads(tag.read_text(encoding='utf-8')) if tag.exists() else {'replace':False,'values':[]}
 if 'slavicmyths:guardian_fire' not in old['values']:old['values'].append('slavicmyths:guardian_fire')
 write(tag,old)
def main():
 from guardian_spawn_assets import generate
 generate()
 O.mkdir(parents=True,exist_ok=True);emit(fire(),'extinguished_flame_guardian','FireGuardianMesh',FIRE,'fire');emit(water(),'defiled_spring_keeper','SpringKeeperMesh',WATER,'water');icons();blocks();sounds();language()
 im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
 for y in range(16):
  for x in range(16):d.point((x,y),fill=(200,235,255,220 if (x+y)%5==0 else 130))
 im.save(A/'textures/entity/guardian_water_surface.png');print('GUARDIAN_ASSETS_PASS two original rigs, matching fragments, 28 OGG')
if __name__=='__main__':main()
