# Agneya V6 — branch 2.4

Approved baseline of 120 clean large cuboids; warm tan skin, amber eyes, wolf skull, classic skirt. The mace rotates 69 degrees forward relative to the hanging right forearm, anchored to the right hand. The left shield measures 12x21 model pixels and is attached to the left forearm with two overlapping straps. No other Agneya V6 anatomy/colors altered.

- Runtime geometry: `src/main/java/org/slavicmyths/client/AgneyaGeometryV6.java`
- NeoForge rig: `AgneyaModelV6.java`, registered in `ClientSetup.java` and `AgneyaRenderer.java`
- Resource atlas: `assets/slavicmyths/textures/entity/agneya_v6.png`
- Offline geometry: `assets/slavicmyths/geometry/agneya_v6.json`
- Editable Blockbench source: `tools/agneya/v6/agneya_v6_rig.bbmodel`

AgneyaBoss is a summonable test entity, without natural spawn. Build Java 21 using `./gradlew --no-daemon clean build`.
