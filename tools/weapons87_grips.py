"""#115/#117: explicit per-model grip coordinates; never edits geometry, UV or gameplay."""
from pathlib import Path
import copy,json,math
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths/models/item'
# (actual model file, registry ID, authored grip Y, TP scale, TP X tilt, right yaw)
SPECS=[
 ('ancient_carolingian_sword_held','ancient_carolingian_sword',-2,.65,75,0),
 ('restored_carolingian_sword_held','restored_carolingian_sword',-2,.65,75,0),
 ('nightingale_dagger','nightingale_dagger',2,.5,70,0),
 ('trophy_kalin_sabre','trophy_kalin_sabre',2,.7,70,0),
 ('ancient_spear_held','ancient_spear',3,.65,55,0),
 ('restored_spear_held','restored_spear',3,.65,55,0),
 ('spear','spear',6,.7,55,0),('silver_spear','silver_spear',6,.7,55,0),
 ('pool_spear','pool_spear',7,.85,55,0),('thunder_spear','thunder_spear',6,.7,55,0),
 ('victor_spear','victor_spear',10,.75,55,0),('berdysh','berdysh',6,.7,55,180),
 ('carved_staff','carved_staff',10,.75,55,0),('storm_staff','storm_staff',10,.75,55,0),
 ('veles_staff','veles_staff',8,.8,55,0),('likho_staff','likho_staff',11,.7,55,0),
 ('morozko_staff_held','morozko_staff',10,.8,55,0),
 ('karachun_scythe_held','karachun_scythe',12,.75,70,180),
]
# Existing short-weapon orientations are retained; only center the actual grip in the palm.
SHORT={'silver_sword':4.5,'perunite_sword':4.5,'club':3.5,'wooden_mace':4,'stone_mace':4,'mace':4,'silver_mace':4,'perunite_mace':4,'battle_axe':3.5,'thunder_axe':4.5}
KEEP={'ancient_chekan_held':'owner accepted orientation, preserve','chekan_held':'owner accepted orientation, preserve'}
def rotate(v,a):
 x,y,z=v;rx,ry,rz=map(math.radians,a);x,y=x*math.cos(rz)-y*math.sin(rz),x*math.sin(rz)+y*math.cos(rz);x,z=x*math.cos(ry)+z*math.sin(ry),-x*math.sin(ry)+z*math.cos(ry);y,z=y*math.cos(rx)-z*math.sin(rx),y*math.sin(rx)+z*math.cos(rx);return [x,y,z]
def centered(grip,angles,scale,left=False,target=(0,0,0)):
 actual=[angles[0],-angles[1] if left else angles[1],-angles[2] if left else angles[2]];delta=rotate([(grip[i]-8)*scale[i] for i in range(3)],actual);translation=[target[i]-delta[i] for i in range(3)]
 if left:translation[0]*=-1
 return {'rotation':angles,'translation':[round(x,8) for x in translation],'scale':scale}
def plan():
 result={}
 for file,id,grip,s,tilt,yaw in SPECS:
  path=A/(file+'.json')
  if not path.exists():raise FileNotFoundError(path)
  d=json.loads(path.read_text(encoding='utf-8'));display=copy.deepcopy(d.get('display',{}));point=[8,grip,8]
  display['thirdperson_righthand']=centered(point,[tilt,yaw,0],[s]*3)
  display['thirdperson_lefthand']=centered(point,[tilt,(yaw+180)%360,0],[s]*3,True)
  # Custom mainhand scythe uses WeaponHands; ordinary offhand still consumes JSON.
  if id!='karachun_scythe':
   fp=min(s,.65);fp_yaw=115 if yaw==180 else -65
   display['firstperson_righthand']=centered(point,[0,fp_yaw,8],[fp]*3,target=(1,-1,-2))
   display['firstperson_lefthand']=centered(point,[0,-((fp_yaw+180)%360),-8],[fp]*3,True,target=(-1,-1,-2))
  else:
   # Offhand uses the ordinary item renderer. Match the actual custom idle wrist.
   display['firstperson_righthand']=centered(point,[0,170,0],[.65]*3,target=(0,-.32,-5.76))
   display['firstperson_lefthand']=centered(point,[0,-10,0],[.65]*3,True,target=(0,-.32,-5.76))
  result[file]={'id':id,'grip':point,'display':display,'reason':'center authored shaft/grip, constrain player proportions, keep existing geometry/UV'}
 for file,y in SHORT.items():
  path=A/(file+'.json');d=json.loads(path.read_text());display=copy.deepcopy(d.get('display',{}));point=[8,y,8]
  for ctx in ['thirdperson_righthand','thirdperson_lefthand']:
   old=display.get(ctx,display.get('thirdperson_righthand',{}));display[ctx]=centered(point,list(old.get('rotation',[0,0,0])),old.get('scale',[1,1,1]),ctx.endswith('lefthand'))
  result[file]={'id':file,'grip':point,'display':display,'reason':'KEEP existing angle and scale; FIX grip offset only'}
 return result
def apply():
 for file,p in plan().items():
  path=A/(file+'.json');d=json.loads(path.read_text(encoding='utf-8'));d['display']=p['display'];path.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
if __name__=='__main__':apply()
