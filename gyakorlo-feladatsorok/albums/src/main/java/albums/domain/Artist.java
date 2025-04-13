package albums.domain;

import java.util.Map;

/**
 * Represents an artist with a name, a nullable country, and social media handles.
 *
 * @param name the name of the artist
 * @param country the country of the artist (can be null)
 * @param socialMediaHandles a map of social media platform names to handles
 */
public record Artist(String name, String country, Map<String, String> socialMediaHandles) {}
