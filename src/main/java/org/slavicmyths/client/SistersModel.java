package org.slavicmyths.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.InflaterInputStream;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.util.Mth;
import org.slavicmyths.sisters.SisterBoss;

/**
 * Production voxel sisters: 644 cuboids authored with Python geometry and
 * encoded losslessly in the two arrays below (T=424, G=220).
 * The data is [x,height,z,width,height,depth,paletteId]; height is measured
 * upward from the entity's feet. Every box is materialized into a baked
 * Minecraft CubeListBuilder, with a distinct real atlas swatch for each tone.
 * The Python front/back technical views use these EXACT arrays.
 */
public final class SistersModel extends EntityModel<SisterBoss> {
    private static final int ATLAS = 512;
    private static final String TRYASEYA = "eNrtW1mSIysQu5C7giXZzjIx97/Gk9iquttJzu+L8Md0jI0MiRBZiWz+/PlyV02vcLnXV7jiy18FL3x6yZVf5e/rz5cfr2b7+Dfb62iX9Mp3e/v1+ZJedbXHK/34PIZOL+8WQC7/owO8C0C4AeVXDxmAHWJ6hjh7aADkG9B+9BCvAMAOMj+DLAOQ8NYOsjyDnD2QxXADftIofBV2kPUXj0IeQ74B7VcPIDLsINsbJuUmqr4cZ53wHzLWAOgreWjnu5VEhjeA2jtIx3awED2JFmUEjH1sRwdFuBBZGQFTPLaT5sKFqmoE9dj+RTZDfMsRB4hcN735K10OgPSWotY/X47t2E1TqREa6ytW8NbuH1KO2KUFK9W+YVwuD5b4yrerlPoAZYwdn4uFmforA/rEyKMb4gO1n79BnmOBsICXmJJgaW5U+jZYRLehXBFvPSHy7IYTC+0KIXzDPAeT/iqixfsHSL5PLGFiMX7jUJ5DReYLdDIWkeTFngARMtQ9dTx7AAeYcOa2iT3gmQ4QE1PZF0FcoIAVzRhtLlJlc+c+Yo0RMmC+89159admfBzJRtKcQMawfRb930B4jCYzVeBTGJ/dsZfRHtnOBDFyAhe14l/aAKbpNNNZHgrkLN3FNDgAJF9GkFiRnuU9U7W7qK0RBchLadAQewv/hP4HiWLo2RndeBJ77gTPKKmvxLmOVI1tw70TyB5439kxzZ1T8UlO37MzJGuoY4oshlcq/Ms1dEyzWQYsQ4BrdbG3Um9h4sceQjqvC1XQ95SRYG3a1XpUkvjcaGHBUtiPDgyTPSfDGYbWN9CCxSD7IfbKa4aOa7W78iBuPseQ3HLkKOy2NGg6zvAT310PEs5xEEGuyuTdkaKy9uxgK59RqS+wDwcUdlM4jFf34/Mw3NqW4Ogw3ESV6ikpXw8opnlvRQVQ8/8QlsOa2WH51MywuJ0xIlZYjnH5Yi4hEk7J5hJi+585Xah6DH6hqEs9+BVX8tyr8QBCIRFN0UDW1V4elJcQvrU8SBih2AO68i8Dehf/ZcCWTD04PBG9tdJIfr6ZK42uKC1jpZEuvbnQDjNM1kKjp2btMGbwZhHK6RWLTzyYUrHoBKiauwvD5XzQ58C0bM0NYpFszQ1aaWbuoM6zIQI8aoOpAY9iUiwJACTFkgCrhmQQgGeVM5M6qwszp6Pe4ZKcFxch+Wgtbm89h41HsJhhA+TMsJnMgxU2nh4S7LBdqoYmUfq6YqgEmOgtlaB8YE1pPtXjEgBLYNfPZaM0Q+k1IcH1jVvSCeSlZ5wzBoey1Cu7A4hPjHAOiMp2RjyJxXU7x4NaTqoRjjiUXxZBo5A3COJoFkE4gYlFUMjoyGIIuVZMirD20aIIUUeTI0QUDY4KOlpCc79Aq+YL8VaaiqqsTyaTKqileGtNQWEj8cEVjlFh48stN70nHB2W3vSgYrkVp7OASkdsqvytOR2F1CUWVRUnIrGpwrlfTKpwThGTKw9G45krnHhYDXUWPN2gH6Bp4sjWywGU6tLLAZTx36mXA6p4P/Vy6srv/HQKPa8EdQDFWzAnlFuCOYF4jrS5wnlWTK5wZBabqzD1omLo+sw1dhdquneZI/VSdsR9Ao0y3ADtx88B5GWlBBUko+p356BkbKx2WaC8lvgQlcNhRCymPIpnsalCu1hU9Tp0jufr2y2aaCPvldFBaFkro4JyeSyNikq7NjiAhDW9s6JKLuyl0aOK8V4aFVWQXuRMFatRGbYVquDuBU5E3YVYYt0LOYgOyb4fzHM99EKvDq1Bh9DATR7nSB3SPQd6birC1y5td4iWDq/QwdYhMTcanOUwEIsUaDrlEy2Ngj6EkrqPGw4IwcoxWx2C7VUeHYYTK3yulROzsbKeUhE4t3G/DKWUH4hpTNQStlIUSAlhK0WBpHEW6UpRIMJz9lSK1kvcSlEQufmtFG1CyW+lKJAWtlDUTm6hqKHkJRRtOrKFonKydaISK1sn2vKgQlhC0aaDrTGF8g5BU3+qpL3RETJRqlsk7xEPiShdjCdhV8g7BPKdY9KaClEgPm+FKIjA/TsVonUS21bI+2AdC5epEGU65RaIwgiTwNCH0gWeiksfGiPxFog2mRq2QFRK0haI1ovfmeQd4pFI+PXYbwVBYnHlEQ2Rc5oa0RApTIloAKnraaN2UdoQiAYoOO8PfagzqWXKQ0M0CHXIQ+3DT3WoZMgQhzoPl6c4VC7alIY6hPNTGSoXnEEXhjqPNhPHe8CjFFlfS34XDv2eunXxHuF5bJ66UPpodQvjPUKQ4JYw3iMS+xjCeA/I7haGMkjzWxjvETxmLmEok21lK0ND1CUNZZDgtzS0QMPWhjbZurWh8VW2NjTOy9KGEqjhBjLQaJiBNI3a2Quc1e7R6UIw9ewEspoyjED6koYPGPlt69kGjP1LEDGIEcsExKyZyQxiRAwLEEPRFTCYYX1uUZMN/4/sFcP+4yKUs/snl9+etmb40Doqlvcnl0vZsP7i1VyxnL+IPOoN448Wm+X78VvqYNh+/B4/Wq4ff/FSDdNPRhVkUjSS0TmiXCzHL+KBmA3DjxRly+/jb72yYffJVZw33D5MqVlmHw3BbHh9/HFIsKw+emrNcPowmIuW0SePLyJ0TK7RsvnoPVbD5UNH4i2TD2E3MTw+TP/+JuIAMgy+/uXT2d+TqxXL3kMpUi13D/14y9xL43uzo7eHkKtp7SWk12I4e8SIZexhtFIMXw9RM39YFDVnuXooYbw3TL3Uf+x09vTQTwiGpZdwdIiWo5eu6qph6AEzjgfniGqshp2HObliuXmp/wbyY+Z9zLyPmfcx8z5m3sfM+5h5HzPvY+Z9zLz/n5nHL7l73ukHNGYw/yqoU+K6vtJVs85v+HAY9/dYy4zbIx0wb49Qfax2Lhm/IxyIxBFDozIOmH7fx4kKGRd11EB5VfAUZ28/hTmu3B2j5NWVc5Bfnrdx0gijX2AZP9Qdd7t8nJC0Ljnyno/vd20y75Tw/XJjQrIxcsbw9lH263IQb+tJv2K0bu1QFdmN35ryF7wAUCS8+ObzjeBtKBXRr0Xl4dzkfjvUdb0hEv/373953r+m";
    private static final String GNETEYA = "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";
    private final ModelPart root;
    private final boolean spirit;
    private final TryaseyaModelV11 v11;

    public SistersModel(ModelPart baked, boolean spirit) {
        this.root = baked.getChild("root");
        this.spirit = spirit;
        this.v11 = spirit ? new TryaseyaModelV11(baked) : null;
    }

    private static JsonArray decode(String packed) {
        try {
            byte[] compressed = Base64.getDecoder().decode(packed);
            try (InflaterInputStream inflater = new InflaterInputStream(new ByteArrayInputStream(compressed))) {
                byte[] bytes = inflater.readAllBytes();
                return JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonArray();
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException("Invalid Slavic Myths Sisters voxel geometry", e);
        }
    }

    public static LayerDefinition create(boolean spirit) {
        // V10 geometry only for Tryaseya; preserve Gneteya model unchanged.
        if (spirit) return TryaseyaModelV11.create();
        JsonArray boxes = decode(spirit ? TRYASEYA : GNETEYA);
        CubeListBuilder geometry = CubeListBuilder.create();
        int expected = spirit ? 424 : 220;
        if (boxes.size() != expected) throw new IllegalStateException("Sister voxel count mismatch: " + boxes.size());
        // One ModelPart with many real cubes avoids hundreds of costly PartDefinition nodes.
        for (JsonElement element : boxes) {
            JsonArray v = element.getAsJsonArray();
            float x = v.get(0).getAsFloat();
            float height = v.get(1).getAsFloat();
            float z = v.get(2).getAsFloat();
            float width = v.get(3).getAsFloat();
            float tall = v.get(4).getAsFloat();
            float depth = v.get(5).getAsFloat();
            int paletteId = v.get(6).getAsInt();
            if (!(width > 0 && tall > 0 && depth > 0) || paletteId < 0 || paletteId >= 23) throw new IllegalStateException("Malformed Sisters cuboid");
            int u = (paletteId % 8) * 64;
            int vTex = (paletteId / 8) * 64;
            geometry.texOffs(u, vTex).addBox(x, -height - tall, z, width, tall, depth);
        }
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("root", geometry, PartPose.ZERO);
        return LayerDefinition.create(mesh, ATLAS, ATLAS);
    }

    @Override
    public void setupAnim(SisterBoss entity, float walk, float stride, float age, float yaw, float pitch) {
        if (v11 != null) { v11.setupAnim(entity, walk, stride, age, yaw, pitch); return; }
        // Body origin is the entity's feet; never set root.y=24, which buried the model.
        root.y = spirit ? Mth.sin(age * .08F) * .7F : 0;
        root.yRot = yaw * Mth.DEG_TO_RAD * .12F;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, int color) {
        if (v11 != null) { v11.renderToBuffer(pose, buffer, light, overlay, color); return; }
        root.render(pose, buffer, light, overlay, color);
    }
}
