/*    */ package intradoc.admin;
/*    */ 
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.provider.Provider;
/*    */ import intradoc.provider.SocketOutgoingProvider;
/*    */ 
/*    */ public class AdminOutgoingProvider extends SocketOutgoingProvider
/*    */ {
/*    */   public void init(String host, int port)
/*    */     throws DataException
/*    */   {
/* 42 */     DataBinder provData = new DataBinder();
/* 43 */     provData.putLocal("IntradocServerHostName", host);
/* 44 */     provData.putLocal("ServerPort", String.valueOf(port));
/* 45 */     Provider provider = new Provider(provData);
/*    */ 
/* 47 */     super.init(provider);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 52 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.admin.AdminOutgoingProvider
 * JD-Core Version:    0.5.4
 */