import static org.slavicmyths.desert.SandKingRules.*;
public class RulesCheck {
 public static void main(String[] args){
  if(LEVEL!=7||phase(1)!=1||phase(.70f)!=2||phase(.35f)!=3)throw new AssertionError("phase");
  if(canUse(COMBO,1)||!canUse(COMBO,2)||canUse(SLAM,2)||!canUse(SLAM,3))throw new AssertionError("unlock");
  if(cooldown(STING,3)!=160||cooldown(SLAM,3)!=800||COMBO_TICKS.length!=6||duration(SLAM)*.65<50)throw new AssertionError("timeline");
  if(Math.abs(speed(3)/speed(1)-1.25)>.001)throw new AssertionError("speed");
  System.out.println("Level VII / 3 phases / 5 skills / 6-hit combo / cooldowns: PASS");
 }
}
