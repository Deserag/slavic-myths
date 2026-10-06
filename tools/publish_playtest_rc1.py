"""Publish the authorized RC1 asset using an existing Git credential; never prints credentials."""
import argparse, subprocess, os, json, urllib.request, urllib.error
from pathlib import Path
REPO='Deserag/slavic-myths';TAG='v0.9.10-rc1';BASE='https://api.github.com/repos/'+REPO
p=argparse.ArgumentParser();p.add_argument('--check',action='store_true');args=p.parse_args()
os.environ['GIT_TERMINAL_PROMPT']='0';os.environ['GCM_INTERACTIVE']='never'
r=subprocess.run(['git','credential','fill'],input='protocol=https\nhost=github.com\n\n',text=True,capture_output=True,timeout=30)
credential=dict(line.split('=',1) for line in r.stdout.splitlines() if '=' in line)
token=credential.get('password')
if not token:raise SystemExit('GitHub authentication unavailable; no publication performed.')
def api(url,body=None,method=None,content_type='application/json'):
 headers={'Authorization':'Bearer '+token,'Accept':'application/vnd.github+json','User-Agent':'SlavicMythsPlaytest','X-GitHub-Api-Version':'2022-11-28'}
 if body is not None:headers['Content-Type']=content_type
 req=urllib.request.Request(url,data=body,headers=headers,method=method)
 with urllib.request.urlopen(req,timeout=90) as response:return json.load(response)
repo=api(BASE)
if not repo.get('permissions',{}).get('push'):raise SystemExit('No push permission.')
print('Repository='+repo['full_name']+' public='+str(not repo['private']))
if args.check:raise SystemExit(0)
try:
 existing=api(BASE+'/releases/tags/'+TAG)
except urllib.error.HTTPError as e:
 if e.code!=404:raise
 existing=None
if existing is not None:raise SystemExit('Release already exists; it was not overwritten.')
release_dir=Path('release/0.9.10-rc1')
body=(release_dir/'CHANGELOG_0.9.10-RC1.md').read_text(encoding='utf-8')+'\n\n'+(release_dir/'DEPENDENCIES.md').read_text(encoding='utf-8')+'\n\n'+(release_dir/'KNOWN_ISSUES.md').read_text(encoding='utf-8')
release=api(BASE+'/releases',json.dumps({'tag_name':TAG,'name':'Slavic Myths 0.9.10 RC1','target_commitish':'release/0.9.10-playtest','body':body,'prerelease':True,'draft':False}).encode(),method='POST')
receipt={'url':release['html_url'],'id':release['id'],'tag':TAG,'prerelease':release['prerelease'],'assets':[]}
receipt_path=Path('work/playtest-rc1-publication.json');receipt_path.write_text(json.dumps(receipt,indent=2)+'\n')
upload=release['upload_url'].split('{')[0]
for name in ['slavicmyths-0.9.10-rc1.jar','SHA256SUMS.txt','README_PLAYTEST.md','CHANGELOG_0.9.10-RC1.md','DEPENDENCIES.md','KNOWN_ISSUES.md']:
 content=(release_dir/name).read_bytes();asset=api(upload+'?name='+name,content,'POST','application/octet-stream')
 if asset['size']!=len(content):raise SystemExit('Upload size mismatch: '+name)
 receipt['assets'].append({'name':name,'size':asset['size'],'url':asset['browser_download_url']})
 receipt_path.write_text(json.dumps(receipt,indent=2)+'\n')
print('Published prerelease: '+receipt['url'])
print('Assets verified: '+str(len(receipt['assets'])))
