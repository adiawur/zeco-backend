package FYP.zecoHelpDesk_backend.util;


public class ZoneUtils {

    public static Zone getZoneByCoordinates(
            double lat,
            double lng
    ) {

        // URBAN WEST
        if (
                lat >= -6.25 &&
                        lat <= -6.05 &&
                        lng >= 39.15 &&
                        lng <= 39.30
        ) {
            return Zone.URBAN_WEST;
        }

        // UNGUJA NORTH
        if (
                lat >= -6.35 &&
                        lat < -6.05 &&
                        lng >= 39.20 &&
                        lng <= 39.50
        ) {
            return Zone.UNGUJA_NORTH;
        }

        // UNGUJA SOUTH
        if (
                lat >= -6.60 &&
                        lat < -6.35 &&
                        lng >= 39.20 &&
                        lng <= 39.50
        ) {
            return Zone.UNGUJA_SOUTH;
        }

        // PEMBA NORTH
        if (
                lat >= -5.20 &&
                        lat <= -4.80 &&
                        lng >= 39.65 &&
                        lng <= 39.85
        ) {
            return Zone.PEMBA_NORTH;
        }

        // PEMBA SOUTH
        if (
                lat >= -5.40 &&
                        lat < -5.20 &&
                        lng >= 39.60 &&
                        lng <= 39.85
        ) {
            return Zone.PEMBA_SOUTH;
        }

        // FALLBACK
        return Zone.ZANZIBAR;
    }


    public static Zone findNearestZone(
            Zone origin
    ) {

        return switch (origin) {

            case URBAN_WEST ->
                    Zone.UNGUJA_NORTH;

            case UNGUJA_NORTH ->
                    Zone.UNGUJA_SOUTH;

            case UNGUJA_SOUTH ->
                    Zone.URBAN_WEST;

            case PEMBA_NORTH ->
                    Zone.PEMBA_SOUTH;

            case PEMBA_SOUTH ->
                    Zone.PEMBA_NORTH;

            default ->
                    Zone.ZANZIBAR;
        };
    }
}
