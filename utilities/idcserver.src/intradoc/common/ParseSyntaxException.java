/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcException;
/*     */ 
/*     */ public class ParseSyntaxException extends IdcException
/*     */ {
/*     */   public ParseLocationInfo m_parseInfo;
/*  52 */   public boolean m_outerOffsetsUsed = false;
/*     */ 
/*     */   public ParseSyntaxException()
/*     */   {
/*     */   }
/*     */ 
/*     */   public ParseSyntaxException(ParseLocationInfo parseInfo, String msg)
/*     */   {
/*  61 */     init(null, 0, IdcMessageFactory.lc(), msg);
/*  62 */     this.m_parseInfo = parseInfo;
/*     */   }
/*     */ 
/*     */   public ParseSyntaxException(ParseLocationInfo parseInfo, String msg, boolean outerOffsetsUsed)
/*     */   {
/*  67 */     init(null, 0, IdcMessageFactory.lc(), msg);
/*  68 */     this.m_parseInfo = parseInfo;
/*  69 */     this.m_outerOffsetsUsed = outerOffsetsUsed;
/*     */   }
/*     */ 
/*     */   public ParseSyntaxException(ParseLocationInfo parseInfo, boolean outerOffsetsUsed, String key, Object[] args)
/*     */   {
/*  75 */     init(null, 0, IdcMessageFactory.lc(key, args), null);
/*  76 */     this.m_parseInfo = parseInfo;
/*  77 */     this.m_outerOffsetsUsed = outerOffsetsUsed;
/*     */   }
/*     */ 
/*     */   public String createMessage(String msg, String parseStr)
/*     */     throws IllegalArgumentException
/*     */   {
/*  83 */     int line = this.m_parseInfo.m_parseLine + 1;
/*  84 */     int offset = this.m_parseInfo.m_parseCharOffset + 1;
/*     */ 
/*  86 */     msg = LocaleUtils.appendMessage(msg, getMessage());
/*     */ 
/*  88 */     if (line > 1)
/*     */     {
/*  90 */       msg = LocaleUtils.encodeMessage("csDynHTMLArgumentMessage1", msg, Integer.toString(line), Integer.toString(offset), parseStr);
/*     */     }
/*     */     else
/*     */     {
/*  98 */       msg = LocaleUtils.encodeMessage("csDynHTMLArgumentMessage2", msg, Integer.toString(offset), parseStr);
/*     */     }
/*     */ 
/* 103 */     return msg;
/*     */   }
/*     */ 
/*     */   public void initFactory()
/*     */   {
/* 108 */     this.m_factory = IdcStringBuilderFactory.m_defaultFactory;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 113 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84223 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ParseSyntaxException
 * JD-Core Version:    0.5.4
 */