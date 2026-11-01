package org.slavicmyths.morovoy;

public final class MorovoyZmeyRulesSmoke {
 private static int checks;
 private static void test(boolean condition,String name){checks++;if(!condition)throw new AssertionError(name);}
 public static void main(String[] args){
  test(MorovoyZmeyRules.EFFECTIVE_HP==2200f,"boss health");
  test(MorovoyZmeyRules.shouldWake(9.25*9.25),"wake boundary");
  test(!MorovoyZmeyRules.shouldWake(9.26*9.26),"no premature wake");
  test(!MorovoyZmeyRules.shouldWake(Double.NaN),"no invalid wake");
  test(!MorovoyZmeyRules.secondPhase(1101,2200),"early phase");
  test(MorovoyZmeyRules.secondPhase(1100,2200),"phase boundary");
  test(!MorovoyZmeyRules.secondPhase(0,2200),"dying not fighting");
  test(MorovoyZmeyRules.hitsAt(1,18)&&!MorovoyZmeyRules.hitsAt(1,17),"Mor bolt window");
  test(MorovoyZmeyRules.hitsAt(2,18)&&!MorovoyZmeyRules.hitsAt(2,19),"Fire bolt window");
  test(MorovoyZmeyRules.hitsAt(3,20)&&!MorovoyZmeyRules.hitsAt(3,21),"Mor breath cadence");
  test(MorovoyZmeyRules.hitsAt(4,24)&&!MorovoyZmeyRules.hitsAt(4,56),"Fire breath end");
  test(MorovoyZmeyRules.hitsAt(5,20)&&MorovoyZmeyRules.hitsAt(5,30),"dual staggered volley");
  test(MorovoyZmeyRules.hitsAt(6,22)&&!MorovoyZmeyRules.hitsAt(6,24),"Mor tail independent");
  test(MorovoyZmeyRules.hitsAt(7,24)&&!MorovoyZmeyRules.hitsAt(7,22),"fire tail independent");
  test(MorovoyZmeyRules.hitsAt(8,20)&&MorovoyZmeyRules.hitsAt(9,20),"two bites");
  test(MorovoyZmeyRules.hitsAt(10,17),"rare claw");
  test(MorovoyZmeyRules.hitsAt(11,35),"dive");
  test(MorovoyZmeyRules.hitsAt(12,20)&&!MorovoyZmeyRules.hitsAt(12,21),"flight fields");
  test(MorovoyZmeyRules.fieldLife(999)==120&&MorovoyZmeyRules.fieldLife(1)==20,"no infinite fields");
  System.out.println("MorovoyZmeyRulesSmoke PASS "+checks+" checks (pure Java 21; NOT NeoForge gameplay QA)");
 }
}
