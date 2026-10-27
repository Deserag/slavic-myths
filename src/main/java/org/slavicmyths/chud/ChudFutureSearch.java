package org.slavicmyths.chud;

import java.util.*;
import java.util.concurrent.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

/** Read-only seed forecast + asynchronous saved-start inspection. Never requests a Minecraft chunk. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class ChudFutureSearch {
    private static final ExecutorService WORKER=Executors.newSingleThreadExecutor(r->{var t=new Thread(r,"slavicmyths-chud-forecast");t.setDaemon(true);t.setPriority(Thread.MIN_PRIORITY);return t;});
    private static final Map<MinecraftServer,Search> ACTIVE=new HashMap<>(),LAST=new HashMap<>();
    private static final class Search {
        final CommandSourceStack source;final String name;final AbandonedPlan.Kind kind;final int page,startIndex,cx,cz;
        final long deadline=System.nanoTime()+60_000_000_000L;volatile int tested;Future<ChudRegionPlanner.Selected> worker;BlockPos resolved;
        CompletableFuture<Optional<net.minecraft.nbt.CompoundTag>> disk;ChudRegionPlanner.Selected selected;
        Search(CommandSourceStack s,String n,AbandonedPlan.Kind k,int p,int first){source=s;name=n;kind=k;page=p;startIndex=first;cx=Math.floorDiv(BlockPos.containing(s.getPosition()).getX(),4096);cz=Math.floorDiv(BlockPos.containing(s.getPosition()).getZ(),4096);}
        ChudRegionPlanner.Selected find(){
            ChudSearchBudget.begin(deadline);
            try{var world=source.getLevel();var generator=world.getChunkSource().getGenerator();
                while(tested<96){ChudSearchBudget.check();int[] offset=spiral(startIndex+tested++);
                    var c=new Structure.GenerationContext(world.registryAccess(),generator,generator.getBiomeSource(),world.getChunkSource().randomState(),world.getStructureManager(),world.getSeed(),new ChunkPos((cx+offset[0])*256,(cz+offset[1])*256),world,b->true);
                    var answer=ChudRegionPlanner.select(c);if(answer.isEmpty())continue;var s=answer.get();
                    boolean match=name.equals("chud_city")?s.city()!=null:kind==AbandonedPlan.Kind.ATTACHED_SMALL?s.city()!=null&&s.abandoned()!=null:s.city()==null&&(kind==null||s.abandoned().kind==kind);
                    if(match)return s;
                }return null;
            }finally{ChudSearchBudget.end();}
        }
    }
    private static int[] spiral(int index){if(index==0)return new int[]{0,0};int ring=(int)Math.ceil((Math.sqrt(index+1)-1)/2),side=ring*2,start=(2*ring-1)*(2*ring-1),at=index-start;
        if(at<side)return new int[]{ring,-ring+1+at};at-=side;if(at<side)return new int[]{ring-1-at,ring};at-=side;if(at<side)return new int[]{-ring,ring-1-at};at-=side;return new int[]{-ring+1+at,-ring};}
    public static int start(CommandSourceStack source,String name,AbandonedPlan.Kind kind,int page){
        return startAt(source,name,kind,page,page*96);
    }
    private static int startAt(CommandSourceStack source,String name,AbandonedPlan.Kind kind,int page,int first){
        if(source.getLevel().dimension()!=Level.OVERWORLD){source.sendFailure(Component.literal("Чудь ищется только в обычном мире."));return 0;}
        if(ACTIVE.containsKey(source.getServer())){source.sendFailure(Component.literal("Поиск Чуди уже идёт. chud_search status/cancel."));return 0;}
        var search=new Search(source,name,kind,page,first);ACTIVE.put(source.getServer(),search);LAST.put(source.getServer(),search);search.worker=WORKER.submit(search::find);
        source.sendSuccess(()->Component.literal("Проверяются seed-планы и сохранённые starts, страница "+page+". Чанки не загружаются; строительство не запускается."),false);return 1;
    }
    public static int control(CommandSourceStack source,boolean cancel){var s=ACTIVE.get(source.getServer());if(s==null){source.sendFailure(Component.literal("Активного поиска Чуди нет."));return 0;}if(cancel){s.worker.cancel(true);ACTIVE.remove(source.getServer());source.sendSuccess(()->Component.literal("Поиск отменён. Блоки и chunk tickets не изменялись."),false);}else source.sendSuccess(()->Component.literal("Поиск Чуди: страница "+s.page+", проверено регионов "+s.tested+"/96."),false);return 1;}
    public static int next(CommandSourceStack source){var s=LAST.get(source.getServer());if(s==null)return start(source,"chud_city",null,0);int first=s.startIndex+Math.max(0,s.tested-(s.worker.isCancelled()?1:0));return startAt(source,s.name,s.kind,Math.min(128,first/96),first);}
    @net.neoforged.bus.api.SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event){
        var s=ACTIVE.get(event.getServer());if(s==null)return;
        try{
            if(System.nanoTime()>=s.deadline){s.worker.cancel(true);end(s,"Поиск остановлен по лимиту 60 секунд. Мир и chunk tickets не изменялись.");return;}
            if(s.disk!=null){if(!s.disk.isDone())return;var saved=s.disk.join();s.disk=null;
                if(saved.isPresent()){
                    String nativeName=s.kind==AbandonedPlan.Kind.ATTACHED_SMALL?"chud_city":s.name;
                    var tag=saved.get().getCompound("structures").getCompound("starts").getCompound("slavicmyths:"+nativeName);
                    if(tag.isEmpty()||tag.getString("id").equals("INVALID")){s.selected=null;s.worker=WORKER.submit(s::find);return;}
                    for(var child:tag.getList("Children",10)){var piece=(net.minecraft.nbt.CompoundTag)child;
                        if(piece.contains("Access")){long[] floors=piece.getCompound("Access").getLongArray("Floors");if(floors.length>0)s.resolved=BlockPos.of(floors[floors.length-1]).above();}
                        else if(s.resolved==null&&piece.contains("ChudPlan")){var p=ChudPlan.load(piece.getCompound("ChudPlan"));s.resolved=p.world(p.points.get(ChudPlan.Role.ENTRANCE));}
                        else if(s.resolved==null&&piece.contains("AbandonedPlan")){var p=AbandonedPlan.load(piece.getCompound("AbandonedPlan"));s.resolved=p.world(p.targets.get("entrance"));}
                    }
                    if(s.resolved==null){s.selected=null;s.worker=WORKER.submit(s::find);return;}
                    boolean full=saved.get().getString("Status").equals("minecraft:full");
                    report(s,full?"Сохранённый native start (полная геометрия сейчас не осматривалась)":"Сохранённый native план, чанки ещё не завершили генерацию");return;
                }
                report(s,"Предполагаемое место будущей естественной генерации; start-чанк ещё не сохранён. Это НЕ FOUND_PHYSICAL");return;
            }
            if(!s.worker.isDone())return;s.selected=s.worker.get();
            if(s.selected==null){end(s,"Подходящего существующего или будущего места на этой странице нет. Мир не менялся.");return;}
            var owner=s.selected.owner();var world=s.source.getLevel();var loaded=world.getChunkSource().getChunkNow(owner.x,owner.z);
            if(loaded!=null){String nativeName=s.kind==AbandonedPlan.Kind.ATTACHED_SMALL?"chud_city":s.name;var type=world.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ResourceLocation.fromNamespaceAndPath("slavicmyths",nativeName));var start=loaded.getStartForStructure(type);
                if(start!=null&&start.isValid()){for(var piece:start.getPieces()){if(piece instanceof ChudPiece p)s.resolved=p.access==null?p.plan.world(p.plan.points.get(ChudPlan.Role.ENTRANCE)):p.access.mouth;else if(piece instanceof AbandonedPiece p)s.resolved=p.access==null?p.plan.world(p.plan.targets.get("entrance")):p.access.mouth;}report(s,"Существующий загруженный native start; поиск ничего не строил");return;}
                s.selected=null;s.worker=WORKER.submit(s::find);return;
            }
            s.disk=world.getChunkSource().chunkMap.read(owner);
        }catch(Exception failure){s.worker.cancel(true);end(s,"Поиск завершён без результата: "+(System.nanoTime()>=s.deadline?"лимит 60 секунд":failure.getClass().getSimpleName())+". Блоки и chunk tickets не изменялись.");}
    }
    private static void report(Search s,String status){BlockPos at=s.resolved!=null?s.resolved:s.selected.surface();String place=s.selected.access()==null?"примерные X/Z="+at.getX()+"/"+at.getZ()+" (вход ПОД ЗЕМЛЁЙ, Y не является уровнем поверхности)":"X/Y/Z="+at.toShortString();s.source.sendSuccess(()->Component.literal(s.name+": "+status+"; "+place+"; owner="+s.selected.owner()+"; страница="+s.page),false);ACTIVE.remove(s.source.getServer());}
    private static void end(Search s,String message){s.source.sendFailure(Component.literal(message));ACTIVE.remove(s.source.getServer());}
    @net.neoforged.bus.api.SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent e){var s=ACTIVE.remove(e.getServer());if(s!=null)s.worker.cancel(true);LAST.remove(e.getServer());}
    private ChudFutureSearch(){}
}
