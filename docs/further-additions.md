# Suggested further additions

These are **recommendations**, not v1 requirements. The first release should stay: offline barcode + INCI OCR, evaluation, personal avoid-list, i18n, non-blocking banners.

Items are grouped by how much they help a shopper **choose a suitable product** without breaking offline-first or trust.

Priority:

| Tag | Meaning |
| --- | --- |
| **P1** | High value, fits the current domain; plan the data model so it is cheap to add after v1 |
| **P2** | Useful once the core scanner is trusted |
| **P3** | Nice-to-have or easy to get wrong (data quality / medical claims) |
| **Avoid** | Conflicts with the product principles |

---

## 1. Make evaluation more accurate (P1)

### 1.1 Product type: leave-on vs rinse-off

Many Annex III limits depend on **use** (rinse-off, leave-on, mucous membranes, children). A shampoo and a face cream with the same INCI should not always get the same comment.

- Store `product.usage` (`RINSE_OFF`, `LEAVE_ON`, `LIP`, `EYE`, `SPRAY`, `UNKNOWN`).
- `HazardPolicy` already receives the formula; pass usage in.
- If usage is unknown (OCR-only scan), ask one tap: “How do you use this?” before showing the final rating, or show the **stricter** interpretation with a note.

This is the single most useful scoring upgrade after the avoid-list.

### 1.2 Verify pack vs catalog (reformulation)

Crowd-sourced INCI lists go stale. After a barcode hit, offer **“Check the label”**: OCR the box and diff against the catalog list.

- Same ingredients → extra trust badge.
- Mismatch → prefer the **photographed** list, mark the catalog row as unverified, queue a local “formula changed” report for later sync.

This turns OCR from a fallback into a **quality tool**.

### 1.3 INCI position as a weak concentration signal

INCI is descending concentration (with the usual 1% exception). Do not invent percentages, but:

- Highlight concerns in the **first five names**.
- Soften “trace-like” placement only in comments (“usually near the end of the list”), never by hiding a `PROHIBITED` item.

### 1.4 Extra INCI syntax the parser should learn

| Pattern | Why |
| --- | --- |
| `… (nano)` / `[nano]` | Nanomaterial labelling in the EU |
| `Parfum/Fragrance` plus allergen list below | EU 26/80 allergens often listed after the main INCI |
| `Aqua (Water)` | Already in aliases; keep expanding |
| Multiple INCI blocks (box + leaflet) | Allow **add another photo** and merge token lists |

Multi-shot OCR matters: full INCI often wraps around the bottle.

### 1.5 Fuzzy-match confirmation

If a token matched only by Levenshtein, show “We read **NIACINAM1DE** as **Niacinamide** — is that right?” rather than silently accepting. One extra tap prevents false HIGH/SAFE.

---

## 2. Stronger “suitable for *me*” (P1–P2)

v1 already has pregnancy, fragrance-free, and a free-form avoid-list. Structure the common cases so users do not have to know INCI names.

| Profile preset | Behaviour |
| --- | --- |
| **EU fragrance allergens** | Checklist of the labelled allergens (26, expanding toward 80). Checking one adds those INCI IDs to the avoid-list. |
| **Children / baby** | Stricter interpretation of restricted substances and “not for children under 3” style annex conditions. |
| **Essential oils / limonene–linalool cluster** | One switch, many related ingredients. |
| **Alcohol in leave-on** | Avoid `ALCOHOL DENAT.` etc. only when usage is leave-on. |

Keep **skin-disease treatment** out (see Avoid). “Eczema-friendly” as a medical claim is a trap. A user-defined avoid-list is enough for “I react to methylisothiazolinone”.

Optional P2: **phototoxicity / “not before sun”** tag on some essential oils and AHAs — as a comment badge, not a diagnosis.

---

## 3. Choose among products, not only judge one (P2)

The original goal is choosing the **most suitable** option, which implies comparison.

- **Save / shelf:** star a product; list lives in `user.sqlite`.
- **Compare two (or three) scans** side by side: overall rating, unique HIGH ingredients, shared allergens. Offline, from history + shelf.
- **Local alternatives:** same `category` + better `overall` in the bundled catalog. Never pretend the shop has it in stock. Label “In this app’s catalog”, not “buy this”.
- **History insight:** “Among products you scanned, these three ingredients appear most often as HIGH.” Helps someone clean up a bathroom shelf.

Do this after matcher + OCR are trustworthy, or comparison will amplify errors.

---

## 4. Scanning UX additions (P1–P2)

| Addition | Why |
| --- | --- |
| **Type the barcode** | Damaged EAN, or user copied digits from a receipt |
| **Scan barcode from gallery** | Screenshot of an online listing (still evaluate offline if GTIN is in the DB) |
| **Torch, haptic, debounce** | Already in the camera plan; keep them |
| **App shortcut / quick tile** | “Scan” from the home screen |
| **Home: last 3 scans** | Faster than opening History |
| **Paste INCI** | Already in the unknown-product flow; also from Home for power users |

Unknown GTIN: store it locally even if evaluation used OCR, so a later catalog delta can attach a name.

---

## 5. Trust, accessibility, privacy (P1)

### 5.1 Ratings that are not colour-only

Traffic lights fail for colour vision deficiency and in bright shops.

- Each rating: **icon + short word + colour** (and a pattern, e.g. fill vs outline).
- TalkBack: “Avoid. Two prohibited ingredients. Three names unidentified.”
- Do **not** let Material You recolour rating semantics.

### 5.2 Onboarding that sets expectations

Three short screens: what a scan is, what “Unknown” means, this is not a doctor. Required disclaimer acknowledgement stored locally. Reduces 1-star “it said unknown so the app is broken” reviews.

### 5.3 Backup without accounts

Android Auto Backup / iOS iCloud of `user.sqlite` (preferences, shelf, history). Matches “no login” and survives phone replacement.

### 5.4 Clear local data

Settings: wipe history, wipe shelf, reset language. Required for trust and store privacy forms.

### 5.5 Optional “report” queue

Wrong name, wrong INCI, missing product: saved on device, flushed when sync exists. No account. Helps the catalog without a social network.

---

## 6. Knowledge, not only a score (P2)

- **Ingredient encyclopedia:** search the knowledge base without scanning (name, alias, CAS). Reuses the same comment rows.
- **Ruleset changelog:** “Catalog 2026-08: …” in Settings ([plan-after-twenty-three.md](plan-after-twenty-three.md) phase 24). Makes offline data feel alive. Not a live CosIng feed.
- **Watch saved products** after sync: if a starred GTIN’s INCI hash changes, badge it. (Needs sync; design the hash in v1.)

Educational copy must stay factual (annex, function). No “causes cancer” headlines.

---

## 7. Second scores — keep them separate (P2–P3)

People will ask for vegan, cruelty-free, microplastics, palm oil, “natural”.

**If you add them, they must not mix into the danger traffic light.** A product can be vegan and still Annex II. UI: safety rating first; optional chips below.

| Layer | Data problem |
| --- | --- |
| Microplastics / “liquid plastic” | Needs a maintained tag list; doable offline |
| Vegan / animal-derived (carmine, keratin, lactose) | Partial INCI lists; many false negatives |
| Cruelty-free / Leaping Bunny | Brand-level, changes, needs updates — weak offline |
| Endocrine-disruptor extra flag | Overlaps SCCS/annex; easy to double-count fear |

Recommendation: **microplastics tag as P2**; vegan/cruelty as P3 only with a clear “incomplete” disclaimer.

---

## 8. Monetization that still respects UX (P2)

Already planned: banners that collapse. Further options that do **not** fight the scanner:

- **Optional IAP “Remove ads”** — banner slot stays collapsed (layout already allows this).
- **No** rewarded-ad “unlock this ingredient comment”. Comments are the product.
- **No** daily scan limit.

A later “supporter” IAP is cleaner than putting paywalls on safety data.

---

## 9. Platform polish (P2)

- Share result as **plain text or a simple image** (product name, rating, date, disclaimer). No tracking links.
- System share sheet only; no social graph.
- Large-font / landscape: result header stays visible; ingredient list scrolls; banner still reserved, not overlaying.
- Foldables: camera in one pane, result in the other is optional; not required for v1.
- OCR language packs: Latin is enough for INCI. CJK packaging is a different OCR model — only if you target those markets.

---

## 10. What not to add (or not as a safety score)

| Idea | Why not (as core) |
| --- | --- |
| Accounts, cloud shelf, friends | Breaks offline/privacy story; v1 explicitly out of scope |
| Interstitials / app-open ads | Blocks the shelf flow |
| Live prices / “buy on Amazon” | Wrong job; affiliate pressure vs honest ratings |
| Dermatological diagnosis, “safe for eczema” medical mode | Regulatory and ethical risk |
| Comedogenicity 0–5 as science | Poor evidence; if ever, a separate optional tag with a huge caveat |
| Cloud OCR | Breaks airplane-mode guarantee |
| Treating CosIng search as a live legal oracle | Stale the moment the phone is offline; annex snapshot is enough |
| Gamification / streaks | Wrong tone for a safety tool |

---

## 11. Suggested order after v1

Catalog scale, four-corner INCI OCR, and Fitatu-inspired UI: **[plan-catalog-ocr-ui.md](plan-catalog-ocr-ui.md)** (largely done). Bundled catalog through share/gallery polish: **[plan-next-phases.md](plan-next-phases.md)** (phases 1–5, shipped).

Phases 6–15 in **[plan-further-improvements.md](plan-further-improvements.md)** and 16–20 in **[plan-after-fifteen.md](plan-after-fifteen.md)** are **shipped**. Phases 21–26 in **[plan-after-twenty.md](plan-after-twenty.md)** and **[plan-after-twenty-three.md](plan-after-twenty-three.md)** are shipped (this PR is the in-app theme override). Remaining: accessibility / TalkBack in §5.1 and items still listed here.

Still out of git: real Remove-ads IAP, production AdMob / sync URLs, vegan/cruelty as safety scores, foldables, CJK OCR.

---

## 12. Relation to v1 success criteria

None of the P1 items should delay the first installable evaluator. They should not require new network calls. They **should** influence v1 schema so you do not migrate SQLite twice.
