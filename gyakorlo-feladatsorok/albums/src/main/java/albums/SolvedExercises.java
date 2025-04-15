package albums;

import albums.domain.Album;
import albums.domain.AlbumRepository;
import albums.domain.Song;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SolvedExercises implements Exercises {
    public static void main(String[] args) {
        final var exercises = new SolvedExercises();

        exercises.printEveryDifferentGenreInAscendingOrder();
        System.out.println(exercises.getTheAlbumWithTheMostSongs());
        System.out.println(exercises.getTheNumberOfSongsOnEachBeatlesAlbum());
        exercises.printEachArtistsInstagramHandle();
        System.out.println(exercises.getTheNumberOfSongsOnEachAlbum());
        System.out.println(exercises.getTheNumberOfAlbumsByArtistName());
    }

    @Override
    public void printEveryDifferentGenreInAscendingOrder() {
        new AlbumRepository().getAllAlbums()
                .stream()
                .map(Album::songs)
                .flatMap(List::stream)
                .map(Song::genre)
                .distinct()
                .sorted()
                .forEach(System.out::println);
    }

    @Override
    public Optional<Album> getTheAlbumWithTheMostSongs() {
        return new AlbumRepository().getAllAlbums()
                .stream()
                .max(Comparator.comparing(album -> album.songs().size()));
    }

    @Override
    public Map<String, Integer> getTheNumberOfSongsOnEachBeatlesAlbum() {
        return new AlbumRepository().getAllAlbums()
                .stream()
                .filter(album -> album.artist().name().equals("The Beatles"))
                .collect(
                        Collectors.toMap(
                                Album::title,
                                album -> album.songs().size()
                        )
                );
    }

    @Override
    public void printEachArtistsInstagramHandle() {
        new AlbumRepository().getAllAlbums()
                .stream()
                .map(Album::artist)
                .distinct()
                .filter(artist -> artist.socialMediaHandles().containsKey("Instagram"))
                .forEach(artist -> {
                    System.out.printf("%s: %s\n", artist.name(), artist.socialMediaHandles().get("Instagram"));
                });
    }

    @Override
    public Map<String, Integer> getTheNumberOfSongsOnEachAlbum() {
        return new AlbumRepository().getAllAlbums()
                .stream()
                .collect(
                        Collectors.toMap(
                                Album::title,
                                album -> album.songs().size()
                        )
                );
    }

    @Override
    public Map<String, Long> getTheNumberOfAlbumsByArtistName() {
        return new AlbumRepository().getAllAlbums()
                .stream()
                .collect(
                        Collectors.groupingBy(
                                album -> album.artist().name(),
                                Collectors.counting()
                        )
                );
    }
}
