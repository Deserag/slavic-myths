"""Side-by-side source renders from actual native vertex/UV captures. Never game screenshots."""
from pathlib import Path
import json,math,copy
from PIL import Image,ImageDraw
from navya_entity_assets import render
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/p0-bosses-327'
VIEWS=[('front',0),('left',math.pi/2),('back',math.pi),('top',0),('three-quarter',-.65)]
def view(frame,texture,label,angle,scale):
 f=copy.deepcopy(frame)
 for face in f['faces']:
  for p in face:
   if label=='top':p[1],p[2]=p[2]+24,-(p[1]-24)
   if label!='three-quarter':p[1]+=(-p[0]*math.sin(angle)+p[2]*math.cos(angle))*.24
 return render(f,texture,size=360,scale=scale,angle=angle)
def main():
 before=json.loads((O/'native-before/models.json').read_text());after=json.loads((O/'native-after/models.json').read_text())
 for name,scale in [('grove_guardian',2.8),('zharenitsa',3.6),('magma_lord',3.2)]:
  sheet=Image.new('RGB',(360*len(VIEWS),400*2),'#18171f')
  for row,(label,data) in enumerate([('before',before),('after',after)]):
   tex=Image.open(O/f'native-{label}/{name}.png').convert('RGBA');frame=data[name]['frames'][0]
   for col,(camera,angle) in enumerate(VIEWS):im=view(frame,tex,camera,angle,scale);ImageDraw.Draw(im).text((8,28),f'{name} {label} / {camera}',fill='white');sheet.paste(im,(360*col,400*row+15))
  sheet.save(O/f'{name}-native-before-after.png')
  tex=Image.open(O/f'native-after/{name}.png').convert('RGBA');frames=[view(f,tex,'three-quarter',-.65,scale) for f in after[name]['frames']]
  frames[0].save(O/f'{name}-native-poses.gif',save_all=True,append_images=frames[1:],duration=250,loop=0)
 sheet=Image.new('RGB',(360*5,400*6),'#18171f')
 for row,name in enumerate(['sheptun','oskolnik','gulokryl','ekhovik','griboshmyg','posten']):
  tex=Image.open(O/f'native-after/{name}.png').convert('RGBA')
  for col,(camera,angle) in enumerate(VIEWS):im=view(after[name]['frames'][0],tex,camera,angle,4 if name!='posten' else 3.6);ImageDraw.Draw(im).text((8,28),f'{name} / {camera}',fill='white');sheet.paste(im,(360*col,row*400+15))
 sheet.save(O/'halls-native-five-views.png')
 print('Native source renders: three before/after comparisons, three pose GIFs, six Halls rigs. ART original comparison and real game capture NOT_RUN.')
if __name__=='__main__':main()
