"""Render exact GameTest-exported voxels; these are source diagrams, not game captures."""
from pathlib import Path
import argparse,csv,html,json,re,shutil
from PIL import Image,ImageDraw,ImageFont

parser=argparse.ArgumentParser()
parser.add_argument('export',type=Path)
parser.add_argument('output',type=Path)
args=parser.parse_args()
args.output.mkdir(parents=True,exist_ok=True)
cells={}
for row in csv.DictReader(args.export.open(encoding='utf-8'),delimiter='\t'):
    v,x,y,z=map(int,[row[k] for k in ['variant','x','y','z']])
    cells[v,x,y,z]=row['blockstate']
ys=range(max(k[2] for k in cells),min(k[2] for k in cells)-1,-1)
palette={'air':'#e6ebee','netherrack':'#79483f','polished_blackstone_bricks':'#3b3e47',
 'cracked_polished_blackstone_bricks':'#54515f','polished_blackstone':'#505562',
 'chiseled_polished_blackstone':'#bd8f70','basalt':'#77818b','polished_basalt':'#9397a6',
 'crimson_stem':'#7b3248','crimson_planks':'#a4495c','campfire':'#ef9b38','magma_block':'#d56126',
 'chain':'#727b86','soul_lantern':'#45cbd3','lantern':'#f7d661','chest':'#b78038',
 'polished_blackstone_brick_stairs':'#a9875d','polished_blackstone_brick_wall':'#74717e',
 'red_wall_banner':'#c73746','skeleton_skull':'#e6dfd4','candle':'#daccba','blackstone':'#2d303a'}
def block(s):return re.search(r'Block\{minecraft:([^}]+)',s).group(1)
def color(s):
    if block(s)=='campfire' and 'lit=false' in s:return '#785747'
    if block(s)=='candle' and 'lit=false' in s:return '#9d9480'
    return palette.get(block(s),'#ad78ba')
width,height=920,60+len(ys)*220
parts=[f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">',
 '<rect width="100%" height="100%" fill="#151c26"/>',
 '<text x="20" y="26" fill="white" font-family="sans-serif" font-size="18">Exact source voxel layers — not Minecraft screenshots</text>']
for v in range(4):
    for iy,y in enumerate(ys):
        left,top=20+v*225,60+iy*220
        parts.append(f'<text x="{left}" y="{top}" fill="white" font-family="sans-serif" font-size="14">Variant {"ABCD"[v]} / Y={y}</text>')
        for z in range(15):
            for x in range(15):
                s=cells.get((v,x,y,z));fill=color(s) if s else '#26313e'
                px,py=left+x*12,top+12+z*12
                parts.append(f'<rect x="{px}" y="{py}" width="12" height="12" fill="{fill}" stroke="#707987" stroke-width=".3"><title>{html.escape(s or "UNTOUCHED")} ({x},{y},{z})</title></rect>')
                if s and 'stairs}' in s:
                    direction=re.search(r'facing=(\w+)',s).group(1)
                    dx,dz={'north':(0,-1),'south':(0,1),'east':(1,0),'west':(-1,0)}[direction]
                    parts.append(f'<line x1="{px+6-dx*3}" y1="{py+6-dz*3}" x2="{px+6+dx*3}" y2="{py+6+dz*3}" stroke="white" stroke-width="2"/>')
        parts.append(f'<text x="{left}" y="{top+207}" fill="#bdc8d5" font-family="sans-serif" font-size="11">X west→east; Z north→south</text>')
parts.append('</svg>')
(args.output/'LAYERS.svg').write_text('\n'.join(parts),encoding='utf-8')
shutil.copy2(args.export,args.output/'VOXELS.tsv')
im=Image.new('RGB',(920,300),'#151c26');draw=ImageDraw.Draw(im)
font=ImageFont.truetype('C:/Windows/Fonts/arial.ttf',15)
draw.text((20,12),'Source geometry / top projection — not game screenshots',font=font,fill='white')
for v in range(4):
    left=20+v*225
    draw.text((left,43),'Variant '+'ABCD'[v],font=font,fill='white')
    for z in range(15):
        for x in range(15):
            s=next((cells[v,x,y,z] for y in ys if (v,x,y,z) in cells and y>=0 and block(cells[v,x,y,z])!='air'),None);px,py=left+x*12,70+z*12
            draw.rectangle((px,py,px+11,py+11),fill=color(s) if s else '#26313e',outline='#707987',width=1)
im.save(args.output/'SURFACE.png')
counts={str(v):sum(1 for (vv,x,y,z),s in cells.items() if vv==v and block(s)!='air') for v in range(4)}
(args.output/'geometry-counts.json').write_text(json.dumps(counts,indent=2)+'\n',encoding='utf-8')
print('Exported exact solid block counts:',counts)
