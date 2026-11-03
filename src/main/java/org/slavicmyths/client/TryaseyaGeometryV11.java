package org.slavicmyths.client;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Authored 3D voxel geometry shared by NeoForge and independent technical 3D export.
 *  Y grows upward, -Z faces the viewer; units are 1/16 Minecraft blocks. */
public final class TryaseyaGeometryV11 {
    public record Box(String part, double x, double y, double z, double w, double h, double d, String material, double alpha) {}
    private final List<Box> boxes = new ArrayList<>();
    private void box(String part, double x, double y, double z, double w, double h, double d, String material) { box(part,x,y,z,w,h,d,material,1); }
    private void box(String part, double x, double y, double z, double w, double h, double d, String material, double alpha) { if (w <= 0 || h <= 0 || d <= 0 || y < 0 || y+h > 74) throw new IllegalArgumentException("Invalid box "+part+" y="+y); boxes.add(new Box(part,x,y,z,w,h,d,material,alpha)); }
    private static double wave(double a) { return Math.sin(a); }
    private static double blend(double a,double b,double t) { return a+(b-a)*t; }
    private static double smooth(double t) { return t*t*(3-2*t); }

    private void tail() {
        for (int i=0;i<16;i++) {
            double y=i*1.14;
            double t=i/15.0;
            double width=.64+5.55*Math.pow(t,1.40);
            double depth=.70+4.00*Math.pow(t,1.34);
            box("tail",-width/2,y,-depth/2,width,1.19,depth,i%3==0?"mist_hi":"mist",.46-.12*t);
            if(i>2) for(int s:new int[]{-1,1}) box("tail",s*(width*.30)-.18,y+.12,-depth*.24,.36,.87,depth*.48,"mist_hi",.24);
        }
    }

    private void dress() {
        // 18.5 to 34.2: hem 9.4 voxels wide, smoothly narrowing toward 4.4 at waist.
        for (int i=0;i<24;i++) {
            double y=18.5+i*.67;
            double t=i/23.0;
            double width=blend(11.65,4.50,smooth(t));
            double depth=blend(7.25,3.70,smooth(t));
            box("skirt",-width/2,y,-depth/2+.15,width,.72,depth,i%5==0?"purple_mid":"purple_dark");
            // center panel is cloth outside the dress, never a box at torso level.
            double frontZ=-depth/2-.38;
            double centerWidth=blend(2.72,2.20,t);
            box("skirt",-centerWidth/2,y+.06,frontZ,centerWidth,.64,.52,i%4==0?"purple_light":"purple_mid");
            // pleated side folds with staggered front depth, not rectangular side walls.
            for(int side:new int[]{-1,1}) for(int j=0;j<2;j++) {
                double x=side*(centerWidth*.63+j*width*.12);
                box("skirt",x-.28,y+.04,frontZ+.28+j*.13,.56,.65,.45,(i+j)%3==0?"purple_light":"purple_mid");
            }
        }
        // Floating panels start high and terminate at different lengths / depths.
        for(int side:new int[]{-1,1}) for(int n=0;n<3;n++) {
            double x=side*(2.5+n*.66);
            double z=-3.23+n*.74;
            double bottom=20.5+n*2.0+(side>0?0:.7);
            for(double y=bottom;y<32.1;y+=1.05) {
                double u=(y-bottom)/(32.1-bottom);
                double w=.79+.20*u;
                box("skirt",x-w/2+.09*wave(y*.24+n),y,z+.12*wave(y*.32),w,1.14,.76,n==1?"purple_mid":"purple_dark");
            }
            for(int j=0;j<2;j++) box("skirt",x-.21+side*j*.13,bottom-(j+1)*.87,z+.12,.42,.92,.59,"purple_mid");
        }
        // Waist trimmings only at Y 33-34, without extra filler on flanks.
        box("body",-2.29,33.20,-2.0,4.58,.93,3.88,"silver");
        box("body",-2.03,33.28,-2.38,4.06,.70,.36,"skin_shadow");
    }

    private void torso() {
        // Anatomical hourglass: waist 4.1, bust 6.25; depth is independently volumetric.
        for(int i=0;i<18;i++) {
            double y=34.0+i*.69;
            double t=i/17.0;
            double width=4.07+2.18*smooth(t);
            double depth=3.65+1.65*smooth(t);
            double front=-1.94-.85*smooth(t);
            box("body",-width/2,y,front,width,.75,depth,"purple_dark");
            // back of corset bulges behind the spine, not a plane.
            if (i>=5 && i<=14) box("body",-width*.34,y+.05,front+depth-.03,width*.68,.65,.35,"purple_mid");
        }
        // One continuous projecting chest shape (NOT two separate cubes).
        for(int i=0;i<7;i++) {
            double y=42.1+i*.65;
            double t=i/6.0;
            double width=5.76-.23*t;
            double front=-3.10-.34*Math.sin(Math.PI*t);
            box("body",-width/2,y,front,width,.70,1.28+.34*Math.sin(Math.PI*t),"purple_mid");
        }
        // Exposed skin arcs from neckline toward narrow, lowered shoulders.
        for(int i=0;i<4;i++) {
            double y=45.1+i*.64;
            double width=6.45-i*.38;
            box("body",-width/2,y,-3.22,width,.66,2.90-i*.08,"skin");
        }
        for(int side:new int[]{-1,1}) for(int i=0;i<4;i++) {
            double x=side*(2.38+i*.27);
            double y=46.35-i*.32;
            box("body",x-.37,y,-2.13,.74,.49,2.50-i*.15,"skin_shadow");
        }
        // Necklace / V neckline, small pale stepped motif between collarbones.
        for(int k=-3;k<=3;k++) {
            double y=45.50-.11*(3-Math.abs(k));
            box("body",k*.47-.22,y,-3.46,.44,.28,.28,"silver");
        }
        box("neck",-.91,47.30,-.96,1.82,2.33,2.16,"skin");
    }

    private void face() {
        // Step-tapered face, no overlay square on a second giant cuboid.
        double[] widths={3.22,3.72,4.52,5.02,5.22,5.24,5.08,4.75,4.10,3.40};
        double[] depths={3.60,4.05,4.34,4.52,4.65,4.66,4.52,4.37,4.08,3.65};
        for(int i=0;i<widths.length;i++) {
            double y=49.2+i*.70;
            double width=widths[i];
            double depth=depths[i];
            box("head",-width/2,y,-depth/2-.55,width,.77,depth,"skin");
        }
        // Small sculptural chin / jaw and rounded cheeks with thin lateral differences.
        box("head",-1.32,48.95,-2.25,2.64,.39,2.75,"skin_shadow");
        for(int side:new int[]{-1,1}) for(int i=0;i<2;i++) {
            double x=side*(1.82+i*.19)-.28;
            box("head",x,50.25+i*.75,-2.85,.53,.74,.54,"skin_shadow");
        }
        // Cyan, evenly centered eyes do not get hidden by bangs.
        for(int side:new int[]{-1,1}) {
            double x=side*1.19-.58;
            box("eyes",x,53.25,-3.11,1.16,.98,.35,"eye");
            box("eyes",x+.23,53.78,-3.20,.30,.26,.14,"eye_glint");
            box("eyes",x+.03,52.98,-3.12,1.00,.12,.13,"skin_shadow");
        }
        box("head",-.23,52.02,-3.06,.46,.55,.26,"skin_shadow");
        box("mouth_closed",-.58,50.95,-3.00,1.16,.19,.16,"mouth");
        box("mouth_open",-.48,50.62,-3.00,.96,.78,.24,"mouth");
        box("mouth_open",-.31,50.72,-3.27,.62,.23,.18,"skin_shadow");
    }

    private void sleeve(String part,int side, boolean holding) {
        // Shoulder anchor about ±3.4; sleeve hangs, rather than winging out.
        for(int i=0;i<12;i++) {
            double y=44.4-i*1.08;
            double u=i/11.0;
            double x=side*(3.57+.43*u+(holding?.35*u:0));
            double w=1.35+1.02*u;
            box(part,x-w/2,y,-1.23+.18*u,w,1.17,2.35+.74*u,i%4==0?"purple_mid":"purple_dark");
        }
        // Soft ragged drapery hangs BELOW the actual arm, with staggered tips.
        for(int k=0;k<5;k++) {
            double x=side*(3.65+k*.42+(holding?.12:0));
            double bottom=holding?27.5+(k%3)*1.23:28.9+(k%3)*1.04;
            double width=.42+.12*(k==2?1:0);
            box(part,x-width/2,bottom,-1.60+k*.27,width,36.6-bottom,1.44,k%2==0?"purple_dark":"purple_mid");
            box(part,x-width*.29,bottom-.88,-1.55+k*.27,width*.57,.91,1.20,"purple_mid");
        }
    }

    private void arms() {
        // Free arm hangs near hip; fingers are part of same group.
        sleeve("right_sleeve",1,false);
        for (int i=0;i<11;i++) {
            double y=44.0-i*1.02;
            double x=3.67+.42*i/10.0;
            box("right_arm",x-.62,y,-1.18,1.24,1.09,1.64,i<6?"purple_dark":"skin");
        }
        box("right_hand",3.62,28.0,-1.45,1.08,4.02,1.42,"skin");
        for(int i=0;i<4;i++) box("right_hand",3.67+i*.23,26.65+(i%2)*.28,-1.64,.20,2.08,.37,"skin");
        // Staff arm curves from -3.5 shoulder to -7.8 grip near Y 40.
        sleeve("left_sleeve",-1,true);
        for(int i=0;i<12;i++) {
            double t=i/11.0;
            double x=blend(-3.73,-7.35,smooth(t));
            double y=blend(44.2,39.25,t);
            box("left_arm",x-.61,y,-1.30-.45*t,1.22,1.10,1.55,t<.6?"purple_dark":"skin");
        }
        box("left_hand",-8.42,38.25,-1.40,1.40,2.54,1.63,"skin");
        for(int i=0;i<4;i++) box("left_hand",-8.35+i*.30,37.68+(i%2)*.20,-1.68,.26,2.31,.38,"skin");
    }

    private void staff() {
        // Shaft visibly passes through palm. Cap/core exact 6x6x1 / 4x4x4.
        box("staff",-8.08,3.6,-.62,.60,65.8,.60,"staff");
        box("staff",-7.99,19.5,-.53,.41,19.0,.41,"staff_dark");
        box("staff",-9.75,64.25,-2.07,4.0,4.0,4.0,"lantern");
        box("staff",-10.75,63.25,-3.07,6.0,1.0,6.0,"frame");
        box("staff",-10.75,68.25,-3.07,6.0,1.0,6.0,"frame");
        box("staff",-9.36,69.25,-1.68,3.22,.28,3.22,"silver");
    }

    private void hair() {
        // Close-fitting crown: overlapping stepped shell over skull, each tier shorter near top.
        for(int i=0;i<9;i++) {
            double y=55.8+i*.51;
            double t=i/8.0;
            double width=7.7-4.15*smooth(t);
            double depth=7.1-3.6*smooth(t);
            box("hair_back",-width/2,y,-depth/2+.08,width,.57,depth,i%3==0?"hair_mid":"hair_dark");
        }
        // Crown side volume slopes down around cheek, not a solid face-covering visor.
        for(int side:new int[]{-1,1}) for(int i=0;i<8;i++) {
            double y=55.9-i*.72;
            double x=side*(2.78+.08*Math.sin(i*.6));
            box(side<0?"hair_left":"hair_right",x-.52,y,-2.28,.98,.78,3.95-.12*i,i%3==0?"hair_mid":"hair_dark");
        }
        // Bangs finish at eyebrows; staggered x,z not parallel dreadlocks.
        for(int i=-5;i<=5;i++) {
            double x=i*.55;
            double length=2.1+.55*(1-Math.abs(i)/5.0);
            double z=-3.25-.12*Math.cos(i*.64);
            for(double y=56.48-length;y<56.49;y+=.65) box("bangs",x-.34+.06*Math.sin(y*.7+i),y,z,.68,.68,.56,i%4==0?"hair_light":"hair_mid");
        }
        // Hair is arranged as merging scalloped masses, NOT rows of vertical rods.
        // Each large tuft moves outward and changes depth through its length.
        for(int side:new int[]{-1,1}) for(int lock=0;lock<4;lock++) {
            double top=55.8-lock*.46;
            double end=lock==0?37.5:lock==1?32.8:lock==2?35.9:30.8;
            int n=(int)Math.ceil((top-end)/.77);
            for(int j=0;j<n;j++) {
                double t=j/(double)n;
                double y=top-j*.77;
                double x=side*(2.80+lock*.64+.78*Math.sin(t*Math.PI*1.45+lock*.38)+.45*t);
                double z=-2.68+lock*.70+.37*Math.sin(t*Math.PI*.95+lock*.32);
                double w=(1.55+(lock%3)*.33)*(.94-.50*Math.pow(t,2.7));
                double d=(1.30+(lock%2)*.18)*(.98-.25*t);
                String mat=lock%3==0?"hair_dark":lock%3==1?"hair_mid":"hair_light";
                box(side<0?"hair_left":"hair_right",x-w/2,y,z,w,.84,d,mat);
                // Curl-lights only on a minority of surfaces; do not make vertical pinstripes.
                if (j%6==2 && t>.08 && t<.78) box(side<0?"hair_left":"hair_right",x-w*.20,y+.12,z-.21,w*.36,.52,.25,"hair_bright");
            }
            for(int tip=0;tip<4;tip++) {
                double x=side*(3.12+lock*.64+.55+.10*tip);
                double w=.80-tip*.15;
                box(side<0?"hair_left":"hair_right",x-w/2,end-(tip+1)*.63,-2.55+lock*.70,w,.69,.80,lock%2==0?"hair_dark":"hair_mid");
            }
        }
        // Back volumes separate into overlapping curved locks; no smooth flat rectangular cape.
        for(int lock=-4;lock<=4;lock++) {
            double end=35.6+(Math.abs(lock)%3)*1.8;
            for(int i=0;i<21;i++) {
                double t=i/20.0;
                double y=55.3-i*(55.3-end)/21.0;
                double x=lock*.87+.55*Math.sin(t*2.9+lock*.6);
                double z=2.15+.24*Math.cos(t*3.1+lock*.5);
                double width=1.26-.45*t;
                box("hair_back",x-width/2,y,z,width,1.07,1.16,lock%3==0?"hair_mid":"hair_dark");
            }
        }
        // Pale thin face-framing threads: two, not dozen identical rods.
        for(int side:new int[]{-1,1}) for(int lock=0;lock<2;lock++) {
            double end=lock==0?39:35;
            for(double y=53.4;y>end;y-=.85) {
                double t=(53.4-y)/(53.4-end);
                double x=side*(2.45+lock*.36+.27*Math.sin(t*3.0));
                box(side<0?"hair_left":"hair_right",x-.17,y,-3.10-lock*.22,.34,.91,.52,lock==0?"hair_bright":"hair_light");
            }
        }
    }

    public static List<Box> build() { TryaseyaGeometryV11 g=new TryaseyaGeometryV11();g.tail();g.dress();g.torso();g.face();g.arms();g.staff();g.hair();return List.copyOf(g.boxes); }
    public static void main(String[] args) throws Exception { List<Box> b=build();StringBuilder s=new StringBuilder("{\"model\":\"TryaseyaV11\",\"cuboids\":[");for(int i=0;i<b.size();i++){Box v=b.get(i);if(i>0)s.append(',');s.append(String.format(Locale.ROOT,"{\"part\":\"%s\",\"x\":%.4f,\"y\":%.4f,\"z\":%.4f,\"w\":%.4f,\"h\":%.4f,\"d\":%.4f,\"material\":\"%s\",\"alpha\":%.3f}",v.part(),v.x(),v.y(),v.z(),v.w(),v.h(),v.d(),v.material(),v.alpha()));}s.append("]}");Path file=Path.of(args.length==0?"tryaseya_v11_geometry.json":args[0]);Files.writeString(file,s.toString(),StandardCharsets.UTF_8);System.out.println("Exported "+b.size()+" boxes into "+file); }
}
