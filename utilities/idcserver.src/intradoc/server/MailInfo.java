/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ResourceContainerUtils;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.shared.AdditionalRenditions;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class MailInfo
/*     */ {
/*     */   public static final int ALIAS = 0;
/*     */   public static final int ALIAS_TYPE = 1;
/*     */   public static final int ALIAS_EMAIL = 2;
/*     */   public static final int COLLATED_ID = 0;
/*     */   public static final int COLLATED_DOCNAME = 1;
/*     */   public static final int COLLATED_DOCTITLE = 2;
/*     */   public static final int COLLATED_REVLABEL = 3;
/*  51 */   public static final String[] FIELDS = { "dSubscriptionAlias", "dSubscriptionAliasType", "dSubscriptionEmail", "dSubscriptionType", "dSubscriptionID" };
/*     */   public static Vector m_collatedFieldsList;
/*  56 */   public static boolean m_isFinishedCollatedFieldList = false;
/*  57 */   public String[] m_fields = null;
/*  58 */   public Vector[] m_collatedFields = null;
/*     */ 
/*     */   public static Vector getCollatedFieldList()
/*     */   {
/*  63 */     if ((m_collatedFieldsList == null) || (!m_isFinishedCollatedFieldList))
/*     */     {
/*  65 */       initCollatedFieldList(false);
/*     */     }
/*  67 */     return m_collatedFieldsList;
/*     */   }
/*     */ 
/*     */   public static Vector initCollatedFieldList(boolean forceInitialize)
/*     */   {
/*  80 */     if ((m_collatedFieldsList == null) || (forceInitialize == true))
/*     */     {
/*  82 */       Hashtable map = new Hashtable();
/*  83 */       m_collatedFieldsList = new IdcVector();
/*     */ 
/*  86 */       String[] coreCollatedFields = ResourceContainerUtils.getDynamicFieldListResource("MailInfoCollatedFields");
/*     */ 
/*  90 */       if ((!coreCollatedFields[0].equals("dID")) || (!coreCollatedFields[1].equals("dDocName")))
/*     */       {
/*  92 */         String msg = LocaleUtils.encodeMessage("csMailCollatedFieldsException", null);
/*  93 */         msg = LocaleResources.localizeMessage(msg, null);
/*  94 */         Report.error("mail", msg, null);
/*     */       }
/*     */ 
/*  97 */       for (int i = 0; i < coreCollatedFields.length; ++i)
/*     */       {
/*  99 */         m_collatedFieldsList.addElement(coreCollatedFields[i]);
/* 100 */         map.put(coreCollatedFields[i], "1");
/*     */       }
/*     */ 
/* 103 */       int numRenditions = AdditionalRenditions.m_maxNum;
/* 104 */       for (int i = 0; i < numRenditions; ++i)
/*     */       {
/* 106 */         String renKey = "dRendition" + (i + 1);
/* 107 */         map.put(renKey, "1");
/*     */       }
/*     */ 
/* 117 */       DataResultSet rset = SharedObjects.getTable("DocMetaDefinition");
/*     */       try
/*     */       {
/* 120 */         FieldInfo[] infos = ResultSetUtils.createInfoList(rset, new String[] { "dName", "dIsEnabled", "dIsSearchable", "dIsPlaceholderField" }, true);
/*     */ 
/* 122 */         for (rset.first(); rset.isRowPresent(); rset.next())
/*     */         {
/* 124 */           String isEnabled = rset.getStringValue(infos[1].m_index);
/* 125 */           String isSearchable = rset.getStringValue(infos[2].m_index);
/* 126 */           String isPlaceholderField = rset.getStringValue(infos[3].m_index);
/* 127 */           if ((!StringUtils.convertToBool(isEnabled, false)) || (!StringUtils.convertToBool(isSearchable, false)) || (StringUtils.convertToBool(isPlaceholderField, false))) {
/*     */             continue;
/*     */           }
/*     */ 
/* 131 */           String name = rset.getStringValue(infos[0].m_index);
/* 132 */           m_collatedFieldsList.addElement(name);
/* 133 */           map.put(name, "1");
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (DataException ignore)
/*     */       {
/* 140 */         String msg = LocaleUtils.encodeMessage("csMailDataException", ignore.getMessage());
/*     */ 
/* 142 */         Report.trace(null, LocaleResources.localizeMessage(msg, null), ignore);
/*     */       }
/*     */ 
/* 146 */       String str = SharedObjects.getEnvironmentValue("AdditionalSubscriptionCollatedFieldList");
/* 147 */       Vector names = StringUtils.parseArray(str, ',', '^');
/* 148 */       int size = names.size();
/* 149 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 151 */         String name = (String)names.elementAt(i);
/* 152 */         Object obj = map.get(name);
/* 153 */         if (obj != null)
/*     */           continue;
/* 155 */         m_collatedFieldsList.addElement(name);
/* 156 */         map.put(name, "1");
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 162 */     if ((!m_isFinishedCollatedFieldList) || (forceInitialize == true))
/*     */     {
/* 164 */       m_isFinishedCollatedFieldList = updateCollatedFieldList("QVLocationCols", m_collatedFieldsList);
/*     */     }
/*     */ 
/* 167 */     return m_collatedFieldsList;
/*     */   }
/*     */ 
/*     */   public static boolean updateCollatedFieldList(String config, Vector v)
/*     */   {
/* 182 */     String qvStr = SharedObjects.getEnvironmentValue(config);
/* 183 */     Hashtable map = new Hashtable();
/* 184 */     if (qvStr == null)
/*     */     {
/* 186 */       return false;
/*     */     }
/* 188 */     Vector qvNames = StringUtils.parseArray(qvStr, ',', '^');
/* 189 */     int qvSize = qvNames.size();
/* 190 */     for (int i = 0; i < qvSize; ++i)
/*     */     {
/* 192 */       String name = (String)qvNames.elementAt(i);
/* 193 */       name = name.trim();
/* 194 */       if (!name.isEmpty()) {
/* 195 */         Object obj = map.get(name);
/* 196 */         if (obj != null)
/*     */           continue;
/* 198 */         v.addElement(name);
/* 199 */         map.put(name, "1");
/*     */       }
/*     */     }
/*     */ 
/* 203 */     return true;
/*     */   }
/*     */ 
/*     */   public MailInfo(Properties props)
/*     */   {
/* 209 */     m_collatedFieldsList = getCollatedFieldList();
/*     */ 
/* 212 */     int field_size = FIELDS.length;
/* 213 */     this.m_fields = new String[field_size];
/* 214 */     for (int i = 0; i < field_size; ++i)
/*     */     {
/* 216 */       this.m_fields[i] = props.getProperty(FIELDS[i]);
/*     */     }
/*     */ 
/* 220 */     int collated_fields_size = m_collatedFieldsList.size();
/* 221 */     this.m_collatedFields = new IdcVector[collated_fields_size];
/*     */ 
/* 223 */     for (int j = 0; j < collated_fields_size; ++j)
/*     */     {
/* 225 */       this.m_collatedFields[j] = new IdcVector();
/*     */     }
/* 227 */     addCollatedFieldInfo(props);
/*     */   }
/*     */ 
/*     */   public void addCollatedFieldInfo(Properties props)
/*     */   {
/* 232 */     String doc_id = props.getProperty("dID");
/*     */ 
/* 234 */     if ((doc_id == null) || (doc_id.length() == 0))
/*     */     {
/* 236 */       return;
/*     */     }
/*     */ 
/* 240 */     boolean isDocUnique = true;
/* 241 */     Vector v = this.m_collatedFields[0];
/* 242 */     for (int j = 0; j < v.size(); ++j)
/*     */     {
/* 244 */       String temp = (String)v.elementAt(j);
/* 245 */       if (!temp.equals(doc_id))
/*     */         continue;
/* 247 */       isDocUnique = false;
/*     */     }
/*     */ 
/* 251 */     if (!isDocUnique)
/*     */       return;
/* 253 */     int size = m_collatedFieldsList.size();
/* 254 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 256 */       String val = props.getProperty((String)m_collatedFieldsList.elementAt(i));
/* 257 */       v = this.m_collatedFields[i];
/* 258 */       if (val == null)
/*     */       {
/* 260 */         v.addElement("");
/*     */       }
/*     */       else
/*     */       {
/* 264 */         v.addElement(val);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public Properties getInfoProperties()
/*     */   {
/* 272 */     Properties props = new Properties();
/*     */ 
/* 274 */     props.put("action", "mail");
/*     */ 
/* 277 */     for (int i = 0; i < this.m_fields.length; ++i)
/*     */     {
/* 279 */       props.put(FIELDS[i], this.m_fields[i]);
/*     */     }
/*     */ 
/* 283 */     for (int j = 0; j < this.m_collatedFields.length; ++j)
/*     */     {
/* 285 */       Vector v = this.m_collatedFields[j];
/* 286 */       String tmp = (String)m_collatedFieldsList.elementAt(j);
/* 287 */       tmp = tmp + ".collated";
/* 288 */       props.put(tmp, StringUtils.createString(v, ',', '^'));
/*     */     }
/*     */ 
/* 291 */     return props;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 296 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102641 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.MailInfo
 * JD-Core Version:    0.5.4
 */