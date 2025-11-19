# Database Import Guide

This guide explains how to import the 171 items from `Items_by_Rack_with_Categories.xlsx` into your Attil Inventory database.

## 📊 Overview

Your Excel file has been processed and split into 5 import files:

1. **categories_import.xlsx** (26 categories)
2. **racks_import.xlsx** (9 racks)
3. **items_import.xlsx** (170 items)
4. **initial_stock_inward.xlsx** (113 items with stock)
5. **expected_current_stock_reference.xlsx** (Reference file - not for import)

## 🗄️ Database Structure

### Items Table
```
- id (UUID, auto-generated)
- name (String)
- category_id (UUID, foreign key → categories)
- godown_id (UUID, nullable, foreign key → godowns)
- rack_id (UUID, nullable, foreign key → racks)
- unit_of_measure (String: kg, pcs, ltr)
- minimum_stock_level (Double)
- is_active (Boolean, default true)
```

### Current Stock (Database View)
```
Current stock is automatically calculated as:
current_stock = SUM(inward_quantity) - SUM(outward_quantity)

You don't insert directly into current_stock.
Instead, you create inward transactions to add stock.
```

## 📋 Step-by-Step Import Process

### Step 1: Import Categories

**File:** `categories_import.xlsx`

Open Supabase or your database admin panel and import categories:

```sql
-- Example INSERT (you can bulk import from Excel)
INSERT INTO categories (name, description)
VALUES ('Spices & Seasonings', 'Spices & Seasonings category');
```

**Action:** Import all 26 categories from the file.

**Result:** Note down the category UUIDs or create a mapping table:
- Category Name → Category UUID

---

### Step 2: Get/Create Godown

Before importing racks and items, you need a Godown (warehouse).

**Option A:** Use existing godown
```sql
SELECT id, name FROM godowns WHERE is_active = true LIMIT 1;
```

**Option B:** Create new godown
```sql
INSERT INTO godowns (name, description, location, is_active)
VALUES ('Main Warehouse', 'Primary storage facility', 'Main Kitchen', true)
RETURNING id;
```

**Result:** Note down the godown UUID: `GODOWN_UUID`

---

### Step 3: Import Racks

**File:** `racks_import.xlsx`

1. Open the file
2. Replace all `REPLACE_WITH_GODOWN_UUID` with your actual godown UUID
3. Import to database:

```sql
-- Example for Rack-1
INSERT INTO racks (name, description, godown_id, is_active)
VALUES ('Rack-1', 'Rack-1 storage rack', 'YOUR_GODOWN_UUID', true);
```

**Action:** Import all 9 racks (Rack-1 through Rack-9)

**Result:** Note down the rack UUIDs or create a mapping:
- Rack Name → Rack UUID

---

### Step 4: Import Items

**File:** `items_import.xlsx`

This is the most important step. The file contains 170 items with these columns:

| Column | Description | Action Required |
|--------|-------------|-----------------|
| name | Item name | No change |
| category_name | For reference | Delete or ignore |
| category_id | Replace with UUID | **REPLACE** |
| rack_name | For reference | Delete or ignore |
| rack_id | Replace with UUID | **REPLACE** |
| godown_id | Replace with UUID | **REPLACE** |
| unit_of_measure | kg, pcs, ltr | No change |
| minimum_stock_level | Number | No change |
| is_active | true | No change |

**Steps:**

1. Open `items_import.xlsx`
2. Create a lookup formula or use find/replace to map:
   - Category names → Category UUIDs (from Step 1)
   - Rack names → Rack UUIDs (from Step 3)
   - All godown_id → Your godown UUID (from Step 2)

3. Remove or ignore the "for reference" columns

4. Import to database:

```sql
-- Example for TATA SALT
INSERT INTO items (name, category_id, godown_id, rack_id, unit_of_measure, minimum_stock_level, is_active)
VALUES (
  'TATA SALT',
  'CATEGORY_UUID_FOR_SPICES_AND_SEASONINGS',
  'YOUR_GODOWN_UUID',
  'RACK_UUID_FOR_RACK1',
  'kg',
  10.0,
  true
);
```

**Action:** Import all 170 items

**Result:** Note down item UUIDs or create mapping:
- Item Name → Item UUID

---

### Step 5: Import Initial Stock (Inward Transactions)

**File:** `initial_stock_inward.xlsx`

This file contains 113 items that have closing stock > 0.

**Columns:**

| Column | Description | Action Required |
|--------|-------------|-----------------|
| item_name | For reference | Delete or ignore |
| item_id | Replace with UUID | **REPLACE** |
| vendor_name | "Initial Stock Import" | No change |
| purchase_date | Today's date | No change |
| inward_quantity | Closing stock from Excel | No change |
| price_per_unit | Rate from Excel | No change |
| price_without_gst | Rate from Excel | No change |
| gst_percentage | 5% (assumed) | Adjust if needed |
| price_with_gst | Rate + GST | Adjust if needed |
| bill_number | "INITIAL-STOCK" | No change |
| created_by | User UUID | **REPLACE** |

**Steps:**

1. Open `initial_stock_inward.xlsx`
2. Map item names to item UUIDs (from Step 4)
3. Get your user UUID:
   ```sql
   SELECT id FROM users WHERE username = 'your_username';
   ```
4. Replace all `REPLACE_WITH_USER_UUID` with your user UUID
5. Import to database:

```sql
-- Example for TATA SALT
INSERT INTO inward_items (
  item_id,
  vendor_name,
  purchase_date,
  inward_quantity,
  price_per_unit,
  price_without_gst,
  gst_percentage,
  price_with_gst,
  bill_number,
  created_by
) VALUES (
  'ITEM_UUID_FOR_TATA_SALT',
  'Initial Stock Import',
  '2025-11-19',
  43.0,
  11.0,
  11.0,
  5.0,
  11.55,
  'INITIAL-STOCK',
  'YOUR_USER_UUID'
);
```

**Action:** Import all 113 inward transactions

**Result:** Your database now has initial stock for all items!

---

### Step 6: Verify Current Stock

**File:** `expected_current_stock_reference.xlsx` (Reference only)

After importing, verify your current stock matches expectations:

```sql
SELECT
  item_name,
  category_name,
  rack_name,
  unit_of_measure,
  minimum_stock_level,
  current_stock,
  is_low_stock
FROM current_stock
ORDER BY item_name;
```

Compare the results with `expected_current_stock_reference.xlsx`.

**Expected Results:**
- 170 items in the database
- 113 items with stock > 0
- 57 items with stock = 0 (no closing stock in original Excel)

---

## 🔧 Automated Import Script (Optional)

If you want to automate the import, you can use the Supabase REST API or create a bulk import script.

### Using Supabase REST API:

```bash
# Example: Import categories
curl -X POST 'https://YOUR_PROJECT.supabase.co/rest/v1/categories' \
  -H "apikey: YOUR_API_KEY" \
  -H "Authorization: Bearer YOUR_API_KEY" \
  -H "Content-Type: application/json" \
  -d '[
    {"name": "Spices & Seasonings", "description": "Spices & Seasonings category"},
    {"name": "Rice & Grains", "description": "Rice & Grains category"}
  ]'
```

---

## 📊 Summary Statistics

| Entity | Count | File |
|--------|-------|------|
| Categories | 26 | categories_import.xlsx |
| Racks | 9 | racks_import.xlsx |
| Godowns | 1 | (Create manually) |
| Items | 170 | items_import.xlsx |
| Items with Stock | 113 | initial_stock_inward.xlsx |
| Items without Stock | 57 | (No inward needed) |

---

## ⚠️ Important Notes

1. **UUID Replacements:** You MUST replace all placeholder UUIDs before importing:
   - `REPLACE_WITH_CATEGORY_UUID` → Actual category UUIDs
   - `REPLACE_WITH_RACK_UUID` → Actual rack UUIDs
   - `REPLACE_WITH_GODOWN_UUID` → Actual godown UUID
   - `REPLACE_WITH_ITEM_UUID` → Actual item UUIDs
   - `REPLACE_WITH_USER_UUID` → Actual user UUID

2. **Import Order:** Follow the exact order (Categories → Godown → Racks → Items → Inward)

3. **Foreign Keys:** Ensure all foreign key references exist before importing child records

4. **GST Calculation:** The script assumes 5% GST for all items. Adjust if your tax rates differ.

5. **Current Stock View:** After importing inward transactions, the `current_stock` view should automatically show the correct stock levels.

---

## 🆘 Troubleshooting

### Issue: "Foreign key constraint violation"
**Solution:** Ensure parent records (categories, racks, godowns) exist before importing items

### Issue: "Current stock is not showing"
**Solution:** Check if your database has a `current_stock` view. If not, create it:

```sql
CREATE OR REPLACE VIEW current_stock AS
SELECT
  i.id AS item_id,
  i.name AS item_name,
  c.name AS category_name,
  g.name AS godown_name,
  r.name AS rack_name,
  i.unit_of_measure,
  i.minimum_stock_level,
  COALESCE(SUM(inw.inward_quantity), 0) AS total_inward,
  COALESCE(SUM(out.outward_quantity), 0) AS total_outward,
  COALESCE(SUM(inw.inward_quantity), 0) - COALESCE(SUM(out.outward_quantity), 0) AS current_stock,
  (COALESCE(SUM(inw.inward_quantity), 0) - COALESCE(SUM(out.outward_quantity), 0)) < i.minimum_stock_level AS is_low_stock,
  i.created_at,
  i.updated_at
FROM items i
LEFT JOIN categories c ON i.category_id = c.id
LEFT JOIN godowns g ON i.godown_id = g.id
LEFT JOIN racks r ON i.rack_id = r.id
LEFT JOIN inward_items inw ON i.id = inw.item_id
LEFT JOIN outward_items out ON i.id = out.item_id
WHERE i.is_active = true
GROUP BY i.id, i.name, c.name, g.name, r.name, i.unit_of_measure, i.minimum_stock_level, i.created_at, i.updated_at;
```

### Issue: "Duplicate item names"
**Solution:** The original Excel has 171 rows but one item might be duplicated. Review and merge duplicates.

---

## ✅ Verification Checklist

- [ ] All 26 categories imported
- [ ] 1 godown created/selected
- [ ] All 9 racks imported with correct godown_id
- [ ] All 170 items imported with correct category_id and rack_id
- [ ] All 113 inward transactions imported
- [ ] Current stock view shows correct quantities
- [ ] Low stock items are flagged correctly
- [ ] All foreign key relationships are valid

---

**Generated on:** 2025-11-19
**Source File:** Items_by_Rack_with_Categories.xlsx (171 items, 9 racks, 26 categories)
