# Terminális műveletek

- [Terminális vs. köztes](#terminális-vs-köztes)
- [Predikátum-megfelelés](#predikátum-megfelelés)
  - [anyMatch(predicate)](#anymatchpredicate)
  - [allMatch(predicate)](#allmatchpredicate)
  - [noneMatch(predicate)](#nonematchpredicate)
- [Minimum és maximum](#minimum-és-maximum)
  - [min(comparator)](#mincomparator)
  - [min() - Int/Long/Double](#min---intlongdouble)
  - [max(comparator)](#maxcomparator)
  - [max() - Int/Long/Double](#max---intlongdouble)
- [Mellékhatással járó műveletek](#mellékhatással-járó-műveletek)
  - [forEach(consumer)](#foreachaction)
- [Keresés](#keresés)
  - [findFirst()](#findfirst)
- [Megszámlálás](#megszámlálás)
  - [count()](#count)
- [Összegyűjtés](#összegyűjtés)
  - [toList()](#tolist)
  - [collect(collector)](#collectcollector)

Ez a dokumentum egy gyorstalpaló, mely a leggyakoribb terminális Stream-műveletek (*terminal operation*) megértését szeretné elősegíteni. Vágjunk is bele!

## Terminális vs. köztes

A Stream-műveletek az alábbi két csoportba sorolhatók:
- A *köztes* műveletek (*intermediate operation*) egy új Streamet adnak vissza. Ezeket használhatjuk arra, hogy továbbfűzzük a csővezetékeinket, újabb és újabb műveleteket végezve az elemeken. Ilyen műveletek például a `filter` vagy a `map`.
  - A [Köztes Stream-műveletek - vizualizálva](./03-koztes-stream-muveletek-vizualizalva.md) dokumentum a leggyakoribb köztes műveletek összefoglalását tartalmazza.
- A terminális műveletek ezzel szemben a csővezetékeink lezárásául szolgálnak, hiszen vagy valamilyen mellékhatással járhatnak (azaz, `void` visszatérési értékűek), vagy pedig Stream-től különböző értéket adnak vissza (például egy `List` példányt). Ilyen műveletek például a `forEach` vagy a `toList`.

## Predikátum-megfelelés 

A következő műveletek mind azt vizsgálják, hogy a Stream elemei megfelelnek-e a megadott predikátumnak (azaz, rendelkeznek-e valamilyen tulajdonsággal). A predikátum minden esetben a `Predicate` funkcionális interfész egy példányaként jelenik meg:

* [Predicate](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/function/Predicate.html)
  * Egy predikátumnak felel meg, igazságértéket képez valamilyen bemenetre.
  * Egyetlen típusparamétere van: a bemeneti érték típusa. A kimenete mindig `boolean`.
  ```Java
  Predicate<String> isLong = str -> str.length() > 10;
        
  isLong.test("Hello, World!");
  ```

Tipikusan olyankor használjuk ezeket a műveleteket, amikor olyan kérdésekre keressük a választ, mint:
- van-e olyan elem, mely...,
- minden elem olyan-e, hogy...,
- ugye egyik elem sem olyan, hogy...

### anyMatch(predicate)

* [JavaDoc](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html#anyMatch(java.util.function.Predicate))

`True` értéket ad vissza, amint az első olyan elemmel találkozik, mely kielégíti a megadott predikátumot. Ebben az esetben a Stream további elemei *nem* lesznek feldolgozva.

`False` értéket ad vissza amennyiben a Stream üres, vagy nem tartalmaz egyetlen olyat elemet sem, mely teljesíti a megadott predikátumot.

```Java
List.of("a", "bb", "ccc")
    .stream()
    .peek(str -> System.out.println(str))
    .anyMatch(str -> str.length() == 2);
```

### allMatch(predicate)

* [JavaDoc](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html#allMatch(java.util.function.Predicate))

`True` értéket ad vissza, ha a Stream minden eleme teljesíti a megadott predikátumot, vagy a Stream üres.

`False` értéket ad vissza, amint az első olyan elemmel találkozik, mely *nem* elégíti ki a predikátumot.

```Java
List.of("a", "bb", "ccc")
    .stream()
    .peek(str -> System.out.println(str))
    .allMatch(str -> str.length() == 1);
```

### noneMatch(predicate)

* [JavaDoc](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html#allMatch(java.util.function.Predicate))

`True` értéket ad vissza, ha a Stream egyetlen eleme sem teljesíti a megadott predikátumot, vagy a Stream üres.

`False` értéket ad vissza, amint az első olyan elemmel találkozik, mely kielégíti a predikátumot.

```Java
List.of("a", "bb", "ccc")
    .stream()
    .peek(str -> System.out.println(str))
    .noneMatch(str -> str.length() == 1);
```

## Minimum és maximum

A következő műveletek a Streamek legkisebb illetve legnagyobb elemének megkeresésére szolgálnak.

### min(comparator)

* [JavaDoc](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html#min(java.util.Comparator))

Visszaadja a Stream legkisebb elemét tartalmazó Optionalt, avagy egy üres Optionalt, ha a Stream nem tartalmaz elemeket.

Az elemek rendezése, valamint a legkisebb elem megállapítása a kapott `Comparator` segítségével történik.
* Lásd [Összehasonlítás és rendezés](./02-osszehasonlitas-es-rendezes.md)

```Java
List.of("bb", "cc", "a")
    .stream()
    .min(Comparator.naturalOrder())
```

### min() - Int/Long/Double

* [JavaDoc (IntStream)](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/IntStream.html#min())

Primitív Streamek (azaz, `IntStream`, `LongStream` és `DoubleStream`) esetén használható művelet. Nincsen `comparator` paramétere, hiszen a primitív streamek elemei mind rendelkeznek természetes rendezéssel.

Visszaadja a Stream legkisebb elemét tartalmazó Optionalt, avagy egy üres Optionalt, ha a Stream nem tartalmaz elemeket.

```Java
// Hány karakterből áll a legrövidebb String?
List.of("bb", "cc", "a")
    .stream()
    .mapToInt(str -> str.length())
    .min();
```

### max(comparator)

* [JavaDoc](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html#max(java.util.Comparator))

Visszaadja a Stream legnagyobb elemét tartalmazó Optionalt, avagy egy üres Optionalt, ha a Stream nem tartalmaz elemeket.

Az elemek rendezése, valamint a legnagyobb elem megállapítása a kapott `Comparator` segítségével történik.
* Lásd [Összehasonlítás és rendezés](./02-osszehasonlitas-es-rendezes.md)

```Java
List.of("bb", "cc", "a")
    .stream()
    .max(Comparator.naturalOrder())
```

### max() - Int/Long/Double

* [JavaDoc (IntStream)](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/IntStream.html#max())

Primitív Streamek (azaz, `IntStream`, `LongStream` és `DoubleStream`) esetén használható művelet. Nincsen `comparator` paramétere, hiszen a primitív streamek elemei mind rendelkeznek természetes rendezéssel.

Visszaadja a Stream legnagyobb elemét tartalmazó Optionalt, avagy egy üres Optionalt, ha a Stream nem tartalmaz elemeket.

```Java
// Hány karakterből áll a leghosszabb String?
List.of("bb", "ccc", "a")
    .stream()
    .mapToInt(str -> str.length())
    .max();
```

## Mellékhatással járó műveletek

A következő műveletek visszatérési értéke `void`, azaz, ahelyett, hogy visszaadnának valamint, inkább valamilyen mellékhatást hajtanak végre. Ilyen mellékhatás lehet például a fájlba vagy a kimenetre írás.

### forEach(action)

* [JavaDoc](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html#forEach(java.util.function.Consumer))

A Stream minden elemével megfuttatja a kapott `action`-t. Az `action` a `Consumer` funkcionális interfészt valósítja meg:
* [Consumer](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/function/Consumer.html)
  * Egy olyan műveletnek felel meg, mely kap valamilyen értéket, azonban nem ad vissza semmit. Lényegében elnyeli a kapott értéket.
  * Egyetlen típusparamétere van: a kapott, elnyelt érték típusa.
  ```Java
  Consumer<String> consumer = str -> System.out.println(str);

  consumer.accept("Hello, World!");
  ```

```Java
List.of("a", "b", "c")
    .stream()
    .forEach(str -> {
        System.out.println(str);
    });
```

## Keresés

A következő műveletekkel ki tudunk venni egyetlen elemet a Streamből, azaz, lényegében keresni tudunk.

### findFirst()

* [JavaDoc](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html#findFirst())

Visszaad egy Optionalt, mely
- a Stream első elemét tartalmazza, ha a Streamnek vannak elemei,
- üres (`Optional.empty`), amennyiben a Streamnek nincsenek elemei.

Hogyan használható a `findFirst()` keresésre, ha egyszer nem mondhatjuk meg neki, hogy mit keresünk? Egyszerűen csak használjuk más művelet után, terminálisként.

- Adott tulajdonságú elem megkeresése (`filter + findFirst`). Lényegében megfelel az `anyMatch`-nek, csak vissza is adja a megfelelő elemet (egy Optionalben).
  ```Java
  List.of("a", "bb", "ccc")
    .stream()
    .filter(str -> str.length() == 2) // Ettől a ponttól a Stream csak 2-hosszúságú elemeket tartalmaz.
    .findFirst();  // Ezek közül kapjuk meg a legelsőt.
  ```
- Rendezett Stream megfelelő elemének kivétele (`sorted + skip + findFirst`).
  ``` Java
  // Keressük a második leghosszabb Stringet.
  Comparator<String> lengthDescending = Comparator.comparing(
    String::length
  ).reversed();

  List.of("a", "ccc", "bb", "d")
    .stream()
    .sorted(lengthDescending) // Hosszúság szerint csökkenő sorrend. Azaz, a leghosszabb lesz a legelső.
    .skip(1) // Kihagyjuk a leghosszabbat, hogy a második leghosszabbat kapjuk.
    .findFirst(); // A skip miatt a második leghosszabb elem lesz a stream legelső eleme.
  ```

## Megszámlálás

### count()

* [JavaDoc](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html#count())

Visszaadja a Stream elemeinek számát.

Remekül használható olyan kérdések megválaszolására, mint

- Hány olyan elem van, mely...?

a `filter + count` minta segítségével.

```Java
List.of("a", "bb", "ccc", "dd")
    .stream()
    .filter(str -> str.length() == 2)
    .count();
```

## Összegyűjtés

### toList()

* [JavaDoc](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html#toList())

A Stream elemeit egy listába gyűjti, a beérkezésük sorrendjében. A visszadott `List` unmodifiable, azaz csak olvasni lehet (nem adhatunk hozzá új elemet, nem távolíthatjuk el az elemeit, stb.).

```Java
List.of("a", "bb", "ccc")
    .stream()
    .filter(str -> str.length() <= 2)
    .toList();
```

### collect(collector)

* [JavaDoc](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html#collect(java.util.stream.Collector))

A megadott `Collector` (vagy gyűjtő) alkalmazásával végez [módosítható redukciót](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/package-summary.html#MutableReduction).

Részéletes ismertetésért, lásd: [Collectors](./05-collectors.md)
