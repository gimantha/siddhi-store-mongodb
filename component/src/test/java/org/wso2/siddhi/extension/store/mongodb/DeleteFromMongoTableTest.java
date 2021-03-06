/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.extension.store.mongodb;

import com.mongodb.MongoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;


public class DeleteFromMongoTableTest {
    private static final Log log = LogFactory.getLog(DeleteFromMongoTableTest.class);

    @BeforeClass
    public void init() {
        log.info("== MongoDB Collection DELETE tests started ==");
    }

    @AfterClass
    public void shutdown() {
        log.info("== MongoDB Collection DELETE tests completed ==");
    }

    @Test
    public void deleteFromMongoTableTest1() throws InterruptedException, MongoException {
        log.info("deleteFromMongoTableTest1 - " +
                "DASC5-903:Delete an event of a MongoDB table successfully");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            MongoTableTestUtils.clearCollection();
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "define stream DeleteStockStream (symbol string, price float, volume long); " +
                    "@Store(type = 'mongodb' , mongodb.uri='mongodb://admin:admin@127.0.0.1/Foo')" +
                    "@PrimaryKey('symbol')" +
                    "define table FooTable (symbol string, price float, volume long);";
            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream " +
                    "insert into FooTable ;" +
                    "" +
                    "@info(name = 'query2') " +
                    "from DeleteStockStream " +
                    "delete FooTable " +
                    "   on (FooTable.symbol == symbol) ";


            ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
            InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
            InputHandler deleteStockStream = executionPlanRuntime.getInputHandler("DeleteStockStream");
            executionPlanRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            stockStream.send(new Object[]{"IBM", 75.6F, 100L});
            stockStream.send(new Object[]{"WSO52", 57.6F, 100L});
            deleteStockStream.send(new Object[]{"IBM", 75.6F, 100L});
            deleteStockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            Thread.sleep(1000);

            long totalDocumentsInCollection = MongoTableTestUtils.getDocumentsCount();
            Assert.assertEquals(totalDocumentsInCollection, 1, "Deletion failed");

            executionPlanRuntime.shutdown();
        } catch (MongoException e) {
            log.info("Test case 'deleteFromMongoTableTest1' ignored due to " + e.getMessage());
            throw e;
        }
    }


    @Test(expectedExceptions = ExecutionPlanValidationException.class)
    public void deleteFromMongoTableTest2() throws InterruptedException, MongoException {
        log.info("deleteFromMongoTableTest2 - " +
                "DASC5-904:Delete an event from a non existing MongoDB table");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            MongoTableTestUtils.clearCollection();
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "define stream DeleteStockStream (symbol string, price float, volume long); " +
                    "@Store(type = 'mongodb' , mongodb.uri='mongodb://admin:admin@127.0.0.1/Foo')" +
                    "define table FooTable (symbol string, price float, volume long);";
            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream " +
                    "insert into FooTable ;" +
                    "" +
                    "@info(name = 'query2') " +
                    "from DeleteStockStream " +
                    "delete FooTable1234 " +
                    "on FooTable.symbol == symbol;";

            ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
            executionPlanRuntime.start();
            executionPlanRuntime.shutdown();

        } catch (MongoException e) {
            log.info("Test case 'deleteFromMongoTableTest2' ignored due to " + e.getMessage());
            throw e;
        }
    }


    @Test(expectedExceptions = ExecutionPlanValidationException.class)
    public void deleteFromMongoTableTest3() throws InterruptedException, MongoException {
        log.info("deleteFromMongoTableTest3 - " +
                "DASC5-905:Delete an event from a MongoDB table by selecting from non existing stream");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            MongoTableTestUtils.clearCollection();
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "define stream DeleteStockStream (symbol string, price float, volume long); " +
                    "@Store(type = 'mongodb' , mongodb.uri='mongodb://admin:admin@127.0.0.1/Foo')" +
                    "define table FooTable (symbol string, price float, volume long);";
            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream " +
                    "insert into FooTable ;" +
                    "" +
                    "@info(name = 'query2') " +
                    "from DeleteStockStream345 " +
                    "delete FooTable " +
                    "on FooTable.symbol == symbol;";

            ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
            executionPlanRuntime.start();
            executionPlanRuntime.shutdown();
        } catch (MongoException e) {
            log.info("Test case 'deleteFromMongoTableTest3' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test(expectedExceptions = ExecutionPlanValidationException.class)
    public void deleteFromMongoTableTest4() throws InterruptedException, MongoException {
        log.info("deleteFromMongoTableTest4 - " +
                "DASC5-906:Delete an event from a MongoDB table based on a non-existing attribute");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            MongoTableTestUtils.clearCollection();
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "define stream DeleteStockStream (symbol string, price float, volume long); " +
                    "@Store(type = 'mongodb' , mongodb.uri='mongodb://admin:admin@127.0.0.1/Foo')" +
                    "define table FooTable (symbol string, price float, volume long);";
            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream " +
                    "insert into FooTable ;" +
                    "" +
                    "@info(name = 'query2') " +
                    "from DeleteStockStream " +
                    "delete FooTable " +
                    "   on (FooTable.length == length) ";

            ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
            executionPlanRuntime.start();
            executionPlanRuntime.shutdown();
        } catch (MongoException e) {
            log.info("Test case 'deleteFromMongoTableTest4' ignored due to " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void deleteFromMongoTableTest5() throws InterruptedException, MongoException {
        log.info("deleteFromMongoTableTest5 - " +
                "DASC5-909:Delete an event from a MongoDB table based on a non-existing attribute value");
        SiddhiManager siddhiManager = new SiddhiManager();
        try {
            MongoTableTestUtils.clearCollection();
            String streams = "" +
                    "define stream StockStream (symbol string, price float, volume long); " +
                    "define stream DeleteStockStream (symbol string, price float, volume long); " +
                    "@Store(type = 'mongodb' , mongodb.uri='mongodb://admin:admin@127.0.0.1/Foo')" +
                    "define table FooTable (symbol string, price float, volume long);";
            String query = "" +
                    "@info(name = 'query1') " +
                    "from StockStream " +
                    "insert into FooTable;" +
                    "" +
                    "@info(name = 'query2') " +
                    "from DeleteStockStream " +
                    "delete FooTable " +
                    "   on (FooTable.symbol == symbol) ";


            ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(streams + query);
            InputHandler stockStream = executionPlanRuntime.getInputHandler("StockStream");
            InputHandler deleteStockStream = executionPlanRuntime.getInputHandler("DeleteStockStream");
            executionPlanRuntime.start();

            stockStream.send(new Object[]{"WSO2", 55.6F, 100L});
            stockStream.send(new Object[]{"IBM", 75.6F, 100L});
            stockStream.send(new Object[]{"WSO2", 57.6F, 100L});
            deleteStockStream.send(new Object[]{"IBM_v2", 75.6F, 100L});
            deleteStockStream.send(new Object[]{"WSO2_v2", 55.6F, 100L});
            Thread.sleep(1000);

            long totalDocumentsInCollection = MongoTableTestUtils.getDocumentsCount();
            Assert.assertEquals(totalDocumentsInCollection, 3, "Deletion failed");

            executionPlanRuntime.shutdown();
        } catch (MongoException e) {
            log.info("Test case 'deleteFromMongoTableTest5' ignored due to " + e.getMessage());
            throw e;
        }
    }


}
