"""Final shared resource policy, also applied after legacy asset generators.

Keep registry/event IDs and authored mob rigs. Spawn eggs use Minecraft's own
two-layer pixels and DeferredSpawnEggItem tints. Enemy sound cues use semantic
vanilla events rather than the previously supplied synthesized samples.
"""
from pathlib import Path
import json

ROOT=Path(__file__).resolve().parents[1]
A=ROOT/'src/main/resources/assets/slavicmyths'
EGGS=('chud_resident_spawn_egg','chud_artisan_spawn_egg','chud_guard_spawn_egg',
      'skim_beast_spawn_egg','idolishche_poganoe_spawn_egg','marinka_spawn_egg',
      'kartaus_spawn_egg','kalin_tsar_spawn_egg','kalin_war_horse_spawn_egg',
      'cursed_volkolak_spawn_egg')

# Explicit per-cue choices: no substring guessing at runtime.
SOUNDS={}
def group(prefix, mapping):
    SOUNDS.update({prefix+'.'+key: 'minecraft:'+value for key,value in mapping.items()})

group('skim', {
 'ambient_1':'entity.ravager.ambient','ambient_2':'entity.wolf.growl',
 'step_1':'entity.ravager.step','step_2':'entity.ravager.step','run_paws':'entity.ravager.step',
 'claw_windup':'entity.player.attack.sweep','claw_hit':'entity.player.attack.strong',
 'leap_charge':'entity.ravager.attack','leap_land':'entity.generic.big_fall',
 'whistle_charge':'entity.warden.listening','whistle_release':'entity.warden.sonic_boom',
 'roar':'entity.ravager.roar','grab':'entity.ravager.attack','grip_hold':'entity.wolf.growl',
 'throw':'entity.player.attack.knockback','rage':'entity.ravager.roar',
 'exhausted':'entity.ravager.stunned','stone_collision':'block.stone.break',
 'hurt_1':'entity.ravager.hurt','hurt_2':'entity.ravager.hurt','death':'entity.ravager.death'})
group('idolishche_poganoe', {
 'death_01':'entity.ravager.death','eat_01':'entity.generic.eat',
 'grasp_01':'entity.iron_golem.attack','hurt_01':'entity.ravager.hurt','hurt_02':'entity.ravager.hurt',
 'idle_growl_01':'entity.ravager.ambient','idle_growl_02':'entity.ravager.ambient',
 'minions_summon_01':'entity.evoker.prepare_summon','poison_breath_01':'entity.generic.extinguish_fire',
 'punch_impact_01':'entity.player.attack.strong','rage_01':'entity.ravager.roar',
 'roar_01':'entity.ravager.roar','rush_charge_01':'entity.ravager.attack',
 'step_heavy_01':'entity.iron_golem.step','stomp_mid_01':'entity.generic.big_fall',
 'stomp_near_01':'entity.generic.big_fall','stomp_outer_01':'entity.generic.small_fall',
 'throw_01':'entity.player.attack.knockback'})
group('marinka', {
 'witch_whisper_01':'entity.witch.ambient','witch_whisper_02':'entity.witch.ambient',
 'cloth_step_01':'block.wool.step','trace_hex_01':'entity.evoker.prepare_attack',
 'fire_charge_01':'entity.blaze.ambient','fire_release_01':'item.firecharge.use',
 'shadow_dissolve_01':'entity.enderman.teleport','shadow_clone_break_01':'block.glass.break',
 'barrier_raise_01':'item.armor.equip_chain','barrier_break_01':'block.glass.break',
 'witch_blink_01':'entity.enderman.teleport','magpie_change_01':'entity.parrot.ambient',
 'magpie_wings_01':'entity.parrot.fly','magpie_wings_02':'entity.parrot.fly',
 'aurochs_hex_01':'entity.evoker.cast_spell','enchanted_herd_01':'entity.cow.ambient',
 'witch_hurt_01':'entity.witch.hurt','witch_rage_01':'entity.witch.celebrate',
 'witch_defeat_01':'entity.witch.death'})
group('entity.kartaus', {
 'intro':'entity.vindicator.ambient','banner_raise':'item.armor.equip_leather',
 'summon_guard':'entity.evoker.prepare_summon','staff_wave':'entity.evoker.cast_spell',
 'staff_melee':'entity.player.attack.strong','chain_bind':'block.chain.place',
 'black_eye_charge':'entity.evoker.prepare_attack','black_eye_impact':'entity.evoker_fangs.attack',
 'curse_falcon':'entity.evoker.cast_spell','barrier_break':'block.glass.break',
 'rage':'entity.vindicator.celebrate','hurt_1':'entity.vindicator.hurt',
 'hurt_2':'entity.vindicator.hurt','hurt_3':'entity.vindicator.hurt','death':'entity.vindicator.death',
 'heavy_step_1':'block.metal.step','heavy_step_2':'block.metal.step','staff_ground':'block.wood.hit'})
group('kalin', {
 'banner_flap':'item.armor.equip_leather','battle_seal_use':'item.book.page_turn',
 'camp_fire_loop_short':'block.campfire.crackle','encounter_start_fx':'item.goat_horn.sound.0',
 'horse_gallop_loop':'entity.horse.gallop','horse_rear':'entity.horse.angry',
 'horse_snort':'entity.horse.breathe','horse_step_01':'entity.horse.step',
 'horse_step_02':'entity.horse.step','horse_stomp':'entity.generic.big_fall',
 'kalin_command_shout_fx':'entity.vindicator.celebrate','kalin_kneel_surrender':'item.armor.equip_leather',
 'kalin_parry':'item.shield.block','kalin_rage_burst':'entity.vindicator.celebrate',
 'kalin_sabre_heavy':'entity.player.attack.strong','kalin_sabre_light':'entity.player.attack.sweep',
 'kalin_spin_attack':'entity.player.attack.sweep','kalin_step_heavy_01':'block.metal.step',
 'kalin_step_heavy_02':'block.metal.step','kalin_war_horn':'item.goat_horn.sound.0',
 'soldier_spawn_common':'item.armor.equip_iron','soldier_spawn_elite':'item.armor.equip_chain',
 'ui_select_two_heroes':'ui.button.click'})

def apply():
    from vanilla_item_style import apply as apply_item_style
    apply_item_style()
    for name in EGGS:
        path=A/'models/item'/f'{name}.json'
        path.parent.mkdir(parents=True,exist_ok=True)
        path.write_text(json.dumps({'parent':'minecraft:item/template_spawn_egg'},indent=2)+'\n',encoding='utf8')
    path=A/'sounds.json';sounds=json.loads(path.read_text(encoding='utf8'))
    for name,target in SOUNDS.items():
        assert name in sounds, 'Missing sound cue: '+name
        sounds[name]['sounds']=[{'name':target,'type':'event','volume':0.65,'attenuation_distance':48 if name=='skim.roar' else 36 if name.startswith('skim.whistle') else 28 if name.startswith('skim.') else 16}]
    path.write_text(json.dumps(sounds,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
    # Delegated recorded selections supersede historical generic aliases.
    from voice_rework_60 import restore_accepted
    restore_accepted()

if __name__=='__main__':
    apply()
    print(f'RESOURCE_POLICY_APPLIED eggs={len(EGGS)} enemy_cues={len(SOUNDS)}')
