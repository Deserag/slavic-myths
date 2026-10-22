"""#160 ThunderAxe: whitelist source regressions, no Minecraft launch."""
from pathlib import Path
import re
ROOT=Path(__file__).resolve().parents[1]
src=(ROOT/'src/main/java/org/slavicmyths/item/ThunderAxeItem.java').read_text(encoding='utf-8')
assert 'extends AxeItem' in src
assert 'AxeItem.createAttributes(tier, 5.0F, -3.1F)' in src
assert '@EventBusSubscriber(modid = SlavicMyths.MOD_ID)' in src
assert '@SubscribeEvent' in src
assert 'LivingDamageEvent.Pre event' in src
assert 'event.getNewDamage() <= 0.0F' in src
assert 'event.getSource().is(DamageTypes.PLAYER_ATTACK)' in src
assert 'event.getSource().getDirectEntity() instanceof ServerPlayer player' in src
assert 'player.getMainHandItem().getItem() != ModItems.THUNDER_AXE.get()' in src
assert 'player.getCooldowns().isOnCooldown(ModItems.THUNDER_AXE.get())' in src
assert 'player.getRandom().nextFloat() >= THUNDER_CHANCE' in src
assert 'THUNDER_CHANCE = 0.2F' in src
assert 'BONUS_DAMAGE = 1.0F' in src
assert 'THUNDER_COOLDOWN = 60' in src
assert 'event.setNewDamage(event.getNewDamage() + BONUS_DAMAGE)' in src
assert src.index('event.setNewDamage(')<src.index('world.sendParticles(')<src.index('world.playSound(')
assert 'target.hurt(' not in src and '.invulnerableTime' not in src
assert src.count('addCooldown(')==1 and src.count('setNewDamage(')==1
assert re.search(r'boolean hurtEnemy\([^}]+return super\.hurtEnemy\(stack, target, attacker\);',src)
print('PASS Thunder Axe: accepted hit only, 20% roll, +1HP final damage, 60 ticks, exactly one proc/FX, no second hurt or invulnerability reset. Runtime NOT_RUN.')
