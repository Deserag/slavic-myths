"""Original scoped trophy sprites; no model/gameplay writes."""
from pathlib import Path
from PIL import Image, ImageDraw
FIX=('kikimora_lock','grave_cloth_scrap','torn_burial_ribbon','mat_banner_remnant','mysterious_black_feather','mor_dust','eye_of_mor','ashen_heart','poloz_core','poloz_head','cursed_volkolak_skull')

def sprite(n):
    assert n in FIX
    size=64 if n=='mat_banner_remnant' else 32 if n in FIX[-3:] else 16
    im=Image.new('RGBA',(size,size));d=ImageDraw.Draw(im)
    if n=='kikimora_lock':
        for points,c in [([(5,2),(9,2),(11,5),(10,9),(7,12),(7,14)],'#343d30'), ([(6,2),(5,5),(7,8),(6,11),(3,13)],'#59634a'), ([(8,3),(9,5),(8,8),(5,10),(4,13)],'#8b8b60'), ([(7,3),(7,6),(9,8),(8,11),(9,13)],'#707753')]:d.line(points,fill=c,width=2 if c=='#343d30' else 1)
        d.line((5,4,10,4),fill='#a99865');d.point((6,13),fill='#a3a17b')
    elif n=='grave_cloth_scrap':
        d.polygon([(2,3),(6,2),(12,3),(13,7),(12,11),(10,11),(9,14),(6,12),(3,13),(3,9),(1,7)],fill='#635c4c')
        d.polygon([(3,4),(7,3),(11,4),(10,8),(11,11),(8,10),(7,12),(4,10)],fill='#a79c7c')
        d.polygon([(4,4),(7,4),(6,8),(4,9)],fill='#c5ba97');d.line([(8,4),(7,8),(9,10)],fill='#81765e');d.line((3,6,5,6),fill='#8f846a');d.point((10,5),fill='#d0c3a0')
    elif n=='torn_burial_ribbon':
        d.polygon([(10,1),(13,2),(11,5),(7,7),(6,9),(10,12),(8,15),(6,13),(3,10),(3,7),(6,4),(9,3)],fill='#605d4b')
        d.polygon([(10,2),(11,2),(9,5),(5,7),(5,9),(9,12),(8,13),(4,10),(4,7),(7,4),(10,3)],fill='#beb28a')
        d.line([(10,3),(7,5),(5,7)],fill='#e0d1a6');d.line([(5,9),(7,11),(8,11)],fill='#8c8063')
    elif n=='mat_banner_remnant':
        d.polygon([(14,6),(48,6),(50,16),(47,26),(48,48),(41,54),(37,47),(28,57),(27,48),(16,53),(17,30),(13,20)],fill='#363b38')
        d.polygon([(18,10),(44,10),(44,27),(43,47),(38,43),(30,50),(29,44),(20,47),(21,28),(17,20)],fill='#747563')
        d.polygon([(21,12),(28,12),(26,39),(21,43),(23,26)],fill='#96947a')
        d.line([(32,16),(32,33),(26,28),(38,28)],fill='#9c5a46',width=3)
        d.line([(18,12),(43,12)],fill='#c0b28d',width=2);d.line([(42,18),(40,34),(41,41)],fill='#50594e',width=2)
    elif n=='mysterious_black_feather':
        d.polygon([(3,14),(4,10),(5,7),(8,3),(12,1),(13,2),(11,7),(8,10),(6,10),(5,13)],fill='#242e35')
        d.polygon([(5,11),(6,7),(10,3),(11,3),(9,7)],fill='#485761')
        d.line([(3,14),(7,8),(12,2)],fill='#8c9796');d.line((7,7,9,5),fill='#667981');d.point((10,7),fill='#121c24')
    elif n=='mor_dust':
        for pts,c in [([(2,11),(4,9),(6,10),(5,12),(3,12)],'#4a303d'), ([(8,10),(10,8),(12,10),(11,12),(9,12)],'#69404f'), ([(5,6),(6,4),(8,5),(7,7)],'#392c3e'), ([(11,4),(12,3),(13,5)],'#75505c'), ([(7,13),(8,12),(9,14)],'#3d3044')]:d.polygon(pts,fill=c)
        d.point((4,10),fill='#94606c');d.point((10,9),fill='#a56a75')
    elif n=='eye_of_mor':
        d.polygon([(1,8),(4,5),(8,4),(12,5),(15,8),(12,10),(8,11),(4,10)],fill='#302832')
        d.polygon([(3,8),(5,6),(10,6),(13,8),(10,9),(5,9)],fill='#9d595c')
        d.polygon([(7,6),(9,6),(10,8),(8,10),(6,8)],fill='#cf7b70');d.line((8,6,8,9),fill='#271d2a');d.point((7,6),fill='#edb799')
    elif n=='ashen_heart':
        d.polygon([(2,5),(4,3),(7,3),(8,5),(10,3),(12,4),(14,7),(12,10),(8,14),(4,11),(2,8)],fill='#29272d')
        d.polygon([(3,5),(5,4),(7,5),(8,7),(11,4),(13,7),(11,10),(8,12),(4,9)],fill='#51474a')
        d.line([(5,5),(6,7),(5,9)],fill='#76625c');d.line([(10,5),(8,8),(10,9),(8,12)],fill='#ad533d');d.line([(9,6),(8,8),(9,9)],fill='#e09653')
    elif n=='poloz_core':
        d.polygon([(9,4),(22,3),(28,11),(26,24),(17,29),(5,23),(3,12)],fill='#443d2e')
        d.polygon([(10,6),(21,5),(25,12),(23,22),(17,26),(8,21),(6,12)],fill='#a38a44')
        d.polygon([(11,9),(20,8),(22,13),(20,21),(16,23),(10,19),(9,13)],fill='#263a35')
        d.line([(12,11),(18,10),(17,14),(13,17),(18,19),(16,22)],fill='#79ad83',width=2)
        d.line([(10,7),(19,6),(22,9)],fill='#d4b865');d.line([(7,15),(8,20),(12,23)],fill='#74643c')
    elif n=='poloz_head':
        d.polygon([(5,9),(9,3),(13,8),(20,7),(25,3),(26,12),(29,17),(25,25),(17,28),(7,25),(3,18)],fill='#3c4534')
        d.polygon([(8,10),(13,11),(20,10),(24,12),(26,18),(23,23),(17,25),(8,23),(6,18)],fill='#7e8a51')
        d.polygon([(9,15),(13,15),(14,17),(9,18)],fill='#d5b965');d.polygon([(20,14),(24,14),(24,17),(19,17)],fill='#d5b965')
        d.line((11,15,11,17),fill='#252d2a');d.line((22,14,22,16),fill='#252d2a')
        d.polygon([(8,20),(23,19),(23,23),(17,25),(10,23)],fill='#29352d')
        d.polygon([(10,20),(13,20),(12,24)],fill='#e2d5a8');d.polygon([(20,20),(23,20),(21,24)],fill='#e2d5a8')
        d.line([(13,11),(16,13),(19,11)],fill='#b3b17a');d.point((17,18),fill='#384533')
    else:
        d.polygon([(7,5),(13,3),(22,5),(26,10),(25,18),(21,20),(20,27),(14,29),(10,24),(6,18),(4,11)],fill='#605f54')
        d.polygon([(8,7),(14,5),(21,7),(23,11),(22,17),(18,19),(18,25),(14,26),(12,21),(8,17),(6,11)],fill='#c5c2a6')
        d.polygon([(8,10),(13,9),(14,13),(11,16),(8,14)],fill='#383b39');d.polygon([(17,9),(22,10),(21,14),(17,15),(16,12)],fill='#383b39')
        d.polygon([(13,17),(17,16),(20,20),(17,23),(12,21)],fill='#969c86');d.polygon([(14,18),(17,18),(17,20),(14,21)],fill='#3f4840')
        d.polygon([(10,19),(12,20),(13,26),(11,24)],fill='#eee5c3');d.polygon([(20,19),(22,18),(21,25),(19,27)],fill='#eee5c3')
        d.line([(14,6),(16,9),(15,11)],fill='#7b826e');d.line([(9,7),(12,6)],fill='#e6ddba')
    return im

def write_owned(path):
    p=Path(path)
    if p.stem not in FIX or not p.as_posix().endswith('/assets/slavicmyths/textures/item/'+p.name):return False
    p.parent.mkdir(parents=True,exist_ok=True);sprite(p.stem).save(p);return True

def apply(root,names=FIX):
    for n in names:assert write_owned(Path(root)/'src/main/resources/assets/slavicmyths/textures/item'/(n+'.png'))

if __name__=='__main__':apply(Path(__file__).resolve().parents[1])
