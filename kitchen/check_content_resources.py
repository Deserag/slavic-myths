#!/usr/bin/env python3
"""Static content audit. Run from repository root: python kitchen/check_content_resources.py."""
import json
import re
import sys
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/slavicmyths"
DATA = ROOT / "src/main/resources/data/slavicmyths"
FAILURES = []
WARNINGS = []

def report(kind, path, message):
    (FAILURES if kind == "ERROR" else WARNINGS).append(f"{kind}: {path}: {message}")

def read_json(path):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, ValueError) as exc:
        report("ERROR", path.relative_to(ROOT), f"invalid JSON: {exc}")
        return None

def main():
    files = sorted((ROOT / "src/main/resources").rglob("*.json"))
    for path in files:
        read_json(path)
    langs = {}
    for name in ("en_us", "ru_ru"):
        path = ASSETS / "lang" / f"{name}.json"
        value = read_json(path)
        if not isinstance(value, dict):
            report("ERROR", path.relative_to(ROOT), "language file must contain a JSON object")
            continue
        if any(not isinstance(k, str) or not isinstance(v, str) for k, v in value.items()):
            report("ERROR", path.relative_to(ROOT), "language keys and values must be strings")
        langs[name] = value
    if len(langs) == 2:
        missing = sorted(set(langs["en_us"]) - set(langs["ru_ru"]))
        extra = sorted(set(langs["ru_ru"]) - set(langs["en_us"]))
        report("WARNING", "lang/ru_ru.json", f"{len(missing)} untranslated keys; sample: {missing[:15]}") if missing else None
        report("WARNING", "lang/ru_ru.json", f"{len(extra)} keys absent in en_us; sample: {extra[:15]}") if extra else None
        placeholders = re.compile(r"%(?:\\d+\\$)?[-#+ 0,(]*\\d*(?:\\.\\d+)?[a-zA-Z]|%s")
        for key in set(langs["ru_ru"]) & set(langs["en_us"]):
            base = Counter(placeholders.findall(langs["en_us"][key]))
            translated = Counter(placeholders.findall(langs["ru_ru"][key]))
            if base != translated:
                report("ERROR", "lang/ru_ru.json", f"placeholder mismatch for {key}: {base} vs {translated}")
    for path in sorted((ASSETS / "models").rglob("*.json")) if (ASSETS / "models").exists() else []:
        obj = read_json(path)
        if not isinstance(obj, dict):
            continue
        refs = []
        if isinstance(obj.get("parent"), str):
            refs.append(obj["parent"])
        textures = obj.get("textures", {})
        if isinstance(textures, dict):
            refs.extend(v for v in textures.values() if isinstance(v, str) and not v.startswith("#"))
        for ref in refs:
            if ":" not in ref:
                continue
            ns, target = ref.split(":", 1)
            if ns != "slavicmyths":
                continue
            if target.startswith("item/") or target.startswith("block/"):
                texture = ASSETS / "textures" / f"{target}.png"
                model = ASSETS / "models" / f"{target}.json"
                if not texture.is_file() and not model.is_file():
                    report("WARNING", path.relative_to(ROOT), f"unresolved local reference {ref}")
    for line in FAILURES + WARNINGS:
        print(line)
    print(f"JSON files scanned: {len(files)}; errors: {len(FAILURES)}; warnings: {len(WARNINGS)}")
    return 1 if FAILURES else 0

if __name__ == "__main__":
    sys.exit(main())
