"""Deterministic pixel clusters for the dedicated #49 native rig (128x64 UV atlas)."""
from pathlib import Path
from PIL import Image, ImageDraw
R=Path(__file__).resolve().parents[1]
PALETTE=dict(skin='#C8D0C6',shade='#AAB8A5',linen='#E8E3D6',worn='#CFC6B4',moss='#8FA27E',hair='#5E6D4E',deep='#37432F',mud='#7E7366',eye='#DFF2E0',dark='#3A3A35')
def generate():
 im=Image.new('RGB',(128,64),PALETTE['dark']);d=ImageDraw.Draw(im)
 def rect(box,c):d.rectangle(box,fill=PALETTE[c])
 def cloth(box):
  x,y,x2,y2=box;rect(box,'linen')
  for a in range(x+2,x2,5):rect((a,min(y+2,y2),a+1,y2),'worn')
  rect((x,y2-2,x2,y2),'worn')
  if x2-x>6:rect((x+1,y2-2,x+3,y2-1),'moss')
 def skin(box):
  x,y,x2,y2=box;rect(box,'skin');rect((x,y,x,y2),'shade');rect((x,y2,x2,y2),'shade')
 def hair(box):
  x,y,x2,y2=box;rect(box,'deep')
  for a in range(x+1,x2,4):rect((a,y,a+1,y2),'hair')
  if x2-x>7:rect((x+4,y+1,x+5,y+3),'moss')
 skin((0,0,27,14));hair((0,0,27,6))
 # Front face follows native Cube UVs: u=7..14, v=7..15.
 rect((7,7,13,7),'hair');rect((7,8,7,13),'shade');rect((13,8,13,13),'shade')
 rect((8,9,9,9),'shade');rect((11,9,12,9),'shade')
 rect((8,10,9,10),'eye');rect((11,10,12,10),'eye');rect((10,11,10,12),'shade');rect((9,13,11,13),'worn')
 cloth((32,0,53,14));rect((36,4,42,4),'worn');rect((39,5,40,5),'skin')
 for box in [(0,18,31,27),(0,30,19,40),(22,30,39,37),(42,30,59,36),(62,18,69,29),(72,18,79,26)]:hair(box)
 rect((84,18,99,19),'deep');rect((86,19,88,19),'moss');rect((93,19,95,19),'hair')
 rect((104,18,107,19),'linen');rect((105,19,105,19),'worn')
 skin((56,0,66,9));cloth((56,0,66,5));skin((68,0,78,7));skin((80,0,90,4))
 skin((92,0,103,13));cloth((92,0,103,7));skin((106,0,121,7));rect((106,7,121,7),'mud')
 for box in [(0,45,10,55),(12,45,22,54),(24,45,34,54),(36,45,46,55),(58,45,69,58),(72,45,83,59),(48,45,55,46)]:cloth(box)
 path=R/'src/main/resources/assets/slavicmyths/textures/entity/rusalka.png';im.save(path,optimize=True)
 return path
if __name__=='__main__':print(generate())
