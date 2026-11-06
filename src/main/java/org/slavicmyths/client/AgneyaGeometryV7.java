package org.slavicmyths.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Agneya V7: CLASSIC large, readable Minecraft cuboids, not a dense voxel cloud.
 * Geometry is the single source for the NeoForge ModelPart and Python previews.
 * Coordinates: X right, Y up, -Z forward; 16 units = 1 Minecraft block.
 * No flame/aura geometry. Each body part owns a real articulated pivot. */
public final class AgneyaGeometryV7 {
    public record Box(String part,double x,double y,double z,double w,double h,double d,String material) {}
    private final ArrayList<Box> boxes = new ArrayList<>();
    private void add(String part,double x,double y,double z,double w,double h,double d,String mat) {
        if(w<=0||h<=0||d<=0||y<0||y+h>84)throw new IllegalArgumentException("Invalid " + part);
        if(part.contains("aura")||mat.contains("flame"))throw new IllegalArgumentException("No floating fire allowed");
        boxes.add(new Box(part,x,y,z,w,h,d,mat));
    }
    private void shape() {
        legs(); skirt(); torso(); arms(-1); arms(1); head(); helmet(); shield(); mace();
    }
    private void legs() {
        // Both legs remain visible below the shortened battle skirt. Feet touch Y=0.
        for (int side : new int[]{-1,1}) {
            String bone=side<0?"left_leg":"right_leg";
            double x = side < 0 ? -5.1 : .4;
            add(bone,x,20,-3,4.7,20.5,5.9,"dark_cloth");
            add(bone,x+.22,5,-2.85,4.25,16,5.65,"leather");
            add(bone,x-.02,0,-4.7,4.75,2.9,8.0,"iron_dark");
            add(bone,x+.08,1.3,-4.6,4.55,1.9,7.8,"iron");
            add(bone,x+.24,2.7,-2.9,4.2,4.1,5.85,"leather_light");
        }
    }
    private void skirt() {
        // SHORT HOLLOW war skirt: real cloth panels, never a solid filled volume.
        // Split front exposes animated legs; overlapping back closes the silhouette.
        double[] y={24.0,29.0,34.0};
        double[] width={17.8,15.0,12.2};
        double[] depth={12.7,10.7,9.0};
        for(int i=0;i<y.length;i++){
            double w=width[i],d=depth[i],z=-d/2;
            double gap=2.05+(2-i)*.33;
            double panel=w/2-gap/2;
            add("dress",-w/2,y[i],z,panel,5.5,1.15,i==0?"dress_hem":"dress");
            add("dress",gap/2,y[i],z,panel,5.5,1.15,i==0?"dress_hem":"dress");
            add("dress",-w/2,y[i],d/2-1.05,w,5.5,1.15,"dress_dark");
            add("dress",-w/2,y[i],z+1.0,1.22,5.5,d-1.9,"dress");
            add("dress",w/2-1.22,y[i],z+1.0,1.22,5.5,d-1.9,"dress");
        }
        // Tapered waist is only 7.6 units wide and sits close to the chest.
        add("torso",-5.3,38.8,-4.15,10.6,5.25,8.3,"dress_dark");
        add("torso",-4.0,43.3,-3.55,8.0,7.3,7.15,"dress_dark");
        add("torso",-4.30,43.6,-4.0,8.6,1.85,8.0,"leather");
        add("torso",-5.6,49.0,2.75,11.2,10.5,1.25,"dress_dark");
    }
    private void torso() {
        // Broad shoulders -> high anatomical bust -> short, narrow waist -> hips.
        add("torso",-5.55,50.0,-3.45,11.1,6.0,6.9,"dress");
        add("torso",-5.70,56.0,-3.0,11.4,3.3,6.4,"skin");
        add("torso",-4.85,58.5,-2.6,9.7,2.15,5.95,"skin_light");
        // Two touching, symmetric, stepped cups: large lower volume, rounded shoulderward contour.
        // X=0 is a shared seam rather than a 1-unit air gap.
        for (int side : new int[]{-1,1}) {
            double outer=side<0?-5.25:.0;
            add("torso",outer,54.0,-6.70,5.25,4.55,3.75,"skin_light");
            add("torso",outer+(side<0?.65:0),56.65,-7.05,4.60,2.25,3.72,"skin");
            add("torso",outer+.2,52.35,-6.85,5.05,3.0,3.55,"dress_light");
            add("torso",outer+.63,54.8,-7.17,4.16,1.10,.45,"dress");
        }
        // Minimal closed neckline; short two-voxel neck, not a tall pillar.
        add("torso",-2.0,59.8,-1.75,4.0,2.05,3.5,"skin_light");
        add("torso",-4.5,44.0,-4.2,9.0,1.60,8.4,"leather");
        add("torso",-.8,44.15,-4.55,1.6,1.22,.48,"gold");
    }
    private void arms(int sign) {
        String arm=sign<0?"left_arm":"right_arm";
        String fore=sign<0?"left_forearm":"right_forearm";
        String hand=sign<0?"left_hand":"right_hand";
        double x=sign<0?-9.60:5.45;
        // Deltoids and biceps are 4-5 px across, not thin sticks.
        add(arm,x,54.15,-2.8,4.70,6.65,5.3,"skin_light");
        add(arm,x+.25,47.2,-2.60,4.20,7.6,4.95,"skin");
        add(arm,x+.65,48.6,-3.15,2.55,4.15,.85,"skin_light");
        // Elbow and muscular forearm, slightly tapered.
        add(fore,x+.50,43.0,-2.48,3.90,5.2,4.7,"skin_shadow");
        add(fore,x+.60,39.1,-2.80,3.75,5.35,4.95,"skin");
        add(fore,x+.70,41.1,-3.13,2.35,2.7,.70,"skin_light");
        add(fore,x+.44,38.4,-3.10,4.05,1.55,5.2,"leather");
        add(hand,x+.55,35.0,-3.0,3.80,4.0,4.9,"skin_light");
    }
    private void faceBox(String part,double x,double y,double z,double w,double h,double d,String mat) {
        add(part,x,y-2.0,z,w,h,d,mat);
    }
    private void head() {
        // Chamfered human head: stepped cheek/jaw contour, just five substantial face blocks.
        // Large center cranial mass with narrower upper forehead and lower jaw.
        faceBox("head",-3.93,64.10,-3.90,7.86,8.30,7.55,"skin");
        faceBox("head",-4.55,65.80,-3.50,.92,5.30,6.75,"skin");
        faceBox("head", 3.63,65.80,-3.50,.92,5.30,6.75,"skin");
        faceBox("head",-3.75,65.20,-4.90,7.50,5.70,1.05,"skin_light");
        faceBox("head",-3.22,63.48,-4.50,6.44,2.30,6.05,"skin_light");
        faceBox("head",-2.75,70.80,-4.45,5.50,1.55,5.75,"skin_light");
        // Low-profile amber eyes on the same plane as facial skin. No headlights.
        for(int sign:new int[]{-1,1}) {
            double x=sign<0?-2.96: .94;
            faceBox("head",x-.10,67.35,-5.09,2.30,1.33,.12,"eye_dark");
            faceBox("head",x,67.50,-5.22,2.06,1.03,.11,"eye_amber");
            faceBox("head",x+(sign<0?1.10:.31),67.52,-5.32,.61,.99,.08,"eye_dark");
            faceBox("head",x+.22,68.20,-5.35,.34,.26,.075,"eye_glint");
            faceBox("head",x-.12,69.1,-5.05,2.30,.35,.15,"hair_dark");
        }
        // A small independent nose and mouth, flush with face.
        faceBox("head",-.26,65.84,-5.13,.52,.55,.12,"skin_shadow");
        faceBox("head",-.72,64.80,-5.10,1.44,.30,.12,"lip");
        // Short layered bob remains exposed on back, not a heavy flat cap.
        faceBox("head",-5.15,72.1,-3.25,10.3,2.5,7.0,"hair_dark");
        faceBox("head",-5.48,70.8,-2.9,10.96,1.65,7.65,"hair_mid");
        for(int i=0;i<5;i++) {
            double xx=-5.30+i*2.15;
            faceBox("head",xx,62.75+(i%2)*.55,3.40,1.90,8.55-(i%2)*.55,1.95,i%2==0?"hair_dark":"hair_mid");
        }
        faceBox("head",-5.68,63.40,-3.85,1.42,8.55,5.85,"hair_mid");
        faceBox("head", 4.26,63.40,-3.85,1.42,8.55,5.85,"hair_mid");
        faceBox("head",-5.68,64.20,-4.30,1.06,5.40,1.15,"hair_light");
        faceBox("head", 4.62,64.20,-4.30,1.06,5.40,1.15,"hair_light");
        faceBox("head",-3.85,70.65,-4.72,3.10,1.40,.95,"hair_mid");
        faceBox("head",  .85,70.65,-4.72,3.00,1.40,.95,"hair_mid");
    }
    private void helmet() {
        // Genuine wolf skull visor, dirty bone-white, separate from warm skin face.
        // Raised braincase bridges brow and temples, not a pink flat cap.
        faceBox("head",-4.55,74.15,-4.25,9.10,2.80,7.10,"bone");
        faceBox("head",-3.65,76.30,-3.50,7.30,1.05,5.80,"bone_light");
        // Bone cheek/eye arches frame two genuinely dark inset eye sockets.
        faceBox("head",-4.35,72.65,-5.95,1.55,2.50,3.55,"bone");
        faceBox("head", 2.80,72.65,-5.95,1.55,2.50,3.55,"bone");
        faceBox("head",-2.80,74.25,-5.92,2.10,.68,1.65,"bone_light");
        faceBox("head", .70,74.25,-5.92,2.10,.68,1.65,"bone_light");
        faceBox("head",-2.85,72.75,-5.78,2.15,1.45,.35,"socket");
        faceBox("head", .70,72.75,-5.78,2.15,1.45,.35,"socket");
        // Long wolf muzzle tapers in width toward nose; main snout touches central brow.
        faceBox("head",-2.10,72.30,-7.05,4.20,2.00,3.60,"bone");
        faceBox("head",-1.55,71.85,-9.65,3.10,1.68,3.50,"bone_shadow");
        faceBox("head",-.88,72.50,-9.88,1.76,.62,.34,"socket");
        // Canine teeth beneath protruding upper snout, not in woman's face.
        faceBox("head",-1.55,70.86,-7.32,.62,1.10,.92,"bone_light");
        faceBox("head", .93,70.86,-7.32,.62,1.10,.92,"bone_light");
        // Pointed wolf ears built with just two stepped bone boxes per side.
        for(int sign:new int[]{-1,1}) {
            double x=sign<0?-4.05:2.10;
            faceBox("head",x,76.05,.05,1.95,2.30,2.25,"bone");
            faceBox("head",x+(sign<0? .35:.10),78.20,.35,1.50,1.65,1.55,"bone_light");
        }
    }
    private void shield() {
        // Broad convex shield, ~12x21 model units, firmly strapped to the left forearm.
        // Two deep mounting bars overlap the radius/ulna instead of floating beside them.
        add("shield",-17.40,34.20,-7.55,12.0,21.0,3.45,"iron_dark");
        add("shield",-16.75,35.05,-8.30,10.70,19.3,1.45,"iron");
        add("shield",-15.85,36.15,-8.63,8.90,17.3,.69,"dress_dark");
        // Flat, readable central heraldic boss and four broad corner plates.
        add("shield",-12.55,36.05,-9.12,2.15,17.6,.82,"gold");
        add("shield",-15.80,43.60,-9.18,8.75,2.10,.86,"gold");
        add("shield",-13.15,42.20,-9.88,3.40,4.8,.95,"gold_light");
        add("shield",-16.95,35.10,-8.50,1.55,18.6,.72,"iron_light");
        add("shield",-6.80,35.10,-8.50,1.45,18.6,.72,"iron_light");
        // Two straps run from the rear of the shield directly through the arm's sleeve.
        add("shield",-11.75,38.60,-5.55,4.30,3.1,4.30,"leather");
        add("shield",-11.75,45.10,-5.55,4.30,2.9,4.30,"leather");
        add("shield",-9.45,39.10,-3.35,3.50,2.10,2.40,"iron_shadow");
        add("shield",-9.45,45.40,-3.35,3.50,1.95,2.40,"iron_shadow");
    }
    private void maceBox(double x,double y,double z,double w,double h,double d,String mat){
        add("mace",x,y>=50.0?y-5.0:y,z,w,h,d,mat);
    }
    private void mace() {
        // Long handle runs DIRECTLY THROUGH right-hand box; no floating weapon.
        maceBox(7.05,35.7,-1.6,1.85,20.4,2.1,"iron_dark");
        maceBox(7.01,37.0,-1.72,1.94,5.0,2.3,"leather");
        maceBox(6.25,59.0,-4.25,5.65,6.45,7.45,"iron");
        maceBox(6.85,59.7,-4.52,4.40,5.05,.68,"iron_light");
        // Six readable, broad spikes instead of noise/dust-sized triangles.
        maceBox(7.42,65.0,-1.65,3.18,3.5,2.65,"iron_light");
        maceBox(7.15,56.6,-1.60,3.7,2.9,2.5,"iron_shadow");
        maceBox(3.80,61.2,-1.90,2.75,2.0,2.65,"iron_light");
        maceBox(11.55,61.2,-1.90,2.75,2.0,2.65,"iron_light");
        maceBox(7.00,61.6,-6.60,4.25,2.0,2.60,"iron_light");
        maceBox(7.00,61.6,2.85,4.25,2.0,2.25,"iron_light");
    }
    public static List<Box> build(){AgneyaGeometryV7 g=new AgneyaGeometryV7();g.shape();return List.copyOf(g.boxes);}
    public static String json(){
        StringBuilder b=new StringBuilder("{\"model\":\"AgneyaV7\",\"cuboids\":[");
        List<Box> boxes=build();
        for(int i=0;i<boxes.size();i++){
            Box q=boxes.get(i);if(i>0)b.append(',');
            b.append(String.format(Locale.ROOT,"{\"part\":\"%s\",\"x\":%.3f,\"y\":%.3f,\"z\":%.3f,\"w\":%.3f,\"h\":%.3f,\"d\":%.3f,\"material\":\"%s\"}",q.part(),q.x(),q.y(),q.z(),q.w(),q.h(),q.d(),q.material()));
        }
        return b.append("]}").toString();
    }
    public static void main(String[] args)throws IOException{
        Path p=Path.of(args.length>0?args[0]:"agneya_v7_geometry.json");
        Files.writeString(p,json(),StandardCharsets.UTF_8);
        System.out.println("V7 cuboids: "+build().size()+" -> "+p);
    }
}