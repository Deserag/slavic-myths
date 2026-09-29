"""Audit registered model paths and pixels; write a contact sheet for human review."""
from pathlib import Path
from collections import defaultdict
import json, re, struct, zlib, hashlib

ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'src/main/resources/assets/slavicmyths'
JAVA=ROOT/'src/main/java/org/slavicmyths/registry/ModItems.java'
ITEMS=sorted(set(re.findall(r'\.register\(\s*"([a-z_]+)"',JAVA.read_text(encoding='utf-8'))))

def decode(path):
    data=path.read_bytes(); assert data.startswith(b'\x89PNG\r\n\x1a\n'),path
    off=8; compressed=b''; width=height=None
    while off<len(data):
        n=struct.unpack('!I',data[off:off+4])[0]; kind=data[off+4:off+8]; part=data[off+8:off+8+n]
        assert zlib.crc32(kind+part)&0xffffffff==struct.unpack('!I',data[off+8+n:off+12+n])[0],path
        if kind==b'IHDR': width,height,depth,color,_,_,_=struct.unpack('!IIBBBBB',part); assert (depth,color)==(8,6)
        if kind==b'IDAT': compressed+=part
        off+=12+n
    raw=zlib.decompress(compressed); rowlen=width*4+1; pixels=[]
    assert len(raw)==height*rowlen
    for y in range(height):
        row=raw[y*rowlen:(y+1)*rowlen]; assert row[0]==0,path
        pixels.append([tuple(row[1+x*4:1+x*4+4]) for x in range(width)])
    return pixels

def texture_for(name):
    path=RES/f'models/item/{name}.json'; assert path.is_file(),name
    obj=json.loads(path.read_text(encoding='utf-8'))
    if obj.get('parent','').startswith('slavicmyths:'):
        sub=obj['parent'].split(':',1)[1]
        obj=json.loads((RES/f'models/{sub}.json').read_text(encoding='utf-8'))
    tex=obj.get('textures',{})
    if not tex: return None # Forge spawn egg uses vanilla model and dynamic colors.
    ref=tex.get('layer0') or tex.get('all') or tex.get('cross') or tex.get('particle')
    if ref is None: return None
    if ref.startswith('#'): ref=tex[ref[1:]]
    if not ref.startswith('slavicmyths:'): return None
    target=RES/'textures'/(ref.split(':',1)[1]+'.png'); assert target.is_file(),(name,target)
    return target

def png_rgba(pixels):
    h=len(pixels); w=len(pixels[0])
    def chunk(kind,data): return struct.pack('!I',len(data))+kind+data+struct.pack('!I',zlib.crc32(kind+data)&0xffffffff)
    raw=b''.join(b'\0'+bytes(c for pixel in row for c in pixel) for row in pixels)
    return b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('!IIBBBBB',w,h,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b'')

def main():
    rows=[]; hashes=defaultdict(list); masks=defaultdict(list); paths=defaultdict(list)
    all_pixels=defaultdict(list)
    for path in (RES/'textures').rglob('*.png'):
        if path.parent.name=='entity': continue
        pixels=decode(path)
        if path.parent.name=='armor':
            assert len(pixels)==32 and len(pixels[0])==64,path
        else: assert len(pixels)==16 and len(pixels[0])==16,path
        assert any(p[3] for row in pixels for p in row),path
        all_pixels[hashlib.sha256(bytes(channel for row in pixels for p in row for channel in p)).hexdigest()].append(str(path.relative_to(RES)))
    for name in ITEMS:
        target=texture_for(name)
        if target is None: continue
        pixels=decode(target); assert len(pixels)==16 and len(pixels[0])==16,(name,target)
        opaque=sum(p[3]>0 for row in pixels for p in row); assert opaque>=10,(name,'empty')
        assert opaque<256 or 'ore' in name or name in {'carved_oak_pillar','carved_birch_planks','carved_oak_block','straw_block','hearth','altar','ancient_idol','amber_block','wall_carving'},(name,'full opaque square')
        paths[str(target.relative_to(RES))].append(name)
        hashes[hashlib.sha256(target.read_bytes()).hexdigest()].append(name)
        masks[''.join('1' if p[3] else '0' for row in pixels for p in row)].append(name)
        rows.append((name,pixels))
    duplicates=[v for v in hashes.values() if len(v)>1]
    if duplicates: raise AssertionError(('identical item textures',duplicates))
    pixel_duplicates=[v for v in all_pixels.values() if len(v)>1]
    if pixel_duplicates: raise AssertionError(('identical textures across folders',pixel_duplicates))
    shared=[v for v in paths.values() if len(v)>1]
    if shared: raise AssertionError(('shared texture references',shared))
    charm_family={'amber_charm','forest_charm','hunter_charm','perun_charm','traveler_charm','warding_charm'}
    suspicious=[v for mask,v in masks.items() if len(v)>=4 and mask!='1'*256 and not set(v)<=charm_family]
    if suspicious: raise AssertionError(('mass reused silhouette',suspicious))
    cols=10; scale=5; cell=20*scale; height=((len(rows)+cols-1)//cols)*cell
    canvas=[[(35,38,44,255) for _ in range(cols*cell)] for _ in range(height)]
    for i,(name,pixels) in enumerate(rows):
        ox=(i%cols)*cell+2*scale; oy=(i//cols)*cell+2*scale
        for y,row in enumerate(pixels):
            for x,p in enumerate(row):
                if p[3]:
                    for dy in range(scale):
                        for dx in range(scale): canvas[oy+y*scale+dy][ox+x*scale+dx]=p
    version=re.search(r"version = '([^']+)'",(ROOT/'build.gradle').read_text()).group(1)
    out=ROOT/f'docs/verification/visual-{version}.png'; out.write_bytes(png_rgba(canvas))
    print('PASS:',len(rows),'item textures;',len(all_pixels),'distinct item/block/armor textures; 0 identical pixels; 0 shared paths; 0 mass reused silhouettes')
    for i,(name,_) in enumerate(rows): print(f'{i:02d} {name}')
    print('Contact sheet:',out)

if __name__=='__main__': main()
