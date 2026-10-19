"""#47 shared guarded installation, scoped #56 evidence; never launch a client."""
from pathlib import Path
from install_audio_test_preview_61 import main
if __name__=='__main__':
 main(build_kind='visual',gate_paths=['docs/implementation/tugarin-56/gates-19718.json'],receipt_dir=Path(__file__).resolve().parents[1]/'docs/implementation/tugarin-56',authorization='https://github.com/Deserag/slavic-myths/issues/56')
