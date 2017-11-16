/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.shared.QueryElementField;
/*     */ import java.util.List;
/*     */ 
/*     */ public class QueryElement
/*     */ {
/*     */   public int m_type;
/*     */   public int m_operator;
/*     */   public int m_fieldIndexIntoQueryList;
/*     */   public QueryElementField m_field;
/*     */   public QueryElementValue m_value;
/*     */   public List<QueryElement> m_subElements;
/*     */ 
/*     */   public QueryElement(QueryElementField field, int operator, String originalValue, Object convertedValue)
/*     */   {
/*  71 */     this.m_type = 100;
/*  72 */     this.m_fieldIndexIntoQueryList = -1;
/*  73 */     this.m_field = field;
/*  74 */     this.m_operator = operator;
/*  75 */     if (originalValue == null)
/*     */       return;
/*  77 */     this.m_value = new QueryElementValue(originalValue, convertedValue);
/*     */   }
/*     */ 
/*     */   public QueryElement(String value)
/*     */   {
/*  88 */     this.m_type = 100;
/*  89 */     if (value != null)
/*     */     {
/*  91 */       this.m_value = new QueryElementValue(value, null);
/*     */     }
/*  93 */     this.m_operator = 5;
/*     */   }
/*     */ 
/*     */   public QueryElement(List subElements)
/*     */   {
/* 102 */     this.m_type = 101;
/* 103 */     this.m_subElements = subElements;
/* 104 */     this.m_operator = 16;
/*     */   }
/*     */ 
/*     */   public QueryElement(List<QueryElement> subElements, int opCode)
/*     */   {
/* 115 */     this.m_type = 101;
/* 116 */     this.m_subElements = subElements;
/* 117 */     this.m_operator = opCode;
/*     */   }
/*     */ 
/*     */   public void appendDebugFormat(IdcAppendable appendable)
/*     */   {
/* 126 */     String opString = SearchQueryUtils.convertToString(this.m_operator);
/* 127 */     if (SearchQueryUtils.hasNotFlag(this.m_operator))
/*     */     {
/* 129 */       appendable.append("not ");
/*     */     }
/* 131 */     switch (this.m_type)
/*     */     {
/*     */     case 101:
/* 135 */       if ((this.m_subElements == null) || (this.m_subElements.size() == 0))
/*     */       {
/* 137 */         appendable.append("()"); return;
/*     */       }
/*     */ 
/* 141 */       for (int i = 0; i < this.m_subElements.size(); ++i)
/*     */       {
/* 143 */         if (i > 0)
/*     */         {
/* 145 */           appendable.append(" ");
/* 146 */           appendable.append(opString);
/* 147 */           appendable.append(" ");
/*     */         }
/* 149 */         QueryElement elt = (QueryElement)this.m_subElements.get(i);
/* 150 */         appendable.append("(");
/* 151 */         elt.appendDebugFormat(appendable);
/* 152 */         appendable.append(")");
/*     */       }
/*     */ 
/* 155 */       break;
/*     */     case 100:
/* 159 */       if (this.m_operator != 5)
/*     */       {
/* 161 */         if (this.m_field != null)
/*     */         {
/* 163 */           appendable.append(this.m_field.m_name);
/*     */         }
/*     */         else
/*     */         {
/* 167 */           appendable.append("[nofield]");
/*     */         }
/*     */ 
/*     */       }
/*     */       else {
/* 172 */         appendable.append("[Content]");
/*     */       }
/* 174 */       appendable.append(" ");
/* 175 */       appendable.append(opString);
/* 176 */       appendable.append(" ");
/* 177 */       appendable.append("`");
/* 178 */       if ((this.m_value != null) && (this.m_value.m_originalValue != null))
/*     */       {
/* 180 */         appendable.append(this.m_value.m_originalValue);
/*     */       }
/* 182 */       appendable.append("`");
/* 183 */       break;
/*     */     default:
/* 186 */       appendable.append("queryElementInvalidType");
/*     */     }
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 198 */     IdcStringBuilder output = new IdcStringBuilder();
/* 199 */     appendDebugFormat(output);
/* 200 */     return output.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 205 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 74666 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.QueryElement
 * JD-Core Version:    0.5.4
 */