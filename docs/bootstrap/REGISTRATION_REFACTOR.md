# Registration refactor — in progress

## Cause
Multiple code paths can attach deferred registries to the NeoForge mod bus. The initial constructor also mixes registry installation, feature initialization, listeners and configs.

## Architecture introduced
`org.slavicmyths.bootstrap.RegistryBootstrap` is an explicit entrypoint for DeferredRegister bus attachment. `ModModule` establishes the module listener contract. This migration is **incomplete**: the main constructor still contains many bootstrap calls and certain feature init routines may attach their own registers.

## New files
- src/main/java/org/slavicmyths/bootstrap/RegistryBootstrap.java
- src/main/java/org/slavicmyths/bootstrap/ModModule.java
- docs/bootstrap/REGISTRATION_INVENTORY.md
- docs/bootstrap/REGISTRATION_REFACTOR.md

## Modified files so far
- src/main/java/org/slavicmyths/SlavicMyths.java
- src/main/java/org/slavicmyths/winters/WintersContent.java
- src/main/java/org/slavicmyths/industry/Industry.java

## Add a module (target design)
1. Declare each DeferredRegister in one feature owner.
2. Register entries before mod bus registry events.
3. Route DeferredRegister bus attachment through RegistryBootstrap only.
4. Implement ModModule for listeners and setup hooks.
5. Add the module to the ordered bootstrap module list once implemented.
6. Verify with `./gradlew clean build` and `./gradlew runClient`.

## Verification
Not yet executed. Success requires Java 21 compilation, working runClient, unchanged registry IDs, and zero registry re-attachment IllegalStateExceptions.
