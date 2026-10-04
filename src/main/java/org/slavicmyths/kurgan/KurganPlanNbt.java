package org.slavicmyths.kurgan;

import net.minecraft.nbt.*;

/** Saves the actual accepted plan, never rerolls geometry when a saved piece loads. */
public final class KurganPlanNbt {
    public static CompoundNBT write(KurganPlan p){CompoundNBT n=new CompoundNBT();n.putInt("Version",KurganPlan.VERSION);n.putInt("Tier",p.tier);n.putLong("Seed",p.seed);n.putInt("Final",p.finalRoom);
        ListNBT rooms=new ListNBT();for(KurganPlan.Room r:p.rooms){CompoundNBT t=new CompoundNBT();t.putIntArray("Room",new int[]{r.id,r.floor,r.cell,r.x,r.y,r.z,r.role.ordinal(),r.palette,r.hall()?1:0,r.critical?1:0});rooms.add(t);}n.put("Rooms",rooms);
        ListNBT links=new ListNBT();for(KurganPlan.Link l:p.links){CompoundNBT t=new CompoundNBT();t.putIntArray("Link",new int[]{l.a,l.b,l.width,l.module.ordinal(),l.palette});int[] path=new int[l.steps.size()*3];for(int i=0;i<l.steps.size();i++){KurganPlan.Step s=l.steps.get(i);path[i*3]=s.x;path[i*3+1]=s.y;path[i*3+2]=s.z;}t.putIntArray("Path",path);links.add(t);}n.put("Links",links);
        if(p.seal!=null)n.putIntArray("Seal",p.seal.array());ListNBT niches=new ListNBT();for(KurganPlan.Box b:p.niches)niches.add(new IntArrayNBT(b.array()));n.put("Niches",niches);return n;
    }
    public static KurganPlan read(CompoundNBT n){if(n.getInt("Version")!=KurganPlan.VERSION)throw new IllegalArgumentException("Unsupported kurgan plan version");KurganPlan p=new KurganPlan(n.getInt("Tier"),n.getLong("Seed"));
        for(INBT v:n.getList("Rooms",10)){int[] a=((CompoundNBT)v).getIntArray("Room");KurganPlan.Room r=new KurganPlan.Room(a[0],a[1],a[2],a[3],a[4],a[5],KurganPlan.Role.values()[a[6]],a[7],a[8]!=0);r.critical=a[9]!=0;p.rooms.add(r);}
        for(INBT v:n.getList("Links",10)){CompoundNBT t=(CompoundNBT)v;int[] a=t.getIntArray("Link"),path=t.getIntArray("Path");KurganPlan.Link l=new KurganPlan.Link(a[0],a[1],a[2],KurganPlan.Module.values()[a[3]],a[4]);for(int i=0;i<path.length;i+=3)l.steps.add(new KurganPlan.Step(path[i],path[i+1],path[i+2]));if(l.a>=0)p.rooms.get(l.a).connectors.add(p.links.size());p.rooms.get(l.b).connectors.add(p.links.size());p.links.add(l);}
        p.finalRoom=n.getInt("Final");if(n.contains("Seal"))p.seal=box(n.getIntArray("Seal"));for(INBT v:n.getList("Niches",11))p.niches.add(box(((IntArrayNBT)v).getAsIntArray()));if(!p.validate().isEmpty())throw new IllegalArgumentException("Invalid saved kurgan plan");return p;
    }
    public static KurganPlan.Box box(int[] a){return new KurganPlan.Box(a[0],a[1],a[2],a[3],a[4],a[5]);}
}
