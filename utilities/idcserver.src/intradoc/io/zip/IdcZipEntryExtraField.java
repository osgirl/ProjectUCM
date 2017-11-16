/*    */ package intradoc.io.zip;
/*    */ 
/*    */ public class IdcZipEntryExtraField
/*    */ {
/*    */   public static final short EF_PKSZ64 = 1;
/*    */   public static final short EF_PKLANGENC = 8;
/*    */   public static final short EF_IZUCOMM = 25461;
/*    */   public static final short EF_IZUPATH = 28789;
/*    */   public boolean m_isReadOnly;
/*    */   public boolean m_localOnly;
/*    */   public short m_id;
/*    */   public int m_length;
/*    */   public byte[] m_data;
/*    */ 
/*    */   public IdcZipEntryExtraField(short id)
/*    */   {
/* 67 */     this.m_id = id;
/*    */   }
/*    */ 
/*    */   public IdcZipEntryExtraField(short id, byte[] data)
/*    */   {
/* 72 */     this.m_id = id;
/* 73 */     this.m_length = data.length;
/* 74 */     this.m_data = new byte[this.m_length];
/* 75 */     System.arraycopy(data, 0, this.m_data, 0, this.m_length);
/*    */   }
/*    */ 
/*    */   public IdcZipEntryExtraField(short id, byte[] data, int length)
/*    */   {
/* 80 */     this.m_id = id;
/* 81 */     this.m_length = length;
/* 82 */     this.m_data = new byte[this.m_length];
/* 83 */     System.arraycopy(data, 0, this.m_data, 0, this.m_length);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 89 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66344 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.zip.IdcZipEntryExtraField
 * JD-Core Version:    0.5.4
 */