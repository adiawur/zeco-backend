package FYP.zecoHelpDesk_backend.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class LocationUtil {

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://nominatim.openstreetmap.org")
            .defaultHeader(
                    "User-Agent",
                    "ZECO-HelpDesk/1.0"
            )
            .build();

    public String reverseGeocode(
            Double latitude,
            Double longitude
    ) {

        if (latitude == null || longitude == null) {
            return "Location unavailable";
        }

        try {

            LocationResponse response = restClient.get()

                    .uri(uriBuilder -> uriBuilder
                            .path("/reverse")
                            .queryParam("format", "json")
                            .queryParam("lat", latitude)
                            .queryParam("lon", longitude)
                            .queryParam("addressdetails", 1)
                            .build())

                    .retrieve()

                    .body(LocationResponse.class);

            if (response == null ||
                    response.address() == null) {

                return latitude + ", " + longitude;
            }

            Address address = response.address();

            StringBuilder location =
                    new StringBuilder();

            add(location, address.village());
            add(location, address.suburb());
            add(location, address.town());
            add(location, address.city());
            add(location, address.state());
            add(location, address.country());

            if (location.isEmpty()) {
                return latitude + ", " + longitude;
            }

            return location.toString();

        } catch (Exception e) {

            return latitude + ", " + longitude;

        }
    }

    private void add(
            StringBuilder builder,
            String value
    ) {

        if (value != null &&
                !value.isBlank() &&
                !builder.toString().contains(value)) {

            if (!builder.isEmpty()) {
                builder.append(", ");
            }

            builder.append(value);
        }
    }

    private record LocationResponse(
            Address address
    ) {}

    private record Address(
            String village,
            String suburb,
            String town,
            String city,
            String state,
            String country
    ) {}

}