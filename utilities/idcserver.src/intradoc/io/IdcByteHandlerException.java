/*     */ package intradoc.io;
/*     */ 
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcMessageContainer;
/*     */ import java.io.IOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ 
/*     */ public class IdcByteHandlerException extends IOException
/*     */   implements IdcMessageContainer
/*     */ {
/*     */   public IdcMessage m_message;
/*     */ 
/*     */   public IdcByteHandlerException()
/*     */   {
/*     */   }
/*     */ 
/*     */   public IdcByteHandlerException(int unsupportedFeatures)
/*     */   {
/*  49 */     this.m_message = computeMessageFromFeatures(unsupportedFeatures);
/*     */   }
/*     */ 
/*     */   public IdcByteHandlerException(Throwable cause, int unsupportedFeatures)
/*     */   {
/*  54 */     super(cause);
/*  55 */     this.m_message = computeMessageFromFeatures(unsupportedFeatures);
/*     */   }
/*     */ 
/*     */   public IdcByteHandlerException(Throwable cause)
/*     */   {
/*  60 */     super(cause);
/*     */   }
/*     */ 
/*     */   public IdcByteHandlerException(Throwable cause, String key, Object[] args)
/*     */   {
/*  65 */     super(cause);
/*  66 */     this.m_message = new IdcMessage(key, args);
/*     */   }
/*     */ 
/*     */   public IdcByteHandlerException(String key, Object[] args)
/*     */   {
/*  72 */     this.m_message = new IdcMessage(key, args);
/*     */   }
/*     */ 
/*     */   public IdcByteHandlerException(char op, long numBytes, long numTotal)
/*     */   {
/*  85 */     this.m_message = computeMessageFromByteCount(op, numBytes, numTotal);
/*     */   }
/*     */ 
/*     */   protected static IdcMessage computeMessageFromFeatures(int features)
/*     */   {
/*  92 */     StringBuilder str = new StringBuilder("Unsupported byte feature(s): ");
/*  93 */     List list = new ArrayList();
/*  94 */     boolean doComma = false;
/*  95 */     if ((features & 0x1) != 0)
/*     */     {
/*  97 */       list.add("syByteFeatureRead");
/*  98 */       if (doComma)
/*     */       {
/* 100 */         str.append(", ");
/*     */       }
/* 102 */       str.append("read");
/* 103 */       doComma = true;
/*     */     }
/* 105 */     if ((features & 0x2) != 0)
/*     */     {
/* 107 */       list.add("syByteFeatureWrite");
/* 108 */       if (doComma)
/*     */       {
/* 110 */         str.append(", ");
/*     */       }
/* 112 */       str.append("write");
/* 113 */       doComma = true;
/*     */     }
/* 115 */     if ((features & 0x100) != 0)
/*     */     {
/* 117 */       list.add("syByteFeatureResize");
/* 118 */       if (doComma)
/*     */       {
/* 120 */         str.append(", ");
/*     */       }
/* 122 */       str.append("resize");
/* 123 */       doComma = true;
/*     */     }
/* 125 */     if ((features & 0x200) != 0)
/*     */     {
/* 127 */       list.add("syByteFeatureRandomAccess");
/* 128 */       if (doComma)
/*     */       {
/* 130 */         str.append(", ");
/*     */       }
/* 132 */       str.append("seek");
/*     */     }
/* 134 */     if ((features & 0x10000) != 0)
/*     */     {
/* 136 */       list.add("syByteFeatureShallowClone");
/* 137 */       if (doComma)
/*     */       {
/* 139 */         str.append(", ");
/*     */       }
/* 141 */       str.append("clone");
/*     */     }
/* 143 */     IdcMessage msg = new IdcMessage("syByteFeaturesUnsupported", new Object[] { list });
/* 144 */     msg.m_msgSimple = str.toString();
/* 145 */     return msg;
/*     */   }
/*     */ 
/*     */   protected static IdcMessage computeMessageFromByteCount(char op, long numBytes, long numTotal)
/*     */   {
/*     */     String opKey;
/*     */     String opStr;
/* 151 */     switch (op)
/*     */     {
/*     */     case 'r':
/* 154 */       opKey = "syByteOpRead";
/* 155 */       opStr = "read";
/* 156 */       break;
/*     */     case 'w':
/* 158 */       opKey = "syByteOpWrite";
/* 159 */       opStr = "write";
/* 160 */       break;
/*     */     default:
/* 162 */       opKey = "unknown";
/* 163 */       opStr = "unknown";
/*     */     }
/* 165 */     IdcMessage msg = new IdcMessage("syByteOpFailed", new Object[] { opKey, String.valueOf(numBytes), String.valueOf(numTotal) });
/* 166 */     StringBuilder str = new StringBuilder("only ");
/* 167 */     str.append(opStr);
/* 168 */     str.append(' ');
/* 169 */     str.append(numBytes);
/* 170 */     str.append(" of ");
/* 171 */     str.append(numTotal);
/* 172 */     msg.m_msgSimple = str.toString();
/* 173 */     return msg;
/*     */   }
/*     */ 
/*     */   public IdcMessage getIdcMessage()
/*     */   {
/* 180 */     return this.m_message;
/*     */   }
/*     */ 
/*     */   public void setIdcMessage(IdcMessage msg)
/*     */   {
/* 185 */     this.m_message = msg;
/*     */   }
/*     */ 
/*     */   public IdcMessage appendAssociatedMessages(IdcMessage top)
/*     */   {
/* 190 */     return top;
/*     */   }
/*     */ 
/*     */   public Object getContainerAttribute(String key)
/*     */   {
/* 195 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 203 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 89479 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.IdcByteHandlerException
 * JD-Core Version:    0.5.4
 */