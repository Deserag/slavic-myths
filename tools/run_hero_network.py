"""Disposable local TCP QA; prepared Gradle runs only, never a PolyMC process."""
import json,os,subprocess,time,sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
RUN=ROOT/'work/hero-network'
attempt=sys.argv[1] if len(sys.argv)>1 else 'initial'
children=[];files=[]
def launch(name):
    spec=json.loads((RUN/(name+'.json')).read_text(encoding='utf-8'))
    cwd=Path(spec['directory']).resolve();assert cwd.is_relative_to(RUN.resolve())
    env=os.environ.copy();env.update(spec['environment'])
    log=RUN/(attempt+'-'+name+'.log');stream=log.open('wb');files.append(stream)
    process=subprocess.Popen(spec['command'],cwd=cwd,env=env,stdin=subprocess.PIPE,stdout=stream,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW)
    children.append((name,process));print('START',name,process.pid,flush=True);return process,log
try:
    server,log=launch('CoopServer');started=time.monotonic()
    while server.poll() is None and 'Done (' not in log.read_text(encoding='utf-8',errors='replace'):
        if time.monotonic()-started>90:raise TimeoutError('Server startup')
        time.sleep(1)
    assert server.poll() is None,'Dedicated server exited during startup'
    launch('CoopA');launch('CoopB')
    while any(p.poll() is None for _,p in children):
        if time.monotonic()-started>300:raise TimeoutError('Network QA deadline')
        time.sleep(1)
    print('EXIT',[(n,p.returncode) for n,p in children],flush=True)
finally:
    for name,p in children:
        if p.poll() is None and name=='CoopServer':
            try:p.stdin.write(b'stop\n');p.stdin.flush();p.wait(timeout=10)
            except (OSError,subprocess.TimeoutExpired):pass
    for name,p in children:
        if p.poll() is None:p.terminate();p.wait(timeout=10)
    for stream in files:stream.close()
