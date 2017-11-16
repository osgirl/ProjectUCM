/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ParseStringLocation
/*     */ {
/*     */   public static final short ERROR_SUCCESS = 0;
/*     */   public static final short ERROR_GENERAL = -1;
/*     */   public static final short ERROR_PATTERN_MISMATCH = -2;
/*     */   public static final short ERROR_INVALID_NUMBER = -3;
/*     */   public static final short ERROR_NUMBER_NOT_IN_RANGE = -4;
/*     */   public static final short ERROR_UNKNOWN_STRING = -5;
/*     */   public static final short ERROR_UNSUPPORTED_FORMAT_SPECIFIER = -6;
/*     */   public static final short ERROR_UNEXPECTED_STRING_END = -7;
/*     */   public static final short ERROR_EMPTY_STRING = -8;
/*     */   public static final short ERROR_STRING_TOO_LONG = -9;
/*  52 */   public final String[] m_traceErrorStrings = { "Success", "GeneralError", "PatternMismatch", "InvalidNumber", "NumberNotInRange", "UnknownString", "UnsupportedFormatSpecifier", "UnexpectedStringEnd", "EmtpyString", "StringTooLong" };
/*     */   public int m_index;
/*     */   public int m_errorIndex;
/*     */   public boolean m_indexIsErrorOffset;
/*     */   public String m_errMsg;
/*     */   public String m_objectPartLocation;
/*     */   public String m_elementLocation;
/*     */   public Object m_activeParsingObject;
/*     */   public Vector m_failedParsingLocations;
/*     */   public int m_state;
/*     */ 
/*     */   public ParseStringLocation()
/*     */   {
/* 117 */     init(0);
/*     */   }
/*     */ 
/*     */   public ParseStringLocation(int index)
/*     */   {
/* 122 */     init(index);
/*     */   }
/*     */ 
/*     */   public void init(int index)
/*     */   {
/* 127 */     this.m_index = 0;
/* 128 */     this.m_errorIndex = -1;
/* 129 */     this.m_indexIsErrorOffset = true;
/* 130 */     this.m_errMsg = null;
/* 131 */     this.m_objectPartLocation = null;
/* 132 */     this.m_elementLocation = null;
/* 133 */     this.m_activeParsingObject = null;
/* 134 */     this.m_failedParsingLocations = null;
/* 135 */     this.m_state = 0;
/*     */   }
/*     */ 
/*     */   public void setErrorState(int index, int state)
/*     */   {
/* 140 */     this.m_index = index;
/* 141 */     this.m_state = state;
/*     */   }
/*     */ 
/*     */   public void setErrorMessage(int index, int state, String msg)
/*     */   {
/* 146 */     this.m_index = index;
/* 147 */     this.m_state = state;
/* 148 */     this.m_errMsg = msg;
/*     */   }
/*     */ 
/*     */   public void copyErrorState(ParseStringLocation parseLocation)
/*     */   {
/* 153 */     this.m_index = parseLocation.m_index;
/* 154 */     this.m_state = parseLocation.m_state;
/* 155 */     this.m_errMsg = parseLocation.m_errMsg;
/*     */   }
/*     */ 
/*     */   public int determineErrorIndex()
/*     */   {
/* 160 */     if (this.m_errorIndex >= 0)
/*     */     {
/* 162 */       return this.m_errorIndex;
/*     */     }
/* 164 */     return this.m_index;
/*     */   }
/*     */ 
/*     */   public void clearError()
/*     */   {
/* 169 */     this.m_state = 0;
/* 170 */     this.m_indexIsErrorOffset = true;
/* 171 */     this.m_errorIndex = -1;
/* 172 */     this.m_errMsg = null;
/*     */   }
/*     */ 
/*     */   public ParseStringLocation getFirstParseError()
/*     */   {
/* 177 */     ParseStringLocation psl = this;
/* 178 */     if ((this.m_failedParsingLocations != null) && (this.m_failedParsingLocations.size() > 0))
/*     */     {
/* 180 */       psl = (ParseStringLocation)this.m_failedParsingLocations.elementAt(0);
/*     */     }
/* 182 */     return psl;
/*     */   }
/*     */ 
/*     */   public String getFirstErrorMessage()
/*     */   {
/* 187 */     ParseStringLocation psl = getFirstParseError();
/* 188 */     return psl.m_errMsg;
/*     */   }
/*     */ 
/*     */   public ParseStringLocation shallowClone()
/*     */   {
/* 193 */     ParseStringLocation psl = new ParseStringLocation();
/* 194 */     shallowCopy(psl);
/* 195 */     return psl;
/*     */   }
/*     */ 
/*     */   public void shallowCopy(ParseStringLocation psl)
/*     */   {
/* 203 */     psl.m_index = this.m_index;
/* 204 */     psl.m_state = this.m_state;
/* 205 */     psl.m_indexIsErrorOffset = this.m_indexIsErrorOffset;
/* 206 */     psl.m_errMsg = this.m_errMsg;
/* 207 */     psl.m_errorIndex = this.m_errorIndex;
/* 208 */     psl.m_objectPartLocation = this.m_objectPartLocation;
/* 209 */     psl.m_elementLocation = this.m_elementLocation;
/* 210 */     psl.m_activeParsingObject = this.m_activeParsingObject;
/* 211 */     psl.m_failedParsingLocations = this.m_failedParsingLocations;
/*     */   }
/*     */ 
/*     */   public ParseStringLocation createRetryCopy()
/*     */   {
/* 216 */     ParseStringLocation psl = new ParseStringLocation();
/* 217 */     if (this.m_failedParsingLocations != null)
/*     */     {
/* 219 */       psl.m_failedParsingLocations = ((Vector)this.m_failedParsingLocations.clone());
/*     */     }
/* 221 */     if (this.m_state != 0)
/*     */     {
/* 223 */       if (psl.m_failedParsingLocations == null)
/*     */       {
/* 225 */         psl.m_failedParsingLocations = new IdcVector();
/*     */       }
/*     */ 
/* 230 */       ParseStringLocation copy = shallowClone();
/* 231 */       copy.m_failedParsingLocations = null;
/* 232 */       psl.m_failedParsingLocations.addElement(copy);
/*     */     }
/* 234 */     return psl;
/*     */   }
/*     */ 
/*     */   public String getStateTraceString()
/*     */   {
/* 239 */     if ((-this.m_state >= 0) && (-this.m_state < this.m_traceErrorStrings.length))
/*     */     {
/* 241 */       return this.m_traceErrorStrings[(-this.m_state)];
/*     */     }
/* 243 */     return "" + this.m_state;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 249 */     if (this.m_errMsg != null)
/*     */     {
/* 251 */       return "ParseStringLocation index " + this.m_index + ", error " + this.m_errMsg;
/*     */     }
/* 253 */     return "ParseStringLocation index " + this.m_index;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 258 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 81508 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ParseStringLocation
 * JD-Core Version:    0.5.4
 */