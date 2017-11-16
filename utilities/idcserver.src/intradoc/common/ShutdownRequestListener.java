/*    */ package intradoc.common;
/*    */ 
/*    */ public class ShutdownRequestListener extends Thread
/*    */ {
/*    */   protected NativeOsUtils m_utils;
/*    */   protected boolean m_initialized;
/*    */   protected long m_handle;
/*    */ 
/*    */   public ShutdownRequestListener()
/*    */   {
/* 25 */     this.m_initialized = false;
/* 26 */     this.m_handle = -1L;
/*    */   }
/*    */ 
/*    */   public boolean init(String identifier)
/*    */   {
/* 33 */     if (!this.m_initialized)
/*    */     {
/* 35 */       this.m_utils = new NativeOsUtils();
/* 36 */       if (this.m_utils.isWin32())
/*    */       {
/* 38 */         if (identifier == null)
/*    */         {
/* 40 */           identifier = this.m_utils.getEnv("SHUTDOWN_MUTEX_NAME");
/*    */         }
/* 42 */         if (identifier == null)
/*    */         {
/* 45 */           this.m_initialized = true;
/* 46 */           return false;
/*    */         }
/* 48 */         this.m_handle = this.m_utils.createMutex(identifier);
/* 49 */         if (this.m_handle == 0L)
/*    */         {
/* 51 */           Report.trace("shutdownrequest", "openMutex(\"" + identifier + "\") failed.", null);
/*    */         }
/*    */         else
/*    */         {
/* 56 */           Report.trace("shutdownrequest", "openMutex(\"" + identifier + "\") returned " + this.m_handle + ".", null);
/*    */ 
/* 59 */           this.m_initialized = true;
/* 60 */           start();
/*    */         }
/*    */       }
/*    */     }
/* 64 */     return this.m_handle >= 0L;
/*    */   }
/*    */ 
/*    */   public void run()
/*    */   {
/* 70 */     if (!this.m_initialized)
/*    */     {
/* 72 */       Report.trace("shutdownrequest", "Shutdown request listener thread started without initialization.", null);
/*    */ 
/* 75 */       return;
/*    */     }
/*    */ 
/*    */     while (true)
/*    */     {
/* 80 */       long rc = this.m_utils.waitMutex(this.m_handle, NativeOsUtils.INFINITE);
/* 81 */       if (rc != NativeOsUtils.WAIT_FAILED)
/*    */       {
/* 83 */         String msg = "Shutting down on external request.";
/* 84 */         Report.trace("shutdownrequest", msg, null);
/* 85 */         System.exit(0);
/*    */       }
/*    */       else
/*    */       {
/* 89 */         Report.trace("shutdownrequest", "waitMutex() failed.", null);
/*    */ 
/* 91 */         SystemUtils.sleepRandom(800L, 1200L);
/*    */       }
/*    */     }
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 98 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ShutdownRequestListener
 * JD-Core Version:    0.5.4
 */