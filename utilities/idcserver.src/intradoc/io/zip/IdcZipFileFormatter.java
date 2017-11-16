/*      */ package intradoc.io.zip;
/*      */ 
/*      */ import intradoc.io.IdcByteConversionUtils;
/*      */ import intradoc.io.IdcByteHandler;
/*      */ import intradoc.io.IdcByteHandlerException;
/*      */ import intradoc.io.IdcRandomAccessByteArray;
/*      */ import intradoc.util.GenericTracingCallback;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcPool;
/*      */ import intradoc.util.MapUtils;
/*      */ import java.io.UnsupportedEncodingException;
/*      */ import java.util.Calendar;
/*      */ import java.util.Collection;
/*      */ import java.util.Iterator;
/*      */ import java.util.Map;
/*      */ 
/*      */ public class IdcZipFileFormatter
/*      */ {
/*      */   public static final int F_SKIP_SIGNATURE = 1;
/*      */   public static final int F_FOR_CENTRAL_DIRECTORY = 2;
/*      */   protected IdcPool<Calendar> m_calendars;
/*      */ 
/*      */   public IdcZipFileFormatter()
/*      */   {
/*  112 */     this.m_calendars = new IdcPool();
/*      */   }
/*      */ 
/*      */   public long dosTimeFromBytes(byte[] bytes, int offset)
/*      */   {
/*  126 */     int time = bytes[(offset++)] & 0xFF | (bytes[(offset++)] & 0xFF) << 8;
/*  127 */     int date = bytes[(offset++)] & 0xFF | (bytes[(offset++)] & 0xFF) << 8;
/*  128 */     int year = (date >> 9 & 0x7F) + 1980;
/*  129 */     int month = date >> 5 & 0xF;
/*  130 */     int day = date & 0x1F;
/*  131 */     int hour = time >> 11 & 0x1F;
/*  132 */     int minute = time >> 5 & 0x3F;
/*  133 */     int second = time << 1 & 0x3E;
/*  134 */     Calendar cal = (Calendar)this.m_calendars.get();
/*  135 */     if (null == cal)
/*      */     {
/*  137 */       cal = Calendar.getInstance();
/*      */     }
/*  139 */     cal.set(year, month - 1, day, hour, minute, second);
/*  140 */     long millis = cal.getTimeInMillis();
/*  141 */     this.m_calendars.put(cal);
/*  142 */     return millis;
/*      */   }
/*      */ 
/*      */   public void dosTimeToBytes(long unix, byte[] bytes, int offset)
/*      */   {
/*  154 */     Calendar cal = (Calendar)this.m_calendars.get();
/*  155 */     if (null == cal)
/*      */     {
/*  157 */       cal = Calendar.getInstance();
/*      */     }
/*  159 */     cal.setTimeInMillis(unix);
/*  160 */     int year = cal.get(1);
/*  161 */     int month = cal.get(2) + 1;
/*  162 */     int day = cal.get(5);
/*  163 */     int hour = cal.get(11);
/*  164 */     int minute = cal.get(12);
/*  165 */     int second = cal.get(13);
/*  166 */     this.m_calendars.put(cal);
/*  167 */     int date = (year - 1980 & 0x7F) << 9 | (month & 0xF) << 5 | day & 0x1F;
/*  168 */     int time = (hour & 0x1F) << 11 | (minute & 0x3F) << 5 | second >> 1 & 0x1F;
/*  169 */     bytes[(offset++)] = (byte)(time & 0xFF);
/*  170 */     bytes[(offset++)] = (byte)(time >> 8);
/*  171 */     bytes[(offset++)] = (byte)(date & 0xFF);
/*  172 */     bytes[(offset++)] = (byte)(date >> 8);
/*      */   }
/*      */ 
/*      */   public int locateCentralDirectory(IdcZipFile zip, int flags)
/*      */     throws IdcZipException
/*      */   {
/*  190 */     int length = 65557;
/*  191 */     if (length > zip.m_zipfileLength)
/*      */     {
/*  193 */       length = (int)zip.m_zipfileLength;
/*      */     }
/*  195 */     byte[] buffer = new byte[length];
/*      */     try
/*      */     {
/*  198 */       zip.m_bytes.readFrom(zip.m_zipfileLength - length, buffer, 0, length);
/*      */     }
/*      */     catch (IdcByteHandlerException e)
/*      */     {
/*  202 */       throw new IdcZipException(e, "syZipFormatCentralDirectoryMissing", new Object[0]);
/*      */     }
/*  204 */     byte[] header = { 80, 75, 5, 6 };
/*  205 */     int pos = length - 18;
/*  206 */     while (pos >= 3)
/*      */     {
/*  209 */       for (int hptr = 3; hptr >= 0; --hptr)
/*      */       {
/*  211 */         if (header[hptr] != buffer[(--pos)]) {
/*      */           break;
/*      */         }
/*      */       }
/*      */ 
/*  216 */       if (hptr < 0) {
/*      */         break;
/*      */       }
/*      */     }
/*      */ 
/*  221 */     if (pos < 1)
/*      */     {
/*  223 */       throw new IdcZipException("syZipFormatCentralDirectoryMissing", new Object[0]);
/*      */     }
/*  225 */     header = new byte[22];
/*  226 */     System.arraycopy(buffer, pos, header, 0, 22);
/*  227 */     int numEntries = header[10] & 0xFF | (header[11] & 0xFF) << 8;
/*  228 */     zip.m_centralDirectoryLength = (header[12] & 0xFF | (header[13] & 0xFF) << 8 | (header[14] & 0xFF) << 16 | (header[15] & 0xFF) << 24);
/*      */ 
/*  230 */     zip.m_centralDirectoryOffset = (header[16] & 0xFF | (header[17] & 0xFF) << 8 | (header[18] & 0xFF) << 16 | (header[19] & 0xFF) << 24);
/*      */ 
/*  232 */     int commentLen = header[20] & 0xFF | (header[21] & 0xFF) << 8;
/*  233 */     if ((zip.m_centralDirectoryLength < 0L) || (zip.m_centralDirectoryOffset + zip.m_centralDirectoryLength > zip.m_zipfileLength - length + pos))
/*      */     {
/*  236 */       throw new IdcZipException("syZipFormatCentralDirectoryOverlap", new Object[0]);
/*      */     }
/*  238 */     zip.m_comment = "";
/*  239 */     if ((commentLen > 0) && ((flags & 0x1) == 0))
/*      */     {
/*  241 */       if (22 + commentLen > length - pos)
/*      */       {
/*  243 */         throw new IdcZipException("syZipFormatFileCommentLengthBad", new Object[0]);
/*      */       }
/*      */       try
/*      */       {
/*  247 */         zip.m_comment = new String(buffer, pos + 22, commentLen, zip.m_encoding);
/*      */       }
/*      */       catch (UnsupportedEncodingException ignore)
/*      */       {
/*  251 */         IdcZipException e = new IdcZipException(ignore, "syZipFormatFileCommentIgnored", new Object[0]);
/*  252 */         zip.m_zipenv.m_trace.report(5, new Object[] { e });
/*      */       }
/*      */     }
/*  255 */     return numEntries;
/*      */   }
/*      */ 
/*      */   public IdcZipEntry loadEntryFromCentralDirectory(IdcZipHandler zip, int flags, int specialFlags)
/*      */     throws IdcZipException
/*      */   {
/*  277 */     boolean skipSignature = (specialFlags & 0x1) != 0;
/*  278 */     IdcZipEntry entry = new IdcZipEntry();
/*      */ 
/*  281 */     byte[] header = new byte[46];
/*  282 */     int headerLength = (skipSignature) ? 42 : 46;
/*      */     int numBytes;
/*      */     try
/*      */     {
/*  286 */       numBytes = zip.m_bytes.readNext(header, (skipSignature) ? 4 : 0, headerLength);
/*  287 */       if (numBytes < headerLength)
/*      */       {
/*  289 */         throw new IdcByteHandlerException('r', numBytes, headerLength);
/*      */       }
/*  291 */       if (!skipSignature)
/*      */       {
/*  293 */         if ((80 != header[0]) || (75 != header[1]))
/*      */         {
/*  295 */           throw new IdcZipException("syZipFormatHeaderBad", new Object[0]);
/*      */         }
/*  297 */         if ((1 != header[2]) || (2 != header[3]))
/*      */         {
/*  299 */           throw new IdcZipException("syZipFormatCentralDirectoryHeaderBad", new Object[0]);
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  305 */       throw new IdcZipException(e, "syZipFormatFileHeaderFailed", new Object[0]);
/*      */     }
/*  307 */     int filenameLen = header[28] & 0xFF | (header[29] & 0xFF) << 8;
/*  308 */     int extrafieldLen = header[30] & 0xFF | (header[31] & 0xFF) << 8;
/*  309 */     int commentLen = header[32] & 0xFF | (header[33] & 0xFF) << 8;
/*      */ 
/*  311 */     byte[] filenameBytes = new byte[filenameLen];
/*  312 */     if (filenameLen > 0)
/*      */     {
/*      */       try
/*      */       {
/*  316 */         numBytes = zip.m_bytes.readNext(filenameBytes, 0, filenameLen);
/*  317 */         if (numBytes < filenameLen)
/*      */         {
/*  319 */           throw new IdcByteHandlerException('r', numBytes, filenameLen);
/*      */         }
/*      */       }
/*      */       catch (IdcByteHandlerException e)
/*      */       {
/*  324 */         throw new IdcZipException(e, "syZipFormatFileHeaderFailed", new Object[0]);
/*      */       }
/*      */     }
/*  327 */     entry.m_extraFields = MapUtils.createSynchronizedMap(0);
/*  328 */     if (extrafieldLen > 0)
/*      */     {
/*  330 */       loadExtraFields(zip, entry, extrafieldLen);
/*      */     }
/*  332 */     entry.m_flags = (short)(header[8] & 0xFF | (header[9] & 0xFF) << 8);
/*      */ 
/*  334 */     entry.m_filename = determineStringEncoding(zip, entry, filenameBytes, 28789, "zip entry filename");
/*      */ 
/*  337 */     if (filenameLen + extrafieldLen + commentLen > 65535)
/*      */     {
/*  339 */       throw new IdcZipException("syZipFormatHeaderLengthBad", new Object[] { entry.m_filename });
/*      */     }
/*      */ 
/*  343 */     entry.m_versionCreated = header[4];
/*  344 */     entry.m_hostCreated = header[5];
/*  345 */     entry.m_versionRequired = header[6];
/*  346 */     entry.m_hostRequired = header[7];
/*  347 */     entry.m_compressionMethod = (short)(header[10] & 0xFF | (header[11] & 0xFF) << 8);
/*  348 */     entry.m_lastModified = dosTimeFromBytes(header, 12);
/*  349 */     entry.m_crc32 = (header[16] & 0xFF | (header[17] & 0xFF) << 8 | (header[18] & 0xFF) << 16 | (header[19] & 0xFF) << 24);
/*      */ 
/*  351 */     entry.m_sizeCompressed = (header[20] & 0xFF | (header[21] & 0xFF) << 8 | (header[22] & 0xFF) << 16 | (header[23] & 0xFF) << 24);
/*      */ 
/*  353 */     entry.m_sizeUncompressed = (header[24] & 0xFF | (header[25] & 0xFF) << 8 | (header[26] & 0xFF) << 16 | (header[27] & 0xFF) << 24);
/*      */ 
/*  355 */     entry.m_internalAttrs = (short)(header[36] & 0xFF | (header[37] & 0xFF) << 8);
/*  356 */     entry.m_externalAttrsMSDOS = header[38];
/*  357 */     entry.m_externalAttrsOther = header[39];
/*  358 */     entry.m_externalAttrsUnix = (short)(header[40] & 0xFF | (header[41] & 0xFF) << 8);
/*  359 */     entry.m_comment = "";
/*  360 */     entry.m_headerOffset = (header[42] & 0xFF | (header[43] & 0xFF) << 8 | (header[44] & 0xFF) << 16 | (header[45] & 0xFF) << 24);
/*      */ 
/*  362 */     if (commentLen > 0)
/*      */     {
/*  364 */       byte[] commentBytes = new byte[commentLen];
/*      */       try
/*      */       {
/*  367 */         numBytes = zip.m_bytes.readNext(commentBytes, 0, commentLen);
/*  368 */         if (numBytes < commentLen)
/*      */         {
/*  370 */           throw new IdcByteHandlerException('r', numBytes, commentLen);
/*      */         }
/*      */       }
/*      */       catch (IdcByteHandlerException e)
/*      */       {
/*  375 */         throw new IdcZipException(e, "syZipFormatEntryCommentIgnored", new Object[] { entry.m_filename });
/*      */       }
/*  377 */       if ((flags & 0x1) == 0)
/*      */       {
/*  379 */         entry.m_comment = determineStringEncoding(zip, entry, commentBytes, 25461, "zip entry comment");
/*      */       }
/*      */     }
/*      */ 
/*  383 */     switch (entry.m_hostCreated)
/*      */     {
/*      */     case 0:
/*      */     case 11:
/*  387 */       entry.m_isDirectory = ((entry.m_externalAttrsMSDOS & 0x10) != 0);
/*  388 */       entry.m_isExecutable = ((entry.m_externalAttrsMSDOS & 0x80) != 0);
/*  389 */       entry.m_isReadOnly = ((entry.m_externalAttrsMSDOS & 0x1) != 0);
/*  390 */       break;
/*      */     case 3:
/*  392 */       entry.m_isDirectory = ((entry.m_externalAttrsUnix & 0xFFFFF000) == 16384);
/*  393 */       entry.m_isExecutable = ((entry.m_externalAttrsUnix & 0x1) != 0);
/*  394 */       entry.m_isReadOnly = ((entry.m_externalAttrsUnix & 0x2) == 0);
/*      */     }
/*      */ 
/*  398 */     return entry;
/*      */   }
/*      */ 
/*      */   public void validateCentralDirectoryEntry(IdcZipHandler zip, IdcZipEntry entry)
/*      */     throws IdcZipException
/*      */   {
/*  411 */     switch (entry.m_hostCreated)
/*      */     {
/*      */     case 0:
/*      */     case 3:
/*      */     case 11:
/*  416 */       break;
/*      */     default:
/*  418 */       throw new IdcZipException("syZipFormatHostUnsupported", new Object[] { entry.m_filename, Byte.valueOf(entry.m_hostCreated) });
/*      */     }
/*  420 */     if (entry.m_versionRequired > 20)
/*      */     {
/*  422 */       throw new IdcZipException("syZipFormatVersionUnsupported", new Object[] { entry.m_filename, Double.valueOf(entry.m_versionRequired / 10.0D) });
/*      */     }
/*  424 */     if (0 != (entry.m_flags & 0x1))
/*      */     {
/*  426 */       throw new IdcZipException("syZipFormatEncryptionUnsupported", new Object[] { entry.m_filename });
/*      */     }
/*  428 */     switch (entry.m_compressionMethod)
/*      */     {
/*      */     case 0:
/*  431 */       if (entry.m_sizeCompressed != entry.m_sizeUncompressed)
/*      */       {
/*  433 */         throw new IdcZipException("syZipFormatMismatchStoreSize", new Object[] { entry.m_filename, Long.valueOf(entry.m_sizeCompressed), Long.valueOf(entry.m_sizeUncompressed) });
/*      */       }
/*      */     }
/*      */ 
/*  437 */     if ((null == zip.m_zipenv.m_compressors) || (!zip.m_zipenv.m_compressors.containsKey(new Short(entry.m_compressionMethod))))
/*      */     {
/*  440 */       throw new IdcZipException("syZipFormatCompressionMethodUnsupported", new Object[] { entry.m_filename, Short.valueOf(entry.m_compressionMethod) });
/*      */     }
/*      */ 
/*  443 */     if ((268435455L == entry.m_sizeCompressed) || (268435455L == entry.m_sizeUncompressed))
/*      */     {
/*  445 */       throw new IdcZipException("syZip64Unsupported", new Object[] { entry.m_filename });
/*      */     }
/*  447 */     if ((entry.m_sizeCompressed < 0L) || (entry.m_sizeUncompressed < 0L))
/*      */     {
/*  449 */       throw new IdcZipException("syZipFormatEntryLengthBad", new Object[] { entry.m_filename });
/*      */     }
/*  451 */     if ((entry.m_isDirectory) && (((entry.m_sizeCompressed > 0L) || (entry.m_sizeUncompressed > 0L))))
/*      */     {
/*  453 */       IdcZipException ignore = new IdcZipException("syZipFormatNonZeroDirectoryLength", new Object[] { entry.m_filename });
/*  454 */       zip.m_zipenv.m_trace.report(5, new Object[] { ignore });
/*  455 */       entry.m_isDirectory = false;
/*      */     }
/*  457 */     boolean endsWithSlash = entry.m_filename.endsWith("/");
/*  458 */     if (endsWithSlash == entry.m_isDirectory)
/*      */       return;
/*  460 */     GenericTracingCallback trace = zip.m_zipenv.m_trace;
/*  461 */     int verbosity = zip.m_zipenv.m_verbosity;
/*  462 */     String zipname = (zip.m_description != null) ? zip.m_description : "<unknown>";
/*  463 */     String filename = entry.m_filename;
/*  464 */     boolean hasSize = entry.m_sizeUncompressed != 0L;
/*  465 */     if (hasSize)
/*      */     {
/*  467 */       if (!endsWithSlash)
/*      */       {
/*  469 */         if (verbosity >= 5)
/*      */         {
/*  471 */           trace.report(5, new Object[] { zipname, ": directory entry \"", filename, "\" has content, treating as a plain entry." });
/*      */         }
/*      */ 
/*  474 */         entry.m_isDirectory = false;
/*      */       }
/*      */       else
/*      */       {
/*  478 */         if (verbosity >= 5)
/*      */         {
/*  480 */           trace.report(5, new Object[] { zipname, ": entry \"", filename, "\" has content, removing trailing slash." });
/*      */         }
/*      */ 
/*  483 */         entry.m_filename = filename.substring(0, filename.length() - 1);
/*      */       }
/*      */ 
/*      */     }
/*  488 */     else if (!endsWithSlash)
/*      */     {
/*  490 */       if (verbosity >= 5)
/*      */       {
/*  492 */         trace.report(5, new Object[] { zipname, ": directory entry \"", filename, "\" is missing trailing slash, added." });
/*      */       }
/*      */ 
/*  495 */       entry.m_filename = (filename + '/');
/*      */     }
/*      */     else
/*      */     {
/*  499 */       if (verbosity >= 6)
/*      */       {
/*  501 */         trace.report(6, new Object[] { zipname, ": entry \"", filename, "\" has trailing slash, treating as a directory." });
/*      */       }
/*      */ 
/*  504 */       entry.m_isDirectory = true;
/*      */     }
/*      */   }
/*      */ 
/*      */   public IdcZipEntry loadEntryFromLocalFileHeader(IdcZipHandler zip, int flags)
/*      */     throws IdcZipException
/*      */   {
/*  521 */     IdcZipEntry entry = new IdcZipEntry();
/*      */ 
/*  524 */     byte[] header = new byte[30];
/*      */     int numBytes;
/*      */     try
/*      */     {
/*  528 */       numBytes = zip.m_bytes.readNext(header, 0, 4);
/*  529 */       if (numBytes < 4)
/*      */       {
/*  531 */         throw new IdcByteHandlerException('r', numBytes, 4L);
/*      */       }
/*  533 */       if ((80 != header[0]) || (75 != header[1]))
/*      */       {
/*  535 */         throw new IdcZipException("syZipFormatHeaderBad", new Object[0]);
/*      */       }
/*  537 */       if ((1 == header[2]) && (2 == header[3]))
/*      */       {
/*  539 */         return null;
/*      */       }
/*  541 */       if ((3 != header[2]) || (4 != header[3]))
/*      */       {
/*  543 */         throw new IdcZipException("syZipFormatLocalHeaderBad", new Object[0]);
/*      */       }
/*  545 */       numBytes = zip.m_bytes.readNext(header, 4, 26);
/*  546 */       if (numBytes < 26)
/*      */       {
/*  548 */         throw new IdcByteHandlerException('r', numBytes, 26L);
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  553 */       throw new IdcZipException(e, "syZipFormatFileHeaderFailed", new Object[0]);
/*      */     }
/*  555 */     int filenameLen = header[26] & 0xFF | (header[27] & 0xFF) << 8;
/*  556 */     int extrafieldLen = header[28] & 0xFF | (header[29] & 0xFF) << 8;
/*      */ 
/*  558 */     byte[] filenameBytes = new byte[filenameLen];
/*  559 */     if (filenameLen > 0)
/*      */     {
/*      */       try
/*      */       {
/*  563 */         numBytes = zip.m_bytes.readNext(filenameBytes, 0, filenameLen);
/*  564 */         if (numBytes < filenameLen)
/*      */         {
/*  566 */           throw new IdcByteHandlerException('r', numBytes, filenameLen);
/*      */         }
/*      */       }
/*      */       catch (IdcByteHandlerException e)
/*      */       {
/*  571 */         throw new IdcZipException(e, "syZipFormatFileHeaderFailed", new Object[0]);
/*      */       }
/*      */     }
/*  574 */     entry.m_extraFields = MapUtils.createSynchronizedMap(0);
/*  575 */     if (extrafieldLen > 0)
/*      */     {
/*  577 */       loadExtraFields(zip, entry, extrafieldLen);
/*      */     }
/*  579 */     entry.m_flags = (short)(header[6] & 0xFF | (header[7] & 0xFF) << 8);
/*      */ 
/*  581 */     entry.m_filename = determineStringEncoding(zip, entry, filenameBytes, 28789, "zip entry filename");
/*      */ 
/*  584 */     if (filenameLen + extrafieldLen > 65535)
/*      */     {
/*  586 */       throw new IdcZipException("syZipFormatHeaderLengthBad", new Object[] { entry.m_filename });
/*      */     }
/*      */ 
/*  590 */     entry.m_dataOffset = zip.m_bytes.getPosition();
/*      */ 
/*  593 */     entry.m_versionRequired = header[4];
/*  594 */     entry.m_hostRequired = header[5];
/*  595 */     entry.m_compressionMethod = (short)(header[8] & 0xFF | (header[9] & 0xFF) << 8);
/*  596 */     entry.m_crc32 = (header[14] & 0xFF | (header[15] & 0xFF) << 8 | (header[16] & 0xFF) << 16 | (header[17] & 0xFF) << 24);
/*      */ 
/*  598 */     entry.m_sizeCompressed = (header[18] & 0xFF | (header[19] & 0xFF) << 8 | (header[20] & 0xFF) << 16 | (header[21] & 0xFF) << 24);
/*      */ 
/*  600 */     entry.m_sizeUncompressed = (header[22] & 0xFF | (header[23] & 0xFF) << 8 | (header[24] & 0xFF) << 16 | (header[25] & 0xFF) << 24);
/*      */ 
/*  602 */     return entry;
/*      */   }
/*      */ 
/*      */   protected void loadExtraFields(IdcZipHandler zip, IdcZipEntry entry, int extrafieldLen)
/*      */     throws IdcZipException
/*      */   {
/*  614 */     entry.m_hasExtraFields = true;
/*  615 */     int extrafieldOffset = 0;
/*  616 */     while (extrafieldOffset < extrafieldLen)
/*      */     {
/*  618 */       if (extrafieldOffset + 4 > extrafieldLen)
/*      */       {
/*  620 */         throw new IdcZipException("syZipFormatExtraFieldsLengthBad", new Object[0]);
/*      */       }
/*  622 */       byte[] extrafieldHeaderBytes = new byte[4];
/*      */       int numBytes;
/*      */       try
/*      */       {
/*  626 */         numBytes = zip.m_bytes.readNext(extrafieldHeaderBytes, 0, 4);
/*  627 */         if (numBytes < 4)
/*      */         {
/*  629 */           throw new IdcByteHandlerException('r', numBytes, 4L);
/*      */         }
/*      */       }
/*      */       catch (IdcByteHandlerException e)
/*      */       {
/*  634 */         throw new IdcZipException(e, "syZipFormatExtraFieldFailed", new Object[] { entry.m_filename });
/*      */       }
/*  636 */       short fieldID = (short)(extrafieldHeaderBytes[0] & 0xFF | (extrafieldHeaderBytes[1] & 0xFF) << 8);
/*  637 */       IdcZipEntryExtraField field = new IdcZipEntryExtraField(fieldID);
/*  638 */       field.m_length = (extrafieldHeaderBytes[2] & 0xFF | (extrafieldHeaderBytes[3] & 0xFF) << 8);
/*  639 */       if (extrafieldOffset + 4 + field.m_length > extrafieldLen)
/*      */       {
/*  641 */         throw new IdcZipException("syZipFormatExtraFieldLengthBad", new Object[] { entry.m_filename, Short.valueOf(fieldID) });
/*      */       }
/*  643 */       field.m_data = new byte[field.m_length];
/*      */       try
/*      */       {
/*  646 */         numBytes = zip.m_bytes.readNext(field.m_data, 0, field.m_length);
/*  647 */         if (numBytes < field.m_length)
/*      */         {
/*  649 */           throw new IdcByteHandlerException('r', numBytes, field.m_length);
/*      */         }
/*      */       }
/*      */       catch (IdcByteHandlerException e)
/*      */       {
/*  654 */         throw new IdcZipException(e, "syZipFormatExtraFieldFailed", new Object[] { entry.m_filename });
/*      */       }
/*  656 */       extrafieldOffset += 4 + field.m_length;
/*  657 */       entry.m_extraFields.put(new Integer(fieldID), field);
/*  658 */       switch (field.m_id)
/*      */       {
/*      */       case 1:
/*      */       case 8:
/*      */       case 25461:
/*      */       case 28789:
/*  664 */         field.m_isReadOnly = true;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void loadDataDescriptor(IdcZipHandler zip, IdcZipEntry entry)
/*      */     throws IdcZipException
/*      */   {
/*  679 */     byte[] desc = new byte[16];
/*      */     try
/*      */     {
/*  682 */       int numBytes = zip.m_bytes.readNext(desc, 0, 4);
/*  683 */       if (numBytes < 4)
/*      */       {
/*  685 */         throw new IdcByteHandlerException('r', numBytes, 4L);
/*      */       }
/*  687 */       if ((80 != desc[0]) || (75 != desc[1]))
/*      */       {
/*  689 */         throw new IdcZipException("syZipFormatHeaderBad", new Object[0]);
/*      */       }
/*  691 */       if ((7 == desc[2]) && (8 == desc[3]))
/*      */       {
/*  693 */         numBytes = zip.m_bytes.readNext(desc, 4, 12);
/*  694 */         if (numBytes < 12)
/*      */         {
/*  696 */           throw new IdcByteHandlerException('r', numBytes, 12L);
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  702 */       throw new IdcZipException(e, "syZipFormatDataDescriptorFailed", new Object[] { entry.m_filename });
/*      */     }
/*  704 */     if ((7 != desc[2]) || (8 != desc[3]))
/*      */     {
/*  706 */       throw new IdcZipException("syZipFormatDataDescriptorMissing", new Object[] { entry.m_filename });
/*      */     }
/*  708 */     entry.m_crc32 = (desc[4] & 0xFF | (desc[5] & 0xFF) << 8 | (desc[6] & 0xFF) << 16 | (desc[7] & 0xFF) << 24);
/*      */ 
/*  710 */     entry.m_sizeCompressed = (desc[8] & 0xFF | (desc[9] & 0xFF) << 8 | (desc[10] & 0xFF) << 16 | (desc[11] & 0xFF) << 24);
/*      */ 
/*  712 */     entry.m_sizeUncompressed = (desc[12] & 0xFF | (desc[13] & 0xFF) << 8 | (desc[14] & 0xFF) << 16 | (desc[15] & 0xFF) << 24);
/*      */   }
/*      */ 
/*      */   public void validateAndMergeLocalHeaderIntoCentralDirectoryEntry(IdcZipEntry local, IdcZipEntry central)
/*      */     throws IdcZipException
/*      */   {
/*  727 */     IdcMessage msg = null;
/*  728 */     if (!central.m_filename.equals(local.m_filename))
/*      */     {
/*  730 */       msg = new IdcMessage("syZipFormatMismatchFilename", new Object[] { central.m_filename, local.m_filename });
/*      */     }
/*  732 */     else if (central.m_versionRequired != local.m_versionRequired)
/*      */     {
/*  734 */       msg = new IdcMessage("syZipFormatMismatchVersion", new Object[] { Double.valueOf(central.m_versionRequired * 10.0D), Double.valueOf(local.m_versionRequired * 10.0D) });
/*      */     }
/*  737 */     else if (central.m_flags != local.m_flags)
/*      */     {
/*  739 */       msg = new IdcMessage("syZipFormatMismatchFlags", new Object[] { Short.valueOf(central.m_flags), Short.valueOf(local.m_flags) });
/*      */     }
/*  741 */     else if (central.m_compressionMethod != local.m_compressionMethod)
/*      */     {
/*  743 */       msg = new IdcMessage("syZipFormatMismatchCompressionMethod", new Object[] { Short.valueOf(central.m_compressionMethod), Short.valueOf(local.m_compressionMethod) });
/*      */     }
/*  746 */     else if (central.m_crc32 != local.m_crc32)
/*      */     {
/*  748 */       msg = new IdcMessage("syZipFormatMismatchCRC", new Object[] { Integer.valueOf(central.m_crc32), Integer.valueOf(local.m_crc32) });
/*      */     }
/*  750 */     else if (central.m_sizeCompressed != local.m_sizeCompressed)
/*      */     {
/*  752 */       msg = new IdcMessage("syZipFormatMismatchSizeCompressed", new Object[] { Long.valueOf(central.m_sizeCompressed), Long.valueOf(local.m_sizeCompressed) });
/*      */     }
/*  755 */     else if (central.m_sizeUncompressed != local.m_sizeUncompressed)
/*      */     {
/*  757 */       msg = new IdcMessage("syZipFormatMismatchSizeUncompressed", new Object[] { Long.valueOf(central.m_sizeUncompressed), Long.valueOf(local.m_sizeUncompressed) });
/*      */     }
/*      */ 
/*  760 */     if (null != msg)
/*      */     {
/*  762 */       IdcMessage general = new IdcMessage("syZipFormatEntryBad", new Object[] { central.m_filename });
/*  763 */       general.m_prior = msg;
/*  764 */       throw new IdcZipException(general);
/*      */     }
/*  766 */     Iterator iter = local.m_extraFields.values().iterator();
/*  767 */     while (iter.hasNext())
/*      */     {
/*  769 */       IdcZipEntryExtraField field = (IdcZipEntryExtraField)iter.next();
/*  770 */       Integer fieldID = new Integer(field.m_id);
/*  771 */       if (!central.m_extraFields.containsKey(fieldID))
/*      */       {
/*  773 */         central.m_extraFields.put(fieldID, field);
/*      */       }
/*      */     }
/*  776 */     central.m_dataOffset = local.m_dataOffset;
/*      */   }
/*      */ 
/*      */   protected String determineStringEncoding(IdcZipHandler zip, IdcZipEntry entry, byte[] bytes, short extraField, String message)
/*      */   {
/*  794 */     char[] chars = new char[65535];
/*      */ 
/*  798 */     if ((entry.m_flags & 0x800) != 0);
/*      */     int numChars;
/*      */     try
/*      */     {
/*  802 */       numChars = IdcByteConversionUtils.parseUTF8(bytes, 0, bytes.length, chars, 0);
/*  803 */       return new String(chars, 0, numChars);
/*      */     }
/*      */     catch (IdcByteHandlerException UTF8Field)
/*      */     {
/*  807 */       IdcZipException z = new IdcZipException(e, "syZipEntryUsesBadUTF8", new Object[0]);
/*  808 */       zip.m_zipenv.m_trace.report(6, new Object[] { z });
/*      */ 
/*  812 */       Integer UTF8Field = new Integer(extraField);
/*  813 */       IdcZipEntryExtraField field = (IdcZipEntryExtraField)entry.m_extraFields.get(UTF8Field);
/*  814 */       if (null != field);
/*      */       try
/*      */       {
/*  818 */         numChars = IdcByteConversionUtils.parseUTF8(field.m_data, 0, field.m_data.length, chars, 0);
/*  819 */         return new String(chars, 0, numChars);
/*      */       }
/*      */       catch (IdcByteHandlerException is7BitClean)
/*      */       {
/*  823 */         IdcZipException z = new IdcZipException(e, "syZipEntryHasBadUTF8Field", new Object[] { UTF8Field });
/*  824 */         zip.m_zipenv.m_trace.report(5, new Object[] { z });
/*      */ 
/*  828 */         UTF8Field = new Integer(8);
/*  829 */         field = (IdcZipEntryExtraField)entry.m_extraFields.get(UTF8Field);
/*  830 */         if (null != field)
/*      */         {
/*      */           try
/*      */           {
/*  834 */             numChars = IdcByteConversionUtils.parseUTF8(field.m_data, 0, field.m_data.length, chars, 0);
/*  835 */             String encoding = new String(chars, 0, numChars);
/*  836 */             return new String(bytes, 0, bytes.length, encoding);
/*      */           }
/*      */           catch (IdcByteHandlerException e)
/*      */           {
/*  840 */             IdcZipException z = new IdcZipException(e, "syZipEntryHasBadLangField", new Object[0]);
/*  841 */             zip.m_zipenv.m_trace.report(5, new Object[] { z });
/*      */           }
/*      */           catch (UnsupportedEncodingException e)
/*      */           {
/*  845 */             zip.m_zipenv.m_trace.report(5, new Object[] { e });
/*      */           }
/*      */         }
/*  848 */         boolean is7BitClean = true;
/*  849 */         for (int i = 0; i < bytes.length; ++i)
/*      */         {
/*  851 */           if ((bytes[i] >= 32) && (bytes[i] < 127))
/*      */             continue;
/*  853 */           is7BitClean = false;
/*  854 */           break;
/*      */         }
/*      */         byte[] urlEscaped;
/*      */         int urlLength;
/*  857 */         if (is7BitClean)
/*      */         {
/*  860 */           urlEscaped = new byte[bytes.length];
/*  861 */           urlLength = 0; int i = 0;
/*  862 */           while (i < bytes.length)
/*      */           {
/*  864 */             if (37 != bytes[i])
/*      */             {
/*  866 */               urlEscaped[(urlLength++)] = bytes[(i++)];
/*      */             }
/*      */ 
/*  869 */             if (++i + 2 > bytes.length)
/*      */             {
/*  871 */               IdcMessage msg = new IdcMessage("syZipURLDecodeMissing", new Object[0]);
/*  872 */               zip.m_zipenv.m_trace.report(4, new Object[] { msg });
/*  873 */               break;
/*      */             }
/*  875 */             byte hex1 = IdcByteConversionUtils.parseHexDigit((char)bytes[(i++)]);
/*  876 */             byte hex2 = IdcByteConversionUtils.parseHexDigit((char)bytes[(i++)]);
/*  877 */             if ((hex1 < 0) || (hex2 < 0))
/*      */             {
/*  879 */               IdcMessage msg = new IdcMessage("syZipURLDecodeBad", new Object[0]);
/*  880 */               zip.m_zipenv.m_trace.report(4, new Object[] { msg });
/*  881 */               break;
/*      */             }
/*  883 */             urlEscaped[(urlLength++)] = (byte)(hex1 << 8 | hex2);
/*      */           }
/*      */         }
/*      */         try {
/*  887 */           numChars = IdcByteConversionUtils.parseUTF8(urlEscaped, 0, urlLength, chars, 0);
/*  888 */           return new String(chars, 0, numChars);
/*      */         }
/*      */         catch (IdcByteHandlerException ignore)
/*      */         {
/*      */           try
/*      */           {
/*  900 */             numChars = IdcByteConversionUtils.parseUTF8(bytes, 0, bytes.length, chars, 0);
/*  901 */             return new String(chars, 0, numChars);
/*      */           }
/*      */           catch (IdcByteHandlerException ignore)
/*      */           {
/*  909 */             numChars = IdcByteConversionUtils.decodeIBM437(bytes, 0, bytes.length, chars, 0);
/*      */           }
/*      */         }
/*      */       }
/*  910 */     }return new String(chars, 0, numChars);
/*      */   }
/*      */ 
/*      */   public void updateEntryForWrite(IdcZipEntry entry, int flags)
/*      */     throws IdcZipException
/*      */   {
/*  935 */     if ((flags & 0x100) == 0)
/*      */     {
/*      */       IdcZipEntry tmp9_8 = entry; tmp9_8.m_externalAttrsMSDOS = (byte)(tmp9_8.m_externalAttrsMSDOS & 0xFFFFFF6E);
/*  938 */       if (entry.m_isDirectory)
/*      */       {
/*      */         IdcZipEntry tmp29_28 = entry; tmp29_28.m_externalAttrsMSDOS = (byte)(tmp29_28.m_externalAttrsMSDOS | 0x10);
/*      */       }
/*  942 */       if (entry.m_isExecutable)
/*      */       {
/*      */         IdcZipEntry tmp48_47 = entry; tmp48_47.m_externalAttrsMSDOS = (byte)(tmp48_47.m_externalAttrsMSDOS | 0x80);
/*      */       }
/*  946 */       if (entry.m_isReadOnly)
/*      */       {
/*      */         IdcZipEntry tmp68_67 = entry; tmp68_67.m_externalAttrsMSDOS = (byte)(tmp68_67.m_externalAttrsMSDOS | 0x1);
/*      */       }
/*      */     }
/*  951 */     if ((flags & 0x200) == 0)
/*      */     {
/*      */       IdcZipEntry tmp87_86 = entry; tmp87_86.m_externalAttrsUnix = (short)(tmp87_86.m_externalAttrsUnix & 0xFFF);
/*      */       IdcZipEntry tmp100_99 = entry; tmp100_99.m_externalAttrsUnix = (short)(tmp100_99.m_externalAttrsUnix | ((entry.m_isDirectory) ? 16384 : -32768));
/*      */       IdcZipEntry tmp126_125 = entry; tmp126_125.m_externalAttrsUnix = (short)(tmp126_125.m_externalAttrsUnix | 0x124);
/*  956 */       if ((entry.m_isDirectory) || (entry.m_isExecutable))
/*      */       {
/*      */         IdcZipEntry tmp153_152 = entry; tmp153_152.m_externalAttrsUnix = (short)(tmp153_152.m_externalAttrsUnix | 0x49);
/*      */       }
/*  960 */       if (!entry.m_isReadOnly)
/*      */       {
/*      */         IdcZipEntry tmp172_171 = entry; tmp172_171.m_externalAttrsUnix = (short)(tmp172_171.m_externalAttrsUnix | 0x92);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  967 */     IdcMessage msg = new IdcMessage("syZipFormatEntryNotUpdated", new Object[0]);
/*      */ 
/*  970 */     IdcRandomAccessByteArray bytes = new IdcRandomAccessByteArray(entry.m_filename.length() * 3);
/*      */ 
/*  972 */     if ((flags & 0x1000) == 0)
/*      */     {
/*      */       int numBytes;
/*      */       try {
/*  976 */         numBytes = IdcByteConversionUtils.formatUTF8String(entry.m_filename, bytes);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  980 */         throw new IdcZipException(e, msg);
/*      */       }
/*  982 */       if (numBytes > 65535)
/*      */       {
/*  984 */         msg.m_prior = new IdcMessage("syZipFormatExtraFieldLengthBad", new Object[] { entry.m_filename, Short.valueOf(28789) });
/*      */ 
/*  986 */         throw new IdcZipException(msg);
/*      */       }
/*  988 */       IdcZipEntryExtraField field = new IdcZipEntryExtraField(28789, bytes.m_bytes, numBytes);
/*  989 */       field.m_length = numBytes;
/*  990 */       field.m_isReadOnly = true;
/*  991 */       entry.setField(field);
/*      */     }
/*      */ 
/*  995 */     if ((null != entry.m_encoding) && (!entry.m_encoding.equals("UTF-8")) && ((flags & 0x2000) == 0))
/*      */     {
/*  998 */       bytes.m_position = 0;
/*      */       int numBytes;
/*      */       try
/*      */       {
/* 1001 */         numBytes = IdcByteConversionUtils.formatUTF8String(entry.m_encoding, bytes);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1005 */         throw new IdcZipException(e, msg);
/*      */       }
/* 1007 */       if (numBytes > 65535)
/*      */       {
/* 1009 */         msg.m_prior = new IdcMessage("syZipFormatExtraFieldLengthBad", new Object[] { entry.m_filename, Short.valueOf(8) });
/*      */ 
/* 1011 */         throw new IdcZipException(msg);
/*      */       }
/* 1013 */       IdcZipEntryExtraField field = new IdcZipEntryExtraField(8, bytes.m_bytes, numBytes);
/* 1014 */       field.m_length = numBytes;
/* 1015 */       field.m_isReadOnly = true;
/* 1016 */       entry.setField(field);
/*      */     }
/*      */ 
/* 1020 */     if ((null == entry.m_comment) || (entry.m_comment.length() <= 0) || ((flags & 0x1) != 0) || ((flags & 0x4000) != 0)) {
/*      */       return;
/*      */     }
/*      */ 
/* 1024 */     bytes.m_position = 0;
/*      */     int numBytes;
/*      */     try {
/* 1027 */       numBytes = IdcByteConversionUtils.formatUTF8String(entry.m_comment, bytes);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1031 */       throw new IdcZipException(e, msg);
/*      */     }
/* 1033 */     if (numBytes > 65535)
/*      */     {
/* 1035 */       msg.m_prior = new IdcMessage("syZipFormatExtraFieldLengthBad", new Object[] { entry.m_filename, Short.valueOf(25461) });
/*      */ 
/* 1037 */       throw new IdcZipException(msg);
/*      */     }
/* 1039 */     IdcZipEntryExtraField field = new IdcZipEntryExtraField(25461, bytes.m_bytes, numBytes);
/* 1040 */     field.m_length = numBytes;
/* 1041 */     field.m_isReadOnly = true;
/* 1042 */     entry.setField(field);
/*      */   }
/*      */ 
/*      */   public byte[] makeEntryHeader(IdcZipEntry entry, int flags, int specialFlags)
/*      */     throws IdcZipException
/*      */   {
/* 1060 */     boolean ignoreComment = (flags & 0x1) != 0;
/* 1061 */     boolean forCentralDir = (specialFlags & 0x2) != 0;
/*      */ 
/* 1063 */     int length = (forCentralDir) ? 46 : 30;
/* 1064 */     String encoding = (null != entry.m_encoding) ? entry.m_encoding : "UTF-8";
/*      */     byte[] filenameBytes;
/*      */     try
/*      */     {
/* 1068 */       filenameBytes = entry.m_filename.getBytes(encoding);
/*      */     }
/*      */     catch (UnsupportedEncodingException e)
/*      */     {
/* 1072 */       throw new IdcZipException(e, "syZipFormatFilenameEncodeFailed", new Object[] { entry.m_filename });
/*      */     }
/* 1074 */     if (filenameBytes.length > 65535)
/*      */     {
/* 1076 */       IdcMessage msg = new IdcMessage("syZipFormatFilenameEncodeFailed", new Object[] { entry.m_filename });
/* 1077 */       msg.m_prior = new IdcMessage("syByteLengthBad", new Object[] { Integer.valueOf(filenameBytes.length) });
/* 1078 */       throw new IdcZipException(msg);
/*      */     }
/* 1080 */     length += filenameBytes.length;
/*      */ 
/* 1082 */     int extrafieldLen = 0; int extrafieldNum = entry.m_extraFields.size();
/* 1083 */     IdcZipEntryExtraField[] extraFields = new IdcZipEntryExtraField[extrafieldNum];
/* 1084 */     Iterator iter = entry.m_extraFields.values().iterator();
/* 1085 */     int index = 0;
/* 1086 */     while (iter.hasNext())
/*      */     {
/* 1088 */       IdcZipEntryExtraField field = (IdcZipEntryExtraField)iter.next();
/* 1089 */       if ((field.m_localOnly) && (forCentralDir)) {
/*      */         continue;
/*      */       }
/*      */ 
/* 1093 */       extraFields[(index++)] = field;
/* 1094 */       extrafieldLen += field.m_length + 4;
/*      */     }
/* 1096 */     if (extrafieldLen > 65535)
/*      */     {
/* 1098 */       IdcMessage msg = new IdcMessage("syZipFormatExtraFieldsLengthBad", new Object[0]);
/* 1099 */       msg.m_prior = new IdcMessage("syByteLengthBad", new Object[] { Integer.valueOf(extrafieldLen) });
/* 1100 */       throw new IdcZipException(msg);
/*      */     }
/* 1102 */     length += extrafieldLen;
/*      */ 
/* 1104 */     byte[] commentBytes = null;
/* 1105 */     if ((forCentralDir) && (!ignoreComment) && (null != entry.m_comment))
/*      */     {
/*      */       try
/*      */       {
/* 1109 */         commentBytes = entry.m_comment.getBytes(encoding);
/*      */       }
/*      */       catch (UnsupportedEncodingException e)
/*      */       {
/* 1113 */         throw new IdcZipException(e, "syZipFormatEntryCommentEncodeFailed", new Object[] { entry.m_filename });
/*      */       }
/* 1115 */       if (commentBytes.length > 65535)
/*      */       {
/* 1117 */         IdcMessage msg = new IdcMessage("syZipFormatEntryCommentEncodeFailed", new Object[] { entry.m_filename });
/* 1118 */         msg.m_prior = new IdcMessage("syByteLengthBad", new Object[] { Integer.valueOf(commentBytes.length) });
/* 1119 */         throw new IdcZipException(msg);
/*      */       }
/* 1121 */       length += commentBytes.length;
/*      */     }
/*      */ 
/* 1125 */     byte[] header = new byte[length];
/* 1126 */     int i = 0;
/* 1127 */     header[(i++)] = 80;
/* 1128 */     header[(i++)] = 75;
/* 1129 */     if (forCentralDir)
/*      */     {
/* 1131 */       header[(i++)] = 1;
/* 1132 */       header[(i++)] = 2;
/*      */     }
/*      */     else
/*      */     {
/* 1136 */       header[(i++)] = 3;
/* 1137 */       header[(i++)] = 4;
/*      */     }
/* 1139 */     if (forCentralDir)
/*      */     {
/* 1141 */       header[(i++)] = entry.m_versionCreated;
/* 1142 */       header[(i++)] = entry.m_hostCreated;
/*      */     }
/* 1144 */     header[(i++)] = entry.m_versionRequired;
/* 1145 */     header[(i++)] = entry.m_hostRequired;
/* 1146 */     int entryFlags = entry.m_flags & 0xFFFFF7FF;
/* 1147 */     if ((null == entry.m_encoding) || (entry.m_encoding.equals("UTF-8")))
/*      */     {
/* 1149 */       entryFlags |= 2048;
/*      */     }
/* 1151 */     header[(i++)] = (byte)(entryFlags & 0xFF);
/* 1152 */     header[(i++)] = (byte)(entryFlags >> 8 & 0xFF);
/* 1153 */     header[(i++)] = (byte)(entry.m_compressionMethod & 0xFF);
/* 1154 */     header[(i++)] = (byte)(entry.m_compressionMethod >> 8 & 0xFF);
/* 1155 */     dosTimeToBytes(entry.m_lastModified, header, i);
/* 1156 */     i += 4;
/* 1157 */     header[(i++)] = (byte)(entry.m_crc32 & 0xFF);
/* 1158 */     header[(i++)] = (byte)(entry.m_crc32 >> 8 & 0xFF);
/* 1159 */     header[(i++)] = (byte)(entry.m_crc32 >> 16 & 0xFF);
/* 1160 */     header[(i++)] = (byte)(entry.m_crc32 >> 24 & 0xFF);
/* 1161 */     header[(i++)] = (byte)(int)(entry.m_sizeCompressed & 0xFF);
/* 1162 */     header[(i++)] = (byte)(int)(entry.m_sizeCompressed >> 8 & 0xFF);
/* 1163 */     header[(i++)] = (byte)(int)(entry.m_sizeCompressed >> 16 & 0xFF);
/* 1164 */     header[(i++)] = (byte)(int)(entry.m_sizeCompressed >> 24 & 0xFF);
/* 1165 */     header[(i++)] = (byte)(int)(entry.m_sizeUncompressed & 0xFF);
/* 1166 */     header[(i++)] = (byte)(int)(entry.m_sizeUncompressed >> 8 & 0xFF);
/* 1167 */     header[(i++)] = (byte)(int)(entry.m_sizeUncompressed >> 16 & 0xFF);
/* 1168 */     header[(i++)] = (byte)(int)(entry.m_sizeUncompressed >> 24 & 0xFF);
/* 1169 */     header[(i++)] = (byte)(filenameBytes.length & 0xFF);
/* 1170 */     header[(i++)] = (byte)(filenameBytes.length >> 8 & 0xFF);
/* 1171 */     header[(i++)] = (byte)(extrafieldLen & 0xFF);
/* 1172 */     header[(i++)] = (byte)(extrafieldLen >> 8 & 0xFF);
/* 1173 */     if (forCentralDir)
/*      */     {
/* 1175 */       int commentBytesLength = (null == commentBytes) ? 0 : commentBytes.length;
/* 1176 */       header[(i++)] = (byte)(commentBytesLength & 0xFF);
/* 1177 */       header[(i++)] = (byte)(commentBytesLength >> 8 & 0xFF);
/* 1178 */       i += 2;
/* 1179 */       header[(i++)] = (byte)(entry.m_internalAttrs & 0xFF);
/* 1180 */       header[(i++)] = (byte)(entry.m_internalAttrs >> 8 & 0xFF);
/* 1181 */       header[(i++)] = entry.m_externalAttrsMSDOS;
/* 1182 */       header[(i++)] = entry.m_externalAttrsOther;
/* 1183 */       header[(i++)] = (byte)(entry.m_externalAttrsUnix & 0xFF);
/* 1184 */       header[(i++)] = (byte)(entry.m_externalAttrsUnix >> 8 & 0xFF);
/* 1185 */       header[(i++)] = (byte)(int)(entry.m_headerOffset & 0xFF);
/* 1186 */       header[(i++)] = (byte)(int)(entry.m_headerOffset >> 8 & 0xFF);
/* 1187 */       header[(i++)] = (byte)(int)(entry.m_headerOffset >> 16 & 0xFF);
/* 1188 */       header[(i++)] = (byte)(int)(entry.m_headerOffset >> 24 & 0xFF);
/*      */     }
/* 1190 */     if (filenameBytes.length > 0)
/*      */     {
/* 1192 */       System.arraycopy(filenameBytes, 0, header, i, filenameBytes.length);
/* 1193 */       i += filenameBytes.length;
/*      */     }
/* 1195 */     for (index = 0; index < extrafieldNum; ++index)
/*      */     {
/* 1197 */       IdcZipEntryExtraField field = extraFields[index];
/* 1198 */       if (null == field) {
/*      */         break;
/*      */       }
/*      */ 
/* 1202 */       header[(i++)] = (byte)(field.m_id & 0xFF);
/* 1203 */       header[(i++)] = (byte)(field.m_id >> 8 & 0xFF);
/* 1204 */       header[(i++)] = (byte)(field.m_length & 0xFF);
/* 1205 */       header[(i++)] = (byte)(field.m_length >> 8 & 0xFF);
/* 1206 */       if (field.m_length <= 0)
/*      */         continue;
/* 1208 */       System.arraycopy(field.m_data, 0, header, i, field.m_length);
/* 1209 */       i += field.m_length;
/*      */     }
/*      */ 
/* 1212 */     if ((forCentralDir) && (!ignoreComment) && (null != commentBytes))
/*      */     {
/* 1214 */       System.arraycopy(commentBytes, 0, header, i, commentBytes.length);
/* 1215 */       i += commentBytes.length;
/*      */     }
/* 1217 */     return header;
/*      */   }
/*      */ 
/*      */   public byte[] makeEntryDataDescriptor(IdcZipEntry entry)
/*      */     throws IdcZipException
/*      */   {
/* 1229 */     int len = ((entry.m_flags & 0x8) != 0) ? 16 : 0;
/* 1230 */     byte[] desc = new byte[len];
/* 1231 */     if (len > 0)
/*      */     {
/* 1233 */       desc[0] = 80;
/* 1234 */       desc[1] = 75;
/* 1235 */       desc[2] = 7;
/* 1236 */       desc[3] = 8;
/* 1237 */       desc[4] = (byte)(entry.m_crc32 & 0xFF);
/* 1238 */       desc[5] = (byte)(entry.m_crc32 >> 8 & 0xFF);
/* 1239 */       desc[6] = (byte)(entry.m_crc32 >> 16 & 0xFF);
/* 1240 */       desc[7] = (byte)(entry.m_crc32 >> 24 & 0xFF);
/* 1241 */       desc[8] = (byte)(int)(entry.m_sizeCompressed & 0xFF);
/* 1242 */       desc[9] = (byte)(int)(entry.m_sizeCompressed >> 8 & 0xFF);
/* 1243 */       desc[10] = (byte)(int)(entry.m_sizeCompressed >> 16 & 0xFF);
/* 1244 */       desc[11] = (byte)(int)(entry.m_sizeCompressed >> 24 & 0xFF);
/* 1245 */       desc[12] = (byte)(int)(entry.m_sizeUncompressed & 0xFF);
/* 1246 */       desc[13] = (byte)(int)(entry.m_sizeUncompressed >> 8 & 0xFF);
/* 1247 */       desc[14] = (byte)(int)(entry.m_sizeUncompressed >> 16 & 0xFF);
/* 1248 */       desc[15] = (byte)(int)(entry.m_sizeUncompressed >> 24 & 0xFF);
/*      */     }
/* 1250 */     return desc;
/*      */   }
/*      */ 
/*      */   public byte[] makeEndOfCentralDirectoryRecord(IdcZipHandler zip, int flags)
/*      */     throws IdcZipException
/*      */   {
/* 1266 */     int numEntries = zip.m_entries.size();
/* 1267 */     if (numEntries > 65535)
/*      */     {
/* 1269 */       throw new IdcZipException("syZipFormatNumEntriesBad", new Object[] { Integer.valueOf(numEntries) });
/*      */     }
/* 1271 */     long dirLen = zip.m_centralDirectoryLength;
/* 1272 */     if (dirLen > 2147483647L)
/*      */     {
/* 1274 */       throw new IdcZipException("syZipFormatCentralDirectorySizeBad", new Object[0]);
/*      */     }
/* 1276 */     int len = 22;
/* 1277 */     IdcRandomAccessByteArray commentBytes = null;
/* 1278 */     int commentLen = 0;
/* 1279 */     if ((null != zip.m_comment) && ((flags & 0x1) == 0))
/*      */     {
/* 1281 */       commentBytes = new IdcRandomAccessByteArray(zip.m_comment.length() * 3);
/*      */       try
/*      */       {
/* 1284 */         IdcByteConversionUtils.formatUTF8String(zip.m_comment, commentBytes);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1288 */         throw new IdcZipException(e, "syZipFormatFileCommentIgnored", new Object[0]);
/*      */       }
/* 1290 */       if (commentBytes.m_position > 65535)
/*      */       {
/* 1292 */         IdcMessage msg = new IdcMessage("syZipFormatFileCommentIgnored", new Object[0]);
/* 1293 */         msg.m_prior = new IdcMessage("syByteLengthBad", new Object[] { Integer.valueOf(commentBytes.m_position) });
/* 1294 */         throw new IdcZipException(msg);
/*      */       }
/* 1296 */       commentLen = commentBytes.m_position;
/* 1297 */       len += commentLen;
/*      */     }
/* 1299 */     byte[] record = new byte[len];
/* 1300 */     record[0] = 80;
/* 1301 */     record[1] = 75;
/* 1302 */     record[2] = 5;
/* 1303 */     record[3] = 6;
/* 1304 */     record[8] = (byte)(numEntries & 0xFF);
/* 1305 */     record[9] = (byte)(numEntries >> 8 & 0xFF);
/* 1306 */     record[10] = record[8];
/* 1307 */     record[11] = record[9];
/* 1308 */     record[12] = (byte)(int)(dirLen & 0xFF);
/* 1309 */     record[13] = (byte)(int)(dirLen >> 8 & 0xFF);
/* 1310 */     record[14] = (byte)(int)(dirLen >> 16 & 0xFF);
/* 1311 */     record[15] = (byte)(int)(dirLen >> 24 & 0xFF);
/* 1312 */     record[16] = (byte)(int)(zip.m_centralDirectoryOffset & 0xFF);
/* 1313 */     record[17] = (byte)(int)(zip.m_centralDirectoryOffset >> 8 & 0xFF);
/* 1314 */     record[18] = (byte)(int)(zip.m_centralDirectoryOffset >> 16 & 0xFF);
/* 1315 */     record[19] = (byte)(int)(zip.m_centralDirectoryOffset >> 24 & 0xFF);
/* 1316 */     record[20] = (byte)(commentLen & 0xFF);
/* 1317 */     record[21] = (byte)(commentLen >> 8 & 0xFF);
/* 1318 */     if ((commentLen > 0) && 
/* 1320 */       (commentBytes.m_position > 0))
/*      */     {
/* 1322 */       System.arraycopy(commentBytes, 0, record, 22, commentBytes.m_position);
/*      */     }
/*      */ 
/* 1325 */     return record;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1331 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99018 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.zip.IdcZipFileFormatter
 * JD-Core Version:    0.5.4
 */