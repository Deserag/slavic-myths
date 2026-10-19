# #62 / #66: comparison of installed 1.9.7.10 assets

Status: IN_PROGRESS / OWNER_PENDING. Runtime NOT_RUN; hearing NOT_LISTENED.
This extends the installed production source 8bf39d78 without changing audio.

| REQUIREMENT | EVIDENCE | PASS/FAIL/BLOCKED/NOT_RUN |
|---|---|---|
| Include all current production recordings | PROJECT_SIMILARITY_METHOD.json: 1288 files, 828828 total pairs, 375052 duration-eligible pairs | PASS |
| Detect candidate similarity, including bandit additions | PROJECT_SIMILARITY.csv: 424 spectral candidates, 299 high waveform matches | PASS |
| Separate current use from retained originals | SIMILARITY_TRIAGE.csv: 51 inactive original pairs, 230 positively identified physical-prop pairs, 18 active legacy/mixed pairs | PASS |
| Do not classify an unknown vocal bank as physical reuse | tools/audio_similarity_triage_61.py uses an explicit material-bank whitelist; cat, unknown and mixed voice/wood examples remain outside it | PASS |
| Resolve active legacy/mixed candidates by semantic and auditory review | 18 pairs remain; notably Ilya ambient/retreat breath and Finist inspire/mark/summon/victory | BLOCKED |
| Owner hearing and gameplay acceptance | No client or launcher started | NOT_RUN |

The additional active mixed pair is nav_impact / bandit_mace_impact. Both
have contact roles; the latter uses a curated recorded physical source, while
the former is legacy. This is a review candidate, not evidence of cloned voices
or permission to replace an unrelated sound automatically. The earlier
nav_impact / poloz_weakspot_hit pair remains for the same review.

Numerical correlation cannot decide whether a heroic signal is appropriate,
whether a voice sounds threatening, or whether a cue is timed correctly in game.
Material reuse is therefore a triage category, not an owner acceptance result.
