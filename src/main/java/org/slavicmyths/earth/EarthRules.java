package org.slavicmyths.earth;

/** Encounter-local balance. HP are half-hearts; time is server ticks at 20 TPS.
 * Raw damage is calibrated against diamond (20 armour, 8 toughness); servants retain iron calibration. No armour bypass. */
public final class EarthRules {
    public static double HEALTH=800, HEIGHT=6.5, SERVANT_HEALTH=18, SPEED=.18, SERVANT_SPEED=.24;
    public static int WAVE=3, CAP=6, INTERVAL=800, DORMANT_AFTER=2400, PRESSURE=100;
    public static int PILLARS=5, VOLLEY_MIN=4, VOLLEY_MAX=6, SHOT_INTERVAL=8;
    public static double PHASE_II=.70, PHASE_III=.30, RUNE_HP=2, RUNE_DAMAGE=.025, RUNE_KB=.15;
    public static final int INTRO=88, DEATH=100, GATE=38, HEAVY_GAP=16, ARMOR_DURATION=120;
    public enum Action {
        DORMANT(0,0), AWAKEN(88,0), IDLE(0,0), APPROACH(100,0),
        MELEE(24,30), PILLAR(76,280), VOLLEY(82,120), COUNTER(50,180), TREMOR(76,180),
        SUMMON(44,800), SPIKES(46,500), ARMOR(40,800), PHASE(50,0), RECOVER(32,0), DEATH(100,0);
        public final int duration;public int cooldown;
        Action(int duration,int cooldown){this.duration=duration;this.cooldown=cooldown;}
    }
    public static final float MELEE_RAW=rawForDiamond(8), PILLAR_RAW=rawForDiamond(13), SPLASH_RAW=rawForDiamond(8),
        STONE_RAW=rawForDiamond(5), WAVE_RAW=rawForDiamond(10.5), SPIKE_RAW=rawForDiamond(9), SERVANT_RAW=rawForIron(1.5);
    public static float afterArmor(float raw,float armor,float toughness){float defense=Math.min(20,Math.max(armor*.2F,armor-raw/(2+toughness/4)));return raw*(1-defense/25);}
    public static float rawForIron(double target){float low=0,high=100;for(int i=0;i<40;i++){float mid=(low+high)/2;if(afterArmor(mid,15,0)<target)low=mid;else high=mid;}return (low+high)/2;}
    public static float rawForDiamond(double target){float low=0,high=100;for(int i=0;i<40;i++){float mid=(low+high)/2;if(afterArmor(mid,20,8)<target)low=mid;else high=mid;}return (low+high)/2;}
    public static int phase(double hp,double max){return hp<=max*PHASE_III?3:hp<=max*PHASE_II?2:1;}
    public static boolean canSummon(int alive,int safe){return alive>=0&&alive+WAVE<=CAP&&safe==WAVE;}
    public static double runeDamage(int n){return 1-Math.clamp(n,0,4)*RUNE_DAMAGE;}
    public static float ramp(float t,float start,float end){float p=Math.clamp((t-start)/(end-start),0,1);return p*p*(3-2*p);}
    public static float pose(float t,float[] times,float[] values){if(t<=times[0])return values[0];for(int i=1;i<times.length;i++)if(t<=times[i])return values[i-1]+(values[i]-values[i-1])*ramp(t,times[i-1],times[i]);return values[values.length-1];}
    /** Logical release point uses the same native wrist chain as the client, in model units. */
    public static VecHand hand(Action action,float t,int volleyTotal,float bodyYaw,float bob,float scale){
        float pelvis=-14,tx=0,ty=0,shoulder=-.08F,elbow=-.12F,wrist=.08F;
        if(action==Action.PILLAR){float[] times={0,7,17,27,39,46,62,76};
            pelvis+=pose(t,times,new float[]{0,1,3,0,-1,2,1,0});tx=pose(t,times,new float[]{0,.12F,.2F,0,-.26F,.25F,.12F,0});ty=pose(t,times,new float[]{0,-.12F,-.15F,-.3F,-.4F,.3F,.15F,0});
            shoulder=pose(t,times,new float[]{0,.3F,.2F,-1.1F,-2,-.8F,-.4F,0});elbow=pose(t,times,new float[]{0,-.3F,-.8F,-1.25F,-1.4F,-.1F,0,0});wrist=-.2F;
        }else if(action==Action.VOLLEY){float end=26+(volleyTotal-1)*SHOT_INTERVAL+16,p=ramp(t,0,17)*(1-ramp(t,end-16,end));float pulse=t>=26?(float)Math.sin(Math.min(6,(t-26)%SHOT_INTERVAL)*Math.PI/6)*.12F:0;
            shoulder=-1.3F*p-pulse;elbow=-.2F*p;wrist=-.18F*p;ty=-.12F*p;
        }
        float s=(float)(HEIGHT/6.6875)*scale/16;
        var matrix=new org.joml.Matrix4f().rotateY((float)Math.toRadians(180-bodyYaw)).scale(-s,-s,s).translate(0,-24,0)
            .translate(0,pelvis,0).translate(0,-3,0).translate(0,bob,0).rotateZYX(0,ty,tx)
            .translate(-23,-32,0).rotateX(shoulder).translate(0,6,0).translate(0,18,0).rotateX(elbow)
            .translate(0,16,0).rotateX(wrist).translate(0,4,0);
        var at=matrix.transformPosition(new org.joml.Vector3f(0,7,-4));return new VecHand(at.x,at.y,at.z);
    }
    public record VecHand(double x,double y,double z){}
    private EarthRules(){}
}
