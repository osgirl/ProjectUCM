/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.DynamicHtml;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcLocale;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ParseSyntaxException;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.SharedPageMergerData;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ import java.io.Writer;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class PageMaker
/*     */ {
/*  40 */   public DataBinder m_binder = null;
/*     */ 
/*     */   public PageMaker()
/*     */   {
/*  44 */     this.m_binder = null;
/*     */   }
/*     */ 
/*     */   public void buildPage(File file, String template, DataBinder binder, PageMerger pageMerger, ExecutionContext cxt)
/*     */     throws IOException, ParseSyntaxException
/*     */   {
/*  55 */     Properties oldProps = null;
/*     */ 
/*  58 */     DataBinder cachedBinder = null;
/*  59 */     OutputStream out = null;
/*  60 */     Writer writer = null;
/*     */     try
/*     */     {
/*  63 */       cachedBinder = (DataBinder)cxt.getCachedObject("DataBinder");
/*  64 */       this.m_binder = binder;
/*  65 */       this.m_binder.setEnvironment(new Properties(SharedObjects.getSecureEnvironment()));
/*  66 */       pageMerger.setActiveBinder(this.m_binder);
/*     */ 
/*  69 */       oldProps = this.m_binder.getLocalData();
/*  70 */       this.m_binder.setLocalData(new Properties(oldProps));
/*     */ 
/*  85 */       String encoding = FileUtils.m_javaSystemEncoding;
/*  86 */       String pageEncoding = DataSerializeUtils.getIsoEncoding(encoding);
/*  87 */       if (pageEncoding == null)
/*     */       {
/*  89 */         IdcLocale pageLocale = (IdcLocale)cxt.getCachedObject("PageLocale");
/*  90 */         if (pageLocale == null)
/*     */         {
/*  92 */           pageLocale = LocaleResources.getLocale("SystemLocale");
/*     */         }
/*  94 */         if ((pageLocale != null) && (pageLocale.m_pageEncoding != null))
/*     */         {
/*  96 */           encoding = DataSerializeUtils.getJavaEncoding(pageLocale.m_pageEncoding);
/*     */ 
/*  98 */           pageEncoding = pageLocale.m_pageEncoding;
/*     */         }
/* 100 */         if ((encoding == null) || (pageEncoding == null))
/*     */         {
/* 102 */           Report.trace("system", "Building static page using utf-8 because system encoding was unuseable.", null);
/*     */ 
/* 108 */           encoding = "UTF8";
/* 109 */           pageEncoding = "utf-8";
/*     */         }
/*     */       }
/* 112 */       this.m_binder.putLocal("PageCharset", pageEncoding);
/*     */ 
/* 115 */       out = FileUtils.openOutputStream(file.getAbsolutePath(), 16);
/* 116 */       writer = FileUtils.openDataWriterEx(out, encoding, 1);
/* 117 */       outputPage(template, writer, pageMerger);
/* 118 */       out = null;
/*     */     }
/*     */     finally
/*     */     {
/* 122 */       FileUtils.abortAndClose(out);
/* 123 */       FileUtils.closeObject(writer);
/* 124 */       if ((oldProps != null) && (binder != null))
/*     */       {
/* 126 */         binder.setLocalData(oldProps);
/*     */       }
/* 128 */       if (cachedBinder != null)
/*     */       {
/* 130 */         pageMerger.setActiveBinder(cachedBinder);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void outputPage(String template, Writer writer, PageMerger pageMerger)
/*     */     throws IOException, ParseSyntaxException
/*     */   {
/* 139 */     DynamicHtml dynHtml = SharedObjects.getHtmlPage(template);
/* 140 */     SharedPageMergerData.loadTemplateData(template, this.m_binder.getLocalData());
/* 141 */     if (dynHtml == null)
/*     */     {
/* 143 */       String msg = LocaleUtils.encodeMessage("csPageMergerTemplateDoesNotExist", null, template);
/*     */ 
/* 145 */       throw new IOException(msg);
/*     */     }
/* 147 */     pageMerger.outputNonPersonalizedHtml(dynHtml, writer);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 152 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95352 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.PageMaker
 * JD-Core Version:    0.5.4
 */