"""Four distinct authored pixel silhouettes; additive canonical resource overlay."""
from pathlib import Path
import json
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'
NAMES={'tugarin_sword':('Меч Тугарина','Tugarin Sword'), 'likho_staff':('Посох Лихо','Likho Staff'), 'ataman_flail':('Кистень атамана',"Ataman's Flail"), 'victor_spear':('Копьё победителя',"Victor's Spear"), 'ataman_sign':('Знак атамана',"Ataman's Sign")}
HINTS={
 'tugarin_sword':('Оба меча в руках: +40% урона полной атаки; кровотечение до 3 стаков, 6 с, 0,5 HP/стак каждые 2 с. Руны срабатывают только с основной руки. Рецепт выдаёт второй меч в ячейку верстака.', 'Both blades held: +40% charged damage; bleeding up to 3 stacks, 6 s, 0.5 HP/stack every 2 s. Only main-hand runes trigger. The recipe leaves the second blade in the bench.'),
 'likho_staff':('В руках: +1 HP раз в 9 с. ПКМ: морок до 12 блоков, стан 1,25 с, дезориентация 6 с; КД 28 с.', 'Held: +1 HP every 9 s. Use: morok within 12 blocks, 1.25 s stun, 6 s disorientation; 28 s cooldown.'),
 'ataman_flail':('Полные попадания: до 3 стаков Размаха. Следующий удар +35% урона, сильный толчок и давление на щит. Пауза 4 с сбрасывает серию.', 'Charged hits: up to 3 Swing stacks. Next hit: +35% damage, strong push and shield pressure. A 4 s pause resets the sequence.'),
 'victor_spear':('Полный удар дальше 3 блоков от края цели: повторный выпад по той же цели без задержки в течение 1,25 с; КД 8 с.', 'Charged hit beyond 3 blocks from target bounds: repeat thrust at the same target without delay within 1.25 s; 8 s cooldown.')}
def write(path,data):
 p=RES/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def append(tag,ids):
 path=f'data/{tag}.json';p=RES/path;data=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]}
 data['values']=list(dict.fromkeys(data['values']+['slavicmyths:'+i for i in ids]));write(path,data)
def icon(id):
 im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im);edge='#201b22';metal='#514650';light='#bcb6bd';red='#a13230';gold='#d5ae52';wood='#503528'
 if id=='tugarin_sword':
  d.line([(7,26),(12,21)],fill=edge,width=5);d.line([(7,26),(12,21)],fill=wood,width=3)
  d.polygon([(10,19),(14,21),(26,9),(28,3),(23,6)],fill=edge)
  d.polygon([(12,19),(14,19),(25,8),(26,5),(23,8)],fill=metal)
  d.line([(15,16),(18,13),(20,13),(23,8)],fill='#de6736');d.line([(9,18),(16,23)],fill=gold,width=2)
  d.rectangle((5,26,8,28),fill=metal)
 elif id=='likho_staff':
  d.line([(5,29),(12,22),(14,14),(20,9)],fill=edge,width=5);d.line([(5,28),(11,22),(13,14),(20,9)],fill=wood,width=3)
  d.polygon([(17,11),(16,5),(20,2),(25,3),(28,7),(26,12),(21,14)],fill=edge)
  d.line([(18,9),(18,5),(21,3),(25,4),(27,7),(25,11),(21,12)],fill='#b6a994',width=2)
  d.polygon([(19,7),(22,5),(25,7),(22,10)],fill=red);d.rectangle((22,6,22,9),fill='#efb17a')
  d.line([(11,21),(14,23)],fill=red,width=2)
 elif id=='ataman_flail':
  d.line([(5,26),(11,15)],fill=edge,width=7);d.line([(5,26),(11,15)],fill=wood,width=4)
  d.line([(6,23),(9,24)],fill=red,width=2);d.line([(8,19),(11,20)],fill=red,width=2)
  d.line([(10,14),(15,8),(20,9),(23,17)],fill=edge,width=4)
  for x,y in [(11,12),(14,9),(18,9),(21,12),(22,15)]:d.rectangle((x,y,x+2,y+2),outline=light)
  d.polygon([(20,18),(26,17),(30,21),(29,27),(25,30),(19,28),(17,23)],fill=edge)
  d.polygon([(20,20),(26,19),(28,22),(27,26),(24,28),(20,26),(19,23)],fill=metal)
  d.line([(21,20),(25,20),(27,23)],fill=light,width=2);d.rectangle((26,16,27,19),fill=metal)
 elif id=='victor_spear':
  d.line([(2,30),(24,8)],fill=edge,width=4);d.line([(3,29),(23,9)],fill=wood,width=2)
  d.polygon([(21,11),(22,6),(29,0),(30,5),(25,12)],fill=edge)
  d.polygon([(23,9),(24,6),(29,2),(28,6),(25,10)],fill=light)
  d.line([(21,10),(24,13)],fill=gold,width=3);d.polygon([(21,13),(18,16),(21,17),(23,14)],fill=red)
  d.line([(6,25),(8,27)],fill=gold,width=2)
 else:
  d.polygon([(9,6),(22,6),(27,12),(25,23),(16,28),(7,23),(5,12)],fill=edge)
  d.polygon([(10,8),(21,8),(25,13),(23,22),(16,25),(9,22),(7,13)],fill=metal)
  d.rectangle((10,12,22,19),outline=gold,width=2);d.line([(12,16),(20,16)],fill=red,width=2)
 return im
def main():
 for id in NAMES:
  path=RES/f'assets/slavicmyths/textures/item/{id}.png';path.parent.mkdir(parents=True,exist_ok=True);icon(id).save(path)
  write(f'assets/slavicmyths/models/item/{id}.json',{'parent':'minecraft:item/generated' if id=='ataman_sign' else 'minecraft:item/handheld','textures':{'layer0':'slavicmyths:item/'+id}})
 weapons=list(HINTS)
 for tag,ids in {'rune_capacity_3':weapons,'rune_staff':['likho_staff'],'rune_heavy':['ataman_flail'],'rune_spear':['victor_spear']}.items():append('slavicmyths/tags/item/'+tag,ids)
 for tag in ('weapon','sharp_weapon','durability'):append('minecraft/tags/item/enchantable/'+tag,weapons)
 recipes={
  'tugarin_sword':['minecraft:diamond_sword','minecraft:diamond_sword','slavicmyths:tugarinova_kozha','slavicmyths:tugarinova_kozha','slavicmyths:tugarinova_kozha','slavicmyths:weapon_wrap'],
  'likho_staff':['slavicmyths:carved_staff','slavicmyths:oko_likha','slavicmyths:spear_shaft','slavicmyths:nit_durnoy_doli','slavicmyths:nit_durnoy_doli'],
  'ataman_flail':['slavicmyths:diamond_flail','slavicmyths:ataman_sign','slavicmyths:flail_chain','slavicmyths:weapon_wrap'],
  'victor_spear':['slavicmyths:diamond_spear','slavicmyths:princely_seal','slavicmyths:druzhinnik_blade_fragment','slavicmyths:silver_fitting','slavicmyths:weapon_wrap']}
 for id,ingredients in recipes.items():
  write(f'data/slavicmyths/recipe/rare_{id}.json',{'type':'slavicmyths:armorer_shapeless','copy_components':True,'ingredients':[{'item':i} for i in ingredients],'result':{'id':'slavicmyths:'+id,'count':1}})
 path='data/slavicmyths/loot_table/entities/ataman.json';data=json.loads((RES/path).read_text());pool={'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:ataman_sign'}]}
 if pool not in data['pools']:data['pools'].append(pool)
 write(path,data)
 for n,locale in enumerate(('ru_ru','en_us')):
  path=f'assets/slavicmyths/lang/{locale}.json';data=json.loads((RES/path).read_text(encoding='utf-8'))
  for id,names in NAMES.items():data['item.slavicmyths.'+id]=names[n]
  for id,hint in HINTS.items():data['tooltip.slavicmyths.weapon.'+id]=hint[n]
  for key,names in {'effect.slavicmyths.rare_bleeding':('Кровотечение','Bleeding'),'effect.slavicmyths.disorientation':('Дезориентация','Disorientation'),'rare.slavicmyths.swing':('Размах: %s/3','Swing: %s/3'),'rare.slavicmyths.second':('Второй выпад готов','Second thrust ready')}.items():data[key]=names[n]
  write(path,data)
 for id,color in [('rare_bleeding','#8c2528'),('disorientation','#602b72')]:
  im=Image.new('RGBA',(18,18));d=ImageDraw.Draw(im)
  if id=='rare_bleeding':d.polygon([(9,1),(3,10),(4,15),(9,17),(14,15),(15,10)],fill=color);d.line([(6,10),(6,13),(8,14)],fill='#e3a3a3')
  else:d.ellipse((1,4,16,13),outline='#b28cbf',width=2);d.ellipse((6,5,11,12),fill=color);d.line([(2,1),(15,16)],fill='#a95b67',width=2)
  im.save(RES/f'assets/slavicmyths/textures/mob_effect/{id}.png')
 sheet=Image.new('RGBA',(5*120,135),'#d4c2a2');d=ImageDraw.Draw(sheet)
 for n,id in enumerate(NAMES):sheet.alpha_composite(icon(id).resize((96,96),Image.Resampling.NEAREST),(n*120+12,8));d.text((n*120+4,112),id,fill='#201b22')
 sheet.save(ROOT/'docs/media/rare-weapons-1322.png')
 print('Rare weapon resources generated: 5 items, 4 bench recipes, 2 effects.')
if __name__=='__main__':main()
