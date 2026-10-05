from pathlib import Path
import concurrent.futures,hashlib,json,urllib.request,urllib.parse,zipfile
root=Path.cwd();cache=root/'.tools/test-pack-0.9.4';cache.mkdir(parents=True,exist_ok=True)
mods=[('curios','9.5.1+1.21.1','curios-neoforge-9.5.1+1.21.1.jar',309927,6529130,'curios',True),('jei','19.51.0.418','jei-1.21.1-neoforge-19.51.0.418.jar',238222,8792638,'jei',False),('jade','15.10.6','Jade-1.21.1-NeoForge-15.10.6.jar',324717,8591319,'jade',False),('xaerominimap','26.5.0','xaerominimap-neoforge-1.21.1-26.5.0.jar',263420,8849842,'xaeros-minimap',False),('xaeroworldmap','1.45.0','xaeroworldmap-neoforge-1.21.1-1.45.0.jar',317780,8698659,'xaeros-world-map',False),('fallingtree','1.21.1.11','FallingTree-1.21.1-1.21.1.11.jar',349559,6835168,'falling-tree',False)]
def fetch(row):
 name,version,filename,project,fileid,slug,core=row
 entry=dict(id=name,version=version,filename=filename,loader='neoforge',minecraft='1.21.1',curseforgeProjectId=project,curseforgeFileId=fileid,sourceUrl=f'https://www.curseforge.com/minecraft/mc-mods/{slug}/files/{fileid}',sha256=None,installedPath=None,requiredForCore=core,requiredForTestingPack=True,selection='required' if core else 'recommended',status='BLOCKED')
 target=cache/filename
 url=f'https://mediafilez.forgecdn.net/files/{fileid//1000}/{fileid%1000}/{urllib.parse.quote(filename)}'
 try:
  if not target.exists():
   req=urllib.request.Request(url,headers={'User-Agent':'SlavicMythsPort/0.9.4'})
   with urllib.request.urlopen(req,timeout=45) as response:
    final=response.geturl();assert urllib.parse.urlparse(final).hostname.endswith('forgecdn.net'),final
    content=response.read()
   with zipfile.ZipFile(__import__('io').BytesIO(content)) as jar:
    assert 'META-INF/neoforge.mods.toml' in jar.namelist(), 'Not a NeoForge artifact'
   target.write_bytes(content)
  with zipfile.ZipFile(target) as jar:
   metadata=jar.read('META-INF/neoforge.mods.toml').decode('utf-8')
   assert '1.21' in metadata or name.startswith('xaero'), 'Minecraft range needs manual verification'
  entry.update(status='DOWNLOADED_NOT_INSTALLED',sha256=hashlib.sha256(target.read_bytes()).hexdigest(),downloadUrl=url,stagedPath=str(target.resolve()),size=target.stat().st_size)
  (cache/(filename+'.metadata.toml')).write_text(metadata,encoding='utf-8')
 except Exception as exc:entry['error']=str(exc)
 return entry
with concurrent.futures.ThreadPoolExecutor(max_workers=6) as pool:entries=list(pool.map(fetch,mods))
lock=dict(schemaVersion=1,modpack='Slavic-Myths-1.21.1-Testing',minecraft='1.21.1',loader='neoforge',neoForge='21.1.255',javaMajor=21,javaPath=r'C:/Program Files/Java/jdk-21.0.12/bin/javaw.exe',deploymentStatus='BLOCKED_BUILD',polyMCPath=r'C:/Users/pavel/AppData/Roaming/PolyMC',installedInstancePath=None,slavicMyths=dict(version='0.9.4',filename='slavicmyths-0.9.4.jar',sha256=None,installedPath=None,status='NOT_BUILT'),mods=entries)
(root/'packaging').mkdir(exist_ok=True);(root/'packaging/test-pack-lock.json').write_text(json.dumps(lock,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
for e in entries:print(e['id'],e['status'],e.get('size'),e.get('error',''))
