package jerseyshore;

import jerseyshore.domain.CastMember;
import jerseyshore.domain.CastMemberRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SolvedExercises implements Exercises {
    public static void main(String[] args) {
        final var exercises = new SolvedExercises();

        exercises.printNicknamesWithoutItalianHeritage();
        System.out.println(exercises.getNicknamesForTheLeastThreeAppearedCastMembers());
        System.out.println(exercises.getTheTotalNumberOfAppearances());
        System.out.println(exercises.getGroupedByHomeState());
        exercises.printNicknamesToNamesInRevesedOrder();
    }

    @Override
    public void printNicknamesWithoutItalianHeritage() {
        new CastMemberRepository().getAllCharacters()
                .stream()
                .filter(castMember -> !castMember.hasItalianHeritage())
                .map(CastMember::nickname)
                .forEach(System.out::println);
    }

    @Override
    public List<String> getNicknamesForTheLeastThreeAppearedCastMembers() {
        return new CastMemberRepository().getAllCharacters()
                .stream()
                .sorted(
                        Comparator.comparingInt(CastMember::appearances)
                )
                .limit(3)
                .map(CastMember::nickname)
                .toList();
    }

    @Override
    public int getTheTotalNumberOfAppearances() {
        return new CastMemberRepository().getAllCharacters()
                .stream()
                .mapToInt(CastMember::appearances)
                .sum();
    }

    @Override
    public Map<String, List<String>> getGroupedByHomeState() {
        return new CastMemberRepository().getAllCharacters()
                .stream()
                .collect(
                        Collectors.groupingBy(
                                CastMember::homeState,
                                Collectors.mapping(
                                        CastMember::nickname,
                                        Collectors.toList()
                                )
                        )
                );
    }

    @Override
    public void printNicknamesToNamesInRevesedOrder() {
        new CastMemberRepository().getAllCharacters()
                .stream()
                .sorted(
                        Comparator.comparing(CastMember::nickname)
                                .reversed()
                )
                .forEach(
                        castMember -> {
                            System.out.printf("%s: %s\n", castMember.nickname(), castMember.name());
                        }
                );
    }
}
