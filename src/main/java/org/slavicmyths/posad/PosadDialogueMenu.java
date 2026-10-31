package org.slavicmyths.posad;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Server-authoritative conversation in a vanilla 3-row inventory UI.
 * Clickable entries are display tokens, never transferable inventory items.
 */
public final class PosadDialogueMenu extends ChestMenu {
    private final PosadResidentEntity posadnik;
    private final SimpleContainer tokens;
    private int page;
    public PosadDialogueMenu(int id,Inventory inventory,PosadResidentEntity npc){
        this(id,inventory,npc,new SimpleContainer(27));
    }
    private PosadDialogueMenu(int id,Inventory inventory,PosadResidentEntity npc,SimpleContainer storage){
        super(MenuType.GENERIC_9x3,id,inventory,storage,3);
        posadnik=npc;tokens=storage;refresh(inventory.player);
    }
    private static ItemStack button(Item item,String name){ItemStack stack=new ItemStack(item);stack.set(DataComponents.CUSTOM_NAME,Component.literal(name));return stack;}
    private void entry(int slot,Item item,String name){tokens.setItem(slot,button(item,name));}
    private void refresh(Player player){
        for(int i=0;i<27;i++)tokens.setItem(i,ItemStack.EMPTY);
        if(!(player instanceof ServerPlayer server) || posadnik.city()==null)return;
        PosadQuestLedger ledger=PosadQuestLedger.get(server.serverLevel());
        int rank=ledger.tier(posadnik.city(),server.getUUID());
        entry(4,Items.GOLDEN_HELMET,"Пекельный посадник | доверие "+rank+"/5 | "+ledger.points(posadnik.city(),server.getUUID())+" очков");
        if(page==0){
            entry(11,Items.WRITABLE_BOOK,"Поручения и поставки");
            entry(13,Items.COMPASS,"Розыск боссов");
            entry(15,Items.EMERALD,"Обмен трофеев");
            entry(22,Items.GOLD_INGOT,"Обычная торговля посадника");
        }else if(page==1){
            int[] slots={10,11,12,14,15,16};int n=0;
            for(PosadQuestLedger.Task task:PosadQuestLedger.SUPPLIES){
                boolean unlocked=rank>=task.tier();boolean active=ledger.active(posadnik.city(),server.getUUID(),task.id());
                entry(slots[n++],unlocked?task.item():Items.BARRIER,task.title()+" | "+task.count()+" шт. | "+(active?"Нажмите для сдачи":unlocked?"Нажмите для принятия":"Ранг "+task.tier())+" | +"+task.points());
            }
            entry(22,Items.ARROW,"Вернуться к разговору");
        }else if(page==2){
            entry(11,Items.NETHER_STAR,rank>=3?"Проклятый мытарь — розыск (ожидает подтверждения местоположения)":"Проклятый мытарь — нужен ранг III");
            entry(15,Items.NETHER_STAR,rank>=3?"Баук — розыск (ожидает подтверждения местоположения)":"Баук — нужен ранг III");
            entry(22,Items.ARROW,"Вернуться к разговору");
        }else{
            entry(13,Items.GOLD_NUGGET,"Трофеи принимаются после проверки подлинности и одноразового claimId");
            entry(22,Items.ARROW,"Вернуться к разговору");
        }
        broadcastChanges();
    }
    @Override public void clicked(int slot,int button,ClickType action,Player player){
        if(!(player instanceof ServerPlayer server)||!stillValid(server)||slot<0||slot>=27)return;
        if(action!=ClickType.PICKUP)return;
        if(page==0){
            if(slot==11)page=1;
            else if(slot==13)page=2;
            else if(slot==15)page=3;
            else if(slot==22){server.closeContainer();posadnik.openTradeFor(server);return;}
        }else if(slot==22)page=0;
        else if(page==1){
            int[] indexes={10,11,12,14,15,16};
            for(int n=0;n<indexes.length;n++)if(slot==indexes[n]){
                PosadQuestLedger ledger=PosadQuestLedger.get(server.serverLevel());
                PosadQuestLedger.Task task=PosadQuestLedger.SUPPLIES.get(n);
                boolean success=ledger.active(posadnik.city(),server.getUUID(),task.id())?ledger.deliver(posadnik.city(),server,task):ledger.accept(posadnik.city(),server,task);
                server.displayClientMessage(Component.literal(success?"Поручение обновлено.":"Пока нельзя принять или сдать поручение."),true);
                break;
            }
        }else if(page==2 && (slot==11||slot==15)){
            server.displayClientMessage(Component.literal("Розыск станет доступен после обнаружения проверенных координат босса."),true);
        }else if(page==3 && slot==13){
            server.displayClientMessage(Component.literal("Подлинность трофея должна подтвердить серверная книга побед."),true);
        }
        refresh(server);
    }
    @Override public ItemStack quickMoveStack(Player player,int slot){return ItemStack.EMPTY;}
    @Override public boolean stillValid(Player player){return posadnik.isAlive()&&posadnik.city()!=null&&posadnik.role()==PosadPopulationPlan.Role.POSADNIK&&player.distanceToSqr(posadnik)<64;}
}
