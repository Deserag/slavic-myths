# Cleanup

File | Decision | Evidence / rollback
--- | --- | ---
item/LoreBookItem.java unused LoreNetwork import | DELETE import only | real use goes to TaleService, class/protocol retained; git diff restores
client/LoreScreen.java | KEEP | registered legacy packet handler
progression/Knowledge.java | KEEP | stable vanilla advancement IDs and mask ordering
registry/ModItems.java,ModBlocks.java | KEEP | saved-world IDs; splitting merely for file size has no demonstrated runtime benefit
all OGG/PNG/structures/generators/docs evidence | KEEP | resource contracts/historical receipts; not production-JAR ballast

No production file deleted, no mass cleanup, no art or progression migration.
