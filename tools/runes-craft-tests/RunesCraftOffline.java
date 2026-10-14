import java.util.*;
import org.slavicmyths.rpg.*;
import org.slavicmyths.item.ItemState.RuneState;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonParser;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.ByteBufCodecs;
import io.netty.buffer.Unpooled;

/** Native codecs and shared state-machine rules. No world, mod loader or game launch. */
public class RunesCraftOffline {
 static int checks;
 static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static void near(double a,double b,String s){check(Math.abs(a-b)<1e-9,s+" got "+a);}
 public static void main(String[] args){
  for(int tier=-1;tier<=6;tier++)for(String mat:List.of("iron","gold","diamond","perunite","unknown","")){
   boolean valid=tier==1&&(mat.equals("iron")||mat.equals("gold"))||tier==2&&(mat.equals("diamond")||mat.equals("perunite"))||(tier==3||tier==4)&&mat.equals("perunite");
   check(RuneBase.valid(tier,mat)==valid,"tier/material boundary");
   var raw=JsonParser.parseString("{\"tier\":"+tier+",\"material\":\""+mat+"\"}");
   check(RuneBase.CODEC.parse(JsonOps.INSTANCE,raw).result().isPresent()==valid,"reject invalid codec");
   if(valid){var base=new RuneBase(tier,mat);check(RuneBase.CODEC.parse(NbtOps.INSTANCE,RuneBase.CODEC.encodeStart(NbtOps.INSTANCE,base).getOrThrow()).getOrThrow().equals(base),"NBT base");var codec=ByteBufCodecs.fromCodec(RuneBase.CODEC);var buf=Unpooled.buffer();try{codec.encode(buf,base);check(codec.decode(buf).equals(base),"wire base");}finally{buf.release();}}
  }
  for(String id:CraftRuneRules.IDS){
   check(Runes.installationReason(1,3,3,List.of(id),id,true).equals("duplicate_rune"),"duplicate IV rejected");
   check(Runes.installationReason(1,3,3,List.of(),id,false).equals("incompatible_rune"),"wrong slot");
   check(Runes.installationReason(2,3,3,List.of(),id,true).equals("unsupported"),"stack rejected");
   check(Runes.installationReason(1,3,3,List.of("earth_lord_iv"),id,true).isEmpty(),"crafted IV coexists with trophy");
   for(String other:CraftRuneRules.IDS)if(!id.equals(other))check(Runes.installationReason(1,3,3,List.of(other),id,true).isEmpty(),"distinct IV admitted with free sockets");
  }
  for(var runes:List.of(List.of("heat","wind","thunder"),List.of("earth_lord_iv","veles_oath_iv"),List.of("mastery_iv","depths_iii","fortune_1"),List.of("forest_spirit_iv","forest_spirit_iv"))){var state=new RuneState(3,runes);check(RuneState.CODEC.parse(NbtOps.INSTANCE,RuneState.CODEC.encodeStart(NbtOps.INSTANCE,state).getOrThrow()).getOrThrow().equals(state),"preserve old and duplicate data without migration");}
  check(!CraftRuneRules.defense(7.999,true)&&CraftRuneRules.defense(8,true)&&!CraftRuneRules.defense(8,false)&&!CraftRuneRules.defense(0,true),"defense boundary");
  for(int incoming=0;incoming<=30;incoming++)for(int absorption=0;absorption<=30;absorption++){
   double health=Math.max(0,incoming-absorption);near(CraftRuneRules.healthDamage(incoming,absorption),health,"native Pre is before absorption");
   if(CraftRuneRules.defense(health,true)){
    var container=new net.neoforged.neoforge.common.damagesource.DamageContainer(null,incoming);
    var pre=new net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Pre(null,container);
    pre.setNewDamage((float)CraftRuneRules.defendedDamage(pre.getNewDamage(),absorption));
    container.setReduction(net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.ABSORPTION,Math.min(absorption,pre.getNewDamage()));
    var post=new net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post(null,container);
    near(post.getNewDamage(),health*.75,"native damage container retains absorption and reduces HP exactly");
   }
  }
  near(8*.75,6,"post-armor reduction");check(CraftRuneRules.DEFENSE_CD==1000,"shared defense CD");
  for(boolean one:new boolean[]{false,true})for(boolean two:new boolean[]{false,true})for(boolean depths:new boolean[]{false,true})for(boolean mastery:new boolean[]{false,true}){double expected=1+Math.min(mastery?.35:.30,(two?.20:one?.10:0)+(depths?.20:0)+(mastery?.25:0));near(CraftRuneRules.mining(one,two,depths,mastery),expected,"mining combinations");}
  near(CraftRuneRules.mining(false,false,false,true),1.25,"mastery alone");near(CraftRuneRules.mining(false,true,true,true),1.35,"all mining capped");
  for(int base=0;base<=8;base++)for(boolean fortune:new boolean[]{false,true})for(boolean mastery:new boolean[]{false,true})for(boolean silk:new boolean[]{false,true})check(CraftRuneRules.fortune(base,fortune,mastery,silk)==(silk||!fortune&&!mastery?base:Math.min(4,base+(fortune?1:0)+(mastery?1:0))),"effective Fortune not a second loot roll");
  for(int s=0;s<=12;s++)for(int c=0;c<=12;c++)for(int marks=0;marks<=5;marks++){double existing=Math.min(.60,RuneBalance.STRENGTH*RuneBalance.effective(s)+RuneBalance.CRUSHING_DAMAGE*RuneBalance.effective(c));check((1+existing)*CraftRuneRules.damageFactor(existing,marks*.02,true)<=1.60000001,"combined melee cap");near(CraftRuneRules.damageFactor(existing,marks*.02,false),AdvancedRuneRules.oath(marks,existing),"old oath unchanged");}
  near(CraftRuneRules.damageFactor(0,0,true),1.16,"rage direct damage");near(CraftRuneRules.attackFactor(1,true),1.1,"rage speed");near(CraftRuneRules.attackFactor(1.3,true),1.35,"speed capped");near(CraftRuneRules.attackFactor(.8,true),.88,"multiply native speed");
  var series=new CraftRuneRules.Series();check(!series.hit(0,false,true)&&series.hits==0,"weak hit does not count");
  check(!series.hit(0,true,true)&&!series.hit(160,true,true)&&!series.hit(320,true,true)&&series.hit(480,true,true),"four hits, exact 8-second gaps");
  check(series.until==580&&!series.hit(500,true,false)&&series.until==580,"active window not refreshed");series.clear();check(series.hits==0&&series.until==0,"switch cancels");
  series.hit(1000,true,true);series.hit(1161,true,true);check(series.hits==1,"expired series reset");check(!series.hit(1162,true,false)&&series.hits==1,"cooldown blocks accumulation");
  check(!CraftRuneRules.forest(10,20,119,0,0)&&CraftRuneRules.forest(10,20,120,0,0)&&!CraftRuneRules.forest(10.01,20,120,0,0)&&!CraftRuneRules.forest(0,20,120,0,0)&&!CraftRuneRules.forest(10,20,120,0,121),"forest health/quiet/CD");
  var healing=new CraftRuneRules.Healing();healing.start(120);int pulses=0;for(int t=120;t<=500;t++){boolean pulse=healing.pulse(t);if(pulse){pulses++;check(t==120+60*pulses,"three-second cadence");}}check(pulses==4,"exact four HP");healing.start(1000);check(healing.pulse(1060),"first heal");healing.stop();check(!healing.pulse(1200),"damage/equipment cancellation");
  check(AdvancedRuneRules.lastStand(7,20,1,true),"previous agreed III threshold preserved");near(AdvancedRuneRules.mining(false,true,true),1.30,"previous III cap preserved");
  System.out.println("RUNES_CRAFT_OFFLINE_CHECKS="+checks);
 }
}
