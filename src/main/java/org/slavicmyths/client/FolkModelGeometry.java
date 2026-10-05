package org.slavicmyths.client;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.util.FastColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/** Bakes the existing authored cuboids without rounding dimensions or changing UVs.
 * Each model owns its builder; no static part cache survives model/resource reloads.
 */
@OnlyIn(Dist.CLIENT)
public final class FolkModelGeometry {
    private record Part(int width, int height, List<ModelPart.Cube> cubes,
                        Map<String, ModelPart> children, CubeListBuilder builder) { }
    private final Map<ModelPart, Part> parts = new IdentityHashMap<>();

    public ModelPart part(int width, int height) { return part(width, height, 0, 0); }
    public ModelPart part(int width, int height, int u, int v) {
        var cubes = new ArrayList<ModelPart.Cube>();
        var children = new LinkedHashMap<String, ModelPart>();
        var result = new ModelPart(cubes, children);
        parts.put(result, new Part(width, height, cubes, children, CubeListBuilder.create().texOffs(u, v)));
        return result;
    }
    public void box(ModelPart model, int u, int v, float x, float y, float z, float w, float h, float d) {
        var part = require(model);
        part.builder.texOffs(u, v);
        box(model, x, y, z, w, h, d);
    }
    public void box(ModelPart model, float x, float y, float z, float w, float h, float d) {
        var part = require(model);
        part.builder.addBox(x, y, z, w, h, d);
        var definitions = part.builder.getCubes();
        part.cubes.add(definitions.getLast().bake(part.width, part.height));
    }
    public void attach(ModelPart parent, ModelPart child) {
        var part = require(parent);
        require(child);
        if (parent == child || child.getAllParts().anyMatch(value -> value == parent))
            throw new IllegalArgumentException("Cyclic model hierarchy");
        if (part.children.containsValue(child)) throw new IllegalArgumentException("Duplicate model child");
        part.children.put("part_" + part.children.size(), child);
    }
    private Part require(ModelPart part) {
        var state = parts.get(part);
        if (state == null) throw new IllegalArgumentException("Part belongs to another geometry builder");
        return state;
    }
    public static int withOpacity(int color, float opacity) {
        return FastColor.ARGB32.color((int)(FastColor.ARGB32.alpha(color) * opacity),
                FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color));
    }
}
