package albums;

import albums.domain.Album;

import java.util.Map;
import java.util.Optional;

public interface Exercises {
    /**
     * Kiírja az összes különböző zenei stílus (genre) nevét az ábécé
     * szerint növekvő sorrendben. Minden stílus új sorba kerüljön.
     */
    void printEveryDifferentGenreInAscendingOrder();

    /**
     * Visszaadja a legtöbb dalt tartalmazó albumot.
     */
    Optional<Album> getTheAlbumWithTheMostSongs();

    /**
     * Visszaad egy olyan Mapet, mely a "The Beatles" előadó által kiadott albumok
     * címéhez rendeli az albumon található dalok számát.
     */
    Map<String, Integer> getTheNumberOfSongsOnEachBeatlesAlbum();

    /**
     * Kiírja az előadók Instrgram felhasználónevét a következő formátumban:
     * <pre>
     *     <előadó neve>: <Instagram felhasználónév>
*    * </pre>
     * <p>
     * Figyelem: Az előadók között lehetnek olyanok is, akiknek nincs Instagram
     * fiókja.
     */
    void printEachArtistsInstagramHandle();

    /**
     * Visszaad egy olyan Mapet, mely az albumok címeihez a rajtuk található dalok számát
     * rendeli.
     */
    Map<String, Long> getTheNumberOfSongsOnEachAlbum();
}
