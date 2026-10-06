"""Publish RC2 after source/tag pushes; verify downloadable artifacts without launching Minecraft."""
from pathlib import Path
import argparse,os,subprocess,json,hashlib,urllib.request,urllib.error
R=Path(__file__).resolve().parents[1];V='0.9.10-rc2';TAG='v'+V;BASE='https://api.github.com/repos/Deserag/slavic-myths'
os.environ['GIT_TERMINAL_PROMPT']='0';os.environ['GCM_INTERACTIVE']='never'
r=subprocess.run(['git','credential','fill'],input='protocol=https\nhost=github.com\n\n',text=True,capture_output=True,timeout=30);token=dict(l.split('=',1) for l in r.stdout.splitlines() if '=' in l).get('password');assert token,'GitHub auth unavailable'
def api(url,data=None,method=None,mime='application/json'):
 headers={'Authorization':'Bearer '+token,'Accept':'application/vnd.github+json','User-Agent':'SlavicMythsPlaytest'}
 if data is not None:headers['Content-Type']=mime
 with urllib.request.urlopen(urllib.request.Request(url,data=data,headers=headers,method=method),timeout=90) as response:return json.load(response)
repo=api(BASE);assert not repo['private'] and repo['permissions']['push']
api(BASE,json.dumps({'default_branch':'main'}).encode(),'PATCH')
try:
 api(BASE+'/releases/tags/'+TAG)
except urllib.error.HTTPError as e:
 if e.code!=404:raise
else:raise SystemExit('RC2 release already exists; refusing to overwrite.')
out=R/'release'/V
body=(out/'CHANGELOG_0.9.10-RC2.md').read_text(encoding='utf-8')+'\n\n'+(out/'KNOWN_ISSUES.md').read_text(encoding='utf-8')+'\n\n'+(out/'DEPENDENCIES.md').read_text(encoding='utf-8')
body+='\n\nServer gates: 9/9 workstation+hotfix tests and 27/27 regressions, clean build/resource checks passed. Client launches: 0. Download player-pack ZIP and run DOWNLOAD_MODS.ps1, or import the MRPACK in a compatible launcher. Player branch: https://github.com/Deserag/slavic-myths/tree/release/0.9.10-distribution . Full source: https://github.com/Deserag/slavic-myths/tree/main .\n'
release=api(BASE+'/releases',json.dumps({'tag_name':TAG,'target_commitish':'main','name':'Slavic Myths 0.9.10 RC2','body':body,'prerelease':True,'draft':False}).encode(),'POST')
receipt={'version':V,'url':release['html_url'],'prerelease':True,'defaultBranch':'main','assets':[]};report=R/'docs/verification/playtest-0.9.10-rc2';receipt_path=R/'work/playtest-rc2-publication.json';upload=release['upload_url'].split('{')[0]
files=['slavicmyths-'+V+'.jar','SHA256SUMS.txt','README_PLAYTEST.md','CHANGELOG_0.9.10-RC2.md','DEPENDENCIES.md','KNOWN_ISSUES.md','REMAINING_WORK.md','slavicmyths-'+V+'-player-pack.zip','slavicmyths-'+V+'.mrpack']
for name in files:
 content=(out/name).read_bytes();a=api(upload+'?name='+name,content,'POST','application/octet-stream');assert a['size']==len(content)
 receipt['assets'].append({'name':name,'url':a['browser_download_url'],'size':a['size'],'sha256':hashlib.sha256(content).hexdigest()});receipt_path.write_text(json.dumps(receipt,indent=2)+'\n');print('Uploaded '+name,flush=True)
for a in receipt['assets']:
 if not a['name'].endswith(('.jar','.zip','.mrpack')):continue
 h=hashlib.sha256()
 with urllib.request.urlopen(a['url'],timeout=60) as response:
  while chunk:=response.read(1024*1024):h.update(chunk)
 assert h.hexdigest()==a['sha256'];a['publicDownloadVerified']=True
receipt['publicArtifactDownloadsVerified']=True;receipt_path.write_text(json.dumps(receipt,indent=2)+'\n');print('PUBLIC_RC2_PASS '+receipt['url'],flush=True)
