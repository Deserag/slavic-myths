#!/usr/bin/env python3
"""For older local scripts: preview flat 2D sisters artifacts rather than obsolete 3D geometry."""
from pathlib import Path
from generate_sister_artifacts_2d import produce,preview
root=Path(__file__).resolve().parents[1]
produce(root,check=True)
preview(root)
