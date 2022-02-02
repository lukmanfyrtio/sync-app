package com.application.syncapp.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.application.syncapp.response.BalanceResponse;

public class DataExcelExporter {

    private XSSFWorkbook workbook = new XSSFWorkbook();;
    private XSSFSheet sheet;
    private List<BalanceResponse>listB;
    
    public DataExcelExporter(List<BalanceResponse> list) {
        this.listB= list;
        workbook = new XSSFWorkbook();
    }
    
    
    private void writeHeaderLine() {
        sheet = workbook.createSheet("Users");
         
        Row row = sheet.createRow(0);
         
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);
         
        createCell(row, 0, "Wallet", style);      
        createCell(row, 1, "ETH Balance", style);
        createCell(row, 2, "BNB Balance", style);
        createCell(row, 3, "TUSD Balance", style);
         
    }
    
    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        }else {
            cell.setCellValue((String) value);
        }
        cell.setCellStyle(style);
    }
    
    private void writeDataLines() {
        int rowCount = 1;
 
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(10);
        font.setBold(true);
        style.setFont(font);
                 
        for (BalanceResponse balance : listB) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
             
            createCell(row, columnCount++, balance.getWallet(), style);
            createCell(row, columnCount++, balance.getEth(), style);
            createCell(row, columnCount++, balance.getBnb(), style);
            createCell(row, columnCount++, balance.getTusd(), style);
             
        }
    }
    
    public void export() throws FileNotFoundException, IOException {
        writeHeaderLine();
        writeDataLines();
         
        try (FileOutputStream outputStream = new FileOutputStream("/Users/lukmanfyrtio/Downloads/Wallet-"+new Date().getTime()+".xlsx")) {
            workbook.write(outputStream);
        }
         
         
    }
     
    
    
    
}
