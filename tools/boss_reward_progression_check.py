"""Offline content and progression invariants; does not claim in-world damage verification."""
import json,re
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'src/main/java/org/slavicmyths'
ASSETS=ROOT/'src/main/resources/assets/slavicmyths'
def source(name):return (JAVA/name).read_text(encoding='utf-8')
def unique(pairs):
 out={}
 for key,value in pairs:
  assert key not in out,'duplicate key '+key
  out[key]=value
 return out
def main():
 sisters=source('sisters/BossRewardWeapons.java')
 peak=max(map(float,re.findall(r'([0-9]+)F',re.search(r'float damage=(.*?);',sisters).group(1))))
 shot=float(re.search(r'new NineMorShot\(server.level\(\),server,([0-9]+)\)',source('ninemor/NineMorRewardAbilities.java')).group(1))
 assert '22.0F, -3.0F' in source('ninemor/NineMorContent.java'),'highest axe must not retain an excessively slow 0.5 attack rate'
 assert peak<4*shot<peak*2,'highest spear volley must exceed one sister cast without an unbounded buff'
 cross=source('gorynych/CrossbowDamage.java')
 special=float(re.search(r'MIN_SPECIAL_DAMAGE=([0-9]+)',cross).group(1));regular=float(re.search(r'MIN_REGULAR_DAMAGE=([0-9]+)',cross).group(1))
 assert regular<special and 3*special>peak,'crossbow damage is per projectile, not per entire volley'
 assert 'CrossbowVolley.Toggle' in source('client/WeaponSkillInput.java')
 assert 'CrossbowVolley.Toggle' not in source('client/WeaponHands.java'),'legacy crossbow input must not duplicate the unified handler'
 assert 'SerpentCrossbow bow && bow.special' in source('combat/WeaponSkillBindings.java')
 assert 'earthRift(server)' in source('poloz/PolozWhip.java') and 'p.isShiftKeyDown()?240:360' in source('poloz/PolozWhip.java')
 combo=source('rewards/KalinSabreCombo.java');assert 'DamageTypes.PLAYER_ATTACK' in combo and 'RareAttackCharge' in combo and 'getNewDamage()<=0' in combo
 hammer=source('sisters/GneteyaGreatHammer.java')
 assert hammer.count('getOffhandItem().isEmpty()')>=3,'both normal attacks and casts require two hands'
 assert 'getUseDuration(stack,p)-remaining<20' in hammer and 'PathData.data(p).putLong' in hammer
 assert 'GneteyaGreatHammer' in source('rpg/Runes.java')
 recipe=json.loads((ROOT/'src/main/resources/data/slavicmyths/recipe/gneteya_great_hammer.json').read_text())
 assert recipe['result']['id']=='slavicmyths:gneteya_great_hammer' and recipe['key']['G']['item']=='slavicmyths:gneteya_ingot'
 model=json.loads((ASSETS/'models/item/gneteya_great_hammer.json').read_text())
 assert model.get('elements'),'new hammer must have real 3D geometry'
 for element in model['elements']:
  assert all(-16<=a<b<=32 for a,b in zip(element['from'],element['to'])),'invalid model bounds'
 for texture in model['textures'].values():
  if texture.startswith('slavicmyths:'):assert (ASSETS/('textures/'+texture.split(':',1)[1]+'.png')).is_file()
 assert 'gneteya_great_hammer\tweapons\t' in (ASSETS/'creative_catalog.tsv').read_text()
 for code in ['ru_ru','en_us']:
  lang=json.loads((ASSETS/f'lang/{code}.json').read_text(encoding='utf-8'),object_pairs_hook=unique)
  assert lang['item.slavicmyths.gneteya_great_hammer']
  for key in re.findall(r'Component.translatable\("([^"\n]+)"',hammer):assert key in lang,key
  for suffix in ['frost','mor','anchor','abyss','pool','trident']:assert 'tooltip.slavicmyths.boss_stats.'+suffix in lang
 print('PASS: upper-tier volley budgets, crossbow controls, whip second skill, melee combo guards, two-handed restrictions, recipe, 3D model, textures, creative catalog and bilingual descriptions')
if __name__=='__main__':main()
