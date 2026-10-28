package org.slavicmyths.trophy;

import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;

/** Six new trophies; the existing Poloz trophy remains independent and unchanged. */
public final class TrophyHeads {
 public enum Kind {
  TUGARIN("tugarin_head",1),SKIM("skim_head",1),VOLKOLAK("cursed_volkolak_head",1),
  LIKHO("likho_head",2),MOR_LIKHO("mor_likho_head",2),PODZEMNIK("large_podzemnik_head",2),MAGMA_LORD("magma_lord_head",1),GROVE_GUARDIAN("grove_guardian_head",1),ZHARENITSA("zharenitsa_head",1),POSTEN("posten_head",1);
  public final String id;public final int height;Kind(String id,int height){this.id=id;this.height=height;}
  /** Native-renderer envelope, verified by Heads140Offline; keeps server collision client-free. */
  public float[] bounds(){return switch(this){
   case TUGARIN->new float[]{.225059F,.04F,.03F,.774941F,.447273F,.97F};
   case SKIM->new float[]{.105511F,.04F,.03F,.894489F,.764252F,.97F};
   case VOLKOLAK->new float[]{.174895F,.04F,.196157F,.825105F,.94F,.803843F};
   case LIKHO->new float[]{.313548F,.04F,.33809F,.686452F,1.94F,.66191F};
   case MOR_LIKHO->new float[]{.313548F,.04F,.332444F,.686452F,1.94F,.667556F};
   case GROVE_GUARDIAN,ZHARENITSA,POSTEN->new float[]{.01F,.02F,.01F,.99F,.98F,.99F};
   case MAGMA_LORD->new float[]{.03F,.04F,.03F,.97F,.94F,.97F};
   case PODZEMNIK->new float[]{.03F,.04F,.0535F,.97F,1.058333F,.9465F};
  };}
 }
 public static final Map<Kind,DeferredHolder<Block,Block>> BLOCKS=new EnumMap<>(Kind.class);
 public static final Map<Kind,DeferredHolder<Item,Item>> ITEMS=new EnumMap<>(Kind.class);
 static {for(Kind k:Kind.values()){
  DeferredHolder<Block,Block> b=k==Kind.ZHARENITSA?org.slavicmyths.blackcinder.CinderBlocks.ALL.get(k.id):ModBlocks.BLOCKS.register(k.id,()->new TrophyHeadBlock(k));BLOCKS.put(k,b);
  if(k==Kind.ZHARENITSA){ITEMS.put(k,org.slavicmyths.blackcinder.CinderBlocks.ITEMS.get(k.id));continue;}
  ITEMS.put(k,ModItems.ITEMS.register(k.id,()->new BlockItem(b.get(),k==Kind.MAGMA_LORD?new Item.Properties().rarity(Rarity.EPIC).fireResistant():new Item.Properties().rarity(Rarity.EPIC))));
 }}
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<TrophyHeadBlock.Tile>> TILE=ModTiles.TILES.register("trophy_head",()->BlockEntityType.Builder.of(TrophyHeadBlock.Tile::new,BLOCKS.values().stream().map(DeferredHolder::get).toArray(Block[]::new)).build(null));
 public static void init(){}
 private TrophyHeads(){}
}
