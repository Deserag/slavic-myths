from pathlib import Path
p=Path('tools/kurgan-tests/org/slavicmyths/verify/YagaPlacementHeadless.java');p.write_text(p.read_text('utf-8-sig'),'utf-8')
p=Path('build.gradle');s=p.read_text();s+='''
// Placement regression checks use vanilla block states in a JVM, not a running game.
tasks.register('verifyYagaPlacement', JavaExec) {
    dependsOn 'kurganCheckClasses'
    classpath = sourceSets.kurganCheck.runtimeClasspath
    mainClass = 'org.slavicmyths.verify.YagaPlacementHeadless'
}
''';p.write_text(s,'utf-8')
