/*     */ package intradoc.shared.gui;
/*     */ 
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class FilterData
/*     */ {
/*  31 */   public boolean m_isUsed = false;
/*  32 */   public String m_id = null;
/*  33 */   public Vector m_values = new IdcVector();
/*  34 */   public Vector m_operators = new IdcVector();
/*  35 */   public ViewFieldDef m_fieldDef = null;
/*  36 */   public String m_table = "Revisions";
/*     */ 
/*  39 */   public String m_clause = null;
/*     */ 
/*     */   public FilterData(ViewFieldDef def)
/*     */   {
/*  43 */     this(def.m_name, def, 1);
/*     */   }
/*     */ 
/*     */   public FilterData(String name, ViewFieldDef def, int size)
/*     */   {
/*  49 */     this.m_id = name;
/*  50 */     this.m_fieldDef = def;
/*     */ 
/*  53 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  55 */       this.m_values.addElement("");
/*  56 */       this.m_operators.addElement("");
/*     */     }
/*     */   }
/*     */ 
/*     */   public FilterData(String name, String type, String op, String value)
/*     */   {
/*  62 */     this.m_id = name;
/*     */ 
/*  64 */     this.m_fieldDef = new ViewFieldDef();
/*  65 */     this.m_fieldDef.m_name = name;
/*  66 */     this.m_fieldDef.m_type = type;
/*     */ 
/*  68 */     this.m_operators.addElement(op);
/*  69 */     this.m_values.addElement(value);
/*     */   }
/*     */ 
/*     */   public FilterData(String name, String type, int size)
/*     */   {
/*  74 */     this.m_id = name;
/*     */ 
/*  76 */     this.m_fieldDef = new ViewFieldDef();
/*  77 */     this.m_fieldDef.m_name = name;
/*  78 */     this.m_fieldDef.m_type = type;
/*     */ 
/*  81 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  83 */       this.m_values.addElement("");
/*  84 */       this.m_operators.addElement("");
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getValueAt(int index)
/*     */   {
/*  90 */     return (String)this.m_values.elementAt(index);
/*     */   }
/*     */ 
/*     */   public void setValueAt(String value, int index)
/*     */   {
/*  95 */     this.m_values.setElementAt(value, index);
/*     */   }
/*     */ 
/*     */   public String getOperatorAt(int index)
/*     */   {
/* 100 */     return (String)this.m_operators.elementAt(index);
/*     */   }
/*     */ 
/*     */   public void setOperatorAt(String op, int index)
/*     */   {
/* 105 */     this.m_operators.setElementAt(op, index);
/*     */   }
/*     */ 
/*     */   public boolean isSet()
/*     */   {
/* 113 */     if (this.m_isUsed)
/*     */     {
/* 115 */       return true;
/*     */     }
/*     */ 
/* 118 */     int num = this.m_values.size();
/* 119 */     if (num == 0)
/*     */     {
/* 121 */       return false;
/*     */     }
/*     */ 
/* 124 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 126 */       String val = (String)this.m_values.elementAt(i);
/* 127 */       if ((val == null) || (val.length() == 0))
/*     */       {
/* 129 */         return false;
/*     */       }
/*     */     }
/* 132 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 137 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.gui.FilterData
 * JD-Core Version:    0.5.4
 */