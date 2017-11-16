/*    */ package intradoc.server.schema;
/*    */ 
/*    */ import java.io.OutputStream;
/*    */ import java.io.Writer;
/*    */ import java.security.MessageDigest;
/*    */ 
/*    */ public class SchemaOutputObject
/*    */ {
/*    */   public Writer m_writer;
/*    */   public MessageDigest m_digester;
/*    */   public String m_finalPath;
/*    */   public String m_tempPath;
/*    */   public OutputStream m_output;
/*    */   public boolean m_isNull;
/*    */ 
/*    */   public SchemaOutputObject()
/*    */   {
/* 32 */     this.m_isNull = false;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg) {
/* 36 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.schema.SchemaOutputObject
 * JD-Core Version:    0.5.4
 */