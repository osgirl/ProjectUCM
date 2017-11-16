package hays.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import infomentum.ontology.Converter;
import infomentum.ontology.loader.OntologyFacade;
import intradoc.common.ServiceException;
import intradoc.common.SystemUtils;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;
import intradoc.provider.Provider;
import intradoc.provider.Providers;
import intradoc.server.ServiceHandler;

public class HaysKillerQuestionsHandler extends ServiceHandler
{
	public final static String TRACE_NAME = "killer_questions";
	public final static String queryNameQuestionByLocale = "QGetQuestionsByLocale";
	public final static String queryNameQuestionsByLocaleCategory = "QGetQuestionByLocaleCategory";
	
	public void getQuestionsByLocale() throws ServiceException, DataException {
			
			// obtain the provider name, the query, and the result set name
			// from the action definition in the service
			String providerName = m_currentAction.getParamAt(0);
			String resultSetName = m_currentAction.getParamAt(1);
			
			String xLocale=m_binder.getLocal("xLocale");

			SystemUtils.trace(TRACE_NAME, "xLocale: " + xLocale);
			SystemUtils.trace(TRACE_NAME, "Office Query : " + queryNameQuestionByLocale);
			
			// validate the provider name
			if (providerName == null || providerName.length() == 0) {
				throw new ServiceException("You must specify a provider name.");
			}
	
			// validate that the provider is a valid database provider
			Provider p = Providers.getProvider(providerName);
			if (p == null) {
				throw new ServiceException("The provider '" + providerName
						+ "' does not exist.");
			} else if (!p.isProviderOfType("database")) {
				throw new ServiceException("The provider '" + providerName
						+ "' is not a valid provider of type 'database'.");
			}
			
			// grab the provider object that does all the work, and scope it to
			// a workspace object for database access, since we can be reasonably
			// certain at this point that the object returned is a Workspace object
			Workspace ws = (Workspace) p.getProvider();
			DataResultSet result = null;
	
			// if they specified a predefined query, execute that
			if (queryNameQuestionByLocale != null && queryNameQuestionByLocale.trim().length() > 0) {
				// obtain a JDBC result set with the data in it. This result set is
				// temporary, and we must copy it before putting it in the binder
				ResultSet temp = ws.createResultSet(queryNameQuestionByLocale, m_binder);
	
				// create a DataResultSet based on the temp result set
				result = new DataResultSet();
				result.copy(temp);
			}
			
			String 	xlocale = null, question = null, xcategories="", id=null, prev_question=null, xcategory=null, prev_id=null;
			DataResultSet fields = new DataResultSet(new String[] {
	                "XLOCALE", "QUESTION", "XCATEGORIES", "ID"
	            });
			if(result !=null ){
				SystemUtils.trace(TRACE_NAME, "ResultSet " + result);
				do {
					try {
						xlocale = result.getStringValueByName("xLocale");
						question = result.getStringValueByName("QUESTION_TEXT");
						xcategory = result.getStringValueByName("xCategory");
						id = result.getStringValueByName("QUESTION_ID");
						
						SystemUtils.trace(TRACE_NAME, "question = " + question);
						if(prev_question ==null || question.equals(prev_question)){
							//same question different category
							if(xcategories == ""){
								xcategories = xcategory;
							}else{
								xcategories = xcategories + ","+ xcategory;
							}
						}else{
							//new question, add previous to list
							Vector<String> row = new Vector<String>();
							row.add(xlocale); 
							row.add(prev_question);
							row.add(xcategories);
							row.add(prev_id);
							fields.addRow(row);	                
							SystemUtils.trace(TRACE_NAME, "After RS was added " + result);
							xcategories = xcategory;
						}
						prev_question = question;
						prev_id = id;
						
					}catch(Exception ex) {
						SystemUtils.trace(TRACE_NAME, "Exception while processing questions: " + question + ", " + ex);
					}
				} while(result.next());
			
				//add last row
				if(!result.isEmpty()){
					Vector<String> row = new Vector<String>();
					row.add(xlocale); 
					row.add(question);
					row.add(xcategories);
					row.add(id);
					fields.addRow(row);
				}
			}
			this.m_binder.addResultSet(resultSetName, fields);
		}
			
	public void updateKillerQuestions() throws DataException, ServiceException
	{
		// obtain the provider name, and the result set name from the action definition in the service
		String providerName = m_currentAction.getParamAt(0);
		String resultSetName = m_currentAction.getParamAt(1); 
		
		String queryDelete = "Delete_Questions_By_Id";
		String queryQuestionId = "Get_Question_Id";
		String queryInsert = "Insert_Question";
		String queryInsertCategories = "Insert_Question_Categories";
		
		String queryUpdate = "Update_Question";
		String queryDeleteQuestionCategory = "Delete_Question_Categories";
		
		
		/*
		job_category1	hays:Retail, hays:Sales_UK
		job_category2	hays:HRXHR
		question1	Queastion 1
		question2	Queastion 2
		*/
		
		String locale = m_binder.getLocal("Country"); 
		String submitted = m_binder.getLocal("submitted"); //1
		String count = m_binder.getLocal("count"); 
		String removedQuestions = m_binder.getLocal("removedQuestions"); //1
		
		int questionCount = Integer.parseInt(count);
		
		List<String> questionList = getQuestions(questionCount);
		List<String> categoryList = getCategories(questionCount);
		List<String> questionIdList = getQuestionIds(questionCount);
		
		if(questionList.size() != categoryList.size()){
			SystemUtils.trace(TRACE_NAME, "Question count not same as category count.."+questionList.size()+" "+categoryList.size());
			throw new ServiceException("Question count not same as category count..");
		}
		
		SystemUtils.trace(TRACE_NAME, "locale found:"+locale);
		SystemUtils.trace(TRACE_NAME, "submitted found:"+submitted);
		SystemUtils.trace(TRACE_NAME, "removedQuestions found:"+removedQuestions);
		
		// validate the provider name
		if (providerName == null || providerName.length() == 0) {
			throw new ServiceException("You must specify a provider name.");
		}

		// validate that the provider is a valid database provider
		Provider p = Providers.getProvider(providerName);
		if (p == null) {
			throw new ServiceException("The provider '" + providerName
					+ "' does not exist.");
		} else if (!p.isProviderOfType("database")) {
			throw new ServiceException("The provider '" + providerName
					+ "' is not a valid provider of type 'database'.");
		}

		
		// grab the provider object that does all the work, and scope it to
		// a workspace object for database access, since we can be reasonably
		// certain at this point that the object returned is a Workspace object
		Workspace ws = (Workspace) p.getProvider();
		ws.beginTran();
		try{
			m_binder.putLocal("xLocale", locale);
			if(removedQuestions !=null && !"".equals(removedQuestions)){
				m_binder.putLocal("questionIds", removedQuestions);
				ws.execute(queryDelete, m_binder);//Execute the update Query to set the question as inactive
			}
			String question = null, categories = null, questionId= null;
			for(int i=0;i<questionList.size();i++){
				question = questionList.get(i);
				m_binder.putLocal("question", question);
				questionId = questionIdList.get(i);
				int id = 0;
				if("00".equals(questionId)){
					//insert new question
					//get id
					ResultSet rs = ws.createResultSet(queryQuestionId, m_binder);
					rs.first();
					SystemUtils.trace(TRACE_NAME, "id found:"+rs.getStringValue(0));
					id = Integer.parseInt(rs.getStringValue(0));
					m_binder.putLocal("questionId", rs.getStringValue(0));
					//insert question
					ws.execute(queryInsert, m_binder);
					//insert questionCategory
					categories = categoryList.get(i);
					String category[] = categories.split(",");
					String catg = null;
					for(int x=0;x<category.length;x++){
						catg = category[x].trim();
						m_binder.putLocal("xCategory", catg);
						ws.execute(queryInsertCategories, m_binder);
					}
				}else{
					//update question
					m_binder.putLocal("questionId", questionId);
					ws.execute(queryUpdate, m_binder);
					//delete existing question-categories
					ws.execute(queryDeleteQuestionCategory, m_binder);
					//insert questionCategory 
					categories = categoryList.get(i);
					String category[] = categories.split(",");
					String catg = null;
					for(int x=0;x<category.length;x++){
						catg = category[x].trim();
						m_binder.putLocal("xCategory", catg);
						ws.execute(queryInsertCategories, m_binder);
					}
				}
			}
			ws.commitTran();
		}catch(Exception e){
			ws.rollbackTran();
		}
		
		// release the JDBC connection assigned to this thread (request)
		// which kills the result set 'temp'
		ws.releaseConnection();
		
	}
	
	public void getQuestionsByLocaleCategory() throws ServiceException, DataException {
		
		// obtain the provider name, the query, and the result set name
		// from the action definition in the service
		String providerName = m_currentAction.getParamAt(0);
		String resultSetName = m_currentAction.getParamAt(1);
		
		String xLocale=m_binder.getLocal("xLocale");
		String categoryIds=m_binder.getLocal("categoryIds");
		
		String categoryListRegexp = "";
		if(categoryIds != null){
			String catgs[] = categoryIds.split(",");
			String catg = null;
			for(int i=0;i<catgs.length;i++){
				catg = catgs[i];
				if(i == 0){
					categoryListRegexp = catg.trim();
				}else{
					categoryListRegexp = categoryListRegexp +"|" + catg.trim();
				}
			}
		}
		m_binder.putLocal("categoryIds", categoryListRegexp);
		
		SystemUtils.trace(TRACE_NAME, "xLocale: " + xLocale);
		SystemUtils.trace(TRACE_NAME, "categoryIds: " + categoryIds);
		SystemUtils.trace(TRACE_NAME, "Office Query : " + queryNameQuestionsByLocaleCategory);
		
		// validate the provider name
		if (providerName == null || providerName.length() == 0) {
			throw new ServiceException("You must specify a provider name.");
		}

		// validate that the provider is a valid database provider
		Provider p = Providers.getProvider(providerName);
		if (p == null) {
			throw new ServiceException("The provider '" + providerName
					+ "' does not exist.");
		} else if (!p.isProviderOfType("database")) {
			throw new ServiceException("The provider '" + providerName
					+ "' is not a valid provider of type 'database'.");
		}
		
		// grab the provider object that does all the work, and scope it to
		// a workspace object for database access, since we can be reasonably
		// certain at this point that the object returned is a Workspace object
		Workspace ws = (Workspace) p.getProvider();
		DataResultSet result = null;

		// if they specified a predefined query, execute that
		if (queryNameQuestionsByLocaleCategory != null && queryNameQuestionsByLocaleCategory.trim().length() > 0) {
			// obtain a JDBC result set with the data in it. This result set is
			// temporary, and we must copy it before putting it in the binder
			ResultSet temp = ws.createResultSet(queryNameQuestionsByLocaleCategory, m_binder);

			// create a DataResultSet based on the temp result set
			result = new DataResultSet();
			result.copy(temp);
		}
		
		String 	xlocale = null, question = null, xcategories="", id=null, prev_question=null, xcategory=null, prev_id=null;
		DataResultSet fields = new DataResultSet(new String[] {
                "XLOCALE", "QUESTION", "XCATEGORIES", "ID"
            });
		do {
			try {
				xlocale = result.getStringValueByName("xLocale");
				question = result.getStringValueByName("QUESTION_TEXT");
				xcategory = result.getStringValueByName("xCategory");
				id = result.getStringValueByName("QUESTION_ID");
				
				SystemUtils.trace(TRACE_NAME, "question = " + question);
				if(prev_question ==null || question.equals(prev_question)){
					//same question different category
					if(xcategories == ""){
						xcategories = xcategory;
					}else{
						xcategories = xcategories + ","+ xcategory;
					}
				}else{
					//new question, add previous to list
					Vector<String> row = new Vector<String>();
					row.add(xlocale); 
					row.add(prev_question);
					row.add(xcategories);
					row.add(prev_id);
					fields.addRow(row);	                
					SystemUtils.trace(TRACE_NAME, "After RS was added " + result);
					xcategories = xcategory;
				}
				prev_question = question;
				prev_id = id;
				
			}catch(Exception ex) {
				SystemUtils.trace(TRACE_NAME, "Exception while processing questions: " + question + ", " + ex);
			}
		} while (result.next());
		//add last row
		if(!result.isEmpty()){
			Vector<String> row = new Vector<String>();
			row.add(xlocale); 
			row.add(question);
			row.add(xcategories);
			row.add(id);
			fields.addRow(row);
		}
		this.m_binder.addResultSet(resultSetName, fields);
	}
	
	private List<String> getQuestions(int count){
		return getParameters("question", count);
	}
	
	private List<String> getCategories(int count){
		return getParameters("job_category", count);
	}
	
	private List<String> getQuestionIds(int count){
		return getParameters("questionId", count);
	}
	
	private List<String> getParameters(String parameterName, int count){
		List<String> paramValueList = new ArrayList<String>();
		int counter =1;
		do{
			String paramValue = m_binder.getLocal(parameterName+counter);
			if(paramValue != null  && !"".equals(paramValue)){
				paramValueList.add(paramValue);
			}
			counter++;
		}while(paramValueList.size() < count);
		
		return paramValueList;
	}
	
}
