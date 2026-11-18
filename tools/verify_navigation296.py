"""Scoped source/resource/JAR audit for #296. No game or client launch."""
from pathlib import Path
import subprocess,json,hashlib,zipfile,runpy
R=Path(__file__).resolve().parents[1]
BASE='50cfd72d'
J='src/main/java/org/slavicmyths/'
OWNED=[J+p for p in ['client/TaleScreen.java','navigation/client/NavigationScreen.java','tales/TaleService.java','navigation/SearchCoordinates.java','navigation/SearchGuidance.java','tales/TaleHintVisibility.java']]
LANG=['src/main/resources/assets/slavicmyths/lang/'+s+'.json' for s in ['ru_ru','en_us']]
def baseline(path):return subprocess.check_output(['git','show',BASE+':'+path],cwd=R)
def main():
 changed=subprocess.check_output(['git','diff',BASE,'--name-only','--','src'],cwd=R,text=True).splitlines()
 changed+=subprocess.check_output(['git','ls-files','--others','--exclude-standard','--','src'],cwd=R,text=True).splitlines()
 assert set(changed)<=set(OWNED+LANG),('out of scope',changed)
 for path in ['navigation/SearchArea.java','navigation/SlavicMarker.java','navigation/NavigationNetwork.java','navigation/NavigationManager.java','navigation/NavigationState.java','tales/TaleData.java','tales/TaleSnapshotMerge.java']:
  assert (R/(J+path)).read_bytes().replace(b'\r\n',b'\n')==baseline(J+path).replace(b'\r\n',b'\n'),('persistence/protocol/generation changed',path)
 old=baseline(J+'tales/TaleService.java').decode('utf-8').replace('h.putBoolean("Stale",stale);h.remove("Target");h.remove("Anchor");','hints.put(key,TaleHintVisibility.publicCopy(h,stale));')
 assert (R/(J+'tales/TaleService.java')).read_text(encoding='utf-8').replace('\r\n','\n')==old.replace('\r\n','\n'),'server changes outside public projection'
 pairs=runpy.run_path(str(R/'tools/navigation296_resources.py'))['PAIRS']
 for path in LANG:
  before=json.loads(baseline(path));after=json.loads((R/path).read_text(encoding='utf-8'))
  assert all(after.get(k)==v for k,v in before.items()),'unrelated locale changed'
  assert set(after)-set(before)=={'navigation.search.'+key for key in pairs},'unexpected locale delta'
  for key in pairs:assert after['navigation.search.'+key].count('%s')==pairs[key][0].count('%s'),'placeholder mismatch'
 hashes={p:hashlib.sha256((R/p).read_bytes()).hexdigest() for p in LANG};runpy.run_path(str(R/'tools/navigation296_resources.py'),run_name='__main__');assert all(hashlib.sha256((R/p).read_bytes()).hexdigest()==v for p,v in hashes.items()),'resource generator drift'
 for path in OWNED[3:]:
  s=(R/path).read_text(encoding='utf-8');assert 'net.minecraft.client' not in s,'common loads client'
  assert all(token not in s for token in ['getChunk','findNearest','setBlock','PacketDistributor','MinecraftServer']),'display helper performs server work'
 screen=(R/(J+'client/TaleScreen.java')).read_text(encoding='utf-8');body=screen.split('private void renderMap(',1)[1].split('private String group(',1)[0]
 assert all(token not in body for token in ['getChunk','sendToServer','findNearest','setBlock','mapZoom']), 'render does search/legacy map'
 assert 'mapLayout.split' in body and 'SearchCoordinates.scroll' in body and 'mapTick!=tick' in body
 nav=(R/(J+'navigation/client/NavigationScreen.java')).read_text(encoding='utf-8');assert 'plainSubstrByWidth' not in nav and 'detailLayout.split' in nav and 'mouseScrolled' in nav
 jar=R/'build/libs/slavicmyths-2.0.1-HELL.jar'
 with zipfile.ZipFile(jar) as z:
  names=z.namelist();assert not any('Navigation296Offline' in n or n.startswith('org/slavicmyths/verify/') for n in names),'test class packaged'
  for path in OWNED:
   cls=path.removeprefix('src/main/java/').removesuffix('.java')+'.class';assert z.read(cls)==(R/'build/classes/java/main'/cls).read_bytes(),('class packaging mismatch',cls)
 out={'scope':'SOURCE/RESOURCES/PRODUCTION_JAR_ONLY','baseline':BASE,'jar':str(jar),'sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'sources':{p:hashlib.sha256((R/p).read_bytes()).hexdigest() for p in OWNED+LANG},'minecraft_launches':0,'visual':'NOT_RUN','relog':'NOT_RUN','MP':'NOT_RUN'}
 (R/'docs/implementation/navigation296-20261025/source-jar.json').write_text(json.dumps(out,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print('PASS scoped #296 source, public projection, protocol preservation, RU/EN overlay and production class packaging')
if __name__=='__main__':main()
