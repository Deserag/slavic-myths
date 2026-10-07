from pathlib import Path
p=Path('build.gradle');s=p.read_text();s=s.replace("gameDirectory = project.hasProperty('placementOverrideCheck') ?", "gameDirectory = project.hasProperty('kurganHotfixCheck') ? file('run-kurgan-hotfix') : project.hasProperty('placementOverrideCheck') ?")
s=s.replace("systemProperty 'neoforge.enabledGameTestNamespaces', project.hasProperty('placementOverrideCheck') ?", "systemProperty 'neoforge.enabledGameTestNamespaces', project.hasProperty('kurganHotfixCheck') ? 'slavicmyths_hotfix' : project.hasProperty('placementOverrideCheck') ?")
s += "\n// The isolated legacy harness also sees registry classes referenced by new native effects.\nsourceSets.portCore.compileClasspath += sourceSets.main.output\ntasks.named('compilePortCoreJava') { dependsOn 'classes' }\n"
p.write_text(s)
# Reuse exact tiny test structure; production has no test namespace.
import shutil
p=next(Path('tools/kurgan-test-resources/data').rglob('port_empty.nbt'))
out=Path('tools/kurgan-test-resources/data/slavicmyths_hotfix/structure/port_empty.nbt');out.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(p,out)
p=Path('tools/kurgan-tests/org/slavicmyths/verify/KurganHeadless.java');s=p.read_text();s=s.replace('tier==2&&disturbance>=75?1:0','tier==2?1:0').replace('if(tier==2&&disturbance>=75)','if(tier==2)');p.write_text(s)
