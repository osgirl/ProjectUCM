/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.FeaturesInterface;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.util.IdcIterable;
/*     */ import intradoc.util.IdcIteratorData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.NoSuchElementException;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class BasicFeatureImplementor
/*     */   implements FeaturesInterface, IdcIterable<Map>
/*     */ {
/*     */   public DataResultSet m_featureList;
/*     */   public Map m_featureLevelMap;
/*     */   public Map<String, List> m_featureComponentMap;
/*     */   public Map<String, List> m_componentFeatureMap;
/*     */ 
/*     */   public synchronized void init()
/*     */   {
/*  42 */     if (this.m_featureList != null)
/*     */       return;
/*  44 */     this.m_featureList = new DataResultSet(new String[] { "idcFeatureName", "idcFeatureVersion", "idcFeatureLevel", "idcFeatureComponent" });
/*     */ 
/*  47 */     this.m_featureLevelMap = new HashMap();
/*  48 */     this.m_featureComponentMap = new HashMap();
/*  49 */     this.m_componentFeatureMap = new HashMap();
/*     */   }
/*     */ 
/*     */   public Object getLevel(String featureName)
/*     */   {
/*  62 */     String level = (String)this.m_featureLevelMap.get(featureName);
/*  63 */     return level;
/*     */   }
/*     */ 
/*     */   public boolean checkLevel(String featureName, String level)
/*     */   {
/*  70 */     String featureLevel = (String)getLevel(featureName);
/*  71 */     if (featureLevel == null)
/*     */     {
/*  73 */       return false;
/*     */     }
/*  75 */     int rc = SystemUtils.compareVersions(level, featureLevel);
/*  76 */     return rc <= 0;
/*     */   }
/*     */ 
/*     */   public void require(String featureName, String level, String msg)
/*     */     throws ServiceException
/*     */   {
/*  85 */     if (checkLevel(featureName, level))
/*     */       return;
/*  87 */     String featureLevel = (String)getLevel(featureName);
/*     */     String tmpMsg;
/*     */     String tmpMsg;
/*  89 */     if (featureLevel != null)
/*     */     {
/*  91 */       tmpMsg = LocaleUtils.encodeMessage("syFeatureNotSupported", null, featureName, featureLevel, level);
/*     */     }
/*     */     else
/*     */     {
/*  97 */       tmpMsg = LocaleUtils.encodeMessage("syFeatureNotInstalled", null, featureName);
/*     */     }
/*     */ 
/* 100 */     if (msg == null)
/*     */     {
/* 102 */       msg = tmpMsg;
/*     */     }
/*     */     else
/*     */     {
/* 106 */       msg = LocaleUtils.appendMessage(tmpMsg, msg);
/*     */     }
/* 108 */     throw new ServiceException(msg);
/*     */   }
/*     */ 
/*     */   public void refuse(String featureName, String level, IdcMessage msg)
/*     */     throws ServiceException
/*     */   {
/* 118 */     if (!checkLevel(featureName, level))
/*     */       return;
/* 120 */     String featureLevel = (String)getLevel(featureName);
/* 121 */     IdcMessage tmpMsg = IdcMessageFactory.lc("syFeatureRefused", new Object[] { featureName, "" + featureLevel });
/*     */ 
/* 123 */     if (msg == null)
/*     */     {
/* 125 */       msg = tmpMsg;
/*     */     }
/*     */     else
/*     */     {
/* 129 */       msg = msg.walkToEnd().m_prior = tmpMsg;
/*     */     }
/* 131 */     throw new ServiceException(null, msg);
/*     */   }
/*     */ 
/*     */   public List getFeatureComponents(String featureName)
/*     */   {
/* 137 */     return (List)this.m_featureComponentMap.get(featureName);
/*     */   }
/*     */ 
/*     */   public List getComponentFeatures(String componentName)
/*     */   {
/* 142 */     return (List)this.m_componentFeatureMap.get(componentName);
/*     */   }
/*     */ 
/*     */   public void parseAndAppendFeatures(String str, List features)
/*     */   {
/* 147 */     Vector featureList = StringUtils.parseArray(str, ',', '^');
/* 148 */     for (int i = 0; i < featureList.size(); ++i)
/*     */     {
/* 150 */       String featureString = (String)featureList.elementAt(i);
/* 151 */       Feature feature = new Feature(featureString);
/* 152 */       features.add(feature);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void registerFeatures(String list, String componentName)
/*     */   {
/* 158 */     Vector featureList = StringUtils.parseArray(list, ',', '^');
/* 159 */     for (int i = 0; i < featureList.size(); ++i)
/*     */     {
/* 161 */       String feature = (String)featureList.elementAt(i);
/* 162 */       registerFeature(feature, componentName);
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized void registerFeature(String featureString, String componentInfo)
/*     */   {
/* 169 */     Feature f = new Feature(featureString);
/*     */ 
/* 171 */     Vector v = new IdcVector();
/* 172 */     v.addElement(f.m_featureName);
/* 173 */     v.addElement(f.m_featureVersion);
/* 174 */     v.addElement(f.m_featureLevel);
/* 175 */     if (componentInfo == null)
/*     */     {
/* 177 */       componentInfo = "";
/*     */     }
/* 179 */     v.addElement(componentInfo);
/* 180 */     this.m_featureList.addRow(v);
/*     */ 
/* 182 */     String currentLevel = (String)this.m_featureLevelMap.get(f.m_featureName);
/*     */ 
/* 185 */     if ((SystemUtils.compareVersions(currentLevel, f.m_featureLevel) <= 0) && (f.m_featureLevel != null))
/*     */     {
/* 188 */       this.m_featureLevelMap.put(f.m_featureName, f.m_featureLevel);
/*     */     }
/* 190 */     ArrayList l = (ArrayList)this.m_featureComponentMap.get(f.m_featureName);
/* 191 */     if (l == null)
/*     */     {
/* 193 */       l = new ArrayList();
/* 194 */       this.m_featureComponentMap.put(f.m_featureName, l);
/*     */     }
/* 196 */     l.add(componentInfo);
/* 197 */     l = (ArrayList)this.m_componentFeatureMap.get(componentInfo);
/* 198 */     if (l == null)
/*     */     {
/* 200 */       l = new ArrayList();
/* 201 */       this.m_componentFeatureMap.put(componentInfo, l);
/*     */     }
/* 203 */     l.add(featureString);
/*     */   }
/*     */ 
/*     */   public IdcIteratorData<Map> iterator()
/*     */   {
/* 208 */     return new IdcIteratorData(this, null);
/*     */   }
/*     */ 
/*     */   public boolean hasNext(IdcIteratorData<Map> data)
/*     */   {
/* 213 */     int rows = this.m_featureList.getNumRows();
/* 214 */     return data.m_pointerOffset < rows;
/*     */   }
/*     */ 
/*     */   public Map next(IdcIteratorData<Map> data) throws NoSuchElementException
/*     */   {
/* 219 */     int rows = this.m_featureList.getNumRows();
/* 220 */     if (data.m_pointerOffset >= rows)
/*     */     {
/* 222 */       throw new NoSuchElementException();
/*     */     }
/* 224 */     Vector row = this.m_featureList.getRowValues(data.m_pointerOffset++);
/* 225 */     Map m = new HashMap();
/* 226 */     this.m_featureList.populateMapWithValues(m, row, 0);
/* 227 */     return m;
/*     */   }
/*     */ 
/*     */   public void remove(IdcIteratorData<Map> data)
/*     */   {
/* 232 */     throw new UnsupportedOperationException();
/*     */   }
/*     */ 
/*     */   public Object clone()
/*     */     throws CloneNotSupportedException
/*     */   {
/* 238 */     BasicFeatureImplementor f = (BasicFeatureImplementor)super.clone();
/* 239 */     f.m_featureList = new DataResultSet();
/* 240 */     f.m_featureList.copy(this.m_featureList);
/* 241 */     f.m_featureLevelMap = cloneMapOfArrayLists(this.m_featureLevelMap);
/* 242 */     f.m_featureComponentMap = cloneMapOfArrayLists(this.m_featureComponentMap);
/* 243 */     f.m_componentFeatureMap = cloneMapOfArrayLists(this.m_componentFeatureMap);
/* 244 */     return f;
/*     */   }
/*     */ 
/*     */   protected Map cloneMapOfArrayLists(Map m)
/*     */   {
/* 249 */     HashMap newMap = new HashMap();
/* 250 */     for (Iterator i$ = m.keySet().iterator(); i$.hasNext(); ) { Object key = i$.next();
/*     */ 
/* 252 */       Object o = m.get(key);
/*     */ 
/* 254 */       if (o instanceof ArrayList)
/*     */       {
/* 256 */         ArrayList l = (ArrayList)o;
/* 257 */         l = (ArrayList)l.clone();
/* 258 */         newMap.put(key, l);
/*     */       }
/*     */       else
/*     */       {
/* 262 */         newMap.put(key, o);
/*     */       } }
/*     */ 
/* 265 */     return newMap;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 271 */     IdcStringBuilder b = new IdcStringBuilder(super.toString());
/* 272 */     for (this.m_featureList.first(); this.m_featureList.isRowPresent(); this.m_featureList.next())
/*     */     {
/* 274 */       b.append2('\n', this.m_featureList.getStringValue(0));
/* 275 */       b.append2('\t', this.m_featureList.getStringValue(1));
/* 276 */       b.append2('\t', this.m_featureList.getStringValue(2));
/* 277 */       b.append2('\t', this.m_featureList.getStringValue(3));
/*     */     }
/* 279 */     return b.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 284 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 73878 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.BasicFeatureImplementor
 * JD-Core Version:    0.5.4
 */