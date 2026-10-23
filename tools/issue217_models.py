"""Restore original solid shield atlas; separate original icons from held surface UV."""
from pathlib import Path
import copy,json,random
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';S=R/'tools/weapon-art-original/issue217'
def write(name,data):(A/'models/item'/(name+'.json')).write_text(json.dumps(data,indent=2)+'\n')
def old(name):return json.loads((S/(name+'.json')).read_text())

CROSSBOWS=['serpent_crossbow','three_breath_crossbow']
STATES=['','_pulling_0','_pulling_1','_pulling_2','_arrow','_firework']
def crossbow_string(name):
 m=old(name);base=copy.deepcopy(m['elements'][7]);suffix=next((s for s in STATES[1:] if name.endswith(s)),'')
 draw={'':0,'_pulling_0':.8,'_pulling_1':1.8,'_pulling_2':3.2,'_arrow':3.2,'_firework':3.2}[suffix]
 # Constant .3-unit thickness; connected short segments form the V instead of a broad slab.
 rope=[]
 for i in range(32):
  x=i*.5;z=10+draw*(1-abs((x+.25)-8)/8);e=copy.deepcopy(base);e['from']=[x,6,z];e['to']=[x+.5,6.3,z+.3];rope.append(e)
 if not draw:rope=[base]
 m['elements']=m['elements'][:7]+rope+m['elements'][8:];return m

def original_crossbow(name,current):
 """Only undo the exact documented rope delta for historical geometry guards."""
 before=old(name)
 if current==before:return current
 assert current==crossbow_string(name),(name,'unexpected change beyond #222 rope')
 return before
def separate(name,held):return {'loader':'neoforge:separate_transforms','base':{'parent':'slavicmyths:item/'+held},'perspectives':{ctx:{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+name}} for ctx in ['gui','ground','fixed']},'textures':{'particle':'slavicmyths:item/'+name}}
def shield_atlas():
 from PIL import Image,ImageDraw
 im=Image.new('RGBA',(256,256));d=ImageDraw.Draw(im);rng=random.Random('retainer-shield-rc2')
 def shade(c,v):return tuple(max(0,min(255,n+v)) for n in c)+(255,)
 for y in range(64):
  for x in range(256):
   tile=x//64;px=x%64;base=[(148,40,29),(121,80,42),(113,117,116),(136,141,141)][tile];v=rng.randrange(-7,8)
   if tile in [0,1] and px%11==0:v-=20
   im.putpixel((x,y),shade(base,v))
 cream=(228,218,182,255)
 for points in [[(32,5),(40,14),(32,23),(24,14)],[(32,41),(40,50),(32,59),(24,50)],[(5,32),(14,24),(23,32),(14,40)],[(41,32),(50,24),(59,32),(50,40)]]:d.polygon(points,outline=cream,width=3)
 d.line((32,17,32,47),fill=cream,width=3);d.line((17,32,47,32),fill=cream,width=3)
 for x in range(66,128,11):d.line((x,0,x,63),fill=(69,43,27,255));d.line((x+1,0,x+1,63),fill=(159,110,61,255))
 for y in [0,63]:d.line((128,y,191,y),fill=(177,178,164,255),width=2)
 d.ellipse((195,3,252,60),fill=(89,97,99,255),outline=(211,212,194,255),width=3);d.ellipse((205,13,242,50),fill=(151,157,157,255),outline=(195,199,190,255),width=2)
 return im
def apply():
 from PIL import Image
 axe=old('thunder_axe');gui=axe['display']['gui'];ratio=.65/gui['scale'][0];gui['scale']=[.65]*3;gui['translation']=[v*ratio for v in gui['translation']];write('thunder_axe',axe)
 for bow in CROSSBOWS:
  for suffix in STATES:write(bow+suffix,crossbow_string(bow+suffix))
 shield_atlas().save(A/'textures/item/retainer_shield_surface.png')
 for name in ['retainer_shield','retainer_shield_body']:
  m=old(name);m['textures']['0']='slavicmyths:item/retainer_shield_surface'
  if name=='retainer_shield':m.pop('overrides',None);write('retainer_shield_held',m)
  else:write(name,m)
 write('retainer_shield_blocking_held',old('retainer_shield_blocking'))
 root=separate('retainer_shield','retainer_shield_held');root['overrides']=old('retainer_shield')['overrides'];write('retainer_shield',root)
 write('retainer_shield_blocking',separate('retainer_shield','retainer_shield_blocking_held'))
 horn=old('beast_challenge_horn');horn['textures']['horn']='slavicmyths:item/beast_challenge_horn_surface'
 # Opaque material tiles replace sampling through the transparent icon background.
 im=Image.new('RGBA',(16,16));palette=[(215,197,148,255),(188,166,115,255),(105,60,37,255),(145,104,51,255)]
 for y in range(16):
  for x in range(16):
   c=palette[x//4];delta=[-8,0,5,-2][y%4];im.putpixel((x,y),tuple(max(0,min(255,v+delta)) for v in c[:3])+(255,))
 im.save(A/'textures/item/beast_challenge_horn_surface.png')
 for index,element in enumerate(horn['elements']):
  tile=2 if index>=8 else 3 if index in [1,5] else index%2
  for face in element['faces'].values():face['uv']=[tile*4,0,tile*4+4,16]
 write('beast_challenge_horn_held',horn);write('beast_challenge_horn',separate('beast_challenge_horn','beast_challenge_horn_held'))
if __name__=='__main__':apply()
