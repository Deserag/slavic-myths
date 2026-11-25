package org.slavicmyths.desert;

/** Shared server/client animation and encounter contract; times in game ticks. */
public final class AntRules {
 public static final int LEVEL=6, IDLE=0,WALK=1,BITE=2,LEGS=3,ABDOMEN=4,SUMMON=5,AIR_SUMMON=6,PHEROMONE=7,HURT=8,DEATH=9,EMERGE=10,TAKEOFF=11,FLIGHT=12,DIVE=13,LAND=14;
 public static final int QUEEN=0,WORKER=1,FLYING=2;
 public static final double QUEEN_HP=650,WORKER_HP=10,FLYING_HP=8;
 public static int phase(float fraction){return fraction<=.30F?3:fraction<=.65F?2:1;}
 public static int duration(int a){return switch(a){case BITE->25;case LEGS->32;case ABDOMEN->46;case SUMMON->60;case AIR_SUMMON->84;case PHEROMONE->40;case HURT->8;case DEATH->20;case EMERGE->16;case TAKEOFF,LAND->16;case DIVE->16;case WALK->20;case FLIGHT->12;default->40;};}
 public static int impact(int a){return switch(a){case BITE->16;case LEGS->20;case ABDOMEN->30;case SUMMON->40;case AIR_SUMMON->60;case PHEROMONE->26;case DIVE->10;default->-1;};}
 public static int cooldown(int a,int phase){return switch(a){case BITE->80;case LEGS->120;case ABDOMEN->200;case SUMMON->phase==3?200:phase==2?240:300;case AIR_SUMMON->500;case PHEROMONE->700;default->0;};}
 public static int count(boolean flying,int phase){return flying?(phase==1?0:phase==2?5:7):(phase==3?15:10);}
 public static int cap(boolean flying,int phase){return flying?(phase==1?0:phase==2?10:15):(phase==3?40:phase==2?30:20);}
 public static float damage(int a){return switch(a){case BITE->20;case LEGS->16;case ABDOMEN->24;default->0;};}
 private AntRules(){}
}
