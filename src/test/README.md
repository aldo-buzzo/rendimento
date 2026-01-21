# Ambiente di Test con Database H2 in Memoria

Questo documento descrive come utilizzare l'ambiente di test con database H2 in memoria per il progetto Rendimento.

## Configurazione

L'ambiente di test è già configurato per utilizzare un database H2 in memoria. La configurazione si trova nel file `src/test/resources/application-test.yml`.

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
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
        format_sql: true
    show-sql: true
  h2:
    console:
      enabled: true
      path: /h2-console
```

Questa configurazione:
- Utilizza un database H2 in memoria (`jdbc:h2:mem:testdb`)
- Configura Hibernate per creare le tabelle all'avvio e eliminarle alla chiusura (`ddl-auto: create-drop`)
- Abilita la console H2 per l'ispezione del database durante i test (`h2.console.enabled: true`)
- Mostra le query SQL eseguite durante i test (`show-sql: true`)

## Dipendenze

Le dipendenze necessarie sono già incluse nel file `pom.xml`:

```xml
<!-- Database H2 in memoria per i test -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

## Utilizzo

### Annotazioni per i Test

Per utilizzare il database H2 in memoria nei test, è necessario aggiungere le seguenti annotazioni alle classi di test:

```java
@SpringBootTest
@ActiveProfiles("test") // Utilizza il profilo di test con il database H2
@Transactional
public class MioTest {
    // ...
}
```

- `@SpringBootTest`: Carica il contesto Spring completo
- `@ActiveProfiles("test")`: Utilizza il profilo "test" che è configurato per usare il database H2
- `@Transactional`: Ogni test viene eseguito in una transazione che viene rollback alla fine

### Creazione di Dati di Test

Per facilitare la creazione di dati di test, è disponibile la classe `TestDataBuilder` che fornisce metodi per creare utenti e titoli di test:

```java
// Crea un utente di test
Utente utente = TestDataBuilder.createDefaultUtente("1");
utenteRepository.save(utente);

// Crea un titolo di test
Titolo titolo = TestDataBuilder.createDefaultTitolo("1", utente);
titoloRepository.save(titolo);

// Crea un insieme di dati di test
List<Utente> utenti = TestDataBuilder.createTestData(utenteRepository, titoloRepository, 2, 3);
// Crea 2 utenti, ciascuno con 3 titoli
```

### Esempi di Test

#### Test di Repository

Vedere `TitoloRepositoryIntegrationTest.java` per un esempio di test di repository che utilizza il database H2 in memoria.

#### Test di Service

Vedere `TitoloServiceTest.java` per un esempio di test di service che utilizza il database H2 in memoria.

#### Test di Controller

Vedere `TitoloControllerTest.java` per un esempio di test di controller che utilizza il database H2 in memoria e MockMvc per simulare le richieste HTTP.

## Consigli per i Test

1. **Isolamento dei Test**: Ogni test dovrebbe essere indipendente dagli altri. Utilizzare `@BeforeEach` per inizializzare i dati di test prima di ogni test.

2. **Transazioni**: L'annotazione `@Transactional` assicura che ogni test venga eseguito in una transazione che viene rollback alla fine, in modo che i dati creati durante un test non influenzino gli altri test.

3. **Dati di Test**: Utilizzare la classe `TestDataBuilder` per creare dati di test in modo coerente e riutilizzabile.

4. **Verifica dei Risultati**: Utilizzare le asserzioni di JUnit per verificare i risultati dei test. Ad esempio:
   ```java
   assertEquals(expected, actual);
   assertTrue(condition);
   assertNotNull(object);
   ```

5. **Test di Integrazione**: I test che utilizzano il database H2 in memoria sono test di integrazione, non test unitari. Per i test unitari, considerare l'utilizzo di mock.

## Risoluzione dei Problemi

### Errore: "Table not found"

Se si verifica un errore "Table not found", assicurarsi che:
- L'annotazione `@ActiveProfiles("test")` sia presente sulla classe di test
- Il file `application-test.yml` sia configurato correttamente
- L'entità JPA sia annotata correttamente con `@Entity` e `@Table`

### Errore: "No qualifying bean of type"

Se si verifica un errore "No qualifying bean of type", assicurarsi che:
- L'annotazione `@SpringBootTest` sia presente sulla classe di test
- Il componente che si sta cercando di iniettare sia annotato correttamente (ad esempio, `@Repository`, `@Service`, `@Controller`)
