/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ParseLocationInfo
/*     */ {
/*     */   public int m_parseLine;
/*     */   public int m_parseCharOffset;
/*     */   public String m_fileName;
/*     */   int m_parseState;
/*     */ 
/*     */   public ParseLocationInfo()
/*     */   {
/*  61 */     this.m_parseLine = 0;
/*  62 */     this.m_parseCharOffset = 0;
/*  63 */     this.m_fileName = null;
/*  64 */     this.m_parseState = 0;
/*     */   }
/*     */ 
/*     */   public void copy(ParseLocationInfo info)
/*     */   {
/*  69 */     this.m_parseLine = info.m_parseLine;
/*  70 */     this.m_parseCharOffset = info.m_parseCharOffset;
/*  71 */     this.m_fileName = info.m_fileName;
/*  72 */     this.m_parseState = info.m_parseState;
/*     */   }
/*     */ 
/*     */   public void reset()
/*     */   {
/*  77 */     this.m_parseLine = 0;
/*  78 */     this.m_parseCharOffset = 0;
/*  79 */     this.m_fileName = null;
/*  80 */     this.m_parseState = 0;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/*  90 */     String output = "[" + this.m_parseLine + "," + this.m_parseCharOffset + "] " + this.m_fileName;
/*  91 */     return output;
/*     */   }
/*     */ 
/*     */   public String getStringPresentation()
/*     */   {
/*  96 */     Vector v = new IdcVector();
/*  97 */     v.addElement("" + this.m_parseLine);
/*  98 */     v.addElement("" + this.m_parseCharOffset);
/*  99 */     if (this.m_fileName == null)
/*     */     {
/* 101 */       v.addElement("");
/*     */     }
/*     */     else
/*     */     {
/* 105 */       v.addElement(this.m_fileName);
/*     */     }
/* 107 */     v.addElement("" + this.m_parseState);
/*     */ 
/* 109 */     return StringUtils.createString(v, ':', '*');
/*     */   }
/*     */ 
/*     */   public void buildFromString(String str)
/*     */   {
/* 114 */     Vector v = StringUtils.parseArray(str, ':', '*');
/* 115 */     int size = v.size();
/* 116 */     if (size != 4)
/*     */     {
/* 118 */       Report.trace("idcDebug", "ParseLocationInfo.buildFromString: Error - incorrect vector size " + size + ".", null);
/*     */ 
/* 120 */       return;
/*     */     }
/*     */ 
/* 123 */     this.m_parseLine = NumberUtils.parseInteger((String)v.elementAt(0), -1);
/* 124 */     this.m_parseCharOffset = NumberUtils.parseInteger((String)v.elementAt(1), -1);
/* 125 */     this.m_fileName = ((String)v.elementAt(2));
/* 126 */     if (this.m_fileName.length() == 0)
/*     */     {
/* 128 */       this.m_fileName = null;
/*     */     }
/* 130 */     this.m_parseState = NumberUtils.parseInteger((String)v.elementAt(3), -1);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 135 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ParseLocationInfo
 * JD-Core Version:    0.5.4
 */