import java.util.*;
import java.nio.file.*;
import org.slavicmyths.depths.*;

/** Plain JVM geometry checks. Never starts or bootstraps Minecraft. */
public final class DepthsOfflineChecks {
    private static int checks;
    static void check(boolean result,String label){if(!result)throw new AssertionError(label);checks++;}
    static boolean solid(String s){return s!=null&&!s.equals("air")&&!s.contains("lantern")&&!s.contains("chain")&&!s.contains("wall")&&!s.contains("fence")&&!s.contains("dripstone")&&!s.contains("carpet")&&!s.contains("cobweb");}
    static Set<DepthsLayout.Cell> reachable(Map<DepthsLayout.Cell,String> layout){
        Set<DepthsLayout.Cell> nodes=new HashSet<>();
        for(var e:layout.entrySet()){var c=e.getKey();if(solid(e.getValue())&&"air".equals(layout.get(new DepthsLayout.Cell(c.u(),c.h()+1,c.v())))&&"air".equals(layout.get(new DepthsLayout.Cell(c.u(),c.h()+2,c.v()))))nodes.add(c);}
        var start=new DepthsLayout.Cell(-6,0,0);var visited=new HashSet<DepthsLayout.Cell>();var queue=new ArrayDeque<DepthsLayout.Cell>();queue.add(start);visited.add(start);
        while(!queue.isEmpty()){var at=queue.removeFirst();for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}})for(int height=-1;height<=1;height++){
            var n=new DepthsLayout.Cell(at.u()+d[0],at.h()+height,at.v()+d[1]);if(nodes.contains(n)&&visited.add(n))queue.addLast(n);
        }}return visited;
    }
    public static void main(String[] args)throws Exception {
        var start=new DepthsPlan.Point(-345,71,289);
        check(DepthsPlan.placementOriginY(63,false)==63,"normal world stays underground");
        for(int surface:new int[]{-61,0,63,100})for(int direction=0;direction<4;direction++){
            var flat=new DepthsPlan(UUID.randomUUID(),start,new DepthsPlan.Point(100,DepthsPlan.placementOriginY(surface,true),100),direction,123,false,0);
            check(flat.bounds().y0()==surface+1,"flat bounding box entirely above surface, including four-layer world");
            check(flat.bossArea().y0()>surface,"flat arena above surface");
            check(DepthsLayout.build(flat).keySet().stream().allMatch(c->flat.world(c.u(),c.h(),c.v()).y()>surface),"every generated flat block above surface");
        }
        for(long seed=0;seed<12;seed++)for(int i=0;i<16;i++){var p=DepthsPlan.candidate(start,seed,i);check(Math.hypot(p.x()-start.x(),p.z()-start.z())>=1500&&Math.hypot(p.x()-start.x(),p.z()-start.z())<=2000,"Euclidean annulus incl negative coordinates");}
        check(DepthsPlan.floor(18)==-6&&DepthsPlan.floor(21)==-6&&DepthsPlan.floor(33)==-12&&DepthsPlan.floor(35)==-12&&DepthsPlan.floor(43)==-16&&DepthsPlan.floor(132)==-24,"exact descent landings");
        for(int u=-5;u<=179;u++)check(Math.abs(DepthsPlan.floor(u)-DepthsPlan.floor(u-1))<=1,"no floor discontinuity");
        check(DepthsPlan.SPAWNS.size()==12&&DepthsPlan.SPAWNS.stream().filter(s->s.guardian()>0).count()==2,"10 ordinary and2 guardians");
        check(DepthsPlan.CHESTS.size()==2,"two loot anchors");
        Map<String,String> output=new LinkedHashMap<>();int plans=0;
        for(int direction=0;direction<4;direction++)for(boolean sky:new boolean[]{false,true}){
            var plan=new DepthsPlan(new UUID(1,11),start,new DepthsPlan.Point(1568,64,-1224),direction,11,sky,0);
            var layout=DepthsLayout.build(plan);DepthsLayout.validate(plan,layout);check(layout.size()<160000,"finite edits bound");
            var reached=reachable(layout);
            for(int u=-6;u<=179;u++)check(reached.contains(new DepthsLayout.Cell(u,DepthsPlan.floor(u),0)),"connected E0–E5 axis u="+u);
            for(var spawn:DepthsPlan.SPAWNS)check(reached.contains(new DepthsLayout.Cell(spawn.u(),DepthsPlan.floor(spawn.u()),spawn.v())),"reachable spawn "+spawn);
            for(var c:DepthsPlan.CHESTS){boolean accessible=false;for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}})if(reached.contains(new DepthsLayout.Cell(c.x()+d[0],c.y()-1,c.z()+d[1])))accessible=true;check(accessible,"accessible chest "+c);}
            for(int side:new int[]{-4,4})check(reached.contains(new DepthsLayout.Cell(76,-21,side)),"pit escape to main route");
            check(layout.equals(DepthsLayout.build(plan)),"deterministic blueprint");
            int lights=0;
            for(var e:layout.entrySet()){
                var c=e.getKey();var token=e.getValue();if(token.startsWith("lantern")){
                    lights++;var support=new DepthsLayout.Cell(c.u(),c.h()+(token.contains("hanging=true")?1:-1),c.v());
                    check(solid(layout.get(support))||"chain".equals(layout.get(support)),"supported lantern "+c);
                }
                if(c.u()>=133&&(token.startsWith("lantern")||token.equals("glowstone")))output.put("light:"+c.toString(),token);
            }
            check(lights>=25&&lights<=45,"bounded lights total "+lights);
            check(layout.get(new DepthsLayout.Cell(156,-24,0))!=null&&plan.bossAnchor().equals(plan.world(156,-23,0)),"empty future boss anchor");
            if(direction==0&&!sky){
                var lines=new ArrayList<String>();for(var e:layout.entrySet()){var c=e.getKey();lines.add(c.u()+","+c.h()+","+c.v()+","+e.getValue());}
                Files.createDirectories(Path.of("work/ancient-depths-1.5.3"));Files.write(Path.of("work/ancient-depths-1.5.3/blueprint.csv"),lines);
            }
            plans++;
        }
        check(output.size()==12,"E5 exactly12 independent lights");
        System.out.println("DEPTHS PURE GEOMETRY PASS: "+checks+" assertions / "+plans+" layouts / Minecraft launches=0; not an engine collision/light/relog test");
    }
}
