/*     */ package intradoc.io;
/*     */ 
/*     */ import intradoc.data.DataException;
/*     */ import java.util.Iterator;
/*     */ 
/*     */ public class MemoryMappableItem extends MemoryMappable
/*     */ {
/*     */   public static final byte EMPTY_RECORD = 0;
/*     */   public static final byte VALID_RECORD = 1;
/*     */   public static final byte DELETED_RECORD = 2;
/*     */   public static final byte NEW_RECORD = 4;
/*     */   public static final byte UPDATED_RECORD = 8;
/*     */   public byte m_status;
/*     */ 
/*     */   public MemoryMappableItem(MemoryMappedFile file)
/*     */   {
/*  40 */     super(file);
/*  41 */     this.m_status = 4;
/*     */   }
/*     */ 
/*     */   public MemoryMappableItem(MemoryMappableItem inputRecord)
/*     */   {
/*  51 */     super(inputRecord);
/*  52 */     this.m_status = 4;
/*     */   }
/*     */ 
/*     */   public MemoryMappable createInstance()
/*     */   {
/*  61 */     return new MemoryMappableItem(this);
/*     */   }
/*     */ 
/*     */   public void clear()
/*     */   {
/*  70 */     this.m_status = 0;
/*     */   }
/*     */ 
/*     */   public long read(long position)
/*     */     throws DataException
/*     */   {
/*  80 */     this.m_id = position;
/*  81 */     long originalPosition = position;
/*  82 */     this.m_status = this.m_file.getByteAt(position);
/*  83 */     position += 1L;
/*  84 */     return position - originalPosition;
/*     */   }
/*     */ 
/*     */   public long write(long position)
/*     */     throws DataException
/*     */   {
/*  94 */     long originalPosition = position;
/*  95 */     if ((isNew()) || (isUpdated()))
/*     */     {
/*  97 */       setValid();
/*     */     }
/*  99 */     setLocation(position);
/* 100 */     this.m_file.setByteAt(position, this.m_status);
/* 101 */     position += 1L;
/* 102 */     return position - originalPosition;
/*     */   }
/*     */ 
/*     */   public long getSize()
/*     */   {
/* 108 */     return 1L;
/*     */   }
/*     */ 
/*     */   public long getMaxSize()
/*     */   {
/* 114 */     return 1L;
/*     */   }
/*     */ 
/*     */   public byte getStatus()
/*     */   {
/* 124 */     return this.m_status;
/*     */   }
/*     */ 
/*     */   public void setStatus(byte status)
/*     */   {
/* 129 */     this.m_status = status;
/*     */   }
/*     */ 
/*     */   public void setNew()
/*     */   {
/* 134 */     setStatus(4);
/*     */   }
/*     */ 
/*     */   public void setValid()
/*     */   {
/* 139 */     setStatus(1);
/*     */   }
/*     */ 
/*     */   public void setUpdated()
/*     */   {
/* 144 */     setStatus(8);
/*     */   }
/*     */ 
/*     */   public void setEmpty()
/*     */   {
/* 149 */     setStatus(0);
/*     */   }
/*     */ 
/*     */   public void setDeleted()
/*     */   {
/* 154 */     setStatus(2);
/*     */   }
/*     */ 
/*     */   public boolean isEmpty()
/*     */   {
/* 159 */     return this.m_status == 0;
/*     */   }
/*     */ 
/*     */   public boolean isValid()
/*     */   {
/* 164 */     return this.m_status == 1;
/*     */   }
/*     */ 
/*     */   public boolean isDeleted()
/*     */   {
/* 169 */     return this.m_status == 2;
/*     */   }
/*     */ 
/*     */   public boolean isNew()
/*     */   {
/* 174 */     return this.m_status == 4;
/*     */   }
/*     */ 
/*     */   public boolean isUpdated()
/*     */   {
/* 179 */     return this.m_status == 8;
/*     */   }
/*     */ 
/*     */   public Iterator iterator()
/*     */   {
/* 188 */     return new NullIterator();
/*     */   }
/*     */ 
/*     */   public MemoryMappable clone()
/*     */     throws CloneNotSupportedException
/*     */   {
/* 212 */     MemoryMappableItem cloned = new MemoryMappableItem(this);
/* 213 */     return cloned;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 222 */     return "Status : " + this.m_status;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 227 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98123 $";
/*     */   }
/*     */ 
/*     */   public class NullIterator
/*     */     implements Iterator
/*     */   {
/*     */     public NullIterator()
/*     */     {
/*     */     }
/*     */ 
/*     */     public Object next()
/*     */     {
/* 195 */       return null;
/*     */     }
/*     */ 
/*     */     public boolean hasNext()
/*     */     {
/* 200 */       return false;
/*     */     }
/*     */ 
/*     */     public void remove()
/*     */     {
/* 205 */       throw new UnsupportedOperationException();
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.MemoryMappableItem
 * JD-Core Version:    0.5.4
 */