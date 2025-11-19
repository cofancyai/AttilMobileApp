#!/usr/bin/env python3
"""
Convert CSV files to XLSX format
Since openpyxl is not available, we'll create minimal XLSX files manually
"""
import csv
import zipfile
import os
from datetime import datetime
import xml.dom.minidom as minidom

def create_xlsx_from_csv(csv_path, xlsx_path):
    """Create a minimal XLSX file from CSV data"""

    # Read CSV data
    with open(csv_path, 'r', encoding='utf-8') as f:
        reader = csv.reader(f)
        rows = list(reader)

    if not rows:
        print(f"Empty CSV file: {csv_path}")
        return

    # Create XLSX structure
    # [Content_Types].xml
    content_types = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/sharedStrings.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml"/>
</Types>'''

    # _rels/.rels
    rels = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>'''

    # xl/_rels/workbook.xml.rels
    workbook_rels = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings" Target="sharedStrings.xml"/>
</Relationships>'''

    # xl/workbook.xml
    workbook = '''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Sheet1" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>'''

    # Build shared strings and sheet data
    shared_strings = []
    string_map = {}

    sheet_rows = []
    for row_idx, row in enumerate(rows, start=1):
        cells = []
        for col_idx, value in enumerate(row):
            col_letter = get_column_letter(col_idx + 1)
            cell_ref = f"{col_letter}{row_idx}"

            # All values as strings
            if value not in string_map:
                string_map[value] = len(shared_strings)
                shared_strings.append(value)

            string_idx = string_map[value]
            cells.append(f'<c r="{cell_ref}" t="s"><v>{string_idx}</v></c>')

        sheet_rows.append(f'<row r="{row_idx}">{"".join(cells)}</row>')

    # xl/sharedStrings.xml
    shared_strings_xml = f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" count="{len(shared_strings)}" uniqueCount="{len(shared_strings)}">
{"".join(f"<si><t>{escape_xml(s)}</t></si>" for s in shared_strings)}
</sst>'''

    # xl/worksheets/sheet1.xml
    sheet_xml = f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <sheetData>
    {"".join(sheet_rows)}
  </sheetData>
</worksheet>'''

    # Create XLSX file (zip archive)
    with zipfile.ZipFile(xlsx_path, 'w', zipfile.ZIP_DEFLATED) as xlsx:
        xlsx.writestr('[Content_Types].xml', content_types)
        xlsx.writestr('_rels/.rels', rels)
        xlsx.writestr('xl/_rels/workbook.xml.rels', workbook_rels)
        xlsx.writestr('xl/workbook.xml', workbook)
        xlsx.writestr('xl/sharedStrings.xml', shared_strings_xml)
        xlsx.writestr('xl/worksheets/sheet1.xml', sheet_xml)

    print(f"✅ Created {xlsx_path}")

def get_column_letter(col_num):
    """Convert column number to Excel column letter (1->A, 2->B, ..., 27->AA)"""
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

def main():
    excel_dir = '/home/user/AttilMobileApp/Excel'

    csv_files = [
        'categories_import.csv',
        'racks_import.csv',
        'items_import.csv',
        'initial_stock_inward.csv',
        'expected_current_stock_reference.csv'
    ]

    print("Converting CSV files to XLSX format...\n")

    for csv_file in csv_files:
        csv_path = os.path.join(excel_dir, csv_file)
        xlsx_path = os.path.join(excel_dir, csv_file.replace('.csv', '.xlsx'))

        if os.path.exists(csv_path):
            create_xlsx_from_csv(csv_path, xlsx_path)
        else:
            print(f"❌ File not found: {csv_path}")

    print("\n✅ All files converted to XLSX format!")
    print(f"\nGenerated XLSX files in {excel_dir}/:")
    for csv_file in csv_files:
        xlsx_file = csv_file.replace('.csv', '.xlsx')
        print(f"  - {xlsx_file}")

if __name__ == '__main__':
    main()
