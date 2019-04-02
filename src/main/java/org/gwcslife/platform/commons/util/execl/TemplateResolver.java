package org.gwcslife.platform.commons.util.execl;

import lombok.Data;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.gwcslife.platform.commons.util.execl.model.Header;
import org.gwcslife.platform.commons.util.execl.model.Issue;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 模版解析器
 *
 * @author roamer
 * @version V1.0
 * @date 2019-04-01 09:51
 */
public class TemplateResolver {

    public static void main(String[] args) {
        try (Workbook wb = createWorkbook("cs.xlsx")) {
            Sheet sheet = wb.getSheetAt(0);
            List<Result> mergeRegions = getAllMergedRegions(sheet);
            // 获取最后一行
            int lastRow = sheet.getLastRowNum();
            if (lastRow < 1) {
                return;
            }
            // 获取表头
            Header header = initHeader(sheet);
            // 获取分类个数
            int classCount = header.getClassList().size();
            // 获取问题个数
            int issueCount = header.getIssues().size();
            // 获取所有数据
            List<List<String>> allData = getAllData(sheet);
            // 处理数据
            for (int y = 0; y < allData.size(); y++) {
                // 行
                List<String> rowData = allData.get(y);
                for (int x = 0; x < rowData.size(); x++) {
                    String value = rowData.get(x);
                    // 不为空
                    if (value != null && !"".equals(value.trim())) {
                        Issue issue = new Issue();
                        issue.setTitle(value);
                        // 表头
                        if (x < clazzCount + issueCount - 2) {
                            issue.setType("C1");
                        } else {
                            if (x % 2 = 0)
                        }

                        Result region = isMergedRegion(mergeRegions, x, y);
                        // 合并行
                        if (region != null) {

                        } else {

                        }
                    }
                    // 为空
                    else {

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Workbook createWorkbook(String path) {
        ClassLoader loader = TemplateResolver.class.getClassLoader();
        URL url = loader.getResource(path);
        assert url != null;
        try (InputStream is = url.openStream()) {
            if (url.getFile().toLowerCase().endsWith(".xlsx")) {
                return new XSSFWorkbook(is);
            } else {
                return new HSSFWorkbook(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 初始化表头
     *
     * @param sheet
     *
     * @return {@code Header}
     */
    private static Header initHeader(Sheet sheet) {
        // 获取表头
        Row firstRow = sheet.getRow(0);
        List<String> headers = new ArrayList<>();
        firstRow.cellIterator().forEachRemaining(cell -> headers.add(cell.getStringCellValue()));
        return new Header(headers);
    }

    /**
     * 获取所有数据
     *
     * @param sheet
     *
     * @return {@code List<List<String>>}
     */
    public static List<List<String>> getAllData(Sheet sheet) {
        // 获取所有数据
        List<List<String>> allData = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            List<String> rowData = new ArrayList<>();
            Row currRow = sheet.getRow(i);
            currRow.cellIterator().forEachRemaining(cell -> rowData.add(cell.getStringCellValue()));
            allData.add(rowData);
        }
        return allData;
    }

    /**
     * 获取所有合并单元格
     *
     * @param sheet
     */
    private static List<Result> getAllMergedRegions(Sheet sheet) {
        // 获取合并单元格
        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        return mergedRegions.stream().map(range -> {
            int firstColumn = range.getFirstColumn();
            int lastColumn = range.getLastColumn();
            int firstRow = range.getFirstRow();
            int lastRow = range.getLastRow();
            return new Result(firstRow, lastRow, firstColumn, lastColumn);
        }).collect(Collectors.toList());
    }

    /**
     * 是否是合并单元格
     *
     * @param mergeRegions 合并单元格集
     * @param row          行（0开始）
     * @param column       列（0开始）
     *
     * @return {@code true} 当前单元格为合并单元格
     */
    private static Result isMergedRegion(List<Result> mergeRegions, int row, int column) {
        return mergeRegions.stream().filter(range -> {
            int firstColumn = range.getStartRow();
            int lastColumn = range.getEndCol();
            int firstRow = range.getStartRow();
            int lastRow = range.getEndRow();
            return (column >= firstColumn && column <= lastColumn) && (row >= firstRow && row <= lastRow);
        }).findFirst().orElse(null);
    }

    @Data
    static class Result {
        public int startRow;
        public int endRow;
        public int startCol;
        public int endCol;

        public Result(int startRow, int endRow, int startCol, int endCol) {
            this.startRow = startRow;
            this.endRow = endRow;
            this.startCol = startCol;
            this.endCol = endCol;
        }
    }

}
