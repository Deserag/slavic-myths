package org.slavicmyths.sisters;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Visible death-mark debuff, executed only when the victim has strictly less than four health. */
public final class NeveyaDeathMarkEffect extends MobEffect{
    public NeveyaDeathMarkEffect(){super(MobEffectCategory.HARMFUL,0x3557A2);}
}
