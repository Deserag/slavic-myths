package org.slavicmyths.client;

import com.google.gson.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Native baking/UV checks and orthographic-preview data. Does not launch a game. */
public final class RuneGlow104Meshes {
    private static int checks, cubes;
    private static final JsonArray output = new JsonArray();
    private static void require(boolean ok, String message) {
        checks++; if (!ok) throw new AssertionError(message);
    }
    private static Object field(Object owner, String name) throws Exception {
        Field f = owner.getClass().getDeclaredField(name); f.setAccessible(true); return f.get(owner);
    }
    private static void visit(ModelPart part, Matrix4f parent, JsonArray faces) throws Exception {
        Matrix4f transform = new Matrix4f(parent).translate(part.x, part.y, part.z)
            .rotateZYX(part.zRot, part.yRot, part.xRot);
        for (Object cube : (List<?>) field(part, "cubes")) {
            cubes++;
            for (String name : List.of("minX","minY","minZ","maxX","maxY","maxZ"))
                require(Float.isFinite(cube.getClass().getField(name).getFloat(cube)), "finite cube");
            for (Object polygon : (Object[]) field(cube, "polygons")) {
                JsonArray face = new JsonArray();
                for (Object vertex : (Object[]) field(polygon, "vertices")) {
                    float u = (float) field(vertex, "u"), v = (float) field(vertex, "v");
                    require(Float.isFinite(u) && Float.isFinite(v) && u >= 0 && u <= 1 && v >= 0 && v <= 1,
                        "UV outside atlas: " + u + "," + v);
                    Vector3f position = transform.transformPosition(new Vector3f((Vector3f) field(vertex, "pos")));
                    require(position.isFinite() && position.length() < 64, "bounded native geometry");
                    JsonArray values = new JsonArray();
                    for (float n : new float[]{position.x,position.y,position.z,u,v}) values.add(n);
                    face.add(values);
                }
                faces.add(face);
            }
        }
        for (Object child : ((Map<?,?>) field(part, "children")).values()) visit((ModelPart) child, transform, faces);
    }
    private static HumanoidModel<?> create(Class<?> type, EquipmentSlot slot) throws Exception {
        Method method = type.getDeclaredMethod("create", EquipmentSlot.class); method.setAccessible(true);
        return (HumanoidModel<?>) method.invoke(null, slot);
    }
    private static void add(String id, String texture, EquipmentSlot slot, HumanoidModel<?> model) throws Exception {
        cubes = 0; JsonArray faces = new JsonArray();
        for (ModelPart part : switch (slot) {
            case HEAD -> List.of(model.head,model.hat);
            case CHEST -> List.of(model.body,model.leftArm,model.rightArm);
            case LEGS -> List.of(model.body,model.leftLeg,model.rightLeg);
            default -> List.of(model.leftLeg,model.rightLeg);
        }) visit(part, new Matrix4f(), faces);
        require(cubes > 0, "nonempty " + id);
        JsonObject entry = new JsonObject(); entry.addProperty("id", id); entry.addProperty("texture", texture);
        entry.addProperty("cubes", cubes); entry.add("faces", faces); output.add(entry);
        System.out.println("ARMOR_MESH " + id + " cubes=" + cubes);
    }
    public static void main(String[] args) throws Exception {
        add("perunite_chestplate","textures/models/armor/perunite_layer_1.png",EquipmentSlot.CHEST,create(PeruniteArmor.class,EquipmentSlot.CHEST));
        var mesh=HumanoidModel.createMesh(new net.minecraft.client.model.geom.builders.CubeDeformation(1),0);
        add("minecraft:iron_chestplate","minecraft:textures/models/armor/iron_layer_1.png",EquipmentSlot.CHEST,
                new HumanoidModel<>(net.minecraft.client.model.geom.builders.LayerDefinition.create(mesh,64,32).bakeRoot()));
        Path target=Path.of(args[0]);Files.createDirectories(target.getParent());
        Files.writeString(target,new GsonBuilder().setPrettyPrinting().create().toJson(output));
        System.out.println("RUNE104_NATIVE_MESH_PASS actual ModelPart bake, two chestplates, no game launch.");
    }
}
