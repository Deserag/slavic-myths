# UI technical layout — implementable layout in logical GUI pixels

PRIMARY REFERENCE `09_ASSETS/references/01_book_main.png`; secondary `04_overview.png`; category/search `03_quests_map.png`. The PNG size is a presentation concept, NOT 1:1 Minecraft scaled pixels. All positions below in virtual 640×400 GUI units; actual framebuffer centered using uniform scale = min(availableW / 656, availableH / 416) including a 8px margin. UI readable at 1280×720 and 1920×1080, GUI scales 2,3,4; if scale <0.70 add scroll/focus rather than minifying text to unreadability.

Base canvas `(0,0)-(639,399)`, dark leather backplate. Page interior left `(28,30)-(298,355)` right `(340,30)-(615,355)`; spine `(307,20)-(330,380)`; corners 24x24; note x-pixel values are relative to full canvas.
- Cover layer: dark leather RGB approx #35231D, spine #4E2D29, corners gold #A87C3E, pressed motif #C49D61. Start screen shows centered golden tree, item is closed book texture 64×64 with clear spine/clasps; no bare default vanilla book sprite.
- Header title (left page) top `(54,48)`: «Книга сказаний», Burgundy #6E211B, 14–18 UI px equivalent pixel font, do not stretch vanilla glyph bitmap.
- Category rail `(6,58)-(37,328)`, 8 icons 26×26 spaced 7px; active behind muted crimson #7B2C22 and gold border; icon sources >=32×32 pixel art with materially distinct shape. On click switch part, preserve last reading position per part.
- Contents left `(53,92)-(296,333)`: part title h=28; chapter row h=31, subsection row h=22, nested indentation 14. 7 rows visible; scroll inside left panel. Row hover 1px gold outline, active row red-brown fill; unlocked 70% of parchment, unknown shown as `???` only when not spoiling.
- Right breadcrumb `(345,41)-(604,54)`: e.g. `Существа › Нечисть › Лихо`; returns ancestor on click; truncated with tooltip if exceeds width.
- Right title `(347,67)-(607,94)`: 16px style, wrapping maximum 2 lines; stage seal occupies last 60 px; must not overlap.
- Right content `(345,110)-(606,318)`: illustration left `(348,113,126,150)` (pixel-art based real entity render, 3D dynamic if performance permits, otherwise 64+ sprite), text right `(486,112)-(603,270)` wrap 10–13 cyrillic glyphs/line; if too narrow use alternate full-width text page, with sticky illustration on cover page. Page turns `‹ ›` switch in-entry sections (`Предание`, `Встреча`, `Повадки`, `Добыча`) not category. Text uses scroll clip/stencil to avoid overflow.
- Bottom info `(347,323)-(603,342)`: current quest/hint/advancement chips, hovered tooltip; footer `(347,354)-(602,381)`: prev left click `(365..407)`, page `N из M` center, next `(541..585)`. Optional «Карта слухов» button h=24 only for verified hint.
- Esc closes; `Backspace` navigates one level, mouse wheel scroll under cursor, Right/Left keys change page when focus not in search, keyboard Tab focuses controls, Enter activates, hover tooltip on icons. Arrow positions fixed independent of string length. Preserve scroll state between page opens in session.

### Typography
Use Minecraft font or bundled licensed bitmap font with complete Cyrillic including Ё/ё, Й/й, soft/hard signs and quotes «». Body effective 9–11 GUI pixels per line, line gap 3, heading 15–17. NO Latin-looking replacement glyphs. Draw shadow minimally on parchment (dark brown #402D27). Every string must be wrapped using real font metrics and `Component` rather than naïve `.length`.

### Visual state treatment
`RUMOR` brown ribbon «Предание», `DISCOVERED` silver «Встречено», `STUDIED` green desaturated «Изучено», `VERIFIED` gold «Проверено». Inaccessible blocks show red wax seal and sentence «Сведения пока неизвестны». Show no bogus % progress for page unrelated to count. Category counter denominator derives actual registered entries dynamically.

### Search area map overlay
Separate right-page overlay or modal `(350,110)-(608,320)` with 232×150 region map, dark red dashed radius (stroke 2 px), a broad biomic zone, ± orientation compass `(585,115)`, legend bottom «Возможное место», «Ваша позиция», «Ориентиры». Player exact coordinates and target center/X/Z NOT printed. Search ring center derived from verified world anchor with deterministic offset; true target must stay within published region. If not enough known map data, display stylized parchment radar/circular compass relative to player and biome text, NOT fabricated existing roads/terrain. Mark freshness (`Слух получен ...` or `Сведения устарели`).

### Failure gates
No stretched two-page PNG used as clickable fake UI; no overlap at longest actual Russian strings; scroll respects panel scissor; no scrolling of background controls; keyboard and mouse both work; tooltip not under cursor hotspot; progress refreshes without relog; all UI resources survive production build and resource reload.
