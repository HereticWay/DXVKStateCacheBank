*Read this in other languages: [English](README.md), [Hungarian](README.hu.md).*

# A projektről
Ez a projekt a "DXVK pipeline state cache" fájlok tárolását és automatikus összevonását
hivatott megoldani alkalmazásonként egyetlen növekvő "incremental cache file"-ba, amelyet
később több eszköz között is meg lehet osztani.

Az ötlet az, hogy különböző felhasználók regisztrálhatnak az API-hoz és megoszthatják
az alkalmazásuk által generált "cache" fájljaikat azon keresztül, melyet a backend
eltárol és a dxvk-cache-tool nevű programmal az alkalmazáshoz tartozó növekvő "cache"
fájllal összevonja. Az aktuális "cache" fájlt bárki bármikor letöltheti, regisztráció
nem szükséges hozzá.

# Végpontok
A végpontok láthatóak program futása közben a [**Swagger dokumentációban**](http://127.0.0.1:8080/swagger-ui.html).

# A program futtatása
Jelenleg a programom csak Dockerrel, vagy Linux alatt futtatható, mert a
dxvk-cache-tool alkalmazásnak nincs natív Windows kiadása.

## Futtatás Dockerben 
```shell
# Navigálja a projekt könyvtárába és futtasd a következő utasítást:
$ docker compose up
```

## Futtatás Linux alól
A program sikeres futtatásának feltétele:
- docker
- docker compose plugin (általában ez a docker-rel együtt automatikusan települ)
- [**dxvk-cache-tool**](https://github.com/DarkTigrus/dxvk-cache-tool/releases/tag/v1.1.2)
megléte **futtatási jogokkal** egy **PATH könyvtárban**
- egy **PostgreSQL adatbázis szerver** (fellőhető docker-ből, lásd [A szükséges PostgreSQL szerver elindítása](#a-szükséges-postgresql-szerver-elindítása))
- egy **futtatási profil** kiválasztása.

### A szükséges PostgreSQL szerver elindítása
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
- A program ezután a szokásos módon indítható.

### Tesztek futtatása Intellij IDEA-val
- Indítsd el a PostgreSQL szervert (lásd: [A szükséges PostgreSQL szerver elindítása](#a-szükséges-postgresql-szerver-elindítása))
- Töltsd be a projektet maven projektként
- Adj hozzá új tesztkonfigurációt: **"Run/DebugConfigurations"->"Add new configuration"->"JUnit"
- Állítsd be a következőképpen:<br>
Profilt itt nem szükséges megadni!<br>
![Cannot load the picture](https://github.com/HereticWay/DXVKStateCacheBank/raw/docs/docs/intellij-test-configuration.png)
- Ha a dxvk-cache-tool **nem** egy PATH könytvárban van, akkor az
    **"Environment Variables"** mezőben meg kell adni még a
    letöltött program könyvtárát PATH könyvtárként, pl.: *"PATH=$PATH:/path/to/dxvk-cache-tool-directory"*

## Futtatás Windows alól
A futtatás Windows alól csak **WSL Docker konténerben** támogatott.<br>
Lásd: [*A program futtatása*](#a-program-futtatása), [*Futtatás Dockerben*](#futtatás-dockerben)
