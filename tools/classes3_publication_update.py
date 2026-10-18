from pathlib import Path
R=Path(__file__).resolve().parents[1];W=Path(r'C:\Users\pavel\.codex\worktrees\classes-3-review\slavic-myths')
for root in [R,W]:
 for name in ['PROJECT_STATUS.md','ROADMAP.md','ARCHITECTURE.md']:
  p=root/'docs'/name;s=p.read_text(encoding='utf-8');s=s.replace('Изолированные dependent draft PR; main не merge-ился.','Код уже включён пользователем в main через PR#23/hot-fix b57771e4; эта задача main не merge-ила. QA followup основан на обновлённом main.');p.write_text(s,encoding='utf-8')
 p=root/'docs/implementation/classes3/REPORT.md';s=p.read_text(encoding='utf-8');s=s.replace('Версия не повышалась: review baseline 1.9.7.3, параллельная интегрированная рабочая копия 1.9.7.5.','Версия не повышалась: изолированные native прогоны на baseline 1.9.7.3, интегрированная рабочая копия 1.9.7.5. Во время работы пользователь создал hot-fix `b57771e4` со всем текущим snapshot, включая Classes3, и слил его через PR#23 (`fb2994e9`). Этот пользовательский commit/merge сохранён; итоговый QA followup основан на обновлённом main.');s=s.replace('Никакая установка в PolyMC/изменение пользовательских миров/merge в main не выполнялись.','Эта задача не устанавливала JAR в PolyMC, не меняла пользовательские миры и не выполняла merge в main; пользовательский PR#23 сохранён.');s=s.replace('Отдельные зависимые ветки A→B→C/D→UI/QA; промежуточные draft PR не объявлены автономно принятыми релизами.','Четыре локальных review-коммита A→B→C/D→UI/QA подготовлены до пользовательского merge: `1a9d2f4b`, `442e0dc8`, `68cfaf17`, `ab8b0fff`. Они не отправлялись как дублирующие PR, поскольку пользователь уже включил весь код через PR#23. Публикуется один актуальный followup с доказательствами/ограничениями QA; Issue19 остаётся открытым.');p.write_text(s,encoding='utf-8')
 text='''# Публикация Classes 3.0

- Реализация уже опубликована пользователем: [PR #23](https://github.com/Deserag/slavic-myths/pull/23), hot-fix `b57771e4e025a44a7e94079853d6fc1724b5bb76`, merge `fb2994e958911169317523e1066b8a1ca7a155af`.
- До этого подготовлены локальные review-коммиты: `1a9d2f4b` (XP), `442e0dc8` (passives/migration), `68cfaf17` (IV/trials), `ab8b0fff` (UI/assets/native QA). Их branches/stash сохранены; дублирующие PR поверх уже слитой реализации не создавались.
- Итоговый QA followup: ветка `codex/classes-3-final-qa`, основана на актуальном `origin/main`. Ссылка на PR добавляется после создания.
- Issue #19 / игровая приёмка остаются PARTIAL: 0 клиентов, реальный Survival pace/GUI/2-client MP/claims/DPS/TTK/MSPT/FPS/client relog NOT_RUN. Никакого автоматического merge followup, установки в PolyMC или изменения миров.
'''
 (root/'docs/implementation/classes3/PUBLICATION.md').write_text(text,encoding='utf-8')
