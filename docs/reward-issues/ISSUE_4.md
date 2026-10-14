# #4 — пламя после попадания

Отдельная ветка от main b7616080. В исходном коде огонь — один пакет
из36 окрашенных DustParticleOptions, а igniteForSeconds(2) относится к цели.
Контракт видимого пламени на30–40 тиков отсутствует. Визуальная before-длительность
в клиенте ещё не измерена; это статическая диагностика, не визуальная приёмка.

Добавлено одно сообщение flame_impact (28байт payload без framing).
Клиент поддерживает bounded local emitter40 тиков, затухание после24.
До48 FLAME частиц за событие, lifetime каждой ограничен оставшимся временем.
В воде используются короткие CLOUD частицы. Близкие события объединяются;
максимум32 emitters, очистка по deadline/смене мира/logout. При паузе
singleplayer время не истекает. Нет recurring server VFX packets и extra DoT.
Common/server не загружают client renderer/particle classes.

GameTest server19:33 MSK:2/2 PASS. Урон100→95, горение40 тиков,
fire-resistant цель остаётся100HP, повреждающее BreathCloud не создаётся.
Codec roundtrip PASS. Clean build Java21 PASS.

Клиентская видимость40 тиков, блок/вода/FPS и реальная доставка на dedicated
соединении PENDING. Срок и packet budget — кодовые границы, не измеренныйFPS.
Реальные запуски этапа:1 GameTest server,0 client; всего4 GameTest server.
Проверки: gradlew.bat clean build; gradlew.bat -I tools/reward-issues.init.gradle runRewardChecks.
