from pathlib import Path
import re
base=Path('src/main/java/org/slavicmyths/client')
for name in ['FlightClient','GusliClient','FolkEquipmentLayer','RpgClient']:
 p=base/(name+'.java');s=p.read_text(encoding='utf-8')
 s=s.replace('import net.minecraft.client.gui.ScreenManager;\n','').replace('import net.minecraftforge.fml.client.registry.ClientRegistry;\n','')
 s=s.replace('e.getPlayer()','e.getEntity()').replace('e.getMatrixStack()','e.getPoseStack()').replace('e.getPartialRenderTick()','e.getPartialTick()').replace('e.getPartialTicks()','e.getPartialTick()').replace('e.getBuffers()','e.getMultiBufferSource()').replace('e.getLight()','e.getPackedLight()')
 s=s.replace('mc.player.yRot','mc.player.getYRot()').replace('p.yRot','p.getYRot()').replace('p.xRot','p.getXRot()').replace('p.getModelName().equals("slim")','p.getSkin().model()==net.minecraft.client.resources.PlayerSkin.Model.SLIM')
 s=s.replace('p.getSkinTextureLocation()','p.getSkin().texture()').replace('p.animationPosition','p.walkAnimation.position()').replace('p.animationSpeed','p.walkAnimation.speed()')
 if name=='FlightClient':
  s=re.sub(r' public static void setup\(\)\{[^\n]*\}', ' public static void keys(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent e){e.register(DOWN);e.register(BRAKE);e.register(CARGO);}\n public static void renderers(EntityRendererProvider.Context context){normal=new RiderRenderer(context,false);slim=new RiderRenderer(context,true);}',s)
  s=s.replace('if(normal==null){normal=new RiderRenderer(Minecraft.getInstance().getEntityRenderDispatcher(),false);slim=new RiderRenderer(Minecraft.getInstance().getEntityRenderDispatcher(),true);}','if(normal==null)return;')
  s=s.replace('RiderRenderer(Context','RiderRenderer(EntityRendererProvider.Context').replace('model=new RiderModel(slim)','model=new RiderModel(m.bakeLayer(slim?net.minecraft.client.model.geom.ModelLayers.PLAYER_SLIM:net.minecraft.client.model.geom.ModelLayers.PLAYER),slim)')
  s=s.replace('RiderModel(boolean slim){super(0,slim);}', 'RiderModel(net.minecraft.client.model.geom.ModelPart root,boolean slim){super(root,slim);}')
  s=s.replace('float yaw,float partial){super.setupRotations(p,m,age,yaw,partial)', 'float yaw,float partial,float scale){super.setupRotations(p,m,age,yaw,partial,scale)')
 if name=='GusliClient':
  s=s.replace('LivingEvent.LivingUpdateEvent e','net.neoforged.neoforge.event.tick.EntityTickEvent.Post e')
  s=s.replace('super(ModSounds.GUSLI_LOOP.get(),SoundSource.PLAYERS)', 'super(ModSounds.GUSLI_LOOP.get(),SoundSource.PLAYERS,net.minecraft.util.RandomSource.create())')
  s=s.replace('player.removed','player.isRemoved()').replace('player.level','player.level()')
  s=s.replace('if(normal==null){normal=new Musician(Minecraft.getInstance().getEntityRenderDispatcher(),false);slim=new Musician(Minecraft.getInstance().getEntityRenderDispatcher(),true);}','if(normal==null)return;')
  s=s.replace(' private static final class Musician', ' public static void renderers(EntityRendererProvider.Context context){normal=new Musician(context,false);slim=new Musician(context,true);}\n private static final class Musician')
  s=s.replace('Musician(Context','Musician(EntityRendererProvider.Context').replace('new PlayerModel<AbstractClientPlayer>(0,slim)', 'new PlayerModel<AbstractClientPlayer>(m.bakeLayer(slim?net.minecraft.client.model.geom.ModelLayers.PLAYER_SLIM:net.minecraft.client.model.geom.ModelLayers.PLAYER),slim)')
  s=s.replace('m,e.getMultiBufferSource());','m,e.getMultiBufferSource(),mc.level,0);')
 if name=='FolkEquipmentLayer':
  s=s.replace('public static void setup(){for(PlayerRenderer r:Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap().values())r.addLayer(new FolkEquipmentLayer(r));}', 'public static void layers(net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers e){for(net.minecraft.client.resources.PlayerSkin.Model skin:e.getSkins()){PlayerRenderer r=e.getSkin(skin);if(r!=null)r.addLayer(new FolkEquipmentLayer(r));}}')
  s=s.replace('e.getPackedLight(),net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,1,1,1,Math.max(0,1-(fade+e.getPartialTick())/20F)', 'e.getPackedLight(),net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,((int)(Math.max(0,1-(fade+e.getPartialTick())/20F)*255)<<24)|0xFFFFFF')
  s=s.replace('pose,buffer);pose.popPose();','pose,buffer,p.level(),0);pose.popPose();')
 if name=='RpgClient':
  s=s.replace('import net.neoforged.neoforge.client.event.InputUpdateEvent;', 'import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;')
  s=re.sub(r' public static void setup\(\)\{[^\n]*\}', ' public static void keys(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent e){e.register(ACTIVATE);}',s)
  s=s.replace('InputEvent.KeyInputEvent','InputEvent.Key').replace('InputUpdateEvent','MovementInputUpdateEvent').replace('MovementMovementInputUpdateEvent','MovementInputUpdateEvent').replace('e.getMovementInput()','e.getInput()')
  s=s.replace('e.getItemStack().getItem().getRegistryName()', 'net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(e.getItemStack().getItem())')
 p.write_text(s,encoding='utf-8')
p=base/'ClientSetup.java';s=p.read_text(encoding='utf-8').replace('            RpgClient.setup();\n','').replace('            FlightClient.setup();\n','').replace('            FolkEquipmentLayer.setup();\n','');pos=s.rfind('}')
s=s[:pos]+'''    @SubscribeEvent public static void keys(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent e){RpgClient.keys(e);FlightClient.keys(e);}
    @SubscribeEvent public static void layers(net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers e){FolkEquipmentLayer.layers(e);FlightClient.renderers(e.getContext());GusliClient.renderers(e.getContext());}
'''+s[pos:];p.write_text(s,encoding='utf-8')
p=Path('src/main/java/org/slavicmyths/wood/WoodlandClient.java');s=p.read_text(encoding='utf-8').replace('import net.minecraft.client.renderer.tileentity.SignTileEntityRenderer;','import net.minecraft.client.renderer.blockentity.SignRenderer;').replace('import net.minecraftforge.fml.client.registry.ClientRegistry;\n','').replace('  ClientRegistry.bindTileEntityRenderer(Woodlands.SIGN_TILE.get(),SignTileEntityRenderer::new);\n','').replace('Atlases.addWoodType','Sheets.addWoodType');pos=s.rfind('}');s=s[:pos]+' @SubscribeEvent public static void renderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers e){e.registerBlockEntityRenderer(Woodlands.SIGN_TILE.get(),SignRenderer::new);}\n'+s[pos:];p.write_text(s,encoding='utf-8')
