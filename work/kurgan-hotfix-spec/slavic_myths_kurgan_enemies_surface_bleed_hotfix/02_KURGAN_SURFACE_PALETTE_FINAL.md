# 02 — KURGAN EXTERIOR MOUND SURFACE PALETTE
## DO NOT LEAVE THE MOUND AS A PURE DIRT HILL

The current mound geometry is approved.
Do NOT redesign the whole Kurgan shape.

The problem is the visible exterior material distribution.

A Kurgan should look like an old artificial burial mound that has existed for a long time:
- mostly earth/grass;
- worn patches;
- exposed soil;
- roots;
- occasional gravel/stone;
- slightly stronger stone treatment near the entrance.

It must NOT look like:
- a giant solid Dirt blob;
- a checkerboard of random blocks;
- a moss mountain;
- a cobblestone pyramid.

References:
- `ref_01_kurgan_blocks_and_decor.png`
- `ref_02_kurgan_dungeon_cutaway.png`

---

# 1. GENERATION METHOD

Do not choose every block independently with `random.nextFloat()`.

That produces visual noise.

Use deterministic patch/noise placement based on:
- Kurgan origin;
- local slope;
- normalized distance from mound center;
- entrance proximity.

Patch sizes should generally be:
**2–6 blocks across**.

Some tiny 1-block accents are allowed only at edges.

---

# 2. MOUND CORE

The invisible/internal mound volume may remain primarily earth.

Recommended structural fill:

- Dirt: **65%**
- Coarse Dirt: **15%**
- Rooted Dirt: **8%**
- Stone: **7%**
- Gravel: **5%**

Exact internal random distribution is less important than surface appearance.

Do not replace every internal block if existing generation already produces stable terrain efficiently.

Priority is the exposed shell.

---

# 3. EXPOSED TOP / CROWN

For blocks that are visible from above around the crown:

Target visual distribution:

- Grass Block: **60%**
- Coarse Dirt: **12%**
- Rooted Dirt: **8%**
- Dirt: **7%**
- Gravel: **5%**
- Moss Block: **4%**
- Stone / appropriate natural exposed stone: **4%**

Apply as patches, not salt-and-pepper noise.

The crown should still read primarily grassy/earthen.

---

# 4. MID-SLOPE

Visible mid-slope:

- Grass Block: **48%**
- Coarse Dirt: **18%**
- Dirt: **10%**
- Rooted Dirt: **8%**
- Gravel: **7%**
- Stone: **5%**
- Moss Block: **2%**
- Mossy Cobblestone: **2%**

Stone/gravel patches should favor steeper local slope.

---

# 5. FOOT / LOWER RING

At the mound foot:

- Grass Block: **55%**
- Coarse Dirt: **12%**
- Dirt: **8%**
- Rooted Dirt: **6%**
- Gravel: **8%**
- Stone: **5%**
- Cobblestone: **3%**
- Mossy Cobblestone: **3%**

Purpose:
suggest old retaining material / disturbed earth without turning the entire base into masonry.

---

# 6. ENTRANCE ZONE

Within roughly 6–10 blocks of the entrance:

Increase visible man-made/weathered stone.

Use controlled patches of:
- Stone;
- Cobblestone;
- Mossy Cobblestone;
- existing Kurgan burial stone accents;
- Gravel;
- Coarse Dirt.

Do NOT cover entrance slope entirely with burial stone.

The entrance should visually transition:
natural mound -> old reinforced burial entrance.

---

# 7. BIOME ADAPTATION

Keep the Kurgan identity but respect local climate.

## Snowy biome

Do not replace the mound with Snow Blocks.

Generate normal mound palette, then allow:
- snow layer on suitable top surfaces;
- sparse exposed earth around entrance/steep slope.

Snow layer is decoration, not structural terrain.

## Taiga / old forest

Up to ~8% of Grass surface patches may become:
- Podzol,
where visually appropriate.

Do not blanket the mound with Podzol.

## Plains

Use default palette.

## Dry/less lush eligible terrain

Reduce Moss Block frequency.
Increase Coarse Dirt/Gravel slightly.

---

# 8. VEGETATION

After surface palette:

Sparse vegetation may appear.

Allowed:
- short grass;
- fern in compatible biomes;
- occasional tall grass;
- very rare small flower;
- rare mushroom in shaded/forest surroundings.

Density:
roughly **8–15%** of valid exposed grass-like top positions.

Do not create flower meadow on burial mound.

Do not place vegetation:
- on entrance path;
- inside doorway;
- on stairs;
- over functional blocks.

---

# 9. STONE ACCENTS

Use stone clusters primarily:
- near entrance;
- lower slope;
- erosion cuts;
- steeper patches.

Typical cluster:
2–5 visible blocks.

Do not distribute isolated Cobblestone every second block.

---

# 10. SMALL / WARRIOR / GREAT VARIATION

Small:
- mostly grass/earth;
- least stone;
- subtle entrance reinforcement.

Warrior:
- slightly more stone/gravel;
- stronger entrance;
- occasional old retaining stones.

Great:
- largest variety;
- visible weathering;
- more reinforced entrance/foot;
- still primarily an earthen Kurgan.

Even Great Kurgan must not become a stone fortress from outside.

---

# 11. TERRAFORMED SUPERFLAT MOUNDS

When Superflat lacks underground depth and Kurgan is raised on an artificial mound:

the artificial slope must receive THIS SAME exterior palette.

Do not leave artificial support volume as:
- exposed Dirt cube;
- Stone wall;
- uniform Grass Block dome.

Blend the raised support into:
grass/earth/root/gravel/stone patches.

---

# 12. NO BLOCK SOUP

The listed percentages are distribution targets across zones.

They do NOT mean every 20 blocks must contain exactly one of each material.

Use coherent patches/noise.

Visual test:
from 30–50 blocks away the mound reads as one natural old mound.
At close range the player discovers varied soil/stone/weathering.

---

# 13. ACCEPTANCE

FAIL if:
- >80% of visible mound shell is plain Dirt;
- exterior is obvious random checkerboard;
- entrance lacks material transition;
- superflat artificial mound exposes uniform fill;
- palette destroys the approved mound shape.
