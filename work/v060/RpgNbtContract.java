import java.io.*;
import java.util.*;
import net.minecraft.nbt.*;
import org.slavicmyths.rpg.RpgNbt;
public class RpgNbtContract {
 static int checks;
 static void check(boolean value,String why){checks++;if(!value)throw new AssertionError(why);}
 public static void main(String[] args)throws Exception{
  CompoundNBT persisted=new CompoundNBT();persisted.putString("OtherMod","keep");
  CompoundNBT path=RpgNbt.player(persisted);check(path.getString("Main").isEmpty(),"safe old-world main default");check(path.getCompound("Skills").isEmpty(),"safe old-world skills default");
  path.putString("Main","druzhinnik");path.putString("Secondary","koldun");CompoundNBT skills=new CompoundNBT();skills.putInt("drill",1);skills.putInt("sign_sense",1);path.put("Skills",skills);
  ByteArrayOutputStream bytes=new ByteArrayOutputStream();CompressedStreamTools.writeCompressed(persisted,bytes);CompoundNBT loaded=CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes.toByteArray()));
  check(loaded.equals(persisted),"save/reload NBT roundtrip");CompoundNBT clone=loaded.copy();RpgNbt.player(clone).putString("Main","vedun");check(RpgNbt.player(loaded).getString("Main").equals("druzhinnik"),"clone does not alias original");check(clone.getString("OtherMod").equals("keep"),"unrelated persisted player data retained");
  CompoundNBT item=JsonToNBT.parseTag("{Damage:42,RepairCost:7,display:{Name:'{\"text\":\"Legacy Blade\"}'},Enchantments:[{id:\"minecraft:sharpness\",lvl:5s},{id:\"minecraft:mending\",lvl:1s}],OtherMod:{seed:123L}}");CompoundNBT before=item.copy();
  RpgNbt.runes(item,1,Collections.emptyList());RpgNbt.runes(item,1,Arrays.asList("thunder"));RpgNbt.runes(item,2,Arrays.asList("thunder","heat"));RpgNbt.runes(item,2,Arrays.asList("heat"));
  CompoundNBT onlyLegacy=item.copy();onlyLegacy.remove("SlavicRunes");check(before.equals(onlyLegacy),"enchantments, name, damage, repair cost and other NBT preserved through reforge/install/remove");check(item.getCompound("SlavicRunes").getList("Runes",8).getString(0).equals("heat"),"removal frees one socket and preserves remaining rune");
  CompoundNBT valid=item.copy();for(int i=0;i<3;i++){boolean rejected=false;try{RpgNbt.runes(item,i==0?4:1,i==1?Arrays.asList("heat","heat"):i==2?Arrays.asList("heat","wind"):Collections.emptyList());}catch(IllegalArgumentException expected){rejected=true;}check(rejected,"reject invalid socket request "+i);check(item.equals(valid),"failed operation atomic "+i);}
  System.out.println("PASS "+checks+" NBT contract checks; no client, server or world started.");
 }
}
