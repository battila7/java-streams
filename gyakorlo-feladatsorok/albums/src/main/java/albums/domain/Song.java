package albums.domain;

/**
 * Represents a song with a title, a genre, and its duration in seconds.
 *
 * @param title the title of the song
 * @param genre the genre of the song
 * @param durationSeconds the duration of the song in seconds
 */
public record Song(String title, String genre, int durationSeconds) {}
