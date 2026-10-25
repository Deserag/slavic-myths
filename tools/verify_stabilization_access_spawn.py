"""Source wiring and preservation checks; not a Minecraft runtime test."""
from pathlib import Path
import subprocess
import json

R = Path(__file__).resolve().parents[1]
def source(path):
    return (R / 'src/main/java/org/slavicmyths' / path).read_text(encoding='utf-8')

yaga = source('yaga/YagaDiscovery.java')
events = source('tales/TaleEvents.java')
assert 'YagaAccessRules.victory(e.getAdvancement().id().getPath()))org.slavicmyths.yaga.YagaDiscovery.refresh(p)' in events
assert 'TickTask(p.server.getTickCount()+1' in yaga and '==p&&p.isCreative())refresh(p)' in yaga
assert 'TaleData.get(p.serverLevel()).stage(p.getUUID(),"slavicmyths:yaga_search")' in yaga
assert 'data.placed&&data.anchor!=null&&data.npc!=null' in yaga
assert 'delta.put("Hints",TaleService.hints(p))' in yaga
sivka = source('sivka/SivkaContent.java')
assert 'w.noCollision(box)&&!w.containsAnyLiquid(box)' in sivka
assert 'SivkaRules.WIDTH/2' in sivka and 'p.getY()+SivkaRules.HEIGHT' in sivka
assert 'w.hasChunkAt' in sivka and 'BlockTags.DIRT' in sivka
assert 'nextInt' not in sivka
for path in ['progression/AdvancementMilestones.java', 'hunt/WorldBoss.java', 'yaga/YagaServices.java', 'yaga/YagaData.java', 'firebird/FirebirdEncounter.java', 'mor/MorRules.java']:
    rel = 'src/main/java/org/slavicmyths/' + path
    before = subprocess.check_output(['git', 'show', '2f82f20e:' + rel], cwd=R)
    assert before.decode('utf-8').replace('\r\n', '\n') == (R / rel).read_text(encoding='utf-8'), ('unexpected change', path)
assert not subprocess.check_output(['git','diff','2f82f20e','--','src/main/resources'],cwd=R), 'unexpected resource change'
spawn = json.loads((R/'src/main/resources/data/slavicmyths/neoforge/biome_modifier/sivka_burka.json').read_text())
assert spawn['spawners'] == [{'type':'slavicmyths:sivka_burka','weight':1,'minCount':1,'maxCount':1}]
print('PASS source wiring/preservation; OFFLINE_ONLY. Collision/world/game-mode event delivery NOT_RUN.')
