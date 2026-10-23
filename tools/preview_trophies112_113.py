"""Actual unchanged multi-material models, offline; no player/game simulation."""
from pathlib import Path
from PIL import Image,ImageDraw
import json

def main():
    root=Path(__file__).resolve().parents[1]
    source=(root/'tools/trophy111_source_preview.py').read_text(encoding='utf-8')
    source=source.replace('colors[selection]=tex[ty,tx]', "face_tex=np.array(MATS[face['texture'].lstrip('#')].convert('RGBA'));fh,fw=face_tex.shape[:2];fx=np.clip(((a+u*(c-a))*fw/16).astype(int),0,fw-1);fy=np.clip(((b+v*(d-b))*fh/16).astype(int),0,fh-1);colors[selection]=face_tex[fy,fx]")
    ns={'__name__':'offline_trophy_audit','__file__':str(root/'tools/trophy111_source_preview.py')}
    exec(compile(source,'offline_trophy_audit','exec'),ns)
    assets=root/'src/main/resources/assets/slavicmyths'
    board=Image.new('RGB',(640,720),'#30343b');d=ImageDraw.Draw(board)
    for i,n in enumerate(['nightingale_lock','nightingale_mark','mor_crystal']):
        m=json.loads((assets/'models/item'/(n+'.json')).read_text())
        if m.get('parent','').startswith('slavicmyths:block/'):m=json.loads((assets/'models/block'/(n+'.json')).read_text())
        mats={k:Image.open(assets/'textures'/(v.split(':')[1]+'.png')).convert('RGBA') for k,v in m['textures'].items() if v.startswith('slavicmyths:')}
        ns['MATS']=mats
        for j,context in enumerate(['gui','firstperson_righthand','thirdperson_righthand']):
            im,pose=ns['render'](m,next(iter(mats.values())),context,64)
            assert im.getbbox(),(n,context)
            im=im.resize((160,160),Image.Resampling.NEAREST)
            board.paste(im,(j*210,i*230+30),im);d.text((j*210,i*230+5),n+' '+context[:5],fill='white')
    board.save(root/'docs/implementation/trophies112-113/special-models-KEEP.png')

if __name__=='__main__':main()
