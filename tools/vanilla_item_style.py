"""Authored 16px GUI sprites for the audited high-resolution item families.

Existing IDs, held meshes and UV atlases are preserved. Weapons: sword 13px
steel blade/short leather grip; spear 15px wood shaft/3px iron head; chekan
9px shaft/7px transverse iron pick. Armor scraps are flat metal fragments,
cloth is a folded sheet, vessels are squat earthenware, belts are horizontal
leather strips. Rings, horns and necklaces have their own open silhouettes.
Shield: 12px tall wood face with an iron rim and a central boss.
No existing bitmap is resampled: these are reproducible pixel drawings.
"""
from pathlib import Path
import json
from PIL import Image, ImageDraw

ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/slavicmyths'
FAMILIES={
 'sword':['ancient_carolingian_sword','restored_carolingian_sword'],
 'spear':['ancient_spear','restored_spear'],
 'pick':['ancient_chekan','chekan'],
 'necklace':['ancient_beads','ancient_fibula','grivna','lunula','volkhv_amulet','obereg_ohotnika','obereg_padayuschey_zvezdy','odnoglazyy_obereg','zolny_obereg','vetrovoy_uzel'],
 'belt':['volchiy_poyas','poyas_tugarina'],
 'vessel':['pustoy_ritualny_sosud','sosud_ognennogo_dyhaniya','sosud_podveya'],
 'potion':['lesnoy_nastoy_vosstanovleniya','yagin_nastoy_stoykosti','otvar_bodrosti','otvar_lesnoy_zorkosti','otvar_ochishcheniya'],
 'bone':['klyk_volkolaka','kost_likha','upyr_fang','nochnoy_kogot'],
 'thread':['nit_durnoy_doli','uzel_durnoy_doli','uzel_podveya','vihrevaya_nit'],
 'essence':['nav_essence'],
 'feather':['ognennoe_pero'],
 'moustache':['us_tugarina'],
 'cloth':['grave_cloth_scrap','torn_burial_ribbon','grave_ward'],
 'metal':['old_arrowhead','shield_boss_fragment','naboyka_tugarina','voevoda_insignia','druzhinnik_blade_fragment'],
 'shield':['retainer_shield'],
 'token':['bandit_token'],
 'buckle':['old_buckle','zmeinaya_pryazhka'],
 'seal':['princely_seal'],
 'scale':['pylayuschaya_cheshuya','tugarinova_kozha'],
 'ash':['ovinnaya_zola','iskra_ovinnika'],
 'bag':['meshochek_trofeev'],
 'ointment':['letuchaya_maz'],
 'horn':['ohotnichiy_rog'],
 'eye':['oko_likha'],
 'ball':['putevodny_klubok'],
 'comb':['ancient_comb'],
 'pottery':['pottery_fragment'],
 'herbs':['svyazka_trav_yagi'],
 'banner':['stepnoy_shtandart'],
}
NAMES=tuple(name for family in FAMILIES.values() for name in family)
DARK='#302D2D';STEEL='#A0AAA8';LIGHT='#DFE2D1';WOOD='#795536';GOLD='#B89953';RED='#923A32';BONE='#D7C9A0'

def sprite(kind,name):
 from trophy111_art import FIX as t2_ids, sprite as t2_sprite
 if name in t2_ids:return t2_sprite(name)
 from trophy110_art import IDS as trophy_ids, sprite as trophy_sprite
 if name in trophy_ids:return trophy_sprite(name)
 im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
 if kind=='sword':
  d.line((3,13,12,4),fill=DARK,width=4);d.line((6,10,12,4),fill=STEEL,width=3);d.line((8,8,13,3),fill=LIGHT);d.line((3,9,7,13),fill=GOLD,width=2);d.line((2,14,5,11),fill=WOOD,width=2)
 elif kind=='spear':
  d.line((1,14,10,5),fill=DARK,width=3);d.line((2,14,11,5),fill=WOOD);d.polygon([(9,5),(14,0),(14,4),(11,7)],fill=DARK);d.line((10,5,13,2),fill=LIGHT,width=2)
 elif kind=='pick':
  d.line((5,14,8,5),fill=DARK,width=3);d.line((6,14,9,5),fill=WOOD);d.polygon([(3,3),(8,2),(13,5),(12,8),(9,5),(3,5)],fill=DARK);d.line((4,3,8,3,12,6),fill=STEEL,width=2)
 elif kind=='necklace':
  d.line([(3,1),(2,5),(4,9),(7,11),(11,9),(13,5),(12,1)],fill=DARK,width=3);d.line([(3,1),(3,5),(5,8),(8,10),(11,8),(12,5),(12,1)],fill=GOLD)
  d.rectangle((6,9,10,14),fill=DARK);d.rectangle((7,10,9,13),fill=RED if 'zolny' in name or 'zvezdy' in name else STEEL)
  if 'beads' in name:
   for x,y in [(3,4),(4,7),(7,9),(10,8),(12,5)]:d.rectangle((x,y,x+1,y+1),fill=RED)
  if 'odnoglazyy' in name:d.point((8,11),fill=GOLD)
 elif kind=='belt':
  d.rectangle((1,5,14,10),fill=DARK);d.rectangle((2,6,13,9),fill=WOOD);d.rectangle((6,4,10,11),fill=GOLD);d.rectangle((7,6,9,9),fill=DARK);d.line((8,6,8,9),fill=STEEL);d.point((12,8),fill=BONE)
 elif kind=='shield':
  d.polygon([(2,2),(13,2),(12,11),(8,15),(3,11)],fill=DARK);d.polygon([(3,3),(12,3),(11,10),(8,13),(4,10)],fill=STEEL);d.polygon([(4,4),(11,4),(10,10),(8,12),(5,10)],fill=RED);d.rectangle((7,5,8,10),fill=GOLD);d.line((5,7,10,7),fill=GOLD)
 elif kind=='token':
  d.polygon([(5,2),(11,2),(14,6),(13,11),(9,14),(4,13),(1,8)],fill=DARK);d.polygon([(5,3),(10,3),(13,6),(12,10),(9,12),(5,12),(3,8)],fill='#747A6C');d.line([(5,5),(9,6),(7,8),(10,10)],fill=RED,width=2)
 elif kind in ('vessel','potion'):
  c=RED if 'ognenn' in name else '#637E88' if 'podvey' in name else WOOD
  d.rectangle((6,1,10,3),fill=DARK);d.rectangle((7,1,9,2),fill=WOOD);d.polygon([(5,4),(11,4),(12,6),(13,12),(11,14),(5,14),(3,12),(4,6)],fill=DARK);d.polygon([(6,4),(10,4),(11,7),(12,12),(10,13),(5,13),(4,11),(5,7)],fill=c if kind=='vessel' else '#A5B390');d.rectangle((6,8,10,11),fill=BONE);d.point((8,9),fill=RED);d.line((5,6,5,9),fill=LIGHT)
  if kind=='potion':d.line((5,12,10,12),fill={'lesnoy_nastoy_vosstanovleniya':RED,'otvar_ochishcheniya':'#709FA6','otvar_bodrosti':'#C48B48','otvar_lesnoy_zorkosti':'#658944','yagin_nastoy_stoykosti':GOLD}[name])
 elif kind=='bone':
  d.polygon([(3,1),(7,2),(11,7),(10,11),(5,14),(7,10),(6,7),(2,4)],fill=DARK);d.polygon([(3,2),(6,3),(9,7),(9,10),(7,12),(8,9),(5,5),(3,4)],fill=BONE);d.line((4,3,7,6),fill=LIGHT)
 elif kind=='thread':
  c='#87A7AE' if 'podvey' in name or 'vihrev' in name or 'nav_' in name else RED
  d.line([(6,1),(10,4),(4,7),(11,10),(8,14)],fill=DARK,width=3);d.line([(6,1),(10,4),(4,7),(11,10),(8,14)],fill=c);d.line((6,8,9,8),fill=LIGHT)
 elif kind=='essence':
  d.polygon([(8,1),(8,5),(12,8),(10,14),(4,13),(2,9),(6,5)],fill=DARK);d.line([(7,3),(5,8),(9,10),(7,13)],fill='#84BCC6',width=2);d.point((6,8),fill=LIGHT)
 elif kind=='moustache':
  d.line([(1,10),(3,8),(6,10),(8,8),(10,10),(13,8),(14,10)],fill=DARK,width=4);d.line([(1,9),(3,8),(6,10),(8,8),(10,10),(13,8),(14,9)],fill=BONE,width=2)
 elif kind=='feather':
  d.polygon([(3,12),(4,7),(11,1),(14,1),(13,6),(7,12)],fill=DARK);d.polygon([(4,11),(6,6),(12,2),(13,2),(12,6),(7,11)],fill='#C77935' if 'ognen' in name else BONE);d.line((2,14,12,4),fill=GOLD)
 elif kind=='cloth':
  d.polygon([(2,4),(7,2),(13,5),(12,13),(7,11),(2,13)],fill=DARK);d.polygon([(3,5),(7,3),(12,6),(11,12),(7,10),(3,12)],fill='#9B8C78');d.line((7,4,7,9),fill=LIGHT);d.line((4,7,10,8),fill=RED)
 elif kind=='metal':
  d.polygon([(3,6),(8,2),(13,6),(12,12),(7,14),(2,10)],fill=DARK);d.polygon([(4,6),(8,3),(12,6),(11,11),(7,12),(3,9)],fill=STEEL);d.line((5,6,9,4),fill=LIGHT);d.line((7,9,10,7),fill=WOOD)
 elif kind=='buckle':
  d.rectangle((2,3,13,12),fill=DARK);d.rectangle((3,4,12,11),fill=GOLD);d.rectangle((5,6,10,9),fill=(0,0,0,0));d.line((8,4,8,9),fill=STEEL)
 elif kind=='seal':
  d.ellipse((2,2,13,13),fill=DARK);d.ellipse((3,3,12,12),fill=GOLD);d.line([(5,5),(10,5),(7,8),(10,10),(5,10)],fill=WOOD,width=2);d.point((4,4),fill=BONE)
 elif kind=='scale':
  d.polygon([(1,5),(5,2),(12,3),(14,8),(11,13),(5,14),(2,10)],fill=DARK);d.polygon([(2,6),(6,3),(11,4),(13,8),(10,12),(5,12),(3,9)],fill=RED);d.line((4,6,10,5),fill='#C58854');d.line((5,9,11,8),fill=WOOD)
 elif kind=='ash':
  d.polygon([(1,12),(4,8),(7,10),(11,6),(14,12),(13,14),(2,14)],fill=DARK);d.line((4,11,7,12,10,9),fill='#65635E',width=2);d.point((10,11),fill=RED);d.point((12,10),fill=GOLD)
 elif kind=='bag':
  d.rectangle((5,2,10,4),fill=WOOD);d.line((4,5,11,5),fill=GOLD,width=2);d.polygon([(5,5),(10,5),(13,9),(12,13),(4,14),(2,10)],fill=DARK);d.polygon([(5,6),(10,6),(11,9),(11,12),(4,12),(3,10)],fill=WOOD);d.line((6,7,5,11),fill='#B48B57')
 elif kind=='ointment':
  d.rectangle((3,4,12,6),fill=DARK);d.rectangle((4,4,11,5),fill=WOOD);d.rectangle((2,7,13,13),fill=DARK);d.rectangle((3,7,12,12),fill='#8C7044');d.rectangle((5,8,10,11),fill=BONE);d.line((7,8,8,10),fill='#61723F')
 elif kind=='horn':
  d.polygon([(1,8),(3,6),(6,9),(11,8),(14,3),(14,10),(11,13),(6,14),(2,12)],fill=DARK);d.polygon([(2,8),(3,7),(6,10),(11,9),(13,6),(12,10),(10,12),(6,12),(3,10)],fill=BONE);d.line((4,8,4,11),fill=GOLD,width=2)
 elif kind=='eye':
  d.line((5,1,8,3),fill=WOOD);d.ellipse((2,4,13,13),fill=DARK);d.ellipse((3,5,12,12),fill=GOLD);d.ellipse((5,6,10,11),fill=BONE);d.rectangle((7,7,8,10),fill=DARK)
 elif kind=='ball':
  d.ellipse((3,2,13,12),fill=DARK);d.ellipse((4,3,12,11),fill=RED);d.line([(5,4),(11,7),(5,9),(10,10)],fill='#D2995C');d.line([(5,11),(3,14),(1,14)],fill=RED)
 elif kind=='comb':
  d.rectangle((2,3,13,7),fill=DARK);d.rectangle((3,4,12,6),fill=BONE)
  for x in range(3,13,2):d.line((x,7,x,12),fill=BONE)
 elif kind=='pottery':
  d.polygon([(2,3),(12,2),(14,7),(10,13),(4,14),(1,8)],fill=DARK);d.polygon([(3,4),(11,3),(12,7),(9,12),(4,12),(2,8)],fill='#A56840');d.line([(4,6),(10,5),(9,9),(5,10)],fill=GOLD)
 elif kind=='herbs':
  d.line((7,15,8,2),fill=WOOD,width=2)
  for x,y in [(4,3),(9,2),(2,7),(10,6),(5,10)]:d.rectangle((x,y,x+3,y+2),fill='#586A3D');d.point((x,y),fill='#A8AC68')
  d.line((5,12,10,12),fill=RED)
 elif kind=='banner':
  d.line((3,1,3,14),fill=DARK,width=2);d.line((3,1,3,14),fill=WOOD);d.polygon([(4,2),(13,3),(11,6),(13,9),(4,9)],fill=DARK);d.polygon([(5,3),(11,4),(9,6),(11,8),(5,8)],fill=RED);d.rectangle((6,4,7,6),fill=GOLD)
 else:raise ValueError(kind)
 # Distinct shapes inside families: the drawing must still identify its purpose.
 if name in ('grivna','ancient_fibula'):
  im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);d.arc((2,2,13,13),-45,225,fill=DARK,width=3);d.arc((3,3,12,12),-45,225,fill=GOLD,width=2)
  if name=='ancient_fibula':d.line((3,11,12,4),fill=STEEL)
 elif name=='lunula':
  d.ellipse((5,8,11,14),fill=GOLD);d.rectangle((6,7,10,10),fill=(0,0,0,0))
 elif name=='obereg_padayuschey_zvezdy':
  d.polygon([(8,8),(9,10),(12,10),(10,12),(10,14),(8,13),(6,14),(6,12),(4,10),(7,10)],fill=GOLD)
 elif name=='obereg_ohotnika':
  d.line([(5,9),(6,12),(8,14),(10,12),(11,9)],fill=BONE);d.line((4,11,6,11),fill=BONE);d.line((10,11,12,11),fill=BONE)
 elif name=='volkhv_amulet':
  d.polygon([(8,9),(12,14),(4,14)],fill=GOLD);d.line((8,11,8,13),fill=DARK)
 elif name=='odnoglazyy_obereg':
  d.ellipse((4,9,12,14),fill=GOLD);d.ellipse((6,10,10,13),fill=BONE);d.line((8,10,8,13),fill=DARK)
 elif name=='vetrovoy_uzel':
  d.line([(5,9),(11,13),(9,14),(6,10),(11,10),(5,14)],fill='#A4B7B3',width=2)
 elif name=='zolny_obereg':
  d.ellipse((5,9,11,15),fill='#59534A');d.rectangle((7,11,9,13),fill=RED)
 elif name=='zmeinaya_pryazhka':
  d.line([(5,6),(10,6),(10,8),(5,8),(5,10),(9,10)],fill=RED)
 elif name=='kost_likha':
  im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);d.line((4,12,12,4),fill=DARK,width=4);d.line((4,12,12,4),fill=BONE,width=2)
  for x,y in [(2,11),(4,13),(10,2),(12,4)]:d.rectangle((x,y,x+2,y+2),fill=BONE)
 elif name=='druzhinnik_blade_fragment':
  im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);d.polygon([(3,13),(3,9),(8,4),(10,5),(12,3),(13,5),(8,10),(5,13)],fill=DARK);d.line((4,11,10,5),fill=STEEL,width=2);d.point((7,8),fill=LIGHT)
 elif name=='old_arrowhead':
  im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);d.polygon([(8,1),(13,10),(9,9),(9,14),(6,14),(6,9),(2,10)],fill=DARK);d.polygon([(8,3),(11,8),(8,7),(7,12),(7,7),(4,8)],fill=STEEL)
 elif name=='shield_boss_fragment':
  im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);d.ellipse((1,3,14,13),fill=DARK);d.ellipse((2,4,13,12),fill=STEEL);d.ellipse((5,4,10,10),fill='#6C7776');d.line((5,5,8,4),fill=LIGHT)
 elif name=='voevoda_insignia':
  d.polygon([(8,3),(11,6),(8,10),(5,6)],fill=GOLD);d.line((8,5,8,8),fill=RED)
 elif name=='torn_burial_ribbon':
  im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);d.polygon([(2,3),(13,1),(13,4),(5,6),(10,11),(7,15),(5,12),(1,6)],fill=DARK);d.line([(3,4),(11,3),(3,6),(8,12)],fill=BONE,width=2)
 elif name.startswith('ancient_') and kind in ('sword','spear','pick'):
  d.point((11,5),fill=WOOD);d.point((9,7),fill=WOOD)
 return im

def apply():
 for kind,names in FAMILIES.items():
  for name in names:
   model=A/'models/item'/f'{name}.json'
   assert model.is_file(),name
   data=json.loads(model.read_text(encoding='utf8'))
   assert f'slavicmyths:item/{name}' in json.dumps(data), 'Not a GUI sprite: '+name
   sprite(kind,name).save(A/'textures/item'/f'{name}.png')
 from trophy111_art import FIX as t2_ids, write as write_t2
 for name in t2_ids:write_t2(name,A/'textures/item')

if __name__=='__main__':
 apply();print('VANILLA_ITEM_STYLE_APPLIED count='+str(len(NAMES)))
