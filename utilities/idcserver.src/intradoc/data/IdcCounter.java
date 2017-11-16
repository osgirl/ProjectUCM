/*     */ package intradoc.data;
/*     */ 
/*     */ import intradoc.common.NumberUtils;
/*     */ 
/*     */ public class IdcCounter
/*     */ {
/*  29 */   public String m_key = null;
/*     */ 
/*  34 */   public String m_name = null;
/*     */ 
/*  39 */   public String m_nativeName = null;
/*     */ 
/*  48 */   public COUNTER_TYPE m_type = null;
/*     */ 
/*  53 */   public long m_initValue = -1L;
/*     */ 
/*  58 */   public int m_increment = 0;
/*     */ 
/*  63 */   public long m_minValue = 0L;
/*     */ 
/*  68 */   public DataBinder m_binder = null;
/*     */ 
/*     */   public IdcCounter(String key, COUNTER_TYPE type, String name, long initValue, int increment, long minvalue)
/*     */   {
/*  72 */     this.m_key = key;
/*  73 */     this.m_type = type;
/*  74 */     this.m_name = name;
/*  75 */     this.m_nativeName = ("IdcSeq" + name);
/*  76 */     this.m_initValue = initValue;
/*  77 */     this.m_increment = increment;
/*  78 */     this.m_minValue = minvalue;
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/*  83 */     loadBinder();
/*     */   }
/*     */ 
/*     */   public long currentValue(Workspace ws) throws DataException
/*     */   {
/*  88 */     ResultSet rset = null;
/*  89 */     String nextValueStr = null;
/*  90 */     switch (1.$SwitchMap$intradoc$data$IdcCounter$COUNTER_TYPE[this.m_type.ordinal()])
/*     */     {
/*     */     case 1:
/*  99 */       if (WorkspaceUtils.isDatabaseType(ws, DatabaseTypes.DB2))
/*     */       {
/* 101 */         ws.createResultSet("QnativeNextCounter", this.m_binder);
/*     */       }
/* 103 */       rset = ws.createResultSet("QnativeCurrentCounter", this.m_binder);
/* 104 */       nextValueStr = rset.getStringValueByName("dCurrentIndex");
/* 105 */       break;
/*     */     case 2:
/* 107 */       rset = ws.createResultSet("QcurrentCounter", this.m_binder);
/* 108 */       nextValueStr = rset.getStringValueByName("dNextIndex");
/* 109 */       break;
/*     */     default:
/* 112 */       throw new DataException(null, "csCounterUnknownCounterType", new Object[] { this.m_name, this.m_type.toString() });
/*     */     }
/*     */ 
/* 115 */     if (nextValueStr == null)
/*     */     {
/* 117 */       throw new DataException(null, "csCounterCurrentValueNotFound", new Object[] { this.m_name });
/*     */     }
/* 119 */     return NumberUtils.parseLong(nextValueStr, -1L);
/*     */   }
/*     */ 
/*     */   public long nextValue(Workspace ws) throws DataException {
/* 123 */     ResultSet rset = null;
/* 124 */     String nextValueStr = null;
/* 125 */     switch (1.$SwitchMap$intradoc$data$IdcCounter$COUNTER_TYPE[this.m_type.ordinal()])
/*     */     {
/*     */     case 1:
/* 128 */       if (WorkspaceUtils.isDatabaseType(ws, DatabaseTypes.MSSQL))
/*     */       {
/* 130 */         CallableResults crset = ws.executeCallable("CnativeNextCounter", this.m_binder);
/* 131 */         return crset.getLong("dNextIndex");
/*     */       }
/*     */ 
/* 134 */       rset = ws.createResultSet("QnativeNextCounter", this.m_binder);
/* 135 */       nextValueStr = rset.getStringValueByName("dNextIndex");
/* 136 */       break;
/*     */     case 2:
/* 138 */       rset = ws.createResultSet("QcurrentCounter", this.m_binder);
/* 139 */       nextValueStr = rset.getStringValueByName("dNextIndex");
/* 140 */       ws.execute("UnextCounter", this.m_binder);
/* 141 */       break;
/*     */     default:
/* 144 */       throw new DataException(null, "csCounterUnknownCounterType", new Object[] { this.m_name, this.m_type.toString() });
/*     */     }
/* 146 */     if (nextValueStr == null)
/*     */     {
/* 148 */       throw new DataException(null, "csCounterNextValueNotFound", new Object[] { this.m_name });
/*     */     }
/* 150 */     return NumberUtils.parseLong(nextValueStr, -1L);
/*     */   }
/*     */ 
/*     */   public synchronized DataBinder loadBinder()
/*     */   {
/* 155 */     if (this.m_binder != null)
/*     */     {
/* 157 */       return this.m_binder;
/*     */     }
/*     */ 
/* 160 */     this.m_binder = new DataBinder();
/* 161 */     this.m_binder.putLocal("dCounterName", this.m_name);
/* 162 */     this.m_binder.putLocal("dMinValue", "" + this.m_minValue);
/* 163 */     this.m_binder.putLocal("dCounterInitValue", "" + this.m_initValue);
/* 164 */     this.m_binder.putLocal("dCounterIncrement", "" + this.m_increment);
/* 165 */     this.m_binder.putLocal("dCounterType", this.m_type.name());
/* 166 */     this.m_binder.putLocal("dNativeCounterName", this.m_nativeName);
/* 167 */     return this.m_binder;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 172 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99530 $";
/*     */   }
/*     */ 
/*     */   public static enum COUNTER_TYPE
/*     */   {
/*  24 */     Native, Table;
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.IdcCounter
 * JD-Core Version:    0.5.4
 */