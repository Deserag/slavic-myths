"""Offline layout illustration from source coordinates; not a game screenshot."""
from pathlib import Path
from PIL import Image,ImageDraw
import zipfile,json,io
ROOT=Path(__file__).resolve().parents[1];ASSET=ROOT/'src/main/resources/assets/slavicmyths';OUT=ROOT/'docs/media/rune-foundation-1341'
GLYPHS={}
with zipfile.ZipFile('C:/Users/pavel/.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar') as archive:
 for provider in json.loads(archive.read('assets/minecraft/font/include/default.json'))['providers']:
  ns,name=provider['file'].split(':');atlas=Image.open(io.BytesIO(archive.read(f'assets/{ns}/textures/{name}'))).convert('RGBA');chars=provider['chars'];cw=atlas.width//len(chars[0]);ch=atlas.height//len(chars);height=provider.get('height',8);factor=height/ch
  for row,sequence in enumerate(chars):
   for col,char in enumerate(sequence):
    if char=='\0' or char in GLYPHS:continue
    glyph=atlas.crop((col*cw,row*ch,(col+1)*cw,(row+1)*ch));bbox=glyph.getchannel('A').getbbox();advance=int((bbox[2] if bbox else 0)*factor+.5)+1
    GLYPHS[char]=(glyph.resize((round(cw*factor),height),Image.Resampling.NEAREST),advance,7-provider['ascent'])
GLYPHS[' ']=(Image.new('RGBA',(4,8)),4,0)
def text(im,s,x,y,color='#333333',center=False):
 if center:x-=sum(GLYPHS.get(c,GLYPHS['?'])[1] for c in s)//2
 for char in s:
  mask,advance,offset=GLYPHS.get(char,GLYPHS['?']);paint=Image.new('RGBA',mask.size,color);paint.putalpha(mask.getchannel('A'));im.alpha_composite(paint,(x,y+offset));x+=advance
def icon(im,id,x,y):
 s=Image.open(ASSET/f'textures/item/{id}.png').resize((16,16),Image.Resampling.NEAREST);im.alpha_composite(s,(x,y))
def panel(popup=False):
 im=Image.new('RGBA',(202,214),'#c6c6c6');d=ImageDraw.Draw(im);d.rectangle((0,0,201,1),fill='#eeeeee');d.rectangle((0,212,201,213),fill='#555555')
 text(im,'Рунная наковальня',101,7,center=True)
 for x,w,s in [(8,60,'Создание'),(71,60,'Усиление'),(134,60,'Установка')]:
  d.rectangle((x,21,x+w-1,38),fill='#a6a6a6',outline='#555555');text(im,s,x+w//2,26,center=True)
 for x,id,count in [(20,'soul','1/1'),(52,'iron_dust','4/4'),(84,'coal_dust','4/4'),(116,None,None),(158,'blank_rune_1',None)]:
  d.rectangle((x-1,53,x+16,70),fill='#8b8b8b',outline='#373737')
  if id:icon(im,id,x,54)
  if count:text(im,count,x+8,43,'#316334',True)
 text(im,'>',142,58)
 d.rectangle((8,79,193,96),fill='#a6a6a6',outline='#555555');text(im,'Пустая руна I',101,84,center=True)
 text(im,'Выбор рецепта / колесо',9,103);text(im,'Инвентарь',20,121,'#404040')
 for row in range(4):
  for col in range(9):
   x=20+col*18;y=132+row*18 if row<3 else 190;d.rectangle((x-1,y-1,x+16,y+16),fill='#8b8b8b',outline='#373737')
 if popup:
  ids=['blank_rune_1','blank_rune_1','blank_rune_2','blank_rune_2','blank_rune_3','bound_soul','coal_dust','diamond_dust','gold_dust','iron_dust','lapis_dust','perunite_dust','empowered_soul','soul','soul']
  d.rectangle((8,42,107,145),fill='#333333')
  for n,id in enumerate(ids):
   x=12+n%4*24;y=46+n//4*24;d.rectangle((x-1,y-1,x+18,y+18),fill='#a38e50' if n==0 else '#777777');icon(im,id,x+1,y+1)
 return im
def main():
 OUT.mkdir(parents=True,exist_ok=True);canvas=Image.new('RGBA',(440,240),'#211912')
 for n,popup in enumerate((False,True)):canvas.alpha_composite(panel(popup),(8+n*220,18))
 text(canvas,'Офлайн-иллюстрация: не игровой скриншот',8,3,'#f2e8d8')
 canvas.resize((1320,720),Image.Resampling.NEAREST).save(OUT/'container-layout.png')
 print('Offline container layout generated')
if __name__=='__main__':main()
