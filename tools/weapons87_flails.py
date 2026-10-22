"""#192 -> #166/#167/#116 source-owned blocky flails. Default writes drafts only."""
from pathlib import Path
import argparse,copy,hashlib,json,math
from PIL import Image,ImageDraw
import numpy as np
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/weapons87';D=O/'blueprints/flails';A=R/'src/main/resources/assets/slavicmyths'
PALETTES={
 'wood':['#3e2b1e','#62422c','#8a623d','#ac8051'],
 'leather':['#30221b','#4c3228','#6e4b36','#927052'],
 'iron':['#3e484b','#65777b','#97a5a3','#cbd1c8'],
 'stone':['#41413b','#64675a','#8c8d7b','#adb09b'],
 'gold':['#6d411b','#a46d24','#d3a13b','#f0cf69'],
 'silver':['#586267','#829b9c','#b5ccca','#e0e5d9'],
 'diamond':['#14504c','#247c77','#48b5a4','#9be0c8'],
 'perunite':['#292e49','#43577a','#668da1','#9ec2c1'],
 'netherite':['#241e24','#3f343b','#635457','#968079'],
}
IDS={m:('flail' if m=='iron' else m+'_flail') for m in ['stone','gold','iron','silver','diamond','perunite','netherite']};IDS['ataman']='ataman_flail'
TILE={m:((i%4)*4,(i//4)*4) for i,m in enumerate(PALETTES)}
def box(lo,hi,material):
 x,y=TILE[material];return {'from':lo,'to':hi,'faces':{f:{'texture':'#surface','uv':[x,y,x+4,y+4]} for f in ['north','south','east','west','up','down']}}
def shifted(parts,x=0,y=0,z=0):
 parts=copy.deepcopy(parts)
 for p in parts:
  p['from']=[v+d for v,d in zip(p['from'],[x,y,z])];p['to']=[v+d for v,d in zip(p['to'],[x,y,z])]
 return parts
def ball(material):
 m='netherite' if material=='ataman' else material
 b=lambda lo,hi:box(lo,hi,m)
 if material=='stone':return [b([-2,-1.7,-1.8],[2,1.7,1.8]),b([-1.4,-2.1,-1.3],[1.4,2.1,1.3])]
 if material=='gold':return [b([-2.1,-1.2,-1.5],[2.1,1.2,1.5]),b([-1.4,-1.9,-1.2],[1.4,1.9,1.2]),b([-1,-2.2,-.8],[1,2.2,.8])]
 if material=='iron':return [b([-1.9,-1.9,-1.9],[1.9,1.9,1.9]),b([-2.25,-.55,-1.5],[2.25,.55,1.5]),b([-1.5,-.55,-2.25],[1.5,.55,2.25])]
 if material=='silver':return [b([-1.5,-1.9,-1.5],[1.5,1.9,1.5]),b([-2.5,-1.2,-.55],[2.5,1.2,.55]),b([-.55,-1.2,-2.5],[.55,1.2,2.5]),b([-1,-2.3,-1],[1,2.3,1])]
 if material=='diamond':return [b([-1.3,-2.3,-1.3],[1.3,2.3,1.3]),b([-1.9,-1.6,-1.9],[1.9,1.6,1.9]),b([-2.3,-.65,-1.3],[2.3,.65,1.3]),b([-1.3,-.65,-2.3],[1.3,.65,2.3])]
 if material=='perunite':return [b([-1.8,-1.8,-1.8],[1.8,1.8,1.8]),b([-2.8,-.5,-.6],[2.8,.5,.6]),b([-.6,-.5,-2.8],[.6,.5,2.8]),b([-.5,-2.5,-.5],[.5,2.5,.5])]
 if material=='netherite':return [b([-2.4,-2,-2.4],[2.4,2,2.4]),b([-1.8,-2.6,-1.8],[1.8,2.6,1.8]),box([-2.6,-.7,-2.6],[2.6,.7,2.6],'iron')]
 return [b([-2.5,-2.3,-2.5],[2.5,2.3,2.5]),b([-3.2,-.8,-1.2],[3.2,.8,1.2]),b([-1.2,-.8,-3.2],[1.2,.8,3.2]),box([-1.9,-2.8,-1.9],[1.9,-2.3,1.9],'gold'),box([-1.9,2.3,-1.9],[1.9,2.8,1.9],'gold')]
def link(center,alternating=False):
 x,y,z=center;w=.48;rx=1.2;ry=1.5
 parts=[box([-rx,-ry,-w/2],[rx,-ry+w,w/2],'iron'),box([-rx,ry-w,-w/2],[rx,ry,w/2],'iron'),box([-rx,-ry+w,-w/2],[-rx+w,ry-w,w/2],'iron'),box([rx-w,-ry+w,-w/2],[rx,ry-w,w/2],'iron')]
 if alternating:
  for p in parts:p['from']=[p['from'][2],p['from'][1],p['from'][0]];p['to']=[p['to'][2],p['to'][1],p['to'][0]]
 return shifted(parts,x,y,z)
def handle(material):
 cap='gold' if material in ['gold','ataman'] else 'iron';parts=[box([7.25,3.5,7.25],[8.75,16,8.75],'wood'),box([7,3,7],[9,4,9],cap),box([6.9,15.5,6.9],[9.1,16.5,9.1],cap)]
 for y in [5,7,9,11]:parts.append(box([7.1,y,7.1],[8.9,y+.7,8.9],'leather'))
 return parts+link([8,17,8])
def assembled(material):
 parts=handle(material)
 for i,c in enumerate([(9.2,15.4,8),(10.5,13.2,8),(11.6,10.9,8),(12,8.5,8),(12,6.1,8),(12,3.7,8)]):parts+=link(c,i%2==0)
 parts+=link([12,1.5,8]);parts+=shifted(ball(material),12,-2,8);return parts
DISPLAY={'gui':{'rotation':[0,0,-25],'translation':[-.7,1.3,0],'scale':[.62,.62,.62]},'ground':{'translation':[0,3,0],'scale':[.35,.35,.35]},'fixed':{'rotation':[0,0,-25],'scale':[.5,.5,.5]},'firstperson_righthand':{'rotation':[0,-70,8],'translation':[1,-1,-2],'scale':[.65,.65,.65]},'firstperson_lefthand':{'rotation':[0,-110,8],'translation':[-1,-1,-2],'scale':[.65,.65,.65]},'thirdperson_righthand':{'rotation':[90,0,0],'translation':[0,0,0],'scale':[.65,.65,.65]},'thirdperson_lefthand':{'rotation':[90,180,0],'translation':[0,0,0],'scale':[.65,.65,.65]}}
def model(parts,display=None):return {'textures':{'surface':'slavicmyths:item/weapons87_materials','particle':'slavicmyths:item/weapons87_materials'},'elements':parts,'display':display or DISPLAY}
def sprite(material,component=False):
 im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im);p=PALETTES['netherite' if material=='ataman' else material];dark,mid,light,hi=p
 if not component:
  d.line([(3,13),(4,4)],fill=PALETTES['wood'][0],width=3);d.line([(3,12),(4,5)],fill=PALETTES['wood'][2],width=1)
  for y in [7,10,13]:d.point((3,y),fill=PALETTES['leather'][3])
  for x,y in [(5,3),(6,4),(7,5),(8,6),(9,7),(10,8),(10,9)]:d.rectangle((x,y,x+1,y+1),fill=PALETTES['iron'][0]);d.point((x,y),fill=PALETTES['iron'][2])
  cx,cy=11,12
 else:cx,cy=8,9
 # Pixel profiles correspond to the constructed cores/flanges, never a blurred render.
 points={'stone':[(-2,-2),(1,-2),(2,-1),(2,1),(1,2),(-2,1)],'gold':[(-1,-3),(1,-2),(2,0),(1,2),(-1,2),(-2,0)],'iron':[(-2,-2),(2,-2),(2,2),(-2,2)],'silver':[(-1,-3),(1,-3),(1,-1),(3,-1),(3,1),(1,1),(1,3),(-1,3),(-1,1),(-3,1),(-3,-1),(-1,-1)],'diamond':[(0,-3),(2,-1),(2,1),(0,3),(-2,1),(-2,-1)],'perunite':[(0,-3),(1,-1),(3,0),(1,1),(0,3),(-1,1),(-3,0),(-1,-1)],'netherite':[(-2,-3),(2,-3),(3,-1),(3,2),(1,3),(-2,2),(-3,0)],'ataman':[(-2,-3),(2,-3),(2,-1),(3,-1),(3,1),(2,1),(2,3),(-2,3),(-2,1),(-3,1),(-3,-1),(-2,-1)]}[material]
 d.polygon([(cx+x,cy+y) for x,y in points],fill=dark);d.rectangle((cx-1,cy-1,cx+1,cy+1),fill=mid);d.line([(cx-1,cy-1),(cx+1,cy-1)],fill=light);d.point((cx-1,cy-2),fill=hi)
 if material in ['iron','netherite','ataman']:d.line([(cx-2,cy),(cx+2,cy)],fill=PALETTES['gold' if material=='ataman' else 'iron'][2])
 if component:d.rectangle((cx-1,cy-5,cx+1,cy-3),outline=PALETTES['iron'][2]);d.point((cx,cy-4),fill=(0,0,0,0))
 return im
def write(path,value):path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def separate(id):return {'loader':'neoforge:separate_transforms','base':{'parent':'slavicmyths:item/'+id+'_held'},'perspectives':{'gui':{'parent':'slavicmyths:item/'+id+'_icon'}}}
def main(apply=None):
 parser=argparse.ArgumentParser();parser.add_argument('--apply',action='store_true');args=parser.parse_args() if apply is None else argparse.Namespace(apply=apply);D.mkdir(parents=True,exist_ok=True);dest=A if args.apply else D/'draft-assets';owned=[]
 if not args.apply and (D/'draft-manifest.json').exists():raise RuntimeError('Existing before-production evidence is preserved. Use a separate revision folder for new designs; --apply reproduces production.')
 atlas=Image.new('RGBA',(16,16));pattern=[[1,1,2,1],[1,2,2,1],[0,1,1,2],[1,1,3,1]]
 for m,(x,y) in TILE.items():
  for py in range(4):
   for px in range(4):atlas.putpixel((x+px,y+py),tuple(bytes.fromhex(PALETTES[m][pattern[py][px]][1:]))+(255,))
 def png(name,im):
  p=dest/'textures/item'/(name+'.png');p.parent.mkdir(parents=True,exist_ok=True);im.save(p);owned.append(p)
 def js(name,data):p=dest/'models/item'/(name+'.json');write(p,data);owned.append(p)
 png('weapons87_materials',atlas)
 if not args.apply:
  import weapons87_geometry as geometry
  original=geometry.texture;geometry.texture=lambda ref:np.array(atlas) if ref=='slavicmyths:item/weapons87_materials' else original(ref)
 plans=[]
 for material,id in IDS.items():
  held=model(assembled(material));js(id+'_held',held);js(id+'_icon',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+id}});js(id,separate(id));png(id,sprite(material))
  if material!='ataman':
   part=material+'_flail_ball';js(part+'_held',model(shifted(ball(material),8,8,8)+link([8,12,8]),{'gui':{'rotation':[15,30,0],'scale':[1,1,1]}}));js(part+'_icon',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+part}});js(part,separate(part));png(part,sprite(material,True))
  # Each sheet is a technical proposal generated before production application.
  if not args.apply:
   board=Image.new('RGB',(1200,680),'#253039');draw=ImageDraw.Draw(board);draw.text((20,15),f'#192 / {id} / TECHNICAL PROPOSAL - actual draft JSON + UV, NOT Minecraft',fill='white');draw.text((20,40),'Axes: X right / Y up / Z depth; units = 1/16 block. Grip (8,8,8). No auto-fit.',fill='white')
   old=geometry.faces(geometry.model('slavicmyths:item/'+id,'thirdperson_righthand'));new=geometry.faces(held)
   for i,(label,f,a) in enumerate([('ACTUAL BEFORE',old,(15,35,0)),('PROPOSED FRONT',new,(0,0,0)),('SIDE / thickness',new,(0,90,0)),('3/4',new,(15,35,0))]):
    im=geometry.project(f,260,36,a);x=20+i*295;board.paste(im,(x,105),im);draw.text((x,82),label,fill='white')
   gui=sprite(material);board.paste(gui,(30,415),gui);big=gui.resize((96,96),Image.Resampling.NEAREST);board.paste(big,(70,390),big);draw.text((20,505),'GUI 16px: 1x +6x NEAREST; own pixel profile',fill='white')
   dims=np.concatenate([v for _,v,_ in new]);size=dims.max(0)-dims.min(0)
   lines=[f'Bounding dimensions X/Y/Z: {size[0]:.2f} / {size[1]:.2f} / {size[2]:.2f} units','Handle width1.5 / length12.5; leather wraps; forged eyelet, opening1.44 x2.04','Seven articulated links, wire0.48; alternating XY/YZ; closed rectangular eyelets','Ball center (12,-2,8): 10 units BELOW grip; chain connects at handle top','Held scale0.65: palm width about4 units, effective handle width0.975 units','Player height32 units; assembled effective height about15 units; head below hand','Animation pivots: handle eyelet(8,17,8); ball attachment(12,1.5,8); preserve server timing']
   for j,line in enumerate(lines):draw.text((380,395+j*28),line,fill='#d3dcca')
   board.save(D/(id+'.png'));plans.append({'id':id,'issue':181 if material=='ataman' else 116,'ball_issue':166,'chain_issue':167,'grip':[8,8,8],'eyelet':[8,17,8],'ball_center':[12,-2,8],'dimensions':size.tolist(),'draft_sha256':hashlib.sha256(json.dumps(held,sort_keys=True).encode()).hexdigest(),'decision':'FIX: volumetric closed links and head below grip; existing flat/material silhouettes insufficient','state':'TECHNICAL_DESIGN, not owner runtime accepted'})
 for id,parts in [('flail_handle',handle('iron')),('flail_chain',sum([link([8,5+i*2.4,8],i%2==1) for i in range(4)],[]))]:
  js(id+'_held',model(parts));js(id+'_icon',{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+id}});js(id,separate(id));im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
  if id=='flail_handle':
   d.rectangle((6,4,8,14),fill=PALETTES['wood'][0]);d.line([(7,5),(7,13)],fill=PALETTES['wood'][2]);d.rectangle((5,1,9,4),outline=PALETTES['iron'][2]);d.rectangle((5,13,9,14),fill=PALETTES['iron'][1])
  else:
   for i,(x,y) in enumerate([(3,2),(6,6),(9,10)]):d.rectangle((x,y,x+3,y+4),outline=PALETTES['iron'][0]);d.line([(x+1,y),(x+3,y),(x+3,y+3)],fill=PALETTES['iron'][2])
  png(id,im)
 if not args.apply:
  part_ids=[m+'_flail_ball' for m in IDS if m!='ataman']+['flail_handle','flail_chain']
  for id in part_ids:
   draft=json.loads((dest/'models/item'/(id+'_held.json')).read_text());new=geometry.faces(draft);old=geometry.faces(geometry.model('slavicmyths:item/'+id));board=Image.new('RGB',(1120,500),'#253039');draw=ImageDraw.Draw(board);draw.text((15,12),'#192 / '+id+' / component geometry, palette and attachment proposal',fill='white')
   for i,(label,f,a) in enumerate([('ACTUAL BEFORE',old,(0,0,0)),('DRAFT FRONT',new,(0,0,0)),('SIDE / thickness',new,(0,90,0)),('3/4',new,(15,35,0))]):
    im=geometry.project(f,240,28,a);x=15+i*275;board.paste(im,(x,65),im);draw.text((x,43),label,fill='white')
   image=Image.open(dest/'textures/item'/(id+'.png')).convert('RGBA');board.paste(image,(25,345),image);large=image.resize((96,96),Image.Resampling.NEAREST);board.paste(large,(60,325),large)
   vertices=np.concatenate([v for _,v,_ in new]);dims=vertices.max(0)-vertices.min(0)
   for j,line in enumerate([f'X/Y/Z extent: {dims[0]:.2f} / {dims[1]:.2f} / {dims[2]:.2f} model units','1 model unit=1/16 block; player height32, palm width about4 units','Assembly uses this exact material profile; square eyelet, wire0.48','Inventory is separate 16px sprite; held geometry has nonzero depth','Existing IDs, recipes, NBT and material/tier values preserved']):draw.text((250,330+j*27),line,fill='white')
   board.save(D/(id+'.png'))
 if args.apply:
  from weapons87_grips import apply as grips87
  grips87()
 write(D/('applied-manifest.json' if args.apply else 'draft-manifest.json'),{p.relative_to(dest).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in owned})
 if args.apply:
  from weapons87_flail_motion import apply as motion87
  motion87()
  from weapons87_spears import apply as spears87
  spears87()
  from weapons87_silver_mace import apply as silver_mace87
  silver_mace87()
  from weapons87_storm_staff import apply as storm87
  storm87()
  from weapons87_tugarin_icon import apply as tugarin87
  tugarin87()
  from weapons87_whip import apply as whip87
  whip87()
  from weapons87_balance_b import apply as balance_b87
  balance_b87()
  from weapons87_likho_gui import apply as likho_gui87
  likho_gui87()
  from weapons87_unique_art import apply as unique_art87
  unique_art87()
 if not args.apply:write(D/'plans.json',plans)
 print(('APPLIED' if args.apply else 'DRAFT ONLY'),len(owned),'assets; 8 per-ID sheets; no gameplay changes')
if __name__=='__main__':main()
