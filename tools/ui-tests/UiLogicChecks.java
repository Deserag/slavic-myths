import java.util.*;
import org.slavicmyths.client.*;
public final class UiLogicChecks {
 static int checks;static void check(boolean ok,String label){checks++;if(!ok)throw new AssertionError(label);}
 public static void main(String[] args){
  check(UiSearch.matches("АЛЕША","Алёша Попович"),"Yo case normalization");check(UiSearch.matches("  ясный   СОКОЛ ","Финист Ясный Сокол"),"All tokens and whitespace");check(!UiSearch.matches("ясный медведь","Ясный Сокол"),"Require every word");check(UiSearch.matches("яга","Баба-яга"),"Hyphen partial");check(UiSearch.matches("", "Любой текст"),"Empty search");check(!UiSearch.matches("[.*]","Финист"),"Literal search");
  var parents=new LinkedHashMap<String,String>();parents.put("root","");parents.put("a","root");parents.put("a1","a");parents.put("a2","a");parents.put("b","root");parents.put("b1","b");parents.put("other","");var positions=TaleTreeLayout.create(parents);check(positions.size()==parents.size(),"Complete forest");check(Arrays.equals(positions.get("a"),new int[]{64,24}),"Parent centered over children");check(positions.get("other")[1]>positions.get("b1")[1],"Separate root intervals");
  for(var item:positions.entrySet()){var p=item.getValue();check(p[0]>=0&&p[1]>=0,"Positive node");for(var other:positions.entrySet())if(!item.getKey().equals(other.getKey()))check(Math.abs(p[0]-other.getValue()[0])>=32||Math.abs(p[1]-other.getValue()[1])>=32,"No overlapping nodes");}
  check(TaleTreeLayout.create(Map.of("a","b","b","a","self","self")).size()==3,"Cycles bounded, missing roots supported");
  for(int width:new int[]{320,426,480,640,853,960}){int panel=Math.min(900,width-16),slot=Math.min(90,Math.max(32,(panel-168)/4));check(12+4*slot<=panel-154,"Loadout avoids learn button");int book=width-12,nav=Math.max(96,Math.min(220,book/3));check(book-nav-54>=100,"Reader width");}
  System.out.println("UI_LOGIC_PASS checks="+checks);
 }
}
