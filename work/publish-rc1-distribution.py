from pathlib import Path
import subprocess,os,json,urllib.request,hashlib
os.environ['GIT_TERMINAL_PROMPT']='0';os.environ['GCM_INTERACTIVE']='never'
r=subprocess.run(['git','credential','fill'],input='protocol=https\nhost=github.com\n\n',text=True,capture_output=True,timeout=30);token=dict(l.split('=',1) for l in r.stdout.splitlines() if '=' in l)['password']
def api(url,data=None,method=None,mime='application/json'):
 headers={'Authorization':'Bearer '+token,'Accept':'application/vnd.github+json','User-Agent':'SlavicMythsPlaytest'}
 if data is not None:headers['Content-Type']=mime
 with urllib.request.urlopen(urllib.request.Request(url,data=data,headers=headers,method=method),timeout=90) as r:return json.load(r)
base='https://api.github.com/repos/Deserag/slavic-myths';release=api(base+'/releases/tags/v0.9.10-rc1');upload=release['upload_url'].split('{')[0]
receipt={'branch':'release/0.9.10-distribution','commit':'bca714d78c6939a866fa88abd0518277dcfba65f','files':9,'dependencyDownloadsVerified':6,'clientLaunches':0,'assets':[]}
paths=[Path('work/slavicmyths-0.9.10-rc1-player-pack.zip'),Path('work/slavicmyths-0.9.10-rc1.mrpack'),Path('work/rc1-distribution/REMAINING_WORK.md')]
for p in paths:
 assert not any(a['name']==p.name for a in release['assets'])
 content=p.read_bytes();asset=api(upload+'?name='+p.name,content,'POST','application/octet-stream');assert asset['size']==len(content)
 receipt['assets'].append({'name':p.name,'size':asset['size'],'sha256':hashlib.sha256(content).hexdigest(),'url':asset['browser_download_url']})
addition='''\n\n## Player distribution (packaging corrected)\n\nPlayer-only branch: https://github.com/Deserag/slavic-myths/tree/release/0.9.10-distribution\n\nDownload the player-pack ZIP and run DOWNLOAD_MODS.ps1 to obtain the six pinned dependencies (verified hashes), or import the attached .mrpack into a compatible launcher. The distribution branch includes the playable Slavic Myths JAR and instructions, without sources, Gradle, configs or worlds.\n\nDetailed unfinished work: https://github.com/Deserag/slavic-myths/blob/release/0.9.10-distribution/REMAINING_WORK.md\n\nCode fixes are implemented; real client/multiplayer/UI acceptance remains pending. Additional meal effects are not shown separately in the kitchen preview. The failed masonry-clearance regression was fixed in code but not rerun in Minecraft. Existing 16 door model parent issues remain. No Minecraft client was launched.\n'''
api(base+'/releases/'+str(release['id']),json.dumps({'body':release['body']+addition}).encode(),'PATCH')
tree=api(base+'/git/trees/'+receipt['commit']+'?recursive=1');files=[f['path'] for f in tree['tree'] if f['type']=='blob'];assert len(files)==9 and 'mods/slavicmyths-0.9.10-rc1.jar' in files
for a in receipt['assets']:
 h=hashlib.sha256()
 with urllib.request.urlopen(a['url'],timeout=60) as r:
  while chunk:=r.read(1024*1024):h.update(chunk)
 assert h.hexdigest()==a['sha256']
receipt['publicDownloadsVerified']=True
Path('work/rc1-distribution-publication.json').write_text(json.dumps(receipt,indent=2)+'\n')
print('PUBLIC_DISTRIBUTION_PASS branch files=9; ZIP/MRPACK/report downloaded and hashes verified.')
