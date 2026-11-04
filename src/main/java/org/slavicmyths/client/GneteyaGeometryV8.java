package org.slavicmyths.client;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public final class GneteyaGeometryV8 {
    public record Box(String part, double x, double y, double z, double w, double h, double d, String material) { }
    private final List<Box> boxes = new ArrayList<>();
    private void box(String part,double x,double y,double z,double w,double h,double d,String material) {
        if(w<=0||h<=0||d<=0||y<0||y+h>82)throw new IllegalArgumentException("Bad cuboid "+part);
        boxes.add(new Box(part,x,y,z,w,h,d,material));
    }
    private static double mix(double a,double b,double t){return a+(b-a)*t;}
    private static double ease(double t){return t*t*(3-2*t);}
    private void lowerDress() {
        for(int side:new int[]{-1,1}){
            String leg=side<0?"leg_left":"leg_right";
            box(leg,side*2.05-1.15,1.2,-1.74,2.30,18.7,3.6,"black");
            box(leg,side*2.05-1.38,.55,-2.30,2.76,1.80,4.55,"black_hi");
        }
        for(int i=0;i<44;i++){
            double y=.65+i*.64,t=i/43.0,width=mix(13.8,6.9,ease(t)),depth=mix(12.8,7.05,ease(t));
            double front=-depth/2+.15;
            box("dress_black",-width*.22,y,front+.8,width*.44,.66,depth-1.0,"black");
            for(int s:new int[]{-1,1}){
                String group=s<0?"skirt_left":"skirt_right";
                double min=s<0?-width*.5:0;
                box(group,min,y,front,width*.5,.67,depth,s<0?"black":"black_hi");
                for(int k=0;k<5;k++){
                    double xx=min+(k+.5)*width*.5/5.0;
                    box(group,xx-.20,y+.02,front-.16+.12*k,.40,.67,.59,k%2==0?"black":"black_hi");
                }
            }
        }
        for(int side:new int[]{-1,1})for(int k=0;k<5;k++){
            String group=side<0?"skirt_left":"skirt_right";
            for(int i=0;i<17;i++){
                double y=.75+i*1.65,t=i/16.0,width=mix(13.8,7.0,ease(t));
                double x=side*(.25+(k+.5)*width*.43/5.0);
                box(group,x-.19,y,-mix(6.6,3.75,t)+k*.12,.38,1.72,.64,k==2?"black_hi":"black");
            }
        }
    }
    private void overdress() {
        for(int i=0;i<31;i++){
            double y=17.65+i*.54,t=i/30.0,width=mix(14.4,5.95,ease(t)),depth=mix(11.8,7.55,ease(t));
            double front=-depth/2+.08;
            double coreWidth=width*.86;
            box("red_skirt",-coreWidth/2,y,front+.20,coreWidth,.58,depth*.92,"red");
            for(int k=-5;k<=5;k++){
                String group=k<0?"red_panel_left":k>0?"red_panel_right":"red_panel_center";
                double x=k*width/12.75;
                double base=y-17.65;
                double hemLift=(Math.abs(k)>=4?.65:Math.abs(k)==3?.27:0);
                if(base<hemLift)continue;
                double z=front-.19+Math.abs(k)*.12;
                double w=.69+(k==0?.14:0);
                box(group,x-w/2,y+.03,z,w,.58,.62,Math.abs(k)==1||Math.abs(k)==4?"red_shadow":"red");
            }
            for(int side:new int[]{-1,1}){
                double x=side*(width*.43)-.4;
                if(i>1)box(side<0?"red_panel_left":"red_panel_right",x,y,front+.50,.8,.57,depth*.40,"red_dark");
            }
        }
    }
    private void torso() {
        for(int i=0;i<27;i++){
            double t=i/26.0,y=34.9+i*.53,w=mix(5.75,9.35,ease(t)),d=mix(6.85,10.15,ease(t)),zc=mix(.12,.30,t);
            box("torso",-w/2,y,zc-d/2,w,.58,d,y>46.9?"black":"red_dark");
        }
        for(int i=0;i<11;i++){
            double t=i/10.0,y=41.4+i*.62,b=Math.pow(Math.sin(Math.PI*t),.75),w=8.15+.86*b-.43*t;
            double z=-5.12-3.05*b;
            box("torso",-w/2,y,z,w,.69,2.14+1.18*b,i%5==0?"red_dark":"red");
        }
        box("torso",-2.97,34.25,-3.56,5.94,1.13,7.12,"red_shadow");
        for(int k=0;k<3;k++)box("torso",-3.25,48.0+k*.51,-4.22,6.5,.55,.94,"black");
        box("neck",-1.04,49.35,-1.42,2.08,3.42,2.95,"skin");
    }
    private void arms() {
        for(int i=0;i<8;i++){
            double t=i/12.0,y=47.85-i*.83,x=mix(-5.26,-6.80,ease(t));
            box("left_arm",x-1.04,y,-1.55,2.08,.91,4.10,"black");
        }
        for(int i=8;i<17;i++){
            double t=i/16.0,y=47.85-i*.85,x=mix(-5.26,-7.24,ease(t));
            box("left_forearm",x-1.02,y,-1.62,2.04,.91,4.13,"black");
        }
        box("left_forearm",-7.90,33.84,-4.07,1.92,2.82,2.52,"bone");
        for(int j=0;j<5;j++)box("left_forearm",-8.08,35.85+j*.94,-3.66,2.27,.58,3.12,j%2==0?"bone":"bone_shadow");
        for(int i=0;i<4;i++)box("left_forearm",-8.12,34.20+i*.48,-4.40,.85,.34,.72,"bone_shadow");
        for(int i=0;i<9;i++){
            double y=47.65-i*.93,x=mix(5.10,5.65,i/16.0);
            box("right_arm",x-1.02,y,-1.48,2.06,.99,4.10,"black");
        }
        for(int i=9;i<19;i++){
            double y=47.65-i*.94,x=mix(5.10,5.80,i/18.0);
            box("right_forearm",x-1.02,y,-1.48,2.06,.99,4.18,"black");
        }
        box("right_forearm",5.13,29.72,-2.03,1.74,2.67,3.03,"bone");
        for(int j=0;j<5;j++)box("right_forearm",4.99,33.44+j*1.00,-2.17,2.19,.56,3.19,j%2==0?"bone":"bone_shadow");
    }
    private void armor() {
        for(int side:new int[]{-1,1})for(int j=0;j<4;j++){
            double x=side*(5.0+j*.48),y=47.8-j*.83;
            box(side<0?"armor_left":"armor_right",x-.47,y,-3.8,.94,.72,4.3,j%2==0?"bone":"bone_shadow");
        }
        for(int side:new int[]{-1,1}){
            String part=side<0?"armor_left":"armor_right";
            for(int row=0;row<6;row++){
                double y=49.0-row*.76,cx=side*(4.69+row*.28),w=3.18-row*.32;
                box(part,cx-w/2,y,-3.42,w,.72,5.7-row*.24,row%2==0?"bone":"bone_shadow");
                box(part,side*(5.94+row*.21)-.28,y-.35,-3.02,.57,.61,1.52,"bone_shadow");
            }
        }
        for(int i=0;i<6;i++){
            double y=47.92-i*1.40,t=Math.max(0,Math.min(1,(y-41.4)/6.20));
            double surface=-5.12-3.05*Math.pow(Math.sin(Math.PI*t),.75)-.46;
            for(int side:new int[]{-1,1})for(int k=0;k<6;k++){
                double x=side*(.47+k*.66),z=surface+.11*k+.038*k*k;
                box("ribcage",x-.37,y-(k>3?.18:0),z,.74,.52,.80,k%4==0?"bone":"bone_shadow");
            }
        }
        for(int i=0;i<13;i++){
            double y=48.8-i*.98,t=Math.max(0,Math.min(1,(y-41.4)/6.2));
            double z=-5.42-3.0*Math.pow(Math.sin(Math.PI*t),.75);
            box("ribcage",-.34,y,z,.68,1.04,.83,i%3==0?"bone_shadow":"bone");
        }
        box("ribcage",-3.35,49.16,-5.13,6.70,.61,.9,"bone");
    }
    private void skull() {
        double[] widths={3.95,4.35,5.18,6.20,7.06,7.42,7.66,7.5,7.21,6.62,5.68,4.4};
        for(int i=0;i<widths.length;i++){
            double y=48.84+i*.76,w=widths[i],dep=4.45+.33*Math.sin(Math.PI*i/11.0);
            box("head",-w/2,y,-3.68,w,.80,dep,i%4==0?"bone_shadow":"bone");
        }
        for(int i=0;i<12;i++){
            double y=48.95+i*.80, w=7.72+.39*Math.sin(i*.27);
            box("hair_crown",-w/2,y,-2.23,w,.84,5.25,i%3==0?"hair_hi":"hair");
        }
        for(int side:new int[]{-1,1})for(int j=0;j<11;j++){
            double x=side*(3.62+.055*j),y=49.40+j*.84;
            box("hair_side",x-.83,y,-3.34,1.37,.92,4.38,j%4==0?"hair_hi":"hair");
        }
        for(int side:new int[]{-1,1}){
            box("head",side*2.32-.63,53.13,-4.32,1.26,.81,.84,"bone");
            box("head",side*2.41-.55,52.06,-4.02,1.13,.80,.80,"bone_shadow");
            box("head",side*1.38-.78,56.24,-4.29,1.56,.55,.73,"bone");
            box("eyes",side*1.39-.79,54.19,-4.54,1.57,1.18,.36,"socket");
            box("eyes",side*1.39-.40,54.49,-4.90,.78,.55,.24,"red_eye");
            box("eyes",side*1.39-.13,54.77,-5.04,.23,.16,.12,"eye_glint");
        }
        box("head",-.34,52.91,-4.52,.68,.91,.40,"socket");
        box("head",-.52,52.17,-4.46,1.04,.39,.44,"bone_shadow");
        box("jaw",-1.82,49.79,-4.12,3.64,.95,.86,"socket");
        for(int i=0;i<7;i++)box("jaw",-1.66+i*.48,49.68+(i%2)*.07,-4.62,.34,.81,.38,"bone");
        box("jaw",-1.85,48.94,-4.03,3.70,.69,1.0,"bone");
    }
    private void hair() {
        for(int c=-6;c<=6;c++) {
            double xBase=c*.68;
            for(int i=0;i<=13;i++) {
                double t=i/13.0,z=-2.9+7.4*t;
                double arch=3.85*Math.pow(Math.sin(Math.PI*t),.72);
                double y=57.25+arch-.075*Math.abs(c)+.18*Math.sin(c*.74+t*4);
                double x=xBase+.22*Math.sin(t*3.3+c*.25);
                double w=.58+.12*Math.sin(Math.PI*t);
                box("hair_crown",x-w/2,y,z,w,.72,.69,(c+i)%5==0?"hair_hi":c%3==0?"hair_dark":"hair");
                if(i>2&&i<11&&c%3==0)box("hair_crown",x+.20,y-.14,z+.22,.34,.55,.49,"hair_hi");
            }
        }
        for(int layer=0;layer<4;layer++)for(int c=-5;c<=5;c++){
            double x=c*.79+.17*Math.sin(layer*.9+c*.6);
            double z=-.40+layer*1.08;
            double headRound=Math.max(.20,1.0-Math.pow(Math.abs(c)/6.0,2));
            double y=57.32+layer*.32+1.42*headRound+.13*Math.sin(c*.8+layer);
            box("hair_crown",x-.44,y,z,.89,1.04,1.15,(layer+c)%4==0?"hair_hi":(c%3==0?"hair_dark":"hair"));
            if(layer%2==0)box("hair_crown",x-.30,y+1.0,z+.20,.61,.62,.78,"hair_hi");
        }
        for(int side:new int[]{-1,1})for(int lock=0;lock<4;lock++) {
            int n=lock==0?15:lock==1?13:lock==2?11:9;
            double z0=-1.92+lock*.90;
            for(int i=0;i<n;i++) {
                double t=i/(double)(n-1),y=57.10-i*.92;
                double x=side*(3.39+lock*.61+.38*t+.24*Math.sin(t*5.7+lock));
                double z=z0+.29*Math.sin(t*5.5+lock*.75),w=1.37-.66*ease(t);
                box("hair_side",x-w/2,y,z,w,.99,1.54+.15*Math.cos(t*5.0),i%4==0?"hair_hi":(lock%2==0?"hair":"hair_dark"));
                if(i%3==1) box("hair_side",x-side*.14-.18,y+.12,z-.23,.45,.73,.49,"hair_hi");
            }
        }
        for(int lock=-5;lock<=5;lock++) {
            int n=12+(Math.abs(lock)%5);
            for(int i=0;i<n;i++) {
                double t=i/(double)(n-1),y=57.20-i*.97;
                double x=lock*.76+.39*Math.sin(t*5+lock*.27);
                double z=2.10+.40*(Math.abs(lock)%3)+.25*Math.sin(t*3.1+lock);
                double w=1.03-.46*ease(t);
                box("hair_back",x-w/2,y,z,w,1.02,1.25,i%4==0?"hair_hi":lock%2==0?"hair":"hair_dark");
            }
        }
        for(int i=-5;i<=5;i++){
            double x=i*.67,y=57.27-.19*Math.abs(i);
            box("hair_crown",x-.24,y,-3.05,.49,1.05,.58,i%3==0?"hair_hi":"hair");
        }
    }
    private void braid() {
        for(int i=0;i<24;i++){
            double t=i/23.0,y=57.0-i*1.02;
            double x=3.13+2.45*ease(Math.min(1,t*2.9))+.18*Math.sin(i*.75);
            double z=mix(3.05,-4.33,ease(Math.min(1,t*2.9))),r=mix(1.90,.82,t);
            for(int side:new int[]{-1,1}){
                double weave=side*(i%2==0?.35:-.34)*r;
                box("braid",x-r*.43+weave,y,z-r*.60,r*.87,1.17,r*1.15,(i+side)%3==0?"hair_hi":"hair");
            }
            box("braid",x-r*.33,y+.27,z-r*.86,r*.66,.65,r*.77,i%2==0?"hair_dark":"hair_hi");
        }
        box("braid",4.69,33.12,-4.13,1.49,.73,1.45,"bone_dark");
        for(int i=0;i<4;i++)box("braid",4.97+i*.09,32.33-i*.79,-3.93,.86-i*.12,.85,1.05-i*.12,i%2==0?"hair":"hair_hi");
    }
    private void scythe() {
        for(int i=0;i<67;i++)box("scythe",-7.88,5+i,-3.80,.9,1.03,.97,i%13==0?"shaft_hi":"shaft");
        box("scythe",-8.36,69.34,-4.19,1.88,2.32,1.72,"bone_dark");
        box("scythe",-8.67,71.18,-4.36,2.48,1.25,2.03,"bone");
        for(int i=0;i<23;i++){
            double t=i/22.0,x=-7.4+17.6*t,y=73.0-17.6*t*t,th=1.86-.83*t;
            box("scythe",x-.48,y-th/2,-3.87+.09*i,mix(1.62,1.04,t),th,1.34-.56*t,i%4==0?"bone":"bone_shadow");
            if(i<11)box("scythe",x+.03,y-th/2-.58,-4.22+.09*i,.50,.91,.47,"bone");
        }
        for(int i=0;i<8;i++)box("scythe",10.02+i*.16,55.02-i*1.49,-2.03,.72-i*.067,1.65,.67-i*.057,"bone");
    }
    private void make(){lowerDress();overdress();torso();arms();armor();skull();hair();braid();scythe();}
    public static List<Box> build(){GneteyaGeometryV8 g=new GneteyaGeometryV8();g.make();return List.copyOf(g.boxes);}
    public static String exportJson(){
        StringBuilder s=new StringBuilder("{\"model\":\"Gneteya V7\",\"units\":\"1/16 Minecraft block\",\"cuboids\":[");
        boolean first=true;
        for(Box b:build()){
            if(!first)s.append(',');first=false;
            s.append(String.format(Locale.ROOT,"{\"part\":\"%s\",\"x\":%.4f,\"y\":%.4f,\"z\":%.4f,\"w\":%.4f,\"h\":%.4f,\"d\":%.4f,\"material\":\"%s\"}",b.part(),b.x(),b.y(),b.z(),b.w(),b.h(),b.d(),b.material()));
        }
        return s.append("]}").toString();
    }
    public static void main(String[] args) throws Exception{if(args.length!=1)throw new IllegalArgumentException("Usage: GneteyaGeometryV8 file.json");Files.writeString(Path.of(args[0]),exportJson(),StandardCharsets.UTF_8);System.out.println("Exported "+build().size()+" real 3D Java cuboids: "+args[0]);}
}
