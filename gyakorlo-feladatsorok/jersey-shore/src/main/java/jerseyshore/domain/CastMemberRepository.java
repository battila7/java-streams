package jerseyshore.domain;

import java.util.List;

public class CastMemberRepository {
    public List<CastMember> getAllCharacters() {
        return List.of(
            new CastMember(
                    "Mike Sorrentino",
                    "The Situation",
                    "New Jersey",
                    true,
                    null,
                    47
            ),
            new CastMember(
                    "Nicole Polizzi",
                    "Snooki",
                    "New York",
                    false,
                    "tequila",
                    30
            ),
            new CastMember(
                    "Paul DelVecchio",
                    "Pauly D",
                    "Rhode Island",
                    true,
                    "hair gel",
                    50
            ),
            new CastMember(
                    "Vinny Guadagnino",
                    "Vinny",
                    "New York",
                    true,
                    "vodka shots",
                    48
            ),
            new CastMember(
                    "Jenny Farley",
                    "JWoww",
                    "New York",
                    false,
                    "vodka",
                    40
            ),
            new CastMember(
                    "Samantha Giancola",
                    "Sammi Sweetheart",
                    "New Jersey",
                    true,
                    "vodka",
                    35
            ),
            new CastMember(
                    "Ronnie Ortiz-Magro",
                    "Ronnie",
                    "New York",
                    true,
                    "whiskey-coke",
                    45
            )
        );
    }
}
