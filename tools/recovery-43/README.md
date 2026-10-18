# Pure offline check for integration #43

CubeParity.java contains cube method bodies extracted from exact main fb2994e9 and PR37 source,
with a recording VertexConsumer/PoseStack stub and only NO_OVERLAY replaced by its common constant.
Run with Java21 javac/java directly; no Minecraft classes, registry bootstrap, OpenGL, worlds or servers.
10000 deterministic positive/negative/zero dimension cases compare float bits, vertex order, UV,
normals, packed color, light and overlay. This is emission parity, not native rendering or FPS testing.
The production method remains byte-for-byte the PR37 method. Test sources never enter main sourceSet/JAR.
