package albums.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;


public class AlbumRepository {
    public List<Album> getAllAlbums() {
        List<Album> albums = new ArrayList<>();

        // Artist and Album for The Beatles - "Abbey Road"
        Map<String, String> beatlesSocial = new HashMap<>();
        beatlesSocial.put("Twitter", "@TheBeatles");
        beatlesSocial.put("Instagram", "@thebeatles");
        Artist beatles = new Artist("The Beatles", "UK", beatlesSocial);
        List<Song> abbeyRoadSongs = List.of(
                new Song("Come Together", "Rock", 259),
                new Song("Something", "Rock", 182),
                new Song("Octopus's Garden", "Rock", 170),
                new Song("Here Comes the Sun", "Rock", 185)
        );
        Album abbeyRoad = new Album("Abbey Road", beatles, abbeyRoadSongs, LocalDate.of(1969, 9, 26));
        albums.add(abbeyRoad);
        List<Song> rubberSoulSongs = List.of(
                new Song("Drive My Car", "Rock", 148),
                new Song("Norwegian Wood (This Bird Has Flown)", "Rock", 124),
                new Song("You Won't See Me", "Rock", 179),
                new Song("In My Life", "Rock", 146),
                new Song("Think For Yourself", "Rock", 138)
        );
        Album rubberSoul = new Album("Rubber Soul", beatles, rubberSoulSongs, LocalDate.of(1965, 12, 3));
        albums.add(rubberSoul);

        // Artist and Album for Michael Jackson - "Thriller"
        Map<String, String> mjSocial = new HashMap<>();
        mjSocial.put("Instagram", "@michaeljackson");
        Artist mj = new Artist("Michael Jackson", "USA", mjSocial);
        List<Song> thrillerSongs = List.of(
                new Song("Thriller", "Pop", 357),
                new Song("Billie Jean", "Pop", 293),
                new Song("Beat It", "Rock", 258)
        );
        Album thriller = new Album("Thriller", mj, thrillerSongs, LocalDate.of(1982, 11, 30));
        albums.add(thriller);

        // Artist and Album for Queen - "A Night at the Opera"
        Map<String, String> queenSocial = new HashMap<>();
        queenSocial.put("Facebook", "QueenOfficial");
        Artist queen = new Artist("Queen", "UK", queenSocial);
        List<Song> nightAtOperaSongs = List.of(
                new Song("Bohemian Rhapsody", "Rock", 354),
                new Song("You're My Best Friend", "Rock", 186)
        );
        Album nightAtOpera = new Album("A Night at the Opera", queen, nightAtOperaSongs, LocalDate.of(1975, 11, 21));
        albums.add(nightAtOpera);

        return albums;
    }
}
