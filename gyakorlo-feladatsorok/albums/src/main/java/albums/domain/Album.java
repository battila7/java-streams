package albums.domain;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents an album with a title, an artist, a list of songs, and a release date.
 *
 * @param title the title of the album
 * @param artist the artist who created the album
 * @param songs the list of songs in the album
 * @param releaseDate the release date of the album
 */
public record Album(String title, Artist artist, List<Song> songs, LocalDate releaseDate) {}