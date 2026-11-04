package org.slavicmyths.client;

import java.util.ArrayList;
import java.util.List;
import org.slavicmyths.client.AgneyaGeometryV7.Box;

public final class LedeyaGeometry {
    public enum Joint {
        BODY(0, 0, 0), HEAD(0, 63, 0),
        LEFT_ARM(-7.25, 58.5, -.2), RIGHT_ARM(7.25, 58.5, -.2),
        LEFT_LEG(-2.75, 38, 0), RIGHT_LEG(2.75, 38, 0),
        HEM_0(0, 0, 0), HEM_1(0, 0, 0), HEM_2(0, 0, 0),
        HEM_3(0, 0, 0), HEM_4(0, 0, 0), HEM_5(0, 0, 0), HEM_6(0, 0, 0);

        public final double x, y, z;

        Joint(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public enum Material {
        SKIN(0xc1cad6), PALE(0xdbe2eb), SHADOW(0x9aa9bd), LIP(0x8d7489),
        PUPIL(0x182a4a), IRIS(0x80d9ff), GLINT(0xe8fbff),
        HAIR_DARK(0x172f62), HAIR_BLUE(0x2856a4), HAIR_LIGHT(0x487cc7),
        CLOTH(0x263b64), FOLD(0x1d2e50), WEAVE(0x3b5885),
        TRIM(0x91b8d0), BOOT(0x253246);

        public final int rgb;

        Material(int rgb) { this.rgb = rgb; }
        public int u() { return ordinal() % 4 * 128 + 2; }
        public int v() { return ordinal() / 4 * 128 + 2; }
    }

    public record Panel(Joint joint, Material material, double x, double y, double z,
                        double w, double h, double d) {}

    public static List<Panel> build() {
        List<Panel> panels = new ArrayList<>();
        for (Box box : AgneyaGeometryV7.build()) {
            Joint joint = switch (box.part()) {
                case "torso" -> Joint.BODY;
                case "head" -> Joint.HEAD;
                case "left_arm", "left_forearm", "left_hand" -> Joint.LEFT_ARM;
                case "right_arm", "right_forearm", "right_hand" -> Joint.RIGHT_ARM;
                case "left_leg" -> Joint.LEFT_LEG;
                case "right_leg" -> Joint.RIGHT_LEG;
                default -> null;
            };
            Material material = switch (box.material()) {
                case "skin" -> Material.SKIN;
                case "skin_light" -> Material.PALE;
                case "skin_shadow" -> Material.SHADOW;
                case "lip" -> Material.LIP;
                case "eye_dark" -> Material.PUPIL;
                case "eye_amber" -> Material.IRIS;
                case "eye_glint" -> Material.GLINT;
                case "hair_dark" -> Material.HAIR_DARK;
                case "hair_mid" -> Material.HAIR_BLUE;
                case "hair_light" -> Material.HAIR_LIGHT;
                case "dress" -> Material.CLOTH;
                case "dress_dark" -> Material.FOLD;
                case "dress_light" -> Material.WEAVE;
                case "gold" -> Material.TRIM;
                case "iron_dark", "iron", "dark_cloth" -> Material.BOOT;
                case "leather", "leather_light" -> Material.FOLD;
                default -> null;
            };
            if (joint == null || material == null) continue;
            if (joint == Joint.HEAD && box.material().startsWith("hair")
                && box.y() < 66 && box.h() > 4) continue;
            panels.add(new Panel(joint, material, box.x(), box.y(), box.z(), box.w(), box.h(), box.d()));
        }
        for (int i = 0; i < 7; i++) {
            double x = -5.25 + i * 1.5;
            double y = 34 + Math.abs(3 - i) * 2.5;
            double z = 2.9 + .55 * Math.cos((i - 3) * .6);
            panels.add(new Panel(Joint.HEAD, i % 2 == 0 ? Material.HAIR_BLUE : Material.HAIR_DARK,
                x, y + 9, z, 1.7, 70 - y - 9, 2.3));
            panels.add(new Panel(Joint.HEAD, Material.HAIR_BLUE,
                x + .22, y, z + .7, 1.18, 10, 1.35));
        }
        for (int side : new int[]{-1, 1}) {
            double x = side < 0 ? -6.05 : 4.35;
            panels.add(new Panel(Joint.HEAD, Material.HAIR_BLUE, x, 54, -3.95, 1.7, 16, 2.85));
            panels.add(new Panel(Joint.HEAD, Material.HAIR_LIGHT, x + .45, 47 + 2 * side,
                -4.1, .85, 13 - 2 * side, 1.65));
        }

        Joint[] joints = Joint.values();
        for (int i = 0; i < 7; i++) {
            double y = 2 + i * 5.4;
            double width = 20 - i * 1.45;
            double depth = 18 - i * 1.55;
            double x = -width / 2;
            double z = -depth / 2;
            Joint joint = joints[Joint.HEM_0.ordinal() + i];
            Material fabric = i == 0 ? Material.TRIM : Material.CLOTH;
            panels.add(new Panel(joint, fabric, x, y, z, width, 5.7, .8));
            panels.add(new Panel(joint, fabric, x, y, -z - .8, width, 5.7, .8));
            panels.add(new Panel(joint, Material.FOLD, x, y, z + .8, .85, 5.7, depth - 1.6));
            panels.add(new Panel(joint, Material.FOLD, -x - .85, y, z + .8, .85, 5.7, depth - 1.6));
            for (int side : new int[]{-1, 1}) {
                panels.add(new Panel(joint, Material.WEAVE, side * width * .25 - .35,
                    y, z - .17, .7, 5.7, .19));
            }
        }
        return List.copyOf(panels);
    }

    public static double hemLift(Joint joint, double step) {
        int tier = joint.ordinal() - Joint.HEM_0.ordinal();
        return tier < 0 ? 0 : Math.abs(step) * (6 - tier) * .83;
    }

    public static void main(String[] args) {
        for (Panel p : build()) {
            Joint j = p.joint();
            System.out.printf(java.util.Locale.ROOT, "%s %06x %f %f %f %f %f %f %f %f %f %f%n",
                j.name(), p.material().rgb, p.x(), p.y(), p.z(), p.w(), p.h(), p.d(),
                j.x, j.y, j.z, hemLift(j, 1));
        }
    }

    private LedeyaGeometry() {}
}
