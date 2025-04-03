# Összehasonlítás és rendezés

A `Comparator` használatáról szóló legjobb forrás:
* [Writing and Combining Comparators](https://dev.java/learn/lambdas/writing-comparators/)

Ennek egy rövid kivonata szerepel itt, hogy egy helyen meglegyenek a legfontosabbak.

* [Comparable<T>](#comparable)
* [Comparator<T>](#comparator)
  * [Comparator vs. Comparable](#comparator-vs-comparable)
  * [Comparator lambdával](#comparator-lambdával)
  * [Rendezés egyedi kulcs szerint](#rendezés-egyedi-kulcs-szerint)
  * [Többszintű vagy láncolt rendezés](#többszintű-avagy-láncolt-rendezés)
  * [Továbbiak](#továbbiak)

## Comparable<T>

Legyen adott a következő, országokat reprezentáló osztály:

```Java
class Country {
    private final String name;
    private final int area;

    public Country(String name, int area) {
        this.name = name;
        this.area = area;
    }

    public String getName() {
        return name;
    }

    public int getArea() {
        return area;
    }
}
```

Tegyük fel, hogy az így megadott országokat szeretnénk terület szerint rendezni. Függetlenül attól, hogy milyen rendezési algoritmust választunk, egy alapproblémát mindenképpen meg kell oldanunk: bármely két országról el kell tudnunk dönteni, hogy melyik szerepel előbb a terület szerinti sorrendben sorrendben.

Ha a fenti kérdést meg tudjuk válaszolni, akkor máris használhatjuk a Java beépített eszközét, a [Comparable](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/lang/Comparable.html) interfészt, hogy az ország típusunkat összehasonlíthatóvá tegyük. A `Comparable<T>` interfész egyetlen implementálandó metódust definiál
* `int compareTo(T o)`
  * A jelenlegi példányt (`this`) hasonlítja a paraméterül kapott értékhez (`o`).
  * Negatív értéket ad vissza, ha ez a példány (`this`) kisebb, mint, amihez hasonlítjuk (`o`)
  * Pozitív értéket ad vissza, ha ez a példány (`this`) nagyobb, mint, amihez hasonlítjuk (`o`)
  * Végül, nullát ad vissza, ha a két példány a sorrend szerint azonos. 


Ha mi terület szerint szeretnénk növekvő sorrendbe rendezni, akkor az implementációnk a következő módon alakulhat:
```Java
class Country implements Comparable<Country> {
    private final String name;
    private final int area;

    public Country(String name, int area) {
        this.name = name;
        this.area = area;
    }

    public String getName() {
        return name;
    }

    public int getArea() {
        return area;
    }

    @Override
    public int compareTo(Country other) {
        if (this.area < other.area) {
            return -1;
        }
        
        if (this.area > other.area) {
            return 1;
        }
        
        return 0;
    }
}
```

Próbáljuk is ki!

```Java
class App {
    public static void main(String[] args) {
        final var countries = new ArrayList<Country>();
        countries.add(new Country("Germany", 357));
        countries.add(new Country("Hungary", 93));
        countries.add(new Country("France", 551));

        Collections.sort(countries);

        for (var country : countries) {
            System.out.println(country.getName() + " - " + country.getArea());
        }
    }
}
```

Szuperjó, az országok valóban a területük szerinti növekvő sorrendben lettek kiírva.

## Comparator<T>

### Comparator vs. Comparable

Ahogy az mindig lenni szokott, megváltoztak a követelmények: míg a kódbázis egyik részén továbbra is terület szerint, addig a másik részén név szerint kellene sorrendbe állítanunk az országokat.

Sajnos nem tudunk új `compareTo(Country other)` metódust hozzáadni, hiszen ugyanazt az interfészt ugyanzon osztály nem valósíthatja meg többször. Azaz, valami más lehetőség után kell néznünk.

Ha megnézzük az előzőleg is használt `Collections.sort` metódust, akkor láthatjuk, hogy van egy második szignatúrája is:
* `<T> void sort(List<T> list, Comparator<? super T> c)`

A fenti változat a rendezendő listán felül egy [Comparator](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/Comparator.html) megvalósítást is vár. Na, de mi az a Comparator?

A `Comparator<T>` egy funkcionális interfész, melynek egyetlen absztrakt metódusa az alábbi:
* `int compare(T o1, T o2)`

Emlékeztetőül, a `Comparable<T>` interfész a következő metódust definiálta:
* `int compareTo(T o)`

Míg a `Comparable` az adott példányt (melyen meghívjuk a `compareTo` metódust) hasonlítja egy másik értékhez, addig a `Comparable` két példány összehasonlítására szolgál. Azaz, a `Comparator` segítségével akárhány, tetszőleges sorrendet ki tudunk fejezni, anélkül, hogy az összehasonlított osztályt módosítanunk kellene vagy módosítani tudnánk.

### Comparator lambdával

Térjünk vissza a megelőző feladatunkhoz: a `Country` osztályt szeretnénk név szerint rendezni.

Kihasználva, hogy a `Comparator<T>` egy funkcionális interfész, a név szerinti rendezést megvalósító `Comparatort` kifejezhetjük egy lambda segítségével:

```Java
class App {
    public static void main(String[] args) {
        final var countries = new ArrayList<Country>();
        countries.add(new Country("Germany", 357));
        countries.add(new Country("Hungary", 93));
        countries.add(new Country("France", 551));

        Comparator<Country> orderByName = (o1, o2 ) -> {
            if (o1.getName().compareTo(o2.getName()) < 0) {
                return -1;
            }

            if (o1.getName().compareTo(o2.getName()) > 0) {
                return 1;
            }

            return 0;
        };

        Collections.sort(countries, orderByName);

        for (var country : countries) {
            System.out.println(country.getName() + " - " + country.getArea());
        }
    }
}
```

Mindezt persze kifejezhetjük sokkal egyszerűbben is, pusztán a `name` `compareTo` metódusára támaszkodva:

```Java
class App {
    public static void main(String[] args) {
        final var countries = new ArrayList<Country>();
        countries.add(new Country("Germany", 357));
        countries.add(new Country("Hungary", 93));
        countries.add(new Country("France", 551));

        Comparator<Country> orderByName = (o1, o2 ) -> o1.getName().compareTo(o2.getName());

        Collections.sort(countries, orderByName);

        for (var country : countries) {
            System.out.println(country.getName() + " - " + country.getArea());
        }
    }
}
```

Csudijó! Immár
* tetszőleges osztályt vagy rekordot (akár olyat is, melyet nem mi írtunk)
* tetszőleges sorrendbe
tudunk rendezni, egy megfelelő saját `Comparator<T>` megvalósítás segítségével.

### Rendezés egyedi kulcs szerint

A fenti módszer, mégpedig, hogy valamely típus példányait valamilyen saját kulcs (vagy tulajdonság) szerint akarjuk rendezni, olyan egyakori, hogy a Java beépített eszközt is kínál a megoldására.

Ez az eszköz pedig a [Comparator.comparing](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/Comparator.html#comparing(java.util.function.Function)) statikus metódus, mely egyetlen paramétert vár: egy olyan, `Function<T, R>` megvalósítást, mely a bemeneti típus (`T`) példányaiból kiszámolja a rendezési kulcsot (melynek típusa `R`).

Az előző példánkban az alábbi `Comparator` szerepelt:
```Java
Comparator<Country> orderByName = (o1, o2 ) -> o1.getName().compareTo(o2.getName());
``` 

Vegyük észre, hogy mind `o1`-en, mind `o2`-n a `getName()` metódust hívjuk, majd az így kapott értékeket hasonlítjuk össze. Azaz, a `getName` fogja a rendezési kulcsot szolgáltatni. Tehát ezt kell hívnunk a `comparing`-nek adott `Function`-megvalósításban is.

```Java
Comparator<Country> orderByName = Comparator.comparing(country -> country.getName());
```

Ha jártasak vagyunk a metódusreferenciákban, akkor észrevehetjük, hogy ez kifejezhető még rövidebben is:
```Java
class App {
    public static void main(String[] args) {
        final var countries = new ArrayList<Country>();
        countries.add(new Country("Germany", 357));
        countries.add(new Country("Hungary", 93));
        countries.add(new Country("France", 551));

        Comparator<Country> orderByName = Comparator.comparing(Country::getName);

        Collections.sort(countries, orderByName);

        for (var country : countries) {
            System.out.println(country.getName() + " - " + country.getArea());
        }
    }
}
```

### Többszintű, vagy láncolt rendezés

Bár azonos nevű országok nincsenek (szerencsére), azonos területűek (legalábbis kerekítve) azonban igen. Hogyan tudnánk egy olyan rendezést készíteni, mely figyelembe veszi mindkét tulajdonságot? Azaz, előbb terület szerint rendez, majd, az azonos területű országokat név szerint.

Egy naiv megvalósítás lehet az alábbi:

```Java
Comparator<Country> orderByAreaThenName = (o1, o2) -> {
    int comparedByArea = o1.compareTo(o2);

    if (comparedByArea != 0) {
        return comparedByArea;
    }

    return o1.getName().compareTo(o2.getName());
};
```

A `getArea()` és a `getName()` hívások mind arra utalnak, hogy előbb az `area`, majd a `name` lesz a rendezés kulcsa. Váltsuk ki így ezeket `Comparator.comparing()` hívásokkal:

```Java
Comparator<Country> orderByAreaThenName = (o1, o2) -> {
    int comparedByArea = Comparator.comparing(Country::getArea).compare(o1, o2);

    if (comparedByArea != 0) {
        return comparedByArea;
    }

    return Comparator.comparing(Country::getName).compare(o1, o2);
};
```

Mivel a `comparing()` egy úgynevezett statikus gyártó metódus (*static factory method*), ezért az csak egy `Comparator` példányt fog visszaadni. A tényleges összehasonlítást a `compare` meghívásával végezhetjük el.

Olyan, mintha valami mintára bukkantunk volna: először az első kulcs szerint hasonlítunk `comparing` segítségével, aztán, ha egyenlőség van, akkor a második kulcs szerint és így tovább. Nincsen erre valami beépített eszköz, hogy mindezt elegánsabban tudjuk megfogalmazni?

Természetesen erre a kérdésre igen a válasz: létezik és `Comparator.thenComparing` a neve. Ezzel a metódussal minden `Comparator` bír (hiszen `default` interfész metódus) és lehetővé teszi a `Comparator`-ok láncolását, egymás utáni alkalmazását. Ennek két paraméterezését érdemes ismerni:
- [`thenComparing(Comparator<? super T> other)`](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/Comparator.html#thenComparing(java.util.Comparator))
  * Paraméterként egy `Comparator`-t vár, mely abban az esetben lesz felhasználva ha az aktuális (`this`) `Comparator` szerint a két példány azonos.
  ```Java
  class App {
    public static void main(String[] args) {
        final var countries = new ArrayList<Country>();
        countries.add(new Country("Germany", 357));
        countries.add(new Country("Hungary", 93));
        countries.add(new Country("Imaginary", 93));
        countries.add(new Country("France", 551));

        Comparator<Country> orderByNameThenArea = Comparator
                .comparing(Country::getName)
                .thenComparing(Comparator.comparing(Country::getArea));

        Collections.sort(countries, orderByNameThenArea);

        for (var country : countries) {
            System.out.println(country.getName() + " - " + country.getArea());
        }
    }
  }  
  ```
- [`thenComparing(Function)`](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/Comparator.html#thenComparing(java.util.function.Function))
  * Leegyszerűsíti azt az esetet, amikor valamilyen kulcs szerint akarunk rendezni. Egészen pontosan nem szükséges újabb `Comparator.comparing` hívás.
  ```Java
  // These are the same
  // using thenComparing(Comparator)
  .thenComparing(Comparator.comparing(Country::getArea));

  // using thenComparing(Function)
  .thenComparing(Country::getArea);
  ```

### Továbbiak

Érdemes még a továbbiakkal is megismerkedni:
* rendezés fordított sorrendben
  * [`Comparator.reverseOrder`](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/Comparator.html#reverseOrder())
  * [`Comparator.reversed`](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/Comparator.html#reversed())
* `null`-értékek megfelelő kezelése
  * [`Comparator.nullsFirst`](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/Comparator.html#nullsFirst(java.util.Comparator))
  * [`Comparator.nullsLast`](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/Comparator.html#nullsLast(java.util.Comparator))
