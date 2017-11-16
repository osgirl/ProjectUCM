/*    */ package intradoc.server.flexarea;
/*    */ 
/*    */ import intradoc.common.LocaleUtils;
/*    */ import intradoc.data.DataException;
/*    */ import java.io.IOException;
/*    */ import java.io.Writer;
/*    */ import java.util.Enumeration;
/*    */ import java.util.Properties;
/*    */ 
/*    */ public class ScriptFlexAreaOutput
/*    */   implements FlexAreaOutput
/*    */ {
/* 34 */   String m_includeName = null;
/*    */ 
/*    */   public ScriptFlexAreaOutput(String includeName) {
/* 37 */     this.m_includeName = includeName;
/*    */   }
/*    */ 
/*    */   public void substituteArea(Writer writer, String area, Properties params)
/*    */     throws DataException
/*    */   {
/* 44 */     StringBuffer buf = new StringBuffer();
/* 45 */     buf.append("<!---Flex areas are obsolete.  This area has been merged for backward compatibility.-->\n");
/* 46 */     appendNameValueAssignment(buf, "flexArea:name", area);
/* 47 */     Enumeration keys = params.keys();
/* 48 */     while (keys.hasMoreElements())
/*    */     {
/* 50 */       String key = (String)keys.nextElement();
/* 51 */       String val = params.getProperty(key);
/* 52 */       appendNameValueAssignment(buf, key, val);
/*    */     }
/*    */ 
/* 56 */     if (this.m_includeName != null)
/*    */     {
/* 58 */       buf.append("<$include ");
/* 59 */       buf.append(this.m_includeName);
/* 60 */       buf.append("$>\n");
/*    */     }
/*    */ 
/*    */     try
/*    */     {
/* 65 */       writer.write(buf.toString());
/*    */     }
/*    */     catch (IOException e)
/*    */     {
/* 69 */       String msg = LocaleUtils.encodeMessage("csFlexAreaException", null, area);
/* 70 */       throw new DataException(msg);
/*    */     }
/*    */   }
/*    */ 
/*    */   public void appendNameValueAssignment(StringBuffer buf, String key, String val)
/*    */   {
/* 76 */     buf.append("<$exec ");
/* 77 */     buf.append(key);
/* 78 */     buf.append("=\"");
/* 79 */     buf.append(val);
/* 80 */     buf.append("\"$>\n");
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 86 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.flexarea.ScriptFlexAreaOutput
 * JD-Core Version:    0.5.4
 */