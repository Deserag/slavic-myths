"""Owner-approved 24.10.2026 loot; exclusive curse substitution and guaranteed boss materials."""
import json
from pathlib import Path
R=Path(__file__).resolve().parents[1]
D=R/'src/main/resources/data/slavicmyths/loot_table/entities'
def item(name,lo=1,hi=1,conditions=None):
 e={'type':'minecraft:item','name':'slavicmyths:'+name}
 if lo!=1 or hi!=1:e['functions']=[{'function':'minecraft:set_count','count':lo if lo==hi else {'type':'minecraft:uniform','min':lo,'max':hi}}]
 if conditions:e['conditions']=conditions
 return e
def pool(entry):return {'rolls':1,'entries':[entry]}
def save(name,pools):
 D.mkdir(parents=True,exist_ok=True);(D/(name+'.json')).write_text(json.dumps({'type':'minecraft:entity','pools':pools,'random_sequence':'slavicmyths:entities/'+name},indent=2)+'\n')
def main():
 chance={'condition':'minecraft:random_chance_with_enchanted_bonus','enchantment':'minecraft:looting','unenchanted_chance':.02,
         'enchanted_chance':{'type':'minecraft:linear','base':.03,'per_level_above_first':.01}}
 for mob,material in [('navka','navya_ash'),('rootling','living_root'),('bone_crawler','bone_thorn')]:
  souls={'type':'minecraft:alternatives','children':[item('soul_blight',conditions=[{'condition':'minecraft:random_chance','chance':.005}]),
         item('soul',conditions=[chance]),item('soul_fragment',1,3)]}
  save(mob,[pool(souls),pool(item(material,1,2))])
 branch=item('guardian_branch',2,5);branch['functions'].append({'function':'minecraft:enchanted_count_increase','enchantment':'minecraft:looting',
                          'count':{'type':'minecraft:uniform','min':0,'max':1},'limit':5})
 save('grove_guardian',[pool(item('grove_core')),pool(branch),pool(item('soul',2,3)),pool(item('soul_blight'))])
 save('navya_wisp',[])
 print('Owner-approved loot: three exclusive soul branches; four guaranteed guardian rewards; no wisp loot')
if __name__=='__main__':main()
