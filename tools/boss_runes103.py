"""Ten source-specific trophy runes. Native integer pixels, no runtime/VFX changes.
Only explicit IDS are writable. Never invokes legacy model/audio exporters.
"""
from pathlib import Path
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[1]
IDS=('stomp','dispelling','pursuit','suppression','battle_line','mor_breath','dragon_flame','storm_strike','earth_heart_iii','earth_lord_iv')
SIZES={id:(64 if id in IDS[:5] or id=='earth_heart_iii' else 32) for id in IDS}

def image(id):
 size=SIZES[id];s=size//32;im=Image.new('RGBA',(size,size));d=ImageDraw.Draw(im)
 def poly(points,c):d.polygon([(x*s,y*s) for x,y in points],fill=c)
 def line(points,c,w=1):d.line([(x*s,y*s) for x,y in points],fill=c,width=w*s)
 def rect(box,c):d.rectangle(tuple(v*s for v in box),fill=c)
 def stone(points,base,shade,light):
  poly(points,'#141922');cx=sum(x for x,y in points)/len(points);cy=sum(y for x,y in points)/len(points)
  inner=[(round(x+(cx-x)*.14),round(y+(cy-y)*.14)) for x,y in points];poly(inner,base)
  line(inner[:max(3,len(inner)//2)],light);line(inner[max(3,len(inner)//2)-1:]+[inner[0]],shade)
 def glyph(points,color,hi,w=2):
  line(points,'#17202b',w+2);line(points,color,w);line(points[:-1],hi,max(1,w-1))
 if id=='stomp':
  stone([(7,4),(18,2),(26,6),(28,15),(25,19),(27,24),(19,29),(8,27),(3,22),(4,15),(2,11)],'#68604e','#393d38','#aaa18a')
  # Heavy heel impact, opposed ground waves, amber fracture.
  poly([(12,7),(19,7),(19,14),(23,16),(22,19),(11,19),(10,16),(12,14)],'#e2ad55')
  line([(6,20),(9,22),(15,23),(23,22),(26,20)],'#dfaa50',2)
  line([(8,25),(15,27),(23,25)],'#a87537')
  line([(15,20),(14,24),(17,27),(16,30)],'#f6ce79')
 elif id=='dispelling':
  stone([(7,4),(17,2),(23,6),(21,11),(18,12),(20,16),(15,20),(16,27),(8,28),(3,21),(4,11)],'#624768','#302d42','#ab84b4')
  stone([(24,10),(29,12),(28,21),(22,26),(19,22),(23,18)],'#765681','#393047','#d1a1d4')
  glyph([(9,9),(15,7),(19,9),(17,13),(12,16),(10,22)],'#b76bc8','#edb4e9')
  line([(7,14),(20,19)],'#efd2ef',2)
  poly([(25,2),(28,5),(25,8),(23,5)],'#c58ad8');poly([(25,27),(28,28),(26,31)],'#aa6ac4')
 elif id=='pursuit':
  # Long asymmetric spear/trail: genuinely different from a medallion.
  stone([(29,2),(27,15),(22,14),(16,21),(13,20),(6,29),(4,26),(10,17),(9,14),(18,8),(18,5)],'#a97832','#5f4a2c','#f4ce79')
  poly([(25,6),(24,12),(20,12),(12,20),(12,17),(20,10)],'#f8da88')
  line([(18,17),(10,26)],'#d5a856',2)
  line([(5,13),(10,8),(15,6)],'#7d653d',2);line([(3,20),(7,15)],'#c8a35d',2)
 elif id=='suppression':
  stone([(9,3),(22,3),(28,9),(27,23),(20,29),(10,28),(4,23),(3,10)],'#3d324d','#242a36','#7a648f')
  # Two massive links, horizontal lock bar. No generic diamond glyph.
  line([(10,8),(7,12),(7,17),(10,20),(14,20),(17,17),(17,12),(14,8),(10,8)],'#ad86c3',2)
  line([(18,12),(15,16),(15,21),(18,24),(22,24),(25,21),(25,16),(22,12),(18,12)],'#9070ad',2)
  line([(5,15),(26,15)],'#d4bbdf',2);rect((13,12,18,18),'#645076');rect((15,14,16,17),'#e7c8e8')
 elif id=='battle_line':
  # Rigid ancient standard, projecting crossbar and forked hem.
  poly([(6,3),(26,3),(28,6),(24,8),(24,29),(16,25),(8,29),(8,8),(4,6)],'#201c27')
  poly([(9,7),(23,7),(23,25),(16,22),(9,26)],'#8e3438')
  line([(9,7),(9,25)],'#cf6b52',2);line([(23,9),(23,25),(16,22)],'#512d36',2)
  rect((5,4,26,6),'#bc9150');rect((8,3,10,7),'#e6c778');rect((22,3,24,7),'#e6c778')
  line([(16,9),(16,21)],'#edc879',2);line([(11,12),(21,12)],'#edc879',2)
  poly([(12,20),(12,15),(14,15),(14,18),(18,18),(18,15),(20,15),(20,20)],'#e0b15a')
 elif id=='mor_breath':
  # Coiled serpent relic, common forked horn + eye across Gorynych collection.
  stone([(8,2),(14,5),(20,3),(26,8),(28,15),(24,20),(26,26),(18,29),(10,27),(4,22),(3,14)],'#393a3e','#242b32','#737a68')
  poly([(8,4),(9,10),(13,7)],'#a7ab8d');poly([(20,4),(18,10),(23,8)],'#a7ab8d')
  glyph([(10,13),(15,10),(21,12),(23,17),(20,21),(13,22),(10,19),(12,17),(17,17)],'#749b60','#b2c480',2)
  rect((18,12,20,13),'#e2d8a1');line([(9,22),(6,25),(9,27)],'#a082bc',2)
  line([(23,18),(27,20),(26,24),(29,25)],'#8964a2',2)
 elif id=='dragon_flame':
  stone([(9,2),(16,5),(22,2),(26,9),(25,14),(29,21),(24,27),(16,30),(7,27),(3,21),(7,14),(5,9)],'#543b36','#2e292f','#9c6b48')
  poly([(9,4),(10,10),(13,7)],'#c29c6a');poly([(21,4),(19,10),(24,8)],'#c29c6a')
  poly([(16,8),(20,13),(19,16),(23,15),(24,20),(20,25),(13,26),(8,22),(9,17),(12,19),(12,13)],'#d7562b')
  poly([(16,12),(18,17),(17,20),(20,20),(18,24),(13,23),(12,21),(15,18)],'#ffc86a')
  line([(11,12),(16,10),(21,12)],'#e4b076',2);rect((18,12,20,13),'#fff0af')
 elif id=='storm_strike':
  # Winged/horned serpent silhouette carries a central lightning fracture.
  stone([(4,3),(12,6),(18,3),(27,7),(24,12),(29,15),(23,21),(21,27),(14,30),(10,23),(4,25),(7,17),(2,13)],'#354959','#24313d','#8bb5c5')
  poly([(6,5),(8,11),(12,8)],'#a8ced7');poly([(19,5),(18,11),(23,8)],'#a8ced7')
  line([(9,13),(14,10),(21,12),(24,15),(21,18)],'#679cae',2);rect((19,12,21,13),'#e2f6ff')
  poly([(17,7),(12,17),(16,17),(11,27),(23,14),(18,14),(21,8)],'#8edcff')
  line([(17,8),(14,16),(18,16),(14,23)],'#e0f8ff')
 elif id=='earth_heart_iii':
  stone([(8,4),(18,2),(20,6),(26,8),(29,17),(25,24),(22,28),(12,30),(5,26),(3,17),(5,9)],'#65584a','#353b37','#b09b7d')
  rect((6,20,9,23),'#586a48');rect((23,11,25,14),'#78805a')
  glyph([(10,23),(22,23),(22,11),(10,11),(10,20),(19,20),(19,14),(13,14),(13,17),(16,17)],'#c4833d','#f2c474',2)
  line([(9,6),(13,7),(14,10)],'#a18c6c');line([(22,25),(20,28)],'#9a7e58')
 elif id=='earth_lord_iv':
  # Crowned S serpent seal, open outer shape instead of hexagonal pendant.
  stone([(5,4),(11,7),(16,2),(21,7),(28,4),(25,13),(28,18),(25,26),(18,30),(8,28),(4,24),(7,20),(12,22),(19,23),(22,19),(20,16),(13,16),(7,13)],'#37634e','#203d36','#83ae79')
  line([(7,6),(11,10),(16,5),(21,10),(26,6)],'#d0ad5c',2)
  glyph([(12,11),(19,11),(23,14),(24,20),(20,25),(12,25),(8,23)],'#c19b4f','#edce80',2)
  poly([(12,10),(17,10),(18,14),(13,15),(10,13)],'#679b67');rect((13,11,15,12),'#f9df87')
 return im

def write_owned(path):
 p=Path(path);id=p.stem.removeprefix('rune_')
 if id not in IDS or not p.as_posix().endswith('/assets/slavicmyths/textures/item/rune_'+id+'.png'):return False
 p.parent.mkdir(parents=True,exist_ok=True);image(id).save(p);return True

def apply(root=ROOT):
 for id in IDS:write_owned(Path(root)/f'src/main/resources/assets/slavicmyths/textures/item/rune_{id}.png')
if __name__=='__main__':apply()
