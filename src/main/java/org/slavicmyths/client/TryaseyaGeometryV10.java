package org.slavicmyths.client;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Actual cubic geometry, shared by Minecraft's model and the Java JSON export. Coordinates: Y increases upward. */
public final class TryaseyaGeometryV10 {
    public record Box(String part, double x, double y, double z, double w, double h, double d, String material, double alpha) {}
    private TryaseyaGeometryV10() {}
    private static final List<Box> out = new ArrayList<>();
    private static void box(String p, double x, double y, double z, double w, double h, double d, String mat) { box(p, x, y, z, w, h, d, mat, 1); }
    private static void box(String p, double x, double y, double z, double w, double h, double d, String mat, double a) { if (w <= 0 || h <= 0 || d <= 0 || y < 0 || y + h > 74.5) throw new IllegalArgumentException("Bad box: " + p); out.add(new Box(p, x, y, z, w, h, d, mat, a)); }
    private static double sin(double v) { return Math.sin(v); }
    private static double cos(double v) { return Math.cos(v); }

    public static List<Box> build() {
        out.clear();
        // Thin spectral point. Physical layers are broad immediately under the skirt, taper smoothly toward the floor.
        for (int i=0;i<13;i++) {
            double y=i*1.4;
            double u=i/12.0;
            double w=.75+5.35*Math.pow(u,1.20);
            double d=.85+4.0*Math.pow(u,1.24);
            box("tail",-w/2,y,-d/2,w,1.42,d,i%3==0?"mist_hi":"mist",0.27+0.18*u);
            for (int side : new int[]{-1,1}) { double xx=side*(w*.38)-.20; box("tail",xx,y+.18,-d*.3,.39,1.1,d*.60,"mist_hi",.21+u*.18); }
        }
        // Upper and lower skirt. Width at hips ~7.2, waist ~4.3; true triangulated-looking tiered pleats, never solid barrel.
        for (int i=0;i<19;i++) {
            double y=18.1+i*.82;
            double u=i/18.0;
            double width=7.9-3.5*u;
            double depth=5.0-1.25*u;
            box("skirt",-width/2,y,-depth/2+.22,width,.88,depth,i%4==0?"purple_mid":"purple_dark");
            for (int j=-3;j<=3;j++) {
                double xx=j*(width/7.8);
                double zz=-depth/2-.20-.18*cos(j*1.2);
                double ww=.38+(.10*(j%2==0?1:0));
                box("skirt",xx-ww/2,y+.10,zz,ww,.80,.45,j%3==0?"purple_light":"purple_mid");
            }
        }
        // Separate long skirt-front panes, with stepped bottom ends at unequal heights.
        for (int j=-3;j<=3;j++) {
            double xx=j*1.00;
            double low=18.8+Math.abs(j)*1.25+(j%2==0?0:.55);
            double high=32.2-Math.abs(j)*.10;
            double depth=-2.62-.16*cos(j);
            box("skirt",xx-.52,low,depth,1.05,high-low,.75,j%2==0?"purple_mid":"purple_dark");
            for (int q=0;q<3;q++) { double y=low-q*.83; box("skirt",xx-.34+(q%2)*.08,y,depth,.58,.85,.58,j%2==0?"purple_dark":"purple_mid"); }
        }
        // Long outer skirt panels hang beside the central pleats; narrow lower silhouette, no cylinders.
        for (int side : new int[]{-1,1}) for (int j=0;j<3;j++) {
            double xx=side*(3.5+j*.40);
            double low=22.0+j*2.20;
            box("skirt",xx-.55,low,-1.40+j*.44,1.10,31.9-low,2.9-j*.24,j%2==0?"purple_dark":"purple_mid");
        }
        // Narrow waistband; no left/right filler cubes are permitted inside the underarm gap.
        box("waist",-2.2,32.5,-1.94,4.4,1.35,3.88,"silver");
        for (int i=0;i<12;i++) {
            double y=33.9+i*.83;
            double t=i/11.0;
            double width=4.0+1.65*Math.pow(t,2.1);       // tight 4.0 waist, widening toward bust.
            double depth=3.4+.75*t;
            box("torso",-width/2,y,-depth/2+.15,width,.88,depth,"purple_dark");
        }
        // Single shaped fitted chest plate; no separated breast blocks. Crown of the bodice projects toward -Z.
        for (int i=0;i<4;i++) {
            double y=42.0+i*.96;
            double width=5.75-i*.08;
            double forward=-2.90-.20*sin(i/3.0*Math.PI);
            box("chest",-width/2,y,forward,width,1.0,3.9,"purple_mid");
        }
        // Exposed collarbones / softly sloped shoulders with pale central V neckline.
        for (int side : new int[]{-1,1}) for (int i=0;i<4;i++) {
            double xx=side*(.65+i*.69);
            double yy=45.4-i*.23;
            box("chest",xx-.50,yy,-2.90,.99,1.02,3.55,"skin");
        }
        box("neck",-.81,46.0,-1.00,1.62,2.85,2.05,"skin");
        for (int j=-5;j<=5;j++) { double xx=j*.45; double yy=45.12+.48*(1-Math.abs(j)/5.0); box("chest",xx-.24,yy,-3.01,.48,.55,.62,j%2==0?"skin":"skin_shadow"); }
        // Angular petite head, one main face volume (no extra facial square mask).
        box("head",-2.73,49.0,-3.48,5.46,7.05,6.75,"skin");
        // Narrow cheek taper for a slightly pointed chin; atop the face, rather than a large second plate.
        for (int side : new int[]{-1,1}) { box("head",side*2.30-.34,49.0,-3.71,.68,2.25,.55,"skin_shadow"); }
        for (int side : new int[]{-1,1}) {
            double xx=side*1.65-.68;
            box("eyes",xx,52.62,-3.91,1.36,1.15,.48,"eye");
            box("eyes",xx+.36,53.15,-3.98,.35,.25,.14,"eye_glint");
        }
        box("head",-.68,50.87,-3.94,1.36,.18,.20,"mouth");
        // The arms belong to their own voxel groups, with a visible negative space against the waist.
        // Viewer-left hand reaches the staff. Arm and forearm touch: no detached hand.
        for (int i=0;i<9;i++) {
            double y=42.0-i*1.05;
            double t=i/8.0;
            double x=-4.45-.36*t;
            double w=1.42+.43*t;
            box("left_sleeve",x-w/2,y,-1.16+.06*t,w,1.15,2.84+.25*t,i%3==0?"purple_mid":"purple_dark");
        }
        // Billowy fabric descends farther than the forearm and ends in jagged tips.
        for (int q=0;q<5;q++) box("left_sleeve",-5.65+q*.28,30.3-(q%3)*1.5,-1.32,.32,6.8-(q%2)*.72,2.25,"purple_dark");
        // Elbow-to-staff bridge: stepped horizontal forearm near Y=39.6.
        for (int k=0;k<7;k++) {
            double xx=-5.10-k*.51;
            double yy=39.0+.15*sin(k*.56);
            box("left_arm",xx,yy,-2.13,.76,.82,1.40,k<4?"purple_dark":"skin");
        }
        box("left_hand",-9.30,38.35,-1.52,1.35,2.8,1.95,"skin");
        for (int f=0;f<4;f++) box("left_hand",-9.31+f*.31,37.82-(f%2)*.12,-1.81,.28,2.46,.55,"skin");
        // Viewer-right arm hangs close to body; visible elongated pale fingertips beneath sleeve.
        for (int i=0;i<13;i++) {
            double y=42.7-i*1.05;
            double t=i/12.0;
            double x=4.02+.30*t;
            double w=1.52+.42*t;
            box("right_sleeve",x-w/2,y,-1.22+.08*t,w,1.15,2.76+.30*t,i%3==0?"purple_mid":"purple_dark");
        }
        for (int q=0;q<6;q++) box("right_sleeve",4.02+q*.24,28.9-(q%3)*1.17,-1.45,.32,5.0-(q%2)*.58,2.52,"purple_dark");
        box("right_hand",3.95,26.85,-1.94,1.22,4.20,1.85,"skin");
        for (int finger=0;finger<4;finger++) box("right_hand",3.96+finger*.30,24.82+(finger%2)*.30,-2.21,.26,2.55,.43,"skin");
        // Staff shaft rests in the palm, not two blocks away.
        box("staff",-9.10,3.5,-.48,.88,65.0,.88,"staff");
        box("staff",-8.94,20.0,-.28,.58,18.0,.56,"staff_dark");
        box("lantern",-10.90,64.10,-2.48,4.45,5.15,4.45,"lantern");
        for (int h=0;h<2;h++) box("lantern",-11.30,h==0?63.52:69.15,-2.88,5.28,.57,5.28,"frame");
        box("lantern",-10.90,69.74,-2.52,4.48,.44,4.48,"frame");
        // Crown of individual tuft voxels: no single horizontal cap or flat helmet.
        // Blocks have differing depth (Z), staggered upper silhouette, and overlapping layers.
        for (int ix=-6;ix<=6;ix++) for (int iz=-3;iz<=4;iz++) {
            double xx=ix*.65;
            double zz=iz*.82;
            double nx=xx/4.5;
            double nz=(zz-.12)/4.1;
            double rad=nx*nx+nz*nz;
            if (rad>1.03) continue;
            double top=61.4-2.35*nx*nx-1.48*nz*nz+.22*sin(ix*.88+iz*1.10);
            for (int h=0;h<8;h++) {
                double yy=55.25+h*.80;
                if (yy>top) break;
                double wave=.09*sin(ix*.7+h*.43+iz);
                String mat=(ix%5==0 && iz%2==0)?"hair_mid":"hair_dark";
                box("hair_cap",xx-.36+wave,yy,zz-.43,.72,.85,.83,mat);
            }
        }
        // Fringe is uneven individual curls, peaking around brows. Separate boxes step along each lock.
        for (int s=-5;s<=5;s++) {
            double x=s*.64-.34;
            int count=2+(int)Math.round((1-Math.abs(s)/6.0)*2);
            for (int j=0;j<count;j++) {
                double yy=56.5-j*.83;
                double xx=x+.12*sin(j*.65+s*.56);
                box("bangs",xx,yy,-4.05-.23*cos(s*.56),.63,.97,.73,s%3==0?"hair_dark":"hair_mid");
            }
        }
        // Side flowing hair: 3D ring layers, different z depth, staggered individual widths and lengths.
        for (int side : new int[]{-1,1}) for (int col=0;col<7;col++) {
            double baseX=side*(3.15+col*.40);
            double baseZ=-1.92+col*.56;
            int count=21-(col%3)*3+(col==0?2:0);
            for (int j=0;j<count;j++) {
                double yy=55.3-j*1.03;
                double xx=baseX+.14*sin(j*.48+col*.81)*side;
                double zz=baseZ+.17*sin(j*.36+col*.90);
                double thick=.64+(col%3)*.09;
                box("hair_side",xx-thick/2,yy,zz,thick,1.13,.83+(col%3)*.10,col%3==0?"hair_dark":col%3==1?"hair_mid":"hair_light");
            }
            for (int tip=0;tip<3;tip++) {
                double y=55.3-count*1.03-tip*.75;
                box("hair_tips",baseX-.22+side*tip*.06,y,baseZ+.1,.43,.72,.62,col%3==0?"hair_dark":"hair_mid");
            }
        }
        // Thin light face-framing locks, flowing forward over shoulders toward waist.
        for (int side : new int[]{-1,1}) for (int strand=0;strand<2;strand++) {
            double x=side*(2.80+strand*.54);
            double z=-3.15-strand*.25;
            int length=17+strand*3;
            for (int i=0;i<length;i++) {
                double y=53.7-i*1.09;
                box("hair_bright",x-.23+.09*sin(i*.58+strand),y,z+.09*sin(i*.49),.48,1.12,.56,strand==0?"hair_bright":"hair_light");
            }
            for (int t=0;t<3;t++) box("hair_tips",x-.16+side*t*.05,53.7-length*1.09-t*.69,z,.32,.64,.50,"hair_bright");
        }
        // Separate outer flyaway locks: connect to the crown, widen at shoulder, taper toward mid-skirt.
        for (int side : new int[]{-1,1}) for (int strand=0;strand<3;strand++) {
            int count=22+strand*2;
            for (int s=0;s<count;s++) {
                double u=s/(double)(count-1);
                double x=side*(4.65+.42*strand+1.05*(1-cos(Math.min(1,u*1.8)*Math.PI/2)));
                double y=54.0-s*1.07;
                double z=-.70+.67*strand+.35*sin(u*2.6+strand);
                double width=(strand==0?.58:.49)*(.92+.12*cos(s*.55));
                box("hair_outer",x-width/2,y,z,width,1.12,.66,strand==1?"hair_light":strand==0?"hair_dark":"hair_mid");
            }
            for (int k=0;k<4;k++) {
                double y=54.0-count*1.07-k*.8;
                double x=side*(5.85+.37*strand+.08*k);
                box("hair_tips",x-.22,y,-.70+.65*strand,.44,.8,.51,strand==1?"hair_light":"hair_dark");
            }
        }
        // Back hair in separate slender strands, without a solid rectangular backplate.
        for (int k=-5;k<=5;k++) {
            double x=k*.68;
            double z=2.60+.31*cos(k*.58);
            int count=13+(k*k%4);
            for (int s=0;s<count;s++) {
                double y=55.0-s*1.06;
                box("hair_back",x-.29+.11*sin(s*.40+k),y,z+.13*sin(s*.50+k),.59,1.12,.84,k%3==0?"hair_dark":"hair_mid");
            }
        }
        return List.copyOf(out);
    }

    public static String toJson(List<Box> boxes) {
        StringBuilder sb=new StringBuilder("{\"model\":\"TryaseyaV10\",\"cuboids\":[");
        for(int i=0;i<boxes.size();i++) {
            Box b=boxes.get(i);
            if(i>0) sb.append(',');
            sb.append(String.format(Locale.ROOT,"{\"part\":\"%s\",\"x\":%.4f,\"y\":%.4f,\"z\":%.4f,\"w\":%.4f,\"h\":%.4f,\"d\":%.4f,\"material\":\"%s\",\"alpha\":%.3f}",b.part,b.x,b.y,b.z,b.w,b.h,b.d,b.material,b.alpha));
        }
        return sb.append("]}").toString();
    }
    public static void main(String[] args) throws Exception { Path file=Path.of(args.length>0?args[0]:"tryaseya_v10_geometry.json"); List<Box> boxes=build(); Files.writeString(file,toJson(boxes),StandardCharsets.UTF_8); System.out.println("Exported " + boxes.size() + " boxes to " + file); }
}