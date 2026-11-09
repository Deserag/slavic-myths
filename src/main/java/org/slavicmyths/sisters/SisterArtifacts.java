package org.slavicmyths.sisters;

import com.mojang.serialization.Codec;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import org.slavicmyths.item.ItemState;
import org.slavicmyths.registry.ModItems;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Five equipment bases, each carrying its own immutable set of up to three sister powers. */
public final class SisterArtifacts {
    public static final List<String> IDS=List.of("tryaseya","gneteya","ledeya","agneya","neveya","lomeya");
    public static final List<String> TYPES=List.of("sisters_ring","sisters_necklace","sisters_belt","sisters_crown","sisters_brooch");
    public static final List<String> SLOTS=List.of("ring","necklace","belt","head","charm");
    public static final Codec<List<String>> CODEC=Codec.STRING.sizeLimitedListOf(3);
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<List<String>>> POWERS=
        ItemState.COMPONENTS.register("sisters_artifact_powers",()->DataComponentType.<List<String>>builder().persistent(CODEC)
            .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(CODEC)).build());
    public static final Map<String,DeferredHolder<Item,Item>> ITEMS=new LinkedHashMap<>();
    static {
        for(int i=0;i<TYPES.size();i++){
            final String type=TYPES.get(i),slot=SLOTS.get(i);
            ITEMS.put(type,ModItems.ITEMS.register(type,()->new Artifact(type,slot)));
        }
    }
    public static void init(IEventBus bus){SisterArtifactRecipe.SERIALIZERS.register(bus);}
    public static boolean isArtifact(ItemStack stack){return stack.getItem() instanceof Artifact;}
    public static List<String> powers(ItemStack stack){
        if(!isArtifact(stack))return List.of();
        List<String> raw=stack.getOrDefault(POWERS.get(),List.of()),safe=new ArrayList<>();
        for(String id:raw)if(IDS.contains(id)&&!safe.contains(id)&&safe.size()<3)safe.add(id);
        return List.copyOf(safe);
    }
    public static ItemStack addPower(ItemStack original,String power){
        List<String> old=powers(original);
        if(!IDS.contains(power)||!isArtifact(original)||old.size()>=3||old.contains(power))return ItemStack.EMPTY;
        ItemStack out=original.copyWithCount(1);
        List<String> next=new ArrayList<>(old);next.add(power);
        out.set(POWERS.get(),List.copyOf(next));
        return out;
    }
    /** A stable first-two selection protects against invalid externally injected Curios stacks. */
    public static List<ItemStack> equipped(LivingEntity entity){
        return CuriosApi.getCuriosInventory(entity).map(inv->inv.findCurios(SisterArtifacts::isArtifact).stream()
            .filter(r->r.stack().getItem() instanceof Artifact artifact && artifact.slot.equals(r.slotContext().identifier()))
            .sorted(Comparator.comparing(r->r.slotContext().identifier()+":"+r.slotContext().index()))
            .limit(2).map(r->r.stack()).toList()).orElse(List.of());
    }
    public static Set<String> activePowers(LivingEntity entity){
        Set<String> active=new HashSet<>();
        for(ItemStack stack:equipped(entity))active.addAll(powers(stack));
        return active;
    }
    public static boolean hasBase(LivingEntity entity,String id){
        for(ItemStack stack:equipped(entity))if(stack.is(ITEMS.get(id).get()))return true;
        return false;
    }
    public static boolean complete(LivingEntity entity){return equipped(entity).size()==2&&activePowers(entity).size()==6;}
    public static final class Artifact extends Item implements ICurioItem {
        private final String id,slot;
        private Artifact(String id,String slot){super(new Properties().rarity(Rarity.EPIC).stacksTo(1));this.id=id;this.slot=slot;}
        @Override public boolean canEquip(SlotContext context,ItemStack stack){
            if(!slot.equals(context.identifier()))return false;
            return CuriosApi.getCuriosInventory(context.entity()).map(inv->inv.findCurios(SisterArtifacts::isArtifact).size()<2).orElse(false);
        }
        @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){
            lines.add(Component.translatable(getDescriptionId()+".effect").withStyle(ChatFormatting.GRAY));
            List<String> powers=SisterArtifacts.powers(stack);
            lines.add(Component.translatable("tooltip.slavicmyths.sisters_sockets",powers.size()).withStyle(ChatFormatting.GOLD));
            for(String power:powers)lines.add(Component.translatable("tooltip.slavicmyths.sister_power."+power).withStyle(ChatFormatting.AQUA));
            if(powers.isEmpty())lines.add(Component.translatable("tooltip.slavicmyths.sisters_unawakened").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
    private SisterArtifacts(){}
}
