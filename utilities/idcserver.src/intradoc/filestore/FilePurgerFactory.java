/*    */ package intradoc.filestore;
/*    */ 
/*    */ import intradoc.common.ExecutionContext;
/*    */ import intradoc.common.NumberUtils;
/*    */ import intradoc.common.filter.PurgerInterface;
/*    */ 
/*    */ public class FilePurgerFactory
/*    */ {
/*    */   protected BaseFileStore m_fileStore;
/* 27 */   protected byte[] m_zerosBuffer = null;
/* 28 */   protected byte[] m_onesBuffer = null;
/* 29 */   protected int m_passes = 0;
/*    */ 
/*    */   public FilePurgerFactory(FileStoreProvider fs)
/*    */   {
/* 33 */     this.m_fileStore = ((BaseFileStore)fs);
/*    */   }
/*    */ 
/*    */   public void init()
/*    */   {
/* 38 */     String str = this.m_fileStore.getConfigValue("FsScrubPasses", null, true);
/* 39 */     this.m_passes = NumberUtils.parseInteger(str, 0);
/*    */ 
/* 41 */     str = this.m_fileStore.getConfigValue("FsScrubBufferSize", null, true);
/* 42 */     int bufferSize = NumberUtils.parseInteger(str, 32000);
/* 43 */     this.m_zerosBuffer = new byte[bufferSize];
/* 44 */     this.m_onesBuffer = new byte[bufferSize];
/*    */ 
/* 46 */     for (int i = 0; i < bufferSize; ++i)
/*    */     {
/* 48 */       this.m_zerosBuffer[i] = 0;
/* 49 */       this.m_onesBuffer[i] = -1;
/*    */     }
/*    */   }
/*    */ 
/*    */   public PurgerInterface createPurger(ExecutionContext cxt)
/*    */   {
/* 55 */     FilePurger purger = new FilePurger(this.m_fileStore);
/* 56 */     purger.m_onesBuffer = this.m_onesBuffer;
/* 57 */     purger.m_zerosBuffer = this.m_zerosBuffer;
/* 58 */     purger.m_passes = this.m_passes;
/*    */ 
/* 60 */     purger.init(cxt);
/*    */ 
/* 62 */     return purger;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 67 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71793 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.FilePurgerFactory
 * JD-Core Version:    0.5.4
 */