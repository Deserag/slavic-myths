"""Resolve actual current and archived FP model transforms for native offline projection."""
import json,zipfile
import weapons87_geometry as g
def main():
 before={}
 with zipfile.ZipFile(g.O/'before-grip-source.zip') as z:
  for name in z.namelist():
   marker='src/main/resources/assets/slavicmyths/models/'
   if marker in name and name.endswith('.json'):before['slavicmyths:'+name.split(marker)[1][:-5]]=json.loads(z.read(name))
 rows=[]
 for entry in json.loads((g.O/'audit-68.json').read_text(encoding='utf-8')):
  id=entry['id'];custom='crossbow' if id in ['serpent_crossbow','three_breath_crossbow'] else 'scythe' if id=='karachun_scythe' else 'pair' if id=='tugarin_sword' else 'standard'
  for left in [False,True]:
   ctx='firstperson_'+('lefthand' if left else 'righthand');new=g.model('slavicmyths:item/'+id,ctx);old=g.model('slavicmyths:item/'+id,ctx,before)
   def display(d):return d.get('display',{}).get(ctx,d.get('display',{}).get(ctx.replace('lefthand','righthand'),{}))
   row={'id':id,'left':left,'custom':custom,'before':display(old),'after':display(new)};rows.append(row)
   if custom in ['scythe','pair']:rows.append(row|{'custom':'standard','slot':'offhand_without_primary_custom_path'})
 (g.O/'fp-inputs.json').write_text(json.dumps(rows,indent=2)+'\n');print('140 actual before/current display inputs; 4 custom render IDs and isolated offhand paths classified')
if __name__=='__main__':main()
