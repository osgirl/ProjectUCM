/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcLinguisticComparator;
/*     */ import intradoc.common.IdcLinguisticComparatorAdapter;
/*     */ import intradoc.common.IdcLocale;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.util.Observable;
/*     */ 
/*     */ public class WebChange extends Observable
/*     */   implements IdcLinguisticComparator
/*     */ {
/*     */   public String m_dID;
/*     */   public String m_dRevClassID;
/*     */   public char m_change;
/*     */   public char m_origChange;
/*     */   public char m_dReleaseState;
/*  52 */   public char m_dIndexerState = '\000';
/*     */   public String m_dWebExtension;
/*     */   protected static int m_recSize;
/*  57 */   protected static int m_webExtSize = 8;
/*     */   protected static int m_idSize;
/*  60 */   public static String m_defaultRule = IdcLinguisticComparatorAdapter.m_defaultRule;
/*     */   public IdcLinguisticComparator m_linguisticComparator;
/*     */   public static final int PRIMARY = 0;
/*     */   public static final int SECONDARY = 1;
/*     */   public static final int TERTIARY = 2;
/*     */ 
/*     */   public WebChange()
/*     */     throws ServiceException
/*     */   {
/*  81 */     this.m_dID = null;
/*  82 */     this.m_dRevClassID = null;
/*  83 */     this.m_change = '\000';
/*  84 */     this.m_origChange = '\000';
/*  85 */     this.m_dReleaseState = '\000';
/*  86 */     this.m_dWebExtension = null;
/*     */ 
/*  90 */     this.m_linguisticComparator = new IdcLinguisticComparatorAdapter();
/*  91 */     this.m_linguisticComparator.init(m_defaultRule);
/*     */   }
/*     */ 
/*     */   public static int getRecordSize()
/*     */   {
/*  96 */     return m_recSize;
/*     */   }
/*     */ 
/*     */   public WebChange(String id, String revClass, char change, char dReleaseState, String ext) throws ServiceException
/*     */   {
/* 101 */     this.m_dID = id;
/* 102 */     this.m_dRevClassID = revClass;
/* 103 */     this.m_change = change;
/* 104 */     this.m_origChange = change;
/* 105 */     this.m_dReleaseState = dReleaseState;
/* 106 */     this.m_dWebExtension = ext;
/*     */ 
/* 110 */     this.m_linguisticComparator = new IdcLinguisticComparatorAdapter();
/* 111 */     this.m_linguisticComparator.init(m_defaultRule);
/*     */   }
/*     */ 
/*     */   public static WebChange createFromBytes(byte[] buf) throws UnsupportedEncodingException, ServiceException
/*     */   {
/* 116 */     WebChange wc = new WebChange();
/* 117 */     wc.parseBytes(buf);
/* 118 */     return wc;
/*     */   }
/*     */ 
/*     */   public void parseBytes(byte[] buf) throws UnsupportedEncodingException
/*     */   {
/* 123 */     this.m_dID = parseString(buf, 0, m_idSize);
/* 124 */     this.m_dRevClassID = parseString(buf, m_idSize, m_idSize);
/* 125 */     this.m_dWebExtension = parseString(buf, 2 * m_idSize, m_webExtSize);
/* 126 */     this.m_change = (char)buf[(2 * m_idSize + m_webExtSize)];
/* 127 */     this.m_origChange = (char)buf[(2 * m_idSize + m_webExtSize + 1)];
/* 128 */     this.m_dReleaseState = (char)buf[(2 * m_idSize + m_webExtSize + 2)];
/* 129 */     this.m_dIndexerState = (char)buf[(2 * m_idSize + m_webExtSize + 3)];
/*     */   }
/*     */ 
/*     */   public String parseString(byte[] buf, int start, int max) throws UnsupportedEncodingException
/*     */   {
/* 134 */     int end = start + max;
/*     */ 
/* 136 */     for (int i = start; i < end; ++i)
/*     */     {
/* 138 */       if (buf[i] == 0) {
/*     */         break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 144 */     return new String(buf, start, i - start, FileUtils.m_javaSystemEncoding).trim();
/*     */   }
/*     */ 
/*     */   public byte[] convertToBytes()
/*     */   {
/* 149 */     byte[] buffer = new byte[m_recSize];
/* 150 */     int offset = 0;
/*     */ 
/* 153 */     for (int i = 0; i < buffer.length; ++i)
/*     */     {
/* 155 */       buffer[i] = 32;
/*     */     }
/*     */ 
/* 158 */     byte[] tmp = this.m_dID.getBytes();
/* 159 */     System.arraycopy(tmp, 0, buffer, offset, tmp.length);
/* 160 */     offset += m_idSize;
/*     */ 
/* 162 */     tmp = this.m_dRevClassID.getBytes();
/* 163 */     System.arraycopy(tmp, 0, buffer, offset, tmp.length);
/* 164 */     offset += m_idSize;
/*     */     try
/*     */     {
/* 168 */       tmp = this.m_dWebExtension.getBytes(FileUtils.m_javaSystemEncoding);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 172 */       Report.trace(null, null, e);
/* 173 */       tmp = this.m_dWebExtension.getBytes();
/*     */     }
/* 175 */     System.arraycopy(tmp, 0, buffer, offset, tmp.length);
/* 176 */     offset += m_webExtSize;
/*     */ 
/* 178 */     buffer[(offset++)] = (byte)this.m_change;
/* 179 */     buffer[(offset++)] = (byte)this.m_origChange;
/* 180 */     buffer[(offset++)] = (byte)this.m_dReleaseState;
/* 181 */     buffer[(offset++)] = (byte)this.m_dIndexerState;
/* 182 */     buffer[(offset++)] = 10;
/*     */ 
/* 184 */     return buffer;
/*     */   }
/*     */ 
/*     */   public static void setChange(byte[] buffer, char change)
/*     */   {
/* 189 */     buffer[(m_recSize - 3)] = (byte)change;
/*     */   }
/*     */ 
/*     */   public void setChange(char change)
/*     */   {
/* 194 */     if (SystemUtils.m_verbose)
/*     */     {
/* 196 */       Report.debug("indexer", "setting change on '" + this.m_dID + "' to '" + change + "'", null);
/*     */     }
/*     */ 
/* 199 */     this.m_change = change;
/* 200 */     setChanged();
/* 201 */     notifyObservers();
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/* 211 */     this.m_linguisticComparator.init();
/*     */   }
/*     */ 
/*     */   public void init(ExecutionContext context)
/*     */   {
/* 223 */     this.m_linguisticComparator.init(context);
/*     */   }
/*     */ 
/*     */   public void init(IdcLocale idcLocale)
/*     */   {
/* 233 */     this.m_linguisticComparator.init(idcLocale);
/*     */   }
/*     */ 
/*     */   public void init(String sortRule, int compLevel)
/*     */   {
/* 246 */     this.m_linguisticComparator.init(sortRule, compLevel);
/*     */   }
/*     */ 
/*     */   public void init(String sortRule)
/*     */   {
/* 257 */     this.m_linguisticComparator.init(sortRule);
/*     */   }
/*     */ 
/*     */   public void init(IdcLocale idcLocale, String sortRule, int compLevel)
/*     */   {
/* 277 */     this.m_linguisticComparator.init(idcLocale, sortRule, compLevel);
/*     */   }
/*     */ 
/*     */   public int compare(Object o1, Object o2)
/*     */   {
/* 282 */     WebChange c1 = null; WebChange c2 = null;
/* 283 */     if (o1 instanceof WebChange)
/*     */     {
/* 285 */       c1 = (WebChange)o1;
/* 286 */       c2 = (WebChange)o2;
/*     */     }
/*     */     else
/*     */     {
/*     */       try
/*     */       {
/* 292 */         c1 = createFromBytes((byte[])(byte[])o1);
/* 293 */         c2 = createFromBytes((byte[])(byte[])o2);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 297 */         Report.trace(null, "Failed comparison in WebChanges " + e.getMessage(), e);
/*     */       }
/*     */     }
/* 300 */     if ((c1 == null) || (c2 == null))
/*     */     {
/* 302 */       return 0;
/*     */     }
/*     */ 
/* 305 */     int comparison = 0;
/*     */ 
/* 307 */     comparison = this.m_linguisticComparator.compare(c1.m_dID, c2.m_dID);
/*     */ 
/* 309 */     return comparison;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 314 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 72401 $";
/*     */   }
/*     */ 
/*     */   static
/*     */   {
/*  69 */     m_idSize = Long.toString(9223372036854775807L).length();
/*  70 */     m_recSize = m_webExtSize;
/*  71 */     m_recSize += 2 * m_idSize;
/*  72 */     m_recSize += 1;
/*  73 */     m_recSize += 1;
/*  74 */     m_recSize += 1;
/*  75 */     m_recSize += 1;
/*  76 */     m_recSize += 1;
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.WebChange
 * JD-Core Version:    0.5.4
 */