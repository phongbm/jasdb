/*
 * The JASDB software and code is Copyright protected 2011 and owned by Renze de Vries
 * 
 * All the code and design principals in the codebase are also Copyright 2011 
 * protected and owned Renze de Vries. Any unauthorized usage of the code or the 
 * design and principals as in this code is prohibited.
 */
package nl.renarj.jasdb.storage.query;

import nl.renarj.jasdb.api.context.RequestContext;
import nl.renarj.jasdb.api.query.QueryBuilder;
import nl.renarj.jasdb.api.query.QueryExecutor;
import nl.renarj.jasdb.api.query.QueryResult;
import nl.renarj.jasdb.core.SimpleKernel;
import nl.renarj.jasdb.core.exceptions.ConfigurationException;
import nl.renarj.jasdb.core.exceptions.JasDBStorageException;
import nl.renarj.jasdb.index.result.SearchLimit;
import nl.renarj.jasdb.service.StorageService;
import nl.renarj.jasdb.service.StorageServiceFactory;
import nl.renarj.jasdb.storage.query.operators.BlockOperation;

public class QueryExecutorImpl implements QueryExecutor {
	private QueryBuilder queryBuilder;
	private SearchLimit limit;
	private String bagName;
	private String instanceId;

    private RequestContext requestContext;
	
	public QueryExecutorImpl(RequestContext requestContext, String instanceId, String bagName, QueryBuilder parentBuilder) {
        this.requestContext = requestContext;
		this.queryBuilder = parentBuilder;
		this.bagName = bagName;
		this.instanceId = instanceId;
		this.limit = new SearchLimit();
	}

	@Override
	public QueryExecutor limit(int limit) {
		this.limit = new SearchLimit(limit);
		return this;
	}

	@Override
	public QueryExecutor paging(int start, int max) {
		this.limit = new SearchLimit(start, max);
		return this;
	}

	@Override
	public QueryResult execute() throws JasDBStorageException {
		BlockOperation parentSearchCondition = BuilderTransformer.transformBuilder(queryBuilder);

		try {
			StorageServiceFactory serviceFactory = SimpleKernel.getStorageServiceFactory();
			StorageService storageService = serviceFactory.getOrCreateStorageService(instanceId, bagName);
			
			return storageService.search(requestContext, parentSearchCondition, limit, queryBuilder.getSortParams());
		} catch(ConfigurationException e) {
			throw new JasDBStorageException("Unable to execute query due to configuration error", e);
		}
		
	}
	

}
