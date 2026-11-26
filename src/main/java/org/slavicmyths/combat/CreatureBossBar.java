package org.slavicmyths.combat;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;
/** Shared numeric health label; existing phase text and special encounter health remain intact. */
public final class CreatureBossBar extends ServerBossEvent {
 private final LivingEntity owner;
 private Component label;
 public CreatureBossBar(LivingEntity owner,Component name,BossEvent.BossBarColor color,BossEvent.BossBarOverlay overlay){
  super(name,color,overlay);this.owner=owner;this.label=name;
 }
 @Override public void setName(Component name){label=name;refresh();}
 @Override public void setProgress(float progress){super.setProgress(progress);refresh();}
 @Override public void setColor(BossEvent.BossBarColor color){super.setColor(owner==null?color:BossBarPalette.colorFor(owner.getType(),color));}
 private void refresh(){
  if(label==null)return;
  if(owner==null){super.setName(label);return;}
  double maximum=owner.getMaxHealth();
  if(owner instanceof org.slavicmyths.ninemor.NineMorSerpent)maximum=org.slavicmyths.ninemor.NineMorSerpent.EFFECTIVE_MAX_HEALTH;
  if(owner instanceof org.slavicmyths.morovoy.MorovoyFireZmey)maximum=org.slavicmyths.morovoy.MorovoyFireZmey.EFFECTIVE_HP;
  if(org.slavicmyths.dungeon.DungeonGuardians.guardian(owner))maximum=org.slavicmyths.dungeon.DungeonGuardians.logicalHealth(owner);
  // These legacy labels already contain their own logical health display.
  if(label.getString().matches(".*[0-9]\\s*/\\s*[0-9].*")){super.setName(label);return;}
  long current=Math.max(0,Math.round(getProgress()*maximum));
  var name=label.copy();if(org.slavicmyths.dungeon.DungeonGuardians.guardian(owner))name.append(" • ").append(Component.translatable("effect.slavicmyths.dungeon_guardian"));
  super.setName(name.append("  "+current+" / "+Math.round(maximum)+" HP"));
 }
}