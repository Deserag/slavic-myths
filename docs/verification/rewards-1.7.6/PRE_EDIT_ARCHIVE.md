# Historical pre-edit source archive

The full pre-1.7.6 source copy is preserved in Git, rather than duplicated in the release checkout. Reports and actual test/design evidence remain here. Current production sources and active tests do not load this directory.

Source commit: `ba098d0da6b7c0658b9d0cc001ef236ac1d064d5`. Tree: `b772eedd6c9b7c41775712a28cc869b1c93e6aae`. Files: 6646.

Recover without modifying the current checkout:

```powershell
git archive --format=zip -o rewards-pre-edit.zip ba098d0da6b7c0658b9d0cc001ef236ac1d064d5 docs/verification/rewards-1.7.6/pre-edit
```

The separate local verified archive and SHA256 are recorded in [cleanup metadata](../../implementation/cleanup-2.0-20261024/archived-snapshot.json). No Git history was rewritten.
