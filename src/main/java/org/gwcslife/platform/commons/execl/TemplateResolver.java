package org.gwcslife.platform.commons.execl;

import lombok.Data;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.gwcslife.platform.commons.execl.model.Header;
import org.gwcslife.platform.commons.execl.model.Issue;
import org.gwcslife.platform.commons.execl.model.Option;
import org.gwcslife.platform.commons.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
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
            // 获取所有数据
            allData = getAllData(sheet);
            // 处理ClassModel数据
            for (int i = 0; i < allData.size(); i++) {
                int column = 0;
                // 行数据
                List<String> rowData = allData.get(i);
                Issue issue = new Issue();
                issue.setTitle(rowData.get(column));
                issue.setType("C1");
                if (issue.getTitle().equals("婴幼儿出生及疾病")) {
                    System.out.println();
                }
                // 合并单元格,需要定位开始行、结束行
                int[] rowSpan = calculateRowSpan(mergeRegions, i, i, column);
                getChildren(issue, rowSpan[0], rowSpan[1], column + 1);
                i = rowSpan[1];
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
        if (column >= header.getIssueStartColumn()) {
            return;
        }
        // 循环当前列单元格
        for (int i = startRow; i <= endRow; i++) {
            List<String> rowData = allData.get(i);
            String cellValue = rowData.get(column);
            // 不为空，加入父节点
            Issue issue = parentIssue;
            // 合并单元格,需要定位开始行、结束行
            int[] rowSpan = calculateRowSpan(mergeRegions, i, i, column);
            // 分类
            if (StringUtils.isNotBlank(cellValue)) {
                issue = new Issue();
                issue.setTitle(cellValue);
                issue.setType("C1");
                parentIssue.getChildren().add(issue);
            } else {
                // 非合并单元格
                if (rowSpan[0] == rowSpan[1]) {
                    String conclusion = rowData.get(header.getConclusionColumn());
                    String remark = rowData.get(header.getConclusionColumn() + 1);
                    Option option = new Option().setContent("默认").setCode("default").setConclusion(conclusion, remark);
                    issue.addOption(option);
                    return;
                } else {
                    // 获取问题
                    getIssues(issue, null, rowSpan[0], rowSpan[1], header.getIssueStartColumn());
                    i = rowSpan[1];
                    continue;
                }
            }
            // 获取子节点
            getChildren(issue, rowSpan[0], rowSpan[1], column + 1);
            i = rowSpan[1];
        }
    }

    private static void getIssues(Issue parentIssue, Issue currIssue, int startRow, int endRow, int column) {
        if (column >= header.getConclusionColumn()) {
            return;
        }

        // 循环当前列单元格
        for (int i = startRow, j = column - header.getIssueStartColumn() + 2; i <= endRow; i++) {
            Issue issue = null;
            List<String> rowData = allData.get(i);
            String cellValue = rowData.get(column);
            // 合并单元格,需要定位开始行、结束行
            int[] rowSpan = calculateRowSpan(mergeRegions, i, i, column);
            // 非合并单元格
            if (rowSpan[0] == rowSpan[1]) {
                // 处理答案（非合并单元格,只能是答案）
                if (StringUtils.isNotBlank(cellValue)) {
                    String optionContent = rowData.get(column);
                    String conclusion = rowData.get(header.getConclusionColumn());
                    String remark = rowData.get(header.getConclusionColumn() + 1);
                    Option option = new Option().setContent(optionContent).setConclusion(conclusion, remark);
                    // 获取对应的题目
                    if (null != currIssue) {
                        issue = currIssue;
                        issue.addOption(option);
                    }
                } else {
                    return;
                }
            } else {
                // 题目
                if (j % 2 == 0) {
                    issue = new Issue();
                    issue.setTitle(cellValue);
                    issue.setType("C2");
                    parentIssue.getChildren().add(issue);
                    // 设置为上一题的下一题
                    if (null != currIssue) {
                        currIssue.getOptions().get(currIssue.getOptions().size() - 1).setNext(issue);
                    }
                }
                // 答案
                else {
                    String optionContent = rowData.get(column);
                    Option option = new Option().setContent(optionContent);
                    option.setFlow("F02");
                    if (null != currIssue) {
                        issue = currIssue;
                        issue.addOption(option);
                    }
                }
            }

            getIssues(parentIssue, issue, rowSpan[0], rowSpan[1], column + 1);
            i = rowSpan[1];
        }
    }


    /**
     * 计算开始行和结束行
     *
     * @param mergeRegions 所有跨行单元
     * @param startRow     开始行
     * @param endRow       结束行
     * @param column       列
     *
     * @return {@code int{newStartRow, newEndRow}} 如果所处在坐标为跨行单元格，则计算开始和结束行
     */
    private static int[] calculateRowSpan(List<Result> mergeRegions, int startRow, int endRow, int column) {
        int[] rowSpan = new int[]{startRow, endRow};
        // 合并单元格,需要定位开始行、结束行
        Result region = isMergedRegion(mergeRegions, startRow, column);
        if (region != null) {
            rowSpan[0] = region.startRow;
            rowSpan[1] = region.endRow;
        }
        return rowSpan;
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
