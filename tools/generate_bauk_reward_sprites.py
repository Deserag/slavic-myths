# Reproducible original 32x32 item icons for Bauk's wool and cloak.
# Python 3 + Pillow. Independent of Minecraft runtime / resources pipeline.
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "src/main/resources/assets/slavicmyths/textures/item"
P = {"edge":"#1A1519","shade":"#2A1F23","dark":"#3A252B","fur":"#67423F","light":"#9E6451","orange":"#C56C48","scar":"#B74835","blood":"#741F2D","red":"#9C3036","gold":"#D2AD77","bone":"#E5D6AB","tint":"#CF956C"}

def wool(d):
    d.polygon([(5,10),(8,5),(11,7),(13,3),(17,6),(21,4),(25,9),(28,13),(26,18),(29,21),(24,26),(20,24),(16,29),(13,25),(8,27),(6,22),(3,20),(6,15)],fill=P["edge"])
    d.polygon([(7,10),(11,6),(14,9),(17,7),(22,6),(25,12),(26,17),(24,20),(25,23),(20,22),(16,26),(12,23),(9,25),(7,19),(5,19)],fill=P["dark"])
    d.polygon([(11,8),(15,10),(18,8),(23,11),(22,16),(20,20),(17,23),(13,20),(10,22),(8,16)],fill=P["fur"])
    for p in [[(9,12),(11,8),(13,12),(11,17)],[(16,10),(19,8),(21,12),(19,17)],[(12,19),(15,14),(17,19),(15,24)]]: d.polygon(p,fill=P["light"])
    for a,b in [((8,14),(7,18)),((23,11),(24,16)),((14,9),(14,14)),((10,23),(12,25)),((19,20),(21,23))]: d.line([a,b],fill=P["orange"],width=1)
    d.line([(12,11),(15,12),(16,15)],fill=P["scar"],width=1)
    d.line([(18,17),(20,14)],fill=P["bone"],width=1)

def cloak(d):
    d.polygon([(11,2),(20,2),(23,5),(24,10),(27,17),(28,23),(25,28),(22,26),(19,29),(16,27),(13,30),(9,27),(7,29),(4,25),(5,19),(7,12),(7,6)],fill=P["edge"])
    d.polygon([(10,6),(22,6),(23,13),(25,19),(26,24),(23,26),(20,24),(18,27),(15,25),(12,27),(9,24),(7,26),(6,23),(8,16)],fill=P["blood"])
    d.polygon([(15,8),(18,9),(19,16),(20,23),(17,25),(14,21),(13,14)],fill=P["red"])
    d.polygon([(21,11),(23,16),(25,22),(23,24),(20,20)],fill=P["scar"])
    d.line([(8,13),(7,22),(9,24)],fill=P["scar"],width=2)
    d.line([(15,12),(16,22),(15,25)],fill=P["orange"],width=1)
    d.polygon([(6,7),(9,3),(12,5),(15,3),(18,5),(21,3),(25,8),(23,13),(19,11),(17,14),(14,11),(11,13),(8,11)],fill=P["edge"])
    d.polygon([(8,7),(10,4),(13,6),(16,5),(19,7),(22,5),(24,9),(22,11),(19,9),(17,12),(14,9),(11,11),(8,10)],fill=P["fur"])
    d.polygon([(8,8),(10,5),(11,8),(10,10)],fill=P["light"])
    d.polygon([(18,7),(21,5),(23,9),(20,10)],fill=P["light"])
    d.rectangle((13,7,18,10),fill=P["edge"])
    d.rectangle((14,7,17,9),fill=P["gold"])
    d.point((15,8),fill=P["bone"])
    d.line([(10,13),(12,15),(11,18)],fill=P["orange"],width=1)
    d.line([(21,12),(20,16),(22,18)],fill=P["gold"],width=1)
    d.point((8,20),fill=P["bone"]); d.point((23,21),fill=P["tint"])

if __name__ == "__main__":
    OUT.mkdir(parents=True,exist_ok=True)
    for name,paint in [("bauk_wool",wool),("bauk_cloak",cloak)]:
        img=Image.new("RGBA",(32,32),(0,0,0,0))
        paint(ImageDraw.Draw(img))
        img.save(OUT/(name+".png"),optimize=True)
