/*     */ package intradoc.tools.utils;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStreamReader;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ 
/*     */ public class SimpleDataBinderUtils
/*     */ {
/*     */   public static String[][] getColumnsFromResultSetUsingFile(File hda, String rsetName, String[] columnNames)
/*     */     throws IOException
/*     */   {
/*  36 */     FileInputStream fis = new FileInputStream(hda);
/*  37 */     InputStreamReader isr = null;
/*  38 */     BufferedReader br = null;
/*     */     try
/*     */     {
/*  41 */       isr = new InputStreamReader(fis, "UTF-8");
/*  42 */       br = new BufferedReader(br);
/*  43 */       String[][] arrayOfString = getColumnsFromResultSetUsingReader(br, rsetName, columnNames);
/*     */ 
/*  53 */       return arrayOfString;
/*     */     }
/*     */     finally
/*     */     {
/*  47 */       if (br != null)
/*     */       {
/*  49 */         br.close();
/*     */       }
/*  51 */       else if (isr != null)
/*     */       {
/*  53 */         isr.close();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String[][] getColumnsFromResultSetUsingReader(BufferedReader br, String rsetName, String[] colNames)
/*     */     throws IOException
/*     */   {
/*  61 */     int numColumns = colNames.length;
/*  62 */     StringBuilder sb = new StringBuilder("@ResultSet ");
/*  63 */     sb.append(rsetName);
/*  64 */     String rsetLine = sb.toString();
/*     */     do
/*  66 */       if (rsetLine.equals(line = br.readLine()))
/*     */         break label75;
/*  68 */     while (line != null);
/*     */ 
/*  70 */     throw new IOException(new StringBuilder().append("unable to find result set ").append(rsetName).toString());
/*     */ 
/*  73 */     label75: String line = br.readLine();
/*  74 */     if (line == null)
/*     */     {
/*  76 */       throw new IOException(new StringBuilder().append("unexpected EOF in result set ").append(rsetName).toString());
/*     */     }
/*     */     int numFields;
/*     */     try
/*     */     {
/*  81 */       numFields = Integer.parseInt(line);
/*     */     }
/*     */     catch (NumberFormatException nfe)
/*     */     {
/*  85 */       throw new IOException(new StringBuilder().append("bad field count for result set ").append(rsetName).toString(), nfe);
/*     */     }
/*  87 */     int[] fieldToColumn = new int[numFields];
/*  88 */     for (int f = 0; f < numFields; ++f)
/*     */     {
/*  90 */       line = br.readLine();
/*  91 */       if (line == null)
/*     */       {
/*  93 */         throw new IOException(new StringBuilder().append("unexpected EOF in result set ").append(rsetName).toString());
/*     */       }
/*  95 */       if (line.equals("@end"))
/*     */       {
/*  97 */         throw new IOException(new StringBuilder().append("missing name for field index ").append(f).append(" in result set ").append(rsetName).toString());
/*     */       }
/*  99 */       int columnIndex = -1;
/* 100 */       for (int c = numColumns - 1; c >= 0; --c)
/*     */       {
/* 102 */         if (!line.equals(colNames[c]))
/*     */           continue;
/* 104 */         columnIndex = c;
/* 105 */         break;
/*     */       }
/*     */ 
/* 108 */       fieldToColumn[f] = columnIndex;
/*     */     }
/*     */ 
/* 111 */     List rows = new ArrayList();
/* 112 */     line = br.readLine();
/* 113 */     if (line == null)
/*     */     {
/* 115 */       throw new IOException(new StringBuilder().append("unexpected EOF in result set ").append(rsetName).toString());
/*     */     }
/* 117 */     while (!"@end".equals(line))
/*     */     {
/* 119 */       String[] row = new String[numColumns];
/* 120 */       rows.add(row);
/* 121 */       int f = 0;
/* 122 */       while (f < numFields)
/*     */       {
/* 124 */         if (line == null)
/*     */         {
/* 126 */           throw new IOException(new StringBuilder().append("unexpected EOF in result set ").append(rsetName).toString());
/*     */         }
/* 128 */         if (line.equals("@end"))
/*     */         {
/* 130 */           throw new IOException(new StringBuilder().append("missing columns in result set ").append(rsetName).toString());
/*     */         }
/* 132 */         int columnIndex = fieldToColumn[f];
/* 133 */         if (columnIndex >= 0)
/*     */         {
/* 135 */           row[columnIndex] = line;
/*     */         }
/* 137 */         ++f;
/* 138 */         line = br.readLine();
/*     */       }
/*     */     }
/*     */ 
/* 142 */     int numRows = rows.size();
/* 143 */     String[][] rowsArray = new String[numRows][];
/* 144 */     rows.toArray(rowsArray);
/* 145 */     return rowsArray;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 151 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98361 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.utils.SimpleDataBinderUtils
 * JD-Core Version:    0.5.4
 */