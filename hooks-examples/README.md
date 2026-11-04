# Hook Examples

This directory contains advanced hook patterns for Claude Code workflow automation.

## What Are Hooks?

**Hooks** are programmable callbacks that execute in response to Claude Code events. The vocabulary expanded substantially in 2026.

### Event types (verified against Claude Code 2.1.318, Nov 2026)

- **SessionStart / SessionEnd** — wraps the whole session
- **PreToolUse / PostToolUse** — wraps individual tool calls (PreToolUse can modify tool inputs or block). Since 2.1.300 a rewritten `updatedInput` is validated like Claude's own input: unknown keys are dropped, and input the tool rejects denies the call
- **PostToolUseFailure** — fires when a tool call errored; useful for surfacing diagnostics or auto-recovering
- **UserPromptSubmit** — pre-process the user's prompt before Claude sees it. Since 2.1.299 it runs before a skill or custom slash command expands, so blocking the prompt also stops the skill's embedded shell commands
- **TaskCreated** — fires when Claude creates a task via TaskCreate. ⚠️ Current default models don't use the task tools (removed 2.1.233); export `CLAUDE_CODE_ENABLE_TODO_TOOLS=1` or this hook never fires
- **DirectoryAdded** — fires after `/add-dir` registers a new working directory mid-session
- **PreModelSwitch / PostModelSwitch** — fire around a `/model` change; `PreModelSwitch` can block, confirm, or annotate the switch (e.g. refuse an expensive model on a cost-capped project)
- **SubagentStart** — fires when a subagent launches (`Explore`, `Plan`, custom agents); useful for resource budgeting and audit
- **Notification** — background agents send `agent_needs_input` / `agent_completed` payloads; pairs well with the `pixoo/` examples

### Hook types (beyond plain shell commands)

A hook is no longer just a shell command. The supported types are:

- `command` — run a shell command (the original form)
- `http` — POST a JSON payload to a URL endpoint
- `prompt` — ask Claude itself for a yes/no decision against a prompt template
- `mcp_tool` — invoke a specific tool on a configured MCP server
- `agent` — spawn a subagent to verify a condition and report back

`prompt` and `agent` are useful when the decision is too nuanced for a regex or a shell exit code. One restriction: `agent`-type hooks don't run on `PermissionRequest`, since their answer could never allow or deny the request — use `command` or `http` there (2.1.280).

### Failure policy: fail open or fail closed?

By default a hook that can't start, times out, or exits with an unexpected code is ignored and the action proceeds (fail-open). Since 2.1.295, `command` and `http` hooks accept `"onFailure": "block"` to fail closed instead — the action is blocked whenever the hook can't deliver a verdict. Use it on hooks that guard something you can't undo.

## Hook Configuration

Hooks are configured in `~/.claude/settings.json` or `.claude/settings.json`. The current array form supports `if` matchers (permission-rule syntax) and `type` selection:

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "type": "command",
        "command": "~/.claude/hooks/security-validator.sh",
        "if": "Write(**) | Edit(**)"
      }
    ],
    "PostToolUseFailure": [
      {
        "type": "http",
        "url": "https://hooks.example.com/claude-failures",
        "if": "Bash(**)"
      }
    ],
    "TaskCreated": [
      {
        "type": "command",
        "command": "~/.claude/hooks/task-to-linear.sh"
      }
    ],
    "SubagentStart": [
      {
        "type": "agent",
        "agent": "team-budget-watcher"
      }
    ],
    "SessionEnd": [
      {
        "type": "command",
        "command": "~/.claude/hooks/session-end-summary.sh"
      }
    ]
  }
}
```

## Examples in This Directory

1. **session-end-summary.sh** — Generate session summary and statistics (`SessionEnd`, `command`)
2. **pre-edit-formatter.sh** — Auto-format files before editing (`PreToolUse`, `command`, `if: Edit(**)`)
3. **security-validator.sh** — Validate security before file writes (`PreToolUse`, `command`, `if: Write(**)`)
4. **git-auto-backup.sh** — Create git stash before major operations (`PreToolUse`, `command`)
5. **test-runner-hook.sh** — Run tests before certain operations (`PreToolUse`, `command`)
6. **dependency-checker.sh** — Check for dependency updates (`SessionStart`, `command`)
7. **task-to-linear.sh** — Mirror Claude's TaskCreate to a Linear issue (`TaskCreated`, `command`/`http`). Requires `CLAUDE_CODE_ENABLE_TODO_TOOLS=1` on current models
8. **post-failure-reporter.sh** *(new)* — Capture a failed tool call's context for triage (`PostToolUseFailure`, `command`)
9. **subagent-budget-check.sh** *(new)* — Block a subagent from launching if the team token budget is exhausted (`SubagentStart`, `command`)
10. **pixoo/** *(new)* — Drive a Divoom Pixoo64 ambient display from Claude Code lifecycle events: yellow "INPUT?" on permission prompts, green "DONE" flash when Claude finishes a response (`Notification`/`Stop`, `command`). Requires a Pixoo64 on your LAN and the [`pixoo-rest`](https://github.com/4ch1m/pixoo-rest) Docker server. Full setup walkthrough in `pixoo/README.md`.

## Hook Best Practices

1. **Exit codes matter**: Return 0 to allow operation, non-zero to block
2. **Feedback is user input**: Hook output appears as if from the user
3. **Be fast**: Hooks run synchronously; slow hooks delay operations
4. **Error handling**: Provide clear error messages when blocking
5. **Idempotent**: Hooks should be safe to run multiple times

## Using These Examples

Copy hook scripts to your `.claude/hooks/` directory and configure in settings:

```bash
# Copy example hooks
cp session-end-summary.sh ~/.claude/hooks/
chmod +x ~/.claude/hooks/session-end-summary.sh

# Configure in ~/.claude/settings.json
{
  "hooks": {
    "sessionEnd": "~/.claude/hooks/session-end-summary.sh"
  }
}
```

## Lab Exercises

Students will:
1. Configure a SessionEnd hook to generate summaries
2. Create a PreToolUse hook for security validation
3. Build a custom hook for their workflow
4. Understand hook feedback and blocking

## Resources

- [Claude Code Hooks Documentation](https://docs.claude.com/en/docs/claude-code/overview)
- Hook examples from changelog (v1.0.85+, v2.0.10+)
