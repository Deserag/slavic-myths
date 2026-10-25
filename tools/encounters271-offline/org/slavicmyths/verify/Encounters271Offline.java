package org.slavicmyths.verify;

import java.util.*;
import java.lang.reflect.Proxy;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import org.slavicmyths.world.EncounterPlacement;
import org.slavicmyths.wolf.EncounterRetryCache;
import org.slavicmyths.sivka.*;
import org.slavicmyths.firebird.*;
import org.slavicmyths.mor.MorRules;

public final class Encounters271Offline {
    static int checks;
    static void require(boolean ok,String name){checks++;if(!ok)throw new AssertionError(name);}
    static final class Terrain {
        Set<Long> loaded=new HashSet<>();boolean collision,fluid;int reads;
        WorldBorder border=new WorldBorder();
        LevelAccessor world=(LevelAccessor)Proxy.newProxyInstance(LevelAccessor.class.getClassLoader(),new Class<?>[]{LevelAccessor.class},(proxy,method,args)->switch(method.getName()){
            case "hasChunk"->loaded.contains(net.minecraft.world.level.ChunkPos.asLong((int)args[0],(int)args[1]));
            case "getWorldBorder"->border;
            case "noCollision"->{reads++;yield !collision;}
            case "containsAnyLiquid"->{reads++;yield fluid;}
            case "toString"->"OFFLINE sparse terrain contract";
            default->throw new AssertionError("Unexpected world access: "+method);
        });
    }
    static void geometry(){
        float[][] sizes={{1.25F,1.55F},{1.1F,2.55F},{1.15F,2.45F},{1.05F,2.25F},{1.35F,2.6F},{1.65F,2.85F}};
        for(float[] size:sizes)for(int x:new int[]{-17,-16,-1,0,15,16})for(int z:new int[]{-17,-16,-1,0,15,16}){
            var box=EncounterPlacement.body(new BlockPos(x,64,z),size[0],size[1]);var t=new Terrain();
            for(int cx=-3;cx<=3;cx++)for(int cz=-3;cz<=3;cz++)t.loaded.add(net.minecraft.world.level.ChunkPos.asLong(cx,cz));
            require(EncounterPlacement.clear(t.world,box),"body fits loaded empty terrain");
            t.collision=true;require(!EncounterPlacement.clear(t.world,box),"solid obstruction denied");t.collision=false;
            t.fluid=true;require(!EncounterPlacement.clear(t.world,box),"liquid denied");t.fluid=false;
            for(long chunk:new ArrayList<>(t.loaded)){
                t.loaded.remove(chunk);t.reads=0;
                if(!EncounterPlacement.loaded(t.world,box)){require(!EncounterPlacement.clear(t.world,box),"unloaded footprint denied");require(t.reads==0,"no terrain query through missing chunk");}
                t.loaded.add(chunk);
            }
        }
        var body=EncounterPlacement.body(new BlockPos(0,64,0),1.25F,1.55F);
        require(!body.intersects(new AABB(2,64,2,3,65,3)),"serpent no longer needs distant five-wide clearing");
        require(!body.intersects(new AABB(0,66,0,1,67,1)),"serpent not rejected by ceiling above actual head");
    }
    static void retry(){
        var c=new EncounterRetryCache(8192);
        require(c.attempt(0,100),"first attempt");require(!c.attempt(0,699),"retry throttle");require(c.attempt(0,700),"terrain may recover");
        for(int i=1;i<20000;i++)require(c.attempt(i,800),"new chunk never disabled by capacity");
        require(c.size()==8192,"bounded cache");require(c.attempt(0,900),"evicted entry retry");
    }
    static void progression(){
        var p=new FirstSivkaProgress(71);
        for(int i=0;i<1200;i++)p.sample(20,0);
        require(p.days==0&&!p.pending&&p.ticks==0,"stationary day not active");
        p.days=39;p.ticks=23980;p.centimetres=100000;p.sample(20,0);
        require(p.pending&&p.days==40,"deadline40 independent of RNG");long rng=p.random;
        for(int i=0;i<1000;i++)p.sample(20,100000);
        require(p.pending&&p.days==40&&p.random==rng,"unsafe pending never rerolls or multiplies");
        var d=new SivkaExploration();UUID id=UUID.fromString("00000000-0000-0000-0000-000000000271");d.players.put(id,p);
        var copy=SivkaExploration.load(d.save(new CompoundTag(),null),null).players.get(id);
        require(copy.pending&&copy.random==rng&&copy.days==40,"actual SavedData retains UUID pending RNG and day");
        copy.encounter();copy=FirstSivkaProgress.load(copy.save());copy.sample(20,100000);
        require(copy.met&&!copy.pending&&copy.days==40,"completed opportunity never creates second");
        for(int seed=0;seed<1000;seed++){
            var a=new FirstSivkaProgress(seed);var b=FirstSivkaProgress.load(a.save());
            for(int day=0;day<40&&!a.pending;day++){
                a.ticks=b.ticks=23980;a.sample(20,100000);b.sample(20,100000);
                require(a.pending==b.pending&&a.random==b.random&&a.days==b.days,"relogin cannot reroll probability");
                b=FirstSivkaProgress.load(b.save());
            }
        }
        long[] at=new long[41];int simulations=2_000_000;
        for(int seed=0;seed<5;seed++)for(int i=0;i<simulations/5;i++){
            var a=new FirstSivkaProgress(((long)seed<<48)^i);
            for(int day=1;day<=40;day++){a.ticks=23980;a.sample(20,100000);if(a.pending){at[day]++;break;}}
            if(!a.pending)throw new AssertionError("missing guaranteed opportunity");
        }
        long sum=0;double survival=1;
        for(int day=1;day<=40;day++){sum+=at[day];survival*=1-FirstSivkaProgress.chance(day);if(day==10||day==20||day==30||day==35||day==40){double observed=sum/(double)simulations;require(Math.abs(observed-(1-survival))<.002,"2M policy distribution");System.out.printf(Locale.ROOT,"Sivka day%d: %.4f%% pending, analytic %.4f%%%n",day,100*observed,100*(1-survival));}}
    }
    static void existing(){
        require(FirebirdRules.window(13000)&&FirebirdRules.window(22999)&&!FirebirdRules.window(23000),"night window preserved");
        var d=new FirebirdEncounter();d.event=UUID.randomUUID();d.bird=UUID.randomUUID();d.planned=3;d.issued=5;d.remaining=100;d.eligible=9;d.processed=7;d.observed=7;d.outcomes=new int[]{0,2,1};
        var c=FirebirdEncounter.load(d.save(new CompoundTag(),null),null);
        require(c.event.equals(d.event)&&c.bird.equals(d.bird)&&c.issued==5&&c.remaining==100&&c.processed==7&&c.eligible==9&&Arrays.equals(c.outcomes,d.outcomes),"bird quota RNG outcomes UUID dates unchanged");
        require(MorRules.natural(true,48000,161*161,true,0)&&MorRules.natural(true,48000,161*161,true,11),"natural Mor before any Yaga knowledge");
        require(!MorRules.natural(true,47999,161*161,true,0)&&!MorRules.natural(true,48000,160*160,true,0)&&!MorRules.natural(true,48000,161*161,true,12)&&!MorRules.natural(false,48000,161*161,true,0),"Mor boundaries preserved");
        require(!MorRules.canApply(1,0,true,true),"protected named/story NPC denied");
    }
    public static void main(String[] args){geometry();retry();progression();existing();System.out.println("PASS "+checks+" native offline checks + 2M first-opportunity histories. No game bootstrap/world/server; GAMEPLAY_NOT_RUN.");}
}
