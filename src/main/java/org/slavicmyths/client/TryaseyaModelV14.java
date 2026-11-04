package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import org.slavicmyths.sisters.SisterBoss;

/** Character V13. Articulated bone hierarchy, unchanged Gneteya, material atlas compatible with V10. */
public final class TryaseyaModelV14 extends EntityModel<SisterBoss> {
    public static final String[] MATERIALS = {"skin","skin_shadow","eye","eye_glint","mouth","purple_dark","purple_mid","purple_light","hair_dark","hair_mid","hair_light","hair_bright","mist","mist_hi","silver","staff","staff_dark","lantern","frame","white","white_light","white_shadow"};
    private static final Map<String, float[]> PIVOTS = Map.ofEntries(
        Map.entry("root",new float[]{0,0,0}),Map.entry("body",new float[]{0,40,0}),Map.entry("neck",new float[]{0,47,0}),
        Map.entry("head",new float[]{0,51,0}),Map.entry("eyes",new float[]{0,53,0}),Map.entry("mouth_closed",new float[]{0,51,0}),Map.entry("mouth_open",new float[]{0,51,0}),Map.entry("skirt",new float[]{0,32,0}),Map.entry("tail",new float[]{0,18,0}),
        Map.entry("hair_back",new float[]{0,55,1}),Map.entry("hair_left",new float[]{-3.3f,54,-.8f}),
        Map.entry("hair_right",new float[]{3.3f,54,-.8f}),Map.entry("bangs",new float[]{0,56,-2.5f}),
        Map.entry("left_arm",new float[]{-3.70f,44,-1}),Map.entry("left_sleeve",new float[]{-3.70f,44,-1}),
        Map.entry("left_hand",new float[]{-7.70f,39,-1}),Map.entry("staff",new float[]{-7.80f,39,-.42f}),
        Map.entry("right_arm",new float[]{3.70f,44,-1}),Map.entry("right_sleeve",new float[]{3.70f,44,-1}),
        Map.entry("right_hand",new float[]{4,32,-1}));

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart eyes;
    private final ModelPart mouthClosed;
    private final ModelPart mouthOpen;
    private final ModelPart hairBack;
    private final ModelPart hairLeft;
    private final ModelPart hairRight;
    private final ModelPart bangs;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftSleeve;
    private final ModelPart rightSleeve;
    private final ModelPart leftHand;
    private final ModelPart rightHand;
    private final ModelPart staff;
    private final ModelPart tail;
    private final ModelPart skirt;

    public TryaseyaModelV14(ModelPart baked) {
        this.root=baked.getChild("root");
        this.head=root.getChild("head");
        this.eyes=head.getChild("eyes");
        this.mouthClosed=head.getChild("mouth_closed");
        this.mouthOpen=head.getChild("mouth_open");
        this.hairBack=head.getChild("hair_back");
        this.hairLeft=head.getChild("hair_left");
        this.hairRight=head.getChild("hair_right");
        this.bangs=head.getChild("bangs");
        this.leftArm=root.getChild("left_arm");
        this.rightArm=root.getChild("right_arm");
        this.leftSleeve=leftArm.getChild("left_sleeve");
        this.rightSleeve=rightArm.getChild("right_sleeve");
        this.leftHand=leftArm.getChild("left_hand");
        this.rightHand=rightArm.getChild("right_hand");
        this.staff=leftHand.getChild("staff");
        this.tail=root.getChild("tail");
        this.skirt=root.getChild("skirt");
    }

    private static PartPose pose(String part, String parent) {
        float[] current=PIVOTS.get(part);
        float[] previous=PIVOTS.get(parent);
        return PartPose.offset(current[0]-previous[0],previous[1]-current[1],current[2]-previous[2]);
    }

    public static LayerDefinition create() {
        Map<String,CubeListBuilder> groups=new LinkedHashMap<>();
        for (TryaseyaGeometryV14.Box box : TryaseyaGeometryV14.build()) {
            int index=Arrays.asList(MATERIALS).indexOf(box.material());
            if(index<0)throw new IllegalStateException("Tryaseya V12 material not in texture palette: "+box.material());
            float[] pivot=PIVOTS.get(box.part());
            if(pivot==null)throw new IllegalStateException("Unknown animation bone: "+box.part());
            CubeListBuilder cubes=groups.computeIfAbsent(box.part(),k->CubeListBuilder.create());
            int u=(index%8)*128;
            int v=(index/8)*128;
            cubes.texOffs(u,v).addBox((float)(box.x()-pivot[0]),(float)(-box.y()-box.h()+pivot[1]),(float)(box.z()-pivot[2]),(float)box.w(),(float)box.h(),(float)box.d());
        }
        MeshDefinition mesh=new MeshDefinition();
        PartDefinition root=mesh.getRoot().addOrReplaceChild("root",CubeListBuilder.create(),PartPose.ZERO);
        root.addOrReplaceChild("body",groups.get("body"),pose("body","root"));
        root.addOrReplaceChild("neck",groups.get("neck"),pose("neck","root"));
        PartDefinition head=root.addOrReplaceChild("head",groups.get("head"),pose("head","root"));
        head.addOrReplaceChild("eyes",groups.get("eyes"),pose("eyes","head"));
        head.addOrReplaceChild("mouth_closed",groups.get("mouth_closed"),pose("mouth_closed","head"));
        head.addOrReplaceChild("mouth_open",groups.get("mouth_open"),pose("mouth_open","head"));
        head.addOrReplaceChild("hair_back",groups.get("hair_back"),pose("hair_back","head"));
        head.addOrReplaceChild("hair_left",groups.get("hair_left"),pose("hair_left","head"));
        head.addOrReplaceChild("hair_right",groups.get("hair_right"),pose("hair_right","head"));
        head.addOrReplaceChild("bangs",groups.get("bangs"),pose("bangs","head"));
        root.addOrReplaceChild("skirt",groups.get("skirt"),pose("skirt","root"));
        root.addOrReplaceChild("tail",groups.get("tail"),pose("tail","root"));
        PartDefinition left=root.addOrReplaceChild("left_arm",groups.get("left_arm"),pose("left_arm","root"));
        left.addOrReplaceChild("left_sleeve",groups.get("left_sleeve"),pose("left_sleeve","left_arm"));
        PartDefinition hand=left.addOrReplaceChild("left_hand",groups.get("left_hand"),pose("left_hand","left_arm"));
        hand.addOrReplaceChild("staff",groups.get("staff"),pose("staff","left_hand"));
        PartDefinition right=root.addOrReplaceChild("right_arm",groups.get("right_arm"),pose("right_arm","root"));
        right.addOrReplaceChild("right_sleeve",groups.get("right_sleeve"),pose("right_sleeve","right_arm"));
        right.addOrReplaceChild("right_hand",groups.get("right_hand"),pose("right_hand","right_arm"));
        return LayerDefinition.create(mesh,1024,1024);
    }

    private static float clamp(float value,float min,float max) { return Math.max(min,Math.min(max,value)); }
    private static float sin(float value) { return (float)Math.sin(value); }
    private static float idleWave(float age,float factor) { return sin(age*factor); }

    @Override public void setupAnim(SisterBoss boss,float limbSwing,float limbSwingAmount,float age,float yaw,float pitch) {
        // Restore every animated axis every frame; no accumulating rotation from attacks.
        float breath=idleWave(age,.07F);
        float hover=idleWave(age,.095F);
        root.y=hover*.37F;
        root.xRot=0;
        root.yRot=0;
        root.zRot=idleWave(age,.044F)*.013F;
        head.yRot=clamp(yaw*(float)Math.PI/180F,-.33F,.33F)*.26F;
        head.xRot=clamp(pitch*(float)Math.PI/180F,-.36F,.36F)*.18F;
        head.zRot=idleWave(age,.054F)*.012F;
        eyes.visible=((int)age%113)>2;
        mouthOpen.visible=false;
        mouthClosed.visible=true;
        hairBack.xRot=idleWave(age-.9F,.07F)*.026F;
        hairLeft.zRot=idleWave(age,.085F)*.038F;
        hairRight.zRot=-idleWave(age+1.2F,.076F)*.041F;
        bangs.xRot=idleWave(age,.08F)*.012F;
        leftArm.xRot=idleWave(age,.077F)*.026F;
        leftArm.zRot=idleWave(age,.06F)*.018F;
        rightArm.xRot=-idleWave(age+1.0F,.065F)*.038F;
        rightArm.zRot=.012F+idleWave(age,.085F)*.018F;
        leftSleeve.xRot=idleWave(age+.6F,.095F)*.041F;
        rightSleeve.xRot=idleWave(age+2F,.10F)*.041F;
        leftHand.xRot=0;
        rightHand.xRot=0;
        staff.xRot=idleWave(age,.075F)*.018F;
        staff.zRot=0;
        // Flowing dress lags *opposite* movement; calculate movement relative to boss orientation.
        // Positive local forward is the boss's facing direction (Minecraft yaw convention).
        net.minecraft.world.phys.Vec3 velocity=boss.getDeltaMovement();
        float yawR=(float)Math.toRadians(boss.getYRot());
        float localForward=(float)(velocity.z*Math.cos(yawR)-velocity.x*Math.sin(yawR));
        float localSide=(float)(velocity.x*Math.cos(yawR)+velocity.z*Math.sin(yawR));
        float flowBack=clamp(localForward*1.9F,-.23F,.23F);
        float flowSide=clamp(localSide*1.6F,-.20F,.20F);
        skirt.xRot=-flowBack+idleWave(age,.073F)*.019F;
        skirt.zRot=flowSide+idleWave(age,.055F)*.014F;
        // The mist tail follows the trailing hem, but with a longer delayed sway.
        tail.xRot=-flowBack*.25F+idleWave(age,.083F)*.07F;
        tail.zRot=flowSide*.45F+idleWave(age+.8F,.063F)*.043F;
        tail.z=flowBack*14.0F;
        if (boss.isTryaseya() && boss.action()!=SisterBoss.IDLE) {
            float frame=boss.actionFrame();
            mouthOpen.visible=(frame>7 && frame<34);
            mouthClosed.visible=!mouthOpen.visible;
            float telegraph=clamp(frame/11F,0,1);
            float release=clamp((frame-26F)/12F,0,1);
            float pose=telegraph*(1-release);
            boolean spell=boss.action()==SisterBoss.FEVER||boss.action()==SisterBoss.FLASH||boss.action()==SisterBoss.SPIRITS||boss.action()==SisterBoss.ZONE;
            head.xRot+=spell?-.10F*pose:.10F*pose;
            root.zRot+=spell?-.045F*pose:.10F*pose;
            leftArm.xRot-=spell?.33F*pose:.18F*pose;
            leftArm.zRot-=.06F*pose;
            rightArm.xRot-=spell?.20F*pose:.11F*pose;
            rightHand.xRot+=spell?.18F*pose:0;
            staff.xRot-=spell?.13F*pose:.04F*pose;
            hairLeft.zRot-=.045F*pose;
            hairRight.zRot+=.048F*pose;
            skirt.zRot+=.04F*pose;
            tail.xRot-=.07F*pose;
        }
        if(boss.hurtTime>0) head.zRot+=.095F*sin(age*.83F);
        if(boss.enraged()) { root.y+=breath*.12F;hairLeft.zRot*=1.5F;hairRight.zRot*=1.5F; }
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer out,int light,int overlay,int color) { root.render(pose,out,light,overlay,color); }
}