"""Resolve actual archived/current spear geometry/displays for native ItemTransform export."""
import json,zipfile
import weapons87_geometry as g
IDS=['ancient_spear','restored_spear','spear','gold_spear','iron_spear','silver_spear','diamond_spear','perunite_spear','netherite_spear']
def main():
 before={}
 with zipfile.ZipFile(g.O/'before-spear-family.zip') as z:
  for n in z.namelist():
   key='src/main/resources/assets/slavicmyths/models/'
   if key in n and n.endswith('.json'):before['slavicmyths:'+n.split(key)[1][:-5]]=json.loads(z.read(n))
 rows=[]
 for id in IDS:
  for left in [False,True]:
   for context in ['thirdperson_','firstperson_']:
    ctx=context+('lefthand' if left else 'righthand')
    for after in [False,True]:
     model=g.model('slavicmyths:item/'+id,ctx,None if after else before);rows.append({'id':id,'left':left,'context':ctx,'after':after,'model':model,'display':model.get('display',{}).get(ctx,{})})
 (g.O/'spear-native-inputs.json').write_text(json.dumps(rows,indent=2)+'\n');print('72 actual before/after/context/hand inputs,9 IDs')
if __name__=='__main__':main()
