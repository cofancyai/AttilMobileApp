#!/usr/bin/env python3
"""
Generate simplified import files with name-based lookups (no manual UUID replacements needed)
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

            if row_data:
                rows.append(row_data)

        return rows

def create_xlsx_from_csv(csv_path, xlsx_path):
    """Create XLSX file from CSV data"""
    # Read CSV
    with open(csv_path, 'r', encoding='utf-8') as f:
        reader = csv.reader(f)
        rows = list(reader)

    if not rows:
        return

    # Build shared strings and sheet data
    shared_strings = []
    string_map = {}
    sheet_rows = []

    for row_idx, row in enumerate(rows, start=1):
        cells = []
        for col_idx, value in enumerate(row):
            col_letter = get_column_letter(col_idx + 1)
            cell_ref = f"{col_letter}{row_idx}"

            if value not in string_map:
                string_map[value] = len(shared_strings)
                shared_strings.append(value)

            string_idx = string_map[value]
            cells.append(f'<c r="{cell_ref}" t="s"><v>{string_idx}</v></c>')

        sheet_rows.append(f'<row r="{row_idx}">{"".join(cells)}</row>')

    # Create XLSX structure
    content_types = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/sharedStrings.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml"/>
</Types>'''

    rels = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>'''

    workbook_rels = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings" Target="sharedStrings.xml"/>
</Relationships>'''

    workbook = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Sheet1" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>'''

    shared_strings_xml = f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" count="{len(shared_strings)}" uniqueCount="{len(shared_strings)}">
{"".join(f"<si><t>{escape_xml(s)}</t></si>" for s in shared_strings)}
</sst>'''

    sheet_xml = f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <sheetData>
    {"".join(sheet_rows)}
  </sheetData>
</worksheet>'''

    # Write XLSX
    with zipfile.ZipFile(xlsx_path, 'w', zipfile.ZIP_DEFLATED) as xlsx:
        xlsx.writestr('[Content_Types].xml', content_types)
        xlsx.writestr('_rels/.rels', rels)
        xlsx.writestr('xl/_rels/workbook.xml.rels', workbook_rels)
        xlsx.writestr('xl/workbook.xml', workbook)
        xlsx.writestr('xl/sharedStrings.xml', shared_strings_xml)
        xlsx.writestr('xl/worksheets/sheet1.xml', sheet_xml)

    print(f"✅ Created {xlsx_path}")

def get_column_letter(col_num):
    """Convert column number to Excel column letter"""
    result = ""
    while col_num > 0:
        col_num -= 1
        result = chr(65 + (col_num % 26)) + result
        col_num //= 26
    return result

def escape_xml(text):
    """Escape XML special characters"""
    text = str(text)
    text = text.replace('&', '&amp;')
    text = text.replace('<', '&lt;')
    text = text.replace('>', '&gt;')
    text = text.replace('"', '&quot;')
    text = text.replace("'", '&apos;')
    return text

def generate_simplified_import_files(excel_data):
    """Generate simplified import files with name-based lookups"""

    # Skip header row
    data_rows = excel_data[1:]

    # Extract unique categories, racks, godowns
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

    print(f"\nFound {len(categories)} unique categories")
    print(f"Found {len(racks)} unique racks")

    # 1. Categories Import (UNCHANGED - still simple)
    print("\nGenerating categories_import.csv...")
    with open('/home/user/AttilMobileApp/Excel/categories_import.csv', 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow(['name', 'description'])
        for category in sorted(categories):
            if category:
                writer.writerow([category, f'{category} category'])

    # 2. Racks Import (SIMPLIFIED - use godown_name instead of godown_id)
    print("Generating racks_import.csv...")
    with open('/home/user/AttilMobileApp/Excel/racks_import.csv', 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow(['name', 'description', 'godown_name'])
        for rack in sorted(racks):
            if rack:
                # Assuming all racks belong to "Main Warehouse" - user can edit
                writer.writerow([rack, f'{rack} storage rack', 'Main Warehouse'])

    # 3. Items Import (SIMPLIFIED - use category_name, rack_name, godown_name)
    print("Generating items_import.csv...")
    with open('/home/user/AttilMobileApp/Excel/items_import.csv', 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow([
            'name',
            'category_name',
            'rack_name',
            'godown_name',
            'unit_of_measure',
            'minimum_stock_level'
        ])

        for row in data_rows:
            if len(row) >= 8:
                rack = row[0] if len(row) > 0 else ''
                item_name = row[2] if len(row) > 2 else ''
                category = row[3] if len(row) > 3 else ''
                unit = row[4] if len(row) > 4 else ''
                min_stock = row[7] if len(row) > 7 else '0'

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
                        rack,
                        'Main Warehouse',  # Default godown - user can edit
                        unit,
                        min_stock_val
                    ])

    # 4. Initial Stock (SIMPLIFIED - use item_name instead of item_id)
    print("Generating initial_stock_inward.csv...")
    today = datetime.now().strftime('%Y-%m-%d')

    with open('/home/user/AttilMobileApp/Excel/initial_stock_inward.csv', 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow([
            'item_name',
            'vendor_name',
            'purchase_date',
            'inward_quantity',
            'price_per_unit',
            'price_without_gst',
            'gst_percentage',
            'price_with_gst',
            'bill_number'
        ])

        for row in data_rows:
            if len(row) >= 9:
                item_name = row[2] if len(row) > 2 else ''
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
                    # Calculate GST (5% for food items)
                    gst_percentage = 5.0
                    price_without_gst = rate_val
                    gst_amount = price_without_gst * (gst_percentage / 100)
                    price_with_gst = price_without_gst + gst_amount

                    writer.writerow([
                        item_name,
                        'Initial Stock Import',
                        today,
                        closing_stock_val,
                        rate_val,
                        price_without_gst,
                        gst_percentage,
                        price_with_gst,
                        'INITIAL-STOCK'
                    ])

    # 5. Expected Current Stock Reference (unchanged)
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

    print("\n✅ All simplified import files generated!")
    print("\nGenerated CSV files:")
    print("1. categories_import.csv - Import categories first")
    print("2. racks_import.csv - Import racks (uses godown_name)")
    print("3. items_import.csv - Import items (uses category_name, rack_name, godown_name)")
    print("4. initial_stock_inward.csv - Import initial stock (uses item_name)")
    print("5. expected_current_stock_reference.csv - Reference file (not for import)")

    print("\n🎯 NO MANUAL UUID REPLACEMENTS NEEDED!")
    print("The app will automatically lookup UUIDs by name during import.")

    print("\n📋 Import Steps:")
    print("1. Create a godown named 'Main Warehouse' (or edit the CSV to match your godown name)")
    print("2. Import categories_import.xlsx")
    print("3. Import racks_import.xlsx")
    print("4. Import items_import.xlsx")
    print("5. Import initial_stock_inward.xlsx")
    print("6. Done! Check current_stock view")

    # Convert CSV to XLSX
    print("\nConverting CSV to XLSX...")
    csv_files = [
        'categories_import.csv',
        'racks_import.csv',
        'items_import.csv',
        'initial_stock_inward.csv',
        'expected_current_stock_reference.csv'
    ]

    for csv_file in csv_files:
        csv_path = f'/home/user/AttilMobileApp/Excel/{csv_file}'
        xlsx_path = csv_path.replace('.csv', '.xlsx')
        if os.path.exists(csv_path):
            create_xlsx_from_csv(csv_path, xlsx_path)

    # Clean up CSV files
    for csv_file in csv_files:
        csv_path = f'/home/user/AttilMobileApp/Excel/{csv_file}'
        if os.path.exists(csv_path):
            os.remove(csv_path)

    print("\n✅ XLSX files ready!")

def main():
    excel_file = '/home/user/AttilMobileApp/Excel/Items_by_Rack_with_Categories.xlsx'

    if not os.path.exists(excel_file):
        print(f"❌ Error: Excel file not found at {excel_file}")
        sys.exit(1)

    # Extract data from Excel
    excel_data = extract_excel_data(excel_file)

    print(f"\n✅ Extracted {len(excel_data)} rows from Excel")

    # Generate simplified import files
    generate_simplified_import_files(excel_data)

    print("\n✅ Done!")

if __name__ == '__main__':
    main()
