package org.slavicmyths.guardians;
import java.util.UUID;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
public final class GuardianNode extends BlockEntity {
 public UUID instance;public int role=-1;
 public GuardianNode(BlockPos p,BlockState s){super(GuardianContent.NODE.get(),p,s);}
 public void configure(UUID id,int role){instance=id;this.role=role;setChanged();}
 public void use(Player player){if(!(player instanceof ServerPlayer p)||instance==null||!p.isAlive()||p.isSpectator()||!p.serverLevel().mayInteract(p,worldPosition)||p.server.isUnderSpawnProtection(p.serverLevel(),worldPosition,p))return;if(role<0){if(!GuardianLedger.get(p.serverLevel()).stage(instance).equals("CLEARED"))p.displayClientMessage(GuardianContent.text("pedestal_hint"),true);else GuardianLedger.get(p.serverLevel()).claim(p,instance);}else GuardianWorld.valve(p,instance,role);}
 @Override protected void saveAdditional(CompoundTag n,HolderLookup.Provider r){super.saveAdditional(n,r);if(instance!=null)n.putUUID("Instance",instance);n.putInt("Role",role);}
 @Override protected void loadAdditional(CompoundTag n,HolderLookup.Provider r){super.loadAdditional(n,r);instance=n.hasUUID("Instance")?n.getUUID("Instance"):null;role=n.getInt("Role");}
 @Override public CompoundTag getUpdateTag(HolderLookup.Provider r){return saveWithoutMetadata(r);}
 @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket(){return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);}
}
