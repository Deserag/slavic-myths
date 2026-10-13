"""Offline rasterization of exported real ModelPart cubes; not a client screenshot."""
from pathlib import Path
import json,math
import numpy as np
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/higher-1.8.2'
meshes=json.loads((R/'work/higher-1.8.2/meshes.json').read_text())
# Reuse the project's existing triangle/UV rasterizer without executing its other previews.
s=(R/'tools/finist_preview.py').read_text();fn=s[s.index('def render('):s.index("for who in ['finist'")]
lines=fn.splitlines();lines[1]=" tex=np.asarray(Image.open(R/'src/main/resources/assets/slavicmyths/textures/entity/mor'/(key.split('_MOR')[0]+'.png')))"
exec('\n'.join(lines))
for name in ('likho','ovinnik'):
 sheet=Image.new('RGBA',(1440,535),(25,27,30,255));d=ImageDraw.Draw(sheet)
 for i,(angle,label) in enumerate([(0,'FRONT'),(90,'SIDE'),(180,'BACK'),(35,'THREE QUARTERS')]):sheet.paste(render(name+'_MOR',angle),(i*360,25));d.text((i*360+130,8),label,fill='white')
 d.text((12,515),'OFFLINE NATIVE MODEL / NOT A GAME SCREENSHOT',fill=(210,190,135));sheet.save(O/(name+'-offline-views.png'))
print('Two greater mor native meshes rendered from baked UVs in four views')
