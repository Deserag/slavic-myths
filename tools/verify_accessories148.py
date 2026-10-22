"""Current full-package gate. Initial partial-stage verifier is archived in docs."""
from pathlib import Path
import runpy
runpy.run_path(str(Path(__file__).with_name('verify_accessories148_full.py')),run_name='__main__')
