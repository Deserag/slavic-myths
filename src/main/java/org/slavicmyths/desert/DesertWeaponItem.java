package org.slavicmyths.desert;

import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.ClipContext;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slavicmyths.combat.*;
import org.slavicmyths.rpg.PathData;

@EventBusSubscriber(modid="slavicmyths")
public final class DesertWeaponItem extends TieredItem implements WeaponInputHandler {
    public final DesertWeaponSpec spec;
    private record Pulse(ServerLevel world,UUID target,long due,float damage,ParticleOptions particle,Vec3 origin){}
    private static final Map<UUID,List<Pulse>> PULSES=new HashMap<>();
    public DesertWeaponItem(DesertWeaponSpec spec){
        super(Tiers.DIAMOND,new Item.Properties().rarity(switch(spec.rank){
            case COMMON->Rarity.COMMON;case RARE->Rarity.RARE;case LEGENDARY->Rarity.EPIC;
        }).attributes(org.slavicmyths.combat.MaceItem.attributes(spec.baseDamage,switch(spec){
            case SCORPION_DOUBLE,STINGER_DAGGER,FOUR_WINDS->2.2F;
            case DUNE_MACE,BURIED_HAMMER->.85F;default->1.4F;
        })));this.spec=spec;
    }
    public static boolean isDesertWeapon(String id){for(var s:DesertWeaponSpec.values())if(s.id.equals(id))return true;return false;}
    public static String cooldownKey(String id,int slot){return "CD_desert_"+id+"_"+slot;}
    private static int slot(Action a){return switch(a){case RIGHT_CLICK->1;case SHIFT_RIGHT_CLICK->2;case SHIFT_LEFT_CLICK->3;default->0;};}
    @Override public boolean intercepts(Action a){int s=slot(a);return s==1||s==2&&spec.rank!=DesertWeaponSpec.Rank.COMMON||s==3&&spec.rank==DesertWeaponSpec.Rank.LEGENDARY||s==0&&(spec==DesertWeaponSpec.SAND_SLING||spec==DesertWeaponSpec.MIRAGE_BOW);}
    @Override public Component getName(ItemStack s){return Component.translatableWithFallback(getDescriptionId(),switch(spec){
        case SCORPION_DOUBLE->"Двужало скорпиона";
        case STINGER_DAGGER->"Кинжал-жало";
        case SAND_SLING->"Песчаная праща";
        case GLASS_BLADE->"Стеклянный клинок";
        case HALA_STAFF->"Солнечный посох Халы";
        case DUNE_MACE->"Булава барханов";
        case MIRAGE_BOW->"Лук миражей";
        case QUICKSAND_WHIP->"Плеть зыбучих песков";
        case COLONY_SCEPTER->"Скипетр колонии";
        case FOUR_WINDS->"Кулаки четырёх ветров";
        case LAMYA_WEAPON->"Оружие Лами";
        case BURIED_HAMMER->"Молот погребённых";
    });}
    @Override public boolean hurtEnemy(ItemStack stack,LivingEntity target,LivingEntity attacker){if(!attacker.level().isClientSide)stack.hurtAndBreak(1,attacker,EquipmentSlot.MAINHAND);return true;}
    @Override public void appendHoverText(ItemStack stack,TooltipContext c,List<Component> lines,TooltipFlag f){
        String[] labels=switch(spec){
            case SCORPION_DOUBLE->new String[]{"Двойной удар"};
            case STINGER_DAGGER->new String[]{"Метание кинжала"};
            case SAND_SLING->new String[]{"Усиленный камень"};
            case GLASS_BLADE->new String[]{"Режущий выпад"};
            case HALA_STAFF->new String[]{"Солнечный луч","Солнечный взрыв"};
            case DUNE_MACE->new String[]{"Ударная волна","Сокрушительный удар"};
            case MIRAGE_BOW->new String[]{"Три призрачные стрелы","Пробивающая стрела"};
            case QUICKSAND_WHIP->new String[]{"Захват","Круговой удар"};
            case COLONY_SCEPTER->new String[]{"Призыв муравьёв","Нашествие роя","Массовая атака"};
            case FOUR_WINDS->new String[]{"Каменный кулак","Четыре удара","Гигантский кулак"};
            case LAMYA_WEAPON->new String[]{"Огненная волна","Солнечный луч","Песчаный вихрь"};
            case BURIED_HAMMER->new String[]{"Удар по земле","Зыбучие пески","Песчаные столбы"};
        };
        for(int i=0;i<labels.length;i++){if(spec==DesertWeaponSpec.COLONY_SCEPTER&&i==0){lines.add(Component.literal("ПКМ — Призыв 4 муравьёв на 30 сек.; урон каждого: 12 HP, откат 10 сек.").withStyle(ChatFormatting.GOLD));continue;}int hp=i==0?spec.rightDamage:i==1?spec.shiftRightDamage:spec.shiftLeftDamage;lines.add(Component.literal(new String[]{"ПКМ","Shift + ПКМ","Shift + ЛКМ"}[i]+" — "+labels[i]+": "+hp+" HP, "+spec.cooldownTicks(i==1,i==2)/20+" сек.").withStyle(ChatFormatting.GOLD));}
    }
    private static long now(ServerPlayer p){return p.server.overworld().getGameTime();}
    private static boolean legal(ServerPlayer p,LivingEntity t){return DesertWeaponTargeting.legal(p,t);}
    private static boolean control(ServerPlayer p,LivingEntity t){return CombatTargeting.canControl(p,t)&&!CombatTargeting.controlResistant(t);}
    private static boolean hit(ServerPlayer p,LivingEntity t,float damage){return legal(p,t)&&t.hurt(MythDamageSources.caused("desert_weapon_skill",p),damage);}
    private static void effect(ServerPlayer p,LivingEntity t,net.minecraft.core.Holder<MobEffect> e,int ticks,int amp){if(control(p,t))t.addEffect(new MobEffectInstance(e,ticks,amp));}
    private static void particles(ServerPlayer p,Vec3 v,ParticleOptions type,int count,double spread){p.serverLevel().sendParticles(type,v.x,v.y,v.z,count,spread,spread,spread,.03);}
    private static void line(ServerPlayer p,Vec3 a,Vec3 b,ParticleOptions type){int steps=Math.max(1,Math.min(64,(int)(a.distanceTo(b)*4)));for(int i=0;i<=steps;i++){Vec3 v=a.lerp(b,(double)i/steps);p.serverLevel().sendParticles(type,v.x,v.y,v.z,1,0,0,0,0);}}
    private static void ring(ServerPlayer p,Vec3 c,double r,ParticleOptions type){for(int i=0;i<32;i++){double a=i*Math.PI/16;particles(p,c.add(Math.cos(a)*r,.12,Math.sin(a)*r),type,1,0);}}
    private static void pull(ServerPlayer p,LivingEntity t,Vec3 point){if(control(p,t)){Vec3 v=point.subtract(t.position()).multiply(1,0,1);if(v.lengthSqr()>.25){v=v.normalize();t.push(v.x*.85,.12,v.z*.85);}}}
    private static List<LivingEntity> area(ServerPlayer p,Vec3 c,double r){return DesertWeaponTargeting.area(p,c,r);}
    private static Vec3 center(DesertWeaponTargeting.Ray r){return r.target()==null?r.end().subtract(r.end().subtract(r.start()).normalize().scale(.25)):r.target().getBoundingBox().getCenter();}
    private static void pulse(ServerPlayer p,LivingEntity t,int delay,float damage,ParticleOptions particle,Vec3 origin){if(t!=null)PULSES.computeIfAbsent(p.getUUID(),k->new ArrayList<>()).add(new Pulse(p.serverLevel(),t.getUUID(),now(p)+delay,damage,particle,origin));}
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer p))return;
        var pending=PULSES.get(p.getUUID());
        if(pending!=null){var it=pending.iterator();while(it.hasNext()){var a=it.next();if(a.due>now(p))continue;it.remove();if(a.world!=p.serverLevel()||!p.isAlive()||p.isSpectator())continue;if(a.world.getEntity(a.target) instanceof LivingEntity t&&legal(p,t)&&t.distanceToSqr(p)<1024){hit(p,t,a.damage);if(a.origin!=null)line(p,a.origin,t.getBoundingBox().getCenter(),a.particle);particles(p,t.getBoundingBox().getCenter(),a.particle,12,.3);}}if(pending.isEmpty())PULSES.remove(p.getUUID());}
        ItemStack stack=p.getMainHandItem();if(!(stack.getItem() instanceof DesertWeaponItem)&&!(stack.getItem() instanceof MirageBowItem))return;
        var spec=stack.getItem() instanceof DesertWeaponItem item?item.spec:DesertWeaponSpec.MIRAGE_BOW;
        var n=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();var saved=PathData.data(p);boolean changed=false;
        for(int i=1;i<=3;i++){String k=cooldownKey(spec.id,i);long d=saved.getLong(k);if(n.getLong(k)!=d){n.putLong(k,d);changed=true;}}
        if(changed)stack.set(DataComponents.CUSTOM_DATA,CustomData.of(n));
    }
    @SubscribeEvent public static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){PULSES.remove(e.getEntity().getUUID());}
    private static List<AntMinionEntity> ants(ServerPlayer p){return p.serverLevel().getEntitiesOfClass(AntMinionEntity.class,p.getBoundingBox().inflate(48),a->a.playerBound()&&p.getUUID().equals(a.ownerId())&&a.isAlive());}
    private static boolean summon(ServerPlayer p,LivingEntity t){for(var a:ants(p))a.discard();int spawned=0;for(int i=0;i<4;i++){double a=i*Math.PI/2;var v=p.position().add(Math.cos(a)*1.6,0,Math.sin(a)*1.6);var ant=AntContent.WORKER.get().create(p.serverLevel());if(ant==null)continue;ant.setPos(v.x,v.y,v.z);if(!p.serverLevel().noCollision(ant))continue;ant.bindPlayer(p,i);if(t!=null)ant.command(t);if(p.serverLevel().addFreshEntity(ant))spawned++;}return spawned>0;}
    private static boolean dash(ServerPlayer p,LivingEntity t){if(!legal(p,t))return false;Vec3 d=t.position().subtract(p.position()).multiply(1,0,1);double len=d.length();if(len<1.6)return true;Vec3 dest=p.position().add(d.normalize().scale(Math.min(3,len-1.4)));var shift=dest.subtract(p.position());if(p.serverLevel().noCollision(p,p.getBoundingBox().move(shift))&&p.serverLevel().clip(new ClipContext(p.getEyePosition(),p.getEyePosition().add(shift),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getType()==HitResult.Type.MISS)p.teleportTo(dest.x,dest.y,dest.z);return legal(p,t)&&t.distanceToSqr(p)<25;}
    private static boolean shot(ServerPlayer p,ItemStack model,float damage,int poison,boolean push,float offset,int pierce){
        var a=new WeaponProjectile(p.serverLevel(),p,model.copyWithCount(1),true,true);
        a.configureDesert(damage,poison,push,pierce);
        a.shootFromRotation(p,p.getXRot(),p.getYRot()+offset,0,2.1F,0);
        return p.serverLevel().addFreshEntity(a);
    }
    private static void ranged(ServerPlayer p,DesertWeaponTargeting.Ray ray,float damage,ParticleOptions type){if(ray.target()!=null)hit(p,ray.target(),damage);line(p,ray.start(),ray.end(),type);}
    @Override public void onWeaponAction(ServerPlayer p,ItemStack held,Action action){
        if(!intercepts(action)||!p.isAlive()||p.isSpectator()||p.getMainHandItem()!=held||held.getItem()!=this)return;
        int s=slot(action);if(p.isShiftKeyDown()!=(s>=2))return;String key=cooldownKey(spec.id,s);var state=PathData.data(p);if(state.getLong(key)>now(p))return;
        var ray=DesertWeaponTargeting.trace(p,spec==DesertWeaponSpec.SCORPION_DOUBLE?4:spec==DesertWeaponSpec.GLASS_BLADE?6:spec==DesertWeaponSpec.QUICKSAND_WHIP?9:24);var t=ray.target();Vec3 c=center(ray);boolean ok=true;
        switch(spec){
            case SCORPION_DOUBLE->{if(t==null)return;if(hit(p,t,13)){effect(p,t,MobEffects.POISON,100,0);pulse(p,t,4,13,ParticleTypes.CRIT,null);}particles(p,c,ParticleTypes.CRIT,16,.25);}
            case STINGER_DAGGER->ok=shot(p,held,24,120,false,0,0);
            case SAND_SLING->ok=shot(p,new ItemStack(Items.COBBLESTONE),s==0?16:28,0,s!=0,0,0);
            case GLASS_BLADE->{if(t==null||!dash(p,t))return;if(hit(p,t,22))for(int i=1;i<=3;i++)pulse(p,t,i*20,2,ParticleTypes.CRIT,null);particles(p,c,ParticleTypes.CRIT,24,.35);}
            case DUNE_MACE->{if(!p.onGround())return;double r=s==1?5:4;for(var e:area(p,p.position(),r))if(hit(p,e,s==1?38:48)&&control(p,e)){if(s==2)e.push(0,.85,0);else{Vec3 v=e.position().subtract(p.position());e.knockback(.65,-v.x,-v.z);}}ring(p,p.position(),r,ParticleTypes.POOF);}
            case MIRAGE_BOW->{if(s==2)ok=shot(p,new ItemStack(Items.ARROW),44,0,false,0,4);else if(s==0)ok=shot(p,new ItemStack(Items.ARROW),26,0,false,0,0);else{ok=false;for(int i=-1;i<=1;i++)ok|=shot(p,new ItemStack(Items.ARROW),12,0,false,i*4,0);}}
            case QUICKSAND_WHIP->{if(s==1){if(t==null)return;if(hit(p,t,30))pull(p,t,p.position());line(p,ray.start(),ray.end(),ParticleTypes.CRIT);}else{for(var e:area(p,p.position(),6))if(hit(p,e,42))effect(p,e,MobEffects.MOVEMENT_SLOWDOWN,120,1);ring(p,p.position(),6,ParticleTypes.POOF);}}
            case COLONY_SCEPTER->{if(s==1)ok=summon(p,t);else if(s==2){for(var e:area(p,c,5))hit(p,e,60);particles(p,c,ParticleTypes.POOF,60,2);}else{if(t==null)return;var own=ants(p);if(own.isEmpty())return;for(var a:own){a.command(t);a.enrage();}hit(p,t,72);particles(p,c,ParticleTypes.CRIT,40,.8);}}
            case FOUR_WINDS->{if(s==1){ranged(p,ray,48,ParticleTypes.CLOUD);}else if(s==2){if(t==null)return;for(int i=0;i<4;i++){double a=i*Math.PI/2;pulse(p,t,i*5+1,15,ParticleTypes.CLOUD,c.add(Math.cos(a)*4,1,Math.sin(a)*4));}}else{var targets=new LinkedHashSet<>(area(p,c,4));if(t!=null)targets.add(t);for(var e:targets)hit(p,e,72);line(p,c.add(0,7,0),c,ParticleTypes.CLOUD);particles(p,c,ParticleTypes.EXPLOSION,4,1);}}
            case LAMYA_WEAPON->{if(s==1){var targets=new LinkedHashSet<>(area(p,c,5));if(t!=null)targets.add(t);for(var e:targets)if(hit(p,e,48))e.setRemainingFireTicks(Math.max(e.getRemainingFireTicks(),100));ring(p,c,5,ParticleTypes.FLAME);}else if(s==2){if(t!=null)hit(p,t,60);particles(p,c,ParticleTypes.FLAME,30,.3);}else{for(var e:area(p,c,6))if(hit(p,e,72))pull(p,e,c);for(int i=1;i<=4;i++)ring(p,c.add(0,i*.4,0),6-i*.8,ParticleTypes.CLOUD);}}
            case BURIED_HAMMER->{if(s==1){if(!p.onGround())return;for(var e:area(p,p.position(),5))hit(p,e,48);ring(p,p.position(),5,ParticleTypes.EXPLOSION);}else if(s==2){for(var e:area(p,c,5)){if(hit(p,e,15)){effect(p,e,MobEffects.MOVEMENT_SLOWDOWN,80,6);for(int i=1;i<=3;i++)pulse(p,e,i*20,15,ParticleTypes.POOF,null);}}ring(p,c,5,ParticleTypes.POOF);}else{for(var e:area(p,c,5))hit(p,e,72);for(int i=0;i<8;i++){double a=i*Math.PI/4;Vec3 v=c.add(Math.cos(a)*3,0,Math.sin(a)*3);line(p,v,v.add(0,4,0),ParticleTypes.POOF);}}}
            case HALA_STAFF->{return;}
        }
        if(ok){state.putLong(key,now(p)+(s==0?12:spec.cooldownTicks(s==2,s==3)));p.swing(InteractionHand.MAIN_HAND,true);if(!p.hasInfiniteMaterials())held.hurtAndBreak(1,p,EquipmentSlot.MAINHAND);}
    }
}
