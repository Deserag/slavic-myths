import java.io.*;
import net.minecraft.nbt.*;
import org.slavicmyths.brewing.BrewRules;
import net.neoforged.neoforge.attachment.AttachmentType;

/** Native codec/disk roundtrip only: no world, player, game or relog is created. */
public class IntoxicationNbtChecks {
 public static void main(String[] args) throws Exception {
  IntoxicationChecks.main(args);
  CompoundTag state=new CompoundTag();state.putInt("Points",6);state.putLong("LastDrink",3200L);
  Tag encoded=CompoundTag.CODEC.encodeStart(NbtOps.INSTANCE,state).getOrThrow();
  CompoundTag decoded=CompoundTag.CODEC.parse(NbtOps.INSTANCE,encoded).getOrThrow();
  if(!decoded.equals(state))throw new AssertionError("production attachment codec");
  ByteArrayOutputStream bytes=new ByteArrayOutputStream();NbtIo.write(decoded,new DataOutputStream(bytes));
  CompoundTag loaded=NbtIo.read(new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())));
  if(!loaded.equals(state))throw new AssertionError("native NBT persistence");
  if(BrewRules.decay(loaded.getInt("Points"),loaded.getLong("LastDrink"),4400L)!=5)throw new AssertionError("decay after loaded state");
  if(BrewRules.addLoad(BrewRules.decay(loaded.getInt("Points"),loaded.getLong("LastDrink"),4400L),1)!=6)throw new AssertionError("drinking after loaded state");
  if(BrewRules.decay(loaded.getInt("Points"),loaded.getLong("LastDrink"),3200L)!=6)throw new AssertionError("paused world clock");
  // Same builder/codec as Brewing.LOAD; assert installed NeoForge default policy.
  var type=AttachmentType.builder(()->new CompoundTag()).serialize(CompoundTag.CODEC).build();
  var flag=AttachmentType.class.getDeclaredField("copyOnDeath");flag.setAccessible(true);
  if(flag.getBoolean(type))throw new AssertionError("default death policy changed");
  System.out.println("INTOXICATION_NATIVE PASS: actual CompoundTag codec, binary NBT, resumed decay, repeated dose, NeoForge copyOnDeath=false default. Actual player death/relog NOT_RUN.");
 }
}
