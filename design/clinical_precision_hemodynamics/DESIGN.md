---
name: Clinical Precision Hemodynamics
colors:
  surface: '#f7f9fc'
  surface-dim: '#d8dadd'
  surface-bright: '#f7f9fc'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f4f7'
  surface-container: '#eceef1'
  surface-container-high: '#e6e8eb'
  surface-container-highest: '#e0e3e6'
  on-surface: '#191c1e'
  on-surface-variant: '#5b403d'
  inverse-surface: '#2d3133'
  inverse-on-surface: '#eff1f4'
  outline: '#8f706c'
  outline-variant: '#e4beb9'
  surface-tint: '#b91d1d'
  primary: '#91000a'
  on-primary: '#ffffff'
  primary-container: '#b71c1c'
  on-primary-container: '#ffcac4'
  inverse-primary: '#ffb4ab'
  secondary: '#515f74'
  on-secondary: '#ffffff'
  secondary-container: '#d5e3fc'
  on-secondary-container: '#57657a'
  tertiary: '#005120'
  on-tertiary: '#ffffff'
  tertiary-container: '#006c2d'
  on-tertiary-container: '#72ef8c'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#ffdad6'
  primary-fixed-dim: '#ffb4ab'
  on-primary-fixed: '#410002'
  on-primary-fixed-variant: '#93000b'
  secondary-fixed: '#d5e3fc'
  secondary-fixed-dim: '#b9c7df'
  on-secondary-fixed: '#0d1c2e'
  on-secondary-fixed-variant: '#3a485b'
  tertiary-fixed: '#7ffc97'
  tertiary-fixed-dim: '#62df7d'
  on-tertiary-fixed: '#002109'
  on-tertiary-fixed-variant: '#005320'
  background: '#f7f9fc'
  on-background: '#191c1e'
  surface-variant: '#e0e3e6'
typography:
  headline-xl:
    fontFamily: Plus Jakarta Sans
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
  headline-xl-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
  headline-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '700'
    lineHeight: 30px
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 18px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 14px
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-desktop: 1.5rem
  margin: 1rem
  margin-desktop: 2rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system targets laboratory technicians, blood bank coordinators, hospital inventory managers, and triage staff who operate under high-stress, zero-tolerance environments. The interface must communicate unwavering authority, clinical reliability, rigorous sterility, and immediate data interpretability. 

The aesthetic adheres to **Corporate / Modern Clinical Precision** with subtle high-information architectural density:
- **Tone:** Authoritative, clinical, analytical, and life-critical.
- **Visual Stance:** Crisp, high-contrast surfaces over neutral gray canvases. Visual elements eliminate decorative flair in favor of functional clarity, immediate inventory recognition, and unambiguous semantic alerts.
- **Emotional Response:** Inspires calm certainty, rapid triage capability, precision stock tracking, and professional trust.

## Colors

The palette balances clinical neutrality with high-consequence signaling:

- **Primary (`#B71C1C` - Deep Blood Red):** Reserved strictly for primary action states, key navigation highlights, vital identity marks, and top-tier hemovigilance summaries.
- **Secondary (`#475569` - Slate Slate Gray):** Handles structured data headers, secondary buttons, navigation frames, and foundational structural boundaries. Accented by neutral slate (`#64748B`) for tertiary labels.
- **Canvas & Surface (`#F5F7FA` - Clinical Light Gray):** Serves as the base application floor. Pure white (`#FFFFFF`) is reserved for card containers, data cells, modal backgrounds, and interactive sheets to maintain crisp contrast ratios.
- **Semantic Palette:**
  - **Sufficient Stock / Safe:** Emerald Green (`#16A34A`) with light-tint backing (`#DCFCE7`).
  - **Low Stock / Pending Review:** Amber Orange (`#D97706`) with light-tint backing (`#FEF3C7`).
  - **Critical Alert / Depleted / Expired:** Crimson Red (`#DC2626`) with soft alert wash (`#FEE2E2`).
- **Data Neutral Borders:** `#E2E8F0` provides crisp cell, input, and panel division without visual clutter.

## Typography

The type scale combines **Plus Jakarta Sans** for crisp, clinical, human-friendly sectional titles and summary statistics with **Inter** for dense, tabular information display and zero-error scannability.

- **Tabular Numerics:** All dynamic inventory counts, blood unit batch numbers, donor timestamps, and cold-storage temperatures must enforce tabular figures (`font-variant-numeric: tabular-nums`) to preserve column alignment across real-time updates.
- **Labels:** Uppercase treatment is restricted to `label-sm` badges and table header cells (`letter-spacing: 0.05em`) to differentiate column metadata from user data rows.
- **Hierarchy Rules:** Section titles use medium-to-bold weights with tight line heights to maintain high visual efficiency and minimize unnecessary page scroll.

## Layout & Spacing

The layout is built upon an 8px base rhythm optimized for responsive clinical workstations, laboratory touchpads, and multi-monitor operations rooms.

- **Grid Architecture:** 
  - **Desktop (≥ 1280px):** 12-column fluid grid, fixed 256px collapsible sidebar navigation, `margin-desktop` (2rem), and `gutter-desktop` (1.5rem).
  - **Tablet (768px – 1279px):** 8-column layout, compact iconography navigation bar, `1.25rem` margins, and `1rem` gutters.
  - **Mobile (< 768px):** Single-column stacked triage feed, full-width data accordions, `1rem` margins, bottom navigation drawer.
- **Density Control:** Core operational tables and data-dense inventory registers support a compact spacing mode (`space-xs` vertically, `space-sm` horizontally) to maximize viewport capacity during batch cross-matching operations.

## Elevation & Depth

Visual hierarchy uses clean **tonal layering and low-contrast boundaries**, supplemented by subtle medical-grade ambient diffusion to avoid visual fatigue:

- **Level 0 (Floor Canvas):** `#F5F7FA`, completely unshadowed.
- **Level 1 (Cards, Modules, Table Containers):** `#FFFFFF` surface accompanied by a crisp `1px solid #E2E8F0` border and a soft ambient drop shadow: `0 1px 3px rgba(15, 23, 42, 0.04), 0 1px 2px rgba(15, 23, 42, 0.02)`.
- **Level 2 (Dropdowns, Popovers, Floating Action Trays):** Pure white container with `1px solid #CBD5E1` and ambient elevation: `0 4px 6px -1px rgba(15, 23, 42, 0.07), 0 2px 4px -2px rgba(15, 23, 42, 0.05)`.
- **Level 3 (Emergency Modals & Critical Triage Sheets):** Pure white container, centered over an analytical backdrop scrim (`rgba(15, 23, 42, 0.5)`), elevated with `0 20px 25px -5px rgba(15, 23, 42, 0.1), 0 8px 10px -6px rgba(15, 23, 42, 0.06)`.

## Shapes

The interface embraces a **Soft (Level 1)** structural language that emphasizes medical precision and systematic order over excessive roundness:

- **Base Radius (0.25rem / 4px):** Applied to form fields, data table headers, nested segmented controls, and secondary buttons.
- **Medium Radius (0.5rem / 8px):** Applied to card panels, dashboard metrics widgets, alert boxes, and modals.
- **Pill Exception (9999px):** Used exclusively for **status badges, blood group tag indicators (e.g., A+, O-, B+), and donor availability chips** to instantly distinguish transactional labels from actionable rectangular inputs.

## Components

### Buttons
- **Primary Action (Issue Units, Cross-match, Save):** Solid Deep Blood Red (`#B71C1C`) fill, text `#FFFFFF`, 0.25rem border radius, hover state darkened to `#991B1B`. Focus state features a 2px offset ring in `#B71C1C`.
- **Secondary Action (Filter, Export, Discard):** White surface with `1px solid #CBD5E1`, text `#475569`, hovering to surface `#F8FAFC` and border `#94A3B8`.
- **Critical Action (Emergency Override, Quarantine Discard):** Crimson Red (`#DC2626`) fill with pure white label.

### Status Badges & Pills
- Pill-shaped (`rounded-full`), `px-2.5 py-0.5`, using `label-sm` weight with high color fidelity:
  - **Sufficient Stock:** `#16A34A` text on `#DCFCE7` background with a subtle `#86EFAC` border.
  - **Low / Expiring Soon:** `#D97706` text on `#FEF3C7` background with `#FDE68A` border.
  - **Critical Depletion / Incompatible:** `#DC2626` text on `#FEE2E2` background with `#FCA5A5` border.
  - **Blood Group Pill:** Monospace-aligned blood identity tags (e.g., `O-NEG`) using bold slate `#1E293B` on light gray `#E2E8F0`.

### Data Tables
- Crisp, clinical, horizontal-row structure.
- Header row uses background `#F8FAFC`, uppercase `label-sm` typography in `#64748B`, with a persistent bottom border `1px solid #E2E8F0`.
- Data rows feature alternating zero-tint hovering (`#F8FAFC`), vertical alignment centered, padded at `0.75rem 1rem`. Monospaced tabular digits for bag numbers and volume levels (mL).

### Input Fields & Controls
- Height fixed at `38px` (desktop) with `1px solid #CBD5E1`, pure white background, inner text `#0F172A`.
- Active/Focused state initiates a clear crisp `#B71C1C` primary ring with zero blur expansion.
- Checkboxes and radios feature standard 16px boxes with `#B71C1C` fill upon selection, ensuring unambiguous state confirmation.

### Cards & Metrics Blocks
- Enclosed with crisp white background, `1px solid #E2E8F0`, 0.5rem corner radius.
- Blood inventory metric cards show large numbers in `headline-lg`, an integrated linear stock meter bar with semantic fill, and blood group designation prominently anchored to the top-right.

### Modals & Dialogs
- Structured with an explicit header border, scrollable internal content region, and an anchored footer containing aligned dismissive (secondary) and affirmative (primary) action triggers.