/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.List;
/*     */ 
/*     */ public class ResourceOptions
/*     */   implements DynamicDataHandleAttribute
/*     */ {
/*     */   public List<String> m_preservedParameters;
/*     */   public int[] m_mergeFlags;
/*     */   public boolean m_isError;
/*     */   public String m_errorKey;
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/*  53 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ 
/*     */   public void handleTag(String tag)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void handleValue(String attribute, String value)
/*     */   {
/*  73 */     boolean foundIt = false;
/*  74 */     if (attribute.equals("preserve"))
/*     */     {
/*  76 */       this.m_preservedParameters = StringUtils.makeListFromSequenceSimple(value);
/*  77 */       foundIt = true;
/*     */     }
/*  79 */     boolean isMinus = false;
/*  80 */     if ((!foundIt) && 
/*  82 */       (attribute.startsWith("-")))
/*     */     {
/*  84 */       isMinus = true;
/*  85 */       attribute = attribute.substring(1);
/*     */     }
/*     */ 
/*  88 */     if (!foundIt)
/*     */     {
/*  90 */       int index = StringUtils.findStringIndex(DynamicHtmlStatic.m_directiveFlags, attribute);
/*  91 */       if (index >= 0)
/*     */       {
/*  93 */         int bitFlag = 1 << index;
/*  94 */         if (!StringUtils.convertToBool(value, true))
/*     */         {
/*  96 */           isMinus = true;
/*     */         }
/*  98 */         int flagIndex = (isMinus) ? 1 : 0;
/*  99 */         if (this.m_mergeFlags == null)
/*     */         {
/* 101 */           this.m_mergeFlags = new int[2];
/*     */         }
/* 103 */         this.m_mergeFlags[flagIndex] |= bitFlag;
/* 104 */         foundIt = true;
/*     */       }
/*     */     }
/* 107 */     if (foundIt)
/*     */       return;
/* 109 */     Report.trace("idocscript", "The ResourceOptions property " + attribute + " is unrecognized", null);
/*     */   }
/*     */ 
/*     */   public void handleError(String errKey, String errMessage, int attributeStartIndex, String attribute, int dataStart, int dataEnd, char[] data)
/*     */   {
/* 127 */     IdcStringBuilder buf = new IdcStringBuilder(dataEnd - dataStart + errMessage.length() + 50);
/* 128 */     buf.append("Improperly formed resource definition tag (");
/* 129 */     buf.append(errMessage);
/* 130 */     buf.append(") for buffer\n");
/* 131 */     buf.append(data, dataStart, dataEnd - dataStart);
/* 132 */     Report.trace("idocscript", buf.toString(), null);
/* 133 */     this.m_isError = true;
/* 134 */     this.m_errorKey = errKey;
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ResourceOptions
 * JD-Core Version:    0.5.4
 */