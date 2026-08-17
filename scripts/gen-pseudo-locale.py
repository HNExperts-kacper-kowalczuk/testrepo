#!/usr/bin/env python3
"""Generate values-en-XA pseudo-locale strings for screenshot QA."""
from pathlib import Path
import re

root = Path(__file__).resolve().parents[1]
src = root / "shared/src/commonMain/composeResources/values/strings.xml"
dst = root / "docs/qa/pseudo-locale-strings.xml"
dst.parent.mkdir(parents=True, exist_ok=True)
text = src.read_text(encoding="utf-8")


def wrap_string(match: re.Match[str]) -> str:
    name = match.group("name")
    body = match.group("body")
    if body.startswith("[!!"):
        return match.group(0)
    return f'<string name="{name}">[!!{body}!!]</string>'


def wrap_item(match: re.Match[str]) -> str:
    quantity = match.group("quantity")
    body = match.group("body")
    if body.startswith("[!!"):
        return match.group(0)
    return f'<item quantity="{quantity}">[!!{body}!!]</item>'


out = re.sub(
    r'<string name="(?P<name>[^"]+)">(?P<body>.*?)</string>',
    wrap_string,
    text,
    flags=re.DOTALL,
)
out = re.sub(
    r'<item quantity="(?P<quantity>[^"]+)">(?P<body>.*?)</item>',
    wrap_item,
    out,
    flags=re.DOTALL,
)
(dst).write_text(out, encoding="utf-8")
print(f"Wrote {dst}")
