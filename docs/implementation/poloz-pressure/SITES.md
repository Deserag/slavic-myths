# Проверенные пути источников

Baseline: main `fb2994e958911169317523e1066b8a1ca7a155af`,1.9.7.5. Это карта исходников, не оценка их стоимости. SHA256 модовых файлов: [source-sites.json](source-sites.json). Native API сверены с установленным Minecraft1.21.1/NeoForge21.1.255, official mappings.

|Путь|Установлено по исходникам|Что следует измерять|
|---|---|---|
|`GreatPoloz.aiStep`|Client branch возвращается до server impact/attack dispatch. `updateParts/syncTrack/impact.sample` раз4 ticks, `impact.tick` каждый server entity tick.|Фактическое число успешных разрушений и активные phase/action, не умножение budget на частоту.|
|`PolozWorldImpact.tick`|Очереди LinkedHashSet до2048 каждая; budget32 breaking attempts и48 conversions/tick. Успешный `destroyBlock` сохраняет native effects/drops/events. Это попытки, не гарантированное число разрушений.|Counter на native Level.destroyBlock RETURN с source instanceof GreatPoloz.|
|`Level.destroyBlock` →2001|Для не-fire блоков вызывается native level event2001; затем drops/setBlock. Успешность изменения возвращается отдельно.|Сопоставить boss-sourced success count с клиентским звуком/частицами; возможное событие перед неудачным setBlock отдельно не считается success.|
|`LevelRenderer.levelEvent`, case2001|Исполняет native break sound, если client block extension не переопределил его, затем addDestroyBlockEffect.|`SoundEngine.play` по event ID/caller и `ParticleEngine.add` по particle type/runtime class.|
|`ParticleEngine.destroy`|Делит voxel boxes с шагом.25, минимум2 части по оси; полный единичный куб даёт4³=64 TerrainParticle. Для иных форм число другое.|Нельзя объявлять все частицы Мор-эффектом или packets/s. Нужны фактические create/remove/queue snapshots.|
|`Library.CountingChannelPool.acquire`|При activeChannels.size≥limit возвращает null и в dev пишет maximum pool warning. SoundEngine при null handle пишет failure.|Pool warning — глобальный факт; handle failure можно привязать к текущему SoundEngine.play event. Tracked map handles не равны физически играющим OpenAL sources.|
|`GreatPoloz.begin/updatePhase/die`|Звуки начала действий/фаз/смерти; rumble раз300 ticks. Windup spark раз4 ticks, debug flame только при debugTicks>0.|Частоты и caller; повторы разных честных событий не являются дублированием одного dispatch.|
|`PolozRenderer` и `TRACK`|Renderer строит модели тела/головы/рубцов; здесь нет playSound/addParticle. TRACK — синхронизация27 частей раз4 ticks.|Не приписывать ему звуковой/particle spam из-за наличия renderer samples. Полосы/части не сокращать.|
|`MorZone.tick`|Client emission раз4 ticks: cloud10 либо ground3 позиции, по2 частицы. Server exposure раз10 ticks; bounded lifetime/owner checks.|Снять отдельные create/caller и исчезновение tracked частиц; это не глобальный спавн каждый render frame.|
|128 native mobs|Walking, crossbows, arrows, damage/другие native эффекты присутствуют без Полоза.|B — контроль того же количества vanilla сущностей **с установленным модом**, не no-mod контроль.|

`syncTrack` и targeted queries остаются отдельными perf-кандидатами #32; эта работа не оптимизирует их без измеренной связи с текущей проблемой. Само большое число tracked particles не доказывает ошибку или утечку. Намеренное разрушение блоков нельзя ускорять удалением его видимых/слышимых последствий.
