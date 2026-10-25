package org.slavicmyths.navya.client;

import java.util.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import org.slavicmyths.navya.NavyaContent.Kind;

/** Distinct authored voxel rigs. UV ledger is exported directly from the same source. */
public final class NavyaMesh {
    public record Box(String part,String parent,int u,int v,int sx,int sy,int sz,String material) { }
    public record Rig(ModelPart root,int atlas,List<Box> boxes,Map<String,ModelPart> nodes) { }
    private final MeshDefinition mesh=new MeshDefinition();
    private final Map<String,PartDefinition> parts=new LinkedHashMap<>();
    private final List<Box> boxes=new ArrayList<>();
    private final int atlas;
    private final List<int[]> free=new ArrayList<>();
    private NavyaMesh(int atlas) {this.atlas=atlas;parts.put("root",mesh.getRoot());free.add(new int[]{2,2,atlas-4,atlas-4});}
    private void box(String parent,String name,float px,float py,float pz,float x,float y,float z,int sx,int sy,int sz,String material) {
        int width=2*(sx+sz),height=sy+sz;
        if(width+4>atlas)throw new IllegalStateException("UV width "+name);
        int[] best=null;int score=Integer.MAX_VALUE;
        for(int[] r:free)if(width+2<=r[2] && height+2<=r[3]){int s=Math.min(r[2]-width-2,r[3]-height-2);if(s<score){best=r;score=s;}}
        if(best==null)throw new IllegalStateException("UV atlas overflow "+name);
        int u=best[0],v=best[1],w=width+2,h=height+2;var next=new ArrayList<int[]>();
        for(int[] r:free){int right=r[0]+r[2],bottom=r[1]+r[3];
            if(u>=right || u+w<=r[0] || v>=bottom || v+h<=r[1]){next.add(r);continue;}
            if(u>r[0])next.add(new int[]{r[0],r[1],u-r[0],r[3]});
            if(u+w<right)next.add(new int[]{u+w,r[1],right-u-w,r[3]});
            if(v>r[1])next.add(new int[]{r[0],r[1],r[2],v-r[1]});
            if(v+h<bottom)next.add(new int[]{r[0],v+h,r[2],bottom-v-h});
        }
        free.clear();for(int i=0;i<next.size();i++){int[] a=next.get(i);boolean contained=false;
            for(int j=0;j<next.size();j++){if(i==j)continue;int[] b=next.get(j);if(a[0]>=b[0]&&a[1]>=b[1]&&a[0]+a[2]<=b[0]+b[2]&&a[1]+a[3]<=b[1]+b[3]&&(a[2]*a[3]<b[2]*b[3]||i>j)){contained=true;break;}}
            if(!contained)free.add(a);
        }
        parts.put(name,parts.get(parent).addOrReplaceChild(name,CubeListBuilder.create().texOffs(u,v).addBox(x,y,z,sx,sy,sz),PartPose.offset(px,py,pz)));
        boxes.add(new Box(name,parent,u,v,sx,sy,sz,material));
    }
    private Rig bake() {
        var root=LayerDefinition.create(mesh,atlas,atlas).bakeRoot();var nodes=new LinkedHashMap<String,ModelPart>();nodes.put("root",root);
        for(var b:boxes)nodes.put(b.part(),nodes.get(b.parent()).getChild(b.part()));
        return new Rig(root,atlas,List.copyOf(boxes),Collections.unmodifiableMap(nodes));
    }
    public static String name(Kind kind) { return switch(kind){case NAVKA->"navka";case ROOTLING->"rootling";case CRAWLER->"bone_crawler";case WISP->"navya_wisp";case GUARDIAN->"grove_guardian";}; }
    public static Rig create(Kind kind) {
        var m=new NavyaMesh(kind==Kind.GUARDIAN||kind==Kind.CRAWLER?256:128);
        switch(kind) {
            case NAVKA -> {
                m.box("root","body",0,2,0,-3.5F,-8,-2.5F,7,12,5,"cloth");
                m.box("body","waist",0,3,0,-2.5F,0,-2,5,5,4,"cloth");
                m.box("body","collar",0,-7,0,-4.5F,-1,-3,9,2,6,"dark");
                m.box("body","head",0,-10,0,-3.5F,-6,-2.5F,7,8,5,"pale");
                m.box("head","hair_cap",0,-6,0,-4,-1,-3,8,2,6,"hair");
                m.box("head","hair_back",0,-5,2,-4,0,-.5F,8,22,2,"hair");
                m.box("head","hair_left",-3.5F,-5,0,-1.5F,0,-2,2,19,3,"hair");
                m.box("head","hair_right",3.5F,-5,0,-.5F,0,-2,2,24,3,"hair");
                m.box("head","nose",0,-2,-2.5F,-.5F,0,-.8F,1,2,1,"pale");
                for(int side:new int[]{-1,1}) {
                    String n=side<0?"left":"right";
                    m.box("head","eye_"+n,side*1.7F,-2.5F,-2.8F,-.5F,0,-.2F,1,1,1,"violet");
                    m.box("body","arm_"+n,side*4.5F,-5,0,-1,0,-1,2,11,2,"cloth");
                    m.box("arm_"+n,"forearm_"+n,0,10,0,-1,0,-1,2,10,2,"pale");
                    m.box("forearm_"+n,"claws_"+n,0,9,-.5F,-1.5F,0,-.5F,3,3,1,"pale");
                    for(int i=0;i<3;i++)m.box("claws_"+n,"digit_"+n+i,i-1,2,0,-.35F,0,-.4F,1,3+i%2,1,"pale");
                }
                m.box("forearm_right","charge",0,10,-2,-2,-2,-2,4,4,4,"violet");
                m.box("body","skirt",0,6,0,-4.5F,0,-2.5F,9,7,5,"cloth");
                for(int i=0;i<5;i++)m.box("body","rag_"+i,(i-2)*2,11,i%2==0?-2:1,-1,0,-.5F,2,6+i%3,1,"cloth");
            }
            case ROOTLING -> {
                m.box("root","body",0,2,0,-4.5F,-10,-3.5F,9,18,7,"bark");
                m.box("body","face",0,-4,-3.5F,-3,-3,-.8F,6,8,1,"dark");
                m.box("face","brow",0,-3,-.8F,-3.5F,-.5F,-.5F,7,1,1,"bark");
                m.box("body","chest_split",0,3,-3.7F,-.5F,-4,-.5F,1,8,1,"dark");
                for(int side:new int[]{-1,1}) {
                    String n=side<0?"left":"right";
                    m.box("face","eye_"+n,side*1.5F,-1,-1,-.5F,0,-.3F,1,1,1,"red");
                    m.box("body","arm_"+n,side*5,-7,0,-1.5F,0,-1.5F,3,12,3,"bark");
                    m.box("arm_"+n,"elbow_"+n,0,10,-1,-2,0,-1,4,4,3,"root");
                    m.box("arm_"+n,"forearm_"+n,0,11,0,-1.5F,0,-1.5F,3,10,3,"root");
                    for(int i=0;i<3;i++)m.box("forearm_"+n,"finger_"+n+i,(i-1)*1.5F,9,-1,-.5F,0,-.5F,1,5+i%2,1,"root");
                    m.box("body","leg_"+n,side*2.5F,7,0,-1.5F,0,-1.5F,3,13,3,"root");
                    m.box("leg_"+n,"foot_"+n,0,12,-1,-2,0,-3,4,2,5,"root");
                    m.box("body","bark_ridge_"+n,side*3.5F,-8,-2,-1,-1,-1,2,15,2,"bark");
                }
                m.box("body","shoulder_branch",-4,-8,0,-1,-5,-1,2,8,2,"bark");
                m.box("shoulder_branch","fork",0,-3,0,0,-1,-1,6,2,2,"root");
                m.box("body","back_root",0,0,3,-2,-6,-1,4,13,2,"root");
            }
            case CRAWLER -> {
                m.box("root","body",0,16,0,-5,-4,-6,10,6,13,"bone_dark");
                m.box("body","abdomen",0,-2,8,-5.5F,-4,-2,11,8,10,"bone");
                m.box("abdomen","abdomen_crown",0,-4,0,-4,-1,-1,8,2,7,"bone_dark");
                m.box("body","head",0,-1,-7,-4,-3,-5,8,6,6,"bone");
                m.box("head","jaw",0,2,-3,-3,0,-2,6,2,4,"bone_dark");
                for(int side:new int[]{-1,1}) {
                    String n=side<0?"left":"right";
                    m.box("head","eye_"+n,side*2,-1,-5.2F,-.5F,0,-.4F,1,1,1,"violet");
                    m.box("head","fang_"+n,side*2,2,-5,-.5F,0,-.5F,1,4,1,"bone");
                    for(int i=0;i<4;i++) {
                        String leg="leg_"+n+i;
                        m.box("body",leg,side*4.5F,-1,-5+i*3.5F,side>0?0:-9,-1,-1,9,2,2,"bone");
                        m.box(leg,"joint_"+n+i,side*8.5F,0,0,-1.5F,-1.5F,-1.5F,3,3,3,"bone_dark");
                        m.box(leg,"shin_"+n+i,side*8.5F,0,0,-1,0,-1,2,11,2,"bone");
                        m.box("shin_"+n+i,"toe_"+n+i,0,10,0,side>0?0:-4,0,-1,4,2,2,"bone_dark");
                    }
                }
                for(int i=0;i<4;i++)m.box("body","spine_"+i,0,-4,-4+i*3,-2.5F,-1,-1,5,2,2,"bone");
                m.box("abdomen","tail",0,0,7,-2,-1,0,4,3,4,"bone_dark");
            }
            case WISP -> {
                m.box("root","body",0,18,0,-2,-2,-2,4,4,4,"wisp_core");
                for(int i=0;i<6;i++) {
                    double a=i*Math.PI/3;
                    m.box("body","flame_"+i,(float)Math.cos(a)*2,0,(float)Math.sin(a)*2,-1,-4+i%2,0,2,7-i%3,1,"wisp");
                }
                m.box("body","spark",0,-4,0,-.5F,-1,-.5F,1,2,1,"wisp_core");
            }
            case GUARDIAN -> {
                m.box("root","body",0,-25,0,-18,-31,-12,36,56,24,"bark");
                m.box("body","held_trunk",0,-16,-28,-36,-5,-5,72,10,10,"bark");
                m.box("body","buttress_left",-17,-12,0,-6,-16,-10,8,43,20,"bark");
                m.box("body","buttress_right",17,-12,0,-2,-16,-10,8,43,20,"bark");
                m.box("body","face",0,-3,-13,-10,-11,-1,20,22,2,"dark");
                m.box("face","brow",0,-11,-2,-11,-2,-1,22,4,3,"bone_dark");
                m.box("face","jaw",0,6,-2,-8,0,-2,16,5,4,"bone_dark");
                for(int side:new int[]{-1,1}) {
                    String s=side<0?"left":"right";
                    m.box("face","cheek_"+s,side*10,-4,-1,-2,-3,-2,4,15,4,"bone_dark");
                    m.box("face","eye_"+s,side*5,-4,-2.5F,-3,0,-1,6,3,2,"red");
                    m.box("body","arm_"+s,side*23,-26,0,-6,0,-6,12,25,12,"bark");
                    m.box("arm_"+s,"shoulder_plate_"+s,side*2,0,-4,-6,-3,-2,12,9,5,"bone_dark");
                    m.box("arm_"+s,"forearm_"+s,0,23,0,-5,0,-5,10,28,10,"root");
                    for(int i=0;i<3;i++)m.box("forearm_"+s,"finger_"+s+i,(i-1)*4,25,-2,-1,0,-2,2,10-i,4,"root");
                    for(int back=0;back<2;back++)m.box("body","leg_"+s+back,side*11,25,back==0?-7:8,-5,0,-6,10,24,12,"root");
                    m.box("body","crown_"+s,side*10,-36,1,-3,-19,-3,6,25,6,"bark");
                    m.box("crown_"+s,"fork_"+s,side*3,-13,0,side>0?0:-9,-2,-2,9,4,4,"bark");
                }
                m.box("body","core_frame",0,16,-14,-8,-8,-2,16,16,4,"bone_dark");
                m.box("core_frame","core",0,0,-2.5F,-5,-6,-1,10,12,3,"ember");
            }
        }
        var rig=m.bake();
        if(kind==Kind.CRAWLER)for(int side:new int[]{-1,1})for(int i=0;i<4;i++){
            String n=side<0?"left":"right";var upper=rig.nodes().get("leg_"+n+i);upper.yRot=side*(.65F-i*.43F);upper.zRot=-side*.35F;upper.setInitialPose(upper.storePose());
            var lower=rig.nodes().get("shin_"+n+i);lower.zRot=side*.75F;lower.setInitialPose(lower.storePose());
        }
        if(kind==Kind.ROOTLING)for(int side:new int[]{-1,1}){String n=side<0?"left":"right";var arm=rig.nodes().get("arm_"+n);arm.zRot=-side*.12F;arm.setInitialPose(arm.storePose());for(int i=0;i<3;i++){var finger=rig.nodes().get("finger_"+n+i);finger.zRot=(i-1)*.2F;finger.setInitialPose(finger.storePose());}}
        return rig;
    }
    public static Rig projectile(int mode) {
        var m=new NavyaMesh(mode==2?256:128);
        if(mode==2) {
            m.box("root","body",0,0,0,-5,-5,-36,10,10,72,"bark");
        } else if(mode==1) {
            m.box("root","body",0,0,0,-1,-1,-6,2,2,12,"bone");
            m.box("body","tip",0,0,-6,-.5F,-.5F,-4,1,1,4,"bone");
        } else {
            m.box("root","body",0,0,0,-3,-3,-3,6,6,6,"violet");
            m.box("body","outer",0,0,0,-1,-4,-1,2,8,2,"pale");
        }
        return m.bake();
    }
    public static Rig roots() {
        var m=new NavyaMesh(128);
        m.box("root","body",0,24,0,-12,-.5F,-12,24,1,24,"root");
        m.box("root","warning",0,23.7F,0,-11,-.3F,-11,22,1,22,"red");
        for(int i=0;i<5;i++) {
            double a=i*Math.PI*2/5;float x=(float)Math.cos(a)*8,z=(float)Math.sin(a)*8;
            m.box("body","root_"+i,x,0,z,-2,-(18+i*2),-2,4,18+i*2,4,"root");
            m.box("root_"+i,"tip_"+i,0,-(18+i*2),0,-1,-4,-1,2,5,2,"red");
        }
        return m.bake();
    }
}
