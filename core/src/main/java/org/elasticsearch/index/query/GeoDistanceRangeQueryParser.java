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

package org.elasticsearch.index.query;

import org.apache.lucene.search.GeoPointDistanceRangeQuery;
import org.apache.lucene.search.Query;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoHashUtils;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.GeoUtils;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.fielddata.IndexGeoPointFieldData;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.mapper.geo.GeoPointFieldMapper;
import org.elasticsearch.index.search.geo.GeoDistanceRangeQuery;

import java.io.IOException;

/**
 * <pre>
 * {
 *     "name.lat" : 1.1,
 *     "name.lon" : 1.2,
 * }
 * </pre>
 */
public class GeoDistanceRangeQueryParser implements QueryParser {

    public static final String NAME = "geo_distance_range";

    @Inject
    public GeoDistanceRangeQueryParser() {
    }

    @Override
    public String[] names() {
        return new String[]{NAME, "geoDistanceRange"};
    }

    @Override
    public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
        XContentParser parser = parseContext.parser();

        XContentParser.Token token;

        String queryName = null;
        String currentFieldName = null;
        GeoPoint point = new GeoPoint();
        String fieldName = null;
        Object vFrom = null;
        Object vTo = null;
        boolean includeLower = true;
        boolean includeUpper = true;
        DistanceUnit unit = DistanceUnit.DEFAULT;
        GeoDistance geoDistance = GeoDistance.DEFAULT;
        String optimizeBbox = "memory";
        boolean normalizeLon = true;
        boolean normalizeLat = true;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (parseContext.isDeprecatedSetting(currentFieldName)) {
                // skip
            } else if (token == XContentParser.Token.START_ARRAY) {
                GeoUtils.parseGeoPoint(parser, point);
                fieldName = currentFieldName;
            } else if (token == XContentParser.Token.START_OBJECT) {
                // the json in the format of -> field : { lat : 30, lon : 12 }
                fieldName = currentFieldName;
                GeoUtils.parseGeoPoint(parser, point);
            } else if (token.isValue()) {
                if (currentFieldName.equals("from")) {
                    if (token == XContentParser.Token.VALUE_NULL) {
                    } else if (token == XContentParser.Token.VALUE_STRING) {
                        vFrom = parser.text(); // a String
                    } else {
                        vFrom = parser.numberValue(); // a Number
                    }
                } else if (currentFieldName.equals("to")) {
                    if (token == XContentParser.Token.VALUE_NULL) {
                    } else if (token == XContentParser.Token.VALUE_STRING) {
                        vTo = parser.text(); // a String
                    } else {
                        vTo = parser.numberValue(); // a Number
                    }
                } else if ("include_lower".equals(currentFieldName) || "includeLower".equals(currentFieldName)) {
                    includeLower = parser.booleanValue();
                } else if ("include_upper".equals(currentFieldName) || "includeUpper".equals(currentFieldName)) {
                    includeUpper = parser.booleanValue();
                } else if ("gt".equals(currentFieldName)) {
                    if (token == XContentParser.Token.VALUE_NULL) {
                    } else if (token == XContentParser.Token.VALUE_STRING) {
                        vFrom = parser.text(); // a String
                    } else {
                        vFrom = parser.numberValue(); // a Number
                    }
                    includeLower = false;
                } else if ("gte".equals(currentFieldName) || "ge".equals(currentFieldName)) {
                    if (token == XContentParser.Token.VALUE_NULL) {
                    } else if (token == XContentParser.Token.VALUE_STRING) {
                        vFrom = parser.text(); // a String
                    } else {
                        vFrom = parser.numberValue(); // a Number
                    }
                    includeLower = true;
                } else if ("lt".equals(currentFieldName)) {
                    if (token == XContentParser.Token.VALUE_NULL) {
                    } else if (token == XContentParser.Token.VALUE_STRING) {
                        vTo = parser.text(); // a String
                    } else {
                        vTo = parser.numberValue(); // a Number
                    }
                    includeUpper = false;
                } else if ("lte".equals(currentFieldName) || "le".equals(currentFieldName)) {
                    if (token == XContentParser.Token.VALUE_NULL) {
                    } else if (token == XContentParser.Token.VALUE_STRING) {
                        vTo = parser.text(); // a String
                    } else {
                        vTo = parser.numberValue(); // a Number
                    }
                    includeUpper = true;
                } else if (currentFieldName.equals("unit")) {
                    unit = DistanceUnit.fromString(parser.text());
                } else if (currentFieldName.equals("distance_type") || currentFieldName.equals("distanceType")) {
                    geoDistance = GeoDistance.fromString(parser.text());
                } else if (currentFieldName.endsWith(GeoPointFieldMapper.Names.LAT_SUFFIX)) {
                    point.resetLat(parser.doubleValue());
                    fieldName = currentFieldName.substring(0, currentFieldName.length() - GeoPointFieldMapper.Names.LAT_SUFFIX.length());
                } else if (currentFieldName.endsWith(GeoPointFieldMapper.Names.LON_SUFFIX)) {
                    point.resetLon(parser.doubleValue());
                    fieldName = currentFieldName.substring(0, currentFieldName.length() - GeoPointFieldMapper.Names.LON_SUFFIX.length());
                } else if (currentFieldName.endsWith(GeoPointFieldMapper.Names.GEOHASH_SUFFIX)) {
                    GeoHashUtils.decode(parser.text(), point);
                    fieldName = currentFieldName.substring(0, currentFieldName.length() - GeoPointFieldMapper.Names.GEOHASH_SUFFIX.length());
                } else if ("_name".equals(currentFieldName)) {
                    queryName = parser.text();
                } else if ("optimize_bbox".equals(currentFieldName) || "optimizeBbox".equals(currentFieldName)) {
                    optimizeBbox = parser.textOrNull();
                } else if ("normalize".equals(currentFieldName)) {
                    normalizeLat = parser.booleanValue();
                    normalizeLon = parser.booleanValue();
                } else {
                    point.resetFromString(parser.text());
                    fieldName = currentFieldName;
                }
            }
        }

        Double from = null;
        Double to = null;
        if (vFrom != null) {
            if (vFrom instanceof Number) {
                from = unit.toMeters(((Number) vFrom).doubleValue());
            } else {
                from = DistanceUnit.parse((String) vFrom, unit, DistanceUnit.DEFAULT);
            }
            from = geoDistance.normalize(from, DistanceUnit.DEFAULT);
        }
        if (vTo != null) {
            if (vTo instanceof Number) {
                to = unit.toMeters(((Number) vTo).doubleValue());
            } else {
                to = DistanceUnit.parse((String) vTo, unit, DistanceUnit.DEFAULT);
            }
            to = geoDistance.normalize(to, DistanceUnit.DEFAULT);
        }

        if (normalizeLat || normalizeLon) {
            GeoUtils.normalizePoint(point, normalizeLat, normalizeLon);
        }

        MappedFieldType fieldType = parseContext.fieldMapper(fieldName);
        if (fieldType == null) {
            throw new QueryParsingException(parseContext, "failed to find geo_point field [" + fieldName + "]");
        }
        if (!(fieldType instanceof GeoPointFieldMapper.GeoPointFieldType)) {
            throw new QueryParsingException(parseContext, "field [" + fieldName + "] is not a geo_point field");
        }
        GeoPointFieldMapper.GeoPointFieldType geoFieldType = ((GeoPointFieldMapper.GeoPointFieldType) fieldType);

        IndexGeoPointFieldData indexFieldData = parseContext.getForField(fieldType);

        if (from == null) {
            from = new Double(0);
        }

        if (to == null) {
            to = new Double(GeoUtils.EARTH_SEMI_MINOR_AXIS);
        }

        Query query = new GeoPointDistanceRangeQuery(indexFieldData.getFieldNames().indexName(), point.lon(), point.lat(),
                (includeLower) ? from : from+1, (includeUpper) ? to : to-1 );
//        Query query = new GeoDistanceRangeQuery(point, from, to, includeLower, includeUpper, geoDistance, geoFieldType, indexFieldData,
//                optimizeBbox);
        if (queryName != null) {
            parseContext.addNamedQuery(queryName, query);
        }
        return query;
    }
}
