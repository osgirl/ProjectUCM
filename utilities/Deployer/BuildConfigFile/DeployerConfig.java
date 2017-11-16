import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;


public class DeployerConfig
{
	public static final String O_STR = "<file name=\"";
	public static final String M_STR = "\" sourcelocation=\"";
	public static final String C_STR = "\" destination=\"\" locationtype=\"relative\"/>";
	public static final String OB = "<";
	public static final String CB = ">";
	public static final String D = ".";
	public static final String FS = "/";

	public static void main(String[] args)
	{
		try
		{
			FileInputStream fstream = new FileInputStream(System.getProperty("user.dir") + "/raw.txt");
			
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			ArrayList<String> printList = new ArrayList<String>();
			while ((strLine = br.readLine()) != null)
			{
				strLine = strLine.trim();
				if (strLine.contains(D))
				{
					printList.add(O_STR + strLine.substring(strLine.lastIndexOf(FS)+1) + M_STR
							+ strLine.substring(0, strLine.lastIndexOf(FS)) + C_STR);
				}
				else
				{
					printList.add(O_STR + M_STR + strLine + C_STR);
				}
			}
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			System.out.println(dateFormat.format(date)); 
			Collections.sort(printList);
			
			HashMap<String, String> movments = new HashMap<String, String>();
			movments.put("DEV", "LOCAL_DIR_FOR_DEV");
			movments.put("UK_OAT_CONTRIB", "LOCAL_DIR_FOR_UK_OAT_CONTRIB");
			movments.put("UK_OAT_CONSUMP", "LOCAL_DIR_FOR_UK_OAT");
			movments.put("APAC_OAT_CONTRIB", "LOCAL_DIR_FOR_APAC_OAT_CONTRIB");
			movments.put("APAC_OAT_CONSUMP", "LOCAL_DIR_FOR_APAC_OAT");
			movments.put("UK_PROD_CONSUMP", "LOCAL_DIR_FOR_UK_PROD");
			movments.put("APAC_PROD_CONSUMP", "LOCAL_DIR_FOR_APAC_PROD");
			
			System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			System.out.println("<deploy>");
			for(String s : movments.keySet())
			{
				System.out.println("\t<movement from=\""+s+"\" to=\""+movments.get(s) + "\">");
				for(String printingLine: printList)
					System.out.println("\t\t"+printingLine);
				System.out.println("\t</movement>");
			}
			System.out.println("</deploy>");
			in.close();
		}
		catch (Exception e)
		{
			System.err.println("Error: " + e.getMessage());
		}

	}

}
