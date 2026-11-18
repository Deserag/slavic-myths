# Второй компьютер: Вий и текущие правки посада

Интеграционная ветка: `codex/integrate-posad-release25-20261106`, цель PR: `release-2.5`. До слияния используйте интеграционную ветку; после подтверждённого merge переключитесь на release-2.5.

## Уже есть репозиторий

В PowerShell перейдите в каталог проекта и выполните `git status`. При незавершённом merge или несохранённых изменениях не выполняйте reset/clean: сначала сохраните свою работу либо используйте отдельный clone ниже.

```powershell
git fetch origin
git switch codex/integrate-posad-release25-20261106
git pull --ff-only origin codex/integrate-posad-release25-20261106
git log -1 --oneline
```

Если локальной ветки ещё нет, обычный `git switch` создаст её из одноимённой origin-ветки при стандартной настройке Git. Альтернатива: `git switch --track origin/codex/integrate-posad-release25-20261106`.

## Отдельная чистая копия

В родительском каталоге, где ещё нет папки slavic-myths-viy:

```powershell
git clone --branch codex/integrate-posad-release25-20261106 https://github.com/Deserag/slavic-myths.git slavic-myths-viy
cd slavic-myths-viy
```

## Сборка и dev-клиент

Нужен JDK21. Укажите фактический путь установленного JDK на втором ПК. Переменная меняется только на время команды:

```powershell
$previousJavaHome = $env:JAVA_HOME
try {
    $env:JAVA_HOME = 'C:/Program Files/Java/jdk-21.0.12.1'
    & "$env:JAVA_HOME/bin/java.exe" -version
    .\gradlew.bat clean build
    if ($LASTEXITCODE -ne 0) { throw 'Сборка не прошла; клиент не запускаем' }
    .\gradlew.bat runClient
} finally {
    $env:JAVA_HOME = $previousJavaHome
}
```

Production JAR: build/libs/slavicmyths-2.5-INTEGRATION-20261105-viy-fix3.jar. Minecraft1.21.1, NeoForge21.1.255. Не кладите dev/QA-классы в mods. Для PolyMC используйте отдельный профиль1.21.1 с нужными зависимостями, одним JAR Slavic Myths и резервной копией старого JAR; старые миры не перезаписывайте.

## Ручная проверка

В новом отдельном Creative-мире возьмите яйцо Вия во вкладке существ мода. Создайте Вия на просторной площадке: модель, закрытые веки и переход во вторую фазу должны отображаться без падения клиента. OP-диагностика: `/summon slavicmyths:viy ~ ~ ~`.

В отдельном Survival-тесте проверьте бой, взгляд, железную длань, слуг и повторный вход; затем dedicated+два клиента. Локальные smoke-тесты подтверждают запуск/рендер/фазу, но не полную механику или окончательный баланс.2800HP и30урона остаются предварительными. Естественный спавн/арена/награды Вия этим импортом не добавляются.
