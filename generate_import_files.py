#!/usr/bin/env python3
"""
Generate import files for Items and Initial Stock from Excel data
"""
import os
import sys
import zipfile
import xml.etree.ElementTree as ET
from datetime import datetime
import csv

def extract_excel_data(excel_path):
    """Extract data from Excel file by parsing XML"""
    print(f"Extracting data from {excel_path}...")

    # Extract the xlsx (which is a zip file)
    with zipfile.ZipFile(excel_path, 'r') as zip_ref:
        # Read shared strings
        shared_strings_xml = zip_ref.read('xl/sharedStrings.xml')
        shared_strings_tree = ET.fromstring(shared_strings_xml)

        # Extract all shared strings
        strings = []
        for si in shared_strings_tree.findall('.//{http://schemas.openxmlformats.org/spreadsheetml/2006/main}si'):
            t = si.find('{http://schemas.openxmlformats.org/spreadsheetml/2006/main}t')
            if t is not None:
                strings.append(t.text if t.text else '')
            else:
                strings.append('')

        # Read sheet data
        sheet_xml = zip_ref.read('xl/worksheets/sheet1.xml')
        sheet_tree = ET.fromstring(sheet_xml)

        # Extract rows
        rows = []
        for row in sheet_tree.findall('.//{http://schemas.openxmlformats.org/spreadsheetml/2006/main}row'):
            row_data = []
            for cell in row.findall('{http://schemas.openxmlformats.org/spreadsheetml/2006/main}c'):
                cell_type = cell.get('t')
                value = cell.find('{http://schemas.openxmlformats.org/spreadsheetml/2006/main}v')

                if value is not None:
                    if cell_type == 's':  # Shared string
                        idx = int(value.text)
                        row_data.append(strings[idx])
                    else:
                        row_data.append(value.text)
                else:
                    row_data.append('')

            if row_data:  # Only add non-empty rows
                rows.append(row_data)

        return rows

def generate_import_files(excel_data):
    """Generate import CSV files from Excel data"""

    # Skip header row
    data_rows = excel_data[1:]

    # Extract unique categories and racks
    categories = set()
    racks = set()

    for row in data_rows:
        if len(row) >= 4:
            rack = row[0] if len(row) > 0 else ''
            category = row[3] if len(row) > 3 else ''

            if rack:
                racks.add(rack)
            if category:
                categories.add(category)

    print(f"Found {len(categories)} unique categories")
    print(f"Found {len(racks)} unique racks")

    # 1. Generate Categories Import File
    print("\nGenerating categories_import.csv...")
    with open('/home/user/AttilMobileApp/Excel/categories_import.csv', 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow(['name', 'description'])
        for category in sorted(categories):
            if category:
                writer.writerow([category, f'{category} category'])

    # 2. Generate Racks Import File
    print("Generating racks_import.csv...")
    with open('/home/user/AttilMobileApp/Excel/racks_import.csv', 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow(['name', 'description', 'godown_id (replace with actual UUID)'])
        for rack in sorted(racks):
            if rack:
                writer.writerow([rack, f'{rack} storage rack', 'REPLACE_WITH_GODOWN_UUID'])

    # 3. Generate Items Import File
    print("Generating items_import.csv...")
    with open('/home/user/AttilMobileApp/Excel/items_import.csv', 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow([
            'name',
            'category_name (for reference)',
            'category_id (replace with UUID after importing categories)',
            'rack_name (for reference)',
            'rack_id (replace with UUID after importing racks)',
            'godown_id (replace with UUID)',
            'unit_of_measure',
            'minimum_stock_level',
            'is_active'
        ])

        for row in data_rows:
            if len(row) >= 8:
                rack = row[0] if len(row) > 0 else ''
                item_name = row[2] if len(row) > 2 else ''
                category = row[3] if len(row) > 3 else ''
                unit = row[4] if len(row) > 4 else ''
                min_stock = row[7] if len(row) > 7 else '0'

                # Clean up values
                item_name = item_name.strip() if item_name else ''
                category = category.strip() if category else ''
                unit = unit.strip() if unit else 'kg'

                try:
                    min_stock_val = float(min_stock) if min_stock else 0.0
                except:
                    min_stock_val = 0.0

                if item_name:
                    writer.writerow([
                        item_name,
                        category,
                        'REPLACE_WITH_CATEGORY_UUID',
                        rack,
                        'REPLACE_WITH_RACK_UUID',
                        'REPLACE_WITH_GODOWN_UUID',
                        unit,
                        min_stock_val,
                        'true'
                    ])

    # 4. Generate Initial Stock (Inward) Import File
    print("Generating initial_stock_inward.csv...")
    today = datetime.now().strftime('%Y-%m-%d')

    with open('/home/user/AttilMobileApp/Excel/initial_stock_inward.csv', 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow([
            'item_name (for reference)',
            'item_id (replace with UUID after importing items)',
            'vendor_name',
            'vendor_contact',
            'purchase_date',
            'inward_quantity',
            'price_per_unit',
            'price_without_gst',
            'gst_percentage',
            'price_with_gst',
            'bill_number',
            'created_by (replace with user UUID)'
        ])

        for row in data_rows:
            if len(row) >= 9:
                item_name = row[2] if len(row) > 2 else ''
                unit = row[4] if len(row) > 4 else ''
                closing_stock = row[8] if len(row) > 8 else '0'
                rate = row[9] if len(row) > 9 else '0'

                item_name = item_name.strip() if item_name else ''

                try:
                    closing_stock_val = float(closing_stock) if closing_stock else 0.0
                except:
                    closing_stock_val = 0.0

                try:
                    rate_val = float(rate) if rate else 0.0
                except:
                    rate_val = 0.0

                # Only create inward if there's closing stock
                if item_name and closing_stock_val > 0:
                    # Calculate GST (assuming 5% GST for food items)
                    gst_percentage = 5.0
                    price_without_gst = rate_val
                    gst_amount = price_without_gst * (gst_percentage / 100)
                    price_with_gst = price_without_gst + gst_amount

                    writer.writerow([
                        item_name,
                        'REPLACE_WITH_ITEM_UUID',
                        'Initial Stock Import',
                        '',
                        today,
                        closing_stock_val,
                        rate_val,
                        price_without_gst,
                        gst_percentage,
                        price_with_gst,
                        'INITIAL-STOCK',
                        'REPLACE_WITH_USER_UUID'
                    ])

    # 5. Generate a reference file showing expected current stock
    print("Generating expected_current_stock_reference.csv...")
    with open('/home/user/AttilMobileApp/Excel/expected_current_stock_reference.csv', 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow([
            'item_name',
            'category_name',
            'rack_name',
            'unit_of_measure',
            'minimum_stock_level',
            'expected_current_stock',
            'will_be_low_stock'
        ])

        for row in data_rows:
            if len(row) >= 9:
                rack = row[0] if len(row) > 0 else ''
                item_name = row[2] if len(row) > 2 else ''
                category = row[3] if len(row) > 3 else ''
                unit = row[4] if len(row) > 4 else ''
                min_stock = row[7] if len(row) > 7 else '0'
                closing_stock = row[8] if len(row) > 8 else '0'

                item_name = item_name.strip() if item_name else ''
                category = category.strip() if category else ''
                unit = unit.strip() if unit else 'kg'

                try:
                    min_stock_val = float(min_stock) if min_stock else 0.0
                except:
                    min_stock_val = 0.0

                try:
                    closing_stock_val = float(closing_stock) if closing_stock else 0.0
                except:
                    closing_stock_val = 0.0

                will_be_low = 'YES' if closing_stock_val < min_stock_val else 'NO'

                if item_name:
                    writer.writerow([
                        item_name,
                        category,
                        rack,
                        unit,
                        min_stock_val,
                        closing_stock_val,
                        will_be_low
                    ])

    print("\n✅ All import files generated successfully!")
    print("\nGenerated files:")
    print("1. Excel/categories_import.csv - Import categories first")
    print("2. Excel/racks_import.csv - Import racks second (need godown UUID)")
    print("3. Excel/items_import.csv - Import items third (need category & rack UUIDs)")
    print("4. Excel/initial_stock_inward.csv - Import inward transactions last (need item UUID)")
    print("5. Excel/expected_current_stock_reference.csv - Reference file (not for import)")

    print("\n📋 Import Order:")
    print("Step 1: Import categories using categories_import.csv")
    print("Step 2: Create/get a godown UUID from your system")
    print("Step 3: Replace REPLACE_WITH_GODOWN_UUID in racks_import.csv and import racks")
    print("Step 4: Replace UUIDs in items_import.csv and import items")
    print("Step 5: Replace UUIDs in initial_stock_inward.csv and import inward transactions")
    print("Step 6: Check current_stock view - should match expected_current_stock_reference.csv")

def main():
    excel_file = '/home/user/AttilMobileApp/Excel/Items_by_Rack_with_Categories.xlsx'

    if not os.path.exists(excel_file):
        print(f"❌ Error: Excel file not found at {excel_file}")
        sys.exit(1)

    # Extract data from Excel
    excel_data = extract_excel_data(excel_file)

    print(f"\n✅ Extracted {len(excel_data)} rows from Excel")
    print(f"Header: {excel_data[0]}")

    # Generate import files
    generate_import_files(excel_data)

    print("\n✅ Done!")

if __name__ == '__main__':
    main()
