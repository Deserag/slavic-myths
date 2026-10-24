import java.util.*;
import java.nio.file.*;
import java.lang.management.ManagementFactory;
import net.minecraft.client.gui.Font;
import net.minecraft.client.StringSplitter;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import org.slavicmyths.client.*;
import com.google.gson.*;
public final class BookUiChecks {
 static int checks;static volatile Object escape;
 static void check(boolean b,String reason){checks++;if(!b)throw new AssertionError(reason);}
 static final class FixtureFont extends Font {
  int calls;final StringSplitter splitter=new StringSplitter((cp,style)->6);
  FixtureFont(){super(id->null,false);}
  @Override public List<FormattedCharSequence> split(FormattedText text,int w){calls++;return Language.getInstance().getVisualOrder(splitter.splitLines(text,w,Style.EMPTY));}
  @Override public int width(String s){return s.codePointCount(0,s.length())*6;}
  @Override public String plainSubstrByWidth(String s,int w){return splitter.plainHeadByWidth(s,w,Style.EMPTY);}
 }
 static String plain(FormattedCharSequence s){var b=new StringBuilder();s.accept((i,style,cp)->{b.appendCodePoint(cp);return true;});return b.toString();}
 static List<String> plain(List<FormattedCharSequence> s){return s.stream().map(BookUiChecks::plain).toList();}
 static void layout(){
  var f=new FixtureFont();var cache=new UiText.Layout();var text=Component.literal("Long heading with several words, line two and an ellipsis.");
  for(int width:new int[]{8,20,48,150,500})for(int rows:new int[]{1,2,5}){
   check(plain(UiText.lines(f,text,width,rows)).equals(plain(cache.lines(f,text,width,rows))),"wrap/ellipsis parity");int calls=f.calls;
   var a=cache.lines(f,text,width,rows);var b=cache.lines(f,text.copy(),width,rows);check(a==b&&f.calls==calls,"cache reuse for equal components");
  }
  int old=f.calls;cache.split(f,text,99);cache.split(f,text,100);check(f.calls==old+2,"resize invalidates");
  var mutable=Component.literal("first");cache.split(f,mutable,150);mutable.append(" changed");check(plain(cache.split(f,mutable,150)).getFirst().contains("changed"),"mutation invalidates");
  var other=new FixtureFont();cache.split(other,text,150);check(other.calls==1,"font replacement invalidates");
 }
 static void lines(){
  Random random=new Random(235237);
  for(int n=0;n<5000;n++){
   int x1=random.nextInt(800)-400,y1=random.nextInt(800)-400,x2=random.nextInt(800)-400,y2=random.nextInt(800)-400;
   Set<Long> expected=new HashSet<>(),actual=new HashSet<>();int steps=Math.max(Math.abs(x2-x1),Math.abs(y2-y1));
   for(int i=0;steps>0&&i<=steps;i++){int x=x1+(x2-x1)*i/steps,y=y1+(y2-y1)*i/steps;if(x>=-30&&x<90&&y>=-20&&y<80)expected.add(((long)x<<32)^(y&0xffffffffL));}
   UiLine.draw(x1,y1,x2,y2,-30,-20,90,80,(a,b,c,d)->{check(a>=-30&&b>=-20&&c<=90&&d<=80,"preclipped rectangle");for(int x=a;x<c;x++)for(int y=b;y<d;y++)actual.add(((long)x<<32)^(y&0xffffffffL));});
   check(expected.equals(actual),"pixel parity "+n);
  }
  int[] fills={0};UiLine.draw(-10000,10,10000,10,0,0,100,100,(a,b,c,d)->fills[0]++);check(fills[0]==1,"long horizontal clipped/coalesced");
  check(!UiLine.intersects(-32,0,32,32,0,0,100,100),"touching outside culled");check(UiLine.intersects(-31,0,32,32,0,0,100,100),"partial node kept");
 }
 static JsonObject allocation(){
  var f=new FixtureFont();var cache=new UiText.Layout();var text=Component.literal("A complete readable page with words and line wrapping. ".repeat(20));
  for(int i=0;i<5000;i++){escape=f.split(text,250);escape=cache.split(f,text,250);}
  var bean=(com.sun.management.ThreadMXBean)ManagementFactory.getThreadMXBean();bean.setThreadAllocatedMemoryEnabled(true);var out=new JsonObject();
  for(boolean cached:new boolean[]{false,true}){var samples=new JsonArray();for(int r=0;r<3;r++){
   long start=bean.getThreadAllocatedBytes(Thread.currentThread().threadId());for(int i=0;i<2000;i++)escape=cached?cache.split(f,text,250):f.split(text,250);
   samples.add((bean.getThreadAllocatedBytes(Thread.currentThread().threadId())-start)/2000.0);
  }out.add(cached?"after_bytes_op":"before_bytes_op",samples);}
  return out;
 }
 static void viewport(){
  var nodes=new LinkedHashMap<String,int[]>();
  for(int i=0;i<1000;i++)nodes.put("n"+i,new int[]{i%20*64,i/20*48});
  for(int width:new int[]{180,320,800})for(int height:new int[]{80,240,600}){
   var view=new TaleViewport();view.fit(nodes,width,height);
   for(var e:nodes.entrySet()){
    int x=view.x(17,e.getValue()[0]),y=view.y(29,e.getValue()[1]);
    check(view.hit(Map.of(e.getKey(),e.getValue()),17,29,x+view.size()/2.0,y+view.size()/2.0).equals(e.getKey()),"node hit shares drawn transform");
   }
   view.zoom(1,width,height);view.center(nodes.get("n999"),width,height);
   check(Math.abs(view.x(0,nodes.get("n999")[0])+16-width/2.0)<=1,"selected centered x");
   check(Math.abs(view.y(0,nodes.get("n999")[1])+16-height/2.0)<=1,"selected centered y");
  }
 }
 public static void main(String[]args)throws Exception{layout();lines();viewport();var out=new JsonObject();out.addProperty("checks",checks);out.addProperty("scope","offline native StringSplitter with fixed glyph widths, exact DDA pixels; not GPU frames or game FPS");out.add("allocation",allocation());Path p=Path.of("docs/implementation/book-performance-20261024/offline.json");Files.writeString(p,new GsonBuilder().setPrettyPrinting().create().toJson(out));System.out.println(out);}
}
