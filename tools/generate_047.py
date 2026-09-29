"""Weapon-only native pixel materials and physical JSON models. Run after generate_046.

Silhouettes before construction (model units; third-person scale is common):
dagger 12: narrow silver blade, tiny guard, leather grip;
silver sword 22: 16-unit straight blade, crossguard; perunite sword 23: broad stepped blade;
spears 40: 30-unit wood shaft, leaf/diamond/angular heads; staves 40: carved fork/caged stone;
axes 23/28: bearded head / broad hooked mythic head, long haft;
maces 23/25: flanged iron / segmented perunite crown; berdysh 39: long side crescent;
shields 16/18: round planked disc / heavy tapered plate with four inlays.
"""
from pathlib import Path
import json, struct, zlib

ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources/assets/slavicmyths'
MATS=['wood','leather','steel','silver','dark','energy','gold','bone',
      'cloth','steel_edge','silver_edge','dark_edge','rust','black','amber','white']
PALETTES=[
 ['332218','68432b','a17145','c69a61'],['261d1c','49322b','74503b','a17a57'],
 ['26323e','4c5c69','8394a0','b9c7cb'],['2b3d50','627f93','afc7d1','ecf5ee'],
 ['17232e','2e4053','4a6175','7890a0'],['204052','38788d','81c9d6','dcf8f1'],
 ['5c4229','9b773d','c3a257','ecd090'],['796246','b99d76','dbc6a0','f0e2c5'],
 ['352326','613536','94524a','b97861'],['3a4754','718591','b1c1c5','d6e1db'],
 ['536c7d','97b4c5','d7e6e6','f4faf2'],['233345','394e62','6b8497','a9c2ca'],
 ['402d24','674331','926344','b88b5a'],['111b25','202d38','354553','57636c'],
 ['733b22','aa6129','dda040','f7d577'],['8e9c9b','becfc8','e5ecda','fbfae8']]
GRAIN=['12111211','12121211','12021201','12121201','11121201','11221211','11211211','11211221']
METAL=['22333211','12222110','12221110','12211110','12111100','12111100','11111000','10000000']
WRAP=['22221100','11222211','00112222','11001122','22110011','22221100','11222211','00112222']

def write(path,data):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

def png(path,rows):
    h,w=len(rows),len(rows[0])
    def chunk(t,d):return struct.pack('!I',len(d))+t+d+struct.pack('!I',zlib.crc32(t+d)&0xffffffff)
    raw=b''.join(b'\0'+bytes(c for pixel in row for c in (*pixel,255)) for row in rows)
    path.write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('!IIBBBBB',w,h,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b''))

def atlas(name):
    rows=[[(0,0,0)]*32 for _ in range(32)]
    for n,pal in enumerate(PALETTES):
        colors=[tuple(int(c[i:i+2],16) for i in (0,2,4)) for c in pal]
        pattern=GRAIN if n==0 else WRAP if n in (1,8) else METAL
        for y in range(8):
            for x in range(8):rows[(n//4)*8+y][(n%4)*8+x]=colors[int(pattern[y][x])]
        # Small, deterministic material wear; silhouettes come from authored geometry.
        x=(sum(map(ord,name))+n)%5+1;y=(len(name)+n*3)%5+2
        rows[(n//4)*8+y][(n%4)*8+x]=colors[1]
    png(RES/f'textures/item/{name}.png',rows)

def uv(mat):
    n=MATS.index(mat);x=n%4*4;y=n//4*4;return [x,y,x+4,y+4]

def part(lo,hi,mat,front=None,rot=None):
    faces={s:{'texture':'#body','uv':uv(front if front and s in ('north','south') else mat)} for s in ('north','south','east','west','up','down')}
    e={'from':lo,'to':hi,'faces':faces}
    if rot:e['rotation']={'origin':rot[0],'axis':rot[1],'angle':rot[2]}
    return e

def shaft(els,bottom,top,mat='wood',width=1.25):
    els.append(part([8-width/2,bottom,8-width/2],[8+width/2,top,8+width/2],mat))

def ring(els,y,mat='steel',width=1.8,h=.6):
    els.append(part([8-width/2,y,8-width/2],[8+width/2,y+h,8+width/2],mat))

def grip(els,lo,hi,mat='leather',width=1.55):
    shaft(els,lo,hi,mat,width)
    for y in range(int(lo)+1,int(hi),2):ring(els,y,'rust' if mat=='leather' else mat,width+.06,.22)

def blade(els,segments,mat,edge):
    # Each segment is a different width, with real edge geometry and a raised central ridge.
    for y,top,w in segments:
        els.append(part([8-w/2,y,7.65],[8+w/2,top,8.35],mat))
        els.append(part([8-w/2,y,7.6],[8-w/2+.25,top,8.4],edge))
        els.append(part([7.86,y,7.45],[8.14,top,8.55],edge if mat=='silver' else mat))

def transforms(height,long=False):
    # Grip is always centered around y=8, unlike the old diagonal inventory sprites.
    sc=.85;gui=min(.9,20/height)
    return {
      'thirdperson_righthand':{'rotation':[0,-90,0],'translation':[0,1.5,0],'scale':[sc]*3},
      'thirdperson_lefthand':{'rotation':[0,90,0],'translation':[0,1.5,0],'scale':[sc]*3},
      'firstperson_righthand':{'rotation':[0,-90,15 if long else 5],'translation':[1,0,-2 if long else 0],'scale':[.7]*3},
      'firstperson_lefthand':{'rotation':[0,90,-15 if long else -5],'translation':[1,0,-2 if long else 0],'scale':[.7]*3},
      'gui':{'rotation':[0,-15,-40],'translation':[0,0,0],'scale':[gui]*3},
      'ground':{'rotation':[0,0,90],'translation':[0,3,0],'scale':[.4]*3},
      'fixed':{'rotation':[0,0,-40],'translation':[0,0,0],'scale':[gui*.75]*3}}

def save(name,els,long=False,display=None):
    atlas(name)
    lo=min(e['from'][1] for e in els);hi=max(e['to'][1] for e in els)
    ds=display or transforms(hi-lo,long)
    # Center GUI only; held grip remains at the hand pivot y=8.
    if not display:
        offset=(8-(lo+hi)/2)*ds['gui']['scale'][1]
        ds['gui']['translation']=[offset*.643,offset*.766,0]
    data={'gui_light':'front','textures':{'body':f'slavicmyths:item/{name}','particle':f'slavicmyths:item/{name}'},'display':ds,'elements':els}
    write(RES/f'models/item/{name}.json',data)
    return data

def weapons():
    for name in ('silver_dagger','silver_sword','perunite_sword','bone_knife','ritual_knife'):
        e=[]
        if name=='silver_dagger':
            grip(e,4,8);ring(e,3.5,'silver',1.65)
            e.append(part([6.8,8,7.5],[9.2,8.6,8.5],'steel'))
            blade(e,[(8.6,11,1.6),(11,13.5,1.25),(13.5,15,.75),(15,15.5,.3)],'silver','silver_edge')
        elif name=='silver_sword':
            grip(e,3,8);ring(e,2,'silver',2,1)
            e.append(part([4.5,8,7.25],[11.5,9,8.75],'steel','silver'))
            e.append(part([4,7.5,7.1],[5,8.5,8.9],'silver'))
            e.append(part([11,7.5,7.1],[12,8.5,8.9],'silver'))
            blade(e,[(9,19,1.65),(19,22,1.25),(22,23.5,.75),(23.5,24,.3)],'silver','silver_edge')
        elif name=='perunite_sword':
            grip(e,2,8,'leather',1.8);ring(e,1,'dark',2.3,1);ring(e,1.3,'energy',1,.3)
            e.append(part([4,8,6.9],[12,9.2,9.1],'dark'))
            for x in (4,10):e.append(part([x,9,7],[x+2,10,9],'dark_edge'))
            blade(e,[(9.2,12,3.4),(12,20,2.8),(20,22,2.2),(22,23,1.4),(23,24,.5)],'dark','dark_edge')
            for x,y,w,h in ((8.4,12,.3,2),(8.1,14,.6,.35),(8.1,14.3,.3,1.4),(7.7,17,.3,1.8),(7.9,18.6,.7,.3),(8.3,18.9,.3,1)):
                e.append(part([x,y,7.39],[x+w,y+h,7.59],'energy'))
        elif name=='bone_knife':
            grip(e,4,8,'cloth',1.5);ring(e,7.5,'bone',1.7)
            for lo,hi in (([7,8,7.6],[9.2,10,8.4]),([7,10,7.6],[8.8,12,8.4]),([7.5,12,7.6],[8.7,13.5,8.4]),([7.7,13.5,7.6],[8.4,14.5,8.4])):e.append(part(lo,hi,'bone'))
            e.append(part([8.2,9.2,7.5],[8.4,11,7.65],'rust'))
        else:
            grip(e,3.5,8,'cloth');ring(e,3,'rust',1.8)
            e.append(part([6.3,8,7.25],[9.7,8.7,8.75],'gold'))
            blade(e,[(8.7,10,1.8),(10,12.4,3.2),(12.4,14,2.4),(14,15,1.2),(15,15.5,.4)],'silver','silver_edge')
            e.append(part([7.8,9,7.3],[8.15,9.7,7.5],'gold'))
        save(name,e)

    for name in ('spear','silver_spear','thunder_spear','carved_staff','storm_staff','berdysh'):
        e=[];shaft(e,-10,21,'wood' if name in ('spear','carved_staff') else 'leather',1.3)
        grip(e,5,10,'leather',1.5)
        ring(e,-10,'wood' if 'staff' in name else 'steel',1.4,.8)
        if name=='spear':
            ring(e,20,'steel',1.8,2)
            blade(e,[(22,23,1.8),(23,25,3),(25,26.5,2.4),(26.5,28,1.5),(28,30,.5)],'steel','steel_edge')
        elif name=='silver_spear':
            ring(e,19,'silver',1.9,2);ring(e,21,'leather',2,.5)
            blade(e,[(21.5,24,1.4),(24,26,2.3),(26,28,1.6),(28,30,.7)],'silver','silver_edge')
            e.append(part([7.9,22,7.3],[8.1,28,7.55],'silver_edge'))
        elif name=='thunder_spear':
            for y in (-6,1,11,18):ring(e,y,'dark_edge',1.8)
            ring(e,19,'dark',2.3,1)
            blade(e,[(20,22,2.4),(22,25,4),(25,27,3),(27,29,1.6),(29,30,.5)],'dark','dark_edge')
            for x,y in ((7.9,22),(8.2,23),(8.1,24),(7.8,25),(7.9,26)):
                e.append(part([x,y,7.3],[x+.3,y+1,7.6],'energy'))
        elif name=='berdysh':
            shaft(e,20,29,'wood');ring(e,18,'steel',1.9);ring(e,27,'steel',1.9)
            # Long single-sided crescent attached both at the tip and at the heel.
            for y,top,left in ((18,19,4),(19,21,2),(21,24,1.5),(24,26,2.5),(26,28,4),(28,29,6)):
                e.append(part([left,y,7.45],[8.3,top,8.55],'steel'))
                e.append(part([left,y,7.35],[left+.5,top,8.65],'steel_edge'))
            e.append(part([6,20,7.3],[7.3,27,7.6],'dark'))
        else:
            # Forked wood holds a carved crown or a caged perunite stone.
            shaft(e,20,24,'wood',1.7)
            e.append(part([5.5,22,7.2],[7.1,28,8.8],'wood',rot=([7,22,8],'z',-22.5)))
            e.append(part([9,23,7.2],[10.5,27.5,8.8],'wood',rot=([9,23,8],'z',22.5)))
            if name=='carved_staff':
                e.append(part([6,27,7.2],[9.8,29,8.8],'wood'))
                e.append(part([6.8,28.5,7.5],[8.8,30,8.5],'bone'))
                ring(e,20,'cloth',2,1.1)
            else:
                ring(e,22,'dark_edge',2.6,1)
                e.append(part([6.5,25,6.5],[9.5,28.5,9.5],'dark'))
                e.append(part([7.2,28.5,7.2],[8.8,30,8.8],'energy'))
                e.append(part([7.6,26,6.3],[8.1,28,6.6],'energy'))
                for x in (6,9.5):e.append(part([x,24,7],[x+.5,28,9],'steel'))
        save(name,e,True)

    for name in ('battle_axe','thunder_axe','club','mace','perunite_mace'):
        e=[];myth=name in ('thunder_axe','perunite_mace')
        shaft(e,1,22 if myth else 21,'wood',1.5);grip(e,3,9,'leather',1.75)
        if name=='club':
            for y,hi,w,x in ((10,14,2,8),(14,17,2.6,8.3),(17,20,3.5,8.3),(20,23,4,8)):
                e.append(part([x-w/2,y,7],[x+w/2,hi,9],'wood'))
            e.append(part([9.6,18,7.4],[11,19.5,8.6],'rust'))
            e.append(part([6.1,21,6.9],[7,22.4,7.3],'bone'))
        elif name=='battle_axe':
            ring(e,18,'steel',2.1,2)
            for y,hi,left,right in ((13,15,2,3.8),(15,17,1.5,6),(17,20,1.5,8.5),(20,22,2.5,8.5),(22,24,4.5,8.5)):
                e.append(part([left,y,7.2],[right,hi,8.8],'steel'))
                e.append(part([left,y,7.1],[left+.5,hi,8.9],'steel_edge'))
            e.append(part([8.5,19,7.4],[11,20.3,8.6],'steel'))
        elif name=='thunder_axe':
            for y in (2,10,18):ring(e,y,'dark_edge',2.2)
            for y,hi,left,right in ((14,16,-1,1),(16,18,-1,4),(18,23,0,8.5),(23,26,1.5,9),(26,27,4,8.5)):
                e.append(part([left,y,6.8],[right,hi,9.2],'dark'))
                e.append(part([left,y,6.7],[left+.6,hi,9.3],'dark_edge'))
            e.append(part([8.5,21,7],[12.5,23,9],'dark'))
            e.append(part([11,23,7.3],[12.5,25,8.7],'dark_edge'))
            for x,y,w,h in ((3,19,.4,2),(3.2,21,1.3,.3),(4.2,21,.35,2),(3.7,23,.8,.3)):
                e.append(part([x,y,6.6],[x+w,y+h,6.85],'energy'))
        else:
            ring(e,15,'dark_edge' if myth else 'steel',2,1)
            shaft(e,16,22,'dark' if myth else 'steel',3 if myth else 2.5)
            if myth:
                # Square crown, long buttress ribs and recessed dark segments.
                for x,z in ((5.2,6.3),(9.3,6.3),(6.3,5.2),(6.3,9.3)):
                    e.append(part([x,17,z],[x+1.4,23,z+1.4],'dark_edge'))
                e.append(part([6.3,22,6.3],[9.7,25,9.7],'dark'))
                e.append(part([7.6,19,6.3],[8.4,21,6.6],'energy'))
                e.append(part([6.3,18,7.6],[6.6,20,8.4],'energy'))
            else:
                # Six separate flanges; the head is neither a ball nor an axe blade.
                for angle in (0,45,-45):
                    e.append(part([5.5,17,7.6],[10.5,22,8.4],'steel_edge',rot=([8,19,8],'y',angle)))
                e.append(part([7.6,17,5.5],[8.4,22,10.5],'steel_edge'))
                ring(e,22,'steel',2,1)
        save(name,e,long=name=='thunder_axe')

def shields():
    for name in ('retainer_shield','perunite_shield'):
        e=[];heavy=name=='perunite_shield';rim='dark_edge' if heavy else 'steel';face='dark' if heavy else 'wood'
        # Rounded stepped disc vs a broad shoulder and tapered lower point.
        widths=[6,10,12,14,16,16,16,16,16,16,16,16,14,12,10,6] if not heavy else [10,14,16,18,18,18,18,18,18,18,16,16,14,12,10,8,6,4]
        for i,w in enumerate(widths):
            y=16-i if heavy else 15-i
            strip=part([8-w/2,y,7],[8+w/2,y+1,8.5],rim)
            for s in ('north','south'):
                u,v,_,_=uv(rim);strip['faces'][s]['uv']=[u,v+4*i/len(widths),u+4,v+4*(i+1)/len(widths)]
            e.append(strip)
            if i>0 and i<len(widths)-1 and w>4:
                strip=part([9-w/2,y,6.8],[7+w/2,y+1,7.05],face)
                for s in ('north','south'):
                    u,v,_,_=uv(face);strip['faces'][s]['uv']=[u,v+4*i/len(widths),u+4,v+4*(i+1)/len(widths)]
                e.append(strip)
        if not heavy:
            for x in (4.5,7,9.5,12):e.append(part([x,3,6.65],[x+.16,13,6.85],'leather'))
            for x,y in ((4,7),(11,9)):
                e.append(part([x,y,6.5],[x+1,y+.5,6.75],'rust'))
        size=4.4 if heavy else 3.5
        e.append(part([8-size/2,8-size/2,6],[8+size/2,8+size/2,7],'dark' if heavy else 'steel'))
        e.append(part([6.7,6.7,5.2],[9.3,9.3,6.2],'dark_edge' if heavy else 'silver'))
        e.append(part([7.4,7.4,4.8],[8.6,8.6,5.4],'steel_edge'))
        if heavy:
            for x,y in ((8,13),(8,3),(3,8),(13,8)):
                e.append(part([x-.8,y-.8,6.1],[x+.8,y+.8,6.9],'black'))
                e.append(part([x-.3,y-.5,5.95],[x+.3,y+.5,6.3],'energy'))
        for x,y in ((4,4),(12,4),(4,12),(12,12)):e.append(part([x-.25,y-.25,6.35],[x+.25,y+.25,6.8],'steel_edge'))
        # Back handle stays centered at the same hand pivot as the face.
        e.append(part([6,6,8.5],[7,10,10.5],'leather'))
        e.append(part([9,6,8.5],[10,10,10.5],'leather'))
        e.append(part([6,7.3,10],[10,8.7,11],'wood'))
        ds={
          'gui':{'rotation':[10,-20,0],'translation':[0,0,0],'scale':[.78]*3},
          'ground':{'rotation':[90,0,0],'translation':[0,3,0],'scale':[.5]*3},
          'fixed':{'rotation':[0,180,0],'translation':[0,0,0],'scale':[.7]*3},
          'thirdperson_righthand':{'rotation':[0,90,0],'translation':[0,2,-1],'scale':[1]*3},
          'thirdperson_lefthand':{'rotation':[0,90,0],'translation':[0,2,-1],'scale':[1]*3},
          'firstperson_righthand':{'rotation':[0,180,5],'translation':[-2,0,-2],'scale':[1]*3},
          'firstperson_lefthand':{'rotation':[0,180,-5],'translation':[-2,0,-2],'scale':[1]*3}}
        data=save(name,e,display=ds)
        data['overrides']=[{'predicate':{'blocking':1},'model':f'slavicmyths:item/{name}_blocking'}]
        write(RES/f'models/item/{name}.json',data)
        # Inherit geometry only through a separate model to avoid an override cycle.
        write(RES/f'models/item/{name}_body.json',{'textures':data['textures'],'elements':e,'gui_light':'front'})
        blocking={'parent':f'slavicmyths:item/{name}_body','display':dict(ds)}
        for hand in ('righthand','lefthand'):
            blocking['display']['thirdperson_'+hand]={'rotation':[30,135,0],'translation':[0,3,-1],'scale':[1]*3}
            blocking['display']['firstperson_'+hand]={'rotation':[0,180,-5],'translation':[-4,2,-3],'scale':[1]*3}
        write(RES/f'models/item/{name}_blocking.json',blocking)

def components():
    # Rolled strap with an open center and loose end; a separate hollow silver socket.
    e=[part([4,4,7],[12,6,9],'leather'),part([4,10,7],[12,12,9],'leather'),
       part([3,6,7],[5,10,9],'rust'),part([11,6,7],[13,10,9],'leather'),
       part([5,6,7.3],[6,10,8.7],'rust'),part([10,6,7.3],[11,10,8.7],'rust'),
       part([9,2,8],[11,5,9],'leather')]
    save('weapon_wrap',e)
    e=[part([4,4,6],[12,6,10],'silver'),part([4,10,6],[12,12,10],'silver'),
       part([4,6,6],[6,10,10],'silver_edge'),part([10,6,6],[12,10,10],'silver')]
    save('silver_fitting',e)

def data():
    names={'thunder_spear':('Громовое копьё','Thunder Spear'),'mace':('Булава','Mace'),
      'perunite_mace':('Перунитовая булава','Perunite Mace'),'berdysh':('Бердыш','Berdysh'),
      'weapon_wrap':('Кожаная оружейная обмотка','Leather Weapon Wrap'),'silver_fitting':('Серебряная оковка','Silver Fitting')}
    tips={
      'silver_dagger':('Короткий клинок для быстрых ударов. Серебро опасно для духов.','A short, fast blade. Silver harms vulnerable spirits.'),
      'silver_sword':('Длинный серебряный клинок против уязвимых духов.','A long silver blade against vulnerable spirits.'),
      'perunite_sword':('Тяжёлый клинок с прожилками Перунита.','A heavy blade veined with perunite.'),
      'bone_knife':('Грубый костяной нож.','A rough, worked-bone knife.'),
      'ritual_knife':('Листовидный серебряный клинок для обрядов.','A leaf-shaped silver ceremonial blade.'),
      'spear':('Длинное древковое оружие. Отталкивает цель.','A long polearm. Pushes the target back.'),
      'silver_spear':('Серебряный наконечник особенно опасен для некоторых духов.','Its silver point is especially harmful to certain spirits.'),
      'thunder_spear':('Гроза в острие: +2 урона при ударе, перезарядка 6 с.','Storm in the point: +2 damage on a hit, 6 s cooldown.'),
      'battle_axe':('Тяжёлый бородатый топор. Медленный, мощный удар.','A heavy bearded axe. Slow, powerful strikes.'),
      'thunder_axe':('Грозовое оружие с Перунитовыми прожилками.','A storm-forged weapon veined with perunite.'),
      'club':('Грубая дубина. Сильное отбрасывание.','A rough wooden club with strong knockback.'),
      'mace':('Ребристая головка. Медленный удар с отбрасыванием.','A flanged head. Slow strikes with knockback.'),
      'perunite_mace':('Тяжёлые Перунитовые сегменты. Очень медленный удар.','Heavy perunite segments. Very slow strikes.'),
      'berdysh':('Тяжёлое древковое рубящее оружие.','A heavy polearm with a long cutting blade.'),
      'carved_staff':('Длинный посох из резного дерева.','A long staff of carved wood.'),
      'storm_staff':('Грозовой импульс по ПКМ. Перезарядка 4 с.','Storm pulse on use. 4 s cooldown.'),
      'retainer_shield':('Удерживайте ПКМ для защиты спереди.','Hold use to block attacks from the front.'),
      'perunite_shield':('Прочный щит. Удерживайте ПКМ для защиты спереди.','A durable shield. Hold use to block frontal attacks.')}
    for n,lang in enumerate(('ru_ru','en_us')):
        p=RES/f'lang/{lang}.json';obj=json.loads(p.read_text(encoding='utf-8'))
        obj.update({f'item.slavicmyths.{k}':v[n] for k,v in names.items()})
        obj.update({f'tooltip.slavicmyths.weapon.{k}':v[n] for k,v in tips.items()});write(p,obj)
    recipes=ROOT/'src/main/resources/data/slavicmyths/recipes'
    def shaped(name,pattern,keys,count=1):
        write(recipes/f'{name}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':{k:{'item':v if ':' in v else 'slavicmyths:'+v} for k,v in keys.items()},'result':{'item':'slavicmyths:'+name,'count':count}})
    shaped('weapon_wrap',['L L',' S '],{'L':'minecraft:leather','S':'minecraft:string'},2)
    shaped('silver_fitting',[' N ','N N',' N '],{'N':'silver_nugget'})
    shaped('spear',['  I',' W ','S  '],{'I':'minecraft:iron_ingot','W':'weapon_wrap','S':'minecraft:stick'})
    shaped('silver_spear',[' IF',' S ',' W '],{'I':'silver_ingot','F':'silver_fitting','S':'spear','W':'weapon_wrap'})
    shaped('mace',['INI',' W ',' S '],{'I':'minecraft:iron_ingot','N':'minecraft:iron_nugget','W':'weapon_wrap','S':'minecraft:stick'})
    shaped('berdysh',['IIS','IWS','  S'],{'I':'minecraft:iron_ingot','W':'weapon_wrap','S':'minecraft:stick'})

def generate():
    weapons();shields();components();data()

if __name__=='__main__':generate()
