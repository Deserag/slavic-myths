"""Read-only local SHA search; imports ONLY exact known PNG bytes, never substitutes generated art."""
from pathlib import Path
import hashlib,zipfile,csv,json
R=Path(__file__).resolve().parents[1]
EXPECTED={
'bae41350d204d5c8a90664560e852c7eddb3b92b83de20f9861a2990b75af3a8':('nether-armor','референс_алатырной_брони_в_незере.png'),
'2c01806a73c9de35634c2a77a3b87083844cf6ab7beb9f044b67e710c6107fa0':('nether-armor','пекельный_владыка_референс_доспеха.png'),
'ccbd535c3482b5ef371b8c2678f66790d10577bdb104796daf2135e90f977ef0':('nether-weapons','NETHER_WEAPONS_REFERENCE_FULL_APPROVAL_CANDIDATE.png'),
'c492bf1c42523d4b2f0e19c41a3afd3f8cfe214115c449b77a28a76f5195373b':('nether-weapons','01_navya_ostroga.png'),
'1d46dd3cfaabe9ed60dacca6021901ff83c7477ff9c4c43ddbb4f6f679ae14ba':('nether-weapons','02_ashen_greatsword.png'),
'9b7613512a5091c14bbec9387f7f737e59882716c083837cd14b8f1cb00d725a':('nether-weapons','03_shadow_knife.png'),
'adb160a83f0aee941a5c00553f51d273841d651ec45b535b6f3113133022b7cd':('nether-weapons','04_volcanic_flail.png'),
'2e5f09b3f833721c165ce56f5993063dc04b6b7b5d5223e8619727168deae6f4':('nether-weapons','05_infernal_bow.png'),
'22bbf1037de1f13fc968a743763097b3e5573e26003d6d77f5a1ec602ca2774b':('nether-ores','ORES_ORIGINAL.png')}
def main():
 roots=[R,Path.home()/'Downloads',Path.home()/'.codex/attachments',Path.home()/'Documents'];found={};errors=[];checked=0
 def check(data,source):
  nonlocal checked
  checked+=1;sha=hashlib.sha256(data).hexdigest()
  if sha not in EXPECTED:return
  folder,name=EXPECTED[sha];dest=R/'docs/concepts'/folder/'approved_originals'/name;dest.parent.mkdir(parents=True,exist_ok=True)
  if dest.exists() and hashlib.sha256(dest.read_bytes()).hexdigest()!=sha:raise ValueError('Refuse to overwrite different original '+str(dest))
  dest.write_bytes(data);found[sha]=(source,str(dest.relative_to(R)))
 import os
 seen=set()
 for root in roots:
  if not root.exists():continue
  for directory,dirs,files in os.walk(root):
   dirs[:]=[d for d in dirs if d not in ['.git','.gradle','node_modules','.tools','build']]
   for name in files:
    path=Path(directory)/name
    if path in seen:continue
    seen.add(path)
    try:
     if path.suffix.lower()=='.png' and path.stat().st_size<30000000:check(path.read_bytes(),str(path))
     elif path.suffix.lower()=='.zip' and path.stat().st_size<250000000:
      with zipfile.ZipFile(path) as z:
       for info in z.infolist():
        if info.filename.lower().endswith('.png') and info.file_size<30000000:check(z.read(info),str(path)+'!'+info.filename)
    except (OSError,ValueError,zipfile.BadZipFile,RuntimeError) as error:errors.append(str(path)+': '+str(error))
 out=R/'docs/implementation/2026-10-29-hell-remaining'
 with (out/'ASSET_PROVENANCE.tsv').open('w',encoding='utf-8',newline='') as f:
  w=csv.writer(f,delimiter='\t');w.writerow(['expected_file','expected_sha256','actual_source','imported_path','status'])
  for sha,(folder,name) in EXPECTED.items():w.writerow([folder+'/'+name,sha,*found.get(sha,('NOT_FOUND','NOT_IMPORTED')),'ART_SOURCE_PASS' if sha in found else 'ART_BLOCKED_REFERENCE'])
 (out/'reference-search.json').write_text(json.dumps({'roots':[str(p) for p in roots],'checked_png':checked,'matched':len(found),'errors':errors,'scope':'Local mounted roots and ZIPs<250MB; not remote ChatGPT library'},ensure_ascii=False,indent=2),encoding='utf-8');print('Reference PNGs inspected',checked,'matched',len(found),'errors',len(errors))
if __name__=='__main__':main()
