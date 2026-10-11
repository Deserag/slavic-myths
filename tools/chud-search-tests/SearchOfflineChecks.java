import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import com.mojang.brigadier.CommandDispatcher;
import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import org.slavicmyths.chud.ChudSearchBudget;
import org.slavicmyths.chud.ChudTerrainPolicy;
import org.slavicmyths.chud.ChudSitePolicy;

/** Plain JVM checks: no Minecraft bootstrap, game loader, server, client, or world. */
public class SearchOfflineChecks {
    private static int checks;
    private static void check(boolean result,String name) {
        if(!result)throw new AssertionError(name);checks++;
    }
    public static void main(String[] args) throws Exception {
        check(ChudTerrainPolicy.roofAnchor(64,56,false)==-12,"dry roof exactly20 below ground");
        check(ChudTerrainPolicy.roofAnchor(40,40,true)==-5,"submerged roof exactly5 below seabed");
        check(ChudTerrainPolicy.roofAnchor(180,56,false)==104,"mountain roof descends from terrain");
        check(ChudTerrainPolicy.fits(-12,-44,73,-64,320),"ordinary underground city fits above bedrock");
        check(!ChudTerrainPolicy.fits(-40,-44,73,-64,320),"actual construction cannot cross bedrock");
        check(ChudTerrainPolicy.fits(69,-44,73,-64,320),"flat command places lowest floor above ground24");
        for(int family=0;family<6;family++)for(boolean ruined:new boolean[]{false,true}){
            int[] amounts=new int[4];for(int roll=0;roll<100;roll++)amounts[org.slavicmyths.chud.ChudDecorPolicy.material(roll,ruined)]++;
            check(amounts[0]==(ruined?38:64)&&amounts[2]==(ruined?22:14),"maintained versus ruined wear weights");
            for(int count:amounts)check(count>0,"all four palette materials used");
            for(int x=-24;x<=24;x+=3)for(int z=-24;z<=24;z+=3){
                int patch=org.slavicmyths.chud.ChudDecorPolicy.patch(11,x,0,z,family);
                check(patch>=0&&patch<100,"stable palette range");
                check(patch==org.slavicmyths.chud.ChudDecorPolicy.patch(11,x+2,3,z+2,family),"coherent3x3x4 masonry patches across negative coordinates");
            }
        }
        for(int x=-32;x<=32;x++)for(int z=-32;z<=32;z++){
            int distance=100;
            for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++)if(org.slavicmyths.chud.ChudDecorPolicy.floorLight(x+dx,z+dz))distance=Math.min(distance,Math.abs(dx)+Math.abs(dz));
            check(distance<=8,"open hall floor grid maximum horizontal Manhattan distance8; not actual light-engine test");
        }
        ChudSearchBudget.check();check(true,"normal generation has no search budget");
        ChudSearchBudget.begin(System.nanoTime()-1);
        try { ChudSearchBudget.check();throw new AssertionError("deadline ignored"); }
        catch(CancellationException expected){checks++;}
        finally { ChudSearchBudget.end(); }
        ChudSearchBudget.check();check(true,"deadline cleanup");
        var ready=new CountDownLatch(1);var cancelled=new AtomicBoolean();
        var thread=new Thread(()->{
            ChudSearchBudget.begin(System.nanoTime()+10_000_000_000L);ready.countDown();
            try { while(true)ChudSearchBudget.check(); }
            catch(CancellationException expected){cancelled.set(true);}
            finally { ChudSearchBudget.end(); }
        });
        thread.setDaemon(true);thread.start();ready.await();
        ChudSearchBudget.check();check(true,"budget isolated to worker thread");
        thread.interrupt();thread.join(1000);
        check(cancelled.get()&&!thread.isAlive(),"cooperative cancellation terminates worker");
        Thread.currentThread().interrupt();ChudSearchBudget.check();Thread.interrupted();
        check(true,"ordinary generation is not changed by unrelated interruption");

        var dispatcher=new CommandDispatcher<Object>();
        dispatcher.register(literal("locate").then(literal("structure")
            .then(argument("structure",greedyString()).executes(c->7))));
        for(String id:new String[]{"chud_city","chud_abandoned"})
            dispatcher.register(literal("locate").then(literal("structure")
                .then(literal("slavicmyths:"+id).executes(c->11))));
        check(dispatcher.execute("locate structure slavicmyths:chud_city","test")==11,"city literal wins");
        check(dispatcher.execute("locate structure slavicmyths:chud_abandoned","test")==11,"abandoned literal wins");
        check(dispatcher.execute("locate structure minecraft:village","test")==7,"vanilla structure route preserved");
        dispatcher.register(literal("execute").then(literal("run").redirect(dispatcher.getRoot())));
        check(dispatcher.execute("execute run locate structure slavicmyths:chud_city","test")==11,"nested execution uses replacement route");
        var root=dispatcher.register(literal("slavicmyths").then(literal("city").then(literal("locate").executes(c->1))));
        dispatcher.register(literal("sm").redirect(root));
        dispatcher.register(literal("slavicmyths").then(literal("city").then(literal("search").executes(c->2))));
        check(dispatcher.execute("sm city search","test")==2,"existing alias sees merged search");
        check(ChudTerrainPolicy.bottom(false,false,0)==-31,"medium actual floor shell");
        check(ChudTerrainPolicy.bottom(false,true,2)==-22,"short shaft actual deck supports");
        check(-16+ChudTerrainPolicy.bottom(false,false,0)>=-64+16,"medium fits ordinary surface64 at descent80");
        check(-16+ChudTerrainPolicy.bottom(false,true,2)>=-64+16,"short great fits ordinary surface64 at descent80");
        check(-16-44 < -64+16,"old padded-box check incorrectly rejected both");
        for(int gx=-3;gx<=3;gx++)for(int gz=-3;gz<=3;gz++) {
            var a=ChudSitePolicy.candidates(gx*256,gz*256,gx*256+11,gz*256+19);
            check(a.size()==10&&a.getFirst().equals(new ChudSitePolicy.Site(gx*256+11,gz*256+19)),"legacy owning site first, retries bounded");
            for(var site:a)check(Math.floorDiv(site.x(),256)==gx&&Math.floorDiv(site.z(),256)==gz,"negative coordinates and cell membership");
            for(int[] neighbor:new int[][]{{1,0},{0,1},{1,1},{-1,1}}) {
                var b=ChudSitePolicy.candidates((gx+neighbor[0])*256,(gz+neighbor[1])*256,(gx+neighbor[0])*256+95,(gz+neighbor[1])*256+95);
                for(var first:a)for(var second:b){
                    double dx=Math.max(0,Math.abs((long)first.x()-second.x())*16-278),dz=Math.max(0,Math.abs((long)first.z()-second.z())*16-278);
                    check(Math.hypot(dx,dz)>=2000,"all retry slots retain independent envelope spacing");
                }
            }
        }
        checks+=TerrainPlanOfflineChecks.run();
        System.out.println("OFFLINE SEARCH CHECKS PASS: "+checks+"; Minecraft launches=0");
    }
}
