*Read this in other languages: [English](README.md), [Hungarian](README.hu.md).*

# A projektről
Ez a projekt a "DXVK pipeline state cache" fájlok tárolását és automatikus összevonását
hivatott megoldani alkalmazásonként egyetlen növekvő "incremental cache file"-ba, amelyet
később több eszköz között is meg lehet osztani. Egy példa ennek a manuális megvalósítására
olvasható [itt](https://www.reddit.com/r/linux_gaming/comments/t5xrho/dxvk_state_cache_for_fixing_stutter_in_apex/).

Az ötlet az, hogy különböző felhasználók regisztrálhatnak az API-hoz és megoszthatják
az alkalmazásuk által generált "cache" fájljaikat, melyet a backend eltárol és a 
dxvk-cache-tool nevű programmal az alkalmazáshoz tartozó növekvő "cache" fájllal
összevonja. Az aktuális növekvő "cache" fájlt bárki bármikor letöltheti, regisztráció
nem szükséges hozzá.

> *Megjegyzés:* a program nem ad UI-t, csak REST végpontokat szolgáltat

# Ami készen van
- DTO-k használata
- DTO-k validációja
- Játékprogramok eltárolása/frissítlse/törlése
- Felhasználók eltárolása/frissítlse/törlése
- Cache fájlok eltárolása/törlése
- Cache fájlok automatikus összevonása játékonként 1-1 növekvő cache fájlba
- Bináris fájlok stream-elése adatbázisból/-ba
- Érvénytelen cache fájlok visszadobása
- Olyan Cache fájlok visszadobása, melyek nem hoznak be új cache entry-t
- Integrációs és egységtesztek
- Aszinkronos futtatás
- Egyszerű logolás
- PostgreSQL backend használata
- Swagger dokumentáció
- Flyway adatbázis migráció:
  - Egy .sql fájl migráció (V1)
  - Egy Java migráció (V2)

# Ami nincs kész
Az idő szorítása miatt nem sikerült elkészítenem minden tervezett funkciót. Amik kimaradtak:
- HTTP Basic Authentication. Jelenleg a felhasználóknak meg kell adniuk egy jelszót, de az
  titkosítatlanul kerül eltárolásra és nem kerül felhasználásra sehol sem. A program nem
  használ jelenleg egyetlen endpointjához sem authentikációt.
- Automatikus Newman black box tesztek írása
- Felhasználó profilképek validálása
- Cache fájlok byte szintű validálása. Jelenleg a validációt úgy végzi a program, hogy minden
  egyes cache fájlon lefuttatja a dxvk-cache-tool -t és ha az hibakóddal tér vissza, akkor a
  cache file mindenbizonnyal hibás. Szerintem nem a legjobb megoldás, de egy gyors megoldás.

# Végpontok
A végpontok láthatóak a [**Wiki oldalon**](https://github.com/HereticWay/DXVKStateCacheBank/wiki)
vagy a program futása közben a [**Swagger dokumentációban**](http://127.0.0.1:8080/swagger-ui.html).

# Az adatbázis tábláinak ER Diagramja
![Cannot load the picture](https://github.com/HereticWay/DXVKStateCacheBank/raw/docs/docs/db-er-diagram.png)

# A program futtatása
Jelenleg a programom csak Dockerrel, vagy Linux alatt futtatható, mert a
dxvk-cache-tool alkalmazásnak nincs natív Windows kiadása.

## Futtatás Dockerben
Szükségletek:
- docker
- docker compose plugin (általában ez a docker-rel együtt automatikusan települ)
```shell
# Navigálj a projekt könyvtárába és futtasd a következő utasítást:
$ docker compose up
```

## Futtatás Linux alól
Szükségletek:
- docker
- docker compose plugin (általában ez a docker-rel együtt automatikusan települ)
- [**dxvk-cache-tool**](https://github.com/DarkTigrus/dxvk-cache-tool/releases/tag/v1.1.2)
  megléte **futtatási jogokkal**:
  - egy **PATH könyvtárban** (ajánlott) 
  - vagy egy bármely másik könyvtárban (több konfiguráció szükséges hozzá, erről majd később)
- egy **PostgreSQL adatbázis szerver** (fellőhető docker-ből, lásd [A szükséges PostgreSQL szerver elindítása](#a-szükséges-postgresql-szerver-elindítása))
- egy **futtatási profil** kiválasztása.

### A szükséges PostgreSQL szerver elindítása
Az adatbázis kívülről a következő adatokkal lesz elérhető:<br>
url: `//localhost:5430/dxvk-cache-bank-db`<br>
user: postgres<br>
password: pass<br>
```shell
$ docker run \
    -it \
    --rm \
    -p "5430:5432" \
    -e POSTGRES_USER=postgres \
    -e POSTGRES_PASSWORD=pass \
    -e POSTGRES_DB=dxvk-cache-bank-db \
    postgres:14.3-alpine3.16
```

### A program indítása Maven segítségével
```shell
# Ha a dxvk-cache-tool egy PATH könyvtárban van:
$ ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Ha a dxvk-cache-tool máshol elérhető, akkor meg kell adni ezt a könyvtárat az alkalmazásnak PATH könyvtárként:
$ env PATH=$PATH:/path/to/dxvk-cache-tool-directory ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### A program indítása Intellij IDEA-ban
- Indítsd el a PostgreSQL szervert (lásd: [A szükséges PostgreSQL szerver elindítása](#a-szükséges-postgresql-szerver-elindítása))
- Töltsd be a projektet maven projektként
- Állítsd be a **"Run/Debug Configurations"** -t a következőképpen:
- ![Cannot load the picture](https://github.com/HereticWay/DXVKStateCacheBank/raw/docs/docs/intellij-run-configuration.png)
- Ha a dxvk-cache-tool **nem** egy PATH könytvárban van, akkor a
**"Run/DebugConfigurations"->"Environment Variables"** mezőben meg kell adni még a
letöltött program könyvtárát PATH könyvtárként, pl.: *"PATH=$PATH:/path/to/dxvk-cache-tool-directory"*
- A program ezután a szokásos módon indítható IDEA-val.

### Tesztek futtatása Intellij IDEA-val
- Indítsd el a PostgreSQL szervert (lásd: [A szükséges PostgreSQL szerver elindítása](#a-szükséges-postgresql-szerver-elindítása))
- Töltsd be a projektet maven projektként
- Adj hozzá új tesztkonfigurációt: **"Run/DebugConfigurations"->"Add new configuration"->"JUnit"
- Állítsd be a következőképpen:<br>
Profilt **itt nem** szükséges megadni!<br>
![Cannot load the picture](https://github.com/HereticWay/DXVKStateCacheBank/raw/docs/docs/intellij-test-configuration.png)
- Ha a dxvk-cache-tool **nem** egy PATH könytvárban van, akkor az
  **"Environment Variables"** mezőben meg kell adni még a letöltött program könyvtárát
  PATH könyvtárként, pl.: *"PATH=$PATH:/path/to/dxvk-cache-tool-directory"*
- Néha a test/resources mappa nem kerül automatikusan "test resources" mappának megjelölésre.
  hogy ezt kijavítsuk, meg kell jelölnünk "test resources root"-ként a következőképpen:<br>
  jobb klikk a test/resources mappára->"Mark directory as"->"Resources root"<br>
  ![Cannot load the picture](https://github.com/HereticWay/DXVKStateCacheBank/raw/docs/docs/mark-resource-1.png)
  ![Cannot load the picture](https://github.com/HereticWay/DXVKStateCacheBank/raw/docs/docs/mark-resource-2.png)
- Mostmár futtathatod a teszteket IDEA-val a szokásos módon

## Futtatás Windows alól
A futtatás Windows alól csak **WSL Docker konténerben** támogatott.<br>
Lásd: [*A program futtatása*](#a-program-futtatása), [*Futtatás Dockerben*](#futtatás-dockerben)

# Black-box tesztelés Postman-nel
- Sajnos newman tesztek elkészítésére már nem volt időm, ezért manuálisan lehet caak kívülről tesztelni a projektet
- Indítsd el a programot Dockerben (lásd: [*Futtatás Dockerben*](#futtatás-dockerben))
- Használd a következő [Postman Workspace](https://www.postman.com/science-saganist-76503499/workspace/cc-dxvk-state-cache-bank/collection/19640926-f241d113-7f3d-4bb9-b59b-1111afe55f89?ctx=documentation)-t
  tesztelésre.
- Példa cache és profilkép fájlokat a Postman kérések futtatásához találsz a projekt resources/sample mappájában
- Egy példa a tesztelésre, a program működésének szemléltetése érdekében:
  1. Egy Game posztolása
  2. Egy User posztolása
  3. r5apex-barely-populated.dxvk-cache CacheFile posztolása a létrehozott User-ként a létrehozott Game-hez -
     El kell tárolnia a cache file-t és 200OK-t kell visszaadnia. Incremental cache-ben is létre kell jönnie a fájlnak.
  4. r5apex-highly-populated.dxvk-cache CacheFile posztolása a létrehozott User-ként a létrehozott Game-hez -
     El kell tárolnia a cache file-t és 200OK-t kell visszaadnia. Incremental cache-ben az összevont fájlnak kell lennie.
  5. r5apex-barely-populated.dxvk-cache CacheFile újraposztolása a létrehozott User-ként a létrehozott Game-hez -
     Nem szabad eltárolnia a cache file-t és 422 UNPROCESSABLE_ENTITY-t kell visszaadnia, mert ugyanazt a cache
     file-t posztolva már nem hoztunk be új cache entry-t. Incremental cache nem szabad hogy változzon.