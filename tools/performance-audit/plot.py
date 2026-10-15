from pathlib import Path
import json,collections
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
R=Path(__file__).resolve().parents[2];O=R/'docs/implementation/performance-audit';d=json.loads((O/'metrics.json').read_text());g=collections.defaultdict(list)
for x in d:g[x['scene']].append(x)
fig,axes=plt.subplots(1,2,figsize=(12,5),layout='constrained');names=list(g)
for ax,key,label in [(axes[0],'tick_ms','Tick body mean, ms'),(axes[1],'frame_ms','Frame p99, ms')]:
 values=[[x[key]['mean' if key=='tick_ms' else 'p99'] for x in g[n]] for n in names];means=[sum(v)/len(v) for v in values]
 ax.barh(names,means,xerr=[[m-min(v) for m,v in zip(means,values)],[max(v)-m for m,v in zip(means,values)]],color='#386b94',capsize=3);ax.set_xlabel(label);ax.invert_yaxis();ax.grid(axis='x',alpha=.2);ax.set_axisbelow(True)
fig.suptitle('main fb2994e9 — short flat-world slices, not full fights\nBars: mean of 3 slices; whiskers: slice range; Poloz target excluded')
fig.savefig(O/'baseline.png',dpi=160);fig.savefig(O/'baseline.svg');print('baseline plot written')
