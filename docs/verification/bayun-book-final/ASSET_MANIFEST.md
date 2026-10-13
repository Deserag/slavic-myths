# Ресурсы 1.6.9

Оригинальный архив проверен по 51 SHA256 entry (`archive-integrity.json`). Изображения references служат направлениями дизайна и не выданы за готовые игровые ресурсы. Источник звука — `docs/bayun/spec/Slavic_Myths_1.6_Final_Bayun_Book_Codex_TZ/09_ASSETS/audio`; права/характер источников описаны в `10_SOURCES/RIGHTS_AND_TECH.md` и `AUDIO_MANIFEST.csv` этого пакета. Все 19 samples — оригинальные синтезированные прототипы, не live cat recordings и не голос из мультфильма.

- `docs/bayun/kot_bayun.bbmodel`: редактируемые 143 bone groups/254 cuboids, embedded 256 skin, 17 imported CSV animations. Runtime IK, grounding, переходы и наследование curled sleep pose реализованы дополнительно в native renderer, не выдаются за полную автономную функциональность Blockbench playback.
- `docs/bayun/rig.json` + `tools/bayun_assets.py`: воспроизводимые native geometry/UV/Java animation curves, 225 CSV keys, 17 timelines.
- `assets/slavicmyths/textures/entity/kot_bayun/skin.png`: 256×256, body16px/block, face64px/block, два отдельных eye islands12×8 с pupils/highlights; только eyes fullbright, eyelids независимы.
- `assets/slavicmyths/textures/gui/tales/`: восемь разных 32px category icons; `docs/bayun/category-icons.png` contact sheet.
- `assets/slavicmyths/textures/item/lore_book.png`:64×64 предмет; прежний registry ID.
- `BayunMesh`/`BayunClient`: native model, отдельные jaw/ears/eyelids/claws/tail/2-joint legs; сон наследует сложенную позу, не поворачивает стоящую модель на90°.
- `native-model-preview.png`: статическая проекция реальной native geometry/UV, **не игровой скриншот**. Все 17 native poses проверены offline; реальное in-game видео/GIF NOT_RUN.
- `tools/tale_content.py`, `creature-notes.json`:217 entries, 58 отдельных русских living profiles, actual registry ownership and recipes. RU tiers45–95/45–100/60–130/80–160 words проверены. English сохраняет краткие механические описания и все новые ключи.
- 19 SoundEvents с RU/EN subtitles и clip markers; client song loop движется вместе с котом, fades/stop при уходе. Реальный игровой микс ещё не принят.

## Декодированные OGG

| Файл | секунд | RMS (PCM16) | peak | SHA256 |
|---|---:|---:|---:|---|
| assets/slavicmyths/sounds/bayun/bayun_purr_soft.ogg | 2.300 | 8673.9 | 22317 | `d2767b25514b647e38d55d9f91d3dc0808e24d09fe24101c89abbc4265e2e238` |
| assets/slavicmyths/sounds/bayun/bayun_purr_happy.ogg | 2.100 | 9488.7 | 22346 | `462ae870b5a67e61596b1aa4b2947fcb861a3d1339b8bc78475de3d6c146541b` |
| assets/slavicmyths/sounds/bayun/bayun_mew_soft.ogg | 0.747 | 11146.0 | 22708 | `50ce967db72532838d3bfcff8d079598ba7c65995eb8cfe21e0b213bc39d7195` |
| assets/slavicmyths/sounds/bayun/bayun_mew_annoyed.ogg | 0.847 | 11835.7 | 22673 | `49d3a5fd5cbf076e1161e6f5eb06206aa260467b62678fa81df90d644aa6a1c3` |
| assets/slavicmyths/sounds/bayun/bayun_breath.ogg | 1.200 | 5537.1 | 22112 | `950a7092da5d71356f067dc80b5f6b11df00a6eb3df00ac7c6da1779165da02d` |
| assets/slavicmyths/sounds/bayun/bayun_settle.ogg | 0.697 | 5489.1 | 20949 | `037a545ccf003065ac497cf5d24ca084d8cc07e0f71d11ad3946b20e39fe8177` |
| assets/slavicmyths/sounds/bayun/bayun_stretch_exhale.ogg | 0.997 | 6513.1 | 21763 | `9ee1fd8fc091648549f5921a054a1b936e1d97d303d53f5e282f26125875ae4b` |
| assets/slavicmyths/sounds/bayun/bayun_groom.ogg | 0.627 | 1998.7 | 20305 | `fa70cf417e243c425ba2b30cd6d2dae6e5bdcc5851bbb17efbc17be6f244b460` |
| assets/slavicmyths/sounds/bayun/bayun_lap_milk.ogg | 0.847 | 2943.8 | 22685 | `bf842df14b49ca096eff7071b5b922fbaa17f59030fe95db6ddb46e31b029f3c` |
| assets/slavicmyths/sounds/bayun/bayun_chew_fish.ogg | 0.747 | 1845.6 | 22199 | `a314ca19cbce26a27493cb7c41292aa299c95a395e24bf81a3793a0c5e70f9e8` |
| assets/slavicmyths/sounds/bayun/bayun_pawstep.ogg | 0.447 | 1858.1 | 22619 | `4242534bf5ad9e93eff8dbcd3ba7c48b73e6665504c010d4e8623bea11dfbbf6` |
| assets/slavicmyths/sounds/bayun/bayun_spell_warning.ogg | 1.300 | 7911.1 | 22180 | `2ce23a116c32d0d0bf698ac16e77bada32bbd32c97e86479fc5877636fb2e39e` |
| assets/slavicmyths/sounds/bayun/bayun_sleep_song.ogg | 3.810 | 5766.5 | 21696 | `ca8a9e9ce48addc609ea0f3d5af97752cf23979379589048c1ddf38535673865` |
| assets/slavicmyths/sounds/bayun/bayun_song_loop.ogg | 2.000 | 11650.4 | 22696 | `83514f760e77e854f5a859d43d25934ec4206c4bf542079ac2802ad00cbdd13d` |
| assets/slavicmyths/sounds/bayun/bayun_pounce.ogg | 0.777 | 6538.7 | 22128 | `926615af22b54ebf496101d61509efdd77a0e59e731ffa433045d623bac0d727` |
| assets/slavicmyths/sounds/bayun/bayun_claw_swoosh.ogg | 0.872 | 2658.8 | 18625 | `071d75ada660d2344b0994b3624da45bb3c005257b3818855c9a9457dcdc8b3e` |
| assets/slavicmyths/sounds/bayun/bayun_book_page.ogg | 0.727 | 3079.7 | 20146 | `2befa95f370de6c3c9b661a16ea6c9831b1b38ba204069eaec6e92f378a7f4a9` |
| assets/slavicmyths/sounds/bayun/book_open.ogg | 0.860 | 4389.7 | 19760 | `113c2563e0cbc770e6150be686fcd7c7919eeb49837e7f8f3791825966fb066f` |
| assets/slavicmyths/sounds/bayun/book_discovery.ogg | 1.200 | 5449.9 | 22745 | `daa3ca68bfeba659fd3b2d1bac4c6c2be5e70774357a68314bbab734b05b8e90` |
