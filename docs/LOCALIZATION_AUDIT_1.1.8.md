# Localization audit 1.1.8

Static UTF-8 audit; no client was launched. RU/EN key parity: 0 missing RU and 0 missing EN keys. Repaired 36 mojibake values, including Kitchen II food/tool names, and repaired their canonical generator strings. Remaining corrupted values: 0.

Added nine grouped kitchen labels, rune incompatibility and 24 compact rune tooltip lines. Descend is named “Снижение на ступе/метле” / “Descend on Mortar/Broom”; the crate is “Ящик” / “Produce Crate”. Rune numeric strings receive definition parameters, not separate balance literals.

Scoped 1.1.x gameplay and client screens contain no hardcoded natural-language `Component.literal` labels: 0 replacements required, 0 remaining. Roman socket indices and numeric/symbol formatting are technical presentation. Runtime translation rendering and narrow GUI scales require manual checks.
