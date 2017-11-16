/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.indexer.OracleTextUtils;
/*     */ import intradoc.shared.MetaFieldUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Vector;
/*     */ import org.xml.sax.Attributes;
/*     */ import org.xml.sax.ContentHandler;
/*     */ import org.xml.sax.Locator;
/*     */ import org.xml.sax.SAXException;
/*     */ 
/*     */ public class DrillDownContentHandler
/*     */   implements ContentHandler
/*     */ {
/*     */   protected DataBinder m_binder;
/*     */   public String m_value;
/*     */   public String m_count;
/*     */   protected boolean m_isInCount;
/*     */   protected List<DataResultSet> m_navRSets;
/*     */   protected List<String> m_fields;
/*     */   protected int m_numFields;
/*     */   protected char m_fieldSeparator;
/*     */   protected int m_totalCount;
/*     */   public int m_numGroups;
/*     */ 
/*     */   public DrillDownContentHandler()
/*     */   {
/*  43 */     this.m_binder = null;
/*     */ 
/*  45 */     this.m_value = null;
/*  46 */     this.m_count = null;
/*     */ 
/*  48 */     this.m_isInCount = false;
/*     */ 
/*  50 */     this.m_navRSets = new ArrayList();
/*     */ 
/*  52 */     this.m_fields = null;
/*  53 */     this.m_numFields = 0;
/*     */ 
/*  56 */     this.m_totalCount = 0;
/*     */ 
/*  58 */     this.m_numGroups = 0;
/*     */   }
/*     */ 
/*     */   public void init(DataBinder binder, List fields, char fieldSeparator) {
/*  62 */     this.m_binder = binder;
/*  63 */     this.m_fields = fields;
/*  64 */     this.m_fieldSeparator = fieldSeparator;
/*  65 */     if (this.m_fields == null)
/*     */       return;
/*  67 */     this.m_numFields = this.m_fields.size();
/*     */   }
/*     */ 
/*     */   public void characters(char[] ch, int start, int length)
/*     */     throws SAXException
/*     */   {
/*  74 */     if (!this.m_isInCount)
/*     */       return;
/*  76 */     this.m_count = new String(ch, start, length);
/*     */   }
/*     */ 
/*     */   public void endDocument()
/*     */     throws SAXException
/*     */   {
/*  83 */     if (this.m_totalCount <= 0)
/*     */       return;
/*  85 */     DataResultSet fields = new DataResultSet(new String[] { "drillDownFieldName", "drillDownDisplayValue", "categoryCount", "totalCount", "drillDownDisplayViewName" });
/*  86 */     for (int i = 0; i < this.m_numFields; ++i)
/*     */     {
/*  88 */       DataResultSet drset = (DataResultSet)this.m_navRSets.get(i);
/*  89 */       String fieldName = (String)this.m_fields.get(i);
/*  90 */       String fieldCaption = null;
/*  91 */       ResultSet metaDefs = SharedObjects.getTable("DocMetaDefinition");
/*     */       try
/*     */       {
/*  94 */         fieldCaption = ResultSetUtils.findValue(metaDefs, "dName", fieldName, "dCaption");
/*  95 */         ResultSetUtils.sortResultSet(drset, new String[] { "drillDownOptionValue" });
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/*  99 */         Report.trace("search", null, e);
/*     */       }
/* 101 */       this.m_binder.addResultSetDirect("SearchResultNavigation" + fieldName, drset);
/*     */ 
/* 103 */       Vector row = new Vector();
/* 104 */       row.add(fieldName);
/* 105 */       if (fieldCaption != null)
/*     */       {
/* 107 */         row.add(fieldCaption);
/*     */       }
/*     */       else
/*     */       {
/* 111 */         row.add("ww" + fieldName);
/*     */       }
/* 113 */       row.add("" + drset.getNumRows());
/* 114 */       row.add("" + this.m_totalCount);
/* 115 */       row.add(MetaFieldUtils.determineViewName(fieldName));
/* 116 */       fields.addRow(row);
/*     */     }
/*     */ 
/* 119 */     this.m_binder.addResultSet("SearchResultNavigation", fields);
/*     */   }
/*     */ 
/*     */   public void endElement(String uri, String localName, String name)
/*     */     throws SAXException
/*     */   {
/* 127 */     if (localName.equals("count"))
/*     */     {
/* 129 */       this.m_isInCount = false;
/*     */     } else {
/* 131 */       if (!localName.equals("group"))
/*     */         return;
/* 133 */       if (this.m_navRSets.size() == 0)
/*     */       {
/* 135 */         for (int i = 0; i < this.m_numFields; ++i)
/*     */         {
/* 137 */           DataResultSet drset = new DataResultSet(new String[] { "drillDownOptionValue", "drillDownModifier", "count", "fieldName" });
/* 138 */           this.m_navRSets.add(drset);
/*     */         }
/*     */       }
/* 141 */       if ((this.m_value != null) && (this.m_count != null))
/*     */       {
/* 143 */         int count = NumberUtils.parseInteger(this.m_count, 0);
/* 144 */         this.m_totalCount += count;
/* 145 */         List keyList = StringUtils.makeListFromSequence(this.m_value, this.m_fieldSeparator, '^', 0);
/* 146 */         if (keyList.size() == this.m_numFields)
/*     */         {
/* 148 */           for (int i = 0; i < this.m_numFields; ++i)
/*     */           {
/* 150 */             DataResultSet drset = (DataResultSet)this.m_navRSets.get(i);
/* 151 */             String key = (String)keyList.get(i);
/* 152 */             if ((key != null) && (key.equalsIgnoreCase("idcnull")))
/*     */             {
/* 154 */               key = "";
/*     */             }
/*     */ 
/* 157 */             key = OracleTextUtils.decodeValue(key, false);
/* 158 */             Vector row = drset.findRow(0, key);
/* 159 */             if (row == null)
/*     */             {
/* 161 */               row = new Vector();
/* 162 */               row.add(key);
/* 163 */               row.add(key);
/* 164 */               row.add("" + count);
/* 165 */               row.add("" + (String)this.m_fields.get(i));
/* 166 */               drset.addRow(row);
/*     */             }
/*     */             else
/*     */             {
/* 170 */               int tmpCount = count + NumberUtils.parseInteger((String)row.elementAt(2), 0);
/* 171 */               row.set(2, "" + tmpCount);
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/* 176 */       this.m_numGroups += 1;
/* 177 */       this.m_value = null;
/* 178 */       this.m_count = null;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void endPrefixMapping(String prefix)
/*     */     throws SAXException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void ignorableWhitespace(char[] ch, int start, int length)
/*     */     throws SAXException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void processingInstruction(String target, String data)
/*     */     throws SAXException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void setDocumentLocator(Locator locator)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void skippedEntity(String name)
/*     */     throws SAXException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void startDocument()
/*     */     throws SAXException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void startElement(String uri, String localName, String name, Attributes atts)
/*     */     throws SAXException
/*     */   {
/* 221 */     if (localName.equals("count"))
/*     */     {
/* 223 */       this.m_isInCount = true;
/*     */     } else {
/* 225 */       if (!localName.equals("group"))
/*     */         return;
/* 227 */       this.m_value = atts.getValue("value");
/*     */     }
/*     */   }
/*     */ 
/*     */   public void startPrefixMapping(String prefix, String uri)
/*     */     throws SAXException
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 240 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94623 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.DrillDownContentHandler
 * JD-Core Version:    0.5.4
 */