/*     */ package intradoc.data;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ 
/*     */ public abstract interface DataFormat
/*     */ {
/*     */   public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98395 $";
/*  62 */   public static final Integer DEFINE_DATABINDER = Integer.valueOf(4);
/*     */ 
/*  70 */   public static final Integer DEFINE_DATABINDER_NAME = Integer.valueOf(5);
/*     */ 
/*  81 */   public static final Integer DEFINE_PROPERTIES = Integer.valueOf(8);
/*     */ 
/*  90 */   public static final Integer DEFINE_PROPERTY_PAIR = Integer.valueOf(9);
/*     */ 
/* 110 */   public static final Integer DEFINE_RESULT_SET = Integer.valueOf(16);
/*     */ 
/* 119 */   public static final Integer DEFINE_RESULT_SET_FIELD = Integer.valueOf(17);
/*     */ 
/* 129 */   public static final Integer DEFINE_RESULT_SET_ROW = Integer.valueOf(18);
/*     */ 
/* 142 */   public static final Integer DEFINE_RESULT_SET_CELL = Integer.valueOf(19);
/*     */ 
/* 154 */   public static final Integer T_NAME = Integer.valueOf(32);
/*     */ 
/* 160 */   public static final Integer T_VALUE = Integer.valueOf(33);
/*     */ 
/* 163 */   public static final Integer T_LOCAL_DATA = Integer.valueOf(48);
/*     */ 
/* 165 */   public static final Integer T_ENVIRONMENT = Integer.valueOf(49);
/*     */ 
/* 167 */   public static final Integer T_RESULT_SETS = Integer.valueOf(50);
/*     */ 
/* 170 */   public static final Integer T_PAIRS = Integer.valueOf(51);
/*     */ 
/* 173 */   public static final Integer T_FIELDS = Integer.valueOf(52);
/*     */ 
/* 175 */   public static final Integer T_ROWS = Integer.valueOf(53);
/*     */ 
/* 177 */   public static final Integer T_CELLS = Integer.valueOf(54);
/*     */ 
/* 181 */   public static final Integer T_PRODUCT_VERSION = Integer.valueOf(64);
/*     */ 
/* 183 */   public static final Integer T_SYSTEM_ISO_ENCODING = Integer.valueOf(65);
/*     */ 
/* 186 */   public static final Integer T_COUNT_TOTAL = Integer.valueOf(72);
/*     */ 
/* 188 */   public static final Integer T_COUNT_DEFAULT = Integer.valueOf(73);
/*     */ 
/* 190 */   public static final Integer T_COUNT_DEFINED = Integer.valueOf(74);
/*     */ 
/* 193 */   public static final Integer T_COUNT_FIELDS = Integer.valueOf(80);
/*     */ 
/* 195 */   public static final Integer T_COUNT_ROWS = Integer.valueOf(81);
/*     */ 
/* 197 */   public static final Integer T_COUNT_ROWS_LIMIT = Integer.valueOf(82);
/*     */ 
/* 199 */   public static final Integer T_ROW_INDEX = Integer.valueOf(83);
/*     */ 
/* 201 */   public static final Integer T_ROW_INDEX_CURRENT = Integer.valueOf(84);
/*     */ 
/* 203 */   public static final Integer T_ROW_INDEX_FIRST = Integer.valueOf(85);
/*     */ 
/* 205 */   public static final Integer T_ROW_INDEX_LAST = Integer.valueOf(86);
/*     */ 
/* 207 */   public static final Integer T_FIELD_INDEX = Integer.valueOf(87);
/*     */ 
/* 212 */   public static final Integer ENDIF = Integer.valueOf(128);
/*     */ 
/* 214 */   public static final Integer ELSE = Integer.valueOf(129);
/*     */ 
/* 220 */   public static final Integer IF_NOT_FIRST = Integer.valueOf(130);
/*     */ 
/* 225 */   public static final Integer IF_SHOW_DEFAULTS = Integer.valueOf(131);
/*     */ 
/* 230 */   public static final Integer IF_IS_DEFAULT = Integer.valueOf(132);
/*     */ 
/* 236 */   public static final Integer IF_IS_SEEKABLE = Integer.valueOf(133);
/*     */ 
/* 241 */   public static final Integer IF_SHOW_CURRENT_ONLY = Integer.valueOf(134);
/*     */ 
/* 246 */   public static final Integer IF_SHOW_LIMITED = Integer.valueOf(135);
/*     */ 
/* 251 */   public static final Integer IF_HAS_ROW = Integer.valueOf(136);
/*     */ 
/* 256 */   public static final Integer IF_DEAD = Integer.valueOf(137);
/*     */ 
/* 261 */   public static final Integer IF_HAS_VALUE = Integer.valueOf(138);
/*     */ 
/* 265 */   public static final Integer IF_HAS_RESULTSET = Integer.valueOf(139);
/*     */ 
/*     */   public abstract Object[][] getTokens();
/*     */ 
/*     */   public abstract void appendLiteral(IdcStringBuilder paramIdcStringBuilder, String paramString);
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.DataFormat
 * JD-Core Version:    0.5.4
 */