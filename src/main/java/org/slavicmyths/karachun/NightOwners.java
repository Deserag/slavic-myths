package org.slavicmyths.karachun;
import java.util.UUID;
import net.minecraft.world.entity.Entity;
/** Credit existing companions without changing their availability or controllers. */
public final class NightOwners {
 public static UUID owner(Entity e){if(e instanceof NightRaven r)return r.allied()?r.owner:null;if(e instanceof org.slavicmyths.quest185.WolfSupport wolf)return wolf.owner();if(e instanceof net.minecraft.world.entity.TamableAnimal pet)return pet.getOwnerUUID();return org.slavicmyths.morozko.FrostWorld.owner(e);}
 private NightOwners(){}
}
