exec((__import__('pathlib').Path(__file__).parent/'implement_classes3.py').read_text(encoding='utf-8').split("edit(C+'ClassDefinitions.java'")[0])
p=ROOT/(C+'ClassPassiveEvents.java');s=p.read_text(encoding='utf-8');a=s.index('        if(!(e.getSource().getEntity() instanceof ServerPlayer p)||');b=s.index('    @SubscribeEvent public static void damaged',a)
s=s[:a]+'''    }
    public static double ambush(ServerPlayer p,net.minecraft.world.entity.LivingEntity target){
        if(!ClassXpService.hostile(p,target))return 0;
        String key="ClassAmbush:"+p.getUUID();int rank=ClassState.passive(p,"ambush_master");
        if(rank<=0||target.getPersistentData().getBoolean(key)||target instanceof Mob mob&&mob.getTarget()==p||target.getLastHurtByMob()==p)return 0;
        target.getPersistentData().putBoolean(key,true);return ClassDefinitions.SKILLS.get("ambush_master").power(rank);
    }
'''+s[b:];p.write_text(s,encoding='utf-8')
edit(C+'ClassEvents.java','e.setAmount((float)(e.getAmount()*(1+bonus)));','double ambush=ClassPassiveEvents.ambush(p,e.getEntity());bonus+=Math.max(0,Math.min(ambush,.80-bonus));e.setAmount((float)(e.getAmount()*(1+bonus)));')
edit(C+'ClassXpService.java','target!=p && !target.isAlliedTo(p)','target!=p && !target.isAlliedTo(p) && !p.isAlliedTo(target)')
# Remove modifiers immediately when a passive is unequipped, not one tick later.
edit(C+'ClassState.java','p.setHealth(Math.min(p.getHealth(),p.getMaxHealth()));','ClassPassiveEvents.tick(p);p.setHealth(Math.min(p.getHealth(),p.getMaxHealth()));')
