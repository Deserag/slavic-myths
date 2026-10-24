import java.util.*;
import java.util.stream.Stream;
import java.lang.management.ManagementFactory;
import java.nio.file.*;
import net.minecraft.core.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.AbortableIterationConsumer;
import com.google.gson.*;

/** Native section lookup only: no Minecraft instance, bootstrap, server, level or game loop. */
public final class CameraQueryChecks {
    static int checks;
    static volatile Object escape;
    static volatile long checksum;
    static final AABB BOX = new AABB(-16, -16, -16, 16, 16, 16);
    static class FixtureEntity implements EntityAccess {
        final int id; final boolean spectator; final AABB box;
        FixtureEntity(int id, double x, boolean spectator) { this.id=id;this.spectator=spectator;box=new AABB(x,0,0,x+1,1,1); }
        public int getId(){return id;}
        public UUID getUUID(){return new UUID(0,id);}
        public BlockPos blockPosition(){return BlockPos.containing(box.minX,box.minY,box.minZ);}
        public AABB getBoundingBox(){return box;}
        public void setLevelCallback(EntityInLevelCallback callback){}
        public Stream<FixtureEntity> getSelfAndPassengers(){return Stream.of(this);}
        public Stream<FixtureEntity> getPassengersAndSelf(){return Stream.of(this);}
        public void setRemoved(Entity.RemovalReason reason){}
        public boolean shouldBeSaved(){return false;}
        public boolean isAlwaysTicking(){return false;}
    }
    static final class Actor extends FixtureEntity { Actor(int id,double x,boolean spectator){super(id,x,spectator);} }
    static final class Boss extends FixtureEntity { Boss(int id,double x,boolean spectator){super(id,x,spectator);} }
    static final EntityTypeTest<FixtureEntity,Actor> ACTORS=EntityTypeTest.forClass(Actor.class);
    static final EntityTypeTest<FixtureEntity,Boss> BOSSES=EntityTypeTest.forClass(Boss.class);
    static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    static <T extends FixtureEntity> void query(LevelEntityGetterAdapter<FixtureEntity> getter,EntityTypeTest<FixtureEntity,T> type,AABB box,List<T> out){
        getter.get(type,box,entity->{if(!entity.spectator)out.add(entity);return AbortableIterationConsumer.Continuation.CONTINUE;});
    }
    static final class Frame {
        final List<Actor> actors=new ArrayList<>();final List<Boss> bosses=new ArrayList<>();
        long run(LevelEntityGetterAdapter<FixtureEntity> getter,boolean reuse,AABB box,boolean fail){
            List<Actor> a=reuse?actors:new ArrayList<>();List<Boss>b=reuse?bosses:new ArrayList<>();
            try {
                query(getter,reuse?ACTORS:EntityTypeTest.forClass(Actor.class),box,a);
                query(getter,reuse?BOSSES:EntityTypeTest.forClass(Boss.class),box,b);
                // Make both result lists observable, as they are to the rendering code.
                escape=a;escape=b;
                if(fail)throw new IllegalStateException("fixture failure");
                long hash=1;for(int i=0;i<a.size();i++)hash=31*hash+a.get(i).id;
                for(int i=0;i<b.size();i++)hash=31*hash+b.get(i).id;
                return hash;
            } finally {if(reuse){a.clear();b.clear();}}
        }
    }
    static JsonObject measure(LevelEntityGetterAdapter<FixtureEntity> getter,int size,com.sun.management.ThreadMXBean bean){
        Frame before=new Frame(),after=new Frame();
        for(int i=0;i<10000;i++){checksum=before.run(getter,false,BOX,false);checksum=after.run(getter,true,BOX,false);}
        JsonObject row=new JsonObject();row.addProperty("fixture_entities",size);
        for(boolean reuse:new boolean[]{false,true}){
            long best=Long.MAX_VALUE;int frames=20000;
            for(int round=0;round<3;round++){
                long start=bean.getThreadAllocatedBytes(Thread.currentThread().threadId());
                for(int i=0;i<frames;i++)checksum=(reuse?after:before).run(getter,reuse,BOX,false);
                best=Math.min(best,bean.getThreadAllocatedBytes(Thread.currentThread().threadId())-start);
            }
            row.addProperty(reuse?"after_bytes_per_frame":"before_bytes_per_frame",best/(double)frames);
        }
        check(row.get("after_bytes_per_frame").getAsDouble()<row.get("before_bytes_per_frame").getAsDouble(),"less allocation after warmup");return row;
    }
    public static void main(String[]args)throws Exception{
        var bean=(com.sun.management.ThreadMXBean)ManagementFactory.getThreadMXBean();bean.setThreadAllocatedMemoryEnabled(true);
        var measurements=new JsonArray();
        for(int size:new int[]{0,1,64,512}){
            var storage=new EntitySectionStorage<FixtureEntity>(FixtureEntity.class,key->Visibility.TICKING);
            var lookup=new EntityLookup<FixtureEntity>();
            var getter=new LevelEntityGetterAdapter<FixtureEntity>(lookup,storage);
            for(int i=0;i<size;i++){
                FixtureEntity e=i%2==0?new Actor(i,(i%3==2?32:0),i%7==6):new Boss(i,(i%3==2?32:0),i%7==6);
                storage.getOrCreateSection(SectionPos.asLong(e.blockPosition())).add(e);lookup.add(e);
            }
            Frame frame=new Frame();
            for(int i=0;i<100;i++){
                check(frame.run(getter,false,BOX,false)==frame.run(getter,true,BOX,false),"native query order/types/bounds/spectator parity");
                check(frame.actors.isEmpty()&&frame.bosses.isEmpty(),"no retained entities");
            }
            for(int offset:new int[]{-48,0,48})check(frame.run(getter,false,BOX.move(offset,0,0),false)==frame.run(getter,true,BOX.move(offset,0,0),false),"moving query parity");
            try {frame.run(getter,true,BOX,true);throw new AssertionError("missing failure");}catch(IllegalStateException expected){}
            check(frame.actors.isEmpty()&&frame.bosses.isEmpty(),"clear after failure");
            measurements.add(measure(getter,size,bean));
        }
        JsonObject report=new JsonObject();report.addProperty("scope","OFFLINE native EntitySectionStorage/LevelEntityGetterAdapter, caller list reuse; not full camera or FPS/MSPT");
        report.addProperty("checks",checks);report.addProperty("java",System.getProperty("java.version"));report.addProperty("game_processes",0);report.add("allocation",measurements);
        var out=Path.of("docs/implementation/performance-20261024/native-query.json");Files.writeString(out,new GsonBuilder().setPrettyPrinting().create().toJson(report)+"\n");System.out.println(report);
    }
}
