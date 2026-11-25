# Sand King VII / V2
Minecraft 1.21.1, NeoForge, Java 21.

This implementation preserves the 327-cuboid prototype. Geometry and 13 original Vorbis sound effects are generated from the Python sources during resource processing. No Python dependency is needed at Minecraft runtime. SoundFile encodes the original audio waveforms directly to OGG Vorbis, so FFmpeg is not required. To build from a clean checkout, install Python 3 and run:

    python -m pip install -r tools/sand_king/requirements.txt

On Windows, set SAND_KING_PYTHON if Python is not available as python. On Linux the default is python3. If all pre-generated assets from the implementation package are placed into src/main/resources, Gradle uses them instead.

    ./gradlew clean build
    ./gradlew -PdesertCheck runDesertTests

Summon in an open test desert: /summon slavicmyths:sand_king ~ ~ ~

Full game validation is pending. Arena generation, rewards, weapons and armor are separate milestones.
