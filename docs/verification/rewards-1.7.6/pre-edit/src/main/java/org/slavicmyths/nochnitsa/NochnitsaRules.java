package org.slavicmyths.nochnitsa;

/** Fixed gameplay contract. HP are vanilla half-hearts. Pure clock used by the live entity. */
public final class NochnitsaRules {
    public static final int WINDUP=30, RELEASE=12, COOLDOWN=600, RETRY=60, ALERT_TTL=240, WRITE_GAP=40, CAP=48;
    public enum State { WANDER, SPOT, PURSUE, CLAW_LEFT, CLAW_RIGHT, WINDUP, RELEASE, HURT, DAWN, DEATH, HEAD_SNAP }
    public enum Event { NONE, HIT, INHALE, SCREAM, DAWN_REMOVE, DEATH_REMOVE }
    public static boolean night(long dayTime){long t=Math.floorMod(dayTime,24000);return t>=13000&&t<23000;}
    public static boolean inRadius(double squared){return squared<=32*32;}
    public static final class Clock {
        public State state=State.WANDER;public int age,cooldown,attackCooldown;public boolean left=true;
        public void begin(State s){state=s;age=0;}
        public boolean locked(){return state!=State.WANDER&&state!=State.PURSUE;}
        public boolean mayScream(){return state==State.PURSUE&&cooldown==0;}
        public void claw(){if(attackCooldown==0){begin(left?State.CLAW_LEFT:State.CLAW_RIGHT);left=!left;attackCooldown=24;}}
        public void abort(){if(state==State.WINDUP){cooldown=Math.max(cooldown,RETRY);begin(State.PURSUE);}}
        public boolean hurt(){boolean interrupted=state==State.WINDUP;if(interrupted)cooldown=Math.max(cooldown,RETRY);if(state!=State.DAWN&&state!=State.DEATH)begin(State.HURT);return interrupted;}
        public Event tick(){cooldown=Math.max(0,cooldown-1);attackCooldown=Math.max(0,attackCooldown-1);age++;
            return switch(state){
                case WINDUP->{if(age==10)yield Event.INHALE;if(age==WINDUP){cooldown=COOLDOWN;begin(State.RELEASE);yield Event.SCREAM;}yield Event.NONE;}
                case CLAW_LEFT,CLAW_RIGHT->{if(age==10)yield Event.HIT;if(age>=16)begin(State.PURSUE);yield Event.NONE;}
                case SPOT,RELEASE->{if(age>=12)begin(State.PURSUE);yield Event.NONE;}
                case HURT->{if(age>=10)begin(State.PURSUE);yield Event.NONE;}
                case HEAD_SNAP->{if(age>=16)begin(State.WANDER);yield Event.NONE;}
                case DAWN->age>=30?Event.DAWN_REMOVE:Event.NONE;
                case DEATH->age>=24?Event.DEATH_REMOVE:Event.NONE;
                default->Event.NONE;
            };
        }
    }
    private NochnitsaRules(){}
}
