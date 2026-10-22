"""P43 canonical additive polearm repair, one-branch workflow (no registry/gameplay edits)."""
from pathlib import Path
import argparse,json
from weapons87_grips import plan
ROOT=Path(__file__).resolve().parents[1]
MODELS=ROOT/'src/main/resources/assets/slavicmyths/models/item'
BASE={'thunder_spear':30,'berdysh':21,'carved_staff':11}
ADDITIONS=json.loads("{\"thunder_spear\":[{\"from\":[6.82,18.84,6.82],\"to\":[9.18,19.19,9.18],\"faces\":{\"north\":{\"texture\":\"#body\",\"uv\":[12,8,16,12]},\"south\":{\"texture\":\"#body\",\"uv\":[12,8,16,12]},\"east\":{\"texture\":\"#body\",\"uv\":[12,8,16,12]},\"west\":{\"texture\":\"#body\",\"uv\":[12,8,16,12]},\"up\":{\"texture\":\"#body\",\"uv\":[12,8,16,12]},\"down\":{\"texture\":\"#body\",\"uv\":[12,8,16,12]}}},{\"from\":[6.78,20.05,6.78],\"to\":[9.22,20.39,9.22],\"faces\":{\"north\":{\"texture\":\"#body\",\"uv\":[12,8,16,12]},\"south\":{\"texture\":\"#body\",\"uv\":[12,8,16,12]},\"east\":{\"texture\":\"#body\",\"uv\":[12,8,16,12]},\"west\":{\"texture\":\"#body\",\"uv\":[12,8,16,12]},\"up\":{\"texture\":\"#body\",\"uv\":[12,8,16,12]},\"down\":{\"texture\":\"#body\",\"uv\":[12,8,16,12]}}},{\"from\":[7.9,22,8.4],\"to\":[8.200000000000001,23,8.7],\"faces\":{\"north\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]},\"south\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]},\"east\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]},\"west\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]},\"up\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]},\"down\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]}}},{\"from\":[8.2,23,8.4],\"to\":[8.5,24,8.7],\"faces\":{\"north\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]},\"south\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]},\"east\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]},\"west\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]},\"up\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]},\"down\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]}}},{\"from\":[8.1,24,8.4],\"to\":[8.4,25,8.7],\"faces\":{\"north\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]},\"south\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]},\"east\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]},\"west\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]},\"up\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]},\"down\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]}}},{\"from\":[7.8,25,8.4],\"to\":[8.1,26,8.7],\"faces\":{\"north\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]},\"south\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]},\"east\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]},\"west\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]},\"up\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]},\"down\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]}}},{\"from\":[7.9,26,8.4],\"to\":[8.200000000000001,27,8.7],\"faces\":{\"north\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]},\"south\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]},\"east\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]},\"west\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]},\"up\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]},\"down\":{\"texture\":\"#body\",\"uv\":[4,4,8,8]}}}],\"berdysh\":[{\"from\":[6.65,19.15,7.2],\"to\":[8.65,19.55,8.8],\"faces\":{\"north\":{\"texture\":\"#body\",\"uv\":[8,0,12,4]},\"south\":{\"texture\":\"#body\",\"uv\":[8,0,12,4]},\"east\":{\"texture\":\"#body\",\"uv\":[8,0,12,4]},\"west\":{\"texture\":\"#body\",\"uv\":[8,0,12,4]},\"up\":{\"texture\":\"#body\",\"uv\":[8,0,12,4]},\"down\":{\"texture\":\"#body\",\"uv\":[8,0,12,4]}}},{\"from\":[7,26.6,7.2],\"to\":[8.7,27.05,8.8],\"faces\":{\"north\":{\"texture\":\"#body\",\"uv\":[8,0,12,4]},\"south\":{\"texture\":\"#body\",\"uv\":[8,0,12,4]},\"east\":{\"texture\":\"#body\",\"uv\":[8,0,12,4]},\"west\":{\"texture\":\"#body\",\"uv\":[8,0,12,4]},\"up\":{\"texture\":\"#body\",\"uv\":[8,0,12,4]},\"down\":{\"texture\":\"#body\",\"uv\":[8,0,12,4]}}}],\"carved_staff\":[{\"from\":[6.7,16.7,6.7],\"to\":[9.3,17.1,9.3],\"faces\":{\"north\":{\"texture\":\"#binding\",\"uv\":[0,0,16,16]},\"south\":{\"texture\":\"#binding\",\"uv\":[0,0,16,16]},\"east\":{\"texture\":\"#binding\",\"uv\":[0,0,16,16]},\"west\":{\"texture\":\"#binding\",\"uv\":[0,0,16,16]},\"up\":{\"texture\":\"#binding\",\"uv\":[0,0,16,16]},\"down\":{\"texture\":\"#binding\",\"uv\":[0,0,16,16]}}},{\"from\":[6.7,18.4,6.7],\"to\":[9.3,18.8,9.3],\"faces\":{\"north\":{\"texture\":\"#binding\",\"uv\":[0,0,16,16]},\"south\":{\"texture\":\"#binding\",\"uv\":[0,0,16,16]},\"east\":{\"texture\":\"#binding\",\"uv\":[0,0,16,16]},\"west\":{\"texture\":\"#binding\",\"uv\":[0,0,16,16]},\"up\":{\"texture\":\"#binding\",\"uv\":[0,0,16,16]},\"down\":{\"texture\":\"#binding\",\"uv\":[0,0,16,16]}}},{\"from\":[6.75,20.1,6.75],\"to\":[9.25,20.55,9.25],\"faces\":{\"north\":{\"texture\":\"#binding\",\"uv\":[0,0,16,16]},\"south\":{\"texture\":\"#binding\",\"uv\":[0,0,16,16]},\"east\":{\"texture\":\"#binding\",\"uv\":[0,0,16,16]},\"west\":{\"texture\":\"#binding\",\"uv\":[0,0,16,16]},\"up\":{\"texture\":\"#binding\",\"uv\":[0,0,16,16]},\"down\":{\"texture\":\"#binding\",\"uv\":[0,0,16,16]}}}]}")
def load(name):return json.loads((MODELS/(name+'.json')).read_text(encoding='utf-8'))
def check():
 for name,parts in ADDITIONS.items():
  data=load(name);elements=data['elements']
  assert len(elements)==BASE[name]+len(parts),(name,len(elements))
  assert elements[-len(parts):]==parts,name+' added component overwritten'
  assert len(set(json.dumps(e,sort_keys=True) for e in elements))==len(elements),name+' duplicated geometry'
  for e in parts:
   assert all(lo<hi for lo,hi in zip(e['from'],e['to'])),name
   for face in e['faces'].values():
    assert face['texture'][1:] in data['textures'],(name,face['texture'])
    if 'uv' in face:assert all(0<=p<=16 for p in face['uv'])
 carved=load('carved_staff')['display'];expected=plan()['carved_staff']['display']
 for key in ('thirdperson_righthand','thirdperson_lefthand'):
  assert carved[key]==expected[key],key+' corrected palm displaced'
  assert carved[key]['scale']==[.68,.68,.68]
 print('PASS P43: stable thunder spear, berdysh and carved-staff details, centered TP; runtime NOT_RUN')
def apply():
 for name,parts in ADDITIONS.items():
  data=load(name);elems=data['elements']
  if len(elems)==BASE[name]:
   elems.extend(parts)
   file=MODELS/(name+'.json')
   file.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
  else:
   assert len(elems)==BASE[name]+len(parts) and elems[-len(parts):]==parts,name+' unexpected geometry; owner review required'
 check()
if __name__=='__main__':
 parser=argparse.ArgumentParser();parser.add_argument('--apply',action='store_true')
 args=parser.parse_args()
 apply() if args.apply else check()
