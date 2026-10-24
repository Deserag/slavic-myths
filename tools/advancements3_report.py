"""Render real parent edges and the review table without launching Minecraft."""
import json,textwrap,html
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
from advancements3_resources import ROOT,OUT

tree=json.loads((OUT/'TREE.json').read_text('utf8'));nodes={n['id']:n for n in tree}
children={k:[]for k in nodes}
for n in tree:
    if n['parent']:children[n['parent']].append(n['id'])
positions={};row=0;bands=[]
def place(key,depth):
    global row
    first=row
    for child in children[key]:place(child,depth+1)
    if not children[key]:y=row*100+180;row+=1
    else:y=(positions[children[key][0]][1]+positions[children[key][-1]][1])/2
    positions[key]=(depth*330+35,y)
for key,label,color in [('first_neighbors','Мирная жизнь · 7','#3f7653'),('first_structure','Исследование мира · 24','#336b91'),('meet_bandits','Сражения · 25','#9b563f'),('tales_come_alive','Сказочные истории · 18','#76538a')]:
    start=row*100+130;place(key,1);bands.append((start,row*100+225,label,color));row+=1
positions['root']=(35,(positions['first_neighbors'][1]+positions['tales_come_alive'][1])/2)
width=int(max(p[0]for p in positions.values())+330);height=row*100+200
image=Image.new('RGB',(width,height),'#f5f1e8');draw=ImageDraw.Draw(image)
font=lambda n:ImageFont.truetype('C:/Windows/Fonts/arial.ttf',n)
f=font(22);small=font(15);large=font(30)
svg=[f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}" height="{height}" viewBox="0 0 {width} {height}">',f'<rect width="{width}" height="{height}" fill="#f5f1e8"/>']
def text(x,y,value,size=22,color='#24352f'):
    draw.text((x,y),value,font=font(size),fill=color)
    svg.append(f'<text x="{x}" y="{y+size}" font-family="Arial,sans-serif" font-size="{size}" fill="{color}">{html.escape(value)}</text>')
text(35,22,'Slavic Myths — 75 достижений, версия 3',30)
text(35,67,'Реальные parent → child. Получение независимо от родителей. Статическая схема; игровой GUI не запускался.',20)
for a,b,label,color in bands:
    draw.rectangle((330,a,width-12,b),fill='#fffdf7');svg.append(f'<rect x="330" y="{a}" width="{width-342}" height="{b-a}" fill="#fffdf7"/>');text(365,a-32,label,24,color)
for key,n in nodes.items():
    if not n['parent']:continue
    x,y=positions[key];px,py=positions[n['parent']];pts=[(px+292,py+37),(x-20,py+37),(x-20,y+37),(x,y+37)]
    draw.line(pts,fill='#a4a497',width=2);svg.append('<polyline points="'+' '.join(f'{a},{b}'for a,b in pts)+'" fill="none" stroke="#a4a497" stroke-width="2"/>')
for key,n in nodes.items():
    x,y=positions[key];color={'task':'#71846d','goal':'#3e7b85','challenge':'#b17a31'}[n['frame']]
    draw.rounded_rectangle((x,y,x+292,y+76),radius=6,fill='#ffffff',outline=color,width=4 if n['frame']=='challenge'else 2)
    svg.append(f'<rect x="{x}" y="{y}" width="292" height="76" rx="6" fill="white" stroke="{color}" stroke-width="{4 if n["frame"]=="challenge"else 2}"/>')
    for i,line in enumerate(textwrap.wrap(n['title_ru'],width=24)):text(x+10,y+5+i*24,line,21)
    text(x+10,y+56,key,13,'#656b66')
svg.append('</svg>');(OUT/'TREE.svg').write_text('\n'.join(svg),encoding='utf8');image.save(OUT/'TREE.png')

lines=['# Дерево достижений, версия 3','','75 карточек: корень + 7 / 24 / 25 / 18. Родитель задаёт только расположение.','','[PNG](TREE.png) · [SVG](TREE.svg) · [Полная машинная ведомость](ACHIEVEMENTS.csv) · [Совместимость всех прежних ID](COMPATIBILITY.csv)','','## Связи дерева','']
def outline(key,level=0):
    lines.append('  '*level+'- **'+nodes[key]['title_ru']+'** (`'+key+'`)')
    for child in children[key]:outline(child,level+1)
outline('root')
lines+=['','## Все 75 условий','','| ID | Родитель | RU / EN | Условие |','|---|---|---|---|']
for n in tree:lines.append(f"| `{n['id']}` | `{n['parent'] or '—'}` | {n['title_ru']} / {n['title_en']} | {n['description_ru']} |")
(OUT/'TREE.md').write_text('\n'.join(lines)+'\n',encoding='utf8')
print(f'TREE: {len(tree)} actual nodes, {len(tree)-1} edges, {width}x{height}; max depth {max(p[0] for p in positions.values())//330}')
