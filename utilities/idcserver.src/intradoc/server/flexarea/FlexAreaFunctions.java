/*     */ package intradoc.server.flexarea;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataException;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.File;
/*     */ import java.io.FileReader;
/*     */ import java.io.FileWriter;
/*     */ import java.io.IOException;
/*     */ import java.io.Writer;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class FlexAreaFunctions
/*     */ {
/*     */   public static void mergeFlexArea(BufferedReader bufReader, Writer writer, FlexAreaOutput flexOutput)
/*     */     throws IOException, DataException
/*     */   {
/*  44 */     String line = null;
/*  45 */     String startPattern = "<!---@@";
/*  46 */     String endPattern = "-->";
/*  47 */     boolean inFlexArea = false;
/*  48 */     while ((line = bufReader.readLine()) != null)
/*     */     {
/*  50 */       int curIndex = 0;
/*  51 */       int newIndex = 0;
/*  52 */       while ((newIndex = line.indexOf(startPattern, curIndex)) >= 0)
/*     */       {
/*  54 */         int startIndex = startPattern.length() + newIndex;
/*  55 */         int endIndex = line.indexOf(endPattern, startIndex);
/*  56 */         if (endIndex < 0) {
/*     */           break;
/*     */         }
/*     */ 
/*  60 */         newIndex = endIndex + endPattern.length();
/*  61 */         if (newIndex > curIndex)
/*     */         {
/*  63 */           writer.write(line.substring(curIndex, newIndex));
/*     */         }
/*  65 */         curIndex = newIndex;
/*     */ 
/*  67 */         String areaDes = line.substring(startIndex, endIndex);
/*  68 */         if (inFlexArea)
/*     */         {
/*  70 */           if (areaDes.substring(0, 3).equalsIgnoreCase("END"))
/*     */           {
/*  72 */             inFlexArea = false;
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/*  77 */           Vector paramList = StringUtils.parseArray(areaDes, ' ', '+');
/*  78 */           if (paramList.size() > 0)
/*     */           {
/*  80 */             String area = (String)paramList.elementAt(0);
/*  81 */             Properties params = new Properties();
/*  82 */             for (int j = 1; j < paramList.size(); ++j)
/*     */             {
/*  84 */               String nameValue = (String)paramList.elementAt(j);
/*  85 */               int eqIndex = nameValue.indexOf(61);
/*  86 */               String name = nameValue;
/*  87 */               String val = "";
/*  88 */               if (eqIndex > 0)
/*     */               {
/*  90 */                 name = name.substring(0, eqIndex);
/*  91 */                 val = name.substring(eqIndex + 1);
/*     */               }
/*  93 */               if (name.length() <= 0)
/*     */                 continue;
/*  95 */               params.put(name, val);
/*     */             }
/*     */ 
/*  98 */             flexOutput.substituteArea(writer, area, params);
/*     */           }
/* 100 */           inFlexArea = true;
/*     */         }
/*     */       }
/*     */ 
/* 104 */       if (!inFlexArea)
/*     */       {
/* 106 */         writer.write(line.substring(curIndex) + "\n");
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void createMergedFile(String dir, String inFile, String outFile, FlexAreaOutput flexOutput)
/*     */     throws DataException
/*     */   {
/* 116 */     File tempFile = new File(dir, "__temp.dat");
/* 117 */     File dataFile = new File(outFile);
/*     */ 
/* 120 */     BufferedReader bufReader = null;
/* 121 */     Writer writer = null;
/*     */     try
/*     */     {
/* 124 */       bufReader = new BufferedReader(new FileReader(inFile));
/* 125 */       writer = new BufferedWriter(new FileWriter(tempFile));
/*     */ 
/* 127 */       mergeFlexArea(bufReader, writer, flexOutput);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 131 */       throw new DataException(e, "syFileSaveError", new Object[1]);
/*     */     }
/*     */     finally
/*     */     {
/* 135 */       FileUtils.closeObjects(bufReader, writer);
/*     */     }
/*     */ 
/* 138 */     if ((dataFile.exists()) && 
/* 140 */       (!dataFile.delete()))
/*     */     {
/*     */       try
/*     */       {
/* 144 */         String msg = LocaleUtils.encodeMessage("syUnableToDeleteFile", null, outFile);
/*     */ 
/* 146 */         FileUtils.validateFile(outFile, msg);
/*     */       }
/*     */       catch (ServiceException se)
/*     */       {
/* 150 */         throw new DataException(null, se);
/*     */       }
/*     */     }
/*     */ 
/* 154 */     tempFile.renameTo(dataFile);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 159 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95352 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.flexarea.FlexAreaFunctions
 * JD-Core Version:    0.5.4
 */