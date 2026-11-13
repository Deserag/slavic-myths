package org.slavicmyths.ships.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Opt-in CPU vertex cache for moving ship hulls. Vanilla block rendering runs
 * only on a palette miss; subsequent frames replay recorded solid vertices to
 * the real entity buffers with current ship pose and packed light. No GPU
 * resource is owned, and no static daylight is baked into this cache.
 */
final class ShipMovingHullBatch {
    private static final int MAX_ENTRIES = 4;
    private static final int MAX_TOTAL_VERTICES = 300_000;
    private static final int MAX_SINGLE_VERTICES = 180_000;
    private static final int FULL_BRIGHT = 0x00F000F0;

    static final class Geometry {
        final Map<RenderType, List<BlockVertices>> layers;
        final Set<BlockPos> batched;
        final int vertexCount;
        final int candidateCount;

        Geometry(Map<RenderType, List<BlockVertices>> layers, Set<BlockPos> batched,
                int vertexCount, int candidateCount) {
            this.layers = layers;
            this.batched = batched;
            this.vertexCount = vertexCount;
            this.candidateCount = candidateCount;
        }
        int remaining(int allNonAir) { return allNonAir - batched.size(); }
    }

    record Lookup(Geometry geometry, boolean built) {}

    private record BlockVertices(BlockPos position, List<CapturedVertex> vertices) {}

    private static final class CapturedVertex {
        final float x, y, z;
        float u, v, nx, ny, nz;
        int red = 255, green = 255, blue = 255, alpha = 255;
        int overlay = OverlayTexture.NO_OVERLAY;

        CapturedVertex(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    /** Recording the *actual* renderSingleBlock output preserves quad tint and face shading. */
    private static final class Recorder implements VertexConsumer {
        final List<CapturedVertex> vertices = new ArrayList<>();
        private CapturedVertex active;

        private void finishVertex() {
            if (active != null) {
                vertices.add(active);
                active = null;
            }
        }
        void finish() { finishVertex(); }

        @Override public VertexConsumer addVertex(float x, float y, float z) {
            finishVertex();
            active = new CapturedVertex(x, y, z);
            return this;
        }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) {
            active.red = r;
            active.green = g;
            active.blue = b;
            active.alpha = a;
            return this;
        }
        @Override public VertexConsumer setUv(float u, float v) {
            active.u = u;
            active.v = v;
            return this;
        }
        @Override public VertexConsumer setUv1(int u, int v) {
            active.overlay = (v << 16) | (u & 0xFFFF);
            return this;
        }
        @Override public VertexConsumer setUv2(int u, int v) {
            // The packed light is deliberately applied at replay time.
            return this;
        }
        @Override public VertexConsumer setNormal(float x, float y, float z) {
            active.nx = x;
            active.ny = y;
            active.nz = z;
            return this;
        }
        @Override public VertexConsumer misc(VertexFormatElement element, int... values) {
            // All supported vanilla solid block vertices use the fields above.
            return this;
        }
    }

    private final Map<Object, Geometry> cache = new LinkedHashMap<>(8, .75F, true);
    private int cachedVertices;
    private Level cachedWorld;
    private Object cachedSpruceModel;

    void clear() {
        cache.clear();
        cachedVertices = 0;
    }

    Lookup get(Object key, Map<BlockPos, BlockState> blocks, Level world) {
        Object model = Minecraft.getInstance().getBlockRenderer().getBlockModel(
                Blocks.SPRUCE_PLANKS.defaultBlockState());
        if (cachedWorld != world || cachedSpruceModel != model) {
            clear();
            cachedWorld = world;
            cachedSpruceModel = model;
        }
        Geometry cached = cache.get(key);
        if (cached != null) return new Lookup(cached, false);
        Geometry built = build(blocks);
        if (built.vertexCount > MAX_SINGLE_VERTICES) {
            built = new Geometry(Map.of(), Set.of(), 0, built.candidateCount);
        }
        cache.put(key, built);
        cachedVertices += built.vertexCount;
        while (cache.size() > MAX_ENTRIES || cachedVertices > MAX_TOTAL_VERTICES) {
            var oldest = cache.entrySet().iterator().next();
            cachedVertices -= oldest.getValue().vertexCount;
            cache.remove(oldest.getKey());
        }
        return new Lookup(built, true);
    }

    private static boolean supported(BlockState state) {
        // Exclude translucency, partial-alpha, tile entities and dynamic models.
        // Logs/planks retain their model state (including axis) and full UVs.
        if (!(state.is(Blocks.SPRUCE_PLANKS)
                || state.is(Blocks.OAK_PLANKS)
                || state.is(Blocks.DARK_OAK_PLANKS)
                || state.is(Blocks.STRIPPED_SPRUCE_LOG)
                || state.is(Blocks.STRIPPED_DARK_OAK_LOG)
                || state.is(Blocks.DARK_OAK_LOG)
                || state.is(Blocks.WHITE_WOOL)
                || state.is(Blocks.LIGHT_GRAY_WOOL)
                || state.is(Blocks.YELLOW_WOOL)
                || state.is(Blocks.RED_WOOL)
                || state.is(Blocks.BLUE_WOOL)
                || state.is(Blocks.BROWN_WOOL))) return false;
        var model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        int layers = 0;
        for (RenderType type : model.getRenderTypes(state, RandomSource.create(42L), ModelData.EMPTY)) {
            if (type != RenderType.solid()) return false;
            layers++;
        }
        return layers == 1;
    }

    private static Geometry build(Map<BlockPos, BlockState> blocks) {
        Map<RenderType, List<BlockVertices>> layers = new LinkedHashMap<>();
        Set<BlockPos> batched = new HashSet<>();
        int vertexCount = 0, candidates = 0;
        var blockRenderer = Minecraft.getInstance().getBlockRenderer();
        for (var entry : blocks.entrySet()) {
            BlockState state = entry.getValue();
            if (state.isAir() || !supported(state)) continue;
            candidates++;
            BlockPos pos = entry.getKey();
            Map<RenderType, Recorder> recorders = new LinkedHashMap<>();
            MultiBufferSource recorderSource = type -> recorders.computeIfAbsent(type, ignored -> new Recorder());
            PoseStack local = new PoseStack();
            local.translate(pos.getX() - .5D, pos.getY(), pos.getZ() - .5D);
            blockRenderer.renderSingleBlock(state, local, recorderSource,
                    FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            for (Recorder recorder : recorders.values()) recorder.finish();
            // A malformed or empty model stays on the original rendering path.
            if (recorders.size() != 1) continue;
            var modelLayer = recorders.entrySet().iterator().next();
            List<CapturedVertex> vertices = modelLayer.getValue().vertices;
            if (vertices.isEmpty() || vertices.size() % 4 != 0) continue;
            layers.computeIfAbsent(modelLayer.getKey(), ignored -> new ArrayList<>())
                    .add(new BlockVertices(pos, List.copyOf(vertices)));
            batched.add(pos);
            vertexCount += vertices.size();
        }
        return new Geometry(layers, batched, vertexCount, candidates);
    }

    static void render(Geometry geometry, PoseStack pose, MultiBufferSource buffers, int light,
            Map<BlockPos, Integer> localLight) {
        for (var layer : geometry.layers.entrySet()) {
            VertexConsumer output = buffers.getBuffer(layer.getKey());
            for (BlockVertices block : layer.getValue()) {
                int visualLight = localLight.isEmpty() ? light
                        : (light & 0xFFFF0000) | Math.max(light & 0xFFFF,
                                localLight.getOrDefault(block.position, 0));
                for (CapturedVertex v : block.vertices) {
                    output.addVertex(pose.last().pose(), v.x, v.y, v.z)
                            .setColor(v.red, v.green, v.blue, v.alpha)
                            .setUv(v.u, v.v)
                            .setOverlay(v.overlay)
                            .setLight(visualLight)
                            .setNormal(pose.last(), v.nx, v.ny, v.nz);
                }
            }
        }
    }
}
