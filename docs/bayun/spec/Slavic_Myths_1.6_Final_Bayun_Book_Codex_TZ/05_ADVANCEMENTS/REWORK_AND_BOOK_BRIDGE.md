# Achievements — preserve genuine vanilla advancement system, restructure and surface in book

## Audit BEFORE edits
Generate actual table for every `data/slavicmyths/advancement/**/*.json`, custom criteria trigger, reward-function and hardcoded player advancement grant. Required export `existingId,oldParent,newParent,criteria,hidden,display,existingReward,bookGroup,descriptionRuKey`. Preserve all stable IDs and granted progress from existing saves; do not delete, rename or grant all automatically to fake migration. Leave vanilla namespace untouched. New definitions must be in `slavicmyths` namespace.

## Proposed thematic groups (exactly map actual content)
`bandits` Разбойники (first encounter → find camp → defeat leader → existing settlement defense outcomes); `spirits` Духи (first ritual/gift → trust with Domovoi/Leshi etc); `kurgans` Курганы (find mound → investigate → cursed tier/elder boss); `chud` Чудь (city/abandoned mines/deep cave if actual); `settlements` Поселения (visited villages and gorodishche; real defenses); `crafting` Ремёсла (craft rune, cook dish, equipment); `nature` Природа (plant/gather fish/herbs/animals); `bosses` Великие противники (actual encounters, victory); `tales` Книга сказаний (read/start and real discovery milestones).

## Concrete sample links — ONLY AFTER verifying event in code
- `first_bandit_encounter`: detect actual hostile hit/killing attack; if old equivalent ID exists use it, otherwise add new one.
- `find_bandit_camp`: trigger on actual structure detection, not merely player crossing generic plains biome.
- `defeat_bandit_leader`: use actual boss death credit or named entity; no free grant by holding loot.
- `defend_settlement`: not grant unless existing world really records defended event and village interactions; fake “all camps vanish” logic OUT OF SCOPE.
- `meet_bayun`: unlock on real encounter after surviving GRACE/being targeted; `friend_of_bayun` trust >=85, no automatic grant on opening book.
- `first_book_entry`: genuine book open; `bookkeeper` after actual N distinct entries verified (N=10 at release), not by reopening same page 10 times.

## Connection graph rule
Each real advancement belongs to one noncyclic tree rooted at actual root, parent means sensible story/dependency but DOES NOT retroactively revoke an earned child when its new parent isn't awarded. Ensure `criteria` AND/OR logic honest, no circular parent, hidden advancements reveal when logical; existing gameplay should never be made impossible by root lock. Do not mutate known advancement id for cosmetic renames. Change JSON displays/parent in-place; provide new root only if needed without losing child data. Keep backgrounds >=64×64 proper pixel art; advancement icon from current usable item/entity sprite.

## Book view contract
Book reads actual server `PlayerAdvancements` snapshot and criteria completion, not duplicated book booleans pretending awards. Show segmented categories, linked edge paths, node states `LOCKED/AVAILABLE/DONE`, optional `%` only if criterion progress is trackable, description/hover + icon. Support zoom 0.8,1.0,1.2; pan 2D canvas, node min32×32 px, arrows connect edges behind nodes; for hidden criterion locked node use `???`. Track recently earned and unread in separate book seen flags; update synced after award.

## Rewards & correctness
Do NOT invent extra XP/item/reputation from example mockups; honor pre-existing rewards. New achievements default to vanilla UI toast only (no extra material reward) unless reward already specified in actual source; if adding future rewards requires explicit approval. Unlock text must be Russian `advancements.slavicmyths...` consistent with project translation keys. Criterion identifiers, JSON shape and source paths verified against installed NeoForge 1.21.1, no `advancements/` plural folder guess.

## Sample achievement UI (approved)
`09_ASSETS/references/05_advancements_tree.png`: dark frame, gold connections, muted parchment background, green accomplished node, amber available, grey lock; **shown sample quest with 300XP, banner, +5 villagers is not authorized as actual reward**. Implement as visual direction only. At least example real tree with 4 nodes and 1 branch demonstrably linked; actual additional trees built from repository.
