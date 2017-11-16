/*     */ package intradoc.server.project;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.PropertiesTreeNode;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.StringTokenizer;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ProjectUtils
/*     */ {
/*  32 */   protected static DataBinder m_decoder = null;
/*     */ 
/*     */   public static void checkDecoderInit()
/*     */   {
/*  36 */     if (m_decoder != null)
/*     */       return;
/*  38 */     m_decoder = new DataBinder();
/*  39 */     m_decoder.m_isCgi = true;
/*     */   }
/*     */ 
/*     */   public static Properties parseSourcePath(String sourcePath, String instanceKey, String beginTag)
/*     */     throws ServiceException
/*     */   {
/*  45 */     int index = sourcePath.indexOf(beginTag);
/*  46 */     if (index < 0)
/*     */     {
/*  48 */       String msg = LocaleUtils.encodeMessage("csProjSourcePathInvalidPrefix", null, sourcePath, beginTag);
/*     */ 
/*  50 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/*  53 */     sourcePath = sourcePath.substring(index + beginTag.length());
/*     */ 
/*  55 */     String instanceName = null;
/*  56 */     int stopIndex = sourcePath.indexOf(63);
/*  57 */     if (stopIndex < 0)
/*     */     {
/*  59 */       instanceName = sourcePath.substring(0);
/*     */     }
/*     */     else
/*     */     {
/*  63 */       instanceName = sourcePath.substring(0, stopIndex);
/*     */     }
/*     */ 
/*  66 */     Properties props = new Properties();
/*  67 */     props.put(instanceKey, instanceName);
/*     */ 
/*  69 */     if (stopIndex < 0)
/*     */     {
/*  71 */       return props;
/*     */     }
/*     */ 
/*  75 */     checkDecoderInit();
/*     */ 
/*  77 */     sourcePath = sourcePath.substring(stopIndex + 1);
/*     */ 
/*  79 */     StringTokenizer tokenizer = new StringTokenizer(sourcePath, "&");
/*  80 */     while (tokenizer.hasMoreElements())
/*     */     {
/*  82 */       String token = tokenizer.nextToken();
/*  83 */       String key = null;
/*  84 */       String value = "";
/*  85 */       index = token.indexOf("=");
/*  86 */       if (index < 0)
/*     */       {
/*  88 */         key = token;
/*     */       }
/*     */       else
/*     */       {
/*  92 */         key = token.substring(0, index);
/*  93 */         if (index < token.length())
/*     */         {
/*  95 */           value = m_decoder.decode(token.substring(index + 1));
/*     */         }
/*     */       }
/*  98 */       props.put(key, value);
/*     */     }
/* 100 */     return props;
/*     */   }
/*     */ 
/*     */   public static Properties findNode(String pagePathID, List nodes)
/*     */   {
/* 105 */     int numNodes = nodes.size();
/* 106 */     for (int i = 0; i < numNodes; ++i)
/*     */     {
/* 108 */       PropertiesTreeNode node = (PropertiesTreeNode)nodes.get(i);
/* 109 */       Properties props = node.m_properties;
/* 110 */       String curID = props.getProperty("pagePathID");
/* 111 */       if ((curID != null) && (curID.equals(pagePathID)))
/*     */       {
/* 113 */         return props;
/*     */       }
/* 115 */       props = findNode(pagePathID, node.m_subNodes);
/* 116 */       if (props != null)
/*     */       {
/* 118 */         return props;
/*     */       }
/*     */     }
/* 121 */     return null;
/*     */   }
/*     */ 
/*     */   public static DataResultSet computePreviewResultSet(Hashtable projects) throws ServiceException
/*     */   {
/* 126 */     String[] columns = { "bodyType", "parent", "nestingLevel", "projectID", "description", "name", "pagePathID", "repoid", "queryPath", "queryUrl", "sourcePath", "urlPath", "template", "ttype", "templateSourcePath", "templateUrlPath" };
/*     */ 
/* 131 */     DataResultSet prjSet = new DataResultSet(columns);
/* 132 */     checkDecoderInit();
/*     */ 
/* 134 */     for (Enumeration en = projects.elements(); en.hasMoreElements(); )
/*     */     {
/* 136 */       Vector resNodes = (Vector)en.nextElement();
/* 137 */       int num = resNodes.size();
/* 138 */       for (int i = 0; i < num; ++i)
/*     */       {
/* 140 */         PropertiesTreeNode node = (PropertiesTreeNode)resNodes.elementAt(i);
/* 141 */         String parent = node.m_properties.getProperty("projectID");
/* 142 */         Vector row = createRowWithDefaults("top", 0, node.m_name, columns, node.m_properties);
/* 143 */         prjSet.addRow(row);
/*     */ 
/* 145 */         chaseNode(prjSet, columns, node, parent, 1);
/*     */       }
/*     */     }
/* 148 */     return prjSet;
/*     */   }
/*     */ 
/*     */   protected static void chaseNode(DataResultSet prjSet, String[] columns, PropertiesTreeNode node, String parent, int nestingLevel)
/*     */     throws ServiceException
/*     */   {
/* 154 */     Vector nodes = node.m_subNodes;
/* 155 */     int num = nodes.size();
/* 156 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 158 */       PropertiesTreeNode subNode = (PropertiesTreeNode)nodes.elementAt(i);
/* 159 */       Vector row = createRowWithDefaults(parent, nestingLevel, subNode.m_name, columns, subNode.m_properties);
/* 160 */       prjSet.addRow(row);
/*     */ 
/* 162 */       String subParent = subNode.m_properties.getProperty("name");
/* 163 */       chaseNode(prjSet, columns, subNode, subParent, nestingLevel + 1);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static Vector createRowWithDefaults(String parent, int nestingLevel, String name, String[] columns, Properties props)
/*     */     throws ServiceException
/*     */   {
/* 170 */     int num = columns.length;
/* 171 */     Vector row = new IdcVector(num);
/* 172 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 174 */       String key = columns[i];
/* 175 */       String value = null;
/* 176 */       if (key.equals("bodyType"))
/*     */       {
/* 178 */         value = name;
/*     */       }
/* 180 */       else if (key.equals("nestingLevel"))
/*     */       {
/* 182 */         value = String.valueOf(nestingLevel);
/*     */       }
/* 184 */       else if (key.equals("parent"))
/*     */       {
/* 186 */         value = parent;
/*     */       }
/*     */       else
/*     */       {
/* 190 */         value = props.getProperty(key);
/*     */       }
/* 192 */       if (value == null)
/*     */       {
/* 194 */         value = "";
/*     */       }
/*     */       else
/*     */       {
/* 198 */         value = m_decoder.decode(value);
/*     */       }
/* 200 */       row.addElement(value);
/*     */     }
/* 202 */     return row;
/*     */   }
/*     */ 
/*     */   public static int computeHitCount(DataResultSet prSet) throws DataException
/*     */   {
/* 207 */     String[] keys = { "bodyType", "projectID" };
/* 208 */     FieldInfo[] infos = ResultSetUtils.createInfoList(prSet, keys, true);
/* 209 */     int typeIndex = infos[0].m_index;
/* 210 */     int idIndex = infos[1].m_index;
/*     */ 
/* 212 */     Properties firstDocNode = null;
/* 213 */     String firstProjectID = null;
/* 214 */     int hitCount = 0;
/* 215 */     for (prSet.first(); prSet.isRowPresent(); prSet.next())
/*     */     {
/* 217 */       String type = prSet.getStringValue(typeIndex);
/* 218 */       if ((firstProjectID == null) && (type.equals("project")))
/*     */       {
/* 220 */         firstProjectID = prSet.getStringValue(idIndex);
/*     */       } else {
/* 222 */         if ((!type.equals("document")) && (!type.equals("directory")))
/*     */           continue;
/* 224 */         ++hitCount;
/* 225 */         if (firstDocNode != null)
/*     */           continue;
/* 227 */         firstDocNode = prSet.getCurrentRowProps();
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 232 */     return hitCount;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 237 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.project.ProjectUtils
 * JD-Core Version:    0.5.4
 */