/*    */ package intradoc.common;
/*    */ 
/*    */ public class PosixStructStat
/*    */ {
/*    */   public int st_dev;
/*    */   public long st_ino;
/*    */   public int st_mode;
/*    */   public int st_nlink;
/*    */   public int st_uid;
/*    */   public int st_gid;
/*    */   public long st_atime;
/*    */   public long st_mtime;
/*    */   public long st_ctime;
/*    */   public long st_size;
/*    */   public long st_blocks;
/*    */   public int st_blksize;
/*    */ 
/*    */   public boolean equals(Object obj)
/*    */   {
/* 40 */     PosixStructStat stat = (PosixStructStat)obj;
/* 41 */     return (this.st_dev == stat.st_dev) && (this.st_ino == stat.st_ino);
/*    */   }
/*    */ 
/*    */   public int hashCode()
/*    */   {
/* 47 */     int dev = (this.st_dev == 0) ? 3 : this.st_dev;
/* 48 */     int ino = (this.st_ino == 0L) ? 5 : (int)this.st_ino;
/* 49 */     return dev * ino * ino;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 54 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 76784 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.PosixStructStat
 * JD-Core Version:    0.5.4
 */