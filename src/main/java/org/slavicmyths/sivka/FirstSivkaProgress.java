package org.slavicmyths.sivka;

import net.minecraft.nbt.CompoundTag;

/** Approved A policy, independent of game clocks, RNG implementations and world construction. */
public final class FirstSivkaProgress {
    public int ticks, days;
    public long centimetres, random;
    public boolean pending, met;
    public FirstSivkaProgress(long seed){random=seed;}
    public static double chance(int day){return day>=40?1:.02+.13*(Math.max(1,day)-1)/38;}
    private double roll(){random+=0x9e3779b97f4a7c15L;long z=random;z=(z^(z>>>30))*0xbf58476d1ce4e5b9L;z=(z^(z>>>27))*0x94d049bb133111ebL;return ((z^(z>>>31))>>>11)*0x1.0p-53;}
    public void sample(int elapsed,long distance){
        if(met||pending)return;
        // The caller samples once/second. Never import offline time or a legacy age.
        ticks+=Math.clamp(elapsed,0,20);centimetres=Math.min(100_000,centimetres+Math.max(0,distance));
        if(ticks<24000)return;
        ticks-=24000;
        if(centimetres>=100_000){days++;pending=days>=40||roll()<chance(days);}
        centimetres=0;
    }
    public void encounter(){met=true;pending=false;}
    public CompoundTag save(){var n=new CompoundTag();n.putInt("Ticks",ticks);n.putInt("Days",days);n.putLong("Distance",centimetres);n.putLong("Random",random);n.putBoolean("Pending",pending);n.putBoolean("Met",met);return n;}
    public static FirstSivkaProgress load(CompoundTag n){var p=new FirstSivkaProgress(n.getLong("Random"));p.ticks=Math.clamp(n.getInt("Ticks"),0,23999);p.days=Math.clamp(n.getInt("Days"),0,40);p.centimetres=Math.clamp(n.getLong("Distance"),0,100_000);p.met=n.getBoolean("Met");p.pending=!p.met&&(n.getBoolean("Pending")||p.days>=40);return p;}
}
