"""Original artifact geometry and offline, plucked-string gusli recording."""
from finalize_065 import *
import sys
def generate():
 tex={'wood':'minecraft:block/stripped_spruce_log','dark':'minecraft:block/dark_oak_planks','rim':'minecraft:block/spruce_planks','bone':'minecraft:block/bone_block_side','red':'minecraft:block/red_terracotta','white':'minecraft:block/white_wool','iron':'minecraft:block/iron_block','green':'minecraft:block/mossy_cobblestone','string':'minecraft:block/sandstone_top','end':'minecraft:block/oak_log_top','bark':'minecraft:block/oak_log','food':'minecraft:block/orange_terracotta','clay':'minecraft:block/terracotta'}
 tex['particle']=tex['wood']
 display={'gui':{'rotation':[30,135,0],'translation':[0,0,0],'scale':[.8,.8,.8]},'ground':{'rotation':[0,0,0],'translation':[0,2,0],'scale':[.65,.65,.65]},'fixed':{'rotation':[0,0,0],'translation':[0,0,0],'scale':[.8,.8,.8]},'thirdperson_righthand':{'rotation':[75,0,0],'translation':[-2,0,-3],'scale':[.85,.85,.85]},'thirdperson_lefthand':{'rotation':[75,0,0],'translation':[2,0,-3],'scale':[.85,.85,.85]},'firstperson_righthand':{'rotation':[35,0,0],'translation':[0,0,0],'scale':[.85,.85,.85]}}
 gusli=[]
 for i in range(7):
  x=1+i*1.8;z=2+i*.65
  gusli += [cube([x,5,z],[x+1.85,7.5,14-i*.1],'wood'),cube([x,5,z],[x+1.85,5.5,14-i*.1],'dark'),cube([x,7.5,z],[x+.15,8,14-i*.1],'string')]
 for i in range(6):gusli.append(cube([2+i*1.8,7.55,13.3],[2.7+i*1.8,7.8,13.9],'red'))
 gusli += [cube([1,7,12.7],[14,8,13.2],'rim'),cube([.6,6,1],[3,9,3.5],'dark'),cube([.6,8,0],[1.6,8.6,2],'bone')]
 model('item/gusli',gusli,tex,display)
 others(tex,display)
 for lang in ('ru_ru','en_us'):
  path=A/f'lang/{lang}.json';data=json.loads(path.read_text(encoding='utf-8'));data.update({'item.slavicmyths.gusli':'Гусли-самогуды' if lang=='ru_ru' else 'Self-playing gusli','subtitles.slavicmyths.gusli_loop':'Гусли перебирают струны' if lang=='ru_ru' else 'Gusli strings resonate','advancement.slavicmyths.self_playing_music.title':'А музыка всё играет' if lang=='ru_ru' else 'And the music plays','advancement.slavicmyths.self_playing_music.description':'Сыграть на Гуслях-самогудах' if lang=='ru_ru' else 'Play the self-playing gusli'});write(path,data)
 sounds=json.loads((A/'sounds.json').read_text(encoding='utf-8'));sounds['gusli_loop']={'subtitle':'subtitles.slavicmyths.gusli_loop','sounds':[{'name':'slavicmyths:gusli_loop','stream':False}]};write(A/'sounds.json',sounds)
 advance('self_playing_music','gusli')
 sound=A/'sounds/gusli_loop.ogg'
 if not sound.exists():
  sys.path.insert(0,str(R/'.tools/audio-libs'));import numpy as np;import soundfile as sf
  rng=np.random.default_rng(650);rate=22050;n=rate*6;out=np.zeros(n)
  # Karplus–Strong: a decaying filtered delay line models each plucked wooden string.
  for start,freq,gain in [(0,196,.7),(.5,293.66,.55),(1,392,.4),(1.5,329.63,.45),(2,293.66,.5),(2.5,220,.5),(3,174.61,.65),(3.5,261.63,.5),(4,349.23,.4),(4.5,293.66,.45),(5,261.63,.5),(5.5,220,.4)]:
   delay=int(rate/freq);buf=rng.uniform(-1,1,delay);tone=np.zeros(rate*3)
   for j in range(len(tone)):
    k=j%delay;tone[j]=buf[k];buf[k]=.497*(buf[k]+buf[(k+1)%delay])
   tone*=gain;offset=int(start*rate)
   for shift in (0,n):
    a=max(0,offset-shift);b=min(n,offset-shift+len(tone))
    if b>a:out[a:b]+=tone[a-(offset-shift):b-(offset-shift)]
  out*=.65/max(1,np.max(np.abs(out)));sf.write(str(sound),out,rate,format='OGG',subtype='VORBIS')
def others(tex,display):
 # Folded cloth, layered hem and sparse red embroidery.
 folded=[cube([3,4,3],[13,5,13],'white'),cube([3.3,5,3.3],[12.8,6,12.8],'white'),cube([3,6,4],[12.5,6.8,12.5],'white')]
 for x in range(4,12,2):folded.append(cube([x,6.8,11],[x+.9,6.95,12],'red'))
 model('item/skatert',folded,tex,display)
 variants={}
 for n in range(7):
  cloth=[cube([0,0,0],[16,.65,16],'white')]
  for x in range(1,15,2):
   for z in (1,14):cloth.append(cube([x,.65,z],[x+1,.8,z+.7],'red'))
   for z in (1,14):cloth.append(cube([z,.65,x],[z+.7,.8,x+1],'red'))
  dishes=[ [cube([3,.65,3],[7,2,6],'food'),cube([3.5,2,3.5],[6.5,3,5.5],'food')],
   [cube([9,.65,3],[13,1.4,7],'dark'),cube([9,1.4,3],[10,2.5,7],'wood'),cube([12,1.4,3],[13,2.5,7],'wood'),cube([10,1.4,3],[12,2.5,4],'wood'),cube([10,1.4,6],[12,2.5,7],'wood')],
   [cube([3,.65,10],[4.8,2.2,11.8],'red'),cube([5,.65,9.5],[6.8,2.2,11.3],'red')],
   [cube([10,.65,10],[13,3.5,13],'clay'),cube([10.5,3.5,10.5],[12.5,5,12.5],'clay'),cube([10,5,10],[13,5.4,13],'clay')],
   [cube([6,.65,6],[9,1.1,9],'food')], [cube([6,1.1,6],[9,1.6,9],'food')] ]
  for dish in dishes[:n]:cloth+=dish
  model(f'block/skatert_{n}',cloth,tex);variants[f'portions={n}']={'model':f'slavicmyths:block/skatert_{n}'}
 write(A/'blockstates/skatert.json',{'variants':variants});write(D/'loot_tables/blocks/skatert.json',{'type':'minecraft:block','pools':[]})
 log=[cube([2,4,4],[14,10,12],'bark'),cube([1.9,4.2,4.2],[2.1,9.8,11.8],'end'),cube([13.9,4.2,4.2],[14.1,9.8,11.8],'end')]
 for x in (6,8):log += [cube([x,3.8,3.8],[x+.6,10.2,4.3],'string'),cube([x,3.8,11.7],[x+.6,10.2,12.2],'string'),cube([x,9.8,4],[x+.6,10.3,12],'string')]
 log += [cube([9,10,7],[12,10.5,7.5],'wood'),cube([11,10.4,6],[13,10.8,8.5],'green')];model('item/badnyak',log,tex,display)
 staff=[cube([7,-6,7],[8.5,5,8.5],'dark'),cube([7,4,7],[9,15,9],'dark',([8,5,8],'z',-22.5)),cube([9,13,7],[11,20,9],'dark'),cube([10,18,7],[11.8,24,8.8],'wood',([11,18,8],'z',-22.5)),cube([7,17,7],[9,24,9],'dark',([8,17,8],'z',22.5)),cube([9,19,7.2],[10.7,21,8.7],'green')]
 for y in (5,7,9):staff.append(cube([7,y,6.8],[9.5,y+.5,9.2],'rim'))
 for x,y in [(5.7,17),(12,19),(13,16)]:staff += [cube([x,y,7],[x+.25,21,7.25],'rim'),cube([x-.3,y-1,6.8],[x+.5,y+1,7.6],'bone')]
 long_display=json.loads(json.dumps(display));long_display['gui']['scale']=[.43]*3
 for hand in ('thirdperson_righthand','thirdperson_lefthand','firstperson_righthand'):long_display[hand]={'rotation':[0,0,-8],'translation':[0,2,0],'scale':[1,1,1]}
 model('item/veles_staff',staff,tex,long_display)
 charm=[cube([5,5,7],[11,11,9],'dark',([8,8,8],'z',45)),cube([7.7,4,6.8],[8.3,12,7.1],'red'),cube([6,12,7.3],[6.6,15,8],'rim'),cube([9.4,12,7.3],[10,15,8],'rim'),cube([6,14.5,7.3],[10,15.2,8],'rim')]
 for x,y in [(5.8,7),(6.8,5.8),(9.4,8.7),(8.5,10)]:charm.append(cube([x,y,6.85],[x+.3,y+1,7.1],'bone'))
 model('item/retribution_charm',charm,tex,display)
 # Keep the existing expensive totem-based recipe and registry ID.
 entries=[('table_is_set','skatert','Стол накрыт','The table is set','Расстелить Скатерть-самобранку','Unfold the self-serving cloth'),('forest_guardian','veles_staff','Сила леса','Strength of the forest','Призвать лесного защитника','Summon a forest guardian'),('not_today','retribution_charm','Не сегодня','Not today','Пережить смертельный удар с Оберегом смерти','Survive a lethal hit with the death charm')]
 for name,item,*_ in entries:advance(name,item)
 for lang in ('ru_ru','en_us'):
  path=A/f'lang/{lang}.json';data=json.loads(path.read_text(encoding='utf-8'));ru=lang=='ru_ru'
  data.update({'block.slavicmyths.skatert':'Скатерть-самобранка' if ru else 'Self-serving cloth','item.slavicmyths.badnyak':'Бадняк' if ru else 'Badnyak ritual log','item.slavicmyths.veles_staff':'Посох Велеса' if ru else 'Staff of Veles','item.slavicmyths.retribution_charm':'Оберег смерти' if ru else 'Death charm','artifact.slavicmyths.resting':'Артефакт ещё восстанавливает силу' if ru else 'The artifact is still recovering','death.attack.slavic_retribution':'%1$s принял смертельную силу от %2$s' if ru else '%1$s received the death meant for %2$s'})
  for k in list(data):
   if 'retribution_charm' in k and k!='item.slavicmyths.retribution_charm':data[k]='Слот: оберег. Одноразовое спасение с 1 HP; до 50 урона смертельному врагу с учётом его защиты.' if ru else 'Charm slot. Consumed to survive at 1 HP; returns up to 50 damage to the lethal attacker, respecting defenses.'
  for name,item,rt,et,rd,ed in entries:data[f'advancement.slavicmyths.{name}.title']=rt if ru else et;data[f'advancement.slavicmyths.{name}.description']=rd if ru else ed
  write(path,data)
def advance(name,item):
 write(D/f'advancements/{name}.json',{'parent':'slavicmyths:root','display':{'icon':{'item':'slavicmyths:'+item},'title':{'translate':f'advancement.slavicmyths.{name}.title'},'description':{'translate':f'advancement.slavicmyths.{name}.description'},'frame':'goal','show_toast':True,'announce_to_chat':True,'hidden':False},'criteria':{'done':{'trigger':'minecraft:impossible'}}})
if __name__=='__main__':generate()
