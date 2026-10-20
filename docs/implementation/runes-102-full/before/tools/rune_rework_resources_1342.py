"""Canonical rune item sprites and counted/component-sensitive anvil recipes; no III effects."""
from pathlib import Path
import json,random,re
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources'
DEFS=[('strength',1,'iron','Силы','Strength'),('speed',1,'gold','Быстроты','Speed'),('resilience',1,'iron','Стойкости','Resilience'),('heat',1,'gold','Огня','Fire'),('crushing',2,'diamond','Сокрушения','Crushing'),('wind',2,'perunite','Ветра','Wind'),('blood',2,'perunite','Крови','Blood'),('fortitude',2,'diamond','Крепости','Fortitude')]
LEGACY=['thunder','forest','midday','shadow','protection','life']
TEXT={
'damage':('Урон ближнего боя: +%s%%','Melee damage: +%s%%'),'attack':('Оружие: +%s%% скорости атаки','Weapon: +%s%% attack speed'),
'movement':('Ботинки: +%s%% скорости движения','Boots: +%s%% movement speed'),'toughness':('Броня: +%s твёрдости','Armor: +%s toughness'),
'shield':('Щит: -%s%% расхода прочности при блоке','Shield: -%s%% durability loss on block'),
'fire':('Оружие: %s%% поджечь на %s с','Weapon: %s%% to ignite for %s s'),'fire_defense':('Броня: -%s%% урона огня/лавы','Armor: -%s%% fire/lava damage'),
'attack_penalty':('Штраф: -%s%% скорости атаки','Penalty: -%s%% attack speed'),'movement_penalty':('Штраф: -%s%% скорости движения','Penalty: -%s%% movement speed'),
'jump':('Ботинки: +%s%% силы прыжка','Boots: +%s%% jump strength'),'projectile':('Лук/арбалет: +%s%% скорости снаряда','Bow/crossbow: +%s%% projectile velocity'),
'bleed':('%s%% кровотечения; до %s стаков, %s HP/стак раз в %s с, %s с','%s%% bleed; up to %s stacks, %s HP/stack every %s s, %s s'),
'kb':('Броня: +%s%% сопротивления отбрасыванию','Armor: +%s%% knockback resistance'),
'incompatible':('Нельзя установить. %s','Cannot install. %s'),'legacy_special':('Сохранён прежний особый эффект','Original special effect preserved'),
'totals':('Итоги надетых рун ограничены общими caps','Equipped rune totals are capped'),
'equipped':('Итого на надетом снаряжении (с пределами):','Equipped totals (after caps):'),
'equipped_movement':('Итог скорости движения: %s%%','Total movement speed: %s%%'),
}
for id,categories,eng in [('strength','Оружие ближнего боя','Melee weapons'),('speed','Оружие ближнего боя, ботинки','Melee weapons, boots'),('resilience','Броня, щиты','Armor, shields'),('heat','Оружие ближнего боя, броня','Melee weapons, armor'),('crushing','Оружие ближнего боя','Melee weapons'),('wind','Ботинки, луки, арбалеты','Boots, bows, crossbows'),('blood','Оружие ближнего боя','Melee weapons'),('fortitude','Броня, щиты','Armor, shields')]:TEXT['compatible_'+id]=(categories,eng)
def write(p,data):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def stone(id,tier):
 im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im);rng=random.Random('rune-'+id)
 points=[(7,4),(19,3),(25,7),(26,23),(22,28),(7,27),(4,23),(5,10)] if tier==1 else [(7,3),(22,3),(27,8),(27,25),(23,28),(7,28),(4,24),(4,8)]
 d.polygon(points,fill='#424344');d.polygon([(8,5),(20,5),(24,8),(24,23),(21,26),(8,25),(6,22),(7,10)],fill='#858580' if tier==1 else '#999b98')
 d.line([(7,22),(7,10),(9,6),(20,5)],fill='#babcb7',width=1 if tier==1 else 2);d.line([(24,9),(24,23),(21,26),(9,26)],fill='#60615e',width=2)
 for n in range(14):
  x=rng.randrange(8,24);y=rng.randrange(7,25);d.rectangle((x,y,x+1,y+1),fill=rng.choice(['#747571','#93958e','#a8aaa2']))
 if tier==1:
  for box in [(4,12,6,14),(22,4,24,5),(10,26,12,28)]:d.rectangle(box,fill=(0,0,0,0))
 colors={'strength':'#7c3433','speed':'#3e7850','resilience':'#495f7b','heat':'#ab6437','crushing':'#aa3940','wind':'#53a5c5','blood':'#ac354b','fortitude':'#8177ab','thunder':'#aa984b','forest':'#4a7151','midday':'#a49a63','shadow':'#53465f','protection':'#576b80','life':'#567e6b'}
 color=colors[id];width=2
 # Dark cut beneath the inset fill; no external aura or skill-card border.
 def line(points,fill=color,w=width):d.line(points,fill='#363535',width=w+2);d.line(points,fill=fill,width=w)
 if id=='strength':line([(10,10),(21,21)]);line([(21,10),(10,21)])
 elif id=='speed':
  line([(10,23),(13,13),(23,8)]);line([(13,15),(22,11)]);line([(12,18),(21,15)]);line([(11,21),(18,19)])
 elif id=='resilience':line([(10,11),(16,9),(22,11),(20,19),(16,23),(12,19),(10,11)])
 elif id=='heat':
  d.polygon([(12,22),(10,17),(16,8),(17,15),(21,13),(22,20),(17,24)],fill='#353433');d.polygon([(13,21),(12,17),(16,11),(17,18),(20,16),(20,20),(17,22)],fill=color)
 elif id=='crushing':
  for end in [(16,8),(24,12),(22,22),(14,25),(8,19),(9,11)]:line([(16,17),end])
 elif id=='wind':line([(23,20),(19,24),(12,22),(9,17),(11,11),(18,9),(22,13),(20,18),(16,19),(14,16),(17,14)])
 elif id=='blood':line([(16,9),(10,19),(12,23),(20,23),(22,19),(16,9)])
 elif id=='fortitude':
  line([(11,10),(11,8),(14,8),(14,10),(18,10),(18,8),(21,8),(21,21),(16,24),(11,21),(11,10)]);line([(16,13),(16,21)])
 elif id=='thunder':line([(19,9),(13,15),(18,16),(12,23)])
 elif id=='forest':line([(11,21),(12,13),(21,10),(20,18),(11,21)]);line([(12,20),(19,13)])
 elif id=='midday':line([(10,20),(10,14),(13,10),(19,10),(22,14),(22,20)]);line([(9,21),(23,21)])
 elif id=='shadow':line([(19,9),(13,11),(10,16),(12,21),(19,23),(16,19),(15,15),(19,9)])
 elif id=='protection':line([(9,12),(16,8),(23,12),(21,21),(16,24),(11,21),(9,12)]);line([(12,16),(20,16)])
 elif id=='life':line([(16,9),(16,23)]);line([(10,13),(16,17),(22,13)]);line([(11,21),(16,18),(21,21)])
 if tier==2:
  # A few luminous pixels remain inside the engraved symbol.
  for x,y in {'crushing':[(16,16),(18,18)],'wind':[(13,11),(18,14)],'blood':[(16,12),(13,20)],'fortitude':[(16,15),(16,18)]}.get(id,[]):d.point((x,y),fill={'crushing':'#e08580','wind':'#b1e4ec','blood':'#e08b92','fortitude':'#c1b9e1'}[id])
 return im
def main():
 for id,tier,base,ru,en in DEFS:
  stone(id,tier).save(RES/f'assets/slavicmyths/textures/item/rune_{id}.png');write(RES/f'assets/slavicmyths/models/item/rune_{id}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/rune_'+id}})
 for id in LEGACY:
  stone(id,1).save(RES/f'assets/slavicmyths/textures/item/rune_{id}.png');write(RES/f'assets/slavicmyths/models/item/rune_{id}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/rune_'+id}})
 for index,lang in enumerate(['ru_ru','en_us']):
  p=RES/f'assets/slavicmyths/lang/{lang}.json';data=json.loads(p.read_text(encoding='utf-8'));data.update({'rune2.slavicmyths.'+k:v[index] for k,v in TEXT.items()})
  for id,tier,base,ru,en in DEFS:data['item.slavicmyths.rune_'+id]=f'Руна {ru.lower()} {"I" if tier==1 else "II"}' if index==0 else f'Rune of {en} {"I" if tier==1 else "II"}'
  write(p,data)
 catalysts={'strength':[('slavicmyths:iron_dust',2),('minecraft:redstone',1)],'speed':[('slavicmyths:gold_dust',2),('minecraft:feather',1)],'resilience':[('slavicmyths:iron_dust',2),('slavicmyths:lapis_dust',1)],'heat':[('slavicmyths:coal_dust',2),('minecraft:flint',1)],'crushing':[('slavicmyths:diamond_dust',2),('slavicmyths:iron_dust',2)],'wind':[('slavicmyths:perunite_dust',2),('minecraft:feather',2)],'blood':[('slavicmyths:perunite_dust',2),('slavicmyths:soul_fragment',2)],'fortitude':[('slavicmyths:diamond_dust',2),('slavicmyths:lapis_dust',2)]}
 for id,tier,base,ru,en in DEFS:
  inputs=[{'ingredient':{'item':f'slavicmyths:blank_rune_{tier}'},'count':1,'base':{'tier':tier,'material':base}}]+[{'ingredient':{'item':item},'count':count} for item,count in catalysts[id]]
  write(RES/f'data/slavicmyths/recipe/rune_rework_{id}.json',{'type':'slavicmyths:rune_crafting','inputs':inputs,'result':{'id':'slavicmyths:rune_'+id,'count':1},'chisel':False})
 # Actual modeled/registered melee families, plus existing shared rune tags; no per-item Java dispatch.
 weapon_models=[p.stem for p in (RES/'assets/slavicmyths/models/item').glob('*.json') if re.fullmatch(r'.*_(sword|dagger|spear|sulitsa|throwing_knife|axe|mace|flail)',p.stem)]
 values=['#minecraft:swords','#minecraft:axes','minecraft:trident','minecraft:mace','#slavicmyths:rune_dagger','#slavicmyths:rune_spear','#slavicmyths:rune_heavy','#slavicmyths:rune_staff','slavicmyths:spear','slavicmyths:flail','slavicmyths:mace','slavicmyths:chekan','slavicmyths:berdysh']+['slavicmyths:'+id for id in sorted(weapon_models)]
 write(RES/'data/slavicmyths/tags/item/rune_melee_weapons.json',{'replace':False,'values':list(dict.fromkeys(values))})
 out=ROOT/'docs/media/runes-rework-1342';out.mkdir(parents=True,exist_ok=True);im=Image.new('RGB',(1040,440),'#211912');d=ImageDraw.Draw(im)
 for n,(id,tier,base,ru,en) in enumerate(DEFS):
  x=n%4*260;y=n//4*220;s=stone(id,tier).resize((160,160),Image.Resampling.NEAREST);im.paste(s,(x+45,y+10),s);d.text((x+16,y+182),f'{en} {tier} / {base}',fill='#e5dac8')
 im.save(out/'runes.png');print('Rune rework: 8 recipes, 6 new canonical IDs, 14 physical stone sprites')
if __name__=='__main__':main()
