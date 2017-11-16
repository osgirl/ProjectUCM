package hays.custom;


import java.util.Date;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.ServiceHandler;
import intradoc.shared.SharedObjects;

public class FeaturedEmployersList extends ServiceHandler {
	
	public static final String cacheExpireTime=SharedObjects.getEnvironmentValue("CACHE_EXPIRE_TIME");
	
	class CachedFeaEmp 
	{
		DataResultSet feaEmp = null;
		long time = -1;
		int rowNum1=0 ;		
		
		public CachedFeaEmp(DataResultSet feaEmp, long lastTimeUpdate,int rowNum1)
		{
			this.feaEmp = feaEmp;			
			this.time = lastTimeUpdate;
			this.rowNum1=rowNum1;
		
		}
	}
	
			
	public void getFeaturedEmployers() throws DataException,ServiceException
	{
		SystemUtils.trace("getFeaturedEmployers", "cacheExpireTime:inside time condition" + cacheExpireTime );
		
			String providerName = m_currentAction.getParamAt(0);
	        String resultSetName = m_currentAction.getParamAt(1);
	        String queryAlwaysCount = m_currentAction.getParamAt(2);
	        String queryNormalRowNumCount = m_currentAction.getParamAt(3);
	       
	        DataResultSet result_always = null;
	        DataResultSet result_normal = null;
			DataResultSet res = null;
			DataResultSet result = null;
			DataResultSet result1 = null;
			DataResultSet RotationCount = null;
			 
			       
	        int numRowsAlways = 0;
	        int numRowsNormal = 0;
	        int i=0;
	        long timeCachedLast = 0;
	        int value1 = 0;
	        int value2=0;
	        int rowCount1= 0;
	        int rowCount2=0;
	        String rowNumber_test="";
	        
	               
	        String locale = m_binder.getLocal("xLocale");
	        SystemUtils.trace("getFeaturedEmployers", "xLocale:"+locale);	
	        
	        Date aDate = new Date();
			long min = aDate.getTime();
			
			
	        
	         
		SystemUtils.trace("getFeaturedEmployers", "cacheExpireTime:"+cacheExpireTime);		
		Object Employer = SharedObjects.getObject("FeaturedEmployers_"+locale, "Emp_List");
		if(Employer != null)
		{
			timeCachedLast = ((CachedFeaEmp)Employer).time;	
			res = new DataResultSet();
			res=((CachedFeaEmp)Employer).feaEmp;
			i=res.getNumRows();
			rowCount1 = ((CachedFeaEmp)Employer).rowNum1;		
		}
		
		
	
		SystemUtils.trace("getFeaturedEmployers", "cacheExpireTime:res.getNumRows()::i"+i);
		SystemUtils.trace("getFeaturedEmployers", "cacheExpireTime:rowCount1"+rowCount1);
	
		boolean a = (timeCachedLast != 0);
		SystemUtils.trace("getFeaturedEmployers", "cacheExpireTime:inside time condition" + (Double.parseDouble(cacheExpireTime)*60*1000) );
		boolean b = (min - timeCachedLast) < (Double.parseDouble(cacheExpireTime)*60*1000);
		SystemUtils.trace("getFeaturedEmployers", "cacheExpireTime:inside time condition" + "a::" +a + "b::" +b );
		
		
		SystemUtils.trace("getFeaturedEmployers", "cacheExpireTime:inside time condition" + "a::" +a + "b::" +b );
		
		if((timeCachedLast != 0)&&(min - timeCachedLast) < (Double.parseDouble(cacheExpireTime)*60*1000) && res.getNumRows() != 0)
		{
				SystemUtils.trace("getFeaturedEmployers", "cacheExpireTime:inside time condition");
				SystemUtils.trace("getFeaturedEmployers","inside Employer:"+ res.getNumRows() );
				m_binder.addResultSet(resultSetName, res);
				
		}
		else
		{          
		
			if (providerName == null || providerName.length() == 0) 
			{
				throw new ServiceException("You must specify a provider name.");			
			}
		
			Provider p = Providers.getProvider(providerName);
			if (p == null) 
			{
				throw new ServiceException("The provider '" + providerName
						+ "' does not exist.");
			} else if (!p.isProviderOfType("database")) {
				throw new ServiceException("The provider '" + providerName
					+ "' is not a valid provider of type 'database'.");
			}
		
		
			Workspace ws = (Workspace) p.getProvider();
		
				
			if (queryAlwaysCount != null && queryAlwaysCount.trim().length() > 0) 
			{	
				SystemUtils.trace("getFeaturedEmployers", "queryAlwaysCount ::: " + queryAlwaysCount);
				ResultSet temp = ws.createResultSet(queryAlwaysCount, m_binder);	
				RotationCount = new DataResultSet();
				//result_always = new DataResultSet();
				RotationCount.copy(temp);			
		
			}
			
			value2 = RotationCount.getNumRows();
			SystemUtils.trace("getFeaturedEmployers", " value2 found: RotationCount" + value2);
			result_always = new DataResultSet();
			result_always.copySimpleFiltered(RotationCount, "HOMEPAGEROTATION", "Always");			
			numRowsAlways = result_always.getNumRows();		
			SystemUtils.trace("getFeaturedEmployers", "numRowsAlways found: RotationCount "+numRowsAlways);
			
			result_normal = new DataResultSet();
			result_normal.copySimpleFiltered(RotationCount, "HOMEPAGEROTATION", "Normal");	
			numRowsNormal = result_normal.getNumRows();
			SystemUtils.trace("getFeaturedEmployers", " numRowsNormal found: RotationCount" + numRowsNormal);
		
			
			if(numRowsAlways == 8)
			{			
				m_binder.addResultSet(resultSetName, result_always);
			
			}		
			else
			{	
				SystemUtils.trace("getFeaturedEmployers", " inside else");
		
//				if (queryNormalCount != null && queryNormalCount.trim().length() > 0)
//				{
//					ResultSet temp = ws.createResultSet(queryNormalCount,m_binder);
//					result_normal = new DataResultSet();
//					result_normal.copy(temp);	
//				}
				numRowsNormal = result_normal.getNumRows();
				int n1 = 8 - (numRowsAlways);
				SystemUtils.trace("getFeaturedEmployers", " inside else ::numRowsNormal " + numRowsNormal +"::n1 "+n1);
			
				if(numRowsNormal < n1)
				{
					result = new DataResultSet();
					if(numRowsAlways != 0)
					{
						result.copy(result_always);
						result.merge("CONTENT_ID",result_normal,false);
					}
					else
					{
						result.copy(result_normal);
					}
					int n = result.getNumRows();
					SystemUtils.trace("getFeaturedEmployers", "inside if::"+ n);
				
				}
				else
				{
					SystemUtils.trace("getFeaturedEmployers", " inside else --> inside else");
					if((timeCachedLast == 0))
					{
						for(int u=1;u<=n1;u++)
						{
							rowNumber_test=rowNumber_test+Integer.toString(u)+",";
							
							SystemUtils.trace("getFeaturedEmployers", " inside else --> inside iffffff "+rowNumber_test);
						}
						
						if(rowNumber_test.endsWith(","))
						{
							rowNumber_test=rowNumber_test.substring(0, rowNumber_test.length()-1);
						}
						value1=2;
						
						SystemUtils.trace("getFeaturedEmployers", " inside else --> inside else"+value1);
						
						SystemUtils.trace("getFeaturedEmployers", " inside else --> inside iffffff"+rowNumber_test);
					}
					else
					{			
							int count1=rowCount1;
							SystemUtils.trace("getFeaturedEmployers", " inside else --> inside else ::count1 "+count1);
							if(count1 > numRowsNormal)
							{
								SystemUtils.trace("getFeaturedEmployers", " inside iffffff ::count1 "+count1);
								count1 = 1;
							}
							int ane=count1;
							SystemUtils.trace("getFeaturedEmployers", " inside iffffff ::ane "+ane);
							
							for(int q=0;q<n1;q++)
							{
								if(ane > numRowsNormal)
								{
									ane=1;
									SystemUtils.trace("getFeaturedEmployers", " inside iffffff (ane > n1) ::ane "+ane);
									
								}
								rowNumber_test=rowNumber_test+Integer.toString(ane)+",";
								ane=ane+1;
								
								SystemUtils.trace("getFeaturedEmployers", " inside else --> inside else   ..."+rowNumber_test + "ane::"+ane);
							}
							if(rowNumber_test.endsWith(","))
							{
								rowNumber_test=rowNumber_test.substring(0, rowNumber_test.length()-1);
							}
							
														
							value1=count1+1;
							
						
						SystemUtils.trace("getFeaturedEmployers", " inside else --> inside else"+value1);
						
						SystemUtils.trace("getFeaturedEmployers", " inside else --> inside elseeee"+rowNumber_test);
					
					}
						
					
					m_binder.putLocal("value1", rowNumber_test);
					
					
					if (queryNormalRowNumCount != null && queryNormalRowNumCount.trim().length() > 0)
					{
						ResultSet temp = ws.createResultSet(queryNormalRowNumCount, m_binder);
						result1 = new DataResultSet();
						result1.copy(temp);	
						SystemUtils.trace("getFeaturedEmployers", " inside else --> inside else"+result1.getNumRows());
					}
					
					result = new DataResultSet();
					if(numRowsNormal != 0)
					{
						result.copy(result_always);
						result.merge("CONTENT_ID",result1,false);
					}
					else
					{
						result.copy(result1);
					}
					
					
					SystemUtils.trace("getFeaturedEmployers", "result.getNumRows( :"+result.getNumRows());
					
				}			
				m_binder.addResultSet(resultSetName, result);

			
		
		
			CachedFeaEmp Employers_List = new CachedFeaEmp(result,min,value1);		
			SharedObjects.putObject("FeaturedEmployers_"+locale, "Emp_List", Employers_List);
			SystemUtils.trace("getFeaturedEmployers","Cached Object created ::" + value1 + ":::" );
	
			}
			ws.releaseConnection(); 
        
		}
	}
}  
	
	


