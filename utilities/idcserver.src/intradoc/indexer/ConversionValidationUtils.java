/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ConversionValidationUtils
/*     */ {
/*  39 */   public static Properties m_tifValidateFormatMap = null;
/*  40 */   protected boolean m_isRequiringValidation = false;
/*     */ 
/*     */   public ConversionValidationUtils()
/*     */   {
/*  44 */     init();
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/*  49 */     this.m_isRequiringValidation = SharedObjects.getEnvValueAsBoolean("ConversionRequiresValidation", false);
/*  50 */     setTifValidateFormatMap();
/*     */   }
/*     */ 
/*     */   public void setTifValidateFormatMap()
/*     */   {
/*  57 */     m_tifValidateFormatMap = null;
/*     */ 
/*  59 */     String formatListStr = SharedObjects.getEnvironmentValue("TextIndexerFilterValidateFormats");
/*  60 */     if (formatListStr == null)
/*     */       return;
/*  62 */     m_tifValidateFormatMap = new Properties();
/*  63 */     Vector formatList = StringUtils.parseArrayEx(formatListStr, ',', '^', true);
/*  64 */     int numFormats = formatList.size();
/*  65 */     for (int i = 0; i < numFormats; ++i)
/*     */     {
/*  67 */       String format = (String)formatList.elementAt(i);
/*  68 */       format = format.toLowerCase();
/*  69 */       if (format.equals(""))
/*     */         continue;
/*  71 */       m_tifValidateFormatMap.put(format, "1");
/*     */     }
/*     */ 
/*  74 */     this.m_isRequiringValidation = true;
/*     */   }
/*     */ 
/*     */   public String validateExportedFile(Properties props, String filePath)
/*     */     throws ServiceException
/*     */   {
/*  81 */     String format = props.getProperty("dFormat");
/*  82 */     format = format.toLowerCase();
/*  83 */     if ((m_tifValidateFormatMap != null) && (m_tifValidateFormatMap.getProperty(format) == null))
/*     */     {
/*  85 */       return filePath;
/*     */     }
/*     */ 
/*  89 */     int index = filePath.lastIndexOf(".");
/*  90 */     String newFilePath = filePath.substring(0, index) + "_validated.txt";
/*     */ 
/*  93 */     FileInputStream fis = null;
/*  94 */     FileOutputStream fos = null;
/*     */     try
/*     */     {
/*  98 */       fis = new FileInputStream(filePath);
/*  99 */       fos = new FileOutputStream(newFilePath);
/*     */ 
/* 101 */       File file = new File(filePath);
/* 102 */       long bytesLeft = file.length();
/* 103 */       byte[] b = new byte[10000];
/*     */ 
/* 105 */       while (bytesLeft > 0L)
/*     */       {
/* 107 */         int numToRead = 10000;
/* 108 */         if (bytesLeft < numToRead)
/*     */         {
/* 110 */           numToRead = (int)bytesLeft;
/*     */         }
/*     */ 
/* 113 */         int numRead = fis.read(b, 0, numToRead);
/*     */ 
/* 116 */         for (int i = 0; i < numRead; ++i)
/*     */         {
/* 118 */           if ((b[i] < 0) || (b[i] >= 32) || (b[i] == 9) || (b[i] == 10) || (b[i] == 13))
/*     */             continue;
/* 120 */           b[i] = 32;
/*     */         }
/*     */ 
/* 124 */         fos.write(b, 0, numRead);
/* 125 */         bytesLeft -= numRead;
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/* 135 */       closeStreams(fis, fos);
/*     */     }
/*     */ 
/* 138 */     return newFilePath;
/*     */   }
/*     */ 
/*     */   public void closeStreams(FileInputStream fis, FileOutputStream fos)
/*     */     throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 146 */       if (fis != null)
/*     */       {
/* 148 */         fis.close();
/*     */       }
/*     */ 
/* 151 */       if (fos != null)
/*     */       {
/* 153 */         fos.close();
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 158 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void validate(Properties props) throws ServiceException
/*     */   {
/* 164 */     boolean isRequiringValidation = StringUtils.convertToBool(props.getProperty("conversionRequiresValidation"), this.m_isRequiringValidation);
/* 165 */     if (!isRequiringValidation)
/*     */       return;
/* 167 */     String outputFilePath = props.getProperty("DOC_FN");
/* 168 */     outputFilePath = validateExportedFile(props, outputFilePath);
/* 169 */     props.put("DOC_FN", outputFilePath);
/*     */   }
/*     */ 
/*     */   public static String getOutputFilePath(IndexerInfo ii, Properties props, boolean isRename, String outputDir)
/*     */     throws ServiceException
/*     */   {
/* 185 */     String fileName = ii.m_indexKey;
/*     */ 
/* 187 */     boolean default7Bit = EnvUtils.isFamily("windows");
/* 188 */     boolean use7BitOutputFilePath = SharedObjects.getEnvValueAsBoolean("Use7BitOutputFilePath", default7Bit);
/* 189 */     if (use7BitOutputFilePath)
/*     */     {
/* 191 */       IdcStringBuilder tempBuf = new IdcStringBuilder();
/*     */       try
/*     */       {
/* 194 */         StringUtils.appendAsHex(tempBuf, fileName);
/* 195 */         fileName = tempBuf.toString();
/*     */       }
/*     */       catch (Exception ignore)
/*     */       {
/* 200 */         Report.trace("indexer", null, ignore);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 205 */     String extension = null;
/* 206 */     if (isRename)
/*     */     {
/* 208 */       extension = props.getProperty("indexerMapExtension");
/*     */     }
/*     */     else
/*     */     {
/* 212 */       extension = "txt";
/*     */     }
/*     */ 
/* 216 */     String outputFilePath = outputDir + fileName + "." + extension;
/*     */ 
/* 218 */     File outputFile = new File(outputFilePath);
/* 219 */     if (outputFile.exists())
/*     */     {
/* 221 */       outputFile.delete();
/*     */     }
/*     */ 
/* 224 */     return outputFilePath;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 229 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94535 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.ConversionValidationUtils
 * JD-Core Version:    0.5.4
 */