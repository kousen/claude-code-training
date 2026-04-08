# Security Review Report

**Project:** lyrics-display
**Date:** 2026-04-08
**Reviewer:** Claude Code

## Executive Summary

This is a static client-side web application with no server-side code, no database, no authentication, and no user input beyond button clicks and a range slider. The attack surface is minimal.

**Risk Level:** Low
**Issues Found:** 3

## Findings

### [LOW] innerHTML used for button label updates

**Location:** `app.js:85`, `app.js:95`
**Category:** XSS (Potential)

**Description:**
The Play/Pause button text is set via `innerHTML`. While the current values are hardcoded HTML entities (no user data flows into them), `innerHTML` as a pattern is a common XSS vector when code evolves. Using `textContent` where possible prevents accidental introduction of XSS if these lines are later modified to include dynamic content.

**Vulnerable Code:**

```javascript
btn.innerHTML = '&#9646;&#9646; Pause';
btn.innerHTML = '&#9654; Play';
```

**Remediation:**

```javascript
btn.textContent = '\u25AE\u25AE Pause';
btn.textContent = '\u25B6 Play';
```

**References:**
- [OWASP DOM-based XSS](https://owasp.org/www-community/attacks/DOM_Based_XSS)

---

### [LOW] No error handling on fetch

**Location:** `app.js:8-11`
**Category:** Error Handling

**Description:**
The `fetch('lyrics.txt')` call has no error handling. If the file is missing or the server is unreachable, the app silently fails with "Loading lyrics..." stuck on screen. While not a direct security vulnerability, unhandled errors can leak information in stack traces and create a poor user experience that might lead users to suspect something more serious.

**Vulnerable Code:**

```javascript
async function loadLyrics() {
    const response = await fetch('lyrics.txt');
    const text = await response.text();
    const lines = text.split('\n').filter(line => line.trim() !== '');
    state = createLyricsState(lines);
    render(false);
}
```

**Remediation:**

```javascript
async function loadLyrics() {
    try {
        const response = await fetch('lyrics.txt');
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const text = await response.text();
        const lines = text.split('\n').filter(line => line.trim() !== '');
        if (lines.length === 0) throw new Error('No lyrics found');
        state = createLyricsState(lines);
        render(false);
    } catch (err) {
        document.getElementById('lyric-line').textContent =
            'Could not load lyrics.';
    }
}
```

---

### [LOW] No Content Security Policy

**Location:** `index.html`
**Category:** Configuration

**Description:**
The page loads Google Fonts from an external CDN but has no Content Security Policy (CSP) meta tag. If this page were ever served from a domain (rather than localhost), a CSP header would limit the damage from any future XSS by restricting which origins can serve scripts, styles, and fonts.

**Remediation:**
Add a CSP meta tag to the `<head>`:

```html
<meta http-equiv="Content-Security-Policy"
      content="default-src 'self'; style-src 'self' https://fonts.googleapis.com; font-src https://fonts.gstatic.com;">
```

**References:**
- [MDN Content Security Policy](https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP)

---

## Dependency Audit

`npm audit` reports **0 vulnerabilities**. All dependencies (vitest, jsdom) are dev-only and not shipped to users.

## Recommendations

1. Replace `innerHTML` with `textContent` using Unicode characters -- simple one-line fix
2. Add try/catch around the fetch call with a user-friendly error message
3. Consider adding a CSP meta tag if ever deployed beyond localhost
