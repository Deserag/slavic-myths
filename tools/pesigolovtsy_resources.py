"""Separate canine voxel rigs, UV islands and authored palettes; no reference image editing."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,math,csv
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data/slavicmyths';S=R/'docs/pesigolovtsy/source';S.mkdir(parents=True,exist_ok=True)
SPEC=R/'work/pesigolovtsy-spec/Slavic_Myths_1.6.3_Pesigolovtsy_Codex_TZ'
def write(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
all_roles={}
for role,h,pelvis,shoulder,upper,lower,leg,shin,headw in [('scout',30.4,14.8,23,5.5,4.7,7.5,5.9,4.1),('hunter',33.6,15.8,25,6.2,5.0,8.2,6.2,4.8),('leader',36.8,16.8,27,7,5.5,8.8,6.6,5.5)]:
 bones=[];atlas=Image.new('RGBA',(256,256));draw=ImageDraw.Draw(atlas);cursor=[2,2,0];serial=0
 colors={'fur':('#665D58','#3A3432','#978574') if role=='scout' else ('#796358','#4B3D35','#AA9280') if role=='hunter' else ('#514B49','#2B2928','#877971'),'muzzle':('#AA9280','#796358','#C2AF98'),'leather':('#3F322B','#29221E','#675044'),'cloth':('#843B30','#51241F','#A45543'),'bone':('#D4C3A4','#978A72','#E2D7BF'),'iron':('#A1A09A','#5E605E','#D1D1C6'),'wood':('#74523B','#44301F','#A77B4F'),'nose':('#252324','#151416','#4C4440'),'eye':('#D3A04A','#795A30','#F0D79F'),'string':('#CBB99A','#CBB99A','#E0D1BB')}
 def island(w,h,d,material):
  nonlocal_dummy=None
  iw=math.ceil(2*(w+d))+4;ih=math.ceil(h+d)+4
  if cursor[0]+iw>254:cursor[0]=2;cursor[1]+=cursor[2];cursor[2]=0
  assert cursor[1]+ih<254,(role,material,cursor,iw,ih)
  x,y=cursor[:2];cursor[0]+=iw;cursor[2]=max(cursor[2],ih);base,shadow,light=colors[material];draw.rectangle((x,y,x+iw-1,y+ih-1),fill=base)
  for px in range(iw):
   for py in range(ih):
    if material in ['fur','muzzle']:tone=shadow if (px//3+py//2+serial)%7==0 else light if (px+py//3+serial)%9==1 else base
    elif material=='cloth':tone=shadow if px%7<2 else light if px%7==5 else base
    elif material=='leather':tone=light if py%8==0 or px%9==1 else shadow if py%8==7 else base
    elif material=='wood':tone=shadow if px%5==0 else light if px%7==2 else base
    else:tone=shadow if px<2 or py>=ih-2 else light if py==2 else base
    draw.point((x+px,y+py),fill=tone)
  return [x+2,y+2]
 def bone(name,parent,pivot,boxes=(),rot=(0,0,0)):
  global serial
  cubes=[]
  for x,y,z,w,hh,d,mat in boxes:
   serial+=1;cubes.append({'origin':[x,y,z],'size':[w,hh,d],'material':mat,'uv':island(w,hh,d,mat),'shell':(serial%19)*.00063})
  bones.append({'name':name,'parent':parent,'pivot':pivot,'rotation':rot,'cubes':cubes})
 width=5.8 if role=='scout' else 7 if role=='hunter' else 8.8;arm_x=width/2+1.4;limb=1.9 if role=='scout' else 2.5 if role=='hunter' else 3.1
 bone('motionRoot','root',(0,0,0));bone('pelvis','motionRoot',(0,-pelvis,0),[(-width*.43,-1,-1.8,width*.86,3,3.6,'leather')]);bone('spine_lower','pelvis',(0,0,0),[(-width*.38,-4,-1.7,width*.76,4,3.4,'fur')]);bone('spine_upper','spine_lower',(0,-(shoulder-pelvis),0));bone('chest','spine_upper',(0,0,0),[(-width/2,-1.5,-2,width,5.7,4,'fur')]);bone('neck','chest',(0,-1,0),[(-1.3,-1.3,-1.2,2.6,2.4,2.4,'fur')])
 ear=3.4 if role=='scout' else 3.5 if role=='hunter' else 3.8;skullh=4.3 if role=='scout' else 4.8 if role=='hunter' else 5.2;head_y=h-ear-skullh/2
 bone('head','neck',(0,-(head_y-shoulder-1),-.4),[(-headw/2,-skullh/2,-2,headw,skullh,4,'fur'),(-headw/2-.3,.1,-1.7,.8,2.1,2.8,'muzzle'),(headw/2-.5,.1,-1.7,.8,2.1,2.8,'muzzle')]);snout=5 if role=='scout' else 6 if role=='hunter' else 6.5
 bone('muzzle_upper','head',(0,0,-2),[(-headw*.35,-.4,-snout,headw*.7,1.8,snout,'muzzle'),(-headw*.3,-.25,-snout-.5,headw*.6,1.15,.8,'nose')]);bone('jaw_lower','head',(0,1.1,-1.8),[(-headw*.32,0,-snout-.3,headw*.64,1.2,snout+.3,'muzzle'),(-headw*.25,-.15,-snout+.1,headw*.5,.3,snout-.2,'nose')])
 for side,sign in [('L',1),('R',-1)]:
  bone('eye_'+side,'head',(sign*(headw/2-.3),-.70,-2.62),[(-.43,-.26,-.16,.86,.52,.36,'eye')],(0,-sign*.12,0));bone('pupil_'+side,'eye_'+side,(0,0,-.19),[(-.11,-.22,-.03,.22,.44,.07,'nose')]);bone('brow_'+side,'head',(sign*(headw/2-.35),-1.40,-2),[(-.65,-.25,-.5,1.3,.5,1,'fur')],(0,0,sign*.12))
  for j in range(3):bone(f'cheek_fur_{side}_{j}','head',(sign*(headw/2+.05),.2+j*.6,.3-j*.5),[(-.5,-.3,-.6,1,1.4,1.2,'fur')],(.15,0,-sign*.5))
  bone('ear_'+side,'head',(sign*(headw/2-.5),-skullh/2,0),[(-.85,-ear,-.6,1.7,ear,1.2,'fur'),(-.52,-ear+.5,-.67,1.04,ear-1,.18,'cloth'),(-.45,-ear-.05,-.4,.9,.7,.8,'fur')],(0,sign*.08,sign*.14))
  for j in range(4):
   bone(f'tooth_upper_{side}_{j}','muzzle_upper',(sign*headw*.3,1.15,-1.8-j*(snout-2)/3),[(-.28,0,-.32,.56,.8,.64,'bone')]);bone(f'tooth_lower_{side}_{j}','jaw_lower',(sign*headw*.27,0,-1.8-j*(snout-2)/3),[(-.25,-.7,-.28,.5,.7,.56,'bone')])
  bone('arm_'+side+'_upper','chest',(sign*arm_x,0,0),[(-limb/2,0,-limb/2,limb,upper,limb,'fur')]);bone('arm_'+side+'_lower','arm_'+side+'_upper',(0,upper,0),[(-limb*.43,0,-limb*.43,limb*.86,lower,limb*.86,'fur'),(-limb*.5,lower*.28,-limb*.5,limb,lower*.42,limb,'leather')]);bone('hand_'+side,'arm_'+side+'_lower',(0,lower,0),[(-1.2,-.7,-.7,2.4,1.4,1.4,'muzzle')]);bone('palm_'+side+'_socket','hand_'+side,(0,0,0))
  for j in range(4):bone(f'finger_{side}_{j}','hand_'+side,((j-1.5)*.57,.6,-.1),[(-.22,0,-.32,.44,1.5,.64,'fur'),(-.17,1.15,-.55,.34,.9,.38,'bone')],(-.42,0,(j-1.5)*.05))
  for j in range(3 if role=='scout' else 5):bone(f'forearm_fur_{side}_{j}','arm_'+side+'_lower',(sign*(limb*.45),j*.8,-.5),[(-.55,0,-.5,1.1,1.7,1,'fur')],(.15,0,-sign*.45))
  bone('leg_'+side+'_upper','pelvis',(sign*(width*.26),0,0),[(-limb*.55,0,-limb*.55,limb*1.1,leg,limb*1.1,'leather')]);bone('leg_'+side+'_lower','leg_'+side+'_upper',(0,leg,0),[(-limb*.42,0,-limb*.42,limb*.84,shin,limb*.84,'fur'),(-limb*.52,0,-limb*.64,limb*1.04,1.3,limb*1.28,'bone')]);bone('foot_'+side,'leg_'+side+'_lower',(0,shin,0),[(-limb*.55,0,-3.1,limb*1.1,1.4,4.2,'leather')])
  for j in range(3):bone(f'toe_{side}_{j}','foot_'+side,((j-1)*limb*.36,.8,-3.0),[(-.3,0,-1,.6,.55,1.5,'bone')])
  for j in range(3):bone(f'shin_fur_{side}_{j}','leg_'+side+'_lower',(sign*(limb*.4),2+j,-.3),[(-.5,0,-.6,1,1.8,1.2,'fur')],(.12,0,-sign*.4))
 bone('mantle_root','chest',(0,-1,1))
 for side,sign in [('L',1),('R',-1)]:bone('mantle_flap_'+side,'mantle_root',(sign*(width/2+.5),1,0),[(-1.1,0,-1.4,2.2,3.0 if role=='scout' else 5.3,2.8,'fur')],(0,0,sign*.17))
 rows=1 if role=='scout' else 3 if role=='hunter' else 4;count=8 if role=='scout' else 12 if role=='hunter' else 14
 for row in range(rows):
  for j in range(count):
   angle=j*2*math.pi/count;radius=width/2+(.4 if role=='scout' else .9)+row*.35;xx=math.cos(angle)*radius;zz=math.sin(angle)*2.4+1;size=1.2 if role=='scout' else 1.8 if role=='hunter' else 2.0;length=2.0+(j%3)*.6+row*.5
   bone(f'fur_{row}_{j}','mantle_root',(xx,row*1.25,zz),[(-size/2,0,-.7,size,length,1.4,'fur'),(-size*.33,length-.3,-.5,size*.66,.9,1,'fur')],(.35*math.sin(angle),.12*math.sin(j),-.42*math.cos(angle)))
 bone('belt','pelvis',(0,0,0),[(-width*.48,-1.2,-2.2,width*.96,1.6,4.4,'leather')]);bone('belt_totem','belt',(0,-.2,-2.5),[(-1,-.5,-.3,2,1.6,.6,'bone'),(-.7,-.15,-.64,.4,.4,.16,'nose'),(.3,-.15,-.64,.4,.4,.16,'nose')])
 for j in range(1 if role=='scout' else 2 if role=='hunter' else 3):bone('pouch_'+str(j),'belt',((-1 if j%2==0 else 1)*(width*.45),1,1.6-j*.8),[(-.7,0,-.7,1.4,2.2,1.4,'leather')])
 for side,sign in [('front',-1),('back',1)]:
  bone('loincloth_'+side,'belt',(0,.8,sign*2.1));n=3 if role=='scout' else 4 if role=='hunter' else 5
  for j in range(n):bone(f'cloth_{side}_{j}','loincloth_'+side,((j-(n-1)/2)*1.2,0,0),[(-.52,0,-.2,1.04,4+(j%3)*.75,.4,'cloth')],(0,0,(j-(n-1)/2)*.025))
 bone('necklace','chest',(0,1,-2.4));teeth=3 if role=='scout' else 7 if role=='hunter' else 11
 for j in range(teeth):bone('necklace_tooth_'+str(j),'necklace',((j-(teeth-1)/2)*.65,1-abs(j-(teeth-1)/2)*.15,0),[(-.23,0,-.3,.46,1.1+(j%3)*.3,.6,'bone')],(.05,0,(j-(teeth-1)/2)*.07))
 bone('weapon_root','chest',(0,0,0));bone('weapon_hand_socket','weapon_root',(0,0,0));bone('weapon_secondary_socket','weapon_root',(0,0,6))
 if role=='hunter':
  bone('spear_haft','weapon_root',(0,0,0),[(-.42,-.42,-20,.84,.84,32,'wood'),(-.7,-.7,11.5,1.4,1.4,2.0,'wood'),(-.64,-.64,-20.3,1.28,1.28,1.8,'iron')])
  for j in range(4):bone('spear_wrap_'+str(j),'weapon_root',(0,0,-18+j*.45),[(-.54,-.54,0,1.08,1.08,.3,'leather')])
  bone('spear_head','weapon_root',(0,0,-20),[(-.8,-.25,-1.7,1.6,.5,1.7,'iron'),(-.55,-.23,-3.1,1.1,.46,1.6,'iron'),(-.25,-.2,-4.2,.5,.4,1.3,'iron')]);bone('weapon_tip','weapon_root',(0,0,-24.2))
 elif role=='leader':
  bone('axe_haft','weapon_root',(0,0,0),[(-.65,-.65,-18,1.3,1.3,22.4,'wood'),(-.95,-.95,-16,1.9,1.9,2.7,'iron')])
  for j in range(6):bone('axe_wrap_'+str(j),'weapon_root',(0,0,-7+j*1.3),[(-.76,-.76,0,1.52,1.52,.65,'leather')])
  bone('axe_head','weapon_root',(0,0,-17),[(-2.2,-.85,-2.0,5.1,1.7,3.3,'iron'),(2.8,-.65,-2.6,2.2,1.3,4.7,'iron'),(4.8,-.45,-2.5,.8,.9,4.5,'iron'),(-3,-.65,-1.6,1,1.3,2.5,'iron')]);bone('weapon_tip','weapon_root',(5.6,0,-17))
 else:
  bone('bow_grip','weapon_root',(0,0,0),[(-.55,-1.4,-.55,1.1,2.8,1.1,'leather')]);bone('bow_top','weapon_root',(0,-1.2,0));bone('bow_bottom','weapon_root',(0,1.2,0))
  for side,sign in [('top',-1),('bottom',1)]:
   for j in range(3):bone(f'bow_limb_{side}_{j}','bow_'+side,(0,sign*j*2.5,-j*.7),[(-.43,-1.3,-.4,.86,2.6,.8,'wood')],(-sign*.22,0,0))
  bone('bow_string_top','weapon_root',(0,-7.6,-1.8),[(-.045,0,-.045,.09,7.6,.09,'string')]);bone('bow_string_bottom','weapon_root',(0,0,0),[(-.045,0,-.045,.09,7.6,.09,'string')]);bone('nocked_arrow','weapon_root',(0,0,0),[(-.13,-.13,-12,.26,.26,12.4,'wood'),(-.34,-.18,-13.5,.68,.36,1.5,'iron'),(-.6,-.07,-1,1.2,.14,1.5,'bone')]);bone('weapon_tip','weapon_root',(0,0,-13.5));bone('quiver','chest',(-2,2,3.1),[(-1,-1,-1,2,7,2,'leather')],(.08,0,.18))
  for j in range(3):bone('quiver_arrow_'+str(j),'quiver',((j-1)*.55,-2,0),[(-.1,-2,-.1,.2,7,.2,'wood'),(-.3,-2,-.07,.6,1.2,.14,'bone')])
  for name,parent in [('knife_root','hand_R'),('knife_sheath','belt')]:
   bone(name,parent,(0,0,0) if name=='knife_root' else (-width*.5,1,-1),[(-.45,-.4,0,.9,.8,2.3,'leather'),(-1,-.25,-.3,2,.5,.6,'iron'),(-.7,-.2,-3,1.4,.4,3,'iron'),(-.4,-.18,-5.5,.8,.36,2.7,'iron')] if name=='knife_root' else [(-.8,-.6,-5.8,1.6,1.2,5.8,'leather'),(-.45,-.4,0,.9,.8,2.3,'leather')])
  bone('knife_tip','knife_root',(0,0,-5.5))
 bone('tail_1','pelvis',(0,1,2),[(-.75,0,0,1.5,2.8,1.5,'fur')],(.55,0,0));bone('tail_2','tail_1',(0,2.5,0),[(-.6,0,-.1,1.2,2.1,1.4,'fur')],(-.3,0,0))
 all_roles[role]={'bones':bones,'arm_lengths':[upper,lower],'height_mu':h,'shoulder_mu':shoulder,'texture_size':256,'used_atlas_height':cursor[1]+cursor[2]}
 p=A/f'textures/entity/pesigolovtsy/{role}.png';p.parent.mkdir(parents=True,exist_ok=True);atlas.save(p)
 write(S/f'{role}-rig.json',all_roles[role]);print(role,len(bones),'bones',sum(len(b['cubes']) for b in bones),'cuboids','atlas height',cursor[1]+cursor[2])
lines=['package org.slavicmyths.client;','import net.minecraft.client.model.geom.*;import net.minecraft.client.model.geom.builders.*;import org.slavicmyths.pesigolovtsy.DogheadRules.Role;','public final class DogheadMesh {public static ModelPart create(Role role){return switch(role){case SCOUT->scout();case HUNTER->hunter();case LEADER->leader();};}']
f=lambda v:f'{v:.5f}F'
for role,data in all_roles.items():
 lines.append(f'private static ModelPart {role}(){{var mesh=new MeshDefinition();var root=mesh.getRoot();')
 for b in data['bones']:
  expr='CubeListBuilder.create()'
  for c in b['cubes']:expr+=f'.texOffs({c["uv"][0]},{c["uv"][1]}).addBox('+','.join(map(f,c['origin']+c['size']))+',new CubeDeformation('+f(c['shell'])+'))'
  lines.append(f'var {b["name"]}={b["parent"]}.addOrReplaceChild("{b["name"]}",{expr},PartPose.offsetAndRotation('+','.join(map(f,list(b['pivot'])+list(b['rotation'])))+'));')
 lines.append('var baked=LayerDefinition.create(mesh,256,256).bakeRoot();baked.setInitialPose(PartPose.offset(0,24,0));baked.resetPose();return baked;}')
lines+=['public static java.util.Map<String,ModelPart> index(ModelPart root){var map=new java.util.LinkedHashMap<String,ModelPart>();scan(root,"root",map);return map;}private static void scan(ModelPart p,String name,java.util.Map<String,ModelPart> map){map.put(name,p);p.getAllParts();']
# No reflection at runtime: common hierarchy plus role extras has explicit parent paths.
lines=lines[:-1]+['public static java.util.Map<String,ModelPart> index(ModelPart root,Role role){var map=new java.util.LinkedHashMap<String,ModelPart>();map.put("root",root);switch(role){']
for role,data in all_roles.items():
 lines.append('case '+role.upper()+'->{')
 for b in data['bones']:lines.append(f'map.put("{b["name"]}",map.get("{b["parent"]}").getChild("{b["name"]}"));')
 lines.append('}')
lines+=['}return map;}private DogheadMesh(){}}'];(R/'src/main/java/org/slavicmyths/client/DogheadMesh.java').write_text('\n'.join(lines)+'\n',encoding='utf8')
mapping={'spear_thrust':'THRUST','spear_sweep':'SWEEP','knife_slash':'KNIFE','bow_draw_release':'BOW','pack_howl':'HOWL','heavy_axe_chop':'CHOP','side_cleave':'CLEAVE'};rows=list(csv.DictReader((SPEC/'manifests/attack_pose_samples.csv').read_text(encoding='utf-8-sig').splitlines()));groups={}
for row in rows:groups.setdefault(mapping[row['clip']],[]).append([float(row[k]) for k in ['tick','torso_pitch_deg','torso_yaw_deg','main_upperarm_raise_deg','main_elbow_deg','support_elbow_deg','head_pitch_deg','jaw_open_deg']])
java=['package org.slavicmyths.pesigolovtsy;import org.slavicmyths.pesigolovtsy.DogheadRules.Clip;public final class DogheadKeyframes {']
for key,values in groups.items():java.append('private static final float[][] '+key+'={'+','.join('{'+','.join(map(f,v))+'}' for v in values)+'};')
java+=['private static final float[][] EMPTY=new float[0][];public static float[][] samples(Clip c){return switch(c){']
for key in groups:java.append('case '+key+'->'+key+';')
java+=['default->EMPTY;};}private DogheadKeyframes(){}}'];(R/'src/main/java/org/slavicmyths/pesigolovtsy/DogheadKeyframes.java').write_text('\n'.join(java)+'\n');write(S/'attack-keyframes.json',groups)
write(D/'tags/worldgen/biome/pesigolovtsy_wilderness.json',{'replace':False,'values':['minecraft:'+n for n in ['forest','dark_forest','birch_forest','old_growth_birch_forest','taiga','old_growth_pine_taiga','old_growth_spruce_taiga','swamp','plains']]});write(D/'neoforge/biome_modifier/pesigolovtsy.json',{'type':'slavicmyths:doghead_spawns'})
write(D/'loot_table/entities/pesigolovets.json',{'type':'minecraft:entity','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'minecraft:bone','functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':0,'max':2}}]}]},{'rolls':1,'conditions':[{'condition':'minecraft:random_chance','chance':.35}],'entries':[{'type':'minecraft:item','name':'minecraft:leather'}]},{'rolls':1,'conditions':[{'condition':'minecraft:killed_by_player'},{'condition':'minecraft:random_chance','chance':.1}],'entries':[{'type':'minecraft:item','name':'minecraft:iron_nugget'}]}]})
for role in all_roles:write(A/f'models/item/pesigolovets_{role}_spawn_egg.json',{'parent':'minecraft:item/template_spawn_egg'})
for language in ['ru_ru','en_us']:
 p=A/f'lang/{language}.json';v=json.loads(p.read_text(encoding='utf8'));v['entity.slavicmyths.pesigolovets']='Песиголовец' if language=='ru_ru' else 'Dog-headed Folk'
 for role,ru,en in [('hunter','охотник','Hunter'),('scout','разведчик','Scout'),('leader','вожак','Leader')]:
  name='Песиголовец-'+ru if language=='ru_ru' else 'Dog-headed '+en;v['entity.slavicmyths.pesigolovets_'+role]=name;v['item.slavicmyths.pesigolovets_'+role+'_spawn_egg']='Яйцо призыва: '+name if language=='ru_ru' else name+' Spawn Egg'
 write(p,v)
