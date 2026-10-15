import sys
from pathlib import Path
import subprocess,hashlib,json
R=Path(__file__).resolve().parents[2];O=R/'docs/implementation/performance-audit';O=O/('focused' if '--focused' in sys.argv else 'extended2' if '--extended2' in sys.argv else 'extended') if any(x in sys.argv for x in ['--extended','--extended2','--focused']) else O;raw=O/'raw';profile=raw/'profile.jfr'
subprocess.run(['C:/Program Files/Java/jdk-21.0.12/bin/java.exe',str(R/'tools/performance-audit/analysis/JfrSummary.java'),str(raw)],check=True)
(O/'jfr-summary.json').write_text(json.dumps(dict(profile='raw/profile.jfr (local, intentionally excluded)',bytes=profile.stat().st_size,sha256=hashlib.sha256(profile.read_bytes()).hexdigest(),summary='raw/jfr-summary.tsv',method='Java 21 jdk.jfr.consumer.RecordingFile streaming; samples are not exact calls, allocation weights are estimates; no retained heap claim'),indent=2))
# Only overwrite this script's own uncommitted generated export; never remove repository data.
if (raw/'jfr-export.json').exists():(raw/'jfr-export.json').write_text('{"superseded_by":"jfr-summary.tsv; streaming consumer avoids expanded JSON memory cost"}\n')
