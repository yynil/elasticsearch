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

package org.elasticsearch.common.geo;


import org.apache.lucene.util.*;
import org.apache.lucene.util.GeoUtils;

/**
 *
 */
public final class GeoPoint {

    private double lat;
    private double lon;
    private final static double TOLERANCE = org.apache.lucene.util.GeoUtils.TOLERANCE;

    public GeoPoint() {
    }

    /**
     * Create a new Geopointform a string. This String must either be a geohash
     * or a lat-lon tuple.
     *   
     * @param value String to create the point from
     */
    public GeoPoint(String value) {
        this.resetFromString(value);
    }

    public GeoPoint(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public GeoPoint reset(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
        return this;
    }

    public GeoPoint resetLat(double lat) {
        this.lat = lat;
        return this;
    }

    public GeoPoint resetLon(double lon) {
        this.lon = lon;
        return this;
    }

    public GeoPoint resetFromString(String value) {
        int comma = value.indexOf(',');
        if (comma != -1) {
            lat = Double.parseDouble(value.substring(0, comma).trim());
            lon = Double.parseDouble(value.substring(comma + 1).trim());
        } else {
            resetFromGeoHash(value);
        }
        return this;
    }

    public GeoPoint resetFromIndexHash(long hash) {
        lon = GeoUtils.mortonUnhashLon(hash);
        lat = GeoUtils.mortonUnhashLat(hash);
        return this;
    }

    public GeoPoint resetFromGeoHash(String hash) {
        GeoHashUtils.decode(hash, this);
        return this;
    }

    public final double lat() {
        return this.lat;
    }

    public final double getLat() {
        return this.lat;
    }

    public final double lon() {
        return this.lon;
    }

    public final double getLon() {
        return this.lon;
    }

    public final String geohash() {
        return org.apache.lucene.util.GeoHashUtils.stringEncode(lon, lat);
    }

    public final String getGeohash() {
        return org.apache.lucene.util.GeoHashUtils.stringEncode(lon, lat);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final GeoPoint geoPoint = (GeoPoint) o;
        final double lonCompare = geoPoint.lon - lon;
        final double latCompare = geoPoint.lat - lat;

        if ( lonCompare < -TOLERANCE || lonCompare > TOLERANCE) return false;
        if ( latCompare < -TOLERANCE || latCompare > TOLERANCE) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = lat != +0.0d ? Double.doubleToLongBits(lat) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = lon != +0.0d ? Double.doubleToLongBits(lon) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "[" + lat + ", " + lon + "]";
    }

    public static GeoPoint parseFromLatLon(String latLon) {
        GeoPoint point = new GeoPoint();
        point.resetFromString(latLon);
        return point;
    }
}
