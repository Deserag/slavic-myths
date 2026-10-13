package org.slavicmyths.village.mixin;
import java.util.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
/** Limit applies to one native jigsaw assembly, not to neighbouring villages. */
@Mixin(targets="net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement$Placer",remap=false)
public abstract class BarracksLimitMixin {
 @Shadow @Final private List<? super PoolElementStructurePiece> pieces;
 @Redirect(method="tryPlacingChildren",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/levelgen/structure/pools/StructureTemplatePool;getShuffledTemplates(Lnet/minecraft/util/RandomSource;)Ljava/util/List;"))
 private List<StructurePoolElement> limit(StructureTemplatePool pool,RandomSource random){var all=pool.getShuffledTemplates(random);if(pieces.stream().anyMatch(p->p instanceof PoolElementStructurePiece e&&e.getElement().toString().matches(".*slavicmyths:(military/barracks|village/[^/]+/defense/druzhinnik_barracks_01).*" )))return all.stream().filter(e->!e.toString().matches(".*slavicmyths:(military/barracks|village/[^/]+/defense/druzhinnik_barracks_01).*" )).toList();return all;}
}
