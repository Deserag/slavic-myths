"""Hand-directed 16px semantic sprites. Runs last so legacy generators cannot restore placeholders."""
from pathlib import Path
import json, struct, zlib

C = {
 '.':(0,0,0,0), 'k':(31,34,39,255), 'd':(61,68,76,255), 's':(100,113,124,255),
 'S':(157,177,187,255), 'w':(226,233,223,255), 'n':(38,55,73,255), 'a':(70,112,145,255),
 'c':(143,217,229,255), 'b':(78,48,32,255), 'B':(132,83,49,255), 't':(190,140,85,255),
 'e':(237,214,167,255), 'y':(235,194,81,255), 'o':(206,115,44,255), 'g':(53,88,51,255),
 'G':(91,145,71,255), 'L':(155,183,113,255), 'r':(145,54,65,255), 'R':(213,104,100,255),
 'p':(192,98,151,255), 'v':(65,69,119,255), 'V':(112,111,164,255)}

def encode(rows):
    h,w=len(rows),len(rows[0]); assert all(len(row)==w for row in rows)
    def chunk(kind,data): return struct.pack('!I',len(data))+kind+data+struct.pack('!I',zlib.crc32(kind+data)&0xffffffff)
    raw=b''.join(b'\0'+bytes(channel for ch in row for channel in C[ch]) for row in rows)
    return b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('!IIBBBBB',w,h,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b'')

class Art:
    def __init__(self,w=16,h=16): self.a=[['.']*w for _ in range(h)]; self.w=w; self.h=h
    def dot(self,x,y,c):
        if 0<=x<self.w and 0<=y<self.h: self.a[y][x]=c
    def line(self,x0,y0,x1,y1,c):
        dx=abs(x1-x0); dy=-abs(y1-y0); sx=1 if x0<x1 else -1; sy=1 if y0<y1 else -1; err=dx+dy
        while True:
            self.dot(x0,y0,c)
            if x0==x1 and y0==y1: break
            e=2*err
            if e>=dy: err+=dy; x0+=sx
            if e<=dx: err+=dx; y0+=sy
    def box(self,x0,y0,x1,y1,c):
        for y in range(y0,y1+1):
            for x in range(x0,x1+1): self.dot(x,y,c)
    def disc(self,cx,cy,rx,ry,c):
        for y in range(cy-ry,cy+ry+1):
            for x in range(cx-rx,cx+rx+1):
                if ((x-cx)/max(rx,1))**2+((y-cy)/max(ry,1))**2<=1.05: self.dot(x,y,c)
    def rows(self): return [''.join(row) for row in self.a]

def weapon(a,name):
    per=name.startswith('perunite') or name=='thunder_axe'
    metal='n' if per else ('S' if name.startswith('silver') or name=='ritual_knife' else ('e' if name=='bone_knife' else 's'))
    shine='c' if per else ('w' if metal=='S' else 't')
    if name in ('perunite_sword','silver_sword'):
        a.line(3,12,11,4,'k'); a.line(4,12,12,4,metal); a.line(5,10,12,3,metal)
        a.line(7,8,11,4,shine); a.dot(12,3,shine)
        a.line(2,12,5,15,'b'); a.line(1,13,4,10,'y' if per else 'd'); a.dot(1,14,'k')
        if per: a.dot(9,5,'c'); a.dot(10,6,'c')
        else: a.line(11,4,13,3,'w')
    elif name in ('silver_dagger','bone_knife','ritual_knife'):
        a.line(3,13,7,9,'b'); a.line(6,10,9,7,'k')
        a.line(8,8,12,4,metal); a.line(9,8,12,5,shine)
        a.dot(13,4,metal)
        if name=='bone_knife': a.dot(12,5,'e'); a.dot(10,5,'e'); a.dot(6,12,'t')
        if name=='ritual_knife': a.box(5,10,7,10,'y'); a.dot(8,7,'v'); a.dot(6,12,'r')
    elif name in ('perunite_axe','thunder_axe'):
        a.line(4,14,11,4,'b'); a.line(5,14,12,4,'t')
        if name=='thunder_axe':
            a.box(3,2,10,7,'k'); a.box(3,3,9,6,'n'); a.line(2,3,1,7,'d'); a.line(9,3,12,1,'d')
            for x,y in ((5,3),(6,4),(5,5),(6,6),(7,5),(8,5)): a.dot(x,y,'c' if (x+y)%2 else 'y')
            a.dot(11,2,'c'); a.dot(2,7,'c')
        else:
            a.box(3,3,9,6,metal); a.line(2,3,2,7,'k'); a.line(3,7,8,7,'s')
            a.dot(5,4,shine); a.dot(6,5,shine)
    elif name=='perunite_pickaxe':
        a.line(4,14,10,5,'b'); a.line(5,14,11,5,'t'); a.line(2,4,12,4,'n'); a.line(3,3,11,3,'s')
        a.dot(1,5,'d'); a.dot(13,5,'d'); a.dot(6,3,'c'); a.dot(9,4,'c')
    elif name=='perunite_shovel':
        a.line(4,14,9,7,'b'); a.line(5,14,10,7,'t'); a.disc(11,4,2,3,'n'); a.line(11,2,11,5,'c'); a.dot(12,5,'s')
    elif name=='perunite_hoe':
        a.line(4,14,9,6,'b'); a.line(5,14,10,6,'t'); a.line(6,3,13,3,'n'); a.line(6,4,11,4,'s')
        a.line(6,4,5,6,'n'); a.dot(9,3,'c')

def plant(a,name):
    if name=='st_johns_wort':
        a.line(8,15,8,5,'g'); a.line(8,10,4,7,'g'); a.line(8,11,12,8,'g')
        for x,y in ((4,7),(5,8),(11,7),(12,8),(6,5),(9,5),(10,4)): a.disc(x,y,1,1,'G')
        for x,y in ((7,3),(10,3),(5,5),(12,5)): a.dot(x,y,'y'); a.dot(x,y-1,'e')
    elif name=='nettle':
        a.line(8,15,8,3,'g')
        for x,y,dir in ((5,6,-1),(11,5,1),(4,10,-1),(12,9,1),(6,12,-1),(10,12,1)):
            a.line(8,y+2,x,y,'G'); a.dot(x+dir,y-1,'G'); a.dot(x-dir,y,'L')
        a.dot(8,2,'L')
    elif name=='fireweed':
        a.line(8,15,8,2,'g'); a.line(8,11,4,9,'G'); a.line(8,12,12,10,'G')
        for x,y in ((7,2),(9,3),(6,4),(10,5),(7,6),(9,7),(7,8)): a.disc(x,y,1,1,'p')
        a.dot(8,2,'R')
    elif name=='juniper_berries':
        a.line(4,13,11,5,'b'); a.line(3,8,12,12,'g')
        for x,y in ((5,6),(8,4),(11,7),(5,10),(9,12)): a.line(x-1,y,x+1,y,'G'); a.dot(x,y-1,'g')
        for x,y in ((5,7),(10,5),(7,11),(12,10)): a.disc(x,y,1,1,'v'); a.dot(x,y,'V')
    elif name=='flax':
        a.line(8,15,8,4,'g'); a.line(6,15,7,6,'g'); a.line(10,15,9,6,'g')
        for x,y in ((7,4),(9,5),(6,7)): a.dot(x,y,'a'); a.dot(x,y-1,'c')
        for x,y in ((5,10),(10,11),(8,9)): a.dot(x,y,'G')
    elif name=='wormwood':
        a.line(8,15,8,3,'g')
        for y in (5,8,11):
            a.line(8,y+2,3,y,'s'); a.line(8,y+1,12,y-1,'s')
            a.dot(4,y-1,'L'); a.dot(11,y-2,'L')
    elif name=='fern_flower':
        a.line(8,15,8,5,'g')
        for y in (8,10,12):
            a.line(8,y,3,y-2,'G'); a.line(8,y,13,y-2,'G')
            a.dot(3,y-3,'L'); a.dot(12,y-3,'L')
        a.disc(8,4,2,2,'r'); a.dot(8,2,'y'); a.dot(8,4,'e')

def item(name):
    a=Art()
    if name in {'perunite_sword','perunite_axe','perunite_pickaxe','perunite_shovel','perunite_hoe','silver_sword','silver_dagger','bone_knife','ritual_knife','thunder_axe'}: weapon(a,name)
    elif name in {'flax','wormwood','st_johns_wort','nettle','fireweed','juniper_berries','fern_flower'}:
        plant(a,name)
        if name!='fern_flower':
            a.line(5,12,11,12,'t'); a.line(6,13,10,13,'b'); a.dot(8,14,'t')
    elif name in {'perunite_helmet','perunite_chestplate','perunite_leggings','perunite_boots'}:
        part=name.split('_')[-1]
        if part=='helmet': a.box(3,3,12,9,'n'); a.box(4,4,11,5,'s'); a.box(4,8,6,10,'d'); a.box(9,8,11,10,'d'); a.line(6,6,9,6,'c')
        if part=='chestplate': a.box(5,4,10,12,'n'); a.box(3,5,5,9,'d'); a.box(10,5,12,9,'d'); a.line(6,6,8,9,'c'); a.line(8,9,10,6,'c')
        if part=='leggings': a.box(4,3,11,6,'n'); a.box(4,7,6,13,'d'); a.box(9,7,11,13,'d'); a.line(5,4,10,4,'c')
        if part=='boots': a.box(4,7,6,12,'d'); a.box(9,7,11,12,'d'); a.box(3,12,7,13,'n'); a.box(8,12,12,13,'n'); a.dot(5,10,'c'); a.dot(10,10,'c')
    elif name in {'silver_ingot','perunite'}:
        a.line(3,9,5,5,'k'); a.box(5,5,12,9,'S' if name=='silver_ingot' else 'n'); a.line(3,10,11,10,'d'); a.line(5,5,12,5,'w' if name=='silver_ingot' else 'c')
        a.dot(10,8,'a' if name=='silver_ingot' else 'c')
    elif name=='silver_nugget': a.disc(8,9,4,3,'S'); a.line(5,7,9,7,'w'); a.dot(11,10,'s')
    elif name=='birch_bark':
        a.box(3,4,12,11,'e'); a.line(3,4,12,3,'w'); a.line(4,11,13,10,'t'); a.line(6,6,9,6,'d'); a.dot(11,8,'d')
        a.dot(5,9,'b')
    elif name=='birch_bark_scroll':
        a.box(4,3,11,12,'e'); a.line(4,3,11,3,'w'); a.line(4,12,11,12,'t'); a.line(6,6,9,6,'b'); a.dot(7,8,'r'); a.line(5,10,10,10,'b'); a.box(3,3,4,12,'t')
    elif name=='thunder_stone':
        a.disc(8,8,5,5,'d'); a.dot(3,7,'k'); a.dot(12,5,'k'); a.dot(11,12,'n'); a.line(9,4,7,8,'c'); a.line(7,8,10,8,'c'); a.line(10,8,6,12,'c')
    elif name in {'warding_charm','perun_charm'}:
        a.line(5,3,8,1,'b'); a.line(8,1,11,3,'b'); a.line(5,3,6,6,'t'); a.line(11,3,10,6,'t')
        if name=='warding_charm':
            a.disc(8,9,4,4,'B'); a.line(6,9,10,9,'e'); a.line(8,6,8,12,'e'); a.dot(8,9,'y')
        else:
            a.disc(8,9,4,4,'n'); a.line(8,5,7,9,'c'); a.line(7,9,10,9,'c'); a.line(10,9,7,12,'c')
    elif name=='linen_thread':
        a.disc(8,8,5,4,'e'); a.disc(8,8,3,2,'b'); a.line(4,7,11,10,'w'); a.line(5,6,12,9,'t'); a.line(6,12,6,14,'e')
    elif name=='linen_cloth':
        a.box(3,4,12,12,'e'); a.box(4,5,11,10,'w'); a.line(4,11,12,11,'t'); a.line(5,5,5,10,'S'); a.line(8,5,8,10,'e'); a.dot(11,8,'t')
    elif name=='herb_pouch':
        a.disc(8,10,5,4,'e'); a.line(4,7,12,7,'b'); a.box(6,4,10,6,'t'); a.line(8,4,8,2,'G'); a.dot(7,3,'g'); a.dot(10,3,'G'); a.dot(7,11,'B')
    elif name in {'ground_wormwood','ground_st_johns_wort','juniper_blend'}:
        a.disc(8,11,5,2,'b'); a.disc(8,10,4,2,'s' if name=='ground_wormwood' else 'G')
        if name=='ground_wormwood':
            for x,y in ((5,9),(7,7),(11,9),(9,10)): a.dot(x,y,'L')
        elif name=='ground_st_johns_wort':
            for x,y in ((5,9),(8,7),(10,10),(12,9)): a.dot(x,y,'y')
        else:
            for x,y in ((5,9),(9,8),(11,10)): a.dot(x,y,'v'); a.dot(x+1,y,'V')
    elif name=='honey_bread':
        a.disc(8,9,6,4,'B'); a.disc(8,7,5,3,'t'); a.line(4,7,11,7,'e'); a.line(6,5,10,5,'y'); a.dot(11,8,'o')
    elif name=='honey_baked_apple':
        a.disc(8,9,5,4,'r'); a.disc(8,8,4,3,'R'); a.line(8,5,8,3,'b'); a.line(8,4,11,3,'G'); a.line(5,9,10,11,'y'); a.dot(11,9,'o')
    elif name=='ritual_bowl':
        a.box(3,6,12,7,'S'); a.line(3,7,5,12,'d'); a.line(12,7,10,12,'d'); a.line(5,12,10,12,'S'); a.line(5,8,10,8,'n'); a.dot(8,7,'c')
    elif name=='ancient_coin':
        a.disc(8,8,5,5,'B'); a.disc(8,8,4,4,'y'); a.line(8,5,8,11,'b'); a.line(6,8,10,8,'b'); a.dot(5,5,'e')
    elif name=='idol_fragment':
        a.line(4,4,10,2,'b'); a.line(10,2,13,8,'b'); a.line(13,8,9,13,'b'); a.line(9,13,3,10,'b'); a.box(5,5,10,10,'t'); a.line(6,6,9,9,'d'); a.dot(8,7,'e')
    elif name=='ancient_sign':
        a.box(4,2,11,12,'b'); a.box(5,3,10,11,'s'); a.line(8,4,8,10,'e'); a.line(6,7,10,7,'e'); a.dot(8,12,'y')
    elif name=='lore_book':
        a.box(3,2,12,13,'b'); a.box(4,3,11,12,'n'); a.box(12,3,13,12,'e'); a.line(8,5,8,10,'y'); a.line(6,7,10,7,'y'); a.dot(8,7,'c'); a.line(4,11,11,11,'t')
    elif name=='leshy_heart':
        a.disc(8,8,5,5,'b'); a.disc(8,8,3,4,'B'); a.line(5,12,3,15,'g'); a.line(10,11,13,15,'g'); a.line(7,5,7,11,'G'); a.dot(8,7,'c')
    elif name=='ancient_idol':
        a.box(4,3,11,13,'B'); a.box(5,2,10,5,'t'); a.dot(6,4,'k'); a.dot(9,4,'k'); a.line(6,8,9,8,'b'); a.box(3,13,12,14,'b')
    elif name=='amber':
        a.disc(8,8,5,5,'o'); a.disc(8,7,3,4,'y'); a.line(6,5,9,4,'e'); a.dot(10,10,'t'); a.dot(6,9,'B')
    elif name=='amber_charm':
        a.line(5,3,8,1,'b'); a.line(8,1,11,3,'b'); a.line(5,3,6,6,'t'); a.line(11,3,10,6,'t')
        a.disc(8,9,4,4,'B'); a.disc(8,9,3,3,'o'); a.dot(7,7,'y'); a.line(8,8,10,10,'e')
    elif name=='carved_staff':
        a.line(3,15,11,2,'b'); a.line(4,15,12,2,'B'); a.disc(11,3,2,2,'t'); a.dot(11,3,'g'); a.line(5,11,7,11,'e')
    elif name=='storm_staff':
        a.line(3,15,10,4,'b'); a.line(4,15,11,4,'n'); a.disc(11,3,3,3,'d'); a.disc(11,3,2,2,'a')
        a.line(11,1,10,4,'c'); a.line(10,4,12,4,'c'); a.line(12,4,10,6,'c'); a.line(5,12,7,12,'y')
    elif name=='club':
        a.line(3,15,10,5,'b'); a.line(4,15,11,5,'B'); a.box(8,2,12,7,'B'); a.box(9,3,11,6,'t'); a.line(8,3,12,3,'b')
    elif name=='battle_axe':
        a.line(4,15,10,4,'b'); a.line(5,15,11,4,'t'); a.box(2,2,10,7,'d'); a.line(2,2,6,1,'S'); a.line(2,7,6,8,'S'); a.box(4,3,8,6,'s'); a.dot(5,4,'w')
    elif name in {'spear','silver_spear'}:
        a.line(2,15,10,4,'b'); a.line(3,15,11,4,'B')
        if name=='spear':
            a.line(10,5,12,1,'d'); a.line(11,5,13,1,'s'); a.dot(13,1,'w'); a.line(8,8,11,8,'t')
        else:
            a.line(9,5,11,1,'S'); a.line(12,5,14,1,'S'); a.line(11,2,12,5,'w'); a.dot(13,1,'w'); a.line(7,9,10,9,'s')
    elif name=='ritual_charcoal':
        a.disc(8,9,5,4,'k'); a.disc(8,8,3,3,'d'); a.line(6,6,10,10,'r'); a.dot(7,7,'y'); a.dot(10,8,'o')
    elif name=='honey_flatbread':
        a.disc(8,9,6,4,'B'); a.disc(8,8,5,3,'t'); a.line(5,7,11,7,'y'); a.line(6,10,10,10,'o'); a.dot(8,6,'e')
    elif name=='berry_mors':
        a.box(5,4,10,12,'S'); a.box(6,6,9,11,'r'); a.box(6,3,9,4,'e'); a.line(7,6,8,10,'V'); a.dot(8,2,'w')
    elif name=='forest_mix':
        a.disc(8,10,5,3,'t'); a.disc(8,9,4,2,'G'); a.dot(5,8,'r'); a.dot(10,7,'v'); a.dot(8,10,'y'); a.line(4,12,11,12,'b')
    elif name in {'forest_charm','hunter_charm','traveler_charm'}:
        a.line(5,3,8,1,'b'); a.line(8,1,11,3,'b'); a.line(5,3,6,6,'t'); a.line(11,3,10,6,'t')
        if name=='forest_charm':
            a.disc(8,9,4,4,'g'); a.line(8,6,8,12,'e'); a.line(8,9,5,7,'L'); a.line(8,9,11,7,'L')
        elif name=='hunter_charm':
            a.disc(8,9,4,4,'B'); a.line(6,11,10,7,'e'); a.line(6,7,10,11,'e'); a.dot(8,9,'r')
        else:
            a.disc(8,9,4,4,'s'); a.line(8,6,8,11,'w'); a.line(5,9,11,9,'w'); a.dot(8,9,'y')
    elif name in {'retainer_shield','perunite_shield'}:
        if name=='retainer_shield':
            a.box(3,2,12,10,'B'); a.line(3,10,8,14,'b'); a.line(12,10,8,14,'b'); a.disc(8,7,3,3,'S'); a.dot(8,7,'d'); a.line(4,3,11,3,'e')
        else:
            a.box(2,2,13,10,'d'); a.line(2,10,8,15,'n'); a.line(13,10,8,15,'n'); a.box(4,4,11,9,'s'); a.line(8,4,8,12,'c'); a.line(5,7,11,7,'c'); a.dot(8,7,'y')
    else: raise ValueError('No semantic sprite for '+name)
    return a.rows()

def block(name):
    a=Art()
    if name in {'flax','wormwood','st_johns_wort','nettle','fireweed','juniper_berries'}:
        plant(a,name); return a.rows()
    if name in {'perunite_ore','silver_ore'}:
        for y in range(16):
            for x in range(16): a.dot(x,y,'d' if (x*7+y*3)%9<4 else 's')
        color='a' if name=='perunite_ore' else 'S'; hi='c' if name=='perunite_ore' else 'w'
        for x,y in ((3,3),(11,2),(7,8),(13,11),(2,13)):
            a.box(x,y,x+1,y+1,color); a.dot(x,y,hi)
    elif name=='straw_block':
        a.box(0,0,15,15,'t')
        for y in (2,5,8,11,14): a.line(0,y,15,y,'y')
        for x in (3,8,13): a.line(x,0,x+1,15,'B')
        a.line(0,7,15,7,'b')
    elif name in {'carved_oak_pillar','carved_birch_planks','carved_oak_block'}:
        pale=name=='carved_birch_planks'; base='e' if pale else 'B'; edge='t' if pale else 'b'
        a.box(0,0,15,15,base); a.line(0,0,15,0,edge); a.line(0,15,15,15,edge); a.line(0,0,0,15,edge); a.line(15,0,15,15,edge)
        if name=='carved_oak_pillar':
            a.line(4,0,4,15,edge); a.line(11,0,11,15,edge); a.line(6,3,9,6,'y'); a.line(9,6,6,9,'y'); a.line(6,9,9,12,'y')
        elif pale:
            for y in (4,9,13): a.line(1,y,14,y,'t')
            a.line(3,5,7,8,'b'); a.line(7,8,11,5,'b')
        else:
            a.box(3,3,12,12,'t'); a.line(3,3,12,12,edge); a.line(12,3,3,12,edge); a.disc(8,8,2,2,'y')
    elif name in {'hearth','altar','ancient_idol'}:
        base='B' if name=='hearth' else ('t' if name=='ancient_idol' else 's')
        a.box(0,0,15,15,base)
        if name=='hearth':
            a.box(2,3,13,12,'b'); a.box(4,5,11,10,'k'); a.disc(8,8,2,2,'o'); a.dot(8,6,'y'); a.line(3,13,12,13,'e')
        elif name=='altar':
            a.box(2,2,13,13,'d'); a.box(3,3,12,12,'s'); a.line(4,8,11,8,'n'); a.line(8,4,8,11,'n'); a.dot(8,8,'c')
        else:
            a.box(4,2,11,13,'B'); a.dot(6,5,'k'); a.dot(9,5,'k'); a.line(6,10,9,10,'b')
    elif name=='amber_block':
        a.box(0,0,15,15,'o'); a.box(1,1,14,14,'y'); a.line(2,2,11,2,'e'); a.line(3,4,12,12,'t'); a.dot(5,8,'B'); a.dot(11,6,'w')
    elif name=='ritual_candle':
        a.box(5,10,10,13,'d'); a.box(6,3,9,10,'e'); a.line(6,4,6,9,'w'); a.dot(8,2,'o'); a.dot(8,1,'y'); a.dot(7,3,'r')
    elif name=='wall_carving':
        a.box(0,0,15,15,'B'); a.box(2,2,13,13,'t'); a.line(3,3,12,12,'b'); a.line(12,3,3,12,'b'); a.disc(8,8,3,3,'y'); a.dot(8,8,'b')
    else: raise ValueError('No block art for '+name)
    return a.rows()

REDRAW_ITEMS={'perunite_sword','perunite_axe','perunite_pickaxe','perunite_shovel','perunite_hoe','perunite_helmet','perunite_chestplate','perunite_leggings','perunite_boots','silver_ingot','silver_nugget','silver_sword','silver_dagger','bone_knife','ritual_knife','linen_cloth','herb_pouch','ground_wormwood','ground_st_johns_wort','juniper_blend','honey_bread','honey_baked_apple','ritual_bowl','thunder_axe','perun_charm','birch_bark','birch_bark_scroll','thunder_stone','warding_charm','linen_thread','fern_flower','perunite','ancient_coin','idol_fragment','ancient_sign','lore_book','leshy_heart'}
REDRAW_BLOCKS={'flax','wormwood','st_johns_wort','nettle','fireweed','juniper_berries','perunite_ore','silver_ore','carved_oak_pillar','carved_birch_planks','carved_oak_block','straw_block','hearth','altar','ancient_idol'}
NEW_NAMES={
 'amber':('Янтарь','Amber'), 'amber_block':('Янтарный блок','Amber Block'), 'amber_charm':('Янтарный оберег','Amber Charm'),
 'carved_staff':('Резной посох','Carved Staff'), 'storm_staff':('Грозовой посох','Storm Staff'),
 'club':('Дубина','Club'), 'battle_axe':('Боевой топор','Battle Axe'), 'spear':('Копьё','Spear'),
 'silver_spear':('Серебряное копьё','Silver Spear'), 'ritual_charcoal':('Обрядовый уголь','Ritual Charcoal'),
 'ritual_candle':('Ритуальная свеча','Ritual Candle'), 'honey_flatbread':('Медовая лепёшка','Honey Flatbread'),
 'berry_mors':('Ягодный морс','Berry Mors'), 'forest_mix':('Лесной сбор','Forest Mix'),
 'wall_carving':('Настенная резьба','Wall Carving')}
NEW_NAMES.update({
 'forest_charm':('Лесной оберег','Forest Charm'), 'hunter_charm':('Охотничий амулет','Hunter Charm'),
 'traveler_charm':('Оберег путника','Traveler Charm'), 'retainer_shield':('Щит дружинника','Retainer Shield'),
 'perunite_shield':('Перунитовый щит','Perunite Shield')})
NEW_BLOCKS={'amber_block','ritual_candle','wall_carving'}
BOOK={
 'silver':(('Серебро','Silver'),('Серебро встречается в новых жилах Верхнего мира. Клинок и кинжал особенно опасны для существ, отмеченных как уязвимые к серебру.','Silver forms in new Overworld veins. Silver blades are especially effective against creatures marked vulnerable to silver.')),
 'amber':(('Янтарь','Amber'),('Янтарь — редкая находка в старых капищах. Его тёплый блеск используют в обереге, который держат в левой руке.','Amber is a rare shrine find. Its warm glow belongs in a charm held in the offhand.')),
 'thunder_axe':(('Громовой топор','Thunder Axe'),('На жертвеннике соедините перунитовый топор, громовой камень, Древний знак и чашу. Удар иногда даёт слабый разряд без разрушительной молнии.','Offer a perunite axe, thunder stone, Ancient Sign and bowl in order. A strike may release a weak pulse without destructive lightning.')),
 'storm_staff':(('Грозовой посох','Storm Staff'),('Резной посох, перунит, громовой камень и Древний знак создают Грозовой посох. Правая кнопка выпускает короткий импульс с паузой между применениями.','A carved staff, perunite, thunder stone and Ancient Sign form the Storm Staff. Right-click for a short pulse with a cooldown.')),
 'charms':(('Обереги','Charms'),('Оберег Перуна смягчает урон молнии, янтарный — урон огня. Держите один из них в левой руке. Это игровые свойства, не историческое утверждение.','The Perun Charm reduces lightning damage; the amber charm reduces fire damage. Hold one in the offhand. These are game rules, not historical claims.')),
 'herbs':(('Травы','Herbs'),('Лён, полынь, зверобой, крапива и иван-чай растут на поверхности новых чанков. Ягоды можжевельника собирают с небольших кустов. Травы идут в смеси и пищу.','Flax, wormwood, St. John’s wort, nettle and fireweed grow in new chunks. Juniper berries come from small bushes. Herbs serve mixtures and food.')),
 'ritual_tools':(('Обрядовое ремесло','Ritual Craft'),('Жертвенник принимает дары строго по порядку. Новые обряды создают посох, янтарный оберег и обрядовый уголь. Обрядовый нож и чаша пока простые инструменты.','The altar takes offerings in order. New rites create a staff, amber charm and ritual charcoal. The ritual knife and bowl remain simple tools for now.'))}

def generate(root):
    res=root/'src/main/resources/assets/slavicmyths'
    for name in sorted(REDRAW_ITEMS): (res/f'textures/item/{name}.png').write_bytes(encode(item(name)))
    for name in sorted(REDRAW_BLOCKS): (res/f'textures/block/{name}.png').write_bytes(encode(block(name)))
    # Plants have separate inventory bundles, so a held herb never looks like a weapon or a block icon.
    for name in ('flax','wormwood','st_johns_wort','nettle','fireweed','juniper_berries'):
        (res/f'textures/item/{name}.png').write_bytes(encode(item(name)))
        p=res/f'models/item/{name}.json'; p.write_text(json.dumps({'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+name}},indent=2)+'\n')
    # Geometry from standard Minecraft armor UVs. Transparent exterior, dark plates, narrow storm seams.
    for layer in (1,2):
        a=Art(64,32)
        for x0,y0,x1,y1 in ((0,0,31,15),(16,16,39,31),(40,16,55,31),(0,16,15,31)):
            a.box(x0,y0,x1,y1,'n'); a.line(x0,y0,x1,y0,'s'); a.line(x0,y1,x1,y1,'d')
            for x in range(x0+2,x1,7): a.line(x,y0+2,x,y1-2,'a')
        a.line(20,20,35,20,'c'); a.line(5,5,10,5,'c'); a.line(23,22,27,28,'c'); a.line(27,28,33,22,'c')
        if layer==2:
            a.line(3,19,12,19,'c'); a.line(5,20,5,29,'a'); a.line(10,20,10,29,'a')
            a.line(21,26,34,26,'s')
        (res/f'textures/models/armor/perunite_layer_{layer}.png').write_bytes(encode(a.rows()))
    # Low complexity three-dimensional silhouettes for the old cube placeholders.
    def model(name,elements,texture):
        obj={'textures':{'body':'slavicmyths:block/'+texture,'particle':'slavicmyths:block/'+texture},'elements':[
            {'from':lo,'to':hi,'faces':{face:{'texture':'#body'} for face in ('up','down','north','south','east','west')}} for lo,hi in elements]}
        (res/f'models/block/{name}.json').write_text(json.dumps(obj,indent=2)+'\n')
    model('hearth',[([1,0,1],[15,3,15]),([3,3,3],[13,10,13]),([2,10,2],[14,12,14])],'hearth')
    model('ancient_idol',[([3,0,3],[13,2,13]),([5,2,5],[11,12,11]),([4,12,4],[12,15,12])],'ancient_idol')
    def js(path,obj):
        p=root/'src/main/resources'/path; p.parent.mkdir(parents=True,exist_ok=True)
        p.write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    for name in NEW_NAMES:
        if name in NEW_BLOCKS:
            (res/f'textures/block/{name}.png').write_bytes(encode(block(name)))
            if name=='ritual_candle':
                model(name,[([5,0,5],[11,2,11]),([6,2,6],[10,11,10]),([7,11,7],[9,14,9])],name)
            else: js(f'assets/slavicmyths/models/block/{name}.json',{'parent':'minecraft:block/cube_all','textures':{'all':'slavicmyths:block/'+name}})
            js(f'assets/slavicmyths/blockstates/{name}.json',{'variants':{'':{'model':'slavicmyths:block/'+name}}})
            js(f'assets/slavicmyths/models/item/{name}.json',{'parent':'slavicmyths:block/'+name})
            js(f'data/slavicmyths/loot_tables/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'slavicmyths:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
        else:
            (res/f'textures/item/{name}.png').write_bytes(encode(item(name)))
            js(f'assets/slavicmyths/models/item/{name}.json',{'parent':'minecraft:item/handheld' if name in {'carved_staff','storm_staff','club','battle_axe','spear','silver_spear'} else 'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+name}})
    for lang,index in [('ru_ru',0),('en_us',1)]:
        p=res/f'lang/{lang}.json'; data=json.loads(p.read_text(encoding='utf-8'))
        for name,pair in NEW_NAMES.items(): data[('block' if name in NEW_BLOCKS else 'item')+'.slavicmyths.'+name]=pair[index]
        data['tooltip.slavicmyths.amber_charm'] = ('В левой руке: −15% урона от огня.' if index==0 else 'Offhand: 15% less fire damage.')
        data['tooltip.slavicmyths.ancient_coin'] = ('Старинная находка. Возможно, кто-то заинтересуется ею.' if index==0 else 'An ancient find. Someone may want it someday.')
        data['tooltip.slavicmyths.forest_charm'] = ('В левой руке: −15% урона от лесных духов.' if index==0 else 'Offhand: 15% less damage from forest spirits.')
        data['tooltip.slavicmyths.hunter_charm'] = ('В левой руке: +0,5 урона животным.' if index==0 else 'Offhand: +0.5 damage to animals.')
        data['tooltip.slavicmyths.traveler_charm'] = ('В левой руке: −15% урона от падения.' if index==0 else 'Offhand: 15% less fall damage.')
        data['book.slavicmyths.craft'] = ('Ремесло и ресурсы' if index==0 else 'Craft and resources')
        data['book.slavicmyths.next_topic'] = ('Следующая тема' if index==0 else 'Next topic')
        for key,(title,body) in BOOK.items():
            data['book.slavicmyths.'+key+'.title']=title[index]
            data['book.slavicmyths.'+key+'.text']=body[index]
        p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    def shaped(name,pattern,key,count=1): js(f'data/slavicmyths/recipes/{name}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':{k:{'item':v} for k,v in key.items()},'result':{'item':'slavicmyths:'+name,'count':count}})
    def shapeless(name,ingredients,count=1): js(f'data/slavicmyths/recipes/{name}.json',{'type':'minecraft:crafting_shapeless','ingredients':[{'item':v} for v in ingredients],'result':{'item':'slavicmyths:'+name,'count':count}})
    shaped('amber_block',['AAA','AAA','AAA'],{'A':'slavicmyths:amber'})
    js('data/slavicmyths/recipes/amber_from_block.json',{'type':'minecraft:crafting_shapeless','ingredients':[{'item':'slavicmyths:amber_block'}],'result':{'item':'slavicmyths:amber','count':9}})
    shaped('carved_staff',[' B',' S','S '],{'B':'slavicmyths:birch_bark','S':'minecraft:stick'})
    shaped('club',['L','L','S'],{'L':'minecraft:oak_log','S':'minecraft:stick'})
    shaped('battle_axe',['II','IS',' S'],{'I':'minecraft:iron_ingot','S':'minecraft:stick'})
    shaped('spear',[' F',' S','S '],{'F':'minecraft:flint','S':'minecraft:stick'})
    shaped('silver_spear',[' I',' S','S '],{'I':'slavicmyths:silver_ingot','S':'minecraft:stick'})
    shaped('ritual_candle',[' H',' C',' B'],{'H':'minecraft:honeycomb','C':'slavicmyths:ritual_charcoal','B':'minecraft:stone_button'})
    shapeless('honey_flatbread',['minecraft:wheat','minecraft:wheat','minecraft:wheat','minecraft:honey_bottle'])
    shapeless('berry_mors',['minecraft:sweet_berries','minecraft:sweet_berries','minecraft:glass_bottle','slavicmyths:fireweed','minecraft:sugar'])
    shapeless('forest_mix',['minecraft:sweet_berries','slavicmyths:juniper_berries','slavicmyths:st_johns_wort'])
    shaped('wall_carving',['W','F'],{'W':'slavicmyths:carved_oak_block','F':'slavicmyths:idol_fragment'})
    shaped('forest_charm',[' P ','LHL',' C '],{'P':'slavicmyths:herb_pouch','L':'slavicmyths:linen_thread','H':'slavicmyths:leshy_heart','C':'slavicmyths:linen_cloth'})
    shaped('hunter_charm',[' B ','LSL',' B '],{'B':'minecraft:bone','L':'slavicmyths:linen_thread','S':'slavicmyths:silver_nugget'})
    shaped('traveler_charm',[' F ','LCL',' B '],{'F':'slavicmyths:fern_flower','L':'slavicmyths:linen_thread','C':'slavicmyths:ancient_coin','B':'slavicmyths:birch_bark'})
    shaped('retainer_shield',['PPP','PIP',' S '],{'P':'minecraft:oak_planks','I':'minecraft:iron_ingot','S':'minecraft:shield'})
    shaped('perunite_shield',['PPP','PSP',' P '],{'P':'slavicmyths:perunite','S':'slavicmyths:retainer_shield'})
    loot_path=root/'src/main/resources/data/slavicmyths/loot_tables/chests/ancient_shrine.json'
    loot=json.loads(loot_path.read_text(encoding='utf-8'))
    loot['pools'][0]['entries'].append({'type':'minecraft:item','name':'slavicmyths:amber','weight':2})
    loot_path.write_text(json.dumps(loot,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    js('data/slavicmyths/tags/entity_types/silver_vulnerable.json',{'replace':False,'values':['slavicmyths:leshy']})
    js('data/slavicmyths/tags/entity_types/forest_spirits.json',{'replace':False,'values':['slavicmyths:leshy']})
    advances={
        'thunder_axe':('first_ritual','thunder_axe','Сила грозы','Storm power','Получите Громовой топор.','Obtain the Thunder Axe.'),
        'storm_staff':('first_ritual','storm_staff','Первый проводник','First conduit','Получите Грозовой посох.','Obtain the Storm Staff.'),
        'amber':('ancient_find','amber','Солнечная смола','Sunlit resin','Найдите янтарь.','Find amber.'),
        'old_weapons':('root','spear','Старое оружие','Old weapons','Создайте копьё.','Craft a spear.')}
    for name,(parent,icon,ru,en,ru_desc,en_desc) in advances.items():
        js(f'data/slavicmyths/advancements/{name}.json',{'parent':'slavicmyths:'+parent,
            'display':{'icon':{'item':'slavicmyths:'+icon},'title':{'translate':'advancements.slavicmyths.'+name+'.title'},
                       'description':{'translate':'advancements.slavicmyths.'+name+'.description'},'frame':'task','show_toast':True,'announce_to_chat':False,'hidden':False},
            'criteria':{'obtained':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'item':'slavicmyths:'+icon}]}}}})
    for lang,index in [('ru_ru',0),('en_us',1)]:
        p=res/f'lang/{lang}.json'; data=json.loads(p.read_text(encoding='utf-8'))
        for name,(_,_,ru,en,ru_desc,en_desc) in advances.items():
            data['advancements.slavicmyths.'+name+'.title']=(ru,en)[index]
            data['advancements.slavicmyths.'+name+'.description']=(ru_desc,en_desc)[index]
        p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

if __name__=='__main__': generate(Path(__file__).resolve().parents[1])
