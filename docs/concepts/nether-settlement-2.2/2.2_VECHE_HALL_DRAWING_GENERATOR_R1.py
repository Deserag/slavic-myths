#!/usr/bin/env python3
# Rebuild technical SVG reference sheets directly from 3598-coordinate R1 model.
# Python standard library only. NO world changes, not an NBT/GameTest.
import collections
import importlib.util
from pathlib import Path

ROOT = Path(__file__).resolve().parent
SPEC = importlib.util.spec_from_file_location("veche_voxel_source", ROOT / "2.2_VECHE_HALL_VOXELS_R1.py")
mod = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(mod)
VOX = mod.decode()
BLOCKS = {(t["u"], t["y"], t["v"]): t["symbol"] for t in VOX}
COLORS = {
    "B":"#596979","C":"#b9b4ae","F":"#8b98a6","G":"#78b8c6",
    "H":"#dc9f82","K":"#aa8066","N":"#ba7456","P":"#cfab8e",
    "Q":"#d6b75f","R":"#983f40","S":"#d77c58","T":"#c8a487",
    "U":"#c59b72","V":"#794d45","X":"#c76b58",
}
BG,EMPTY,GRID,TEXT="#101923","#21303e","#435969","#f5ebd8"
ROOMS=[
("R01 Летописная",(1,1,5,5)),("R02 Старейшина",(7,1,19,5)),("R03 Награды",(21,1,25,5)),
("R04 Лестница",(1,8,5,14)),("R05 Вечевой зал",(7,7,19,14)),("R06 Приём наград",(21,7,25,14)),
("R07 Писцовая",(1,16,5,19)),("R08 Сени",(7,16,19,19)),("R09 Стража",(21,16,25,19)),
("R10 Верхняя летописная",(1,1,5,5)),("R11 Верхний совет",(7,1,19,5)),("R12 Реликвии",(21,1,25,5)),
("R13 Лестница II",(1,7,5,14)),("R14 Галерея",(7,7,19,14)),("R15 Дозорная",(21,7,25,14)),
("R16 Отдых",(1,16,5,19)),("R17 Писцовая II",(7,16,19,19)),("R18 Стража II",(21,16,25,19))
]

def esc(s):
    return str(s).replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
def tile(x,y,c,size):
    return '<rect x="%s" y="%s" width="%s" height="%s" fill="%s" stroke="%s" stroke-width=".4"/>'%(x,y,size,size,COLORS.get(c,EMPTY),GRID)
def text(x,y,label,n=13):
    return '<text x="%s" y="%s" font-family="DejaVu Sans,sans-serif" font-size="%s" fill="%s">%s</text>'%(x,y,n,TEXT,esc(label))
def save(name,title,grids,room_key=None,labels=None):
    # Each grid is (name, two-dimensional array of block symbols); render 3 cols of panels.
    scale=15
    cols=3
    blockw=max(len(g[0]) for _,g in grids)
    blockh=max(len(g) for _,g in grids)
    panelw=blockw*scale+62
    panelh=blockh*scale+105
    rows=(len(grids)+cols-1)//cols
    w=cols*panelw+20
    h=rows*panelh+140
    out=['<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 %d %d" width="%d" height="%d">'%(w,h,w,h),
         '<rect width="%d" height="%d" fill="%s"/>'%(w,h,BG),text(24,42,title,23),text(24,69,"1 cell = 1 Minecraft block; R1 concept, not GameTest / NBT",14)]
    for idx,(lab,g) in enumerate(grids):
        px=12+(idx%cols)*panelw
        py=88+(idx//cols)*panelh
        out.append('<rect x="%d" y="%d" width="%d" height="%d" fill="#162838" stroke="%s"/>'%(px,py,panelw-9,panelh-8,GRID))
        out.append(text(px+14,py+24,lab,17))
        for row,vec in enumerate(g):
            for col,code in enumerate(vec):
                out.append(tile(px+26+col*scale,py+43+row*scale,code,scale))
        if labels and len(grids)==1:
            for label,(x1,z1,x2,z2) in labels:
                if x1<-1 or z1<-1:continue
                x=px+26+scale*(x1+1)
                y=py+43+scale*(z1+1)
                out.append('<rect x="%d" y="%d" width="%d" height="%d" fill="none" stroke="#f9d183" stroke-width="1.4"/>'%(x,y,(x2-x1+1)*scale,(z2-z1+1)*scale))
                out.append(text(x+3,y+14,label.split()[0],12))
    out.append(text(24,h-34,"Palette: "+", ".join("%s=%s"%(k,v) for k,v in COLORS.items()),11))
    out.append('</svg>')
    dest=ROOT/name
    dest.write_text("\n".join(out),encoding="utf8")
    print(str(dest))
def layer(y):
    return [[BLOCKS.get((u,y,v),".") for u in range(-1,28)] for v in range(-1,22)]
def proj_front(v):
    return [[BLOCKS.get((u,y,v),".") for u in range(-1,28)] for y in range(17,-1,-1)]
def proj_side(u):
    return [[BLOCKS.get((u,y,v),".") for v in range(-1,22)] for y in range(17,-1,-1)]
def roof():
    arr=[]
    for v in range(-1,22):
        rr=[]
        for u in range(-1,28):
            cell="."
            for y in range(17,-1,-1):
                cell=BLOCKS.get((u,y,v),".")
                if cell!=".":break
            rr.append(cell)
        arr.append(rr)
    return arr
def floor_map(y):
    # show structural floor plus immediate wall layer where floor block is hidden
    p=layer(y)
    nxt=layer(y+1)
    for i,row in enumerate(p):
        for j,c in enumerate(row):
            if c=="." and nxt[i][j]!=".":
                p[i][j]=nxt[i][j]
    return p
assert len(VOX)==3598
save("VECHE_01_FLOORS.svg","VECHE HALL 2.2 B — ROOM LAYOUT",[
    ("I FLOOR: Y0 + Y1",floor_map(0)),
    ("II FLOOR: Y6 + Y7",floor_map(6)),
    ("ROOF: highest roof block",roof()),
])
save("VECHE_02_FACADES.svg","VECHE HALL 2.2 B — FOUR FACADES",[
    ("SOUTH (main entrance) v20",proj_front(20)),
    ("NORTH v0",proj_front(0)),
    ("WEST u0",proj_side(0)),
    ("EAST u26",proj_side(26)),
])
save("VECHE_03_ALL_18_LAYERS.svg","VECHE HALL 2.2 B — ALL 18 BLOCK LAYERS",[
    ("Y=%02d"%y,layer(y)) for y in range(18)
])
save("VECHE_04_ROOM_BOUNDS_I.svg","VECHE HALL 2.2 B — FIRST FLOOR / ROOM BOUNDARIES",[("Y0/Y1",floor_map(0))],labels=ROOMS[:9])
save("VECHE_05_ROOM_BOUNDS_II.svg","VECHE HALL 2.2 B — SECOND FLOOR / ROOM BOUNDARIES",[("Y6/Y7",floor_map(6))],labels=ROOMS[9:])
