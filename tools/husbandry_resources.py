"""Reproducible, crisp 1.1.3 assets. Authored cuboids/UVs, no runtime image generation."""
from pathlib import Path
import json, math, random
from PIL import Image, ImageDraw

ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources'
A=RES/'assets/slavicmyths'
D=RES/'data/slavicmyths'

def write(path, data):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

def save(path,image):
    path.parent.mkdir(parents=True,exist_ok=True);image.save(path)

# Parts: name, pivot, local cuboid, material. Separate authored baby proportions.
def geometry(kind,baby):
    parts=[]
    def part(name,p,b,mat):parts.append((name,p,b,mat))
    if kind in ('goose','duck'):
        goose=kind=='goose'
        if baby:
            part('body',(0,18 if goose else 19,0),(-3.5,-3 if goose else -2.5,-4 if goose else -4.5,7,6 if goose else 5,8 if goose else 9),'down')
            part('neck',(0,15 if goose else 17,-3),(-1.5,-2,-1.5,3,4 if goose else 2,3),'down')
            part('head',(0,12.5 if goose else 14,-4),(-2.5,-2,-2,5,4,4),'head')
            part('beak',(0,13 if goose else 14.5,-6),(-1.5 if goose else -2,0,-2.5 if goose else -3,3 if goose else 4,1.5 if goose else 1,2.5 if goose else 3),'beak')
            wingY=17 if goose else 18;wingW=1;wingH=3;wingD=5 if goose else 6;wingX=3.5;legY=21;legH=2;legX=2;tailY=18 if goose else 19
        elif goose:
            part('body',(0,14,1),(-4.5,-4,-6,9,8,12),'body')
            part('breast',(0,13,-4),(-3.5,-3,-2,7,6,3),'breast')
            part('neck',(0,11,-4),(-1.5,-7,-1.5,3,8,3),'neck')
            part('head',(0,4,-4.5),(-2,-2,-2,4,4,4),'head')
            part('beak',(0,5,-6.5),(-1.5,-.5,-3,3,2,3),'beak')
            wingY=11;wingW=1.5;wingH=7;wingD=10;wingX=4.5;legY=18;legH=5;legX=2.5;tailY=13
        else:
            part('body',(0,17,0),(-4,-3,-5.5,8,6,11),'body')
            part('breast',(0,16,-4.5),(-3,-2,-1.5,6,4,2),'breast')
            part('neck',(0,14,-3.5),(-1.5,-2,-1.5,3,3,3),'neck')
            part('head',(0,11.5,-4),(-2,-2,-2,4,4,4),'head')
            part('beak',(0,12.5,-6),(-2,-.5,-3.5,4,1.5,3.5),'beak')
            wingY=15;wingW=1;wingH=4;wingD=8;wingX=4;legY=20;legH=3;legX=2.5;tailY=16
        for side,x in [('left',wingX),('right',-wingX)]:
            part(side+'_wing',(x,wingY,0),(0 if side=='left' else -wingW,0,-wingD/2,wingW,wingH,wingD),'wing')
        for side,x in [('left',legX),('right',-legX)]:
            part(side+'_leg',(x,legY,0),(-.5,0,-.5,1,legH,1),'feet')
            part(side+'_foot',(x,legY+legH,0),(-1,0,-2,2,1,3),'feet')
        part('tail',(0,tailY,5 if not baby else 3.5),(-2,0,0,4,2,3),'wing')
        eyeY=(12 if goose else 13.5) if baby else 3.5 if goose else 11
        eyeZ=-5.2 if baby else -5.6 if goose else -5.1
        for side,x in [('left',2.5 if baby else 2),('right',-2.7 if baby else -2.2)]:part(side+'_eye',(x,eyeY,eyeZ),(0,0,0,.2,.7,.7),'eyes')
    else:
        bodyY=18 if baby else 12;headY=15 if baby else 8.5
        part('body',(0,bodyY,0),(-3 if baby else -4.5,-2.5 if baby else -4,-4 if baby else -6,6 if baby else 9,5 if baby else 9,8 if baby else 13),'body')
        part('neck',(0,bodyY-1,-3 if baby else -5),(-2,-3,-1.5,4,5,4),'neck')
        part('head',(0,headY,-5 if baby else -7),(-2.5,-2.5,-2,5,5,4),'head')
        part('muzzle',(0,headY+1,-7 if baby else -9),(-1.5,-.5,-2,3,2,2),'muzzle')
        for side,x in [('left',2.5),('right',-2.5)]:
            part(side+'_ear',(x,headY-1,-5 if baby else -7),((0 if x>0 else -2),-.5,-.5,2,1,2),'ear')
            part(side+'_eye',(x if x>0 else x-.2,headY-.8,-6.5 if baby else -8.5),(0,0,0,.2,.8,.8),'eyes')
        if not baby:
            for side,x in [('left',1.5),('right',-1.5)]:
                part(side+'_horn',(x,headY-2.5,-7),(-.6,-3,0,1.2,3,1.2),'horn')
                part(side+'_horn_tip',(x,headY-5,-6.2),(-.5,-.7,0,1,1,2),'horn')
            part('beard',(0,headY+2,-7),(-.8,0,-1,1.6,2,2),'breast')
            part('collar',(0,bodyY-1,-5),(-2.4,-2,-1.7,4.8,1,4.4),'collar')
            part('bell',(0,bodyY-1,-7),(-.7,0,-.5,1.4,1.5,1),'bell')
        legY=20 if baby else 16;legH=3 if baby else 7
        for name,x,z in [('front_left',2 if baby else 3,-2.5 if baby else -4),('front_right',-2 if baby else -3,-2.5 if baby else -4),('back_left',2 if baby else 3,2.5 if baby else 5),('back_right',-2 if baby else -3,2.5 if baby else 5)]:
            part(name+'_leg',(x,legY,z),(-.75,0,-.75,1.5,legH,1.5),'leg')
            part(name+'_hoof',(x,legY+legH,z),(-1,0,-1,2,1,2),'hoof')
        part('tail',(0,bodyY-2,4 if baby else 7),(-.7,-1,0,1.4,2,3),'breast')
    return parts

PALETTES={
 'goose':{'body':(223,219,205),'breast':(239,232,211),'neck':(235,227,208),'head':(239,232,214),'wing':(178,180,175),'beak':(226,129,36),'feet':(211,120,35),'eyes':(24,21,18),'down':(221,211,158)},
 'duck':{'body':(133,90,57),'breast':(190,151,104),'neck':(118,82,53),'head':(69,50,37),'wing':(106,77,57),'beak':(227,146,47),'feet':(213,122,34),'eyes':(18,17,14),'down':(225,198,130)},
 'domestic_goat':{'body':(224,212,189),'breast':(227,215,193),'neck':(225,213,189),'head':(237,224,199),'muzzle':(210,192,159),'ear':(216,177,160),'eyes':(23,20,16),'horn':(145,128,97),'collar':(107,44,26),'bell':(205,152,37),'leg':(220,208,184),'hoof':(54,48,39)}}

def model_assets():
    code=[]
    manifest={}
    for k,kind in enumerate(('goose','duck','domestic_goat')):
        for baby in (False,True):
            image=Image.new('RGBA',(128,128),(0,0,0,0));draw=ImageDraw.Draw(image);rng=random.Random(k*20+int(baby))
            u=v=rowh=0;entries=[]
            code.append(f'        if(kind=={k} && baby=={str(baby).lower()}){{')
            for name,p,b,mat in geometry(kind,baby):
                x,y,z,w,h,d=b;uw=math.ceil(2*(w+d));vh=math.ceil(h+d)
                if u+uw>128:u=0;v+=rowh+1;rowh=0
                assert v+vh<128,(kind,baby,name,v,vh)
                color=PALETTES[kind][mat]
                if baby and kind=='goose' and mat in ('head','neck','body','breast','wing'):color=(191,185,160) if mat=='wing' else (223,211,156)
                if baby and kind=='duck' and mat in ('body','breast','neck','head','wing'):color=(215,185,117) if mat!='head' else (153,116,64)
                for py in range(v,v+vh):
                    for px in range(u,u+uw):
                        offset=rng.choice((-9,-5,0,0,4,7))
                        if mat not in ('eyes','beak','feet','hoof') and (px+py*2)%9==0:offset-=12
                        if kind=='domestic_goat' and mat in ('body','head','leg') and ((px//7+py//6)%7 in (1,2)):offset-=58
                        draw.point((px,py),fill=tuple(max(0,min(255,c+offset)) for c in color)+(255,))
                if kind=='duck' and mat=='wing' and not baby:draw.rectangle((u+1,v+vh//2,u+max(2,uw//3),v+vh//2+2),fill=(75,108,123,255))
                nums=lambda arr:','.join(f'{n:g}F' for n in arr)
                code.append(f'            add("{name}",{u},{v},new float[]{{{nums(p)}}},new float[]{{{nums(b)}}});')
                entries.append(dict(part=name,uv=[u,v],size=[uw,vh],pivot=p,cube=b));u+=uw+1;rowh=max(rowh,vh)
            code.append('        }')
            suffix='_baby' if baby else '';save(A/f'textures/entity/{kind}{suffix}.png',image);manifest[kind+suffix]=entries
    write(ROOT/'work/husbandry-model-uv.json',manifest)
    java='''package org.slavicmyths.client;
import java.util.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.husbandry.YardAnimal;

/** Six separately authored meshes: adult/baby goose, duck and domestic goat. */
public final class HusbandryModel extends EntityModel<YardAnimal> {
    private final FolkModelGeometry geometry=new FolkModelGeometry();
    private final ModelPart root=geometry.part(128,128);
    private final Map<String,ModelPart> parts=new LinkedHashMap<>();
    private final Map<String,float[]> rest=new LinkedHashMap<>();
    private final int kind;
    public HusbandryModel(int kind,boolean baby){this.kind=kind;
'''+ '\n'.join(code)+'''
    }
    private void add(String name,int u,int v,float[] p,float[] box){
        var part=geometry.part(128,128);
        boolean headChild=Set.of("beak","muzzle","left_eye","right_eye","left_ear","right_ear","left_horn","right_horn","left_horn_tip","right_horn_tip","beard").contains(name);
        if(headChild){var h=rest.get("head");p=new float[]{p[0]-h[0],p[1]-h[1],p[2]-h[2]};}
        part.setPos(p[0],p[1],p[2]);geometry.box(part,u,v,box[0],box[1],box[2],box[3],box[4],box[5]);geometry.attach(headChild?parts.get("head"):root,part);parts.put(name,part);rest.put(name,p);
    }
    @Override public void setupAnim(YardAnimal e,float walk,float amount,float age,float yaw,float pitch){
        parts.forEach((name,p)->{var r=rest.get(name);p.setPos(r[0],r[1],r[2]);p.xRot=p.yRot=p.zRot=0;});
        float turn=yaw*Mth.DEG_TO_RAD,tilt=pitch*Mth.DEG_TO_RAD;
        var head=parts.get("head");head.yRot=turn;head.xRot=tilt;
        if(kind==2&&e.eatingTicks>0){head.y+=3;head.xRot+=.65F;}
        parts.get("neck").yRot=turn*.35F;
        boolean swim=e.isInWater();float phase=swim?age*.35F:walk*(e.isBaby()?1.5F:.8F);float stride=swim?.25F:amount*.8F;
        int index=0;for(var entry:parts.entrySet())if(entry.getKey().endsWith("_leg")){
            var p=entry.getValue();p.xRot=Mth.cos(phase+((index++%2==0)?0:Mth.PI))*stride;
            String end=entry.getKey().replace("_leg",kind==2?"_hoof":"_foot");var foot=parts.get(end);if(foot!=null){foot.z+=Mth.sin(p.xRot)*(foot.y-p.y);foot.y=p.y+Mth.cos(p.xRot)*(foot.y-p.y);foot.xRot=p.xRot;}
        }
        parts.get("tail").yRot=Mth.sin(age*.10F)*.1F;
        if(kind<2){
            float flap=(!e.onGround()&&!swim)?Mth.sin(age*1.2F)*.7F:(age%100<8?Mth.sin(age*.8F)*.18F:0);
            parts.get("left_wing").zRot=-flap;parts.get("right_wing").zRot=flap;
            if(swim){parts.get("body").xRot=-.04F;parts.get("body").y+=Mth.sin(age*.08F)*.15F;}
            if(e.defenceState()>0){parts.get("neck").xRot=-.18F;parts.get("head").y-=1.5F;parts.get("left_wing").zRot=-.25F;parts.get("right_wing").zRot=.25F;}
            if(e.defenceState()==3){parts.get("head").z-=1.5F;}
        }
        parts.get("body").y+=Mth.sin(age*.06F)*.08F;
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer buffer,int light,int overlay,int color){root.render(pose,buffer,light,overlay,color);}
}
'''
    (ROOT/'src/main/java/org/slavicmyths/client/HusbandryModel.java').write_text(java,encoding='utf-8')

def item_assets():
    for name in ('goose_egg','duck_egg','goat_milk_bucket','raw_goose','cooked_goose','raw_duck','cooked_duck','raw_goat','cooked_goat'):
        im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im)
        if name.endswith('_egg'):
            goose=name=='goose_egg';shape=[(15,3 if goose else 6),(19,5 if goose else 8),(25,17),(25,25),(21,29),(10,29),(6,25),(6,19),(10,8)]
            base=(233,218,181) if goose else (178,188,155);edge=(137,119,88) if goose else (111,124,102)
            d.polygon(shape,fill=edge);d.polygon([(15,5 if goose else 8),(19,8),(23,19),(23,25),(19,27),(10,26),(8,23),(9,16),(12,8)],fill=base)
            d.polygon([(14,8),(16,8),(17,19),(12,22),(10,20),(11,14)],fill=(251,239,210) if goose else (207,214,180))
            if not goose:
                for x,y in [(16,13),(20,20),(11,23),(16,25)]:d.rectangle((x,y,x+1,y+1),fill=edge)
        elif name=='goat_milk_bucket':
            d.polygon([(4,9),(27,9),(25,28),(8,28)],fill=(49,49,45));d.polygon([(6,10),(25,10),(23,26),(9,26)],fill=(111,112,104));d.rectangle((8,12,11,23),fill=(157,159,148));d.rectangle((6,8,25,13),fill=(55,54,49));d.rectangle((8,9,23,11),fill=(243,229,195));d.line([(5,12),(5,5),(10,2),(22,2),(27,6),(27,12)],fill=(49,49,45),width=2);d.rectangle((12,22,18,25),fill=(158,116,50))
        else:
            cooked=name.startswith('cooked');species=name.split('_')[1]
            shapes={'goose':[(4,21),(7,14),(14,8),(20,5),(26,8),(28,16),(23,24),(15,29),(7,27)],'duck':[(5,23),(4,19),(11,12),(20,7),(26,10),(27,17),(21,24),(12,28),(7,27)],'goat':[(5,25),(4,16),(10,12),(13,5),(20,4),(27,13),(28,23),(21,27),(11,29)]}
            shape=shapes[species];d.polygon(shape,fill=(64,32,20) if cooked else (91,31,36));inner=[(16+(x-16)*.8,16+(y-16)*.8) for x,y in shape];d.polygon(inner,fill=(143,70,29) if cooked else (205,91,98))
            d.line([(8,22),(13,15),(21,10),(24,13)],fill=(197,119,48) if cooked else (241,160,144),width=3);d.line([(10,25),(17,19),(22,14)],fill=(104,43,24) if cooked else (235,185,161),width=2)
            if cooked:d.rectangle((21,7,24,10),fill=(216,181,125))
        save(A/f'textures/item/{name}.png',im);write(A/f'models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/{name}'}})
    for name in ('goose','duck','domestic_goat'):write(A/f'models/item/{name}_spawn_egg.json',{'parent':'minecraft:item/template_spawn_egg'})

def element(a,b,texture):
    return {'from':a,'to':b,'faces':{face:{'texture':'#'+texture} for face in ('north','south','east','west','up','down')}}

def block_assets():
    for name,base in [('yard_wood',(117,75,39)),('yard_metal',(62,65,61)),('yard_feed',(168,149,75)),('nest_straw',(186,139,52)),('nest_egg',(229,217,184))]:
        im=Image.new('RGBA',(32,32),base+(255,));d=ImageDraw.Draw(im);rng=random.Random(name)
        for _ in range(150):
            x=rng.randrange(32);y=rng.randrange(32);shade=rng.randrange(-30,25);c=tuple(max(0,min(255,v+shade)) for v in base)+(255,)
            if name in ('yard_wood','nest_straw'):d.line((x,y,x+3,y+1),fill=c)
            else:d.point((x,y),fill=c)
        save(A/f'textures/block/{name}.png',im)
    for nest,name,prop in [(False,'feeder','fill_level'),(True,'straw_nest','egg_count')]:
        variants={}
        for count in range(4):
            elements=[]
            if nest:
                # Stepped octagonal ring, with a lower recessed centre.
                elements+=[element([3,0,3],[13,1,13],'straw')]
                for a,b in [([3,0,1],[13,5,3]),([3,0,13],[13,5,15]),([1,0,3],[3,5,13]),([13,0,3],[15,5,13]),([2,0,2],[4,4,4]),([12,0,2],[14,4,4]),([2,0,12],[4,4,14]),([12,0,12],[14,4,14])]:elements.append(element(a,b,'straw'))
                for x,z in [(6,7),(10,8),(8,10)][:count]:
                    elements.append(element([x-1,1,z-1],[x+1,4,z+1],'egg'));elements.append(element([x-.7,4,z-.7],[x+.7,4.8,z+.7],'egg'))
                textures={'straw':'slavicmyths:block/nest_straw','egg':'slavicmyths:block/nest_egg','particle':'slavicmyths:block/nest_straw'}
            else:
                for a,b in [([1,2,3],[15,3,13]),([1,2,3],[15,8,4]),([1,2,12],[15,8,13]),([1,2,4],[2,8,12]),([14,2,4],[15,8,12]),([2,0,4],[4,2,12]),([12,0,4],[14,2,12])]:elements.append(element(a,b,'wood'))
                for x in (3,12):
                    elements+= [element([x,2,2.9],[x+1,8.1,4.1],'metal'),element([x,2,11.9],[x+1,8.1,13.1],'metal')]
                if count:elements.append(element([2,3,4],[14,3+count*1.3,12],'feed'))
                textures={'wood':'slavicmyths:block/yard_wood','metal':'slavicmyths:block/yard_metal','feed':'slavicmyths:block/yard_feed','particle':'slavicmyths:block/yard_wood'}
            model=f'{name}_{count}';write(A/f'models/block/{model}.json',{'parent':'minecraft:block/block','textures':textures,'elements':elements})
            for facing,angle in [('north',0),('east',90),('south',180),('west',270)]:variants[f'{prop}={count},facing={facing}']={'model':f'slavicmyths:block/{model}','y':angle,'uvlock':True}
        write(A/f'blockstates/{name}.json',{'variants':variants})
        write(A/f'models/item/{name}.json',{'parent':f'slavicmyths:block/{name}_0'})
        write(D/f'loot_table/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'slavicmyths:{name}'}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
    write(D/'recipe/feeder.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':['P P','PNP','PPP'],'key':{'P':{'tag':'minecraft:planks'},'N':{'item':'minecraft:iron_nugget'}},'result':{'id':'slavicmyths:feeder','count':1}})
    write(D/'recipe/straw_nest.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':['WWW','W W','WWW'],'key':{'W':{'item':'minecraft:wheat'}},'result':{'id':'slavicmyths:straw_nest','count':1}})

def data_assets():
    feeds={'goose':['oat_grain','barley_grain'],'duck':['oat_grain','barley_grain','pea_pod'],'domestic_goat':['minecraft:wheat','oat_grain','cabbage'],'cow':['rye_grain','barley_grain','oat_grain'],'sheep':['rye_grain','barley_grain','oat_grain'],'pig':['turnip'],'chicken':['rye_seeds','barley_seeds','oat_seeds','flax_seeds'],'rabbit':['cabbage']}
    for kind,foods in feeds.items():write(D/f'tags/item/animal_feed/{kind}.json',{'replace':False,'values':[f if ':' in f else 'slavicmyths:'+f for f in foods]})
    write(D/'tags/item/animal_feed/all_valid.json',{'replace':False,'values':['#slavicmyths:animal_feed/'+k for k in feeds]+['minecraft:wheat','minecraft:wheat_seeds','minecraft:melon_seeds','minecraft:pumpkin_seeds','minecraft:beetroot_seeds','minecraft:carrot','minecraft:potato','minecraft:beetroot','minecraft:dandelion','minecraft:golden_carrot']})
    for kind in ('goose','duck','goat'):
        for method,time in [('smelting',200),('smoking',100)]:write(D/f'recipe/cooked_{kind}_from_{method}.json',{'type':'minecraft:'+method,'category':'food','ingredient':{'item':f'slavicmyths:raw_{kind}'},'result':{'id':f'slavicmyths:cooked_{kind}'},'experience':.35,'cookingtime':time})
    for kind,biomes,weight,maximum in [('goose',['plains','meadow','river'],8,5),('duck',['river','swamp','meadow','plains'],10,4),('domestic_goat',['plains','meadow'],4,3)]:
        write(D/f'neoforge/biome_modifier/{kind}_spawns.json',{'type':'neoforge:add_spawns','biomes':['minecraft:'+b for b in biomes],'spawners':{'type':'slavicmyths:'+kind,'weight':weight,'minCount':2,'maxCount':maximum}})
        meat='goat' if kind=='domestic_goat' else kind
        functions=[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':3 if meat=='goat' else 2}}, {'function':'minecraft:enchanted_count_increase','enchantment':'minecraft:looting','count':{'type':'minecraft:uniform','min':0,'max':1}}, {'function':'minecraft:furnace_smelt','conditions':[{'condition':'minecraft:entity_properties','entity':'this','predicate':{'flags':{'is_on_fire':True}}}]}]
        pools=[{'rolls':1,'entries':[{'type':'minecraft:item','name':f'slavicmyths:raw_{meat}','functions':functions}]}]
        if meat!='goat':pools.append({'rolls':1,'entries':[{'type':'minecraft:item','name':'minecraft:feather','functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':0,'max':2}},{'function':'minecraft:enchanted_count_increase','enchantment':'minecraft:looting','count':{'type':'minecraft:uniform','min':0,'max':1}}]}]})
        write(D/f'loot_table/entities/{kind}.json',{'type':'minecraft:entity','pools':pools})
    # Vanilla advancements are preserved; this is a new mod advancement with AND requirements.
    write(D/'advancement/full_yard.json',{'parent':'minecraft:husbandry/breed_an_animal','display':{'icon':{'id':'slavicmyths:goose_spawn_egg'},'title':{'translate':'advancement.slavicmyths.full_yard.title'},'description':{'translate':'advancement.slavicmyths.full_yard.description'},'frame':'task','show_toast':True,'announce_to_chat':True,'hidden':False},'criteria':{k:{'trigger':'minecraft:bred_animals','conditions':{'child':{'type':'slavicmyths:'+k}}} for k in ('goose','duck','domestic_goat')},'requirements':[[k] for k in ('goose','duck','domestic_goat')]})
    for tag in ('mineable/axe',):
        p=RES/f'data/minecraft/tags/block/{tag}.json';data=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]}
        for item in ('slavicmyths:feeder','slavicmyths:straw_nest'):
            if item not in data['values']:data['values'].append(item)
        write(p,data)

def lang_audio():
    names={'goose':('Гусь','Goose'),'duck':('Утка','Duck'),'domestic_goat':('Домашняя коза','Domestic Goat'),'goose_egg':('Гусиное яйцо','Goose Egg'),'duck_egg':('Утиное яйцо','Duck Egg'),'goat_milk_bucket':('Ведро козьего молока','Goat Milk Bucket'),'raw_goose':('Сырая гусятина','Raw Goose'),'cooked_goose':('Жареная гусятина','Cooked Goose'),'raw_duck':('Сырая утка','Raw Duck'),'cooked_duck':('Жареная утка','Cooked Duck'),'raw_goat':('Сырая козлятина','Raw Goat'),'cooked_goat':('Жареная козлятина','Cooked Goat'),'feeder':('Кормушка','Feeder'),'straw_nest':('Соломенное гнездо','Straw Nest'),'goose_spawn_egg':('Яйцо призыва гуся','Goose Spawn Egg'),'duck_spawn_egg':('Яйцо призыва утки','Duck Spawn Egg'),'domestic_goat_spawn_egg':('Яйцо призыва домашней козы','Domestic Goat Spawn Egg')}
    sounds=json.loads((A/'sounds.json').read_text(encoding='utf-8'))
    audio={'goose':{'ambient':'chicken.ambient','hiss':'cat.hiss','hurt':'chicken.hurt','death':'chicken.death'},'duck':{'ambient':'chicken.ambient','hurt':'chicken.hurt','death':'chicken.death'},'domestic_goat':{'ambient':'sheep.ambient','hurt':'sheep.hurt','death':'sheep.death'}}
    for kind,actions in audio.items():
        for action,fallback in actions.items():
            id=f'entity.{kind}.{action}';sounds[id]={'subtitle':f'subtitles.slavicmyths.{id}','sounds':[{'name':'minecraft:entity.'+fallback,'type':'event','pitch':.75 if kind=='goose' else 1.10 if kind=='duck' else .9}]}
    write(A/'sounds.json',sounds)
    for index,lang in enumerate(('ru_ru','en_us')):
        p=A/f'lang/{lang}.json';data=json.loads(p.read_text(encoding='utf-8'))
        for id,values in names.items():
            prefix='entity' if id in ('goose','duck','domestic_goat') else 'block' if id in ('feeder','straw_nest') else 'item'
            data[f'{prefix}.slavicmyths.{id}']=values[index]
        data['advancement.slavicmyths.full_yard.title']=('Полный двор','Full Yard')[index]
        data['advancement.slavicmyths.full_yard.description']=('Разведи гуся, утку и домашнюю козу','Breed a goose, a duck, and a domestic goat')[index]
        for kind,actions in audio.items():
            for action in actions:
                verbs={'ambient':('подаёт голос','calls'),'hiss':('шипит','hisses'),'hurt':('ранен','hurts'),'death':('погибает','dies')}
                data[f'subtitles.slavicmyths.entity.{kind}.{action}']=names[kind][index]+' '+verbs[action][index]
        write(p,data)

if __name__=='__main__':
    model_assets();item_assets();block_assets();data_assets();lang_audio()
    print('1.1.3 husbandry resources and six UV-mapped animal meshes generated')

# Preserve the latest integration assets and definitive recipes.
from integration_polish_resources import main as polish_118
polish_118()
