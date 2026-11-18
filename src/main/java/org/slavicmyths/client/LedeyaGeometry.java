package org.slavicmyths.client;

import java.util.ArrayList;
import java.util.List;
import org.slavicmyths.client.AgneyaGeometryV7.Box;

public final class LedeyaGeometry {
    public static final double BOW_LENGTH_SCALE = (72.6 * .70) / 17.9;
    private static final double STRING_HALF_LENGTH = 8.8 * BOW_LENGTH_SCALE;

    public enum Joint {
        BODY(0, 0, 0), HEAD(0, 63, 0),
        LEFT_ARM(-7.25, 58.5, -.2), RIGHT_ARM(7.25, 58.5, -.2),
        LEFT_LEG(-2.75, 38, 0), RIGHT_LEG(2.75, 38, 0),
        HEM_0(0, 0, 0), HEM_1(0, 0, 0), HEM_2(0, 0, 0),
        HEM_3(0, 0, 0), HEM_4(0, 0, 0), HEM_5(0, 0, 0), HEM_6(0, 0, 0),
        BOW(-7.25, 36.8, -2.1), STRING_UP(-7.25, 36.8, -1.05),
        STRING_DOWN(-7.25, 36.8, -1.05),
        ARROW_CENTER(-7.25, 36.8, -2.1), ARROW_LEFT(-7.25, 36.8, -2.1),
        ARROW_RIGHT(-7.25, 36.8, -2.1);

        public Joint parent() {
            return switch (this) {
                case BOW -> LEFT_ARM;
                case STRING_UP, STRING_DOWN, ARROW_CENTER, ARROW_LEFT, ARROW_RIGHT -> BOW;
                default -> null;
            };
        }

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
        TRIM(0x91b8d0), BOOT(0x253246), BOW_WOOD(0x49576c);

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
        for (int side : new int[]{-1, 1}) {
            for (int segment = 0; segment < 5; segment++) {
                double reach = segment * 1.7;
                double y = Joint.BOW.y + (side * reach - (side < 0 ? 2.1 : 0)) * BOW_LENGTH_SCALE;
                double z = -4.15 + segment * segment * .16;
                panels.add(new Panel(Joint.BOW, segment == 4 ? Material.TRIM : Material.BOW_WOOD,
                    -7.8, y, z, 1.1 - segment * .12, 2.2 * BOW_LENGTH_SCALE, 1.38));
            }
        }
        panels.add(new Panel(Joint.BOW, Material.BOOT, -7.93, 35.25, -3.8, 1.36, 3.1, 2.1));
        panels.add(new Panel(Joint.STRING_UP, Material.PALE, -7.39, Joint.BOW.y, -1.17, .28, STRING_HALF_LENGTH, .24));
        panels.add(new Panel(Joint.STRING_DOWN, Material.PALE, -7.39, Joint.BOW.y - STRING_HALF_LENGTH, -1.17, .28, STRING_HALF_LENGTH, .24));
        Joint[] arrows = {Joint.ARROW_CENTER, Joint.ARROW_LEFT, Joint.ARROW_RIGHT};
        for (int i = 0; i < arrows.length; i++) {
            double x = -7.25 + (i == 0 ? 0 : i == 1 ? -.85 : .85);
            double y = i == 0 ? 37 : 37.55;
            panels.add(new Panel(arrows[i], Material.BOW_WOOD, x - .15, y, -11.2, .3, .3, 10.9));
            panels.add(new Panel(arrows[i], Material.IRIS, x - .35, y - .16, -12, .7, .62, 1.25));
            panels.add(new Panel(arrows[i], Material.PALE, x - .4, y - .12, -2, .8, .54, 1.3));
        }
        return List.copyOf(panels);
    }

    public static double hemLift(Joint joint, double step) {
        int tier = joint.ordinal() - Joint.HEM_0.ordinal();
        return tier < 0 || tier > 6 ? 0 : Math.abs(step) * (6 - tier) * .83;
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
