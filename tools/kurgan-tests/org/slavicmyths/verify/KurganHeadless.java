package org.slavicmyths.verify;

import java.util.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import org.slavicmyths.kurgan.*;

/** No game bootstrap, worlds, windows, entities or Forge server are started. */
public final class KurganHeadless {
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    public static void main(String[] args){long started=System.nanoTime();Set<String> maps=new HashSet<>();Set<KurganPlan.Module> modules=EnumSet.noneOf(KurganPlan.Module.class);int loops=0;
        for(int tier=0;tier<3;tier++)for(int seed=0;seed<400;seed++){
            KurganPlan p=KurganPlan.create(tier,seed*7919L);check(p.validate().isEmpty(),"accepted plan invalid");
            check(p.floors>=(tier==0?1:tier==1?2:4)&&p.floors<=(tier==0?1:tier==1?3:5),"floor count");
            CompoundNBT original=KurganPlanNbt.write(p);KurganPlan restored=KurganPlanNbt.read(original);
            check(original.equals(KurganPlanNbt.write(restored)),"actual layout must roundtrip without reroll");
            maps.add(original.toString());
            int stairs=0;for(KurganPlan.Link l:p.links){modules.add(l.module);if(l.module==KurganPlan.Module.LOOP)loops++;if(l.stair)stairs++;check(l.width>=3&&l.width<=5&&l.height>=4&&l.height<=5,"navigation size");
                if(l.a>=0)check(p.rooms.get(l.a).floor==p.rooms.get(l.b).floor||l.stair,"floor transition without stairs");
            }check(stairs==p.floors,"entry plus complete stair chain");
            for(KurganPlan.Room r:p.rooms){modules.add(p.junction(r));if(!r.loot.isEmpty())for(int origin=0;origin<16;origin++){
                int foot=origin+r.x-r.rx+2,head=foot-1;if(Math.floorDiv(foot,16)!=Math.floorDiv(head,16)){foot=origin+r.x+r.rx-2;head=foot+1;}check(Math.floorDiv(foot,16)==Math.floorDiv(head,16),"paired coffin crosses generation chunk");
            }}
            if(tier==2)sealed(p);
        }
        check(maps.size()==1200,"deterministic seeds should generate varied maps");check(loops>600,"tier II/III loops absent");
        check(modules.containsAll(Arrays.asList(KurganPlan.Module.SHORT,KurganPlan.Module.NORMAL,KurganPlan.Module.LONG,KurganPlan.Module.CORNER,KurganPlan.Module.T_JUNCTION,KurganPlan.Module.CROSSROADS,KurganPlan.Module.DEAD_END,KurganPlan.Module.ROOM_CONNECTOR,KurganPlan.Module.STAIR,KurganPlan.Module.LOOP)),"missing semantic module categories: "+modules);
        persistence();creatures();System.out.println("PASS: 1200 seeded plans, physical bounds/connectors, 10 module categories, loops, chunk-safe coffins, closed tomb shells, plan/instance/curse NBT, encounter lifecycle, 900 tier/disturbance rosters and six fixed combat profiles; "+((System.nanoTime()-started)/1000000)+" ms. Minecraft launches: 0.");
    }
    private static void sealed(KurganPlan p){KurganPlan.Room r=p.rooms.get(p.finalRoom);check(r.floor==p.floors-1&&p.niches.size()==3,"final tomb anchors");
        // Every playable tomb voxel must be enclosed by the octagonal solid envelope.
        for(int x=r.x-r.rx;x<=r.x+r.rx;x++)for(int z=r.z-r.rz;z<=r.z+r.rz;z++)for(int y=r.y;y<=r.y+r.height+1;y++)if(p.roomAir(r,x,y,z)){
            for(int[] d:new int[][]{{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}}){int nx=x+d[0],ny=y+d[1],nz=z+d[2];check(r.box().contains(nx,ny,nz)&&Math.abs(nx-r.x)+Math.abs(nz-r.z)<=23,"tomb bypass gap");}
        }
        for(int x=p.seal.x0;x<=p.seal.x1;x++)for(int y=p.seal.y0;y<=p.seal.y1;y++)for(int z=p.seal.z0;z<=p.seal.z1;z++)if(x==p.seal.x1)check(!p.roomAir(r,x,y,z),"entrance not sealed");
    }
    private static void persistence(){KurganPlan p=KurganPlan.create(2,74);KurganInstance a=new KurganInstance(UUID.randomUUID(),new BlockPos(24,70,24),p),b=new KurganInstance(UUID.randomUUID(),new BlockPos(400,70,400),p);
        check(a.record("loot:1",8)&&!a.record("loot:1",8)&&a.disturbance==8,"reopen changes disturbance");check(b.disturbance==0,"shared global disturbance");
        BurialRecords records=new BurialRecords();records.opened(a.id);records.register(a);records.register(b);UUID player=UUID.randomUUID();records.persistentCurses.put(player,new HashSet<>(Arrays.asList(a.id,b.id)));
        CompoundNBT saved=records.save(new CompoundNBT());BurialRecords loaded=new BurialRecords();loaded.load(saved);check(loaded.burialOpened(a.id),"legacy data migration");
        KurganInstance copy=loaded.instances.get(a.id);check(copy.disturbance==8&&!copy.record("loot:1",8),"source dedup lost after reload");check(loaded.at(a.origin)==copy,"spatial index not rebuilt");
        check(loaded.persistentCurses.get(player).containsAll(Arrays.asList(a.id,b.id)),"player UUID/source curse state lost");copy.record("zone:2",200);check(copy.disturbance==100,"disturbance not bounded");
    }
    private static void creatures(){
        double[] hp={40,32,60,150,150,350};int n=0;
        for(KurganFighter.Kind k:KurganFighter.Kind.values()){check(k.hp==hp[n++],"incorrect creature health");check(k.speed>0&&k.range>=26&&k.height>=1.89,"combat profile");}
        check(KurganFighter.phase(KurganFighter.Kind.PRINCE,.66)==0&&KurganFighter.phase(KurganFighter.Kind.PRINCE,.65)==1&&KurganFighter.phase(KurganFighter.Kind.PRINCE,.30)==2,"prince phase thresholds");
        check(KurganFighter.phase(KurganFighter.Kind.VOEVODA,.50)==1,"voevoda stance");
        check(KurganFighter.Move.GRAB.cooldown>=180&&KurganFighter.Move.SEAL.windup==28&&KurganFighter.Move.HEAVY.windup>=16&&KurganFighter.Move.HEAVY.recovery>=20,"fair special timings");
        for(int tier=0;tier<3;tier++)for(int seed=0;seed<100;seed++)for(int disturbance:new int[]{0,50,75}){
            KurganInstance i=new KurganInstance(UUID.randomUUID(),BlockPos.ZERO,KurganPlan.create(tier,seed));i.disturbance=disturbance;int prince=0,voevoda=0,volkhv=0;
            for(KurganPlan.Room room:i.plan.rooms){List<KurganFighter.Kind> roster=KurganRoster.roster(i,room);check(roster.equals(KurganRoster.roster(i,room)),"room distribution rerolled");check(roster.size()<=3,"unbounded room roster");
                for(KurganFighter.Kind k:roster){if(k==KurganFighter.Kind.PRINCE){prince++;check(room.hall(),"boss outside final hall");}if(k==KurganFighter.Kind.VOEVODA)voevoda++;if(k==KurganFighter.Kind.VOLKHV)volkhv++;if(tier==0)check(!k.boss(),"boss in small tomb");}
            }check(prince==(tier==2&&disturbance>=75?1:0),"prince encounter count");check(voevoda<=1&&volkhv<=1,"repeated miniboss chamber");if(tier==2&&disturbance>=75)check(voevoda==1&&volkhv==1,"missing Great Kurgan miniboss room");
        }
        KurganEncounterState state=new KurganEncounterState();UUID a=UUID.randomUUID(),b=UUID.randomUUID();check(state.start(),"encounter cannot start");state.alive.add(a);state.alive.add(b);
        KurganInstance i=new KurganInstance(UUID.randomUUID(),BlockPos.ZERO,KurganPlan.create(2,74));i.encounters.put(2,state);i.bossDefeated=true;i.sealOpened=true;
        KurganInstance restored=KurganInstance.load(i.save());KurganEncounterState copy=restored.encounters.get(2);check(restored.bossDefeated&&restored.sealOpened&&copy.alive.equals(state.alive)&&!copy.start(),"reload loses once-only encounter");
        check(!copy.death(UUID.randomUUID())&&!copy.cleared,"unknown/unloaded UUID treated as death");check(copy.death(a)&&!copy.cleared,"premature encounter clear");check(copy.death(b)&&copy.cleared&&!copy.start(),"cleared encounter respawn");
        CompoundNBT old=i.save();old.remove("Encounters");old.remove("BossDefeated");KurganInstance legacy=KurganInstance.load(old);check(legacy.encounters.isEmpty()&&!legacy.bossDefeated,"0.8.5 migration defaults");
        BurialRecords records=new BurialRecords();UUID source=UUID.randomUUID(),other=UUID.randomUUID(),single=UUID.randomUUID(),multiple=UUID.randomUUID();records.persistentCurses.put(single,new HashSet<>(Arrays.asList(source)));records.persistentCurses.put(multiple,new HashSet<>(Arrays.asList(source,other)));
        check(records.clearCurseSource(source).equals(Collections.singleton(single)),"victory clears unrelated source/player");check(!records.persistentCurses.containsKey(single)&&records.persistentCurses.get(multiple).equals(Collections.singleton(other)),"offline source cleanup");
        BurialRecords reloaded=new BurialRecords();reloaded.load(records.save(new CompoundNBT()));check(reloaded.persistentCurses.equals(records.persistentCurses),"victory not persisted");check(reloaded.clearCurseSource(source).isEmpty(),"repeated victory changes curses");
        check(KurganFighter.phase(KurganFighter.Kind.PRINCE,(float)105/350)==2&&KurganFighter.phase(KurganFighter.Kind.PRINCE,(float)227.5/350)==1,"float health threshold roundoff");
    }
}
