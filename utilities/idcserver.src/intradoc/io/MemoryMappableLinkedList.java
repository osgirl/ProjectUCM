/*     */ package intradoc.io;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.data.DataException;
/*     */ import java.io.File;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ 
/*     */ public class MemoryMappableLinkedList<T extends MemoryMappableRecord> extends MemoryMappable
/*     */   implements Iterable
/*     */ {
/*     */   protected List<T> m_records;
/*     */   protected T m_prototype;
/*     */ 
/*     */   public MemoryMappableLinkedList(T prototype, MemoryMappedFile file)
/*     */   {
/*  46 */     super(file);
/*  47 */     this.m_prototype = prototype;
/*  48 */     this.m_records = new ArrayList();
/*     */   }
/*     */ 
/*     */   public MemoryMappable createInstance()
/*     */   {
/*  58 */     return new MemoryMappableLinkedList((MemoryMappableRecord)this.m_prototype.createInstance(), this.m_file);
/*     */   }
/*     */ 
/*     */   public void clear()
/*     */   {
/*  68 */     this.m_records = new ArrayList();
/*     */   }
/*     */ 
/*     */   public long read(long position)
/*     */     throws DataException
/*     */   {
/*  81 */     Report.trace(null, "Start - Read linked list position : " + position + " file : " + this.m_file.getAbsolutePath(), null);
/*     */ 
/*  83 */     long OriginalPosition = position;
/*  84 */     this.m_id = position;
/*  85 */     MemoryMappableRecord record = null;
/*     */     try
/*     */     {
/*  88 */       record = (MemoryMappableRecord)this.m_prototype.createInstance();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  92 */       String msg = LocaleUtils.encodeMessage("csFailedOperation", null, "Read", this.m_file.m_file.getAbsolutePath());
/*  93 */       throw new DataException(e, msg, new Object[0]);
/*     */     }
/*  95 */     long bytes = record.read(position);
/*  96 */     position += bytes;
/*  97 */     this.m_records.add(record);
/*  98 */     Report.trace(null, "End - Read linked list bytes :" + (position - OriginalPosition), null);
/*  99 */     return position - OriginalPosition;
/*     */   }
/*     */ 
/*     */   public long write(long position)
/*     */     throws DataException
/*     */   {
/* 112 */     Report.trace(null, "Start - write linked list position :" + position + " file :" + this.m_file.getAbsolutePath(), null);
/*     */ 
/* 114 */     if ((this.m_records == null) || (this.m_records.size() == 0) || (this.m_records.get(0) == null))
/*     */     {
/* 116 */       return 0L;
/*     */     }
/* 118 */     setLocation(position);
/* 119 */     long totalBytes = 0L;
/* 120 */     long recordBytes = 0L;
/*     */ 
/* 122 */     MemoryMappableRecord record = (MemoryMappableRecord)this.m_records.get(0);
/* 123 */     if ((record != null) && (isDirty(record, position)))
/*     */     {
/* 125 */       recordBytes = record.write(position);
/* 126 */       totalBytes += recordBytes;
/*     */     }
/*     */ 
/* 129 */     for (int recNo = 1; recNo < this.m_records.size(); ++recNo)
/*     */     {
/* 131 */       record = (MemoryMappableRecord)this.m_records.get(recNo);
/* 132 */       if (!isDirty(record, record.m_id))
/*     */         continue;
/* 134 */       recordBytes = record.write();
/* 135 */       totalBytes += recordBytes;
/*     */     }
/*     */ 
/* 138 */     Report.trace(null, "End - write linked list bytes :" + totalBytes, null);
/* 139 */     return totalBytes;
/*     */   }
/*     */ 
/*     */   public long write(int index)
/*     */     throws DataException
/*     */   {
/* 152 */     MemoryMappableRecord record = (MemoryMappableRecord)this.m_records.get(index);
/* 153 */     long bytes = record.write();
/* 154 */     return bytes;
/*     */   }
/*     */ 
/*     */   public long writeLast() throws DataException
/*     */   {
/* 159 */     return write(this.m_records.size() - 1);
/*     */   }
/*     */ 
/*     */   public long getSize()
/*     */   {
/* 165 */     long size = 0L;
/* 166 */     for (int recNo = 0; recNo < this.m_records.size(); ++recNo)
/*     */     {
/* 168 */       MemoryMappableRecord record = (MemoryMappableRecord)this.m_records.get(recNo);
/* 169 */       size += record.getSize();
/*     */     }
/* 171 */     return size;
/*     */   }
/*     */ 
/*     */   public long getMaxSize()
/*     */   {
/* 177 */     return this.m_prototype.getMaxSize() * this.m_records.size();
/*     */   }
/*     */ 
/*     */   public void add(T record)
/*     */   {
/* 182 */     if (!this.m_records.isEmpty())
/*     */     {
/* 184 */       MemoryMappableRecord lastRecord = (MemoryMappableRecord)this.m_records.get(this.m_records.size() - 1);
/* 185 */       lastRecord.m_next = record.m_id;
/* 186 */       lastRecord.setUpdated();
/*     */     }
/* 188 */     record.setNew();
/* 189 */     this.m_records.add(record);
/*     */   }
/*     */ 
/*     */   public boolean isDirty(T record, long position)
/*     */   {
/* 203 */     boolean isRequired = false;
/* 204 */     if ((record.isNew()) || (record.isUpdated()) || (record.m_id != position))
/*     */     {
/* 206 */       isRequired = true;
/*     */     }
/* 208 */     return isRequired;
/*     */   }
/*     */ 
/*     */   public Iterator iterator()
/*     */   {
/* 213 */     return new ListIterator();
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 222 */     IdcStringBuilder linkedListBuilder = new IdcStringBuilder();
/* 223 */     linkedListBuilder.append("Linked List at : " + this.m_id);
/* 224 */     for (int recNo = 0; recNo < this.m_records.size(); ++recNo)
/*     */     {
/* 226 */       MemoryMappableItem item = (MemoryMappableItem)this.m_records.get(recNo);
/* 227 */       linkedListBuilder.append("\n");
/* 228 */       linkedListBuilder.append(item.toString());
/*     */     }
/* 230 */     return linkedListBuilder.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 293 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98770 $";
/*     */   }
/*     */ 
/*     */   class ListIterator
/*     */     implements Iterator
/*     */   {
/*     */     protected int m_iteratorIndex;
/*     */ 
/*     */     ListIterator()
/*     */     {
/* 239 */       this.m_iteratorIndex = -1;
/*     */     }
/*     */ 
/*     */     public boolean hasNext()
/*     */     {
/* 246 */       if ((MemoryMappableLinkedList.this.m_records == null) || (MemoryMappableLinkedList.this.m_records.size() == 0))
/*     */       {
/* 248 */         return false;
/*     */       }
/*     */ 
/* 251 */       if ((this.m_iteratorIndex == -1) && (MemoryMappableLinkedList.this.m_records.size() > 0))
/*     */       {
/* 253 */         return true;
/*     */       }
/* 255 */       MemoryMappableRecord currentRecord = (MemoryMappableRecord)MemoryMappableLinkedList.this.m_records.get(this.m_iteratorIndex);
/* 256 */       return (currentRecord != null) && (currentRecord.m_next != 0L);
/*     */     }
/*     */ 
/*     */     public Object next() {
/* 260 */       this.m_iteratorIndex += 1;
/* 261 */       MemoryMappableRecord nextRecord = null;
/* 262 */       if (this.m_iteratorIndex < MemoryMappableLinkedList.this.m_records.size())
/*     */       {
/* 265 */         return MemoryMappableLinkedList.this.m_records.get(this.m_iteratorIndex);
/*     */       }
/*     */ 
/* 268 */       MemoryMappableRecord lastRecord = (MemoryMappableRecord)MemoryMappableLinkedList.this.m_records.get(MemoryMappableLinkedList.this.m_records.size() - 1);
/* 269 */       if ((lastRecord != null) && (lastRecord.m_next != 0L))
/*     */       {
/*     */         try
/*     */         {
/* 273 */           MemoryMappableLinkedList.this.read(lastRecord.m_next);
/*     */         }
/*     */         catch (DataException e)
/*     */         {
/* 277 */           String msg = LocaleUtils.encodeMessage("csFailedOperation", null, "Read", MemoryMappableLinkedList.this.m_file.m_file.getAbsolutePath());
/*     */ 
/* 279 */           Report.error(null, msg, e);
/*     */         }
/* 281 */         nextRecord = (MemoryMappableRecord)MemoryMappableLinkedList.this.m_records.get(MemoryMappableLinkedList.this.m_records.size() - 1);
/*     */       }
/* 283 */       return nextRecord;
/*     */     }
/*     */ 
/*     */     public void remove() {
/* 287 */       throw new UnsupportedOperationException();
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.MemoryMappableLinkedList
 * JD-Core Version:    0.5.4
 */