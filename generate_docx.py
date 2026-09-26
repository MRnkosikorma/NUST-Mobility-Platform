import os
import re
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import parse_xml
from docx.oxml.ns import nsdecls

def create_styled_document(md_filepath, output_docx_path):
    doc = Document()

    # Set page margins (1 inch = 72 pt)
    sections = doc.sections
    for section in sections:
        section.top_margin = Inches(1.0)
        section.bottom_margin = Inches(1.0)
        section.left_margin = Inches(1.0)
        section.right_margin = Inches(1.0)

    # Style colors
    PRIMARY_COLOR = RGBColor(15, 41, 66)     # Deep Navy
    SECONDARY_COLOR = RGBColor(30, 136, 229) # Vibrant Blue
    DARK_TEXT_COLOR = RGBColor(44, 62, 80)   # Charcoal
    ALERT_BG = "FFF3CD"                      # Light Amber/Yellow for Callouts
    ALERT_BORDER = "856404"                  # Dark Gold/Amber
    CODE_BG = "F4F6F9"                       # Soft Gray
    TABLE_HEADER_BG = "0F2942"               # Deep Navy

    # Base Normal style setup
    style_normal = doc.styles['Normal']
    font = style_normal.font
    font.name = 'Calibri'
    font.size = Pt(11)
    font.color.rgb = DARK_TEXT_COLOR

    def set_cell_background(cell, fill_hex):
        tcPr = cell._tc.get_or_add_tcPr()
        shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{fill_hex}"/>')
        tcPr.append(shd)

    def set_cell_margins(cell, top=140, bottom=140, left=200, right=200):
        tcPr = cell._tc.get_or_add_tcPr()
        tcMar = parse_xml(f'<w:tcMar {nsdecls("w")}><w:top w:w="{top}" w:type="dxa"/><w:bottom w:w="{bottom}" w:type="dxa"/><w:left w:w="{left}" w:type="dxa"/><w:right w:w="{right}" w:type="dxa"/></w:tcMar>')
        tcPr.append(tcMar)

    def add_custom_title(text):
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.LEFT
        p.paragraph_format.space_before = Pt(0)
        p.paragraph_format.space_after = Pt(6)
        run = p.add_run(text)
        run.font.name = 'Calibri'
        run.font.size = Pt(24)
        run.font.bold = True
        run.font.color.rgb = PRIMARY_COLOR

    def add_custom_h1(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(18)
        p.paragraph_format.space_after = Pt(6)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        run.font.name = 'Calibri'
        run.font.size = Pt(16)
        run.font.bold = True
        run.font.color.rgb = PRIMARY_COLOR

    def add_custom_h2(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(14)
        p.paragraph_format.space_after = Pt(4)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        run.font.name = 'Calibri'
        run.font.size = Pt(13)
        run.font.bold = True
        run.font.color.rgb = SECONDARY_COLOR

    def add_custom_h3(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(10)
        p.paragraph_format.space_after = Pt(2)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        run.font.name = 'Calibri'
        run.font.size = Pt(11.5)
        run.font.bold = True
        run.font.color.rgb = PRIMARY_COLOR

    def add_formatted_runs(paragraph, text):
        tokens = re.split(r'(\*\*.*?\*\*|\*.*?\*)', text)
        for token in tokens:
            if not token:
                continue
            if token.startswith('**') and token.endswith('**'):
                r = paragraph.add_run(token[2:-2])
                r.bold = True
            elif token.startswith('*') and token.endswith('*'):
                r = paragraph.add_run(token[1:-1])
                r.italic = True
            else:
                paragraph.add_run(token)

    def add_callout_box(lines):
        tbl = doc.add_table(rows=1, cols=1)
        tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
        tbl.autofit = False
        
        cell = tbl.cell(0, 0)
        set_cell_background(cell, ALERT_BG)
        set_cell_margins(cell, top=160, bottom=160, left=240, right=240)
        
        tcPr = cell._tc.get_or_add_tcPr()
        borders = parse_xml(f'<w:tcBorders {nsdecls("w")}><w:left w:val="single" w:sz="36" w:space="0" w:color="{ALERT_BORDER}"/><w:top w:val="none"/><w:right w:val="none"/><w:bottom w:val="none"/></w:tcBorders>')
        tcPr.append(borders)

        p = cell.paragraphs[0]
        p.paragraph_format.space_before = Pt(0)
        p.paragraph_format.space_after = Pt(4)
        
        for idx, line in enumerate(lines):
            if idx > 0:
                p = cell.add_paragraph()
                p.paragraph_format.space_before = Pt(2)
                p.paragraph_format.space_after = Pt(4)
            clean_line = line.lstrip('> ').strip()
            if clean_line.startswith('### '):
                r = p.add_run(clean_line[4:])
                r.bold = True
                r.font.size = Pt(13)
                r.font.color.rgb = RGBColor(133, 100, 4)
            elif clean_line.startswith('[!CAUTION]') or clean_line.startswith('[!NOTE]') or clean_line.startswith('[!IMPORTANT]'):
                continue
            else:
                add_formatted_runs(p, clean_line)

        empty_p = doc.add_paragraph()
        empty_p.paragraph_format.space_before = Pt(0)
        empty_p.paragraph_format.space_after = Pt(6)

    def add_code_block(code_text):
        tbl = doc.add_table(rows=1, cols=1)
        tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
        tbl.autofit = False
        cell = tbl.cell(0, 0)
        set_cell_background(cell, CODE_BG)
        set_cell_margins(cell, top=120, bottom=120, left=180, right=180)

        p = cell.paragraphs[0]
        p.paragraph_format.space_before = Pt(0)
        p.paragraph_format.space_after = Pt(0)
        p.paragraph_format.line_spacing = Pt(12)
        
        run = p.add_run(code_text)
        run.font.name = 'Consolas'
        run.font.size = Pt(9.5)
        run.font.color.rgb = RGBColor(33, 37, 41)

        empty_p = doc.add_paragraph()
        empty_p.paragraph_format.space_before = Pt(0)
        empty_p.paragraph_format.space_after = Pt(6)

    with open(md_filepath, 'r', encoding='utf-8') as f:
        md_content = f.read()

    lines = md_content.split('\n')
    i = 0
    in_code_block = False
    code_lines = []
    in_quote_block = False
    quote_lines = []

    while i < len(lines):
        line = lines[i]

        if line.strip().startswith('```'):
            if in_code_block:
                add_code_block('\n'.join(code_lines))
                code_lines = []
                in_code_block = False
            else:
                in_code_block = True
                code_lines = []
            i += 1
            continue

        if in_code_block:
            code_lines.append(line)
            i += 1
            continue

        if line.strip().startswith('>'):
            if not in_quote_block:
                in_quote_block = True
                quote_lines = []
            quote_lines.append(line)
            i += 1
            continue
        else:
            if in_quote_block:
                add_callout_box(quote_lines)
                quote_lines = []
                in_quote_block = False

        if line.strip().startswith('|') and i + 1 < len(lines) and '|---' in lines[i+1]:
            table_lines = [line, lines[i+1]]
            i += 2
            while i < len(lines) and lines[i].strip().startswith('|'):
                table_lines.append(lines[i])
                i += 1
            
            headers = [c.strip() for c in table_lines[0].split('|')[1:-1]]
            rows_data = []
            for r_line in table_lines[2:]:
                rows_data.append([c.strip() for c in r_line.split('|')[1:-1]])

            table = doc.add_table(rows=len(rows_data) + 1, cols=len(headers))
            table.alignment = WD_TABLE_ALIGNMENT.CENTER
            
            hdr_cells = table.rows[0].cells
            for idx, text in enumerate(headers):
                hdr_cells[idx].text = text
                set_cell_background(hdr_cells[idx], TABLE_HEADER_BG)
                set_cell_margins(hdr_cells[idx], top=120, bottom=120, left=150, right=150)
                for paragraph in hdr_cells[idx].paragraphs:
                    for run in paragraph.runs:
                        run.font.bold = True
                        run.font.color.rgb = RGBColor(255, 255, 255)
                        run.font.size = Pt(10)

            for r_idx, r_data in enumerate(rows_data):
                row_cells = table.rows[r_idx + 1].cells
                bg_color = "F8F9FA" if r_idx % 2 == 1 else "FFFFFF"
                for c_idx, val in enumerate(r_data):
                    row_cells[c_idx].text = ""
                    p = row_cells[c_idx].paragraphs[0]
                    p.paragraph_format.space_before = Pt(0)
                    p.paragraph_format.space_after = Pt(0)
                    add_formatted_runs(p, val)
                    set_cell_background(row_cells[c_idx], bg_color)
                    set_cell_margins(row_cells[c_idx], top=100, bottom=100, left=150, right=150)

            empty_p = doc.add_paragraph()
            empty_p.paragraph_format.space_before = Pt(0)
            empty_p.paragraph_format.space_after = Pt(6)
            continue

        if line.startswith('# '):
            add_custom_title(line[2:].strip())
        elif line.startswith('## '):
            add_custom_h1(line[3:].strip())
        elif line.startswith('### '):
            add_custom_h2(line[4:].strip())
        elif line.startswith('#### '):
            add_custom_h3(line[5:].strip())
        elif line.strip() == '---':
            p = doc.add_paragraph()
            p.paragraph_format.space_before = Pt(6)
            p.paragraph_format.space_after = Pt(6)
            r = p.add_run('_________________________________________________________________________________')
            r.font.color.rgb = RGBColor(220, 224, 230)
            r.font.size = Pt(8)
        elif line.strip().startswith('- ') or line.strip().startswith('* '):
            p = doc.add_paragraph(style='List Bullet')
            p.paragraph_format.space_before = Pt(1)
            p.paragraph_format.space_after = Pt(2)
            add_formatted_runs(p, line.strip()[2:])
        elif re.match(r'^\d+\.\s', line.strip()):
            p = doc.add_paragraph(style='List Number')
            p.paragraph_format.space_before = Pt(1)
            p.paragraph_format.space_after = Pt(2)
            text_part = re.sub(r'^\d+\.\s', '', line.strip())
            add_formatted_runs(p, text_part)
        elif line.strip():
            p = doc.add_paragraph()
            p.paragraph_format.space_before = Pt(0)
            p.paragraph_format.space_after = Pt(4)
            p.paragraph_format.line_spacing = Pt(14)
            add_formatted_runs(p, line.strip())

        i += 1

    if in_quote_block:
        add_callout_box(quote_lines)

    doc.save(output_docx_path)
    print(f"Successfully saved DOCX file to: {output_docx_path}")

if __name__ == '__main__':
    md_file = "/home/alpsa/.gemini/antigravity-ide/brain/66f32e86-59d0-4016-83d6-c4beabd87f1e/nust_mobility_platform_deep_research_documentation.md"
    desktop_output = "/home/alpsa/Desktop/NUST_Mobility_Platform_Deep_Research_Documentation.docx"
    create_styled_document(md_file, desktop_output)
    
    # Also create a copy for the standard documentation filename on Desktop
    md_file2 = "/home/alpsa/.gemini/antigravity-ide/brain/66f32e86-59d0-4016-83d6-c4beabd87f1e/nust_mobility_platform_documentation.md"
    desktop_output2 = "/home/alpsa/Desktop/NUST_Mobility_Platform_Documentation.docx"
    create_styled_document(md_file2, desktop_output2)
