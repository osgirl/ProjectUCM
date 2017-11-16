/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class HtmlChunk
/*     */ {
/*     */   public static final int HTML_TEXT = 0;
/*     */   public static final int MERGE_VARIABLE = 1;
/*     */   public static final int EXEC_STATEMENT = 2;
/*     */   public static final int OPTION_LIST = 3;
/*     */   public static final int IF_STATEMENT = 4;
/*     */   public static final int ELSEIF_STATEMENT = 5;
/*     */   public static final int ELSE_STATEMENT = 6;
/*     */   public static final int ENDIF_STATEMENT = 7;
/*     */   public static final int BEGIN_LOOP = 8;
/*     */   public static final int BEGIN_LOOPWHILE = 9;
/*     */   public static final int BREAK_LOOP = 10;
/*     */   public static final int END_LOOP = 11;
/*     */   public static final int INCLUDE_RESOURCE = 12;
/*     */   public static final int BEGIN_DATA = 13;
/*     */   public static final int END_DATA = 14;
/*     */   public static final int DIRECTIVE = 15;
/*     */   public static final int COMMENT = 16;
/*  54 */   public static final String[] CHUNK_TYPES = { "html", "merge", "exec", "optList", "if", "elseif", "else", "endif", "loop", "loopwhile", "break", "endloop", "include", "idcbegindata", "idcenddata", "directive", "comment" };
/*     */   public int m_chunkType;
/*     */   public GrammarElement m_grammarElement;
/*     */   public char[] m_chars;
/*     */   public int m_charsOffset;
/*     */   public int m_charsLength;
/*     */   public int m_parseLine;
/*     */   public int m_parseCharOffset;
/*     */   public String m_fileName;
/* 101 */   public IdcBreakpoints m_markers = null;
/*     */ 
/* 107 */   public Object m_extraData = null;
/*     */ 
/*     */   public HtmlChunk()
/*     */   {
/* 111 */     init(null);
/*     */   }
/*     */ 
/*     */   public HtmlChunk(IdcBreakpoints bps)
/*     */   {
/* 116 */     init(bps);
/*     */   }
/*     */ 
/*     */   protected void init(IdcBreakpoints bps)
/*     */   {
/* 121 */     this.m_chunkType = 0;
/* 122 */     this.m_grammarElement = null;
/* 123 */     this.m_chars = null;
/* 124 */     this.m_charsOffset = 0;
/* 125 */     this.m_charsLength = 0;
/* 126 */     this.m_parseLine = 0;
/* 127 */     this.m_parseCharOffset = 0;
/* 128 */     this.m_fileName = null;
/*     */ 
/* 131 */     this.m_markers = bps;
/*     */   }
/*     */ 
/*     */   public String getStringPresentation()
/*     */   {
/* 136 */     Vector v = new IdcVector();
/* 137 */     if (this.m_fileName == null)
/*     */     {
/* 139 */       if (this.m_markers != null)
/*     */       {
/* 141 */         v.addElement(this.m_markers.m_fileName);
/*     */       }
/*     */       else
/*     */       {
/* 145 */         v.addElement("");
/*     */       }
/*     */ 
/*     */     }
/*     */     else {
/* 150 */       v.addElement(this.m_fileName);
/*     */     }
/* 152 */     v.addElement("" + this.m_parseLine);
/* 153 */     v.addElement("" + this.m_parseCharOffset);
/* 154 */     v.addElement("" + CHUNK_TYPES[this.m_chunkType]);
/*     */ 
/* 156 */     return StringUtils.createString(v, ',', '^');
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 162 */     String rep = getStringPresentation() + "  element=" + this.m_grammarElement;
/* 163 */     return rep;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 168 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.HtmlChunk
 * JD-Core Version:    0.5.4
 */