/*      */ package intradoc.io;
/*      */ 
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.lang.SoftHashMap;
/*      */ import java.io.IOException;
/*      */ import java.util.HashMap;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Set;
/*      */ import java.util.WeakHashMap;
/*      */ 
/*      */ public class MemoryMappableHashMap<T extends MemoryMappable> extends MemoryMappable
/*      */ {
/*   60 */   protected int m_maxKeySize = 2000;
/*   61 */   protected int m_primaryIndexSize = 100;
/*   62 */   protected int m_secondaryIndexSizePerPrimary = 200;
/*   63 */   protected int m_collisionBucketSize = 10;
/*      */   protected MemoryMappedFile m_hashMetaFile;
/*      */   protected MemoryMappedFile m_primaryIndexFile;
/*      */   protected MemoryMappedFile m_secondaryIndexFile;
/*      */   protected MemoryMappedFile m_recordsFile;
/*      */   protected MemoryMappableHashMap<T>.HashMetadata m_primaryIndexMetadata;
/*      */   protected MemoryMappableHashMap<T>.HashMetadata m_secondaryIndexMetadata;
/*      */   protected MemoryMappableHashMap<T>.HashMetadata m_dataMetadata;
/*      */   protected MemoryMappableHashMap<T>.HashEntry m_prototypePrimaryHashEntry;
/*      */   protected MemoryMappableHashMap<T>.HashEntry m_prototypeSecondaryHashEntry;
/*      */   protected T m_prototypeDataMappable;
/*      */   protected Map<String, T> m_records;
/*      */   protected Map<Long, Long> m_primaryIndexMap;
/*      */   protected MemoryMappableArray<MemoryMappableHashMap<T>.HashEntry> m_primaryIndex;
/*      */ 
/*      */   public MemoryMappableHashMap(T dataPrototype, MemoryMappedFile hashMetaFile)
/*      */     throws DataException
/*      */   {
/*  106 */     super(hashMetaFile);
/*  107 */     init(dataPrototype, hashMetaFile);
/*      */   }
/*      */ 
/*      */   public MemoryMappableHashMap(T dataPrototype, MemoryMappedFile hashMetaFile, int primaryIndexSize, int secondaryIndexSizePerPrimary, int collisionBucketSize, int maxKeySize)
/*      */     throws DataException
/*      */   {
/*  126 */     super(hashMetaFile);
/*  127 */     init(dataPrototype, hashMetaFile, primaryIndexSize, secondaryIndexSizePerPrimary, collisionBucketSize, maxKeySize);
/*      */   }
/*      */ 
/*      */   public void init(T dataPrototype, MemoryMappedFile hashMetaFile, int primaryIndexSize, int secondaryIndexSizePerPrimary, int collisionBucketSize, int maxKeySize)
/*      */     throws DataException
/*      */   {
/*  133 */     setPrimaryIndexSize(primaryIndexSize);
/*  134 */     setSecondaryIndexSizePerPrimary(secondaryIndexSizePerPrimary);
/*  135 */     setCollisionBucketSize(collisionBucketSize);
/*  136 */     setMaxKeySize(maxKeySize);
/*  137 */     init(dataPrototype, hashMetaFile);
/*      */   }
/*      */ 
/*      */   public void init(T dataPrototype, MemoryMappedFile hashMetaFile)
/*      */     throws DataException
/*      */   {
/*  154 */     this.m_hashMetaFile = hashMetaFile;
/*  155 */     long metaPosition = 0L;
/*  156 */     String parentPath = hashMetaFile.getParent();
/*  157 */     this.m_primaryIndexMetadata = new HashMetadata(hashMetaFile);
/*  158 */     long bytes = this.m_primaryIndexMetadata.read(metaPosition);
/*  159 */     if ((this.m_primaryIndexMetadata.m_filePath == null) || (this.m_primaryIndexMetadata.m_filePath.length() == 0))
/*      */     {
/*  162 */       createHashMetaFile(hashMetaFile);
/*      */     }
/*      */     else
/*      */     {
/*  167 */       metaPosition += bytes;
/*  168 */       this.m_secondaryIndexMetadata = new HashMetadata(hashMetaFile);
/*  169 */       bytes = this.m_secondaryIndexMetadata.read(metaPosition);
/*  170 */       metaPosition += bytes;
/*  171 */       this.m_dataMetadata = new HashMetadata(hashMetaFile);
/*  172 */       bytes = this.m_dataMetadata.read(metaPosition);
/*  173 */       metaPosition += bytes;
/*      */     }
/*      */ 
/*  179 */     this.m_primaryIndexFile = new MemoryMappedFile(parentPath + "/" + this.m_primaryIndexMetadata.m_filePath, 1048576);
/*      */ 
/*  181 */     this.m_secondaryIndexFile = new MemoryMappedFile(parentPath + "/" + this.m_secondaryIndexMetadata.m_filePath, 1048576);
/*      */ 
/*  183 */     this.m_recordsFile = new MemoryMappedFile(parentPath + "/" + this.m_dataMetadata.m_filePath, 1048576);
/*      */ 
/*  189 */     this.m_prototypeDataMappable = dataPrototype;
/*  190 */     this.m_prototypeDataMappable.m_file = this.m_recordsFile;
/*  191 */     this.m_prototypePrimaryHashEntry = new HashEntry(this.m_primaryIndexFile);
/*  192 */     this.m_prototypeSecondaryHashEntry = new HashEntry(this.m_secondaryIndexFile);
/*  193 */     this.m_primaryIndex = new MemoryMappableArray(this.m_prototypePrimaryHashEntry, this.m_primaryIndexFile, this.m_primaryIndexSize);
/*      */ 
/*  198 */     this.m_primaryIndex.read(0L);
/*  199 */     this.m_primaryIndexMap = new HashMap();
/*  200 */     Iterator pIterator = this.m_primaryIndex.iterator();
/*  201 */     while (pIterator.hasNext())
/*      */     {
/*  203 */       HashEntry primaryHashEntry = (HashEntry)pIterator.next();
/*  204 */       if (primaryHashEntry != null)
/*      */       {
/*  206 */         this.m_primaryIndexMap.put(Long.valueOf(primaryHashEntry.m_hashCode), Long.valueOf(primaryHashEntry.m_offset));
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  212 */     this.m_records = new SoftHashMap();
/*      */   }
/*      */ 
/*      */   private void setPrimaryIndexSize(int primaryIndexSize)
/*      */   {
/*  221 */     this.m_primaryIndexSize = primaryIndexSize;
/*      */   }
/*      */ 
/*      */   private void setSecondaryIndexSizePerPrimary(int secondaryIndexSizePerPrimary) {
/*  225 */     this.m_secondaryIndexSizePerPrimary = secondaryIndexSizePerPrimary;
/*      */   }
/*      */ 
/*      */   private void setCollisionBucketSize(int collisionBucketSize) {
/*  229 */     this.m_collisionBucketSize = collisionBucketSize;
/*      */   }
/*      */ 
/*      */   private void setMaxKeySize(int keySize) {
/*  233 */     this.m_maxKeySize = keySize;
/*      */   }
/*      */ 
/*      */   public void createHashMetaFile(MemoryMappedFile hashMetaFile)
/*      */     throws DataException
/*      */   {
/*  248 */     this.m_primaryIndexMetadata = new HashMetadata(hashMetaFile);
/*  249 */     this.m_primaryIndexMetadata.m_id = 0L;
/*  250 */     this.m_primaryIndexMetadata.m_filePath = (hashMetaFile.getFileName() + "$pIndex");
/*  251 */     this.m_primaryIndexMetadata.m_tail = 0L;
/*      */ 
/*  253 */     this.m_secondaryIndexMetadata = new HashMetadata(hashMetaFile);
/*  254 */     this.m_secondaryIndexMetadata.m_id = (this.m_primaryIndexMetadata.m_id + this.m_primaryIndexMetadata.getSize());
/*  255 */     this.m_secondaryIndexMetadata.m_filePath = (hashMetaFile.getFileName() + "$sIndex");
/*  256 */     this.m_secondaryIndexMetadata.m_tail = 0L;
/*      */ 
/*  258 */     this.m_dataMetadata = new HashMetadata(hashMetaFile);
/*  259 */     this.m_dataMetadata.m_id = (this.m_secondaryIndexMetadata.m_id + this.m_secondaryIndexMetadata.getSize());
/*  260 */     this.m_dataMetadata.m_filePath = (hashMetaFile.getFileName() + "$data");
/*  261 */     this.m_dataMetadata.m_tail = 0L;
/*      */ 
/*  263 */     long position = 0L;
/*  264 */     long bytes = this.m_primaryIndexMetadata.write(0L);
/*  265 */     position += bytes;
/*  266 */     bytes = this.m_secondaryIndexMetadata.write(position);
/*  267 */     position += bytes;
/*  268 */     bytes = this.m_dataMetadata.write(position);
/*      */     try
/*      */     {
/*  271 */       hashMetaFile.flush(0L, position);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  275 */       throw new DataException("Unable to flush", e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public MemoryMappable createInstance()
/*      */   {
/*      */     try
/*      */     {
/*  288 */       return new MemoryMappableHashMap(this.m_prototypeDataMappable.createInstance(), this.m_hashMetaFile, this.m_primaryIndexSize, this.m_secondaryIndexSizePerPrimary, this.m_collisionBucketSize, this.m_maxKeySize);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*      */     }
/*  293 */     return null;
/*      */   }
/*      */ 
/*      */   public void clear()
/*      */   {
/*  303 */     this.m_records = new WeakHashMap();
/*      */   }
/*      */ 
/*      */   public long read(long position)
/*      */     throws DataException
/*      */   {
/*  313 */     return this.m_primaryIndex.read(position);
/*      */   }
/*      */ 
/*      */   public long write(long position)
/*      */     throws DataException
/*      */   {
/*  324 */     long bytes = 0L;
/*  325 */     bytes = this.m_primaryIndexMetadata.write();
/*  326 */     bytes = this.m_secondaryIndexMetadata.write();
/*  327 */     bytes = this.m_dataMetadata.write();
/*  328 */     return bytes;
/*      */   }
/*      */ 
/*      */   public long getSize()
/*      */   {
/*  334 */     throw new UnsupportedOperationException();
/*      */   }
/*      */ 
/*      */   public long getMaxSize()
/*      */   {
/*  340 */     throw new UnsupportedOperationException();
/*      */   }
/*      */ 
/*      */   public void cacheAll()
/*      */     throws DataException
/*      */   {
/*  350 */     Iterator iterator = this.m_primaryIndexMap.keySet().iterator();
/*  351 */     while (iterator.hasNext())
/*      */     {
/*  353 */       Long primaryHashKey = (Long)iterator.next();
/*  354 */       Long secondaryBucketOffset = (Long)this.m_primaryIndexMap.get(primaryHashKey);
/*      */ 
/*  356 */       for (int entryNo = 0; entryNo < this.m_secondaryIndexSizePerPrimary; ++entryNo)
/*      */       {
/*  358 */         HashEntry secondaryHashEntry = getSecondaryHashEntry(secondaryBucketOffset.longValue(), entryNo);
/*  359 */         if ((secondaryHashEntry == null) || (secondaryHashEntry.isEmpty())) continue; if (secondaryHashEntry.isDeleted())
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/*  364 */         MemoryMappableArray dataRecordArray = new MemoryMappableArray(new DataRecord(this.m_prototypeDataMappable, this.m_recordsFile), this.m_recordsFile, this.m_collisionBucketSize);
/*      */ 
/*  366 */         MemoryMappableRecord dataRecordArrayAsRecord = new MemoryMappableRecord(dataRecordArray, this.m_recordsFile);
/*      */ 
/*  368 */         MemoryMappableLinkedList dataRecords = new MemoryMappableLinkedList(dataRecordArrayAsRecord, this.m_recordsFile);
/*      */ 
/*  370 */         dataRecords.read(secondaryHashEntry.m_offset);
/*  371 */         Iterator dataListIterator = dataRecords.iterator();
/*  372 */         while (dataListIterator.hasNext())
/*      */         {
/*  374 */           MemoryMappableRecord record = (MemoryMappableRecord)dataListIterator.next();
/*  375 */           MemoryMappableArray dataBlock = (MemoryMappableArray)record.m_mappable;
/*  376 */           Iterator dataBlockIterator = dataBlock.iterator();
/*  377 */           while (dataBlockIterator.hasNext())
/*      */           {
/*  379 */             DataRecord dataRecord = (DataRecord)dataBlockIterator.next();
/*  380 */             if (dataRecord != null)
/*      */             {
/*  382 */               this.m_records.put(dataRecord.m_key, get(dataRecord));
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public T get(String key)
/*      */     throws DataException
/*      */   {
/*  405 */     Report.trace(null, "Start - Read HashMap key : " + key + " file : " + this.m_file.getAbsolutePath(), null);
/*  406 */     if (this.m_records.get(key) != null)
/*      */     {
/*  408 */       return (MemoryMappable)this.m_records.get(key);
/*      */     }
/*  410 */     long primaryHashKey = primaryHash(key);
/*  411 */     Long secondaryBucketOffset = (Long)this.m_primaryIndexMap.get(Long.valueOf(primaryHashKey));
/*  412 */     if (secondaryBucketOffset == null)
/*      */     {
/*  414 */       return null;
/*      */     }
/*  416 */     long secondaryHashKey = secondaryHash(key);
/*  417 */     HashEntry secondaryHashEntry = getSecondaryHashEntry(secondaryBucketOffset.longValue(), secondaryHashKey);
/*  418 */     if ((secondaryHashEntry == null) || (secondaryHashEntry.isEmpty()) || (secondaryHashEntry.isDeleted()))
/*      */     {
/*  420 */       return null;
/*      */     }
/*      */ 
/*  423 */     MemoryMappableArray dataRecordArray = new MemoryMappableArray(new DataRecord(this.m_prototypeDataMappable, this.m_recordsFile), this.m_recordsFile, this.m_collisionBucketSize);
/*      */ 
/*  425 */     MemoryMappableRecord dataRecordArrayAsRecord = new MemoryMappableRecord(dataRecordArray, this.m_recordsFile);
/*      */ 
/*  427 */     MemoryMappableLinkedList dataRecords = new MemoryMappableLinkedList(dataRecordArrayAsRecord, this.m_recordsFile);
/*      */ 
/*  430 */     DataRecord foundRecord = null;
/*  431 */     Report.trace(null, "Primary key :" + primaryHashKey + " Secondary key :" + secondaryHashKey + " Data offset :" + secondaryHashEntry.m_offset, null);
/*      */ 
/*  433 */     dataRecords.read(secondaryHashEntry.m_offset);
/*  434 */     Iterator iterator = dataRecords.iterator();
/*  435 */     while (iterator.hasNext())
/*      */     {
/*  437 */       MemoryMappableRecord record = (MemoryMappableRecord)iterator.next();
/*  438 */       MemoryMappableArray dataArray = (MemoryMappableArray)record.m_mappable;
/*  439 */       Report.trace(null, "Finding record in array - " + dataArray.toString(), null);
/*  440 */       foundRecord = find(dataArray, key);
/*  441 */       if (foundRecord != null) {
/*      */         break;
/*      */       }
/*      */     }
/*      */ 
/*  446 */     if (foundRecord != null)
/*      */     {
/*  448 */       return get(foundRecord);
/*      */     }
/*  450 */     Report.trace(null, "End - Read HashMap key : " + key + " file : " + this.m_file.getAbsolutePath(), null);
/*  451 */     return null;
/*      */   }
/*      */ 
/*      */   public T get(long id)
/*      */     throws DataException
/*      */   {
/*  463 */     MemoryMappable mappable = this.m_prototypeDataMappable.createInstance();
/*  464 */     mappable.read(id);
/*  465 */     return mappable;
/*      */   }
/*      */ 
/*      */   public long put(String key, T record)
/*      */     throws DataException
/*      */   {
/*  481 */     this.m_records.put(key, record);
/*  482 */     long id = write(key, record);
/*  483 */     return id;
/*      */   }
/*      */ 
/*      */   public long write(String key, T record)
/*      */     throws DataException
/*      */   {
/*  508 */     Report.trace(null, "Start - Write hashMap key :" + key + " hash map meta file : " + this.m_file.getAbsolutePath(), null);
/*      */ 
/*  510 */     boolean primaryRecordAdded = false;
/*  511 */     long primaryHashKey = primaryHash(key);
/*  512 */     Long secondaryBucketOffset = (Long)this.m_primaryIndexMap.get(Long.valueOf(primaryHashKey));
/*  513 */     if (secondaryBucketOffset == null)
/*      */     {
/*  515 */       HashEntry primaryHashEntry = addPrimaryHashEntry(primaryHashKey);
/*  516 */       secondaryBucketOffset = Long.valueOf(primaryHashEntry.m_offset);
/*  517 */       this.m_primaryIndexMap.put(Long.valueOf(primaryHashEntry.m_hashCode), Long.valueOf(primaryHashEntry.m_offset));
/*  518 */       primaryRecordAdded = true;
/*      */     }
/*      */ 
/*  521 */     long secondayHashKey = secondaryHash(key);
/*  522 */     HashEntry secondaryHashEntry = getSecondaryHashEntry(secondaryBucketOffset.longValue(), secondayHashKey);
/*  523 */     if (!secondaryHashEntry.isValid())
/*      */     {
/*  525 */       secondaryHashEntry = setSecondaryHashEntry(secondaryBucketOffset.longValue(), secondayHashKey);
/*      */     }
/*  527 */     Report.trace(null, "PrimaryHashKey : " + primaryHashKey + " secondayHashKey : " + secondayHashKey, null);
/*      */ 
/*  529 */     DataRecord dataRecord = new DataRecord(this.m_prototypeDataMappable.createInstance(), this.m_recordsFile);
/*  530 */     dataRecord.m_key = key;
/*  531 */     record.m_file = this.m_recordsFile;
/*  532 */     dataRecord.m_mappable = record;
/*  533 */     dataRecord.setValid();
/*  534 */     setDataRecord(secondaryHashEntry.m_offset, dataRecord);
/*      */ 
/*  536 */     if (primaryRecordAdded == true)
/*      */     {
/*  538 */       this.m_primaryIndex.write();
/*      */     }
/*  540 */     Report.trace(null, "End - Write hashMap key :" + key, null);
/*  541 */     return record.m_id;
/*      */   }
/*      */ 
/*      */   public MemoryMappableHashMap<T>.HashEntry addPrimaryHashEntry(long primaryHashKey)
/*      */     throws DataException
/*      */   {
/*  556 */     long secondaryBucketLocation = appendSecondaryBucket();
/*  557 */     HashEntry primaryEntry = (HashEntry)this.m_prototypePrimaryHashEntry.createInstance();
/*  558 */     primaryEntry.m_hashCode = primaryHashKey;
/*  559 */     primaryEntry.m_offset = secondaryBucketLocation;
/*  560 */     this.m_primaryIndex.set(this.m_primaryIndex.getEmptySlot(), primaryEntry);
/*  561 */     return primaryEntry;
/*      */   }
/*      */ 
/*      */   public long primaryHash(String str)
/*      */   {
/*  574 */     long hash = 5381L;
/*  575 */     for (int charNo = 0; charNo < str.length(); ++charNo)
/*      */     {
/*  577 */       hash = (hash << 5) + hash + str.charAt(charNo);
/*      */     }
/*  579 */     return hash % this.m_primaryIndexSize;
/*      */   }
/*      */ 
/*      */   public long appendSecondaryBucket()
/*      */     throws DataException
/*      */   {
/*  591 */     long secondaryBucketLocation = this.m_secondaryIndexMetadata.m_tail;
/*  592 */     MemoryMappableArray secondaryHashArray = new MemoryMappableArray(this.m_prototypeSecondaryHashEntry, this.m_secondaryIndexFile, this.m_secondaryIndexSizePerPrimary);
/*      */ 
/*  594 */     long bytes = secondaryHashArray.write(secondaryBucketLocation);
/*  595 */     this.m_secondaryIndexMetadata.m_tail += bytes;
/*  596 */     return secondaryBucketLocation;
/*      */   }
/*      */ 
/*      */   public MemoryMappableHashMap<T>.HashEntry setSecondaryHashEntry(long secondaryBucketOffset, long secondaryHashKey)
/*      */     throws DataException
/*      */   {
/*  616 */     HashEntry secondaryHashEntry = (HashEntry)this.m_prototypeSecondaryHashEntry.createInstance();
/*  617 */     long secondaryHashEntryOffset = secondaryHashKey * secondaryHashEntry.getSize();
/*  618 */     secondaryHashEntry.m_hashCode = secondaryHashKey;
/*  619 */     secondaryHashEntry.m_offset = appendDataBucket();
/*  620 */     secondaryHashEntry.write(secondaryBucketOffset + secondaryHashEntryOffset);
/*  621 */     return secondaryHashEntry;
/*      */   }
/*      */ 
/*      */   public MemoryMappableHashMap<T>.HashEntry getSecondaryHashEntry(long secondaryBucketOffset, long secondaryHashKey) throws DataException
/*      */   {
/*  626 */     HashEntry secondaryHashEntry = (HashEntry)this.m_prototypeSecondaryHashEntry.createInstance();
/*  627 */     long secondaryHashEntryOffset = secondaryHashKey * secondaryHashEntry.getSize();
/*  628 */     secondaryHashEntry.read(secondaryBucketOffset + secondaryHashEntryOffset);
/*  629 */     return secondaryHashEntry;
/*      */   }
/*      */ 
/*      */   public long appendDataBucket()
/*      */     throws DataException
/*      */   {
/*  641 */     long dataLocation = this.m_dataMetadata.m_tail;
/*  642 */     MemoryMappableArray dataRecordArray = new MemoryMappableArray(new DataRecord(this.m_prototypeDataMappable, this.m_recordsFile), this.m_recordsFile, this.m_collisionBucketSize);
/*      */ 
/*  644 */     MemoryMappableRecord dataRecordArrayAsRecord = new MemoryMappableRecord(dataRecordArray, this.m_recordsFile);
/*  645 */     long bytes = dataRecordArrayAsRecord.write(dataLocation);
/*  646 */     this.m_dataMetadata.m_tail += bytes;
/*  647 */     return dataLocation;
/*      */   }
/*      */ 
/*      */   public long setDataRecord(long dataBucketOffset, MemoryMappableHashMap<T>.DataRecord dataRecord)
/*      */     throws DataException
/*      */   {
/*  670 */     Report.trace(null, "Data Linked list offset : " + dataBucketOffset, null);
/*  671 */     MemoryMappableArray dataRecordArray = new MemoryMappableArray(new DataRecord(this.m_prototypeDataMappable, this.m_recordsFile), this.m_recordsFile, this.m_collisionBucketSize);
/*      */ 
/*  673 */     MemoryMappableRecord dataRecordArrayAsRecord = new MemoryMappableRecord(dataRecordArray, this.m_recordsFile);
/*  674 */     MemoryMappableLinkedList dataRecords = new MemoryMappableLinkedList(dataRecordArrayAsRecord, this.m_recordsFile);
/*  675 */     dataRecords.read(dataBucketOffset);
/*  676 */     boolean isSuccess = false;
/*  677 */     Iterator iterator = dataRecords.iterator();
/*  678 */     while (iterator.hasNext())
/*      */     {
/*  680 */       MemoryMappableRecord record = (MemoryMappableRecord)iterator.next();
/*  681 */       MemoryMappableArray dataArray = (MemoryMappableArray)record.m_mappable;
/*      */ 
/*  683 */       DataRecord existingRecord = find(dataArray, dataRecord.m_key);
/*  684 */       if (existingRecord == null)
/*      */       {
/*  687 */         int emptySlot = dataArray.getEmptySlot();
/*  688 */         if (emptySlot != -1)
/*      */         {
/*  690 */           dataArray.set(emptySlot, dataRecord);
/*  691 */           record.setUpdated();
/*  692 */           isSuccess = true;
/*  693 */           break;
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/*  699 */         existingRecord.m_mappable = dataRecord.m_mappable;
/*  700 */         record.setUpdated();
/*  701 */         isSuccess = true;
/*  702 */         break;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  707 */     if (!isSuccess)
/*      */     {
/*  709 */       Report.trace(null, "Buckets " + dataRecords.m_records.size() + " are fully filled. Adding another node in linked list.", null);
/*      */ 
/*  711 */       MemoryMappableRecord dataAppend = (MemoryMappableRecord)dataRecordArrayAsRecord.createInstance();
/*  712 */       dataAppend.setLocation(this.m_dataMetadata.m_tail);
/*  713 */       dataRecords.add(dataAppend);
/*  714 */       MemoryMappableArray dataArray = (MemoryMappableArray)dataAppend.m_mappable;
/*  715 */       dataArray.set(0, dataRecord);
/*      */ 
/*  719 */       Report.trace(null, "New linked list node added at : " + this.m_dataMetadata.m_tail, null);
/*  720 */       long bytes = dataAppend.write();
/*  721 */       this.m_dataMetadata.m_tail += bytes;
/*  722 */       isSuccess = true;
/*      */     }
/*  724 */     dataRecords.write();
/*  725 */     if (isSuccess == true)
/*      */     {
/*  727 */       return dataRecord.m_id;
/*      */     }
/*  729 */     return -1L;
/*      */   }
/*      */ 
/*      */   public long secondaryHash(String str)
/*      */   {
/*  742 */     long hash = 0L;
/*  743 */     for (int charNo = 0; charNo < str.length(); ++charNo)
/*      */     {
/*  745 */       hash = str.charAt(charNo) + (hash << 6) + (hash << 16) - hash;
/*      */     }
/*  747 */     return hash % this.m_secondaryIndexSizePerPrimary;
/*      */   }
/*      */ 
/*      */   private MemoryMappableHashMap<T>.DataRecord find(MemoryMappableArray<MemoryMappableHashMap<T>.DataRecord> dataRecordArray, String key)
/*      */   {
/*  761 */     Iterator iterator = dataRecordArray.iterator();
/*  762 */     while (iterator.hasNext())
/*      */     {
/*  764 */       DataRecord dataRecord = (DataRecord)iterator.next();
/*  765 */       if (dataRecord != null)
/*      */       {
/*  767 */         this.m_records.put(dataRecord.m_key, get(dataRecord));
/*  768 */         if (dataRecord.m_key.equals(key))
/*      */         {
/*  770 */           return dataRecord;
/*      */         }
/*      */       }
/*      */     }
/*  774 */     return null;
/*      */   }
/*      */ 
/*      */   private T get(MemoryMappableHashMap<T>.DataRecord dataRecord)
/*      */   {
/*  784 */     return dataRecord.m_mappable;
/*      */   }
/*      */ 
/*      */   public void printPrimaryHashMap() throws DataException
/*      */   {
/*  789 */     Report.trace(null, "Primary HashMap Entries :", null);
/*  790 */     long position = 0L;
/*      */     while (true)
/*      */     {
/*  793 */       HashEntry primaryHashEntry = (HashEntry)this.m_prototypePrimaryHashEntry.createInstance();
/*  794 */       long bytes = primaryHashEntry.read(position);
/*  795 */       Report.trace(null, "position : " + position + " -- " + primaryHashEntry.toString(), null);
/*  796 */       position += bytes;
/*  797 */       if (position > this.m_primaryIndexFile.length())
/*      */         return;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void printSecondaryHashMap()
/*      */     throws DataException
/*      */   {
/*  806 */     Report.trace(null, "Secondary HashMap Entries :", null);
/*  807 */     long position = 0L;
/*      */     while (true)
/*      */     {
/*  810 */       HashEntry secondaryHashEntry = (HashEntry)this.m_prototypeSecondaryHashEntry.createInstance();
/*  811 */       long bytes = secondaryHashEntry.read(position);
/*  812 */       Report.trace(null, "position : " + position + " -- " + secondaryHashEntry.toString(), null);
/*  813 */       position += bytes;
/*  814 */       if (position > this.m_secondaryIndexFile.length())
/*      */         return;
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1121 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98770 $";
/*      */   }
/*      */ 
/*      */   public class DataRecord extends MemoryMappableRecord
/*      */   {
/*      */     String m_key;
/*      */ 
/*      */     public DataRecord()
/*      */       throws CloneNotSupportedException
/*      */     {
/* 1029 */       super(inputInfo);
/* 1030 */       this.m_key = inputInfo.m_key;
/*      */     }
/*      */ 
/*      */     public DataRecord(MemoryMappedFile mappable)
/*      */     {
/* 1035 */       super(mappable, file);
/*      */     }
/*      */ 
/*      */     public MemoryMappable createInstance()
/*      */     {
/* 1041 */       return new DataRecord(MemoryMappableHashMap.this, MemoryMappableHashMap.this.m_prototypeDataMappable.createInstance(), this.m_file);
/*      */     }
/*      */ 
/*      */     public void clear()
/*      */     {
/* 1047 */       this.m_status = 0;
/* 1048 */       this.m_key = "";
/* 1049 */       this.m_mappable.clear();
/* 1050 */       this.m_next = 0L;
/*      */     }
/*      */ 
/*      */     public long read(long position)
/*      */       throws DataException
/*      */     {
/* 1059 */       long originalPosition = position;
/* 1060 */       long bytes = super.read(position);
/* 1061 */       position += bytes;
/* 1062 */       if (!super.isEmpty())
/*      */       {
/* 1064 */         int length = this.m_file.getIntAt(position);
/* 1065 */         position += 4L;
/* 1066 */         char[] key = new char[length];
/* 1067 */         this.m_file.getCharsAt(position, key, 0, length);
/* 1068 */         this.m_key = new String(key);
/* 1069 */         position += length * 2;
/*      */       }
/*      */       else
/*      */       {
/* 1075 */         return bytes + getMaxSize();
/*      */       }
/* 1077 */       return position - originalPosition;
/*      */     }
/*      */ 
/*      */     public long write(long position)
/*      */       throws DataException
/*      */     {
/* 1086 */       long originalPosition = position;
/* 1087 */       long bytes = super.write(position);
/* 1088 */       position += bytes;
/* 1089 */       int length = this.m_key.length();
/* 1090 */       this.m_file.setIntAt(position, length);
/* 1091 */       position += 4L;
/* 1092 */       this.m_file.setCharsAt(position, this.m_key.toCharArray(), 0, length);
/* 1093 */       position += length * 2;
/* 1094 */       return position - originalPosition;
/*      */     }
/*      */ 
/*      */     public long getSize()
/*      */     {
/* 1100 */       return 4 + this.m_key.length() * 2 + super.getSize();
/*      */     }
/*      */ 
/*      */     public long getMaxSize()
/*      */     {
/* 1106 */       return 4 + MemoryMappableHashMap.this.m_maxKeySize * 2 + super.getMaxSize();
/*      */     }
/*      */ 
/*      */     public String toString()
/*      */     {
/* 1115 */       return " Key : " + this.m_key;
/*      */     }
/*      */   }
/*      */ 
/*      */   public class HashEntry extends MemoryMappableItem
/*      */   {
/*      */     public long m_hashCode;
/*      */     public long m_offset;
/*      */ 
/*      */     public HashEntry(MemoryMappedFile file)
/*      */     {
/*  926 */       super(file);
/*      */     }
/*      */ 
/*      */     public HashEntry()
/*      */     {
/*  931 */       super(input);
/*  932 */       this.m_hashCode = input.m_hashCode;
/*  933 */       this.m_offset = input.m_offset;
/*      */     }
/*      */ 
/*      */     public MemoryMappable createInstance()
/*      */     {
/*  939 */       return new HashEntry(MemoryMappableHashMap.this, this.m_file);
/*      */     }
/*      */ 
/*      */     public void clear()
/*      */     {
/*  945 */       this.m_hashCode = 0L;
/*  946 */       this.m_offset = 0L;
/*      */     }
/*      */ 
/*      */     public long read(long position)
/*      */       throws DataException
/*      */     {
/*  956 */       long OriginalPosition = position;
/*  957 */       long bytes = super.read(position);
/*  958 */       position += bytes;
/*  959 */       if (!super.isEmpty())
/*      */       {
/*  961 */         this.m_hashCode = this.m_file.getLongAt(position);
/*  962 */         position += 8L;
/*  963 */         this.m_offset = this.m_file.getLongAt(position);
/*  964 */         position += 8L;
/*      */       }
/*      */       else
/*      */       {
/*  970 */         return bytes + getMaxSize();
/*      */       }
/*  972 */       return position - OriginalPosition;
/*      */     }
/*      */ 
/*      */     public long write(long position)
/*      */       throws DataException
/*      */     {
/*  982 */       long OriginalPosition = position;
/*  983 */       long bytes = super.write(position);
/*  984 */       position += bytes;
/*  985 */       this.m_file.setLongAt(position, this.m_hashCode);
/*  986 */       position += 8L;
/*  987 */       this.m_file.setLongAt(position, this.m_offset);
/*  988 */       position += 8L;
/*  989 */       return position - OriginalPosition;
/*      */     }
/*      */ 
/*      */     public long getSize()
/*      */     {
/*  995 */       return super.getSize() + 8L + 8L;
/*      */     }
/*      */ 
/*      */     public long getMaxSize()
/*      */     {
/* 1001 */       return super.getMaxSize() + 8L + 8L;
/*      */     }
/*      */ 
/*      */     public String toString()
/*      */     {
/* 1010 */       IdcStringBuilder hashEntryBuilder = new IdcStringBuilder();
/* 1011 */       hashEntryBuilder.append(" HashCode : " + this.m_hashCode);
/* 1012 */       hashEntryBuilder.append(" Offset : " + this.m_offset);
/* 1013 */       return hashEntryBuilder.toString();
/*      */     }
/*      */   }
/*      */ 
/*      */   public class HashMetadata extends MemoryMappableItem
/*      */   {
/*      */     public static final int MAX_PATH_SIZE = 4000;
/*      */     public String m_filePath;
/*      */     public long m_tail;
/*      */ 
/*      */     public HashMetadata(MemoryMappedFile file)
/*      */     {
/*  832 */       super(file);
/*      */     }
/*      */ 
/*      */     public MemoryMappable createInstance()
/*      */     {
/*  838 */       return new HashMetadata(MemoryMappableHashMap.this, this.m_file);
/*      */     }
/*      */ 
/*      */     public void clear()
/*      */     {
/*  844 */       this.m_filePath = "";
/*  845 */       this.m_tail = 0L;
/*      */     }
/*      */ 
/*      */     public long read(long position)
/*      */       throws DataException
/*      */     {
/*  854 */       long originalPosition = position;
/*  855 */       long bytes = super.read(position);
/*  856 */       position += bytes;
/*  857 */       if (!super.isEmpty())
/*      */       {
/*  859 */         int length = this.m_file.getIntAt(position);
/*  860 */         position += 4L;
/*  861 */         char[] path = new char[length];
/*  862 */         this.m_file.getCharsAt(position, path, 0, length);
/*  863 */         this.m_filePath = new String(path);
/*  864 */         position += length * 2;
/*  865 */         this.m_tail = this.m_file.getLongAt(position);
/*  866 */         position += 8L;
/*      */       }
/*      */       else
/*      */       {
/*  872 */         return bytes + getMaxSize();
/*      */       }
/*  874 */       return position - originalPosition;
/*      */     }
/*      */ 
/*      */     public long write(long position)
/*      */       throws DataException
/*      */     {
/*  883 */       long originalPosition = position;
/*  884 */       long bytes = super.write(position);
/*  885 */       position += bytes;
/*  886 */       this.m_file.setIntAt(position, this.m_filePath.length());
/*  887 */       position += 4L;
/*  888 */       this.m_file.setCharsAt(position, this.m_filePath.toCharArray(), 0, this.m_filePath.length());
/*  889 */       position += this.m_filePath.length() * 2;
/*  890 */       this.m_file.setLongAt(position, this.m_tail);
/*  891 */       position += 8L;
/*  892 */       return position - originalPosition;
/*      */     }
/*      */ 
/*      */     public long getSize()
/*      */     {
/*  898 */       return super.getSize() + 4L + this.m_filePath.length() * 2 + 8L;
/*      */     }
/*      */ 
/*      */     public long getMaxSize()
/*      */     {
/*  905 */       return super.getSize() + 4L + 8000L + 8L;
/*      */     }
/*      */ 
/*      */     public String toString()
/*      */     {
/*  912 */       return this.m_filePath + " " + this.m_tail;
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.MemoryMappableHashMap
 * JD-Core Version:    0.5.4
 */