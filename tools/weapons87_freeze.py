"""Freeze completed local package evidence, never changes production or GitHub."""
from pathlib import Path
import argparse,hashlib,json,subprocess,zipfile
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/weapons87'
def main():
 p=argparse.ArgumentParser();p.add_argument('number',type=int);p.add_argument('scope');p.add_argument('folders',nargs='+');args=p.parse_args();n=args.number
 target=O/f'package{n}-receipt.json'
 if target.exists():raise RuntimeError('Preserve frozen receipt')
 checks=json.loads((O/'package-checks.json').read_text());assert checks.get('jar') and all(x['result']=='PASS' for x in checks['checks']);jar=Path(checks['jar']['path']);assert hashlib.sha256(jar.read_bytes()).hexdigest()==checks['jar']['sha256']
 log=(O/f'package{n}-build.log').read_text(encoding='utf-8',errors='replace');assert 'BUILD SUCCESSFUL' in log and 'FAILED' not in log
 manifest={path.relative_to(R).as_posix():hashlib.sha256(path.read_bytes()).hexdigest() for root in [R/'src',R/'tools'] for path in root.rglob('*') if path.is_file() and '__pycache__' not in path.parts};manifest['gradle.properties']=hashlib.sha256((R/'gradle.properties').read_bytes()).hexdigest();mf=O/f'source-manifest-package{n}.json';mf.write_text(json.dumps(manifest,indent=2)+'\n')
 checks.update({'scope':args.scope,'version':jar.stem.removeprefix('slavicmyths-'),'source_base_commit':subprocess.check_output(['git','rev-parse','HEAD'],cwd=R,text=True).strip(),'source_state':'UNCOMMITTED_WORKING_TREE','source_manifest_sha256':hashlib.sha256(mf.read_bytes()).hexdigest()});target.write_text(json.dumps(checks,indent=2)+'\n')
 with zipfile.ZipFile(O/f'package{n}-source-evidence.zip','w',zipfile.ZIP_DEFLATED) as z:
  files=[O/f'PACKAGE-{n}.md',target,mf,O/f'package{n}-build.log',O/f'package{n}-gates.log']
  for folder in args.folders:files+=list((O/folder).rglob('*')) if (O/folder).is_dir() else [O/folder]
  for path in files:
   if path.is_file():z.write(path,path.relative_to(O).as_posix())
 print(json.dumps(checks['jar']));print('Frozen uncommitted source manifest',len(manifest),'files; no installation or game launch')
if __name__=='__main__':main()
