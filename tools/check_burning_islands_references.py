"""Read-only search for approved binary hashes, including renamed PNGs and ZIPs.
No extraction/overwrite; report only matching approved sources, not unrelated filenames.
"""
from pathlib import Path
import hashlib,json,struct,subprocess,zipfile
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/burning-islands-205'
EXPECTED={
'BURNING_ISLANDS_205_BIOME_CONCEPT.png':'a770acc9cb8401baa8199994f73cf28ee1fb3d43eff25fe0fd5bdbff92fbf605',
'BURNING_ISLANDS_205_CREATURES_CONCEPT.png':'a06f8d7730e43f93003a0a9c465c806e6e28a41260e7a61640d1f336924651d1',
'BURNING_ISLANDS_205_RUNE_MATERIALS_CONCEPT.png':'47a9f11cd0e3c98e48983cf28c8b5ed7c6687689d9abb9cc5ee28edd7a47fdab',
'BURNING_ISLANDS_205_BOSS_CONCEPT.png':'354c2c318c01ee67b21b60adc653a0a4747acc81786a433cb28e1c3fc26881a6',
'BURNING_ISLANDS_205_MAGMA_LORD_HEAD_PLACED_INVENTORY.png':'287ce3f15e28b1a646cf31de1e51fab85519bb03dc935d83bb7aea920395bc93'}
def main():
 result=subprocess.run(['rg','--files','--hidden',str(Path.home()),'-g','*.png','-g','*.zip'],capture_output=True,encoding='utf-8',errors='replace')
 matches=[];pngs=zips=candidates=errors=0
 for text in result.stdout.splitlines():
  p=Path(text)
  try:
   if p.suffix.lower()=='.png':
    pngs+=1
    with p.open('rb') as f:header=f.read(24)
    if len(header)<24 or header[:8]!=b'\x89PNG\r\n\x1a\n':continue
    width,height=struct.unpack('>II',header[16:24])
    if (width,height)!=(1536,1024) and p.name not in EXPECTED:continue
    candidates+=1;sha=hashlib.sha256(p.read_bytes()).hexdigest()
    for name,expected in EXPECTED.items():
     if sha==expected:matches.append({'path':str(p),'approvedName':name,'sha256':sha})
   elif p.stat().st_size<=30_000_000:
    zips+=1
    with zipfile.ZipFile(p) as z:
     for entry in z.infolist():
      name=Path(entry.filename).name
      if name in EXPECTED and entry.file_size<=12_100_000:
       sha=hashlib.sha256(z.read(entry)).hexdigest()
       if sha==EXPECTED[name]:matches.append({'archive':str(p),'member':entry.filename,'approvedName':name,'sha256':sha})
  except (OSError,ValueError,zipfile.BadZipFile,RuntimeError):errors+=1
 report={'scope':'Read-only home-profile PNG headers and exact candidate SHA; ZIP<=30MB central directories/approved filenames only','pngHeadersRead':pngs,'pngHashCandidates':candidates,'zipArchivesInspected':zips,'unreadableOrInvalidFiles':errors,'matches':matches,'status':'FOUND_REQUIRES_IMPORT' if matches else 'BLOCKED_REFERENCE_BINARY','limitations':'Unreadable files, other drives/users, non-1536x1024 renamed PNGs and archives>30MB not ruled out; no source bytes fabricated'}
 (O/'reference-search.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))
if __name__=='__main__':main()
