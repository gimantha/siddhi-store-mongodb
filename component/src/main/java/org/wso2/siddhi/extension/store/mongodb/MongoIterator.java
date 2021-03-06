/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.extension.store.mongodb;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.wso2.siddhi.core.table.record.RecordIterator;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class representing a RecordIterator which is responsible for processing MongoDB Event Table find() operations in a
 * streaming fashion.
 */
public class MongoIterator implements RecordIterator<Object[]> {
    private MongoCursor documents;
    private List<Attribute> attributes;

    private boolean preFetched;
    private Object[] nextDocument;

    public MongoIterator(FindIterable documents, List<Attribute> attributes) {
        this.documents = documents.iterator();
        this.attributes = attributes;
    }

    @Override
    public boolean hasNext() {
        if (!this.preFetched) {
            this.nextDocument = this.next();
            this.preFetched = true;
        }
        return this.nextDocument.length != 0;
    }

    @Override
    public Object[] next() {
        if (this.preFetched) {
            this.preFetched = false;
            Object[] result = this.nextDocument;
            this.nextDocument = null;
            return result;
        }
        if (this.documents.hasNext()) {
            return this.extractRecord((Document) this.documents.next());
        } else {
            return new Object[0];
        }
    }

    /**
     * Method which is used for extracting record values (in the form of an Object array) from a
     * MongoDB {@link Document}, according to the table's field type order.
     *
     * @param document the {@link Document} from which the values should be retrieved.
     * @return an array of extracted values, all cast to {@link Object} type for portability.
     */
    private Object[] extractRecord(Document document) {
        List<Object> result = new ArrayList<>();
        for (Attribute attribute : this.attributes) {
            result.add(document.get(attribute.getName()));
        }
        return result.toArray();
    }

    @Override
    public void close() throws IOException {

    }
}
