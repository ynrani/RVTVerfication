package com.sa.rvt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

import org.apache.poi.hssf.record.cf.PatternFormatting;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class VerifyWorksheet {

	public static void main(String[] args) throws IOException {
		
			
		
		String projectPath = System.getProperty("user.dir").toString();
		final  String OUTPUT_FILE_PATH = projectPath + "//output//Scenario15";
		/*System.out.println(" Please enter output File directory location : ");
		Scanner inputReader = new Scanner(System.in);
		System.out.println();*/
		String filePath = OUTPUT_FILE_PATH;
		if(filePath == null || filePath.isEmpty() ){
			System.out.println("Invalid file path");
			System.exit(1);
		}
		//inputReader.close();
			
		Map<String,Map<String, Map<String, String>>> parentReader = CsvReader.getComparedValues();
		try {
			List<String> filesInFolder = listAllFiles(filePath);
			for(String outputFileName : parentReader.keySet()){
				String actualFileName = outputFileName.replaceAll(":", "");
				/*actualFileName = actualFileName.replaceAll("(", "");
				actualFileName = actualFileName.replaceAll(")", "");*/
				if(!filesInFolder.contains(actualFileName+".xlsx")){
					System.out.println("OuputFile does not exist in specified path......");
					System.out.println(actualFileName+".xlsx");
					//System.exit(1);
					continue;
				}
				System.out.println(actualFileName+".xlsx");
				Map<String, Map<String, String>> reader = parentReader.get(outputFileName);
		//	InputStream file = new FileInputStream(new File(projectPath + "//output//Scenario 2.xlsx"));
			InputStream file = new FileInputStream(new File(filePath+"//"+actualFileName+".xlsx"));

			// Create Workbook instance holding reference to .xlsx file
			XSSFWorkbook workbook = new XSSFWorkbook(file);
			
			for (int mySheet = 0; mySheet <= workbook.getNumberOfSheets() - 1; mySheet++) {
				// Get desired sheet from the workbook
				XSSFSheet sheet = workbook.getSheetAt(mySheet);
				System.out.println(sheet.getSheetName());
				String sheetName = sheet.getSheetName().toLowerCase();

				//adding extra color
/*				SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

				// Condition 1: Formula   
				ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule("(C4-D4)=0");

				org.apache.poi.ss.usermodel.PatternFormatting fill1 = rule1.createPatternFormatting();
				//fill1.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.index);
				fill1.setFillBackgroundColor(IndexedColors.GREEN.index);
				fill1.setFillPattern(PatternFormatting.SOLID_FOREGROUND);

				// Condition 2: Formula   
				ConditionalFormattingRule rule2 = sheetCF.createConditionalFormattingRule("(C4-D4)!=0");

				org.apache.poi.ss.usermodel.PatternFormatting fill2 = rule2.createPatternFormatting();

				fill2.setFillBackgroundColor(IndexedColors.RED.index);
				fill2.setFillPattern(PatternFormatting.SOLID_FOREGROUND);*/
				
				
				//end of color

				
				if (sheetName.equalsIgnoreCase("summary") || sheetName.contains("cov") || sheetName.contains("coverage"))
					continue;
				// Iterate through each rows one by one
				try {
					Iterator<Row> rowIterator = sheet.iterator();
					int iHeaderChecker = 0;

					while (rowIterator.hasNext()) {
						int inititalIndex = 0;
						Row row = rowIterator.next();
						if (iHeaderChecker == 0) {
							iHeaderChecker++;
							continue;
						}
						// For each row, iterate through all the columns
						Iterator<Cell> cellIterator = row.cellIterator();
						if (cellIterator.hasNext()) {
							inititalIndex = cellIterator.next().getColumnIndex();
						} else {
							break;
						}
						if (row.getCell(inititalIndex) != null
								&& row.getCell(inititalIndex).getStringCellValue().trim().length() > 0) {
							String factor = row.getCell(inititalIndex).getStringCellValue();
							
							for (Map.Entry<String, Map<String, String>> entry : reader.entrySet()) {
								
								if (entry.getKey().contains(sheet.getSheetName().toUpperCase().split("-")[1])) {
									
									for (Map.Entry<String, String> subFilter : entry.getValue().entrySet()) {
										if (subFilter.getKey().equalsIgnoreCase(factor)) {
											
											row.getCell(inititalIndex + 2).setCellType(Cell.CELL_TYPE_NUMERIC);
											row.getCell(inititalIndex + 2).setCellValue(
													Double.valueOf(subFilter.getValue()));
											break;
										}
									}
								}
							}
							row.getCell(inititalIndex + 3).setCellType(Cell.CELL_TYPE_NUMERIC);
							row.getCell(inititalIndex + 3).setCellValue(
									row.getCell(inititalIndex + 2).getNumericCellValue()
											- row.getCell(inititalIndex + 1).getNumericCellValue());
							if (row.getCell(inititalIndex + 2).getNumericCellValue()
									- row.getCell(inititalIndex + 1).getNumericCellValue() != 0){
								CellStyle backgroundStyle = workbook.createCellStyle();
								row.removeCell(row.getCell(inititalIndex + 4));
							    backgroundStyle.setFillBackgroundColor(IndexedColors.DARK_BLUE.getIndex());
								XSSFFont font = workbook.createFont();
							     font.setFontName(XSSFFont.DEFAULT_FONT_NAME);
							     font.setFontHeightInPoints((short)10);
							     font.setColor(IndexedColors.RED.getIndex());
							     backgroundStyle.setFont(font);
							     backgroundStyle.setBorderBottom(CellStyle.BORDER_THIN);
								    backgroundStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
								    backgroundStyle.setBorderLeft(CellStyle.BORDER_THIN);
								    backgroundStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
								    backgroundStyle.setBorderRight(CellStyle.BORDER_THIN);
								    backgroundStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
								    backgroundStyle.setBorderTop(CellStyle.BORDER_THIN);
								    backgroundStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
								    
									row.createCell(inititalIndex + 4).setCellStyle(backgroundStyle);
								row.getCell(inititalIndex + 4).setCellValue("Fail");
							}
							else{
								CellStyle backgroundStyle = workbook.createCellStyle();
								row.removeCell(row.getCell(inititalIndex + 4));
							    backgroundStyle.setFillBackgroundColor(IndexedColors.DARK_BLUE.getIndex());
							    /*backgroundStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);*/
							    XSSFFont font = workbook.createFont();
							     font.setFontName(XSSFFont.DEFAULT_FONT_NAME);
							     font.setFontHeightInPoints((short)10);
							     font.setColor(IndexedColors.BLUE.getIndex());
							     backgroundStyle.setFont(font);
							     //cellStyle.setFont(font);
							    backgroundStyle.setBorderBottom(CellStyle.BORDER_THIN);
							    backgroundStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
							    backgroundStyle.setBorderLeft(CellStyle.BORDER_THIN);
							    backgroundStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
							    backgroundStyle.setBorderRight(CellStyle.BORDER_THIN);
							    backgroundStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
							    backgroundStyle.setBorderTop(CellStyle.BORDER_THIN);
							    backgroundStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
							    
								row.createCell(inititalIndex + 4).setCellStyle(backgroundStyle);
								row.getCell(inititalIndex+4).setCellValue("Pass");
								
								
							//row.setRowStyle(IndexedColors.GREEN.index);
							}
							
							
							
						}
						iHeaderChecker++;
						// System.out.println(iHeaderChecker);
						
											}
					/*org.apache.poi.ss.util.CellRangeAddress cellRangeAddress = new org.apache.poi.ss.util.CellRangeAddress(iFirstRow, iLastRow, iFirstRow+4, iFirstRow+4);
					org.apache.poi.ss.util.CellRangeAddress[] regions = {
						     //org.apache.poi.ss.util.CellRangeAddress.valueOf("F4:F4")
							cellRangeAddress
						     
						};

						//sheetCF.addConditionalFormatting(regions, rule1, rule2);
						sheetCF.addConditionalFormatting(regions, rule1);*/
				} catch (Exception e) {
					System.out.println("Getting error in the sheet =====> " + sheet.getSheetName());
					e.printStackTrace();
				}
			}
			file.close();
			FileOutputStream outFile = new FileOutputStream(new File(filePath+"//"+actualFileName
					+ Math.random() * 10 + ".xlsx"));
			workbook.write(outFile);
			// workbook.
			outFile.close();
			workbook.close();
		}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static List<String> listAllFiles(String path){
        System.out.println("In listAllfiles(String path) method");
        List<String> listFiles = new ArrayList<String>();
        try(Stream<Path> paths = Files.walk(Paths.get(path))) {
            paths.forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    try {
                    	System.out.println(filePath.getFileName().toString());
                    	listFiles.add(filePath.getFileName().toString());
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return listFiles;
    }
    
}