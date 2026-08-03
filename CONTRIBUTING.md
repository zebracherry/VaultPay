# Contributing to VaultPay

VaultPay is a training target. Contributions that add **new, clearly-documented,
intentionally vulnerable** scenarios — or improve the docs/tests around existing ones — are welcome.

## Ground rules

1. **Every vulnerability must be intentional and documented.** In the code, comment the flaw with:
   - the OWASP Mobile Top 10 (2024) risk ID (e.g. `M9`),
   - the MASVS v2.1.0 control ID (e.g. `MASVS-STORAGE-1`),
   - a one-line "SECURE:" note describing the correct implementation.
2. **Update the coverage table in `README.md`** so the new scenario is discoverable.
3. **No real secrets, no real endpoints, no real PII.** Use obviously-fake placeholder values.
4. **Keep it emulator-safe.** Nothing that could harm a host device or reach a real network service.

## Adding a scenario

- Put the code in the relevant package (`crypto/`, `data/`, `auth/`, `net/`, `web/`, `ui/`).
- Add a row to the README coverage table and, if it introduces a new test technique, a note to the
  testing workflow section.
- Reference the matching MASTG test case (`MASTG-TEST-…`) where one exists.

## What we won't merge

- Undocumented or accidental vulnerabilities.
- Anything that weaponises the app against third parties or real infrastructure.
- Real credentials, tokens, or keys of any kind.
