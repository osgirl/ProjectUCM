/*     */ package intradoc.search;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class InternetSearchQueryField
/*     */ {
/*  28 */   public int m_type = -1;
/*  29 */   public char[] m_values = null;
/*     */ 
/*  31 */   public Map<String, char[]> m_processedValueMap = new Hashtable();
/*  32 */   public int m_length = 0;
/*     */   public char[] m_wildCardAny;
/*     */   public char[] m_wildCardOne;
/*     */   public char m_wildCardTextAny;
/*     */   public char m_wildCardTextOne;
/*  37 */   public boolean m_containsWildCard = false;
/*  38 */   public ArrayList m_wcAnyList = new ArrayList();
/*  39 */   public ArrayList m_wcOneList = new ArrayList();
/*     */ 
/*     */   public InternetSearchQueryField(String wildCardAny, String wildCardOne)
/*     */   {
/*  43 */     this.m_wildCardAny = wildCardAny.toCharArray();
/*  44 */     this.m_wildCardOne = wildCardOne.toCharArray();
/*     */   }
/*     */ 
/*     */   public void setType(int type) {
/*  48 */     this.m_type = type;
/*     */   }
/*     */ 
/*     */   public int getType(int type)
/*     */   {
/*  53 */     return this.m_type;
/*     */   }
/*     */ 
/*     */   public void setValue(String value)
/*     */   {
/*  58 */     if (value == null)
/*     */       return;
/*  60 */     this.m_values = value.toCharArray();
/*  61 */     this.m_length = this.m_values.length;
/*  62 */     updateValues();
/*     */   }
/*     */ 
/*     */   public void setProcessedValue(String operator, String value)
/*     */   {
/*  75 */     if (value == null)
/*     */       return;
/*  77 */     this.m_processedValueMap.put(operator, value.toCharArray());
/*     */   }
/*     */ 
/*     */   public void setValue(char[] value, int start, int len)
/*     */   {
/*  83 */     if ((value == null) || (value.length < start + len) || (start < 0) || (len <= 0))
/*     */       return;
/*  85 */     this.m_values = new char[len];
/*  86 */     System.arraycopy(value, start, this.m_values, 0, len);
/*  87 */     this.m_length = len;
/*  88 */     updateValues();
/*     */   }
/*     */ 
/*     */   public void updateValues()
/*     */   {
/*  94 */     if (this.m_values == null)
/*     */     {
/*  96 */       return;
/*     */     }
/*     */ 
/*  99 */     if (this.m_type == 21)
/*     */     {
/* 101 */       int shift = 0;
/* 102 */       for (int i = 0; i < this.m_values.length; ++i)
/*     */       {
/* 104 */         if (this.m_values[i] == '"')
/*     */         {
/* 106 */           ++shift;
/* 107 */           ++i;
/* 108 */           this.m_length -= 1;
/*     */         }
/* 110 */         if (shift == 0)
/*     */           continue;
/* 112 */         this.m_values[(i - shift)] = this.m_values[i];
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 118 */       int shift = 0;
/*     */ 
/* 120 */       char[] originalValue = new char[this.m_length];
/* 121 */       System.arraycopy(this.m_values, 0, originalValue, 0, this.m_length);
/* 122 */       char[] replaceChars = null;
/*     */ 
/* 124 */       for (int i = 0; i < originalValue.length; ++i)
/*     */       {
/* 126 */         int originalShift = shift;
/* 127 */         replaceChars = null;
/*     */ 
/* 129 */         if (originalValue[i] == '*')
/*     */         {
/* 131 */           replaceChars = this.m_wildCardAny;
/* 132 */           this.m_wcAnyList.add(new Integer(i + shift));
/* 133 */           shift = shift + this.m_wildCardAny.length - 1;
/*     */         }
/* 135 */         else if (originalValue[i] == '?')
/*     */         {
/* 137 */           replaceChars = this.m_wildCardOne;
/* 138 */           this.m_wcOneList.add(new Integer(i + shift));
/* 139 */           shift = shift + this.m_wildCardOne.length - 1;
/*     */         }
/*     */ 
/* 142 */         if ((shift <= 0) && (replaceChars == null)) {
/*     */           continue;
/*     */         }
/*     */ 
/* 146 */         if (replaceChars == null)
/*     */         {
/* 148 */           replaceChars = new char[] { originalValue[i] };
/*     */         }
/*     */ 
/* 151 */         if (i + originalShift + replaceChars.length > this.m_values.length)
/*     */         {
/* 153 */           char[] newValueArray = new char[i + originalShift + replaceChars.length];
/* 154 */           System.arraycopy(this.m_values, 0, newValueArray, 0, this.m_values.length);
/* 155 */           this.m_values = newValueArray;
/*     */         }
/*     */ 
/* 158 */         if (replaceChars.length > 1)
/*     */         {
/* 160 */           System.arraycopy(replaceChars, 0, this.m_values, i + originalShift, replaceChars.length);
/*     */         }
/*     */         else
/*     */         {
/* 164 */           this.m_values[(i + originalShift)] = replaceChars[0];
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 169 */       this.m_length = this.m_values.length;
/*     */     }
/*     */   }
/*     */ 
/*     */   public char[] getValue(boolean useProcessedValue, String operator)
/*     */   {
/* 184 */     if (this.m_values == null)
/*     */     {
/* 186 */       return null;
/*     */     }
/*     */ 
/* 189 */     int length = this.m_length;
/* 190 */     char[] valueToCopy = this.m_values;
/* 191 */     if ((useProcessedValue == true) && (this.m_processedValueMap != null) && (operator != null))
/*     */     {
/* 193 */       char[] processedValue = (char[])this.m_processedValueMap.get(operator);
/*     */ 
/* 195 */       if (processedValue == null)
/*     */       {
/* 197 */         processedValue = (char[])this.m_processedValueMap.get(operator.toLowerCase());
/*     */       }
/*     */ 
/* 200 */       if (processedValue != null)
/*     */       {
/* 202 */         valueToCopy = processedValue;
/* 203 */         length = valueToCopy.length;
/*     */       }
/*     */     }
/*     */ 
/* 207 */     char[] value = new char[length];
/* 208 */     System.arraycopy(valueToCopy, 0, value, 0, value.length);
/* 209 */     return value;
/*     */   }
/*     */ 
/*     */   public char[] getValue()
/*     */   {
/* 214 */     return getValue(false, null);
/*     */   }
/*     */ 
/*     */   public char[] getTextValue(char[][] wildCards, boolean useProcessedValue, String operator)
/*     */   {
/* 219 */     char[] value = getValue(useProcessedValue, operator);
/*     */     Iterator iter;
/* 220 */     if ((containsWildCard()) && (value != null) && (wildCards != null))
/*     */     {
/* 222 */       for (Iterator iter = this.m_wcAnyList.iterator(); iter.hasNext(); )
/*     */       {
/* 224 */         Integer integer = (Integer)iter.next();
/*     */ 
/* 226 */         if (wildCards[0].length > 1)
/*     */         {
/* 228 */           System.arraycopy(wildCards[0], 0, value, integer.intValue(), wildCards[0].length);
/*     */         }
/*     */         else
/*     */         {
/* 232 */           value[integer.intValue()] = wildCards[0][0];
/*     */         }
/*     */       }
/* 235 */       for (iter = this.m_wcOneList.iterator(); iter.hasNext(); )
/*     */       {
/* 237 */         Integer integer = (Integer)iter.next();
/*     */ 
/* 239 */         if (wildCards[1].length > 1)
/*     */         {
/* 241 */           System.arraycopy(wildCards[1], 0, value, integer.intValue(), wildCards[1].length);
/*     */         }
/*     */         else
/*     */         {
/* 245 */           value[integer.intValue()] = wildCards[1][0];
/*     */         }
/*     */       }
/*     */     }
/* 249 */     return value;
/*     */   }
/*     */ 
/*     */   public char[] getTextValue(char[][] wildCards)
/*     */   {
/* 254 */     return getTextValue(wildCards, false, null);
/*     */   }
/*     */ 
/*     */   public boolean containsWildCard()
/*     */   {
/* 259 */     return (this.m_wcAnyList.size() > 0) || (this.m_wcOneList.size() > 0);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 264 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 91887 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.InternetSearchQueryField
 * JD-Core Version:    0.5.4
 */