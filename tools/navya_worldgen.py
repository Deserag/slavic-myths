"""Final integration of this one biome; does not alter vanilla structure placements or old chunks."""
import json,zipfile
from pathlib import Path
from navya_assets import tag,write
R=Path(__file__).resolve().parents[1];D=R/'src/main/resources/data/slavicmyths'
def main():
 with zipfile.ZipFile(Path.home()/'.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar') as z:
  biome=json.loads(z.read('data/minecraft/worldgen/biome/nether_wastes.json'))
 biome['effects']={'fog_color':0x28222d,'sky_color':0x201b25,'water_color':0x53545d,'water_fog_color':0x28232d,
  'ambient_sound':'minecraft:ambient.soul_sand_valley.loop',
  'mood_sound':{'sound':'minecraft:ambient.soul_sand_valley.mood','tick_delay':8000,'block_search_extent':8,'offset':2},
  'particle':{'options':{'type':'minecraft:ash'},'probability':.0015}}
 # Keep the pinned vanilla ore/spring features, with geology before ores and
 # colour/vegetation after ores so netherrack ore targets remain applicable.
 while len(biome['features'])<10:biome['features'].append([])
 biome['features'][2]=['slavicmyths:navya_geology']
 biome['features'][9].append('slavicmyths:navya_nature')
 biome['spawners']['monster']=[{'type':'slavicmyths:navka','weight':6,'minCount':1,'maxCount':1},
  {'type':'slavicmyths:rootling','weight':30,'minCount':1,'maxCount':2},
  {'type':'slavicmyths:bone_crawler','weight':30,'minCount':1,'maxCount':2},
  {'type':'minecraft:zombified_piglin','weight':40,'minCount':2,'maxCount':4},
  {'type':'minecraft:piglin','weight':12,'minCount':1,'maxCount':2},
  {'type':'minecraft:ghast','weight':3,'minCount':1,'maxCount':1},
  {'type':'minecraft:enderman','weight':2,'minCount':1,'maxCount':1}]
 biome['spawners']['ambient']=[{'type':'slavicmyths:navya_wisp','weight':2,'minCount':1,'maxCount':1}]
 write(D/'worldgen/biome/navya_grove.json',biome)
 for feature in ['navya_geology','navya_nature']:
  write(D/'worldgen/configured_feature'/(feature+'.json'),{'type':'slavicmyths:'+feature,'config':{}})
  write(D/'worldgen/placed_feature'/(feature+'.json'),{'feature':'slavicmyths:'+feature,'placement':[{'type':'minecraft:biome'}]})
 write(D/'worldgen/structure/navya_heart.json',{'type':'slavicmyths:navya_heart','biomes':['slavicmyths:navya_grove'],'step':'surface_structures','spawn_overrides':{},'terrain_adaptation':'none'})
 write(D/'worldgen/structure_set/navya_heart.json',{'structures':[{'structure':'slavicmyths:navya_heart','weight':1}],
       'placement':{'type':'minecraft:random_spread','spacing':32,'separation':16,'salt':2022026,'frequency':.6}})
 tag('minecraft','worldgen/biome','is_nether',['slavicmyths:navya_grove'])
 for name in ['nether_fortress','bastion_remnant','ruined_portal_nether']:
  tag('minecraft','worldgen/biome','has_structure/'+name,['slavicmyths:navya_grove'])
 tag('minecraft','block','base_stone_nether',['slavicmyths:'+n for n in ['navya_stone','cracked_navya_stone','rooted_navya_stone']])
 print('One biome + 2 staged features + one rare Heart; no other biome or vanilla placement overrides')
if __name__=='__main__':main()
