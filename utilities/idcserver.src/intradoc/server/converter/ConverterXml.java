/*    */ package intradoc.server.converter;
/*    */ 
/*    */ import intradoc.common.FileUtils;
/*    */ import intradoc.common.Report;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.common.SystemUtils;
/*    */ import java.io.File;
/*    */ import java.io.FileOutputStream;
/*    */ import javax.xml.transform.Transformer;
/*    */ import javax.xml.transform.TransformerFactory;
/*    */ import javax.xml.transform.stream.StreamResult;
/*    */ import javax.xml.transform.stream.StreamSource;
/*    */ 
/*    */ public class ConverterXml
/*    */ {
/*    */   public static void convertToHtml(String inputFilePath, String templateFilePath, String outputFilePath)
/*    */     throws ServiceException
/*    */   {
/* 36 */     StreamSource inSource = null;
/* 37 */     StreamResult outSource = null;
/*    */     try
/*    */     {
/* 40 */       if (SystemUtils.m_verbose)
/*    */       {
/* 42 */         Report.debug(null, "ConverterXml.convertToHtml:inputFilePath=" + inputFilePath + ";templateFilePath=" + templateFilePath + ";outputFilePath=" + outputFilePath, null);
/*    */       }
/*    */ 
/* 50 */       TransformerFactory tFactory = TransformerFactory.newInstance();
/*    */ 
/* 55 */       StreamSource xslSource = new StreamSource(new File(templateFilePath));
/* 56 */       Transformer transformer = tFactory.newTransformer(xslSource);
/*    */ 
/* 59 */       inSource = new StreamSource(new File(inputFilePath));
/* 60 */       FileOutputStream outStream = new FileOutputStream(outputFilePath);
/* 61 */       outSource = new StreamResult(outStream);
/* 62 */       transformer.transform(inSource, outSource);
/*    */     }
/*    */     catch (Exception e)
/*    */     {
/* 66 */       String docName = "";
/* 67 */       String template = "";
/* 68 */       int i = inputFilePath.lastIndexOf("/");
/* 69 */       if (i >= 0)
/*    */       {
/* 71 */         docName = inputFilePath.substring(i + 1);
/*    */       }
/* 73 */       int j = templateFilePath.lastIndexOf("/");
/* 74 */       if (j >= 0)
/*    */       {
/* 76 */         template = templateFilePath.substring(j + 1);
/*    */       }
/*    */       String msg;
/* 79 */       throw new ServiceException(msg);
/*    */     }
/*    */     finally
/*    */     {
/* 83 */       FileUtils.closeObjects(inSource, outSource);
/*    */     }
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 89 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95352 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.converter.ConverterXml
 * JD-Core Version:    0.5.4
 */