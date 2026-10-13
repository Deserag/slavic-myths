package org.slavicmyths.chud;

import java.util.*;
import java.util.concurrent.*;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

/** Explicit, bounded seed planning. No chunk loading, world reads, or tick polling on workers. */
@EventBusSubscriber(modid="slavicmyths")
public final class ChudSearchCommands {
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(r -> {
        var t = new Thread(r, "slavicmyths-chud-search"); t.setDaemon(true); t.setPriority(Thread.MIN_PRIORITY); return t;
    });
    private static final ScheduledExecutorService NOTICES = Executors.newSingleThreadScheduledExecutor(r -> {
        var t = new Thread(r, "slavicmyths-chud-search-notices"); t.setDaemon(true); return t;
    });
    private static Job active;
    private static Continuation continuation;
    private record Continuation(MinecraftServer server,String dimension,long seed,String name,
                                AbandonedPlan.Kind kind,int page,int offset,BlockPos origin) {}
    private static final class Job {
        final MinecraftServer server;
        volatile int checked;
        volatile boolean cancelled;
        volatile boolean closing;
        volatile Thread thread;
        Job(MinecraftServer server) { this.server=server; }
        void cancel() { cancelled=true; var t=thread; if(t!=null)t.interrupt(); }
    }
    @SubscribeEvent public static void register(RegisterCommandsEvent event) {
        for(String name:List.of("chud_city","chud_abandoned")) {
            // Exact literals take precedence over vanilla's argument; other structures retain vanilla locate.
            event.getDispatcher().register(Commands.literal("locate").requires(s->s.hasPermission(2))
                .then(Commands.literal("structure").then(Commands.literal("slavicmyths:"+name)
                    .executes(c->start(c.getSource(),name,null,0)))));
        }
        for(String branch:List.of("city","abandoned")) {
            String structure=branch.equals("city")?"chud_city":"chud_abandoned";
            var search=Commands.literal("search").executes(c->start(c.getSource(),structure,null,0))
                .then(Commands.argument("page",IntegerArgumentType.integer(0,128))
                    .executes(c->start(c.getSource(),structure,null,IntegerArgumentType.getInteger(c,"page"))));
            if(branch.equals("abandoned"))for(var kind:AbandonedPlan.Kind.values()) {
                String label=switch(kind){case ATTACHED_SMALL->"attached";case INDEPENDENT_MEDIUM->"medium";case INDEPENDENT_GREAT->"great";};
                search.then(Commands.literal(label).executes(c->start(c.getSource(),structure,kind,0))
                    .then(Commands.argument("page",IntegerArgumentType.integer(0,128))
                        .executes(c->start(c.getSource(),structure,kind,IntegerArgumentType.getInteger(c,"page")))));
            }
            event.getDispatcher().register(Commands.literal("slavicmyths").then(Commands.literal(branch)
                .requires(s->s.hasPermission(2)).then(search)));
        }
        event.getDispatcher().register(Commands.literal("slavicmyths").then(Commands.literal("chud_search")
            .requires(s->s.hasPermission(2))
            .then(Commands.literal("status").executes(c->control(c.getSource(),false)))
            .then(Commands.literal("next").executes(c->next(c.getSource())))
            .then(Commands.literal("cancel").executes(c->control(c.getSource(),true)))));
    }
    private static synchronized int control(CommandSourceStack source,boolean cancel) {
        if(active==null||active.server!=source.getServer()) { say(source,"Активного поиска нет.");return 0; }
        if(cancel)active.cancel();
        say(source,(cancel?"Отмена запрошена. ":"Поиск идёт. ")+"Проверено кандидатов: "+active.checked+"/96.");return 1;
    }
    @SubscribeEvent public static synchronized void stopping(ServerStoppingEvent event) {
        ChudRegionPlanner.clear();
        if(active!=null&&active.server==event.getServer()){active.closing=true;active.cancel();}
        if(continuation!=null&&continuation.server==event.getServer())continuation=null;
    }
    static int searchCity(CommandSourceStack source) { return start(source,"chud_city",null,0); }
    static int searchAbandoned(CommandSourceStack source,AbandonedPlan.Kind kind) { return start(source,"chud_abandoned",kind,0); }
    private static synchronized int next(CommandSourceStack source) {
        var c=continuation;
        if(c==null||c.server!=source.getServer()||c.seed!=source.getLevel().getSeed()
            ||!c.dimension.equals(source.getLevel().dimension().location().toString())) {
            source.sendFailure(Component.literal("Нет продолжения поиска в этом измерении."));return 0;
        }
        return start(source,c.name,c.kind,c.page,c.offset,c.origin);
    }
    private static synchronized int start(CommandSourceStack source,String name,AbandonedPlan.Kind kind,int page) {
        return start(source,name,kind,page,0,BlockPos.containing(source.getPosition()));
    }
    private static synchronized int start(CommandSourceStack source,String name,AbandonedPlan.Kind kind,int page,int offset,BlockPos origin) {
        if(active!=null) { source.sendFailure(Component.literal("Уже идёт поиск; /sm chud_search status или /sm chud_search cancel."));return 0; }
        var level=source.getLevel();
        if(!source.getServer().getWorldData().worldGenOptions().generateStructures()) {
            source.sendFailure(Component.literal("Генерация структур выключена в настройках мира; поиск новых кандидатов не запускается."));return 0;
        }
        var registry=level.registryAccess();
        String plannedName=kind==AbandonedPlan.Kind.ATTACHED_SMALL?"chud_city":name;
        Structure structure=registry.registryOrThrow(Registries.STRUCTURE).get(ResourceLocation.fromNamespaceAndPath("slavicmyths",plannedName));
        var set=registry.registryOrThrow(Registries.STRUCTURE_SET).get(ResourceLocation.fromNamespaceAndPath("slavicmyths",plannedName));
        if(structure==null||set==null||!(set.placement() instanceof RandomSpreadStructurePlacement placement)) {
            source.sendFailure(Component.literal("Структура или её стандартная сетка отсутствует в этом datapack."));return 0;
        }
        var generator=level.getChunkSource().getGenerator();
        if(Collections.disjoint(generator.getBiomeSource().possibleBiomes(),structure.biomes().stream().toList())) {
            source.sendFailure(Component.literal("В этом измерении нет подходящих биомов для структуры."));return 0;
        }
        // Capture immutable generation inputs on the server thread, not ServerLevel on the worker.
        var random=level.getChunkSource().randomState();var templates=level.getStructureManager();
        var height=LevelHeightAccessor.create(level.getMinBuildHeight(),level.getHeight());
        long seed=level.getSeed();
        double minX=level.getWorldBorder().getMinX(),maxX=level.getWorldBorder().getMaxX();
        double minZ=level.getWorldBorder().getMinZ(),maxZ=level.getWorldBorder().getMaxZ();
        String dimension=level.dimension().location().toString();
        var job=new Job(source.getServer());active=job;continuation=null;
        say(source,"Поиск областей новых чанков запущен: страница "+page+", до 96 областей, до 10 точек на область / 60 секунд. Мир не генерируется. /sm chud_search cancel");
        WORKER.execute(()->{
            job.thread=Thread.currentThread();
            var progress=NOTICES.scheduleAtFixedRate(()->job.server.execute(()->{
                synchronized(ChudSearchCommands.class){if(active==job&&!job.server.isStopped())
                    say(source,"Поиск: "+job.checked+"/96 областей; страница "+page+". /sm chud_search cancel");}
            }),2,2,TimeUnit.SECONDS);
            ChudSearchBudget.begin(System.nanoTime()+TimeUnit.SECONDS.toNanos(60));
            try {
                int gx=Math.floorDiv(origin.getX()>>4,placement.spacing()),gz=Math.floorDiv(origin.getZ()>>4,placement.spacing());
                var candidates=new ArrayList<ChunkPos>();
                for(int x=gx-64;x<=gx+64;x++)for(int z=gz-64;z<=gz+64;z++)
                    candidates.add(placement.getPotentialStructureChunk(seed,x*placement.spacing(),z*placement.spacing()));
                candidates.sort(Comparator.comparingDouble(p->Math.hypot((double)p.getMiddleBlockX()-origin.getX(),(double)p.getMiddleBlockZ()-origin.getZ())));
                for(int index=page*96+offset;index<Math.min(candidates.size(),(page+1)*96);index++) {
                    if(job.cancelled)throw new CancellationException();ChudSearchBudget.check();
                    var chunk=candidates.get(index);job.checked++;
                    if(chunk.getMiddleBlockX()<minX+160||chunk.getMiddleBlockX()>maxX-160||chunk.getMiddleBlockZ()<minZ+160||chunk.getMiddleBlockZ()>maxZ-160)continue;
                    if(!placement.applyAdditionalChunkRestrictions(chunk.x,chunk.z,seed))continue;
                    var context=new Structure.GenerationContext(registry,generator,generator.getBiomeSource(),random,templates,seed,chunk,height,structure.biomes()::contains);
                    if(placement instanceof org.slavicmyths.worldgen.ChudRegionPlacement) {
                        var selected=ChudRegionPlanner.select(context);
                        if(selected.isEmpty())continue;
                        var plan=selected.get();
                        if(plannedName.equals("chud_city")!=(plan.city()!=null))continue;
                        if(kind!=null&&(plan.abandoned()==null||plan.abandoned().kind!=kind))continue;
                        var anchor=plan.city()!=null?plan.city().anchor:plan.abandoned().anchor;
                        reply(job,source,"План комплекса: "+anchor.toShortString()+"; dimension="+dimension+"; root="+(plan.city()!=null?plan.city().cityId():plan.abandoned().rootId)+"; тип="+(plan.city()!=null?"город Чуди":plan.abandoned().kind)+". Это координаты новых чанков; загрузите окрестность и используйте /sm "+(plan.city()!=null?"city":"abandoned")+" locate.");
                        return;
                    }
                    var start=structure.generate(registry,generator,generator.getBiomeSource(),random,templates,seed,chunk,0,height,structure.biomes()::contains);
                    if(!start.isValid())continue;
                    for(var piece:start.getPieces()) {
                        AbandonedPlan abandoned=piece instanceof AbandonedPiece b?b.plan:piece instanceof ChudPiece a?a.abandoned:null;
                        if(kind!=null&&(abandoned==null||abandoned.kind!=kind))continue;
                        BlockPos anchor=piece instanceof ChudPiece a?a.plan.anchor:abandoned==null?null:abandoned.anchor;
                        if(anchor==null)continue;
                        reply(job,source,"Кандидат "+plannedName+(abandoned==null?"":" / "+abandoned.kind)+": "+anchor.toShortString()+"; dimension="+dimension+"; проверено="+job.checked+". Это план для новых чанков; в старых чанках комплекс может отсутствовать. Для входа сначала загрузите окрестность и используйте /sm "+(plannedName.equals("chud_city")?"city":"abandoned")+" locate.");
                        return;
                    }
                }
                synchronized(ChudSearchCommands.class){if(page<128&&!job.closing)continuation=new Continuation(job.server,dimension,seed,name,kind,page+1,0,origin);}
                reply(job,source,"На странице "+page+" подходящего кандидата нет (проверено "+job.checked+"). Это не отсутствие во всём мире. "+(page<128?"Следующая: /sm chud_search next":"Достигнут предел области поиска."));
            } catch(CancellationException stopped) {
                synchronized(ChudSearchCommands.class){if(!job.closing)continuation=new Continuation(job.server,dimension,seed,name,kind,page,offset+Math.max(0,job.checked-1),origin);}
                reply(job,source,(job.cancelled?"Поиск отменён":"Достигнут лимит 60 секунд")+"; проверено="+job.checked+". Мир не генерировался. Продолжить с незавершённого кандидата: /sm chud_search next");
            } catch(Exception failure) {
                LogUtils.getLogger().error("Chud candidate search failed",failure);
                reply(job,source,"Ошибка поиска; подробности в latest.log. Мир не генерировался.");
            } finally {
                progress.cancel(false);ChudSearchBudget.end();Thread.interrupted();
                synchronized(ChudSearchCommands.class){if(active==job)active=null;}
            }
        });return 1;
    }
    private static void reply(Job job,CommandSourceStack source,String text) {
        job.server.execute(()->{if(!job.closing&&!job.server.isStopped())say(source,text);});
    }
    private static void say(CommandSourceStack source,String text) { source.sendSuccess(()->Component.literal(text),false); }
    private ChudSearchCommands() {}
}
