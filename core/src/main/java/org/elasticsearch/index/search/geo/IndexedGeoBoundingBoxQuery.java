/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
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

package org.elasticsearch.index.search.geo;

import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.GeoPointInBBoxQuery;
import org.apache.lucene.search.Query;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.index.mapper.geo.GeoPointFieldMapper;

/**
 */
public class IndexedGeoBoundingBoxQuery {

    public static Query create(GeoPoint topLeft, GeoPoint bottomRight, GeoPointFieldMapper.GeoPointFieldType fieldType) {
        return new GeoPointInBBoxQuery(fieldType.names().fullName(), topLeft.lon(), bottomRight.lat(), bottomRight.lon(), topLeft.lat());
    }
//        if (!fieldType.isLatLonEnabled()) {
//            throw new IllegalArgumentException("lat/lon is not enabled (indexed) for field [" + fieldType.names().fullName() + "], can't use indexed filter on it");
//        }
//        //checks to see if bounding box crosses 180 degrees
//        if (topLeft.lon() > bottomRight.lon()) {
//            return westGeoBoundingBoxFilter(topLeft, bottomRight, fieldType);
//        } else {
//            return eastGeoBoundingBoxFilter(topLeft, bottomRight, fieldType);
//        }
    }

//    private static Query westGeoBoundingBoxFilter(GeoPoint topLeft, GeoPoint bottomRight, GeoPointFieldMapper.GeoPointFieldType fieldType) {
//        return new GeoPointInBBoxQuery(fieldType.names().fullName(), topLeft.lon(), bottomRight.lon(), bottomRight.lat(), topLeft.lat());
//    }
//
//    private static Query eastGeoBoundingBoxFilter(GeoPoint topLeft, GeoPoint bottomRight, GeoPointFieldMapper.GeoPointFieldType fieldType) {
//        BooleanQuery filter = new BooleanQuery();
//        filter.add(fieldType.lonFieldType().rangeQuery(topLeft.lon(), bottomRight.lon(), true, true, null), Occur.MUST);
//        filter.add(fieldType.latFieldType().rangeQuery(bottomRight.lat(), topLeft.lat(), true, true, null), Occur.MUST);
//        return new ConstantScoreQuery(filter);
//    }
}
