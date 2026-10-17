from pathlib import Path
import csv,json,gzip
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parents[2];O=R/'docs/implementation/performance-2.0'
if (O/'cube-metrics.json').exists():
 data=json.loads((O/'cube-metrics.json').read_text());labels=[];p99=[];cpu=[]
 for name in ['cube-B1','cube-A2','cube-A5','cube-B2']:
  v=data.get(name,{}).get('uncapped',{})
  if not v.get('eligible'):continue
  labels.append(name);p99.append(v['frame']['p99']);cpu.append(v['render_cpu_mean_ms'])
 if labels:
  fig,ax=plt.subplots(1,2,figsize=(9,3.3));ax[0].bar(labels,p99,color=['#2b8390' if 'B' in s else '#5973a5' for s in labels]);ax[0].set_ylabel('Frame p99 (ms)');ax[1].bar(labels,cpu,color=['#2b8390' if 'B' in s else '#5973a5' for s in labels]);ax[1].set_ylabel('Mean render CPU/frame (ms)');fig.suptitle('PR37: independent eligible runs; A=original, B=helper');fig.tight_layout();fig.savefig(O/'cube-comparison.png',dpi=160);plt.close(fig)
for d in O.glob('world-*'):
 if not (d/'ticks.csv').exists():continue
 t=list(csv.DictReader((d/'ticks.csv').open(encoding='utf-8-sig')))
 if not t:continue
 start=int(t[0]['epoch_ms']);x=[(int(v['epoch_ms'])-start)/60000 for v in t];heap=[int(v['heap'])/2**20 for v in t];mspt=[float(v['native_mspt']) for v in t]
 fig,ax=plt.subplots(2,1,figsize=(12,6),sharex=True);ax[0].plot(x,heap,lw=.5,color='#2b8390');ax[0].set_ylabel('Used heap (MiB), not retained');ax[1].plot(x,mspt,lw=.4,color='#5973a5');ax[1].axhline(50,color='#b34f45',ls='--',lw=.8);ax[1].set_yscale('symlog',linthresh=10);ax[1].set_ylabel('Native tick tally (ms), symlog');ax[1].set_xlabel('Minutes from first measured tick');last=None
 for v,minute in zip(t,x):
  if v['scene']!=last:
   last=v['scene'];ax[0].axvline(minute,color='#888888',lw=.35);ax[0].annotate(last,(minute,max(heap)),rotation=90,fontsize=6,va='top')
 ax[1].set_ylim(bottom=0)
 fig.suptitle(d.name+' — all raw ticks incl. warmup/setup, no filtering of spikes');fig.tight_layout();fig.savefig(d/'heap-mspt.png',dpi=160);plt.close(fig)
