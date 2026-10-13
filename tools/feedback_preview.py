"""Front views of actual exported baked models, at one common scale (not game frames)."""
from pathlib import Path
import json
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1];O=R/'docs/verification/feedback-1.7.7'
source=(R/'tools/bogatyr_preview.py').read_text(encoding='utf8').split("for who in ['ilya','idol']:")[0]
source=source.replace("('ilya_muromets.png' if key.startswith('ilya') else 'idolishche_poganoe.png')","texture_names[key]")
source=source.replace("scale=9 if key.startswith('ilya') else 2.8;","scale=7.5;")
scope={'__file__':str(R/'tools/bogatyr_preview.py')};exec(source,scope)
meshes={}
for family in ['bogatyr','marinka','skim','finist']:meshes.update(json.loads((R/'work'/family/'meshes.json').read_text()))
names={'ilya_IDLE':'ilya_muromets.png','dobrynya_IDLE':'dobrynya_nikitich.png','alyosha_IDLE':'alyosha_popovich.png','finist_IDLE':'finist.png'}
scope.update(meshes=meshes,texture_names=names)
sheet=Image.new('RGB',(1440,530),(25,27,30));draw=ImageDraw.Draw(sheet)
for i,(key,label) in enumerate(zip(names,['Ilya','Dobrynya','Alyosha','Finist'])):
 sheet.paste(scope['render'](key,0),(i*360,20));draw.text((i*360+145,8),label,fill='white')
draw.text((12,518),'BAKED NATIVE MODELS / COMMON SCALE / NOT A MINECRAFT SCREENSHOT',fill=(210,190,135))
sheet.save(O/'heroes-preview.png');print('Feedback native preview saved')
