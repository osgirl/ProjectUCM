/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.IOException;
/*     */ 
/*     */ public class FileQueueHeader
/*     */ {
/*     */   public static final short FORMATTED_LEN = 98;
/*     */   public static final short APPEND_ENTRY = 0;
/*     */   public static final short DELETE_ENTRY = 1;
/*     */   public static final short NO_ACTION = 2;
/*     */   public int m_action;
/*     */   public int m_index;
/*     */   public int m_size;
/*     */   public int m_numBytesUsed;
/*     */   public String m_specialPrefix;
/*     */   public int m_offsetToData;
/*     */   public int m_startByte;
/*     */   public int m_endByte;
/*     */   public int m_startMessage;
/*     */   public int m_endMessage;
/*     */   public int m_usedMessages;
/*     */   public int m_maxMessages;
/*     */ 
/*     */   public FileQueueHeader()
/*     */   {
/*  90 */     this.m_action = 2;
/*  91 */     this.m_index = -1;
/*     */ 
/*  93 */     this.m_specialPrefix = "FILEQUEUE V 1.00";
/*     */ 
/*  95 */     this.m_size = 0;
/*  96 */     this.m_numBytesUsed = 0;
/*  97 */     this.m_offsetToData = 98;
/*  98 */     this.m_startByte = -1;
/*  99 */     this.m_endByte = -1;
/* 100 */     this.m_startMessage = -1;
/* 101 */     this.m_endMessage = -1;
/* 102 */     this.m_usedMessages = 0;
/* 103 */     this.m_maxMessages = 0;
/*     */   }
/*     */ 
/*     */   public void serializeFixedLen(FixedFieldFormatter formatter, boolean isWrite)
/*     */     throws IOException
/*     */   {
/* 110 */     formatter.m_len = 2;
/* 111 */     if (isWrite)
/* 112 */       formatter.formatInt(this.m_action);
/*     */     else {
/* 114 */       this.m_action = formatter.parseInt();
/*     */     }
/* 116 */     formatter.m_len = 6;
/* 117 */     if (isWrite)
/* 118 */       formatter.formatInt(this.m_index);
/*     */     else {
/* 120 */       this.m_index = formatter.parseInt();
/*     */     }
/*     */ 
/* 123 */     formatter.m_len = 9;
/* 124 */     if (isWrite)
/* 125 */       formatter.formatInt(this.m_size);
/*     */     else {
/* 127 */       this.m_size = formatter.parseInt();
/*     */     }
/* 129 */     formatter.m_len = 9;
/* 130 */     if (isWrite)
/* 131 */       formatter.formatInt(this.m_numBytesUsed);
/*     */     else {
/* 133 */       this.m_numBytesUsed = formatter.parseInt();
/*     */     }
/* 135 */     formatter.m_len = 20;
/* 136 */     if (isWrite)
/*     */     {
/* 138 */       formatter.formatString(this.m_specialPrefix);
/*     */     }
/*     */     else
/*     */     {
/* 142 */       String temp = formatter.parseString();
/* 143 */       if (!temp.equals(this.m_specialPrefix))
/*     */       {
/* 145 */         throw new IOException(LocaleUtils.encodeMessage("csFileQueueVersionMismatch", null, this.m_specialPrefix, temp));
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 150 */     formatter.m_len = 9;
/* 151 */     if (isWrite)
/* 152 */       formatter.formatInt(this.m_offsetToData);
/*     */     else {
/* 154 */       this.m_offsetToData = formatter.parseInt();
/*     */     }
/* 156 */     formatter.m_len = 9;
/* 157 */     if (isWrite)
/* 158 */       formatter.formatInt(this.m_startByte);
/*     */     else {
/* 160 */       this.m_startByte = formatter.parseInt();
/*     */     }
/* 162 */     formatter.m_len = 9;
/* 163 */     if (isWrite)
/* 164 */       formatter.formatInt(this.m_endByte);
/*     */     else {
/* 166 */       this.m_endByte = formatter.parseInt();
/*     */     }
/* 168 */     formatter.m_len = 6;
/* 169 */     if (isWrite)
/* 170 */       formatter.formatInt(this.m_startMessage);
/*     */     else {
/* 172 */       this.m_startMessage = formatter.parseInt();
/*     */     }
/* 174 */     formatter.m_len = 6;
/* 175 */     if (isWrite)
/* 176 */       formatter.formatInt(this.m_endMessage);
/*     */     else {
/* 178 */       this.m_endMessage = formatter.parseInt();
/*     */     }
/* 180 */     formatter.m_len = 6;
/* 181 */     if (isWrite)
/* 182 */       formatter.formatInt(this.m_usedMessages);
/*     */     else {
/* 184 */       this.m_usedMessages = formatter.parseInt();
/*     */     }
/* 186 */     formatter.m_len = 6;
/* 187 */     if (isWrite)
/* 188 */       formatter.formatInt(this.m_maxMessages);
/*     */     else {
/* 190 */       this.m_maxMessages = formatter.parseInt();
/*     */     }
/*     */ 
/* 194 */     formatter.insertCarriageReturn(isWrite);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 200 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.FileQueueHeader
 * JD-Core Version:    0.5.4
 */