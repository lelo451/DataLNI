package com.lni.datalni.ui.imports;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Reads {@code .xlsx} via Apache POI (first sheet, header row at index 0). */
public class XlsxFileParser implements TabularFileParser {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public ParsedFile parse(String fileName, InputStream content) throws IOException {
        try (Workbook wb = new XSSFWorkbook(content)) {
            Sheet sheet = wb.getSheetAt(0);
            List<String> headers = new ArrayList<>();
            List<Map<String, String>> rows = new ArrayList<>();
            if (sheet == null) {
                return new ParsedFile(fileName, headers, rows);
            }
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return new ParsedFile(fileName, headers, rows);
            }
            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                Cell cell = headerRow.getCell(c);
                headers.add(cell == null ? "" : cell.getStringCellValue().trim());
            }
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                Map<String, String> values = new LinkedHashMap<>();
                for (int c = 0; c < headers.size(); c++) {
                    values.put(headers.get(c), cellToString(row.getCell(c)));
                }
                rows.add(values);
            }
            return new ParsedFile(fileName, headers, rows);
        }
    }

    private String cellToString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().format(ISO);
                }
                double n = cell.getNumericCellValue();
                // Render whole numbers without trailing ".0" so Integer parsers don't choke.
                if (n == Math.floor(n) && !Double.isInfinite(n)) {
                    return Long.toString((long) n);
                }
                return Double.toString(n);
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                // Cached value; recalculation is out of scope.
                if (cell.getCachedFormulaResultType() == CellType.NUMERIC) {
                    return Double.toString(cell.getNumericCellValue());
                }
                return cell.getStringCellValue();
            case BLANK:
            case _NONE:
            case ERROR:
            default:
                return "";
        }
    }
}
