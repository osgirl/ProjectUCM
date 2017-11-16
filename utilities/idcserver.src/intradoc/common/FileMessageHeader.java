/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.IOException;
/*     */ 
/*     */ public class FileMessageHeader
/*     */ {
/*     */   public static final short FORMATTED_LEN = 34;
/*     */   public String m_id;
/*     */   public int m_state;
/*     */   public boolean m_isReversed;
/*     */   public int m_offset;
/*     */   public int m_len;
/*     */   public int m_marker;
/*     */ 
/*     */   public FileMessageHeader()
/*     */   {
/*  61 */     this.m_id = null;
/*  62 */     this.m_state = 0;
/*  63 */     this.m_isReversed = false;
/*  64 */     this.m_offset = 0;
/*  65 */     this.m_len = 0;
/*  66 */     this.m_marker = 0;
/*     */   }
/*     */ 
/*     */   public void copy(FileMessageHeader msgHeader)
/*     */   {
/*  71 */     this.m_id = msgHeader.m_id;
/*  72 */     this.m_state = msgHeader.m_state;
/*  73 */     this.m_isReversed = msgHeader.m_isReversed;
/*  74 */     this.m_offset = msgHeader.m_offset;
/*  75 */     this.m_len = msgHeader.m_len;
/*  76 */     this.m_marker = msgHeader.m_marker;
/*     */   }
/*     */ 
/*     */   public void serializeFixedLen(FixedFieldFormatter formatter, boolean isWrite)
/*     */     throws IOException
/*     */   {
/*  82 */     formatter.m_len = 10;
/*  83 */     if (isWrite)
/*  84 */       formatter.formatString(this.m_id);
/*     */     else {
/*  86 */       this.m_id = formatter.parseString();
/*     */     }
/*  88 */     formatter.m_len = 2;
/*  89 */     if (isWrite)
/*  90 */       formatter.formatInt(this.m_state);
/*     */     else {
/*  92 */       this.m_state = formatter.parseInt();
/*     */     }
/*  94 */     formatter.m_len = 2;
/*  95 */     if (isWrite)
/*  96 */       formatter.formatBoolean(this.m_isReversed);
/*     */     else {
/*  98 */       this.m_isReversed = formatter.parseBoolean();
/*     */     }
/* 100 */     formatter.m_len = 9;
/* 101 */     if (isWrite)
/* 102 */       formatter.formatInt(this.m_offset);
/*     */     else {
/* 104 */       this.m_offset = formatter.parseInt();
/*     */     }
/* 106 */     formatter.m_len = 5;
/* 107 */     if (isWrite)
/* 108 */       formatter.formatInt(this.m_len);
/*     */     else {
/* 110 */       this.m_len = formatter.parseInt();
/*     */     }
/* 112 */     formatter.m_len = 5;
/* 113 */     if (isWrite)
/* 114 */       formatter.formatInt(this.m_marker);
/*     */     else {
/* 116 */       this.m_marker = formatter.parseInt();
/*     */     }
/*     */ 
/* 120 */     formatter.insertCarriageReturn(isWrite);
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 126 */     IdcAppendable msg = new IdcStringBuilder();
/* 127 */     msg.append("m_id: " + this.m_id);
/* 128 */     String state = null;
/* 129 */     if (this.m_state == 1)
/*     */     {
/* 131 */       state = "available";
/*     */     }
/* 133 */     else if (this.m_state == 2)
/*     */     {
/* 135 */       state = "reserved";
/*     */     }
/* 137 */     msg.append("\nm_state: " + state);
/* 138 */     msg.append("\nm_isReversed: " + this.m_isReversed);
/* 139 */     msg.append("\nm_offset: " + this.m_offset);
/* 140 */     msg.append("\nm_len: " + this.m_len);
/* 141 */     msg.append("\nm_marker: " + this.m_marker);
/* 142 */     return msg.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 147 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96613 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.FileMessageHeader
 * JD-Core Version:    0.5.4
 */