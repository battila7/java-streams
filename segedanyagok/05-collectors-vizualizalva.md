# Collectors - Vizualizálva

## Összegyűjtés ismert kollekciókba

### toList()

### toSet()

### toMap(keyMapper, valueMapper)

### joining()

## Csoportosítás

### groupingBy(classifier)

### partitioningBy(predicate)

## Bevezetés a többszintű redukcióba

### Mi az a többszintű redukció?

### További építőkockák a többszintű redukcióhoz

#### counting()

#### minBy(comparator)/minBy(comparator)

#### summing*()

#### averaging*()

## Többszintű redukciót támogató Collectorok

### groupingBy(classifier, downstream)

### partitioningBy(predicate, downstream)

### mapping(mapper, downstream)

### filtering(predicate, downstream)

### collectingAndThen(downstream, finisher)

