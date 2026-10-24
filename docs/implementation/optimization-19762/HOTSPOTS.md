# Reviewed hotspots

Method | Frequency | STATIC_CONFIRMED work | Decision
--- | --- | --- | ---
TaleService.open/snapshot | each explicit open | full catalog/recipes copied/re-sent | session definitions cache + reload invalidation
TaleService.advancements | open and award | display JSON serialization | runtime measurement pending
TaleScreen.textLines | each frame | native font splitting | page/layout cache
TaleRecipeText.display | each recipe frame | JSON parse | page revision cache
TaleScreen.current | multiple/frame | linear catalog lookup | id index
TaleScreen.render count | each frame | 242-entry scan | snapshot count
TaleScreen.renderDeeds | each frame | offscreen nodes/items | viewport culling/icons cache
TaleScreen.line | each edge/frame | one fill per DDA pixel | pixel-equivalent clipped/coalesced lines
FolkUi.PaintedButton | each button/frame | label wrapping | one layout per widget
ClassScreen.renderTree | each frame | new nodes HashMap | init-time tree index
EarthCamera.camera | each camera frame | two result arrays/lists | reused scratch/finally
TaleEvents.seen | 20ticks/player | 64 entities/32loadedstarts bounded | KEEP until runtime profile
RpgEvents/ClassEvents | player tick | different rune/class responsibilities | KEEP, not proven duplicate
TaleScreen.illustrate | selected preview/frame | cached live model | cleanup on selection/removal; preserve preview

All game costs SUSPECTED_COST until native profiles; offline allocations are labeled fixture only. No PROFILE_CONFIRMED game FPS claim yet. Full source inventory is a scan, not a claim of post-line review of every class.
