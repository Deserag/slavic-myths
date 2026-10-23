package org.slavicmyths.verify;
import java.util.*;
import java.nio.file.*;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionHand;
import org.slavicmyths.finist.*;
import org.slavicmyths.item.ItemState;

/** Actual bow release, Curios quiver, native hit/mark and projectile ticks. */
@GameTestHolder("slavicmyths_weapons") @PrefixGameTestTemplate(false)
public final class Finist217ShotTests {
 @GameTest(template="empty",timeoutTicks=400) public static void markedShotsAnglesAndDraw(GameTestHelper t)throws Exception{
  var p=Issue217GameTests.player(t,"FinistShots217");var w=t.getLevel();p.moveTo(p.getX(),200,p.getZ());
  // Real floor: an air-only fixture hid weak backward shots striking the ground.
  for(int x=-32;x<=32;x++)for(int z=-32;z<=32;z++)w.setBlock(p.blockPosition().offset(x,-1,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
  var bow=Issue217GameTests.item("finist_bow");p.setItemInHand(InteractionHand.MAIN_HAND,bow);
  var quiver=Issue217GameTests.item("finist_quiver");var slots=top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(p).orElseThrow();slots.setEquippedCurio("belt",0,quiver);
  quiver=FinistQuiver.equipped(p);t.assertTrue(!quiver.isEmpty(),"Actual active Curios belt did not equip quiver");
  var target=EntityType.ZOMBIE.create(w);target.setNoAi(true);target.setNoGravity(true);target.moveTo(p.getX(),p.getY(),p.getZ()+8);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);target.setHealth(1000);w.addFreshEntity(target);
  var report=new ArrayList<Map<String,Object>>();
  // First arrow must establish the mark through native collision/events, not by writing NBT.
  for(int shot=0;shot<91;shot++){
   int draw=shot==0?30:new int[]{8,18,30}[((shot-1)/10)%3];int angle=shot==0?0:new int[]{0,10,45,90,135,180,-10,-45,-90,-135}[(shot-1)%10];int distance=shot==0?8:new int[]{8,16,24}[(shot-1)/30];target.moveTo(p.getX(),p.getY(),p.getZ()+distance);
   target.setHealth(1000);target.invulnerableTime=0;quiver.set(ItemState.FINIST_QUIVER.get(),new ItemState.QuiverState(12,0,"qa217"));
   p.setYRot(angle);p.setXRot(0);p.setShiftKeyDown(false);p.startUsingItem(InteractionHand.MAIN_HAND);
   bow.getItem().releaseUsing(bow,w,p,bow.getUseDuration(p)-draw);p.stopUsingItem();
   var arrows=w.getEntitiesOfClass(FinistArrow.class,p.getBoundingBox().inflate(4));t.assertTrue(arrows.size()==1,"Bow did not spawn exactly one actual arrow at draw="+draw+" angle="+angle);
   var arrow=arrows.getFirst();boolean guided=target.getUUID().equals(arrow.guide());t.assertTrue(shot==0?!guided:guided,"Wrong native mark/guide assignment shot="+shot);
   double speed=arrow.getDeltaMovement().length();t.assertTrue(Math.abs(speed-((FinistBow)bow.getItem()).power(draw)*3.6)<.06,"Incorrect draw power/speed");
   t.assertTrue(FinistQuiver.charges(quiver)==11,"Shot did not spend exactly one quiver charge");
   double nearest=arrow.distanceTo(target);int steps=0;for(;steps<160&&!arrow.isRemoved()&&target.getHealth()==1000;steps++){arrow.tick();nearest=Math.min(nearest,arrow.distanceTo(target));}
   boolean hit=target.getHealth()<1000;var row=new LinkedHashMap<String,Object>();row.put("shot",shot);row.put("drawTicks",draw);row.put("yawOffsetDegrees",angle);row.put("targetDistance",distance);row.put("initialSpeed",speed);row.put("guidedAtLaunch",guided);row.put("hit",hit);row.put("ticks",steps);row.put("nearestCenterDistance",nearest);row.put("damage",1000-target.getHealth());row.put("guideRetained",target.getUUID().equals(arrow.guide()));report.add(row);
   System.out.println("ISSUE217_FINIST_SHOT "+new com.google.gson.Gson().toJson(row));arrow.discard();
   t.assertTrue(FinistMarks.target(p)==target,"Native first hit did not establish/retain target mark shot="+shot);
   t.assertTrue(hit,"Marked shot missed at draw="+draw+" angle="+angle+" distance="+distance);
  }
  Path out=Path.of("../../..", "docs/implementation/weapons87/issue217/finist-91-shots-after223.json").normalize();Files.createDirectories(out.getParent());Files.writeString(out,new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(report));
  // Shift/manual aim must still opt out of the existing mark.
  quiver.set(ItemState.FINIST_QUIVER.get(),new ItemState.QuiverState(12,0,"qa217"));p.setShiftKeyDown(true);p.setYRot(90);p.startUsingItem(InteractionHand.MAIN_HAND);bow.getItem().releaseUsing(bow,w,p,bow.getUseDuration(p)-30);p.stopUsingItem();var manual=w.getEntitiesOfClass(FinistArrow.class,p.getBoundingBox().inflate(4)).getFirst();t.assertTrue(manual.guide()==null&&manual.getPersistentData().getBoolean("FinistManual"),"Manual aim acquired guide despite Shift");manual.discard();p.setShiftKeyDown(false);
  // A marked enemy behind a solid wall must not be hit through native block collision.
  target.moveTo(p.getX(),p.getY(),p.getZ()+8);target.setHealth(1000);target.invulnerableTime=0;var wall=p.blockPosition().offset(0,0,4);for(int x=-1;x<=1;x++)for(int y=0;y<=3;y++)w.setBlock(wall.offset(x,y,0),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);
  quiver.set(ItemState.FINIST_QUIVER.get(),new ItemState.QuiverState(12,0,"qa217"));p.setYRot(0);p.startUsingItem(InteractionHand.MAIN_HAND);bow.getItem().releaseUsing(bow,w,p,bow.getUseDuration(p)-30);p.stopUsingItem();var blocked=w.getEntitiesOfClass(FinistArrow.class,p.getBoundingBox().inflate(4)).getFirst();t.assertTrue(blocked.guide()==null,"Hidden target acquired guidance at launch");for(int i=0;i<40&&!blocked.isRemoved();i++)blocked.tick();t.assertTrue(target.getHealth()==1000&&blocked.getZ()<wall.getZ()+1,"Arrow bypassed solid wall");blocked.discard();for(int x=-1;x<=1;x++)for(int y=0;y<=3;y++)w.removeBlock(wall.offset(x,y,0),false);
  quiver.set(ItemState.FINIST_QUIVER.get(),new ItemState.QuiverState(12,0,"qa217"));p.startUsingItem(InteractionHand.MAIN_HAND);bow.getItem().releaseUsing(bow,w,p,bow.getUseDuration(p)-30);p.stopUsingItem();var occluded=w.getEntitiesOfClass(FinistArrow.class,p.getBoundingBox().inflate(4)).getFirst();t.assertTrue(target.getUUID().equals(occluded.guide()),"Visible target failed initial lock");for(int x=-1;x<=1;x++)for(int y=0;y<=3;y++)w.setBlock(wall.offset(x,y,0),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),3);occluded.tick();t.assertTrue(occluded.guide()==null,"Arrow kept homing after target became occluded");occluded.discard();for(int x=-1;x<=1;x++)for(int y=0;y<=3;y++)w.removeBlock(wall.offset(x,y,0),false);
  target.discard();System.out.println("ISSUE223_FINIST_PASS 91 actual releases: native mark +90 hits across draws/angles/distances; manual opt-out and solid-wall collision preserved");t.succeed();
 }
}
