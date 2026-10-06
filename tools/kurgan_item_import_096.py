import json,shutil
from pathlib import Path
from PIL import Image,ImageDraw,ImageOps
rows=json.loads(Path('work/kurgan-item-sources-096.json').read_text());assert len(rows)==24;root=Path('art/kurgan-0.9.6/items');root.mkdir(parents=True,exist_ok=True);out=Path('src/main/resources/assets/slavicmyths');sheet=Image.new('RGB',(6*176,((len(rows)+5)//6)*190),'#283038');draw=ImageDraw.Draw(sheet)
for i,row in enumerate(rows):
 name=row['name'];src=Path(row['path']);shutil.copy2(src,root/(name+'-source.png'));im=Image.open(src).convert('RGBA');assert min(im.size)>=256 and im.getextrema()[3][0]==0
 fit=ImageOps.contain(im,(256,256),Image.Resampling.NEAREST);tile=Image.new('RGBA',(256,256));tile.paste(fit,((256-fit.width)//2,(256-fit.height)//2));tile.save(out/'textures/item'/f'{name}.png');x=(i%6)*176;y=(i//6)*190;preview=tile.resize((160,160),Image.Resampling.NEAREST);sheet.paste(preview,(x,y),preview);draw.text((x,y+162),name[:25],fill='white')
 p=out/'models/item'/f'{name}.json';model=json.loads(p.read_text(encoding='utf-8-sig'));sprite={'parent':'minecraft:item/generated','textures':{'layer0':'slavicmyths:item/'+name}}
 if model.get('loader')=='neoforge:separate_transforms':model['perspectives']['gui']=sprite
 elif model.get('elements'):
  held=out/'models/item'/f'{name}_held.json';held.write_text(json.dumps(model,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');model={'loader':'neoforge:separate_transforms','base':{'parent':'slavicmyths:item/'+name+'_held'},'perspectives':{'gui':sprite},'textures':{'particle':'slavicmyths:item/'+name}}
 else:model=sprite
 p.write_text(json.dumps(model,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');row['authorSource']=str(root/(name+'-source.png'));row['runtimeSize']=[256,256]
(root/'provenance.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2),encoding='utf-8');sheet.save(root/'item-texture-review.png');print('AUTHORED_ITEM_ICONS',len(rows),'held3DModelsPreserved')
