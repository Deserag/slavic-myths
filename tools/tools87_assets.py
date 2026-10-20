"""Three targeted native16 tool head repairs; original handles and JSON transforms stay.
Original pixels are frozen source data, not read back from current production output.
"""
from pathlib import Path
import json
from PIL import Image,ImageDraw
R=Path(__file__).resolve().parents[1]
SOURCE=json.loads(Path(__file__).with_name('tools87_original.json').read_text())
IDS=tuple(SOURCE)
def image(id):
 im=Image.new('RGBA',(16,16));im.putdata([tuple(p) for p in SOURCE[id]]);d=ImageDraw.Draw(im)
 # Head region only; all pixels y>=6 are preserved exactly.
 d.rectangle((0,0,15,5),fill=(0,0,0,0))
 dark='#263749';mid='#64717c';hi='#8fd9e5';steel='#9db1bb'
 if id=='perunite_pickaxe':
  d.polygon([(3,2),(5,1),(10,1),(12,2),(14,4),(14,5),(12,4),(10,3),(7,3),(5,4),(3,5),(2,4)],fill=dark)
  d.line([(3,3),(5,2),(10,2),(12,3),(13,4)],fill=mid)
  d.line([(5,1),(9,1),(10,2)],fill=steel);d.point((7,2),fill=hi)
  d.rectangle((10,4,11,5),fill='#be8c55')
 elif id=='perunite_shovel':
  d.polygon([(11,0),(13,0),(14,2),(13,4),(11,5),(9,3),(9,2)],fill=dark)
  d.polygon([(11,1),(12,1),(13,2),(12,4),(11,4),(10,3)],fill=mid)
  d.line([(11,1),(10,2),(11,3)],fill=hi);d.point((12,2),fill=steel)
 else:
  d.polygon([(6,1),(12,1),(13,2),(12,4),(11,5),(9,5),(10,3),(6,3)],fill=dark)
  d.line([(6,1),(11,1),(12,2)],fill=steel);d.line([(7,2),(10,2)],fill=mid)
  d.point((9,2),fill=hi);d.point((11,3),fill=mid);d.point((10,4),fill=mid)
 return im

def write_owned(path):
 p=Path(path)
 if p.stem not in IDS or not p.as_posix().endswith('/assets/slavicmyths/textures/item/'+p.stem+'.png'):return False
 image(p.stem).save(p);return True

def apply(root=R):
 for id in IDS:write_owned(Path(root)/f'src/main/resources/assets/slavicmyths/textures/item/{id}.png')
if __name__=='__main__':apply()
