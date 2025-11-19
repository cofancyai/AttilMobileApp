# Simplified Import Guide - NO Manual UUID Work!

## 🎯 Key Improvement
**The app now automatically looks up UUIDs by name!** You only need readable names in your Excel files.

## 📁 Import Files

1. **categories_import.xlsx** - Just names and descriptions
2. **racks_import.xlsx** - Uses godown_name (not UUID)
3. **items_import.xlsx** - Uses category_name, rack_name, godown_name (not UUIDs)
4. **initial_stock_inward.xlsx** - Uses item_name (not UUID)

## 📋 Quick Import Steps

### Step 1: Create Godown
Go to **Management → Godowns** and create:
- Name: **Main Warehouse**
- Description: Primary storage
- Location: Main Kitchen

### Step 2: Import Categories
1. Open app → **Import / Export**
2. Click "Import Data" → Select "Categories"
3. Choose `categories_import.xlsx`
4. ✅ Done! 26 categories imported

### Step 3: Import Racks
1. Click "Import Data" → Select "Racks"
2. Choose `racks_import.xlsx`
3. ✅ Done! 9 racks imported (App auto-finds "Main Warehouse" UUID)

### Step 4: Import Items
1. Click "Import Data" → Select "Items"
2. Choose `items_import.xlsx`
3. ✅ Done! 170 items imported (App auto-finds all UUIDs by names)

### Step 5: Import Initial Stock
1. Click "Import Data" → Select "Initial Stock"
2. Choose `initial_stock_inward.xlsx`
3. ✅ Done! 113 inward transactions imported (App auto-finds item UUIDs)

### Step 6: Verify
Go to **Transactions → Current Stock** and verify items have correct quantities.

## ✨ What's Different?

### Old Way (Manual):
```
Excel: category_id = REPLACE_WITH_CATEGORY_UUID
You: Find category UUID, copy, paste 170 times 😫
```

### New Way (Automatic):
```
Excel: category_name = Spices & Seasonings
App: Automatically finds the UUID! 🎉
```

## 📊 File Structures

### categories_import.xlsx
| name | description |
|------|-------------|
| Spices & Seasonings | Spices & Seasonings category |

### racks_import.xlsx
| name | description | godown_name |
|------|-------------|-------------|
| Rack-1 | Rack-1 storage rack | Main Warehouse |

### items_import.xlsx
| name | category_name | rack_name | godown_name | unit_of_measure | minimum_stock_level |
|------|---------------|-----------|-------------|-----------------|---------------------|
| TATA SALT | Spices & Seasonings | Rack-1 | Main Warehouse | kg | 10.0 |

### initial_stock_inward.xlsx
| item_name | vendor_name | purchase_date | inward_quantity | price_per_unit | ... |
|-----------|-------------|---------------|-----------------|----------------|-----|
| TATA SALT | Initial Stock Import | 2025-11-19 | 43.0 | 11.0 | ... |

## 🆘 Common Issues

**"Godown 'Main Warehouse' not found"**
→ Create a godown named "Main Warehouse" first

**"Category 'XYZ' not found"**
→ Import categories before items

**"Item 'XYZ' not found"**
→ Import items before initial stock

## ✅ Import Order
1. Create Godown manually
2. Import Categories
3. Import Racks
4. Import Items
5. Import Initial Stock
6. Done!

---
**Note:** All name matching is case-insensitive. "Main Warehouse" = "main warehouse"
