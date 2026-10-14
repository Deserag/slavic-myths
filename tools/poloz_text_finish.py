from pathlib import Path
import json
R=Path(__file__).resolve().parents[1]/'src/main/resources/assets/slavicmyths'
RU={'mor_horn_call':'Моровой рог гудит','mor_horn_charge':'Рог наполняется мором','poloz_distant_reply':'Полоз отвечает вдали','poloz_earth_rumble':'Земля содрогается','poloz_ambient_rumble':'Тяжёлая чешуя скользит','poloz_surface_roar':'Полоз пробуждается','poloz_move_crush':'Полоз крушит преграды','poloz_armor_deflect':'Чешуя отражает удар','poloz_weakspot_hit':'Полоз ревёт от боли','poloz_tail_slam':'Каменный хвост грохочет','poloz_mor_rain':'Моровые сгустки свистят','poloz_burrow':'Почва проседает','poloz_emerge':'Полоз вырывается из земли','poloz_phase2_cracks':'Моровая чешуя раскалывается','poloz_phase3_roar':'Полоз ревёт в ярости','poloz_minion_call':'Полоз зовёт порождения','poloz_death':'Полоз падает с рёвом'}
def finish():
 p=R/'lang/ru_ru.json';d=json.loads(p.read_text(encoding='utf-8'));d.update({'subtitles.slavicmyths.'+k:v for k,v in RU.items()});p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 for lang,title,description in [('ru_ru','Падение Великого Полоза','Предъявите Книге сказаний Сердцевину Полоза — разрушения остаются.'),('en_us','The Fall of Great Poloz','Show the Poloz Core to the Book of Tales. Destruction remains.')]:
  p=R/f'lang/{lang}.json';d=json.loads(p.read_text(encoding='utf-8'));d['advancement.slavicmyths.great_poloz_fall.title']=title;d['advancement.slavicmyths.great_poloz_fall.description']=description;p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 relics={'mor_dust':('Прах корневых моровых существ. Компонент рога; повторный призыв требует 4 праха.','Dust from root Mor creatures. A horn ingredient; repeat summons require four dust.'),'eye_of_mor':('Гарантированный трофей высшего морового Лиха. Компонент Морового рога.','Guaranteed trophy from Higher Mor Likho. An ingredient for the Mor Horn.'),'ashen_heart':('Гарантированный трофей высшего морового Овинника. Компонент Морового рога.','Guaranteed trophy from Higher Mor Ovinnik. An ingredient for the Mor Horn.'),'poloz_core':('Доказательство победы: прочитайте запись о Полозе в Книге сказаний. Предмет не расходуется.','Proof of victory: read the Poloz entry in the Book of Tales. The core is not consumed.'),'poloz_scale':('Трофей для коллекции Книги сказаний. Крафтового применения пока нет.','A trophy for the Book of Tales collection. It currently has no crafting use.')}
 for lang,index in [('ru_ru',0),('en_us',1)]:
  p=R/f'lang/{lang}.json';d=json.loads(p.read_text(encoding='utf-8'));d.update({'poloz.relic.'+key:pair[index] for key,pair in relics.items()});p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
if __name__=='__main__':finish()

# Preserve the user-authorized 1.9.1 summon rules after legacy text generation.
from summon_access_policy import apply as apply_summon_access
apply_summon_access()

from weapon_feedback_assets import apply as weapon_feedback
weapon_feedback()
