# 09 — RELEASE BRANCH / ARTIFACT RULES

Continue the already-established RC workflow.

Do not create another unrelated release architecture.

---

# 1. BRANCH

Use current release/playtest branch if already created.

Expected:
`release/0.9.10-playtest`

If currently working branch differs:
inspect before changing.
Do not lose uncommitted work.

---

# 2. VERSION

If RC2 is current:
keep:
`0.9.10-rc2`

Do not downgrade to RC1.

If project already incremented beyond RC2:
do not rewrite backwards.

---

# 3. BUILD ARTIFACT

After all P0 fixes:

produce normal playable JAR.

Do not present:
sources/dev jar.

---

# 4. RELEASE DOCS

Update as appropriate:
- changelog;
- known issues;
- playtest README;
- checksum.

Mention only real completed fixes.

---

# 5. KNOWN ISSUES

Bandit/Solovey overhaul is currently deferred by explicit user decision.

Do not claim it was fixed in this UI/model pass.

---

# 6. NO FORCE PUSH

Normal commit/push only if remote auth is already configured.

Do not fake publication success.
