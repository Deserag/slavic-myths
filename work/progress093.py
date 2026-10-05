from pathlib import Path
for path in [Path('src/main/java/org/slavicmyths/yaga/YagaData.java'),Path('tools/kurgan-tests/org/slavicmyths/verify/YagaHeadless.java')]:path.write_text(path.read_text('utf-8-sig'),'utf8')
p=Path('build.gradle');s=p.read_text('utf8');s+="\ntasks.register('verifyYaga', JavaExec) {\n    dependsOn 'kurganCheckClasses'\n    classpath = sourceSets.kurganCheck.runtimeClasspath\n    mainClass = 'org.slavicmyths.verify.YagaHeadless'\n}\n";p.write_text(s,'utf8')
