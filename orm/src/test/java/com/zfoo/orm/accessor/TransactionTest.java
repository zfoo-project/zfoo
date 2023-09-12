/*
 * Copyright (C) 2020 The zfoo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.orm.accessor;

import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.TransactionBody;
import com.zfoo.orm.OrmContext;
import com.zfoo.orm.entity.UserEntity;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 必须要用复制集群才能使用事务
 *
 * @author godotg
 */
@Ignore
public class TransactionTest {

    @Test
    public void transactionTest() {
        var context = new ClassPathXmlApplicationContext("application.xml");

        /* Step 1: Start a client session. */
        var clientSession = OrmContext.getOrmManager().mongoClient().startSession();

        /* Step 2: Optional. Define options to use for the transaction. */

        var txnOptions = TransactionOptions.builder()
                .readPreference(ReadPreference.primary())
                .readConcern(ReadConcern.LOCAL)
                .writeConcern(WriteConcern.MAJORITY)
                .build();

        /* Step 3: Define the sequence of operations to perform inside the transactions. */


        var txnBody = new TransactionBody<String>() {
            public String execute() {
                var collection = OrmContext.getOrmManager().getCollection(UserEntity.class);
                var userEntity1 = new UserEntity(1, (byte) 2, (short) 3, 5, true, "helloORM", "helloOrm");
                var userEntity2 = new UserEntity(1, (byte) 2, (short) 3, 5, true, "helloORM", "helloOrm");

                /* Important:: You must pass the session to the operations.. */

                collection.insertOne(clientSession, userEntity1);
                collection.insertOne(clientSession, userEntity2);

                return "Inserted into collections in different databases";
            }
        };

        /* Step 4: Use .withTransaction() to start a transaction, execute the callback, and commit (or abort on error). */
        try {
            clientSession.withTransaction(txnBody, txnOptions);
        } catch (RuntimeException e) {
            // some error handling
            e.printStackTrace();
        } finally {
            clientSession.close();
        }
    }


}
