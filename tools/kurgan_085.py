"""0.8.5 authored masonry, native block geometry and burial-seal icon. No vanilla texture recolors."""
from finalize_065 import A,D,R,write
from water_070 import png
import math,random,json
NAMES={
'pale_blue_kurgan_stone':('Бледно-синий камень кургана','Pale Blue Kurgan Stone'),
'pale_blue_kurgan_cobblestone':('Бледно-синий булыжник кургана','Pale Blue Kurgan Cobblestone'),
'cracked_pale_blue_kurgan_cobblestone':('Потрескавшийся бледно-синий булыжник кургана','Cracked Pale Blue Kurgan Cobblestone'),
'mossy_pale_blue_kurgan_cobblestone':('Замшелый бледно-синий булыжник кургана','Mossy Pale Blue Kurgan Cobblestone'),
'bog_green_kurgan_stone':('Болотный камень кургана','Bog Green Kurgan Stone'),
'bog_green_kurgan_cobblestone':('Болотный булыжник кургана','Bog Green Kurgan Cobblestone'),
'cracked_bog_green_kurgan_cobblestone':('Потрескавшийся болотный булыжник кургана','Cracked Bog Green Kurgan Cobblestone'),
'mossy_bog_green_kurgan_cobblestone':('Замшелый болотный булыжник кургана','Mossy Bog Green Kurgan Cobblestone'),
'sealed_kurgan_masonry':('Запечатанная кладка кургана','Sealed Kurgan Masonry'),
'burial_stone_slab':('Погребальная каменная плита','Burial Stone Slab'),
'kurgan_column_fragment':('Обломок колонны кургана','Kurgan Column Fragment'),
'kurgan_ritual_bowl':('Ритуальная каменная чаша','Ritual Stone Bowl'),
'kurgan_pottery':('Погребальная керамика','Burial Pottery'),
'kurgan_stone_altar':('Каменный алтарь кургана','Kurgan Stone Altar'),
'kurgan_wall_torch_holder':('Настенный держатель факела','Wall Torch Holder'),
'kurgan_hanging_brazier':('Подвесная курильница','Hanging Brazier'),
'kurgan_rubble':('Обломки кладки кургана','Kurgan Rubble')}

def masonry(name):
 green='bog_green' in name; rough='cobblestone' in name; sealed=name=='sealed_kurgan_masonry'
 rng=random.Random(851+(71 if green else 0)+(211 if rough else 0))
 count=19 if rough else 9
 centers=[(rng.randrange(64),rng.randrange(64),rng.randrange(-13,15)) for _ in range(count)]
 base=(112,120,102) if green else (120,137,151)
 if sealed:base=(86,100,108)
 def px(x,y):
  # Toroidal irregular stone faces; bevels and chips are authored in the same material space.
  distances=sorted((min(abs(x-cx),64-abs(x-cx))**2+(min(abs(y-cy),64-abs(y-cy))*(1 if rough else 1.6))**2,i) for i,(cx,cy,v) in enumerate(centers))
  d,i=distances[0]; edge=math.sqrt(distances[1][0])-math.sqrt(d)
  grain=((x//2*17+y//2*31+x//2*y//2*7)%11)-5
  value=centers[i][2]+grain
  if edge<1.2:value=-43+grain
  elif edge<2.4:value+=14 if (x+y)%7<4 else -12
  elif edge<4:value+=5
  if (x//5+y//3+i)%9==0:value-=9
  color=tuple(max(0,min(255,c+value)) for c in base)
  if 'cracked' in name:
   line=18+y//3+((y//6)%3-1)*2
   branch=50-y//2+(y//8%2)*3
   if abs(x-line)<=1 or (y>20 and abs(x-branch)<=1):color=(62,68,57) if green else(56,66,73)
   elif abs(x-line)==2:color=tuple(max(0,c-17)for c in base)
  if 'mossy' in name:
   patch=(math.sin(x*.13)+math.cos(y*.16)+math.sin((x-y)*.12))
   if patch>1.1 and (edge<5 or y%24>13):
    shade=((x//2*11+y//2*7)%4)*7;color=(65+shade,73+shade,48+shade) if green else(76+shade,85+shade,61+shade)
  if sealed:
   # Restrained recessed ring with three marks, not luminous ornament.
   dist=math.hypot(x-32,y-32)
   if 10<dist<13 or (abs(x-32)<2 and 16<y<22):color=(69,81,93)
  return color+(255,)
 return px

def elem(a,b,t):
 return {'from':a,'to':b,'faces':{d:{'texture':'#'+t,'uv':[0,0,16,16]} for d in ['north','south','west','east','up','down']}}

def generate():
 core=list(NAMES)[:9]
 for n in core:
  png(A/f'textures/block/{n}.png',64,masonry(n))
  write(A/f'models/block/{n}.json',{'parent':'minecraft:block/cube_all','textures':{'all':'slavicmyths:block/'+n}})
  write(A/f'blockstates/{n}.json',{'variants':{'':{'model':'slavicmyths:block/'+n}}})
 materials={'ceramic':(119,76,49),'iron':(49,46,39),'flame':(216,126,43),'seal':(86,100,108)}
 for n,base in materials.items():
  def pixel(x,y,base=base,n=n):
   v=((x//2*11+y//2*7)%7-3)*2
   if n=='ceramic':v+=-15 if y%20<3 else 4
   if n=='iron':v+=8 if x%16<2 else -3
   if n=='flame':v+=int(25*math.sin(x*.2+y*.07))
   return tuple(max(0,min(255,c+v))for c in base)+(255,)
  png(A/f'textures/block/kurgan_{n}.png',64,masonry('sealed_kurgan_masonry') if n=='seal' else pixel)
 stone='pale_blue_kurgan_stone'
 shapes={
 'burial_stone_slab':[( [0,0,0],[16,3,16],'stone'),([1,3,1],[15,5,15],'stone'),([2,5,2],[12,5.5,13],'seal')],
 'kurgan_column_fragment':[([1,0,1],[15,3,15],'rough'),([3,3,3],[13,12,13],'stone'),([3,12,4],[8,16,12],'rough'),([8,12,3],[12,14,12],'rough')],
 'kurgan_ritual_bowl':[([4,0,4],[12,2,12],'stone'),([6,2,6],[10,4,10],'stone'),([3,4,3],[13,6,13],'rough'),([2,6,2],[14,9,4],'stone'),([2,6,12],[14,9,14],'stone'),([2,6,4],[4,9,12],'stone'),([12,6,4],[14,9,12],'stone')],
 'kurgan_pottery':[([5,0,5],[11,2,11],'clay'),([3,2,3],[13,9,13],'clay'),([4,9,4],[12,12,12],'clay'),([5,12,5],[7,15,11],'clay'),([9,12,5],[11,15,11],'clay'),([7,12,5],[9,15,7],'clay'),([7,12,9],[9,15,11],'clay')],
 'kurgan_stone_altar':[([1,0,1],[15,3,15],'rough'),([3,3,3],[13,5,13],'stone'),([5,5,5],[11,12,11],'stone'),([2,12,2],[14,15,14],'rough'),([1,15,1],[15,16,15],'stone')],
 'kurgan_wall_torch_holder':[([6,2,14],[10,13,16],'iron'),([7,3,7],[9,5,15],'iron'),([5,5,6],[11,7,12],'iron'),([7,7,8],[9,12,10],'flame'),([6,8,7],[10,10,11],'flame')],
 'kurgan_hanging_brazier':[([4,0,4],[12,2,12],'iron'),([2,2,2],[14,4,4],'iron'),([2,2,12],[14,4,14],'iron'),([2,2,4],[4,4,12],'iron'),([12,2,4],[14,4,12],'iron'),([6,2,6],[10,7,10],'flame'),([5,3,5],[11,5,11],'flame')],
 'kurgan_rubble':[([1,0,2],[7,4,8],'rough'),([8,0,6],[14,6,13],'rough'),([4,0,10],[9,2,15],'stone'),([10,0,1],[15,3,5],'rough')]
 }
 # Three separate chains converge at a common hanger. Deliberately not four.
 for sx,sz in [(3,3),(12,3),(8,12)]:
  for i in range(6):
   t=i/6;x=sx+(8-sx)*t;z=sz+(8-sz)*t
   shapes['kurgan_hanging_brazier'].append(([x,4+i*2,z],[x+1,6+i*2,z+1],'iron'))
 textures={'seal':'slavicmyths:block/kurgan_seal','stone':'slavicmyths:block/'+stone,'rough':'slavicmyths:block/cracked_pale_blue_kurgan_cobblestone','clay':'slavicmyths:block/kurgan_ceramic','iron':'slavicmyths:block/kurgan_iron','flame':'slavicmyths:block/kurgan_flame','particle':'slavicmyths:block/'+stone}
 for n,parts in shapes.items():
  write(A/f'models/block/{n}.json',{'parent':'minecraft:block/block','textures':textures,'elements':[elem(*v)for v in parts]})
  write(A/f'blockstates/{n}.json',{'variants':{'facing='+d:{'model':'slavicmyths:block/'+n,'y':i*90}for i,d in enumerate(['north','east','south','west'])}})
 for n in NAMES:
  write(A/f'models/item/{n}.json',{'parent':'slavicmyths:block/'+n})
  write(D/f'loot_tables/blocks/{n}.json',{'type':'minecraft:block','pools':[] if n=='sealed_kurgan_masonry' else [{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:'+n}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
 def icon(x,y):
  dx=x-31.5;dy=y-31.5;r=math.hypot(dx,dy);notch=any((x-cx)**2+(y-cy)**2<25 for cx,cy in [(32,7),(11,45),(53,45)])
  if r>23 and not notch:return(0,0,0,0)
  if r>21 and x<22 and y<18:return(0,0,0,0)
  c=(62,71,75) if r>19 else(101,119,128)
  if 11<r<15:c=(139,153,151)
  if y>36 and dx>0:c=(93,103,79)
  if abs(dx*.75+dy+((y//8)%2)*2)<2.4:c=(34,41,44)
  return c+(255,)
 png(A/'textures/mob_effect/kurgan_curse.png',64,icon)
 for tier in ['small','warrior','great']:
  for profile in ['burial','offering','important']:
   group='burial_jewelry' if profile=='offering' else 'burial_weapons' if profile=='important' else 'burial_common'
   write(D/f'loot_tables/chests/kurgan_{tier}_{profile}.json',{'type':'minecraft:chest','pools':[{'rolls':1,'entries':[{'type':'minecraft:loot_table','name':'slavicmyths:chests/burial_common'}],'conditions':[{'condition':'minecraft:random_chance','chance':.6}]},{'rolls':1,'entries':[{'type':'minecraft:loot_table','name':'slavicmyths:chests/'+group}],'conditions':[{'condition':'minecraft:random_chance','chance':.4 if tier=='great' else .25}]}]})
 for lang,index in [('ru_ru',0),('en_us',1)]:
  p=A/f'lang/{lang}.json';data=json.loads(p.read_text(encoding='utf-8'))
  for n,names in NAMES.items():data['block.slavicmyths.'+n]=names[index]
  messages={
   'effect.slavicmyths.kurgan_curse':('Проклятие кургана','Kurgan Curse'),
   'kurgan.command.generated':('Курган создан: %s, вход %s %s %s; этажей %s, помещений %s.','Kurgan generated: %s, entrance %s %s %s; %s floors, %s rooms.'),
   'kurgan.command.invalid_site':('Нет безопасного места для кургана: высота, вода, постройки или пересечение. Используйте свободную тестовую местность.','No safe kurgan site: height, water, buildings or intersection. Use open test terrain.'),
   'kurgan.command.info':('Курган %s; тип %s; начало %s; этажей %s; seed %s; нарушение покоя %s.','Kurgan %s; tier %s; origin %s; floors %s; seed %s; disturbance %s.'),
   'kurgan.command.cleared':('Проклятие кургана снято с %s.','Kurgan curse cleared for %s.'),
   'kurgan.command.distance':('Расстояние: %s блоков.','Distance: %s blocks.'),
   'kurgan.kind.1':('Родовой / Воинский курган','Warrior / Clan Kurgan')}
  for key,val in messages.items():data[key]=val[index]
  write(p,data)
 write(R/'docs/verification/kurgan-0.8.5.json',{'blocks':list(NAMES),'items':list(NAMES),'source_texture_size':64,'reference_package':'Slavic_Myths_0.8.5_Kurgan_Codex_Package.zip'})
if __name__=='__main__':generate()
