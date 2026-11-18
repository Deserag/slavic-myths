package org.slavicmyths.verify;
import java.nio.file.*;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.*;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.navigation.*;
import org.slavicmyths.tales.*;
public final class Navigation296Offline {
 static int checks;static void check(boolean value,String reason){checks++;if(!value)throw new AssertionError(reason);}
 static void language(String locale)throws Exception{
  var map=new HashMap<String,String>();try(var in=Files.newInputStream(Path.of("src/main/resources/assets/slavicmyths/lang/"+locale+".json"))){Language.loadFromJson(in,map::put);}
  Language.inject(new Language(){public String getOrDefault(String key,String fallback){return map.getOrDefault(key,fallback);}public boolean has(String key){return map.containsKey(key);}public boolean isDefaultRightToLeft(){return false;}public net.minecraft.util.FormattedCharSequence getVisualOrder(FormattedText text){return net.minecraft.util.FormattedCharSequence.forward(text.getString(),Style.EMPTY);}});
 }
 static CompoundTag hint(double x,double z,int r){var h=new CompoundTag();h.putDouble("X",x);h.putDouble("Z",z);h.putInt("Radius",r);h.putString("Dimension","minecraft:overworld");h.putLong("Expires",10000);return h;}
 static String display(CompoundTag h,String dim,double x,double z,long now){return SearchGuidance.hint(h,dim,x,z,now).getString();}
 public static void main(String[] args)throws Exception{
  net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  var examples=new LinkedHashMap<String,Object>();
  for(String locale:List.of("ru_ru","en_us")){
   language(locale);var lines=new ArrayList<String>();
   for(int radius:new int[]{180,250,320,380,550,700})for(double x:new double[]{-29_999_980,-1500.25,-.25,0,1500.25,29_999_980}){
    double z=-x/2;var h=hint(x,z,radius);h.putLong("Anchor",123456789);h.putUUID("Target",UUID.randomUUID());h.putInt("Y",1234);h.putString("Secret","PRIVATE");
    var old=h.copy();var pub=TaleHintVisibility.publicCopy(h,false);check(h.equals(old),"redaction mutated save");check(!pub.contains("Anchor")&&!pub.contains("Target")&&!pub.contains("Y")&&!pub.contains("Secret"),"private leakage");
    var text=display(pub,"minecraft:overworld",x,z,100);check(!text.contains("navigation.search."),"missing translation "+locale);check(!text.contains("PRIVATE")&&!text.contains("123456789"),"private text");check(text.contains(Integer.toString(radius)),"radius substituted");check(!text.contains("Y "),"unknown height leaked");
    check(SearchCoordinates.lower(x,radius)<=SearchCoordinates.upper(x,radius),"inverted range");check(SearchCoordinates.lower(x,radius)>=-SearchCoordinates.WORLD_LIMIT&&SearchCoordinates.upper(x,radius)<=SearchCoordinates.WORLD_LIMIT,"world overflow");
    check(SearchCoordinates.inside(x,z,radius,x,z),"center outside");check(!SearchCoordinates.inside(x,z,radius,x+radius+1,z),"outside counted inside");check(SearchCoordinates.distance(x,z,radius,x+radius+31,z)==31,"boundary distance");
    var stale=pub.copy();stale.putBoolean("Stale",true);check(!display(stale,"minecraft:overworld",x,z,100).contains("X "),"stale coordinates");check(!display(pub,"minecraft:overworld",x,z,10001).contains("X "),"expired coordinates");
    var other=display(pub,"minecraft:the_nether",x,z,100);check(other.contains(locale.equals("ru_ru")?"другом измерении":"another dimension"),"dimension status");check(!other.contains(locale.equals("ru_ru")?"Вы внутри":"You are inside"),"inside wrong dimension");
   }
   for(String key:List.of("X","Z","Radius","Dimension")){var h=hint(12,34,250);h.remove(key);check(!display(h,"minecraft:overworld",0,0,0).contains("X "),"missing default coordinates "+key);}
   for(double invalid:new double[]{Double.NaN,Double.POSITIVE_INFINITY,30_002_000})check(!display(hint(invalid,0,250),"minecraft:overworld",0,0,0).contains("X "),"invalid center");
   for(int invalid:new int[]{0,-1,Integer.MAX_VALUE})check(!display(hint(0,0,invalid),"minecraft:overworld",0,0,0).contains("X "),"invalid radius");
   var fraction=hint(0,0,250);fraction.putDouble("Radius",250.5);check(!display(fraction,"minecraft:overworld",0,0,0).contains("X "),"fractional radius truncated");
   for(var scale:SearchArea.Scale.values()){
    var a=SearchArea.generate(-1500,500,scale,UUID.nameUUIDFromBytes(scale.name().getBytes(java.nio.charset.StandardCharsets.UTF_8)),296,0,(x,z,r)->true);check(a!=null,"native area not generated");
    var m=new SlavicMarker(UUID.randomUUID(),"qa296",MarkerCategory.QUEST,SlavicMarker.Kind.SEARCH_AREA,SlavicMarker.Lifetime.UNTIL_QUEST_END,"test",ResourceLocation.parse("minecraft:overworld"),9876543,1234,8765432,0,0,a);
    var loaded=SlavicMarker.read(m.save());check(loaded.equals(m),"legacy NBT changed");var text=SearchGuidance.marker(loaded,"minecraft:overworld",0,0,true).getString();check(text.contains(Integer.toString(a.radius())),"marker radius changed");check(!text.contains("Y ")&&!text.contains("9876543")&&!text.contains("8765432"),"search marker placeholder leaked");
    for(var state:SearchArea.State.values()){var v=m.withArea(a.withState(state));String status=SearchGuidance.marker(v,"minecraft:overworld",0,0,true).getString();check(!status.contains("navigation.search."),"state untranslated");if(state==SearchArea.State.EXPIRED||state==SearchArea.State.COMPLETED)check(!status.contains("X "),"ended state geometry remains current");}
    check(!SearchGuidance.marker(m,"minecraft:overworld",0,0,false).getString().contains("X "),"ended marker coordinates");
    lines.add(scale+" center="+a.centerX()+","+a.centerZ()+" radius="+a.radius()+"\n"+text);
   }
   examples.put(locale,lines);
  }
  var tale=new TaleData();var first=UUID.randomUUID();var second=UUID.randomUUID();var legacy=hint(-1200.5,340.25,250);legacy.putUUID("Target",UUID.randomUUID());legacy.putLong("Anchor",99112233);TaleData.compound(tale.player(first),"Hints").put("legacy",legacy);
  var saved=tale.save(new CompoundTag(),null);var reloaded=TaleData.load(saved,null);check(reloaded.player(first).getCompound("Hints").getCompound("legacy").equals(legacy),"old hint save roundtrip changed");check(reloaded.player(second).getCompound("Hints").isEmpty(),"cross-player state leakage");
  var projected=TaleHintVisibility.publicCopy(reloaded.player(first).getCompound("Hints").getCompound("legacy"),false);var delta=new CompoundTag();var publicHints=new CompoundTag();publicHints.put("legacy",projected);delta.put("Hints",publicHints);var merged=TaleSnapshotMerge.merge(new CompoundTag(),delta,false);check(merged.getCompound("Hints").getCompound("legacy").equals(projected),"hint delta merge changed");
  check(SearchCoordinates.number(-.01).equals("0"),"negative zero");
  for(int[] resolution:new int[][]{{854,480},{1920,1080}})for(int scale=1;scale<=4;scale++){
   int width=(resolution[0]+scale-1)/scale,height=(resolution[1]+scale-1)/scale;if(width<320||height<240)continue;
   for(boolean sidebar:new boolean[]{true,false}){int bookWidth=Math.min(1000,width-12),left=(width-bookWidth)/2,navWidth=sidebar?Math.max(90,Math.min(210,bookWidth/3)):0,contentX=left+(sidebar?navWidth+20:14),contentWidth=Math.max(30,left+bookWidth-14-contentX);check(contentX>=0&&contentX+contentWidth<=width,"horizontal viewport bounds");check(contentWidth>=80,"unreadable column width");int h=height-12,top=6,bottom=top+h-55,start=top+99,visible=SearchCoordinates.visibleRows(start,bottom);check(visible>0&&start+(visible-1)*12+9<=bottom,"book viewport geometry");
    for(int lines=1;lines<160;lines++){int end=SearchCoordinates.scroll(Integer.MAX_VALUE,lines,visible);check(end+visible>=lines,"last text inaccessible");check(SearchCoordinates.scroll(-4,lines,visible)==0,"negative scroll");}
   }
   check(58+9<=height-78&&height-68+20<height-43,"marker actions overlap text");
  }
  var output=Path.of("docs/implementation/navigation296-20261025/offline-examples.json");Files.writeString(output,new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(examples));
  System.out.println("PASS "+checks+" native offline checks; no Minecraft client/server/world. Visual, relog and MP NOT_RUN.");
 }
}
