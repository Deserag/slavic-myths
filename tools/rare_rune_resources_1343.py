"""Reproducible dark-stone abstract glyphs, four distinct fragments and additive source loot."""
from pathlib import Path
import json,random
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1];RES=ROOT/'src/main/resources'
RUNES={'floating_weapon':('Руна парящего оружия','Rune of Floating Weapon','#d4a15c'),'flight':('Руна полёта','Rune of Flight','#5baad0'),'rare_protection':('Руна защиты','Rune of Protection','#6cc0a3'),'death':('Руна смерти','Rune of Death','#cc5255'),'berserker':('Руна берсерка','Rune of Berserker','#df8048'),'sacrifice':('Руна жертвы','Rune of Sacrifice','#b779d6')}
SOURCES={'nightingale':('flight',3),'tugarin_zmey':('berserker',1),'likho_one_eyed':('death',1),'kurgan_voevoda':('rare_protection',1)}
TEXT={
'rare':('Редкая руна • одна копия на предмет','Rare rune • one copy per item'),'boots':('Только ботинки','Boots only'),'chest':('Только нагрудник','Chestplate only'),'melee':('Оружие ближнего боя','Melee weapons'),
'source_floating_weapon':('Источник: обмен у Бабы Яги','Source: Baba Yaga exchange'),'source_flight':('Источник: Соловей-разбойник','Source: Nightingale the Robber'),'source_rare_protection':('Источник: курганный воевода','Source: Kurgan Voevoda'),'source_death':('Источник: Лихо одноглазое','Source: One-Eyed Likho'),'source_berserker':('Источник: Тугарин','Source: Tugarin'),'source_sacrifice':('Источник: обмен у Бабы Яги','Source: Baba Yaga exchange'),
'effect_floating_weapon':('Активная: до 6 с, 3 атаки по 70% физического урона; перезарядка 35 с','Active: up to 6 s, 3 attacks at 70% physical damage; cooldown 35 s'),
'effect_flight':('Активная: полёт 5 с, скорость ×1,35; защита от падения 3 с; перезарядка 45 с','Active: flight 5 s, speed ×1.35; fall grace 3 s; cooldown 45 s'),
'effect_rare_protection':('Три щита поглощают по прямому удару; до 20 с; перезарядка после окончания 60 с','Three shields each absorb one direct hit; up to 20 s; cooldown after depletion/expiry 60 s'),
'effect_death':('Добивание цели с ≤5 HP; цена 12 HP, ваше HP >12; перезарядка 20 с','Execute at ≤5 HP; costs 12 HP, requires your HP >12; cooldown 20 s'),
'effect_berserker':('HP ≤50/30/15%: урон +10/20/30%, скорость атаки +5/10/15%; защита −5/12/20%','HP ≤50/30/15%: damage +10/20/30%, attack speed +5/10/15%; defense −5/12/20%'),
'effect_sacrifice':('Активная: цена 6 HP, ваше HP >6; 8 с: урон +25%, скорость атаки +15%; перезарядка 40 с','Active: costs 6 HP, requires your HP >6; 8 s: damage +25%, attack speed +15%; cooldown 40 s'),
'conflict':('Конфликт копий: действует одна совместимая редкая руна','Duplicate conflict: only one compatible rare rune is active'),'cooldown':('Перезарядка: %s с','Cooldown: %s s'),'ready':('Готово','Ready'),'key':('%s — использовать; Shift — выбрать','%s — use; Shift — cycle'),'fragment':('3 одинаковых осколка → руна, только на наковальне','3 matching fragments → rune, runic anvil only')}
def write(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def stone(id):
 im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im);rng=random.Random('rare-'+id);d.polygon([(15,2),(20,5),(25,12),(27,22),(23,28),(16,30),(8,27),(4,21),(6,13),(11,5)],fill='#25262b');d.polygon([(15,4),(19,7),(23,13),(24,23),(20,27),(12,27),(7,21),(9,12)],fill='#42434a');d.line([(15,4),(11,9),(8,16),(7,21)],fill='#92929a',width=2)
 for n in range(27):
  x,y=rng.randrange(8,24),rng.randrange(8,26);d.point((x,y),fill=rng.choice(['#30323a','#565660','#65656b']))
 c=RUNES[id][2]
 def line(p):d.line(p,fill='#16151d',width=4);d.line(p,fill=c,width=2)
 if id=='floating_weapon':line([(16,7),(15,23),(16,26)]);line([(11,10),(9,14),(10,20)]);line([(21,10),(23,15),(21,21)]);line([(13,15),(18,16)])
 elif id=='flight':line([(13,8),(16,11),(20,7)]);line([(16,11),(11,15),(12,21),(18,24),(21,20),(18,17),(15,19),(16,21)])
 elif id=='rare_protection':line([(12,9),(9,14),(10,23),(20,25),(23,20)]);line([(17,10),(12,13),(12,20),(19,22),(21,17)]);line([(18,13),(15,15),(15,19),(18,19)]);d.rectangle((16,16,17,17),fill=c)
 elif id=='death':line([(12,12),(9,17),(11,22),(16,24)]);line([(21,22),(23,17),(20,12)]);line([(16,7),(16,23)]);line([(16,23),(12,27)]);line([(16,23),(20,27)])
 elif id=='berserker':line([(10,8),(13,13),(10,17),(14,21),(12,25)]);line([(22,8),(19,13),(22,17),(18,21),(20,25)]);line([(14,16),(18,19)])
 elif id=='sacrifice':line([(11,10),(8,17),(11,23),(20,24),(24,18),(22,12)]);line([(16,6),(16,12)]);line([(10,16),(13,17)]);line([(22,18),(19,17)]);line([(16,22),(16,27)]);line([(16,14),(19,17),(16,20),(13,17),(16,14)])
 d.point((16,17),fill='#f0d9eb');return im
def fragment(id):
 full=stone(id);mask=Image.new('L',(32,32));d=ImageDraw.Draw(mask);points={'flight':[(8,10),(16,7),(20,13),(17,22),(8,20)],'rare_protection':[(10,17),(22,12),(25,22),(20,27),(11,26)],'death':[(14,5),(21,8),(23,17),(15,19),(11,14)],'berserker':[(6,14),(14,11),(19,17),(16,26),(8,24)]}[id];d.polygon(points,fill=255);out=Image.new('RGBA',(32,32));out.paste(full,(0,0),mask);return out
def main():
 ids={}
 for id,(ru,en,c) in RUNES.items():ids['rune_'+id]=(ru,en,stone(id))
 for id in ['flight','rare_protection','death','berserker']:ids['rune_'+id+'_fragment']=('Осколок '+RUNES[id][0].lower(),'Fragment of '+RUNES[id][1],fragment(id))
 for id,(ru,en,im) in ids.items():
  im.save(RES/f'assets/slavicmyths/textures/item/{id}.png');write(RES/f'assets/slavicmyths/models/item/{id}.json',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+id}})
 for index,lang in enumerate(['ru_ru','en_us']):
  p=RES/f'assets/slavicmyths/lang/{lang}.json';v=json.loads(p.read_text(encoding='utf-8'));v.update({'item.slavicmyths.'+id:names[index] for id,names in ids.items()});v.update({'rare_runes.slavicmyths.'+k:value[index] for k,value in TEXT.items()});v['key.slavicmyths.rare_rune']=['Редкая руна (Shift — выбор)','Rare rune (Shift — cycle)'][index]
  for kind in ['echo','execute']:v['death.attack.rune_'+kind]=['%s пал от редкой руны','%s fell to a rare rune'][index];v['death.attack.rune_'+kind+'.player']=['%s пал от редкой руны %s','%s fell to %s\'s rare rune'][index]
  write(p,v)
 for entity,(id,count) in SOURCES.items():
  p=RES/f'data/slavicmyths/loot_table/entities/{entity}.json';v=json.loads(p.read_text());v['pools']=[pool for pool in v['pools'] if pool.get('name','').startswith('slavicmyths:rare_rune_') is False]
  for suffix,item,n,chance in [('guaranteed','rune_'+id+'_fragment',count,1),('extra','rune_'+id+'_fragment',1,.25),('whole','rune_'+id,1,.04)]:
   pool={'name':'slavicmyths:rare_rune_'+suffix,'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:'+item,'functions':[{'function':'minecraft:set_count','count':n}]}]}
   if chance<1:pool['conditions']=[{'condition':'minecraft:random_chance','chance':chance}]
   v['pools'].append(pool)
  write(p,v)
 for id in ['flight','rare_protection','death','berserker']:write(RES/f'data/slavicmyths/recipe/rare_rune_restore_{id}.json',{'type':'slavicmyths:rune_crafting','inputs':[{'ingredient':{'item':'slavicmyths:rune_'+id+'_fragment'},'count':3}],'result':{'id':'slavicmyths:rune_'+id,'count':1},'chisel':False})
 for kind in ['echo','execute']:write(RES/f'data/slavicmyths/damage_type/rune_{kind}.json',{'message_id':'rune_'+kind,'scaling':'never','exhaustion':.1})
 for tag in ['bypasses_armor','bypasses_shield','bypasses_cooldown']:
  p=RES/f'data/minecraft/tags/damage_type/{tag}.json';v=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]};v['values']=list(dict.fromkeys(v['values']+['slavicmyths:rune_execute']));write(p,v)
 # Conservative immunity for actual scripted phased bosses; ordinary mobs remain executable.
 write(RES/'data/slavicmyths/tags/entity_type/rune_death_immune.json',{'replace':False,'values':['slavicmyths:nightingale','slavicmyths:likho_one_eyed','slavicmyths:tugarin_zmey','slavicmyths:kurgan_voevoda','slavicmyths:unresting_prince']})
 shield=Image.new('RGBA',(32,32));d=ImageDraw.Draw(shield);d.polygon([(4,5),(16,2),(28,5),(25,23),(16,30),(7,23)],fill='#6683a866',outline='#c4cef2cc');d.line([(16,5),(16,25)],fill='#c4cef2aa',width=2);d.line([(8,12),(23,12)],fill='#c4cef299',width=1);p=RES/'assets/slavicmyths/textures/entity/rune_shield.png';p.parent.mkdir(parents=True,exist_ok=True);shield.save(p)
 cracked=shield.copy();d=ImageDraw.Draw(cracked);d.line([(16,2),(13,10),(20,14),(12,20),(16,29)],fill=(0,0,0,0),width=2);d.line([(20,14),(27,9)],fill=(0,0,0,0),width=2);cracked.save(p.with_name('rune_shield_cracked.png'))
 out=ROOT/'docs/media/rare-runes-1343';out.mkdir(parents=True,exist_ok=True);sheet=Image.new('RGB',(960,360),'#201b24');d=ImageDraw.Draw(sheet)
 for n,(id,(ru,en,im)) in enumerate(ids.items()):x=(n%6)*160;y=(n//6)*180;image=im.resize((128,128),Image.Resampling.NEAREST);sheet.paste(image,(x+16,y+6),image);d.text((x+5,y+145),id.replace('rune_',''),fill='#ddd4df')
 sheet.save(out/'items.png');print('6 rare runes, 4 named fragments, 4 restorations, 4 source loot overlays')
if __name__=='__main__':main()
