"""Read-only measurement: real volume free space and exact retired path inventory."""
import json,shutil,subprocess,time
from pathlib import Path
B=Path('C:/Users/pavel/.codex/recovery/slavic-myths-20261018')
R=Path(__file__).resolve().parents[2]
before=json.loads((B/'FINAL_CLEANUP_BEFORE.json').read_text('utf-8-sig'))
safe=json.loads((B/'SAFE_CANDIDATES.json').read_text('utf-8-sig'))
retired=json.loads((B/'RETIRED_DATA.json').read_text('utf-8-sig'))
old=json.loads((B/'SIZES.json').read_text('utf-8-sig'))
sizes={str(Path(x['path'])).lower():x['directories']['']['bytes'] for x in old}
removed_safe=[{'path':x['path'],'bytes':x['bytes']} for x in safe if not Path(x['path']).exists()]
removed_trees=[]
for x in retired:
    if not Path(x['root']).exists():
        total=sizes[str(Path(x['root'])).lower()]
        prior=sum(p['bytes'] for p in safe if str(Path(p['root'])).lower()==str(Path(x['root'])).lower())
        removed_trees.append({'path':x['root'],'head':x['head'],'snapshot_bytes':total,'bytes_after_safe_cleanup':total-prior})
free={d:shutil.disk_usage(d+':/').free for d in ('C','D')}
net={d:free[d]-before['free'][d] for d in free}
new_archives=['retired-unique-data.zip','retired-unique-data-v2.zip','historical-tracked-artifacts.zip','publish-final-delta.zip','retired-local-work.bundle']
backup_bytes=sum((B/n).stat().st_size for n in new_archives if (B/n).exists())
result={'measured_at':time.time(),'before_free_bytes':before['free'],'after_free_bytes':free,'net_free_bytes_including_new_archives':net,
 'new_unique_archives_bytes':backup_bytes,'safe_directories_removed':removed_safe,'worktrees_removed':removed_trees,
 'worktrees_remaining':subprocess.check_output(['git','worktree','list','--porcelain'],cwd=R).decode('utf8'),
 'publish_clone':'KEPT: automatic approval review rejected recursive removal; verified full archive exists',
 'history_rewritten':False,'minecraft_launches':0}
(B/'FINAL_LOCAL_RESULTS.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n','utf8')
print(json.dumps({'safe_removed':len(removed_safe),'worktrees_removed':len(removed_trees),'net_free_GiB':{d:round(n/2**30,3) for d,n in net.items()},'new_unique_archive_MiB':round(backup_bytes/2**20,3)}))
