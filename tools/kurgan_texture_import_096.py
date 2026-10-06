import json,shutil
from pathlib import Path
from PIL import Image,ImageDraw
meta=json.loads(Path('work/kurgan-texture-sources-096.json').read_text());root=Path('art/kurgan-0.9.6');root.mkdir(parents=True,exist_ok=True)
out=Path('src/main/resources/assets/slavicmyths/textures/block');sheet=Image.new('RGB',(4*272,3*292),'#161d22');draw=ImageDraw.Draw(sheet)
for i,row in enumerate(meta):
 src=Path(row['path']);dst=root/(row['name']+'-source.png');shutil.copy2(src,dst);im=Image.open(src).convert('RGBA');assert min(im.size)>=256
 tile=im.resize((256,256),Image.Resampling.NEAREST);tile.save(out/(row['name']+'.png'));x=(i%4)*272;y=(i//4)*292;sheet.paste(tile,(x,y));draw.text((x,y+258),row['name'][:38],fill='white');row['authorSource']=str(dst);row['runtimeSize']=[256,256]
(root/'provenance.json').write_text(json.dumps(meta,ensure_ascii=False,indent=2),encoding='utf-8');sheet.save(root/'block-texture-review.png');print('AUTHORED_STONE_TILES',len(meta))
