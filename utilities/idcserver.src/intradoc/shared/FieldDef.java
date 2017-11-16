/*     */ package intradoc.shared;
/*     */ 
/*     */ public class FieldDef
/*     */ {
/*     */   public String m_name;
/*     */   public String m_caption;
/*     */   public String m_type;
/*     */   public String m_optionListKey;
/*     */   public boolean m_isOptionList;
/*     */   public String m_optionListType;
/*     */   public String m_default;
/*     */ 
/*     */   public FieldDef()
/*     */   {
/*  56 */     this.m_name = null;
/*  57 */     this.m_caption = null;
/*  58 */     this.m_type = "Text";
/*  59 */     this.m_optionListKey = null;
/*  60 */     this.m_isOptionList = false;
/*  61 */     this.m_optionListType = "";
/*  62 */     this.m_default = null;
/*     */   }
/*     */ 
/*     */   public void copy(FieldDef fd)
/*     */   {
/*  67 */     this.m_name = fd.m_name;
/*  68 */     this.m_caption = fd.m_caption;
/*  69 */     this.m_type = fd.m_type;
/*  70 */     this.m_optionListKey = fd.m_optionListKey;
/*  71 */     this.m_isOptionList = fd.m_isOptionList;
/*  72 */     this.m_optionListType = fd.m_optionListType;
/*  73 */     this.m_default = fd.m_default;
/*     */   }
/*     */ 
/*     */   public boolean isMandatoryOptionList()
/*     */   {
/*  78 */     if (!this.m_isOptionList)
/*     */     {
/*  80 */       return false;
/*     */     }
/*  82 */     if (isComplexOptionList())
/*     */     {
/*  84 */       return false;
/*     */     }
/*  86 */     if ((this.m_optionListType == null) || (this.m_optionListType.length() == 0))
/*     */     {
/*  88 */       return true;
/*     */     }
/*     */ 
/*  94 */     return (this.m_optionListType.indexOf("multi") < 0) && (this.m_optionListType.indexOf("combo") < 0) && (this.m_optionListType.indexOf("chunval") < 0);
/*     */   }
/*     */ 
/*     */   public boolean isMultiOptionList()
/*     */   {
/* 101 */     if ((!this.m_isOptionList) || (this.m_optionListType == null))
/*     */     {
/* 103 */       return false;
/*     */     }
/* 105 */     return this.m_optionListType.indexOf("multi") >= 0;
/*     */   }
/*     */ 
/*     */   public boolean isComboOptionList()
/*     */   {
/* 110 */     if ((!this.m_isOptionList) || (this.m_optionListType == null))
/*     */     {
/* 112 */       return false;
/*     */     }
/* 114 */     return this.m_optionListType.indexOf("combo") >= 0;
/*     */   }
/*     */ 
/*     */   public boolean isUnvalidatedList()
/*     */   {
/* 119 */     if ((!this.m_isOptionList) || (this.m_optionListType == null))
/*     */     {
/* 121 */       return false;
/*     */     }
/* 123 */     return this.m_optionListType.indexOf("chunval") >= 0;
/*     */   }
/*     */ 
/*     */   public boolean isComplexOptionList()
/*     */   {
/* 128 */     if (this.m_isOptionList)
/*     */     {
/* 130 */       boolean isView = this.m_optionListKey.startsWith("view://");
/* 131 */       boolean isTree = this.m_optionListKey.startsWith("tree://");
/* 132 */       return (isTree) || (isView);
/*     */     }
/* 134 */     return false;
/*     */   }
/*     */ 
/*     */   public String getViewName()
/*     */   {
/* 139 */     String viewName = null;
/* 140 */     if ((this.m_isOptionList) && (this.m_optionListKey.startsWith("view://")))
/*     */     {
/* 142 */       String tag = "view://";
/* 143 */       viewName = this.m_optionListKey.substring(tag.length());
/*     */     }
/* 145 */     return viewName;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 150 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 69804 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.FieldDef
 * JD-Core Version:    0.5.4
 */