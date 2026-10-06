package org.slavicmyths.entity;

import net.minecraft.util.RandomSource;

/** One roll per encounter; never roll in getType(), rendering or registry lookup. */
public record WildlifeFamilyPlan(Adult adult, int babies) {
 public enum Adult { BEAR, MOTHER_BEAR, STAG, DOE }
 public static WildlifeFamilyPlan bear(RandomSource random, boolean natural) {
  boolean family=random.nextInt(100)<(natural?6:10);
  return new WildlifeFamilyPlan(family?Adult.MOTHER_BEAR:Adult.BEAR,family?1+random.nextInt(2):0);
 }
 public static WildlifeFamilyPlan deer(RandomSource random) {
  int roll=random.nextInt(100);
  return roll<45?new WildlifeFamilyPlan(Adult.STAG,0):roll<90?new WildlifeFamilyPlan(Adult.DOE,0)
   :new WildlifeFamilyPlan(Adult.DOE,random.nextInt(100)<35?2:1);
 }
}
