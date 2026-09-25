import os
import re

frontend_dir = r"C:\Users\vigne\.gemini\antigravity\scratch\agentx-campus\frontend\src"

print("=== AUDITING THEME & BUTTON STYLING ===")

hardcoded_patterns = [
    (re.compile(r'bg-(stone|slate|gray|zinc|neutral)-(800|900|950)'), "Dark bg hardcoded"),
    (re.compile(r'bg-(stone|slate|gray|zinc|neutral)-(50|100|200)'), "Light bg hardcoded"),
    (re.compile(r'text-(stone|slate|gray|zinc|neutral)-(800|900|950)'), "Dark text hardcoded"),
    (re.compile(r'text-(stone|slate|gray|zinc|neutral)-(50|100|200)'), "Light text hardcoded"),
    (re.compile(r'border-(stone|slate|gray|zinc|neutral)-(700|800|900)'), "Dark border hardcoded"),
    (re.compile(r'border-(stone|slate|gray|zinc|neutral)-(100|200|300)'), "Light border hardcoded"),
    (re.compile(r'bg-\[#[0-9a-fA-F]+\]'), "Hex bg hardcoded"),
    (re.compile(r'text-\[#[0-9a-fA-F]+\]'), "Hex text hardcoded"),
    (re.compile(r'style=\{\{[^}]*(background|color)[^}]*\}\}'), "Inline style background/color")
]

findings = []

for root, dirs, files in os.walk(frontend_dir):
    for f in sorted(files):
        if f.endswith(".jsx") or f.endswith(".js"):
            filepath = os.path.join(root, f)
            relpath = os.path.relpath(filepath, frontend_dir)
            with open(filepath, "r", encoding="utf-8") as fh:
                content = fh.read()
                lines = content.splitlines()
                for i, line in enumerate(lines, 1):
                    for pat, desc in hardcoded_patterns:
                        m = pat.search(line)
                        if m:
                            # Check if line has dark: prefix for this pattern or if it is already theme-paired
                            matched_str = m.group(0)
                            # If it's preceded by dark: or followed by dark: on same element, let's see
                            idx = line.find(matched_str)
                            is_dark_variant = line[max(0, idx-5):idx] == "dark:"
                            findings.append({
                                "file": relpath,
                                "line": i,
                                "desc": desc,
                                "matched": matched_str,
                                "is_dark_variant": is_dark_variant,
                                "text": line.strip()
                            })

print(f"Total pattern matches found: {len(findings)}")

# Group by file
files_with_findings = {}
for item in findings:
    files_with_findings.setdefault(item["file"], []).append(item)

for fname, items in sorted(files_with_findings.items()):
    print(f"\n--- {fname} ({len(items)} matches) ---")
    for it in items[:15]:
        print(f"  L{it['line']} [{it['desc']} - '{it['matched']}' (dark_variant={it['is_dark_variant']})]: {it['text'][:120]}")
    if len(items) > 15:
        print(f"  ... and {len(items)-15} more in this file")
