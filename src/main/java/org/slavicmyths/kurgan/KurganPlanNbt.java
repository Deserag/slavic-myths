package org.slavicmyths.kurgan;
import net.minecraft.nbt.*;
/** Persist accepted geometry. Version 1 is loaded literally, never regenerated with version 2. */
public final class KurganPlanNbt {
 public static CompoundTag write(KurganPlan p){
  CompoundTag n=new CompoundTag();n.putInt("Version",p.formatVersion);n.putInt("Floors",p.floors);n.putInt("Tier",p.tier);n.putLong("Seed",p.seed);n.putInt("Final",p.finalRoom);
  ListTag rooms=new ListTag();for(var r:p.rooms){CompoundTag t=new CompoundTag();t.putIntArray("Room",new int[]{r.id,r.floor,r.cell,r.x,r.y,r.z,r.role.ordinal(),r.palette,r.hall()?1:0,r.critical?1:0});
   if(p.formatVersion>=2){t.putIntArray("Shape",new int[]{r.rx,r.rz,r.height,r.archetype==null?-1:r.archetype.ordinal(),r.nodeType.ordinal(),r.roomNode?1:0,r.mandatory?1:0});t.putString("Loot",r.loot);}rooms.add(t);
  }n.put("Rooms",rooms);
  ListTag links=new ListTag();for(var l:p.links){CompoundTag t=new CompoundTag();t.putIntArray("Link",new int[]{l.a,l.b,l.width,l.module.ordinal(),l.palette});
   if(p.formatVersion>=2){t.putBoolean("Secret",l.secret);t.putInt("Style",l.style);t.putInt("TransitionStyle",l.transitionStyle);}
   int[] path=new int[l.steps.size()*3];for(int i=0;i<l.steps.size();i++){var s=l.steps.get(i);path[i*3]=s.x;path[i*3+1]=s.y;path[i*3+2]=s.z;}t.putIntArray("Path",path);links.add(t);
  }n.put("Links",links);if(p.seal!=null)n.putIntArray("Seal",p.seal.array());ListTag niches=new ListTag();for(var b:p.niches)niches.add(new IntArrayTag(b.array()));n.put("Niches",niches);return n;
 }
 public static KurganPlan read(CompoundTag n){
  int version=n.getInt("Version"),tier=n.getInt("Tier");if(version<1||version>KurganPlan.VERSION||tier<0||tier>2)throw new IllegalArgumentException("Unsupported kurgan plan");
  KurganPlan p=new KurganPlan(tier,n.getLong("Seed"));p.formatVersion=version;if(n.contains("Floors"))p.floors=n.getInt("Floors");
  for(Tag raw:n.getList("Rooms",10)){CompoundTag t=(CompoundTag)raw;int[] a=t.getIntArray("Room");if(a.length!=10||a[0]!=p.rooms.size())throw new IllegalArgumentException("Invalid room record");KurganPlan.Room r;
   if(version>=2){int[] s=t.getIntArray("Shape");if(s.length!=7)throw new IllegalArgumentException("Invalid room shape");r=new KurganPlan.Room(a[0],a[1],a[2],a[3],a[4],a[5],KurganPlan.Role.values()[a[6]],a[7],s[0],s[1],s[2],s[3]<0?null:KurganPlan.Archetype.values()[s[3]],KurganPlan.NodeType.values()[s[4]],s[5]!=0,t.getString("Loot"));r.mandatory=s[6]!=0;}
   else r=new KurganPlan.Room(a[0],a[1],a[2],a[3],a[4],a[5],KurganPlan.Role.values()[a[6]],a[7],a[8]!=0);r.critical=a[9]!=0;p.rooms.add(r);
  }
  for(Tag raw:n.getList("Links",10)){CompoundTag t=(CompoundTag)raw;int[] a=t.getIntArray("Link"),path=t.getIntArray("Path");if(a.length!=5||path.length%3!=0)throw new IllegalArgumentException("Invalid link record");var l=new KurganPlan.Link(a[0],a[1],a[2],KurganPlan.Module.values()[a[3]],a[4]);l.secret=t.getBoolean("Secret");l.style=t.getInt("Style");l.transitionStyle=t.getInt("TransitionStyle");for(int i=0;i<path.length;i+=3)l.steps.add(new KurganPlan.Step(path[i],path[i+1],path[i+2]));if(l.a>=0)p.rooms.get(l.a).connectors.add(p.links.size());p.rooms.get(l.b).connectors.add(p.links.size());p.links.add(l);
  }
  p.finalRoom=n.getInt("Final");if(n.contains("Seal"))p.seal=box(n.getIntArray("Seal"));for(Tag raw:n.getList("Niches",11))p.niches.add(box(((IntArrayTag)raw).getAsIntArray()));if(!p.validate().isEmpty())throw new IllegalArgumentException("Invalid saved kurgan plan: "+p.validate());return p;
 }
 public static KurganPlan.Box box(int[] a){if(a.length!=6)throw new IllegalArgumentException("Invalid box");return new KurganPlan.Box(a[0],a[1],a[2],a[3],a[4],a[5]);}
}
