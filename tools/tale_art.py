"""Small original pixel icons; each category has a distinct silhouette."""
from pathlib import Path
from PIL import Image,ImageDraw
import json,uuid,base64,csv
ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'src/main/resources/assets/slavicmyths/textures/gui/tales'
OUT.mkdir(parents=True,exist_ok=True)
ink='#38291e';gold='#cfaa59';red='#963e32';green='#526a3d';paper='#e7d3a0'
def icon(key,paint):
 im=Image.new('RGBA',(32,32));d=ImageDraw.Draw(im);paint(d);im.save(OUT/(key+'.png'));return im
icons=[]
icons.append(icon('tales',lambda d:(d.polygon([(4,7),(14,5),(17,8),(28,5),(28,25),(18,28),(14,26),(4,28)],fill=ink),d.polygon([(6,9),(13,7),(15,10),(15,24),(6,25)],fill=paper),d.polygon([(18,10),(26,8),(26,23),(18,25)],fill=gold),d.line([(8,12),(12,11)],fill=red,width=2),d.line([(8,17),(12,16)],fill=ink,width=2))))
icons.append(icon('creatures',lambda d:(d.polygon([(4,4),(12,8),(21,8),(28,4),(27,23),(21,29),(10,29),(5,23)],fill=ink),d.rectangle((9,14,13,18),fill=gold),d.rectangle((20,14,24,18),fill=gold),d.rectangle((11,14,11,18),fill='#17191a'),d.rectangle((22,14,22,18),fill='#17191a'),d.polygon([(14,21),(19,21),(16,24)],fill=red))))
icons.append(icon('people',lambda d:(d.ellipse((11,4,21,15),fill=paper),d.rectangle((10,4,22,8),fill=ink),d.polygon([(9,17),(23,17),(27,29),(5,29)],fill=red),d.rectangle((14,18,18,28),fill=gold))))
icons.append(icon('places',lambda d:(d.polygon([(2,15),(16,3),(30,15)],fill=ink),d.rectangle((5,15,27,29),fill=gold),d.rectangle((13,20,19,29),fill=ink),d.rectangle((8,18,10,21),fill=paper),d.rectangle((22,18,24,21),fill=paper))))
icons.append(icon('crafts',lambda d:(d.rectangle((5,22,28,26),fill=ink),d.rectangle((11,17,24,21),fill=ink),d.polygon([(3,10),(29,10),(23,16),(9,16)],fill='#778081'),d.line([(9,3),(22,17)],fill=gold,width=4),d.polygon([(4,6),(12,1),(17,6),(9,12)],fill=ink))))
icons.append(icon('nature',lambda d:(d.rectangle((14,20,18,29),fill=ink),d.polygon([(16,2),(8,12),(12,12),(5,21),(27,21),(20,12),(24,12)],fill=green),d.rectangle((12,12,14,16),fill=gold))))
icons.append(icon('quests',lambda d:(d.rectangle((8,4,24,27),fill=paper),d.ellipse((4,3,12,10),fill=gold),d.rectangle((6,7,8,24),fill=gold),d.ellipse((20,23,28,30),fill=gold),d.line([(11,12),(13,14),(17,9)],fill=green,width=2),d.rectangle((12,18,21,19),fill=ink))))
icons.append(icon('deeds',lambda d:(d.rectangle((3,22,9,29),fill=ink),d.rectangle((23,22,29,29),fill=ink),d.line([(6,24),(16,13),(26,24)],fill=gold,width=2),d.polygon([(16,2),(20,9),(27,10),(22,15),(23,23),(16,19),(9,23),(10,15),(5,10),(12,9)],fill=gold),d.rectangle((14,10,18,15),fill=red))))
sheet=Image.new('RGBA',(256,32),(235,217,176,255))
for i,im in enumerate(icons):sheet.alpha_composite(im,(i*32,0))
(ROOT/'docs/bayun').mkdir(parents=True,exist_ok=True);sheet.save(ROOT/'docs/bayun/category-icons.png')

# Editable Blockbench source uses the same authored boxes/pivots as BayunMesh.
# Per-face UV sizes use each native cube's unit (64 for dense face children).
rig=json.loads((ROOT/'docs/bayun/rig.json').read_text(encoding='utf8'))
elements=[];groups={};byname={n['name']:n for n in rig}
for n in rig:
 group={'name':n['name'],'origin':[v*16 for v in n['pivot']],'rotation':[0,0,0],'uuid':str(uuid.uuid5(uuid.NAMESPACE_URL,'slavicmyths/bayun/bone/'+n['name'])),'children':[]}
 groups[n['name']]=group
 for i,b in enumerate(n['boxes']):
  x,y,z,w,h,dep,u,v=b;px,py,pz=n['pivot'];unit=n['unit'];U=w*unit;V=h*unit;D=dep*unit
  faces={side:dict(uv=uv,texture=0) for side,uv in {'north':[u+D,v+D,u+D+U,v+D+V], 'south':[u+D+U+D,v+D,u+D+U+D+U,v+D+V], 'west':[u,v+D,u+D,v+D+V], 'east':[u+D+U,v+D,u+D+U+D,v+D+V], 'up':[u+D,v,u+D+U,v+D], 'down':[u+D+U,v,u+D+U+U,v+D]}.items()}
  cube={'name':n['name']+'_'+str(i),'from':[(px+x)*16,(py+y)*16,(pz+z)*16],'to':[(px+x+w)*16,(py+y+h)*16,(pz+z+dep)*16],'origin':[px*16,py*16,pz*16],'faces':faces,'uuid':str(uuid.uuid5(uuid.NAMESPACE_URL,'slavicmyths/bayun/cube/'+n['name']+'/'+str(i)))}
  elements.append(cube);group['children'].append(cube['uuid'])
for n in rig:
 if n['parent']:groups[n['parent']]['children'].append(groups[n['name']])
skin=ROOT/'src/main/resources/assets/slavicmyths/textures/entity/kot_bayun/skin.png'
animations={}
animation_source=ROOT/'docs/bayun/spec/Slavic_Myths_1.6_Final_Bayun_Book_Codex_TZ/09_ASSETS/animation/BAYUN_KEYFRAMES.csv'
for row in csv.DictReader(animation_source.open(encoding='utf-8-sig')):
    clip=row['clip'];bone=row['bone'];bone_id=groups[bone]['uuid']
    animation=animations.setdefault(clip,{'name':clip,'uuid':str(uuid.uuid5(uuid.NAMESPACE_URL,'slavicmyths/bayun/animation/'+clip)),
                                         'loop':'loop' if clip in ('idle_breath','idle_sit','sleep_curl_loop','walk','hypnosis_song') else 'once',
                                         'length':int(row['total_ticks'])/20,'animators':{}})
    animator=animation['animators'].setdefault(bone_id,{'name':bone,'type':'bone','keyframes':[]})
    for channel,fields,signs,scale in [('rotation',('pitch_deg','yaw_deg','roll_deg'),(-1,1,-1),1),
                                     ('position',('translate_x_blocks','translate_y_blocks','translate_z_blocks'),(1,1,1),16)]:
        animator['keyframes'].append({'channel':channel,'time':int(row['key_tick'])/20,'interpolation':'catmullrom',
                                     'data_points':[{axis:float(row[field])*sign*scale for axis,field,sign in zip(('x','y','z'),fields,signs)}]})
bb=dict(meta={'format_version':'4.10','model_format':'free','box_uv':False},name='Kot Bayun native rig',resolution={'width':256,'height':256},elements=elements,outliner=[groups['root']],animations=list(animations.values()),textures=[{'name':'skin.png','width':256,'height':256,'uv_width':256,'uv_height':256,'id':'0','source':'data:image/png;base64,'+base64.b64encode(skin.read_bytes()).decode('ascii')}])
(ROOT/'docs/bayun/kot_bayun.bbmodel').write_text(json.dumps(bb,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
print('Eight distinct 32px category icons; editable native-geometry Blockbench source:',len(elements),'cubes.')
