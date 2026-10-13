package org.slavicmyths.mor;

import net.minecraft.sounds.*;
import org.slavicmyths.entity.LandSpiritEntity;

/** Actual 1.21.1 vanilla events; no synthetic silence, music or external audio. */
public final class MorAudio {
    private static void play(LandSpiritEntity m,SoundEvent s,float volume,float pitch){m.playSound(s,volume,pitch);}
    public static void transform(LandSpiritEntity m,int kind,boolean end){play(m,switch(kind){case 1->SoundEvents.WOOD_BREAK;case 2->end?SoundEvents.AXE_SCRAPE:SoundEvents.GRASS_BREAK;case 3->end?SoundEvents.SILVERFISH_HURT:SoundEvents.CROSSBOW_LOADING_MIDDLE.value();default->end?SoundEvents.BLAZE_AMBIENT:SoundEvents.FIRE_AMBIENT;},kind==1?.8F:kind==4?.8F:end?.65F:.7F,kind==4?.8F:.75F);}
    public static void idle(LandSpiritEntity m,int kind){play(m,switch(kind){case 1->SoundEvents.WOOD_BREAK;case 2->SoundEvents.GRASS_STEP;case 3->SoundEvents.SILVERFISH_AMBIENT;default->SoundEvents.BLAZE_AMBIENT;},kind==1?.55F:kind==2?.5F:kind==3?.45F:.6F,.8F);}
    public static void start(LandSpiritEntity m,int skill){play(m,switch(skill){case 1->SoundEvents.ROOTED_DIRT_BREAK;case 2->SoundEvents.WOOD_STEP;case 3->SoundEvents.BEACON_POWER_SELECT;case 4->SoundEvents.PLAYER_ATTACK_SWEEP;case 5->SoundEvents.WITCH_CELEBRATE;case 6->SoundEvents.CROSSBOW_LOADING_MIDDLE.value();case 7->SoundEvents.BLAZE_AMBIENT;case 8->SoundEvents.PHANTOM_FLAP;default->SoundEvents.ILLUSIONER_MIRROR_MOVE;},skill==2?.4F:skill==9?.9F:.75F,.8F);}
    public static void tick(LandSpiritEntity m,int skill,int tick){
        if(skill==1&&tick==24)play(m,SoundEvents.WOOD_BREAK,.9F,.75F);
        if(skill==2&&tick==12)play(m,SoundEvents.RAVAGER_ROAR,.95F,.75F);
        if(skill==3&&tick==20)play(m,SoundEvents.BREEZE_WIND_CHARGE_BURST.value(),.85F,.8F);
        if(skill==4&&(tick==14||tick==19||tick==25))play(m,SoundEvents.PLAYER_ATTACK_SWEEP,.7F,.85F);
        if(skill==5&&(tick==10||tick==16||tick==22))play(m,SoundEvents.WOOL_STEP,.35F,.8F);
        if(skill==6&&tick==16)play(m,SoundEvents.TRIPWIRE_ATTACH,.55F,.8F);
        if(skill==7&&MorRules.release(skill,tick))play(m,SoundEvents.BLAZE_SHOOT,.7F,.8F);
        if(skill==8&&tick==18)play(m,SoundEvents.ELYTRA_FLYING,.75F,.8F);
    }
    public static SoundEvent hurt(int kind){return switch(kind){case 1->SoundEvents.WOOD_BREAK;case 2->SoundEvents.PHANTOM_HURT;case 3->SoundEvents.SILVERFISH_HURT;case 5->SoundEvents.WITCH_HURT;case 6->SoundEvents.RAVAGER_HURT;default->SoundEvents.BLAZE_HURT;};}
    public static SoundEvent death(int kind){return switch(kind){case 1->SoundEvents.WOOD_BREAK;case 2->SoundEvents.PHANTOM_DEATH;case 3->SoundEvents.SILVERFISH_DEATH;case 5->SoundEvents.WITCH_DEATH;case 6->SoundEvents.RAVAGER_DEATH;default->SoundEvents.BLAZE_DEATH;};}
    public static void split(LandSpiritEntity m){play(m,SoundEvents.BLAZE_SHOOT,.8F,.8F);}
    private MorAudio(){}
}
