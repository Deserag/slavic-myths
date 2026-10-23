"""Actual framebuffer evidence: marker follows the enemy, is above it and absent at the HUD anchor."""
from pathlib import Path
from PIL import Image
import json
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/weapons87/issue217';folder=O/'client-marker224-camera'
def gold(rgb):
 r,g,b=rgb[:3];return r>120 and r>=g and g>b*1.12
rows=[]
for stage in range(5):
 p=folder/f'marker-stage{stage}.png'
 with Image.open(p).convert('RGB') as im:
  assert im.size==(1280,720)
  pixels=[(x,y) for y in range(280,325) for x in range(128,950) if gold(im.getpixel((x,y)))]
  hud=sum(gold(im.getpixel((x,y))) for y in range(342,378) for x in range(654,682))
  row={'stage':stage,'frame':str(p.relative_to(R)),'marker_pixels':len(pixels),'old_hud_anchor_pixels':hud}
  if pixels:row['bounds']=[min(x for x,y in pixels),min(y for x,y in pixels),max(x for x,y in pixels),max(y for x,y in pixels)]
  rows.append(row)
(O/'marker-frame-check.json').write_text(json.dumps(rows,indent=2)+'\n')
assert rows[0]['marker_pixels']>5 and rows[0]['bounds'][2]<540,rows[0]
assert rows[1]['marker_pixels']>5 and rows[1]['bounds'][0]>740,rows[1]
assert all(row['marker_pixels']==0 for row in rows[2:]),rows[2:]
assert all(row['old_hud_anchor_pixels']==0 for row in rows),rows
print('PASS actual world marker above left/right target; absent when off-camera, behind wall or removed; old HUD anchor clear')
