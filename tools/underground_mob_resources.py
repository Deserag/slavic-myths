"""Original voxel atlases and add-only mob resources. Never modifies industry/chest/worldgen assets."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,random
ROOT=Path(__file__).resolve().parent.parent;R=ROOT/'src/main/resources'
def write(path,data):
 p=R/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def save(im,path):
 p=R/path;p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
palettes={'podzemnik':[(105,99,87),(75,66,53),(62,60,54),(68,60,46)],'podzemnik_miner':[(118,113,97),(95,75,54),(63,65,62),(115,83,45)],'kladovik':[(101,84,70),(64,46,46),(71,62,54),(146,113,60)],'large_podzemnik':[(100,102,108),(68,69,74),(40,42,47),(94,84,75)]}
for name,palette in palettes.items():
 im=Image.new('RGBA',(128,128));d=ImageDraw.Draw(im);rng=random.Random(name)
 for y in range(0,128,2):
  for x in range(0,128,2):
   base=palette[0 if x<64 and (y<32 or 64<=y<96) else 1 if x<64 else 2 if y<64 else 3]
   noise=rng.choice([-13,-7,0,0,5,10]);color=tuple(max(0,min(255,c+noise)) for c in base)+(255,)
   d.rectangle((x,y,x+1,y+1),fill=color)
 for i in range(80):
  x=rng.randrange(64,122);y=rng.randrange(0,58);d.line((x,y,x+2,y+4,x,y+7),fill=(40,42,47),width=1)
 hw,hh,hd=(11,11,9) if name=='large_podzemnik' else (9,8,7) if name=='kladovik' else (8,8,7)
 d.rectangle((hd,hd,hd+hw-1,hd+hh-1),fill=palette[0]);eye=(216,123,58) if name=='large_podzemnik' else (216,168,79)
 for x in (hd+1,hd+hw-3):
  d.rectangle((x,hd+2,x+1,hd+3),fill=(32,27,23));d.point((x,hd+3),fill=eye);d.point((x+1,hd+3),fill=(245,212,114))
 d.line((hd+2,hd+hh-2,hd+hw-3,hd+hh-2),fill=(44,36,30),width=1)
 # Cloth seam/stitch panels and tarnished metal, not a flat recolor.
 for y in range(34,59,4):d.line((4,y,30,y),fill=tuple(c+min(25,255-c) for c in palette[1]),width=1)
 d.rectangle((0,48,39,50),fill=(50,36,26));d.rectangle((17,48,20,51),fill=(164,125,61))
 if name=='podzemnik_miner':
  for x in (12,27):d.rectangle((x,34,x+2,46),fill=(113,88,50))
  d.rectangle((100,92,105,95),fill=(132,141,137));d.rectangle((101,93,104,94),fill=(213,201,119))
 if name=='kladovik':
  for y in range(66,92,5):d.line((64,y,100,y),fill=(83,61,34),width=1)
  d.rectangle((96,64,122,67),fill=(186,147,61));d.rectangle((103,65,105,66),fill=(224,180,87))
 if name=='large_podzemnik':
  for x,y in [(73,10),(87,22),(70,38),(95,49)]:d.line((x,y,x+2,y+3,x+1,y+6),fill=(132,85,48),width=1)
 d.rectangle((108,108,127,127),fill=(169,165,144) if name=="large_podzemnik" else (152,146,133));
 save(im,Path(f'assets/slavicmyths/textures/entity/underground/{name}.png'))
 if name in ('kladovik','large_podzemnik'):
  rage=im.copy();dr=ImageDraw.Draw(rage)
  for x in (hd+1,hd+hw-3):dr.rectangle((x,hd+3,x+1,hd+3),fill=(255,153,63))
  if name=='large_podzemnik':
   for x,y in [(73,10),(87,22),(70,38),(95,49)]:dr.line((x,y,x+2,y+3,x+1,y+6),fill=(216,123,58),width=1)
  save(rage,Path(f'assets/slavicmyths/textures/entity/underground/{name}_rage.png'))
im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im);d.polygon([(9,4),(21,3),(28,11),(26,25),(14,29),(5,21),(4,10)],fill=(61,56,67),outline=(29,26,34));d.line([(9,7),(19,5),(25,11),(23,23),(13,26),(7,20)],fill=(94,84,100),width=2);d.line([(10,11),(13,14),(12,19),(16,22),(19,18),(19,13),(23,9)],fill=(174,147,184),width=2);d.line([(13,14),(17,11),(19,13)],fill=(135,113,146),width=2)
save(im,Path('assets/slavicmyths/textures/item/rune_ominous_aura.png'))
write(Path('assets/slavicmyths/models/item/rune_ominous_aura.json'),{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/rune_ominous_aura'}})
for name in ('podzemnik','kladovik','large_podzemnik'):write(Path(f'assets/slavicmyths/models/item/{name}_spawn_egg.json'),{'parent':'minecraft:item/template_spawn_egg'})
soundmap={'kladovik':{'idle':'entity.piglin.ambient','hurt':'entity.piglin.hurt','death':'entity.piglin.death','impact':'block.dripstone_block.break','rage':'entity.piglin.angry','burrow':'block.gravel.break','stone':'block.stone.break','coins':'block.chain.hit'},'large_podzemnik':{'idle':'entity.ravager.ambient','hurt':'entity.ravager.hurt','death':'entity.ravager.death','warning':'entity.ravager.roar','impact':'entity.iron_golem.damage','rage':'entity.ravager.roar','charge':'entity.ravager.attack'},'podzemnik':{'idle':'entity.husk.ambient','hurt':'entity.husk.hurt','death':'entity.husk.death'},'chud':{'work':'block.anvil.hit','panic':'entity.piglin.retreat'}}
p=R/'assets/slavicmyths/sounds.json';sounds=json.loads(p.read_text(encoding='utf-8'))
for mob,events in soundmap.items():
 for action,event in events.items():sounds[f'underground.{mob}.{action}']={'sounds':[{'name':'minecraft:'+event,'type':'event','volume':.65}]}
write(Path('assets/slavicmyths/sounds.json'),sounds)
immune=json.loads((R/'data/slavicmyths/tags/entity_type/control_immune.json').read_text(encoding='utf-8'))['values']
immune=list(dict.fromkeys(immune+['minecraft:warden','minecraft:wither','minecraft:ender_dragon','slavicmyths:kladovik','slavicmyths:large_podzemnik','slavicmyths:chud_guard']))
write(Path('data/slavicmyths/tags/entity_type/ominous_aura_immune.json'),{'replace':False,'values':immune})
def entry(item,count):
 return {'type':'minecraft:item','name':item if ':' in item else 'minecraft:'+item,'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':count[0],'max':count[1]}}]}
def pool(item,count=(1,1),chance=1):
 p={'rolls':1,'entries':[entry(item,count)]}
 if chance<1:p['conditions']=[{'condition':'minecraft:random_chance','chance':chance}]
 return p
def loot(name,pools):write(Path(f'data/slavicmyths/loot_table/entities/{name}.json'),{'type':'minecraft:entity','pools':pools})
loot('podzemnik',[pool('coal',(0,2),.45),pool('iron_nugget',(0,1),.2),pool('slavicmyths:ancient_coin',(0,1),.08)])
loot('podzemnik_miner',[pool('iron_nugget',(0,2),.5),pool('coal',(1,1),.35),pool('slavicmyths:purification_flux',(1,1),.06),pool('iron_pickaxe',(1,1),.04)])
loot('kladovik',[pool('slavicmyths:ancient_coin',(4,8)),pool('gold_ingot',(2,5)),pool('diamond',(2,4),.35),pool('netherite_scrap',(1,1),.07),pool('slavicmyths:mining_rune_1',(1,1),.15)])
loot('large_podzemnik',[pool('diamond',(2,4)),pool('slavicmyths:purified_iron_ingot',(1,2),.4),pool('netherite_scrap',(1,1),.15),pool('slavicmyths:mining_rune_2',(1,1),.2),pool('slavicmyths:rune_ominous_aura',(1,1),.3)])
for lang in ('ru_ru','en_us'):
 p=R/f'assets/slavicmyths/lang/{lang}.json';data=json.loads(p.read_text(encoding='utf-8'));ru=lang=='ru_ru'
 for name,russian,english in [('podzemnik','РџРѕРґР·РµРјРЅРёРє','Podzemnik'),('podzemnik_miner','РџРѕРґР·РµРјРЅРёРє-С€Р°С…С‚С‘СЂ','Podzemnik Miner'),('kladovik','РљР»Р°РґРѕРІРёРє','Kladovik'),('large_podzemnik','РљСЂСѓРїРЅС‹Р№ РџРѕРґР·РµРјРЅРёРє','Large Podzemnik')]:
  data['entity.slavicmyths.'+name]=russian if ru else english
  if name!='podzemnik_miner':data['item.slavicmyths.'+name+'_spawn_egg']=('РЇР№С†Рѕ РїСЂРёР·С‹РІР°: '+russian) if ru else english+' Spawn Egg'
 data['item.slavicmyths.rune_ominous_aura']='Р СѓРЅР° Р—Р»РѕРІРµС‰РµР№ Р°СѓСЂС‹' if ru else 'Rune of Ominous Aura'
 data['effect.slavicmyths.underground_knockdown']='РЎР±РёС‚ СЃ РЅРѕРі' if ru else 'Knocked Down'
 data['industry.slavicmyths.hint.rune_ominous_aura']='РўСЂРѕС„РµР№ РљСЂСѓРїРЅРѕРіРѕ РџРѕРґР·РµРјРЅРёРєР°. РЈСЃС‚Р°РЅР°РІР»РёРІР°РµС‚СЃСЏ С‡РµСЂРµР· СЂСѓРЅРёС‡РµСЃРєСѓСЋ РЅР°РєРѕРІР°Р»СЊРЅСЋ.' if ru else 'Large Podzemnik trophy. Install through the runic anvil.'
 data['rpg.slavicmyths.effect_ominous_aura']='Р”РІРµ Р°РєС‚РёРІРЅС‹Рµ СЂСѓРЅС‹: РѕР±С‹С‡РЅР°СЏ РІСЂР°Р¶РґРµР±РЅР°СЏ РЅРµС‡РёСЃС‚СЊ РґРѕ 50 РјР°РєСЃ. HP РЅРµ РЅР°РїР°РґР°РµС‚ РїРµСЂРІРѕР№. РЎРёР»СЊРЅС‹Рµ, РѕСЃРѕР±С‹Рµ СЃСѓС‰РµСЃС‚РІР° Рё РїСЂРѕРІРѕС†РёСЂРѕРІР°РЅРЅС‹Рµ РІСЂР°РіРё РЅРµ РїРѕРґС‡РёРЅСЏСЋС‚СЃСЏ.' if ru else 'Two active runes: unprovoked ordinary hostile creatures with up to 50 max HP do not attack first. Strong, special and provoked enemies are unaffected.'
 data['rpg.slavicmyths.rune_hint_ominous_aura_1']='РћРґРЅР° СЂСѓРЅР°: РїРµС‡Р°С‚СЊ РЅРµ Р·Р°РІРµСЂС€РµРЅР°' if ru else 'One rune: the seal is incomplete'
 data['rpg.slavicmyths.rune_hint_ominous_aura_2']=data['rpg.slavicmyths.effect_ominous_aura']
 data['rpg.slavicmyths.rune_hint_ominous_aura_3']='РўРѕР»СЊРєРѕ СѓСЃС‚Р°РЅРѕРІР»РµРЅРЅС‹Рµ СЂСѓРЅС‹ РІ СЂСѓРєР°С… Рё РЅР°РґРµС‚РѕР№ Р±СЂРѕРЅРµ; СЂСЋРєР·Р°Рє РЅРµ СЃС‡РёС‚Р°РµС‚СЃСЏ.' if ru else 'Only installed runes on held equipment and worn armor count; backpacks do not.'
 write(Path(f'assets/slavicmyths/lang/{lang}.json'),data)
print('Underground mob assets generated; industry/chest/worldgen untouched')
