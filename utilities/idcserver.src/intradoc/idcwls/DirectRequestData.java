/*    */ package intradoc.idcwls;
/*    */ 
/*    */ import intradoc.data.DataBinder;
/*    */ import java.io.InputStream;
/*    */ import java.io.OutputStream;
/*    */ import java.util.Map;
/*    */ 
/*    */ public class DirectRequestData
/*    */ {
/*    */   public boolean m_isDirect;
/*    */   public DataBinder m_binder;
/*    */   public Map m_args;
/*    */   public InputStream m_in;
/*    */   public OutputStream m_out;
/*    */   public String m_tracingSection;
/*    */ 
/*    */   public DirectRequestData()
/*    */   {
/* 67 */     this.m_isDirect = false;
/*    */   }
/*    */ 
/*    */   public DirectRequestData(DataBinder binder, Map args, InputStream in, OutputStream out, String tracingSection)
/*    */   {
/* 76 */     this.m_isDirect = true;
/* 77 */     this.m_binder = binder;
/* 78 */     this.m_args = args;
/* 79 */     this.m_in = in;
/* 80 */     this.m_out = out;
/* 81 */     this.m_tracingSection = tracingSection;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 86 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70444 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.idcwls.DirectRequestData
 * JD-Core Version:    0.5.4
 */