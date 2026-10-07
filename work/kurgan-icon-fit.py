import zipfile
z=zipfile.ZipFile('build/moddev/artifacts/neoforge-21.1.255-sources.jar');s=z.read('net/neoforged/neoforge/client/model/geometry/GeometryLoaderManager.java').decode();print('\n'.join(l for l in s.splitlines() if 'separate' in l.lower()))
from pathlib import Path
p=Path('tools/kurgan_item_import_096.py');s=p.read_text(encoding='utf-8-sig').replace('from PIL import Image,ImageDraw','from PIL import Image,ImageDraw,ImageOps').replace("rows=json.loads(Path('work/kurgan-item-sources-096.json').read_text());root=", "rows=json.loads(Path('work/kurgan-item-sources-096.json').read_text());assert len(rows)==24;root=")
s=s.replace("tile=im.resize((256,256),Image.Resampling.NEAREST);tile.save", "fit=ImageOps.contain(im,(256,256),Image.Resampling.NEAREST);tile=Image.new('RGBA',(256,256));tile.paste(fit,((256-fit.width)//2,(256-fit.height)//2));tile.save")
p.write_text(s,encoding='utf-8')
