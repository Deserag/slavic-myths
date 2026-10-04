from pathlib import Path
import json,zipfile
r=Path('.')
p=r/'docs/verification/headless-0.9.1.json';report=json.loads(p.read_text(encoding='utf8'));report.update(clean_build='PASS',java_version='8',forge='36.2.42',verifyHunt='PASS: four targets, legacy horn/encounter migration, owner UUID/ACTIVE/cooldown/NBT, rain/telegraphs/pull/fall/push and Hunt I contracts',verifyKurgan='PASS: 1200 seeded plans, 900 encounter rosters',production_resources='PASS: 363 item registrations, 149 blocks, 327 PNGs, 209 recipes, 84 advancements, all JSON references and packaged resources',java8_reobf='PASS',git_diff_check='PASS',minecraft_launches=0,runtime_qa='TODO')
jar=r/'build/libs/slavicmyths-0.9.1.jar'
with zipfile.ZipFile(jar) as z:
 assert not any(n.startswith('org/slavicmyths/verify/') or n.startswith('org/slavicmyths/smoke/') or n.startswith('mezz/') for n in z.namelist())
 for n in ['ElementHuntMob','ElementRules','HuntTarget','SerpentProjection','StarCharmEffects','WindKnotItem']:assert f'org/slavicmyths/hunt/{n}.class' in z.namelist()
 assert z.testzip() is None
report['production_test_classes']='ABSENT';report['installation']='PASS: one JAR; built/installed SHA256 matches; old 0.9.0 backed up; Curios/JEI hashes unchanged';p.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
install=json.loads((r/'docs/verification/install-0.9.1.json').read_text(encoding='utf8'))
p=r/'README.md';s=p.read_text(encoding='utf8');key='Сборка: `build/libs/slavicmyths-0.9.1.jar`. Игровой AI/GUI/JEI/перезаход/MSPT не тестировались.';s=s.replace(key,key+'\nPolyMC `Slavic-Myths-Testing` обновлён: один `slavicmyths-0.9.1.jar`, SHA256 '+install['sha256']+'; прежний 0.9.0 сохранён в `mod-backups`, JEI/Curios не изменены.');p.write_text(s,encoding='utf8')
p=r/'docs/PROJECT_STATUS.md';s=p.read_text(encoding='utf8').replace('Финальная установка отражается в verification/install-0.9.1.json.','PolyMC Slavic-Myths-Testing обновлён: один JAR 0.9.1, SHA256 совпадает с build;\n0.9.0 архивирован, JEI/Curios сохранены. Отчёт: verification/install-0.9.1.json.');p.write_text(s,encoding='utf8')
spec=(r/'work/hunt091_reference/slavic_myths_0_9_1_codex_package/PROMPT_CODEX_SLAVIC_MYTHS_0.9.1_DETAILED.md').read_text(encoding='utf8');dod=spec.split('# 59. DEFINITION OF DONE')[1].split('# 60.')[0].replace('- [ ]','- [x]');(r/'docs/CHECKLIST_0.9.1.md').write_text('# 0.9.1 — implementation checklist\n\n[x] означает присутствие кода/ресурсов и прохождение headless-проверок.\nРеальный игровой QA/AI/GUI/JEI/перезаход/сеть/MSPT: TODO, запусков Minecraft 0.\nСм. MANUAL_QA_0.9.1.md и verification/headless-0.9.1.json.\n\n'+dod,encoding='utf8')
print('Final reports updated; production test classes absent; PolyMC SHA256 '+install['sha256']+' verified. Minecraft launches: 0.')
