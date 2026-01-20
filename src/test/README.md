# Ambiente di Test con Database H2 in Memoria

Questo documento descrive la configurazione dell'ambiente di test che utilizza un database H2 in memoria per i test JUnit del progetto Rendimento.

## Configurazione

L'ambiente di test è configurato per utilizzare un database H2 in memoria invece del database PostgreSQL utilizzato in produzione. Questo approccio offre diversi vantaggi:

1. **Isolamento**: I test non influenzano il database di produzione
2. **Velocità**: Il database in memoria è molto più veloce di un database su disco
3. **Consistenza**: Ogni esecuzione di test parte da uno stato pulito e prevedibile
4. **Portabilità**: Non è necessario configurare un database esterno per eseguire i test

## File di Configurazione

### 1. Dipendenze Maven (pom.xml)

Le seguenti dipendenze sono state aggiunte al file `pom.xml`:

```xml
<!-- Dipendenze per i test -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- Database H2 in memoria per i test -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

### 2. Configurazione del Database di Test (application-test.yml)

Il file `src/test/resources/application-test.yml` contiene la configurazione del database H2 in memoria:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password: 
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
        format_sql: true
  h2:
    console:
      enabled: true
```

## Classi di Test

Sono state create due classi di test di esempio che mostrano come utilizzare il database H2 in memoria:

### 1. TitoloRepositoryTest

Questa classe testa le operazioni CRUD del repository `TitoloRepository`. Utilizza l'annotazione `@SpringBootTest` e `@ActiveProfiles("test")` per caricare la configurazione del database H2 in memoria.

### 2. TitoloServiceTest

Questa classe testa un servizio di esempio che utilizza il repository `TitoloRepository`. Mostra come testare la logica di business con un database in memoria.

## Come Eseguire i Test

Per eseguire i test, è possibile utilizzare Maven:

```bash
mvn test
```

Oppure eseguire i test direttamente dall'IDE.

## Buone Pratiche

1. **Utilizzare `@Transactional`**: Questa annotazione garantisce che ogni test venga eseguito in una transazione separata che viene rollback alla fine del test, mantenendo il database in uno stato pulito.

2. **Utilizzare `@ActiveProfiles("test")`**: Questa annotazione assicura che venga utilizzata la configurazione del database H2 in memoria.

3. **Inizializzare i dati di test nel metodo `@BeforeEach`**: Questo garantisce che ogni test abbia i dati necessari in uno stato prevedibile.

4. **Utilizzare asserzioni JUnit per verificare i risultati**: Le asserzioni JUnit forniscono messaggi di errore chiari in caso di fallimento dei test.

## Estensione

Per estendere questa configurazione ad altri test:

1. Creare nuove classi di test nella directory `src/test/java`
2. Annotare le classi con `@SpringBootTest` e `@ActiveProfiles("test")`
3. Utilizzare l'annotazione `@Transactional` per garantire il rollback delle transazioni
4. Iniettare i repository o i servizi necessari con `@Autowired`
5. Implementare i metodi di test con le asserzioni JUnit
