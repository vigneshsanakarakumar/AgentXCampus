import os
import re

frontend_dir = r"C:\Users\vigne\.gemini\antigravity\scratch\agentx-campus\frontend\src"

print("=== AUDITING ALL BUTTONS AND INTERACTIVE ELEMENTS ===")

buttons = []
for root, dirs, files in os.walk(frontend_dir):
    for f in sorted(files):
        if f.endswith(".jsx"):
            filepath = os.path.join(root, f)
            rel = os.path.relpath(filepath, frontend_dir)
            with open(filepath, "r", encoding="utf-8") as fh:
                content = fh.read()
                # Find all <button ... > and <Button ... >
                for m in re.finditer(r'<(button|Button)\b([^>]*)>', content):
                    tag = m.group(1)
                    attrs = m.group(2)
                    cls_m = re.search(r'className=["\'`{]([^"\'`}]*)["\'`}]', attrs)
                    cls = cls_m.group(1) if cls_m else ""
                    variant_m = re.search(r'variant=["\'`{]([^"\'`}]*)["\'`}]', attrs)
                    variant = variant_m.group(1) if variant_m else ""
                    buttons.append({
                        "file": rel,
                        "tag": tag,
                        "variant": variant,
                        "class": cls
                    })

print(f"Total buttons found: {len(buttons)}")

# Analyze button classes
classes_summary = {}
for b in buttons:
    c = b["class"]
    v = b["variant"]
    key = f"{b['tag']}[variant={v}]: {c}"
    classes_summary[key] = classes_summary.get(key, 0) + 1

print("\n--- Button Patterns Found ---")
for k, count in sorted(classes_summary.items(), key=lambda x: x[1], reverse=True)[:35]:
    print(f"[{count}x] {k}")

# Check for hardcoded colors in buttons
print("\n--- Buttons with hardcoded colors or dark: overrides ---")
for b in buttons:
    c = b["class"]
    if any(h in c for h in ["bg-slate", "bg-gray", "bg-zinc", "bg-stone", "dark:", "bg-black", "bg-white", "text-black"]):
        print(f"{b['file']} <{b['tag']} variant='{b['variant']}'>: {c}")
