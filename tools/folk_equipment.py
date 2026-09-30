"""Reproducible cuboid equipment designs and original synthesized wildlife calls."""
from generate_051 import R,A,png,rgba
import json,sys,math
D=R/'src/main/resources/data'
def write(p,v):
 p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
ITEMS={
 'invisibility_cap':('head','Шапка-невидимка','Invisibility cap','Слот: голова. Невидимость через 1 с покоя; бег и бой раскрывают на 5 с.','Head slot. Invisible after 1s calm; sprinting and combat reveal for 5s.'),
 'retribution_charm':('charm','Оберег воздаяния','Retribution charm','Слот: оберег. Однократно спасает от смерти: 2 сердца, восстановление; 6 урона близкому нападавшему.','Charm slot. Consumed to prevent death: 2 hearts, recovery; 6 damage to a nearby attacker.'),
 'perun_ring':('ring','Перстень Перуна','Perun ring','Слот: кольцо. +2 урона в ближнем бою раз в 3 с; −35% урона молнии. Одинаковые кольца не складываются.','Ring slot. +2 melee damage every 3s; 35% lightning resistance. Duplicates do not stack.'),
 'veles_amulet':('necklace','Оберег Велеса','Veles amulet','Слот: ожерелье. Уменьшает радиус агрессии медведя и кабана на 45%; не защищает от мести и охраны детёныша.','Necklace slot. 45% smaller bear/boar aggression radius; retaliation and cub protection remain.'),
 'hunter_belt':('belt','Пояс охотника','Hunter belt','Слот: пояс. +10% урона стрелами по диким животным.','Belt slot. +10% projectile damage against wildlife.'),
 'resin_ring':('ring','Смоляное кольцо','Resin ring','Слот: кольцо. +½ сердца раз в 5 с при сытости ≥18; расходует насыщение. Одинаковые кольца не складываются.','Ring slot. Heal half a heart every 5s at food 18+; costs exhaustion. Duplicates do not stack.'),
 'seven_league_boots':('feet','Сапоги-скороходы','Seven-league boots','Слот: обувь. Разгон до +30% за 3 с прямого бега; остановка, прыжок и резкий поворот сбрасывают разгон.','Feet slot. Up to +30% speed after 3s straight sprint; stopping, jumping or sharp turns reset it.')}
def generate():
 langs={l:json.loads((A/f'lang/{l}.json').read_text(encoding='utf-8')) for l in ('ru_ru','en_us')}
 materials={'fur':['4a3528','74583e'],'cloth':['572931','803b43'],'leather':['503323','79513a'],'gold':['987541','d3b66b'],'dark':['211d1b','37302a'],'wood':['67492d','9b7749'],'silver':['697680','bac3c7'],'resin':['9c501e','e3ac41'],'blue':['2b4b65','6bb3cd']}
 atlas=[[(0,0,0,0)]*16 for _ in range(16)]
 uv={}
 for n,(mat,colors) in enumerate(materials.items()):
  x=n%4*4;y=n//4*4;uv[mat]=[x,y,x+4,y+4]
  for yy in range(4):
   for xx in range(4):atlas[y+yy][x+xx]=rgba(colors[0 if yy==3 or xx==0 else 1])
 png(A/'textures/item/folk_materials.png',atlas)
 armor=[[rgba('633c2b') for x in range(64)] for y in range(32)]
 for y in range(32):
  for x in range(64):
   if y in (20,21):armor[y][x]=rgba('aa8851')
   elif y>=29:armor[y][x]=rgba('322a25')
   elif x%8 in (0,7):armor[y][x]=rgba('482a23')
 png(A/'textures/armor/seven_league.png',armor)
 for id,(slot,ru,en,rt,et) in ITEMS.items():
  for lang,name,tip in [('ru_ru',ru,rt),('en_us',en,et)]:langs[lang]['item.slavicmyths.'+id]=name;langs[lang]['item.slavicmyths.'+id+'.effect']=tip
  if slot!='feet':
   p=D/f'curios/tags/items/{slot}.json';data=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]}
   if 'slavicmyths:'+id not in data['values']:data['values'].append('slavicmyths:'+id)
   write(p,data)
  elements=[]
  def box(a,b,material):elements.append({'from':a,'to':b,'faces':{f:{'uv':uv[material],'texture':'#'+material} for f in ('north','south','east','west','up','down')}})
  if id=='invisibility_cap':
   box([2,2,3],[14,5,13],'fur');box([3,5,4],[13,10,12],'cloth');box([4,10,5],[12,13,11],'cloth');box([6,13,6],[12,15,10],'cloth')
  elif id=='seven_league_boots':
   for x in (2,9):
    box([x,1,2],[x+5,4,12],'leather');box([x,4,6],[x+4,14,12],'leather');box([x-.2,12,5.8],[x+4.2,14,12.2],'gold');box([x+1,6,5.6],[x+3,11,6.2],'cloth')
  elif id=='hunter_belt':
   box([1,6,6],[15,9,9],'leather');box([6,5,5],[10,10,6],'gold');box([2,2,5],[5,7,10],'leather');box([11,3,5],[14,8,10],'leather');box([7,6,4.8],[9,9,5.2],'dark')
  elif id=='veles_amulet':
   box([3,8,7],[4,15,8],'leather');box([12,8,7],[13,15,8],'leather');box([4,14,7],[12,15,8],'leather');box([4,5,7],[12,8,8],'leather');box([6,2,6],[10,7,9],'wood');box([5,4,5.8],[11,5,6.2],'gold');box([7,2,5.8],[9,7,6.2],'gold')
  elif id=='retribution_charm':
   box([7,10,7],[9,15,8],'leather');box([4,4,6],[12,10,9],'silver');box([6,2,6],[10,12,9],'silver');box([6,5,5],[10,9,6],'cloth');box([7,6,4.8],[9,8,5],'gold')
  else:
   mat='silver' if id=='perun_ring' else 'wood'
   for a,b in [([3,3,7],[5,12,10]),([11,3,7],[13,12,10]),([5,2,7],[11,4,10]),([5,11,7],[11,13,10])]:box(a,b,mat)
   box([6,11,5],[10,14,8],'gold' if id=='resin_ring' else 'silver');box([7,11.5,4.5],[9,13.5,5.5],'resin' if id=='resin_ring' else 'blue')
  textures={k:'slavicmyths:item/folk_materials' for k in materials}
  textures['particle']=textures['leather']
  write(A/f'models/item/{id}.json',{'textures':textures,'elements':elements,'display':{'gui':{'rotation':[15,-30,0],'scale':[.8,.8,.8]},'ground':{'scale':[.4,.4,.4]},'fixed':{'scale':[.8,.8,.8]},'thirdperson_righthand':{'rotation':[0,90,0],'scale':[.45,.45,.45]}}})
  special={'invisibility_cap':'slavicmyths:rune_shadow','retribution_charm':'minecraft:totem_of_undying','perun_ring':'slavicmyths:rune_thunder','veles_amulet':'slavicmyths:rune_forest','hunter_belt':'minecraft:arrow','resin_ring':'slavicmyths:pine_resin','seven_league_boots':'slavicmyths:rune_wind'}[id]
  if id=='resin_ring':special='slavicmyths:rune_life'
  write(D/f'slavicmyths/recipes/{id}.json',{'type':'minecraft:crafting_shaped','pattern':[' L ','LXL',' L '],'key':{'L':{'item':'minecraft:leather' if slot in ('head','belt','feet') else 'minecraft:iron_ingot'},'X':{'item':special}},'result':{'item':'slavicmyths:'+id}})
 sounds=json.loads((A/'sounds.json').read_text())
 for id,icon,ru,en,rd,ed in [
  ('first_accessory','veles_amulet','Сила оберега','A little protection','Наденьте аксессуар в его слот','Equip an accessory in its slot'),
  ('two_rings','perun_ring','Два перстня','Two rings','Наденьте два разных кольца','Equip two different rings'),
  ('rare_accessory','invisibility_cap','Из сказки','Out of a tale','Наденьте редкий аксессуар','Equip a rare accessory'),
  ('wildlife_encounter','stag_spawn_egg','Лес насторожился','The forest listens','Встретьте настороженного оленя','Encounter an alert deer'),
  ('boar_charge_block','hunter_belt','Выстоять','Stand firm','Заблокируйте щитом удар кабана','Block a boar attack with a shield')]:
  title='advancements.slavicmyths.'+id+'.title';desc='advancements.slavicmyths.'+id+'.description'
  for l,t,d in [('ru_ru',ru,rd),('en_us',en,ed)]:langs[l][title]=t;langs[l][desc]=d
  write(D/f'slavicmyths/advancements/{id}.json',{'parent':'slavicmyths:root','display':{'icon':{'item':'slavicmyths:'+icon},'title':{'translate':title},'description':{'translate':desc},'frame':'task','show_toast':True,'announce_to_chat':True,'hidden':False},'criteria':{'event':{'trigger':'minecraft:impossible'}}})
 sys.path.insert(0,str(R/'.tools/audio-libs'));import numpy as np;import soundfile as sf
 for i,(animal,base) in enumerate([('bear',75),('cub',210),('wolf',290),('boar',115),('stag',135),('doe',340)]):
  for j,event in enumerate(('ambient','hurt','death','alert','roar','attack','impact')):
   duration=1.5 if event in ('roar','death') else .65 if event=='ambient' else .38
   t=np.arange(int(22050*duration))/22050;rng=np.random.default_rng(6500+i*10+j)
   freq=base*(1+.18*np.sin(t*(4+i)))* (1.4 if event=='hurt' else 1)
   phase=np.cumsum(freq)*2*np.pi/22050
   # Different harmonic/tremolo envelopes, deterministic recordings authored for this project.
   noise=rng.normal(0,1,len(t));noise=np.convolve(noise,np.ones(9)/9,mode='same')
   tone=sum(np.sin(phase*k)/k**1.3 for k in range(1,7))
   rasp={'bear':.8,'cub':.15,'wolf':.12,'boar':.9,'stag':.35,'doe':.08}[animal]
   wave=(tone+noise*rasp)*np.sin(np.pi*t/duration)**1.4*(.65+.35*np.sin(t*(22 if animal=='boar' else 8)+.5)**2)
   if event=='impact':wave=noise*np.exp(-t*14)
   wave=wave/(np.max(np.abs(wave))+1e-6)*.55
   key=animal+'_'+event;path=A/f'sounds/wildlife/{key}.ogg';path.parent.mkdir(exist_ok=True,parents=True);sf.write(str(path),wave,22050,format='OGG',subtype='VORBIS')
   subtitle='subtitles.slavicmyths.'+key;sounds[key]={'subtitle':subtitle,'sounds':[{'name':'slavicmyths:wildlife/'+key}]}
   for lang in langs:langs[lang][subtitle]=({'bear':'Медведь','cub':'Медвежонок','wolf':'Волк','boar':'Кабан','stag':'Олень','doe':'Олениха'}[animal]+' — '+{'ambient':'голос','hurt':'ранен','death':'погибает','alert':'насторожен','roar':'ревёт','attack':'атакует','impact':'удар'}[event]) if lang=='ru_ru' else animal.title()+' '+event
 write(A/'sounds.json',sounds)
 for lang,data in langs.items():write(A/f'lang/{lang}.json',data)
if __name__=='__main__':generate()
