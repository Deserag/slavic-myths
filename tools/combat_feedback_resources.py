"""Visible serpent guard, authored shield-and-scales icon and current RU/EN names."""
from pathlib import Path
import json
from PIL import Image, ImageDraw
ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/slavicmyths'
for locale,text,title in [('ru_ru','Змеиная броня: −50% входящего урона','Тугарин Змей — защита 50%'),('en_us','Serpent Armor: −50% incoming damage','Tugarin Zmey — 50% armor')]:
    p=A/f'lang/{locale}.json'
    d=json.loads(p.read_text(encoding='utf-8'))
    d['effect.slavicmyths.serpent_guard']=text
    d['boss.slavicmyths.tugarin.true_form']=title
    p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
im=Image.new('RGBA',(18,18));draw=ImageDraw.Draw(im)
draw.polygon([(3,2),(14,2),(15,10),(12,14),(8,17),(4,13),(2,8)],fill='#492725',outline='#E6AE57')
draw.polygon([(5,4),(12,4),(13,9),(10,13),(8,15),(5,10)],fill='#AF573B')
for y in [5,8,11]:
    for x in [5,8,11]:draw.line([(x,y),(x+1,y+1),(x+2,y)],fill='#EDB45F')
im.save(A/'textures/mob_effect/serpent_guard.png')
