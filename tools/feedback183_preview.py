"""Actual production native hero views, no alternate mock-up geometry."""
from pathlib import Path
import json,math
import numpy as np
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/feedback-1.8.3'
meshes=json.loads((R/'work/feedback-1.8.3/meshes.json').read_text())
s=(R/'tools/finist_preview.py').read_text();fn=s[s.index('def render('):s.index("for who in ['finist'")]
lines=fn.splitlines();lines[1]=" tex=np.asarray(Image.open(R/'src/main/resources/assets/slavicmyths/textures/entity'/({'ilya':'ilya_muromets.png','alyosha':'alyosha_popovich.png','dobrynya':'dobrynya_nikitich.png','finist':'finist.png'}[key.split('_')[0]])))";exec('\n'.join(lines))
for name in ['ilya','alyosha','dobrynya','finist']:
 sheet=Image.new('RGBA',(1080,535),(25,27,30,255));draw=ImageDraw.Draw(sheet)
 for i,(angle,label) in enumerate([(0,'FRONT'),(90,'SIDE'),(35,'THREE QUARTERS')]):sheet.paste(render(name+'_IDLE',angle),(i*360,25));draw.text((i*360+140,8),label,fill='white')
 draw.text((12,515),'PRODUCTION NATIVE MODEL / NOT A GAME SCREENSHOT',fill=(210,190,135));sheet.save(O/(name+'-actual-model.png'))
print('Production hero previews generated')
