/*     */ package intradoc.soap;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.data.DataException;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ 
/*     */ public class SoapDimeRecord
/*     */ {
/*     */   public static final int DIME_VERSION = 1;
/*     */   public static final int DIME_HEADER_SIZE = 12;
/*     */   public static final int DIME_PADDING = 4;
/*     */   public static final int DIME_TYPE_FORMAT_MEDIA = 1;
/*     */   public static final int DIME_TYPE_FORMAT_URI = 2;
/*     */   public boolean m_isMsgBegin;
/*     */   public boolean m_isMsgEnd;
/*     */   public int m_chunkFlag;
/*     */   public int m_typeFormat;
/*     */   public int m_optionsLength;
/*     */   public int m_idLength;
/*     */   public int m_typeLength;
/*     */   public int m_dataLength;
/*     */   public String m_options;
/*     */   public String m_recordID;
/*     */   public String m_recordType;
/*     */ 
/*     */   public SoapDimeRecord()
/*     */   {
/*  37 */     this.m_isMsgBegin = false;
/*  38 */     this.m_isMsgEnd = false;
/*  39 */     this.m_chunkFlag = -1;
/*  40 */     this.m_typeFormat = -1;
/*     */ 
/*  42 */     this.m_optionsLength = 0;
/*  43 */     this.m_idLength = 0;
/*  44 */     this.m_typeLength = 0;
/*  45 */     this.m_dataLength = 0;
/*     */ 
/*  48 */     this.m_options = null;
/*  49 */     this.m_recordID = null;
/*  50 */     this.m_recordType = null;
/*     */   }
/*     */ 
/*     */   public void parse(InputStream is) throws IOException, DataException {
/*  54 */     parseHeader(is);
/*  55 */     parseBody(is);
/*     */   }
/*     */ 
/*     */   protected void parseHeader(InputStream is) throws DataException, IOException
/*     */   {
/*  60 */     byte[] header = SoapUtils.readStream(is, 12, false);
/*     */ 
/*  63 */     int version = (header[0] & 0xF8) >> 3;
/*  64 */     if (version != 1)
/*     */     {
/*  66 */       String errorMsg = LocaleUtils.encodeMessage("csSoapDimeInvalidVersion", null);
/*  67 */       throw new DataException(errorMsg);
/*     */     }
/*     */ 
/*  72 */     this.m_chunkFlag = (header[0] & 0x1);
/*  73 */     if ((this.m_chunkFlag == 1) && (this.m_isMsgBegin))
/*     */     {
/*  75 */       String errorMsg = LocaleUtils.encodeMessage("csSoapDimeChunkingNotSupported", null);
/*  76 */       throw new DataException(errorMsg);
/*     */     }
/*     */ 
/*  80 */     int msgBegin = (header[0] & 0x4) >> 2;
/*  81 */     this.m_isMsgBegin = (msgBegin == 1);
/*     */ 
/*  83 */     int msgEnd = (header[0] & 0x2) >> 1;
/*  84 */     this.m_isMsgEnd = (msgEnd == 1);
/*     */ 
/*  87 */     this.m_optionsLength = convertBytesToInt(header, 2, 2);
/*  88 */     this.m_idLength = convertBytesToInt(header, 4, 2);
/*  89 */     this.m_typeLength = convertBytesToInt(header, 6, 2);
/*  90 */     this.m_dataLength = convertBytesToInt(header, 8, 4);
/*     */   }
/*     */ 
/*     */   protected void parseBody(InputStream is)
/*     */     throws IOException
/*     */   {
/*  96 */     byte[] optionsBytes = SoapUtils.readStream(is, this.m_optionsLength, false);
/*  97 */     readPadding(is, this.m_optionsLength);
/*  98 */     this.m_options = new String(optionsBytes);
/*     */ 
/* 101 */     byte[] idBytes = SoapUtils.readStream(is, this.m_idLength, false);
/* 102 */     readPadding(is, this.m_idLength);
/* 103 */     this.m_recordID = new String(idBytes);
/*     */ 
/* 106 */     byte[] typeBytes = SoapUtils.readStream(is, this.m_typeLength, false);
/* 107 */     readPadding(is, this.m_typeLength);
/* 108 */     this.m_recordType = new String(typeBytes);
/*     */   }
/*     */ 
/*     */   public byte[] send()
/*     */   {
/* 114 */     this.m_idLength = this.m_recordID.length();
/* 115 */     this.m_typeLength = this.m_recordType.length();
/*     */ 
/* 117 */     int length = 12 + this.m_idLength + getPaddingLength(this.m_idLength) + this.m_typeLength + getPaddingLength(this.m_typeLength);
/*     */ 
/* 119 */     byte[] body = new byte[length];
/*     */ 
/* 121 */     sendHeader(body);
/*     */ 
/* 124 */     int start = 12;
/* 125 */     convertStringToBytes(this.m_recordID, body, start);
/*     */ 
/* 127 */     start += this.m_idLength + getPaddingLength(this.m_idLength);
/* 128 */     convertStringToBytes(this.m_recordType, body, start);
/*     */ 
/* 130 */     return body;
/*     */   }
/*     */ 
/*     */   public void sendHeader(byte[] body)
/*     */   {
/* 135 */     int msgBeginFlag = (this.m_isMsgBegin) ? 1 : 0;
/* 136 */     int msgEndFlag = (this.m_isMsgEnd) ? 1 : 0;
/*     */ 
/* 139 */     body[0] = (byte)(0x8 | msgBeginFlag << 2 | msgEndFlag << 1);
/*     */ 
/* 141 */     body[1] = (byte)(this.m_typeFormat << 4);
/*     */ 
/* 144 */     body[2] = 0;
/* 145 */     body[3] = 0;
/*     */ 
/* 148 */     convertIntToBytes(this.m_idLength, body, 4, 2);
/* 149 */     convertIntToBytes(this.m_typeLength, body, 6, 2);
/* 150 */     convertIntToBytes(this.m_dataLength, body, 8, 4);
/*     */   }
/*     */ 
/*     */   protected int convertBytesToInt(byte[] b, int start, int length)
/*     */   {
/* 155 */     int value = 0;
/* 156 */     int shift = (length - 1) * 8;
/*     */ 
/* 158 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 160 */       int curValue = 0;
/* 161 */       int curByte = b[(start + i)];
/*     */ 
/* 163 */       if (curByte < 0)
/*     */       {
/* 165 */         curValue = curByte + 256;
/*     */       }
/*     */       else
/*     */       {
/* 169 */         curValue = curByte;
/*     */       }
/*     */ 
/* 172 */       value |= curValue << shift;
/* 173 */       shift -= 8;
/*     */     }
/*     */ 
/* 176 */     return value;
/*     */   }
/*     */ 
/*     */   protected void convertIntToBytes(int value, byte[] b, int start, int length)
/*     */   {
/* 181 */     for (int curByte = length - 1; curByte >= 0; --curByte)
/*     */     {
/* 183 */       b[(curByte + start)] = (byte)value;
/* 184 */       value >>= 8;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void convertStringToBytes(String value, byte[] b, int start)
/*     */   {
/* 190 */     byte[] valueBytes = value.getBytes();
/* 191 */     int length = valueBytes.length;
/*     */ 
/* 193 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 195 */       b[(start + i)] = valueBytes[i];
/*     */     }
/*     */   }
/*     */ 
/*     */   public void readPadding(InputStream is, int length) throws IOException
/*     */   {
/* 201 */     int reminder = length % 4;
/* 202 */     if (reminder <= 0)
/*     */       return;
/* 204 */     int padding = 4 - reminder;
/* 205 */     SoapUtils.readStream(is, padding, false);
/*     */   }
/*     */ 
/*     */   public int getPaddingLength(int length)
/*     */   {
/* 211 */     int reminder = length % 4;
/* 212 */     int padding = 0;
/* 213 */     if (reminder > 0)
/*     */     {
/* 215 */       padding = 4 - reminder;
/*     */     }
/*     */ 
/* 218 */     return padding;
/*     */   }
/*     */ 
/*     */   public byte[] getPaddingBytes(int length)
/*     */   {
/* 223 */     int paddingLength = getPaddingLength(length);
/* 224 */     byte[] padding = new byte[paddingLength];
/* 225 */     for (int i = 0; i < paddingLength; ++i)
/*     */     {
/* 227 */       padding[i] = 0;
/*     */     }
/*     */ 
/* 230 */     return padding;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 235 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.SoapDimeRecord
 * JD-Core Version:    0.5.4
 */