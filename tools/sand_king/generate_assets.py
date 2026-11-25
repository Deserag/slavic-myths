"""Generate missing assets in Gradle's build directory, never overwrite source assets."""
import os
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / "src/main/resources"
OUT = Path(os.environ["SAND_KING_RESOURCE_ROOT"]).resolve()
GEOMETRY = Path("assets/slavicmyths/sand_king/geometry.bin.gz")
SOUNDS = Path("assets/slavicmyths/sounds/desert/sand_king")
NAMES = ("ambient", "alert", "hurt", "death", "step", "sting", "poison", "claw", "jump", "land", "slam_charge", "slam", "rage")
required = [GEOMETRY] + [SOUNDS / (name + ".ogg") for name in NAMES]
if OUT == SOURCE or ROOT not in OUT.parents or "build" not in OUT.relative_to(ROOT).parts:
    raise RuntimeError(f"Refusing to clear a non-build directory: {OUT}")
if OUT.exists():
    shutil.rmtree(OUT)
OUT.mkdir(parents=True)
env = dict(os.environ, SAND_KING_RESOURCE_ROOT=str(OUT))
def run(name):
    subprocess.run([sys.executable, str(Path(__file__).with_name(name))], cwd=ROOT, env=env, check=True)
try:
    if not (SOURCE / GEOMETRY).is_file():
        run("export_geometry.py")
    if any(not (SOURCE / p).is_file() for p in required[1:]):
        run("make_sounds.py")
    for p in required:
        generated = OUT / p
        source = SOURCE / p
        if source.is_file() and generated.is_file():
            generated.unlink()
        actual = source if source.is_file() else generated
        if not actual.is_file() or actual.stat().st_size == 0:
            raise RuntimeError(f"Missing asset: {p}")
        with actual.open("rb") as f:
            magic = f.read(4)
        if p == GEOMETRY and magic[:2] != b"\x1f\x8b":
            raise RuntimeError(f"Invalid gzip geometry: {p}")
        if p != GEOMETRY and magic != b"OggS":
            raise RuntimeError(f"Invalid OGG sound: {p}")
except subprocess.CalledProcessError as exc:
    raise SystemExit(f"Sand King asset generator failed ({exc.returncode}). Install dependencies: {sys.executable} -m pip install -r tools/sand_king/requirements.txt") from exc
print("Sand King assets verified: geometry and 13 OGG files.")
