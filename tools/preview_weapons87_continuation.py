"""Actual native ModelPart, hand ItemTransform and real JSON/UV A/B at fixed scale."""
from pathlib import Path
import json
import numpy as np
from PIL import Image,ImageDraw
import armor88_preview as painter
import weapons87_geometry as geometry
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/weapons87/continuation53'
original_texture=painter.texture
def texture(ref):
    return original_texture(ref) if ':textures/' in ref else geometry.texture(ref)
painter.texture=texture
def main():
    rows=json.loads((O/'player-source.json').read_text());representatives=json.loads((O/'representative-input.json').read_text())
    whip={round(r['phase'],2):r['faces'] for r in json.loads((O/'whip-source.json').read_text())}
    phases=[0,.2,.45,.72,1]
    for shape,source in representatives.items():
        board=Image.new('RGB',(2400,1520),'#18212b');d=ImageDraw.Draw(board)
        d.text((8,8),source['id']+' | actual ModelPart/hand/JSON/UV BEFORE / AFTER | fixed scale / native matrices | source only, game NOT_RUN',fill='white')
        for ri,phase in enumerate(phases):
            for ci,(body,left,yaw) in enumerate([('Steve',False,0),('Steve',False,90),('Steve',False,-35),('Alex',True,0),('Alex',True,90),('Alex',True,-35)]):
                for ai,after in enumerate([False,True]):
                    row=next(r for r in rows if r['shape']==shape and r['body']==body and r['left']==left and abs(r['phase']-phase)<1e-5 and r['after']==after)
                    layers=[{'texture':row['texture'],'faces':row['faces']}]
                    matrix=np.array(row['item_matrix']).reshape((4,4)).T
                    faces=whip[phase] if shape=='WHIP' and after else source['faces']
                    for face in faces:
                        vertices=np.array(face['vertices'])/16
                        transformed=(np.column_stack((vertices,np.ones(4)))@matrix.T)[:,:3]*16
                        layers.append({'texture':face['texture'],'faces':[np.column_stack((transformed,np.array(face['uv']))).tolist()]})
                    image=painter.render({'layers':layers},yaw);x=(ci*2+ai)*200;y=55+ri*290
                    board.paste(image,(x,y));d.text((x+3,y),f'{body} {"L" if left else "R"} {yaw} / {phase} '+('AFTER' if after else 'BEFORE'),fill='white')
        board.save(O/(shape+'-SOURCE-AB.png'))
    lash=Image.new('RGB',(1500,650),'#18212b');d=ImageDraw.Draw(lash)
    d.text((8,8),'Poloz actual authored mesh and production WhipMotion, unchanged textures/UV, same scale/camera. Grip fixed. NOT gameplay.',fill='white')
    for i,phase in enumerate(phases):
        for j,faces in enumerate([representatives['WHIP']['faces'],whip[phase]]):
            render=geometry.project([(f['texture'],np.array(f['vertices']),np.array(f['uv'])) for f in faces],295,64,(15,35,0))
            lash.paste(render,(i*300,45+j*300),render);d.text((i*300+4,30+j*300),str(phase)+(' BEFORE authored pose0' if j==0 else ' AFTER flexible lash'),fill='white')
    lash.save(O/'WHIP-FIVE-PHASES.png')
    print('PASS: nine native-player A/B sheets and actual-UV five-phase lash sheet; no conceptual image substitutions')
if __name__=='__main__':main()
