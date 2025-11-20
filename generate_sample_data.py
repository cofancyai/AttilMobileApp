#!/usr/bin/env python3
"""
Generate sample Excel import files for Attil Inventory Management System
with comprehensive sample data - 5 items per category
"""

import openpyxl
from openpyxl import Workbook
from openpyxl.styles import Font, PatternFill, Alignment
from datetime import datetime, timedelta

def create_categories_file():
    """Create categories_import.xlsx with sample categories"""
    wb = Workbook()
    ws = wb.active
    ws.title = "Categories"

    # Headers
    headers = ["name", "description"]
    ws.append(headers)

    # Style headers
    for cell in ws[1]:
        cell.font = Font(bold=True)
        cell.fill = PatternFill(start_color="4472C4", end_color="4472C4", fill_type="solid")
        cell.font = Font(bold=True, color="FFFFFF")

    # Sample categories
    categories = [
        ("RICE", "All types of rice"),
        ("PULSES", "Dal and lentils"),
        ("SPICES", "Spices and masalas"),
        ("OIL", "Cooking oils"),
        ("VEGETABLES", "Fresh vegetables"),
        ("DAIRY", "Milk products"),
        ("SNACKS", "Packaged snacks"),
        ("BEVERAGES", "Drinks and beverages"),
    ]

    for cat in categories:
        ws.append(cat)

    # Auto-size columns
    for col in ws.columns:
        max_length = 0
        column = col[0].column_letter
        for cell in col:
            if cell.value:
                max_length = max(max_length, len(str(cell.value)))
        ws.column_dimensions[column].width = max_length + 2

    wb.save("/home/user/AttilMobileApp/Excel/categories_import.xlsx")
    print("✅ Created categories_import.xlsx with 8 categories")

def create_items_file():
    """Create items_import.xlsx with 5 items per category"""
    wb = Workbook()
    ws = wb.active
    ws.title = "Items"

    # Headers
    headers = ["name", "category_name", "godown_name", "rack_name", "unit_of_measure", "minimum_stock_level", "is_active"]
    ws.append(headers)

    # Style headers
    for cell in ws[1]:
        cell.font = Font(bold=True)
        cell.fill = PatternFill(start_color="4472C4", end_color="4472C4", fill_type="solid")
        cell.font = Font(bold=True, color="FFFFFF")

    # Sample items - 5 per category
    items = [
        # RICE (5 items)
        ("BASMATI RICE", "RICE", "MAIN GODOWN", "RACK A1", "kg", 50.0, "true"),
        ("SONA MASOORI RICE", "RICE", "MAIN GODOWN", "RACK A1", "kg", 50.0, "true"),
        ("PONNI RICE", "RICE", "MAIN GODOWN", "RACK A1", "kg", 40.0, "true"),
        ("BROWN RICE", "RICE", "MAIN GODOWN", "RACK A1", "kg", 20.0, "true"),
        ("JASMINE RICE", "RICE", "MAIN GODOWN", "RACK A1", "kg", 30.0, "true"),

        # PULSES (5 items)
        ("TOOR DAL", "PULSES", "MAIN GODOWN", "RACK A2", "kg", 30.0, "true"),
        ("MOONG DAL", "PULSES", "MAIN GODOWN", "RACK A2", "kg", 25.0, "true"),
        ("CHANA DAL", "PULSES", "MAIN GODOWN", "RACK A2", "kg", 25.0, "true"),
        ("URAD DAL", "PULSES", "MAIN GODOWN", "RACK A2", "kg", 20.0, "true"),
        ("MASOOR DAL", "PULSES", "MAIN GODOWN", "RACK A2", "kg", 20.0, "true"),

        # SPICES (5 items)
        ("TURMERIC POWDER", "SPICES", "MAIN GODOWN", "RACK B1", "kg", 5.0, "true"),
        ("CHILLI POWDER", "SPICES", "MAIN GODOWN", "RACK B1", "kg", 5.0, "true"),
        ("CORIANDER POWDER", "SPICES", "MAIN GODOWN", "RACK B1", "kg", 5.0, "true"),
        ("GARAM MASALA", "SPICES", "MAIN GODOWN", "RACK B1", "kg", 3.0, "true"),
        ("CUMIN SEEDS", "SPICES", "MAIN GODOWN", "RACK B1", "kg", 3.0, "true"),

        # OIL (5 items)
        ("SUNFLOWER OIL", "OIL", "MAIN GODOWN", "RACK B2", "liters", 20.0, "true"),
        ("MUSTARD OIL", "OIL", "MAIN GODOWN", "RACK B2", "liters", 15.0, "true"),
        ("REFINED OIL", "OIL", "MAIN GODOWN", "RACK B2", "liters", 20.0, "true"),
        ("COCONUT OIL", "OIL", "MAIN GODOWN", "RACK B2", "liters", 10.0, "true"),
        ("OLIVE OIL", "OIL", "MAIN GODOWN", "RACK B2", "liters", 5.0, "true"),

        # VEGETABLES (5 items)
        ("ONION", "VEGETABLES", "COLD STORAGE", "RACK C1", "kg", 100.0, "true"),
        ("POTATO", "VEGETABLES", "COLD STORAGE", "RACK C1", "kg", 100.0, "true"),
        ("TOMATO", "VEGETABLES", "COLD STORAGE", "RACK C1", "kg", 50.0, "true"),
        ("CARROT", "VEGETABLES", "COLD STORAGE", "RACK C1", "kg", 30.0, "true"),
        ("CABBAGE", "VEGETABLES", "COLD STORAGE", "RACK C1", "kg", 30.0, "true"),

        # DAIRY (5 items)
        ("MILK POWDER", "DAIRY", "COLD STORAGE", "RACK C2", "kg", 10.0, "true"),
        ("PANEER", "DAIRY", "COLD STORAGE", "RACK C2", "kg", 5.0, "true"),
        ("BUTTER", "DAIRY", "COLD STORAGE", "RACK C2", "kg", 5.0, "true"),
        ("CHEESE", "DAIRY", "COLD STORAGE", "RACK C2", "kg", 3.0, "true"),
        ("CURD", "DAIRY", "COLD STORAGE", "RACK C2", "liters", 10.0, "true"),

        # SNACKS (5 items)
        ("CHIPS PACKETS", "SNACKS", "PANTRY", "RACK D1", "pcs", 100.0, "true"),
        ("BISCUITS", "SNACKS", "PANTRY", "RACK D1", "packets", 50.0, "true"),
        ("NAMKEEN", "SNACKS", "PANTRY", "RACK D1", "packets", 50.0, "true"),
        ("COOKIES", "SNACKS", "PANTRY", "RACK D1", "packets", 40.0, "true"),
        ("CRACKERS", "SNACKS", "PANTRY", "RACK D1", "packets", 40.0, "true"),

        # BEVERAGES (5 items)
        ("TEA LEAVES", "BEVERAGES", "PANTRY", "RACK D2", "kg", 5.0, "true"),
        ("COFFEE POWDER", "BEVERAGES", "PANTRY", "RACK D2", "kg", 3.0, "true"),
        ("FRUIT JUICE", "BEVERAGES", "PANTRY", "RACK D2", "liters", 20.0, "true"),
        ("SOFT DRINKS", "BEVERAGES", "PANTRY", "RACK D2", "pcs", 50.0, "true"),
        ("MINERAL WATER", "BEVERAGES", "PANTRY", "RACK D2", "liters", 100.0, "true"),
    ]

    for item in items:
        ws.append(item)

    # Auto-size columns
    for col in ws.columns:
        max_length = 0
        column = col[0].column_letter
        for cell in col:
            if cell.value:
                max_length = max(max_length, len(str(cell.value)))
        ws.column_dimensions[column].width = max_length + 2

    wb.save("/home/user/AttilMobileApp/Excel/items_import.xlsx")
    print(f"✅ Created items_import.xlsx with {len(items)} items (5 per category)")

def create_initial_stock_file():
    """Create initial_stock_inward.xlsx with stock for all items"""
    wb = Workbook()
    ws = wb.active
    ws.title = "Initial Stock"

    # Headers
    headers = ["item_name", "vendor_name", "vendor_contact", "vendor_address", "purchase_date",
               "inward_quantity", "price_per_unit", "price_without_gst", "gst_percentage",
               "price_with_gst", "bill_number", "expiry_date", "cuisine_name"]
    ws.append(headers)

    # Style headers
    for cell in ws[1]:
        cell.font = Font(bold=True)
        cell.fill = PatternFill(start_color="4472C4", end_color="4472C4", fill_type="solid")
        cell.font = Font(bold=True, color="FFFFFF")

    # Base date for purchases
    base_date = datetime(2025, 1, 15)

    # Sample initial stock entries
    stock_entries = [
        # RICE items
        ("BASMATI RICE", "Rice Suppliers Ltd", "9876543210", "123 Market Road, Mumbai", 100.0, 55.0, "NORTH INDIAN"),
        ("SONA MASOORI RICE", "Rice Suppliers Ltd", "9876543210", "123 Market Road, Mumbai", 100.0, 45.0, "SOUTH INDIAN"),
        ("PONNI RICE", "Rice Suppliers Ltd", "9876543210", "123 Market Road, Mumbai", 80.0, 50.0, "SOUTH INDIAN"),
        ("BROWN RICE", "Health Foods Co", "9876543211", "456 Health Street, Delhi", 40.0, 80.0, "CONTINENTAL"),
        ("JASMINE RICE", "Asian Imports", "9876543212", "789 Import Lane, Bangalore", 50.0, 75.0, "CHINESE"),

        # PULSES items
        ("TOOR DAL", "Pulse Traders", "9876543213", "321 Dal Market, Pune", 60.0, 120.0, "NORTH INDIAN"),
        ("MOONG DAL", "Pulse Traders", "9876543213", "321 Dal Market, Pune", 50.0, 110.0, "NORTH INDIAN"),
        ("CHANA DAL", "Pulse Traders", "9876543213", "321 Dal Market, Pune", 50.0, 100.0, "NORTH INDIAN"),
        ("URAD DAL", "Pulse Traders", "9876543213", "321 Dal Market, Pune", 40.0, 115.0, "SOUTH INDIAN"),
        ("MASOOR DAL", "Pulse Traders", "9876543213", "321 Dal Market, Pune", 40.0, 105.0, "NORTH INDIAN"),

        # SPICES items
        ("TURMERIC POWDER", "Spice Merchants", "9876543214", "111 Spice Bazaar, Kerala", 10.0, 300.0, "NORTH INDIAN"),
        ("CHILLI POWDER", "Spice Merchants", "9876543214", "111 Spice Bazaar, Kerala", 10.0, 250.0, "NORTH INDIAN"),
        ("CORIANDER POWDER", "Spice Merchants", "9876543214", "111 Spice Bazaar, Kerala", 10.0, 200.0, "NORTH INDIAN"),
        ("GARAM MASALA", "Spice Merchants", "9876543214", "111 Spice Bazaar, Kerala", 6.0, 400.0, "NORTH INDIAN"),
        ("CUMIN SEEDS", "Spice Merchants", "9876543214", "111 Spice Bazaar, Kerala", 6.0, 350.0, "NORTH INDIAN"),

        # OIL items
        ("SUNFLOWER OIL", "Oil Distributors", "9876543215", "222 Oil Mill Road, Gujarat", 40.0, 150.0, ""),
        ("MUSTARD OIL", "Oil Distributors", "9876543215", "222 Oil Mill Road, Gujarat", 30.0, 180.0, ""),
        ("REFINED OIL", "Oil Distributors", "9876543215", "222 Oil Mill Road, Gujarat", 40.0, 140.0, ""),
        ("COCONUT OIL", "South Traders", "9876543216", "333 Coconut Grove, Tamil Nadu", 20.0, 200.0, ""),
        ("OLIVE OIL", "Premium Imports", "9876543217", "444 Import Street, Delhi", 10.0, 800.0, ""),

        # VEGETABLES items
        ("ONION", "Fresh Veggie Mart", "9876543218", "555 Vegetable Market, Nashik", 200.0, 30.0, ""),
        ("POTATO", "Fresh Veggie Mart", "9876543218", "555 Vegetable Market, Nashik", 200.0, 25.0, ""),
        ("TOMATO", "Fresh Veggie Mart", "9876543218", "555 Vegetable Market, Nashik", 100.0, 40.0, ""),
        ("CARROT", "Fresh Veggie Mart", "9876543218", "555 Vegetable Market, Nashik", 60.0, 35.0, ""),
        ("CABBAGE", "Fresh Veggie Mart", "9876543218", "555 Vegetable Market, Nashik", 60.0, 20.0, ""),

        # DAIRY items
        ("MILK POWDER", "Dairy Suppliers", "9876543219", "666 Dairy Farm, Amul", 20.0, 400.0, ""),
        ("PANEER", "Dairy Suppliers", "9876543219", "666 Dairy Farm, Amul", 10.0, 300.0, "NORTH INDIAN"),
        ("BUTTER", "Dairy Suppliers", "9876543219", "666 Dairy Farm, Amul", 10.0, 450.0, ""),
        ("CHEESE", "Dairy Suppliers", "9876543219", "666 Dairy Farm, Amul", 6.0, 500.0, "CONTINENTAL"),
        ("CURD", "Dairy Suppliers", "9876543219", "666 Dairy Farm, Amul", 20.0, 50.0, "NORTH INDIAN"),

        # SNACKS items
        ("CHIPS PACKETS", "Snack Distributors", "9876543220", "777 Snack Market, Indore", 200.0, 10.0, ""),
        ("BISCUITS", "Snack Distributors", "9876543220", "777 Snack Market, Indore", 100.0, 20.0, ""),
        ("NAMKEEN", "Snack Distributors", "9876543220", "777 Snack Market, Indore", 100.0, 15.0, ""),
        ("COOKIES", "Snack Distributors", "9876543220", "777 Snack Market, Indore", 80.0, 25.0, ""),
        ("CRACKERS", "Snack Distributors", "9876543220", "777 Snack Market, Indore", 80.0, 18.0, ""),

        # BEVERAGES items
        ("TEA LEAVES", "Beverage Suppliers", "9876543221", "888 Tea Gardens, Assam", 10.0, 500.0, ""),
        ("COFFEE POWDER", "Beverage Suppliers", "9876543221", "888 Tea Gardens, Assam", 6.0, 600.0, ""),
        ("FRUIT JUICE", "Beverage Suppliers", "9876543221", "888 Tea Gardens, Assam", 40.0, 80.0, ""),
        ("SOFT DRINKS", "Beverage Suppliers", "9876543221", "888 Tea Gardens, Assam", 100.0, 30.0, ""),
        ("MINERAL WATER", "Beverage Suppliers", "9876543221", "888 Tea Gardens, Assam", 200.0, 20.0, ""),
    ]

    for idx, (item_name, vendor_name, vendor_contact, vendor_address, quantity, price_per_unit, cuisine) in enumerate(stock_entries):
        purchase_date = (base_date + timedelta(days=idx)).strftime("%Y-%m-%d")
        expiry_date = (base_date + timedelta(days=365 + idx)).strftime("%Y-%m-%d")

        # Calculate GST amounts
        price_without_gst = quantity * price_per_unit
        gst_percentage = 18.0
        gst_amount = price_without_gst * (gst_percentage / 100)
        price_with_gst = price_without_gst + gst_amount

        bill_number = f"INIT-2025-{str(idx + 1).zfill(3)}"

        row = [
            item_name,
            vendor_name,
            vendor_contact,
            vendor_address,
            purchase_date,
            quantity,
            price_per_unit,
            price_without_gst,
            gst_percentage,
            price_with_gst,
            bill_number,
            expiry_date,
            cuisine if cuisine else ""
        ]
        ws.append(row)

    # Auto-size columns
    for col in ws.columns:
        max_length = 0
        column = col[0].column_letter
        for cell in col:
            if cell.value:
                max_length = max(max_length, len(str(cell.value)))
        ws.column_dimensions[column].width = min(max_length + 2, 50)

    wb.save("/home/user/AttilMobileApp/Excel/initial_stock_inward.xlsx")
    print(f"✅ Created initial_stock_inward.xlsx with {len(stock_entries)} stock entries")

def create_godowns_file():
    """Create godowns_import.xlsx"""
    wb = Workbook()
    ws = wb.active
    ws.title = "Godowns"

    headers = ["name", "description", "location"]
    ws.append(headers)

    for cell in ws[1]:
        cell.font = Font(bold=True)
        cell.fill = PatternFill(start_color="4472C4", end_color="4472C4", fill_type="solid")
        cell.font = Font(bold=True, color="FFFFFF")

    godowns = [
        ("MAIN GODOWN", "Primary storage facility", "Ground Floor, Building A"),
        ("COLD STORAGE", "Temperature controlled storage", "Basement, Building B"),
        ("PANTRY", "Dry goods storage", "First Floor, Building A"),
    ]

    for godown in godowns:
        ws.append(godown)

    for col in ws.columns:
        max_length = 0
        column = col[0].column_letter
        for cell in col:
            if cell.value:
                max_length = max(max_length, len(str(cell.value)))
        ws.column_dimensions[column].width = max_length + 2

    wb.save("/home/user/AttilMobileApp/Excel/godowns_import.xlsx")
    print("✅ Created godowns_import.xlsx with 3 godowns")

def create_racks_file():
    """Create racks_import.xlsx"""
    wb = Workbook()
    ws = wb.active
    ws.title = "Racks"

    headers = ["name", "description", "godown_name"]
    ws.append(headers)

    for cell in ws[1]:
        cell.font = Font(bold=True)
        cell.fill = PatternFill(start_color="4472C4", end_color="4472C4", fill_type="solid")
        cell.font = Font(bold=True, color="FFFFFF")

    racks = [
        ("RACK A1", "Rice and grains section", "MAIN GODOWN"),
        ("RACK A2", "Pulses section", "MAIN GODOWN"),
        ("RACK B1", "Spices section", "MAIN GODOWN"),
        ("RACK B2", "Oil section", "MAIN GODOWN"),
        ("RACK C1", "Fresh vegetables", "COLD STORAGE"),
        ("RACK C2", "Dairy products", "COLD STORAGE"),
        ("RACK D1", "Snacks and packaged food", "PANTRY"),
        ("RACK D2", "Beverages", "PANTRY"),
    ]

    for rack in racks:
        ws.append(rack)

    for col in ws.columns:
        max_length = 0
        column = col[0].column_letter
        for cell in col:
            if cell.value:
                max_length = max(max_length, len(str(cell.value)))
        ws.column_dimensions[column].width = max_length + 2

    wb.save("/home/user/AttilMobileApp/Excel/racks_import.xlsx")
    print("✅ Created racks_import.xlsx with 8 racks")

def create_cuisines_file():
    """Create cuisines_import.xlsx"""
    wb = Workbook()
    ws = wb.active
    ws.title = "Cuisines"

    headers = ["name", "description"]
    ws.append(headers)

    for cell in ws[1]:
        cell.font = Font(bold=True)
        cell.fill = PatternFill(start_color="4472C4", end_color="4472C4", fill_type="solid")
        cell.font = Font(bold=True, color="FFFFFF")

    cuisines = [
        ("NORTH INDIAN", "North Indian cuisine"),
        ("SOUTH INDIAN", "South Indian cuisine"),
        ("CHINESE", "Chinese cuisine"),
        ("CONTINENTAL", "Continental cuisine"),
    ]

    for cuisine in cuisines:
        ws.append(cuisine)

    for col in ws.columns:
        max_length = 0
        column = col[0].column_letter
        for cell in col:
            if cell.value:
                max_length = max(max_length, len(str(cell.value)))
        ws.column_dimensions[column].width = max_length + 2

    wb.save("/home/user/AttilMobileApp/Excel/cuisines_import.xlsx")
    print("✅ Created cuisines_import.xlsx with 4 cuisines")

if __name__ == "__main__":
    print("🚀 Generating comprehensive sample import files...")
    print()

    create_godowns_file()
    create_racks_file()
    create_categories_file()
    create_cuisines_file()
    create_items_file()
    create_initial_stock_file()

    print()
    print("✨ All sample import files created successfully!")
    print()
    print("📋 Import Order:")
    print("   1. godowns_import.xlsx (3 godowns)")
    print("   2. racks_import.xlsx (8 racks)")
    print("   3. categories_import.xlsx (8 categories)")
    print("   4. cuisines_import.xlsx (4 cuisines)")
    print("   5. items_import.xlsx (40 items - 5 per category)")
    print("   6. initial_stock_inward.xlsx (40 stock entries)")
    print()
    print("💡 All files use correct unit formats (kg, pcs, ltr, gm, pkt)")
