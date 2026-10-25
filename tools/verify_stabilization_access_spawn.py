"""Source wiring and preservation checks; not a Minecraft runtime test."""
from pathlib import Path
import subprocess
import json
from verify_yaga272 import OWNED as YAGA272, scope as yaga272_scope

R = Path(__file__).resolve().parents[1]
def source(path):
    return (R / 'src/main/java/org/slavicmyths' / path).read_text(encoding='utf-8')

yaga = source('yaga/YagaDiscovery.java')
events = source('tales/TaleEvents.java')
assert 'YagaAccessRules.victory(e.getAdvancement().id().getPath()))org.slavicmyths.yaga.YagaDiscovery.refresh(p)' in events
yaga272_scope()  # Committed synchronous hook supersedes the historical next-tick callback.
assert 'YagaStoryProgress.reconcile' in yaga
assert 'data.placed&&data.anchor!=null&&data.npc!=null' in yaga
assert 'delta.put("Hints",TaleService.hints(p))' in yaga
sivka = source('sivka/SivkaContent.java')
assert 'w.noCollision(box)&&!w.containsAnyLiquid(box)' in sivka
assert 'SivkaRules.WIDTH/2' in sivka and 'p.getY()+SivkaRules.HEIGHT' in sivka
assert 'w.hasChunkAt' in sivka and 'BlockTags.DIRT' in sivka
assert 'nextInt' not in sivka
# Firebird placement is now owned by #271; its ledger is covered by the native #271 fixture.
for path in ['progression/AdvancementMilestones.java', 'hunt/WorldBoss.java', 'yaga/YagaServices.java', 'yaga/YagaData.java', 'mor/MorRules.java']:
    if path in YAGA272:continue
    rel = 'src/main/java/org/slavicmyths/' + path
    before = subprocess.check_output(['git', 'show', '2f82f20e:' + rel], cwd=R)
    assert before.decode('utf-8').replace('\r\n', '\n') == (R / rel).read_text(encoding='utf-8'), ('unexpected change', path)
# Resource preservation with the explicit #272 strings/mixin exception is checked above.
spawn = json.loads((R/'src/main/resources/data/slavicmyths/neoforge/biome_modifier/sivka_burka.json').read_text())
assert spawn['spawners'] == [{'type':'slavicmyths:sivka_burka','weight':1,'minCount':1,'maxCount':1}]
print('PASS source wiring/preservation; OFFLINE_ONLY. Collision/world/game-mode event delivery NOT_RUN.')
