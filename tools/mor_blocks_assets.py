"""Reproducible, material-specific 16px corruption assets. Vanilla files are read only."""
from pathlib import Path
import json, zipfile, io, random
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'
VANILLA=Path('C:/Users/pavel/.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar')
IDS=['dirt','grass_block','stone','cobblestone','sand','gravel','oak_log','oak_leaves','oak_planks','birch_log','sandstone','obsidian','stone_bricks','moss_block']
RU=['Поражённая земля','Поражённый дёрн','Поражённый камень','Поражённый булыжник','Поражённый песок','Поражённый гравий','Поражённое дубовое бревно','Поражённая дубовая листва','Поражённые дубовые доски','Поражённое берёзовое бревно','Поражённый песчаник','Поражённый обсидиан']+['Поражённый каменный кирпич','Поражённый мох']
def write(path,data):
    p=RES/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def asset(kind,name): return f'assets/slavicmyths/{kind}/{name}.json'
def main():
    with zipfile.ZipFile(VANILLA) as z:
        textures=set()
        for name in IDS:
            state=json.loads(z.read(f'assets/minecraft/blockstates/{name}.json'))
            model_names=set()
            def rewrite(value):
                if isinstance(value,dict):
                    for k,v in value.items():
                        if k=='model':model_names.add(v.split('/')[-1]);value[k]='slavicmyths:block/mor_'+v.split('/')[-1]
                        else:rewrite(v)
                elif isinstance(value,list):
                    for v in value:rewrite(v)
            rewrite(state);write(asset('blockstates','mor_'+name),state)
            for m in model_names:
                model=json.loads(z.read(f'assets/minecraft/models/block/{m}.json'))
                for k,v in list(model.get('textures',{}).items()):
                    if v.startswith('minecraft:block/'):
                        t=v.split('/')[-1];textures.add(t);model['textures'][k]='slavicmyths:block/mor_'+t
                if name=='oak_leaves':model['render_type']='minecraft:cutout'
                write(asset('models/block','mor_'+m),model)
            write(asset('models/item','mor_'+name),{'parent':'slavicmyths:block/mor_'+name})
            # Authoritative block loot delegates to the vanilla table, retaining all conditions.
            write(f'data/slavicmyths/loot_table/blocks/mor_{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:loot_table','value':'minecraft:blocks/'+name}]}]})
        for t in sorted(textures):
            im=Image.open(io.BytesIO(z.read(f'assets/minecraft/textures/block/{t}.png'))).convert('RGBA')
            assert im.size==(16,16),(t,im.size)
            if t in ('grass_block_top','oak_leaves'):
                for y in range(16):
                    for x in range(16):
                        r,g,b,a=im.getpixel((x,y));im.putpixel((x,y),(int(r*.42),int(g*.66),int(b*.25),a))
            rng=random.Random('mor:'+t);d=ImageDraw.Draw(im)
            # Distinct paths follow wood grain / plank seams / stone fracture / end rings.
            path=[]
            if t.endswith('_top') and 'log' in t:
                path=[(x,3) for x in range(3,13)]+[(12,y) for y in range(4,13)]+[(x,12) for x in range(4,12)]
            elif t=='oak_planks':path=[(x,4 if x<8 else 12) for x in range(16)]+[(9,y) for y in range(5,12)]
            elif t.startswith('sandstone'):
                path=[(x,5+x//7) for x in range(16)]+[(x,11) for x in range(4,13)]
            elif t=='obsidian':
                path=[(3, y) for y in range(5,12)]+[(12,y) for y in range(5,12)]+[(x,3) for x in range(5,11)]+[(x,13) for x in range(5,11)]+[(4,4),(11,4),(4,12),(11,12),(7,7),(8,8),(9,9)]
            elif t=='gravel':
                path=[(2,3),(3,4),(5,7),(5,8),(6,8),(10,2),(11,3),(12,3),(12,10),(11,11),(10,12),(3,13),(4,14),(7,4),(8,5),(9,6)]
            else:
                x=rng.randrange(4,12)
                for y in range(16):
                    x=max(1,min(14,x+rng.choice([-1,0,1])));path.append((x,y))
                for j in range(12):path.append((max(0,min(15,path[6][0]+j//2)),max(0,6-j//3)))
            # Paint fracture shadows first so later neighbours cannot erase red seams.
            for x,y in path:
                if im.getpixel((x,y))[3]==0:continue
                for dx in [-1,1]:
                    if 0<=x+dx<16 and im.getpixel((x+dx,y))[3]>0:d.point((x+dx,y),fill=(32,25,31,255))
            for x,y in path:
                if im.getpixel((x,y))[3]>0:d.point((x,y),fill=(181,31,48,255) if rng.random()<.65 else (101,25,35,255))
            for x,y in path[::13]:
                if im.getpixel((x,y))[3]:d.point((x,y),fill=(237,67,80,255))
            p=RES/f'assets/slavicmyths/textures/block/mor_{t}.png';p.parent.mkdir(parents=True,exist_ok=True);im.save(p)
        for tag in ['mineable/pickaxe','mineable/shovel','mineable/axe','mineable/hoe','needs_diamond_tool','logs','logs_that_burn','leaves','sand','dirt','planks']:
            vanilla=json.loads(z.read(f'data/minecraft/tags/block/{tag}.json'))
            values=['slavicmyths:mor_'+n for n in IDS if 'minecraft:'+n in vanilla['values']]
            if values:
                p=RES/f'data/minecraft/tags/block/{tag}.json'
                data=json.loads(p.read_text(encoding='utf-8')) if p.exists() else {'replace':False,'values':[]}
                data['values']=list(dict.fromkeys(data['values']+values));write(p.relative_to(RES),data)
    for lang in ['ru_ru','en_us']:
        path=f'assets/slavicmyths/lang/{lang}.json';data=json.loads((RES/path).read_text(encoding='utf-8'))
        for n,ru in zip(IDS,RU):data['block.slavicmyths.mor_'+n]=ru if lang=='ru_ru' else 'Corrupted '+n.replace('_',' ').title()
        data['death.attack.mor_block']='%1$s погибает от поражённой земли' if lang=='ru_ru' else '%1$s succumbed to corrupted ground'
        data['death.attack.mor_block.player']='%1$s погибает от поражённой земли, спасаясь от %2$s' if lang=='ru_ru' else '%1$s succumbed to corrupted ground while escaping %2$s'
        write(path,data)
    write('data/slavicmyths/damage_type/mor_block.json',{'message_id':'mor_block','scaling':'never','exhaustion':.1,'effects':'hurt'})
if __name__=='__main__':main()
