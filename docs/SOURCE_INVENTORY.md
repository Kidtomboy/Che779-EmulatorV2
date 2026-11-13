# Source Inventory and Baseline Integrity

## Input archive

- `Che779_EmulatorV2_0.0.0.2_custom(1).zip`
- SHA-256: `1830f850004ac9a5a241e6868954a6376e6bad6c337d71c9774e863e3933af47`
- 411 ZIP entries; archive CRC test passed.

## Current source inventory

- 320 Java files under `emulator-src/src/`.
- 3 JNA compile-time stub Java files.
- 14 resource files.
- 1 bundled dependency archive: ASM 3.2.
- 1 supplied baseline runnable JAR.

## JAR comparison

- Baseline JAR: 655 non-directory entries.
- Rebuilt JAR: 655 non-directory entries.
- Missing entries: 0.
- Additional entries: 0.
- Changed entries: exactly four: `com/che779/emu/IpcServer.class` and its three anonymous inner-class files. These differences are expected from the IPC hardening in this revision.
- `IpcServer.class` class-file major version: 52 (Java 8).

This comparison confirms that the new archive retains the same entry set and that content changes are limited to IPC classes; it is not a proof of full application equivalence or all runtime behavior.
