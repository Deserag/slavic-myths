import org.slavicmyths.sisters.SistersArmorRules;
public final class ArmorFixChecks {
 static int checks;
 static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 public static void main(String[] args){
  boolean snapshot=SistersArmorRules.flightBaseline(true,true);
  check(!SistersArmorRules.afterRune(snapshot,false),"Removing armor during rune must not leave permanent flight");
  check(SistersArmorRules.afterRune(snapshot,true),"Rune expiry must preserve a currently complete armor set");
  check(SistersArmorRules.afterRune(SistersArmorRules.flightBaseline(true,false),false),"Preserve pre-existing independent flight");
  check(!SistersArmorRules.afterRune(SistersArmorRules.flightBaseline(false,false),false),"Rune alone must expire");
  for(int amplifier=-1;amplifier<=5;amplifier++)for(boolean aura:new boolean[]{false,true}){
   double potion=amplifier<0?1:1+.2*(amplifier+1);
   double combined=potion*(1+SistersArmorRules.speedSupplement(amplifier,true,aura));
   check(Math.abs(combined-Math.max(potion,aura?1.8:1.6))<1e-8,"Potion and set must not multiply into an unintended speed buff");
   check(SistersArmorRules.speedSupplement(amplifier,false,aura)==0,"Unequip must remove all armor speed without changing the potion");
  }
  System.out.println("ARMOR_FIX_CHECKS_PASS="+checks);
 }
}
