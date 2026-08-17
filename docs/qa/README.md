# Screenshot / pseudo-locale QA

Compose Multiplatform does not accept an `en-XA` resource qualifier. Generate elongated copy for layout QA with:

```bash
python3 scripts/gen-pseudo-locale.py
```

That writes `docs/qa/pseudo-locale-strings.xml` (`[!!…!!]` wrappers). Use it when checking large-font result cards and Polish overflow; ship only `values` and `values-pl` in the app.
