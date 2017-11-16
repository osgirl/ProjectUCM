/*      */ package intradoc.io;
/*      */ 
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.lang.Appender;
/*      */ import java.io.Closeable;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.io.RandomAccessFile;
/*      */ import java.lang.reflect.Method;
/*      */ import java.nio.MappedByteBuffer;
/*      */ import java.nio.channels.FileChannel;
/*      */ import java.nio.channels.FileChannel.MapMode;
/*      */ import java.nio.channels.FileLock;
/*      */ import java.security.AccessController;
/*      */ import java.security.PrivilegedActionException;
/*      */ import java.security.PrivilegedExceptionAction;
/*      */ import java.util.Arrays;
/*      */ 
/*      */ public class MemoryMappedFile
/*      */   implements Appender, Closeable
/*      */ {
/*      */   public static final int BYTE_SIZE = 1;
/*      */   public static final int INT_SIZE = 4;
/*      */   public static final int LONG_SIZE = 8;
/*      */   public static final int CHAR_SIZE = 2;
/*      */   public static final int SHORT_SIZE = 2;
/*      */   public static final int MIN_EXTENT_SIZE = 1048576;
/*      */   public static final int MAX_EXTENT_SIZE = 1073741824;
/*      */   public final boolean m_isReadOnly;
/*      */   public final int m_extentSize;
/*      */   public final int m_extentShift;
/*      */   public MappedByteBuffer[] m_extents;
/*      */   public final File m_file;
/*      */   protected RandomAccessFile m_randomAccessFile;
/*      */   protected FileChannel m_channel;
/*      */   protected FileLock m_lock;
/*      */   public static boolean s_isUnmapSupported;
/*      */   protected static boolean s_wasUnmapChecked;
/*      */ 
/*      */   public MemoryMappedFile(String pathname, int extentSize)
/*      */   {
/*  109 */     this(new File(pathname), extentSize, false);
/*      */   }
/*      */ 
/*      */   public MemoryMappedFile(String pathname, int extentSize, boolean isReadOnly)
/*      */   {
/*  125 */     this(new File(pathname), extentSize, isReadOnly);
/*      */   }
/*      */ 
/*      */   public MemoryMappedFile(File file, int extentSize)
/*      */   {
/*  139 */     this(file, extentSize, false);
/*      */   }
/*      */ 
/*      */   public MemoryMappedFile(File file, int extentSize, boolean isReadOnly)
/*      */   {
/*  155 */     this.m_file = file;
/*  156 */     this.m_isReadOnly = isReadOnly;
/*  157 */     if (extentSize < 1048576)
/*      */     {
/*  159 */       extentSize = 1048576;
/*      */     }
/*      */ 
/*  163 */     int log2 = log2roundup(extentSize);
/*  164 */     this.m_extentSize = (1 << log2);
/*  165 */     this.m_extentShift = log2;
/*      */     try
/*      */     {
/*  168 */       map();
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  172 */       Report.error(null, "Unable to map", e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void close()
/*      */     throws IOException
/*      */   {
/*  185 */     unmap();
/*  186 */     unlock();
/*  187 */     this.m_channel = null;
/*  188 */     RandomAccessFile raf = this.m_randomAccessFile;
/*  189 */     if (raf == null)
/*      */       return;
/*  191 */     this.m_randomAccessFile = null;
/*  192 */     raf.close();
/*      */   }
/*      */ 
/*      */   public synchronized void extend()
/*      */     throws IOException
/*      */   {
/*  203 */     MappedByteBuffer[] oldExtents = this.m_extents;
/*  204 */     if (oldExtents == null)
/*      */     {
/*  206 */       throw new IOException(this.m_file + " is not mapped");
/*      */     }
/*  208 */     int oldExtentsLength = oldExtents.length;
/*  209 */     int numExtents = oldExtentsLength + 1;
/*  210 */     MappedByteBuffer[] newExtents = new MappedByteBuffer[numExtents];
/*  211 */     int extentShift = this.m_extentShift;
/*  212 */     long extentsLength = numExtents << extentShift;
/*  213 */     FileChannel channel = getOpenChannel();
/*  214 */     long fileLength = this.m_randomAccessFile.length();
/*  215 */     if (extentsLength > fileLength)
/*      */     {
/*  217 */       this.m_randomAccessFile.setLength(extentsLength);
/*      */     }
/*  219 */     long position = oldExtentsLength << extentShift;
/*  220 */     FileChannel.MapMode mode = (this.m_isReadOnly) ? FileChannel.MapMode.READ_ONLY : FileChannel.MapMode.READ_WRITE;
/*  221 */     newExtents[oldExtentsLength] = channel.map(mode, position, 1 << extentShift);
/*  222 */     System.arraycopy(oldExtents, 0, newExtents, 0, oldExtentsLength);
/*  223 */     this.m_extents = newExtents;
/*  224 */     Arrays.fill(oldExtents, null);
/*      */   }
/*      */ 
/*      */   public void flush(long position, long length)
/*      */     throws IOException
/*      */   {
/*  238 */     MappedByteBuffer[] extents = this.m_extents;
/*  239 */     if ((extents == null) || (this.m_isReadOnly))
/*      */     {
/*  241 */       return;
/*      */     }
/*  243 */     int extentShift = this.m_extentShift;
/*  244 */     int firstBufferIndex = (int)(position >>> extentShift);
/*  245 */     int lastBufferIndex = (int)(position + length >>> extentShift);
/*  246 */     for (int index = firstBufferIndex; index <= lastBufferIndex; ++index)
/*      */     {
/*  248 */       MappedByteBuffer extent = extents[index];
/*      */       try
/*      */       {
/*  252 */         extent.force();
/*      */       }
/*      */       catch (Throwable t)
/*      */       {
/*  256 */         if (t instanceof IOException)
/*      */         {
/*  258 */           throw ((IOException)t);
/*      */         }
/*  260 */         throw new RuntimeException(t);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void flushAll()
/*      */     throws IOException
/*      */   {
/*  272 */     MappedByteBuffer[] extents = this.m_extents;
/*  273 */     if (extents == null)
/*      */       return;
/*  275 */     long length = (extents.length << this.m_extentShift) - 1L;
/*  276 */     flush(0L, length);
/*      */   }
/*      */ 
/*      */   protected FileChannel getOpenChannel()
/*      */     throws IOException
/*      */   {
/*  289 */     RandomAccessFile raf = this.m_randomAccessFile;
/*  290 */     if (raf == null)
/*      */     {
/*  292 */       raf = this.m_randomAccessFile = new RandomAccessFile(this.m_file, (this.m_isReadOnly) ? "r" : "rw");
/*      */     }
/*  294 */     FileChannel channel = this.m_channel;
/*  295 */     if (channel == null)
/*      */     {
/*  297 */       channel = this.m_channel = raf.getChannel();
/*      */     }
/*  299 */     return channel;
/*      */   }
/*      */ 
/*      */   public long length()
/*      */   {
/*  309 */     long length = 0L;
/*      */     try
/*      */     {
/*  312 */       getOpenChannel();
/*  313 */       length = this.m_randomAccessFile.length();
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  318 */       String msg = LocaleUtils.encodeMessage("csFailedOperation", null, "Length", this.m_file.getAbsolutePath());
/*      */ 
/*  320 */       Report.error(null, msg, e);
/*      */     }
/*  322 */     return length;
/*      */   }
/*      */ 
/*      */   public void lock(boolean isShared)
/*      */     throws IOException
/*      */   {
/*  334 */     if (this.m_lock != null)
/*      */     {
/*  336 */       throw new IOException(this.m_file + " is already locked");
/*      */     }
/*  338 */     FileChannel channel = getOpenChannel();
/*  339 */     this.m_lock = channel.lock(0L, 9223372036854775807L, isShared);
/*      */   }
/*      */ 
/*      */   public synchronized void map()
/*      */     throws IOException
/*      */   {
/*  349 */     if (this.m_extents != null)
/*      */     {
/*  351 */       throw new IOException(this.m_file + " is already mapped");
/*      */     }
/*  353 */     int extentShift = this.m_extentShift;
/*  354 */     int extentSize = 1 << this.m_extentShift;
/*  355 */     FileChannel channel = getOpenChannel();
/*  356 */     long fileLength = this.m_randomAccessFile.length();
/*  357 */     int numExtents = (fileLength == 0L) ? 1 : (int)(fileLength - 1L >>> extentShift) + 1;
/*  358 */     long extentsLength = numExtents << extentShift;
/*  359 */     if (extentsLength > fileLength)
/*      */     {
/*  361 */       this.m_randomAccessFile.setLength(extentsLength);
/*      */     }
/*  363 */     FileChannel.MapMode mode = (this.m_isReadOnly) ? FileChannel.MapMode.READ_ONLY : FileChannel.MapMode.READ_WRITE;
/*  364 */     MappedByteBuffer[] extents = new MappedByteBuffer[numExtents];
/*      */     try
/*      */     {
/*  367 */       for (int e = 0; e < numExtents; ++e)
/*      */       {
/*  369 */         long position = e << extentShift;
/*  370 */         extents[e] = channel.map(mode, position, extentSize);
/*      */       }
/*      */     }
/*      */     catch (IOException ioe)
/*      */     {
/*  375 */       this.m_extents = extents;
/*      */       try
/*      */       {
/*  378 */         unmap();
/*      */       }
/*      */       catch (IOException ignore)
/*      */       {
/*      */       }
/*      */ 
/*  385 */       throw ioe;
/*      */     }
/*  387 */     this.m_extents = extents;
/*      */   }
/*      */ 
/*      */   public void unlock()
/*      */     throws IOException
/*      */   {
/*  397 */     FileLock lock = this.m_lock;
/*  398 */     if (lock == null)
/*      */     {
/*  400 */       return;
/*      */     }
/*  402 */     lock.release();
/*  403 */     this.m_lock = null;
/*      */   }
/*      */ 
/*      */   public void unmap()
/*      */     throws IOException
/*      */   {
/*  419 */     MappedByteBuffer[] extents = this.m_extents;
/*  420 */     if (extents == null)
/*      */     {
/*  422 */       return;
/*      */     }
/*  424 */     int numExtents = extents.length;
/*      */ 
/*  426 */     this.m_extents = null;
/*      */ 
/*  428 */     boolean isUnmapSupported = getOrComputeIsUnmapSupported();
/*  429 */     ExtentCleaner cleaner = (isUnmapSupported) ? new ExtentCleaner() : null;
/*  430 */     PrivilegedActionException firstPAE = null;
/*  431 */     for (int e = numExtents - 1; e >= 0; --e)
/*      */     {
/*  433 */       MappedByteBuffer extent = extents[e];
/*      */ 
/*  436 */       extents[e] = null;
/*  437 */       if (!isUnmapSupported)
/*      */         continue;
/*  439 */       cleaner.m_buffer = extent;
/*      */       try
/*      */       {
/*  442 */         AccessController.doPrivileged(cleaner);
/*      */       }
/*      */       catch (PrivilegedActionException pae)
/*      */       {
/*  448 */         if (firstPAE == null)
/*      */         {
/*  450 */           firstPAE = pae;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  455 */     if (firstPAE == null)
/*      */       return;
/*  457 */     IOException ioe = new IOException("unable to unmap " + this.m_file);
/*  458 */     ioe.initCause(firstPAE.getCause());
/*  459 */     throw ioe;
/*      */   }
/*      */ 
/*      */   public byte getByteAt(long position)
/*      */   {
/*  499 */     int extentShift = this.m_extentShift;
/*  500 */     int bufferIndex = (int)(position / this.m_extentSize);
/*  501 */     int bufferOffset = (int)(position % this.m_extentSize);
/*  502 */     if ((bufferIndex >= this.m_extents.length) || (bufferOffset >= this.m_extentSize))
/*      */     {
/*  504 */       return 0;
/*      */     }
/*  506 */     MappedByteBuffer extent = this.m_extents[bufferIndex];
/*  507 */     return extent.get(bufferOffset);
/*      */   }
/*      */ 
/*      */   public void getBytesAt(long position, byte[] bytes, int offset, int length)
/*      */   {
/*  524 */     int extentShift = this.m_extentShift;
/*  525 */     int extentSize = 1 << extentShift;
/*  526 */     int bufferIndex = (int)(position / this.m_extentSize);
/*  527 */     int bufferOffset = (int)(position % this.m_extentSize);
/*  528 */     MappedByteBuffer[] extents = this.m_extents;
/*  529 */     MappedByteBuffer extent = extents[bufferIndex];
/*  530 */     while (length-- > 0)
/*      */     {
/*  532 */       bytes[(offset++)] = extent.get(bufferOffset);
/*  533 */       ++bufferOffset;
/*  534 */       if (bufferOffset < extentSize)
/*      */         continue;
/*  536 */       extent = extents[(++bufferIndex)];
/*  537 */       bufferOffset = 0;
/*      */     }
/*      */   }
/*      */ 
/*      */   public char getCharAt(long position)
/*      */   {
/*  551 */     int extentShift = this.m_extentShift;
/*  552 */     int bufferIndex = (int)(position / this.m_extentSize);
/*  553 */     int bufferOffset = (int)(position % this.m_extentSize);
/*  554 */     MappedByteBuffer extent = this.m_extents[bufferIndex];
/*  555 */     if (bufferOffset + 2 > this.m_extentSize)
/*      */     {
/*  557 */       byte[] bytes = new byte[2];
/*  558 */       getBytesAt(position, bytes, 0, 2);
/*  559 */       return getChar(bytes);
/*      */     }
/*  561 */     return extent.getChar(bufferOffset);
/*      */   }
/*      */ 
/*      */   public void getCharsAt(long position, char[] chars, int offset, int length)
/*      */   {
/*  579 */     int extentShift = this.m_extentShift;
/*  580 */     int extentSize = 1 << extentShift;
/*  581 */     int bufferIndex = (int)(position / this.m_extentSize);
/*  582 */     int bufferOffset = (int)(position % this.m_extentSize);
/*  583 */     MappedByteBuffer[] extents = this.m_extents;
/*  584 */     MappedByteBuffer extent = extents[bufferIndex];
/*  585 */     while (length-- > 0)
/*      */     {
/*  587 */       if (bufferOffset + 2 > this.m_extentSize)
/*      */       {
/*  589 */         chars[(offset++)] = getCharAt(position);
/*      */       }
/*      */       else
/*      */       {
/*  593 */         chars[(offset++)] = extent.getChar(bufferOffset);
/*      */       }
/*  595 */       bufferOffset += 2;
/*  596 */       position += 2L;
/*  597 */       if (bufferOffset < extentSize)
/*      */         continue;
/*  599 */       extent = extents[(++bufferIndex)];
/*  600 */       bufferOffset -= extentSize;
/*      */     }
/*      */   }
/*      */ 
/*      */   public short getShortAt(long position)
/*      */   {
/*  614 */     int extentShift = this.m_extentShift;
/*  615 */     int bufferIndex = (int)(position / this.m_extentSize);
/*  616 */     int bufferOffset = (int)(position % this.m_extentSize);
/*  617 */     MappedByteBuffer extent = this.m_extents[bufferIndex];
/*  618 */     if (bufferOffset + 2 > this.m_extentSize)
/*      */     {
/*  620 */       byte[] bytes = new byte[2];
/*  621 */       getBytesAt(position, bytes, 0, 2);
/*  622 */       return getShort(bytes);
/*      */     }
/*  624 */     return extent.getShort(bufferOffset);
/*      */   }
/*      */ 
/*      */   public void getShortsAt(long position, short[] shorts, int offset, int length)
/*      */   {
/*  642 */     int extentShift = this.m_extentShift;
/*  643 */     int extentSize = 1 << extentShift;
/*  644 */     int bufferIndex = (int)(position / this.m_extentSize);
/*  645 */     int bufferOffset = (int)(position % this.m_extentSize);
/*  646 */     MappedByteBuffer[] extents = this.m_extents;
/*  647 */     MappedByteBuffer extent = extents[bufferIndex];
/*  648 */     while (length-- > 0)
/*      */     {
/*  650 */       if (bufferOffset + 2 > this.m_extentSize)
/*      */       {
/*  652 */         shorts[(offset++)] = getShortAt(position);
/*      */       }
/*      */       else
/*      */       {
/*  656 */         shorts[(offset++)] = extent.getShort(bufferOffset);
/*      */       }
/*  658 */       bufferOffset += 2;
/*  659 */       position += 2L;
/*  660 */       if (bufferOffset < extentSize)
/*      */         continue;
/*  662 */       extent = extents[(++bufferIndex)];
/*  663 */       bufferOffset -= extentSize;
/*      */     }
/*      */   }
/*      */ 
/*      */   public int getIntAt(long position)
/*      */   {
/*  677 */     int extentShift = this.m_extentShift;
/*  678 */     int bufferIndex = (int)(position / this.m_extentSize);
/*  679 */     int bufferOffset = (int)(position % this.m_extentSize);
/*  680 */     MappedByteBuffer extent = this.m_extents[bufferIndex];
/*  681 */     if (bufferOffset + 4 > this.m_extentSize)
/*      */     {
/*  683 */       byte[] bytes = new byte[4];
/*  684 */       getBytesAt(position, bytes, 0, 4);
/*  685 */       return getInt(bytes);
/*      */     }
/*  687 */     return extent.getInt(bufferOffset);
/*      */   }
/*      */ 
/*      */   public void getIntsAt(long position, int[] ints, int offset, int length)
/*      */   {
/*  705 */     int extentShift = this.m_extentShift;
/*  706 */     int extentSize = 1 << extentShift;
/*  707 */     int bufferIndex = (int)(position / this.m_extentSize);
/*  708 */     int bufferOffset = (int)(position % this.m_extentSize);
/*  709 */     MappedByteBuffer[] extents = this.m_extents;
/*  710 */     MappedByteBuffer extent = extents[bufferIndex];
/*  711 */     while (length-- > 0)
/*      */     {
/*  713 */       if (bufferOffset + 4 > this.m_extentSize)
/*      */       {
/*  715 */         ints[(offset++)] = getIntAt(position);
/*      */       }
/*      */       else
/*      */       {
/*  719 */         ints[(offset++)] = extent.getInt(bufferOffset);
/*      */       }
/*  721 */       bufferOffset += 4;
/*  722 */       position += 4L;
/*  723 */       if (bufferOffset < extentSize)
/*      */         continue;
/*  725 */       extent = extents[(++bufferIndex)];
/*  726 */       bufferOffset -= extentSize;
/*      */     }
/*      */   }
/*      */ 
/*      */   public long getLongAt(long position)
/*      */   {
/*  740 */     int extentShift = this.m_extentShift;
/*  741 */     int bufferIndex = (int)(position / this.m_extentSize);
/*  742 */     int bufferOffset = (int)(position % this.m_extentSize);
/*  743 */     MappedByteBuffer extent = this.m_extents[bufferIndex];
/*  744 */     if (bufferOffset + 8 > this.m_extentSize)
/*      */     {
/*  746 */       byte[] bytes = new byte[8];
/*  747 */       getBytesAt(position, bytes, 0, 8);
/*  748 */       return getLong(bytes);
/*      */     }
/*  750 */     return extent.getLong(bufferOffset);
/*      */   }
/*      */ 
/*      */   public void getLongsAt(long position, long[] longs, int offset, int length)
/*      */   {
/*  768 */     int extentShift = this.m_extentShift;
/*  769 */     int extentSize = 1 << extentShift;
/*  770 */     int bufferIndex = (int)(position / this.m_extentSize);
/*  771 */     int bufferOffset = (int)(position % this.m_extentSize);
/*  772 */     MappedByteBuffer[] extents = this.m_extents;
/*  773 */     MappedByteBuffer extent = extents[bufferIndex];
/*  774 */     while (length-- > 0)
/*      */     {
/*  776 */       if (bufferOffset + 8 > this.m_extentSize)
/*      */       {
/*  778 */         longs[(offset++)] = getLongAt(position);
/*      */       }
/*      */       else
/*      */       {
/*  782 */         longs[(offset++)] = extent.getLong(bufferOffset);
/*      */       }
/*  784 */       bufferOffset += 8;
/*  785 */       position += 8L;
/*  786 */       if (bufferOffset < extentSize)
/*      */         continue;
/*  788 */       extent = extents[(++bufferIndex)];
/*  789 */       bufferOffset -= extentSize;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setByteAt(long position, byte value)
/*      */   {
/*  804 */     if (this.m_isReadOnly)
/*      */     {
/*  806 */       return;
/*      */     }
/*  808 */     byte[] bytes = { value };
/*  809 */     setBytesAt(position, bytes, 0, 1);
/*      */   }
/*      */ 
/*      */   public void setBytesAt(long position, byte[] bytes, int offset, int length)
/*      */   {
/*  826 */     if (this.m_isReadOnly)
/*      */     {
/*  828 */       return;
/*      */     }
/*  830 */     int extentShift = this.m_extentShift;
/*  831 */     int extentSize = 1 << extentShift;
/*  832 */     int bufferIndex = (int)(position / this.m_extentSize);
/*  833 */     int bufferOffset = (int)(position % this.m_extentSize);
/*  834 */     if (bufferIndex >= this.m_extents.length)
/*      */     {
/*      */       try
/*      */       {
/*  839 */         extend();
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/*  843 */         Report.error(null, "Error extending file : Could not write", e);
/*      */       }
/*      */     }
/*  846 */     MappedByteBuffer extent = this.m_extents[bufferIndex];
/*  847 */     while (length-- > 0)
/*      */     {
/*  849 */       extent.put(bufferOffset, bytes[(offset++)]);
/*  850 */       ++bufferOffset;
/*  851 */       if (bufferOffset < extentSize)
/*      */         continue;
/*  853 */       ++bufferIndex;
/*  854 */       if (bufferIndex >= this.m_extents.length)
/*      */       {
/*      */         try
/*      */         {
/*  859 */           extend();
/*      */         }
/*      */         catch (IOException e)
/*      */         {
/*  863 */           Report.error(null, "Error extending file : Could not write", e);
/*      */         }
/*      */       }
/*  866 */       extent = this.m_extents[bufferIndex];
/*  867 */       bufferOffset = 0;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setCharAt(long position, char value)
/*      */   {
/*  882 */     if (this.m_isReadOnly)
/*      */     {
/*  884 */       return;
/*      */     }
/*  886 */     byte[] bytes = getBytes(value);
/*  887 */     setBytesAt(position, bytes, 0, 2);
/*      */   }
/*      */ 
/*      */   public void setCharsAt(long position, char[] chars, int offset, int length)
/*      */   {
/*  905 */     if (this.m_isReadOnly)
/*      */     {
/*  907 */       return;
/*      */     }
/*  909 */     while (length-- > 0)
/*      */     {
/*  911 */       setCharAt(position, chars[(offset++)]);
/*  912 */       position += 2L;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setShortAt(long position, short value)
/*      */   {
/*  926 */     if (this.m_isReadOnly)
/*      */     {
/*  928 */       return;
/*      */     }
/*  930 */     byte[] bytes = getBytes(value);
/*  931 */     setBytesAt(position, bytes, 0, 2);
/*      */   }
/*      */ 
/*      */   public void setShortsAt(long position, short[] shorts, int offset, int length)
/*      */   {
/*  949 */     if (this.m_isReadOnly)
/*      */     {
/*  951 */       return;
/*      */     }
/*  953 */     while (length-- > 0)
/*      */     {
/*  955 */       setShortAt(position, shorts[(offset++)]);
/*  956 */       position += 2L;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setIntAt(long position, int value)
/*      */   {
/*  970 */     if (this.m_isReadOnly)
/*      */     {
/*  972 */       return;
/*      */     }
/*  974 */     byte[] bytes = getBytes(value);
/*  975 */     setBytesAt(position, bytes, 0, 4);
/*      */   }
/*      */ 
/*      */   public void setIntsAt(long position, int[] ints, int offset, int length)
/*      */   {
/*  993 */     if (this.m_isReadOnly)
/*      */     {
/*  995 */       return;
/*      */     }
/*  997 */     while (length-- > 0)
/*      */     {
/*  999 */       setIntAt(position, ints[(offset++)]);
/* 1000 */       position += 4L;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setLongAt(long position, long value)
/*      */   {
/* 1014 */     if (this.m_isReadOnly)
/*      */     {
/* 1016 */       return;
/*      */     }
/* 1018 */     byte[] bytes = getBytes(value);
/* 1019 */     setBytesAt(position, bytes, 0, 8);
/*      */   }
/*      */ 
/*      */   public void setLongsAt(long position, long[] longs, int offset, int length)
/*      */   {
/* 1037 */     if (this.m_isReadOnly)
/*      */     {
/* 1039 */       return;
/*      */     }
/* 1041 */     while (length-- > 0)
/*      */     {
/* 1043 */       setLongAt(position, longs[(offset++)]);
/* 1044 */       position += 8L;
/*      */     }
/*      */   }
/*      */ 
/*      */   public String getAbsolutePath()
/*      */   {
/* 1050 */     return this.m_file.getAbsolutePath();
/*      */   }
/*      */ 
/*      */   public String getFileName()
/*      */   {
/* 1055 */     return this.m_file.getName();
/*      */   }
/*      */ 
/*      */   public String getParent()
/*      */   {
/* 1060 */     String path = getAbsolutePath();
/* 1061 */     int index = path.lastIndexOf('/');
/*      */ 
/* 1063 */     if (index == -1)
/*      */     {
/* 1065 */       index = path.lastIndexOf('\\');
/*      */     }
/* 1067 */     return path.substring(0, index);
/*      */   }
/*      */ 
/*      */   public void appendTo(StringBuilder sb)
/*      */   {
/* 1074 */     File file = this.m_file;
/* 1075 */     String filepath = (file != null) ? file.toString() : "null";
/* 1076 */     sb.append(filepath);
/*      */   }
/*      */ 
/*      */   public String toString()
/*      */   {
/* 1082 */     File file = this.m_file;
/* 1083 */     return (file != null) ? file.toString() : "null";
/*      */   }
/*      */ 
/*      */   protected static boolean getOrComputeIsUnmapSupported()
/*      */   {
/* 1091 */     boolean isUnmapSupported = s_isUnmapSupported;
/* 1092 */     if (s_wasUnmapChecked)
/*      */     {
/* 1094 */       return isUnmapSupported;
/*      */     }
/*      */     try
/*      */     {
/* 1098 */       Class.forName("sun.misc.Cleaner");
/* 1099 */       Class.forName("java.nio.DirectByteBuffer").getMethod("cleaner", new Class[0]);
/* 1100 */       isUnmapSupported = MemoryMappedFile.s_isUnmapSupported = 1;
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/*      */     }
/*      */ 
/* 1106 */     return isUnmapSupported;
/*      */   }
/*      */ 
/*      */   public static int log2roundup(int value)
/*      */   {
/* 1118 */     --value;
/* 1119 */     int log2 = 1;
/* 1120 */     while (value >>>= 1 != 0)
/*      */     {
/* 1122 */       ++log2;
/*      */     }
/* 1124 */     return log2;
/*      */   }
/*      */ 
/*      */   public static char getChar(byte[] bytes)
/*      */   {
/* 1132 */     return (char)(bytes[0] << 8 | bytes[1] & 0xFF);
/*      */   }
/*      */ 
/*      */   public static short getShort(byte[] bytes)
/*      */   {
/* 1137 */     return (short)(bytes[0] << 8 | bytes[1] & 0xFF);
/*      */   }
/*      */ 
/*      */   public static int getInt(byte[] bytes)
/*      */   {
/* 1142 */     return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | bytes[3] & 0xFF;
/*      */   }
/*      */ 
/*      */   public static long getLong(byte[] bytes)
/*      */   {
/* 1147 */     return bytes[0] << 56 | (bytes[1] & 0xFF) << 48 | (bytes[2] & 0xFF) << 40 | (bytes[3] & 0xFF) << 32 | (bytes[4] & 0xFF) << 24 | (bytes[5] & 0xFF) << 16 | (bytes[6] & 0xFF) << 8 | bytes[7] & 0xFF;
/*      */   }
/*      */ 
/*      */   public static byte[] getBytes(boolean val)
/*      */   {
/* 1158 */     byte[] bytes = new byte[1];
/* 1159 */     bytes[0] = (byte)((val) ? 1 : 0);
/* 1160 */     return bytes;
/*      */   }
/*      */ 
/*      */   public static byte[] getBytes(char val)
/*      */   {
/* 1165 */     byte[] bytes = new byte[2];
/* 1166 */     bytes[1] = (byte)val;
/* 1167 */     bytes[0] = (byte)(val >>> '\b');
/* 1168 */     return bytes;
/*      */   }
/*      */ 
/*      */   public static byte[] getBytes(short val)
/*      */   {
/* 1173 */     byte[] bytes = new byte[2];
/* 1174 */     bytes[1] = (byte)val;
/* 1175 */     bytes[0] = (byte)(val >>> 8);
/* 1176 */     return bytes;
/*      */   }
/*      */ 
/*      */   public static byte[] getBytes(int val)
/*      */   {
/* 1181 */     byte[] bytes = new byte[4];
/* 1182 */     bytes[3] = (byte)val;
/* 1183 */     bytes[2] = (byte)(val >>> 8);
/* 1184 */     bytes[1] = (byte)(val >>> 16);
/* 1185 */     bytes[0] = (byte)(val >>> 24);
/* 1186 */     return bytes;
/*      */   }
/*      */ 
/*      */   public static byte[] getBytes(long val)
/*      */   {
/* 1191 */     byte[] bytes = new byte[8];
/* 1192 */     bytes[7] = (byte)(int)val;
/* 1193 */     bytes[6] = (byte)(int)(val >>> 8);
/* 1194 */     bytes[5] = (byte)(int)(val >>> 16);
/* 1195 */     bytes[4] = (byte)(int)(val >>> 24);
/* 1196 */     bytes[3] = (byte)(int)(val >>> 32);
/* 1197 */     bytes[2] = (byte)(int)(val >>> 40);
/* 1198 */     bytes[1] = (byte)(int)(val >>> 48);
/* 1199 */     bytes[0] = (byte)(int)(val >>> 56);
/* 1200 */     return bytes;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1205 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98149 $";
/*      */   }
/*      */ 
/*      */   public class ExtentCleaner
/*      */     implements PrivilegedExceptionAction<Void>
/*      */   {
/*      */     public MappedByteBuffer m_buffer;
/*      */ 
/*      */     public ExtentCleaner()
/*      */     {
/*      */     }
/*      */ 
/*      */     public Void run()
/*      */       throws Exception
/*      */     {
/*  473 */       Class bufferClass = this.m_buffer.getClass();
/*  474 */       Method cleanerMethod = bufferClass.getMethod("cleaner", new Class[0]);
/*  475 */       cleanerMethod.setAccessible(true);
/*  476 */       Object cleaner = cleanerMethod.invoke(this.m_buffer, new Object[0]);
/*  477 */       if (cleaner != null)
/*      */       {
/*  479 */         Class cleanerClass = cleaner.getClass();
/*  480 */         Method cleanMethod = cleanerClass.getMethod("clean", new Class[0]);
/*  481 */         cleanMethod.invoke(cleaner, new Object[0]);
/*      */       }
/*  483 */       return null;
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.MemoryMappedFile
 * JD-Core Version:    0.5.4
 */