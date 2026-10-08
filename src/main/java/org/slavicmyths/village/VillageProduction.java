package org.slavicmyths.village;

import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slavicmyths.kitchen.*;
import org.slavicmyths.brewing.*;
import org.slavicmyths.textile.TextileStation;
import org.slavicmyths.storage.*;

/** Operates the real workstation inventories/processes. No private NPC recipe type or fast-forward. */
public final class VillageProduction {
    public record Prepared(List<ItemStack> remaining,List<ItemStack> inputs){}
    public static Prepared prepare(Container inv,List<KitchenRecipe.Part> parts){
        List<ItemStack> left=InventoryTransactions.snapshot(inv),loaded=new ArrayList<>(),eligible=new ArrayList<>();
        for(ItemStack stack:left)eligible.add(parts.stream().anyMatch(p->p.ingredient().test(stack))?stack:ItemStack.EMPTY);
        int[] used=KitchenRecipe.allocation(parts,new KitchenInput(eligible));if(used==null)return null;
        for(int i=0;i<used.length;i++)if(used[i]>0){ItemStack take=left.get(i).copyWithCount(used[i]);left.get(i).shrink(used[i]);boolean merged=false;for(ItemStack old:loaded)if(ItemStack.isSameItemSameComponents(old,take)&&old.getCount()+take.getCount()<=old.getMaxStackSize()){old.grow(take.getCount());merged=true;break;}if(!merged)loaded.add(take);}
        return loaded.size()<=4?new Prepared(left,loaded):null;
    }
    private static boolean outputFits(List<ItemStack> after,ItemStack result,List<ItemStack> returns){var copy=after.stream().map(ItemStack::copy).collect(java.util.stream.Collectors.toCollection(ArrayList::new));if(!InventoryTransactions.insert(copy,result,64))return false;for(ItemStack s:returns)if(!InventoryTransactions.insert(copy,s,64))return false;return true;}
    private static boolean takeOutputs(Container from,int first,int end,Container inv){boolean moved=false;for(int i=first;i<end;i++){int n=from.getItem(i).getCount();if(n>0&&InventoryTransactions.transfer(from,i,inv,n))moved=true;}return moved;}
    public static String process(ServerLevel level,Villager v,BlockEntity tile){
        var inv=v.getInventory();String role=WorkPolicy.role(v);
        if(tile instanceof KitchenTile kitchen&&role.equals("cook")){
            if(takeOutputs(kitchen.inventory,6,9,inv))return "COLLECTED";
            if(kitchen.servings>0){for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).is(Items.BOWL)){
                var after=InventoryTransactions.snapshot(inv);after.get(i).shrink(1);if(!InventoryTransactions.insert(after,kitchen.dish.copyWithCount(1),64))return "PERSONAL_INVENTORY_FULL";
                InventoryTransactions.commit(inv,after);if(--kitchen.servings==0){kitchen.dish=ItemStack.EMPTY;kitchen.maxServings=0;}kitchen.changed();return "COLLECTED";
            }return "NO_INPUT";}
            if(kitchen.busy())return "PROCESSING";
            for(var holder:level.getRecipeManager().getAllRecipesFor(KitchenRecipe.TYPE.get())){
                var r=holder.value();Prepared plan=prepare(inv,r.parts());if(plan==null)continue;
                int toolSlot=r.pot()?1:0;List<ItemStack> remaining=plan.remaining();ItemStack tool=kitchen.inventory.getItem(toolSlot);
                if(tool.isEmpty()){for(ItemStack s:remaining)if(r.tool().test(s)){tool=s.copyWithCount(1);s.shrink(1);break;}}
                if(!r.tool().test(tool)||!outputFits(remaining,r.result(),r.remainders()))continue;
                boolean empty=true;for(int i=2;i<6;i++)if(!kitchen.inventory.getItem(i).isEmpty())empty=false;if(!empty)continue;
                // Real engine rechecks allocation, tool, output and returns before consuming inputs.
                kitchen.inventory.setItem(toolSlot,tool);for(int i=0;i<plan.inputs().size();i++)kitchen.inventory.setItem(i+2,plan.inputs().get(i));InventoryTransactions.commit(inv,remaining);return "LOADED";
            }return "NO_INPUT";
        }
        if(tile instanceof BrewTile vat&&role.equals("brewer")){
            if(takeOutputs(vat,4,8,inv))return "COLLECTED";if(vat.total>0)return "PROCESSING";
            for(var holder:level.getRecipeManager().getAllRecipesFor(VatRecipe.TYPE.get())){
                var r=holder.value();Prepared plan=prepare(inv,r.parts());if(plan==null||!outputFits(plan.remaining(),r.result(),r.remainders()))continue;
                // Existing catalysts must be present; never conjure/borrow a magical artifact.
                if(!r.catalyst().isEmpty()&&!r.catalyst().test(vat.getItem(8))||r.catalyst().isEmpty()&&!vat.getItem(8).isEmpty())continue;
                boolean empty=true;for(int i=0;i<4;i++)if(!vat.getItem(i).isEmpty())empty=false;if(!empty)continue;
                for(int i=0;i<plan.inputs().size();i++)vat.setItem(i,plan.inputs().get(i));InventoryTransactions.commit(inv,plan.remaining());return "LOADED";
            }return "NO_INPUT";
        }
        if(tile instanceof TextileStation textile&&role.equals("weaver")){
            ItemStack out=textile.npcOutput();if(!out.isEmpty()){if(!InventoryTransactions.canInsert(inv,out))return "PERSONAL_INVENTORY_FULL";inv.addItem(textile.npcTakeOutput());return "COLLECTED";}
            if(!InventoryTransactions.canInsert(inv,textile.npcResult()))return "PERSONAL_INVENTORY_FULL";
            if(textile.hasFlaxToBreak()){ItemStack made=textile.breakFlax();if(!made.isEmpty())inv.addItem(made);return made.isEmpty()?"PROCESSING":"PRODUCED";}
            for(int i=0;i<inv.getContainerSize();i++){ItemStack s=inv.getItem(i);if(textile.accepts(s)){int n=textile.insert(s,4);if(n>0){inv.removeItem(i,n);return "LOADED";}}}return "NO_INPUT";
        }
        if(tile instanceof org.slavicmyths.husbandry.YardStorage feeder&&role.equals("herder")){
            for(int i=0;i<inv.getContainerSize();i++){ItemStack food=inv.getItem(i);if(!feeder.accepts(food))continue;int n=feeder.insert(food,4);if(n>0){inv.removeItem(i,n);return "LOADED";}}return "NO_INPUT";
        }
        if(tile instanceof StorageTile rack&&rack.kind==Household.Kind.DRY&&role.equals("hunter")){
            for(int i=0;i<rack.items.size();i++)if(rack.time[i]==0&&!rack.getItem(i).isEmpty()&&InventoryTransactions.transfer(rack,i,inv,1))return "COLLECTED";
            for(int i=0;i<rack.items.size();i++)if(rack.getItem(i).isEmpty())for(int j=0;j<inv.getContainerSize();j++){
                ItemStack s=inv.getItem(j);var match=level.getRecipeManager().getRecipeFor(DryingRecipe.TYPE.get(),new SingleRecipeInput(s),level);if(match.isEmpty())continue;
                var r=match.get();if(!InventoryTransactions.canInsert(inv,r.value().result()))return "PERSONAL_INVENTORY_FULL";
                rack.items.set(i,s.copyWithCount(1));inv.removeItem(j,1);rack.results[i]=r.value().result().copy();rack.time[i]=r.value().dryingTime();rack.progress[i]=0;rack.recipes[i]=r.id().toString();rack.setChanged();return "LOADED";
            }return "NO_INPUT";
        }
        if(role.equals("miller")){
            for(String id:List.of("wheat_flour","rye_flour","barley_groats","oat_groats")){
                var optional=level.getRecipeManager().byKey(VillageRoles.id(id));if(optional.isEmpty()||!(optional.get().value() instanceof CraftingRecipe r))continue;
                List<KitchenRecipe.Part> parts=r.getIngredients().stream().filter(i->!i.isEmpty()).map(i->new KitchenRecipe.Part(i,1)).toList();Prepared p=prepare(inv,parts);if(p==null)continue;
                List<ItemStack> grid=new ArrayList<>(Collections.nCopies(9,ItemStack.EMPTY));int slot=0;for(ItemStack s:p.inputs())for(int n=0;n<s.getCount();n++)grid.set(slot++,s.copyWithCount(1));var input=CraftingInput.of(3,3,grid);
                if(!r.matches(input,level))continue;ItemStack out=r.assemble(input,level.registryAccess());var remainders=r.getRemainingItems(input);
                if(!outputFits(p.remaining(),out,remainders))return "PERSONAL_INVENTORY_FULL";InventoryTransactions.insert(p.remaining(),out,64);for(ItemStack s:remainders)InventoryTransactions.insert(p.remaining(),s,64);InventoryTransactions.commit(inv,p.remaining());return "PRODUCED";
            }return "NO_INPUT";
        }
        return "VISUAL_WORK";
    }
    private VillageProduction(){}
}
