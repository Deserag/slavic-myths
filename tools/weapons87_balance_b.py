"""Approved22.10 packageB tooltip overlay; no art generation or arbitrary balance edits."""
from pathlib import Path
import argparse,hashlib,json,zipfile
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';O=R/'docs/implementation/weapons87';D=O/'blueprints/balance-b'
TEXT={
 'ru_ru':{'tooltip.slavicmyths.weapon.perunite_mace':'Тяжёлые перунитовые сегменты. Медленные удары.','tooltip.slavicmyths.weapon.storm_staff':'Базовый разряд: 6 магического урона. Перезарядка: 3 с.'},
 'en_us':{'tooltip.slavicmyths.weapon.perunite_mace':'Heavy perunite segments. Slow strikes.','tooltip.slavicmyths.weapon.storm_staff':'Base pulse: 6 magic damage. Cooldown: 3 s.'}}

def apply():return main(True)
def main(apply=False):
 D.mkdir(parents=True,exist_ok=True)
 if not apply:
  archive=O/'before-balance-b.zip'
  if archive.exists():raise RuntimeError('Preserve beforeB source')
  paths=['src/main/java/org/slavicmyths/registry/ModItems.java','src/main/java/org/slavicmyths/item/StormStaffItem.java']+['src/main/resources/assets/slavicmyths/lang/'+lang+'.json' for lang in TEXT]+['docs/implementation/weapons87/native-attributes-after.json']
  with zipfile.ZipFile(archive,'w',zipfile.ZIP_DEFLATED) as z:
   for p in paths:z.write(R/p,p)
 manifest={}
 for lang,updates in TEXT.items():
  key='lang/'+lang+'.json';data=json.loads((A/key).read_text(encoding='utf-8'));data.update(updates);path=A/key if apply else D/'draft-assets'/key;path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');manifest[key]=hashlib.sha256(path.read_bytes()).hexdigest()
 (D/('applied-manifest.json' if apply else 'draft-manifest.json')).write_text(json.dumps(manifest,indent=2)+'\n');print(('APPLIED' if apply else 'DRAFT'),'approvedB two exact tooltips RU/EN; other translations retained');return manifest
if __name__=='__main__':
 p=argparse.ArgumentParser();p.add_argument('--apply',action='store_true');main(p.parse_args().apply)
