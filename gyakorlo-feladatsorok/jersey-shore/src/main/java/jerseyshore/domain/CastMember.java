package jerseyshore.domain;

/**
 * Represents a cast member of the Jersey Shore show.
 * @param name the real name of the cast member
 * @param nickname the nickname of the cast member
 * @param homeState the home state of the cast member
 * @param hasItalianHeritage whether the cast member has Italian heritage
 * @param favoriteDrink the favorite drink of the cast member
 * @param appearances the number of episodes the cast member appeared in
 */
public record CastMember(
    String name,
    String nickname,
    String homeState,
    boolean hasItalianHeritage,
    String favoriteDrink,
    int appearances
) {}