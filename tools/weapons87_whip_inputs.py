"""Exact archived/current source models and inherited right-to-left metadata fallback."""
import json,zipfile
import weapons87_geometry as g
def main():
 with zipfile.ZipFile(g.O/'before-whip180.zip') as z:
  old={'slavicmyths:'+p.split('/models/')[1][:-5]:json.loads(z.read(p)) for p in z.namelist() if '/models/' in p}
 rows=[]
 for pose in range(5):
  for context in ['firstperson_','thirdperson_']:
   for left in [False,True]:
    ctx=context+('lefthand' if left else 'righthand')
    for after in [False,True]:
     model=g.model('slavicmyths:item/poloz_whip_pose_'+str(pose),ctx,None if after else old);display=model.get('display',{});rows.append({'pose':pose,'context':ctx,'left':left,'after':after,'model':model,'display':display.get(ctx,display.get(context+'righthand',{}))})
 (g.O/'whip-native-inputs.json').write_text(json.dumps(rows,indent=2)+'\n');print('40 actual old/new/context/physical-hand whip transform inputs')
if __name__=='__main__':main()
