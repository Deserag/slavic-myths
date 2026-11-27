# Savanna reward implementation — stage 1

Initial unverified 3D armor prototype based on approved concept: 17 armor, no toughness, +6 HP, +25% speed and +15% melee attribute damage. Independent slot geometry, 8 material atlas tiles, individual pixel icons and leather-based recipes. Jackal reward allocation uses a stable pack UUID offset; 5 hides / 4 fangs per nine eligible player kills. Hyena leader and bodyguards have separate materials. No ready weapons.

Pending: in-game visual comparison, full-set dodge/stealth/critical integration, cloak, enchantment-preserving upgrades, summon changes and future class hook, sulfur/fire coal/sky coal/steppe ingot, natural Skarapeya spawning. No build or GameTests executed in remote-only environment. Do not treat as release.

## Stage 2
Added a layered back cloak with separate fur collar, golden diamond and articulated cloth, Curios charm slot; 10% damage/speed, +5 pp crit chance, 15% detection reduction and summoned damage. Added full armor dodge, stealth and critical multiplier through NeoForge events. Critical applies to charged melee, not every custom skill. Added sulfur stone/deepslate variants (reuse existing sulfur), Fortune/Silk Touch, 10 upper and 7 lower attempts vs vanilla coal 30/20, 2 deep attempts, fire coal in savanna and two sky-coal recipes. Fuel consumption in flight is deferred; ship flight not present in confirmed code. Class hooks and summon staff next; all figures provisional. Build, dedicated server and client still unverified.
