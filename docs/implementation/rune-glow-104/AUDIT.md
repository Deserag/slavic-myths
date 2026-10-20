# #104 — аудит фактических клиентских путей

API проверен по установленным sourcesAndCompiledWithNeoForge1.21.1 / NeoForge21.1.255,
исходникам ItemRenderer, HumanoidArmorLayer, RenderType, MultiBufferSource и events
RegisterShadersEvent / RegisterRenderBuffersEvent. ABI дополнительно проверен ASM
на фактических compiled class resources через plain JVM, без загрузки игрового мира.

| Путь | Реальный source hook | Ограничение / фактическая проверка |
|---|---|---|
| GUI/хотбар/Creative/контейнер/наковальня | ItemRenderer.render, аргумент buffers | generic registered source; реальный GUI NOT_RUN |
| main/off hand и third-person | тот же ItemRenderer.render, существующий display context | display/poses не меняются; руки NOT_RUN |
| ground/frame | тот же generic ItemRenderer | поведение в игре NOT_RUN |
| builtin/custom item renderer | renderByItem получает wrapped source | если renderer использует этот source; варианты NOT_RUN |
| vanilla armor4 slots | full NeoForge renderArmorPiece overload с6 floats | читается ItemStack соответствующего слота |
| Perunite/Dragon/Poloz/Reward/Unique armor | getArmorModelHook → тот же armor piece | модель/анимации не заменены; runtime NOT_RUN |
| ClothingItem | отдельный TextileClothingLayer по slot | explicit wrapper, player/Slim models исходные |
| vanilla enchantment glint | отдельный POSITION_TEX buffer | не входит в RuneGlowBuffers format filter; coexistence NOT_RUN |
| armor trim/multiple layers | overlay только первого material mesh | исключение повторной яркости; trim coexistence NOT_RUN |
| Fabulous translucent item target | второй RenderType с тем жеshader/texture | source-маршрут отражает vanilla foil check; actual Fabulous NOT_RUN |
| foreign immediate/outline sources | не имеют registered fixed buffers | overlay bypass, исходный рендер сохранён; поддержку не обещаем |
| foreign nonQUADS entity models | QUADS guard | overlay bypass; нет индивидуального renderer под каждыйID |
| ресурсный reload | RegisterShadersEvent callback сохраняет новый ShaderInstance | реальный Minecraft reload NOT_RUN |
| dedicated server | новые Java вclient namespace, config содержит только client mixins | runtime server NOT_RUN; всеcommon Java исходные |

Нет глобального tick scan/cache поItem, permanent glow flag, сети или light entities.
Два fixed buffers1536 начальных bytes каждый используют стандартный lifetime RenderBuffers.
Overlay стоит один дополнительный geometry pass; стоимость не измерена в реальной игре.
