# POLYMC DEPLOYMENT CONTRACT

Final desired test instance:
`Slavic-Myths-1.21.1-Testing`
unless an existing post-port 1.21.1 instance is already clearly designated.

Requirements:
- Minecraft 1.21.1;
- exact NeoForge version from current Gradle project;
- Java 21;
- independent game directory;
- exact one Slavic 0.9.4 JAR;
- Curios;
- JEI;
- Jade;
- Xaero Minimap;
- Xaero World Map;
- FallingTree.

Safety:
- preserve 1.16.5 fallback;
- do not auto-convert old saves;
- do not launch Minecraft;
- do not hand-write launcher metadata without inspecting actual PolyMC schema;
- do not copy old 1.16.5 mods wholesale.

Before replacing Slavic JAR:
- clean build must pass;
- headless checks must pass;
- production JAR must exist.

Create:
`packaging/test-pack-lock.json`

Include:
- exact MC/NeoForge/Java;
- every mod filename/version/project/file ID;
- SHA-256;
- installed path;
- Slavic JAR SHA-256.

User then manually launches PolyMC.
