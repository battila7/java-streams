# Lambdák és metódusreferenciák

* [Angol nyelvű hivatalos útmutató: Writing Your First Lambda Expression](https://dev.java/learn/lambdas/first-lambdas/)

## Ön itt áll

Ez az útmutató feltételezi, hogy sem a lambdákról, sem a metódusreferenciákról nem tudunk még semmit. Ugyanakkor, tisztában vagyunk a Java nyelv olyan, alapvetőenk mondható eszközeivel, mint az osztályok és az interfészek.

A következőkben lépésenként fogunk eljutni a lambdákig, olyan köztes lépéseken át, mint a beágyazott (*nested*) és a névtelen (*anonymous*) osztályok.

## Miért van szükség lambdákra?

Induljunk ki egy elképzelt, de nagyon is valóságos alapokon álló esetből: felhasználói felületet szeretnénk írni Javaban. A felületre el szeretnénk helyezni gombokat, melyek kattintásra ilyen-olyan dolgokat csinálnak. Természetesen mindegyik gomb valamilyen egyedi logikát kell, hogy végrehajtson, amikor rákattintunk. Hogyan tudnánk ezt leprogramozni?

Kézenfekvő megoldás lehet, hogy készítünk egy absztrakt `Button` osztályt, melyben a kattintási logikát örökléssel lehet megadni. Azaz:

```Java
abstract class Button {
    private final String title;
    
    public Button(String title) {
        this.title = title;
    }
    
    public abstract void onClick();
}
```

A kattintási logikát (azaz: mi történjék, ha rákattintanak a gombra) az `onClick` metódus implementálásával tudjuk definiálni, mégpedig a `Button` osztály leszármazottjaiban. Képzeljünk el például egy olyan gombot, mely felad egy rendelést:

```Java
class PlaceOrderButton extends Button {
    public PlaceOrderButton() {
        super("Place Order");
    }
    
    @Override
    public void onClick() {
        System.out.println("Order placed!");        
    }
}
```

Szuperjó! Van egy rendelés-feladó gombunk, mely kattintásra felad egy rendelést.

Akad azonban egy probléma: a legtöbb felhasználói felület nem egy, hanem sok-sok gombot tartalmaz. Ha szeretnénk egy lemondó gombot készíteni, akkor ahhoz megint írnunk kell egy `Button`-leszármazottat:

```Java
class CancelOrderButton extends Button {
    public CancelOrderButton() {
        super("Cancel Order");
    }

    @Override
    public void onClick() {
        System.out.println("Order canceled!");
    }
}
```

Újabb gomb, újabb osztály. Vegyük észre továbbá, hogy ezekben az osztályokban többségben van az úgynevezett *boilerplate* kód. Ez azt jelenti, hogy a saját logikánk mindössze egy sort foglal el, minden további kódsor csak arra szolgál, hogy legyen mibe csomagolni ezt a logikát.

Nyilvánvalóan ez a megközelítés nagyon fáradságos és egyáltalán nem elegáns. Pontosan ezekre a gondokra kínálnak majd megoldást a lambdák:
* kevesebb *boilerplate*,
* olvashatóbb, elegánsabb kód,
* könnyebben karbantartható kód,
* bónusz: a funkcionális programozási paradigma elősegítése.

De mégis hogyan? A következő részekben kiderül; olvassunk tovább!

## Szervezzük ki a viselkedést

Alakítsuk át egy picit a `Button` osztályunkat! Immár ne legyen absztrakt, hanem ezt a kattintási viselkedést lehessen valahogy kívülről beleinjektálni. Valahogy így:

```Java
class ClickableButton {
    private final String title;
    private final OnClickHandler onClickHandler;

    public ClickableButton(String title, OnClickHandler onClickHandler) {
        this.title = title;
        this.onClickHandler = onClickHandler;
    }
    
    public void onClick() {
        onClickHandler.click();
    }
}
```

Honnan jön az `OnClickHandler`? Nos, egyelőre sehonnan: a szoftverfejlesztés klasszikus trükkjét alkalmaztuk, miszerint meghívtunk egy olyan absztrakciót, ami még nem létezik, csak kitaláltuk.

Az `OnClickHandler` lesz annak a módja, hogy viselkedést (= mit tegyünk, ha ránk kattintanak) injektáljunk a `Button` példányokba. Ennek az `OnClickHandler` dolognak csak egyetlen metódust kell definiálnia: a kattintáskor meghívott `click()`-et.

Azaz:

```Java
interface OnClickHandler {
    void click();
}
```

Nézzük meg, hogyan lehetne most elkészíteni egy rendelés-feladó gombot. Ehhez immár nincs szükség a `ClickableButton` kiterjesztésére. Ugyanakkor, implementálni kell az `OnClickHandler` interfészt:

```Java
class PlaceOrderOnClickHandler implements OnClickHandler {
    @Override
    public void click() {
        System.out.println("Order placed!");
    }
}

final var placeOrderButton = new ClickableButton(
    "Place Order",
    new PlaceOrderOnClickHandler()
); 
```

Vegyük észre, hogy a felesleges, logikát körülölelő kód jelentősen lecsökkent. Egyúttal, ez egy igazi cseberből vederbe helyzetnek is tűnik: ahelyett, hogy a `Button` osztályt kellene kiterjeszteni, most már az `OnClickHandler`-t kell implementálni. Lényegesen nem lett elegánsabb a kód.

## Névtelen osztályok

Itt siet segítségünkre a Java nyelv egy régi, jól ismert eszköze.

Ha jobban belegondolunk, akkor rájöhetünk, hogy a "Mi történjen, ha rákattintanak a rendelés-feladó gombra?" kérdésre válaszoló logikát csak *pontosan egy* helyen szeretnénk használni: a rendelés-feladó gombban. Ez a logika lényegében eldobható, csupán egy helyen van haszna, azaz nem fogunk a kódbázis mindenféle pontján `PlaceOrderOnClickHandler` példányokat létrehozni.

Hogyan tudnánk egy ilyen eldobható csomagba belerakni a logikánkat? Itt jönnek a képbe a [névtelen osztályok](https://docs.oracle.com/javase/tutorial/java/javaOO/anonymousclasses.html). Ezek segítségével (szinte) bárhol a kódunkban egyszeri, eldobható absztrakt osztály- és interfész-implementációkat készíthetünk, azonnal példányosítva is azokat.

Tegyünk is egy próbát!

```Java
final var placeOrderButton = new ClickableButton(
    "Place Order",
    new OnClickHandler() {
        @Override
        public void click() {
            System.out.println("Order placed!");
        }
    }
);
```

A fenti kód lényegesen tömörebb, mint az eddigiek. Nincsen szükség nevesített `OnClickHandler`-implementációra, hiszen a logikánkat egy névtelen megvalósításba csomagoltuk. Ez a megvalósítást rögtön példányosítjuk és át is adjuk a `ClickableButton` osztály konstruktorának. Ezt követően a `ClickableButton` ugyanúgy tudja használni, mint bármely másik `OnClickHandler` implementációt.

Ha Java 7-et vagy régebbi kiadást használnánk, akkor ez a dokumentum itt véget is érne, hiszen ennél elegánsabb megoldást nem tudnánk készíteni. Szerencsére, mi már a jövőben élünk, így birtokunkban vannak a Java 8 adata szuperképességek! Lássuk, hogyan tehetjük a fenti kódot még olvashatóbbá és egyszerűbbé!

## Közjáték: Funkcionális interfészek

A funkcionális interfészek (*functional interface*) olyan interfészek, melyek egyetlen abstract (azaz, implementáció nélküli) metódussal rendelkeznek. Ilyen például a jól ismert [java.lang.Runnable](https://docs.oracle.com/javase/8/docs/api/java/lang/Runnable.html):

```Java
public interface Runnable {
    void run();
}
```

vagy az imént definiált `OnClickHandler`:

```Java
interface OnClickHandler {
    void click();
}
```

Nevezik őket SAM (*single abstract method*) interfészeknek is, hiszen lényegük, hogy tetszőlegesen sok [`default` metódussal](https://docs.oracle.com/javase/tutorial/java/IandI/defaultmethods.html) rendelkezhetnek, ugyanakkor metódustörzs nélküli, azaz `abstract` metódusuk csak egyetlen lehet.

Bevezettünk egy újabb, annyira nem is egyszerű fogalmat, de miért? Lapozzunk is tovább!

## Lambdák

Frappánsan megválaszolva az előző rész lezárását: A funkcionális interfészekre azért van szükség, mert a lambda kifejezések lényegében mind névtelen funkcionális interfész-megvalósítások.

Hiába csak pár szó, mégis nehéz a fentiek megértése, így próbáljuk meg a korábbi példánkon keresztül kibontani.

Amint láttuk, a funkcionális interfészek lényege, hogy csupán egyetlen implementálatlan metódussal rendelkeznek. Azaz, bármely megvalósításuknak csak ezt az egy metódust kell kötelezően megvalósítania. Mármost, ha egy implementáló osztálynak eleve csak ezt az egy metódust kell megvalósítania (és itt most gondoljunk a `PlaceOrderOnClickHandler` osztályra és a névtelen `OnClickHandler` implementációnkra), akkor miért is ne tennénk ezt kicsit egyszerűbbé? Azaz, ahelyett, hogy a nagy csinnadrattával létre kell hoznunk egy anonim *osztályt*, mi lenne, ha csak egy anonim *metódust* hoznánk létre?

A lambda kifejezések pontosan ezt teszik lehetővé, kis szintaktikai támogatással: egy adott funkcionális interfész egyetlen absztrakt metódusához adnak konkrét kódot, ezzel névtelenül megvalósítva az egész interfészt.

Lássuk is!

```Java
final OnClickHandler handler = () -> {
    System.out.println("Order placed!");
};

final var placeOrderButton = new ClickableButton(
    "Place Order",
    handler
);
```

A fenti kódrészlet bevezet egy `handler` nevű változót. A handler nevű változó típusa `OnClickHandler`, értéke pedig egy lambdából származik. A lambda lényegében az `OnClickHandler.click()` metódusát valósítja meg, csak név nélkül. Ennek következében:
* nincsenek paraméterei (ezt reprezentálja az üres zárójelpár `()`),
* és nincs visszatérési értéke sem (ezért nincs `return` a kapcsosok között).

Ugyanúgy van azonban paraméterlistája (ismét, ez az üres zárójelpár) és törzse (a két kapcsos által határolt blokk), mint bármely Java metódusnak. A kapcsos közé pedig, természetesen annyi utasítást írunk, amennyit csak szeretnénk.

Ha azonban csak egyetlen utasításból (vagy kifejezésből) áll a lambdánk törzse, akkor a kapcsosokat el is hagyhatjuk:

```Java
final OnClickHandler handler = () -> System.out.println("Order placed!");

final var placeOrderButton = new ClickableButton(
    "Place Order",
    handler
);
```

Végül megszabadulhatunk a köztes `handler` változótól is, melyet csupán a szemléltetés végett vezettünk be:

```Java
final var placeOrderButton = new ClickableButton(
    "Place Order",
    () -> System.out.println("Order placed!")
);
```

A fordító automatikusan tudni fogja, hogy a lambdával az `OnClickHandler` interfészt szeretnénk megvalósítani.

### Szintaktikai kisokos

A lambda kifejezések az alábbi szintaktikai variációkban léteznek:

Variációk a törzset illetően:
* Blokk
  ```Java
  Supplier<String> greeting = () -> {
    return "Hello, World!";
  }
  ```
* Egyetlen kifejezés
  * Ekkor a `return` elhagyható.
  ```Java
  Supplier<String> greeting = () -> "Hello, World";
  ```
  * Használható akkor is, ha a lambda `void` visszatérési értékű.
  ```Java
  Consumer<String> sayHello = name -> System.out.println("Hello " + name);
  ```

Variációk a paraméterlistát illetően:
* Nincsenek paraméterek
  ```Java
  Supplier<String> greeting = () -> "Hello, World";
  ```
* Egyetlen paraméter, típusannotációval
  ```Java
  Consumer<String> sayHello = (String name) -> System.out.println("Hello " + name);
  ```
* Egyetlen paraméter, típusannotáció nélkül
  ```Java
  Consumer<String> sayHello = name -> System.out.println("Hello " + name);
  ```
* Több paraméter, típusannotációval
  ```Java
  BinaryOperator<String> concat = (String a, String b) -> a + b;
  ```
* Több paraméter, típusannotáció nélkül
  ```Java
  BinaryOperator<String> concat = (a, b) -> a + b;
  ```

Összefoglalva:
- Ha egy paraméterünk van, típusannotációk nélkül, akkor opcionális a paraméter körüli zárójel.
- Egyébként kötelező.

### Beépített funkcionális interfészek

A Java standard könyvtár számos beépített funkcionális interfészt tartalmaz a [java.util.function](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/function/package-summary.html) csomagban.

Ezek közül néhány gyakrabban használt interfész az alábbi:
* [Consumer](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/function/Consumer.html)
  * Egy olyan műveletnek felel meg, mely kap valamilyen értéket, azonban nem ad vissza semmit. Lényegében elnyeli a kapott értéket.
  * Egyetlen típusparamétere van: a kapott, elnyelt érték típusa.
  ```Java
  Consumer<String> consumer = str -> System.out.println(str);

  consumer.accept("Hello, World!");
  ```

* [Function](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/function/Function.html)
  * Egy olyan műveletnek felel meg, mely egyetlen értékből képez valamilyen másik értéket.
  * Két típusparamétere van: a bemeneti és a kimeneti érték típusa.
  ```Java
  Function<String, Integer> function = str -> str.length();

  function.apply("Hello, world");
  ```
* [Predicate](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/function/Predicate.html)
  * Egy predikátumnak felel meg, igazságértéket képez valamilyen bemenetre.
  * Egyetlen típusparamétere van: a bemeneti érték típusa. A kimenete mindig `boolean`.
  ```Java
  Predicate<String> isLong = str -> str.length() > 10;
        
  isLong.test("Hello, World!");
  ```
* [Supplier](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/function/Supplier.html)
  * Olyan műveletnek felel meg, mely előállít valamilyen értéket. Nem rendelkezik bemenettel, csak kimenete van.
  * Egyetlen típusparamétere a kimenet típusának felel meg.
  ```
  Supplier<String> greeting = () -> "Hello, World!";
        
  greeting.get();
  ```
* [BinaryOperator](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/function/BinaryOperator.html)
  * Két, azonos típusú értéken végzett művelet.
  * Egyetlen típusparamétere együttesen jelenti a bementi értékek és a kimenet típusát.
  ```
  BinaryOperator<String> concat = (a, b) -> a + b;
        
  concat.apply("Hello", "World");
  ```

## Metódusreferenciák

Ha a lambdák a hab, akkor a metódusreferenciák ([method references](https://docs.oracle.com/javase/tutorial/java/javaOO/methodreferences.html)) a cseresznye!

Nagyon gyakran előfordul, hogy a lambdáink nem is rendelkeznek saját logikával, hanem csak meghívnak egy másik metódust. Például igen gyakori az alábbi:

```Java
List.of("hello", "world")
    .forEach(str -> System.out.println(str));
```

A fenti kódrészlet átad egy lambdát a `forEach` metódusnak. A lambda ugye egy névtelen metódus, mely azonban nem csinál mást, csak átadja az egyetlen paraméterét egy nagyon is nevesített metódusnak, a `System.out` `println`-jének.

A lambda itt egy felesleges indirekciónak tűnik, hiszen a `System.out.println` már adott, és csak ezt csomagoljuk be, hogy megvalósítson egy funkcionális interfészt.

Pontosan itt jönnek a képbe a metódusreferenciák: segítségükkel létező metódusokkal valósíthatunk meg funkcionális interfészeket.

Azaz, a fenti kód helyett írhatjuk a következőt is:

```Java
Consumer<String> printer = System.out::println;

List.of("hello", "world")
    .forEach(printer);
```

Vagy rövidebben:

```Java
List.of("hello", "world")
    .forEach(System.out::println);
```

### Referenciák típusai, használata

De mégis milyen metódusokra tudunk hivatkozni és hogyan?

Haladjunk az egyszerűtől a bonyolultabb esetekig!

* Osztály statikus metódusa
  * A legegyszerűbb ha, egy statikus (*static*) metódusra szeretnénk hivatkozni, melyet `Osztály::statikusMetódus` formában tehetünk meg. A metódus olyan funkcionális interfészeket tud megvalósítani, melyek absztrakt metódusa illeszthető a saját paramétereire és visszatérési típusára. Például:
  ```Java
  class App {
    public static void main(String[] args) {
        List.of("hello", "world")
            .forEach(str -> App.print(str));

        // with method ref
        Consumer<String> printer = App::print; 

        List.of("hello", "world")
                .forEach(printer);
    }
    
    public static void print(String str) {
        System.out.println(str);
    }
  }
  ```
* Adott példány példányszintű metódusa
  * Ha egy osztályból rendelkezünk egy példánnyal, akkor annak is meghivatkozhatjuk egy metódusát `példány::metódus` formában. A metódus olyan funkcionális interfészeket tud megvalósítani, melyek absztrakt metódusa illeszthető a saját paramétereire és visszatérési típusára. Például:
  ```Java
  class Printer {
    public void print(String str) {
        System.out.println(str);
    }
  }

  class App {
    public static void main(String[] args) {
        Printer printer = new Printer(); 
        
        List.of("hello", "world")
                .forEach(str -> printer.print(str));
        
        // with method ref
        List.of("hello", "world")
                .forEach(printer::print);
    }
  }
  ```
  ```Java
  // Strings equal to "world"
  List.of("hello", "world")
    .stream()
    .filter(str -> "world".equals(str))
    .toList();
  
  // with method ref
  List.of("hello", "world")
    .stream()
    .filter("world"::equals)
    .toList();
  ```
* Osztály példányszintű metódusa
  * Akkor is meghívhatunk példányszintű metódust, ha nem rendelkezünk példánnyal az osztályból, mégpedig `Osztály::példányMetódus` módon. Ekkor azonban el kell képzelnünk, hogy a hívott metódus első paramétere az osztály egy példánya. Azaz:
  ```Java
  record Country(String capital) {}

  class App {
      public static void main(String[] args) {
          final var countries = List.of(
                  new Country("Budapest"),
                  new Country("Berlin")
          );
        
          countries.stream()
                  .map(country -> country.capital())
                  .toList();

          // with method ref
          countries.stream()
                  .map(Country::capital)
                  .toList();
      }
  }
  ```
* Osztály konstruktora
  * Az `Osztály::new` formában egy osztály konstruktorára hivatkozhatunk. Ekkor a megvalósított funkcionális interfész paramétereinek a konstruktor paramétereire kell illeszkednie, míg a visszatérési érték típusa nyilván maga az osztály lesz. Például:
  ```Java
  record Country(String capital) {}

  class App {
    public static void main(String[] args) {
        final var capitals = List.of(
                "Budapest",
                "Berlin"
        );

        capitals.stream()
                .map(capital -> new Country(capital))
                .toList();

        // with method ref
        capitals.stream()
                .map(Country::new)
                .toList();
    }
  }
  ```

