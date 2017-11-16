/*     */ package intradoc.io;
/*     */ 
/*     */ import java.io.FilterOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ 
/*     */ public class HexDumpOutputStream extends FilterOutputStream
/*     */ {
/*     */   public long offset;
/*  31 */   public byte[] line = new byte[16];
/*     */   protected int index;
/*  33 */   protected byte[] buffer = new byte[80];
/*     */ 
/*     */   public HexDumpOutputStream(OutputStream stream)
/*     */   {
/*  37 */     super(stream);
/*     */   }
/*     */ 
/*     */   public void finalize()
/*     */     throws Throwable
/*     */   {
/*  43 */     dumpLine();
/*  44 */     super.finalize();
/*     */   }
/*     */ 
/*     */   public void flush()
/*     */     throws IOException
/*     */   {
/*  54 */     dumpLine();
/*  55 */     super.flush();
/*     */   }
/*     */ 
/*     */   public void write(byte[] b)
/*     */     throws IOException
/*     */   {
/*  61 */     write(b, 0, b.length);
/*     */   }
/*     */ 
/*     */   public void write(int b)
/*     */     throws IOException
/*     */   {
/*  67 */     this.offset += 1L;
/*  68 */     this.line[(this.index++)] = (byte)b;
/*  69 */     if (this.index < 16)
/*     */       return;
/*  71 */     dumpLine();
/*     */   }
/*     */ 
/*     */   public void write(byte[] b, int off, int length)
/*     */     throws IOException
/*     */   {
/*  78 */     while (length-- > 0) {
/*     */       do {
/*  80 */         this.offset += 1L;
/*  81 */         this.line[(this.index++)] = b[(off++)];
/*  82 */       }while (this.index < 16);
/*     */ 
/*  84 */       dumpLine();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void dumpHex(int idx, int digits, int number)
/*     */     throws IOException
/*     */   {
/*  92 */     for (int i = 0; i < digits; number >>= 4)
/*     */     {
/*  94 */       int digit = number & 0xF;
/*  95 */       digit += ((digit < 10) ? 48 : 87);
/*  96 */       this.buffer[(idx + digits - i - 1)] = (byte)digit;
/*     */ 
/*  92 */       ++i;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void dumpLine()
/*     */     throws IOException
/*     */   {
/* 102 */     int idx = 0;
/* 103 */     dumpHex(idx, 8, (int)(this.offset - this.index));
/* 104 */     idx += 8;
/* 105 */     this.buffer[(idx++)] = 58;
/* 106 */     if (this.index < 1)
/*     */     {
/* 108 */       this.buffer[(idx++)] = 10;
/* 109 */       this.out.write(this.buffer, 0, this.index);
/* 110 */       this.index = 0;
/* 111 */       return;
/*     */     }
/* 113 */     for (int i = 0; i < 16; ++i)
/*     */     {
/* 115 */       if ((i & 0x7) == 0)
/*     */       {
/* 117 */         this.buffer[(idx++)] = 32;
/*     */       }
/* 119 */       this.buffer[(idx++)] = 32;
/* 120 */       if (i < this.index)
/*     */       {
/* 122 */         dumpHex(idx, 2, this.line[i]);
/* 123 */         idx += 2;
/*     */       }
/*     */       else
/*     */       {
/* 127 */         this.buffer[(idx++)] = 32;
/* 128 */         this.buffer[(idx++)] = 32;
/*     */       }
/*     */     }
/* 131 */     this.buffer[(idx++)] = 32;
/* 132 */     this.buffer[(idx++)] = 32;
/* 133 */     this.buffer[(idx++)] = 91;
/* 134 */     for (int i = 0; i < this.index; ++i)
/*     */     {
/* 136 */       this.buffer[(idx++)] = (((this.line[i] < 32) || (this.line[i] >= 127)) ? 46 : this.line[i]);
/*     */     }
/* 138 */     this.buffer[(idx++)] = 93;
/* 139 */     this.buffer[(idx++)] = 10;
/* 140 */     this.out.write(this.buffer, 0, idx);
/* 141 */     this.index = 0;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 147 */     return "releaseInfo=dev,releaseRevision=$Rev: 66344 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.HexDumpOutputStream
 * JD-Core Version:    0.5.4
 */