#!/usr/bin/env python3
"""Pure procedural Slavic-fantasy inventory sprites. Five bases; every unique 0..3 sister set.
No AI artwork, external images, dynamic textures, Java or third-party assets.
"""
import argparse
import itertools
import json
import math
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

SIZE=64
NAMES=("sisters_ring","sisters_necklace","sisters_belt","sisters_crown","sisters_brooch")
SISTERS=("tryaseya","gneteya","ledeya","agneya","neveya","lomeya")
COLORS={
"tryaseya":("#ad70ee","#e5c6ff","#49235f"),
"gneteya":("#d7cfc2","#fffaeb","#6f6561"),
"ledeya":("#55c6f6","#cbf8ff","#27637b"),
"agneya":("#fb7031","#ffe39c","#8b271e"),
"neveya":("#8545b3","#db9efa","#382045"),
"lomeya":("#79838f","#d4d4d6","#444855"),
}
INK="#171923"; DEEP="#272530"; BRONZE="#86613f"; BRONZE_D="#49332e"; GOLD="#d1a76d"; GOLD_L="#ffe2a3"; GOLD_M="#ad7f50"; SILVER="#909daa"; SILVER_L="#e3e7e1"; SILVER_D="#4b5662"; BONE="#c9bd9f"; BONE_L="#fff0cc"; RED="#6f3746"; RED_L="#a35c61"; RED_D="#392331"; JADE="#4f807d"; PARCH="#b7aa89"; STONE="#313340";

# Each accessory has three clearly legible stone/rune mounts.
MOUNTS={"sisters_ring":((20,23),(32,14),(44,23)),"sisters_necklace":((20,35),(32,39),(44,35)),"sisters_belt":((18,31),(32,29),(46,31)),"sisters_crown":((19,35),(32,28),(45,35)),"sisters_brooch":((22,21),(42,24),(32,44))}

def draw_base(name):
    im=Image.new("RGBA",(SIZE,SIZE),(0,0,0,0)); d=ImageDraw.Draw(im)
    def ln(points,fill,w=1):d.line(points,fill=fill,width=w,joint='curve')
    def px(x,y,fill):d.point((x,y),fill=fill)
    def bolt(x,y):
        d.rectangle((x-1,y-1,x+1,y+1),fill=BRONZE_D)
        px(x,y,GOLD_L);px(x+1,y+1,SILVER_D)
    def leaf(x,y,angle=0,color=GOLD):
        if angle==0:d.polygon([(x-4,y+2),(x,y-3),(x+3,y-2),(x+2,y+2),(x,y+4)],fill=color,outline=BRONZE_D)
        else:d.polygon([(x-4,y),(x,y-3),(x+4,y),(x,y+3)],fill=color,outline=BRONZE_D)
        px(x-1,y-1,GOLD_L)
    def rune_bar(x,y):
        # Cross-stitch border, stepped for deliberate 1px pixel edges.
        d.rectangle((x-3,y-2,x+3,y+2),fill=BRONZE_D)
        ln([(x-2,y),(x,y-2),(x+2,y),(x,y+2),(x-2,y)],GOLD_M)
        px(x,y,GOLD_L)

    if name=="sisters_ring":
        # Twisted dark-steel band with visible interior and wrought vine shoulders.
        d.ellipse((12,15,53,58),fill=INK)
        d.ellipse((14,17,51,56),fill=SILVER_D)
        d.ellipse((18,19,48,54),fill=SILVER)
        d.ellipse((24,29,43,51),fill=(0,0,0,0))
        d.arc((17,20,48,52),35,152,fill=SILVER_L,width=2)
        d.arc((17,22,48,55),170,315,fill=INK,width=3)
        d.arc((15,18,51,55),215,340,fill=GOLD_M,width=2)
        for a in range(3):
            xx=16+a*4;yy=37+a*5
            ln([(xx,yy),(xx+3,yy-2)],GOLD_M,1)
        for a in range(4):
            xx=46+a//2*2;yy=34+a*4
            px(xx,yy,GOLD_L if a%2==0 else BRONZE)
        # Braided shoulders and central octagonal dark amethyst setting.
        d.polygon([(12,27),(20,15),(27,16),(29,30),(21,33)],fill=INK)
        d.polygon([(16,27),(21,18),(26,19),(28,29),(21,31)],fill=BRONZE)
        d.polygon([(36,16),(44,15),(55,27),(47,33),(35,30)],fill=INK)
        d.polygon([(37,19),(43,18),(51,27),(46,31),(36,29)],fill=BRONZE)
        for side in (-1,1):
            x=32+side*12
            ln([(32,27),(x,28),(x+side*3,31)],GOLD_M,2)
            ln([(32,25),(x,23),(x+side*3,20)],SILVER_L,1)
            bolt(x,32)
        d.polygon([(31,5),(41,13),(38,26),(31,32),(23,25),(22,13)],fill=INK)
        d.polygon([(31,7),(39,14),(36,25),(31,29),(25,24),(24,14)],fill=SILVER)
        d.polygon([(31,10),(36,15),(34,23),(31,26),(27,22),(26,15)],fill=DEEP)
        ln([(26,14),(31,9),(37,15)],SILVER_L,1)
        for x,y in ((29,10),(25,24),(37,23)):px(x,y,GOLD_L)
        for a in range(3):bolt(23+a*8,34+a%2)
        sockets=MOUNTS[name]
    elif name=="sisters_necklace":
        # Cord and bead links suspended around a ceremonial bronze lunula.
        ln([(5,4),(10,14),(15,20),(21,24)],INK,5)
        ln([(59,4),(54,14),(49,20),(43,24)],INK,5)
        ln([(5,4),(10,14),(15,20),(21,24)],GOLD_M,3)
        ln([(59,4),(54,14),(49,20),(43,24)],GOLD_M,3)
        for x,y in ((7,8),(10,13),(14,18),(18,22),(57,8),(54,13),(50,18),(46,22)):
            d.ellipse((x-2,y-2,x+2,y+2),fill=INK)
            d.ellipse((x-1,y-1,x+1,y+1),fill=GOLD_L)
        # Ancient moon pendant: two sweeping forged arms and intricate negative crescent.
        d.polygon([(11,23),(19,27),(28,31),(32,30),(36,31),(45,27),(53,23),(54,38),(50,46),(43,46),(38,42),(32,45),(26,42),(21,46),(14,46),(10,38)],fill=INK)
        d.polygon([(13,25),(21,29),(29,33),(32,32),(35,33),(43,29),(51,25),(52,38),(47,43),(42,43),(37,39),(32,42),(27,39),(22,43),(17,43),(12,38)],fill=GOLD_M)
        d.polygon([(17,30),(25,35),(32,36),(39,35),(47,30),(47,34),(39,40),(32,41),(25,40),(17,34)],fill=INK)
        ln([(13,26),(20,30),(29,34),(35,34),(44,30),(51,26)],GOLD_L,2)
        ln([(13,39),(17,44),(22,44)],BRONZE,1);ln([(42,44),(47,44),(51,39)],BRONZE,1)
        for x in (17,24,32,40,47):
            d.polygon([(x-3,29),(x,26),(x+3,29),(x,32)],fill=INK)
            ln([(x-1,28),(x,27),(x+1,28)],SILVER_L)
        # Three suspended bone-drop pendants.
        for x,y,sz in ((18,48,5),(32,49,7),(46,48,5)):
            ln([(x,y-5),(x,y)],BRONZE,2)
            d.polygon([(x-2,y),(x,y+sz),(x+2,y),(x,y-2)],fill=INK)
            d.polygon([(x-1,y),(x,y+sz-2),(x+1,y)],fill=BONE)
            px(x,y+1,BONE_L)
        sockets=MOUNTS[name]
    elif name=="sisters_belt":
        # Embroidered oxblood leather ceremonial belt, braided silver hardware.
        d.polygon([(3,23),(15,17),(48,17),(60,23),(60,43),(48,47),(15,47),(3,42)],fill=INK)
        d.polygon([(5,24),(15,19),(48,19),(58,24),(58,40),(48,45),(15,45),(5,40)],fill=RED)
        ln([(5,26),(17,21),(48,21),(57,25)],RED_L,2)
        ln([(5,40),(15,43),(48,43),(58,39)],RED_D,3)
        for y in (23,41):
            ln([(8,y),(16,y-1),(48,y-1),(56,y)],GOLD_M,1)
        for x in range(8,57,5):
            # angular northern embroidery, restrained not a random scribble
            off=1 if x%2 else 0
            ln([(x,27+off),(x+2,29+off),(x,31+off),(x+2,33+off)],GOLD_M,1)
            ln([(x,37-off),(x+2,35-off)],RED_L,1)
        for x in (11,52):
            d.rectangle((x-2,25,x+2,39),outline=BRONZE_D,width=2)
            bolt(x,25);bolt(x,39)
        # Three carved wards, middle large shielded buckle.
        d.polygon([(14,24),(22,21),(26,26),(26,38),(20,42),(14,38)],fill=INK)
        d.polygon([(16,25),(21,23),(24,27),(24,37),(20,40),(16,37)],fill=GOLD_M)
        d.polygon([(38,26),(42,21),(50,24),(50,38),(44,42),(38,38)],fill=INK)
        d.polygon([(40,27),(43,23),(48,25),(48,37),(44,40),(40,37)],fill=GOLD_M)
        d.polygon([(25,18),(39,18),(45,26),(44,40),(32,47),(20,39),(20,26)],fill=INK)
        d.polygon([(26,20),(38,20),(42,27),(41,39),(32,44),(23,39),(23,27)],fill=GOLD_L)
        d.polygon([(27,23),(37,23),(39,29),(38,38),(32,41),(26,38),(25,29)],fill=BRONZE_D)
        ln([(26,25),(32,22),(38,25)],GOLD_M,1)
        for x in (7,57):bolt(x,32)
        sockets=MOUNTS[name]
    elif name=="sisters_crown":
        # A light ceremonial silver-and-antler circlet, not a solid helmet.
        d.arc((5,20,59,54),177,359,fill=INK,width=6)
        d.arc((6,20,58,53),182,357,fill=GOLD_M,width=3)
        d.arc((8,21,56,52),190,345,fill=GOLD_L,width=1)
        # Natural antler branching, inset foliage and tiny freely hanging charms.
        for side in (-1,1):
            sx=32+side*18
            ln([(32,32),(sx,24),(sx+side*7,17),(sx+side*9,9)],INK,5)
            ln([(32,32),(sx,24),(sx+side*7,17),(sx+side*9,9)],BONE,3)
            ln([(sx,24),(sx+side*2,12),(sx+side*6,6)],INK,4)
            ln([(sx,24),(sx+side*2,12),(sx+side*6,6)],BONE_L,2)
            ln([(sx+side*3,21),(sx+side*12,20),(sx+side*14,15)],INK,4)
            ln([(sx+side*3,21),(sx+side*12,20),(sx+side*14,15)],BONE,2)
            ln([(sx-2*side,29),(sx+side*4,28)],GOLD_L,1)
            for x,y in ((sx-5*side,23),(sx+3*side,20),(sx+5*side,31)):
                leaf(x,y,1,GOLD if x%2 else BRONZE)
        d.polygon([(26,30),(32,9),(38,30),(33,38),(31,38)],fill=INK)
        d.polygon([(28,29),(32,13),(36,29),(33,35),(31,35)],fill=SILVER)
        ln([(30,26),(32,16),(34,26)],SILVER_L,1)
        d.polygon([(29,7),(32,2),(35,7),(32,12)],fill=BONE)
        # Dangling bone crystals at either side.
        for x,y in ((15,43),(23,47),(41,47),(49,43)):
            ln([(x,y-8),(x,y)],BRONZE,1)
            d.polygon([(x-2,y),(x,y+5),(x+2,y)],fill=INK)
            d.polygon([(x-1,y),(x,y+3),(x+1,y)],fill=BONE)
        for x in (9,55):bolt(x,32)
        sockets=MOUNTS[name]
    elif name=="sisters_brooch":
        # A bronze fibula with three triskelion swirls and an actual working pin.
        d.ellipse((10,8,55,55),fill=INK)
        d.ellipse((12,10,53,53),fill=BRONZE)
        d.ellipse((16,14,49,49),fill=INK)
        d.ellipse((18,16,47,47),fill=STONE)
        for angle in range(0,360,45):
            cx=int(32+24*math.cos(math.radians(angle)))
            cy=int(32+24*math.sin(math.radians(angle)))
            d.polygon([(cx-3,cy-2),(cx,cy-5),(cx+3,cy-2),(cx+2,cy+3),(cx,cy+5),(cx-2,cy+3)],fill=INK)
            d.polygon([(cx-2,cy-1),(cx,cy-3),(cx+2,cy-1),(cx+1,cy+2),(cx,cy+3),(cx-1,cy+2)],fill=BRONZE)
        # Ancient three-curl motif inside ring, cut with bright worn metal edges.
        for ang in (0,120,240):
            rad=math.radians(ang)
            px0=32+9*math.cos(rad);py0=32+9*math.sin(rad)
            points=[]
            for t in range(15):
                theta=rad+t*.42;r=9-t*.43
                points.append((int(round(32+r*math.cos(theta))),int(round(32+r*math.sin(theta)))))
            ln(points,BRONZE_D,5)
            ln(points,GOLD_M,2)
            px(*points[-1],GOLD_L)
        d.ellipse((27,27,37,37),fill=INK)
        d.ellipse((29,29,35,35),fill=GOLD_M)
        px(31,30,GOLD_L)
        # Diagonal fibula pin drawn as a distinct steel element.
        ln([(10,54),(48,12)],INK,6)
        ln([(12,52),(48,12)],SILVER_D,4)
        ln([(13,50),(47,14)],SILVER_L,2)
        d.polygon([(47,13),(51,8),(50,17)],fill=BONE_L)
        d.polygon([(8,56),(13,49),(16,52)],fill=GOLD_M)
        sockets=MOUNTS[name]
    else:raise ValueError(name)

    # Metal prongs are kept on the base icon even before awakening.
    for x,y in sockets:
        if name=="sisters_crown":radius=5
        else:radius=5 if x!=32 else 6
        d.ellipse((x-radius-1,y-radius-1,x+radius+1,y+radius+1),fill=INK)
        d.ellipse((x-radius,y-radius,x+radius,y+radius),fill=GOLD_M)
        d.ellipse((x-radius+2,y-radius+2,x+radius-2,y+radius-2),fill=STONE)
        for dx,dy in ((0,-radius),(radius,0),(0,radius),(-radius,0)):
            px(x+dx,y+dy,GOLD_L)
    return im

def jewel(im,x,y,id):
    d=ImageDraw.Draw(im)
    mid,light,dark=COLORS[id]
    # Each sister gives a different geometric stone cut, engraved glyph and surrounding mark.
    r=4 if (x!=32 or y>=30) else 5
    d.ellipse((x-r,y-r,x+r,y+r),fill=dark)
    if id=="tryaseya":
        d.polygon([(x,y-r),(x+r,y),(x,y+r),(x-r,y)],fill=mid)
        d.arc((x-2,y-3,x+2,y+3),220,40,fill=light,width=2)
        d.point((x+2,y-1),fill=light)
    elif id=="gneteya":
        d.polygon([(x-3,y-1),(x-1,y-4),(x+3,y-2),(x+2,y+3),(x-1,y+3)],fill=mid)
        d.line([(x-2,y-2),(x+2,y+2)],fill=dark,width=1)
        d.line([(x,y-2),(x-1,y+2)],fill=light,width=1)
        d.point((x+2,y-3),fill=light)
    elif id=="ledeya":
        d.polygon([(x,y-4),(x+4,y),(x,y+4),(x-4,y)],fill=mid)
        d.line([(x-3,y),(x+3,y)],fill=light)
        d.line([(x,y-3),(x,y+3)],fill=light)
        d.point((x-2,y-2),fill=light);d.point((x+2,y+2),fill=light)
    elif id=="agneya":
        d.polygon([(x-3,y+3),(x-2,y-2),(x,y-4),(x+1,y-1),(x+3,y+1),(x+2,y+4)],fill=mid)
        d.polygon([(x-1,y+3),(x,y),(x+2,y+3)],fill=light)
    elif id=="neveya":
        d.ellipse((x-3,y-3,x+3,y+3),fill=mid)
        d.ellipse((x,y-4,x+4,y+1),fill=dark)
        d.point((x-2,y-2),fill=light)
        d.point((x-1,y+2),fill=light)
    elif id=="lomeya":
        d.polygon([(x-3,y-3),(x+2,y-4),(x+4,y),(x+1,y+4),(x-4,y+2)],fill=mid)
        d.line([(x-2,y-2),(x+1,y+1),(x-1,y+3)],fill=light)
        d.line([(x+1,y-3),(x+2,y-1)],fill=dark)
    d.point((x-2,y-3),fill=light)
    # Every gem is an actual distinct sister material, not a generic tint.

def awaken(base,name,ids):
    im=base.copy();d=ImageDraw.Draw(im)
    slots=MOUNTS[name]
    for n,id in enumerate(ids):
        x,y=slots[n];jewel(im,x,y,id)
        # Patinated cracks and filigree branches react to the installed sister.
        color=COLORS[id][0]
        if name=="sisters_ring":
            d.point((x-3,y+8),fill=color);d.point((x+3,y+8),fill=color)
        elif name=="sisters_necklace":
            d.point((x-2,y-8),fill=color);d.point((x+2,y-8),fill=color)
        elif name=="sisters_belt":
            d.point((x-3,y+9),fill=color);d.point((x+3,y-9),fill=color)
        elif name=="sisters_crown":
            d.point((x-3,y-9),fill=color);d.point((x+3,y-9),fill=color)
        else:
            d.point((x-5,y+5),fill=color);d.point((x+5,y-5),fill=color)
    return im

def masks():return [i for i in range(1<<len(SISTERS)) if i.bit_count()<=3]

def produce(root,check=False):
    asset=root/"src/main/resources/assets/slavicmyths"
    tex=asset/"textures/item/sisters_artifacts";models=asset/"models/item"
    tex.mkdir(parents=True,exist_ok=True);models.mkdir(parents=True,exist_ok=True)
    entries=[];changed=[]
    for name in NAMES:
        base=draw_base(name)
        model={"parent":"minecraft:item/generated","textures":{"layer0":f"slavicmyths:item/sisters_artifacts/{name}_00"},"overrides":[]}
        for mask in masks():
            ids=tuple(s for i,s in enumerate(SISTERS) if mask & (1<<i))
            suffix=f"{mask:02x}"
            im=awaken(base,name,ids)
            image_path=tex/f"{name}_{suffix}.png"
            if check:
                if not image_path.is_file() or Image.open(image_path).convert("RGBA").tobytes()!=im.tobytes():changed.append(str(image_path))
            else:
                im.save(image_path,optimize=True)
            if mask:
                model["overrides"].append({"predicate":{"slavicmyths:sisters_visual":float(mask)},"model":f"slavicmyths:item/sisters_artifacts/{name}_{suffix}"})
                variant_path=models/"sisters_artifacts"/f"{name}_{suffix}.json"
                variant={"parent":"minecraft:item/generated","textures":{"layer0":f"slavicmyths:item/sisters_artifacts/{name}_{suffix}"}}
                entries.append((variant_path,variant))
        entries.append((models/f"{name}.json",model))
    for p,data in entries:
        s=json.dumps(data,ensure_ascii=False,indent=2)+"\n"
        if check:
            if not p.is_file() or p.read_text(encoding="utf8")!=s:changed.append(str(p))
        else:
            p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s,encoding="utf8")
    assert len(masks())==42
    if changed:raise SystemExit("Outdated/missing sister artifact resources:\n"+"\n".join(changed[:15])+f"\n({len(changed)} differences)")
    print(f"Sister relics 2D: {len(NAMES)} unique sprites, {len(masks())} variants/item, {len(entries)} JSON models, {'PASS' if check else 'generated'}")

def preview(root):
    names_ru=("Кольцо", "Ожерелье", "Пояс", "Венец", "Брошь")
    widths=[300,300,300,300,300]
    w=1480;h=1100;out=Image.new("RGB",(w,h),"#161924");d=ImageDraw.Draw(out)
    fontpath="/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
    fbpath="/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
    try:
        f=lambda sz,b=False:ImageFont.truetype(fbpath if b else fontpath,sz)
    except OSError:
        f=lambda sz,b=False:ImageFont.load_default()
    d.text((34,18),"АРТЕФАКТЫ ШЕСТИ СЕСТЁР  |  ПЛОСКИЕ ИГРОВЫЕ СПРАЙТЫ",font=f(28,True),fill="#e9d3aa")
    d.text((34,60),"Реальные процедурные PNG 64×64 — не AI-арт и не скриншот клиента",font=f(17),fill="#a9abb7")
    for j,n in enumerate(NAMES):
        y=108+j*192
        d.rounded_rectangle((25,y,1455,y+182),radius=10,fill="#262734",outline="#4e4951",width=2)
        d.text((44,y+25),names_ru[j],font=f(27,True),fill="#f1dfb5")
        masks_to_show=(0,1,8,4,1+8+16)
        for i,mask in enumerate(masks_to_show):
            path=root/"src/main/resources/assets/slavicmyths/textures/item/sisters_artifacts"/f"{n}_{mask:02x}.png"
            sprite=Image.open(path).convert("RGBA")
            x=292+i*224
            d.rectangle((x,y+8,x+164,y+168),fill="#4d4e57",outline="#8f8b88",width=3)
            size=128
            zoom=sprite.resize((size,size),Image.Resampling.NEAREST)
            out.paste(zoom,(x+18,y+22),zoom)
            label=("Без сил","Трясея","Агнея","Ледея","3 силы")[i]
            d.text((x+30,y+148),label,font=f(16),fill="#dad5d1")
    outpath=root/"build/sisters-artifact-25-previews/inventory-sprites-64.png"
    outpath.parent.mkdir(parents=True,exist_ok=True);out.save(outpath)
    # Actual 16px Minecraft inventory-scale sampling; 32px upscale adjacent for comparison.
    inv=Image.new("RGB",(1040,380),"#22232b");di=ImageDraw.Draw(inv)
    di.text((16,12),"ПРОВЕРКА МАСШТАБА ИНВЕНТАРЯ · 16×16 игровых пикселей",font=f(21,True),fill="#ead8b6")
    for j,n in enumerate(NAMES):
        for k,mask in enumerate((0,1+8+16)):
            tex=root/"src/main/resources/assets/slavicmyths/textures/item/sisters_artifacts"/f"{n}_{mask:02x}.png"
            icon=Image.open(tex).convert("RGBA").resize((16,16),Image.Resampling.NEAREST)
            px=34+j*197+k*84;py=95
            di.rectangle((px,py,px+58,py+58),fill="#84858c",outline="#393a41",width=3)
            up=icon.resize((48,48),Image.Resampling.NEAREST);inv.paste(up,(px+5,py+5),up)
            di.text((px,py+75),"0" if k==0 else "3",font=f(17),fill="#e1d8c8")
        di.text((25+j*197,210),names_ru[j],font=f(17),fill="#e3cca0")
    inv.save(root/"build/sisters-artifact-25-previews/inventory-scale-16.png")
    print(outpath)

if __name__=="__main__":
    p=argparse.ArgumentParser();p.add_argument("--root",type=Path,default=Path(__file__).resolve().parents[1]);p.add_argument("--check",action="store_true");p.add_argument("--preview",action="store_true")
    args=p.parse_args();produce(args.root,args.check)
    if args.preview:preview(args.root)