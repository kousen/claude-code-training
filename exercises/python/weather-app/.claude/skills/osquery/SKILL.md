---
name: osquery
description: Answer questions about this machine's health using osqueryi. Use when the user asks "why is my computer slow", "what's using my CPU", "what's using memory", "fan is running hot", "is my Mac overheating", "what processes are running", "what's my IP", or similar system-diagnostic questions.
allowed-tools: Bash
---

# osquery

Answer system questions by querying osquery's SQL tables, then explain the result in plain English.

## How to run

Always use `--json` so the output is structured:

```bash
osqueryi --json "<SQL>"
```

Sizes come back as strings of bytes; load averages and temperatures as strings of floats. Convert before presenting (e.g. `resident_size / 1024 / 1024` → MB). Prefer `LIMIT` — never dump the full `processes` table.

Tables differ by OS. The examples below are verified on macOS (osquery 5.x); on Linux, `memory_info` replaces `virtual_memory_info` and `fan_speed_sensors`/`temperature_sensors` may be empty.

## Natural language → SQL

**"What's using my CPU?" / "Why is my computer slow?"**
```sql
SELECT pid, name, (user_time + system_time) AS cpu_time
FROM processes ORDER BY cpu_time DESC LIMIT 10;
```
`cpu_time` is cumulative since the process started, so a long-running app can top the list without being busy right now. Pair with load average to judge current pressure:
```sql
SELECT period, average FROM load_average;
```
A 1m average above the CPU core count means the machine is currently saturated.

**"What's using memory?"**
```sql
SELECT p.pid, p.name, p.resident_size, u.username
FROM processes p JOIN users u ON p.uid = u.uid
ORDER BY p.resident_size DESC LIMIT 10;
```
For overall pressure (macOS; values are in pages, usually 16 KB on Apple silicon, 4 KB on Intel):
```sql
SELECT free, active, inactive, wired, compressed FROM virtual_memory_info;
```
High `compressed` relative to `free` is the classic "under memory pressure" signal.

**"Fan is running hot" / "Is my Mac overheating?"**
```sql
SELECT fan, actual, min, max FROM fan_speed_sensors;
SELECT name, celsius FROM temperature_sensors ORDER BY celsius DESC LIMIT 5;
```
Fan `actual` near `max` means the machine is working hard. Follow up with the CPU query to name the culprit.

**"What's my IP?" / "What network am I on?"**
```sql
SELECT interface, address FROM interface_addresses
WHERE address NOT LIKE 'fe80%' AND address NOT IN ('127.0.0.1', '::1');
```
`en0` is usually Wi-Fi on a Mac laptop; `fe80` addresses are link-local noise.

**"What machine is this?"**
```sql
SELECT hostname, hardware_model, cpu_brand, physical_memory FROM system_info;
SELECT days, hours, minutes FROM uptime;
```
Long uptime plus memory pressure often means "reboot" is the honest answer.

**"What processes are running?"**
```sql
SELECT COUNT(*) AS running FROM processes;
SELECT pid, name, path FROM processes WHERE name LIKE '%<app>%';
```

## Interpreting results

Lead with the answer, not the table: "PyCharm is using 3.9 GB and Chrome renderers another 1.6 GB; free memory is low and 1.3 GB is compressed, so the machine is under memory pressure." Then name the top offenders with human units, and suggest the obvious action (quit X, reboot) only when the data supports it. If a query returns `[]`, say the table has no data on this OS rather than guessing.
