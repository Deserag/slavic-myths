"""Publish truthful current asset previews; no fictional gameplay or AI artwork."""
from pathlib import Path
import shutil, subprocess, sys
from PIL import Image, ImageDraw

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'docs/media/readme'
OUT.mkdir(parents=True,exist_ok=True)
(ROOT/'work').mkdir(exist_ok=True)
for script in ['preview_gorodishche_125.py','preview_buildings_124.py']:
    subprocess.run([sys.executable,str(ROOT/'tools'/script)],cwd=ROOT,check=True)
for source,target in [('gorodishche-125-geometry.png','gorodishche_schematic.png'),('village-buildings-1.2.4-preview.png','village_schematic.png')]:
    shutil.copy2(ROOT/'work'/source,OUT/target)
names=['rye_grain','barley_grain','oat_grain','flax_stalk','flax_fiber','linen_thread','linen_cloth','rolling_pin','metal_pot','wooden_mug','dark_bottle','hops','malted_barley','bandit_token','rye_bread','fruit_pomace']
sheet=Image.new('RGB',(1000,660),'#e6dfce');draw=ImageDraw.Draw(sheet)
draw.text((24,18),'SLAVIC MYTHS 1.2.5 / actual item texture assets / nearest-neighbor',fill='#30281e')
for i,name in enumerate(names):
    path=ROOT/f'src/main/resources/assets/slavicmyths/textures/item/{name}.png'
    if not path.is_file():raise FileNotFoundError(path)
    im=Image.open(path).convert('RGBA');im.thumbnail((104,104),Image.Resampling.NEAREST)
    if max(im.size)<104:im=im.resize((im.width*max(1,104//max(im.size)),im.height*max(1,104//max(im.size))),Image.Resampling.NEAREST)
    x=30+(i%4)*245;y=58+(i//4)*145
    sheet.paste(im,(x+(185-im.width)//2,y),im)
    draw.text((x,y+110),name,fill='#30281e')
sheet.save(OUT/'item_assets.png')
print('Published 2 NBT schematics and 16 actual item textures')
