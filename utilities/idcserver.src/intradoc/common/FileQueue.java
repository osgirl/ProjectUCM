/*      */ package intradoc.common;
/*      */ 
/*      */ import intradoc.shared.SharedObjects;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.io.OutputStream;
/*      */ import java.util.Date;
/*      */ 
/*      */ public class FileQueue
/*      */ {
/*      */   public static final int MIN_NUM_MESSAGES = 50;
/*      */   public static final int MIN_ORIGINAL_SIZE = 100000;
/*      */   public static final int MIN_SIZE = 200000;
/*      */   public static final int MAX_NUM_MESSAGES_ALLOWED = 500000;
/*      */   public static final int MAX_SIZE_ALLOWED = 1000000000;
/*      */   public static final short AVAILABLE_MESSAGE = 1;
/*      */   public static final short RESERVED_MESSAGE = 2;
/*      */   public static final short HEADER_CACHE_INDEX = 0;
/*      */   public static final short MESSAGE_CACHE_INDEX = 1;
/*   64 */   public static int[] m_smallCacheSizes = { 1024, 0 };
/*      */ 
/*   67 */   public static int[] m_largeCacheSizes = { 4096, 4096 };
/*      */   protected String m_dir;
/*      */   protected int m_headerLen;
/*      */   protected int m_msgHeaderLen;
/*      */   protected int m_expirationInterval;
/*      */   protected FixedFieldFormatter m_headerFormatter;
/*      */   protected FixedFieldFormatter m_msgHeadFormatter;
/*      */   protected boolean m_queueFilePresent;
/*      */   protected RandomAccessConfigFile m_fileAccess;
/*      */   protected String m_queueName;
/*      */   protected File m_file;
/*      */   protected FileQueueHeader m_header;
/*      */   protected FileMessageHeader m_msgHeader;
/*      */   protected int m_loadedMsgHeaderIndex;
/*      */   protected boolean m_isReserved;
/*      */   protected boolean m_isPromoted;
/*  118 */   protected boolean m_doLargeCaching = false;
/*      */ 
/*  121 */   protected int m_currentCacheIndex = 0;
/*      */ 
/*  124 */   protected byte[][] m_caches = (byte[][])null;
/*      */ 
/*  127 */   protected long[] m_startPointsOfCache = null;
/*      */ 
/*  130 */   protected long[] m_endPointsOfCache = null;
/*      */ 
/*  133 */   protected long[][] m_currentByteRanges = (long[][])null;
/*      */   protected static final String EMPTY_QUEUE_STATUS_FILE = "hasContents.dat";
/*      */ 
/*      */   public FileQueue()
/*      */   {
/*  143 */     this.m_dir = null;
/*  144 */     init();
/*      */   }
/*      */ 
/*      */   public FileQueue(String dir)
/*      */   {
/*  149 */     this.m_dir = dir;
/*  150 */     init();
/*      */   }
/*      */ 
/*      */   public void setDirectory(String dir)
/*      */   {
/*  155 */     this.m_dir = dir;
/*      */   }
/*      */ 
/*      */   public void init()
/*      */   {
/*  160 */     this.m_expirationInterval = SharedObjects.getTypedEnvironmentInt("FileQueueExpirationIntervalTimeoutInSeconds", FileUtils.m_lockTimeout / 2, 24, 24);
/*      */ 
/*  164 */     this.m_headerLen = 98;
/*  165 */     this.m_msgHeaderLen = 34;
/*  166 */     this.m_isReserved = false;
/*  167 */     this.m_isPromoted = false;
/*  168 */     reset();
/*      */   }
/*      */ 
/*      */   public void reset()
/*      */   {
/*  174 */     if (this.m_headerFormatter != null)
/*      */     {
/*  176 */       this.m_headerFormatter.release();
/*      */     }
/*  178 */     if (this.m_msgHeadFormatter != null)
/*      */     {
/*  180 */       this.m_msgHeadFormatter.release();
/*      */     }
/*  182 */     this.m_headerFormatter = new FixedFieldFormatter(this.m_headerLen);
/*  183 */     this.m_msgHeadFormatter = new FixedFieldFormatter(this.m_msgHeaderLen);
/*      */ 
/*  185 */     this.m_fileAccess = null;
/*  186 */     this.m_file = null;
/*      */ 
/*  188 */     this.m_header = new FileQueueHeader();
/*  189 */     this.m_msgHeader = new FileMessageHeader();
/*  190 */     this.m_queueFilePresent = false;
/*      */ 
/*  192 */     this.m_loadedMsgHeaderIndex = -1;
/*      */ 
/*  194 */     clearCaches();
/*      */   }
/*      */ 
/*      */   public void clearCaches()
/*      */   {
/*  199 */     this.m_startPointsOfCache = null;
/*  200 */     this.m_endPointsOfCache = null;
/*  201 */     this.m_currentByteRanges = ((long[][])null);
/*      */   }
/*      */ 
/*      */   public void reserve()
/*      */   {
/*  208 */     if (this.m_isReserved)
/*      */     {
/*  210 */       Report.trace("filequeue", "Locking already locked queue " + this.m_queueName, null);
/*      */     }
/*      */     try
/*      */     {
/*  214 */       reserveAccess();
/*  215 */       this.m_doLargeCaching = true;
/*  216 */       this.m_isReserved = true;
/*      */     }
/*      */     catch (IOException io)
/*      */     {
/*  220 */       Report.trace("system", null, io);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void release()
/*      */   {
/*  226 */     this.m_isReserved = false;
/*  227 */     this.m_doLargeCaching = false;
/*  228 */     releaseAccess();
/*  229 */     this.m_headerFormatter.release();
/*  230 */     this.m_msgHeadFormatter.release();
/*      */   }
/*      */ 
/*      */   public long getLastModified(String queueName)
/*      */   {
/*  235 */     File tempFile = FileUtilsCfgBuilder.getCfgFile(this.m_dir + queueName + ".qdt", "Queue", false);
/*  236 */     long l = tempFile.lastModified();
/*  237 */     if (l < 0L)
/*      */     {
/*  239 */       l = 0L;
/*      */     }
/*  241 */     return l;
/*      */   }
/*      */ 
/*      */   public int getCreationLength()
/*      */   {
/*  247 */     return this.m_headerLen + 50 * this.m_msgHeaderLen + 200000;
/*      */   }
/*      */ 
/*      */   public void appendMessage(String queueName, String id, String msg) throws IOException
/*      */   {
/*  252 */     reserveAccess();
/*      */     try
/*      */     {
/*  256 */       loadQueue(queueName);
/*  257 */       appendMessageEntry(id, msg);
/*      */     }
/*      */     finally
/*      */     {
/*  262 */       releaseAccess();
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean deleteMessage(String queueName, String id)
/*      */     throws IOException
/*      */   {
/*  270 */     reserveAccess();
/*      */     try
/*      */     {
/*  274 */       loadQueue(queueName);
/*  275 */       if (!findMessageHeaderById(id))
/*      */       {
/*  277 */         Report.trace("filequeue", "delete from queue " + this.m_queueName + " could not find " + id, null);
/*  278 */         int i = 0;
/*      */         return i;
/*      */       }
/*  282 */       deleteCurrentEntry();
/*      */     }
/*      */     finally
/*      */     {
/*  287 */       releaseAccess();
/*      */     }
/*  289 */     return true;
/*      */   }
/*      */ 
/*      */   public String getFirstAvailableMessage(String queueName, boolean reserveMsg, FileMessageHeader msgHeader)
/*      */     throws IOException
/*      */   {
/*  300 */     reserveAccess();
/*  301 */     String msg = null;
/*      */     try
/*      */     {
/*  306 */       loadQueue(queueName);
/*      */ 
/*  316 */       if (!findFirstAvailableMessage())
/*      */       {
/*  318 */         Object localObject1 = null;
/*      */         return localObject1;
/*      */       }
/*  322 */       if ((reserveMsg) && 
/*  324 */         (!this.m_msgHeader.m_isReversed))
/*      */       {
/*  326 */         reserveCurrentMessage();
/*      */       }
/*      */ 
/*  331 */       msg = getMessageContents(msgHeader);
/*      */ 
/*  333 */       if (SystemUtils.m_verbose)
/*      */       {
/*  335 */         Report.debug("filequeue", "foundFirstAvailableMessage id=" + msgHeader.m_id + " index=" + this.m_loadedMsgHeaderIndex + " maxIndex=" + this.m_header.m_maxMessages, null);
/*      */       }
/*      */ 
/*  339 */       if (this.m_msgHeader.m_isReversed == true)
/*      */       {
/*  343 */         deleteCurrentEntry();
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  348 */       releaseAccess();
/*      */     }
/*      */ 
/*  351 */     return msg;
/*      */   }
/*      */ 
/*      */   public String getMessageByIndex(int index, String queueName, FileMessageHeader msgHeader, boolean reserveMsg)
/*      */     throws IOException
/*      */   {
/*  361 */     reserveAccess();
/*  362 */     String msg = null;
/*      */     try
/*      */     {
/*  367 */       loadQueue(queueName);
/*  368 */       if (this.m_header.m_startMessage < 0)
/*      */       {
/*  370 */         Object localObject1 = null;
/*      */         return localObject1;
/*      */       }
/*  373 */       int hindex = this.m_header.m_startMessage + index;
/*      */       Object localObject2;
/*  374 */       if (this.m_header.m_startMessage <= this.m_header.m_endMessage)
/*      */       {
/*  376 */         if (hindex > this.m_header.m_endMessage)
/*      */         {
/*  378 */           localObject2 = null;
/*      */           return localObject2;
/*      */         }
/*      */       }
/*  383 */       else if (hindex >= this.m_header.m_maxMessages)
/*      */       {
/*  385 */         hindex -= this.m_header.m_maxMessages;
/*  386 */         if (hindex > this.m_header.m_endMessage)
/*      */         {
/*  388 */           localObject2 = null;
/*      */           return localObject2;
/*      */         }
/*      */       }
/*  393 */       this.m_loadedMsgHeaderIndex = hindex;
/*  394 */       serializeMessageHeader(false);
/*      */ 
/*  397 */       if (this.m_msgHeader.m_state == 2)
/*      */       {
/*  399 */         int curMarker = computeMarker();
/*  400 */         if (checkExpiredReservation(this.m_msgHeader, curMarker))
/*      */         {
/*  402 */           this.m_msgHeader.m_state = 1;
/*  403 */           serializeMessageHeader(true);
/*      */         }
/*      */       }
/*      */ 
/*  407 */       if (reserveMsg == true)
/*      */       {
/*  409 */         reserveCurrentMessage();
/*      */       }
/*      */ 
/*  413 */       msg = getMessageContents(msgHeader);
/*      */ 
/*  416 */       if ((index == 0) && (msgHeader.m_state == 0))
/*      */       {
/*  418 */         deleteCurrentEntry();
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  423 */       releaseAccess();
/*      */     }
/*      */ 
/*  426 */     return msg;
/*      */   }
/*      */ 
/*      */   public String findMessage(String queueName, String id, boolean reserveMsg, FileMessageHeader msgHeader)
/*      */     throws IOException
/*      */   {
/*  434 */     reserveAccess();
/*  435 */     String msg = null;
/*      */     try
/*      */     {
/*  440 */       loadQueue(queueName);
/*  441 */       if (!findMessageHeaderById(id))
/*      */       {
/*  443 */         Report.trace("filequeue", "find in queue " + this.m_queueName + " failed to find msg id " + id, null);
/*  444 */         Object localObject1 = null;
/*      */         return localObject1;
/*      */       }
/*  446 */       Report.trace("filequeue", "foundMessage id=" + id, null);
/*      */ 
/*  449 */       if (reserveMsg)
/*      */       {
/*  451 */         reserveCurrentMessage();
/*      */       }
/*      */ 
/*  455 */       msg = getMessageContents(msgHeader);
/*      */     }
/*      */     finally
/*      */     {
/*  459 */       releaseAccess();
/*      */     }
/*  461 */     return msg;
/*      */   }
/*      */ 
/*      */   public boolean continueReservation(String queueName, FileMessageHeader msgHeader)
/*      */     throws IOException
/*      */   {
/*  474 */     reserveAccess();
/*      */ 
/*  476 */     String id = msgHeader.m_id;
/*      */     try
/*      */     {
/*  481 */       loadQueue(queueName);
/*  482 */       if (!findMessageHeaderById(id))
/*      */       {
/*  484 */         Report.trace("filequeue", "continue lock in queue " + this.m_queueName + " failed to find msg id " + id, null);
/*  485 */         msgHeader.m_id = null;
/*  486 */         int i = 0;
/*      */         return i;
/*      */       }
/*      */       boolean bool1;
/*  490 */       boolean bool1 = this.m_msgHeader.m_marker == msgHeader.m_marker;
/*  491 */       if ((bool1) || (this.m_msgHeader.m_isReversed == true))
/*      */       {
/*  493 */         if (!this.m_msgHeader.m_isReversed)
/*      */         {
/*  495 */           reserveCurrentMessage();
/*      */         }
/*      */ 
/*  498 */         msgHeader.copy(this.m_msgHeader);
/*      */ 
/*  500 */         if (this.m_msgHeader.m_isReversed == true)
/*      */         {
/*  504 */           Report.trace("filequeue", "msg id: " + id + " was reversed and will be removed from queue", null);
/*  505 */           deleteCurrentEntry();
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  510 */         Report.trace("filequeue", "Reservation ending for msg id: " + id + "; marker match: " + bool1 + "; was reversed: " + this.m_msgHeader.m_isReversed, null);
/*      */ 
/*  512 */         msgHeader.m_state = 2;
/*  513 */         int j = 0;
/*      */         return j;
/*      */       } } finally { releaseAccess(); }
/*      */ 
/*  520 */     return !msgHeader.m_isReversed;
/*      */   }
/*      */ 
/*      */   public String reverseMessage(String queueName, FileMessageHeader msgHeader)
/*      */     throws IOException
/*      */   {
/*  534 */     reserveAccess();
/*      */ 
/*  536 */     String id = msgHeader.m_id;
/*  537 */     String msg = null;
/*      */     try
/*      */     {
/*  542 */       loadQueue(queueName);
/*  543 */       if (!findMessageHeaderById(id))
/*      */       {
/*  545 */         Report.trace("filequeue", "reverse msg in queue " + this.m_queueName + " msg id " + id + " (not present)", null);
/*  546 */         Object localObject1 = null;
/*      */         return localObject1;
/*      */       }
/*  550 */       msg = getMessageContents(msgHeader);
/*      */ 
/*  553 */       if (this.m_msgHeader.m_state == 2)
/*      */       {
/*  555 */         Report.trace("filequeue", "reverse msg in queue " + this.m_queueName + " msg id " + id + " (released)", null);
/*  556 */         this.m_msgHeader.m_isReversed = true;
/*  557 */         msgHeader.m_isReversed = true;
/*  558 */         serializeMessageHeader(true);
/*      */       }
/*      */       else
/*      */       {
/*  562 */         Report.trace("filequeue", "reverse msg in queue " + this.m_queueName + " msg id " + id + " (delete)", null);
/*  563 */         deleteCurrentEntry();
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  568 */       releaseAccess();
/*      */     }
/*  570 */     return msg;
/*      */   }
/*      */ 
/*      */   public boolean transferMessage(String fromQueue, String toQueue, String id, String msg)
/*      */     throws IOException
/*      */   {
/*  579 */     reserveAccess();
/*  580 */     Report.trace("filequeue", "Transfer from " + fromQueue + " to " + toQueue + " msg id " + id, null);
/*      */     try
/*      */     {
/*  585 */       loadQueue(fromQueue);
/*      */ 
/*  587 */       if (!findMessageHeaderById(id))
/*      */       {
/*  589 */         int i = 0;
/*      */         return i;
/*      */       }
/*  593 */       FileQueue fileQueue = createNewQueueInstance();
/*  594 */       fileQueue.loadQueue(toQueue);
/*      */ 
/*  597 */       fileQueue.appendMessageEntry(id, msg);
/*      */ 
/*  600 */       fileQueue.closeFile();
/*      */ 
/*  603 */       deleteCurrentEntry();
/*      */     }
/*      */     finally
/*      */     {
/*  607 */       releaseAccess();
/*      */     }
/*  609 */     return true;
/*      */   }
/*      */ 
/*      */   public int getCount(String queueName)
/*      */     throws IOException
/*      */   {
/*  615 */     int count = 0;
/*  616 */     reserveAccess();
/*      */     try
/*      */     {
/*  621 */       loadQueue(queueName);
/*  622 */       count = this.m_header.m_usedMessages;
/*      */     }
/*      */     finally
/*      */     {
/*  626 */       releaseAccess();
/*      */     }
/*  628 */     return count;
/*      */   }
/*      */ 
/*      */   public boolean isQueueEmpty(String queueName)
/*      */   {
/*  633 */     String path = this.m_dir + queueName + "hasContents.dat";
/*  634 */     return checkHasContentsFlag(path);
/*      */   }
/*      */ 
/*      */   protected boolean checkHasContentsFlag(String path)
/*      */   {
/*  642 */     int fileStatus = FileUtils.checkFile(path, true, true);
/*      */ 
/*  645 */     return fileStatus != 0;
/*      */   }
/*      */ 
/*      */   protected void appendMessageEntry(String id, String msg)
/*      */     throws IOException
/*      */   {
/*  652 */     byte[] buf = StringUtils.getBytes(msg, FileUtils.m_javaSystemEncoding);
/*  653 */     int msgLen = buf.length;
/*      */ 
/*  656 */     if (msgLen > 10000000)
/*      */     {
/*  658 */       throw new IOException(LocaleUtils.encodeMessage("csFileQueueMessageTooLarge", null, new Integer(msgLen), this.m_queueName));
/*      */     }
/*      */ 
/*  662 */     Report.trace("filequeue", "Append to " + this.m_queueName + " msg id " + id, null);
/*  663 */     setContentsFlag(true);
/*      */ 
/*  665 */     int newIndex = 0;
/*  666 */     boolean isWrapping = false;
/*      */     while (true)
/*      */     {
/*  671 */       newIndex = this.m_header.m_endMessage + 1;
/*  672 */       if (newIndex == this.m_header.m_maxMessages)
/*      */       {
/*  674 */         newIndex = 0;
/*      */       }
/*      */ 
/*  677 */       if (newIndex != this.m_header.m_startMessage)
/*      */       {
/*  681 */         int wrappingLen = 0;
/*  682 */         int followLen = 0;
/*  683 */         if ((this.m_header.m_startByte < 0) || (this.m_header.m_startMessage < 0))
/*      */         {
/*  685 */           followLen = this.m_header.m_size;
/*      */         }
/*  687 */         else if (this.m_header.m_endByte >= this.m_header.m_startByte)
/*      */         {
/*  689 */           wrappingLen = this.m_header.m_startByte - this.m_header.m_offsetToData;
/*  690 */           followLen = this.m_header.m_size - (this.m_header.m_endByte - this.m_header.m_offsetToData);
/*      */         }
/*      */         else
/*      */         {
/*  695 */           followLen = this.m_header.m_startByte - this.m_header.m_endByte;
/*      */         }
/*  697 */         if (msgLen < followLen) {
/*      */           break;
/*      */         }
/*      */ 
/*  701 */         if (msgLen < wrappingLen)
/*      */         {
/*  703 */           isWrapping = true;
/*  704 */           break;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  709 */       rebuildQueue(msgLen);
/*      */     }
/*      */ 
/*  713 */     this.m_header.m_action = 0;
/*  714 */     this.m_header.m_index = this.m_header.m_endMessage;
/*      */ 
/*  717 */     serializeQueueHeader(true);
/*      */ 
/*  720 */     int msgLoc = this.m_header.m_endByte;
/*  721 */     if ((this.m_header.m_startByte < 0) || (this.m_header.m_startMessage < 0))
/*      */     {
/*  723 */       this.m_header.m_startByte = this.m_header.m_offsetToData;
/*  724 */       this.m_header.m_startMessage = 0;
/*      */ 
/*  726 */       isWrapping = true;
/*      */     }
/*  728 */     if (isWrapping)
/*      */     {
/*  730 */       msgLoc = this.m_header.m_offsetToData;
/*  731 */       this.m_header.m_endByte = (this.m_header.m_offsetToData + msgLen);
/*      */     }
/*      */     else
/*      */     {
/*  735 */       this.m_header.m_endByte += msgLen;
/*      */     }
/*  737 */     this.m_header.m_endMessage = newIndex;
/*  738 */     this.m_header.m_usedMessages += 1;
/*  739 */     this.m_header.m_numBytesUsed += msgLen;
/*      */ 
/*  742 */     this.m_currentCacheIndex = 1;
/*  743 */     serializeBytes(msgLoc, buf, true);
/*      */ 
/*  746 */     this.m_msgHeader.m_id = id;
/*  747 */     this.m_msgHeader.m_state = 1;
/*  748 */     this.m_msgHeader.m_offset = msgLoc;
/*  749 */     this.m_msgHeader.m_len = msgLen;
/*  750 */     this.m_msgHeader.m_marker = computeMarker();
/*      */ 
/*  752 */     this.m_loadedMsgHeaderIndex = newIndex;
/*  753 */     serializeMessageHeader(true);
/*      */ 
/*  755 */     if (SystemUtils.m_verbose)
/*      */     {
/*  757 */       Report.debug("filequeue", "append with index=" + this.m_loadedMsgHeaderIndex + " maxIndex=" + this.m_header.m_maxMessages + " msgLen=" + msgLen + " queueSize=" + this.m_header.m_size + " bytesUsed=" + this.m_header.m_numBytesUsed, null);
/*      */     }
/*      */ 
/*  762 */     this.m_header.m_action = 2;
/*  763 */     serializeQueueHeader(true);
/*      */   }
/*      */ 
/*      */   protected void checkForRebuild(int msgLen)
/*      */     throws IOException
/*      */   {
/*  769 */     if ((this.m_header.m_numBytesUsed + msgLen + 200000 >= this.m_header.m_size / 4) && (this.m_header.m_usedMessages + 50 >= this.m_header.m_maxMessages / 9)) {
/*      */       return;
/*      */     }
/*  772 */     rebuildQueue(0);
/*      */   }
/*      */ 
/*      */   protected boolean findMessageHeaderById(String id)
/*      */     throws IOException
/*      */   {
/*  778 */     if (this.m_header.m_startMessage < 0)
/*      */     {
/*  780 */       return false;
/*      */     }
/*      */ 
/*  784 */     this.m_loadedMsgHeaderIndex = this.m_header.m_startMessage;
/*      */     do
/*      */     {
/*  787 */       serializeMessageHeader(false);
/*  788 */       if ((this.m_msgHeader.m_state >= 1) && (this.m_msgHeader.m_id.equals(id)))
/*      */       {
/*  790 */         return true;
/*      */       }
/*      */     }
/*  792 */     while (incrementHeaderIndex());
/*      */ 
/*  797 */     return false;
/*      */   }
/*      */ 
/*      */   protected void deleteCurrentEntry() throws IOException
/*      */   {
/*  802 */     boolean isRemove = false;
/*  803 */     boolean nothingLeft = false;
/*      */ 
/*  806 */     int msgLen = this.m_msgHeader.m_len;
/*      */ 
/*  808 */     String id = this.m_msgHeader.m_id;
/*      */ 
/*  815 */     if (this.m_loadedMsgHeaderIndex == this.m_header.m_startMessage)
/*      */     {
/*  817 */       if (!findFirstNonDeletedFollowing())
/*      */       {
/*  819 */         nothingLeft = true;
/*      */       }
/*      */ 
/*  822 */       isRemove = true;
/*      */     }
/*      */ 
/*  831 */     this.m_header.m_action = 1;
/*  832 */     String extraDesc = "mark as deleted";
/*      */ 
/*  834 */     if (isRemove)
/*      */     {
/*  837 */       if (!nothingLeft)
/*      */       {
/*  840 */         this.m_header.m_index = this.m_loadedMsgHeaderIndex;
/*  841 */         extraDesc = "advance to msg id " + this.m_msgHeader.m_id;
/*      */       }
/*      */       else
/*      */       {
/*  845 */         this.m_header.m_index = -1;
/*  846 */         setContentsFlag(false);
/*  847 */         extraDesc = "emptying queue";
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*  853 */       this.m_header.m_index = this.m_header.m_startMessage;
/*      */     }
/*      */ 
/*  856 */     Report.trace("filequeue", "delete from " + this.m_queueName + " msg id " + id, null);
/*  857 */     if (SystemUtils.m_verbose)
/*      */     {
/*  859 */       Report.debug("filequeue", "delete with index=" + this.m_loadedMsgHeaderIndex + " maxIndex=" + this.m_header.m_maxMessages + " msgLen=" + msgLen + " queueSize=" + this.m_header.m_size + " bytesUsed=" + this.m_header.m_numBytesUsed + " (" + extraDesc + ")", null);
/*      */     }
/*      */ 
/*  865 */     serializeQueueHeader(true);
/*      */ 
/*  868 */     if (isRemove)
/*      */     {
/*  870 */       this.m_header.m_startMessage = this.m_header.m_index;
/*  871 */       if (this.m_header.m_index < 0)
/*      */       {
/*  873 */         this.m_header.m_endMessage = -1;
/*  874 */         this.m_header.m_startByte = -1;
/*  875 */         this.m_header.m_endByte = -1;
/*      */       }
/*      */       else
/*      */       {
/*  879 */         this.m_header.m_startByte = this.m_msgHeader.m_offset;
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*  885 */       this.m_msgHeader.m_state = 0;
/*  886 */       serializeMessageHeader(true);
/*      */     }
/*      */ 
/*  890 */     if (this.m_header.m_index < 0)
/*      */     {
/*  892 */       this.m_header.m_numBytesUsed = 0;
/*  893 */       this.m_header.m_usedMessages = 0;
/*      */     }
/*      */     else
/*      */     {
/*  897 */       this.m_header.m_numBytesUsed -= msgLen;
/*  898 */       this.m_header.m_usedMessages -= 1;
/*      */     }
/*      */ 
/*  902 */     this.m_header.m_action = 2;
/*  903 */     serializeQueueHeader(true);
/*      */ 
/*  906 */     checkForRebuild(msgLen);
/*      */   }
/*      */ 
/*      */   protected boolean findFirstAvailableMessage()
/*      */     throws IOException
/*      */   {
/*  913 */     if (this.m_header.m_startMessage < 0)
/*      */     {
/*  915 */       return false;
/*      */     }
/*      */ 
/*  918 */     int curMarker = computeMarker();
/*      */ 
/*  920 */     boolean isFirstLoop = true;
/*      */ 
/*  923 */     this.m_loadedMsgHeaderIndex = this.m_header.m_startMessage;
/*      */     do {
/*      */       do {
/*  926 */         serializeMessageHeader(false);
/*      */ 
/*  929 */         if ((!isFirstLoop) || (this.m_msgHeader.m_state != 0))
/*      */           break label85;
/*  931 */         deleteCurrentEntry();
/*  932 */         Report.trace("filequeue", "removing deleted entries at the beginning of queue.", null);
/*  933 */         this.m_loadedMsgHeaderIndex = this.m_header.m_startMessage;
/*  934 */       }while (this.m_header.m_startMessage >= 0);
/*      */ 
/*  936 */       break;
/*      */ 
/*  940 */       label85: isFirstLoop = false;
/*      */ 
/*  943 */       if ((this.m_msgHeader.m_state == 2) && 
/*  945 */         (checkExpiredReservation(this.m_msgHeader, curMarker)))
/*      */       {
/*  947 */         this.m_msgHeader.m_state = 1;
/*  948 */         serializeMessageHeader(true);
/*      */       }
/*      */ 
/*  953 */       if (this.m_msgHeader.m_state == 1)
/*      */       {
/*  955 */         return true;
/*      */       }
/*      */     }
/*  957 */     while (incrementHeaderIndex());
/*      */ 
/*  962 */     return false;
/*      */   }
/*      */ 
/*      */   protected boolean checkExpiredReservation(FileMessageHeader msgHeader, int curMarker)
/*      */   {
/*  967 */     int diff = curMarker - msgHeader.m_marker;
/*  968 */     if (diff < 0)
/*      */     {
/*  970 */       diff = -diff;
/*      */     }
/*  972 */     int diff2 = 10000 - diff;
/*  973 */     if (diff2 < 0)
/*      */     {
/*  975 */       diff2 = -diff2;
/*      */     }
/*  977 */     if (diff2 < diff)
/*      */     {
/*  979 */       diff = diff2;
/*      */     }
/*      */ 
/*  983 */     return diff > this.m_expirationInterval;
/*      */   }
/*      */ 
/*      */   protected boolean findFirstNonDeletedFollowing()
/*      */     throws IOException
/*      */   {
/*  990 */     while (incrementHeaderIndex() == true) {
/*      */       do {
/*  992 */         serializeMessageHeader(false);
/*  993 */         if (this.m_msgHeader.m_state > 0)
/*      */         {
/*  995 */           return true;
/*      */         }
/*      */       }
/*  997 */       while (!SystemUtils.m_verbose);
/*      */ 
/*  999 */       Report.debug("filequeue", "skippingOverDeletedEntry id=" + this.m_msgHeader.m_id, null);
/*      */     }
/*      */ 
/* 1003 */     return false;
/*      */   }
/*      */ 
/*      */   protected String getMessageContents(FileMessageHeader msgHeader) throws IOException
/*      */   {
/* 1008 */     this.m_currentCacheIndex = 1;
/* 1009 */     byte[] buf = new byte[this.m_msgHeader.m_len];
/* 1010 */     serializeBytes(this.m_msgHeader.m_offset, buf, false);
/* 1011 */     String msg = StringUtils.getString(buf, FileUtils.m_javaSystemEncoding);
/* 1012 */     if (msgHeader != null)
/*      */     {
/* 1014 */       msgHeader.copy(this.m_msgHeader);
/*      */     }
/* 1016 */     return msg;
/*      */   }
/*      */ 
/*      */   protected void checkIsWriteAccessible() throws IOException
/*      */   {
/* 1021 */     if (this.m_fileAccess != null)
/*      */       return;
/* 1023 */     createNewQueue(50, 200000, null);
/* 1024 */     this.m_queueFilePresent = true;
/* 1025 */     this.m_fileAccess = FileUtilsCfgBuilder.getCfgRandomAccess(this.m_file, "rw");
/*      */   }
/*      */ 
/*      */   protected void loadQueue(String queueName)
/*      */     throws IOException
/*      */   {
/* 1031 */     this.m_queueName = queueName;
/* 1032 */     this.m_loadedMsgHeaderIndex = -1;
/* 1033 */     File tempFile = FileUtilsCfgBuilder.getCfgFile(this.m_dir + queueName + ".qdt", "Queue", false);
/* 1034 */     boolean isSame = (this.m_file != null) && (tempFile.getAbsolutePath().equals(this.m_file.getAbsolutePath()));
/*      */ 
/* 1036 */     if (!isSame)
/*      */     {
/* 1038 */       this.m_file = tempFile;
/*      */     }
/*      */ 
/* 1041 */     this.m_queueFilePresent = (this.m_file.length() >= 100000L);
/*      */ 
/* 1043 */     if ((!isSame) || (this.m_fileAccess == null))
/*      */     {
/* 1052 */       closeFile();
/*      */ 
/* 1056 */       if (this.m_queueFilePresent)
/*      */       {
/* 1058 */         if (SystemUtils.m_verbose)
/*      */         {
/* 1060 */           Report.debug("filequeue", "load queue " + this.m_queueName, null);
/*      */         }
/* 1062 */         this.m_fileAccess = FileUtilsCfgBuilder.getCfgRandomAccess(this.m_file, "rw");
/*      */       }
/* 1067 */       else if (this.m_header.m_size > 0)
/*      */       {
/* 1069 */         this.m_header = new FileQueueHeader();
/* 1070 */         this.m_msgHeader = new FileMessageHeader();
/*      */       }
/*      */ 
/*      */     }
/* 1076 */     else if (!this.m_queueFilePresent)
/*      */     {
/* 1078 */       String msg = LocaleUtils.encodeMessage("csFileQueueFileVanished", null, this.m_file.getAbsolutePath());
/*      */ 
/* 1080 */       throw new IOException(msg);
/*      */     }
/*      */ 
/* 1084 */     if (this.m_queueFilePresent)
/*      */     {
/* 1086 */       serializeQueueHeader(false);
/*      */ 
/* 1089 */       boolean isRebuild = false;
/* 1090 */       switch (this.m_header.m_action)
/*      */       {
/*      */       case 0:
/* 1093 */         isRebuild = true;
/* 1094 */         this.m_header.m_endMessage = this.m_header.m_index;
/* 1095 */         if (this.m_header.m_index < 0)
/*      */         {
/* 1097 */           this.m_header.m_startMessage = this.m_header.m_index; } break;
/*      */       case 1:
/* 1101 */         isRebuild = true;
/* 1102 */         this.m_header.m_startMessage = this.m_header.m_index;
/*      */       }
/*      */ 
/* 1106 */       if (isRebuild)
/*      */       {
/* 1108 */         rebuildQueue(0);
/*      */       }
/*      */       else
/*      */       {
/* 1113 */         checkForRebuild(0);
/*      */       }
/*      */     }
/*      */ 
/* 1117 */     if (this.m_startPointsOfCache != null)
/*      */       return;
/* 1119 */     this.m_startPointsOfCache = new long[] { 0L, this.m_header.m_offsetToData };
/* 1120 */     this.m_endPointsOfCache = new long[] { this.m_header.m_offsetToData, this.m_header.m_size };
/*      */   }
/*      */ 
/*      */   protected void createNewQueue(int numMessages, int size, FileQueue oldQueue)
/*      */     throws IOException
/*      */   {
/* 1127 */     if ((numMessages > 500000) || (size > 1000000000))
/*      */     {
/* 1129 */       throw new IOException(LocaleUtils.encodeMessage("csFileQueueMessageQueueTooLong", null, this.m_queueName));
/*      */     }
/*      */ 
/* 1134 */     if (this.m_fileAccess != null)
/*      */     {
/* 1136 */       clearCaches();
/* 1137 */       this.m_fileAccess.close();
/* 1138 */       this.m_fileAccess = null;
/*      */     }
/*      */ 
/* 1142 */     OutputStream outStream = null;
/*      */     try
/*      */     {
/*      */       try
/*      */       {
/* 1147 */         outStream = FileUtilsCfgBuilder.getCfgOutputStream(this.m_file);
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/* 1151 */         LocaleUtils.encodeMessage("csFileQueueUnableToCreateFile", e.getMessage(), this.m_queueName);
/*      */       }
/*      */ 
/* 1156 */       this.m_header = new FileQueueHeader();
/* 1157 */       this.m_header.m_size = size;
/* 1158 */       this.m_header.m_maxMessages = numMessages;
/*      */ 
/* 1160 */       int offsetToData = this.m_headerLen + numMessages * this.m_msgHeaderLen;
/* 1161 */       this.m_header.m_offsetToData = offsetToData;
/*      */ 
/* 1164 */       int oldMsgCount = 0;
/* 1165 */       if (oldQueue != null)
/*      */       {
/* 1167 */         oldMsgCount = oldQueue.m_header.m_usedMessages;
/* 1168 */         if (oldMsgCount > 0)
/*      */         {
/* 1170 */           int numBytes = oldQueue.m_header.m_numBytesUsed;
/* 1171 */           this.m_header.m_startMessage = 0;
/* 1172 */           this.m_header.m_endMessage = (oldMsgCount - 1);
/* 1173 */           this.m_header.m_usedMessages = oldMsgCount;
/* 1174 */           this.m_header.m_numBytesUsed = numBytes;
/* 1175 */           this.m_header.m_startByte = offsetToData;
/* 1176 */           this.m_header.m_endByte = (offsetToData + numBytes);
/* 1177 */           oldQueue.m_doLargeCaching = true;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1182 */       int fileSize = offsetToData + size;
/* 1183 */       FixedFieldFormatter formatter = new FixedFieldFormatter(100000);
/*      */ 
/* 1185 */       String msg = (oldQueue != null) ? "Copying queue " : "Creating queue ";
/* 1186 */       Report.trace("filequeuebuild", msg + this.m_queueName + " to have " + numMessages + " messages and " + size + " bytes for messages", null);
/*      */ 
/* 1190 */       this.m_header.serializeFixedLen(formatter, true);
/*      */ 
/* 1193 */       this.m_msgHeader = new FileMessageHeader();
/* 1194 */       FileMessageHeader msgHeaderToUse = null;
/*      */ 
/* 1197 */       long amountWritten = 0L;
/*      */ 
/* 1203 */       for (int count = 0; count < 2; ++count)
/*      */       {
/* 1205 */         if ((count == 1) && 
/* 1208 */           (amountWritten + formatter.m_offset != offsetToData))
/*      */         {
/* 1211 */           msg = LocaleUtils.encodeMessage("csFileQueueRebuildInvalidLogic", null, this.m_queueName);
/* 1212 */           throw new IOException(msg);
/*      */         }
/*      */ 
/* 1217 */         int byteCount = 0;
/* 1218 */         if (oldMsgCount > 0)
/*      */         {
/* 1220 */           oldQueue.m_loadedMsgHeaderIndex = oldQueue.m_header.m_startMessage;
/* 1221 */           oldQueue.serializeMessageHeader(false);
/*      */         }
/* 1223 */         for (int i = 0; i < numMessages; ++i)
/*      */         {
/* 1225 */           if (i < oldMsgCount)
/*      */           {
/* 1227 */             msgHeaderToUse = oldQueue.m_msgHeader;
/* 1228 */             int newOffset = byteCount + offsetToData;
/* 1229 */             int msgLen = msgHeaderToUse.m_len;
/* 1230 */             if (count == 1)
/*      */             {
/* 1233 */               byte[] data = new byte[msgLen];
/* 1234 */               oldQueue.m_currentCacheIndex = 1;
/* 1235 */               oldQueue.serializeBytes(msgHeaderToUse.m_offset, data, false);
/* 1236 */               int amtWritten = ensureMinimumSpace(formatter, outStream, msgLen + 1024);
/* 1237 */               amountWritten += amtWritten;
/*      */ 
/* 1239 */               System.arraycopy(data, 0, formatter.m_data, formatter.m_offset, msgLen);
/* 1240 */               formatter.m_offset += msgLen;
/*      */             }
/*      */ 
/* 1244 */             msgHeaderToUse.m_offset = newOffset;
/* 1245 */             byteCount += msgLen;
/*      */           }
/*      */           else
/*      */           {
/* 1250 */             msgHeaderToUse = this.m_msgHeader;
/*      */           }
/* 1252 */           if (count == 0)
/*      */           {
/* 1255 */             int amtWritten = ensureMinimumSpace(formatter, outStream, 1024);
/* 1256 */             amountWritten += amtWritten;
/* 1257 */             msgHeaderToUse.serializeFixedLen(formatter, true);
/*      */           }
/*      */ 
/* 1260 */           if (i >= oldMsgCount - 1)
/*      */             continue;
/* 1262 */           oldQueue.findFirstNonDeletedFollowing();
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1268 */       long bytesDone = amountWritten + formatter.m_offset;
/* 1269 */       while (bytesDone < fileSize)
/*      */       {
/* 1271 */         int spaceFillAmount = 10000;
/* 1272 */         if (spaceFillAmount > fileSize - bytesDone)
/*      */         {
/* 1274 */           spaceFillAmount = (int)(fileSize - bytesDone);
/*      */         }
/* 1276 */         int amtWritten = ensureMinimumSpace(formatter, outStream, spaceFillAmount);
/* 1277 */         amountWritten += amtWritten;
/* 1278 */         formatter.spaceFill(spaceFillAmount, true);
/* 1279 */         bytesDone += spaceFillAmount;
/*      */       }
/* 1281 */       if (formatter.m_offset > 0)
/*      */       {
/* 1283 */         outStream.write(formatter.m_data, 0, formatter.m_offset);
/* 1284 */         amountWritten += formatter.m_offset;
/*      */       }
/* 1286 */       formatter.release();
/* 1287 */       if (amountWritten != fileSize)
/*      */       {
/* 1290 */         msg = LocaleUtils.encodeMessage("csFileQueueRebuildInvalidLogic", null, this.m_queueName);
/* 1291 */         throw new IOException(msg);
/*      */       }
/* 1293 */       Report.trace("filequeuebuild", "Built queue file " + this.m_file.getPath(), null);
/*      */     }
/*      */     finally
/*      */     {
/* 1297 */       FileUtils.closeObject(outStream);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void rebuildQueue(int msgLen)
/*      */     throws IOException
/*      */   {
/* 1305 */     if (this.m_header.m_startMessage < 0)
/*      */     {
/* 1307 */       Report.trace("filequeue", "rebuilding empty queue", null);
/* 1308 */       createNewQueue(50, 200000 + 2 * msgLen, null);
/* 1309 */       this.m_fileAccess = FileUtilsCfgBuilder.getCfgRandomAccess(this.m_file, "rw");
/* 1310 */       this.m_queueFilePresent = true;
/* 1311 */       return;
/*      */     }
/*      */ 
/* 1315 */     long prevTimeStamp = this.m_file.lastModified();
/*      */ 
/* 1317 */     Report.trace("filequeuebuild", "The queue " + this.m_queueName + " has messages size " + this.m_header.m_size + " and messages allocated " + this.m_header.m_maxMessages + "-isPromotedLongLock=" + this.m_isPromoted, null);
/*      */ 
/* 1319 */     Report.trace("filequeuebuild", "Rebuilding queue " + this.m_queueName + " bytes actually used is " + this.m_header.m_numBytesUsed + " and number of messages used is " + this.m_header.m_usedMessages, null);
/*      */ 
/* 1322 */     if (!this.m_isPromoted)
/*      */     {
/*      */       try
/*      */       {
/* 1326 */         FileUtils.promoteExistingLockToLongTermLock(this.m_dir, null);
/* 1327 */         this.m_isPromoted = true;
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/* 1331 */         IOException io = new IOException(e.getMessage());
/* 1332 */         io.initCause(e);
/* 1333 */         throw io;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1340 */     this.m_loadedMsgHeaderIndex = this.m_header.m_startMessage;
/* 1341 */     int byteCount = 0;
/* 1342 */     int msgCount = 0;
/*      */ 
/* 1345 */     if ((this.m_header.m_endMessage >= this.m_header.m_maxMessages) || (this.m_header.m_endMessage < 0))
/*      */     {
/* 1347 */       String filePath = (this.m_file != null) ? this.m_file.getAbsolutePath() : "<no file>";
/* 1348 */       Report.trace("filequeuebuild", "Invalid queue sizes endMessage=" + this.m_header.m_endMessage + " maxMessages=" + this.m_header.m_maxMessages + " for queue " + filePath, null);
/*      */ 
/* 1350 */       String msg = LocaleUtils.encodeMessage("csInvalidQueueSizes", null, this.m_file.getAbsolutePath());
/* 1351 */       throw new IOException(msg);
/*      */     }
/*      */ 
/*      */     do
/*      */     {
/* 1356 */       serializeMessageHeader(false);
/* 1357 */       if (this.m_msgHeader.m_state <= 0)
/*      */         continue;
/* 1359 */       byteCount += this.m_msgHeader.m_len;
/* 1360 */       ++msgCount;
/*      */     }
/* 1362 */     while (incrementHeaderIndex());
/*      */ 
/* 1367 */     this.m_header.m_numBytesUsed = byteCount;
/* 1368 */     this.m_header.m_usedMessages = msgCount;
/*      */ 
/* 1371 */     int newSize = 2 * (200000 + 2 * msgLen + byteCount);
/* 1372 */     int newNumMessages = 3 * (50 + msgCount);
/*      */ 
/* 1375 */     FileQueue fileQueue = createNewQueueInstance();
/*      */ 
/* 1377 */     fileQueue.m_file = FileUtilsCfgBuilder.getCfgFile(this.m_dir + this.m_queueName + ".tmp", "Queue", false);
/* 1378 */     fileQueue.m_queueName = (this.m_queueName + "_tmp_copy");
/* 1379 */     fileQueue.createNewQueue(newNumMessages, newSize, this);
/*      */ 
/* 1382 */     long curTimeStamp = this.m_file.lastModified();
/* 1383 */     this.m_fileAccess.close();
/* 1384 */     this.m_fileAccess = null;
/*      */ 
/* 1387 */     clearCaches();
/*      */ 
/* 1389 */     if (prevTimeStamp != curTimeStamp)
/*      */     {
/* 1391 */       String msg = LocaleUtils.encodeMessage("csFileQueueLockFailureDuringRebuild", null, this.m_file.getAbsolutePath());
/* 1392 */       throw new IOException(msg);
/*      */     }
/* 1394 */     this.m_file.delete();
/* 1395 */     fileQueue.m_file.renameTo(this.m_file);
/* 1396 */     if (fileQueue.m_file.exists())
/*      */     {
/* 1398 */       Report.trace("filequeuebuild", "Rename of " + fileQueue.m_file.getAbsolutePath() + " to " + this.m_file.getAbsolutePath() + " failed", null);
/*      */     }
/*      */     else
/*      */     {
/* 1402 */       Report.trace("filequeuebuild", "Swapped new file for queue " + this.m_queueName + " into place", null);
/*      */     }
/*      */ 
/* 1408 */     this.m_fileAccess = FileUtilsCfgBuilder.getCfgRandomAccess(this.m_file, "rw");
/* 1409 */     this.m_queueFilePresent = true;
/* 1410 */     serializeQueueHeader(false);
/*      */   }
/*      */ 
/*      */   protected void reserveAccess()
/*      */     throws IOException
/*      */   {
/* 1419 */     reserveAccess(false);
/*      */   }
/*      */ 
/*      */   protected void reserveAccess(boolean reserveAnyway) throws IOException {
/* 1423 */     if ((FileUtils.storeInDB(this.m_dir)) && (!reserveAnyway))
/*      */     {
/* 1425 */       return;
/*      */     }
/*      */ 
/* 1428 */     if (SystemUtils.m_verbose)
/*      */     {
/* 1430 */       if (this.m_isReserved)
/*      */       {
/* 1432 */         Report.debug("filequeue", "meaningless reserve access on " + this.m_dir + " because file queue has been reserved long term", null);
/*      */       }
/*      */       else
/*      */       {
/* 1436 */         Report.debug("filequeue", "reserve access " + this.m_dir, null);
/*      */       }
/*      */     }
/*      */     try
/*      */     {
/* 1441 */       if (!this.m_isReserved)
/*      */       {
/* 1443 */         this.m_dir = FileUtils.directorySlashes(this.m_dir);
/* 1444 */         FileUtils.reserveDirectoryEx(this.m_dir, false, false, 0, "filequeue", null);
/*      */       }
/* 1446 */       else if (!this.m_isPromoted)
/*      */       {
/* 1449 */         this.m_isPromoted = FileUtils.checkForAndPromoteToLongTermLock(this.m_dir, null);
/*      */       }
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 1454 */       IOException io = new IOException(e.getMessage());
/* 1455 */       io.initCause(e);
/* 1456 */       throw io;
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void releaseAccess()
/*      */   {
/* 1462 */     releaseAccess(false);
/*      */   }
/*      */ 
/*      */   protected void releaseAccess(boolean releaseAnyway)
/*      */   {
/* 1467 */     if ((FileUtils.storeInDB(this.m_dir)) && (!releaseAnyway))
/*      */     {
/* 1469 */       return;
/*      */     }
/*      */ 
/* 1472 */     if (SystemUtils.m_verbose)
/*      */     {
/* 1474 */       if (this.m_isReserved)
/*      */       {
/* 1476 */         Report.debug("filequeue", "meaningless release access on " + this.m_dir + " because file queue has been reserved long term", null);
/*      */       }
/*      */       else
/*      */       {
/* 1480 */         Report.debug("filequeue", "release access " + this.m_dir, null);
/*      */       }
/*      */     }
/* 1483 */     if (this.m_isReserved)
/*      */       return;
/*      */     try
/*      */     {
/* 1487 */       closeFile();
/*      */     }
/*      */     catch (IOException ignore)
/*      */     {
/* 1491 */       ignore.printStackTrace();
/*      */     }
/*      */     finally
/*      */     {
/* 1495 */       this.m_isPromoted = false;
/* 1496 */       FileUtils.releaseDirectory(this.m_dir);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void closeFile()
/*      */     throws IOException
/*      */   {
/* 1503 */     if (SystemUtils.m_verbose)
/*      */     {
/* 1505 */       String msg = (this.m_fileAccess != null) ? "close call for " : "skipping unnecessary close call for";
/* 1506 */       Report.debug("filequeue", msg + " " + this.m_queueName, null);
/*      */     }
/* 1508 */     clearCaches();
/* 1509 */     if (this.m_fileAccess == null)
/*      */       return;
/* 1511 */     this.m_fileAccess.close();
/* 1512 */     this.m_fileAccess = null;
/*      */   }
/*      */ 
/*      */   protected int ensureMinimumSpace(FixedFieldFormatter header, OutputStream out, int minReservedSpace)
/*      */     throws IOException
/*      */   {
/* 1527 */     int numWritten = 0;
/* 1528 */     boolean reAlloc = false;
/* 1529 */     if (header.m_data.length - header.m_offset < minReservedSpace)
/*      */     {
/* 1531 */       reAlloc = true;
/* 1532 */       if (header.m_offset > 0)
/*      */       {
/* 1534 */         out.write(header.m_data, 0, header.m_offset);
/* 1535 */         numWritten = header.m_offset;
/* 1536 */         header.m_offset = 0;
/* 1537 */         if (header.m_data.length >= minReservedSpace)
/*      */         {
/* 1539 */           reAlloc = false;
/*      */         }
/*      */       }
/*      */     }
/* 1543 */     if (reAlloc)
/*      */     {
/* 1545 */       header.m_data = new byte[minReservedSpace];
/*      */     }
/* 1547 */     return numWritten;
/*      */   }
/*      */ 
/*      */   protected boolean incrementHeaderIndex()
/*      */   {
/* 1552 */     if (this.m_loadedMsgHeaderIndex == this.m_header.m_endMessage)
/*      */     {
/* 1554 */       return false;
/*      */     }
/* 1556 */     this.m_loadedMsgHeaderIndex += 1;
/* 1557 */     if (this.m_loadedMsgHeaderIndex == this.m_header.m_maxMessages)
/*      */     {
/* 1559 */       this.m_loadedMsgHeaderIndex = 0;
/*      */     }
/* 1561 */     return true;
/*      */   }
/*      */ 
/*      */   protected void reserveCurrentMessage() throws IOException
/*      */   {
/* 1566 */     if (SystemUtils.m_verbose)
/*      */     {
/* 1568 */       Report.debug("filequeue", "Updating reservation on item: " + this.m_msgHeader.m_id, null);
/*      */     }
/* 1570 */     this.m_msgHeader.m_state = 2;
/* 1571 */     this.m_msgHeader.m_marker = computeMarker();
/* 1572 */     serializeMessageHeader(true);
/*      */   }
/*      */ 
/*      */   protected void serializeQueueHeader(boolean isWrite)
/*      */     throws IOException
/*      */   {
/* 1578 */     this.m_currentCacheIndex = 0;
/* 1579 */     if (!isWrite)
/*      */     {
/* 1581 */       this.m_headerFormatter.resetForRead();
/* 1582 */       serializeBytes(0L, this.m_headerFormatter.m_data, isWrite);
/*      */     }
/* 1584 */     this.m_headerFormatter.m_offset = 0;
/* 1585 */     this.m_header.serializeFixedLen(this.m_headerFormatter, isWrite);
/*      */ 
/* 1587 */     if (isWrite)
/*      */     {
/* 1589 */       serializeBytes(0L, this.m_headerFormatter.m_data, isWrite);
/*      */     }
/*      */     else
/*      */     {
/* 1594 */       this.m_startPointsOfCache = new long[] { 0L, this.m_header.m_offsetToData };
/* 1595 */       this.m_endPointsOfCache = new long[] { this.m_header.m_offsetToData, this.m_header.m_size };
/*      */     }
/* 1597 */     if (!SystemUtils.m_verbose)
/*      */       return;
/* 1599 */     String msg = null;
/* 1600 */     if (!isWrite)
/*      */     {
/* 1602 */       msg = "serializeQueueHeader(read)";
/*      */     }
/*      */     else
/*      */     {
/* 1606 */       msg = "serializeQueueHeader(write)";
/*      */     }
/* 1608 */     long lastModified = this.m_file.lastModified();
/* 1609 */     msg = msg + " lastModified=" + lastModified;
/* 1610 */     msg = msg + " startMessage=" + this.m_header.m_startMessage + " endMessage=" + this.m_header.m_endMessage + " usedMessages=" + this.m_header.m_usedMessages;
/*      */ 
/* 1612 */     Report.debug("filequeue", msg, null);
/*      */   }
/*      */ 
/*      */   protected void serializeMessageHeader(boolean isWrite)
/*      */     throws IOException
/*      */   {
/* 1619 */     if (this.m_loadedMsgHeaderIndex < 0)
/*      */     {
/* 1621 */       return;
/*      */     }
/*      */ 
/* 1624 */     this.m_currentCacheIndex = 0;
/* 1625 */     long pos = this.m_headerLen + this.m_loadedMsgHeaderIndex * this.m_msgHeaderLen;
/*      */ 
/* 1627 */     if (!isWrite)
/*      */     {
/* 1629 */       this.m_msgHeadFormatter.resetForRead();
/* 1630 */       serializeBytes(pos, this.m_msgHeadFormatter.m_data, isWrite);
/*      */     }
/* 1632 */     this.m_msgHeadFormatter.m_offset = 0;
/* 1633 */     this.m_msgHeader.serializeFixedLen(this.m_msgHeadFormatter, isWrite);
/* 1634 */     if (isWrite != true)
/*      */       return;
/* 1636 */     serializeBytes(pos, this.m_msgHeadFormatter.m_data, isWrite);
/*      */   }
/*      */ 
/*      */   protected void serializeBytes(long pos, byte[] data, boolean isWrite)
/*      */     throws IOException
/*      */   {
/* 1647 */     checkIsWriteAccessible();
/*      */ 
/* 1649 */     boolean inRange = false;
/* 1650 */     int adjOffset = 0;
/* 1651 */     byte[] buf = null;
/*      */ 
/* 1656 */     int cacheObjectIndex = -1;
/*      */ 
/* 1659 */     if ((this.m_currentByteRanges != null) && (this.m_startPointsOfCache != null) && (this.m_caches != null))
/*      */     {
/* 1662 */       for (int i = 0; i < 3; ++i)
/*      */       {
/* 1666 */         long[] byteRange = this.m_currentByteRanges[i];
/* 1667 */         if (byteRange == null)
/*      */           continue;
/* 1669 */         if ((byteRange[0] <= pos) && (pos + data.length < byteRange[1]) && (!inRange))
/*      */         {
/* 1671 */           buf = this.m_caches[i];
/*      */ 
/* 1674 */           adjOffset = (int)(pos - byteRange[0]);
/* 1675 */           cacheObjectIndex = i;
/* 1676 */           inRange = true;
/* 1677 */           if (isWrite)
/*      */             continue;
/* 1679 */           break;
/*      */         }
/*      */ 
/* 1682 */         if ((!isWrite) || 
/* 1684 */           (byteRange[0] > pos) || (pos >= byteRange[1])) {
/*      */           continue;
/*      */         }
/* 1687 */         this.m_currentByteRanges[i] = null;
/* 1688 */         break;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1694 */     long startByte = pos;
/* 1695 */     boolean readInBuffer = false;
/* 1696 */     int cacheSize = 0;
/* 1697 */     if ((!inRange) && (!isWrite))
/*      */     {
/* 1699 */       int[] cacheSizes = ((this.m_doLargeCaching) && (pos > 0L)) ? m_largeCacheSizes : m_smallCacheSizes;
/* 1700 */       cacheSize = cacheSizes[this.m_currentCacheIndex];
/* 1701 */       if (cacheSize >= 2 * data.length)
/*      */       {
/* 1706 */         long startRangeIndex = 0L;
/* 1707 */         long endRangeIndex = 0L;
/* 1708 */         if (this.m_startPointsOfCache != null)
/*      */         {
/* 1711 */           startRangeIndex = this.m_startPointsOfCache[this.m_currentCacheIndex];
/* 1712 */           endRangeIndex = this.m_endPointsOfCache[this.m_currentCacheIndex];
/*      */         }
/*      */         else
/*      */         {
/* 1716 */           endRangeIndex = this.m_fileAccess.length();
/*      */         }
/* 1718 */         if ((startByte >= startRangeIndex) && (startByte + data.length < endRangeIndex))
/*      */         {
/* 1720 */           adjOffset = 0;
/* 1721 */           if ((startByte > startRangeIndex) && (startByte < startRangeIndex + (cacheSize - data.length) / 2))
/*      */           {
/* 1725 */             startByte = startRangeIndex;
/* 1726 */             adjOffset = (int)(pos - startByte);
/*      */           }
/* 1728 */           readInBuffer = true;
/* 1729 */           if (cacheSize > endRangeIndex - startByte)
/*      */           {
/* 1732 */             cacheSize = (int)(endRangeIndex - startByte);
/*      */           }
/*      */         }
/*      */       }
/* 1736 */       if (readInBuffer)
/*      */       {
/* 1738 */         if (this.m_caches == null)
/*      */         {
/* 1740 */           this.m_caches = new byte[][] { null, null, null };
/*      */         }
/* 1742 */         if (startByte == 0L)
/*      */         {
/* 1745 */           cacheObjectIndex = 0;
/*      */         }
/*      */         else
/*      */         {
/* 1749 */           cacheObjectIndex = this.m_currentCacheIndex + 1;
/*      */         }
/* 1751 */         buf = this.m_caches[cacheObjectIndex];
/* 1752 */         if ((buf == null) || (buf.length < cacheSize))
/*      */         {
/* 1754 */           buf = new byte[cacheSize];
/* 1755 */           this.m_caches[cacheObjectIndex] = buf;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1760 */     if ((!inRange) || (readInBuffer) || (isWrite))
/*      */     {
/* 1762 */       this.m_fileAccess.seek(startByte);
/*      */     }
/* 1764 */     if (isWrite)
/*      */     {
/* 1766 */       this.m_fileAccess.write(data);
/* 1767 */       if (!inRange) {
/*      */         return;
/*      */       }
/* 1770 */       System.arraycopy(data, 0, buf, adjOffset, data.length);
/*      */     }
/* 1775 */     else if ((inRange) || (readInBuffer))
/*      */     {
/* 1777 */       if (readInBuffer)
/*      */       {
/* 1779 */         this.m_fileAccess.read(buf, 0, cacheSize);
/*      */ 
/* 1782 */         if (this.m_currentByteRanges == null)
/*      */         {
/* 1784 */           this.m_currentByteRanges = new long[][] { null, null, null };
/*      */         }
/* 1786 */         long[] ranges = this.m_currentByteRanges[cacheObjectIndex];
/* 1787 */         if (ranges == null)
/*      */         {
/* 1789 */           ranges = new long[] { 0L, 0L };
/* 1790 */           this.m_currentByteRanges[cacheObjectIndex] = ranges;
/*      */         }
/* 1792 */         ranges[0] = startByte;
/* 1793 */         ranges[1] = (startByte + cacheSize);
/*      */       }
/*      */ 
/* 1797 */       System.arraycopy(buf, adjOffset, data, 0, data.length);
/*      */     }
/*      */     else
/*      */     {
/* 1802 */       this.m_fileAccess.read(data);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected int computeMarker()
/*      */   {
/* 1810 */     Date dt = new Date();
/* 1811 */     long seconds = dt.getTime() / 1000L;
/* 1812 */     return (int)(seconds % 10000L);
/*      */   }
/*      */ 
/*      */   protected FileQueue createNewQueueInstance() throws IOException
/*      */   {
/* 1817 */     Class fileQueueClass = super.getClass();
/* 1818 */     FileQueue fileQueue = null;
/*      */     try
/*      */     {
/* 1821 */       fileQueue = (FileQueue)fileQueueClass.newInstance();
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1825 */       throw new IOException(LocaleUtils.encodeMessage("csFileQueueFailedInstantiation", e.getMessage(), fileQueueClass.getName()));
/*      */     }
/*      */ 
/* 1829 */     fileQueue.m_dir = this.m_dir;
/*      */ 
/* 1833 */     fileQueue.m_isPromoted = this.m_isPromoted;
/* 1834 */     return fileQueue;
/*      */   }
/*      */ 
/*      */   protected void setContentsFlag(boolean hasContents)
/*      */   {
/* 1839 */     String path = this.m_dir + this.m_queueName + "hasContents.dat";
/* 1840 */     if (hasContents)
/*      */     {
/* 1842 */       if (FileUtils.checkFile(path, true, true) == 0)
/*      */         return;
/* 1844 */       FileUtils.touchFile(path);
/*      */     }
/*      */     else
/*      */     {
/* 1849 */       FileUtils.deleteFile(path);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1856 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97521 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.FileQueue
 * JD-Core Version:    0.5.4
 */