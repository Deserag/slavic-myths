# Inventory sprites, headers and achievement prose style

## Book item
64×64 minimum sprite. Closed weathered dark brown (#3A2926) leather book with uneven spine, 5px gold edge motif, embossed tree-of-life shape on cover (not cross/dark religious icon), metal clasp 7×16px and crimson stitched corners. Item silhouette distinct from vanilla book. Render at 16px inventory scale still obvious: gold tree fills >=45% center cover; no scribbly microscopic marks. Optional subtle glimmer enchant-style only if not overused. Actual texture provided in mod, not just image reference.

## Advancement icon art
At least 32×32 for new custom icons; categories: bandit two crossed axes with red scarf; spirits amber protective hearth; mound black stone with entrance; Chud copper underground gate; people wooden palisade; craft rune anvil; nature blueberry bough; boss stylized one-eyed skull; tales open book with gold quill. Each distinct silhouette at tiny size; do not reuse one generic achievement emblem recolored.

## Spoken flavor without voice acting
Cyrillic, low verbosity in combat. Bayun narrator lines should sound folkloric but natural: avoid modern MMO jargon, no “Квест доступен!”, “+25 репутации” floating in speech (can appear in tooltip). Example successful gift: «Рыбкой пришёл угостить? Добро, путник. Пожалуй, сегодня я спою тебе сказку, а не колыбельную...» Example lore: «Что ж, садись подальше от когтей да слушай...» Text of future bogatyr location hints is not injected before entity exists.

## Achievements examples (must verify real gameplay existence)
- «Нежданная встреча» — «Услышьте первый разбойничий окрик.»
- «Тропа разбойников» — «Обнаружьте настоящий разбойничий лагерь.»
- «Угрозе конец» — «Победите главаря лагеря.»
- «Старые курганы» — «Найдите курган в дикой местности.»
- «Незваный гость» — «Встретьтесь с одним из духов.»
- «Не всякая песнь добра» — «Переживите опасную встречу с Котом Баюном.»
- «Кошачья милость» — «Подружитесь с Котом Баюном.»
- «Хранитель сказаний» — «Откройте десять настоящих записей в Книге сказаний.»
No invented extra items/XP/currency attached to these example names. If equivalent achievement already exists, change Russian display text in-place, preserve existing ID. Progress conditions defined by reliable runtime facts in `ADVANCEMENTS` spec.
