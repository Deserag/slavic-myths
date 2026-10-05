from pathlib import Path
import re,zipfile
z=zipfile.ZipFile('build/moddev/artifacts/neoforge-21.1.255-sources.jar');index={n[:-5].replace('/','.') for n in z.namelist() if n.endswith('.java')}
mapping={'Heightmap':'net.minecraft.world.level.levelgen.Heightmap','GenerationStage':'net.minecraft.world.level.levelgen.GenerationStep','EntitySpawnPlacementRegistry':'net.minecraft.world.entity.SpawnPlacements','MobSpawnInfo':'net.minecraft.world.level.biome.MobSpawnSettings','PlayerRenderer':'net.minecraft.client.renderer.entity.player.PlayerRenderer','AtlasTexture':'net.minecraft.client.renderer.texture.TextureAtlas','RenderTypeLookup':'net.minecraft.client.renderer.ItemBlockRenderTypes'}
for t in mapping.values():assert t in index,t
for p in Path('src/main/java').rglob('*.java'):
 old=s=p.read_text(encoding='utf-8')
 if 'TickEvent.Phase' not in s and not re.search(r'(?<!\.)\bTickEvent\.',s):s=s.replace('import net.minecraftforge.event.TickEvent;','')
 for oldname,new in mapping.items():
  if re.search(r'\b'+oldname+r'\b',s):
   s=re.sub(r'\b'+oldname+r'\b',new.rsplit('.',1)[-1],s)
   s=re.sub(r'import [\w.]+\.'+new.rsplit('.',1)[-1]+r';','import '+new+';',s)
   if 'import '+new+';' not in s:s=s.replace('\n','\nimport '+new+';\n',1)
 if re.search(r'\bContext\s+\w+\)',s) and 'extends ' in s and 'Renderer' in p.name and 'BlockEntityRendererProvider.Context' not in s and 'import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;' not in s:s=s.replace('\n','\nimport net.minecraft.client.renderer.entity.EntityRendererProvider.Context;\n',1)
 s=s.replace('import net.minecraft.client.renderer.model.ItemCameraTransforms;','import net.minecraft.world.item.ItemDisplayContext;').replace('ItemCameraTransforms.TransformType.','ItemDisplayContext.')
 if s!=old:p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/client/BanditModel.java');s=p.read_text(encoding='utf-8').replace('var root=mesh.getRoot();','var root=mesh.getRoot();int[] partIndex={0};').replace('part(root.getChild("head"),','part(root.getChild("head"),partIndex,').replace('part(root.getChild("body"),','part(root.getChild("body"),partIndex,').replace('PartDefinition parent,int u','PartDefinition parent,int[] partIndex,int u').replace('parent.getChildren().size()','partIndex[0]++');p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/client/FlightRenderer.java');s=p.read_text(encoding='utf-8').replace('OverlayTexture.NO_OVERLAY,m,b);','OverlayTexture.NO_OVERLAY,m,b,e.level(),e.getId());');p.write_text(s,encoding='utf-8')
