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
public final class ArmorRevisionMeshes {
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
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET)) {
            String part = switch(slot) {case HEAD -> "helmet";case CHEST -> "chestplate";case LEGS -> "leggings";default -> "boots";};
            for (Class<?> type : List.of(PeruniteArmor.class,PolozArmor.class,DragonArmor.class)) {
                String family = type == PeruniteArmor.class ? "perunite" : type == PolozArmor.class ? "poloz" : "dragon";
                String texture = "textures/models/armor/" + family + (family.equals("perunite") ? "_layer_" + (slot==EquipmentSlot.LEGS?2:1) : "") + ".png";
                add(family+"_"+part,texture,slot,create(type,slot));
            }
            String kind = switch(slot) {case HEAD -> "clear_sight";case CHEST -> "bogatyr";case LEGS -> "indomitability";default -> "wind";};
            add(switch(slot) {case HEAD -> "clear_sight_helmet";case CHEST -> "bogatyr_chestplate";case LEGS -> "indomitability_leggings";default -> "wind_boots";},
                "textures/armor/unique_"+kind+"_layer_"+(slot==EquipmentSlot.LEGS?2:1)+".png",slot,create(UniqueArmorModels.class,slot));
        }
        for (int i=0;i<3;i++) {
            String id = List.of("trophy_idol_chest","trophy_marinka_cloak","trophy_kartaus_mantle").get(i);
            add(id,"textures/models/armor/"+id+".png",EquipmentSlot.CHEST,(HumanoidModel<?>)field(new RewardArmor(i),"model"));
        }
        for (String id : List.of("gambeson","plate","seven_league")) {
            boolean boots=id.equals("seven_league");
            var mesh=net.minecraft.client.model.HumanoidModel.createMesh(new net.minecraft.client.model.geom.builders.CubeDeformation(1),0);
            var model=new HumanoidModel<>(net.minecraft.client.model.geom.builders.LayerDefinition.create(mesh,64,32).bakeRoot());
            add(boots?"seven_league_boots":id.equals("plate")?"plate_cuirass":id,
                boots?"textures/armor/seven_league.png":"textures/models/armor/"+id+"_layer_1.png",
                boots?EquipmentSlot.FEET:EquipmentSlot.CHEST,model);
        }
        require(output.size()==22,"all armor covered");
        Path target=Path.of(args[0]); Files.createDirectories(target.getParent());
        Files.writeString(target,new GsonBuilder().setPrettyPrinting().create().toJson(output));
        System.out.println("ARMOR_MESH_PASS checks="+checks+" models="+output.size());
    }
}
