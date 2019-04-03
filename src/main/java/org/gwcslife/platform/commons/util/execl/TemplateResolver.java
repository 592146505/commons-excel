package org.gwcslife.platform.commons.util.execl;

import lombok.Data;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.gwcslife.platform.commons.util.execl.model.Header;
import org.gwcslife.platform.commons.util.execl.model.Issue;
import org.gwcslife.platform.commons.util.execl.model.Option;

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

    static Header header;
    static List<Result> mergeRegions;
    static List<List<String>> allData;

    public static void main(String[] args) {
        try (Workbook wb = createWorkbook("cs.xlsx")) {
            Sheet sheet = wb.getSheetAt(0);
            mergeRegions = getAllMergedRegions(sheet);
            // 获取最后一行
            int lastRow = sheet.getLastRowNum();
            if (lastRow < 1) {
                return;
            }
            // 获取表头
            header = parseHeader(sheet);
            // 获取分类个数
            int classCount = header.getClassCount();
            // 获取问题个数
            int issueCount = header.getIssueCount();
            // 获取所有数据
            allData = getAllData(sheet);
            // 处理ClassModel数据
            for (int row = 0; row < allData.size(); row++) {
                int column = 0;
                // 行数据
                List<String> rowData = allData.get(row);
                Issue issue = new Issue();
                issue.setTitle(rowData.get(column));
                issue.setType("C1");
                // 合并单元格
                Result region = isMergedRegion(mergeRegions, row, column);

                if (region != null) {
                    getChildren(issue, region.startRow, region.endRow, column + 1);
                    row = region.endRow;
                } else {
                    getChildren(issue, row, row, column + 1);
                }
                System.out.println(issue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取下级
     *
     * @param parentIssue 上级节点
     * @param startRow    开始行
     * @param endRow      结束行
     * @param column      列
     */
    private static void getChildren(Issue parentIssue, int startRow, int endRow, int column) {
        //                if (column >= header.getClassCount() + (header.getIssueCount() * 2)) {
        if (column > header.getClassCount()) {
            return;
        }
        // 循环当前列单元格
        for (int i = startRow; i <= endRow; i++) {
            List<String> rowData = allData.get(i);
            String cellValue = rowData.get(column);
            // 不为空，加入父节点
            Issue issue = parentIssue;
            if (null != cellValue && !"".equals(cellValue)) {
                issue = new Issue();
                issue.setTitle(cellValue);
                issue.setType("C1");
                parentIssue.getChildren().add(issue);
            }
            // 合并单元格,则递归子节点
            Result region = isMergedRegion(mergeRegions, i, column);
            if (region != null) {
                getChildren(issue, region.startRow, region.endRow, column + 1);
                i = region.endRow;
            }
            // 非合并单元格
            else {
                // 数据为空时,后续问题列都为空,则直接取到结论
                if (null == cellValue || "".equals(cellValue)) {
                    String conclusion = rowData.get(header.getConclusionColumn());
                    String remark = rowData.get(header.getConclusionColumn() + 1);
                    Option option = new Option().setContent("默认").setCode("default");
                    if (null != conclusion && !"".equals(conclusion.trim())) {
                        if ("正常承保".equals(conclusion)) {
                            option.setFlow("F05");
                        } else if ("非常遗憾，被保险人无法投保该险种。".equals(conclusion)) {
                            option.setFlow("F01");
                        }
                    } else {
                        if (null != remark && !"".equals(remark.trim())) {
                            option.setFlow("F04");
                            option.setExceptCode(remark);
                        }
                    }
                    issue.addOption(option);
                }
            }

        }
    }

    /**
     * 创建工作薄对象
     *
     * @param path 资源路径
     *
     * @return {@code Workbook} File suffix (.xslx) creates {@link XSSFWorkbook}, otherwise create {@link HSSFWorkbook}
     */
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
     * 解析表头
     *
     * @param sheet Excel sheet
     *
     * @return {@code Header}
     */
    private static Header parseHeader(Sheet sheet) {
        // 获取表头
        Row firstRow = sheet.getRow(0);
        List<String> headers = new ArrayList<>();
        firstRow.cellIterator().forEachRemaining(cell -> headers.add(cell.getStringCellValue()));
        return new Header(headers);
    }

    /**
     * 获取所有数据
     *
     * @param sheet Excel sheet
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
     * @param sheet Excel sheet
     */
    private static List<Result> getAllMergedRegions(Sheet sheet) {
        // 获取合并单元格
        List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
        return mergedRegions.stream().map(range -> {
            int firstColumn = range.getFirstColumn();
            int lastColumn = range.getLastColumn();
            // 由于数据行从行号1（第二行）开始读取，所以这里减1
            int firstRow = range.getFirstRow() - 1;
            int lastRow = range.getLastRow() - 1;
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
            int firstRow = range.getStartRow();
            int lastRow = range.getEndRow();
            int firstColumn = range.getStartCol();
            int lastColumn = range.getEndCol();
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
