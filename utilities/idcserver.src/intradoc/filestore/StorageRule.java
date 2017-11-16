/*    */ package intradoc.filestore;
/*    */ 
/*    */ import intradoc.common.StringUtils;
/*    */ import java.util.Hashtable;
/*    */ import java.util.List;
/*    */ import java.util.Map;
/*    */ 
/*    */ public class StorageRule
/*    */ {
/*    */   public String m_name;
/*    */   public String m_type;
/*    */   public boolean m_isWebless;
/*    */   public String m_renditionsOnFS;
/*    */   public List m_rendList;
/*    */   public Map m_props;
/*    */   public Map m_pathConfig;
/*    */ 
/*    */   public StorageRule(Map props)
/*    */   {
/* 38 */     this.m_props = props;
/*    */ 
/* 41 */     this.m_name = ((String)props.get("StorageRule"));
/* 42 */     this.m_type = ((String)props.get("StorageType"));
/* 43 */     this.m_isWebless = StringUtils.convertToBool((String)props.get("IsWeblessStore"), false);
/* 44 */     this.m_renditionsOnFS = ((String)props.get("RenditionsOnFileSystem"));
/* 45 */     this.m_rendList = StringUtils.makeListFromSequenceSimple(this.m_renditionsOnFS);
/*    */ 
/* 47 */     this.m_pathConfig = new Hashtable();
/*    */   }
/*    */ 
/*    */   public Map getPathConfig(String storageClass)
/*    */   {
/* 52 */     return (Map)this.m_pathConfig.get(storageClass);
/*    */   }
/*    */ 
/*    */   public String getRuleProperty(String key)
/*    */   {
/* 57 */     return (String)this.m_props.get(key);
/*    */   }
/*    */ 
/*    */   public boolean getRuleBoolean(String key, boolean dfltValue)
/*    */   {
/* 62 */     String str = (String)this.m_props.get(key);
/* 63 */     return StringUtils.convertToBool(str, dfltValue);
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 69 */     return "rule:" + this.m_name + " type:" + this.m_type + " webless:" + this.m_isWebless + " onFS: =" + this.m_renditionsOnFS;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 75 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.StorageRule
 * JD-Core Version:    0.5.4
 */