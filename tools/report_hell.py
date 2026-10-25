"""Plot native base-column data at identical coordinates/scale; no game screenshots."""
import json
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1] / 'docs/implementation/hell-patch1'
before = json.loads((ROOT/'vanilla-columns.json').read_text())
after = json.loads((ROOT/'candidate-columns.json').read_text())
assert len(before) == len(after) == 20
uncached = json.loads((ROOT/'uncached-candidate-columns.json').read_text())
for old,new in zip(uncached,after):
    assert {k:v for k,v in old.items() if k!='baseColumnMillis'} == {k:v for k,v in new.items() if k!='baseColumnMillis'}
(ROOT/'cache-comparison.json').write_text(json.dumps({
    'geometry':'PASS: all native samples and metrics identical in 20 regions',
    'uncachedNativeColumnMillis':sum(r['baseColumnMillis'] for r in uncached),
    'cachedNativeColumnMillis':sum(r['baseColumnMillis'] for r in after),
    'fullChunkGenerationAndMSPT':'NOT_RUN'},indent=2)+'\n',encoding='utf-8')
rows = []
for old, new in zip(before, after):
    assert (old['seed'],old['x'],old['z']) == (new['seed'],new['x'],new['z'])
    assert old['height'] == 128 and new['height'] == 224
    assert old['columns'] == new['columns'] == 4096
    assert new['solidBottomColumns'] == new['solidRoofColumns'] == 4096
    rows.append({k:new[k] for k in ['seed','x','z']})
    for k in ['columnsWith48Air','maximumAirGap','seaSurfaceColumnsY31','largestBaseGroundComponentStep1']:
        rows[-1][k]={'vanilla':old[k],'hell':new[k]}
summary = {'scope':'Native base columns, no surfaces/features/structures/worlds',
           'seeds':5,'regionsPerSeed':4,'columnsPerVariant':81920,
           'regionsWithMore48BlockAirColumns':sum(b['columnsWith48Air']>a['columnsWith48Air'] for a,b in zip(before,after)),
           'regionsWithLargerBaseGroundComponent':sum(b['largestBaseGroundComponentStep1']>a['largestBaseGroundComponentStep1'] for a,b in zip(before,after)),
           'totals':{},'regions':rows}
for name, values in [('vanilla',before),('hell',after)]:
    summary['totals'][name]={k:sum(row[k] for row in values) for k in
        ['airBlocks','lavaBlocks','columnsWith48Air','seaSurfaceColumnsY31']}
    summary['totals'][name]['meanLargestBaseGroundComponentStep1']=sum(r['largestBaseGroundComponentStep1'] for r in values)/20
    summary['totals'][name]['rawNativeColumnMillis']=sum(r['baseColumnMillis'] for r in values)
(ROOT/'summary.json').write_text(json.dumps(summary,indent=2)+'\n',encoding='utf-8')
colors={'#':(108,68,67),'.':(25,22,29),'~':(247,104,25)}
for old,new in zip(before,after):
    if old['x']!=1024: continue
    canvas=Image.new('RGB',(500,770),(244,241,233));d=ImageDraw.Draw(canvas)
    d.text((12,8),'NATIVE BASE TERRAIN / NO SURFACES OR FEATURES',fill='black')
    d.text((12,26),f"seed={old['seed']} x=1024..1087 z=-992",fill='black')
    for i,(label,row) in enumerate([('VANILLA',old),('HELL PROTOTYPE',new)]):
        x=40+i*240; top=72
        d.text((x,52),label,fill='black')
        for dx, column in enumerate(row['sliceX1Y1_atZ32']):
            for y in range(224):
                color=colors[column[y]] if y<len(column) else (198,194,184)
                d.rectangle((x+dx*3,top+(223-y)*3,x+dx*3+2,top+(223-y)*3+2),fill=color)
        for y in [0,31,127,223]:
            d.text((x-27,top+(223-y)*3),str(y),fill='black')
    d.text((12,750),'Same Y and X scale; grey = outside vanilla noise range.',fill='black')
    canvas.save(ROOT/f"section-seed-{old['seed']}.png")
print(json.dumps({k:v for k,v in summary.items() if k!='regions'},indent=2))
