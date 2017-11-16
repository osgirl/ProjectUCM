/*     */ package intradoc.common;
/*     */ 
/*     */ public class GrammarParseState
/*     */ {
/*     */   public boolean m_isXmlLiteralEscape;
/*     */   public char[] m_array;
/*     */   public int m_startOffset;
/*     */   public int m_endOffset;
/*     */   public String m_parseStr;
/*     */   public int m_parseStrOffset;
/*     */   public int m_parseLines;
/*     */   public int m_curLineOffset;
/*     */   public boolean m_ignoreLf;
/*     */   public char m_terminateChar;
/*     */ 
/*     */   GrammarParseState(char[] array, int startOffset, int endOffset, String parseStr, int parseStrOffset)
/*     */   {
/*  94 */     this.m_isXmlLiteralEscape = false;
/*  95 */     this.m_array = array;
/*  96 */     this.m_startOffset = startOffset;
/*  97 */     this.m_endOffset = endOffset;
/*  98 */     this.m_parseStr = parseStr;
/*  99 */     this.m_parseStrOffset = parseStrOffset;
/* 100 */     this.m_parseLines = 0;
/* 101 */     this.m_curLineOffset = parseStrOffset;
/* 102 */     this.m_ignoreLf = false;
/* 103 */     this.m_terminateChar = '\000';
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 108 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.GrammarParseState
 * JD-Core Version:    0.5.4
 */