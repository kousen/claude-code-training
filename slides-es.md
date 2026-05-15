---
theme: seriph
background: https://images.unsplash.com/photo-1555066931-4365d14bab8c?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80

addons:
  - slidev-component-progress
  - slidev-addon-qrcode

class: text-center
highlighter: shiki
lineNumbers: false
info: |
  ## Claude Code Training
  
  By Kenneth Kousen
  
  Learn more at [KouseniT](https://kousenit.com)
drawings:
  persist: false
transition: slide-left
title: "Claude Code Training"
mdc: true
slidev:
  slide-number: true
  controls: true
  progress: true
css: unocss
---

<style>
.slidev-page-num {
  display: block !important;
  opacity: 1 !important;
  visibility: visible !important;
  position: fixed !important;
  bottom: 1rem !important;
  right: 1rem !important;
  z-index: 100 !important;
  color: #666 !important;
  font-size: 0.875rem !important;
}
</style>

# Claude Code Training

<div class="pt-12">
  <span @click="$slidev.nav.next" class="px-2 py-1 rounded cursor-pointer" hover="bg-white bg-opacity-10">
    Presiona Espacio para la siguiente página <carbon:arrow-right class="inline"/>
  </span>
</div>

---

# Información de Contacto

Ken Kousen  
Kousen IT, Inc.

- ken.kousen@kousenit.com
- http://www.kousenit.com
- http://kousenit.org (blog)
- Redes Sociales:
  - [@kenkousen](https://twitter.com/kenkousen) (twitter)
  - [@kenkousen@foojay.social](https://foojay.social/@kenkousen) (mastodon)
  - [@kousenit.com](https://bsky.app/profile/kousenit.com) (bluesky)
- *Tales from the jar side* (boletín gratuito)
  - https://kenkousen.substack.com
  - https://youtube.com/@talesfromthejarside

---

# Descripción del Curso

<v-clicks>

- **Duración**: 5 horas de aprendizaje práctico
- **Formato**: Instructor en vivo con múltiples laboratorios
- **Laboratorios Prácticos**: Bases de código reales en Python, JavaScript, Java
- **Requisitos Previos**: Experiencia en línea de comandos, conocimientos de desarrollo

</v-clicks>

---

# Temas Cubiertos

<v-clicks>

- **Fundamentos**: Instalación, superficies, conceptos básicos de CLI, exploración de código
- **Habilidades Esenciales**: Pruebas, documentación, operaciones con git
- **Personalización**: CLAUDE.md, skills, hooks, estilos de salida
- **Extensibilidad**: Plugins, integración con MCP
- **Avanzado**: Niveles de Esfuerzo, Modo Plan, Ultraplan, Subagentes, Equipos de Agentes, SDKs

</v-clicks>

---

# ¿Qué es Claude Code?

<v-clicks>

- Herramienta de desarrollo con IA en **5 superficies principales** (CLI, VS Code, JetBrains, Desktop, Web) más integraciones (Slack, Chrome, iOS, GitHub Actions, GitLab CI/CD)
- Comprensión contextual de bases de código
- Modos autónomo, colaborativo y **multi-agente**
- Soporte multilenguaje con **inteligencia de código LSP**
- Operaciones integradas de git
- **Extensible**: Skills, Plugins, MCP, Hooks

</v-clicks>

---
layout: image-right
image: https://images.unsplash.com/photo-1551288049-bebda4e38f71?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80
backgroundSize: cover
---

# Cinco Superficies

<v-clicks>

- **CLI (Terminal)** — Interfaz principal con todas las funciones
- **Extensión de VS Code** — Diffs en línea, menciones con @, revisión de planes
- **Plugin de JetBrains** — IntelliJ, PyCharm, WebStorm
- **App de Escritorio** — Mac/Windows nativo con diffs visuales, programación de tareas, conectores
- **Web (claude.ai/code)** — Basado en navegador, sin configuración local requerida

Las superficies locales comparten: configuraciones, CLAUDE.md, servidores MCP, skills y hooks. Las sesiones web solo heredan la configuración confirmada en el repositorio, no la configuración de nivel de usuario en `~/.claude`.

</v-clicks>

---

# App de Escritorio de Claude

<v-clicks>

- **Tres pestañas**: Chat (general), Cowork (agente en segundo plano), Code (codificación interactiva)
- **Revisión visual de diffs** con comentarios en línea y botón "Review code"
- **Vista previa en vivo de la app** con navegador integrado y verificación automática
- **Tareas programadas** — persistentes, locales, sobreviven reinicios
- **Conectores** — GitHub, Slack, Linear, Notion, Google Calendar
- **Uso del equipo** (vista previa de investigación) — Claude controla la pantalla en macOS/Windows
- **Sesiones paralelas** con aislamiento automático de git worktree

</v-clicks>

---

# Claude Code en la Web

<v-clicks>

- **claude.ai/code** — Ejecuta tareas en la infraestructura cloud de Anthropic
- **Bandera `--remote`**: Inicia una sesión web desde CLI: `claude --remote "Fix the auth bug"`
- **Vista de diffs**: Revisa los cambios archivo por archivo antes de crear PRs
- **PRs de corrección automática**: Claude responde a fallos de CI y comentarios de revisión automáticamente
  - **Advertencia**: Las respuestas se publican bajo tu cuenta — puede activar automatizaciones (Atlantis, Actions)
- **`/teleport`**: Trae las sesiones web de vuelta a tu terminal local
- **Configuración**: Conecta GitHub, instala la App de Claude para GitHub, selecciona el entorno
- También accesible desde las **apps de Claude para iOS y Android**

</v-clicks>

---

# Integraciones y Superficies Sin Interfaz

<v-clicks>

- **Slack** — invoca Claude Code en canales y hilos
- **Extensión de Chrome** — asistencia consciente de la página en el navegador
- **App de iOS** — lee sesiones, despacha trabajo, revisa diffs desde tu teléfono
- **GitHub Actions** — `anthropics/claude-code-action` para revisiones de PR, corrección automática, flujos de trabajo con scripts
- **GitLab CI/CD** — integración oficial para pipelines
- **Sin transición limpia** desde la pestaña *Code* del Desktop de vuelta al terminal al momento de escribir esto — planifica en consecuencia

</v-clicks>

Comandos de transición entre superficies que verás más adelante:
- `/desktop` (alias `/app`) — terminal → pestaña *Code* del desktop *(macOS / Windows)*
- `/teleport` (alias `/tp`) — web → terminal
- `/remote-control` (alias `/rc`) — hace que la sesión de terminal sea controlable desde claude.ai

---

# Despacho y Control Remoto

<v-clicks>

### Despacho
- Envía tareas desde tu **teléfono** a tu **App de Escritorio**
- El despacho enruta tareas de desarrollo a la pestaña Code, las demás a Cowork
- Notificación push cuando termina o necesita aprobación

### Control Remoto
- Continúa una **sesión CLI en ejecución** desde el teléfono o el navegador
- `claude --rc` para iniciar con Control Remoto habilitado
- `/remote-control` para habilitarlo en medio de una sesión
- La sesión corre localmente — la web/móvil es solo una ventana hacia ella

</v-clicks>

---

# Ultraplan

<v-clicks>

- **Planificación basada en la nube** para cambios complejos a nivel de toda la base de código
- Lanzamiento: `/ultraplan migrate the auth service from sessions to JWTs`
- O incluye "ultraplan" en cualquier prompt
- Claude redacta el plan en la nube mientras **tú sigues trabajando localmente**
- **Revisión en el navegador**: Comentarios en línea, reacciones con emoji, navegación por esquema
- **Opciones de ejecución**: Ejecutar en la nube (crea un PR) o teleportar de vuelta al terminal

</v-clicks>

```
Terminal status indicators:
◇ ultraplan           — Claude is researching and drafting
◇ ultraplan needs your input  — Clarifying question
◆ ultraplan ready     — Plan ready to review in browser
```

---

# Cuándo NO Usar Claude Code

<v-clicks>

- **Ediciones de una sola línea que ya sabes cómo hacer** — escribir es más rápido que indicar
- **Bases de código altamente reguladas** sin un plan empresarial y un registro de auditoría implementado
- **Tareas que requieren juicio humano en tiempo real** (decisiones de texto UX, voz de marca, revisión legal)
- **Incidentes de producción bajo presión de tiempo** — trabaja con un humano, no lo hagas solo
- **Aprender un nuevo lenguaje/framework** — deja que la fricción te enseñe primero
- **Cuando no puedes revisar el diff** — si no lo vas a leer, no lo envíes

</v-clicks>

La pregunta correcta no es "¿puede Claude hacer esto?" — sino "¿entenderé lo que se envió?"

---

# Niveles de Suscripción

<v-clicks>

- **Pro** — $20/mes · ~10-40 prompts cada 5h · Sonnet 4.6
- **Max 5x** — $100/mes · ~50-200 prompts cada 5h · Sonnet 4.6 u Opus
- **Max 20x** — $200/mes · ~200-800 prompts cada 5h · Sonnet 4.6 u Opus
- **Team** — puestos compartidos, facturación centralizada, controles de administrador
- **Enterprise** — SSO, auditoría, retención personalizada, enrutamiento a Bedrock / Vertex / Foundry
- Opus usa ~5× los créditos de Sonnet; los límites se reinician cada 5 horas
- Ruta API: créditos prepagados vía Console; crea automáticamente un espacio de trabajo "Claude Code" para rastreo de costos

</v-clicks>

📖 **Detalles completos**: [Using Claude Code with your Pro or Max plan](https://support.anthropic.com/en/articles/11145838-using-claude-code-with-your-pro-or-max-plan)

---

# Elegir un Modelo

<v-clicks>

| Modelo | Cuándo usarlo |
|---|---|
| **Opus 4.7** | Decisiones de arquitectura, refactorizaciones multi-archivo, depuración difícil, orquestación de agentes |
| **Sonnet 4.6** | Uso diario predeterminado — la mayoría de codificación, exploración, generación de documentación |
| **Haiku 4.5** | Bucles rápidos, operaciones por lotes, hooks, clasificadores, llamadas a herramientas económicas |

- **Cambiar a mitad de conversación**: `Alt+P` / `Option+P`
- **Establecer por sesión**: `claude --model claude-opus-4-7`
- **Los niveles de esfuerzo** (`/effort low|medium|high`) son ortogonales — controlan la profundidad independientemente del modelo elegido

</v-clicks>

Regla general: primero Sonnet. Recurre a Opus cuando ya hayas fallado una vez con Sonnet, no de forma preventiva.

---

# Proveedores Empresariales

<v-clicks>

Tres proveedores de primera clase para empresas que necesitan su propia infraestructura:

- **AWS Bedrock** — `CLAUDE_CODE_USE_BEDROCK=1`
- **Google Vertex AI** — `CLAUDE_CODE_USE_VERTEX=1`
- **Microsoft Foundry** — `CLAUDE_CODE_USE_FOUNDRY=1`

</v-clicks>

<v-clicks>

⚠️ **Advertencia — los alias de modelos apuntan por defecto a modelos de *versión anterior* en los tres:**

- `opus` → Opus **4.6** (no 4.7)
- `sonnet` → Sonnet **4.5** (no 4.6)

Usa IDs de versión explícitos para los modelos más recientes:
```bash
ANTHROPIC_MODEL=claude-opus-4-7         # explicit, latest
ANTHROPIC_MODEL=claude-sonnet-4-6       # explicit, latest
```

Autenticación a través del IAM del proveedor cloud, no con una clave API de Anthropic. Patrón de gateway LLM: `ANTHROPIC_BASE_URL` + `CLAUDE_CODE_ENABLE_GATEWAY_MODEL_DISCOVERY=1`.

</v-clicks>

---

# Instalación

<v-clicks>

- **Recomendado** (macOS / Linux / WSL): instalador nativo, actualizaciones automáticas
  ```bash
  curl -fsSL https://claude.ai/install.sh | bash
  ```
- **Homebrew** (macOS): `brew install --cask claude-code` *(actualización manual)*
- **WinGet** (Windows): `winget install Anthropic.ClaudeCode` *(actualización manual)*
- **Paquetes de Linux**: `apt`, `dnf`, `apk` para Debian / Fedora / RHEL / Alpine
- **Avanzado** (legado): `npm install -g @anthropic-ai/claude-code`
- Verificar: `claude --version`

</v-clicks>

El binario `claude` es nativo — npm ya no es la ruta principal.

---

# Crear Proyectos desde Cero

<v-clicks>

- **Comenzar de la nada**: Directorio vacío + idea = aplicación funcionando
- **Desarrollo iterativo**: Concepto → base → mejoras
- **Creación full-stack**: UI, lógica, estilos, pruebas en una sola sesión
- **Ejemplo real**: Nuestro ejercicio `lyrics-trainer` comenzó exactamente así

</v-clicks>

```bash
mkdir my-project && cd my-project && git init
claude
"Create a web app that displays song lyrics one line at a time
with Next, Previous, and Play buttons"
```

---

# Modos de Operación

<v-clicks>

- **Modo Comando** (predeterminado) - Conversación interactiva
- **Modo Auto-Aceptar** (Shift+Tab) - Ejecución autónoma
- **Modo Plan** (`/plan` o ciclar con `Shift+Tab`) - Revisa planes antes de ejecutar
- **Modo Auto** - El clasificador de seguridad elimina los prompts de permiso (opt-in)
- **Niveles de esfuerzo**: `/effort low|medium|high` para controlar la profundidad de razonamiento
- **Cambio de modelo**: `Alt+P` / `Option+P` para cambiar modelos a mitad de conversación

</v-clicks>

---
layout: image-right
image: https://images.unsplash.com/photo-1517077304055-6e89abbf09b0?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80
backgroundSize: cover
---

# Funciones Esenciales de Productividad

<div class="text-center mt-20">
  <h2 class="text-4xl font-bold text-white bg-black bg-opacity-60 px-6 py-3 rounded-lg">
    Sé Productivo de Inmediato
  </h2>
  <p class="text-xl text-white bg-black bg-opacity-60 px-4 py-2 rounded mt-4">
    Funciones esenciales para el trabajo de desarrollo diario
  </p>
</div>

---

# Exploración de Código

<v-clicks>

- Buscar archivos, funciones y patrones
- Entender la arquitectura del sistema
- Rastrear dependencias
- Identificar frameworks
- Referenciar archivos específicos con `@path/to/file.java`

</v-clicks>

```bash
"Analyze the UserService class"
"Explain @src/main/java/com/example/UserController.java"
"How does @pom.xml configure Spring Boot?"
```

---

# Generación de Pruebas

<v-clicks>

- Creación de pruebas unitarias
- Identificación de casos límite
- Pruebas de integración
- Configuración de objetos mock

</v-clicks>

```bash
"Create unit tests for the UserService"
"Add tests for error scenarios"
```

---

# Refactorización, Documentación y Depuración

<v-clicks>

### Refactorización
```bash
"Convert all callbacks in @src/api/ to async/await"
"Replace the manual JSON parsing with Jackson annotations"
```

### Documentación
```bash
"Generate JavaDoc for every public method in UserService"
"Write a README.md from the current package.json scripts and folder layout"
```

### Depuración
```bash
"Here's the stack trace — trace it back to the root cause and propose a fix"
"@logs/2026-05-08.log shows three different errors. Group them by likely cause."
```

</v-clicks>

Patrón: nombra el *archivo o símbolo*, indica el *resultado esperado* y deja que Claude elija los pasos.

---
layout: image-right
image: https://images.unsplash.com/photo-1556075798-4825dfaaf498?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80
---

# Integración con Git

<v-clicks>

- Generación de mensajes de commit
- Gestión de ramas
- Resolución de conflictos de merge
- Creación de pull requests

</v-clicks>

```bash
"Commit these changes with an appropriate message"
"Create a pull request for this feature"
```

---

# Flujos de Trabajo con Múltiples Herramientas

<v-clicks>

- **Operaciones por lotes**: Llamar a múltiples herramientas en una sola respuesta
- **Ejecución en paralelo**: Ejecutar git status + diff + log simultáneamente
- **Optimización de rendimiento**: Reduce los viajes de ida y vuelta
- **Flujos de trabajo complejos**: Encadenar operaciones dependientes

</v-clicks>

```bash
# Parallel git operations
"Show me git status, recent commits, and current diff"

# Multi-file analysis
"Check all test files and their coverage simultaneously"
```

💡 **Consejo profesional**: Solicita "en paralelo" para una ejecución más rápida

---

---
layout: image-left
image: https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80
backgroundSize: cover
---

# Herramientas Esenciales del Flujo de Trabajo

<div class="text-center mt-20">
  <h2 class="text-4xl font-bold text-white bg-black bg-opacity-60 px-6 py-3 rounded-lg">
    Personaliza tu Experiencia
  </h2>
  <p class="text-xl text-white bg-black bg-opacity-60 px-4 py-2 rounded mt-4">
    Gestión de sesiones y personalización
  </p>
</div>

---

# Archivos CLAUDE.md

<v-clicks>

- **Memoria del proyecto**: `./CLAUDE.md` (compartida con el equipo)
- **Memoria del usuario**: `~/.claude/CLAUDE.md` (preferencias personales)
- **Directorio de reglas**: `.claude/rules/` para reglas de proyecto organizadas
- Se descubren automáticamente subiendo en el árbol de directorios
- **Agregar rápido**: Comienza la entrada con `#` para agregar a la memoria
- **Memoria automática**: Claude registra y recuerda entre sesiones
- **Comandos**: `/memory` para editar, `/init` para inicializar
- **Importar archivos**: Usa la sintaxis `@path/to/import`

</v-clicks>

---

# AGENTS.md vs CLAUDE.md (la trampa)

<v-clicks>

- **`AGENTS.md`** es la convención para *otras* herramientas (Codex, etc.)
- **Claude Code NO lee `AGENTS.md` de forma nativa** — solo `CLAUDE.md` (issue abierto [#6235](https://github.com/anthropics/claude-code/issues/6235))
- Un repositorio con ambos archivos parece bilingüe pero es silenciosamente de un solo idioma para Claude Code
- **Patrón puente** — referencia `AGENTS.md` desde `CLAUDE.md` para que Claude Code lo incluya:

```markdown
# CLAUDE.md
…project-specific guidance for Claude Code…

## Cross-tool conventions
See @AGENTS.md for conventions shared with Codex and other agentic tools.
```

- La importación con `@` mantiene una sola fuente de verdad sin duplicar contenido

</v-clicks>

---

# Barra de Estado Personalizada

<v-clicks>

- **Configurar la visualización del contexto**: `~/.claude/settings.json` o `/statusline`
- Muestra rama de git, estado, directorio de trabajo, información personalizada
- Mantiene el contexto importante visible sin necesidad de preguntar
- Reduce las comprobaciones de estado repetitivas

</v-clicks>

```json
{
  "statusline": {
    "items": [
      {"type": "git_branch"},
      {"type": "git_status"},
      {"type": "cwd"},
      {"type": "custom", "command": "node -v"}
    ]
  }
}
```

Ideal para equipos que quieren una visibilidad de contexto estandarizada

---

# Comandos de Barra (Cuatro Categorías)

<v-clicks>

- **Integrados** — `/help`, `/clear`, `/compact`, `/init`, `/memory`, `/permissions`, `/agents`, `/config`, `/plan`, `/login`, `/mcp`, … (~30 y creciendo)
- **Personalizados (ahora integrados en skills)** — `.claude/commands/<name>.md` sigue funcionando; los skills son la ruta moderna
- **Derivados de skills** — cualquier skill con `user-invocable: true` expone `/<skill-name>`
- **Proporcionados por plugins** — los plugins instalados aportan sus propios comandos
- **Descubrimiento**: `/help` lista los comandos actuales; autocompletado con `/` al escribir
- **Regla de conflicto**: si un comando y un skill comparten nombre, el skill tiene prioridad
- **Alcance**: proyecto (`.claude/`) se comparte con el equipo; usuario (`~/.claude/`) es personal

</v-clicks>

No memorices el catálogo — cambia mensualmente. Aprende las categorías y usa `/help` para enumerarlos.

---

# Crear Comandos Personalizados

<v-clicks>

- **Comandos simples**: Solo un archivo markdown — no se necesita frontmatter
- **Skills**: Agrega frontmatter YAML para modelo, esfuerzo, herramientas, rutas
- **Usa `$ARGUMENTS`** o `$0`, `$1` para contenido dinámico

</v-clicks>

```bash
# Quick command (lightweight)
mkdir -p .claude/commands
echo "Create service for $ARGUMENTS entity" > .claude/commands/service.md

# User-scoped command (personal, available across projects)
mkdir -p ~/.claude/commands
echo "Fix issue #$ARGUMENTS" > ~/.claude/commands/fix.md

# Usage: /service User  or  /fix 123
```

Ejemplo del mundo real:
```markdown
# ~/.claude/commands/docs.md
Update both the README.md and CLAUDE.md files as appropriate.
If either file does not exist, please create it. Generate the
CLAUDE.md file as though the user invoked the init task.
```

---

# Hooks y Automatización

<v-clicks>

- **Automatización de flujos de trabajo basada en eventos** con cuatro tipos de hooks:
  - **Hooks de comandos**: Ejecutan comandos de shell en eventos
  - **Hooks HTTP**: Envían JSON mediante POST a una URL
  - **Hooks de prompt**: Le preguntan a Claude decisiones de sí/no
  - **Hooks de agente**: Lanzan subagentes para verificar condiciones
- **Hooks condicionales**: El campo `if` filtra cuándo se ejecutan los hooks (sintaxis de reglas de permisos)
- **PreToolUse**: Modifica entradas de herramientas, bloquea operaciones peligrosas
- **Controles de seguridad**: Valida y filtra operaciones antes de que se ejecuten
- **Configuración**: `~/.claude/settings.json` o `.claude/settings.json`

</v-clicks>

---

# Ejemplos de Hooks

<v-clicks>

### Validación de seguridad (bloquear ediciones peligrosas)
```json
{ "hooks": { "PreToolUse": [{
  "type": "command", "command": "validate-edit.sh", "if": "Edit(**)"
}] } }
```

### Automatización de flujo de trabajo (dar formato automáticamente al escribir)
```json
{ "hooks": { "PreToolUse": [{
  "type": "command", "command": "prettier --write $FILE", "if": "Write(**)"
}] } }
```

### Ciclo de vida de sesión (reportar al finalizar)
```json
{ "hooks": { "SessionEnd": [{
  "type": "command", "command": "generate-session-report.sh"
}] } }
```

</v-clicks>

**Importante**: Trata la retroalimentación de los hooks como entrada del usuario — Claude ajusta si es bloqueado.

---

# Eventos de Hooks

<v-clicks>

- **Sesión**: `SessionStart`, `SessionEnd`, `InstructionsLoaded`
- **Herramientas**: `PreToolUse`, `PostToolUse`, `PermissionRequest`, `PermissionDenied`
- **Equipos**: `TeammateIdle`, `TaskCreated`, `TaskCompleted`
- **Archivos**: `FileChanged`, `CwdChanged`, `WorktreeCreate`, `WorktreeRemove`
- **Configuración**: `ConfigChange`, `Notification`
- **Contexto**: `PreCompact`, `PostCompact`
- **MCP**: `Elicitation`, `ElicitationResult`
- **Hooks a nivel de agente**: Los skills y agentes definen sus propios hooks en el frontmatter

</v-clicks>

---

# Atajos de Teclado Personalizables

<v-clicks>

- Comando **`/keybindings`** para configurar atajos de teclado
- **Archivo de configuración**: `~/.claude/keybindings.json`
- Reasigna cualquier acción a combinaciones de teclas preferidas
- Acción **`chat:newline`** para entrada multilínea configurable
- **Abreviatura de argumentos**: `$0`, `$1` en comandos personalizados (no solo `$ARGUMENTS`)

</v-clicks>

```json
{
  "chat:submit": "enter",
  "chat:newline": "shift+enter",
  "chat:switch_model": "alt+p",
  "chat:open_external_editor": "ctrl+x ctrl+e"
}
```

Atajos clave: `Ctrl+B` (segundo plano), `Ctrl+X Ctrl+K` (terminar agentes), `Ctrl+X Ctrl+E` (editor externo)

---

# LSP: Inteligencia de Código

<v-clicks>

- Integración con **Language Server Protocol** para una navegación precisa del código
- **Ir a definición**: Salta a donde están definidos los símbolos
- **Buscar referencias**: Localiza todos los usos en la base de código
- **Información al pasar el cursor**: Obtén información de tipos y documentación
- **Jerarquía de llamadas**: Rastrea llamadas entrantes y salientes
- **Símbolos del espacio de trabajo**: Busca símbolos en todo el proyecto
- Funciona con cualquier servidor LSP configurado (TypeScript, Java, Python, etc.)

</v-clicks>

---

# Estilos de Salida

<v-clicks>

- **Modifica el prompt del sistema** para establecer rol, tono y formato — no lo que Claude sabe
- **Estilos integrados**: **Default**, **Explanatory**, **Learning**
- **Estilos personalizados**: Crea los tuyos propios en `~/.claude/output-styles/` (usuario) o `.claude/output-styles/` (proyecto)
- **Casos de uso**:
  - Incorporación de nuevos miembros del equipo (Explanatory)
  - Programación en pareja y enseñanza (Learning — inserta marcadores `TODO(human)` para que los completes)
  - Registros personalizados para revisión de código, trabajo en producción, etc.
- **Para orientación específica del proyecto** (convenciones, datos de la base de código), usa `CLAUDE.md` en su lugar

</v-clicks>

---

# Usar Estilos de Salida Integrados

<v-clicks>

- **Default**: Prompt del sistema estándar de ingeniería de software
- **Explanatory**: Agrega "Perspectivas" educativas entre los pasos de codificación
- **Learning**: Aprendizaje colaborativo haciendo — Claude inserta marcadores `TODO(human)` para que los implementes
- **Cambiar mediante `/config`** → selecciona **Output style** del menú
- O edita `outputStyle` directamente en `.claude/settings.local.json`:

```json
{ "outputStyle": "Explanatory" }
```

- **Los cambios surten efecto en la próxima sesión** (el prompt del sistema se fija al inicio de la sesión para que el caché de prompts se mantenga activo)

</v-clicks>

---

# Crear Estilos de Salida Personalizados

Crea `~/.claude/output-styles/production.md`:

```markdown
---
name: Production
description: Concise output for experienced developers
---

# Instructions for Claude

- Be concise and action-focused
- Skip explanations unless asked
- Show code without lengthy preambles
- Assume expert-level knowledge
```

Luego ejecuta `/config` → **Output style** y elige `Production`. Inicia una nueva sesión para que el cambio surta efecto.

---

# Reanudar Conversaciones

<v-clicks>

- **`--continue`**: Reanuda automáticamente la conversación más reciente
- **`--resume`**: Selector interactivo que muestra el historial de conversaciones con marcas de tiempo y conteo de mensajes
- **Historial completo restaurado**: Se mantiene el contexto completo de los mensajes (incluso cientos de mensajes)
- **Configuración original preservada**: Modelo y configuración retenidos
- **Almacenado localmente**: Base de datos completa de conversaciones mantenida en tu máquina

</v-clicks>

```bash
# Continue most recent conversation
claude --continue

# Show conversation picker with details
claude --resume

# Continue with new prompt
claude --continue --print "Continue with my task"
```

---
layout: image-right
image: https://images.unsplash.com/photo-1586953208448-b95a79798f07?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80
---

# Trabajar con Imágenes

<v-clicks>

- **Arrastrar y soltar** imágenes en la ventana de Claude Code
- **Copiar/pegar** con `Ctrl+V` (¡no `Cmd+V` incluso en Mac!)
- **Proporcionar ruta de archivo**: "Analyze this image: `/path/to/screenshot.png`"
- Analizar diseños de UI, capturas de pantalla de errores, diagramas
- Generar código a partir de maquetas visuales
- Depurar problemas visuales y de diseño

</v-clicks>

```bash
# Common image workflows
"Analyze this error screenshot and suggest fixes"
"Generate HTML/CSS for this UI mockup"
"Explain what this diagram shows"
"Convert this whiteboard sketch to code"
```

---

# Soporte para Jupyter y Ciencia de Datos

<v-clicks>

- **Leer archivos .ipynb** con todas las salidas de celdas
- **Analizar notebooks**: Código, markdown y visualizaciones
- **Editar celdas de notebooks**: Usa la herramienta NotebookEdit
- **Flujos de trabajo de análisis de datos**: Procesar conjuntos de datos y resultados
- **Comprensión de visualizaciones**: Interpretar gráficos y diagramas

</v-clicks>

```bash
"Analyze this Jupyter notebook and explain the data pipeline"
"Add error handling to the data processing cells"
"Convert this notebook to a production Python script"
```

---
layout: image-right
image: https://images.unsplash.com/photo-1451187580459-43490279c0fa?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80
backgroundSize: cover
---

# Funciones Avanzadas

<div class="mt-20">
  <h2 class="text-4xl font-bold text-white bg-black bg-opacity-60 px-6 py-3 rounded-lg">
    Capacidades para Usuarios Avanzados
  </h2>
  <p class="text-xl text-white bg-black bg-opacity-60 px-4 py-2 rounded mt-4">
    Funciones complejas para flujos de trabajo sofisticados
  </p>
</div>

---

# La Escalera de Decisiones

<v-clicks>

Antes de construir cualquier cosa, nombra lo que estás construyendo:

| Si... | Tienes un/una... | Vive en |
|-------|---------------|----------|
| El trabajo ocurre **una sola vez** | **Prompt** | la conversación |
| El trabajo se repite de la misma manera cada vez | **Skill** | `.claude/skills/<name>/SKILL.md` |
| Una suite coherente necesita ser entregada | **Plugin** | `<plugin>/.claude-plugin/plugin.json` |
| El agente necesita **acceso en vivo** a un sistema | **Servidor MCP** | `.mcp.json` (o dentro de un plugin) |
| Un paso debe ser **determinista** | **Hook** | `settings.json` (o `hooks/` del plugin) |

La mayoría de las capacidades instalables son **skills individuales**. Los plugins típicamente agrupan una suite coherente de un solo autor — `document-skills` incluye ~16 skills (xlsx, pdf, docx, pptx…); `autoresearch` incluye ~10.

📖 Vocabulario completo en **`glossary.md`** en la raíz del repositorio.

</v-clicks>

---

# Skills: Experiencia de Dominio Persistente

<v-clicks>

- **Capacidades modulares** que extienden la funcionalidad de Claude más allá del modelo base
- **Unificadas con comandos de barra** (v2.1): Skills y comandos fusionados en un solo sistema
- **Sistema de carga en tres niveles** para mayor eficiencia:
  - Metadatos (siempre cargados): Nombre y descripción (~100 tokens)
  - Instrucciones (activadas): SKILL.md principal con procedimientos
  - Recursos (bajo demanda): Scripts, plantillas, archivos de referencia
- **Recarga en caliente**: Edita SKILL.md y los cambios surten efecto de inmediato
- **Activación automática** cuando es contextualmente relevante
- **Divulgación progresiva**: Carga solo lo necesario para cada tarea

</v-clicks>

---

# Skills Integrados

<v-clicks>

Anthropic proporciona cuatro Agent Skills listos para producción:

- **📊 Excel (xlsx)**: Construye hojas de cálculo, genera informes con gráficos
- **📄 Word (docx)**: Crea y formatea documentos profesionales
- **📽️ PowerPoint (pptx)**: Crea y edita presentaciones
- **📑 PDF (pdf)**: Genera documentos PDF formateados e informes

**Uso**: Estos skills se activan automáticamente cuando haces referencia a tipos de archivo relevantes o solicitas la creación de documentos

</v-clicks>

```bash
# Skills activate automatically
"Create a quarterly report spreadsheet with sales data"
"Generate a PDF proposal document with our company branding"
"Build a presentation deck for the product launch"
```

---

# Crear Skills Personalizados

<v-clicks>

### Estructura de un Skill
```
~/.claude/skills/my-skill/
├── SKILL.md          # Required: Instructions with YAML frontmatter
├── templates/        # Optional: Reusable templates
├── scripts/          # Optional: Helper scripts
└── reference/        # Optional: Documentation, schemas
```

### Ejemplo de SKILL.md
```markdown
---
name: Java Spring Generator
description: Generate Spring Boot components following team patterns
effort: high
paths:
  - "src/**/*.java"
---

# Instructions

When generating Spring Boot code:
1. Use constructor injection, not @Autowired
2. Follow package conventions: controller/service/repository
3. Include comprehensive JavaDoc
4. Generate corresponding test files with @SpringBootTest
```

Nuevo frontmatter: `effort`, `context: fork`, `paths`, `shell`, `model`

</v-clicks>

---

# Plugins: Extensibilidad para Todo el Equipo

<v-clicks>

- El **sistema de plugins** (v2.0.12+) proporciona paquetes instalables de comandos, agentes, hooks y servidores MCP
- **Marketplace de plugins**: Descubre y comparte flujos de trabajo del equipo
- **Configuración a nivel de repositorio**: `extraKnownMarketplaces` para control empresarial
- **Comandos de gestión**:
  - `/plugin install <name>` - Instalar desde el marketplace
  - `/plugin enable/disable <name>` - Controlar plugins activos
  - `/plugin marketplace` - Explorar plugins disponibles
  - `/plugin list` - Ver plugins instalados

</v-clicks>

---

# Casos de Uso de Plugins

<v-clicks>

### Flujos de Trabajo Empresariales
- Estandarizar patrones de generación de código entre equipos
- Hacer cumplir procesos de revisión de seguridad
- Automatizar documentación de cumplimiento normativo
- Integrarse con herramientas y APIs internas

### Colaboración en Equipo
- Compartir comandos y agentes personalizados
- Distribuir configuraciones de servidores MCP
- Mantener prácticas de desarrollo consistentes
- Incorporar nuevos miembros del equipo más rápido

### Ejemplo
```bash
# Install company's internal plugin
/plugin install acme-corp-standards

# Plugin provides:
# - Custom slash commands for service generation
# - Security review hooks
# - MCP servers for internal APIs
# - Pre-configured output styles
```

</v-clicks>

---
layout: image-right
image: https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80
---

# Niveles de Esfuerzo

<v-clicks>

- **`/effort low|medium|high`** controla la profundidad del razonamiento
- **Bajo**: Respuestas rápidas para tareas simples
- **Medio**: Razonamiento equilibrado (predeterminado)
- **Alto**: Análisis profundo para arquitecturas complejas
- **Las palabras clave siguen funcionando**: "think", "think harder", "ultrathink"
- Se puede establecer en el frontmatter del skill: `effort: high`

</v-clicks>

```bash
/effort high

# Or use keywords in your prompt
"Ultrathink about the best approach for implementing OAuth2"
```

---

# Modo Plan

<v-clicks>

- Escribe **`/plan`** o cicla con `Shift+Tab` para activarlo
- Claude presenta el plan de implementación antes de escribir código
- Revisa la estrategia, aprueba o modifica el enfoque
- Ideal para cambios complejos en múltiples archivos
- **Usa el subagente Plan** detrás de escena
- **`/ultraplan`**: Planificación basada en la nube para cambios a nivel de toda la base de código (ver sección Superficies)

</v-clicks>

---

# Subagentes: Manejadores de Tareas Especializados

<v-clicks>

- **Agentes autónomos** que Claude lanza para tareas especializadas
- **Selección dinámica**: Claude elige el subagente apropiado automáticamente
- **Selección de modelo**: Diferentes subagentes pueden usar diferentes modelos
- **Tipos integrados**:
  - **Plan**: Descomposición estratégica de tareas y planificación
  - **Explore**: Exploración y búsqueda rápida de bases de código
  - **General-purpose**: Acceso completo de lectura/escritura para trabajo complejo
- **Agentes personalizados**: Define en `.claude/agents/` como markdown con frontmatter YAML
  - Especifica: `model`, `tools`, `effort`, `hooks`, `permissionMode`
- **Ejecución en segundo plano**: `Ctrl+B` para enviar al fondo, `Ctrl+X Ctrl+K` para terminar
- **Aislamiento con worktree**: `isolation: "worktree"` para trabajo paralelo seguro

</v-clicks>

---

# Cuándo Claude Usa Subagentes

<v-clicks>

### Activación Automática
Claude lanza subagentes cuando las tareas coinciden con capacidades especializadas:

```bash
# Triggers Explore subagent (read-only, fast search)
"Find all API endpoints in this codebase"
"How does authentication work across the project?"

# Triggers Plan subagent
/plan or "Create a plan for adding OAuth"

# Triggers General-purpose subagent (full read/write)
"Generate comprehensive test coverage for UserService"
"Create API documentation for all REST endpoints"
```

**No gestionas esto** — Claude maneja la selección de subagentes automáticamente para obtener resultados óptimos

</v-clicks>

---

# Equipos de Agentes (Vista Previa de Investigación)

<v-clicks>

- **Orquestación multi-agente**: Un agente principal coordina a múltiples compañeros de equipo
- **Lista de tareas compartida**: Tareas con dependencias, propiedad y seguimiento de estado
- **Contexto independiente**: Cada compañero de equipo obtiene su propia ventana de contexto
- **Mensajería entre agentes**: Mensajes directos, difusiones y coordinación de cierre
- **Distribución automática de trabajo**: Los compañeros se autoasignan tareas a medida que terminan

</v-clicks>

```bash
# Enable teams (research preview)
export CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS=1

# Example prompt
"Create a team to refactor the auth module: one agent updates
the service layer, another updates tests, a third updates docs"
```

---

# Cómo Funcionan los Equipos de Agentes

<v-clicks>

- El **agente principal** crea el equipo, divide el trabajo en tareas y asigna compañeros
- Los **compañeros** trabajan de forma independiente, reportan resultados y toman nuevas tareas
- **Coordinación de tareas**: Las dependencias `blocks`/`blockedBy` evitan conflictos
- **El estado inactivo es normal**: Los compañeros quedan inactivos entre turnos y se despiertan al recibir mensajes
- **Eventos de hook**: `TeammateIdle`, `TaskCompleted` para automatización
- **Ideal para**: Grandes refactorizaciones, funcionalidades en múltiples archivos, trabajo paralelo de código + pruebas

</v-clicks>

```
Lead Agent ──→ creates tasks ──→ assigns teammates
     ↑                              │
     └── receives results ←─────────┘
         (via shared task list + messages)
```

---

# Agentes en Segundo Plano

<v-clicks>

- **Ejecuta tareas mientras sigues trabajando** — tu prompt permanece libre
- **`Ctrl+B`** para enviar un agente en ejecución al segundo plano
- **`Ctrl+X Ctrl+K`** para terminar todos los agentes en segundo plano
- **Skill `/batch`**: cambios en paralelo en muchos archivos (usa worktrees internamente)
- Los **subagentes** pueden declarar `isolation: "worktree"` en el frontmatter para escrituras paralelas seguras
- Consulta la diapositiva de *Git Worktrees* más adelante para los mecanismos de worktree

</v-clicks>

---

# Gestión de Sesiones

<v-clicks>

- **Sesiones con nombre**: `/rename my-feature` para identificación fácil posterior
- **Sesiones vinculadas a PR**: `claude --from-pr 123` reanuda con el contexto del PR cargado
- **Bifurcación de sesión**: `/branch` para bifurcar una conversación cuando quieres probar dos caminos
- **Selector de reanudación**: `claude --resume` muestra hasta 50 sesiones recientes con marcas de tiempo
- **Continuar la última**: `claude --continue` vuelve directamente a la más reciente
- **Memoria automática**: Claude registra y recuerda contexto entre sesiones

</v-clicks>

(Las transiciones entre superficies — `--remote`, `/teleport`, `/desktop`, Despacho — se tratan en la sección de Superficies.)

---

# Memoria Automática

<v-clicks>

- Claude **registra y recuerda automáticamente** memorias mientras trabaja
- Almacenadas en `~/.claude/projects/<project>/memory/`
- El archivo índice **`MEMORY.md`** se carga al inicio de sesión (primeras 200 líneas)
- Los archivos de temas se cargan bajo demanda cuando son relevantes
- Comando **`/memory`** para ver y gestionar memorias
- Tipos de memoria: preferencias del usuario, retroalimentación, contexto del proyecto, referencias
- Los subagentes pueden mantener su propia memoria automática
- Actívalo/desactívalo con la configuración `autoMemoryEnabled`

</v-clicks>

---

# Modo Auto

<v-clicks>

- **Elimina los prompts de permiso** mediante un clasificador de seguridad en segundo plano (Sonnet 4.6)
- El clasificador revisa cada acción y permite/bloquea automáticamente
- **Diferente del Auto-Aceptar** (`Shift+Tab`): El Modo Auto es inteligente, no general
- **Permite**: Operaciones de archivos locales, instalación de dependencias, HTTP de solo lectura, push a la rama actual
- **Bloquea**: Descargar + ejecutar código, deploys de producción, pushes forzados, cambios de IAM
- **Requisitos**: Plan Team / Enterprise / API, en Sonnet 4.6 u Opus actual
- Activar: `--enable-auto-mode` o ciclar con `Shift+Tab`
- **Recomendado sobre** `--dangerously-skip-permissions` para nuevos flujos de trabajo; el flag antiguo sigue funcionando para uso personal/Pro

</v-clicks>

---

# Tareas Programadas: Tres Niveles

<v-clicks>

| | **`/loop`** | **Desktop** | **Nube** |
|---|---|---|---|
| **Se ejecuta en** | Tu máquina | Tu máquina | Nube de Anthropic |
| **Requiere sesión abierta** | Sí | No | No |
| **Persistente** | No | Sí | Sí |
| **Acceso a archivos locales** | Sí | Sí | No |
| **Intervalo mínimo** | 1 minuto | 1 minuto | 1 hora |

</v-clicks>

---

# Usar Tareas Programadas

<v-clicks>

- **`/loop`** — Sondeo con alcance de sesión: `/loop 5m check if the deployment finished`
- **Desktop** — Tareas locales persistentes que sobreviven reinicios
- **Nube** — Siempre activo vía `/schedule` — se ejecuta incluso con tu máquina apagada
- Las tareas en la nube clonan el repositorio en cada ejecución (rama predeterminada)

</v-clicks>

---

# Canales (Vista Previa de Investigación)

<v-clicks>

- **Recibe eventos** de fuentes externas en sesiones de Claude Code en ejecución
- Soportados: **Telegram**, **Discord**, **iMessage** (vía plugins)
- Bidireccional: Claude lee eventos y responde de vuelta
- Casos de uso: puente de chat desde el teléfono, webhooks de CI/monitoreo
- Bandera **`--channels`** para habilitarlo: `claude --channels plugin:telegram@claude-plugins-official`
- Requiere inicio de sesión en claude.ai
- Empresas: el administrador debe habilitar `channelsEnabled`

</v-clicks>

---

# Protocolo de Contexto de Modelos (MCP)

<v-clicks>

- Protocolo estándar para conexiones entre IA y sistemas
- Integración de herramientas (APIs, bases de datos, servicios)
- Mejora del contexto para respuestas más precisas de la IA
- Controles de seguridad y permisos
- **Búsqueda de Herramientas MCP** (predeterminado desde v2.1): Carga herramientas de forma diferida bajo demanda
  - Reduce el uso de contexto en ~95% con muchas herramientas MCP
  - Las herramientas se descubren automáticamente cuando se necesitan

</v-clicks>

---

# Configuración de MCP

<v-clicks>

```bash
# Import from Claude Desktop
claude mcp add-from-claude-desktop

# Add remote server (HTTP)
claude mcp add --transport http context7 https://mcp.context7.com/mcp

# Add local server
claude mcp add context7 -- npx -y @upstash/context7-mcp
```

- **Configuración del proyecto**: `.mcp.json` en la raíz del proyecto
- **Gestión**: `claude mcp list`, `/mcp enable|disable`

</v-clicks>

---

# Ejemplos de Servidores MCP

<v-clicks>

- **GitHub MCP** - Operaciones de repositorio, issues, PRs
- **Context7** - Descarga la documentación y ejemplos más recientes de APIs para código moderno
- **Docker MCP Toolkit** - Gestión de contenedores y operaciones
- **Playwright MCP** - Generación de pruebas de UI y automatización del navegador
- **Heroku MCP** - Despliegue y gestión de aplicaciones

</v-clicks>

---

# Configurar Servidores MCP

<v-clicks>

- Configuración interactiva: `claude mcp`
- Servidores locales: Control total de la configuración
- Servidores remotos: Autenticación OAuth, sin mantenimiento
- Docker MCP Toolkit: `docker mcp gateway run`

</v-clicks>

```bash
# List existing MCP servers
claude mcp list

# Add local server
claude mcp add my-server -e API_KEY=123 -- /path/to/server

# Add remote server (HTTP)
claude mcp add --transport http remote-server https://example.com/mcp

# Add Docker MCP toolkit
claude mcp add docker-mcp docker mcp gateway run
```

---

# SDKs de Claude Code

<v-clicks>

- **SDKs disponibles**: TypeScript, Python, línea de comandos
- **Construye asistentes de codificación con IA** en tus flujos de trabajo
- **Conversaciones de múltiples turnos** y gestión de sesiones
- **Prompts del sistema personalizados** y formatos de E/S flexibles
- **Integración con MCP** para capacidades extendidas

</v-clicks>

```bash
# Command line usage
claude -p "Write a function to calculate Fibonacci numbers"
claude -p "Generate a hello world function" --output-format json
```

```typescript
// TypeScript SDK
import { query } from "@anthropic-ai/claude-code";

for await (const message of query({
  prompt: "Write a haiku about foo.py",
  options: { maxTurns: 3 }
})) {
  // Process messages
}
```

---

# SDK en Tres Patrones

<v-clicks>

### Python — iteración asíncrona con archivos de contexto
```python
from claude_code import query

async for msg in query("Refactor this module",
                       context_files=["app.py"], max_turns=3):
    print(msg.content)
```

### CI/CD — salida JSON para análisis
```yaml
- name: AI Code Review
  run: claude -p "Review PR changes" --output-format json > review.json
```

### Git hooks — herramientas restringidas, fallo rápido
```bash
# .git/hooks/pre-commit
claude -p "Check for security issues" --allowed-tools read,grep
```

</v-clicks>

El mismo SDK, tres formas de despliegue: script interactivo, paso de CI, git hook.

---

# Extensión de VS Code

<v-clicks>

- **Integración nativa con el IDE** que trae Claude Code a tu editor
- **Experiencia dentro del editor**: Trabaja con Claude sin salir de VS Code
- **Consciente del contexto**: Accede a los archivos y configuraciones de tu espacio de trabajo
- **Todas las funciones de Claude Code**: Skills, MCP, comandos personalizados disponibles
- **Instalación**: Busca "Claude Code" en el marketplace de Extensiones de VS Code

</v-clicks>

---

# Funciones de la Extensión de VS Code

<v-clicks>

### Flujo de Trabajo Integrado
- Referencia archivos con la sintaxis `@` directamente en VS Code
- Ver diffs y aprobar cambios en línea
- **Gestión de sesiones** con títulos generados por IA, renombrar, bifurcar
- **Panel de vista de Plan** con markdown completo y comentarios
- **Gestión de servidores MCP** vía `/mcp` en la extensión
- **Visualización de compactación** como tarjeta plegable

### También Disponible
- **Plugin de JetBrains**: IntelliJ, PyCharm, WebStorm — mismas funciones principales

</v-clicks>

---
layout: image-left
image: https://images.unsplash.com/photo-1460925895917-afdab827c52f?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80
backgroundSize: cover
---

# Gestión y Control

<div class="mt-20">
  <h2 class="text-4xl font-bold text-white bg-black bg-opacity-70 px-6 py-3 rounded-lg">
    Monitorea y Controla
  </h2>
  <p class="text-xl text-white bg-black bg-opacity-70 px-4 py-2 rounded mt-4">
    Gestión de costos, contexto y permisos
  </p>
</div>

---

# Monitoreo de Costos

<v-clicks>

- Usa el comando `/cost` para verificar el uso
- Muestra el uso actual y los límites
- Plan Pro: Muestra el conteo de prompts vs el límite
- Planes Max: Muestra el resumen de uso mensual
- Los límites se reinician cada 5 horas
- Planifica con anticipación para sesiones de trabajo intensivo

</v-clicks>

```bash
# Check your current usage
/cost

# Example output (Pro Plan):
# 📊 Cost information:
#    - Input tokens: 1,245
#    - Output tokens: 3,782
#    - Total cost: $0.076

# Example output (Max Plan):
# With your Claude Max subscription, no need to monitor cost
# — your subscription includes Claude Code usage
```

---

# Gestión del Contexto

<v-clicks>

- **`/compact`** comprime el historial de conversación preservando lo esencial
- La **compactación automática** se activa cerca del límite de contexto; verás una advertencia primero
- **La manual es mejor** cuando sabes que un bloque lógico ha terminado — elige el momento
- **Las sesiones largas** se mantienen coherentes durante cientos de mensajes con compactaciones periódicas
- **`/context`** muestra qué está consumiendo la ventana actualmente

</v-clicks>

```bash
/compact   # proactive — pick your moment

# What you'll see when auto-compaction is imminent:
⚠️ Context limit approaching. Auto-compacting in next response
to preserve conversation history and continue working.
```

---

# Configuración de Permisos

<v-clicks>

- **Control detallado** sobre las capacidades de Claude Code
- **Usa la UI `/permissions`** para gestionar permisos de herramientas
- **Reglas de Permitir/Denegar** para herramientas y acciones específicas
- **Políticas empresariales** para control a nivel de toda la organización
- **Precedencia de permisos**: Enterprise → CLI → Proyecto → Usuario

</v-clicks>

```bash
# Example permission rules
Bash(npm run test:*)     # Allow npm test commands
Edit(docs/**)           # Allow editing docs directory
Read(src/*)             # Allow reading source files

# Access permissions UI
/permissions
```

---

# Modos de Permiso

<v-clicks>

### Seis modos (ciclar con `Shift+Tab`)
- **Default**: Solicita confirmación para cada acción
- **Accept Edits**: Aprueba automáticamente las ediciones de archivos
- **Plan**: Exploración de solo lectura, sin ediciones
- **Auto**: El clasificador de seguridad decide (Team/Enterprise/API)
- **Don't Ask**: Solo se ejecutan las herramientas preaprobadas (CI/CD)
- **Bypass Permissions**: Sin prompts en absoluto

</v-clicks>

```bash
# Quick permission profiles via aliases
alias claude-dev='claude --allowed-tools all'
alias claude-review='claude --allowed-tools read,grep'
alias claude-safe='claude --disabled-tools bash,webfetch'
alias claude-ci='claude --allowed-tools bash,git,test'
```

---
layout: image-left
image: https://images.unsplash.com/photo-1522071820081-009f0129c71c?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80
backgroundSize: cover
---

# Equipo y Buenas Prácticas

<div class="text-center mt-20">
  <h2 class="text-4xl font-bold text-white bg-black bg-opacity-60 px-6 py-3 rounded-lg">
    Colabora Eficazmente
  </h2>
  <p class="text-xl text-white bg-black bg-opacity-60 px-4 py-2 rounded mt-4">
    Flujos de trabajo en equipo y prácticas profesionales
  </p>
</div>

---

# Git Worktrees para Sesiones Paralelas

<v-clicks>

- **Soporte integrado para worktrees**: `claude -w` o `claude --worktree`
  - Crea automáticamente un worktree aislado + nueva rama
  - Pregunta si conservar o eliminar al salir de la sesión
- Los **worktrees manuales** también funcionan para un control total
- Comparte el historial de git mientras aísla los archivos de trabajo
- Ideal para el desarrollo de múltiples funcionalidades

</v-clicks>

```bash
# Built-in (preferred) — automatic isolation
claude -w                     # Start in new worktree
claude --worktree             # Same thing

# Manual worktrees for full control
git worktree add ../project-feature-a -b feature-a
cd ../project-feature-a && claude

# Manage worktrees
git worktree list
git worktree remove ../project-feature-a
```

---

# Prompting Efectivo y Buenas Prácticas

<div class="grid grid-cols-2 gap-8">
<div>

### Prompting Efectivo
<v-clicks>

- **Sé específico** sobre lo que quieres lograr
- **Proporciona contexto** sobre tus objetivos y restricciones
- **Usa el refinamiento iterativo** para tareas complejas
- **Incluye ejemplos** cuando sea posible para mostrar los patrones deseados

</v-clicks>

</div>
<div>

### Buenas Prácticas
<v-clicks>

- **Crea primero una rama de git** para cualquier cambio significativo
- **Confirma checkpoints regularmente** durante el desarrollo
- **Revisa todo el código generado por IA** antes de aceptarlo
- **Prueba el código generado** exhaustivamente

</v-clicks>

</div>
</div>

---

# Solución de Problemas y Configuración

<v-clicks>

### Verificación del Estado del Sistema
```bash
claude /doctor  # Diagnose installation issues
```

### Configuración Global
```bash
claude config set -g model claude-sonnet-4-6
claude config set -g verbose true
claude config set -g max_conversation_turns 10
```

### Verificar Configuración Actual
```bash
claude config list  # View all settings
echo $ANTHROPIC_API_KEY  # Verify API key
```

</v-clicks>

---

# Problemas Comunes: Instalación

<v-clicks>

- **Comando no encontrado** → Verifica PATH: `which claude` (nativo) o `npm list -g @anthropic-ai/claude-code` (instalación npm)
- **Permiso denegado** → Vuelve a ejecutar el instalador nativo; para npm, corrige el prefijo o usa sudo
- **Usuarios de Windows** → Usa WinGet o ejecútalo dentro de WSL 2 (nota: `/sandbox` requiere WSL 2, no Windows nativo ni WSL 1)

</v-clicks>

```bash
# Recommended: native installer (auto-updates)
curl -fsSL https://claude.ai/install.sh | bash

# Legacy npm (still works, no auto-update)
npm uninstall -g @anthropic-ai/claude-code
npm install -g @anthropic-ai/claude-code
```

---

# Problemas Comunes: Tiempo de Ejecución

<v-clicks>

- **Clave API no encontrada** → Establece la variable de entorno `ANTHROPIC_API_KEY`
- **Límites de velocidad** → Usa `/cost` para monitorear el uso
- **Contexto demasiado grande** → Usa `/compact` para reducir el tamaño de la conversación

</v-clicks>

```bash
# Reinstall the native binary (auto-updates after install)
curl -fsSL https://claude.ai/install.sh | bash
```

---

# Proceso de Desarrollo

<v-clicks>

- Comienza con un estado limpio de git
- Genera pruebas si no existen
- Confirma checkpoints regularmente
- Usa Claude para flujos de trabajo de git (commits, issues, merges)
- Usa git worktrees para sesiones paralelas en diferentes ramas
- Revisa los cambios antes de aceptarlos
- Prueba el código generado exhaustivamente

</v-clicks>

---

# Referencia de Comandos: Flujo de Trabajo

<v-clicks>

| Comando | Descripción |
|---------|-------------|
| `/effort low\|medium\|high` | Establecer profundidad de razonamiento |
| `/plan` | Entrar al Modo Plan desde el prompt |
| `/ultraplan` | Sesión de planificación basada en la nube |
| `/batch` | Cambios en paralelo en la base de código |
| `/loop 5m prompt` | Ejecución recurrente de prompts |
| `/memory` | Ver y gestionar la memoria automática |

</v-clicks>

---

# Referencia de Comandos: Utilidades

<v-clicks>

| Comando | Descripción |
|---------|-------------|
| `/branch` | Bifurcar la conversación (antes `/fork`) |
| `/copy N` | Copiar la N-ésima respuesta más reciente al portapapeles |
| `/context` | Obtener sugerencias de optimización del contexto |
| `/color` | Establecer el color de la barra de prompts para la sesión |
| `/powerup` | Lecciones interactivas de funcionalidades |

**Eliminados**: `/tag`, `/vim` (usa `/config`).

</v-clicks>

---

# Acceso Rápido

<div class="grid grid-cols-2 gap-8 mt-8 place-items-center">
  <div class="flex flex-col items-center">
    <h3>Documentación de Claude Code</h3>
    <QRCode
      :width="200"
      :height="200"
      type="svg"
      data="https://docs.anthropic.com/en/docs/claude-code/overview"
      :margin="5"
      :dotsOptions="{ type: 'rounded', color: '#3b82f6' }"
    />
    <p class="text-sm mt-2">docs.anthropic.com/claude-code</p>
  </div>
  <div class="flex flex-col items-center">
    <h3>Repositorio del Curso</h3>
    <QRCode
      :width="200"
      :height="200"
      type="svg"
      data="https://github.com/kousen/claude-code-training"
      :margin="5"
      :dotsOptions="{ type: 'rounded', color: '#10b981' }"
    />
    <p class="text-sm mt-2">github.com/kousen/claude-code-training</p>
  </div>
</div>

---

# Enlaces Importantes

<div class="mt-8 space-y-6 text-xl">

<v-clicks>

### 📚 Documentación de Claude Code
`https://docs.anthropic.com/en/docs/claude-code`

### 🐙 Repositorio Oficial en GitHub  
`https://github.com/anthropics/claude-code`

### 💻 Código Fuente del Curso y Ejercicios
`https://github.com/kousen/claude-code-training`

### 🆘 Soporte e Issues
`https://github.com/anthropics/claude-code/issues`

</v-clicks>

</div>

---

# ¡Gracias!

<div class="text-center">

## ¿Preguntas?

<div class="pt-12">
  <span class="text-6xl"><carbon:logo-github /></span>
</div>

**Kenneth Kousen**  
*Autor, Conferencista, Experto en Java e IA*

[kousenit.com](https://kousenit.com) | [@kenkousen](https://twitter.com/kenkousen)

</div>
