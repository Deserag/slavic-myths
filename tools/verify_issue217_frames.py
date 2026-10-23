"""Check actual client framebuffer pixels around every outlined inventory slot."""
from pathlib import Path
from PIL import Image
import json,sys
R=Path(__file__).resolve().parents[1]
O=R/'docs/implementation/weapons87/issue217'
ids=[line.split('\t')[0] for line in (R/'src/main/resources/assets/slavicmyths/creative_catalog.tsv').read_text().splitlines() if '\tweapons\t' in line]
ids+=['retainer_shield','beast_challenge_horn','birch_bark','animal_fat','goat_hide','large_animal_bone','stone_spearhead','stone_light_spearhead','gold_spearhead','gold_light_spearhead','iron_spearhead','iron_light_spearhead','silver_spearhead','silver_light_spearhead','tugarin_head','skim_head','cursed_volkolak_head','likho_head','mor_likho_head','large_podzemnik_head']
rows=[]
for index,name in enumerate(ids):
    page,slot=divmod(index,48)
    path=O/(sys.argv[1] if len(sys.argv)>1 else 'client-lash45-gui-after')/f'inventory-page{page}.png'
    with Image.open(path) as image:
        image=image.convert('RGB');assert image.size==(1280,720)
        x=2*(12+slot%8*76);y=2*(28+slot//8*49)
        outside=sum(image.getpixel((xx,yy))!=(32,40,48) for yy in range(y-8,y+40) for xx in range(x-8,x+40) if xx<x-2 or xx>=x+34 or yy<y-2 or yy>=y+34)
        inside=sum(image.getpixel((xx,yy))!=(96,96,96) for yy in range(y,y+32) for xx in range(x,x+32))
        rows.append(dict(id=name,frame=str(path.relative_to(R)),outside_pixels=outside,item_pixels=inside,status='PASS' if outside==0 and inside>0 else 'FAIL'))
(O/'inventory-frame-bounds.json').write_text(json.dumps({'scope':'Actual 1280x720 GUI framebuffer; no held/use/MP claim','items':rows},indent=2)+'\n')
assert all(row['status']=='PASS' for row in rows),[row for row in rows if row['status']!='PASS']
print(f'PASS {len(rows)} actual item renders: visible pixels, zero spill outside each outlined inventory slot')
