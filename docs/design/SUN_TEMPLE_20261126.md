# Храм Солнца / Sun Temple — first game implementation

Branch: feature/desert-biome-20261125. Approved fortress direction: twin towers, bridge, solar crown, massive gate and stepped open arena. Geometry derived from Python V1 (111 x 137 x 83); not an exact binary copy, converted to procedural Java with passable main stairs, water basins and ladder shafts. Material accents are block mosaics, not custom banners. Interior rooms, three-seal quest, treasure vault and phase-driven architecture are not final.

Admin command (permission 2): `/slavicmyths dev sun_temple generate`. Command position X/Z is the center of the front edge; height is taken from the terrain and clamped to world build limits. Destructive inside the full 111 x 137 footprint: terrain is replaced by the plan (including air), holes filled and hills removed. Use a disposable world; no biome/slope requirement. One job per level, placement in bounded column batches. Existing registered temple overlap rejected; other player buildings are not protected by this command.

Natural: native structure slavicmyths:sun_temple in minecraft:desert; random-spread spacing 128/separation 96 chunks (candidate grid 2048 blocks, actual nearest sites vary). Center biome required, median sampled elevation; no slope limit; the footprint is levelled by per-column chunk-clipped placement. Candidate clearance checks villages, pyramids, outposts and other mod structures conservatively. /locate structure slavicmyths:sun_temple.

Natural encounter is registered from the center chunk; Hala spawns once on entering the arena if difficulty is not Peaceful and central chunks/floor are present. Manual generation attempts spawn after building. UUID and defeat are persisted; no respawn after death. Hala bound to this site has arena-restricted targets and a 30-block in-arena boss bar, without affecting unrelated Hala instances. No change to combat constants. Interrupted manual placement is not automatically resumed; use a fresh test site. Full runtime and worldgen verification required.

Do not claim this version passed a client test before running it.
