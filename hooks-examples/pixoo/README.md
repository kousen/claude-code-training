# Pixoo64 Hooks: Ambient Display for Claude Code

This example wires a Divoom Pixoo64 display to Claude Code lifecycle events:

- **Yellow + "INPUT?"** when Claude is blocked waiting for your permission to use a tool.
- **Green + "DONE"** (3-second flash) when Claude finishes responding, then auto-reverts to your weather/Faces channel.

It is a clean illustration of the `Notification` and `Stop` hook events — both lifecycle-reaction hooks where the model has no involvement. The Claude Code harness fires shell commands; the shell commands hit a local HTTP API that talks to the device over your LAN.

## What you'll need

- A **Divoom Pixoo64** on the same LAN as your computer (you'll need its IP address).
- **Docker Desktop** running.
- **Claude Code** installed.
- A few minutes.

## Step 1 — Run the Pixoo REST server

The display speaks Divoom's proprietary HTTP protocol. We use the open-source `pixoo-rest` Flask wrapper, which exposes a clean REST API around it.

```bash
git clone https://github.com/4ch1m/pixoo-rest.git
cd pixoo-rest
cp .env.example .env
```

Edit `.env` and set at minimum:

- `PIXOO_HOST` — your Pixoo64's local IP address (find it in the Divoom app: Device Settings → Device Info).
- `PORT` — the host port the API will listen on. Default is `5000`, but on macOS Monterey or later, port 5000 is often taken by AirPlay Receiver. Use `5001` to avoid the conflict:

```
PORT=5001
```

Then start it detached so the terminal is free:

```bash
docker compose up -d
```

Verify:

```bash
docker ps --filter name=pixoo-rest
curl -s -m 2 -o /dev/null -w "API HTTP %{http_code}\n" http://localhost:5001/apidocs/
```

You want `HTTP 200`. The container's `restart: unless-stopped` policy means it survives reboots as long as Docker Desktop launches at login.

## Step 2 — Install the hook scripts

From this directory:

```bash
mkdir -p ~/.claude/hooks
cp pixoo-waiting.sh pixoo-done.sh ~/.claude/hooks/
chmod +x ~/.claude/hooks/pixoo-waiting.sh ~/.claude/hooks/pixoo-done.sh
```

Smoke-test them directly (the device should flash yellow, then green):

```bash
bash ~/.claude/hooks/pixoo-waiting.sh
sleep 2
bash ~/.claude/hooks/pixoo-done.sh
```

If your REST server uses a port other than `5001`, export `PIXOO_REST` before running, or edit the default at the top of each script:

```bash
PIXOO_REST=http://localhost:5000 bash ~/.claude/hooks/pixoo-done.sh
```

## Step 3 — Merge the hook config

Open `~/.claude/settings.json` and merge the contents of `settings-snippet.json` into the `hooks` object. If you already have other hooks registered, **add** these entries — don't replace what's there.

The shape Claude Code expects for each event is an array of `{ matcher, hooks: [...] }` blocks. Adding a second block for an event coexists with existing blocks — both fire when their matchers match.

After saving, restart Claude Code (or start a new session) so it picks up the changes.

## Step 4 — Try it live

1. Open Claude Code in a project where permissions are *not* set to `bypassPermissions` — you need real permission prompts for the demo. The simplest way is to run in Manual mode:

   ```bash
   claude --permission-mode=manual
   ```

2. Ask Claude to do something that requires a tool call (e.g., "list the files here"). When Claude asks for permission, the Pixoo lights up yellow with "INPUT?".

3. Approve the action. Claude responds. As soon as the response finishes, the Pixoo flashes green with "DONE", then 3 seconds later returns to your weather face.

## Design notes (worth covering in class)

Several decisions in these hooks deserve explanation, because each one teaches a generalizable pattern.

### Why `permission_prompt` only — not `idle_prompt`

The `Notification` event has several matcher values. Two looked relevant:

- `permission_prompt` — Claude is *blocked* waiting for you to approve a tool call.
- `idle_prompt` — Claude finished a response and you've been quiet for a while.

The first iteration of this example matched both. The result was unpleasant: the Pixoo would spontaneously go yellow during any conversational pause, with no clear signal as to why, and no way to clear it short of typing something. The hook had no auto-revert, because for a real permission prompt you *want* the indicator to stay until you respond.

**Lesson: matcher patterns should reflect semantic intent, not just "events that sound similar."** `permission_prompt` means "I am stuck, fix me." `idle_prompt` means "I am bored." Treating them identically created a hook that lied about Claude's state.

### Why `Stop` fires every turn (and the 3-second revert)

`Stop` is per-turn, not per-session. Every Claude response triggers it. There is no separate "end of conversation" event; if you want one, you build it yourself (e.g., debounce on top of `Stop`).

For an ambient indicator, this is fine — *if* the visual is brief. A 30-second sustained green would dominate the screen during a back-and-forth conversation. 3 seconds is long enough to register, short enough to disappear before the next response.

### Why `push_immediately=false` then `push_immediately=true`

The Pixoo REST API exposes a `push_immediately` form parameter on each draw call. Setting it to `false` on the fill and `true` on the text batches both operations into a single display update. Without this, you would see a flash of yellow with no text before the text appears — a small but noticeable seam.

**Lesson:** when a device API supports batched commits, use them. The user-visible quality difference is large compared to the implementation cost.

### Why `nohup ... & disown` for the auto-revert

`pixoo-done.sh` schedules a `sleep 3 && curl ...` to run in the background, then exits. A naive `( sleep 3 && curl ... ) &` works most of the time, but if Claude Code kills the hook's process group on timeout, the background subshell can die with it. `nohup` plus `disown` detaches it from the hook's process group entirely, so the revert fires reliably even if the parent is killed.

**Lesson:** when a hook spawns work that needs to survive the hook's exit, detach explicitly. Plain `&` is not enough.

### What happens if the REST server is down

Each `curl` uses `-m 3` (3-second max time) and pipes output to `/dev/null`. The scripts unconditionally `exit 0`. If the REST server is unreachable:

- On `localhost`, the kernel rejects the connection in milliseconds — there is no wait.
- Both `curl` calls fail silently.
- The script exits 0 in under 20 milliseconds.
- Claude Code sees a clean, fast hook execution. Nothing surfaces, nothing blocks.

**Lesson:** ambient-display hooks should fail invisibly. They are decorative, not load-bearing. A failure to light up a device is not an error worth interrupting the workflow over.

## Troubleshooting

| Symptom | First thing to check |
|---|---|
| Pixoo doesn't react to anything | `docker ps` — is the container running? |
| API returns non-200 from `/apidocs/` | The REST server is up but the Pixoo IP in `.env` may be wrong — check the Divoom app. |
| Yellow "INPUT?" appears but no green "DONE" | `Stop` hook may not be registered; re-check `settings.json`. |
| Permission prompts never trigger yellow | You're probably in `bypassPermissions` mode — try `claude --permission-mode=manual`. |
| Pixoo stays stuck on green | The 3s revert process was killed. Run `curl -X PUT http://localhost:5001/channel/0` to clear, then check for `nohup`/`disown` in your `pixoo-done.sh`. |

## Files in this directory

- `pixoo-waiting.sh` — runs on `permission_prompt` notifications.
- `pixoo-done.sh` — runs on `Stop` events; auto-reverts after 3 seconds.
- `settings-snippet.json` — the minimal `hooks` block to merge into `~/.claude/settings.json`.
- `README.md` — this file.

## Where this fits in the wider hooks landscape

This example demonstrates the **lifecycle-reaction** category of hooks: harness events trigger purely-decorative side effects with no model involvement. Compare with:

- **Validation hooks** (e.g., `check-coverage.sh`) — `PreToolUse` hooks that inspect tool arguments and can block.
- **Auto-formatting hooks** (e.g., `pre-edit-formatter.sh`) — `PostToolUse` hooks that fix files after the model writes them.
- **External-sync hooks** (e.g., `task-to-linear.sh`) — `TaskCreated` hooks that mirror Claude's state to another system (needs `CLAUDE_CODE_ENABLE_TODO_TOOLS=1` on current models).

All four categories use the same configuration shape; the difference is in the script body and the event chosen.
