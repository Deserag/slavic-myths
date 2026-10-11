package org.slavicmyths.industry;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.*;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

public final class MachineTile extends BlockEntity implements WorldlyContainer,MenuProvider {
    public final boolean refining;
    private NonNullList<ItemStack> items;
    public int burn,burnDuration,progress,total;
    private String activeRecipe="";
    private float experience;
    public final ContainerData data=new ContainerData(){public int get(int i){return switch(i){case 0->burn;case 1->burnDuration;case 2->progress;default->total;};}public void set(int i,int n){switch(i){case 0->burn=n;case 1->burnDuration=n;case 2->progress=n;case 3->total=n;}}public int getCount(){return 4;}};
    public MachineTile(BlockPos p,BlockState s,boolean refining){super(refining?Industry.HEARTH_TILE.get():Industry.BLOOMERY_TILE.get(),p,s);this.refining=refining;items=NonNullList.withSize(refining?4:3,ItemStack.EMPTY);total=refining?300:200;}
    public int fuel(){return refining?2:1;}public int output(){return refining?3:2;}
    public RecipeType<MachineRecipe> recipes(){return refining?MachineRecipe.HEARTH.get():MachineRecipe.BLOOMERY.get();}
    public static void tick(Level world,BlockPos pos,BlockState state,MachineTile t){
        if(world.isClientSide)return;boolean dirty=false;boolean was=t.burn>0;if(t.burn>0){t.burn--;dirty=true;}
        var input=new MachineRecipe.Input(t.items.get(0),t.refining?t.items.get(1):ItemStack.EMPTY);
        var holder=world.getRecipeManager().getRecipeFor(t.recipes(),input,world).orElse(null);
        String key=holder==null?"":holder.id().toString();if(!key.equals(t.activeRecipe)){t.progress=0;t.activeRecipe=key;dirty=true;}
        MachineRecipe recipe=holder==null?null:holder.value();ItemStack result=recipe==null?ItemStack.EMPTY:recipe.assemble(input,world.registryAccess());ItemStack current=t.items.get(t.output());
        boolean can=recipe!=null&&!result.isEmpty()&&IndustryRules.canFinish(t.refining,recipe.matches(input,world),input.metal().getCount(),input.flux().getCount(),current.getCount(),result.getMaxStackSize(),current.isEmpty()||ItemStack.isSameItemSameComponents(current,result));
        if(can){t.total=recipe.duration();if(t.burn==0){var fuel=t.items.get(t.fuel());int time=fuel.getBurnTime(t.recipes());if(time>0){t.burn=t.burnDuration=time;ItemStack remainder=fuel.getCraftingRemainingItem();fuel.shrink(1);if(fuel.isEmpty())t.items.set(t.fuel(),remainder);dirty=true;}}
            if(t.burn>0){t.progress++;dirty=true;if(t.progress>=t.total){
                // Same server invocation validates both ingredients and output before consuming exactly once.
                if(recipe.matches(input,world)){t.items.get(0).shrink(1);if(t.refining)t.items.get(1).shrink(1);if(current.isEmpty())t.items.set(t.output(),result.copy());else current.grow(result.getCount());t.experience+=recipe.experience();}t.progress=0;
            }}else if(t.progress>0){t.progress=Math.max(0,t.progress-2);dirty=true;}
        }else if(t.progress!=0){t.progress=0;dirty=true;}
        if(was!=(t.burn>0))world.setBlock(pos,state.setValue(MachineBlock.LIT,t.burn>0),3);
        if(dirty)t.setChanged();
    }
    public void claimExperience(Player player){if(player instanceof ServerPlayer p&&experience>0){int xp=(int)experience;if(p.getRandom().nextFloat()<experience-xp)xp++;ExperienceOrb.award(p.serverLevel(),p.position(),xp);experience=0;setChanged();}}
    protected void saveAdditional(CompoundTag n,HolderLookup.Provider p){super.saveAdditional(n,p);ContainerHelper.saveAllItems(n,items,p);n.putInt("Burn",burn);n.putInt("BurnDuration",burnDuration);n.putInt("Progress",progress);n.putInt("Total",total);n.putString("Recipe",activeRecipe);n.putFloat("Experience",experience);}
    protected void loadAdditional(CompoundTag n,HolderLookup.Provider p){super.loadAdditional(n,p);items=NonNullList.withSize(getContainerSize(),ItemStack.EMPTY);ContainerHelper.loadAllItems(n,items,p);burn=Math.max(0,n.getInt("Burn"));burnDuration=Math.max(0,n.getInt("BurnDuration"));total=Math.max(1,n.getInt("Total"));progress=Math.max(0,Math.min(total-1,n.getInt("Progress")));activeRecipe=n.getString("Recipe");experience=Math.max(0,n.getFloat("Experience"));}
    public Component getDisplayName(){return Component.translatable("block.slavicmyths."+(refining?"refining_hearth":"chud_bloomery"));}
    public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new MachineMenu(id,inv,this,data,worldPosition,refining);}
    public int getContainerSize(){return refining?4:3;}
    public boolean isEmpty(){return items.stream().allMatch(ItemStack::isEmpty);}
    public ItemStack getItem(int i){return items.get(i);}
    public ItemStack removeItem(int i,int n){var stack=ContainerHelper.removeItem(items,i,n);if(!stack.isEmpty())setChanged();return stack;}
    public ItemStack removeItemNoUpdate(int i){return ContainerHelper.takeItem(items,i);}
    public void setItem(int i,ItemStack s){items.set(i,s);s.setCount(Math.min(s.getCount(),s.getMaxStackSize()));setChanged();}
    public boolean stillValid(Player p){return level!=null&&level.getBlockEntity(worldPosition)==this&&p.isAlive()&&p.distanceToSqr(worldPosition.getCenter())<=64;}
    public void clearContent(){items.clear();}
    public boolean canPlaceItem(int i,ItemStack s){if(i==output())return false;if(i==fuel())return s.getBurnTime(recipes())>0;if(refining&&i==1)return s.is(Industry.FLUX.get());return refining?s.is(Items.IRON_INGOT)||s.is(Items.COPPER_INGOT):s.is(Industry.LUMP.get());}
    public int[] getSlotsForFace(Direction d){return d==Direction.DOWN?new int[]{output(),fuel()}:d==Direction.UP?new int[]{0}:refining?new int[]{1,2}:new int[]{1};}
    public boolean canPlaceItemThroughFace(int i,ItemStack s,Direction d){return d!=Direction.DOWN&&canPlaceItem(i,s);}
    public boolean canTakeItemThroughFace(int i,ItemStack s,Direction d){return d==Direction.DOWN&&(i==output()||i==fuel()&&s.is(Items.BUCKET));}
    @EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD)
    public static final class CapabilitiesRegistration {
        @SubscribeEvent public static void register(RegisterCapabilitiesEvent e){e.registerBlockEntity(Capabilities.ItemHandler.BLOCK,Industry.BLOOMERY_TILE.get(),(t,side)->new SidedInvWrapper(t,side==null?Direction.UP:side));e.registerBlockEntity(Capabilities.ItemHandler.BLOCK,Industry.HEARTH_TILE.get(),(t,side)->new SidedInvWrapper(t,side==null?Direction.UP:side));}
    }
}
