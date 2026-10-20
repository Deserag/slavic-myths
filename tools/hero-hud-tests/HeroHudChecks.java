import java.util.*;
import org.slavicmyths.client.HeroHudView;
import org.slavicmyths.client.HeroHudView.Entry;

/** Actual production filter/layout/draw code, no Minecraft/client/world construction. */
public class HeroHudChecks {
    static final Map<Integer,Integer> ADVANCE = new HashMap<>();
    static final UUID OWNER = new UUID(0, 100), OTHER = new UUID(0, 101);
    static Entry hero(int i) {
        String[] names = {"Илья Муромец", "Добрыня Никитич", "Алёша Попович", "Финист Ясный Сокол"};
        String[] types = {"ilya", "dobrynya", "alyosha", "finist"};
        return new Entry(new UUID(0, i+1), OWNER, types[i%4], names[i%4], 40+i*20, 280,
                i==1 ? "Отступает" : i==2 ? "Восстанавливается" : "Помогает", i==1||i==2, 4096, true);
    }
    static void require(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    static class Trace implements HeroHudView.Canvas {
        final List<String> ops = new ArrayList<>(); int labels, fills;
        public int width(String s) { return s.codePoints().map(c -> ADVANCE.getOrDefault(c,6)).sum(); }
        public String trim(String s,int w) {
            int used=0,end=0;
            for(int c:s.codePoints().toArray()){int next=ADVANCE.getOrDefault(c,6);if(used+next>w)break;used+=next;end+=Character.charCount(c);}
            return s.substring(0,end);
        }
        public void fill(int x,int y,int r,int b,int color) {
            require(r>=x&&b>=y,"invalid rectangle");fills++;
            ops.add("{\"op\":\"fill\",\"x\":"+x+",\"y\":"+y+",\"r\":"+r+",\"b\":"+b+",\"color\":"+color+"}");
        }
        public void text(String s,int x,int y,int color) {
            labels++;
            ops.add("{\"op\":\"text\",\"text\":\""+s+"\",\"x\":"+x+",\"y\":"+y+",\"color\":"+color+"}");
        }
    }
    public static void main(String[] args) throws Exception {
        System.setOut(new java.io.PrintStream(System.out,true,java.nio.charset.StandardCharsets.UTF_8));
        if(args.length>0)for(String line:java.nio.file.Files.readAllLines(java.nio.file.Path.of(args[0]))) {
            var parts=line.split("\t");ADVANCE.put(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]));
        }
        var list = new ArrayList<Entry>(); for(int i=0;i<4;i++)list.add(hero(i));
        require(HeroHudView.select(null,list).isEmpty(),"no player");
        require(HeroHudView.select(OTHER,list).isEmpty(),"other owner");
        list.add(hero(0));require(HeroHudView.select(OWNER,list).size()==4,"UUID dedup preserves four types");
        var e=hero(0);
        require(HeroHudView.select(OWNER,List.of(new Entry(e.id(),e.owner(),e.type(),e.name(),1,2,"",false,4096.01,true))).isEmpty(),"range remains 64");
        require(HeroHudView.select(OWNER,List.of(new Entry(e.id(),e.owner(),e.type(),e.name(),1,2,"",false,0,false))).isEmpty(),"dead");
        int cases=0;
        for(int[] screen:new int[][]{{1280,720},{1920,1080}})for(int scale=1;scale<=4;scale++)for(int count=0;count<=4;count++) {
            int w=(int)Math.ceil(screen[0]/(double)scale),h=(int)Math.ceil(screen[1]/(double)scale);
            var heroes=new ArrayList<Entry>();for(int i=0;i<count;i++)heroes.add(hero(i));
            var slots=HeroHudView.layout(w,h,count,0,0);require(slots.size()==count,"all count="+count);
            for(int i=0;i<slots.size();i++) {
                var a=slots.get(i);
                require(a.x()>=0&&a.y()>=0&&(a.x()+a.width())*HeroHudView.SCALE<=w&&(a.y()+a.height())*HeroHudView.SCALE<=h,"screen bounds");
                require((a.y()+a.height())*HeroHudView.SCALE<=h-Math.min(64,h/3)+1,"vanilla bottom reserve");
                for(int j=i+1;j<slots.size();j++){var b=slots.get(j);require(a.x()+a.width()<=b.x()||b.x()+b.width()<=a.x()||a.y()+a.height()<=b.y()||b.y()+b.height()<=a.y(),"no overlap");}
            }
            var trace=new Trace();HeroHudView.draw(trace,heroes,slots);require(trace.labels==3*count&&trace.fills==3*count,"every row retains name HP state and bar");cases++;
        }
        // Overflow wraps and configurable offsets clamp; no hidden hard cap for loaded entities.
        for(int n:new int[]{5,8,12})require(HeroHudView.layout(320,180,n,4096,4096).size()==n,"wrap all "+n);
        var huge=new Entry(e.id(),e.owner(),e.type(),"Очень длинное локализованное имя героя".repeat(4),0,0,"Очень длинный статус".repeat(4),true,0,true);
        var trace=new Trace();var slots=HeroHudView.layout(320,180,1,-4096,-4096);HeroHudView.draw(trace,List.of(huge),slots);
        require(trace.ops.stream().anyMatch(s->s.contains("…")),"explicit ellipsis for clipped text");
        System.out.println("{\"result\":\"HERO_HUD_OFFLINE PASS\",\"cases\":"+cases+",\"runtime\":\"NOT_RUN\",\"previews\":[");
        for(int n=1;n<=4;n++) {
            var heroes=new ArrayList<Entry>();for(int i=0;i<n;i++)heroes.add(hero(i));heroes=new ArrayList<>(HeroHudView.select(OWNER,heroes));
            var capture=new Trace();HeroHudView.draw(capture,heroes,HeroHudView.layout(320,180,n,0,0));
            System.out.print("{\"count\":"+n+",\"ops\":["+String.join(",",capture.ops)+"]}"+(n<4?",":""));
        }
        System.out.println("]}");
    }
}
