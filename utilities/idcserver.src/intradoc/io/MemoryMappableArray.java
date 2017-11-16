/*     */ package intradoc.io;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.data.DataException;
/*     */ import java.io.File;
/*     */ import java.util.Arrays;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ 
/*     */ public class MemoryMappableArray<T extends MemoryMappableItem> extends MemoryMappable
/*     */   implements Iterable
/*     */ {
/*     */   protected int m_length;
/*     */   protected MemoryMappableItem[] m_items;
/*     */   protected T m_prototype;
/*     */ 
/*     */   public MemoryMappableArray(T prototype, MemoryMappedFile file)
/*     */   {
/*  46 */     super(file);
/*  47 */     init(prototype, file, 100);
/*     */   }
/*     */ 
/*     */   public MemoryMappableArray(T prototype, MemoryMappedFile file, int length)
/*     */   {
/*  63 */     super(file);
/*  64 */     init(prototype, file, length);
/*     */   }
/*     */ 
/*     */   public void init(T prototype, MemoryMappedFile file, int length)
/*     */   {
/*  81 */     this.m_prototype = prototype;
/*  82 */     this.m_length = length;
/*  83 */     this.m_items = new MemoryMappableItem[this.m_length];
/*     */   }
/*     */ 
/*     */   public MemoryMappable createInstance()
/*     */   {
/*  93 */     return new MemoryMappableArray((MemoryMappableItem)this.m_prototype.createInstance(), this.m_file, this.m_length);
/*     */   }
/*     */ 
/*     */   public void clear()
/*     */   {
/* 103 */     this.m_items = new MemoryMappableItem[this.m_length];
/*     */   }
/*     */ 
/*     */   public long read(long position)
/*     */     throws DataException
/*     */   {
/* 115 */     Report.trace(null, "Start - Reading Array position : " + position + " file : " + this.m_file.getAbsolutePath(), null);
/* 116 */     this.m_id = position;
/* 117 */     this.m_items = new MemoryMappableItem[this.m_length];
/* 118 */     MemoryMappableItem item = null;
/* 119 */     long OriginalPosition = position;
/* 120 */     int recNo = 0;
/* 121 */     for (recNo = 0; recNo < this.m_length; ++recNo)
/*     */     {
/*     */       try
/*     */       {
/* 125 */         item = (MemoryMappableItem)this.m_prototype.createInstance();
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 129 */         String msg = LocaleUtils.encodeMessage("csFailedOperation", null, "Read", this.m_file.m_file.getAbsolutePath());
/*     */ 
/* 131 */         throw new DataException(e, msg, new Object[0]);
/*     */       }
/* 133 */       long bytes = item.read(position);
/* 134 */       if (item.isEmpty()) {
/*     */         break;
/*     */       }
/*     */ 
/* 138 */       position += bytes;
/* 139 */       this.m_items[recNo] = item;
/*     */     }
/* 141 */     if (recNo < this.m_length)
/*     */     {
/* 144 */       item = (MemoryMappableItem)this.m_prototype.createInstance();
/* 145 */       int numberOfRecordPending = this.m_length - recNo;
/* 146 */       position += numberOfRecordPending * item.getMaxSize();
/*     */     }
/* 148 */     Report.trace(null, "End - Read Array bytes :" + (position - OriginalPosition), null);
/* 149 */     return position - OriginalPosition;
/*     */   }
/*     */ 
/*     */   public long write(long position)
/*     */     throws DataException
/*     */   {
/* 161 */     Report.trace(null, "Start - Writing Array position : " + position + " file : " + this.m_file.getAbsolutePath(), null);
/* 162 */     setLocation(position);
/* 163 */     long OriginalPosition = position;
/* 164 */     for (int recNo = 0; recNo < this.m_items.length; ++recNo)
/*     */     {
/* 166 */       MemoryMappableItem item = this.m_items[recNo];
/* 167 */       long bytes = 0L;
/* 168 */       if (item == null)
/*     */       {
/*     */         try
/*     */         {
/* 173 */           item = (MemoryMappableItem)this.m_prototype.createInstance();
/* 174 */           item.clear();
/* 175 */           item.setEmpty();
/* 176 */           bytes = item.getMaxSize();
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 180 */           String msg = LocaleUtils.encodeMessage("csFailedOperation", null, "Write", this.m_file.m_file.getAbsolutePath());
/*     */ 
/* 182 */           throw new DataException(e, msg, new Object[0]);
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 188 */         if (item.isEmpty())
/*     */         {
/* 190 */           item.setValid();
/*     */         }
/* 192 */         bytes = item.getSize();
/*     */       }
/* 194 */       item.setLocation(position);
/* 195 */       item.write(position);
/* 196 */       position += bytes;
/*     */     }
/* 198 */     Report.trace(null, "End - Writing Array position : " + position + " file : " + this.m_file.getAbsolutePath(), null);
/* 199 */     return position - OriginalPosition;
/*     */   }
/*     */ 
/*     */   public long getMaxSize()
/*     */   {
/* 205 */     return this.m_prototype.getMaxSize() * this.m_length;
/*     */   }
/*     */ 
/*     */   public long getSize()
/*     */   {
/* 211 */     long size = 0L;
/* 212 */     for (int recNo = 0; recNo < this.m_items.length; ++recNo)
/*     */     {
/* 214 */       MemoryMappableItem item = this.m_items[recNo];
/* 215 */       size += item.getSize();
/*     */     }
/* 217 */     return size;
/*     */   }
/*     */ 
/*     */   public void set(int index, T item)
/*     */   {
/* 222 */     item.setNew();
/* 223 */     this.m_items[index] = item;
/*     */   }
/*     */ 
/*     */   public T get(int index)
/*     */   {
/* 228 */     return this.m_items[index];
/*     */   }
/*     */ 
/*     */   public T getLast()
/*     */   {
/* 233 */     if (this.m_items.length == 0)
/*     */     {
/* 235 */       return null;
/*     */     }
/* 237 */     return this.m_items[(this.m_items.length - 1)];
/*     */   }
/*     */ 
/*     */   public int getEmptySlot()
/*     */   {
/* 242 */     for (int recNo = 0; recNo < this.m_items.length; ++recNo)
/*     */     {
/* 244 */       MemoryMappableItem item = this.m_items[recNo];
/* 245 */       if ((item == null) || (!item.isValid()))
/*     */       {
/* 247 */         return recNo;
/*     */       }
/*     */     }
/* 250 */     return -1;
/*     */   }
/*     */ 
/*     */   public int getLength() {
/* 254 */     return this.m_length;
/*     */   }
/*     */ 
/*     */   public Iterator iterator() {
/* 258 */     return Arrays.asList(this.m_items).iterator();
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 267 */     IdcStringBuilder arrayBuilder = new IdcStringBuilder();
/* 268 */     arrayBuilder.append("Array at : " + this.m_id);
/* 269 */     for (int recNo = 0; recNo < this.m_items.length; ++recNo)
/*     */     {
/* 271 */       MemoryMappableItem item = this.m_items[recNo];
/* 272 */       arrayBuilder.append("\n");
/* 273 */       if (item == null)
/*     */         continue;
/* 275 */       arrayBuilder.append(item.toString());
/*     */     }
/*     */ 
/* 278 */     return arrayBuilder.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 283 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98123 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.MemoryMappableArray
 * JD-Core Version:    0.5.4
 */