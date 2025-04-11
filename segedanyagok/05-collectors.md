# Collectors

- [Bevezetés](#bevezetés)
  - [Sztori: Telefonkönyv](#sztori-telefonkönyv)
  - [Redukció (reduction)](#redukció-reduction)
  - [Módosítható redukció (mutable reduction)](#módosítható-redukció-mutable-reduction)
  - [collect, Collector, Collectors](#collect-collector-collectors)
- [Összegyűjtés jól ismert kollekciókba](#összegyűjtés-jól-ismert-kollekciókba)
  - [toList()](#tolist)
  - [toSet()](#toset)
  - [toMap(keyMapper, valueMapper)](#tomapkeymapper-valuemapper)
  - [joining()](#joining)
- [Csoportosítás](#csoportosítás)
  - [Változik a telefonkönyv](#változik-a-telefonkönyv)
  - [groupingBy(classifier)](#groupingbyclassifier)
  - [partitioningBy(predicate)](#partitioningbypredicate)
- [Közjáték: Teljesen szükségtelennek tűnő Collectorok](#közjáték-teljesen-szükségtelennek-tűnő-collectorok)
  - [counting()](#counting)
  - [summing*(mapper)](#summingmapper)
  - [averaging*(mapper)](#averagingmapper)
  - [minBy(comparator)/maxBy(comparator)](#minbycomparatormaxbycomparator)
- [Többszintű redukció](#többszintű-redukció)
  - [Mi az a többszintű redukció?](#mi-az-a-többszintű-redukció)
  - [Többszintű redukciót támogató Collectorok](#többszintű-redukciót-támogató-collectorok)
  - [Zárszó: A kész telefonkönyv](#zárszó-a-kész-telefonkönyv)

Ez a dokumentum egy gyorstalpaló, mely a leggyakoribb Collector-implementációk megértését szeretné elősegíteni.

## Bevezetés

A [Lambdák és metódusreferenciák](./01-lambdak-es-metodus-referenciak.md) útmutatóhoz hasonlóan, ezúttal is a kályhától indulunk, és csak lassan, apránként haladunk az egyre bonyolultabb megoldások felé. Elsőként nem is a Collectorokkal foglalkozunk, hanem egyáltalán azzal, hogy miért van szükség rájuk.

### Sztori: Telefonkönyv

Induljunk ki ismét egy elképzelt, de szokás szerint valamennyire valós alapokon álló esetből: Mivel elegünk van a közösségi médiából, az algoritmusokból és az AI-generálta tartalomból, ezért úgy döntünk, hogy megválunk a telefonunktól és vonalas telefont fogunk használni ([inspiráció](https://www.youtube.com/watch?v=nnsyGSTKlw0)).

Természetesen továbbra is szeretnénk hívásokat indítani és fogadni, tehát valahogy fel szeretnénk jegyezni az ismerőseink telefonszámát. Szerencsére erre van egy régimódi, de remek megoldás: a telefonkönyv. A telefonkönyvek legegyszerűbb fajtája csupán név-telefonszám párokat tartalmaz, semmi mást. A következőkben mi is valami ilyesmit szeretnénk majd csinálni, kiindulva az okostelefonunk kontaktjaiból.

Azaz, tegyük fel, hogy az okostelefonunk az alábbi módon tárol egy ismerőst:

```Java
record Contact(String name, String phoneNumber) {}
```

A következőkben a célunk, hogy ilyen `Contact` objektumok sokaságából egy szuperjó telefonkönyvet készítsünk, mely lehetővé teszi, hogy túléljük a vonalas telefonos időszakot.

```Java
List<Contact> contacts = List.of(
    new Contact("Feri", "123-456-7890"),
    new Contact("Ica", "987-654-3210"),
    new Contact("Zoli", "555-555-5555"),
    new Contact("Festő Zoli", "555-555-5555"),
    new Contact("Kati", "444-444-4444"),
    new Contact("Laci", "333-333-3333")
);
```

Vágjunk is bele!

### Redukció (Reduction)

Mielőtt elkészítenénk a telefonkönyvünket, előbb fel kell mérnünk, hogy hány telefonszámot kell feljegyeznünk. Ha csak keveset, akkor megteszi néhány cetli is, de ha igazán sok ismerősünk van, akkor szükség lehet egy komolyabb füzetre.

Egy lista elemeit nagyon egyszerűen meg tudjuk számolni:

```Java
int count = 0;

for (Contact contact : contacts) {
    count += 1;
}

System.out.println(count);
```

A fenti kód az elemeink sokaságából (`contacts`) egyetlen értéket képzett, az elemek számát (`count`). Mindennek két kulcsösszetevője volt:
- Volt egy kezdeti értékünk, a nulla (`int count = 0;`).
- A végeredményt ugyanazon művelet (`count += 1;`) ismételt alkalmazásával állítottuk elő.

Ezt a *mintát* redukciónak (*reduction*) nevezzük: elemek egy sorozatából egyetlen értéket állítunk elő, valamilyen egyesítő művelet ismételt alkalmazásával. Mivel ez egy gyakori minta, ezért a Streamek beépített támogatást is adnak hozzá a `reduce` terminális művelet formájában:
* [JavaDoc: reduce](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html#reduce(U,java.util.function.BiFunction,java.util.function.BinaryOperator))

A `reduce` terminálissal a következő formában valósíthatjuk meg a fentieket:
```Java
int count = contacts
    .stream()
    .reduce(
        // identity: A számlálás kezdeti értéke
        0,
        // accumulator: Minden ismerősnél eggyel növeljük az ismerőseink számát
        (countSoFar, contact) -> countSoFar + 1,
        // combiner: ??? 😲
        (countA, countB) -> countA + countB
    );

System.out.println(count);
```

A `reduce` első két paramétere érthető, hiszen egy az egyben megfelelnek a ciklusunkban szereplő kódnak. Sőt, át is tudnánk írni őket "ciklusos" formába:

```Java
int identity = 0;
// Paraméterek: Integer, Contact
// Visszatérési típus: Integer
BiFunction<Integer, Contact, Integer> accumulator = (countSoFar, contact) -> countSoFar + 1;

int count = identity;
for (Contact contact : contacts) {
    count = accumulator.apply(count, contact);
}

System.out.println(count);
```

De hová tűnt a fenti ciklusból `(countA, countB) -> countA + countB` rész, azaz a `combiner`?

A válasz nagyon egyszerű: Míg a ciklus-alapú kódunk szekvenciálisan végighalad az összes elemen, addig a `reduce(identity, accumulator, combiner`) támogatja a *párhuzamos* végrehajtást is. Ilyenkor a `reduce` felbontja a Stream elemeit kisebb részekre, azokat külön-külön redukálja, végül az egyes részekre kapott értékeket kombinálja - természetesen a `combiner` segítségével.

Mindezt valahogy így képzelhetjük el:

```Java
int identity = 0;
BiFunction<Integer, Contact, Integer> accumulator = (countSoFar, contact) -> countSoFar + 1;
// Bináris, azaz a bemenet két Integer
// A kimenet is Integer.
BinaryOperator<Integer> combiner = (countA, countB) -> countA + countB;

List<Contact> contactsA = contacts.subList(0, 3);
List<Contact> contactsB = contacts.subList(3, 6);

// Képzeljük el, hogy ez a két ciklus párhuzamosan fut!
int countA = identity;
for (Contact contact : contactsA) {
    countA = accumulator.apply(countA, contact);
}

int countB = identity;
for (Contact contact : contactsB) {
    countB = accumulator.apply(countB, contact);
}

// A két rész eredményét a combiner segítségével egyesítjük egyetlen
// értékbe.
int count = combiner.apply(countA, countB);

System.out.println(count);
```

A redukció egy páratlanul sokoldalú művelet, hiszen a kimeneti érték bármi lehet, például egy kollekció is. Azaz, ha nem csak az ismerőseink számára vagyunk kíváncsiak, hanem ténylegesen arra is, hogy kik az ismerőseink, akkor valami ilyesmit is írhatunk:

```Java
List<String> names = contacts
    .stream()
    .map(Contact::name)
    .reduce(
        // A kezdeti értékünk az üres lista.
        new ArrayList<>(),
        // Minden elemet berakunk a listába.
        (namesSoFar, name) -> {
            namesSoFar.add(name);
            return namesSoFar;
        },
        // Végül, ha több részben redukáltunk, akkor egyszerűen összefűzzük a listákat.
        (names1, names2) -> {
            names1.addAll(names2);
            return names1;
        }
    );

System.out.println(names);
```
- Kimenet:
  ```
  [Feri, Ica, Zoli, Festő Zoli, Kati, Laci]
  ```

Szuperjó! Ezúttal egy listát kapunk, mégpedig az összes névvel. És ha már beszéltünk arról, hogy a `reduce` terminális azért is ilyen nyerő, mert támogatja a párhuzamos végrehajtást is, akkor nézzük meg, hogy mindez hogyan futna párhuzamosan:

```Java
List<String> names = contacts
    .parallelStream() // Ez a kulcs, így lesz párhuzamos a végrehajtás.
    .map(Contact::name)
    .reduce(
        new ArrayList<>(),
        (namesSoFar, name) -> {
            namesSoFar.add(name);
            return namesSoFar;
        },
        (names1, names2) -> {
            names1.addAll(names2);
            return names1;
        }
    );

System.out.println(names);
```
- Kimenet:
  ```
  [Festő Zoli, Feri, Kati, Laci, Festő Zoli, Feri, Kati, Laci, Festő Zoli, Feri, Kati, Laci, Festő Zoli, Feri, Kati, Laci, Ica, Zoli, Festő Zoli, Feri, Kati, Laci, Festő Zoli, Feri, Kati, Laci, Festő Zoli, Feri, Kati, Laci, Festő Zoli, Feri, Kati, Laci, Ica, Zoli, Festő Zoli, Feri, Kati, Laci, Festő Zoli, Feri, Kati, Laci, Festő Zoli, Feri, Kati, Laci, Festő Zoli, Feri, Kati, Laci, Ica, Zoli, Festő Zoli, Feri, Kati, Laci, Festő Zoli, Feri, Kati, Laci, Festő Zoli, Feri, Kati, Laci, Festő Zoli, Feri, Kati, Laci, Ica, Zoli, Festő Zoli, Feri, Kati, Laci, Festő Zoli, Feri, Kati, Laci, Festő Zoli, Feri, Kati, Laci, Festő Zoli, Feri, Kati, Laci, Ica, Zoli, Festő Zoli, Feri, Kati, Laci, Festő Zoli, Feri, Kati, Laci, Festő Zoli, Feri, Kati, Laci, Festő Zoli, Feri, Kati, Laci, Ica, Zoli, Festő Zoli, Feri, Kati, Laci, Festő Zoli, Feri, Kati, Laci, Festő Zoli, Feri, Kati, Laci, Festő Zoli, Feri, Kati, Laci, Ica, Zoli, Festő Zoli, Feri, Kati, Laci, Festő Zoli, Feri, Kati, Laci, Festő Zoli, Feri, Kati, Laci, Festő Zoli, Feri, Kati, Laci, Ica, Zoli]
  ```

Ajajj! A két futás között kizárt, hogy ennyi új (ráadásul ugyanolyan nevű) ismerősünk lett volna, így csak a kódunkkal lehet baj. De mégis mi a baj? A problémát az `accumulator`-unk okozza, melyet az alábbi módon implementáltunk:

```Java
(namesSoFar, name) -> {
    namesSoFar.add(name);
    return namesSoFar;
}
```

A fenti lambda minden hívás alkalmával veszi az addig látott nevek listáját (`namesSoFar`) és egyszerűen hozzáadja az aktuálisan feldolgozott nevet (`name`). Azaz, *módosítja* a `namesSoFar` értékét.

Ez azonban **tilos**! Az `accumulator`-nak mindig *új* értéket kell visszaadnia, nem módosíthatja a paramétereit!

Azaz, minden hívás alkalmával új listát kell visszaadni:

```Java
List<String> names = contacts
    .parallelStream()
    .map(Contact::name)
    .reduce(
        new ArrayList<>(),
        (namesSoFar, name) -> {
            var newNamesSoFar = new ArrayList<String>();
            newNamesSoFar.addAll(namesSoFar);
            newNamesSoFar.add(name);
            return newNamesSoFar;
        },
        (names1, names2) -> {
            names1.addAll(names2);
            return names1;
        }
    );

System.out.println(names);
```
- Kimenet:
  ```
  [Feri, Ica, Zoli, Festő Zoli, Kati, Laci]
  ```

Csudijó! Immár a párhuzamos végrehajtás kimenete is helyes.

Ugyanakkor van egy, jelenleg még apró, azonban sok név esetén igen kellemetlen probléma: Az `accumulator` minden egyes hívás esetén új `ArrayList` példányt hoz létre. Ha a feldolgozott Stream nagyon sok elemből áll, akkor ez nagyon sok `ArrayList` példányt fog jelenteni. És ezek a példányok teljesen feleslegesek, hiszen csak átmeneti tárolóként szolgálnak.

Lennie kell valamilyen jobb, kevésbé pazarló megoldásnak arra, hogy listába redukáljuk az elemeinket. Lássuk!

> [!TIP]
> Összefoglalva, a redukció egy olyan minta, mely elemek egy sorozatából egyetlen értéket állít elő, valamilyen egyesítő művelet ismételt alkalmazásával. Ez az egyetlen érték lehet valamilyen numerikus érték (minimum, maximum, darabszám, stb.), de akár valamilyen kollekció is (`List`, `Set` és társaik).

### Módosítható redukció (Mutable reduction)

Ez a jobb megoldás lesz a *módosítható redukció*.

Míg a `reduce` művelet `accumulator` lambdája minden híváskor új értéket kell, hogy visszaadjon, addig a módosítható redukció pont azért lesz módosítható, mert egy módosítható (azaz, *mutable*) eredmény konténerbe redukálja az elemeket. Azaz, a listába gyűjtésünk első változata itt már helyes megvalósítás lesz!

Módosítható redukciót a Streamek `collect` metódusával végezhetünk:
* [JavaDoc: collect(supplier, accumulator, combiner)](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html#collect(java.util.function.Supplier,java.util.function.BiConsumer,java.util.function.BiConsumer))

Feladatunk ugyanaz, szeretnénk összegyűjteni az ismerőseinket:

```Java
List<String> names = contacts
    .stream()
    .map(Contact::name)
    .collect(
        // supplier -> a módosítható konténer kiinduló értékét adja
        () -> new ArrayList<String>(),
        // accumulator -> módosítja a konténert
        (namesSoFar, name) -> namesSoFar.add(name),
        // combiner -> egyesít több konténert
        (namesA, namesB) -> namesA.addAll(namesB)
    );

System.out.println(names);
```

Hasonlítsuk össze a fenti `collect` hívást a megelőző `reduce` hívásokkal!
- Míg a `reduce` esetén az `identity` egy konkrét érték, addig a `collect` esetén a `supplier` egy értéket előállító lambda. Ez adja a módosítható konténerünk kezdeti értékét.
- A `collect` `accumulator`-a nem ad vissza semmit, hanem módosítja az első paraméterét.
- A `collect` `combiner`-e sem ad vissza semmit, egyszerűen módsítja az első paramétert.

A `collect`-alapú kódunk nem hoz létre felesleges köztes `ArrayList` példányokat. Csak annyi `ArrayList` példányt fog létrehozni, amennyi feltétlenül szükséges. Szekvenciális futtatás esetén például csak egyet:

```Java
List<String> names = contacts
    .stream()
    .map(Contact::name)
    .collect(
        // supplier -> a módosítható konténer kiinduló értékét adja
        () -> {
            System.out.println("Creating a new ArrayList");
            return new ArrayList<String>(),
        },
        // accumulator -> módosítja a konténert
        (namesSoFar, name) -> namesSoFar.add(name),
        // combiner -> egyesít több konténert
        (namesA, namesB) -> namesA.addAll(namesB)
    );

System.out.println(names);
```
- Kimenet:
  ```
  Creating a new ArrayList
  [Feri, Ica, Zoli, Festő Zoli, Kati, Laci]
  ```

A `collect` segítségével sokkal egyszerűbb volt a listába gyűjtést megoldani, mint a `reduce`-t használva. Mindazonáltal ez még mindig nagyon bonyolult!

Lennie kell valamilyen egyszerűbb megoldásnak.

> [!TIP]
> Összefoglalva, a módosítható redukció egy olyan redukció (azaz, továbbra is egyetlen értéket képzünk, valamilyen művelet ismételt alkalmazásával), mely valamilyen módosítható eredmény konténerbe redukálja a feldolgozott elemeket. Ez a konténer általában valamilyen kollekció, például `List`.

### collect, Collector, Collectors

Ez az egyszerűbb megoldás lesz a `Collector`:
* [JavaDoc: Collector](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Collector.html)

Melyet a `collect` metódus egyparaméteres változatával tudunk használni:
* [JavaDoc collect(collector)](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html#collect(java.util.stream.Collector))

Egy `Collector`-megvalósítás valamilyen módosítható redukciónak felel meg, mint például a "listába gyűjtés". Lényegében az egybecsomagolása az előzőleg a `collect`-nek átadott három függvénynek: `supplier`, `accumulator`, `combiner`.

Bár készíthetünk mi magunk is `Collector`-megvalósításokat, azonban a standard könyvtár számos előredefinált implementációt kínál a `Collectors` osztály statikus gyár metódusain keresztül:
* [JavaDoc: Collectors](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Collectors.html)

Azaz:
- `collect(collector)`
  - Terminális Stream-művelet, amivel módosítható redukciót végezhetünk, egy `Collector`-példányt használva.
- `Collector`-példányok
  - Minden példány valamilyen módosítható redukciós logikát reprezentál. Például a listába gyűjtést.
- `Collectors`
  - Gyakran használt `Collector`-implementációk sokaságát tartalmazza.

## Összegyűjtés jól ismert kollekciókba

A következőkben olyan `Collector`-okat tekintünk, melyek jól ismert kollekciókba gyűjtik össze a feldolgozott elemeket.

> [!NOTE]
> A következőkben a `Collectors` statikus gyár metódusait fogjuk tekinteni. Ezek mindegyike egy `Collector`-példányt ad vissza. Annak érdekében, hogy a leírások egyszerűbbek legyenek, ahelyett, hogy mindig azt írnánk, hogy "Visszaad egy olyan `Collector`-t, mely az elemeket egy listába gyűjti.", inkább úgy teszünk mintha eleve a `Collector`-ról lenne szó: "Az elemeket egy listába gyűjti".

### toList()

* [JavaDoc](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Collectors.html#toList())

A feldolgozott elemeket egy `List`-be gyűjti.

Tekintsük az immáron unásig ismert feladatunkat: szeretnénk összegyűjteni az ismerőseinket.

```Java
List<String> names = contacts
    .stream()
    .map(Contact::name)
    .collect(Collectors.toList());

System.out.println(names);
```

### toSet()

* [JavaDoc](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Collectors.html#toSet())

A feldolgozott elemeket egy `Set`-be gyűjti. Mivel a `Set` egy halmaznak felel meg, ezért a kapott kollekcióban nem lesznek duplikátumok.

Sajnos van olyan telefonszám, amit kétszer írtunk be a telefonunkba. Keressük meg csak az egyedi telefonszámokat:

```Java
Set<String> uniquePhoneNumbers = contacts
    .stream()
    .map(Contact::phoneNumber)
    .collect(Collectors.toSet());

System.out.println(uniquePhoneNumbers);
```

### toMap(keyMapper, valueMapper)

* [JavaDoc](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Collectors.html#toMap(java.util.function.Function,java.util.function.Function))

Visszaad egy `Map`-et, melynek kulcsait a `keyMapper`, értékeit a `valueMapper` függvény kimenetei adják. A létrehozott `Map` a Stream minden eleméhez egy kulcs-érték párt fog tartalmazni.

Ha a `keyMapper` több elemre is ugyanazt az értéket adja vissza, akkor kivételt dob. Azaz, nem képezhetünk azonos kulcsokat az elemekből.

A `toMap` segítségével most már tényleg létrehozhatjuk a telefonkönyvünket, azaz a nevekhez telefonszámokat rendelhetünk:

```Java
Map<String, String> phoneBook = contacts
    .stream()
    .collect(
        Collectors.toMap(
            // keyMapper -> nevekhez rendelünk
            Contact::name,
            // valueMapper -> telefonszámokat
            Contact::phoneNumber
        )
    );

System.out.println(phoneBook);
```

A fenti kódrészlet megfelel valami ilyesminek, Streamek nélkül:

```Java
// Egy Contact objektumból egy Stringet képzünk.
Function<Contact, String> keyMapper = Contact::name;
// Egy Contact objektumból egy Stringet képzünk.
Function<Contact, String> valueMapper = Contact::phoneNumber;

Map<String, String> phoneBook = new HashMap<>();
for (Contact contact : contacts) {
    String key = keyMapper.apply(contact);

    if (phoneBook.containsKey(key)) {
        throw new IllegalStateException();
    }

    phoneBook.put(
        key,
        valueMapper.apply(contact)
    );
}

System.out.println(phoneBook);
```

### joining()

* [JavaDoc: joining()](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Collectors.html#joining())

Csak Stringeken használható `Collector`.

Három paraméterezése van:
- `joining()`
  - Összefűzi az elemeket.
- `joining(delimiter)`
  - Összefűzi az elemeket, minden elem közé beszúrva a `delimiter` Stringet.
- `joining(delimiter, prefix, suffix)`
  - Először a prefix áll, utána az összefűzött elemek (közéjük beszúrva a `delimiter`), végül a `suffix`.
  - Ha valamire nincs szükségünk (például nem szeretnénk az elemek közé semmit se beszúrni), akkor használjuk az üres Stringet (`""`) és ne `null`-t!

Azaz, ha szeretnénk kinyomtatni a telefonkönyvünket, akkor csinálhatunk valami ilyesmit:

```Java
String printablePhoneBook = contacts
    .stream()
    .map(contact -> contact.name() + ": " + contact.phoneNumber())
    .collect(
        Collectors.joining(
            // delimiter
            "\n",
            // prefix
            "My Phone Book\n\n",
            // suffix, erre nincs szükségünk, ezért üres String
            ""
        )
    );

System.out.println(printablePhoneBook);
```

Streamek használata nélkül, a fenti kód valami hasonlónak felel meg:

```Java
String delimiter = "\n";
String prefix = "My Phone Book\n\n";
String suffix = "";

var joiner = new StringJoiner(
    delimiter,
    prefix,
    suffix
);

for (Contact contact : contacts) {
    String entry = contact.name() + ": " + contact.phoneNumber();
    joiner.add(entry);
}

String printablePhoneBook = joiner.toString();

System.out.println(printablePhoneBook);
```

## Csoportosítás

Itt az ideje, hogy bonyolultabb `Collector`-megvalósításokat is tekintsünk! Mindenekelőtt, kezdjük a csoportosítással!

### Változik a telefonkönyv

Mielőtt ebbe belevágnánk, egy kicsit frissítjük a kontaktjainkat. Ezidáig csupán az okostelefonunk memóriájában tárolt kontaktokkal foglalkoztunk:

```Java
record Contact(String name, String phoneNumber) {}
```

Azonban rájöttünk, hogy bizony máshol is vannak még telefonszámok: a felhőben, WhatsAppban, Messengerben és így tovább. Ráadásul ezek között még duplikációk is vannak.

Azaz, a kontaktjaink most már valahogy így néznek ki:

```Java
enum ContactSource {
    PHONE,
    CLOUD,
    WHATSAPP,
    MESSENGER,
}

record Contact(
    String name,
    String phoneNumber,
    ContactSource source,
    LocalDateTime addedAt,
    int minutesSpoken
) {}
```

És a telefonkönyvünket is immár a következő kontaktokból kell generálnunk:

```Java
List<Contact> contacts = List.of(
    new Contact(
        "Feri",
        "+36123456789",
        ContactSource.PHONE,
        LocalDateTime.of(2023, 10, 1, 12, 0),
        20
    ),
    new Contact(
        "Juli",
        "+36301111111",
        ContactSource.CLOUD,
        LocalDateTime.of(2023, 10, 2, 12, 0),
        30
    ),
    new Contact(
        "Juli",
        "+36301111111",
        ContactSource.PHONE,
        LocalDateTime.of(2022, 10, 2, 12, 0),
        30
    ),
    new Contact(
        "Peti",
        "+36302222222",
        ContactSource.WHATSAPP,
        LocalDateTime.of(2023, 10, 3, 12, 0),
        50
    ),
    new Contact(
        "Peti",
        "+36302222222",
        ContactSource.MESSENGER,
        LocalDateTime.of(2024, 10, 4, 12, 0),
        50
    ),
    new Contact(
        "Peti",
        "+36302222222",
        ContactSource.CLOUD,
        LocalDateTime.of(2023, 10, 5, 12, 0),
        50
    )
);
```

### groupingBy(classifier)

* [JavaDoc](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Collectors.html#groupingBy(java.util.function.Function))

Egy `Map`-et képez a feldolgozott elemekből, a következő módon:
- a `Map` kulcsait úgy kapjuk, hogy az elemekre ráfuttatjuk a `classifier` függvényt,
- minden kulcshoz egy lista tartozik, azon elemek listája, melyekre azonos értéket adott a `classifier`.

Azaz, szemben a [toMap(keyMapper, valueMapper)](#tomapkeymapper-valuemapper) áltak visszaadott `Collector`-ral, itt lehetséges, hogy több elem is ugyanazzal kulccsal rendelkezzen. Tehát:
- ha az elemeinket csoportokba szeretnénk rendezni, akkor használjuk a `groupingBy(classifier)` metódust (ahol a csoportokban maguk az elemek találhatók),
- ha pedig minden elemhez szeretnénk egy kulcs-érték párt képezni, egyénileg képzett kulcssal és értékkel, akkor használjuk a `toMap(keyMapper, valueMapper)` metódust.

Folytatva a példánkat, szeretnénk egyesíteni a különböző forrásból származó telefonszámokat. Azaz, szeretnénk egyben látni például Juli összes számát, függetlenül attól, hogy a felhőben található vagy a telefonunk memóriájában.

Ezt megtehetjük a `groupingBy` segítségével, név szerint csoportosítva a `Contact` példányokat:

```Java
Map<String, List<Contact>> phoneBook = contacts
    .stream()
    .collect(
        Collectors.groupingBy(
            contact -> contact.name()
        )
    );

System.out.println(phoneBook);
```
- Kimenet:
  ```
  {Feri=[Contact[name=Feri, phoneNumber=+36123456789, source=PHONE, addedAt=2023-10-01T12:00, minutesSpoken=20]], Juli=[Contact[name=Juli, phoneNumber=+36301111111, source=CLOUD, addedAt=2023-10-02T12:00, minutesSpoken=30], Contact[name=Juli, phoneNumber=+36301111111, source=PHONE, addedAt=2022-10-02T12:00, minutesSpoken=30]], Peti=[Contact[name=Peti, phoneNumber=+36302222222, source=WHATSAPP, addedAt=2023-10-03T12:00, minutesSpoken=50], Contact[name=Peti, phoneNumber=+36302222222, source=MESSENGER, addedAt=2024-10-04T12:00, minutesSpoken=50], Contact[name=Peti, phoneNumber=+36302222222, source=CLOUD, addedAt=2023-10-05T12:00, minutesSpoken=50]]}
  ```

Amint a kimeneten láthatjuk, a nevekhez (melyek `Stringek`) rendeltük az adott névvel bíró `Contact` objektumok listáját.

Streamek nélkül, a fenti kód megfelel valami hasonlónak:

```Java
// Contact objektumból képzünk Stringet.
Function<Contact, String> classifier = contact -> contact.name();
Map<String, List<Contact>> phoneBook = new HashMap<>();

for (Contact contact : contacts) {
    String klazz = classifier.apply(contact);

    if (!phoneBook.containsKey(klazz)) {
        phoneBook.put(klazz, new ArrayList<>());
    }

    List<Contact> contactsInKlazz = phoneBook.get(klazz);
    contactsInKlazz.add(contact);
}

System.out.println(phoneBook);
```

Ez szuperjó! Azonban hogyan tudnánk megoldani, hogy a nevekhez, igazi telefonkönyvhöz méltó módon, ne `Contact` objektumokat, hanem tényleges telefonszámokat rendeljünk? A folytatásban kiderül!

### partitioningBy(predicate)

* [JavaDoc](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Collectors.html#partitioningBy(java.util.function.Predicate))

De előtte még egy gyors kitérő! 

A `partitioningBy(predicate)` egy olyan `Map`-et ad vissza, melynek két kulcsa van: `true` és `false`. Ennek megfelelően két listába választja szét az elemeket:
- azok az elemek, melyek kielégítik a predikátumot,
- és azok az elemek, melyek **nem** elégítik ki a predikátumot.

Lényegében megfelel a `groupingBy` olyan változatának, ahol a `classifier` `true/false` értéket ad vissza. Lényeges eltérés ugyanakkor, hogy a visszaadott `Map` **mindig** fog mind `true`, mind `false` kulcsot tartalmazni, akkor is, ha valamelyikhez egyáltalán nem is tartoznak elemek.

Tegyük fel, hogy 2023-ben telefont váltottunk. Ha szeretnénk megnézni, hogy mely számok szerepeltek be a régi (2023. előtti) telefonunkban és melyek az újban, akkor azt megtehetjük valahogy így:

```Java
Map<Boolean, List<Contact>> contactsAcrossPhones = contacts
    .stream()
    .filter(contact -> ContactSource.PHONE.equals(contact.source()))
    .collect(
        Collectors.partitioningBy(
            contact -> contact.addedAt().getYear() < 2023
        )
    );

System.out.println(contactsAcrossPhones);
```
- Kimenet:
  ```
  {false=[Contact[name=Feri, phoneNumber=+36123456789, source=PHONE, addedAt=2023-10-01T12:00, minutesSpoken=20]], true=[Contact[name=Juli, phoneNumber=+36301111111, source=PHONE, addedAt=2022-10-02T12:00, minutesSpoken=30]]}
  ```

Streamek nélkül, ezt valahogy így implementálhatnánk:

```Java
// Contactből képzünk igazságértéket
Predicate<Contact> predicate = contact -> contact.addedAt().getYear() < 2023;
Map<Boolean, List<Contact>> contactsAcrossPhones = new HashMap<>();

// Mindig van mindkét kulcs.
contactsAcrossPhones.put(true, new ArrayList<>());
contactsAcrossPhones.put(false, new ArrayList<>());

for (Contact contact : contacts) {
    boolean isBefore2023 = predicate.test(contact);

    contactsAcrossPhones.get(isBefore2023).add(contact);
}

System.out.println(contactsAcrossPhones);
```

Ahogy az előző esetben, most is szuper lenne, ha `Contact` objektumok helyett inkább nevek lennének az egyes listákban. Hogyan lehet ezt megcsinálni? Már csak egy picit kell várni!

## Közjáték: Teljesen szükségtelennek tűnő Collectorok

A következőkben olyan `Collector`-okat fogunk tekinteni, melyek a jelenlegi tudásunkkal teljesen szükségtelennek tűnhetnek, hiszen funkcionalitásuk megvalósítható sokkal egyszerűbb eszközökkel is. Ugyanakkor később ezek is rendkívül hasznosnak fognak bizonyulni. Lássuk is őket!

#### counting()

- [JavaDoc](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Collectors.html#counting())

Megszámlálja a kapott elemeket.

Ha meg szeretnénk nézni, hány kontaktunk van, akkor:
```Java
long count = contacts
    .stream()
    .collect(
        Collectors.counting()
    );

System.out.println(count);
```

Ami lényegében ekvivalens a következővel:
```Java
long count = contacts
    .stream()
    .count();

System.out.println(count);
```

#### summing*(mapper)

- [JavaDoc: summingInt](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Collectors.html#summingInt(java.util.function.ToIntFunction))

Minden elemre meghívja a `mapper` függvényt (aminek kimenete `int`, `long` vagy `double` lehet), majd összegzi a kapott értékeket.

Például, ha szeretnénk megvizsgálni, hogy összesen hány percet beszéltünk:
```Java
int totalMinutesSpoken = contacts
    .stream()
    .collect(
        Collectors.summingInt(
            contact -> contact.minutesSpoken()
        )
    );

System.out.println(totalMinutesSpoken);
```

Ami ekvivalens a következővel:
```Java
int totalMinutesSpoken = contacts
    .stream()
    .mapToInt(contact -> contact.minutesSpoken())
    .sum();

System.out.println(totalMinutesSpoken);
```

#### averaging*(mapper)

Minden elemre meghívja a `mapper` függvényt (aminek kimenete `int`, `long` vagy `double` lehet), majd veszi a kapott értékek átlagát.


Például, ha szeretnénk megvizsgálni, hogy átlagosan hány percet beszéltünk:
```Java
double avgMinutesSpoken = contacts
    .stream()
    .collect(
        Collectors.averagingInt(
            contact -> contact.minutesSpoken()
        )
    );

System.out.println(avgMinutesSpoken);
```

Ami (lényegében) ekvivalens a következővel:
```Java
OptionalDouble avgMinutesSpoken = contacts
    .stream()
    .mapToInt(contact -> contact.minutesSpoken())
    .average();

System.out.println(avgMinutesSpoken);
```

#### minBy(comparator)/maxBy(comparator)

- [JavaDoc: minBy](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Collectors.html#minBy(java.util.Comparator))
- [JavaDoc: maxBy](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Collectors.html#maxBy(java.util.Comparator))

Visszaadja a feldolgozott elemek közül a legkisebbet (`minBy`)/legnagyobbat (`maxBy`). Az elemek rendezése a megadott `comparator` segítségével történik.

Ha szeretnénk megkeresni a legújabb kontaktunkat, akkor azt a következő módon tehetjük meg:
```Java
// Optionalt ad vissza, hiszen üres is lehet a stream
Optional<Contact> newestContact = contacts
    .stream()
    .collect(
        Collectors.maxBy(
            Comparator.comparing(
                Contact::addedAt
            )
        )
    );

System.out.println(newestContact);
```

Ami megfelel az alábbi kódrészletnek:
```Java
Optional<Contact> newestContact = contacts
    .stream()
    .max(Comparator.comparing(
        Contact::addedAt
    ));

System.out.println(newestContact);
```

## Többszintű redukció

Az előző fejezetekben feltettünk két kérdést:

> Ez szuperjó! Azonban hogyan tudnánk megoldani, hogy a nevekhez, igazi telefonkönyvhöz méltó módon, ne `Contact` objektumokat, hanem tényleges telefonszámokat rendeljünk? A folytatásban kiderül!
 
> Ahogy az előző esetben, most is szuper lenne, ha `Contact` objektumok helyett inkább nevek lennének az egyes listákban. Hogyan lehet ezt megcsinálni?

Továbbá felsoroltunk néhány, teljesen feleslegesnek tűnő `Collector`-t.

Itt az ideje, hogy megválaszoljuk ezeket a kérdéseket! A válasz pedig nem más, mint a *többszintű redukció* (multilevel reduction)!

### Mi az a többszintű redukció?

Szokás szerint tekintsünk egy példát! Mégpedig pontosan azt a példát, amit az imént is említettünk:

> Hogyan tudnánk megoldani, hogy a nevekhez, igazi telefonkönyvhöz méltó módon, ne `Contact` objektumokat, hanem tényleges telefonszámokat rendeljünk?

Emlékeztetőül, a kód, mely után feltettük ezt a kérdést, az alábbi:
```Java
Map<String, List<Contact>> phoneBook = contacts
    .stream()
    .collect(
        Collectors.groupingBy(
            contact -> contact.name()
        )
    );

System.out.println(phoneBook);
```
- Kimenet:
  ```
  {Feri=[Contact[name=Feri, phoneNumber=+36123456789, source=PHONE, addedAt=2023-10-01T12:00, minutesSpoken=20]], Juli=[Contact[name=Juli, phoneNumber=+36301111111, source=CLOUD, addedAt=2023-10-02T12:00, minutesSpoken=30], Contact[name=Juli, phoneNumber=+36301111111, source=PHONE, addedAt=2022-10-02T12:00, minutesSpoken=30]], Peti=[Contact[name=Peti, phoneNumber=+36302222222, source=WHATSAPP, addedAt=2023-10-03T12:00, minutesSpoken=50], Contact[name=Peti, phoneNumber=+36302222222, source=MESSENGER, addedAt=2024-10-04T12:00, minutesSpoken=50], Contact[name=Peti, phoneNumber=+36302222222, source=CLOUD, addedAt=2023-10-05T12:00, minutesSpoken=50]]}
  ```

A kulcsok tökéletesek, hiszen azok az ismerőseink nevei. A kulcsokhoz rendelt kontaktok is rendben vannak. Valahogy azonban ezekből a kontaktokból ki kellene nyerni a telefonszámokat. Azaz, a `groupingBy` által összeállított csoportok elemein még szeretnénk valamilyen műveletet végezni.

Ciklus-alapú kódra lefordítva, valami ilyesmit szeretnénk:

```Java
// Contact objektumból képzünk Stringet.
Function<Contact, String> classifier = contact -> contact.name();
// Contact objektumból képzünk Stringet.
Function<Contact, String> mapper = contact -> contact.phoneNumber();
Map<String, List<String>> phoneBook = new HashMap<>();

for (Contact contact : contacts) {
    String klazz = classifier.apply(contact);

    if (!phoneBook.containsKey(klazz)) {
        phoneBook.put(klazz, new ArrayList<>());
    }

    List<String> numbersInKlazz = phoneBook.get(klazz);

    String mappedContact = mapper.apply(contact);
    numbersInKlazz.add(mappedContact);
}

System.out.println(phoneBook);
```

Szerencsére a `groupingBy(classifier, downstream)` pontosan ebben nyújt segítséget:
* [JavaDoc: groupingBy(classifier, downstream)](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Collectors.html#groupingBy(java.util.function.Function,java.util.stream.Collector))

Ennek második paramétere a `downstream` nevet viseli és egy `Collector`-t vár. Ez a downstream `Collector` fogja redukálni az egyes csoportokba kerülő elemeket.

Mindenekelőtt próbáljuk meg az egyszerű `groupingBy(classifier)` funkcionalitását implementálni az új `groupingBy(classifier, downstream)` segítségével. Emlékeztetőül, a `groupingBy(classifier)` listákba szervezi az azonos csoportba tartozó elemeket (melyekre a `classifier` azonos értéket ad). Listát képezni a `toList()` `Collector` segítségével tudunk. Azaz, ha listákba akarjuk redukálni az adott csoportokba tartozó elemeket, akkor a `downstream` `Collector` nem lesz más, mint a `Collectors.toList()`:

```Java
Map<String, List<Contact>> phoneBook = contacts
    .stream()
    .collect(
        Collectors.groupingBy(
            // classifier
            contact -> contact.name(),
            // downstream
            Collectors.toList()
        )
    );

System.out.println(phoneBook);
```

És pontosan ez a többszintű redukció: olyan `Collector`-csővezeték, mely egymásba ágyazva egy vagy több *downstream* `Collector`-t tartalmaz.

A működési elv pedig pofonegyszerű: a magasabb szinten levő `Collector` (azaz, *upstream*) egyszerűen továbbad megfelelő elemeket az alacsonyabb szinten levő (azaz, *downstream*) `Collector`-nak.

Visszatérve a példánkhoz:

> Hogyan tudnánk megoldani, hogy a nevekhez, igazi telefonkönyvhöz méltó módon, ne `Contact` objektumokat, hanem tényleges telefonszámokat rendeljünk?

Egy olyan `downstream` `Collector`-ra van szükségünk, mely transzformálni (mappelni) képes a kapott elemeket, minden `Contact` objektumból telefonszámot előállítva. Szerencsére van is ilyen, mégpedig a `mapping(mapper, downstream)`:
* [JavaDoc: mapping(mapper, downstream)](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Collectors.html#mapping(java.util.function.Function,java.util.stream.Collector))

Ez egy olyan `Collector`, mely minden elemre lefuttatja a `mapper` függvényt, majd az így kapott értékeket továbbadja a `downstream` `Collector`-nak. Ha például egy listába szeretnénk gyűjteni a kontaktjaink telefonszámait, akkor azt megtehetjük a következő módon:

```Java
List<String> numbers = contacts
    .stream()
    .map(Contact::phoneNumber)
    .collect(Collectors.toList());

System.out.println(numbers);
```

Vagy pedig, `mapping` használatával:
```Java
List<String> numbers = contacts
    .stream()
    .collect(
        Collectors.mapping(
            // mapper -> a számokat szeretnénk
            Contact::phoneNumber,
            // downstream -> egyszerű listában
            Collectors.toList()
        )
    );

System.out.println(numbers);
```

Persze, a fenti esetben ez teljesen felesleges, hiszen a `.map().collect()` egy sokkal egyszerűbb megoldás.

A `mapping` igazi haszna akkor jön elő, amikor `downstream` `Collector`-ként használjuk!

```Java
Map<String, List<String>> phoneBook = contacts
    .stream()
    .collect(
        Collectors.groupingBy(
            // classifier
            contact -> contact.name(),
            // downstream
            Collectors.mapping(
                // mapper
                contact -> contact.phoneNumber(),
                // downstream
                Collectors.toList()
            )
        )
    );

System.out.println(phoneBook);
```

Vegyük észre, hogy három `Collector`-ból képeztünk többszintű csővezetéket:
- Először a `groupingBy` csoportosítja a `Contact` objektumokat név szerint.
- Ezután a csoportokban található `Contact` objektumokból a `mapping` telefonszámokat képez.
- Végül az egyes csoportokban szereplő telefonszámokat a `toList` egy listába gyűjti.

És itt nyernek majd értelmet a korábbi, feleslegesnek bélyegzett `Collector`-ok is: míg legfelső szinten használva vannak helyettük más, jobb eszközök is, addig egy többszintű redukció belsejében rendkívül hasznosak lehetnek.

## Többszintű redukciót támogató Collectorok

Néhány gyakran használt, többszintű redukciót támogató `Collector`:
- [groupingBy(classifier, downstream)](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Collectors.html#groupingBy(java.util.function.Function))
  - A `classifier` által felbontott csoportokra futtatja a `downstream` `Collector`-t.
- [partitioningBy(predicate, downstream)](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Collectors.html#partitioningBy(java.util.function.Predicate,java.util.stream.Collector))
  - A `predicate` által felbontott két csoportra futtatja a `downstream` `Collector`-t.
- [mapping(mapper, downstream)](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Collectors.html#mapping(java.util.function.Function,java.util.stream.Collector))
  - A kapott elemekre lefuttatja a `mapper` transzformációt, majd az így nyert értékeket átadja a `downstream` `Collector`-nak.
- [filtering(predicate, downstream)](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Collectors.html#filtering(java.util.function.Predicate,java.util.stream.Collector))
  - A `predicate` predikátumot teljesítő elemeket átadja a `downstream` `Collector`-nak.
- [collectingAndThen(downstream, finisher)](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Collectors.html#collectingAndThen(java.util.stream.Collector,java.util.function.Function))
  - Minden elemet átad a `downstream` `Collector`-nak, majd a `downstream` kimenetére lefuttatja a `finisher` transzformációt.

## Zárszó: A kész telefonkönyv

Zárszóként álljon itt egy olyan Stream-csővezeték, mely egy igazán bonyolult többszintű redukciót valósít meg!

A feladat nem is olyan bonyolult, a telefonkönyvünk befejezése: minden ismerősünk nevéhez rendeljük hozzá az ismerős legújabb telefonszámát. Feltesszük, hogy a legújabb a helyes.

```Java
Map<String, String> newestNumbers = contacts
    .stream()
    .collect(
        Collectors.groupingBy(
            // A névhez szeretnénk hozzárendelni
            Contact::name,
            Collectors.collectingAndThen(
                // a legújabb
                Collectors.maxBy(
                    Comparator.comparing(Contact::addedAt)
                )
                // telefonszámot
                optionalContact -> optionalContact.get().phoneNumber()
            )
        )
    );

System.out.println(newestNumbers);
```
- Kimenet:
  ```
  {Feri=+36123456789, Juli=+36301111111, Peti=+36302222222}
  ```

Vegyük észre, hogy a `collectingAndThen`-re azért van szükség, mert

- egyfelől a `maxBy` `Optional` értéket ad vissza, mi viszont tudjuk, hogy mindenkihez tartozni fog telefonszám,
- másfelől nem a legújabb telefonszámot tartalmazó `Contact` objektumot, hanem csak annak a telefonszámát szeretnénk a névhez hozzárendelni.

Ennek folytán a legújabb `Contact` megállapítása után még ki kell nyernünk a telefonszámot (ez lesz az `AndThen`).
