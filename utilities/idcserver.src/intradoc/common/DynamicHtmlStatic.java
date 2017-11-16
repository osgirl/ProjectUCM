/*     */ package intradoc.common;
/*     */ 
/*     */ public class DynamicHtmlStatic
/*     */ {
/*  33 */   public static final char[] XML_SCRIPT_TAG_START_CHARS = { '[', '<' };
/*  34 */   public static final char[] XML_SIMPLE_SCRIPT_TAG_START_CHARS = { '<' };
/*  35 */   public static final char[] XML_SCRIPT_TAG_END_CHARS = { ']', '>' };
/*  36 */   public static final char[] XML_SCRIPT_TAG = { '!', '-', '-', '$' };
/*     */   public static final int CLOSE_TAG_EXTRA_LENGTH = 2;
/*  42 */   public static final char[] NORMAL_SCRIPT_START_COMMENT_CHARS = { '[', '[', '%' };
/*     */ 
/*  47 */   public static final char[] NORMAL_SCRIPT_END_COMMENT_CHARS = { '%', ']', ']' };
/*     */   public static final int REDUCE_WHITESPACE = 1;
/*     */   public static final int TRIM_TEXT = 2;
/*     */   public static final int USE_XML_SYNTAX = 4;
/*  68 */   public static String[] m_directiveFlags = { "reducews", "trimtext", "xmlsyntax" };
/*     */ 
/*  73 */   public static int m_defaultDirectives = 1;
/*     */ 
/*  78 */   public static int m_numCustomParseStateObjects = 2;
/*     */ 
/*  83 */   protected static boolean m_isInit = false;
/*     */ 
/*     */   public static void checkInit(ParseOutput parseOutput)
/*     */   {
/*  90 */     if (m_isInit) {
/*     */       return;
/*     */     }
/*     */ 
/*  94 */     m_isInit = true;
/*     */   }
/*     */ 
/*     */   public static Object[] createNewParsingStateArray()
/*     */   {
/* 103 */     Object[] objArray = new Object[m_numCustomParseStateObjects];
/* 104 */     for (int i = 0; i < objArray.length; ++i)
/*     */     {
/* 106 */       objArray[i] = null;
/*     */     }
/* 108 */     return objArray;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 115 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.DynamicHtmlStatic
 * JD-Core Version:    0.5.4
 */