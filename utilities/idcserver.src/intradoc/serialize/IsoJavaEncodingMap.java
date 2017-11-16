/*     */ package intradoc.serialize;
/*     */ 
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class IsoJavaEncodingMap extends DataResultSet
/*     */ {
/*     */   protected List m_javaEnc;
/*     */   protected List m_isoEnc;
/*     */ 
/*     */   public IsoJavaEncodingMap()
/*     */   {
/*  34 */     this.m_javaEnc = new ArrayList();
/*  35 */     this.m_isoEnc = new ArrayList();
/*     */   }
/*     */ 
/*     */   public DataResultSet shallowClone()
/*     */   {
/*  41 */     DataResultSet rset = new IsoJavaEncodingMap();
/*  42 */     initShallow(rset);
/*     */ 
/*  44 */     return rset;
/*     */   }
/*     */ 
/*     */   public void load(ResultSet rset) throws DataException
/*     */   {
/*  49 */     int javaIndex = ResultSetUtils.getIndexMustExist(rset, "lcJavaEncoding");
/*  50 */     int isoIndex = ResultSetUtils.getIndexMustExist(rset, "lcIsoEncoding");
/*     */ 
/*  53 */     List javaEnc = new ArrayList();
/*  54 */     List isoEnc = new ArrayList();
/*  55 */     copy(rset);
/*     */ 
/*  58 */     for (int i = 0; isRowPresent(); ++i)
/*     */     {
/*  60 */       Vector v = getCurrentRowValues();
/*  61 */       if ((v != null) && (v.size() > 0))
/*     */       {
/*  63 */         String javaEncoding = (String)v.elementAt(javaIndex);
/*  64 */         String isoEncoding = (String)v.elementAt(isoIndex);
/*     */ 
/*  66 */         if ((javaEncoding != null) && (javaEncoding.length() > 0) && (isoEncoding != null) && (isoEncoding.length() > 0))
/*     */         {
/*  69 */           javaEnc.add(javaEncoding);
/*  70 */           isoEnc.add(isoEncoding);
/*     */         }
/*     */       }
/*  58 */       next();
/*     */     }
/*     */ 
/*  75 */     this.m_javaEnc = javaEnc;
/*  76 */     this.m_isoEnc = isoEnc;
/*     */   }
/*     */ 
/*     */   public String getIsoEncoding(String javaEncoding)
/*     */   {
/*  81 */     return getIsoOrJavaEncoding(javaEncoding, false);
/*     */   }
/*     */ 
/*     */   public String getJavaEncoding(String isoEncoding)
/*     */   {
/*  86 */     return getIsoOrJavaEncoding(isoEncoding, true);
/*     */   }
/*     */ 
/*     */   protected String getIsoOrJavaEncoding(String enc, boolean isJava)
/*     */   {
/*  91 */     String retEnc = null;
/*  92 */     List v1 = null;
/*  93 */     List v2 = null;
/*     */ 
/*  95 */     if (isJava)
/*     */     {
/*  97 */       v1 = this.m_isoEnc;
/*  98 */       v2 = this.m_javaEnc;
/*     */     }
/*     */     else
/*     */     {
/* 102 */       v1 = this.m_javaEnc;
/* 103 */       v2 = this.m_isoEnc;
/*     */     }
/*     */ 
/* 106 */     int i = -1;
/* 107 */     boolean found = false;
/* 108 */     for (i = 0; i < v1.size(); ++i)
/*     */     {
/* 110 */       String tempStr = (String)v1.get(i);
/* 111 */       if ((tempStr == null) || (!enc.equalsIgnoreCase(tempStr)))
/*     */         continue;
/* 113 */       found = true;
/* 114 */       break;
/*     */     }
/*     */ 
/* 117 */     if (found)
/*     */     {
/* 119 */       retEnc = (String)v2.get(i);
/*     */     }
/* 121 */     return retEnc;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 126 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.serialize.IsoJavaEncodingMap
 * JD-Core Version:    0.5.4
 */