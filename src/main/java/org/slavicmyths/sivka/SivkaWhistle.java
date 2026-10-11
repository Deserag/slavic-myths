package org.slavicmyths.sivka;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;

public final class SivkaWhistle extends Item {
    public SivkaWhistle(){super(new Properties().stacksTo(1));}
    @Override public boolean isFoil(ItemStack stack){return SivkaBonds.token(stack).getInt("Stage")==7;}
    @Override public InteractionResultHolder<ItemStack> use(Level world,Player player,InteractionHand hand){var s=player.getItemInHand(hand);if(world.isClientSide)return InteractionResultHolder.success(s);
        if(!(player instanceof ServerPlayer p)||p.isSpectator())return InteractionResultHolder.fail(s);
        var d=SivkaBonds.get(p.serverLevel());var b=d.bonds.get(p.getUUID());
        if(b==null||!SivkaBonds.matches(s,b,p.getUUID())){SivkaBonds.message(p,"other_owner");return InteractionResultHolder.fail(s);}
        if(!b.valid()){SivkaBonds.message(p,"expired");return InteractionResultHolder.fail(s);}
        long now=world.getGameTime();if(p.getPersistentData().getLong("SivkaCallAttempt")>now)return InteractionResultHolder.fail(s);p.getPersistentData().putLong("SivkaCallAttempt",now+30);
        if(b.play-b.lastCall<SivkaRules.CALL_DELAY||b.play<b.recovery){SivkaBonds.message(p,"cooldown");return InteractionResultHolder.fail(s);}
        var existing=d.loaded(p.serverLevel(),b);
        if(existing!=null&&existing.level()==world&&existing.distanceToSqr(p)<=24*24&&existing.generation==b.generation){
            if(existing.isVehicle()){SivkaBonds.message(p,"nearby");return InteractionResultHolder.success(s);}
            existing.arrive(p);b.lastCall=b.play;d.setDirty();p.playSound(SivkaContent.sound("whistle_player"),.8F,1);return InteractionResultHolder.success(s);
        }
        Vec3 spot=null;for(int i=0;i<40&&spot==null;i++){double angle=p.getYRot()*Math.PI/180+(i%2==0?1:-1)*(.6+i*.37);double radius=5+i%8;int x=(int)Math.floor(p.getX()-Math.sin(angle)*radius),z=(int)Math.floor(p.getZ()+Math.cos(angle)*radius);
            for(int y=2;y>=-4;y--){var q=new BlockPos(x,p.blockPosition().getY()+y,z);if(safe(p.serverLevel(),q)){spot=new Vec3(x+.5,q.getY(),z+.5);break;}}
        }
        if(spot==null){SivkaBonds.message(p,"no_room");p.playNotifySound(SivkaContent.sound("summon_reject"),net.minecraft.sounds.SoundSource.NEUTRAL,.27F,1);return InteractionResultHolder.fail(s);}
        var h=SivkaContent.ENTITY.get().create(world);if(h==null)return InteractionResultHolder.fail(s);
        // Retire only after successful clearance. Unloaded old generations self-remove on load.
        long generation=b.generation+1;h.owner=b.owner;h.bond=b.id;h.generation=generation;h.moveTo(spot.x,spot.y,spot.z,p.getYRot()+180,0);h.setHealth(b.health);
        if(!p.serverLevel().addFreshEntity(h))return InteractionResultHolder.fail(s);
        if(existing!=null){existing.ejectPassengers();existing.discard();}
        b.generation=generation;b.entity=h.getUUID();b.dimension=world.dimension().location().toString();b.last=h.blockPosition();b.lastCall=b.play;d.setDirty();h.arrive(p);h.react(3,26);h.spark(16);h.cue("summon_response",.65F);h.cue("materialize",.58F);p.playSound(SivkaContent.sound("whistle_player"),.8F,1);return InteractionResultHolder.success(s);
    }
    public static boolean safe(net.minecraft.server.level.ServerLevel w,BlockPos p){
        for(int x=-1;x<=1;x++)for(int z=-2;z<=1;z++){var q=p.offset(x,0,z);if(!w.hasChunkAt(q)||!w.getWorldBorder().isWithinBounds(q)||!safeFloor(w,q.below()))return false;}
        AABB box=new AABB(p.getX()-1,p.getY(),p.getZ()-2,p.getX()+2,p.getY()+4.5,p.getZ()+2);
        for(var q:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX-.001,box.maxY-.001,box.maxZ-.001)))if(!w.hasChunkAt(q)||!w.getFluidState(q).isEmpty()||!w.getBlockState(q).getCollisionShape(w,q).isEmpty())return false;
        return w.getEntities((net.minecraft.world.entity.Entity)null,box,e->e.isPickable()).isEmpty();
    }
    public static boolean safeFloor(Level w,BlockPos p){var s=w.getBlockState(p);return s.isFaceSturdy(w,p,net.minecraft.core.Direction.UP)&&w.getFluidState(p).isEmpty()&&!s.is(Blocks.MAGMA_BLOCK)&&!s.is(Blocks.CACTUS)&&!s.is(Blocks.CAMPFIRE)&&!s.is(Blocks.SOUL_CAMPFIRE)&&!s.is(Blocks.POWDER_SNOW);}
    @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> out,TooltipFlag f){var n=SivkaBonds.token(s);int stage=n.getInt("Stage");out.add(Component.translatable("sivka.slavicmyths."+(stage==7?"permanent":"temporary")));out.add(Component.translatable("sivka.slavicmyths.progress",stage));if(stage<7){long seconds=n.getLong("Remaining")/20;out.add(Component.translatable("sivka.slavicmyths.timer",String.format(java.util.Locale.ROOT,"%02d:%02d",seconds/60,seconds%60)));out.add(Component.translatable("sivka.slavicmyths.next",Component.translatable("item."+SivkaRules.FOODS[Math.clamp(stage,0,6)].replace(':','.'))));}out.add(Component.translatable("sivka.slavicmyths.controls"));}
}
