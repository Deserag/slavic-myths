"""Hosted-only launcher for two real packaged-mod clients over a loopback TCP server."""
from pathlib import Path
import json,os,shutil,socket,subprocess,time
if os.environ.get('GITHUB_ACTIONS')!='true' or os.environ.get('RUNNER_ENVIRONMENT')!='github-hosted':
 raise SystemExit('Refusing client launch outside isolated GitHub-hosted VM')
root=Path.cwd();config=json.loads((root/'build/netherboss-client-launch.json').read_text());out=root/'build/netherboss-mp';out.mkdir(parents=True,exist_ok=True)
jar=next((root/'build/libs').glob('slavicmyths-*.jar'))
processes=[];handles=[]
def start(role):
 game=root/'work'/('netherboss-mp-'+role);(game/'mods').mkdir(parents=True,exist_ok=True);shutil.copy2(jar,game/'mods'/jar.name)
 frames=out/role;frames.mkdir(exist_ok=True)
 jvm=[]
 for argument in config['jvm']:
  if argument.startswith('@'):
   path=Path(argument[1:]);text=path.read_text().replace('-Xmx3G','-Xmx1800M')
   copy=out/(role+'-vm.txt');copy.write_text(text);jvm.append('@'+str(copy))
  else:jvm.append(argument)
 program=[]
 for argument in config['program']:
  if argument.startswith('@'):
   lines=Path(argument[1:]).read_text().splitlines()
   for i,line in enumerate(lines[:-1]):
    if line=='--width':lines[i+1]='960'
    if line=='--height':lines[i+1]='600'
    if line=='--uuid':lines[i+1]='00000000-0000-0000-0000-000000000000' if role=='host' else '00000000-0000-0000-0000-00000000000f'
   lines+=['--username','BossQAHost' if role=='host' else 'BossQAPeer']
   path=out/(role+'-program.txt');path.write_text('\n'.join(lines)+'\n');program.append('@'+str(path))
  else:program.append(argument)
 command=[config['java'],*jvm,'-Dhellvisual.mp=true','-Dhellvisual.peer='+str(role=='peer').lower(),'-Dhellvisual.output='+str(frames),'-cp',config['classpath'],config['mainClass'],*program]
 log=(out/(role+'.log')).open('w');handles.append(log)
 p=subprocess.Popen(command,cwd=game,stdout=log,stderr=subprocess.STDOUT,env={**os.environ,**config.get('environment',{})});processes.append(p);return p
try:
 host=start('host');deadline=time.monotonic()+600
 while True:
  if host.poll() is not None:raise RuntimeError('Host exited before loopback listener')
  try:
   with socket.create_connection(('127.0.0.1',25591),timeout=1):break
  except OSError:
   if time.monotonic()>deadline:raise TimeoutError('Host listener unavailable')
   time.sleep(2)
 peer=start('peer');deadline=time.monotonic()+45*60
 while host.poll() is None:
  if peer.poll() is not None:raise RuntimeError('Peer exited during the MP fights')
  if time.monotonic()>deadline:raise TimeoutError('Two-client QA exceeded45min')
  time.sleep(5)
 if host.returncode!=0:raise RuntimeError('Host client failed: '+str(host.returncode))
 peer.wait(timeout=120)
 if peer.returncode!=0:raise RuntimeError('Peer client failed: '+str(peer.returncode))
 host_result=json.loads((out/'host/multiplayer-fights.json').read_text());peer_result=json.loads((out/'peer/peer-finished.json').read_text())
 assert host_result['fights']==30,host_result
 assert peer_result['two_players_seen'] and peer_result['distinct_bosses_tracked']>=30,peer_result
 print('PASS actual two-client loopback MP and30diagnostic fights. Modified arena stats; not normal balance/owner acceptance.')
finally:
 for p in processes:
  if p.poll() is None:p.terminate()
 for log in handles:log.close()
