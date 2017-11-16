/*    */ package intradoc.server.chunker;
/*    */ 
/*    */ import intradoc.common.ExecutionContext;
/*    */ import intradoc.common.IdcPipedInputStream;
/*    */ import intradoc.common.IdcPipedOutputStream;
/*    */ import intradoc.common.LocaleResources;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.provider.IncomingConnection;
/*    */ import java.io.IOException;
/*    */ import java.io.InputStream;
/*    */ import java.io.OutputStream;
/*    */ 
/*    */ public class ChunkedIncomingConnection
/*    */   implements IncomingConnection
/*    */ {
/*    */   protected IdcPipedInputStream m_ipis;
/*    */   protected IdcPipedOutputStream m_ipos;
/*    */ 
/*    */   public InputStream getInputStream()
/*    */     throws IOException
/*    */   {
/* 34 */     return this.m_ipis;
/*    */   }
/*    */ 
/*    */   public OutputStream getOutputStream() throws IOException
/*    */   {
/* 39 */     return this.m_ipos;
/*    */   }
/*    */ 
/*    */   public void setProviderData(DataBinder providerData)
/*    */   {
/*    */   }
/*    */ 
/*    */   public DataBinder getProviderData()
/*    */   {
/* 48 */     return null;
/*    */   }
/*    */ 
/*    */   public void prepareUse(DataBinder binder)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void checkRequestAllowed(DataBinder binder, ExecutionContext cxt)
/*    */     throws ServiceException
/*    */   {
/*    */   }
/*    */ 
/*    */   public void close()
/*    */   {
/*    */     try
/*    */     {
/* 65 */       this.m_ipis.close();
/* 66 */       this.m_ipos.close();
/*    */     }
/*    */     catch (IOException e)
/*    */     {
/* 70 */       e.printStackTrace();
/*    */     }
/*    */   }
/*    */ 
/*    */   public void setInputStream(IdcPipedInputStream in)
/*    */     throws IOException
/*    */   {
/* 77 */     if (in == null)
/*    */     {
/* 79 */       throw new IOException(LocaleResources.localizeMessage("!csChunkingNullInputStream", null));
/*    */     }
/*    */ 
/* 82 */     this.m_ipis = in;
/*    */   }
/*    */ 
/*    */   public void setOutputStream(IdcPipedOutputStream out) throws IOException
/*    */   {
/* 87 */     if (out == null)
/*    */     {
/* 89 */       throw new IOException(LocaleResources.localizeMessage("!csChunkingNullOutputStream", null));
/*    */     }
/*    */ 
/* 92 */     this.m_ipos = out;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 98 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.chunker.ChunkedIncomingConnection
 * JD-Core Version:    0.5.4
 */