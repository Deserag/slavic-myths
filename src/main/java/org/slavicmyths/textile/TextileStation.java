package org.slavicmyths.textile;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.*;

public final class TextileStation extends BlockEntity {
    private final int kind;private int input,output,progress;
    public TextileStation(BlockPos p,BlockState s){super(Textiles.STATION.get(),p,s);kind=((StationBlock)s.getBlock()).kind;}
    private Item inputItem(){return Textiles.item(kind==0?"flax_stalk":kind==1?"flax_fiber":"linen_thread");}
    private Item outputItem(){return Textiles.item(kind==0?"flax_fiber":kind==1?"linen_thread":"linen_cloth");}
    private int capacity(){return kind==0?4:kind==1?16:32;}
    private int required(){return kind==1?2:4;}
    private int duration(){return kind==1?100:160;}
    public boolean accepts(ItemStack s){return s.is(inputItem());}
    public int insert(ItemStack s,int requested){if(!accepts(s))return 0;int n=Math.min(Math.min(s.getCount(),requested),capacity()-input);input+=n;if(n>0)changed();return n;}
    public void workOrExtract(Player p){
        if(kind==0){
            if(input<4){p.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.slavicmyths.requires_flax"),true);return;}
            progress++;sound(SoundEvents.WOOD_HIT);
            if(level instanceof net.minecraft.server.level.ServerLevel server)server.sendParticles(new net.minecraft.core.particles.ItemParticleOption(net.minecraft.core.particles.ParticleTypes.ITEM,new ItemStack(inputItem())),worldPosition.getX()+.5,worldPosition.getY()+.75,worldPosition.getZ()+.5,3,.15,.05,.15,.01);
            if(progress==3){input=0;progress=0;give(p,new ItemStack(outputItem(),4));}
        }else if(output>0){give(p,new ItemStack(outputItem(),output));output=0;}
        changed();
    }
    private void give(Player p,ItemStack s){if(!p.getInventory().add(s))p.drop(s,false);}
    private void sound(SoundEvent sound){level.playSound(null,worldPosition,sound,SoundSource.BLOCKS,.5F,1F);}
    public void tick(){
        if(input>=required()&&output<16){if(progress==0)sound(kind==1?SoundEvents.WOOD_STEP:SoundEvents.UI_LOOM_TAKE_RESULT);if(++progress>=duration()){input-=required();output++;progress=0;sound(kind==1?SoundEvents.WOOD_STEP:SoundEvents.UI_LOOM_TAKE_RESULT);}setChanged();sync();}
        else if(progress!=0){progress=0;changed();}
    }
    public void dropContents(){if(input>0)Block.popResource(level,worldPosition,new ItemStack(inputItem(),input));if(output>0)Block.popResource(level,worldPosition,new ItemStack(outputItem(),output));input=output=progress=0;}
    private void changed(){setChanged();sync();}
    private void sync(){
        if(level==null||level.isClientSide)return;BlockState s=level.getBlockState(worldPosition);if(!(s.getBlock() instanceof StationBlock))return;BlockState next=s;
        if(s.hasProperty(StationBlock.LOADED))next=next.setValue(StationBlock.LOADED,input>0);
        if(s.hasProperty(StationBlock.WORK_STEP))next=next.setValue(StationBlock.WORK_STEP,Math.min(2,progress));
        if(s.hasProperty(StationBlock.ACTIVE))next=next.setValue(StationBlock.ACTIVE,input>=required()&&output<16);
        if(next!=s)level.setBlock(worldPosition,next,3);
    }
    @Override public void onLoad(){super.onLoad();sync();}
    @Override protected void saveAdditional(CompoundTag n,HolderLookup.Provider r){super.saveAdditional(n,r);n.putInt("Input",input);n.putInt("Output",output);n.putInt("Progress",progress);}
    @Override protected void loadAdditional(CompoundTag n,HolderLookup.Provider r){super.loadAdditional(n,r);input=Math.max(0,Math.min(capacity(),n.getInt("Input")));output=kind==0?0:Math.max(0,Math.min(16,n.getInt("Output")));progress=Math.max(0,Math.min(kind==0?2:duration()-1,n.getInt("Progress")));if(input<required())progress=0;}
}
