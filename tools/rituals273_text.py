"""Scoped current ritual instructions, shared by the historical asset generators."""
import json
from pathlib import Path
R=Path(__file__).resolve().parents[1]
TEXT={
 'sanctuary193.activation_failed':('Ритуал не активирован: состояние блока изменилось или чаша недоступна. Предмет не израсходован.','The ritual could not activate: the block changed or is unavailable. Your item was not consumed.'),
 'sanctuary193.sealed':('Восстанови печать у края большой чаши Священным углём из обычных сундуков святилища. Затем вернись к восточному залу хранителя.','Use Sacred Ember from the sanctuary chests on the seal at the edge of the great brazier, then return to the eastern guardian hall.'),
 'sanctuary193.sealed_spring':('Очисти багровые камни двух загрязнённых бассейнов в западном зале: ПКМ очищающим порошком или разбей оба камня. Затем вернись к северо-восточному залу хранительницы.','Cleanse the crimson nodes of both tainted pools in the western hall: use cleansing powder or break both nodes. Then return to the northeastern keeper hall.'),
 'sanctuary193.ember_needed':('Нужен Священный уголь из обычных сундуков святилища; один гарантирован в складе на северо-западе. Примени его ПКМ к печати у края большой чаши, а не к костру.','Find Sacred Ember in the sanctuary chests; the northwestern store guarantees one. Right-click the seal at the edge of the great brazier, rather than a campfire.'),
 'sanctuary193.fire_restored':('Огонь восстановлен. Открыты боковой тайник и печать восточного зала. Одолей хранителя, затем ПКМ ритуального очага в его зале и забери часть амулета с постамента.','The fire is restored. The side cache and eastern hall seal are open. Overcome the guardian, then touch the ritual hearth in its hall and claim your fragment at the pedestal.'),
 'sanctuary193.already_restored':('Огонь уже восстановлен. Иди в восточный зал хранителя; после боя очисти его ритуальным очагом и забери часть амулета с постамента. Повторный уголь не нужен.','The fire is already restored. Enter the eastern guardian hall; after combat, purify it at the ritual hearth and claim your fragment at the pedestal. No more embers are needed.'),
 'sanctuary193.powder_needed':('Примени один очищающий порошок или разбей этот багровый камень. Порошок: Пепел очищения + костная мука; пепел: Священный уголь + бумага. Очисти оба загрязнённых бассейна.','Use one cleansing powder or break this crimson node. Powder: Purifying Ash + bone meal; ash: Sacred Ember + paper. Cleanse both tainted pools.'),
 'sanctuary193.pool_clean':('Этот бассейн чист. Если второй ещё загрязнён, очисти и его; затем иди в северо-восточный зал хранительницы.','This pool is clean. Cleanse the other tainted pool if needed, then enter the northeastern keeper hall.'),
 'sanctuary193.spring_restored':('Оба бассейна чисты. Открыты тайник и северо-восточный зал. Одолей хранительницу, затем ПКМ чистого источника в её зале и забери часть амулета с постамента.','Both pools are clean. The cache and northeastern hall are open. Overcome the keeper, then touch the clean spring in her hall and claim your fragment at the pedestal.'),
 'sanctuary193.spring_hint':('В западном зале три бассейна, два загрязнены. Очисти их багровые камни порошком или разбей их. После боя коснись чистого источника, чтобы завершить очищение хранительницы.','The western hall has three pools, two tainted. Use powder on their crimson nodes or break them. After combat, touch the clean spring to finish purifying the keeper.'),
 'item.slavicmyths.sacred_ember.desc':('Из сундуков огненного святилища. ПКМ печати у края большой чаши восстанавливает огонь один раз. Также компонент Пепла очищения.','Found in fire sanctuary chests. Right-click the seal at the great brazier edge to restore its fire once. Also used to craft Purifying Ash.'),
 'guardians194.open_fire_hint':('Священным углём из сундуков восстанови печать у края большой чаши, затем вернись в этот зал.','Restore the great brazier seal with Sacred Ember from the chests, then return to this hall.'),
 'guardians194.open_water_hint':('Очисти два багровых камня загрязнённых бассейнов западного зала порошком или киркой.','Cleanse both crimson nodes in the western tainted pools with powder or a pickaxe.'),
 'guardians194.purify_fire_hint':('Хранитель обессилен и не должен погибнуть. ПКМ ритуального очага в северо-западном углу его зала завершит очищение; затем ПКМ постамента.','The guardian is subdued and survives. Touch the ritual hearth in the northwest of its hall to purify it, then right-click the pedestal.'),
 'guardians194.purify_water_hint':('Хранительница ждёт очищения. ПКМ чистого источника в её зале завершит обряд; затем ПКМ постамента у северной стены.','The keeper awaits purification. Touch the clean spring in her hall to finish the ritual, then right-click the pedestal by the north wall.'),
}
LORE={
 'sanctuary_unfading_fire':('В северо-западном складе есть сундук со Священным углём. Примени уголь к печати у края большой чаши во дворе. Откроются тайник и восточный зал. Одолей стража до его обессиливания; он не погибает. Коснись ритуального очага в зале, дождись очищения и забери личную Пламенную часть амулета с постамента.','The northwestern store has a chest with Sacred Ember. Apply it to the seal at the courtyard brazier edge to open the cache and eastern hall. Subdue the guardian; it survives. Touch the hall ritual hearth, wait for purification, and claim your personal Flame Fragment at the pedestal.'),
 'abode_pure_spring':('В западном зале три бассейна: два загрязнены. Примени очищающий порошок к двум багровым камням или разбей их киркой. Порошок делают из Пепла очищения и костной муки; пепел — из Священного угля и бумаги. Без порошка путь тоже доступен. После очищения откроется северо-восточный зал. Одолей хранительницу, перекрывая два затвора во время её лечения, затем коснись чистого источника в её зале и забери Водную часть амулета с постамента.','The western hall has three pools, two tainted. Apply cleansing powder to both crimson nodes or break them with a pickaxe. Craft powder from Purifying Ash and bone meal, and ash from Sacred Ember and paper; powder is optional. Cleansing opens the northeastern hall. Subdue the keeper, close both valves during her healing, then touch the clean spring in her hall and claim the Water Fragment at the pedestal.'),
 'mor_guardian_fragments':('Два хранителя дают две разные части одного оберега. После очищения каждый игрок получает свою часть на постаменте, один раз на тип; утраченные части повторно не выдаются. Соедини Пламенную часть, Водную часть и Око Мора на верстаке — рецепт Оберега от Хвори уже доступен.','The two guardians offer matching halves of one ward. After purification, each player can claim one fragment of each type at its pedestal; lost fragments cannot be reclaimed. Combine the Flame Fragment, Water Fragment and Eye of Mor at a crafting table: the Blight Ward recipe is available now.'),
}
def expected(data,index):
 out={k:v[index] for k,v in TEXT.items()}
 for name,body in LORE.items():
  for key in data:
   if key.startswith('tales.entry.'+name+'.') and not key.endswith('.title'):out[key]=body[index]
 for water in [False,True]:
  fragment='mor_water_amulet_fragment' if water else 'mor_fire_amulet_fragment'
  out['item.slavicmyths.'+fragment+'.desc']=LORE['mor_guardian_fragments'][index]
  key='advancements.slavicmyths.claimed_'+('water' if water else 'fire')+'_fragment.description'
  out[key]=('Личная часть оберега получена. Две части и Око Мора можно соединить на верстаке.' if index==0 else 'Your personal ward fragment is claimed. Craft both fragments with Eye of Mor at a crafting table.')
 return out
def apply():
 for index,locale in enumerate(['ru_ru','en_us']):
  path=R/'src/main/resources/assets/slavicmyths/lang'/f'{locale}.json';data=json.loads(path.read_text(encoding='utf-8'));data.update(expected(data,index));path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
if __name__=='__main__':apply()
