/*    */ package intradoc.common;
/*    */ 
/*    */ public class StructStatFS
/*    */ {
/*    */   public int f_type;
/*    */   public long f_flags;
/*    */   public long f_bsize;
/*    */   public long f_iosize;
/*    */   public long f_blocks;
/*    */   public long f_bfree;
/*    */   public long f_bavail;
/*    */   public long f_files;
/*    */   public long f_ffree;
/*    */   public long f_syncwrites;
/*    */   public long f_asyncwrites;
/*    */   public long f_syncreads;
/*    */   public long f_asyncreads;
/*    */   public long f_owner;
/*    */   public long f_fsid;
/*    */   public String f_fstypename;
/*    */   public String f_mntfromname;
/*    */   public String f_mntonname;
/*    */   public int f_namemax;
/*    */ 
/*    */   public boolean equals(Object obj)
/*    */   {
/* 52 */     StructStatFS stat = (StructStatFS)obj;
/* 53 */     return (this.f_type == stat.f_type) && (this.f_fsid == stat.f_fsid);
/*    */   }
/*    */ 
/*    */   public int hashCode()
/*    */   {
/* 59 */     int type = (this.f_type == 0) ? 5 : this.f_type;
/*    */ 
/* 61 */     long fsid = (this.f_fsid == 0L) ? 5L : this.f_fsid;
/* 62 */     return (int)(type * fsid * fsid & 0xFFFFFFFF);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 67 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 72718 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.StructStatFS
 * JD-Core Version:    0.5.4
 */