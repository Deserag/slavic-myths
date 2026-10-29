> Исторический checkpoint. Текущая проверка домашнего hell от29.10.2026: [матрица](../home-desktop-sync-20261029/ACCEPTANCE.md), [отчёт/квитанция](../home-desktop-sync-20261029/REPORT.md). Старые NOT_STARTED/нет wearable/запуск запрещён ниже не описывают актуальный HEAD; они сохранены как история. Текущий scope IN_PROGRESS, owner acceptance не получена.

# Pre-implementation matrix

| Requirement | Already in HEAD | Missing | Production files | Reference | Test | Status |
|---|---|---|---|---|---|---|
| D ocean-only biome/topology/boss | data biome+entities+MagmaEncounters | common ocean resolver,feature,arena | burningislands,navya/mixin | #304–323 + full biome lock |5 native seed profiles + owner actual chunks | IN_PROGRESS |
| II original state/owner/expiry/protection | response damage only | persisted provenance and restoration | MagmaRunes,new mutation authority | #314/#316 | mutation crash/reload conflict tests,owner MP | NOT_RUN |
| Boss reward cross-player | BossRewardAuthority/Inbox | fault cuts/recovery proof | losthalls/BossReward* | #321 | actual file cut simulations,not real crash | NOT_RUN |
|8 armor+effects/slots/transforms | catalysts/materials/NetherSmithing | wearable/model/effects | nethersmithing +client,Runes | #325/326 armor locks | recipes/components/native model/owner wear | NOT_RUN |
|5 weapons/skills/2 sockets |5 catalysts/six normal upgrades/nativeengine | special IDs/skills/models | combat,item,NetherSmithing | #329/330 weapon contract | damage/charge/projectile/owner PvP | NOT_RUN |
| Furnace/special fuel | vanilla roast recipes | block/BE/menu/screen/XP/hoppers | nethersmithing,furnace client | #325 provisional parameters | fuel/output/native tests;owner automation/relog | NOT_RUN |
| Ores/Nature debts/amulet/test norm | source current scopes | heat bias,surface,budgets,prototype,test cooldown | netherores,balance,equipment | #324/#328/#325 | source/native/owner survival | NOT_RUN |
| Old biome/head regression | accepted production content | targeted regression | navya,cinder,halls,trophy | #251–303/#327/#331 | native existing gates,owner runtime | NOT_RUN |
| Original art/audio | indices and technical diagrams | SHA-matched physical originals/genuine OGG | assets/reference files | exact index paths | actual file SHA audit | BLOCKED_REFERENCE pending scan |

No gameplay PASS before actual game runs; those remain forbidden.
