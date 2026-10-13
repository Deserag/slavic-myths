package org.slavicmyths.client;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.world.phys.*;
import net.minecraft.client.model.geom.ModelPart;
import org.slavicmyths.morozko.*;
/** Policy, real SavedData/NBT and native ModelPart UV checks; no server, client or world. */
public final class FrostChecks {
 static int checks;
 static void check(boolean ok,String label){checks++;if(!ok)throw new AssertionError(label);}
 static void model(ModelPart p)throws Exception{var cf=ModelPart.class.getDeclaredField("cubes");cf.setAccessible(true);var hf=ModelPart.class.getDeclaredField("children");hf.setAccessible(true);var pf=ModelPart.Cube.class.getDeclaredField("polygons");pf.setAccessible(true);for(var cube:(List<?>)cf.get(p))for(var poly:(Object[])pf.get(cube)){var vf=poly.getClass().getDeclaredField("vertices");vf.setAccessible(true);for(var v:(Object[])vf.get(poly)){var uf=v.getClass().getDeclaredField("u");var vv=v.getClass().getDeclaredField("v");uf.setAccessible(true);vv.setAccessible(true);float u=uf.getFloat(v),y=vv.getFloat(v);check(Float.isFinite(u)&&Float.isFinite(y)&&u>=0&&u<=1&&y>=0&&y<=1,"native UV "+u+" "+y);}}for(var child:((Map<?,?>)hf.get(p)).values())model((ModelPart)child);}
 public static void main(String[] args)throws Exception{
  check(FrostRules.DOUBLE_HP==50&&FrostRules.MIRROR_HP==15,"fixed health");check(FrostRules.MIRROR_CD==3600&&FrostRules.MIRROR_LIFE==600,"mirror timers");
  check(FrostRules.phase(601,1000)==1&&FrostRules.phase(599,1000)==2&&FrostRules.phase(299,1000)==3&&FrostRules.phase(50,1000)==4,"phase boundaries");
  for(float hp:new float[]{50,51,60,400,1000})for(float damage:new float[]{0,1,50,900,10000,Float.MAX_VALUE})check(hp-FrostRules.clampedDamage(hp,1000,damage)>=50,"overkill cannot kill");
  for(int value=0;value<=100;value++)check(FrostRules.tier(value)==(value<25?0:value<50?1:value<75?2:value<100?3:4),"cold tier");check(FrostRules.rate(2)>FrostRules.rate(5)&&FrostRules.rate(5)>FrostRules.rate(7)&&FrostRules.rate(9)==0,"distance rates");
  check(!FrostRules.eligible(0,500,false),"AFK denied");check(!FrostRules.eligible(0,299,true),"under15s denied");check(FrostRules.eligible(0,300,true),"attacked15s eligible");check(FrostRules.eligible(1,0,false),"damage contributor");
  var rng=new Random(191);int resistance=0,staff=0;for(int i=0;i<1000;i++){var first=FrostRules.roll(true,rng);check(first.mirror(),"first mirror");if(first.resistance())resistance++;if(first.staff())staff++;var repeat=FrostRules.roll(false,rng);check(!repeat.mirror(),"no repeat mirror");}check(resistance>430&&resistance<570,"seeded rune distribution");check(staff>110&&staff<190,"independent staff distribution");
  check(!FrostRules.roll(false,true,.15).staff()&&FrostRules.roll(false,false,.14999).staff(),"exact15% threshold");
  for(int tick=0;tick<=40;tick++)check(FrostRules.spikeActive(tick)==(tick>=5&&tick<=10),"spike growth and six-tick damage window");
  for(var state:org.slavicmyths.poloz.PolozEvent.State.values())check(org.slavicmyths.poloz.PolozEvent.canStart(state)==(state==org.slavicmyths.poloz.PolozEvent.State.IDLE||state==org.slavicmyths.poloz.PolozEvent.State.DEFEATED||state==org.slavicmyths.poloz.PolozEvent.State.FAILED),"single Poloz admission including first use and repeat");
  var left=new Vec3(-2,1,0);var right=new Vec3(2,1,0);var wide=new AABB(-1.5,0,2,1.5,4,5);check(FrostWorld.bboxDistance(left,wide)<9&&FrostWorld.bboxDistance(right,wide)<9,"both sides reach wide bbox");check(FrostWorld.bboxDistance(new Vec3(-20,1,0),wide)>9,"outside reach rejected");
  var ledger=new FrostLedger();var id=UUID.randomUUID();var record=ledger.player(id);record.putBoolean("MirrorClaimed",true);record.putBoolean("Completed",true);record.putInt("Wins",3);record.putLong("MirrorReady",10000);var trial=UUID.randomUUID();ledger.session(trial).putUUID("Owner",id);var finished=UUID.randomUUID();ledger.session(finished).putBoolean("Complete",true);ledger.session(finished).put("Rewarded",new CompoundTag());ledger.session(finished).getCompound("Rewarded").putBoolean(id.toString(),true);
  var round=FrostLedger.load(ledger.save(new CompoundTag(),null),null);check(round.player(id).getBoolean("MirrorClaimed")&&round.player(id).getInt("Wins")==3&&round.player(id).getLong("MirrorReady")==10000,"durable progression and cooldown");check(round.session(trial).getBoolean("Cancelled"),"reload cancels unfinished trial");check(!round.session(finished).getBoolean("Cancelled")&&round.session(finished).getCompound("Rewarded").getBoolean(id.toString()),"finished claim survives reload");
  model(MorozkoMesh.create());System.out.println("FROST_OFFLINE_PASS checks="+checks+" seededTrials=1000; nativeModels=1; clientStarts=0 serverStarts=0 (NBT is not relog)");
 }
}
