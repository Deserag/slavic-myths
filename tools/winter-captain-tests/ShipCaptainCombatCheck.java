import java.util.*;
import org.slavicmyths.winterboss.ShipCaptainCombat;
import org.slavicmyths.winterboss.ShipCaptainCombat.Attack;
import org.slavicmyths.winterboss.ShipCaptainCombat.Kind;
public final class ShipCaptainCombatCheck {
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 public static void main(String[] args){
  Kind[] primary={Kind.MELEE,Kind.DOUBLE_MELEE,Kind.THROW,Kind.SPIN_HIT,Kind.SLAM,Kind.VOLLEY,Kind.GRASP,Kind.DASH_START};
  int[][] frames={{13},{18},{14},{12,22,32,42},{25},{20},{18},{14}};
  int assertions=0;
  for(Attack a:Attack.values()){
   var c=new ShipCaptainCombat();double d=(a.minRange+a.maxRange)/2;
   check(!c.canStart(a,d,2,false),"LOS "+a);assertions++;
   check(!c.canStart(a,a.maxRange+.01,2,true),"range "+a);assertions++;
   if(a.requiredAxes>0){check(!c.canStart(a,d,a.requiredAxes-1,true),"axes "+a);assertions++;}
   check(c.start(a,d,2,true).getFirst().kind()==Kind.SOUND,"windup "+a);assertions++;
   check(c.cooldown(a)==a.cooldown,"initial cooldown "+a);assertions++;
   check(c.start(a,d,2,true).isEmpty(),"overlap "+a);assertions++;
   var hits=new ArrayList<Integer>();int complete=0,dashEnd=0;
   for(int t=1;t<=a.duration;t++)for(var event:c.tick()){
    if(event.kind()==primary[a.ordinal()])hits.add(t);
    if(event.kind()==Kind.COMPLETE){check(t==a.duration,"completion frame "+a);assertions++;complete++;}
    if(event.kind()==Kind.DASH_END){check(t==24,"dash end");assertions++;dashEnd++;}
   }
   check(hits.equals(Arrays.stream(frames[a.ordinal()]).boxed().toList()),"hit frames "+a+" "+hits);assertions++;
   check(complete==1&&c.active()==null&&!c.ready(),"recovery "+a);assertions++;
   check(dashEnd==(a==Attack.FROST_DASH?1:0),"dash termination "+a);assertions++;
   for(int i=0;i<10;i++)check(c.tick().isEmpty(),"late hits "+a);
   assertions+=10;check(c.ready(),"ready after recovery "+a);assertions++;
   check(c.cooldown(a)==Math.max(0,a.cooldown-a.duration-10),"cooldown ticks "+a);assertions++;
   c.restoreCooldown(a,99999);check(c.cooldown(a)==a.cooldown,"restore upper "+a);assertions++;
   c.restoreCooldown(a,-1);check(c.cooldown(a)==0,"restore lower "+a);assertions++;
   c.start(a,d,2,true);c.cancel();check(c.tick().isEmpty()&&c.active()==null,"cancel "+a);assertions++;
   System.out.println("PASS "+a+" hit frames="+hits);
  }
  var c=new ShipCaptainCombat();var rng=new Random(7);for(int i=0;i<100;i++){
   var a=c.choose(10,0,true,rng);check(a==Attack.GHOST_GRASP||a==Attack.FROST_DASH,"unarmed choice");
  }assertions+=100;
  System.out.println("PASS total assertions="+assertions);
 }
}
