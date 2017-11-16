/*    */ package intradoc.common;
/*    */ 
/*    */ import java.awt.Image;
/*    */ import java.awt.image.ImageObserver;
/*    */ 
/*    */ public class WaitImageLoad
/*    */   implements ImageObserver
/*    */ {
/* 29 */   public boolean m_ready = false;
/* 30 */   public boolean m_isError = false;
/*    */ 
/*    */   public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h)
/*    */   {
/* 39 */     boolean isError = (infoflags & 0xC0) != 0;
/* 40 */     boolean isFullStop = (infoflags & 0x20) != 0;
/* 41 */     boolean isReadyToShow = (infoflags & 0x10) != 0;
/* 42 */     if ((isError) || (isFullStop) || (isReadyToShow))
/*    */     {
/* 44 */       if (isError)
/*    */       {
/* 46 */         this.m_isError = true;
/*    */       }
/* 48 */       notifyReady();
/* 49 */       return (!isFullStop) && (!isError);
/*    */     }
/* 51 */     return true;
/*    */   }
/*    */ 
/*    */   public synchronized void notifyReady()
/*    */   {
/* 56 */     this.m_ready = true;
/* 57 */     super.notify();
/*    */   }
/*    */ 
/*    */   public synchronized void waitReady() {
/* 61 */     if (this.m_ready)
/*    */       return;
/*    */     try
/*    */     {
/* 65 */       super.wait();
/*    */     }
/*    */     catch (InterruptedException ignore)
/*    */     {
/* 69 */       Report.trace(null, null, ignore);
/*    */     }
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 76 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.WaitImageLoad
 * JD-Core Version:    0.5.4
 */