/*     */ package intradoc.io;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.data.DataException;
/*     */ 
/*     */ public class MemoryMappableRecord<T extends MemoryMappable> extends MemoryMappableItem
/*     */   implements Iterable
/*     */ {
/*     */   public T m_mappable;
/*  27 */   public long m_next = 0L;
/*     */ 
/*     */   public MemoryMappableRecord(T mappable, MemoryMappedFile file)
/*     */   {
/*  41 */     super(file);
/*  42 */     this.m_mappable = mappable;
/*     */   }
/*     */ 
/*     */   public MemoryMappableRecord(MemoryMappableRecord inputRecord)
/*     */     throws CloneNotSupportedException
/*     */   {
/*  53 */     super(inputRecord);
/*  54 */     this.m_mappable = inputRecord.m_mappable.clone();
/*  55 */     this.m_next = inputRecord.m_next;
/*     */   }
/*     */ 
/*     */   public MemoryMappable createInstance()
/*     */   {
/*  61 */     return new MemoryMappableRecord(this.m_mappable.createInstance(), this.m_file);
/*     */   }
/*     */ 
/*     */   public void clear()
/*     */   {
/*  67 */     super.clear();
/*  68 */     this.m_mappable.clear();
/*  69 */     this.m_next = 0L;
/*     */   }
/*     */ 
/*     */   public long read(long position)
/*     */     throws DataException
/*     */   {
/*  79 */     Report.trace(null, "Reading memory mapped record from position " + position + " file : " + this.m_file.getAbsolutePath(), null);
/*     */ 
/*  81 */     long originalPosition = position;
/*  82 */     long bytes = super.read(position);
/*  83 */     position += bytes;
/*  84 */     if (!super.isEmpty())
/*     */     {
/*  86 */       bytes = this.m_mappable.read(position);
/*  87 */       position += bytes;
/*  88 */       this.m_next = this.m_file.getLongAt(position);
/*  89 */       position += 8L;
/*     */     }
/*     */     else
/*     */     {
/*  95 */       this.m_mappable.m_id = position;
/*  96 */       return bytes + getMaxSize();
/*     */     }
/*  98 */     Report.trace(null, "Read memory mapped record bytes :" + (position - originalPosition), null);
/*  99 */     return position - originalPosition;
/*     */   }
/*     */ 
/*     */   public long write(long position)
/*     */     throws DataException
/*     */   {
/* 108 */     long originalPosition = position;
/* 109 */     long bytes = super.write(position);
/* 110 */     position += bytes;
/* 111 */     bytes = this.m_mappable.write(position);
/* 112 */     position += bytes;
/* 113 */     this.m_file.setLongAt(position, this.m_next);
/* 114 */     position += 8L;
/* 115 */     return position - originalPosition;
/*     */   }
/*     */ 
/*     */   public long getSize()
/*     */   {
/* 121 */     return super.getSize() + this.m_mappable.getSize() + 8L;
/*     */   }
/*     */ 
/*     */   public long getMaxSize()
/*     */   {
/* 127 */     return super.getMaxSize() + this.m_mappable.getMaxSize() + 8L;
/*     */   }
/*     */ 
/*     */   public long getNext()
/*     */   {
/* 132 */     return this.m_next;
/*     */   }
/*     */ 
/*     */   public void setNext(long next)
/*     */   {
/* 137 */     this.m_next = next;
/*     */   }
/*     */ 
/*     */   public MemoryMappable clone()
/*     */     throws CloneNotSupportedException
/*     */   {
/* 143 */     MemoryMappableRecord cloned = new MemoryMappableRecord(this);
/* 144 */     return cloned;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 150 */     IdcStringBuilder recordBuilder = new IdcStringBuilder();
/* 151 */     recordBuilder.append(this.m_mappable.toString());
/* 152 */     recordBuilder.append("Next : " + this.m_next);
/* 153 */     return recordBuilder.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 158 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98770 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.MemoryMappableRecord
 * JD-Core Version:    0.5.4
 */