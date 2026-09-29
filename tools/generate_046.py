"""Targeted art polish for physical items and placed blocks. Runs after older generators."""
from pathlib import Path
import json
from generate_045 import Art, C, encode

C.update({
 'u':(45,28,25,255),'U':(91,52,38,255),'T':(210,161,101,255),
 'N':(25,39,55,255),'A':(77,101,122,255),'D':(70,82,96,255),
 'W':(248,240,209,255),'O':(132,65,34,255),'Y':(255,223,125,255),
 'q':(92,113,113,255),'Q':(185,196,177,255),'z':(69,77,55,255)})

def pitem(name):
    a=Art()
    if name=='thunder_axe':
        a.line(4,15,10,5,'u'); a.line(5,15,11,5,'B'); a.line(6,13,10,7,'t')
        a.box(4,2,10,7,'N'); a.line(3,2,9,1,'A'); a.line(2,3,2,7,'D'); a.line(3,8,8,8,'D')
        a.line(10,2,13,1,'N'); a.line(10,7,13,8,'N'); a.dot(1,5,'s'); a.dot(13,2,'s')
        a.line(5,3,9,3,'A'); a.line(5,5,8,5,'s'); a.line(7,2,6,5,'c'); a.line(6,5,9,6,'c')
        a.dot(8,4,'W'); a.dot(10,8,'y'); a.line(5,11,7,12,'D')
    elif name in ('carved_staff','storm_staff'):
        a.line(3,15,9,5,'u'); a.line(4,15,10,5,'B'); a.line(5,13,8,8,'t')
        a.dot(3,14,'T'); a.line(5,11,7,11,'e'); a.dot(6,12,'U')
        a.line(9,5,8,3,'U'); a.line(10,5,12,3,'B'); a.line(8,3,9,1,'u'); a.line(12,3,11,1,'u')
        if name=='carved_staff':
            a.dot(10,2,'G'); a.dot(9,4,'T'); a.dot(11,4,'t'); a.dot(7,9,'u')
        else:
            a.box(8,5,11,6,'D'); a.box(9,3,11,4,'A'); a.disc(10,2,2,2,'a')
            a.dot(10,1,'W'); a.dot(9,3,'c'); a.dot(12,2,'N'); a.line(6,10,7,8,'c')
            a.dot(8,6,'y')
    elif name in ('retainer_shield','perunite_shield'):
        dark=name=='perunite_shield'
        edge='N' if dark else 'u'; face='D' if dark else 'B'; lit='A' if dark else 't'
        for y in range(2,13):
            inset=max(0,(y-8)//3)
            a.line(3+inset,y,12-inset,y,edge)
            a.line(4+inset,y,11-inset,y,face)
            if y in (3,6,9): a.line(5+inset,y,10-inset,y,lit)
        a.line(4,2,11,2,'S' if dark else 'T'); a.line(3,3,4,11,'s' if dark else 't')
        a.line(12,3,11,11,'s' if dark else 't')
        if dark:
            a.box(6,5,9,9,'N'); a.line(8,4,7,7,'c'); a.line(7,7,10,7,'c'); a.line(10,7,7,10,'c')
            a.dot(5,4,'Y'); a.dot(10,11,'a')
        else:
            a.disc(8,7,3,3,'D'); a.disc(8,7,2,2,'S'); a.dot(7,6,'W')
            a.line(5,3,5,11,'u'); a.line(11,3,11,11,'u')
    elif name in {'warding_charm','perun_charm','amber_charm','forest_charm','hunter_charm','traveler_charm'}:
        a.line(5,2,8,1,'u'); a.line(8,1,11,2,'u'); a.line(5,2,6,5,'B'); a.line(11,2,10,5,'B')
        a.box(7,4,9,5,'T'); a.dot(8,4,'D')
        if name=='warding_charm':
            a.box(5,6,11,12,'U'); a.box(6,7,10,11,'B'); a.line(7,8,9,10,'e'); a.line(9,8,7,10,'e'); a.dot(8,6,'T')
        elif name=='perun_charm':
            a.line(5,7,8,6,'N'); a.line(8,6,11,8,'N'); a.line(11,8,9,12,'N'); a.line(9,12,5,11,'A')
            a.line(8,7,7,10,'c'); a.line(7,10,10,10,'c'); a.dot(9,8,'W')
        elif name=='amber_charm':
            a.disc(8,9,3,4,'O'); a.disc(8,9,2,3,'o'); a.dot(7,7,'Y'); a.dot(9,10,'e'); a.dot(6,11,'B')
        elif name=='forest_charm':
            a.line(8,6,5,10,'g'); a.line(8,6,11,10,'g'); a.line(5,10,8,13,'z'); a.line(11,10,8,13,'z')
            a.line(8,7,8,12,'L'); a.dot(6,9,'G'); a.dot(10,9,'G')
        elif name=='hunter_charm':
            a.line(5,7,11,12,'e'); a.line(11,7,5,12,'e'); a.dot(5,7,'W'); a.dot(11,7,'W')
            a.disc(8,9,1,1,'r'); a.dot(8,10,'Y')
        else:
            a.disc(8,9,3,3,'D'); a.line(8,5,8,12,'W'); a.line(5,9,11,9,'Q'); a.dot(8,9,'Y'); a.dot(7,12,'A')
    elif name=='ancient_sign':
        a.box(5,2,10,12,'D'); a.line(4,4,4,10,'s'); a.line(11,3,12,8,'k'); a.dot(5,2,'Q'); a.dot(10,12,'N')
        a.box(6,4,9,10,'q'); a.line(8,5,8,10,'e'); a.line(6,7,10,7,'Y')
        a.dot(7,3,'s'); a.dot(9,11,'u'); a.line(5,12,8,14,'D')
    elif name=='ancient_coin':
        a.disc(8,8,5,5,'O'); a.disc(8,8,4,4,'B'); a.line(5,5,10,5,'t')
        a.line(8,6,8,10,'e'); a.line(6,8,10,8,'e'); a.dot(5,10,'u'); a.dot(11,7,'u')
        a.dot(3,7,'k'); a.dot(10,12,'y')
    elif name=='idol_fragment':
        a.line(4,3,10,2,'u'); a.line(10,2,13,7,'D'); a.line(13,7,9,13,'k'); a.line(9,13,3,10,'u')
        a.box(5,5,10,10,'s'); a.line(5,5,10,5,'Q'); a.line(7,6,7,9,'e'); a.line(7,9,10,9,'e')
        a.dot(11,11,'t'); a.dot(4,9,'b')
    elif name=='lore_book':
        a.box(3,2,11,13,'u'); a.line(2,4,2,12,'U'); a.line(12,3,13,12,'e'); a.line(4,13,12,13,'W')
        a.box(4,3,10,11,'n'); a.line(4,3,10,3,'A'); a.line(5,10,9,10,'U')
        a.line(7,5,9,7,'T'); a.line(9,7,7,9,'T'); a.dot(8,7,'Y'); a.box(3,7,4,8,'D')
    elif name=='amber':
        a.line(4,4,9,2,'O'); a.line(9,2,13,6,'O'); a.line(13,6,11,12,'O'); a.line(11,12,5,13,'B'); a.line(5,13,2,8,'O')
        a.disc(8,8,4,4,'o'); a.disc(8,7,2,3,'y'); a.line(6,5,9,4,'Y'); a.dot(9,9,'B'); a.dot(10,10,'e')
    elif name in ('ritual_bowl','ritual_knife','thunder_stone'):
        if name=='ritual_bowl':
            a.line(3,6,12,6,'Q'); a.line(3,7,5,11,'D'); a.line(12,7,10,11,'D'); a.line(5,11,10,11,'S')
            a.line(6,8,9,8,'N'); a.line(7,12,7,13,'D'); a.line(5,14,10,14,'S'); a.dot(8,6,'Y')
        elif name=='ritual_knife':
            a.line(3,14,7,10,'u'); a.line(4,14,8,10,'U'); a.box(6,9,9,10,'y'); a.line(9,8,13,3,'D')
            a.line(10,8,13,4,'W'); a.dot(11,5,'S'); a.dot(5,12,'r')
        else:
            a.disc(8,8,5,5,'N'); a.disc(8,8,4,4,'D'); a.dot(4,7,'k'); a.dot(11,11,'s')
            a.line(9,4,7,8,'c'); a.line(7,8,10,8,'W'); a.line(10,8,6,12,'c')
    elif name in ('perunite_sword','silver_sword','silver_dagger','bone_knife','club','battle_axe','spear','silver_spear','perunite_axe'):
        from generate_045 import item
        a.a=[list(r) for r in item(name)]
        if name in ('perunite_sword','perunite_axe'):
            a.dot(7,7,'A'); a.dot(8,6,'c'); a.dot(5,11,'T')
        elif name.startswith('silver'):
            a.dot(10,5,'W'); a.dot(9,6,'D'); a.dot(6,11,'S')
        elif name in ('club','battle_axe'):
            a.dot(9,4,'T' if name=='club' else 'Q'); a.dot(10,6,'u' if name=='club' else 'D')
        else: a.dot(10,5,'Q'); a.dot(6,11,'u')
    else: raise ValueError(name)
    return a.rows()

ITEMS={'thunder_axe','carved_staff','storm_staff','retainer_shield','perunite_shield',
       'warding_charm','perun_charm','amber_charm','forest_charm','hunter_charm','traveler_charm',
       'ancient_sign','ancient_coin','idol_fragment','lore_book','amber','ritual_bowl','ritual_knife',
       'thunder_stone','perunite_sword','perunite_axe','silver_sword','silver_dagger','bone_knife',
       'club','battle_axe','spear','silver_spear'}

def pblock(name):
    a=Art()
    if name in ('silver_ore','perunite_ore'):
        for y in range(16):
            for x in range(16): a.dot(x,y,'s' if (x*7+y*5)%11<4 else ('d' if (x+2*y)%7<3 else 'q'))
        lumps=((3,3),(11,2),(7,8),(12,12),(2,12))
        for x,y in lumps:
            a.dot(x,y,'D' if name=='silver_ore' else 'N'); a.dot(x+1,y,'S' if name=='silver_ore' else 'a')
            a.dot(x,y+1,'W' if name=='silver_ore' else 'c'); a.dot(x+1,y+1,'D' if name=='silver_ore' else 'A')
    elif name in ('carved_oak_pillar','carved_oak_block','carved_birch_planks','wall_carving'):
        pale=name=='carved_birch_planks'; base='e' if pale else 'B'; shade='t' if pale else 'U'; light='W' if pale else 'T'
        a.box(0,0,15,15,base)
        for y in range(16):
            for x in range(16):
                if (x*5+y*3)%17==0: a.dot(x,y,shade)
        a.line(0,0,15,0,light); a.line(0,15,15,15,'u'); a.line(0,0,0,15,shade); a.line(15,0,15,15,'u')
        if name=='carved_oak_pillar':
            a.line(4,1,4,14,'u'); a.line(11,1,11,14,'u'); a.line(5,3,9,6,'u'); a.line(9,6,6,10,'u'); a.line(6,10,10,13,'u')
            a.line(5,4,9,7,light)
        elif name=='carved_birch_planks':
            for y in (4,9,13): a.line(1,y,14,y,shade)
            a.line(3,5,7,8,'U'); a.line(7,8,12,5,'U'); a.line(4,6,7,9,light)
        elif name=='wall_carving':
            a.box(2,2,13,13,shade); a.line(3,3,12,3,light); a.line(3,12,12,12,'u')
            a.line(5,5,8,8,'u'); a.line(11,5,8,8,'u'); a.line(8,8,8,11,'u')
            a.line(5,6,8,9,light); a.dot(8,8,'y')
        else:
            a.box(3,3,12,12,shade); a.line(4,4,11,11,'u'); a.line(11,4,4,11,'u')
            a.disc(8,8,2,2,'u'); a.dot(7,7,light); a.line(3,3,12,3,light)
    elif name=='straw_block':
        a.box(0,0,15,15,'t')
        for y in (1,4,8,11,14): a.line(0,y,15,y,'y')
        for x in (3,8,12): a.line(x,0,x+1,15,'B')
        a.line(0,7,15,7,'u'); a.dot(5,2,'e'); a.dot(10,10,'O')
    elif name=='amber_block':
        a.box(0,0,15,15,'O'); a.box(1,1,14,14,'o'); a.line(1,1,13,1,'Y'); a.line(2,3,10,3,'y')
        a.line(3,5,12,12,'B'); a.line(4,5,11,11,'y'); a.dot(11,6,'W'); a.dot(6,9,'U')
    elif name=='altar':
        a.box(0,0,15,15,'D')
        for y in range(16):
            for x in range(16):
                if (x*3+y*7)%13==0: a.dot(x,y,'s')
        a.box(2,2,13,13,'s'); a.line(2,2,13,2,'Q'); a.line(2,13,13,13,'N')
        a.box(5,5,10,10,'d'); a.line(8,5,8,10,'q'); a.line(5,8,10,8,'q'); a.dot(8,8,'c')
        a.dot(3,4,'N'); a.dot(12,11,'N')
    elif name=='hearth':
        a.box(0,0,15,15,'U'); a.line(0,0,15,0,'T'); a.line(0,15,15,15,'u')
        for x in (3,12): a.line(x,2,x,13,'b')
        a.box(4,4,11,10,'u'); a.box(5,5,10,9,'N'); a.disc(8,8,2,2,'o'); a.dot(8,6,'Y')
        a.line(3,12,12,12,'e'); a.line(3,13,12,13,'B'); a.dot(4,3,'T')
    elif name=='ancient_idol':
        a.box(0,0,15,15,'B'); a.line(0,0,15,0,'T'); a.line(0,15,15,15,'u')
        for y in (4,10): a.line(2,y,13,y,'U')
        a.dot(6,6,'u'); a.dot(9,6,'u'); a.line(6,11,9,11,'u'); a.line(8,7,8,10,'e')
        a.dot(4,12,'t'); a.dot(11,3,'t')
    elif name=='ritual_candle':
        a.box(5,10,10,13,'D'); a.line(5,10,10,10,'S'); a.box(6,4,9,10,'e'); a.line(6,4,6,9,'W')
        a.dot(8,3,'u'); a.dot(8,2,'o'); a.dot(8,1,'Y'); a.dot(9,3,'r')
    else: raise ValueError(name)
    return a.rows()

BLOCKS={'altar','hearth','ancient_idol','ritual_candle','carved_oak_pillar','carved_oak_block',
        'carved_birch_planks','wall_carving','straw_block','amber_block','silver_ore','perunite_ore'}

def generate(root):
    res=root/'src/main/resources/assets/slavicmyths'
    for name in sorted(ITEMS): (res/f'textures/item/{name}.png').write_bytes(encode(pitem(name)))
    for name in sorted(BLOCKS): (res/f'textures/block/{name}.png').write_bytes(encode(pblock(name)))
    def cube(lo,hi,faces):
        return {'from':lo,'to':hi,'faces':{side:{'texture':'#'+texture} for side,texture in faces.items()}}
    sides=('north','south','east','west')
    def faces(side,top=None,bottom=None):
        return {**{s:side for s in sides},'up':top or side,'down':bottom or side}
    def save(name,textures,elements):
        data={'textures':{**{k:'slavicmyths:block/'+v for k,v in textures.items()},'particle':'slavicmyths:block/'+name},'elements':elements}
        (res/f'models/block/{name}.json').write_text(json.dumps(data,indent=2)+'\n')
    # Each profile uses several cuboids and distinct face materials. All coordinates remain inside 16px.
    save('altar',{'stone':'altar','rim':'altar_rim','top':'altar_top'},[
        cube([2,0,2],[14,3,14],faces('stone','rim')),
        cube([4,3,4],[12,10,12],faces('stone')),
        cube([1,10,1],[15,12,15],faces('rim','top'))])
    save('hearth',{'wood':'hearth','top':'hearth_top','dark':'hearth_dark'},[
        cube([1,0,1],[15,3,15],faces('wood','top')),
        cube([3,3,3],[13,11,13],faces('wood','dark')),
        cube([2,11,2],[14,13,14],faces('wood','top')),
        cube([4,4,2],[12,10,4],faces('dark'))])
    save('ancient_idol',{'wood':'ancient_idol','carving':'idol_front','top':'hearth_top'},[
        cube([3,0,3],[13,2,13],faces('wood')),
        cube([5,2,5],[11,12,11],{**faces('wood'),'north':'carving'}),
        cube([4,12,4],[12,15,12],{**faces('wood','top'),'north':'carving'})])
    save('ritual_candle',{'holder':'ritual_candle','wax':'candle_wax','flame':'candle_flame'},[
        cube([5,0,5],[11,2,11],faces('holder')),
        cube([6,2,6],[10,11,10],faces('wax')),
        cube([7,11,7],[9,14,9],faces('flame'))])
    # Face sheets used by the multipart models above.
    extras={
      'altar_rim':lambda a:(a.box(0,0,15,15,'D'),a.box(2,2,13,13,'s'),a.line(2,2,13,2,'Q'),a.line(2,13,13,13,'N'),a.dot(4,5,'q'),a.dot(11,10,'q')),
      'altar_top':lambda a:(a.box(0,0,15,15,'s'),a.box(2,2,13,13,'D'),a.box(3,3,12,12,'q'),a.line(8,4,8,11,'N'),a.line(5,8,11,8,'N'),a.dot(8,8,'c'),a.dot(3,3,'Q')),
      'hearth_top':lambda a:(a.box(0,0,15,15,'B'),a.line(0,0,15,0,'T'),a.line(0,15,15,15,'u'),a.box(4,4,11,11,'t'),a.dot(8,8,'e')),
      'hearth_dark':lambda a:(a.box(0,0,15,15,'u'),a.box(3,3,12,12,'N'),a.disc(8,9,3,3,'O'),a.dot(8,6,'Y'),a.dot(9,8,'o')),
      'idol_front':lambda a:(a.box(0,0,15,15,'U'),a.line(0,0,15,0,'T'),a.dot(5,5,'u'),a.dot(10,5,'u'),a.line(8,6,8,10,'e'),a.line(6,12,10,12,'u')),
      'candle_wax':lambda a:(a.box(0,0,15,15,'e'),a.line(0,0,15,0,'W'),a.line(15,1,15,15,'t'),a.dot(4,10,'y')),
      'candle_flame':lambda a:(a.box(0,0,15,15,'o'),a.box(4,4,11,11,'y'),a.dot(7,5,'Y'),a.dot(8,8,'W'))}
    for name,draw in extras.items():
        a=Art(); draw(a); (res/f'textures/block/{name}.png').write_bytes(encode(a.rows()))
    # Distinct plate edges and reduced luminous areas on the two vanilla armor UV sheets.
    for layer in (1,2):
        a=Art(64,32)
        for x0,y0,x1,y1 in ((0,0,31,15),(16,16,39,31),(40,16,55,31),(0,16,15,31)):
            a.box(x0,y0,x1,y1,'N'); a.line(x0,y0,x1,y0,'A'); a.line(x0,y1,x1,y1,'d')
            for x in range(x0+3,x1,7): a.line(x,y0+2,x,y1-2,'D')
        a.line(20,18,35,18,'S'); a.line(21,21,34,21,'A'); a.line(24,23,27,28,'c'); a.line(27,28,31,23,'a')
        a.line(4,3,11,3,'S'); a.line(5,5,10,5,'a'); a.dot(8,7,'c')
        if layer==2: a.line(3,19,12,19,'S'); a.line(5,21,5,29,'A'); a.line(10,21,10,29,'A')
        (res/f'textures/models/armor/perunite_layer_{layer}.png').write_bytes(encode(a.rows()))

    from polish_046 import finish
    finish(res)

if __name__=='__main__': generate(Path(__file__).resolve().parents[1])
