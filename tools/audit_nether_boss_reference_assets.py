#!/usr/bin/env python3
"""Read-only SHA audit of approved Nether concept originals (no downloads, no substitutes).

Usage (from the repository root):
  python tools/audit_nether_boss_reference_assets.py
  python tools/audit_nether_boss_reference_assets.py --root . --root "C:\Users\user\Downloads"
  python tools/audit_nether_boss_reference_assets.py --json report.json
Only actual readable files count as FOUND; metadata/Markdown/SVG never do.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import sys
import zipfile

EXPECTED = {
    "lost_halls/blocks.png": ("ff3b278772fe276a464c6a5c0da7592552a1e921e016e6c075ffe20cce01e781", "1448x1086", "#289/#290"),
    "lost_halls/scenes.png": ("d0d95085857c930a24d6ed6fc67729ae927a5103d77b536a1387ff8db79fe4b2", "1448x1086", "#291/#294"),
    "lost_halls/mobs.png": ("a330f127e60c4055dd86aadb91f169e337f4e60c9c90ad8e7982b49cac4be042", "1672x941", "#292"),
    "lost_halls/rewards.png": ("0eb92eea714205ca75219af6aa9f9f1e586dae30494b6534ad28e412daff9856", "1536x1024", "#301"),
    "lost_halls/posten.png": ("34e30e8cde56dffa7ab8e2ff0bf917da3ebdd808675e02da5cfeb123502d65d0", "1536x1024", "#293"),
    "black_cinder/landscape.png": ("09a92d889d1dcabc2821d872e2287e6400f00bf01baf8674d32323d3bdb3bce8", "1672x941", "#261"),
    "black_cinder/blocks_REVISED.png": ("8697b9bd400c7790065bfa243db65d02d7468d19f873b9929a09ba54e47c5e0a", "1448x1086", "#263"),
    "black_cinder/mobs.png": ("0210bc96b2b4ff72b8ded59a5a0a2b1ba75f0e980974ec14a86f2ad090b29a81", "1448x1086", "#265"),
    "black_cinder/zharenitsa.png": ("ad805df5f2c2c3e2ee3e2ab0ff86c5103464a54c662d3c2f2e820ed96f4d874e", "1312x1199", "#266"),
    "burning_islands/biome.png": ("a770acc9cb8401baa8199994f73cf28ee1fb3d43eff25fe0fd5bdbff92fbf605", "", "#313"),
    "burning_islands/creatures.png": ("a06f8d7730e43f93003a0a9c465c806e6e28a41260e7a61640d1f336924651d1", "", "#316"),
    "burning_islands/rune_materials.png": ("47a9f11cd0e3c98e48983cf28c8b5ed7c6687689d9abb9cc5ee28edd7a47fdab", "", "#316"),
    "burning_islands/magma_lord.png": ("354c2c318c01ee67b21b60adc653a0a4747acc81786a433cb28e1c3fc26881a6", "", "#318"),
    "burning_islands/magma_head.png": ("287ce3f15e28b1a646cf31de1e51fab85519bb03dc935d83bb7aea920395bc93", "", "#321"),
}
ARCHIVES = {
    "Чертоги_заблудших_2.0.4_жёсткое_ТЗ_референсы.zip": (8837561, ""),
    "Чертоги_заблудших_2.0.4.4_референсы_для_Codex.zip": (4629422, ""),
    "Чертоги_2.0.4_награды_утверждённые_референсы.zip": (3241149, ""),
    "Постень_2.0.4.5_утверждённый_арт_и_чертежи_Codex.zip": (5106217, ""),
    "Чёрная_гарь_2.0.3_жёсткие_референсы.zip": (8686150, ""),
    "BURNING_ISLANDS_205_COMPLETE_REFERENCES_FOR_GITHUB.zip": (
        12002229, "22256bf4df4f8c8abf6c5aec2ec6a9d30c3be02b0a3d7b59c5751908bc6edada"
    ),
}
IGNORE_DIRS = {".git", ".gradle", ".idea", ".venv", "node_modules", "build", "runs", "logs", "__pycache__"}
MAX_PNG = 32 * 1024 * 1024  # refuse huge/unrelated files; reference PNG files are much smaller

def sha_bytes(data):
    return hashlib.sha256(data).hexdigest()

def sha_file(path):
    h = hashlib.sha256()
    with path.open("rb") as inp:
        for chunk in iter(lambda: inp.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()

def png_dimensions(data):
    if data[:8] != b"\x89PNG\r\n\x1a\n" or len(data) < 24:
        return None
    return f"{int.from_bytes(data[16:20], 'big')}x{int.from_bytes(data[20:24], 'big')}"

def scan(roots):
    by_sha = {value[0]: key for key, value in EXPECTED.items()}
    found = {key: [] for key in EXPECTED}
    archives = {}
    errors = []
    seen = set()
    for root in roots:
        if not root.exists():
            continue
        for directory, dirs, filenames in os.walk(root, followlinks=False):
            dirs[:] = [d for d in dirs if d not in IGNORE_DIRS and not (Path(directory) / d).is_symlink()]
            for filename in filenames:
                path = Path(directory) / filename
                # Never trust filename alone: validate every recognized ZIP and every readable PNG.
                is_zip = filename in ARCHIVES or (
                    filename.lower().endswith(".zip") and
                    ("референс" in filename.lower() or "reference" in filename.lower())
                )
                is_png = filename.lower().endswith(".png")
                if not (is_png or is_zip):
                    continue
                try:
                    resolved = path.resolve()
                    if resolved in seen or not path.is_file():
                        continue
                    seen.add(resolved)
                    size = path.stat().st_size
                    if is_png and size <= MAX_PNG:
                        data = path.read_bytes()
                        digest = sha_bytes(data)
                        if digest in by_sha:
                            key = by_sha[digest]
                            found[key].append({"source": str(path), "size": size,
                                               "dimensions": png_dimensions(data)})
                    elif is_zip:
                        digest = sha_file(path) if filename in ARCHIVES and ARCHIVES[filename][1] else ""
                        archive_info = {"source": str(path), "size": size}
                        if filename in ARCHIVES:
                            expected_size, expected_hash = ARCHIVES[filename]
                            archive_info["size_matches"] = size == expected_size
                            if expected_hash:
                                archive_info["sha256_matches"] = digest == expected_hash
                        try:
                            with zipfile.ZipFile(path) as z:
                                for item in z.infolist():
                                    if item.is_dir() or not item.filename.lower().endswith(".png"):
                                        continue
                                    if item.file_size > MAX_PNG:
                                        errors.append(f"oversize member: {path}!{item.filename}")
                                        continue
                                    data = z.read(item)
                                    checked = sha_bytes(data)
                                    if checked in by_sha:
                                        key = by_sha[checked]
                                        found[key].append({"source": str(path) + "!" + item.filename,
                                                           "size": len(data), "dimensions": png_dimensions(data)})
                        except (OSError, zipfile.BadZipFile, RuntimeError, ValueError) as exc:
                            errors.append(f"invalid archive: {path}: {exc}")
                        archives.setdefault(filename, []).append(archive_info)
                except (OSError, PermissionError, ValueError, MemoryError) as exc:
                    errors.append(f"unreadable: {path}: {exc}")
    results = {}
    for key, (sha, dimensions, issue) in EXPECTED.items():
        matches = found[key]
        valid = [m for m in matches if not dimensions or m["dimensions"] == dimensions]
        results[key] = {
            "status": "FOUND" if valid else ("DIMENSION_MISMATCH" if matches else "MISSING"),
            "expected_sha256": sha, "expected_dimensions": dimensions or None,
            "issue": issue, "matches": matches,
        }
    # Guardian last dark recolor has no verified original SHA yet.
    results["navya_grove/guardian_last_dark_recolor.png"] = {
        "status": "UNVERIFIED_SOURCE", "expected_sha256": None,
        "issue": "#256", "note": "Final approved dark recolor has no confirmed raw PNG or SHA",
        "matches": [],
    }
    return {"roots": [str(x) for x in roots], "results": results,
            "archives": archives, "errors": errors}

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", action="append", type=Path, default=None,
                        help="directory to inspect (repeatable)")
    parser.add_argument("--json", dest="json_output", type=Path, help="optional JSON report")
    args = parser.parse_args()
    if args.root:
        roots = args.root
    else:
        roots = [Path.cwd(), Path.home() / "Downloads", Path.home() / "Desktop"]
    report = scan(roots)
    for name, result in report["results"].items():
        print(f'{result["status"]:19} {name:48} {result["issue"]}')
        for match in result["matches"][:3]:
            print(f'  {match["source"]} [{match["dimensions"]}]')
    print(f'Archive candidates: {sum(len(x) for x in report["archives"].values())}; '
          f'errors: {len(report["errors"])}')
    if args.json_output:
        args.json_output.parent.mkdir(parents=True, exist_ok=True)
        args.json_output.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
        print(f"JSON report: {args.json_output}")
    # Nonzero exit if any original is not verified; 2 is an expected BLOCKED gate.
    return 0 if all(r["status"] == "FOUND" for k, r in report["results"].items()
                    if not k.startswith("navya_grove/")) else 2

if __name__ == "__main__":
    sys.exit(main())
