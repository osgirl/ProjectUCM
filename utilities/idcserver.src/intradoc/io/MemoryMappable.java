/*     */ package intradoc.io;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.data.DataException;
/*     */ 
/*     */ public abstract class MemoryMappable
/*     */   implements Cloneable
/*     */ {
/*     */   public long m_id;
/*     */   public MemoryMappedFile m_file;
/*     */ 
/*     */   public MemoryMappable(MemoryMappedFile file)
/*     */   {
/*  29 */     this.m_file = file;
/*     */   }
/*     */ 
/*     */   public MemoryMappable(MemoryMappable input)
/*     */   {
/*  34 */     this.m_id = input.m_id;
/*  35 */     this.m_file = input.m_file;
/*     */   }
/*     */ 
/*     */   public abstract MemoryMappable createInstance();
/*     */ 
/*     */   public abstract void clear();
/*     */ 
/*     */   public abstract long read(long paramLong)
/*     */     throws DataException;
/*     */ 
/*     */   public long write()
/*     */     throws DataException
/*     */   {
/*  74 */     return write(this.m_id);
/*     */   }
/*     */ 
/*     */   public void setLocation(long location)
/*     */   {
/*  79 */     this.m_id = location;
/*     */   }
/*     */ 
/*     */   public abstract long write(long paramLong)
/*     */     throws DataException;
/*     */ 
/*     */   public abstract long getSize();
/*     */ 
/*     */   public abstract long getMaxSize();
/*     */ 
/*     */   public MemoryMappable clone()
/*     */     throws CloneNotSupportedException
/*     */   {
/* 123 */     return (MemoryMappable)super.clone();
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 132 */     IdcStringBuilder mappableBuilder = new IdcStringBuilder();
/* 133 */     mappableBuilder.append("ID : " + this.m_id);
/* 134 */     mappableBuilder.append("File : " + this.m_file.getAbsolutePath());
/* 135 */     return mappableBuilder.toString();
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.MemoryMappable
 * JD-Core Version:    0.5.4
 */