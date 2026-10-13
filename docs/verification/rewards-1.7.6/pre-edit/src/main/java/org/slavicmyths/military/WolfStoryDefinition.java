package org.slavicmyths.military;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.google.gson.JsonParser;

/** Four authored definitions for this story, not a separate global quest framework. */
public record WolfStoryDefinition(String id, String titleKey, String rewardPolicy,
                                  String rewardId, String prerequisiteId, String icon,
                                  int sortOrder, int required, String objectiveKind) {
 public static final List<WolfStoryDefinition> ALL=load();
 private static List<WolfStoryDefinition> load(){
  try(var stream=WolfStoryDefinition.class.getResourceAsStream("/data/slavicmyths/wolf_story/quests.json")){
   if(stream==null)throw new IllegalStateException("Missing wolf story definitions");
   var definitions=new java.util.ArrayList<WolfStoryDefinition>();
   for(var entry:JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonArray()){
    var q=entry.getAsJsonObject();var objective=q.getAsJsonArray("objectives").get(0).getAsJsonObject();
    definitions.add(new WolfStoryDefinition(q.get("id").getAsString(),q.get("title_key").getAsString(),q.get("reward_visibility_policy").getAsString(),q.get("reward_id").getAsString(),q.get("prerequisite_id").getAsString(),q.get("icon").getAsString(),q.get("sort_order").getAsInt(),objective.get("required").getAsInt(),objective.get("kind").getAsString()));
   }
   if(definitions.size()!=4)throw new IllegalStateException("Wolf story must contain exactly four definitions");
   for(int i=0;i<4;i++)if(definitions.get(i).sortOrder()!=i)throw new IllegalStateException("Invalid wolf story order");
   return List.copyOf(definitions);
  }catch(java.io.IOException e){throw new IllegalStateException("Cannot read wolf story definitions",e);}
 }
}
