"""Authored 0.8.6 material atlases, distinct artifact sprites and explicit vanilla sound fallback.
Run this layer alone to preserve the user's existing weapon artwork.
"""
from finalize_065 import A,D,R,write
from water_070 import png
import json,math

CREATURES={
 'upyr':('Упырь','Upyr',40), 'nav':('Навь','Nav',32),
 'kurgan_druzhinnik':('Курганный дружинник','Kurgan Druzhinnik',60),
 'kurgan_voevoda':('Курганный воевода','Kurgan Voevoda',150),
 'buried_volkhv':('Погребённый волхв','Buried Volkhv',150),
 'unresting_prince':('Неупокоенный князь','Unresting Prince',350)}
ITEMS={
 'upyr_fang':('Клык упыря','Upyr Fang'), 'grave_cloth_scrap':('Лоскут погребальной ткани','Grave Cloth Scrap'),
 'nav_essence':('Сущность нави','Nav Essence'), 'torn_burial_ribbon':('Разорванная погребальная лента','Torn Burial Ribbon'),
 'shield_boss_fragment':('Обломок умбона','Shield Boss Fragment'), 'druzhinnik_blade_fragment':('Обломок клинка дружинника','Druzhinnik Blade Fragment'),
 'voevoda_insignia':('Знак воеводы','Voevoda Insignia'), 'volkhv_amulet':('Амулет волхва','Volkhv Amulet'),
 'princely_seal':('Княжеская печать','Princely Seal')}
CUES={
 'upyr':'ambient hurt death attack leap bite step', 'nav':'ambient hurt death attack shift spectral_appear step',
 'kurgan_druzhinnik':'ambient hurt death sword_attack shield_block shield_bash step',
 'kurgan_voevoda':'ambient hurt death axe_attack shield_charge shield_impact phase_shift step',
 'buried_volkhv':'ambient hurt death cast_basic cast_clones cast_ground_seal summon step',
 'unresting_prince':'ambient hurt death combo summon phase_shift heavy_strike grab shield_block step'}

def atlas(name):
 skin=(198,187,163) if name=='upyr' else (191,212,227) if name=='nav' else (145,150,138)
 palette=[skin,(210,200,175),(94,99,106),(227,218,180),(115,32,33),(57,45,36),(177,143,77),(32,31,35),
          (193,27,20) if name=='upyr' else (124,191,219),(79,72,62),(98,66,39),(125,30,34),(43,40,48),(180,208,225),(139,42,42),(63,80,67)]
 if name=='kurgan_voevoda':palette[2]=(67,70,75)
 if name=='unresting_prince':palette[2]=(110,113,119);palette[11]=(153,32,36)
 def pixel(x,y):
  mat=x//64+y//64*4;u=x%64;v=y%64;base=palette[mat];noise=((u//2*13+v//2*17+u//2*v//2*3)%9)-4
  # Each repeating material remains readable at native cuboid UV dimensions.
  if mat==2:
   ring=((u%6-3)**2+(v%5-2)**2);noise=20 if 3<=ring<=7 else -18 if ring<3 else -5
  elif mat in (1,12):
   noise+=4 if u%3==0 else -4
   if v%12 in (0,1) and (u//2+v//12)%4<2:base=(114,38,37)
   if mat==1 and ((u*7+v*13)%97<2):base=(97,85,66)
  elif mat in (4,11,14):
   noise+=7 if u%5==0 else -3
   if v%16<2 and u%8 in (1,2,5,6):base=(191,153,79) if name=='unresting_prince' else (193,177,138)
  elif mat==5:noise+=11 if (u+v//3)%7==0 else -6
  elif mat==10:noise+=14 if (u+v//9)%7==0 else -5
  elif mat==6:noise+=18 if (u+v)%9==0 else -4
  elif mat==0:
   if (u*5+v*7)%73<3:base=(97,106,100)
   if v%15==13 and u%11<3:base=(133,119,105)
  elif mat==8:noise=0
  alpha=160+(u//3+v//4)%4*14 if name=='nav' and mat in (0,13) else 255
  return tuple(max(0,min(255,c+noise)) for c in base)+(alpha,)
 png(A/f'textures/entity/{name}.png',256,pixel)

def sprite(name):
 def pixel(x,y):
  u=x-15.5;v=y-15.5;r=math.hypot(u,v);color=None
  if name=='upyr_fang' and 5<y<28 and abs(x-(19-(y-6)*.19))<(28-y)*.18+1:color=(217,209,177)
  elif name=='grave_cloth_scrap' and 6<=x<=25 and 7<=y<25+(x%4) and not (x<10 and y<12):color=(166,150,115) if y%7>1 else (127,46,39)
  elif name=='nav_essence' and ((u/7)**2+((v+3)/10)**2<1 or abs(u+v*.22)<2 and 15<y<29):color=(116,181,203) if r>4 else (213,235,227)
  elif name=='torn_burial_ribbon' and 4<y<28 and abs(x-(15+math.sin(y*.27)*4))<3+(y%9==0):color=(168,181,167) if y%7>1 else (78,112,115)
  elif name=='shield_boss_fragment' and 6<r<12 and not (u>3 and v<-2):color=(103,109,109) if r<10 else (165,160,134)
  elif name=='shield_boss_fragment' and r<6:color=(76,82,87)
  elif name=='druzhinnik_blade_fragment' and 5<y<26 and abs(x-(23-y*.38))<3 and y>5+(x%3):color=(164,172,172) if x%3 else (92,101,108)
  elif name=='voevoda_insignia' and (abs(u)<8 and abs(v)<8 or abs(u)<2 and abs(v)<12):color=(169,139,76) if abs(u)+abs(v)>5 else (117,42,35)
  elif name=='volkhv_amulet' and (5<r<9 or abs(u)<1.3 and -12<v<0):color=(132,114,74)
  elif name=='volkhv_amulet' and abs(u)<4 and 0<v<11:color=(206,198,164) if y%4 else (87,82,62)
  elif name=='princely_seal' and r<11:color=(169,135,69) if r>8 or abs(u)<1 or abs(v)<1 or abs(abs(u)-abs(v))<1 else (87,69,43)
  if color is None:return (0,0,0,0)
  grain=((x//2*7+y//2*11)%7)-3
  return tuple(max(0,min(255,c+grain*2)) for c in color)+(255,)
 png(A/f'textures/item/{name}.png',32,pixel)

def pool(item,chance=1,maximum=1):
 p={'rolls':1,'entries':[{'type':'minecraft:item','name':item,'functions':[{'function':'minecraft:set_count','count':{'min':1,'max':maximum}}]}]}
 if chance<1:p['conditions']=[{'condition':'minecraft:random_chance','chance':chance}]
 return p

def generate():
 langs={l:json.loads((A/f'lang/{l}.json').read_text(encoding='utf-8')) for l in ('ru_ru','en_us')}
 sounds=json.loads((A/'sounds.json').read_text(encoding='utf-8'));fallback={}
 for name,(ru,en,hp) in CREATURES.items():
  atlas(name);write(A/f'models/item/{name}_spawn_egg.json',{'parent':'minecraft:item/template_spawn_egg'})
  for l,label in zip(langs,(ru,en)):langs[l]['entity.slavicmyths.'+name]=label;langs[l]['item.slavicmyths.'+name+'_spawn_egg']=label+(' — яйцо призыва' if l=='ru_ru' else ' Spawn Egg')
  family={'upyr':'husk','nav':'phantom','kurgan_druzhinnik':'wither_skeleton','kurgan_voevoda':'wither_skeleton','buried_volkhv':'evoker','unresting_prince':'wither_skeleton'}[name]
  for cue in CUES[name].split():
   if cue in ('ambient','hurt','death'):event='entity.'+family+'.'+cue
   elif cue=='step':event='block.stone.step' if name!='nav' else 'entity.phantom.flap'
   elif cue in ('shield_block','shield_impact'):event='item.shield.block'
   elif cue in ('shield_bash','shield_charge'):event='entity.player.attack.knockback'
   elif cue in ('shift','spectral_appear','cast_clones'):event='entity.illusioner.mirror_move'
   elif cue in ('cast_basic','cast_ground_seal'):event='entity.evoker.cast_spell'
   elif cue=='summon':event='entity.evoker.prepare_summon'
   elif cue in ('phase_shift','grab','bite'):event='entity.ravager.roar' if name!='upyr' else 'entity.husk.hurt'
   elif cue=='leap':event='entity.husk.ambient'
   else:event='entity.player.attack.sweep'
   key=name+'_'+cue;sub='subtitles.slavicmyths.'+key
   sounds[key]={'subtitle':sub,'sounds':[{'name':'minecraft:'+event,'type':'event','pitch':.72 if name!='nav' else .85,'volume':.55 if cue in ('ambient','step') else .85}]};fallback[key]=event
   langs['ru_ru'][sub]=ru+': '+{'ambient':'стоны','hurt':'ранен','death':'погибает','step':'шаги','shift':'исчезновение','summon':'призыв','shield_block':'щит отражает удар','phase_shift':'ярость'}.get(cue,'атака')
   langs['en_us'][sub]=en+': '+cue.replace('_',' ')
 for name,(ru,en) in ITEMS.items():
  sprite(name);write(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+name}})
  langs['ru_ru']['item.slavicmyths.'+name]=ru;langs['en_us']['item.slavicmyths.'+name]=en
 loot={
  'upyr':[pool('minecraft:rotten_flesh',.8,2),pool('slavicmyths:upyr_fang',.35),pool('slavicmyths:grave_cloth_scrap',.12)],
  'nav':[pool('slavicmyths:nav_essence',.65),pool('slavicmyths:torn_burial_ribbon',.15)],
  'kurgan_druzhinnik':[pool('slavicmyths:shield_boss_fragment',.45),pool('slavicmyths:druzhinnik_blade_fragment',.3),pool('minecraft:iron_nugget',.7,3)],
  'kurgan_voevoda':[pool('slavicmyths:voevoda_insignia'),pool('slavicmyths:shield_boss_fragment',.7,2)],
  'buried_volkhv':[pool('slavicmyths:volkhv_amulet'),pool('slavicmyths:nav_essence',.8,2)],
  'unresting_prince':[pool('slavicmyths:princely_seal'),pool('slavicmyths:grave_cloth_scrap',1,3)]}
 for name,pools in loot.items():write(D/f'loot_tables/entities/{name}.json',{'type':'minecraft:entity','pools':pools})
 langs['ru_ru']['kurgan.command.encounter_info']='Комната %s; встреча начата %s; очищена %s; живых UUID %s; печать открыта %s; князь побеждён %s.'
 langs['en_us']['kurgan.command.encounter_info']='Room %s; encounter triggered %s; cleared %s; live UUIDs %s; seal opened %s; prince defeated %s.'
 for l,data in langs.items():write(A/f'lang/{l}.json',data)
 write(A/'sounds.json',sounds)
 write(R/'docs/verification/kurgan-creatures-0.8.6.json',{'version':'0.8.6','creatures':CREATURES,'items':list(ITEMS),'sounds_vanilla_fallback':fallback,'minecraft_launches':0})

if __name__=='__main__':generate()
