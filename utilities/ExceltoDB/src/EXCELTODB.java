import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;
import java.util.Scanner;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
 
public class EXCELTODB {
    
	public static void main(String[] args) {
       Properties prop = new Properties();
       String filePath = null;
       String siteLocale = null;
       String databaseDetail = null;
       
       try {
			InputStream is = new FileInputStream("environment.properties");
			prop.load(is);
			filePath = prop.getProperty("filePath");
			siteLocale = prop.getProperty("siteLocale");
		    databaseDetail = prop.getProperty("databaseDetail");
			System.out.println("filePath : " + filePath + "siteLocale : " + siteLocale + "databaseDetail : " +databaseDetail);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	   String[] dbDetail = databaseDetail.split("#");
       String databaseString = dbDetail[1];
       String username = dbDetail[2];
       String password = dbDetail[3];
       Scanner sc = new Scanner(System.in);
       System.out.println("Salary DataBase is to be updated for locale : " + siteLocale + " and database : "+databaseString+" Press y to confim : ");
		if(sc.nextLine().equalsIgnoreCase("y")){
			System.out.println("Executing program ");
    	try{
    		
    		Class.forName("oracle.jdbc.OracleDriver");
            Connection con = (Connection) DriverManager.getConnection(databaseString,username,password);
            con.setAutoCommit(false);
            PreparedStatement pstm = null ;
            PreparedStatement pstmDelete = null ;
            FileInputStream input = new FileInputStream(filePath);
            POIFSFileSystem fs = new POIFSFileSystem( input );
            HSSFWorkbook wb = new HSSFWorkbook(fs);
            HSSFSheet sheet = wb.getSheetAt(0);
            Row row;
            System.out.println("No. of rows =  "+sheet.getLastRowNum());
            String industry="NA";
            String locationname="NA";
            String sector="NA";
            String positiontype="NA";
            String area="NA";
            String company="NA";
            String positionname="NA";
            String salaryrange="NA";
            String salarytypical="NA";
            String note="NA";
            DataFormatter formatter = new DataFormatter(Locale.US);
            String sqlDelete = "delete from hays_Salary_guide where sitelocale = '"+siteLocale+"'";
            
            
            pstmDelete = (PreparedStatement) con.prepareStatement(sqlDelete);
            pstmDelete.execute();
            pstmDelete.close();
            System.out.println("Values deleted for locale  "+siteLocale);
            for(int i=1; i<=sheet.getLastRowNum(); i++){
                row = sheet.getRow(i);
                if(row.getCell(0).getStringCellValue() !=null  )
                industry = row.getCell(0).getStringCellValue().replace("'", "''").trim();
                System.out.println("INDUSTRY =  "+industry);
                if(row.getCell(1).getStringCellValue() !=null  )
                locationname = row.getCell(1).getStringCellValue().replace("'", "''").trim();
                System.out.println("LOCATION NAME =  "+locationname);
                if(row.getCell(2).getStringCellValue() !=null  )
                sector = row.getCell(2).getStringCellValue().replace("'", "''").trim();
                System.out.println("SECTOR =  "+sector);
                if(row.getCell(3).getStringCellValue() !=null  )
                positiontype = row.getCell(3).getStringCellValue().replace("'", "''").trim();
                System.out.println("POSITION TYPE =  "+positiontype);
                area = null;
                if(row.getCell(4).getStringCellValue() !=null  )
                area = row.getCell(4).getStringCellValue().replace("'", "''").trim();
                System.out.println("AREA =  "+area);
                company = null;
                if(row.getCell(5).getStringCellValue() !=null)
                company = row.getCell(5).getStringCellValue().replace("'", "''").trim();
                System.out.println("COMPANY =  "+company);
                if(row.getCell(6).getStringCellValue() !=null  )
                positionname = row.getCell(6).getStringCellValue().replace("'", "''").trim();
                System.out.println("POSITION NAME =  "+positionname);
                if(formatter.formatCellValue(row.getCell(7)) !=null  )
                salaryrange = formatter.formatCellValue(row.getCell(7)).trim();
                System.out.println("SALARY RANGE =  "+salaryrange);
                if(formatter.formatCellValue(row.getCell(8)) !=null)
                salarytypical = formatter.formatCellValue(row.getCell(8)).trim();
                System.out.println("SALARY TYPICAL =  "+salarytypical);
                if(row.getCell(9).getStringCellValue() !=null  ){
                note = row.getCell(9).getStringCellValue().replace("'", "''").trim();
                System.out.println("NOTE =  "+note);
                }
                System.out.println("SITELOCALE =  "+siteLocale);
                String sql = "INSERT INTO hays_salary_guide VALUES('"+industry+"','"+locationname+"','"+sector+"','"+positiontype+"','"+area+"','"+company+"','"+positionname+"','"+salaryrange+"','"+salarytypical+"','"+note+"','"+siteLocale+"')";
                System.out.println("sql : " + sql);
                pstm = (PreparedStatement) con.prepareStatement(sql);
                pstm.execute();
                pstm.close();
                System.out.println("Import rows "+i);
            }
            con.commit();
            con.close();
            input.close();
            System.out.println("Success import excel to Oracle table");
        }catch(ClassNotFoundException e){
            System.out.println(e);
        }catch(SQLException ex){
            System.out.println(ex);
        }catch(IOException ioe){
            System.out.println(ioe);
        }
    	catch(Exception e){
            System.out.println(e);
        }
		}
    }
   
}