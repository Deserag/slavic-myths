"""Compile exact production method bodies against explicit transaction test doubles.

No Minecraft classes, registries, worlds or bootstrap are loaded. This tests branch/
transaction semantics, NOT Minecraft integration. --baseline demonstrates pre-fix failures.
"""
from pathlib import Path
import argparse, subprocess, textwrap, json
R=Path(__file__).resolve().parents[1]
J=R/'src/main/java/org/slavicmyths'
p=argparse.ArgumentParser();p.add_argument('--baseline',action='store_true');args=p.parse_args()
OUT=R/'build/items286-offline';OUT.mkdir(parents=True,exist_ok=True)
def body(file,signature):
    path='src/main/java/org/slavicmyths/'+file
    s=subprocess.check_output(['git','show','79ff212c:'+path],cwd=R,encoding='utf8') if args.baseline else (J/file).read_text(encoding='utf8')
    start=s.index('{',s.index(signature));depth=1;i=start+1
    while depth:
        depth+=(s[i]=='{')-(s[i]=='}');i+=1
    return s[start:i]
yaga=body('yaga/YagaUtilityItem.java','public ItemStack finishUsingItem')
water=body('farming/WateringCanItem.java','public InteractionResult useOn')
stubs={
'net/minecraft/core/Holder.java':'package net.minecraft.core; public class Holder<T>{}',
'org/slavicmyths/progression/Knowledge.java':'package org.slavicmyths.progression; public class Knowledge {public static int awards; public static void award(Object p,String id){awards++;}}',
'Fixture.java':r'''
import java.util.*;
class Fixture {
 static int checks,failures;static void check(boolean ok,String name){checks++;if(!ok){failures++;System.out.println("FAIL "+name);}}
 enum Kind {THREAD,CLEANSE,SALVE,SIGHT,RESIST,VIGOR,RESTORE,HERBS}
 enum InteractionResult {PASS,FAIL,SUCCESS;static InteractionResult sidedSuccess(boolean client){return SUCCESS;}}
 enum Direction {UP,DOWN}
 static class ItemStack {int count;boolean bottle;int charges=8;ItemStack(int n){count=n;}ItemStack(Object i){count=1;bottle=true;}void shrink(int n){count-=n;}boolean isEmpty(){return count<=0;}int getOrDefault(Object key,int fallback){return charges;}void set(Object key,int value){charges=value;}}
 static class Items {static Object GLASS_BOTTLE=new Object();}
 static class LivingEntity {int effects;void removeEffect(Object e){effects++;}void addEffect(Object e){effects++;}}
 static class Player extends LivingEntity {Abilities a=new Abilities();Inventory inv=new Inventory();boolean shift,allowed=true;int drops;Abilities getAbilities(){return a;}Inventory getInventory(){return inv;}void drop(ItemStack s,boolean f){drops+=s.count;}boolean mayUseItemAt(Object p,Object f,Object s){return allowed;}boolean isShiftKeyDown(){return shift;}BlockPos blockPosition(){return new BlockPos(0,0,0);}}
 static class Abilities {boolean instabuild;}
 static class Inventory {boolean room=true;int bottles;boolean add(ItemStack s){if(room)bottles+=s.count;return room;}}
 static class MobEffect {} static class MobEffectInstance {MobEffectInstance(Object e,int ticks){}}
 static class MobEffects {static net.minecraft.core.Holder<MobEffect> POISON=new net.minecraft.core.Holder<>(),WITHER=POISON,BLINDNESS=POISON,MOVEMENT_SLOWDOWN=POISON,WEAKNESS=POISON,HUNGER=POISON,CONFUSION=POISON,NIGHT_VISION=POISON,DAMAGE_RESISTANCE=POISON,DIG_SPEED=POISON,REGENERATION=POISON;}
 static class FlightRecovery {static void apply(Player p){p.effects++;}}
 static class BlockPos {int x,y,z;BlockPos(int x,int y,int z){this.x=x;this.y=y;this.z=z;}BlockPos relative(Direction d){return offset(0,d==Direction.UP?1:-1,0);}BlockPos below(){return offset(0,-1,0);}BlockPos offset(int a,int b,int c){return new BlockPos(x+a,y+b,z+c);}int getX(){return x;}int getY(){return y;}int getZ(){return z;}public boolean equals(Object o){return o instanceof BlockPos p&&x==p.x&&y==p.y&&z==p.z;}public int hashCode(){return Objects.hash(x,y,z);}}
 static class Block {State defaultBlockState(){return new State(this,0);}}static class CropBlock extends Block {}
 static class Blocks {static Block FARMLAND=new Block();}
 static class State {Block block;int moisture;State(Block b,int m){block=b;moisture=m;}boolean is(Object b){return block==b;}Block getBlock(){return block;}int getValue(Object key){return moisture;}State setValue(Object key,int m){return new State(block,m);}boolean canBeReplaced(){return true;}boolean canSurvive(Object w,Object p){return true;}}
 static class Ref {Block b=new Block();Block get(){return b;}}static class ModBlocks {static Ref RASPBERRY_BUSH=new Ref(),BLUEBERRY_BUSH=new Ref();}
 static class Level {boolean isClientSide,allow=true,write=true;int writes,sounds;Map<BlockPos,State> states=new HashMap<>();State getBlockState(BlockPos p){return states.getOrDefault(p,new State(new Block(),0));}boolean mayInteract(Player p,BlockPos pos){return allow;}boolean hasChunkAt(BlockPos p){return true;}boolean setBlock(BlockPos p,State s,int flags){writes++;if(write)states.put(p,s);return write;}void playSound(Object p,Object pos,Object e,Object source,float v,float f){sounds++;}}
 static class ServerLevel extends Level {int particles;void sendParticles(Object t,double x,double y,double z,int n,double a,double b,double c,double d){particles++;}}
 static class UseOnContext {Level w;Player p;ItemStack s;BlockPos pos=new BlockPos(0,0,0);UseOnContext(Level w,Player p,ItemStack s){this.w=w;this.p=p;this.s=s;}Level getLevel(){return w;}Player getPlayer(){return p;}BlockPos getClickedPos(){return pos;}Direction getClickedFace(){return Direction.UP;}ItemStack getItemInHand(){return s;}}
 static class ComponentRef {Object get(){return this;}}static class ItemState {static ComponentRef WATER_CHARGES=new ComponentRef();}static class FarmBlock {static Object MOISTURE=new Object();}static class ParticleTypes {static Object SPLASH=new Object();}static class SoundEvents {static Object BUCKET_EMPTY=new Object();}static class SoundSource {static Object BLOCKS=new Object();}
 static class Yaga {Kind kind;Yaga(Kind k){kind=k;}ItemStack finishUsingItem(ItemStack s,Level w,LivingEntity e) YAGA_BODY }
 static class Water {InteractionResult useOn(UseOnContext context) WATER_BODY }
 public static void main(String[] args){
  for(Kind kind:List.of(Kind.CLEANSE,Kind.SALVE,Kind.SIGHT,Kind.RESIST,Kind.VIGOR,Kind.RESTORE))for(int count:new int[]{1,3})for(boolean creative:new boolean[]{false,true}){
   Level client=new Level();client.isClientSide=true;Player p=new Player();p.a.instabuild=creative;ItemStack s=new ItemStack(count);new Yaga(kind).finishUsingItem(s,client,p);
   check(s.count==count&&p.inv.bottles==0&&p.drops==0&&p.effects==0,"client has no inventory/effect mutation "+kind+"/"+count+"/"+creative);
   for(boolean room:new boolean[]{false,true}){Level server=new Level();p=new Player();p.inv.room=room;p.a.instabuild=creative;s=new ItemStack(count);ItemStack result=new Yaga(kind).finishUsingItem(s,server,p);
    check(s.count==count-(creative?0:1),"server consumes once "+kind);check(p.effects>0,"existing effect "+kind);
    int bottles=(result.bottle?result.count:0)+p.inv.bottles+p.drops;check(bottles==(!creative&&kind!=Kind.SALVE?1:0),"one server remainder including full inventory "+kind);
   }
  }
  for(int moisture:new int[]{0,6,7})for(boolean allow:new boolean[]{false,true})for(boolean write:new boolean[]{false,true}){
   ServerLevel w=new ServerLevel();w.allow=allow;w.write=write;Player p=new Player();ItemStack s=new ItemStack(1);w.states.put(p.blockPosition(),new State(Blocks.FARMLAND,moisture));InteractionResult result=new Water().useOn(new UseOnContext(w,p,s));boolean success=moisture<7&&allow&&write;
   check(s.charges==(success?7:8),"watering charge iff mutation accepted");check(w.particles==(success?1:0)&&w.sounds==(success?1:0),"watering cues iff mutation");check((result==InteractionResult.SUCCESS)==success,"watering result");
  }
  System.out.println("checks="+checks+" failures="+failures+"; exact production bodies; test doubles; Minecraft launches=0");if(failures>0)System.exit(1);
 }
}
'''.replace('YAGA_BODY',yaga).replace('WATER_BODY',water)
}
for rel,source in stubs.items():
    file=OUT/rel;file.parent.mkdir(parents=True,exist_ok=True);file.write_text(textwrap.dedent(source),encoding='utf8')
java=Path('C:/Program Files/Java/jdk-21.0.12/bin')
subprocess.run([str(java/'javac.exe'),'-d',str(OUT),*[str(OUT/p) for p in stubs]],check=True)
r=subprocess.run([str(java/'java.exe'),'-cp',str(OUT),'Fixture'],capture_output=True,text=True)
print(r.stdout)
(OUT/('baseline.txt' if args.baseline else 'after.txt')).write_text(r.stdout,encoding='utf8')
raise SystemExit(r.returncode)
