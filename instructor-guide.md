# Claude Code Training: Demos and Exercises

This document outlines suggested demos and exercises for the projects in the `exercises` directory. The projects offer a diverse mix of languages, frameworks, and code quality, perfect for showcasing the capabilities of an AI coding assistant.

---

### Core Best Practice: The "Branch-First" Workflow

Before starting any exercise that involves significant code changes, get into the habit of creating a new Git branch. This is the most important safety measure you can take. It isolates your work, allows for fearless experimentation, and makes it easy to discard changes if they don't work out, all without affecting the `main` branch.

**Recommended Branching Strategy for Exercises:**

Since this is a training course with multiple projects in one repository, use a simple local branching approach:

1. **Create a branch for each exercise:** Use descriptive names based on the exercise
   - Examples: `flask-api-refactor`, `weather-app-forecast`, `lyrics-trainer-debug`
   
2. **Work locally:** No need to push branches - this is just for practice and experimentation

3. **Easy reset:** If you want to start over, simply:
   ```bash
   git checkout main
   git checkout -b new-attempt
   ```

4. **Commit as you go:** Create checkpoints for your progress
   ```bash
   git commit -m "test: add pytest suite"
   git commit -m "refactor: extract database logic"
   ```

**Example Prompt:** `Using git, create a new feature branch called 'flask-api-refactor' and switch to it.`

---

### 1. Python: `flask-api` (The "Awful" Project)

**Primary Demo Vehicle.** This project is a prime candidate for demonstrating large-scale refactoring, code cleanup, and adding foundational best practices. Use this project to tell a continuous story of improvement.

**Suggested Demos & Exercises:**

*   **Demo: Initial Triage & Understanding**
    *   **Prompt:** `Analyze this project and identify the top 3-5 areas for improvement in terms of code quality, security, and maintainability.`

*   **Demo: Branching for Safety**
    *   Demonstrate the "branch-first" workflow before making any changes.
    *   **Prompt:** `Using git, create a new feature branch called 'flask-api-refactor' and switch to it.`

*   **Exercise: Add a Safety Net (Testing)**
    *   **Prompt:** `Write unit tests for the main endpoints in app.py. Use the pytest framework and mock any database interactions.`

*   **Demo: Major Refactoring**
    *   **Prompt:** `Now that we have tests, let's refactor app.py. Separate the database logic, models, and API routes into their own modules within a new application structure.`

*   **Exercise: Git Integration - Committing Changes**
    *   After refactoring, have students use the assistant to commit their work.
    *   **Prompt:** `Commit the changes on this branch with a Conventional Commit message that describes the refactoring.`

*   **Exercise: Documentation**
    *   **Prompt:** `Create a complete README.md for this project, including setup instructions and API endpoint documentation.`

*   **Exercise: Vulnerability Remediation**
    *   This exercise demonstrates a real-world security workflow.
    *   **Instructor note — do not "fix" this in the repo:** the old pins in `exercises/python/flask-api/requirements.txt` (`Flask==1.1.2`, `Werkzeug==1.0.1`, `markupsafe==2.0.1`) are **intentionally vulnerable**; they are what students find and upgrade here. GitHub will show Dependabot alerts for that file, and that is expected. Leave them open. (`weather-app` has no such exercise, so its dependencies are kept current.)
    *   **Step 1: Identify Vulnerabilities.** Have students ask the assistant to check for outdated packages.
    *   **Prompt:** `Analyze the requirements.txt file. Are these packages up-to-date? Are there any known security vulnerabilities in these specific versions?`
    *   **Step 2: Plan the Upgrade.** Ask the assistant to create a plan to upgrade the packages to secure versions.
    *   **Prompt:** `Create a plan to upgrade the packages in requirements.txt to the latest secure versions. What are the potential breaking changes I should be aware of?`
    *   **Step 3: Execute the Upgrade.** Have the students instruct the assistant to perform the upgrade.
    *   **Prompt:** `Now, please update the requirements.txt file to the latest stable and secure versions of Flask, Werkzeug, and MarkupSafe.`
    *   **Step 4: Verify the Fix.** After updating, students should run the tests to ensure the application still works.
    *   **Prompt:** `Run the tests to ensure the application is still functional after the upgrade.`

---

### 2. Python: `weather-app` (The "Beginner" Project)

This project is perfect for showing how to iteratively add features and work with different types of files.

**Suggested Demos & Exercises:**

*   **Exercise: Image Analysis - From Wireframe to Code**
    *   Use the provided wireframe to generate the initial UI.
    *   **Prompt:** `Analyze the weather-app-wireframes.jpg and generate the HTML and CSS for the main home page.`

*   **Exercise: Add a New Feature**
    *   **Prompt:** `Modify the application to add a 5-day forecast page that is linked from the main city weather view.`

*   **Demo: Error Handling & Robustness**
    *   **Prompt:** `Add error handling to the weather data fetching logic. If the external API fails or returns an error, display a user-friendly error page.`

---

### 3. JavaScript/TypeScript: `lyrics-trainer`

A modern web project with an existing test suite, ideal for demonstrating workflows on a realistic, contemporary stack.

**Suggested Demos & Exercises:**

*   **Demo: Working with an Existing Test Suite**
    *   **Prompt:** `Review the existing tests in the tests/ directory. Now, write a new test file for src/script.ts that verifies the main application logic.`

*   **Exercise: Bug Fixing**
    *   Introduce a bug and have students use the assistant and the test suite to find and fix it.
    *   **Prompt:** `The lyrics are not advancing correctly when the user types. Run the tests to confirm the failure, then analyze src/script.ts to find and fix the bug.`

---

### 4. Java: `shopping-service` (Old Grails Project)

This project is a classic "legacy modernization" scenario, perfect for showing how an AI assistant can act as a subject matter expert for older or unfamiliar technologies.

**Suggested Demos & Exercises:**

*   **Demo: "Teach Me This Framework"**
    *   **Prompt:** `This is a Grails project. Explain the purpose of the key directories like 'grails-app/controllers', 'grails-app/domain', and 'grails-app/views'.`

*   **Exercise: Understand and Translate Code**
    *   **Prompt:** `Explain the ProductController.groovy file. What are its main actions? Translate the 'list' action to an equivalent in Java with Spring Boot.`

*   **Demo: Modernization Plan**
    *   **Prompt:** `Think step-by-step and create a detailed plan to migrate this Grails project to a modern Spring Boot application. The new application should provide a JSON REST API instead of server-side views.`

---

### 5. Java: `certificate-service` (Modern Spring Boot Project)

This project represents a current, common stack, ideal for demonstrating advanced, best-practice development workflows.

**Suggested Demos & Exercises:**

*   **Demo: Advanced Test Generation**
    *   **Prompt:** `Write a test for the CertificateService class. Use Mockito to mock the repository dependency and verify that the service's methods are called correctly.`

*   **Exercise: Working with Build Files**
    *   **Prompt:** `Add the Spring Boot Actuator dependency to this project. Modify the build.gradle.kts file correctly to include it.`

*   **Exercise: Custom Commands for Workflow Automation**
    *   Showcase how to create a custom, project-specific command.
    *   **Prompt:** `Create a project-level custom command named /newcert. When I run '/newcert PDF', it should scaffold a new PdfCertificateController.java and a PdfCertificateService.java in the appropriate directories.`

*   **Demo: Deployment & DevOps Tasks**
    *   **Prompt:** `First, explain the heroku-deploy.sh script. Then, create a multi-stage Dockerfile to build and run this Spring Boot application efficiently.`

---

## Advanced Features Demonstrations

This section covers demonstrations for the newest Claude Code features: Skills, Plugins, Output Styles, Hooks, and Subagents. These demos showcase cutting-edge capabilities suitable for enterprise development teams.

---

### 6. Skills: Persistent Domain Expertise

**Project:** Any project (demonstrate with `certificate-service` for Java)

Skills provide persistent, reusable knowledge that activates automatically when contextually relevant. They use a three-tier loading system for efficiency.

**Demo Sequence:**

*   **Demo: Built-in Skills - Document Generation**
    *   Show how built-in skills activate automatically
    *   **Prompt:** `Create a professional PDF report documenting the certificate service API. Include endpoint descriptions, request/response examples, and usage instructions.`
    *   **Observe:** The PDF skill loads automatically
    *   **Point out:** No manual skill activation needed - it's contextual

*   **Demo: Installing a Custom Skill**
    ```bash
    # Show the skill directory structure
    ls -la skills-and-plugins/spring-boot-skill/

    # Install it locally
    cp -r skills-and-plugins/spring-boot-skill ~/.claude/skills/
    ```

    *   **Prompt:** `Create a new Spring Boot REST controller for handling training certificates with full CRUD operations.`
    *   **Observe:** The Spring Boot skill activates, applying team standards automatically
    *   **Point out:** Notice constructor injection, JavaDoc, proper package structure

*   **Exercise: Create Your Own Skill**
    *   **Prompt:** `Help me create a custom skill for our team's testing standards. It should enforce:
        - Use of AssertJ for assertions
        - @DisplayName annotations on all tests
        - Given-When-Then comment structure
        - Proper test isolation
        Save it to ~/.claude/skills/team-testing-standards/`
    *   **Follow up:** `Now write tests for CertificateService using this new skill`
    *   **Observe:** The skill activates and applies your standards

*   **Discussion Points:**
    - Skills persist across conversations (unlike one-time prompts)
    - Three-tier loading: metadata (always), instructions (when triggered), resources (as needed)
    - Skills vs. custom commands: When to use each
    - Team collaboration: Sharing skills via version control

---

### 7. Plugins: Team-Wide Standardization

**Project:** Demonstration without active coding

Plugins bundle commands, skills, hooks, and MCP servers into installable packages for team distribution.

**Demo Sequence:**

*   **Demo: Plugin Architecture**
    *   Open and review `skills-and-plugins/plugin-examples/team-standards-plugin.md`
    *   Show the plugin.json structure
    *   Explain how plugins bundle multiple capabilities:
        - Custom slash commands
        - Domain-specific skills
        - Validation hooks
        - MCP server configurations
        - Team templates

*   **Demo: Plugin Use Cases**
    *   **Enterprise scenario:** "Imagine Acme Corp has 50 Java developers"
    *   **Problem:** How do you ensure consistent:
        - Code generation patterns
        - Security review processes
        - API standards
        - Testing approaches
    *   **Solution:** Create an `acme-corp-standards` plugin
    *   **Distribution:** `/plugin install acme-corp-standards`
    *   **Result:** All developers instantly get:
        - `/service` command for Spring Boot services
        - Security review hooks
        - Internal API MCP servers
        - Company-wide skills

*   **Demo: Plugin Marketplace Configuration**
    *   Show `~/.claude/settings.json` configuration:
    ```json
    {
      "extraKnownMarketplaces": [
        {
          "name": "Acme Internal",
          "url": "https://plugins.acme.corp/marketplace.json"
        }
      ]
    }
    ```
    *   Explain marketplace discoverability vs. direct installation

*   **Exercise: Design Your Team's Plugin**
    *   Have students design (not implement) a plugin for their organization
    *   Questions to guide them:
        - What commands would save your team time?
        - What skills encode your best practices?
        - What hooks enforce quality gates?
        - What internal systems need MCP integration?
    *   Share designs with the class

*   **Discussion Points:**
    - Plugin versioning and updates
    - Project vs. user-level plugin installation
    - Security considerations for plugin distribution
    - Building a team culture around shared tooling

---

### 8. Output Styles: Adaptive Communication

**Project:** Any project (use `weather-app` for variety)

Output styles customize how Claude presents solutions to match different contexts and learning preferences.

**Demo Sequence:**

> **Heads-up for instructors**: output styles are switched via `/config` → **Output style** or `/output-style <name>` (the bare command came back in 2.1.269 and lists the styles, now including **Proactive**). `/output-style <name>` applies **immediately, mid-session** (verified on 2.1.278), so the demos below run all three styles back-to-back in one session. Only a hand-edited `outputStyle` in a settings file waits for the next session.

*   **Demo: Built-in Styles Comparison** (Default / Explanatory / Learning)

    Open Claude Code in the project. Run `/output-style Explanatory`.

    *   **Prompt:** `Explain how the weather API integration works in this Flask app`
    *   **Note:** Observe the educational "Insights" interleaved with the answer

    Switch in place: `/output-style Learning`.

    *   **Same Prompt:** `Explain how the weather API integration works in this Flask app`
    *   **Compare:** More collaborative — Claude may insert `TODO(human)` markers asking the user to implement small pieces themselves

*   **Demo: Creating Custom Output Styles**
    *   **Scenario:** "In production, we want concise, expert-level responses"
    *   **Prompt:** `Create a custom output style called 'production' with these characteristics:
        - Concise and action-focused
        - Skip explanations unless explicitly asked
        - Assume expert-level knowledge
        - Show code without preambles
        - Use technical terminology freely
        Save it to ~/.claude/output-styles/production.md`

    *   **Activate it:** `/output-style production` (relaunch once only if the new file isn't listed yet).
    *   **Same Prompt:** `Explain how the weather API integration works`
    *   **Compare:** Notice the terse, expert-oriented response

*   **Demo: When to Use Each Style**
    *   **Default:** General software-engineering work
    *   **Explanatory:** Onboarding new team members, code reviews
    *   **Learning:** Training sessions, pair-programming, teaching unfamiliar concepts
    *   **Custom (e.g. production):** Match your team's register and communication culture

*   **Exercise: Style for Your Context**
    *   **Prompt:** `Create an output style for code review sessions that:
        - Focuses on potential issues and improvements
        - Provides specific fix recommendations
        - Explains the reasoning behind suggestions
        - Uses a constructive, collaborative tone`
    *   Test with: `Review the error handling in weather_service.py`

*   **Discussion Points:**
    - Output styles don't change Claude's capabilities, only presentation
    - Can be set per-session or as default
    - Consider creating styles for different team roles
    - Balance verbosity with developer experience level

---

### 9. Hooks: Workflow Automation

**Project:** `weather-app` or `certificate-service`

Hooks execute shell commands in response to Claude Code events, enabling sophisticated workflow automation.

**Demo Sequence:**

*   **Demo: SessionEnd Hook**
    ```bash
    # Install the session summary hook
    cp hooks-examples/session-end-summary.sh ~/.claude/hooks/
    chmod +x ~/.claude/hooks/session-end-summary.sh
    ```

    *   **Prompt:** `Configure a sessionEnd hook in my settings.json to run ~/.claude/hooks/session-end-summary.sh`
    *   Work through a few file edits and commits
    *   Exit Claude Code
    *   **Show:** The generated session summary with statistics

*   **Demo: PreToolUse Hook - Security Validation**
    ```bash
    # Install security validator
    cp hooks-examples/security-validator.sh ~/.claude/hooks/
    chmod +x ~/.claude/hooks/security-validator.sh
    ```

    *   **Prompt:** `Add a preToolUse hook for Write operations that runs security-validator.sh`

    *   **Test it - This should BLOCK:**
    *   **Prompt:** `Create a config.py file with: API_KEY = "sk_live_1234567890abcdef"`
    *   **Observe:** Hook blocks the operation, explains the security issue

    *   **Fix it:**
    *   **Prompt:** `Update that to use environment variables: API_KEY = os.getenv("API_KEY")`
    *   **Observe:** Hook allows the operation

*   **Demo: PreToolUse Hook - Auto-Formatting**
    ```bash
    # Install formatter hook
    cp hooks-examples/pre-edit-formatter.sh ~/.claude/hooks/
    chmod +x ~/.claude/hooks/pre-edit-formatter.sh
    ```

    *   **Prompt:** `Add a preToolUse hook for Edit operations that auto-formats files`
    *   Make an edit to a Python file with poor formatting
    *   **Observe:** Hook auto-formats before the edit is applied
    *   **Point out:** Hooks run transparently, maintaining code quality

*   **Demo: Hook Feedback is User Input**
    *   When a hook blocks an operation, its output appears as if from the user
    *   Claude adjusts behavior based on hook feedback
    *   This creates a collaborative safety net

*   **Exercise: Build a Test Runner Hook**
    *   **Prompt:** `Create a hook that runs pytest before any git commit operation. If tests fail, block the commit.`
    *   **Test it:** Make a change that breaks tests, attempt to commit
    *   **Observe:** Hook prevents broken code from being committed

*   **Demo: Conditional Hook with `if` field**
    *   Show a hook that only runs on Python files:
    ```json
    {
      "hooks": {
        "PreToolUse": [{
          "type": "command",
          "command": "python-lint.sh",
          "if": "Edit(**.py)"
        }]
      }
    }
    ```
    *   **Point out:** The `if` field uses permission rule syntax to filter when hooks fire

*   **Discussion Points:**
    - Exit code 0 = allow, non-zero = block
    - Four hook types: command, HTTP, prompt, agent
    - Hooks should be fast (they run synchronously)
    - Use hooks for: validation, formatting, security, backups
    - Project vs. user-level hook configuration
    - Conditional hooks with `if` field for targeted execution

---

### 10. Mods: Code Inside Claude Code

**Project:** `weather-app` (any project works)

A mod is a plugin whose JavaScript/TypeScript hooks run *inside* Claude Code, not as a shell command. Teach it right after settings hooks: same idea (react to events), different reach (draw a pane, add a `/command`, hold or rewrite a tool call). Docs: https://code.claude.com/docs/en/plugins/mods/overview

**Prerequisite:** Claude Code 2.1.287+ (`claude --version`). Mods are on by default; `--safe-mode` or `disableAllHooks` turns them off.

**Demo Sequence:**

*   **Demo: Settings hook vs. mod** (slide "Mods: Code That Runs Inside Claude Code")
    *   Point at the `.env` guard: same job as the `security-validator.sh` PreToolUse hook from section 9, but it's a function with `$`, `e`, and `next` instead of a script parsing JSON on stdin
    *   Three verbs: **guard** (`{ deny }`), **rewrite** (`next({ ...e, ... })`), **observe** (`next(e)`)
    *   What only a mod can do: panes, bands above the prompt, `/commands` that run without a Claude turn, restyling the spinner

*   **Demo: Ask Claude for a mod** (live, ~5 minutes)
    *   **Prompt:** `Make a mod that shows the current git branch above the prompt`
    *   Claude loads the built-in `plugin-authoring` skill and writes the mod under `~/.claude/dev-mods/<session-id>/`
    *   **Show:** the "Turn on the mods Claude writes in this session?" question — this is the switch, and only the person at the prompt can flip it
    *   Choose **Turn them on**; the band appears when the turn ends
    *   Run `/plugin` → **Installed** tab: `1 mod active · git-branch`
    *   **Then:** `Change the mod to show the branch in yellow when it isn't main` — reloads at the end of the turn. Hot reload is the point of the demo

*   **Demo: Trust before you install**
    *   Mods run with the user's permissions, unsandboxed: files, processes, network, env vars, every prompt and tool call
    *   `claude plugin validate ./some-mod` lists `hooks:` and `calls:` without running it — show the output and point at `$.fs.write`, `$.http.fetch`, `$.process` as the lines to read first
    *   Enterprise angle: managed settings can restrict to managed mods only (`allowManagedModsOnly`)

*   **Optional: Sample mods** from Anthropic's `claude-code-playground` repo (`claude-code/mods/`): `blast-radius` holds a risky shell command and shows what it would change — a good callback to the security-hook demo. Load with `claude --plugin-dir <clone>/claude-code/mods/blast-radius`

*   **Exercise:** Lab 6 Part J — students ask for a tool-calls pane, use it, change it, then validate and reload it with `--plugin-dir`

**Key Point:** Skills give Claude knowledge, settings hooks give you deterministic checks, MCP gives Claude tools. A mod changes Claude Code itself. Reach for it when the other three can't draw it, hold it, or run it without a turn.

### 11. Subagents: Specialized Task Handlers

**Project:** `flask-api` for comprehensive demonstration

Subagents are autonomous specialized agents that Claude launches automatically for specific task types.

**Demo Sequence:**

*   **Demo: Exploring Subagent Types**
    *   **Prompt:** `Explain which subagents you use and when each activates automatically`
    *   **Review:** The three built-in types: Plan, Explore, and General-purpose
    *   **Key point:** You don't manually select these - Claude chooses optimally

*   **Demo: Plan Subagent (Plan Mode)**
    *   Type `/plan` to activate Plan Mode
    *   **Prompt:** `Plan a comprehensive refactoring to modernize this Flask app with blueprints, better error handling, and structured logging`
    *   **Observe:** Plan subagent creates detailed strategy
    *   **Point out:** This uses strategic decomposition, different from direct execution
    *   Approve the plan and watch step-by-step execution

*   **Demo: Explore Subagent**
    *   **Prompt:** `Find all API endpoints in this codebase and explain their authentication requirements`
    *   **Observe:** Explore subagent activates automatically
    *   **Point out:** Read-only, faster than general-purpose exploration
    *   **Show:** Systematic codebase search results

*   **Demo: General-purpose Subagent**
    *   **Prompt:** `Generate comprehensive test coverage for all API endpoints with edge cases and error scenarios`
    *   **Observe:** General-purpose subagent handles testing, documentation, and other complex tasks
    *   **Point out:** Full read/write access — Claude delegates to this for tasks needing code changes

*   **Demo: Model Selection for Subagents**
    *   Explain that different subagents can use different models
    *   Fast subagents (Explore) might use Haiku (fastest tier; Haiku 5.5 has 1M context at $0.10/$0.50 per Mtok)
    *   Complex subagents (Plan) might use Opus (top tier)
    *   This optimizes both cost and performance
    *   Since 2.1.292 Claude can pass `effort` per Agent call, so "Explore on Haiku at low effort, Plan on Opus at high" needs no custom agent file
    *   Since 2.1.317 a subagent on a different model no longer inherits the session's effort — it runs at that model's own saved level or default; `CLAUDE_CODE_SUBAGENT_EFFORT` (2.1.316) sets a floor for all of them

*   **Demo: Custom Agent Definitions**
    *   Show `.claude/agents/` directory for defining custom agents
    *   Each agent is a markdown file with YAML frontmatter:
      - `model`, `tools`, `effort`, `hooks`, `permissionMode`, `autoCompactWindow`
    *   **Prompt:** `Show me how to create a custom agent definition for security review`

*   **Discussion Points:**
    - Subagents are transparent - you see when they activate
    - They're automatic - no manual selection needed
    - They specialize both tools and reasoning
    - Custom agents in `.claude/agents/` extend the built-in types
    - Think of them as "expert consultants" Claude calls in

---

### 12. Orchestrating Multiple Features

**Project:** `certificate-service` (final comprehensive demo)

Demonstrate how Skills, Hooks, Output Styles, and Subagents work together seamlessly.

**Demo Sequence:**

*   **Setup: Configure the Environment**
    ```bash
    # Ensure skills are installed
    ls ~/.claude/skills/spring-boot-skill/

    # Ensure hooks are configured
    cat ~/.claude/settings.json  # Show security hook

    # Set production output style: /config → Output style → production
    # (Restart Claude Code so the new system prompt applies)
    claude
    ```

*   **Demo: Complete Feature Development**
    *   Activate Plan Mode: `/plan` (or cycle with `Shift+Tab`)
    *   **Prompt:** `Plan and implement a new training transcript PDF generation feature with:
        - REST controller following our Spring Boot standards
        - Service layer with business logic
        - Comprehensive tests
        - API documentation
        - Security validation (no secrets in code)
        Generate the PDF documentation when done.`

    *   **Observe the Orchestration:**
        1. Plan subagent creates implementation strategy
        2. Spring Boot skill activates for code generation
        3. Security hook validates no hardcoded secrets
        4. Testing skill ensures proper test coverage
        5. PDF skill generates documentation
        6. Production style keeps responses concise

    *   **Point out:** All features working together automatically

*   **Demo: Session Wrap-up**
    *   Complete some commits
    *   Exit Claude Code
    *   **Show:** SessionEnd hook generates summary
    *   Review what was accomplished with all tools working together

*   **Reflection Questions:**
    - Which features provided the most value?
    - How would you configure this for your team?
    - What custom skills would help your workflow?
    - What hooks would enforce your quality gates?

---

## Teaching Tips for Advanced Features

**Pacing:**
- Introduce one feature at a time
- Show before explaining - demos are more powerful than theory
- Let students experiment immediately after each demo

**Common Questions:**

*Q: When should I use a skill vs. a custom command?*
- **Skills:** Persistent domain expertise that activates contextually
- **Commands:** Quick shortcuts for repetitive prompts
- **Example:** Spring Boot standards = skill; "create service" = command

*Q: Are hooks required?*
- No, they're optional workflow enhancements
- Start without hooks, add as needs arise
- Great for teams with strict quality gates

*Q: Do output styles affect code quality?*
- No, only presentation style
- Same underlying capabilities and reasoning
- Choose based on context and audience

*Q: Can I control which subagent activates?*
- No, Claude selects automatically for optimal results
- Trust the system - it's designed to choose correctly
- Plan Mode is the only manually triggered subagent

**Troubleshooting:**

*Skills not activating:*
- Check YAML frontmatter format
- Verify directory location (~/.claude/skills/)
- Skill descriptions must match task context

*Hooks failing silently:*
- Check execute permissions (chmod +x)
- Validate JSON syntax in settings
- Review hook script for errors

*Output style not applying:*
- Verify file location (`~/.claude/output-styles/` for user, `.claude/output-styles/` for project)
- Check YAML frontmatter
- Run bare `/output-style` to see the current style and the available list
- **Restart Claude Code** if a newly created style file isn't listed, or if `outputStyle` was set by editing a settings file

---

### 13. Surfaces, Dispatch & Code Review

**Project:** Any project

Demonstrate the multi-surface ecosystem and cloud planning.

**Demo Sequence:**

*   **Demo: Five Surfaces Overview**
    *   Show the 5 surfaces: CLI, VS Code, JetBrains, Desktop app, Web (claude.ai/code)
    *   Since 2.1.318 launching `claude` inside an IDE terminal no longer installs the extension — run `/ide` from an outside terminal (or install from the editor's extensions view) before the demo
    *   Emphasize: local surfaces share settings, CLAUDE.md, MCP servers, skills, hooks. Note: web sessions only inherit repo-committed config, not user-level `~/.claude` settings.
    *   **Discussion:** When would you use each surface?

*   **Demo: Remote Session**
    ```bash
    # Start a cloud session from CLI
    claude --cloud "Analyze the certificate-service for dependency updates"
    ```
    *   Show the session appearing on claude.ai/code
    *   Demonstrate `/teleport` to pull it back to terminal

*   **Demo: Deliberate Code Review**
    *   In shopping-service, make a small change (or reuse the lab diff), then **Prompt:** `/code-review`
    *   **Observe:** findings ranked by severity; with `high`+ it runs in a background agent while you keep working
    *   **Point out:** since 2.1.215 Claude never runs `/code-review` on its own — reviews are a deliberate act
    *   Mention `/code-review ultra` (deep multi-agent cloud review, billed) without running it live

*   **Discussion: Dispatch**
    *   Explain: send tasks from Claude mobile app → Desktop app
    *   Use case: "On my commute, I send 'update the CI config' to my desktop"
    *   Dispatch routes dev work to Code tab, other work to Cowork tab

---

### 14. Auto-Memory, Scheduled Tasks & Channels

**Project:** Any project

**Demo Sequence:**

*   **Demo: Auto-Memory**
    *   **Prompt:** `/memory`
    *   Show how Claude has recorded project context, preferences, and patterns
    *   **Point out:** Memory persists across sessions at `~/.claude/projects/<project>/memory/`
    *   Discuss: what kinds of things Claude remembers (user prefs, feedback, project context)

*   **Demo: `/loop` for Polling**
    ```
    /loop 2m check if there are any new issues on this repo
    ```
    *   **Observe:** Runs the prompt every 2 minutes
    *   **Point out:** Session-scoped — stops when you exit
    *   Compare with Desktop scheduling (persistent) and Cloud scheduling (always-on)

*   **Discussion: Channels (Research Preview)**
    *   Push events from Telegram, Discord, iMessage into a Claude session
    *   Use case: monitoring alerts that Claude can act on
    *   `--channels` flag to enable
    *   Enterprise: admin must enable `channelsEnabled`

---

## Course Conclusion: Bringing It All Together

After completing all demonstrations and exercises, students should understand:

1. **Core Features:** Code generation, exploration, testing, documentation
2. **Surfaces:** CLI, VS Code, JetBrains, Desktop app, Web — and when to use each
3. **Customization:** CLAUDE.md, skills, output styles, effort levels
4. **Extensibility:** Plugins, MCP integration, custom agent definitions
5. **Automation:** Hooks (command, HTTP, prompt, agent), scheduled tasks
6. **Intelligence:** Subagents, Agent Teams, Dynamic Workflows, Code Review
7. **Cross-Device:** Dispatch, Remote Control, session management
8. **Enterprise Practices:** Auto Mode, security, team collaboration, quality gates

**Final Exercise:** Have students design their ideal Claude Code setup for their organization, including:
- Which skills they would create
- What hooks would enforce quality
- How they would structure a team plugin
- Which surfaces their team would use for different workflows
- What scheduled tasks would automate routine work

**Next Steps:**
- Practice with real work projects
- Share configurations with team
- Try `/powerup` for self-directed learning
- Contribute to plugin ecosystem
- Stay updated on new features