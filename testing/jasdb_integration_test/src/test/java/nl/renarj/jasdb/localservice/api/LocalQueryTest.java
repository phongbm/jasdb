/*
 * The JASDB software and code is Copyright protected 2012 and owned by Renze de Vries
 * 
 * All the code and design principals in the codebase are also Copyright 2012 
 * protected and owned Renze de Vries. Any unauthorized usage of the code or the 
 * design and principals as in this code is prohibited.
 */
package nl.renarj.jasdb.localservice.api;

import nl.renarj.jasdb.LocalDBSessionFactory;
import nl.renarj.jasdb.service.EntityQueryTest;

/**
 * User: renarj
 * Date: 5/11/12
 * Time: 2:40 PM
 */
public class LocalQueryTest extends EntityQueryTest {
    public LocalQueryTest() {
        super(new LocalDBSessionFactory());
    }

}
