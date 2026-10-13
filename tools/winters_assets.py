"""Reproduce only 1.9.7 resources; supplied pixel sprites, vanilla audio."""
from pathlib import Path
from PIL import Image
import json,shutil
R=Path(__file__).resolve().parents[1];A=R/'src/main/resources/assets/slavicmyths';D=R/'src/main/resources/data/slavicmyths'
def write(p,value):
 p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def main():
 for name,ref in [('amulet_two_winters','04_amulet_neutral_32.png'),('amulet_two_winters_morozko','05_amulet_morozko_32.png'),('amulet_two_winters_karachun','06_amulet_karachun_32.png')]:
  source=R/'docs/specs/1.9.7/references'/ref;im=Image.open(source)
  assert im.size==(32,32) and im.mode=='RGBA' and im.getextrema()[3][0]==0
  dest=A/'textures/item'/f'{name}.png';dest.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(source,dest)
  model={'parent':'minecraft:item/generated','textures':{'layer0':f'slavicmyths:item/{name}'}}
  if name=='amulet_two_winters':model['overrides']=[{'predicate':{'slavicmyths:winter_mode':1},'model':'slavicmyths:item/amulet_two_winters_morozko'},{'predicate':{'slavicmyths:winter_mode':2},'model':'slavicmyths:item/amulet_two_winters_karachun'}]
  write(A/'models/item'/f'{name}.json',model)
 for recipe,category in [('original','equipment'),('copy','misc')]:
  write(D/'recipe'/f'two_winters_{recipe}.json',{'type':'slavicmyths:two_winters','category':category})
 write(D/'tags/entity_type/two_winters_nether_enemies.json',{'replace':False,'values':['minecraft:'+s for s in ['blaze','ghast','magma_cube','wither_skeleton','hoglin','piglin_brute']]})
 # Explicit known bosses; health is also a conservative fallback for third-party bosses.
 registered=set()
 import re
 for p in (R/'src/main/java').rglob('*.java'):
  registered.update(re.findall(r'ENTITIES\.register\("([^"]+)"',p.read_text(encoding='utf-8')))
 candidates=['great_poloz','morozko','karachun','zmey_gorynych','extinguished_flame_guardian','defiled_spring_keeper','idolishche_poganoe','marinka','skim_beast','kartaus','kalin_tsar','nightingale','unresting_prince','ovinnik','volkolak','fire_serpent','podvey','likho_one_eyed','tugarin_zmey']
 write(D/'tags/entity_type/two_winters_bosses.json',{'replace':False,'values':['minecraft:wither','minecraft:ender_dragon']+['slavicmyths:'+s for s in candidates if s in registered]})
 ru={'item.slavicmyths.amulet_two_winters':'Оберег двух зим','effect.slavicmyths.nether_aura':'Адская слабость',
 'original':'Оригинал-шаблон: сохраняется при копировании на верстаке','copy':'Оберег: не является шаблоном для копирования',
 'controls':'Shift + ПКМ: режим · ПКМ: способность (только Нижний мир)','passive':'В основном инвентаре: пассивы режима и защита от адской ауры',
 'mode_query':'ПКМ сообщает оставшееся время общей перезарядки','mode':'Режим: %s','morozko':'Морозко','karachun':'Карачун','nether_only':'Способность доступна только в Нижнем мире','cooldown':'Оберег восстанавливается: %s с',
 'shield':'Ледяной покров: 10 здоровья, до 8 с','wave':'Объятия Карачуна',
 'aura_0':'Адская аура нарастает. Найдите Оберег двух зим','aura_1':'Адская аура: Слабость I','aura_2':'Адская аура: Слабость II, урон каждые 4 с','aura_3':'Адская аура: Слабость II, урон каждые 3 с'}
 en={'item.slavicmyths.amulet_two_winters':'Amulet of Two Winters','effect.slavicmyths.nether_aura':'Nether Weakness',
 'original':'Original template: retained when copied at a crafting table','copy':'Amulet: cannot serve as a copying template',
 'controls':'Sneak + RMB: switch · RMB: ability (Nether only)','passive':'In main inventory: mode passives and protection from Nether aura',
 'mode_query':'RMB reports the remaining shared cooldown','mode':'Mode: %s','morozko':'Morozko','karachun':'Karachun','nether_only':'This ability requires the Nether','cooldown':'Amulet recovers in %s s',
 'shield':'Ice Mantle: 10 health, up to 8 s','wave':"Karachun's Embrace",
 'aura_0':'The Nether aura gathers. Find an Amulet of Two Winters','aura_1':'Nether aura: Weakness I','aura_2':'Nether aura: Weakness II, damage every 4 s','aura_3':'Nether aura: Weakness II, damage every 3 s'}
 for lang,strings in [('ru_ru',ru),('en_us',en)]:
  p=A/'lang'/f'{lang}.json';data=json.loads(p.read_text(encoding='utf-8'));data.update({k if '.' in k else 'winters.slavicmyths.'+k:v for k,v in strings.items()});write(p,data)
 out=R/'docs/verification/winters-1.9.7';out.mkdir(parents=True,exist_ok=True)
 (out/'ASSETS.md').write_text('## Assets 1.9.7\n\nThree unchanged 32×32 RGBA pixel sprites supplied by the user in the task archive. No poster textures are packaged. Flat generated item models use native vanilla transforms. Sounds use vanilla Minecraft events; no external OGG or new audio licenses. Cold wave: twelve local dust particles over ten ticks; shield: six snowflakes per second; aura: two dust particles every five seconds.\n',encoding='utf-8')
if __name__=='__main__':main()
