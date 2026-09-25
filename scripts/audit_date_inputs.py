import os
import re

frontend_dir = r"C:\Users\vigne\.gemini\antigravity\scratch\agentx-campus\frontend\src"
matches = []

for root, dirs, files in os.walk(frontend_dir):
    for f in sorted(files):
        if f.endswith('.jsx') or f.endswith('.js') or f.endswith('.css'):
            fpath = os.path.join(root, f)
            rel = os.path.relpath(fpath, frontend_dir)
            with open(fpath, 'r', encoding='utf-8') as fh:
                for idx, line in enumerate(fh, 1):
                    if re.search(r'type=[\'"](date|datetime-local|time)[\'"]', line):
                        matches.append((rel, idx, line.strip()))

print(f"Total date/time inputs found: {len(matches)}")
for rel, idx, line in matches:
    print(f"{rel}:{idx} -> {line[:120]}")
