/*    */ package intradoc.common;
/*    */ 
/*    */ import java.util.Map;
/*    */ import java.util.regex.Pattern;
/*    */ 
/*    */ public class TraceSection
/*    */ {
/*    */   public String m_name;
/* 35 */   public String m_substring = null;
/* 36 */   public Pattern m_regex = null;
/* 37 */   public String m_regexStr = null;
/* 38 */   public boolean m_outputMatches = true;
/* 39 */   public boolean m_isOutput = true;
/*    */ 
/* 42 */   public boolean m_isFileOutput = false;
/* 43 */   public String m_logFileName = null;
/* 44 */   public int m_verboseLevel = 0;
/*    */ 
/*    */   public TraceSection()
/*    */   {
/*    */   }
/*    */ 
/*    */   public TraceSection(String name)
/*    */   {
/* 53 */     init(name);
/*    */   }
/*    */ 
/*    */   public void init(String name)
/*    */   {
/* 58 */     this.m_name = name;
/* 59 */     Map env = SystemUtils.getReadOnlyEnvironment();
/* 60 */     if (env != null)
/*    */     {
/* 62 */       this.m_outputMatches = StringUtils.convertToBool((String)env.get(name + ":traceFilterOutputMatches"), true);
/*    */ 
/* 64 */       this.m_substring = ((String)env.get(name + ":traceSubstringFilter"));
/* 65 */       this.m_regexStr = ((String)env.get(name + ":traceRegexFilter"));
/*    */     }
/* 67 */     if (this.m_regexStr == null)
/*    */       return;
/* 69 */     this.m_regex = Pattern.compile(this.m_regexStr);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 75 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66817 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.TraceSection
 * JD-Core Version:    0.5.4
 */