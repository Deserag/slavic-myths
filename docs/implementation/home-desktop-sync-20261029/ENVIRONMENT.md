# Изоляция 29.10.2026
Локально Windows10/Java21: только compile/offline/headless GameTest, безclient/PolyMC запуска и desktop automation.
Графическая попытка: GitHub-hosted ubuntu-latest VM, Xvfb1600×1000×24, Mesa software OpenGL, отдельный disposable flat creative world seed3492026. No local owner files or devices mounted; no remote control connection to owner desktop. Access token используется только для публикации/Actions черезgh, не передаётся тестовому клиенту.
Workflow: .github/workflows/hell-isolated-visual-qa.yml. Fixture guard вtools/hell-visual-qa.gradle иHellVisualCapture. Реальный JAR path/хэш проверяется вклиенте, законченная съёмка требуетfinished.json.
Наличие pipeline/compile PASS не означает successful runtime. Run receipt икадры проверяются отдельно.
