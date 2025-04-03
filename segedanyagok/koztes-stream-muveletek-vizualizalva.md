# Köztes Stream-műveletek - Vizualizálva

* [A vizualizációkról](#a-vizualizációkról)
  * [Marble diagrams](#marble-diagrams)
  * [Hogyan jeleníthetjük meg a műveleteket?](#hogyan-jeleníthetjük-meg-a-műveleteket)
* [Vizualizált köztes műveletek](#vizualizált-köztes-műveletek)
  * [distinct()](#distinct)
  * [filter(predicate)](#filterpredicate)
  * [flatMap(mapper)](#flatmapmapper)
  * [limit(maxSize)](#limitmaxsize)
  * [map(mapper)](#mapmapper)
  * [mapToDouble, mapToInt, mapToLong](#maptodoublemapper-maptointmapper-maptolongmapper)
  * [peek(action)](#peekaction)
  * [skip(n)](#skipn)
  * [sorted()](#sorted)
  * [sorted(comparator)](#sortedcomparator)

Ez a dokumentum egy vizualizált gyorstalpaló, mely a leggyakoribb köztes Stream-műveletek (*intermediate operation*) megértését szeretné elősegíteni. Vágjunk is bele!

## A vizualizációkról

Mielőtt belekezdenénk, álljunk meg egy szóra: miért érdemes vizualizálni?

A Streamek egy absztrakciót jelentenek. Ahelyett, hogy az olyan feladatokat, mint például a szűrés (*filtering*) beépített, egyszerű nyelvi eszközökkel oldanánk meg (ciklus és elágazás), inkább absztraháljuk a szűrés logikáját, hogy tisztán csak a szűrési feltételre koncentrálhassunk. Ugyanígy történik ez a rendezéssel is (*sorting*) és sok más, elemek sokaságán végzett művelettel.

Kapunk egy új, magasabb szintű eszközkészletet, amelynek segítségével nem kell az apróságokkal (Hogyan iterálok végig az elemeken?) foglalkoznunk, hiszen ezeket már leprogramozták nekünk. Helyettük csak a saját, feladatspecifikus kódunk marad (Milyen sorrendbe kellene állítani az elemeket?), azt kell beleillesztenünk ebbe az eszközkészletbe.

Ugyanakkor, ezt az absztrakciót, ezt az eszközkészletet csak akkor tudjuk **helyesen** és **hatékonyan** használni, ha megértjük, hogyan működik.

Ezt a megértést pedig
- egyrészt a Streamek természetéből (elemek akár végtelen sorozata),
- msárészt az implementáció bonyolultságából (funkcionális interfészek és lambdák, generikusok és sok-sok típusparaméter)

adódóan legjobban talán vizualizációkkal segíthetjük elő.

Vágjunk is bele!

### Marble diagrams

A vizualizációkhoz úgynevezett *marble diagram*okat fogunk használni, amit magyarra talán *golyódiagram*ként fordíthatunk (hiszen a *marble* itt nem az anyagot, hanem a kis golyókat, golyóbisokat jelenti).

> [!TIP]
> A golyódiagramokat a reaktív programozás tette népszerűvé; előszeretettel használták olyan eszközök, mint például a JavaScriptben készült RxJS könyvtár. Érdemes megnézni az [RxMarbles](https://rxmarbles.com/) oldalt, mely szerkeszthető RxJS diagramokat tartalmaz.

Nézzünk is meg egy ilyen diagramot!

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 442.3094010767585 110" width="442.3094010767585" height="110"><rect x="0" y="0" width="442.3094010767585" height="110" fill="white"></rect><g transform="translate(25 25)"><g><g><line x1="0" y1="30" x2="390" y2="30" stroke="black" stroke-width="2"></line><polyline points="380,24.226497308103742 390,30 380,35.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(329 0)"><line x1="0" y1="5" x2="0" y2="55" stroke="black" stroke-width="2"></line></g><g transform="translate(210 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(175, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"c"</text></g><g transform="translate(120 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(214, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"b"</text></g><g transform="translate(30 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(324, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"a"</text></g></g></g></svg>

A fenti diagramon a következők szerepelnek:
* Egy balról jobbra mutató nyíl. Ez jelképezi az idő múlását. Azaz, az idő balról jobbra folyik.
* Három darab kis kör (*marble*), melyek az értékeket jelképezik. Jelen esetben három darab Stringet: `"a"`, `"b"` és `"c"`.

Hogyan kapcsolódnak ezek a diagramok a Streamekhez? A fenti diagram a következő kódnak felel meg:

```Java
List.of("a", "b", "c")
    .stream()
    .forEach(System.out::println);
```

> [!NOTE]
> `List.of().stream()` helyett használhatnánk a `Stream.of()` metódust is a Streamek létrehozására. Ugyanakkor, mivel a gyakorlaton is elsősorban listákból készítettünk Streameket, ebben a dokumentumban is mindig listákból fogunk kiindulni.

> [!TIP]
> A `Collection` interfész minden implementációja "streamelhető", azaz Streamekkel könnyedén fel tudjuk dolgozni például a `Set` és `Map` példányok tartalmát is.

Ahogy a listában az `"a"` String volt az első elem, úgy a diagramon is ez jelenik meg elsőként. Pontosan azért, mert az idő balról jobbra halad, és az `"a"` elem az első, mely a Streamen végig fog haladni. Utána követi a `"b"`, majd utolsóként a `"c"`.

### Hogyan jeleníthetjük meg a műveleteket?

Az előző példához hasonló Streamekkel szinte sosem találkozunk, hiszen a Streamek egyik lényege, hogy köztes műveletekkel manipuláljuk, kezeljük az adatokat.

Nézzük meg, hogyan ábrázolhatók a köztes műveletek golyódiagramokon, például egy egyszerű `filter`:

```Java
List.of("a", "b", "c")
    .stream()
    .filter(str -> "b".equals(str))
    .forEach(System.out::println);
```

> [!TIP]
> A fenti példában írhatunk `str -> "b".equals(str)` helyett akár `"b"::equals` metódusreferenciát is!

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 442.3094010767585 290" width="442.3094010767585" height="290"><rect x="0" y="0" width="442.3094010767585" height="290" fill="white"></rect><g transform="translate(25 25)"><g><g><line x1="0" y1="30" x2="390" y2="30" stroke="black" stroke-width="2"></line><polyline points="380,24.226497308103742 390,30 380,35.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(329 0)"><line x1="0" y1="5" x2="0" y2="55" stroke="black" stroke-width="2"></line></g><g transform="translate(210 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(175, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"c"</text></g><g transform="translate(120 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(214, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"b"</text></g><g transform="translate(30 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(324, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"a"</text></g></g><g transform="translate(0 80)"><rect x="1" y="11" width="390.3094010767585" height="58" fill="white" stroke="black" stroke-width="2" rx="0"></rect><text x="196.15470053837925" y="40" fill="black" dominant-baseline="middle" text-anchor="middle" font-family="Arial, Helvetica, sans-serif" font-size="24px" font-weight="normal" font-style="normal" width="392.3094010767585">.filter(str -&gt; "b".equals(str))</text></g><g transform="translate(0 180)"><g><line x1="0" y1="30" x2="390" y2="30" stroke="black" stroke-width="2"></line><polyline points="380,24.226497308103742 390,30 380,35.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(329 0)"><line x1="0" y1="5" x2="0" y2="55" stroke="black" stroke-width="2"></line></g><g transform="translate(120 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(214, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"b"</text></g></g></g></svg>

Szemben a korábbi esettel, ezúttal megjelenik egy újabb nyíl, továbbá egy doboz is. Hogyan értelmezhetjük ezeket?
* A két nyíl között elhelyezkedő doboz egy köztes műveletnek felel meg. A felette található nyílról kapja a bemeneteket, az alatta található nyíl pedig a kimenetét jelenti.
* A köztes művelet ezúttal a `.filter(str -> "b".equals(str))`, mely csak a `"b"` értékeket engedi át (minden más értéket eltávolít).
* Vegyük észre, hogy ennek megfelelően a lenti nyíl már nem tartalmazza sem az `"a"`, sem a `"c"` értékeket.
* Az eltávolított értékek helye ugyanakkor ki van hagyva a lenti nyílon is. Ez annak következménye, hogy a Streamek végrehajtása vertikális, azaz nem műveletenként, hanem elemenként történik. Először az `"a"` értékre fogjuk végrehajtani az összes műveletet. Mivel az `"a"` eltávolítja a `filter`, ezért az ő helyén időben nem lehet senki a lenti nyílon.

Nézzünk meg még egy példát, immár több művelettel, beleértve egy állapottal rendelkező (*stateful*) műveletet is:

```Java
List.of("b", "c", "a")
    .stream()
    .sorted()
    .map(str -> str + str)
    .forEach(System.out::println);
```

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 712.3094010767585 470" width="712.3094010767585" height="470"><rect x="0" y="0" width="712.3094010767585" height="470" fill="white"></rect><g transform="translate(25 25)"><g><g><line x1="0" y1="30" x2="390" y2="30" stroke="black" stroke-width="2"></line><polyline points="380,24.226497308103742 390,30 380,35.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(329 0)"><line x1="0" y1="5" x2="0" y2="55" stroke="black" stroke-width="2"></line></g><g transform="translate(210 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(324, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"a"</text></g><g transform="translate(120 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(175, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"c"</text></g><g transform="translate(30 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(214, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"b"</text></g></g><g transform="translate(0 80)"><rect x="1" y="11" width="660.3094010767585" height="58" fill="white" stroke="black" stroke-width="2" rx="0"></rect><text x="331.15470053837925" y="40" fill="black" dominant-baseline="middle" text-anchor="middle" font-family="Arial, Helvetica, sans-serif" font-size="24px" font-weight="normal" font-style="normal" width="662.3094010767585">.sorted()</text></g><g transform="translate(0 180)"><g><line x1="0" y1="30" x2="660" y2="30" stroke="black" stroke-width="2"></line><polyline points="650,24.226497308103742 660,30 650,35.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(599 0)"><line x1="0" y1="5" x2="0" y2="55" stroke="black" stroke-width="2"></line></g><g transform="translate(480 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(175, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"c"</text></g><g transform="translate(390 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(214, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"b"</text></g><g transform="translate(300 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(324, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"a"</text></g></g><g transform="translate(0 260)"><rect x="1" y="11" width="660.3094010767585" height="58" fill="white" stroke="black" stroke-width="2" rx="0"></rect><text x="331.15470053837925" y="40" fill="black" dominant-baseline="middle" text-anchor="middle" font-family="Arial, Helvetica, sans-serif" font-size="24px" font-weight="normal" font-style="normal" width="662.3094010767585">.map(str -&gt; str + str)</text></g><g transform="translate(0 360)"><g><line x1="0" y1="30" x2="660" y2="30" stroke="black" stroke-width="2"></line><polyline points="650,24.226497308103742 660,30 650,35.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(599 0)"><line x1="0" y1="5" x2="0" y2="55" stroke="black" stroke-width="2"></line></g><g transform="translate(480 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(213, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"cc"</text></g><g transform="translate(390 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(322, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"bb"</text></g><g transform="translate(300 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(227, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"aa"</text></g></g></g></svg>

Ez a digram egészen más, mint amit eddig láttunk, hiszen az értékek egyszer csak eltolva szerepelnek. Mi történik itt pontosan?
* Rendezni (`sorted()`) csak akkor tudunk, ha már láttuk az összes értéket, pontosan emiatt fog állapottal rendelkezni a `sorted()`.
* Egyúttal ez azt is jelenti, hogy a `sorted()` műveletnek "be kell várnia" minden értéket, rendezni csak utána tud.
* Ha visszaemlékszünk, a balról jobbra mutató nyilak az idő múlását jelképezik. Mivel a `sorted` mindenkit "bevár", ezért a rendezett elemeket csak időben eltolva tudja kibocsátani, nem rögtön.
* És pontosan emiatt fognak a rendezett elemek nem közvetlenül a rendezetlen elemek alatt, hanem csak azok után megjelenni a `sorted()` alatti nyílon.
* A második köztes művelet, a `map()` már a korábban látott módon működik, hiszen állapotmentes.

## Vizualizált köztes műveletek

A következőkben néhány gyakori köztes műveletet tekintünk.

### distinct()

* [JavaDoc](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html#distinct())

Visszaad egy olyan Streamet, mely nem tartalmaz duplikált elemeket (ahol a duplikáció megállapítása `Object.equals(Object)` segítségével történik).

```Java
List.of("a", "b", "a", "b")
    .stream()
    .distinct()
    .forEach(System.out::println);
```

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 712.3094010767585 290" width="712.3094010767585" height="290"><rect x="0" y="0" width="712.3094010767585" height="290" fill="white"></rect><g transform="translate(25 25)"><g><g><line x1="0" y1="30" x2="480" y2="30" stroke="black" stroke-width="2"></line><polyline points="470,24.226497308103742 480,30 470,35.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(419 0)"><line x1="0" y1="5" x2="0" y2="55" stroke="black" stroke-width="2"></line></g><g transform="translate(300 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(214, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"b"</text></g><g transform="translate(210 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(324, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"a"</text></g><g transform="translate(120 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(214, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"b"</text></g><g transform="translate(30 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(324, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"a"</text></g></g><g transform="translate(0 80)"><rect x="1" y="11" width="660.3094010767585" height="58" fill="white" stroke="black" stroke-width="2" rx="0"></rect><text x="331.15470053837925" y="40" fill="black" dominant-baseline="middle" text-anchor="middle" font-family="Arial, Helvetica, sans-serif" font-size="24px" font-weight="normal" font-style="normal" width="662.3094010767585">.distinct()</text></g><g transform="translate(0 180)"><g><line x1="0" y1="30" x2="660" y2="30" stroke="black" stroke-width="2"></line><polyline points="650,24.226497308103742 660,30 650,35.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(599 0)"><line x1="0" y1="5" x2="0" y2="55" stroke="black" stroke-width="2"></line></g><g transform="translate(480 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(214, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"b"</text></g><g transform="translate(390 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(324, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"a"</text></g></g></g></svg>

### filter(predicate)

* [JavaDoc](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html#filter(java.util.function.Predicate))

Visszaad egy olyan Streamet, mely csak azokat az elemeket tartalmazza, melyek megfelelnek a kapott predikátumnak (azaz, melyekre a predikátum `true` értéket ad).

```
List.of("a", "aa", "aaa")
    .stream()
    .filter(str -> str.length() == 2)
    .forEach(System.out::println);
```
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 442.3094010767585 290" width="442.3094010767585" height="290"><rect x="0" y="0" width="442.3094010767585" height="290" fill="white"></rect><g transform="translate(25 25)"><g><g><line x1="0" y1="30" x2="390" y2="30" stroke="black" stroke-width="2"></line><polyline points="380,24.226497308103742 390,30 380,35.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(329 0)"><line x1="0" y1="5" x2="0" y2="55" stroke="black" stroke-width="2"></line></g><g transform="translate(210 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(192, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"aaa"</text></g><g transform="translate(120 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(227, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"aa"</text></g><g transform="translate(30 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(324, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"a"</text></g></g><g transform="translate(0 80)"><rect x="1" y="11" width="390.3094010767585" height="58" fill="white" stroke="black" stroke-width="2" rx="0"></rect><text x="196.15470053837925" y="40" fill="black" dominant-baseline="middle" text-anchor="middle" font-family="Arial, Helvetica, sans-serif" font-size="24px" font-weight="normal" font-style="normal" width="392.3094010767585">.filter(str -&gt; str.length() == 2)</text></g><g transform="translate(0 180)"><g><line x1="0" y1="30" x2="390" y2="30" stroke="black" stroke-width="2"></line><polyline points="380,24.226497308103742 390,30 380,35.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(329 0)"><line x1="0" y1="5" x2="0" y2="55" stroke="black" stroke-width="2"></line></g><g transform="translate(120 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(227, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"aa"</text></g></g></g></svg>

### flatMap(mapper)

* [JavaDoc](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html#flatMap(java.util.function.Function))

Visszaad egy olyan Streamet, mely a `mapper` által visszaadott Streamek elemeit tartalmazza.

Azaz, a `mapper` az eredeti Streamünk elemeiből Streameket kell, hogy képezzen.

```
List.of(List.of("a"), List.of("b", "c"), List.of("d"))
    .stream()
    .flatMap(lst -> lst.stream())
    .forEach(System.out::println);
```

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 822.3094010767585 330" width="822.3094010767585" height="330"><rect x="0" y="0" width="822.3094010767585" height="330" fill="white"></rect><g transform="translate(25 25)"><g><g><line x1="0" y1="40" x2="770" y2="40" stroke="black" stroke-width="2"></line><polyline points="760,34.226497308103745 770,40 760,45.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(689 0)"><line x1="0" y1="15" x2="0" y2="65" stroke="black" stroke-width="2"></line></g><g transform="translate(440 0)"><ellipse cx="40" cy="40" rx="39" ry="39" fill="hsl(193, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(40 40)">["d"]</text></g><g transform="translate(170 0)"><ellipse cx="40" cy="40" rx="39" ry="39" fill="hsl(303, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(40 40)">["b", "c"]</text></g><g transform="translate(20 0)"><ellipse cx="40" cy="40" rx="39" ry="39" fill="hsl(298, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(40 40)">["a"]</text></g></g><g transform="translate(0 100)"><rect x="1" y="11" width="770.3094010767585" height="58" fill="white" stroke="black" stroke-width="2" rx="0"></rect><text x="386.15470053837925" y="40" fill="black" dominant-baseline="middle" text-anchor="middle" font-family="Arial, Helvetica, sans-serif" font-size="24px" font-weight="normal" font-style="normal" width="772.3094010767585">.flatMap(lst -&gt; lst.stream())</text></g><g transform="translate(0 200)"><g><line x1="0" y1="40" x2="740" y2="40" stroke="black" stroke-width="2"></line><polyline points="730,34.226497308103745 740,40 730,45.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(659 0)"><line x1="0" y1="15" x2="0" y2="65" stroke="black" stroke-width="2"></line></g><g transform="translate(440 0)"><ellipse cx="40" cy="40" rx="39" ry="39" fill="hsl(25, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(40 40)">"d"</text></g><g transform="translate(260 0)"><ellipse cx="40" cy="40" rx="39" ry="39" fill="hsl(175, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(40 40)">"c"</text></g><g transform="translate(170 0)"><ellipse cx="40" cy="40" rx="39" ry="39" fill="hsl(214, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(40 40)">"b"</text></g><g transform="translate(20 0)"><ellipse cx="40" cy="40" rx="39" ry="39" fill="hsl(324, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(40 40)">"a"</text></g></g></g></svg>

### limit(maxSize)

* [JavaDoc](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html#limit(long))

Visszaad egy olyan Streamet, mely legfeljebb `maxSize` hosszúságú (azaz, legfeljebb annyi elemet tartalmaz).

```
List.of("a", "b", "c")
    .stream()
    .limit(2)
    .forEach(System.out::println);
```

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 442.3094010767585 290" width="442.3094010767585" height="290"><rect x="0" y="0" width="442.3094010767585" height="290" fill="white"></rect><g transform="translate(25 25)"><g><g><line x1="0" y1="30" x2="390" y2="30" stroke="black" stroke-width="2"></line><polyline points="380,24.226497308103742 390,30 380,35.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(329 0)"><line x1="0" y1="5" x2="0" y2="55" stroke="black" stroke-width="2"></line></g><g transform="translate(210 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(175, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"c"</text></g><g transform="translate(120 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(214, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"b"</text></g><g transform="translate(30 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(324, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"a"</text></g></g><g transform="translate(0 80)"><rect x="1" y="11" width="390.3094010767585" height="58" fill="white" stroke="black" stroke-width="2" rx="0"></rect><text x="196.15470053837925" y="40" fill="black" dominant-baseline="middle" text-anchor="middle" font-family="Arial, Helvetica, sans-serif" font-size="24px" font-weight="normal" font-style="normal" width="392.3094010767585">.limit(2)</text></g><g transform="translate(0 180)"><g><line x1="0" y1="30" x2="390" y2="30" stroke="black" stroke-width="2"></line><polyline points="380,24.226497308103742 390,30 380,35.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(329 0)"><line x1="0" y1="5" x2="0" y2="55" stroke="black" stroke-width="2"></line></g><g transform="translate(120 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(214, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"b"</text></g><g transform="translate(30 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(324, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"a"</text></g></g></g></svg>

### map(mapper)

* [JavaDoc](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html#map(java.util.function.Function))

Alkalmazza a kapott transzformációt a Stream minden elemére és visszaadja a transzformált elemeket tartalmazó Streamet.

```
List.of("a", "b", "c")
    .stream()
    .map(str -> str + "f")
    .forEach(System.out::println);
```

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 442.3094010767585 290" width="442.3094010767585" height="290"><rect x="0" y="0" width="442.3094010767585" height="290" fill="white"></rect><g transform="translate(25 25)"><g><g><line x1="0" y1="30" x2="390" y2="30" stroke="black" stroke-width="2"></line><polyline points="380,24.226497308103742 390,30 380,35.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(329 0)"><line x1="0" y1="5" x2="0" y2="55" stroke="black" stroke-width="2"></line></g><g transform="translate(210 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(175, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"c"</text></g><g transform="translate(120 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(214, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"b"</text></g><g transform="translate(30 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(324, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"a"</text></g></g><g transform="translate(0 80)"><rect x="1" y="11" width="390.3094010767585" height="58" fill="white" stroke="black" stroke-width="2" rx="0"></rect><text x="196.15470053837925" y="40" fill="black" dominant-baseline="middle" text-anchor="middle" font-family="Arial, Helvetica, sans-serif" font-size="24px" font-weight="normal" font-style="normal" width="392.3094010767585">.map(str -&gt; str + "f")</text></g><g transform="translate(0 180)"><g><line x1="0" y1="30" x2="390" y2="30" stroke="black" stroke-width="2"></line><polyline points="380,24.226497308103742 390,30 380,35.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(329 0)"><line x1="0" y1="5" x2="0" y2="55" stroke="black" stroke-width="2"></line></g><g transform="translate(210 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(137, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"cf"</text></g><g transform="translate(120 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(220, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"bf"</text></g><g transform="translate(30 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(194, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"af"</text></g></g></g></svg>

### mapToDouble(mapper), mapToInt(mapper), mapToLong(mapper)

* [JavaDoc: mapToDouble](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html#mapToDouble(java.util.function.ToDoubleFunction))
* [JavaDoc: mapToInt](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html#mapToInt(java.util.function.ToIntFunction))
* [JavaDoc: mapToLong](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html#mapToLong(java.util.function.ToLongFunction))

Alkalmazza a megadott, `double`, `int` vagy `long` visszatérési típusú transzformációt a Stream minden elemére és visszadja a transzformált értékeket egy `DoubleStream`, `IntStream` vagy `LongStream` formájában.

```
List.of("a", "bb", "ccc")
    .stream()
    .mapToLong(str -> str.length())
    .forEach(System.out::println);
```

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 442.3094010767585 290" width="442.3094010767585" height="290"><rect x="0" y="0" width="442.3094010767585" height="290" fill="white"></rect><g transform="translate(25 25)"><g><g><line x1="0" y1="30" x2="390" y2="30" stroke="black" stroke-width="2"></line><polyline points="380,24.226497308103742 390,30 380,35.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(329 0)"><line x1="0" y1="5" x2="0" y2="55" stroke="black" stroke-width="2"></line></g><g transform="translate(210 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(36, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"ccc"</text></g><g transform="translate(120 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(322, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"bb"</text></g><g transform="translate(30 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(324, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"a"</text></g></g><g transform="translate(0 80)"><rect x="1" y="11" width="390.3094010767585" height="58" fill="white" stroke="black" stroke-width="2" rx="0"></rect><text x="196.15470053837925" y="40" fill="black" dominant-baseline="middle" text-anchor="middle" font-family="Arial, Helvetica, sans-serif" font-size="24px" font-weight="normal" font-style="normal" width="392.3094010767585">.mapToLong(str -&gt; str.length())</text></g><g transform="translate(0 180)"><g><line x1="0" y1="30" x2="390" y2="30" stroke="black" stroke-width="2"></line><polyline points="380,24.226497308103742 390,30 380,35.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(329 0)"><line x1="0" y1="5" x2="0" y2="55" stroke="black" stroke-width="2"></line></g><g transform="translate(210 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(147, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">3</text></g><g transform="translate(120 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(206, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">2</text></g><g transform="translate(30 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(35, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">1</text></g></g></g></svg>

### peek(action)

* [JavaDoc](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html#peek(java.util.function.Consumer))

Átereszti a Stream elemeit (azaz, a visszaadott Stream pontosan ugyanazokat az elemeket fogja tartalmazni, ugyanabban a sorrendben), ugyanakkor végrehajtja minden elemre a kapott műveletet (`action`).

```
List.of("a", "b", "c")
    .stream()
    .peek(str -> System.out.println(str + "f"))
    .forEach(System.out::println);
```

> [!NOTE]
> A fenti példa remekül megjeleníti a Streamek vertikális végrehajtását: először az "a" elem halad végig a csővezetéken, őt követi "b", majd végül "c". Ennek következében a kimenet így alakul: `af a bf b cf c` (természetesen külön sorokba írva).

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 622.3094010767585 290" width="622.3094010767585" height="290"><rect x="0" y="0" width="622.3094010767585" height="290" fill="white"></rect><g transform="translate(25 25)"><g><g><line x1="0" y1="30" x2="570" y2="30" stroke="black" stroke-width="2"></line><polyline points="560,24.226497308103742 570,30 560,35.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(509 0)"><line x1="0" y1="5" x2="0" y2="55" stroke="black" stroke-width="2"></line></g><g transform="translate(270 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(175, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"c"</text></g><g transform="translate(180 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(214, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"b"</text></g><g transform="translate(90 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(324, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"a"</text></g></g><g transform="translate(0 80)"><rect x="1" y="11" width="570.3094010767585" height="58" fill="white" stroke="black" stroke-width="2" rx="0"></rect><text x="286.15470053837925" y="40" fill="black" dominant-baseline="middle" text-anchor="middle" font-family="Arial, Helvetica, sans-serif" font-size="24px" font-weight="normal" font-style="normal" width="572.3094010767585">.peek(str -&gt; System.out.println(str + "f"))</text></g><g transform="translate(0 180)"><g><line x1="0" y1="30" x2="570" y2="30" stroke="black" stroke-width="2"></line><polyline points="560,24.226497308103742 570,30 560,35.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(509 0)"><line x1="0" y1="5" x2="0" y2="55" stroke="black" stroke-width="2"></line></g><g transform="translate(270 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(175, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"c"</text></g><g transform="translate(180 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(214, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"b"</text></g><g transform="translate(90 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(324, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"a"</text></g></g></g></svg>

### skip(n)

* [JavaDoc](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html#skip(long))

Eldobja a Stream első `n` elemét és visszaad egy olyan Streamet, mely a megmaradt elemeket tartalmazza.

Ha a Streamnek `n`-nél kevesebb eleme van, akkor a visszaadott Stream üres lesz.

```
List.of("a", "b", "c")
    .stream()
    .skip(2)
    .forEach(System.out::println);
```

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 442.3094010767585 290" width="442.3094010767585" height="290"><rect x="0" y="0" width="442.3094010767585" height="290" fill="white"></rect><g transform="translate(25 25)"><g><g><line x1="0" y1="30" x2="390" y2="30" stroke="black" stroke-width="2"></line><polyline points="380,24.226497308103742 390,30 380,35.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(329 0)"><line x1="0" y1="5" x2="0" y2="55" stroke="black" stroke-width="2"></line></g><g transform="translate(210 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(175, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"c"</text></g><g transform="translate(120 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(214, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"b"</text></g><g transform="translate(30 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(324, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"a"</text></g></g><g transform="translate(0 80)"><rect x="1" y="11" width="390.3094010767585" height="58" fill="white" stroke="black" stroke-width="2" rx="0"></rect><text x="196.15470053837925" y="40" fill="black" dominant-baseline="middle" text-anchor="middle" font-family="Arial, Helvetica, sans-serif" font-size="24px" font-weight="normal" font-style="normal" width="392.3094010767585">.skip(2)</text></g><g transform="translate(0 180)"><g><line x1="0" y1="30" x2="390" y2="30" stroke="black" stroke-width="2"></line><polyline points="380,24.226497308103742 390,30 380,35.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(329 0)"><line x1="0" y1="5" x2="0" y2="55" stroke="black" stroke-width="2"></line></g><g transform="translate(210 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(175, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"c"</text></g></g></g></svg>

### sorted()

* [JavaDoc](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html#sorted())

Visszaad egy olyan Streamet, mely rendezve tartalmazza ennek a Streamnek az elemeit.

Csak akkor tudja végrehajtani a rendezést, ha a Stream rendezhető elemeket tartalmaz (azaz, az elemek típusa implementálja a `Comparable` interfészt).

A rendezés az alapértelmezett sorrend (természetes vagy *natural order*) szerint történik. Ha egyedi feltételek szerint akarunk rendezni, akkor használjuk a `sorted(comparator)` metódust.

```Java
List.of("b", "c", "a")
    .stream()
    .sorted()
    .forEach(System.out::println);
```

> [!TIP]
> A fenti példa ekvivalens a következővel: `sorted(Comparator.naturalOrder())`.

<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 802.3094010767585 290" width="802.3094010767585" height="290"><rect x="0" y="0" width="802.3094010767585" height="290" fill="white"></rect><g transform="translate(25 25)"><g><g><line x1="0" y1="30" x2="750" y2="30" stroke="black" stroke-width="2"></line><polyline points="740,24.226497308103742 750,30 740,35.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(689 0)"><line x1="0" y1="5" x2="0" y2="55" stroke="black" stroke-width="2"></line></g><g transform="translate(270 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(324, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"a"</text></g><g transform="translate(180 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(175, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"c"</text></g><g transform="translate(90 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(214, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"b"</text></g></g><g transform="translate(0 80)"><rect x="1" y="11" width="750.3094010767585" height="58" fill="white" stroke="black" stroke-width="2" rx="0"></rect><text x="376.15470053837925" y="40" fill="black" dominant-baseline="middle" text-anchor="middle" font-family="Arial, Helvetica, sans-serif" font-size="24px" font-weight="normal" font-style="normal" width="752.3094010767585">.sorted()</text></g><g transform="translate(0 180)"><g><line x1="0" y1="30" x2="750" y2="30" stroke="black" stroke-width="2"></line><polyline points="740,24.226497308103742 750,30 740,35.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(689 0)"><line x1="0" y1="5" x2="0" y2="55" stroke="black" stroke-width="2"></line></g><g transform="translate(450 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(175, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"c"</text></g><g transform="translate(360 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(214, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"b"</text></g><g transform="translate(270 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(324, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"a"</text></g></g></g></svg>

### sorted(comparator)

* [JavaDoc](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/util/stream/Stream.html#sorted(java.util.Comparator))

Visszaad egy olyan Streamet, mely rendezve tartalmazza ennek a Streamnek az elemeit.

A rendezés a megadott `comparator` segítségével történik.

```Java
List.of("bb", "c", "aaa")
    .stream()
    .sorted(Comparator.comparingLong(str -> str.length()))
    .forEach(System.out::println);
```
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 802.3094010767585 290" width="802.3094010767585" height="290"><rect x="0" y="0" width="802.3094010767585" height="290" fill="white"></rect><g transform="translate(25 25)"><g><g><line x1="0" y1="30" x2="750" y2="30" stroke="black" stroke-width="2"></line><polyline points="740,24.226497308103742 750,30 740,35.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(689 0)"><line x1="0" y1="5" x2="0" y2="55" stroke="black" stroke-width="2"></line></g><g transform="translate(270 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(192, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"aaa"</text></g><g transform="translate(180 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(175, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"c"</text></g><g transform="translate(90 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(322, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"bb"</text></g></g><g transform="translate(0 80)"><rect x="1" y="11" width="750.3094010767585" height="58" fill="white" stroke="black" stroke-width="2" rx="0"></rect><text x="376.15470053837925" y="40" fill="black" dominant-baseline="middle" text-anchor="middle" font-family="Arial, Helvetica, sans-serif" font-size="24px" font-weight="normal" font-style="normal" width="752.3094010767585">.sorted(Comparator.comparingLong(str -&gt; str.length()))</text></g><g transform="translate(0 180)"><g><line x1="0" y1="30" x2="750" y2="30" stroke="black" stroke-width="2"></line><polyline points="740,24.226497308103742 750,30 740,35.773502691896255" fill="none" stroke="black" stroke-width="2" stroke-linecap="square"></polyline></g><g transform="translate(689 0)"><line x1="0" y1="5" x2="0" y2="55" stroke="black" stroke-width="2"></line></g><g transform="translate(450 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(192, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"aaa"</text></g><g transform="translate(360 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(322, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"bb"</text></g><g transform="translate(270 0)"><ellipse cx="30" cy="30" rx="29" ry="29" fill="hsl(175, 60%, 80%)" stroke="black" stroke-width="2"></ellipse><text x="0" y="0" fill="black" font-family="Arial, Helvetica, sans-serif" font-size="18px" font-weight="normal" font-style="normal" dominant-baseline="middle" text-anchor="middle" transform="translate(30 30)">"c"</text></g></g></g></svg>
