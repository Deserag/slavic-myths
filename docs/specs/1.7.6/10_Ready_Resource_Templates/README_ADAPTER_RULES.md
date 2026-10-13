# Copy-ready asset files are implementation scaffolding, not registry classes

These 19 `models/item/*.json`, 19 `textures/item/*.png`, five 1.21.1-style recipe drafts and original reward sound OGG are intended to be copied into the real mod resources after Codex confirms the actual package IDs and current APIs. Do not copy without checking registry namespace, item category in recipe and armor handling. JSON recipes can fail if item IDs or current Minecraft mappings differ: adapt targeted recipe keys only.

Actual armor-worn models require an additional matching current project render layer: mere item JSON alone does not equip a 3D cloak. Use the design and UV guidance in each item file. The source sheets in 07_Visual_References/Approved are the visual source of truth; pixel art item resources here are functional starter icons.
