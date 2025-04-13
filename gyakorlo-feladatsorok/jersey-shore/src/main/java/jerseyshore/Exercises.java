package jerseyshore;

import java.util.List;
import java.util.Map;

public interface Exercises {
    /**
     * Kiírja azon szereplők beceneveit a konzolra, akik nem olasz származásúak.
     * Minden név külön sorban szerepel.
     */
    void printNicknamesWithoutItalianHeritage();

    /**
     * Visszaadja a TOP 3 legkevesebb részben feltűnt szereplő becenevét.
     * A lista első eleme a legkevesebb részben megjelenő szereplő beceneve
     * legyen és így tovább.
     */
    List<String> getNicknamesForTheLeastThreeAppearedCastMembers();

    /**
     * Összegzi és visszaadja, hogy a szereplők összesen hány epizódban szerepeltek.
     */
    int getTheTotalNumberOfAppearances();

    /**
     * Csoportosítja a szereplőket a származási államuk szerint. Visszaad egy olyan
     * Mapet, melyben a kulcsok az államok nevei, az értékek pedig a szereplők
     * becenevei.
     */
    Map<String, List<String>> getGroupedByHomeState();

    /**
     * Kiírja a szereplők neveit és beceneveit a konzolra, a becenevek szerinti
     * fordított sorrendben. Azaz, akinek az ábécében a leghátul van a beceneve,
     * az kerüljön kiírásra először.
     * A kiírás formátuma a következő legyen
     * <pre>
     *     "Becenév: Név"
     * </pre>
     */
    void printNicknamesToNamesInRevesedOrder();
}
