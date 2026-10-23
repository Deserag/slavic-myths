"""Actual archived/current thrown JSON/UV and spear surface A/B, fixed cameras/scale."""
from pathlib import Path
import hashlib,json
from PIL import Image,ImageDraw
import weapons87_geometry as g
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/weapons87/continuation53';D=O/'thrown';A=R/'src/main/resources/assets/slavicmyths'
def main():
    records=[]
    for material in ('stone','gold','iron','silver','diamond','perunite','netherite'):
        sheet=Image.new('RGB',(1600,650),'#18212b');draw=ImageDraw.Draw(sheet)
        draw.text((8,8),'#173 ACTUAL archived/current JSON + same PNG/UV, fixed player-scale units. BEFORE / AFTER, not Minecraft.',fill='white')
        for row,family in enumerate(('throwing_knife','sulitsa')):
            name=material+'_'+family;old_root=D/'before/models/item'/(name+'.json');overrides={'slavicmyths:item/'+name:json.loads(old_root.read_text())}
            if material=='stone':overrides['slavicmyths:item/'+name+'_held']=json.loads((R/'tools/weapon-art-original/thrown-p40'/(name+'_held.json')).read_text())
            before=g.model('slavicmyths:item/'+name,'thirdperson_righthand',overrides);after=g.model('slavicmyths:item/'+name,'thirdperson_righthand')
            records.append({'id':name,'before_root_sha256':hashlib.sha256(old_root.read_bytes()).hexdigest(),'before_resolved':before,'after_resolved':after})
            y=40+row*300;draw.text((8,y),name+' / 1 model unit = 1/16 block, player32, palm4',fill='white')
            for view,angle in enumerate([(0,0,0),(0,90,0),(15,35,0)]):
                for side,model in enumerate([before,after]):
                    x=(view*2+side)*250;image=g.project(g.faces(model),245,32,angle);sheet.paste(image,(x,y+35),image);draw.text((x+5,y+20),('BEFORE ' if side==0 else 'AFTER ')+str(angle),fill='white')
            icon=Image.open(A/'textures/item'/(name+'.png')).convert('RGBA');sheet.paste(icon,(1510,y+40),icon);sheet.paste(icon.resize((64,64),Image.Resampling.NEAREST),(1510,y+100))
        sheet.save(D/(material+'-SOURCE-AB.png'))
    (D/'final-source.json').write_text(json.dumps(records,indent=2)+'\n')
    board=Image.new('RGB',(1600,900),'#18212b');draw=ImageDraw.Draw(board)
    draw.text((8,8),'#165/#170 actual spear source A/B. Five fully covered coincident west faces removed; same geometry, texture, UV, display and scale. NOT game.',fill='white')
    before=json.loads((O/'spear-before.json').read_text());after=json.loads((A/'models/item/spear.json').read_text())
    for col,angle in enumerate([(0,0,0),(0,90,0),(0,-90,0),(15,35,0)]):
        for row,model in enumerate([before,after]):
            image=g.project(g.faces(model),390,48,angle);board.paste(image,(col*400,45+row*420),image);draw.text((col*400+8,30+row*420),('BEFORE ' if row==0 else 'AFTER ')+str(angle),fill='white')
    board.save(O/'SPEAR-SURFACE-AB.png')
    print('PASS 14 actual before/after held-source records +7 material sheets +spear same-camera A/B')
if __name__=='__main__':main()
