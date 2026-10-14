"""Write this patch's manifest and prepend a brief result to the current shared documents."""
from pathlib import Path
import hashlib,json,subprocess
R=Path(__file__).resolve().parents[1];O=R/'docs/implementation/runes-patch'
java=['rpg/Runes.java','rpg/RuneDefinition.java','rpg/RuneEffects.java','rpg/RuneRework.java','rpg/RpgEvents.java','rpg/RareRuneRuntime.java','rpg/AdvancedRuneRules.java','rpg/AdvancedRunes.java','item/ItemState.java','morozko/FrostCold.java','morozko/FrostEvents.java','rewards/RewardEvents.java','industry/IndustryAccessories.java','yaga/YagaServices.java','yaga/YagaMenu.java','client/YagaScreen.java','equipment/mixin/WardBlockMixin.java','poloz/PolozRewardEvents.java','gorynych/GorynychRewards.java']
paths=['src/main/java/org/slavicmyths/'+p for p in java]+['src/main/resources/assets/slavicmyths/lang/'+l+'.json' for l in ['ru_ru','en_us']]+['src/main/resources/data/slavicmyths/tags/entity_type/rune_death_immune.json']
for id in ['counterstrike_iii','hunter_iii','last_stand_iii','depths_iii','veles_oath_iv']:
 paths+=['src/main/resources/assets/slavicmyths/'+kind+'/item/rune_'+id+ext for kind,ext in [('models','.json'),('textures','.png')]]
 if id!='veles_oath_iv':paths+=['src/main/resources/data/slavicmyths/recipe/rune_rework_'+id+'.json']
paths+=['tools/rune-rework-tests/org/slavicmyths/verify/RuneReworkOffline.java','tools/rare-rune-tests/org/slavicmyths/verify/RareRuneOffline.java','tools/runes_patch_assets.py','tools/verify_runes_patch.py','tools/runes_patch_report.py']
for path in ['src/main/java/org/slavicmyths/client/RewardArmor.java','tools/rewards-offline/RewardModelChecks.java','tools/gorynych-tests/GorynychChecks.java']:
 before=subprocess.check_output(['git','show','b76160800223267abc04e7c6d46812dfba8976dc:'+path],cwd=R);after=(R/path).read_bytes();assert before.replace(b'\r\n',b'\n')==after.replace(b'\r\n',b'\n'),path
(O/'FILES.md').write_text('# Файлы патча рун\n\n'+f'Production/fixtures/tools: **{len(paths)}** файлов. Чужие исходники стартового HP-патча не включены.\n\n'+'\n'.join('- `'+p+'`' for p in paths)+'\n\nОбщие документы дополнены только текущим итогом: `docs/PROJECT_STATUS.md`, `docs/ROADMAP.md`, `docs/ARCHITECTURE.md`. Полный diff этих трёх файлов содержит и чужие незакоммиченные записи, поэтому целиком в index они не добавляются.\n\nСобственные отчёты: REPORT.md, FILES.md, REGRESSION_LIMITATIONS.md, MANUAL_CHECKS.md, SOURCE_CHECKS.json, RESOURCE_JAR_CHECKS.json. Evidence: BUILD/COMPILE/OFFLINE/REGRESSION/RELATED_REGRESSION/REMAINING_REGRESSION logs, предыдущие build logs, BASELINE_HASHES.json, START_STATUS.txt, ICONS.png, spec/*.md.\n',encoding='utf-8')
(O/'OWNED_HASHES.json').write_text(json.dumps({p:hashlib.sha256((R/p).read_bytes()).hexdigest() for p in paths},indent=2)+'\n',encoding='utf-8')
common='''## Патч рун — A–C (2026-10-14)

COMPLETE_WITH_LIMITATIONS: 37 прежних ID сохранены, 4 крафтовые III и
Велесова клятва IV у Яги; совместимость/старые дубликаты/legacy extraction,
death immunity, source-local морозная защита, 4-секундное Подавление,
числа старых рун и немедленный anvil refresh. Оружие/броня и чужой HP-патч
сохранены. Production версия1.9.7. Финальная clean build PASS; 390/20491/75064
основных offline assertions и15333 resource/JAR gates PASS. Earth6186,
Frost6946, Poloz4290 PASS. Два старых смежных fixture FAIL: RewardModelChecks
(cube counts8/8/7), GorynychChecks (ожидает старые3600/1800/180). Они не исправлены.
Client/server/GameTest/PolyMC0; игровые события/GUI/MP/relog/MSPT NOT_RUN.
Профили и миры не менялись. Исторические creative receipts неотличимы по
старому формату; новые помечены отдельно. Отчёт: docs/implementation/runes-patch/REPORT.md.
Ветка runs, собственные отчёты staged; commit/push не выполнялись.

'''
extra={
 'PROJECT_STATUS.md':'',
 'ROADMAP.md':'Реализация A/B/C завершена в пределах ZIP; игровая приёмка из MANUAL_CHECKS.md\nожидает отдельного разрешения. Новые milestone/редизайн экипировки не добавлены.\n\n',
 'ARCHITECTURE.md':'AdvancedRuneRules — pure константы/границы; AdvancedRunes — server Pre/Post,\nsource-identity pending и weak per-player stack-identity series; persisted RPG\ncooldowns, accepted-block bridge, AbstractArrow launch snapshot, общий capped\nBreakSpeed. Одна IV по порядку записи, вторичные Poloz hits исключены. YagaMenu\nсинхронизирует server claim gate; GorynychRewards добавляет только CreativeClaim.\nItemState codec сохраняет старые дубликаты, admission отдельно запрещает новые.\nКлиентский UI не загружается common/server кодом, JEI остаётся optional.\nНовых world scans, forced chunks, глобального player polling и библиотек нет.\n\n'
}
for file,suffix in extra.items():
 p=R/'docs'/file;prior=p.read_bytes();prefix=common+suffix
 if '## Патч рун — A–C (2026-10-14)' not in prior.decode('utf-8'):
  assert p.read_bytes()==prior,'Shared document changed during read: '+file
  p.write_bytes(prefix.encode('utf-8')+prior)
print(f'Owned manifest: {len(paths)} files; three shared documents prepended without replacing their contents')
