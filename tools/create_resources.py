from pathlib import Path
import struct, zlib
from generate_data import generate
root = Path(__file__).resolve().parents[1]
res = root / 'src/main/resources'
generate(root)
from generate_textures import generate as generate_textures
generate_textures(root)

# Original hand-authored pixels; no external images, interpolation or image editing.
palette={'.':(0,0,0,0),'o':(55,40,31,255),'b':(126,84,47,255),'t':(185,139,78,255),'c':(222,188,126,255),'w':(249,223,165,255),'r':(113,48,36,255),'s':(48,57,73,255),'g':(83,100,122,255),'h':(126,147,165,255),'l':(176,213,217,255),'y':(255,226,120,255)}
sprites={
'birch_bark_scroll':[
'................','...ooooooo......','..obwwwwwbo.....','..ocooooocto....','...ocwwwccto....','...ocbbwccto....','...ocwwwccto....','...ocwbbccto....','...orrrrrrro....','...ocwwwccto....','...ocbbwccto....','...ocwwwccto....','..obccccctbo....','..obooooobbo....','...ooooooo......','................'],
'thunder_stone':[
'................','......ssss......','....sshhhgs.....','...shhhylggs....','..shhhylggggs...','..shhylgggggs...','.shhylllggggss..','.shgggylggggss..','.sgggylggggsss..','.sggyllggggsss..','..sggylgggsss...','..sgylgggssss...','...sgggsssss....','....sssssss.....','................','................'],
'warding_charm':[
'.....oooooo.....','....ob....bo....','....ob....bo....','.....ob..bo.....','......otto......','......owwo......','.....otyyto.....','....otwyycto....','...otwwyyccto...','...otyyooycto...','...otcyooycto...','....otcyycto....','.....otccto.....','......otto......','.......oo.......','................']}
def chunk(t,data):
    return struct.pack('!I',len(data))+t+data+struct.pack('!I',zlib.crc32(t+data)&0xffffffff)
for item,rows in sprites.items():
    assert len(rows)==16 and all(len(r)==16 for r in rows)
    raw=b''.join(b'\x00'+bytes(c for p in row for c in palette[p]) for row in rows)
    png=b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('!IIBBBBB',16,16,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b'')
    path=res/'assets/slavicmyths/textures/item'/f'{item}.png'
    path.parent.mkdir(parents=True,exist_ok=True); path.write_bytes(png)
from generate_spirits import generate as generate_spirits
generate_spirits(root)
from generate_milestone import generate as generate_milestone
generate_milestone(root)
from generate_041 import generate as generate_041
generate_041(root)
from generate_045 import generate as generate_045
generate_045(root)
from generate_046 import generate as generate_046
generate_046(root)
from generate_047 import generate as generate_047
generate_047()
from generate_050_models import build_models
from generate_050 import items as items_050, blocks as blocks_050, resources as resources_050, sounds as sounds_050
build_models()
items_050(); blocks_050(); resources_050(); sounds_050()
from generate_051 import generate as generate_051
generate_051()
print('Created 0.5.1 resources.')

# RPG resources remain the final layer.
import generate_060
from generate_065 import *
from wildlife_overhaul import generate as wildlife_overhaul
wildlife_overhaul()
from folk_equipment import generate as folk_equipment
folk_equipment()
from finalize_065 import all_resources as finalize_065
finalize_065()
from artifact_065 import generate as artifact_065
artifact_065()
