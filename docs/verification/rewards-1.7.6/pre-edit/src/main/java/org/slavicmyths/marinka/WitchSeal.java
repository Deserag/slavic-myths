package org.slavicmyths.marinka;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
/** Explicit right-click confirmation; no walk-in auto start. */
public final class WitchSeal extends Block {
    public static final MapCodec<WitchSeal> CODEC=simpleCodec(p->new WitchSeal());
    public WitchSeal(){super(BlockBehaviour.Properties.of().strength(-1,3600000).noOcclusion().lightLevel(s->3).sound(SoundType.DEEPSLATE));}
    @Override protected MapCodec<? extends Block> codec(){return CODEC;}
    @Override protected InteractionResult useWithoutItem(BlockState s,Level w,BlockPos at,Player p,BlockHitResult hit){if(p instanceof net.minecraft.server.level.ServerPlayer server&&server.distanceToSqr(at.getCenter())<=20.25)WitchYard.start(server,at,false);return InteractionResult.sidedSuccess(w.isClientSide);}
}
