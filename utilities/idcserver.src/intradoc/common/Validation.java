/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class Validation
/*     */ {
/*     */   public static final short URL_FILE_SEGMENT = 0;
/*     */   public static final short DATABASE_FIELD_NAME = 1;
/*     */   public static final short STANDARD_FORM_FIELD = 2;
/*     */   public static final short VAL_SUCCESS = 0;
/*     */   public static final short VAL_EMPTY = -1;
/*     */   public static final short VAL_HAS_SPACES = -2;
/*     */   public static final short VAL_INVALID_CHARS = -3;
/*     */   public static final short VAL_INVALID_FIRST_CHAR = -4;
/*     */   public static final short VAL_IS_NOT_INTEGER = -5;
/*     */   public static final short VAL_TOO_LONG = -6;
/*     */   public static final String VAL_ILLEGAL_URL_SEGMENT_CHARS = ";/\\?:@&=+\"#%<>*~|[]ıİ";
/*     */   public static final String VAL_ILLEGAL_FORM_CHARS = ":%<>\"";
/*     */   public static final String VAL_VARNAME_CHARS = "_";
/*  60 */   public static final String[] VAL_ALLOWABLE_HTML_TAGS = { "table", "img", "font", "strong", "div", "span", "sub", "sup", "strike", "big", "small", "ins", "del" };
/*     */ 
/*  66 */   public static Hashtable m_allowableHtmlTags = null;
/*     */ 
/*     */   public static int checkUrlFileSegment(String fSegment)
/*     */   {
/*  73 */     return checkString(fSegment, 0);
/*     */   }
/*     */ 
/*     */   public static int checkUrlFilePathPart(String fPart)
/*     */   {
/*  82 */     int offset = 0;
/*  83 */     int len = fPart.length();
/*     */ 
/*  85 */     while (offset < len)
/*     */     {
/*  87 */       int nextSlash = fPart.indexOf(47, offset);
/*  88 */       int endIndex = (nextSlash >= 0) ? nextSlash : len;
/*  89 */       if (offset == endIndex)
/*     */       {
/*  91 */         return -3;
/*     */       }
/*     */ 
/*  94 */       String subStr = fPart.substring(offset, endIndex);
/*  95 */       int retVal = checkUrlFileSegment(subStr);
/*  96 */       if (retVal != 0)
/*     */       {
/*  98 */         return retVal;
/*     */       }
/*     */ 
/* 101 */       offset = endIndex + 1;
/*     */     }
/*     */ 
/* 104 */     return 0;
/*     */   }
/*     */ 
/*     */   public static int checkDatabaseFieldName(String fName)
/*     */   {
/* 110 */     return checkString(fName, 1);
/*     */   }
/*     */ 
/*     */   public static int checkFormField(String fName)
/*     */   {
/* 115 */     return checkString(fName, 2);
/*     */   }
/*     */ 
/*     */   public static int checkPassword(String password)
/*     */   {
/* 122 */     return checkString(password, 2, -1);
/*     */   }
/*     */ 
/*     */   public static void validateSortField(String sortField)
/*     */     throws ServiceException
/*     */   {
/* 128 */     if ((sortField == null) || (sortField.length() == 0)) {
/* 129 */       return;
/*     */     }
/* 131 */     for (int i = 0; i < sortField.length(); ++i)
/*     */     {
/* 133 */       if (!isSpace(sortField.charAt(i)))
/*     */         continue;
/* 135 */       String errorMsg = LocaleUtils.encodeMessage("csSortFieldInvalidCharacters", null, sortField);
/* 136 */       throw new ServiceException(null, errorMsg, new Object[0]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void validateSortOrder(String sortOrder)
/*     */     throws ServiceException
/*     */   {
/* 143 */     if ((sortOrder == null) || (sortOrder.length() == 0)) {
/* 144 */       return;
/*     */     }
/* 146 */     Vector sortOrderList = StringUtils.parseArray(sortOrder, ',', ',');
/* 147 */     for (String order : sortOrderList)
/*     */     {
/* 149 */       if ((!order.equalsIgnoreCase("ASC")) && (!order.equalsIgnoreCase("DESC")))
/*     */       {
/* 151 */         String errorMsg = LocaleUtils.encodeMessage("csSortOrderInvalidCharacters", null, sortOrder);
/* 152 */         throw new ServiceException(null, errorMsg, new Object[0]);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void validatePassword(String password) throws ServiceException
/*     */   {
/* 159 */     int valResult = checkPassword(password);
/* 160 */     switch (valResult) {
/*     */     case 0:
/* 163 */       return;
/*     */     case -3:
/* 165 */       throw new ServiceException(null, "apInvalidCharsInPassword", new Object[0]);
/*     */     case -1:
/* 167 */       throw new ServiceException(null, "apErrorPasswordEmpty", new Object[0]);
/*     */     case -2:
/*     */     }
/* 169 */     throw new ServiceException(null, "syUnknownError", new Object[0]);
/*     */   }
/*     */ 
/*     */   public static int checkString(String str, int type)
/*     */   {
/* 175 */     return checkString(str, type, -1);
/*     */   }
/*     */ 
/*     */   public static int checkString(String str, int type, int maxLength)
/*     */   {
/* 181 */     if ((str == null) || (str.length() == 0))
/*     */     {
/* 183 */       return -1;
/*     */     }
/* 185 */     int len = str.length();
/* 186 */     if ((maxLength >= 0) && (len > maxLength))
/*     */     {
/* 188 */       return -6;
/*     */     }
/*     */ 
/* 191 */     String extraChars = ":%<>\"";
/* 192 */     if (type == 0)
/*     */     {
/* 194 */       extraChars = ";/\\?:@&=+\"#%<>*~|[]ıİ";
/*     */     }
/*     */ 
/* 197 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 199 */       char ch = str.charAt(i);
/* 200 */       if ((type == 1) && 
/* 202 */         (i == 0) && (!isAlpha(ch)))
/*     */       {
/* 204 */         return -4;
/*     */       }
/*     */ 
/* 207 */       if (isAlphaNum(ch)) {
/*     */         continue;
/*     */       }
/*     */ 
/* 211 */       if ((isSpace(ch)) && ((
/* 213 */         (ch != ' ') || (type != 2))))
/*     */       {
/* 215 */         return -2;
/*     */       }
/*     */ 
/* 218 */       if (type == 1)
/*     */       {
/* 220 */         if ("_".indexOf(ch) < 0)
/*     */         {
/* 222 */           return -3;
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 228 */         if (((ch >= 0) && (ch < ' ')) || (ch == ''))
/*     */         {
/* 230 */           return -3;
/*     */         }
/*     */ 
/* 235 */         if (extraChars.indexOf(ch) >= 0)
/*     */         {
/* 237 */           return -3;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 242 */     if ((type == 0) && ((
/* 244 */       (str.equals(".")) || (str.equals("..")))))
/*     */     {
/* 246 */       return -3;
/*     */     }
/*     */ 
/* 250 */     return 0;
/*     */   }
/*     */ 
/*     */   public static int checkInteger(String str)
/*     */   {
/* 264 */     if ((str == null) || (str.length() == 0))
/*     */     {
/* 266 */       return -1;
/*     */     }
/*     */ 
/* 269 */     if (str.charAt(0) == '-')
/*     */     {
/* 271 */       str = str.substring(1);
/*     */     }
/* 273 */     int len = str.length();
/* 274 */     if (len == 0)
/*     */     {
/* 276 */       return -3;
/*     */     }
/*     */ 
/* 279 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 281 */       if (!isNum(str.charAt(i)))
/*     */       {
/* 283 */         return -5;
/*     */       }
/*     */     }
/* 286 */     return 0;
/*     */   }
/*     */ 
/*     */   public static boolean isConfigValueWhitespace(String str)
/*     */   {
/* 300 */     if ((str == null) || (str.length() == 0))
/*     */     {
/* 302 */       return false;
/*     */     }
/* 304 */     int nchars = str.length();
/* 305 */     boolean isAllWhitespace = true;
/* 306 */     for (int i = 0; i < nchars; ++i)
/*     */     {
/* 308 */       if (str.charAt(i) <= ' ')
/*     */         continue;
/* 310 */       isAllWhitespace = false;
/* 311 */       break;
/*     */     }
/*     */ 
/* 314 */     return isAllWhitespace;
/*     */   }
/*     */ 
/*     */   public static boolean isAlphaNum(char ch)
/*     */   {
/* 321 */     return (isAlpha(ch)) || (isNum(ch));
/*     */   }
/*     */ 
/*     */   public static boolean isAlpha(char ch)
/*     */   {
/* 326 */     return ((ch >= 'a') && (ch <= 'z')) || ((ch >= 'A') && (ch <= 'Z'));
/*     */   }
/*     */ 
/*     */   public static boolean isNum(char ch)
/*     */   {
/* 331 */     return (ch >= '0') && (ch <= '9');
/*     */   }
/*     */ 
/*     */   public static boolean isSpace(char ch)
/*     */   {
/* 336 */     return (ch == ' ') || (ch == '\t') || (ch == '\r') || (ch == '\n') || (ch == ' ');
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String checkUrlFileSegmentForDB(String val, String msgStub, int length)
/*     */   {
/* 343 */     IdcMessage msg = checkUrlFileSegmentForDB(val, msgStub, length, null);
/* 344 */     return (msg != null) ? LocaleUtils.encodeMessage(msg) : null;
/*     */   }
/*     */ 
/*     */   public static IdcMessage checkUrlFileSegmentForDB(String val, String msgStub, int length, Map options)
/*     */   {
/* 350 */     String key = null;
/*     */ 
/* 352 */     int valResult = checkUrlFileSegment(val);
/* 353 */     switch (valResult)
/*     */     {
/*     */     case 0:
/* 356 */       break;
/*     */     case -1:
/* 358 */       key = "syValidationMsgMissing";
/* 359 */       break;
/*     */     case -2:
/* 361 */       key = "syValidationMsgHasSpaces";
/* 362 */       break;
/*     */     case -3:
/* 364 */       key = "syValidationInvalidChars";
/*     */     }
/*     */ 
/* 367 */     if ((key == null) && (length > 0) && (val.length() > length))
/*     */     {
/* 369 */       IdcMessage msg = IdcMessageFactory.lc("syValidationTooManyChars", new Object[] { msgStub, new Integer(length) });
/* 370 */       return msg;
/*     */     }
/* 372 */     if (key != null)
/*     */     {
/* 374 */       return IdcMessageFactory.lc(key, new Object[] { msgStub });
/*     */     }
/*     */ 
/* 377 */     return null;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String checkFormFieldForDB(String val, String msgStub, int length)
/*     */   {
/* 384 */     IdcMessage msg = checkFormFieldForDB(val, msgStub, length, null);
/* 385 */     return (msg != null) ? LocaleUtils.encodeMessage(msg) : null;
/*     */   }
/*     */ 
/*     */   public static IdcMessage checkFormFieldForDB(String val, String msgStub, int length, Map options)
/*     */   {
/* 390 */     String key = null;
/* 391 */     int valResult = checkFormField(val);
/* 392 */     switch (valResult)
/*     */     {
/*     */     case 0:
/* 395 */       break;
/*     */     case -1:
/* 397 */       key = "syValidationMsgMissing";
/* 398 */       break;
/*     */     case -3:
/* 400 */       key = "syValidationInvalidChars";
/*     */     case -2:
/*     */     }
/* 403 */     if ((key == null) && (length > 0) && (val.length() > length))
/*     */     {
/* 405 */       return IdcMessageFactory.lc("syValidationTooManyChars", new Object[] { msgStub, new Integer(length) });
/*     */     }
/* 407 */     if (key != null)
/*     */     {
/* 409 */       return IdcMessageFactory.lc(key, new Object[] { msgStub });
/*     */     }
/*     */ 
/* 412 */     return null;
/*     */   }
/*     */ 
/*     */   public static boolean isAllowableHtmlTag(String key)
/*     */   {
/* 420 */     if (m_allowableHtmlTags == null)
/*     */     {
/* 422 */       Hashtable tags = new Hashtable();
/* 423 */       for (int i = 0; i < VAL_ALLOWABLE_HTML_TAGS.length; ++i)
/*     */       {
/* 425 */         tags.put(VAL_ALLOWABLE_HTML_TAGS[i], Boolean.TRUE);
/*     */       }
/* 427 */       m_allowableHtmlTags = tags;
/*     */     }
/*     */ 
/* 432 */     if ((key == null) || (key.length() <= 2))
/*     */     {
/* 434 */       return true;
/*     */     }
/*     */ 
/* 447 */     String temp = key.toLowerCase();
/* 448 */     return m_allowableHtmlTags.get(temp) != null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 453 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97103 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.Validation
 * JD-Core Version:    0.5.4
 */