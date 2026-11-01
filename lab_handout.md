# Claude Code Training Labs

This document contains hands-on exercises for learning to use Claude Code for professional development workflows.

## Table of Contents

0. [Lab 0: Project Creation from Scratch](#lab-0-project-creation-from-scratch)
1. [Lab 1: First Steps with Claude Code](#lab-1-first-steps-with-claude-code)
2. [Lab 2: Code Exploration](#lab-2-code-exploration)
3. [Lab 3: Test Generation](#lab-3-test-generation)
4. [Lab 4: Documentation Creation](#lab-4-documentation-creation)
5. [Lab 5: Legacy Code Refactoring](#lab-5-legacy-code-refactoring)
6. [Lab 6: Advanced Workflows](#lab-6-advanced-workflows)

## Prerequisites

- Claude Code installed (`curl -fsSL https://claude.ai/install.sh | bash`, or Homebrew / WinGet — see slides)
- An active subscription (Pro / Max / Team / Enterprise) **or** API credits via the Claude Console
- Git repository access
- Development environment for multiple languages (Python, JavaScript/TypeScript, Java)

### Permission modes (background)

You'll meet a few permission modes during the labs:

- **Manual** — Claude prompts before risky actions. Good for learning. **Since 2.1.284 a fresh install starts in Auto, not Manual.** For these labs, start with `claude --permission-mode manual` (or set `permissions.defaultMode: "manual"` in `~/.claude/settings.json`) so you see the prompts the labs describe.
- **Auto-Accept** (`Shift+Tab`) — blanket approval of write actions. Use when you trust the next few steps.
- **Auto Mode** (the default since 2.1.284) — a background safety classifier decides per action. Modern recommended escape hatch (replaces `--dangerously-skip-permissions`, which still works but is now legacy).
- **`/sandbox`** — sandboxed shell for risky commands. **Note:** `/sandbox` requires WSL 2 — not available on native Windows or WSL 1.

When restricting tools in headless or CI workflows, prefer `--disallowedTools` over `--allowedTools`: with `--permission-mode bypassPermissions`, the allow-list does not constrain tools (re-verified on 2.1.278 — a Write ran despite `--allowedTools "Read"`). For unattended runs, newer flags are a better fit than bypass: `--permission-prompts none` auto-denies anything that would prompt (the permission mode still decides the rest), and `--restricted` removes the command-running tools and `WebFetch` outright and refuses `bypassPermissions`.

## Lab 0: Project Creation from Scratch

**Duration**: 20 minutes  
**Goal**: Create a lyrics display application from scratch (this is how the `lyrics-trainer` project began, which we'll look at later)

### Setup

1. Create a new empty directory: `mkdir lyrics-display && cd lyrics-display`
2. Initialize git: `git init`
3. Start Claude Code: `claude`
4. Create your first branch: `git checkout -b basic-lyrics-app`
5. Choose source text: prefer public-domain content for demos so copyright filters don't degrade Claude's responses. The repo ships `sonnet_18.txt` (Shakespeare) at the root — copy it as your starting point:
   ```bash
   cp ../sonnet_18.txt lyrics.txt
   ```
   You can also use any other public-domain text — Project Gutenberg has thousands of options. Avoid copyrighted song lyrics; they tend to trip content filters and make demos unreliable.

### Exercises

1. **Project foundation**:
   ```
   I have song lyrics saved in lyrics.txt. Create a web application that displays these lyrics one line at a time with three buttons: Next, Previous, and Play. The Play button should automatically advance through lines with a 2-second delay.
   ```

2. **Enhanced functionality**:
   ```
   Add line numbering, show progress (e.g., 'Line 5 of 32'), and make the Play button toggle to Pause when active. Style it to look modern and clean.
   ```

3. **Visual improvements**:
   ```
   Add smooth transitions between lines, better typography, and responsive design. Make it visually appealing with a music-themed color scheme.
   ```

4. **Advanced features**:
   ```
   Add keyboard shortcuts: spacebar for play/pause, arrow keys for next/previous. Also add a progress bar showing position in the song.
   ```

5. **Documentation and structure**:
   ```
   Create a README.md explaining the project, and organize the code into separate CSS and JavaScript files for better maintainability.
   ```

6. **Version control and experimentation**:
   ```
   Help me commit this work, then create a new branch called 'speed-control' and add a slider to control the playback speed.
   ```

### Expected Outcomes

- Build a real, useful application from nothing
- See how Claude Code handles file interactions and user interface logic
- Experience the full development cycle: concept → working app → enhancements
- Understand that this is exactly how the `lyrics-trainer` project started
- Practice iterative development with AI assistance

**Note**: This exercise recreates the origin story of the `lyrics-trainer` project in the exercises folder - you're experiencing the same process that created one of our example applications!

[← Back to Table of Contents](#table-of-contents)

## Lab 1: First Steps with Claude Code

**Duration**: 5 minutes
**Goal**: Get comfortable with the basic Claude Code interface

### Setup

1. Navigate to any existing project directory
2. Start Claude Code: `claude`
3. Verify the interface responds to basic commands

### Exercises

1. **Basic interaction**: Ask Claude Code to explain what it can do
   ```
   What are your main capabilities for helping with development?
   ```

2. **Project overview**: Get a high-level understanding
   ```
   Give me an overview of this project structure
   ```

3. **File exploration**: Find specific files
   ```
   Show me the main application files in this project
   ```

### Expected Outcomes

- Understand Claude Code's conversational interface
- See how it analyzes project structure
- Practice natural language interaction

[← Back to Table of Contents](#table-of-contents)

## Lab 2: Code Exploration

**Duration**: 10 minutes  
**Goal**: Use Claude Code to understand a complex codebase

### Setup

Choose one of the provided exercise projects:
- `exercises/java/certificate-service` (Spring Boot PDF generation)
- `exercises/java/shopping-service` (Legacy Grails/Groovy application)
- `exercises/javascript/lyrics-trainer` (TypeScript web app)
- `exercises/python/weather-app` (Flask application)

### Exercises

1. **Architecture analysis**: 
   ```
   Analyze the architecture of this application and explain the main components
   ```

2. **Technology stack identification**:
   ```
   What frameworks and libraries does this project use?
   ```

3. **Entry point discovery**:
   ```
   Show me the main entry points and how the application starts
   ```

4. **Configuration analysis**:
   ```
   Explain the configuration files and their purposes
   ```

5. **Dependency mapping**:
   ```
   Trace the flow from the main controller to the service layer
   ```

### Expected Outcomes

- Quickly understand unfamiliar codebases
- Identify key architectural patterns
- Learn to ask effective exploration questions

[← Back to Table of Contents](#table-of-contents)

## Lab 3: Test Generation

**Duration**: 15 minutes  
**Goal**: Generate comprehensive test suites with Claude Code

### Setup

Use the same project from Lab 2, focusing on service classes or core business logic.

### Exercises

1. **Unit test generation**:
   ```
   Create unit tests for the [ServiceName] class with comprehensive coverage
   ```

2. **Edge case identification**:
   ```
   What edge cases should I test for the [methodName] method? Generate tests for them
   ```

3. **Integration test creation**:
   ```
   Create integration tests for the [ControllerName] that test the full request/response cycle
   ```

4. **Mock setup**:
   ```
   Generate tests with proper mocking for external dependencies
   ```

5. **Test data generation**:
   ```
   Create realistic test data and fixtures for these test cases
   ```

### Expected Outcomes

- Understand automated test generation
- Learn about edge case identification
- Practice test-driven development with AI assistance

[← Back to Table of Contents](#table-of-contents)

## Lab 4: Documentation Creation

**Duration**: 15 minutes  
**Goal**: Generate professional documentation with Claude Code

### Setup

Continue with your chosen project, focusing on undocumented areas.

### Exercises

1. **README generation**:
   ```
   Create a comprehensive README file for this project including setup, usage, and examples
   ```

2. **API documentation**:
   ```
   Generate API documentation for all the REST endpoints in this application
   ```

3. **Code comments**:
   ```
   Add meaningful JavaDoc/docstring comments to the main service classes
   ```

4. **Architecture documentation**:
   ```
   Create an architectural overview document explaining the system design
   ```

5. **Troubleshooting guide**:
   ```
   Generate a troubleshooting guide for common issues with this application
   ```

### Expected Outcomes

- Create professional-quality documentation
- Learn different documentation formats
- Understand how to maintain documentation

[← Back to Table of Contents](#table-of-contents)

## Lab 5: Legacy Code Refactoring

**Duration**: 15 minutes  
**Goal**: Modernize legacy code using Claude Code assistance, with a first taste of skills

### Setup

Use the `exercises/java/shopping-service` (Grails/Groovy) project or any legacy code you have available. Optionally install two skills from this repo so the modernization and security work are one slash command away:

```bash
# From the project root
mkdir -p ~/.claude/skills
cp -r skills/modernize-java skills/security-review ~/.claude/skills/
```

### Exercises

1. **Code modernization** (try the skill, then a freeform prompt):
   ```
   /modernize-java
   ```
   Then, on a different file:
   ```
   Refactor this class to use modern Java features like records, switch expressions, and Optional
   ```

2. **Spring Boot updates**:
   ```
   Update this Spring configuration to use modern Spring Boot patterns and annotations
   ```

3. **Exception handling improvement**:
   ```
   Improve the error handling in this service class with proper exception types and messages
   ```

4. **Performance optimization**:
   ```
   Review this code for performance issues and suggest optimizations
   ```

5. **Security review** (try the skill — note it's read-only by `allowed-tools`):
   ```
   /security-review
   ```
   Compare with a freeform request:
   ```
   Review this controller for security vulnerabilities and suggest fixes
   ```

### Expected Outcomes

- Learn systematic refactoring approaches
- Understand modern development patterns
- Practice security-focused code review
- Experience how a skill's frontmatter (e.g., `allowed-tools: Read, Grep, Glob`) shapes its behavior

[← Back to Table of Contents](#table-of-contents)

## Lab 6: Advanced Workflows

**Duration**: ~98 minutes for Parts A–H and K–M (Parts I and J are optional)
**Goal**: Master advanced Claude Code features including Plan Mode, Skills, Plugins, Output Styles, Hooks, MCP, Agent Teams, custom subagents, dynamic workflows, headless runs, Surfaces, and Mods

> **In the live class** these parts run right after the slides they go with, not as one block at the end: Part D after hooks; Parts B, A and K after skills and subagents; Parts H and L after agent teams and workflows; Part E after MCP; Part M after the SDK.

### Setup

Navigate to the `exercises/python/weather-app` directory for this lab.

**One-time prerequisite for Part B**: install osquery (used for the skill-wrapping exercise). It's free, cross-platform, and ~30 seconds to install:

```bash
# macOS
brew install --cask osquery

# Linux (Debian/Ubuntu — see https://osquery.io/downloads for others)
curl -L https://pkg.osquery.io/deb/pubkey.gpg | sudo apt-key add -
sudo add-apt-repository 'deb [arch=amd64] https://pkg.osquery.io/deb deb main'
sudo apt-get update && sudo apt-get install osquery

# Windows
# Download the MSI from https://osquery.io/downloads
```

Verify with:
```bash
osqueryi --json "SELECT version FROM osquery_info;"
```

### Exercises

#### Part A: Plan Mode and Subagents (5 minutes)

1. **Plan Mode usage**:
   - Type `/plan` to activate Plan Mode (or cycle with `Shift+Tab`)
   ```
   Plan a comprehensive improvement to add error handling and logging to this Flask application
   ```
   - Review the plan before execution
   - Observe that Plan Mode uses the Plan subagent
   - Execute step by step or approve the entire plan

2. **Understanding Subagents and Effort Levels**:
   ```
   Explain which subagents you use and when they activate automatically
   ```
   - Learn about Explore, Plan, and General-purpose subagents
   - Understand how Claude selects the right agent for each task
   - **Effort levels**: `/effort low|medium|high|xhigh|max` controls reasoning depth
   - **Keyboard shortcuts**: `Alt+P` (or `/model`) switches models mid-conversation

#### Part B: From Skill to Plugin (18 minutes)

> **Note**: As of Claude Code 2.1, **custom slash commands have been merged into skills**. `.claude/skills/<name>/SKILL.md` is the recommended form; `.claude/commands/<name>.md` still works but is the legacy form. If a command and a skill share a name, the skill wins. Skills support frontmatter for `paths`, `allowed-tools`, `context: fork`, `disable-model-invocation`, `user-invocable`, `model`, and `effort`. Hot-reload — edits take effect immediately. Precedence: `Enterprise > Personal > Project`; plugin skills are namespaced separately.

> **The decision ladder.** Before you build anything, name what you're building:
> - **Prompt** — work happens once.
> - **Skill** — work happens repeatedly, the same way each time. A reusable operating procedure, not a saved prompt. Most installable capabilities you'll use are single skills.
> - **Plugin** — *packaging* for one or more skills. In practice, most plugins bundle a **coherent suite** from one author (Anthropic's `document-skills` plugin contains 16 skills: xlsx, pdf, docx, pptx, canvas-design, brand-guidelines, …). A single-skill plugin is also valid — usually because the distribution channel (an official marketplace) requires the plugin wrapper.
> - **MCP / hook** — orthogonal layers for live access (MCP) or deterministic checks (hooks).
>
> This part climbs the first two rungs: build a skill, then wrap it as a plugin.

3. **Explore Built-in Skills**:
   ```
   Create a quarterly weather report as an Excel spreadsheet with charts showing temperature trends
   ```
   - The xlsx skill will activate automatically
   - Observe how skills load progressively

4. **Build a skill that wraps a CLI tool — osquery** (~7 minutes):

   *Why osquery?* It's a system-diagnostics tool whose native interface is SQL against system tables (`processes`, `memory_info`, `interface_addresses`, etc.). Almost nobody memorizes the schema. A skill turns "why is my fan running?" into the right query and a plain-English answer. The same pattern works for any CLI tool with non-obvious syntax — `kubectl`, `aws`, `gh api`, `jq`, `ffmpeg`, `psql`.

   In your project, ask Claude to scaffold the skill:
   ```
   Help me build a skill at .claude/skills/osquery/SKILL.md that wraps osqueryi.

   Frontmatter:
   - allowed-tools: Bash
   - description should trigger on questions like "why is my computer slow",
     "what's using my CPU", "what's using memory", "fan is running hot",
     "what processes are running"

   Body should:
   - Tell Claude to run `osqueryi --json "<SQL>"` for structured output
   - Include 4-5 example natural-language → SQL mappings against real tables
     (processes, memory_info, interface_addresses, system_info)
   - Tell Claude to interpret the JSON result in plain English
   ```

   Test it from a fresh question (don't mention osquery in the prompt — let the skill description trigger):
   ```
   What's hammering my CPU right now?
   ```
   - The skill should activate automatically based on its description
   - Claude runs an osquery query and explains the result

   *Reference implementation* (in case you want to compare or got stuck): `skills-and-plugins/osquery-plugin/skills/osquery/SKILL.md` in this repo.

5. **Wrap your skill as a plugin** (~8 minutes):

   *Why?* You're learning the mechanics here — manifest layout, namespacing, the `--plugin-dir` test loop — so that when you build a *suite* of related skills (the dominant real-world case), you already know how to package them. A single-skill plugin is also useful when you want versioning or a marketplace listing for one capability.

   Create the plugin scaffold (outside any one project — plugins are portable):
   ```bash
   mkdir -p ~/osquery-plugin/.claude-plugin
   mkdir -p ~/osquery-plugin/skills/osquery
   cp .claude/skills/osquery/SKILL.md ~/osquery-plugin/skills/osquery/SKILL.md
   ```

   Create `~/osquery-plugin/.claude-plugin/plugin.json`:
   ```json
   {
     "name": "osquery-tools",
     "description": "Natural-language system diagnostics via osquery",
     "version": "0.1.0",
     "author": { "name": "Your Name" }
   }
   ```

   > **Manifest rules** (the boring part that makes the good part work):
   > - The manifest **must** live at `.claude-plugin/plugin.json` — *not* at the plugin root.
   > - Skills go at `<plugin-root>/skills/<name>/SKILL.md` — *not* inside `.claude-plugin/`.
   > - Only `name` is required. `description`, `version`, and `author` are recommended.

   Test it without installing — start a fresh Claude Code session pointed at the plugin directory:
   ```bash
   claude --plugin-dir ~/osquery-plugin
   ```

   In the new session, run `/help` and look for your skill. **It will be namespaced**: `/osquery-tools:osquery`, not bare `/osquery`. Plugin skills are *always* namespaced as `<plugin-name>:<skill-name>` to prevent collisions. That namespace is the `name` field in `plugin.json`.

   Now ask the same diagnostic question in natural language:
   ```
   What's hammering my CPU right now?
   ```
   The skill auto-loads from its description, exactly like the standalone version. The behavior is identical; **what changed is the packaging**.

   *Iteration tip*: edit `SKILL.md` and run `/reload-skills` — no restart needed.

6. **Where the plugin grows from here** (~2 minute discussion):
   - **The dominant pattern is multi-skill bundling.** The plugin you just built has one skill, which is the *minor* mode. Look at `/help` in any session that has plugins installed: you'll typically see plugin-namespaced groups like `document-skills:xlsx`, `document-skills:pdf`, `document-skills:docx`, … (16 skills in that one plugin); `autoresearch:plan`, `autoresearch:debug`, `autoresearch:fix`, … (10 skills). When you have two or three related osquery-style wrappers — say one for `osqueryi`, one for `kubectl`, one for `aws` — that's the natural moment to bundle them under one plugin (`cli-wrappers`, maybe).
   - When **other people** need it → push the plugin folder to a git repo, then register it in a [plugin marketplace](https://code.claude.com/docs/en/plugin-marketplaces).
   - When a skill needs **live external context** (e.g., reading from a monitoring system or ticketing tool) → add an MCP server config inside the plugin (`.mcp.json` at the plugin root).
   - When a step has to be **deterministic** (e.g., always block a query that touches sensitive tables) → add a hook (`hooks/hooks.json` at the plugin root).
   - Before you hand skills or a `CLAUDE.md` to a team → run **`/doctor prompt-audit`** (2.1.283). It flags prompting patterns written for older models across the CLAUDE.md files, skills, agents, and commands that load into your sessions, in this project and under `~/.claude/`. Try it now — it reports findings with a proposed diff and applies nothing on its own.

   Reference for the multi-layer case: `skills-and-plugins/plugin-examples/team-standards-plugin.md`.

**Takeaway exercise** (do after class):
> Pick a CLI tool whose syntax you don't enjoy memorizing — `kubectl`, `aws`, `gh api`, `jq`, `ffmpeg`, `psql`. Wrap it as a skill following the osquery pattern. If your team would use it, wrap that skill as a plugin.

#### Part C: Output Styles (5 minutes)

> **Note**: As of recent versions, output styles are switched via `/config` → **Output style**, or with `/output-style <name>` (bare `/output-style` lists the available styles). `/output-style <name>` **applies immediately** — no restart (verified on 2.1.278). Editing `outputStyle` in a settings file by hand is picked up on the next session.

7. **Test Built-in Output Styles** (Default / Proactive / Concise / Explanatory / Learning):

   In your current session, run:
   ```
   /output-style Explanatory
   ```
   Then, in the same session, ask:
   ```
   Explain how the weather API integration works
   ```
   Notice the inline "Insights" Claude weaves between coding steps.

   Now switch again with `/output-style Learning` and repeat — no restart needed:
   ```
   Explain how the weather API integration works
   ```
   Same prompt, more collaborative answer — Claude may insert `TODO(human)` markers asking you to implement small pieces yourself.

8. **Create Custom Output Style**:
   ```
   Help me create a custom output style called 'production' that:
   - Is concise and action-focused
   - Skips explanations unless asked
   - Assumes expert-level knowledge
   - Shows code without preambles
   Save it to ~/.claude/output-styles/production.md
   ```

   Then run `/output-style production` and verify the new register. (If the new style isn't listed yet, restart once so the file is picked up.) Finish with `/output-style default`.

#### Part D: Hooks and Automation (10 minutes)

9. **Configure a SessionEnd Hook**:
   ```bash
   # Copy example hook (run from project root)
   mkdir -p ~/.claude/hooks
   cp hooks-examples/session-end-summary.sh ~/.claude/hooks/
   chmod +x ~/.claude/hooks/session-end-summary.sh
   ```

   Then configure it:
   ```
   Add a SessionEnd hook to my ~/.claude/settings.json that runs ~/.claude/hooks/session-end-summary.sh using the current hook array format
   ```

10. **Test PreToolUse Hook** - Security Validator:
   ```bash
   # Copy security hook (run from project root)
   cp hooks-examples/security-validator.sh ~/.claude/hooks/
   chmod +x ~/.claude/hooks/security-validator.sh
   ```

   Configure it:
   ```
   Add a PreToolUse hook for Write operations that runs ~/.claude/hooks/security-validator.sh to check for hardcoded secrets. Use the current hook array format with an "if" matcher.
   ```

   Test it:
   ```
   Create a config file with: API_KEY = 'sk_live_1234567890abcdef'
   ```
   - The hook should block this and explain why
   - Try again with environment variable approach

11. **Create Your Own Hook**:
   ```
   Create a hook that runs prettier to auto-format Python files before any Edit operation. Save it to ~/.claude/hooks/format-python.sh
   ```

#### Part E: MCP Server Integration (5 minutes)

> **Note**: MCP Tool Search (lazy loading) is now enabled by default. Instead of loading all tool definitions upfront, Claude discovers tools on demand — reducing context usage by ~95% with many MCP tools.

12. **List and Explore MCP Servers**:
   ```bash
   # List currently configured MCP servers
   claude mcp list
   ```

   Then in Claude Code:
   ```
   What MCP servers are available and what tools do they provide?
   ```

13. **Add Context7 MCP Server** (for up-to-date documentation):
   ```bash
   # Add Context7 for fetching latest library docs
   claude mcp add --transport http context7 https://mcp.context7.com/mcp
   ```

   Test it:
   ```
   Using Context7, look up the latest Flask documentation for error handling
   ```

14. **Import from Claude Desktop** (if you have Claude Desktop installed):
   ```bash
   # Import any MCP servers configured in Claude Desktop
   claude mcp add-from-claude-desktop
   ```

15. **Explore MCP Tools**:
   ```
   Show me what tools are available from the MCP servers and demonstrate one
   ```
   - Observe how MCP extends Claude's capabilities
   - Note the `mcp__` prefix on tool names

#### Part F: CLAUDE.md, AGENTS.md, and Skills vs Commands (5 minutes)

16. **CLAUDE.md creation**:
   ```
   Create a CLAUDE.md file for this weather app with Python coding standards, dependencies, and project context
   ```

   Note that the built-in slash command `/init` creates the `CLAUDE.md` file, but you can simply ask Claude to create it.

17. **The AGENTS.md fallback rule (and the bridge pattern)**:
   ```
   This repo also has an AGENTS.md (the cross-tool convention). Claude Code only reads AGENTS.md when there is no CLAUDE.md, so with both files present it is ignored. Update CLAUDE.md to reference @AGENTS.md so the cross-tool conventions are picked up. Explain what changed and why.
   ```

   Observe how `@filepath` import keeps a single source of truth. (Since 2.1.277, a project with *only* an `AGENTS.md` is read natively — the bridge matters when both files exist.)

18. **Skill vs. legacy command** (compare side-by-side):
   ```
   Help me create a skill called 'update-deps' under .claude/skills/update-deps/SKILL.md for updating Python dependencies safely. Use frontmatter to scope tools (Bash, Read, Edit) and trigger on requirements.txt and pyproject.toml. Then create the same workflow as a legacy .claude/commands/update-deps.md file and explain which capabilities the skill has that the command doesn't.
   ```

   This contrast — same automation, two forms — is the heart of the merge.

#### Part G: Feature Enhancement with All Tools (5 minutes)

18. **Comprehensive Enhancement**:

   Now use Plan Mode with all your new tools:
   ```
   Using Plan Mode, add comprehensive error handling and structured logging to this application. The security hook should verify no secrets are added, and generate API documentation using the skill when done.
   ```

   - Observe how multiple advanced features work together
   - Note subagent selection, skill activation, and hook execution

#### Part H: Agent Teams (10 minutes)

> **Note**: Agent Teams is a research preview feature. Enable it with:
> ```bash
> export CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS=1
> ```

19. **Launch an Agent Team**:

   Navigate to the `exercises/java/certificate-service` directory:
   ```
   Create a team of agents to improve this project. One agent should review
   the test suite and identify coverage gaps, another should review the code
   for security issues, and a third should update the documentation.
   Coordinate the work and give me a summary when done.
   ```

   Observe:
   - The lead agent creates a task list and assigns teammates
   - Each teammate works independently in its own context
   - Teammates report back as they complete tasks
   - The lead agent synthesizes results

20. **Monitor Team Progress**:

   While the team works, observe:
   - Task status updates in the output
   - Inter-agent messages (DMs between teammates)
   - How idle teammates wake up when assigned new work
   - The shared task list with dependencies

21. **Simpler Team Exercise** (alternative if time is short):
   ```
   Create a team with two agents: one to add input validation to the
   certificate controller, and another to write tests for that validation.
   The test agent should wait until the validation agent finishes.
   ```

   This demonstrates task dependencies (`blocks`/`blockedBy`).

22. **Background Agents and Worktree Isolation**:
   ```bash
   # Start Claude in an isolated worktree
   claude -w
   ```
   ```
   Refactor the PdfService to use the builder pattern, then run all tests
   ```
   - Notice you're on a separate branch in an isolated directory
   - Changes don't affect your main working tree
   - On exit, you'll be asked to keep or remove the worktree

#### Part I: Surfaces & Remote Workflows (10 minutes, optional)

23. **Explore `/powerup`** (Self-learning):
   ```
   /powerup
   ```
   - Browse interactive lessons on Claude Code features
   - Try a lesson on a feature you haven't used before

24. **Try Remote Session** (if claude.ai/code access available):
   ```bash
   # Start a cloud session from your terminal
   claude --cloud "Analyze the weather-app for potential performance improvements"
   ```
   - Observe the session running on Anthropic cloud
   - Visit claude.ai/code to see the session in browser
   - Try `/teleport` to pull it back to your terminal

25. **Discussion: Surfaces & Dispatch**:
   - Review the 5 surfaces: CLI, VS Code, JetBrains, Desktop app, Web
   - Discuss Dispatch: sending tasks from phone to desktop
   - When would you use each surface?
   - How do scheduled tasks differ across surfaces?

#### Part J: Mods — Code Inside Claude Code (10 minutes, optional)

> **Note**: Mods need Claude Code 2.1.287 or later (`claude --version`). A mod is a plugin whose JavaScript/TypeScript hooks run *inside* Claude Code, so it can draw a pane, add a `/command`, or guard a tool call — things a settings hook (a shell command) can't do. Docs: https://code.claude.com/docs/en/plugins/mods/overview

26. **Ask Claude for a mod** (you don't write the code):
   ```
   Make a mod that opens a pane listing the tools Claude has called this session, newest last
   ```
   - Claude loads its built-in `plugin-authoring` skill and writes three files under `~/.claude/dev-mods/<session-id>/<mod-name>/`: `.claude-plugin/plugin.json`, `hooks/hooks.json`, and `hooks/register.tsx`
   - In `default` or `acceptEdits` mode, approve each file (the mods folder is a protected path)
   - When the first file lands, Claude Code asks **"Turn on the mods Claude writes in this session?"** — choose **Turn them on**. The mod loads when the turn ends
   - Run `/plugin` and Tab to the **Installed** tab: the mod is listed, with a dim `1 mod active` line

27. **Use it, then change it**:
   - Ask Claude to do something with a few tool calls (`list the files here and read the README`) and watch the pane fill
   - Now ask for a change. The mod reloads at the end of the turn:
   ```
   Add a /tool-calls-clear command to the mod that empties the list
   ```
   - Open `hooks/register.tsx` and find the three ideas every mod is built from: `on(event, matcher, hook)`, the `$` API (`$.ui.open`, `$.command.register`), and `next(e)` passing the event on

28. **Keep it** (optional): a session's mods folder is cleaned up after `cleanupPeriodDays`. Copy the mod directory somewhere permanent and load it in any session:
   ```bash
   cp -r ~/.claude/dev-mods/<session-id>/tool-calls ~/mods/tool-calls
   claude plugin validate ~/mods/tool-calls   # lists its hooks: and calls: lines
   claude --plugin-dir ~/mods/tool-calls
   ```
   - `claude plugin validate` is also how you vet a mod *before* installing one: mods run with your permissions, unsandboxed

#### Part K: Build a Custom Subagent (15 minutes)

> **Note**: A subagent is a markdown file with YAML frontmatter and a system prompt, in `.claude/agents/` (project) or `~/.claude/agents/` (personal). Each one runs in its own context window and hands back only its result, so your main context stays clean. The frontmatter scopes what it can do: `tools`, `model`, `effort`, `permissionMode`, `hooks`, and `isolation`.

29. **Read a working example** (in `exercises/python/weather-app`):
   ```
   Read .claude/agents/dependency-compliance-auditor.md. Explain what each frontmatter field does, and how its description decides when you delegate to it.
   ```

30. **Build a read-only security reviewer** — switch to `exercises/python/flask-api` and create a new branch first:
   ```
   Create a project subagent at .claude/agents/security-reviewer.md. It may only use Read, Grep, and Glob, should run on Sonnet at high effort, and reviews Python web code for SQL injection, hardcoded secrets, and missing input validation. It returns findings ranked by severity, each with file and line. Write the description so that you delegate to it whenever I ask for a security review.
   ```
   - Open the file and check the `tools`, `model`, and `effort` lines
   - Run `/agents` to see it listed beside the built-in agents

31. **Delegate to it**:
   ```
   Do a security review of app.py and database.py
   ```
   - Watch Claude hand the task to `security-reviewer` instead of doing it inline
   - Notice that only the findings come back into your main context
   - Ask the reviewer to fix its top finding. It can't: it has no Edit or Write tool, which is the point of scoping tools

32. **Stretch: a fixer that works in isolation**:
   ```
   Create a second subagent, security-fixer, with Read, Edit, and Bash, and isolation set to worktree. Have it fix the reviewer's top finding and show me the diff.
   ```
   - The fix lands in a separate worktree and branch, not in your working tree

#### Part L: Dynamic Workflows (10 minutes)

> **Note**: A dynamic workflow is a script that runs background agents: fan-out, loops, and verification steps are decided by code, not improvised by a lead agent. They spend tokens fast. The default size is **medium** (about 10 agents; **small** on Pro), set in `/config` → **Dynamic workflow size**. Opt in with `/effort ultracode on`, the keyword `ultracode` in a prompt, or by asking Claude to "use a workflow."

33. **Run a multi-angle review** (still in `exercises/python/flask-api`):
   ```
   Use a workflow to review this project from three separate angles: security, error handling, and test coverage. Give each angle its own agent, then have a separate agent verify each finding before you report. Report only the confirmed findings, ranked by severity.
   ```
   - If Claude Code asks you to approve the workflow, read its description first
   - Run `/workflows` to watch the agents start, finish, and hand off to the verify step
   - Keep working at the main prompt while it runs; you're notified when it finishes
   - On a Pro plan, ask for two angles instead of three to stay inside the small size

34. **Compare with Part H**:
   - In the agent team, the lead agent decided how to split the work. Here you described the structure and the script enforced it
   - Which would you pick for a migration across 200 files? For an exploratory refactor?
   - Compare the cost of the two runs (`/cost`, or the usage shown when each finishes)

#### Part M: Headless Claude in Scripts and CI (10 minutes)

35. **One prompt, JSON out** (in `exercises/python/flask-api`):
   ```bash
   claude -p "List the three riskiest functions in app.py and why" --output-format json | jq -r '.result'
   ```
   - `-p` runs one prompt and exits; nothing waits for you to approve
   - The JSON carries more than the answer. Run it again with `jq '.total_cost_usd, .num_turns'`

36. **A pre-commit check that fails fast**:
   ```bash
   cat > "$(git rev-parse --git-path hooks)/pre-commit" <<'HOOK'
   #!/bin/sh
   verdict=$(git diff --cached | claude -p "Reply with PASS or FAIL on the first line, then one line of reason. FAIL if this diff adds a hardcoded secret or builds an SQL query by string concatenation." --allowedTools "Read,Grep" --permission-prompts none --max-turns 3)
   echo "$verdict"
   echo "$verdict" | head -1 | grep -q '^PASS' || exit 1
   HOOK
   chmod +x "$(git rev-parse --git-path hooks)/pre-commit"
   ```
   Test it: add `API_KEY = 'sk_live_1234567890abcdef'` to `config.py`, then `git add config.py && git commit -m "test"`. The commit should be refused with the reason.
   - `--permission-prompts none` auto-denies anything that would prompt, so the hook can never hang waiting for you
   - Remove the hook when you're done, since it runs on every commit in this repo: `rm "$(git rev-parse --git-path hooks)/pre-commit"`

37. **Optional: the same call from Python**:
   ```bash
   pip install claude-agent-sdk
   ```
   ```python
   import anyio
   from claude_agent_sdk import query, ClaudeAgentOptions, ResultMessage

   async def main():
       options = ClaudeAgentOptions(allowed_tools=["Read", "Grep"], max_turns=5)
       async for msg in query(prompt="Summarize what app.py does", options=options):
           if isinstance(msg, ResultMessage):
               print(msg.result)

   anyio.run(main)
   ```

### Expected Outcomes

After completing this lab, you will:
- Master Plan Mode and understand subagent architecture
- Know when and how to use Skills for domain expertise
- Understand plugin system for team collaboration
- Customize output styles for different contexts
- Implement hooks for workflow automation
- Configure and use MCP servers for extended capabilities
- Create reusable project configurations with CLAUDE.md
- Build skills (with optional `.claude/commands/` legacy form for contrast) for common workflows
- Orchestrate multiple advanced features together
- **Launch and coordinate Agent Teams for parallel work**
- **Use worktree isolation for safe experimentation**
- **Run background agents for concurrent workflows**
- **Understand the 5 Claude Code surfaces and when to use each**
- **Use `/powerup` for self-directed feature learning**
- **Ask Claude for a mod, and know what a mod can reach before installing one**
- **Build a custom subagent with scoped tools, model, and effort, and delegate to it**
- **Run a dynamic workflow and know when it beats an agent team**
- **Run Claude headless with JSON output and as a fail-fast pre-commit check**

### Troubleshooting

**Skills not activating?**
- Check that SKILL.md has proper YAML frontmatter
- Verify skill directory is in `~/.claude/skills/`
- Ask Claude if the skill loaded: `/skills list` (if available)

**Hooks not running?**
- Verify hook script has execute permissions: `chmod +x hook.sh`
- Check settings.json syntax is valid JSON
- Review hook output - errors appear as user messages

**Output style not changing?**
- Verify file is in `~/.claude/output-styles/` (or `.claude/output-styles/` for project scope)
- Check YAML frontmatter format
- Run bare `/output-style` to confirm which style is current and that yours is listed
- **Restart Claude Code** if a newly created style file isn't listed, or if you set `outputStyle` by editing a settings file

**MCP servers not working?**
- Run `claude mcp list` to verify server is configured
- Check that remote servers are accessible (network/firewall)
- For local servers, verify the command exists and is executable
- Restart Claude Code after adding new MCP servers
- Note: MCP Tool Search (lazy loading) means tools aren't visible until used

**Agent Teams not starting?**
- Verify the environment variable: `echo $CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS`
- Must be set to `1` before starting Claude Code
- Teams require sufficient plan credits (each teammate uses tokens independently)
- If teammates seem stuck, the lead agent will typically retry or reassign

**Worktree issues?**
- Must be in a git repository to use `claude -w`
- Worktree is created under `.claude/worktrees/` in the repo
- If a worktree wasn't cleaned up, use `git worktree list` and `git worktree remove`

[← Back to Table of Contents](#table-of-contents)

## Tips for Success

### Effective Prompting
- Be specific about what you want to achieve
- Provide context about your goals and constraints
- Use iterative refinement for complex tasks
- Include examples when possible

### Best Practices
- Start with clean git state before major changes
- Commit checkpoints regularly during development
- Review all AI-generated code before accepting
- Test generated code thoroughly

### Common Issues and Solutions

**Issue**: Claude Code doesn't understand the project  
**Solution**: Ask it to explore the project first: "Analyze this project structure"

**Issue**: Generated code doesn't follow project conventions  
**Solution**: Create a CLAUDE.md file with your coding standards

**Issue**: Tests fail after code generation  
**Solution**: Ask Claude to fix: "These tests are failing, please analyze and fix them"

**Issue**: Code is too verbose or not optimized  
**Solution**: Request refinement: "Make this code more concise and efficient"

## Next Steps

After completing these labs:

1. **Practice regularly**: Use Claude Code for daily development tasks
2. **Share with team**: Introduce colleagues to effective workflows
3. **Customize**: Create project-specific CLAUDE.md files (with `@AGENTS.md` bridge if applicable) and skills
4. **Iterate**: Refine your prompting techniques based on results
5. **Stay updated**: Follow Claude Code updates and new features

## Additional Resources

- [Claude Code Documentation](https://code.claude.com/docs/en/overview)
- [Best Practices Guide](https://www.anthropic.com/engineering/claude-code-best-practices)
- [MCP Server Directory](https://github.com/modelcontextprotocol/servers)