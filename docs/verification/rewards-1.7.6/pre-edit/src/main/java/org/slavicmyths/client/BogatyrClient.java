package org.slavicmyths.client;

import java.util.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slavicmyths.bogatyr.*;
import org.slavicmyths.bogatyr.BogatyrRules.State;

/** Only client presentation. Hits, timers, ownership and healing are server-authoritative. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class BogatyrClient {
    public static final KeyMapping OPEN=new KeyMapping("key.slavicmyths.bogatyr_menu",76,"key.categories.slavicmyths");
    public static final KeyMapping ESCAPE=new KeyMapping("key.slavicmyths.grab_escape",82,"key.categories.slavicmyths");
    @EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent public static void keys(RegisterKeyMappingsEvent e){e.register(OPEN);e.register(ESCAPE);}
        @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(BogatyrContent.ILYA.get(),HeroRenderer::new);e.registerEntityRenderer(BogatyrContent.IDOL.get(),BossRenderer::new);}
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){var mc=Minecraft.getInstance();if(mc.player==null)return;while(OPEN.consumeClick())if(mc.screen==null)mc.setScreen(new HeroScreen());while(ESCAPE.consumeClick())if(mc.screen==null)PacketDistributor.sendToServer(new BogatyrNetwork.Call(1));}
    public static final class HeroScreen extends Screen {
        public HeroScreen(){super(Component.translatable("gui.slavicmyths.171.hero_menu"));}
        @Override protected void init(){addRenderableWidget(Button.builder(Component.translatable("gui.slavicmyths.171.call"),b->{PacketDistributor.sendToServer(new BogatyrNetwork.Call(0));onClose();}).bounds(width/2-100,height/2-16,200,24).build());var future=Button.builder(Component.translatable("gui.slavicmyths.172.call_dobrynya"),b->{PacketDistributor.sendToServer(new BogatyrNetwork.Call(2));onClose();}).bounds(width/2-100,height/2+16,200,24).build();future.active=true;addRenderableWidget(future);}
        @Override public void render(net.minecraft.client.gui.GuiGraphics g,int x,int y,float partial){super.render(g,x,y,partial);g.drawCenteredString(font,title,width/2,height/2-42,0xF1D8A2);}
        @Override public boolean isPauseScreen(){return false;}
    }
    @SubscribeEvent public static void hud(RenderGuiEvent.Post e){var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||mc.screen!=null||mc.options.hideGui||!BogatyrConfig.HUD.get())return;IlyaEntity hero=null;for(var entity:mc.level.entitiesForRendering())if(entity instanceof IlyaEntity h&&mc.player.getUUID().equals(h.syncedOwner())){hero=h;break;}if(hero==null)return;var g=e.getGuiGraphics();int x=14,y=mc.getWindow().getGuiScaledHeight()-110;g.fill(x,y,x+176,y+46,0xD925211C);g.fill(x+1,y+1,x+175,y+2,0xFFA18A62);g.drawString(mc.font,hero.getName(),x+6,y+5,0xEEE1C9);g.fill(x+28,y+24,x+162,y+32,0xFF4E2926);g.fill(x+28,y+24,x+28+(int)(134*Math.clamp(hero.getHealth()/280,0,1)),y+32,hero.recovering()?0xFF897951:0xFFAC493A);g.drawString(mc.font,Component.literal((int)hero.getHealth()+" / 280"),x+105,y+14,0xDAD6C8);g.fill(x+7,y+22,x+19,y+33,0xFF969BA5);g.fill(x+9,y+33,x+17,y+36,0xFF969BA5);
        String key=switch(hero.state()){case TAUNT_CAST,UNYIELDING->"taunting";case SHIELD_RAISE,SHIELD_HOLD,PROTECTOR_DASH->"shielding";case RETREAT_START,RETREAT_MOVING,DOWN_FOR_ENCOUNTER->"retreating";case HEAL_DRINK,RECOVERY->"healing";case RETURNING->"returning";default->"guarding";};g.drawString(mc.font,Component.translatable("gui.slavicmyths.171.ilya."+key,Math.max(0,(hero.duration()-hero.elapsed()+19)/20)),x+28,y+36,0xC1B497);
    }
    public static final class HeroRenderer extends MobRenderer<IlyaEntity,Model<IlyaEntity>> {
        public HeroRenderer(EntityRendererProvider.Context c){super(c,new Model<>(IlyaMesh.create(),true),.55F);}
        public ResourceLocation getTextureLocation(IlyaEntity h){return BogatyrContent.id("textures/entity/ilya_muromets.png");}
        protected float getFlipDegrees(IlyaEntity h){return 0;}
    }
    public static final class BossRenderer extends MobRenderer<IdolishcheEntity,Model<IdolishcheEntity>> {
        private final Map<Long,Double> heights=new HashMap<>();private long heightTick=-1;
        public BossRenderer(EntityRendererProvider.Context c){super(c,new Model<>(IdolishcheMesh.create(),false),2.1F);}
        public ResourceLocation getTextureLocation(IdolishcheEntity h){return BogatyrContent.id("textures/entity/idolishche_poganoe.png");}
        protected float getFlipDegrees(IdolishcheEntity h){return 0;}
        @Override public void render(IdolishcheEntity b,float yaw,float partial,PoseStack p,MultiBufferSource buffers,int light){super.render(b,yaw,partial,p,buffers,light);if(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceToSqr(b.position())>96*96)return;var s=b.state();long tick=b.level().getGameTime()/10;if(tick!=heightTick){heights.clear();heightTick=tick;}var v=buffers.getBuffer(RenderType.entityTranslucent(BogatyrContent.id("textures/entity/bogatyr_warning.png")));
            if(b.encounterActive()){var origin=b.arenaOrigin().subtract(b.position());for(int ring=0;ring<2;ring++)for(int i=0;i<64;i++){double a=i*Math.PI/32,c=(i+.6)*Math.PI/32,r=26+ring*2;quad(v,p,b,origin.x+Math.cos(a)*(r-.06),origin.z+Math.sin(a)*(r-.06),origin.x+Math.cos(a)*(r+.06),origin.z+Math.sin(a)*(r+.06),origin.x+Math.cos(c)*(r+.06),origin.z+Math.sin(c)*(r+.06),origin.x+Math.cos(c)*(r-.06),origin.z+Math.sin(c)*(r-.06),0xB3A586,.35F);}}if(s!=State.STOMP_TELEGRAPH&&s!=State.STOMP_PULSE&&s!=State.CHARGE_WINDUP&&s!=State.GRAB_START&&s!=State.BREATH_WINDUP&&s!=State.FIST_WINDUP)return;
            if(s==State.STOMP_TELEGRAPH||s==State.STOMP_PULSE){int[] colors={0xB94837,0xD88B40,0xC8B967};double yawRad=Math.toRadians(b.getYRot()),ox=-Math.sin(yawRad),oz=Math.cos(yawRad);for(int ring=0;ring<3;ring++)for(int line=0;line<3-ring;line++){double radius=(ring+1)*5-line*.18;for(int i=0;i<64;i++){if(i%(5-ring)==0)continue;double a=i*Math.PI/32,c=(i+.7)*Math.PI/32;quad(v,p,b,ox+Math.cos(a)*(radius-.06),oz+Math.sin(a)*(radius-.06),ox+Math.cos(a)*(radius+.06),oz+Math.sin(a)*(radius+.06),ox+Math.cos(c)*(radius+.06),oz+Math.sin(c)*(radius+.06),ox+Math.cos(c)*(radius-.06),oz+Math.sin(c)*(radius-.06),colors[ring],s==State.STOMP_PULSE?.65F:.35F);}}}
            else{double a=Math.toRadians(s==State.CHARGE_WINDUP?b.lockedYaw():b.getYRot()),dx=-Math.sin(a),dz=Math.cos(a);double length=s==State.CHARGE_WINDUP?17:s==State.BREATH_WINDUP?13:8;double width=s==State.CHARGE_WINDUP?2.2:s==State.GRAB_START?length*Math.tan(Math.toRadians(31)):length*Math.tan(Math.toRadians(37.5));int color=s==State.BREATH_WINDUP?0x879F43:0xB94837;for(int i=0;i<12;i++){double t=i/12.0,u=(i+.65)/12;double wt=s==State.CHARGE_WINDUP?width:width*t,wu=s==State.CHARGE_WINDUP?width:width*u;quad(v,p,b,dx*length*t-dz*wt,dz*length*t+dx*wt,dx*length*t+dz*wt,dz*length*t-dx*wt,dx*length*u+dz*wu,dz*length*u-dx*wu,dx*length*u-dz*wu,dz*length*u+dx*wu,color,.28F);}}
        }
        private double height(IdolishcheEntity b,double x,double z){var pos=BlockPos.containing(b.getX()+x,b.getY(),b.getZ()+z);return heights.computeIfAbsent(pos.asLong(),key->{if(!b.level().hasChunkAt(pos))return Double.NaN;for(int dy=2;dy>=-3;dy--){var floor=pos.offset(0,dy-1,0);var air=floor.above();if(!b.level().getBlockState(floor).getCollisionShape(b.level(),floor).isEmpty()&&b.level().getBlockState(air).getCollisionShape(b.level(),air).isEmpty())return floor.getY()+1.025-b.getY();}return Double.NaN;});}
        private void quad(VertexConsumer v,PoseStack p,IdolishcheEntity b,double x1,double z1,double x2,double z2,double x3,double z3,double x4,double z4,int color,float alpha){double[][] points={{x1,z1},{x2,z2},{x3,z3},{x4,z4}};double[] ys=new double[4];for(int i=0;i<4;i++){ys[i]=height(b,points[i][0],points[i][1]);if(!Double.isFinite(ys[i]))return;}for(int i=0;i<4;i++)v.addVertex(p.last().pose(),(float)points[i][0],(float)ys[i],(float)points[i][1]).setColor((color>>16&255)/255F,(color>>8&255)/255F,(color&255)/255F,alpha).setUv(i==0||i==3?0:1,i<2?0:1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(p.last(),0,1,0);}
    }
    public static final class Model<T extends CombatEntity> extends EntityModel<T> {
        public final ModelPart root;public final Map<String,ModelPart> nodes=new HashMap<>();private final boolean hero;
        public Model(ModelPart root,boolean hero){this.root=root;this.hero=hero;index("root",root);}
        private void index(String key,ModelPart p){nodes.put(key,p);for(String name:hero?List.of("hips","body","neck","head","right_thigh","right_shin","right_foot","left_thigh","left_shin","left_foot","right_shoulder","right_arm","right_hand","left_shoulder","left_arm","left_hand","shield","mace","flask"):List.of("hips","belly","chest","head","jaw","right_thigh","right_shin","right_foot","left_thigh","left_shin","left_foot","right_shoulder","right_arm","right_forearm","right_hand","left_shoulder","left_arm","left_forearm","left_hand","right_finger_0","right_finger_1","right_finger_2","right_finger_3","left_finger_0","left_finger_1","left_finger_2","left_finger_3","right_thumb","left_thumb","rack","trophy_0","trophy_1","trophy_2","trophy_3","belt_trophy_0","belt_trophy_1","belt_trophy_2","rage_eye_0","rage_eye_2"))if(p.hasChild(name))index(name,p.getChild(name));}
        ModelPart n(String key){return nodes.get(key);}static float rad(float d){return d*Mth.DEG_TO_RAD;}static float ramp(float t,float a,float b){float u=Math.clamp((t-a)/(b-a),0,1);return u*u*(3-2*u);}static float key(float t,float[] ts,float[] vs){if(t<=ts[0])return vs[0];for(int i=1;i<ts.length;i++)if(t<=ts[i])return Mth.lerp(ramp(t,ts[i-1],ts[i]),vs[i-1],vs[i]);return vs[vs.length-1];}
        public void pose(State s,float t,float swing,float movement,float age,float yaw,float pitch,boolean rage){root.getAllParts().forEach(ModelPart::resetPose);float gait=Mth.cos(swing*.65F)*Math.clamp(movement,0,1);n("head").yRot=rad(Math.clamp(yaw,hero?-50:-30,hero?50:30));n("head").xRot=rad(Math.clamp(pitch,-22,22));n(hero?"body":"chest").xRot=Mth.sin(age*.08F)*.02F;for(int sign:new int[]{-1,1}){String tag=sign<0?"right":"left";n(tag+"_thigh").xRot=gait*sign*(hero?.34F:.25F);n(tag+"_shin").xRot=Math.max(0,-gait*sign)*.3F;n(tag+"_shoulder").xRot=-gait*sign*.14F;}
            if(hero)heroPose(s,t,age);else bossPose(s,t,age,rage);
        }
        private void heroPose(State s,float t,float age){n("flask").visible=s==State.HEAL_DRINK;n("mace").visible=s!=State.HEAL_DRINK;float shield=s==State.SHIELD_RAISE?ramp(t,0,12):s==State.SHIELD_HOLD||s==State.PROTECTOR_DASH||s==State.RETREAT_START?1:s==State.SHIELD_RECOVER?1-ramp(t,0,10):0;if(shield>0){n("left_shoulder").xRot=-rad(65)*shield;n("left_arm").yRot=-rad(20)*shield;n("body").xRot+=rad(8)*shield;n("left_thigh").zRot=rad(7)*shield;n("right_thigh").zRot=-rad(7)*shield;n("shield").xRot=rad(60)*shield;}
            switch(s){case TAUNT_CAST->{n("right_shoulder").xRot=rad(key(t,new float[]{0,6,12,15,22,28},new float[]{0,-35,-70,-70,-20,0}));n("body").yRot=rad(8)*(ramp(t,0,12)-ramp(t,15,28));n("head").xRot-=rad(15)*(ramp(t,0,12)-ramp(t,15,28));}
                case MACE_WINDUP->{n("right_shoulder").xRot=rad(key(t,new float[]{0,7,14,19,23,31},new float[]{0,-98,-120,42,80,0}));n("body").yRot=rad(key(t,new float[]{0,14,19,31},new float[]{0,-20,26,0}));}
                case SHOCKWAVE_CAST->{n("right_shoulder").xRot=rad(key(t,new float[]{0,14,24,29,46},new float[]{0,-155,-155,70,0}));n("right_shin").xRot=n("left_shin").xRot=rad(12)*(ramp(t,14,24)-ramp(t,29,46));}
                case UNYIELDING->{n("body").xRot=rad(15)*(1-ramp(t,8,20));n("right_shoulder").xRot=-rad(70)*(ramp(t,8,16)-ramp(t,20,30));n("left_shoulder").xRot=-rad(65)*ramp(t,0,8);}
                case RETREAT_START,RETREAT_MOVING->{n("body").xRot=rad(10);n("left_shoulder").xRot=-rad(50);}
                case HEAL_DRINK,RECOVERY,DOWN_FOR_ENCOUNTER->{n("hips").y+=5;n("right_thigh").xRot=-rad(60);n("right_shin").xRot=rad(100);n("left_thigh").xRot=-rad(15);n("left_shin").xRot=rad(80);n("head").xRot+=rad(12);if(s==State.HEAL_DRINK){n("right_shoulder").xRot=rad(key(t,new float[]{0,9,14,38,45,54},new float[]{0,-130,-130,-130,-30,0}));n("right_hand").xRot=-rad(60)*(ramp(t,9,14)-ramp(t,38,45));n("flask").xRot=rad(60)*(ramp(t,9,14)-ramp(t,38,45));}}
                case RETURNING->{float k=1-ramp(t,0,22);n("hips").y+=5*k;n("right_shin").xRot=rad(80)*k;n("right_shoulder").xRot=-rad(30)*ramp(t,12,22);}
                case PROTECTOR_DASH->{n("body").xRot+=rad(15);}
                default->{}
            }
        }
        private void bossPose(State s,float t,float age,boolean rage){n("belly").zScale=1+Mth.sin(age*.07F)*.009F;for(int i=0;i<4;i++)n("trophy_"+i).zRot=Mth.sin(age*.05F+i)*.1F;for(int i=0;i<3;i++)n("belt_trophy_"+i).xRot=Mth.sin(age*.06F+i)*.08F;float jaw=.06F+Mth.sin(age*.07F)*.025F;
            switch(s){case FIST_WINDUP->{n("right_shoulder").xRot=rad(key(t,new float[]{0,6,15,22},new float[]{0,-35,-65,-95}));n("right_forearm").xRot=rad(65)*ramp(t,6,15);n("chest").yRot=-rad(12)*ramp(t,0,15);}
                case FIST_SLAM->{n("right_shoulder").xRot=rad(key(t,new float[]{0,5,8,12,17},new float[]{-95,82,95,45,0}));n("chest").xRot=rad(14)*(ramp(t,0,8)-ramp(t,8,17));}
                case STOMP_TELEGRAPH->{n("right_thigh").xRot=-rad(30)*ramp(t,0,14);n("right_shin").xRot=rad(60)*ramp(t,0,14);n("right_foot").y-=16*ramp(t,0,14);n("left_shoulder").zRot=rad(20)*ramp(t,0,14);}
                case STOMP_PULSE->{float k=1-ramp(t,0,3);n("right_thigh").xRot=-rad(30)*k;n("right_shin").xRot=rad(60)*k;n("right_foot").y-=16*k;n("hips").y+=4*(ramp(t,0,3)-ramp(t,3,22));}
                case GRAB_START,GRAB_HOLD,GRAB_THROW->{float open=s==State.GRAB_START?ramp(t,0,25):s==State.GRAB_HOLD?1-ramp(t,8,12):ramp(t,8,12);n("right_shoulder").xRot=-rad(s==State.GRAB_THROW?key(t,new float[]{0,8,12,24},new float[]{70,75,15,0}):s==State.GRAB_HOLD?key(t,new float[]{0,8,18},new float[]{45,65,90}):45*ramp(t,0,25));n("right_forearm").xRot=-rad(38);for(int i=0;i<4;i++)n("right_finger_"+i).xRot=rad(60)*(1-open);n("right_thumb").zRot=rad(25)*open;}
                case FEED_SUMMON->{n("chest").xRot=rad(20)*(ramp(t,0,20)-ramp(t,29,50));n("left_shoulder").xRot=rad(65)*(ramp(t,0,20)-ramp(t,29,50));}
                case FEED_GRAB,FEED_CHEW->{float k=s==State.FEED_GRAB?ramp(t,0,24):1-ramp(t,40,52);n("right_shoulder").xRot=-rad(112)*k;n("right_forearm").xRot=-rad(55)*k;for(int i=0;i<4;i++)n("right_finger_"+i).xRot=rad(60)*k;jaw=rad(s==State.FEED_GRAB?15*k:t<18?36:25+11*(Mth.sin((t-18)*Mth.PI/6)+1)/2)*(s==State.FEED_CHEW?1-ramp(t,40,52):1);}
                case BREATH_WINDUP,BREATH_ACTIVE->{float k=s==State.BREATH_WINDUP?ramp(t,0,25):1-ramp(t,55,60);jaw=rad(36)*k;n("belly").zScale+=.07F*k;n("chest").xRot=-rad(8)*k;if(s==State.BREATH_ACTIVE)n("head").yRot+=rad(key(t,new float[]{0,8,30,55,60},new float[]{0,-12,12,0,0}));}
                case CHARGE_WINDUP,CHARGE_RUNNING,CONTEST->{n("chest").xRot=rad(s==State.CHARGE_WINDUP?18*ramp(t,0,28):23);n("head").xRot+=rad(20);if(s==State.CHARGE_RUNNING){n("right_thigh").xRot=rad(27)*Mth.cos(t*Mth.PI/10);n("left_thigh").xRot=-n("right_thigh").xRot;}if(s==State.CONTEST)n("right_shoulder").xRot=-rad(45);}
                case STUNNED->{n("head").xRot=rad(25);n("chest").xRot=rad(8)+Mth.sin(t*.12F)*.05F;jaw=rad(18);n("left_shoulder").zRot=rad(8);}
                case RAGE_TRANSITION->{n("chest").xRot=rad(key(t,new float[]{0,15,25,40,65},new float[]{0,35,35,-8,0}));n("right_shoulder").xRot=rad(45)*(ramp(t,0,15)-ramp(t,40,65));jaw=rad(40)*(ramp(t,15,25)-ramp(t,40,65));}
                case DEATH->{n("right_shin").xRot=rad(90)*ramp(t,0,25);n("left_shin").xRot=rad(90)*ramp(t,25,43);n("hips").y+=32*ramp(t,25,65);n("hips").zRot=rad(78)*ramp(t,43,88);n("left_shoulder").xRot=-rad(65)*ramp(t,25,43);jaw=rad(28)*ramp(t,65,88);n("rack").zRot=rad(10)*ramp(t,65,88);}
                default->{}
            }if(s!=State.DEATH){var arm=BogatyrMotion.arm(s,t,age);n("chest").xRot=arm.chestX();n("chest").yRot=arm.chestY();n("right_shoulder").xRot=arm.shoulder();n("right_forearm").xRot=arm.forearm();n("belly").zScale=arm.bellyScale();}n("rage_eye_0").visible=n("rage_eye_2").visible=rage;n("jaw").xRot=jaw;if(rage)n("head").xRot+=rad(3);
        }
        @Override public void setupAnim(T e,float swing,float movement,float age,float yaw,float pitch){pose(e.state(),e.age(age-e.tickCount),swing,movement,age,yaw,pitch,e instanceof IdolishcheEntity b&&b.rage());if(e.hurtTime>0){float k=Math.clamp(e.hurtTime/10F,0,1);n("head").xRot+=rad(hero?8:3)*k;if(hero&&e.state()==State.SHIELD_HOLD)n("shield").xRot+=rad(8)*k;else n(hero?"body":"chest").xRot+=rad(hero?10:3)*k;}}
        @Override public void renderToBuffer(PoseStack p,VertexConsumer v,int light,int overlay,int color){root.render(p,v,light,overlay,color);}
    }
    private BogatyrClient(){}
}
