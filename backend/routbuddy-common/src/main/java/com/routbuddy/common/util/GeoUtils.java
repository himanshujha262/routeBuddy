package com.routbuddy.common.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public final class GeoUtils {

    private static final int SRID = 4326;
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), SRID);
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";

    private GeoUtils() {}

    public static Point createPoint(double longitude, double latitude) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }

    public static double haversineDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public static String encodeGeohash(double latitude, double longitude, int precision) {
        double[] latRange = {-90.0, 90.0};
        double[] lonRange = {-180.0, 180.0};
        StringBuilder geohash = new StringBuilder();
        boolean isEven = true;
        int bit = 0;
        int ch = 0;

        while (geohash.length() < precision) {
            if (isEven) {
                double mid = (lonRange[0] + lonRange[1]) / 2;
                if (longitude > mid) {
                    ch |= (1 << (4 - bit));
                    lonRange[0] = mid;
                } else {
                    lonRange[1] = mid;
                }
            } else {
                double mid = (latRange[0] + latRange[1]) / 2;
                if (latitude > mid) {
                    ch |= (1 << (4 - bit));
                    latRange[0] = mid;
                } else {
                    latRange[1] = mid;
                }
            }
            isEven = !isEven;
            if (bit < 4) {
                bit++;
            } else {
                geohash.append(BASE32.charAt(ch));
                bit = 0;
                ch = 0;
            }
        }
        return geohash.toString();
    }
}
