/*     */ package intradoc.io.zip;
/*     */ 
/*     */ import intradoc.io.IdcByteHandler;
/*     */ import intradoc.util.MapUtils;
/*     */ import java.text.FieldPosition;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Date;
/*     */ import java.util.Locale;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class IdcZipEntry
/*     */ {
/*     */   public static final byte HOST_MSDOS = 0;
/*     */   public static final byte HOST_UNIX = 3;
/*     */   public static final byte HOST_NTFS = 11;
/*     */   public static final short F_ENCRYPTED = 1;
/*     */   public static final short F_COMPRESS_INFO_IN_DATA = 8;
/*     */   public static final short F_USE_UTF8 = 2048;
/*     */   public static final short STORED = 0;
/*     */   public static final short DEFLATED = 8;
/*     */   public static final short IS_TEXTFILE = 1;
/*     */   public static final short UNIX_IFMT = -4096;
/*     */   public static final short UNIX_IFREG = -32768;
/*     */   public static final short UNIX_IFDIR = 16384;
/*     */   public static final short UNIX_ISUID = 2048;
/*     */   public static final short UNIX_ISGID = 1024;
/*     */   public static final short UNIX_ISVTX = 512;
/*     */   public static final short UNIX_IRUSR = 256;
/*     */   public static final short UNIX_IWUSR = 128;
/*     */   public static final short UNIX_IXUSR = 64;
/*     */   public static final short UNIX_IRGRP = 32;
/*     */   public static final short UNIX_IWGRP = 16;
/*     */   public static final short UNIX_IXGRP = 8;
/*     */   public static final short UNIX_IROTH = 4;
/*     */   public static final short UNIX_IWOTH = 2;
/*     */   public static final short UNIX_IXOTH = 1;
/*     */   public static final int MSDOS_EXEC = 128;
/*     */   public static final int MSDOS_LINK = 64;
/*     */   public static final int MSDOS_ARCHIVE = 32;
/*     */   public static final int MSDOS_DIR = 16;
/*     */   public static final int MSDOS_LABEL = 8;
/*     */   public static final int MSDOS_SYSTEM = 4;
/*     */   public static final int MSDOS_HIDDEN = 2;
/*     */   public static final int MSDOS_READONLY = 1;
/*     */   public byte m_versionCreated;
/*     */   public byte m_hostCreated;
/*     */   public byte m_versionRequired;
/*     */   public byte m_hostRequired;
/*     */   public short m_flags;
/*     */   public short m_compressionMethod;
/*     */   public long m_lastModified;
/*     */   public int m_crc32;
/*     */   public long m_sizeCompressed;
/*     */   public long m_sizeUncompressed;
/*     */   public String m_filename;
/*     */   public Map<Integer, IdcZipEntryExtraField> m_extraFields;
/*     */   public String m_comment;
/*     */   public short m_internalAttrs;
/*     */   public byte m_externalAttrsMSDOS;
/*     */   public byte m_externalAttrsOther;
/*     */   public short m_externalAttrsUnix;
/*     */   public long m_headerOffset;
/*     */   public long m_dataOffset;
/*     */   public boolean m_hasExtraFields;
/*     */   public boolean m_isDirectory;
/*     */   public boolean m_isExecutable;
/*     */   public boolean m_isReadOnly;
/*     */   public String m_encoding;
/*     */   public IdcByteHandler m_bytesCompressed;
/*     */   public IdcByteHandler m_bytesUncompressed;
/*     */   protected boolean m_wasExtracted;
/*     */   protected String m_string;
/*     */ 
/*     */   public IdcZipEntry()
/*     */   {
/* 139 */     setDefaultAttributes();
/*     */   }
/*     */ 
/*     */   public IdcZipEntry(String filename)
/*     */   {
/* 147 */     setDefaultAttributes();
/* 148 */     this.m_filename = filename;
/*     */   }
/*     */ 
/*     */   public IdcZipEntry(String filename, IdcByteHandler bytes)
/*     */   {
/* 157 */     setDefaultAttributes();
/* 158 */     this.m_filename = filename;
/* 159 */     this.m_bytesUncompressed = bytes;
/*     */   }
/*     */ 
/*     */   public void setDefaultAttributes()
/*     */   {
/* 168 */     this.m_versionCreated = 63;
/* 169 */     this.m_hostCreated = 3;
/* 170 */     this.m_versionRequired = 20;
/* 171 */     this.m_hostRequired = 3;
/* 172 */     this.m_flags = 2048;
/* 173 */     this.m_externalAttrsUnix = -32384;
/*     */   }
/*     */ 
/*     */   public IdcZipEntryExtraField getField(int id)
/*     */   {
/* 186 */     if (this.m_extraFields == null)
/*     */     {
/* 188 */       return null;
/*     */     }
/* 190 */     return (IdcZipEntryExtraField)this.m_extraFields.get(Integer.valueOf(id));
/*     */   }
/*     */ 
/*     */   public void setField(IdcZipEntryExtraField field)
/*     */   {
/* 200 */     Integer fieldID = new Integer(field.m_id);
/* 201 */     if (this.m_extraFields == null)
/*     */     {
/* 203 */       this.m_extraFields = MapUtils.createConcurrentMap();
/* 204 */       this.m_hasExtraFields = true;
/*     */     }
/* 206 */     this.m_extraFields.put(fieldID, field);
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 219 */     if (null != this.m_string)
/*     */     {
/* 221 */       return this.m_string;
/*     */     }
/* 223 */     StringBuffer str = new StringBuffer(128);
/* 224 */     int xattr = this.m_externalAttrsUnix;
/* 225 */     char[] attrs = new char[15];
/* 226 */     switch (this.m_hostCreated)
/*     */     {
/*     */     case 0:
/* 229 */       if ((xattr & 0x1C0) != (0x100 | (this.m_externalAttrsMSDOS & 0x1 ^ 0xFFFFFFFF) << 7 | (this.m_externalAttrsMSDOS & 0x10) << 2))
/*     */       {
/* 232 */         xattr = this.m_externalAttrsMSDOS;
/* 233 */         attrs[0] = (((xattr & 0x10) != 0) ? 100 : '-');
/* 234 */         attrs[1] = 'r';
/* 235 */         attrs[2] = (((xattr & 0x1) != 0) ? 45 : 'w');
/* 236 */         attrs[3] = (((xattr & 0x10) != 0) ? 120 : '-');
/* 237 */         if ((null != this.m_filename) && (((this.m_filename.endsWith(".com")) || (this.m_filename.endsWith(".exe")) || (this.m_filename.endsWith(".btm")) || (this.m_filename.endsWith(".cmd")) || (this.m_filename.endsWith(".bat")))))
/*     */         {
/* 241 */           attrs[3] = 'x';
/*     */         }
/* 243 */         attrs[4] = (((xattr & 0x20) != 0) ? 97 : '-');
/* 244 */         attrs[5] = (((xattr & 0x2) != 0) ? 104 : '-');
/* 245 */         attrs[6] = (((xattr & 0x4) != 0) ? 115 : '-');
/* 246 */         for (int i = 7; i < 12; ++i)
/*     */         {
/* 248 */           attrs[i] = ' ';
/*     */         }
/*     */       }
/* 250 */       break;
/*     */     case 3:
/*     */     case 11:
/* 255 */       switch (xattr & 0xFFFFF000)
/*     */       {
/*     */       case 16384:
/* 258 */         attrs[0] = 'd';
/* 259 */         break;
/*     */       case -32768:
/* 261 */         attrs[0] = '-';
/* 262 */         break;
/*     */       default:
/* 264 */         attrs[0] = '?';
/*     */       }
/*     */ 
/* 267 */       attrs[1] = (((xattr & 0x100) != 0) ? 114 : '-');
/* 268 */       attrs[2] = (((xattr & 0x80) != 0) ? 119 : '-');
/* 269 */       attrs[3] = (((xattr & 0x800) != 0) ? 83 : ((xattr & 0x40) != 0) ? 120 : ((xattr & 0x800) != 0) ? 115 : '-');
/*     */ 
/* 271 */       attrs[4] = (((xattr & 0x20) != 0) ? 114 : '-');
/* 272 */       attrs[5] = (((xattr & 0x10) != 0) ? 119 : '-');
/* 273 */       attrs[6] = (((xattr & 0x400) != 0) ? 108 : ((xattr & 0x8) != 0) ? 120 : ((xattr & 0x400) != 0) ? 115 : '-');
/*     */ 
/* 275 */       attrs[7] = (((xattr & 0x4) != 0) ? 114 : '-');
/* 276 */       attrs[8] = (((xattr & 0x2) != 0) ? 119 : '-');
/* 277 */       attrs[9] = (((xattr & 0x200) != 0) ? 84 : ((xattr & 0x1) != 0) ? 120 : ((xattr & 0x200) != 0) ? 116 : '-');
/*     */ 
/* 279 */       attrs[10] = ' ';
/* 280 */       attrs[11] = ' ';
/* 281 */       break;
/*     */     default:
/* 283 */       for (int i = 0; i < 10; ++i)
/*     */       {
/* 285 */         attrs[i] = '?';
/*     */       }
/* 287 */       attrs[10] = ' ';
/* 288 */       attrs[11] = ' ';
/*     */     }
/*     */ 
/* 291 */     attrs[12] = (char)(48 + this.m_versionCreated / 10);
/* 292 */     attrs[13] = '.';
/* 293 */     attrs[14] = (char)(48 + this.m_versionCreated % 10);
/* 294 */     str.append(attrs);
/*     */ 
/* 296 */     switch (this.m_hostCreated)
/*     */     {
/*     */     case 0:
/* 299 */       str.append(" fat ");
/* 300 */       break;
/*     */     case 3:
/* 302 */       str.append(" unx ");
/* 303 */       break;
/*     */     case 11:
/* 305 */       str.append(" ntf ");
/* 306 */       break;
/*     */     default:
/* 308 */       str.append(" ??? ");
/*     */     }
/*     */ 
/* 311 */     str.append(this.m_sizeUncompressed);
/* 312 */     str.append(((this.m_internalAttrs & 0x1) != 0) ? " t" : ((this.m_flags & 0x1) != 0) ? " B" : ((this.m_internalAttrs & 0x1) != 0) ? " T" : " b");
/*     */ 
/* 315 */     str.append(((this.m_flags & 0x8) != 0) ? 'l' : (this.m_hasExtraFields) ? 'x' : ((this.m_flags & 0x8) != 0) ? 'X' : '-');
/*     */ 
/* 318 */     switch (this.m_compressionMethod)
/*     */     {
/*     */     case 0:
/* 321 */       str.append(" stor ");
/* 322 */       break;
/*     */     case 8:
/* 324 */       str.append(" def");
/* 325 */       str.append("NXFS".charAt(this.m_flags >> 1 & 0x3));
/* 326 */       str.append(' ');
/* 327 */       break;
/*     */     default:
/* 329 */       str.append(" u");
/* 330 */       str.append(this.m_compressionMethod);
/* 331 */       str.append(' ');
/*     */     }
/*     */ 
/* 334 */     SimpleDateFormat fmt = new SimpleDateFormat("dd-MMM-yy HH:mm", Locale.US);
/* 335 */     Date date = new Date(this.m_lastModified);
/* 336 */     FieldPosition pos = new FieldPosition(0);
/* 337 */     fmt.format(date, str, pos);
/* 338 */     str.append(' ');
/* 339 */     str.append(this.m_filename);
/*     */ 
/* 341 */     this.m_string = str.toString();
/* 342 */     return this.m_string;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 348 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 89508 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.zip.IdcZipEntry
 * JD-Core Version:    0.5.4
 */