# Sample Import Files for Attil Inventory Management

This folder contains comprehensive sample Excel files ready for import into the Attil Inventory Management system.

## 📊 Files Included

1. **godowns_import.xlsx** - 3 sample godowns (storage facilities)
2. **racks_import.xlsx** - 8 sample racks organized by godown
3. **categories_import.xlsx** - 8 item categories
4. **cuisines_import.xlsx** - 4 cuisine types
5. **items_import.xlsx** - 40 items (5 items per category)
6. **initial_stock_inward.xlsx** - 40 initial stock entries for all items

## 🚀 Import Order (IMPORTANT!)

Import files in this exact order to avoid foreign key errors:

```
1. godowns_import.xlsx       → Creates storage locations
2. racks_import.xlsx          → Creates racks within godowns
3. categories_import.xlsx     → Creates item categories
4. cuisines_import.xlsx       → Creates cuisine types
5. items_import.xlsx          → Creates items (references categories, racks, godowns)
6. initial_stock_inward.xlsx  → Adds initial stock (references items, cuisines)
```

## 📦 Sample Data Overview

### Categories (8 categories with 5 items each)
- **RICE** - Basmati, Sona Masoori, Ponni, Brown, Jasmine
- **PULSES** - Toor, Moong, Chana, Urad, Masoor Dal
- **SPICES** - Turmeric, Chilli, Coriander, Garam Masala, Cumin
- **OIL** - Sunflower, Mustard, Refined, Coconut, Olive
- **VEGETABLES** - Onion, Potato, Tomato, Carrot, Cabbage
- **DAIRY** - Milk Powder, Paneer, Butter, Cheese, Curd
- **SNACKS** - Chips, Biscuits, Namkeen, Cookies, Crackers
- **BEVERAGES** - Tea, Coffee, Juice, Soft Drinks, Water

### Godowns (3 storage locations)
- **MAIN GODOWN** - Ground Floor, Building A (Rice, Pulses, Spices, Oil)
- **COLD STORAGE** - Basement, Building B (Vegetables, Dairy)
- **PANTRY** - First Floor, Building A (Snacks, Beverages)

### Racks (8 organized racks)
- **RACK A1** - Rice section (MAIN GODOWN)
- **RACK A2** - Pulses section (MAIN GODOWN)
- **RACK B1** - Spices section (MAIN GODOWN)
- **RACK B2** - Oil section (MAIN GODOWN)
- **RACK C1** - Fresh vegetables (COLD STORAGE)
- **RACK C2** - Dairy products (COLD STORAGE)
- **RACK D1** - Snacks (PANTRY)
- **RACK D2** - Beverages (PANTRY)

### Cuisines (4 types)
- NORTH INDIAN
- SOUTH INDIAN
- CHINESE
- CONTINENTAL

## 📝 Unit Formats Used

All unit variations are supported thanks to smart normalization:

- **kg** - Kilograms (also accepts: KG, kgs, kilogram, kilograms)
- **pcs** - Pieces (also accepts: PCS, pc, piece, pieces)
- **liters** - Liters (also accepts: ltr, ltrs, l, L, litre, litres)
- **grams** - Grams (also accepts: gm, gms, g, G, gram)
- **packets** - Packets (also accepts: pkt, pkts, packet)

## 💡 How to Use

### In the App:
1. Go to **Import / Export** screen
2. Select **Import Data** tab
3. Choose import type (e.g., "Godowns")
4. Click **Choose File** and select the corresponding Excel file
5. Click **Import**
6. Repeat for each file in order

### Expected Results:
- ✅ 3 godowns imported
- ✅ 8 racks imported
- ✅ 8 categories imported
- ✅ 4 cuisines imported
- ✅ 40 items imported (5 per category)
- ✅ 40 initial stock entries imported

## 🔄 Starting Fresh

If you want to start with clean sample data:
1. Clear your database (if needed)
2. Import files in order listed above
3. Your system will have 40 items across 8 categories with initial stock

## 📈 What You'll Have

After importing all files:
- **40 items** organized by category
- Items distributed across **3 godowns** and **8 racks**
- **Initial stock** for all items with:
  - Realistic quantities (20-200 units)
  - Vendor information
  - Purchase dates
  - GST calculations (18%)
  - Expiry dates (1 year from purchase)
  - Bill numbers
  - Cuisine associations where applicable

## ⚠️ Important Notes

1. **Import Order Matters!** Always follow the order above
2. **Duplicate Detection:** Re-importing the same file will skip duplicates
3. **Case Insensitive:** All names are converted to UPPERCASE
4. **Unit Flexibility:** Use any common unit abbreviation (kg, KG, pcs, ltr, etc.)
5. **Required Fields:** Make sure all required fields are filled

## 🛠️ Customization

Feel free to modify these files:
- Add more items to categories
- Change quantities or prices
- Add your own categories
- Update vendor information
- Modify storage locations

Just maintain the column headers and data format!

---

**Generated:** 2025-11-20
**Total Items:** 40 (5 per category)
**Ready to Import:** Yes ✅
