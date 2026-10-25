package org.slavicmyths.navya.client;

import net.minecraft.client.model.geom.ModelPart;
import org.slavicmyths.navya.*;
import org.slavicmyths.navya.NavyaContent.Kind;
import org.slavicmyths.navya.NavyaMob.Action;

/** Poses actual joint pivots, shared by production rendering and source-frame QA. */
public final class NavyaPose {
    private static final String[] ARMS={"arm_left","arm_right"},FOREARMS={"forearm_left","forearm_right"};
    private static final String[][] LEGS={{"leg_left0","leg_left1","leg_left2"},{"leg_right0","leg_right1","leg_right2"}};
    private static float clamp(float f){return Math.max(0,Math.min(1,f));}
    private static float smooth(float f){f=clamp(f);return f*f*(3-2*f);}
    public static void pose(NavyaMesh.Rig rig,Kind kind,Action action,float time,float age,float walk,float amount,float yaw,float pitch,int hurt,float death) {
        for(ModelPart part:rig.nodes().values())part.resetPose();
        var p=rig.nodes();var body=p.get("body");
        int windup=NavyaTiming.windup(kind,action),duration=NavyaTiming.duration(kind,action);
        float prepare=windup==0?0:smooth(time/windup);
        float recovery=windup==0?0:smooth((time-windup)/Math.max(1,duration-windup));
        float reach=prepare*(1-recovery);
        float strike=(float)Math.sin(Math.PI*clamp((time-windup)/7));
        float step=(float)Math.sin(walk*.65F)*amount;
        if(p.containsKey("head")){p.get("head").yRot=Math.max(-.6F,Math.min(.6F,yaw*.017453F));p.get("head").xRot=pitch*.008F;}
        if(p.containsKey("face"))p.get("face").yRot=Math.max(-.18F,Math.min(.18F,yaw*.008F));
        switch(kind) {
            case NAVKA -> {
                body.y+=(float)Math.sin(age*.07F)*.9F;body.zRot=(float)Math.sin(age*.035F)*.035F;
                p.get("hair_back").xRot=.08F+(float)Math.sin(age*.06F-1)*.05F;
                p.get("hair_left").zRot=-.04F+(float)Math.sin(age*.055F)*.06F;
                p.get("hair_right").zRot=.04F+(float)Math.sin(age*.055F-1.5F)*.07F;
                for(int i=0;i<5;i++)p.get("rag_"+i).xRot=(float)Math.sin(age*.06F-i*.7F)*.06F;
                p.get("arm_right").xRot=(action==Action.SHOT?-1.6F:action==Action.MELEE?-1.2F:0)*reach+strike*.8F;
                p.get("forearm_right").xRot=action==Action.SHOT?-.6F*reach:0;
                var charge=p.get("charge");charge.visible=action==Action.SHOT && time<windup;
                charge.xScale=charge.yScale=charge.zScale=.15F+.85F*prepare;
                p.get("arm_left").zRot=-.12F;
            }
            case ROOTLING -> {
                body.zRot=step*.08F;body.xRot=.03F;
                p.get("leg_left").xRot=step*.4F;p.get("leg_right").xRot=-step*.4F;
                for(int i=0;i<2;i++) {
                    p.get(ARMS[i]).xRot=(i==0?-step:step)*.25F;
                    if(action==Action.ROOTS || action==Action.MELEE)p.get(ARMS[i]).xRot+=-reach*(action==Action.ROOTS?1.1F:1.6F)+strike*.9F;
                    p.get(FOREARMS[i]).zRot=(i==0?-.1F:.1F)+(float)Math.sin(age*.08F)*.015F;
                }
            }
            case CRAWLER -> {
                body.y+=(float)Math.sin(walk*1.3F)*amount*.45F;
                for(int side=0;side<2;side++)for(int i=0;i<3;i++) {
                    float gait=(float)Math.sin(walk*1.1F+i*2.1F+side*Math.PI)*amount;
                    p.get(LEGS[side][i]).yRot=gait*.25F;p.get(LEGS[side][i]).zRot=(side==0?-.1F:.1F)+gait*.15F;
                }
                p.get("jaw").xRot=action==Action.MELEE?.65F*reach-strike*.25F:action==Action.SHOT?.3F*reach:.04F;
                if(action==Action.SHOT)p.get("head").xRot-=.25F*reach;
                if(action==Action.DASH)body.xRot=-.2F*reach;
                p.get("tail").yRot=(float)Math.sin(walk*.65F)*amount*.15F;
            }
            case WISP -> {
                body.y+=(float)Math.sin(age*.09F)*1.1F;body.yRot=age*.015F;
                for(int i=0;i<6;i++) {
                    var flame=p.get("flame_"+i);flame.xRot=(float)Math.sin(age*.11F+i)*.12F;flame.zRot=(float)Math.cos(age*.09F+i)*.12F;
                    flame.yScale=.8F+(float)Math.sin(age*.18F+i)*.18F;
                }
            }
            case GUARDIAN -> {
                body.xRot=(float)Math.sin(age*.035F)*.01F;body.y+=(float)Math.sin(age*.04F)*.2F;
                for(int side=0;side<2;side++) {
                    for(int i=0;i<2;i++)p.get((side==0?"leg_left":"leg_right")+i).xRot=step*(side==i?.15F:-.15F);
                    var arm=p.get(ARMS[side]);var forearm=p.get(FOREARMS[side]);
                    arm.xRot=step*(side==0?-.18F:.18F);forearm.xRot=-.08F;
                    if(action==Action.MELEE){arm.yRot=(side==0?1:-1)*(.6F*reach-strike*.9F);arm.xRot=-.5F*reach;body.yRot=-.2F*reach+strike*.3F;}
                    if(action==Action.ROOTS || action==Action.SUMMON){arm.xRot=-1.1F*reach+strike*.6F;forearm.xRot=-.25F*reach;}
                    if(action==Action.THROW){arm.xRot=-1.15F*reach;arm.zRot=(side==0?-.18F:.18F)*reach;forearm.xRot=-.4F*reach;}
                    if(action==Action.PHASE){arm.zRot=(side==0?-.22F:.22F)*prepare;body.xRot=-.08F*(float)Math.sin(prepare*Math.PI);}
                }
                var trunk=p.get("held_trunk");trunk.visible=action==Action.THROW && time<windup;
                trunk.y=49-65*prepare;
                p.get("core").xScale=p.get("core").yScale=p.get("core").zScale=1+(action==Action.PHASE?.15F*(float)Math.sin(time*.2F):.025F*(float)Math.sin(age*.1F));
            }
        }
        if(hurt>0){body.zRot+=(float)Math.sin(hurt*.6F)*.07F;body.xRot-=.08F;}
        if(death>0){float collapse=smooth(death/20);body.xRot+=collapse*(kind==Kind.CRAWLER?.3F:.6F);body.y+=collapse*(kind==Kind.GUARDIAN?12:4);body.yScale=1-collapse*.35F;}
    }
    private NavyaPose() { }
}
