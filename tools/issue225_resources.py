"""Owner-requested #225: actual hunting horn and consistent whip material icon."""
from pathlib import Path
import json,copy
from PIL import Image
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths'

def apply():
    original=R/'tools/weapon-art-original/issue225/ohotnichiy_rog.json'
    horn=json.loads(original.read_text(encoding='utf-8'))
    horn.setdefault('display',{})['firstperson_righthand']={'rotation':[25,-20,-25],'translation':[0,6,0],'scale':[.4,.4,.4]}
    horn['display']['firstperson_lefthand']={'rotation':[25,20,25],'translation':[0,6,0],'scale':[.4,.4,.4]}
    horn['textures']['material']='slavicmyths:item/ohotnichiy_rog_surface225'
    im=Image.new('RGBA',(16,16))
    palette=[(221,205,166,255),(167,133,86,255),(82,91,85,255),(79,53,37,255)]
    for y in range(16):
        for x in range(16):
            base=palette[min(3,x//4)];delta=([12,2,-10,-3][y%4] if x%4!=0 else -20);im.putpixel((x,y),tuple(max(0,min(255,c+delta)) for c in base[:3])+(255,))
    for index,element in enumerate(horn['elements']):
        tile=2 if index in (5,6,7,8,13,14,15) else 3 if index>=16 else 0 if index<5 else 1
        for face in element['faces'].values():face['uv']=[tile*4,0,tile*4+4,16]
    im.save(A/'textures/item/ohotnichiy_rog_surface225.png')
    def write(name,data):(A/'models/item'/(name+'.json')).write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8')
    write('ohotnichiy_rog_held225',horn)
    write('ohotnichiy_rog',{'loader':'neoforge:separate_transforms','base':{'parent':'slavicmyths:item/ohotnichiy_rog_held225'},'perspectives':{'gui':{'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/ohotnichiy_rog'}}}})
    path=A/'models/item/poloz_whip.json';root=json.loads(path.read_text())
    root.pop('parent',None);root['loader']='neoforge:separate_transforms';root['base']={'parent':'slavicmyths:item/poloz_whip_pose_0'}
    root.setdefault('perspectives',{})['gui']={'parent':'slavicmyths:item/poloz_whip_icon87'}
    path.write_text(json.dumps(root,indent=2)+'\n',encoding='utf-8')
    from weapons87_flails import TILE
    for phase in range(5):
        path=A/'models/item'/f'poloz_whip_pose_{phase}.json';model=json.loads(path.read_text());model['textures']['lash']='slavicmyths:item/weapons87_materials'
        for index,element in enumerate(model['elements']):
            tx,ty=TILE['wood' if index==0 else 'gold' if index==1 else 'leather']
            for face in element['faces'].values():face['uv']=[tx,ty,tx+4,ty+4]
        path.write_text(json.dumps(model,indent=2)+'\n',encoding='utf-8')
    legacy=R/'src/main/resources/data/slavicmyths/recipe/flail.json'
    recipe=json.loads(legacy.read_text());recipe['neoforge:conditions']=[{'type':'neoforge:false'}];legacy.write_text(json.dumps(recipe,indent=2)+'\n',encoding='utf-8')
    for locale,label in [('ru_ru','Железный кистень'),('en_us','Iron Flail')]:
        path=A/'lang'/(locale+'.json');data=json.loads(path.read_text(encoding='utf-8'));data['item.slavicmyths.flail']=label;path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

if __name__=='__main__':apply()
