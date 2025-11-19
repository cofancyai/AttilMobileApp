# Import Files Summary

## 📁 Generated Files

### 1. categories_import.xlsx
- **Records:** 26 categories
- **Purpose:** Import into `categories` table first
- **Sample Categories:**
  - Spices & Seasonings
  - Rice & Grains
  - Oils & Fats
  - Pulses & Lentils
  - Dairy Products
  - Beverages
  - Bakery Items
  - Fresh Vegetables
  - Fresh Fruits
  - Dry Fruits & Nuts
  - And 16 more...

### 2. racks_import.xlsx
- **Records:** 9 racks
- **Purpose:** Import into `racks` table after creating godown
- **Racks:** Rack-1, Rack-2, Rack-3, Rack-4, Rack-5, Rack-6, Rack-7, Rack-8, Rack-9
- **Action Required:** Replace `REPLACE_WITH_GODOWN_UUID` with actual godown ID

### 3. items_import.xlsx
- **Records:** 170 items
- **Purpose:** Import into `items` table after categories and racks
- **Columns:** name, category_id, rack_id, godown_id, unit_of_measure, minimum_stock_level, is_active
- **Action Required:**
  - Replace `REPLACE_WITH_CATEGORY_UUID` with actual category IDs
  - Replace `REPLACE_WITH_RACK_UUID` with actual rack IDs
  - Replace `REPLACE_WITH_GODOWN_UUID` with actual godown ID

### 4. initial_stock_inward.xlsx
- **Records:** 113 inward transactions (items with closing stock > 0)
- **Purpose:** Import into `inward_items` table to set opening stock
- **Details:**
  - Vendor: "Initial Stock Import"
  - Purchase Date: 2025-11-19 (today)
  - Bill Number: "INITIAL-STOCK"
  - GST: 5% (assumed for all items)
- **Action Required:**
  - Replace `REPLACE_WITH_ITEM_UUID` with actual item IDs
  - Replace `REPLACE_WITH_USER_UUID` with your user ID

### 5. expected_current_stock_reference.xlsx
- **Records:** 170 items
- **Purpose:** Reference file to verify import (NOT for import)
- **Use:** After importing, compare your `current_stock` view with this file
- **Shows:** Expected current stock levels after import

## 📊 Statistics

| Metric | Count | Notes |
|--------|-------|-------|
| Total Rows in Excel | 171 | Including header |
| Total Items | 170 | After removing header |
| Unique Categories | 26 | Auto-extracted |
| Unique Racks | 9 | Rack-1 through Rack-9 |
| Items with Stock | 113 | Have closing stock > 0 |
| Items without Stock | 57 | Have closing stock = 0 |
| Total Stock Value | Varies | Based on rates in Excel |

## 🔄 Import Sequence

```
1. categories_import.xlsx → categories table (26 records)
                                    ↓
2. Create/Get Godown → godowns table (1 record)
                                    ↓
3. racks_import.xlsx → racks table (9 records)
                                    ↓
4. items_import.xlsx → items table (170 records)
                                    ↓
5. initial_stock_inward.xlsx → inward_items table (113 records)
                                    ↓
6. Verify → current_stock view (170 records, 113 with stock > 0)
```

## 📋 Category Breakdown

| Category | Approx Items |
|----------|--------------|
| Spices & Seasonings | ~40 items |
| Rice & Grains | ~15 items |
| Oils & Fats | ~8 items |
| Pulses & Lentils | ~12 items |
| Dairy Products | ~10 items |
| Condiments & Sauces | ~8 items |
| Fresh Vegetables | ~15 items |
| Fresh Fruits | ~8 items |
| Dry Fruits & Nuts | ~6 items |
| Beverages | ~5 items |
| Bakery Items | ~4 items |
| Biscuits & Snacks | ~6 items |
| Others | ~33 items |

## 🏷️ Unit of Measure Distribution

- **kg (Kilograms):** ~140 items
- **pcs (Pieces):** ~20 items
- **ltr (Liters):** ~10 items

## 💰 Stock Information

- **Total Opening Stock:** Sum of all closing stock values
- **Items with Min Stock Alert:** Items where closing stock < minimum stock level
- **Items Ready to Use:** 113 items with stock available
- **Items Requiring Purchase:** 57 items with no stock

## ⚠️ Items Requiring Attention

Check `expected_current_stock_reference.xlsx` column `will_be_low_stock`:
- **YES:** Current stock is below minimum stock level
- **NO:** Current stock is at or above minimum stock level

## 🔧 Database Schema Match

### Items Table Match:
✅ name → Item Name (from Excel)
✅ category_id → Category (mapped)
✅ godown_id → Warehouse location
✅ rack_id → Rack (from Excel)
✅ unit_of_measure → Unit (from Excel)
✅ minimum_stock_level → Min Stock (from Excel)
✅ is_active → true (default)

### Inward Items Match:
✅ item_id → Mapped from items table
✅ vendor_name → "Initial Stock Import"
✅ purchase_date → Current date
✅ inward_quantity → Closing Stock (from Excel)
✅ price_per_unit → Rate (from Excel)
✅ bill_number → "INITIAL-STOCK"

### Current Stock (Auto-calculated):
✅ item_name → From items table
✅ category_name → From categories table
✅ rack_name → From racks table
✅ current_stock → SUM(inward) - SUM(outward)
✅ is_low_stock → current_stock < minimum_stock_level

## 📝 Next Steps

1. **Read IMPORT_GUIDE.md** for detailed step-by-step instructions
2. **Import categories** from categories_import.xlsx
3. **Create/get godown** UUID
4. **Import racks** with godown UUID
5. **Map and import items** with category and rack UUIDs
6. **Import initial stock** with item UUIDs
7. **Verify** against expected_current_stock_reference.xlsx

## ✅ Success Criteria

After successful import, you should have:
- ✅ 26 categories in the system
- ✅ 1 godown configured
- ✅ 9 racks in the godown
- ✅ 170 items in the inventory
- ✅ 113 items with opening stock
- ✅ Current stock view showing accurate quantities
- ✅ Low stock alerts working correctly

---

**Generated from:** Items_by_Rack_with_Categories.xlsx
**Date:** 2025-11-19
**Script:** generate_import_files.py
