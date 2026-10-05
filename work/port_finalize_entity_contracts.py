from pathlib import Path
import re,json
for p in Path('src/main/java').rglob('*.java'):
 s=p.read_text(encoding='utf-8');o=s
 s=s.replace('.saturationMod(','.saturationModifier(').replace('.alwaysEat()', '.alwaysEdible()')
 s=s.replace('net.minecraft.util.math.vector.com.mojang.math.Axis','com.mojang.math.Axis')
 s=s.replace('SnowballEntity','Snowball').replace('EggEntity','ThrownEgg').replace('ProjectileEntity','Projectile').replace('CropsBlock','CropBlock').replace('Blocks.GRASS?', 'Blocks.SHORT_GRASS?')
 s=s.replace('protected float getVoicePitch()', 'public float getVoicePitch()')
 s=re.sub(r'public boolean canChangeDimensions\(\)', 'public boolean canChangeDimensions(net.minecraft.world.level.Level from,net.minecraft.world.level.Level to)',s).replace('super.canChangeDimensions()', 'super.canChangeDimensions(from,to)')
 s=re.sub(r'\b(\w+)\.canChangeDimensions\(\)',r'\1.canChangeDimensions(\1.level(),\1.level())',s)
 s=re.sub(r'public boolean causeFallDamage\(float (\w+),float (\w+)\)',r'public boolean causeFallDamage(float \1,float \2,net.minecraft.world.damagesource.DamageSource source)',s)
 s=s.replace('super.causeFallDamage(Math.max(0,distance-7),multiplier)', 'super.causeFallDamage(Math.max(0,distance-7),multiplier,source)')
 if p.name=='ModItems.java':s=s.replace('java.util.function.Supplier<? extends net.minecraft.world.entity.EntityType<?>>','java.util.function.Supplier<? extends net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob>>')
 if p.name=='NightingaleEntity.java':
  s=s.replace('remove(boolean keepData)','remove(Entity.RemovalReason reason)').replace('super.remove(keepData)','super.remove(reason)').replace('dropAllDeathLoot(DamageSource source)','dropAllDeathLoot(ServerLevel world,DamageSource source)').replace('super.dropAllDeathLoot(fatal)','super.dropAllDeathLoot((ServerLevel)level(),fatal)').replace('}remove();','}discard();').replace('((ServerLevel)level)','((ServerLevel)level())').replace('p.level==level','p.level()==level()')
 if p.name in ['HuntMob.java','SerpentProjection.java']:s=s.replace('remove();','discard();')
 if p.name=='WaterSpirit.java':s=s.replace('PathType.WATER','net.minecraft.world.level.pathfinder.PathType.WATER').replace('Action.MOVE_TO','Operation.MOVE_TO').replace('Action.WAIT','Operation.WAIT').replace('yRot=(float)(Math.atan2(d.z,d.x)*180/Math.PI)-90;', 'setYRot((float)(Math.atan2(d.z,d.x)*180/Math.PI)-90);').replace(' public boolean canBreatheUnderwater(){return true;}\n','')
 if p.name=='WorldBoss.java':s=s.replace('protected float getStandingEyeHeight(net.minecraft.world.entity.Pose pose,net.minecraft.entity.EntitySize size){return kind()==BossKind.LIKHO?3.02F:2.68F;}', 'protected net.minecraft.world.entity.EntityDimensions getDefaultDimensions(net.minecraft.world.entity.Pose pose){return super.getDefaultDimensions(pose).withEyeHeight(kind()==BossKind.LIKHO?3.02F:2.68F);}')
 if p.name=='PoludnitsaEntity.java':s=s.replace('public EntitySize getDimensions(Pose pose){return EntitySize.scalable(', 'protected EntityDimensions getDefaultDimensions(Pose pose){return EntityDimensions.scalable(')
 if p.name=='RusalkaSound.java':s=s.replace('super(ModSounds.RUSALKA_SONG.get(),SoundSource.HOSTILE)', 'super(ModSounds.RUSALKA_SONG.get(),SoundSource.HOSTILE,net.minecraft.util.RandomSource.create())').replace('LivingEvent.LivingUpdateEvent','net.neoforged.neoforge.event.tick.EntityTickEvent.Post').replace('e.getEntity().level.isClientSide','e.getEntity().level().isClientSide').replace('singer.removed','singer.isRemoved()').replace('singer.level','singer.level()')
 if p.name=='KurganCreatureRenderer.java':s=s.replace('KurganCreatureRenderer(Context','KurganCreatureRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context').replace('IEntityRenderer<','net.minecraft.client.renderer.entity.RenderLayerParent<').replace('OverlayTexture.NO_OVERLAY,1,1,1,.3F','OverlayTexture.NO_OVERLAY,0x4CFFFFFF')
 if p.name=='YagaMenu.java':s=s.replace('public ItemStack clicked(int slot,int button,ClickType type,Player p){if(!stillValid(p)||slot>=0&&slot<5&&tab()!=3)return ItemStack.EMPTY;return super.clicked(slot,button,type,p);}', 'public void clicked(int slot,int button,ClickType type,Player p){if(!stillValid(p)||slot>=0&&slot<5&&tab()!=3)return;super.clicked(slot,button,type,p);}').replace('SoundEvents.EXPERIENCE_ORB_PICKUP','net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP')
 if p.name=='NightingaleModel.java':
  s=s.replace('private final BreathingChest chest;', 'private final ModelPart chest,chestScale;\n    private float chestExpansion;')
  s=s.replace('chest=new BreathingChest();geometry.attach(body,chest);', 'chestScale=part(body,1,0,0,0);chest=part(chestScale,1,0,0,0);')
  s=s.replace('chest.expansion','chestExpansion')
  a=s.index('    private final class BreathingChest');b=s.index('    @Override public void renderToBuffer',a);s=s[:a]+s[b:]
  s=s.replace('root.render(pose,out,light,overlay,color);','chestScale.xScale=1+chestExpansion*.035F;chestScale.yScale=1+chestExpansion*.015F;chestScale.zScale=1+chestExpansion*.1F;root.render(pose,out,light,overlay,color);')
 if s!=o:p.write_text(s,encoding='utf-8')
p=Path('src/main/resources/data/minecraft/tags/entity_type/can_breathe_under_water.json');p.write_text(json.dumps({'replace':False,'values':['slavicmyths:'+v for v in ['vodyanoy','rusalka','elder_vodyanoy']]},indent=2)+'\n',encoding='utf-8')
