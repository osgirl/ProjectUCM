/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class RoleDefinitions extends DataResultSet
/*     */ {
/*  36 */   public static String m_tableName = "RoleDefinition";
/*  37 */   protected Hashtable m_roles = null;
/*  38 */   protected Hashtable m_groups = null;
/*  39 */   protected Hashtable m_psRoles = null;
/*  40 */   protected Hashtable m_psGroups = null;
/*  41 */   protected Hashtable m_displayNames = null;
/*     */ 
/*     */   public RoleDefinitions()
/*     */   {
/*  45 */     this.m_roles = new Hashtable();
/*  46 */     this.m_groups = new Hashtable();
/*  47 */     this.m_psRoles = new Hashtable();
/*  48 */     this.m_psGroups = new Hashtable();
/*  49 */     this.m_displayNames = new Hashtable();
/*     */   }
/*     */ 
/*     */   public DataResultSet shallowClone()
/*     */   {
/*  55 */     DataResultSet rset = new RoleDefinitions();
/*  56 */     initShallow(rset);
/*     */ 
/*  58 */     return rset;
/*     */   }
/*     */ 
/*     */   public void initShallow(DataResultSet rset)
/*     */   {
/*  64 */     super.initShallow(rset);
/*  65 */     RoleDefinitions roleDef = (RoleDefinitions)rset;
/*  66 */     roleDef.m_roles = this.m_roles;
/*  67 */     roleDef.m_groups = this.m_groups;
/*  68 */     roleDef.m_psRoles = this.m_psRoles;
/*  69 */     roleDef.m_psGroups = this.m_psGroups;
/*  70 */     roleDef.m_displayNames = this.m_displayNames;
/*     */   }
/*     */ 
/*     */   public void load(ResultSet rset)
/*     */     throws DataException
/*     */   {
/*  76 */     super.copy(rset);
/*     */ 
/*  78 */     this.m_roles = new Hashtable();
/*  79 */     this.m_groups = new Hashtable();
/*  80 */     this.m_psRoles = new Hashtable();
/*  81 */     this.m_psGroups = new Hashtable();
/*  82 */     this.m_displayNames = new Hashtable();
/*     */     try
/*     */     {
/*  85 */       boolean hasPsGroup = false;
/*  86 */       int allAppPriv = SecurityAccessListUtils.getAllAppRights();
/*  87 */       String[] keys = { "dRoleName", "dGroupName", "dPrivilege", "dRoleDisplayName" };
/*     */ 
/*  89 */       String[][] table = ResultSetUtils.createStringTable(this, keys);
/*  90 */       for (int i = 0; i < table.length; ++i)
/*     */       {
/*  92 */         String rName = table[i][0];
/*  93 */         String gName = table[i][1];
/*     */ 
/*  95 */         Hashtable roleMap = this.m_roles;
/*  96 */         Hashtable groupMap = this.m_groups;
/*  97 */         if ((gName.charAt(0) == '#') || (gName.charAt(0) == '$'))
/*     */         {
/* 100 */           roleMap = this.m_psRoles;
/* 101 */           groupMap = this.m_psGroups;
/*     */         }
/*     */ 
/* 104 */         long priv = Long.parseLong(table[i][2]);
/*     */ 
/* 106 */         String dName = table[i][3];
/* 107 */         Hashtable displayNameMap = this.m_displayNames;
/*     */ 
/* 109 */         RoleGroupData data = new RoleGroupData(rName, gName, priv, dName);
/*     */ 
/* 111 */         if ((!hasPsGroup) && (rName.equals("admin")) && (gName.equals("#AppsGroup")))
/*     */         {
/* 113 */           hasPsGroup = true;
/* 114 */           data.m_privilege = allAppPriv;
/*     */         }
/*     */ 
/* 117 */         Vector groups = (Vector)roleMap.get(data.m_roleName);
/* 118 */         if (groups == null)
/*     */         {
/* 120 */           groups = new IdcVector();
/* 121 */           roleMap.put(data.m_roleName, groups);
/*     */         }
/* 123 */         groups.addElement(data);
/*     */ 
/* 125 */         Vector roles = (Vector)groupMap.get(data.m_groupName);
/* 126 */         if (roles == null)
/*     */         {
/* 128 */           roles = new IdcVector();
/* 129 */           groupMap.put(data.m_groupName, roles);
/*     */         }
/* 131 */         roles.addElement(data);
/*     */ 
/* 134 */         if (displayNameMap.containsKey(rName))
/*     */           continue;
/* 136 */         displayNameMap.put(rName, dName);
/*     */       }
/*     */ 
/* 141 */       if (!hasPsGroup)
/*     */       {
/* 143 */         RoleGroupData data = new RoleGroupData("admin", "#AppsGroup", allAppPriv, "");
/* 144 */         Vector v = (Vector)this.m_psRoles.get(data.m_roleName);
/* 145 */         if (v == null)
/*     */         {
/* 147 */           v = new IdcVector();
/* 148 */           this.m_psRoles.put(data.m_roleName, v);
/*     */         }
/* 150 */         v.addElement(data);
/*     */ 
/* 152 */         v = (Vector)this.m_psGroups.get(data.m_groupName);
/* 153 */         if (v == null)
/*     */         {
/* 155 */           v = new IdcVector();
/* 156 */           this.m_psGroups.put(data.m_groupName, v);
/*     */         }
/* 158 */         v.addElement(data);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 164 */       String msg = LocaleUtils.encodeMessage("apUnableToLoadRoleDefinitions", e.getMessage());
/*     */ 
/* 166 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 170 */     first();
/* 171 */     SharedLoader.cacheOptListEx(this, "dRoleName", "roles", true);
/* 172 */     first();
/*     */   }
/*     */ 
/*     */   public Hashtable getRoles()
/*     */   {
/* 177 */     return this.m_roles;
/*     */   }
/*     */ 
/*     */   public Vector getRoleGroups(String name)
/*     */   {
/* 182 */     return (Vector)this.m_roles.get(name);
/*     */   }
/*     */ 
/*     */   public Hashtable getGroups()
/*     */   {
/* 187 */     return this.m_groups;
/*     */   }
/*     */ 
/*     */   public Vector getGroupRoles(String name)
/*     */   {
/* 192 */     return (Vector)this.m_groups.get(name);
/*     */   }
/*     */ 
/*     */   public Hashtable getPsRoles()
/*     */   {
/* 197 */     return this.m_psRoles;
/*     */   }
/*     */ 
/*     */   public Vector getPsRoleGroups(String name)
/*     */   {
/* 202 */     return (Vector)this.m_psRoles.get(name);
/*     */   }
/*     */ 
/*     */   public Hashtable getPsGroups()
/*     */   {
/* 207 */     return this.m_psGroups;
/*     */   }
/*     */ 
/*     */   public Vector getPsGroupRoles(String name)
/*     */   {
/* 212 */     return (Vector)this.m_psGroups.get(name);
/*     */   }
/*     */ 
/*     */   public String getTableName()
/*     */   {
/* 217 */     return m_tableName;
/*     */   }
/*     */ 
/*     */   public int getRolePrivilege(String roleName, String groupName) throws DataException
/*     */   {
/* 222 */     return (int)getRolePrivilegeEx(roleName, groupName);
/*     */   }
/*     */ 
/*     */   protected long getRolePrivilegeEx(String roleName, String groupName) throws DataException
/*     */   {
/* 227 */     Hashtable roleMap = this.m_roles;
/* 228 */     boolean checkGroup = false;
/* 229 */     boolean isPseudoGroup = false;
/* 230 */     if ((groupName != null) && (groupName.trim().length() > 0))
/*     */     {
/* 232 */       checkGroup = true;
/* 233 */       if ((groupName.charAt(0) == '#') || (groupName.charAt(0) == '$'))
/*     */       {
/* 235 */         isPseudoGroup = true;
/* 236 */         roleMap = this.m_psRoles;
/*     */       }
/*     */     }
/*     */ 
/* 240 */     long priv = 0L;
/* 241 */     Vector groups = (Vector)roleMap.get(roleName);
/* 242 */     if (groups == null)
/*     */     {
/* 244 */       return 0L;
/*     */     }
/*     */ 
/* 247 */     int size = groups.size();
/* 248 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 250 */       RoleGroupData data = (RoleGroupData)groups.elementAt(i);
/* 251 */       if ((checkGroup) && (groupName.equalsIgnoreCase(data.m_groupName)))
/*     */       {
/* 253 */         return data.m_privilege;
/*     */       }
/* 255 */       if (checkGroup)
/*     */         continue;
/* 257 */       priv = (priv < data.m_privilege) ? data.m_privilege : priv;
/*     */     }
/*     */ 
/* 261 */     if ((!checkGroup) || (isPseudoGroup))
/*     */     {
/* 263 */       return priv;
/*     */     }
/*     */ 
/* 266 */     if (roleName.equals("admin"))
/*     */     {
/* 268 */       priv = 7L;
/*     */     }
/*     */     else
/*     */     {
/* 272 */       priv = SharedObjects.getEnvironmentInt("UnknownGroupDefaultPrivilege", 0);
/*     */     }
/*     */ 
/* 275 */     return priv;
/*     */   }
/*     */ 
/*     */   public String getRoleDisplayName(String roleName) throws DataException
/*     */   {
/* 280 */     return (String)this.m_displayNames.get(roleName);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 285 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97133 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.RoleDefinitions
 * JD-Core Version:    0.5.4
 */