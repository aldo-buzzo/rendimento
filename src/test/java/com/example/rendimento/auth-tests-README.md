# Test di Autenticazione con Database H2 in Memoria

Questo documento descrive i test di autenticazione implementati per il progetto Rendimento, che utilizzano un database H2 in memoria per verificare le funzionalità di registrazione, login e gestione degli utenti.

## Struttura dei Test

Sono stati implementati due tipi di test per l'autenticazione:

1. **Test del Servizio (UtenteServiceTest)**: Verifica la logica di business del servizio UtenteService.
2. **Test del Controller (AuthControllerTest)**: Verifica le API REST esposte dal controller AuthController.

## Funzionalità Testate

### Test del Servizio (UtenteServiceTest)

- **Registrazione utente**: Verifica che un nuovo utente possa essere registrato correttamente.
- **Registrazione con username esistente**: Verifica che non sia possibile registrare un utente con un username già in uso.
- **Registrazione con email esistente**: Verifica che non sia possibile registrare un utente con un'email già in uso.
- **Ricerca utente per username**: Verifica che un utente possa essere trovato tramite il suo username.
- **Ricerca utente per email**: Verifica che un utente possa essere trovato tramite la sua email.
- **Cambio password**: Verifica che un utente possa cambiare la propria password.
- **Cambio password con vecchia password errata**: Verifica che non sia possibile cambiare la password se la vecchia password è errata.
- **Aggiornamento dati utente**: Verifica che i dati di un utente possano essere aggiornati.
- **Eliminazione utente**: Verifica che un utente possa essere eliminato.

### Test del Controller (AuthControllerTest)

- **Registrazione utente (API)**: Verifica che l'API di registrazione funzioni correttamente.
- **Registrazione con username esistente (API)**: Verifica che l'API di registrazione restituisca un errore se l'username è già in uso.
- **Registrazione con email esistente (API)**: Verifica che l'API di registrazione restituisca un errore se l'email è già in uso.
- **Ottenimento utente corrente (API)**: Verifica che l'API per ottenere l'utente corrente funzioni correttamente.
- **Cambio password (API)**: Verifica che l'API per cambiare la password funzioni correttamente.
- **Cambio password con vecchia password errata (API)**: Verifica che l'API per cambiare la password restituisca un errore se la vecchia password è errata.
- **Logout (API)**: Verifica che l'API di logout funzioni correttamente.
- **Visualizzazione pagina di login**: Verifica che la pagina di login venga visualizzata correttamente.
- **Visualizzazione pagina di registrazione**: Verifica che la pagina di registrazione venga visualizzata correttamente.

## Configurazione

I test utilizzano un database H2 in memoria configurato nel file `application-test.yml`. Questo database viene creato e distrutto automaticamente durante l'esecuzione dei test, garantendo che ogni test parta da uno stato pulito e prevedibile.

Le annotazioni chiave utilizzate nei test sono:

- `@SpringBootTest`: Carica l'intero contesto Spring per i test.
- `@ActiveProfiles("test")`: Utilizza il profilo "test" che configura il database H2 in memoria.
- `@Transactional`: Garantisce che ogni test venga eseguito in una transazione separata che viene rollback alla fine del test.
- `@AutoConfigureMockMvc`: Configura MockMvc per i test del controller.
- `@WithMockUser`: Simula un utente autenticato per i test che richiedono autenticazione.

## Come Eseguire i Test

Per eseguire i test, è possibile utilizzare Maven:

```bash
mvn test
```

Oppure eseguire i test direttamente dall'IDE, facendo clic sul pulsante "Run" accanto alla classe di test o al metodo di test specifico.

## Estensione

Per estendere questi test o aggiungere nuovi test di autenticazione:

1. Aggiungere nuovi metodi di test alle classi esistenti per testare funzionalità specifiche.
2. Creare nuove classi di test per testare altri aspetti dell'autenticazione, come la gestione dei ruoli o le autorizzazioni.
3. Utilizzare le annotazioni appropriate per configurare il contesto di test e simulare l'autenticazione.

## Note

- I test utilizzano `@BeforeEach` per pulire il repository prima di ogni test, garantendo che ogni test parta da uno stato pulito.
- I test del controller utilizzano MockMvc per simulare le richieste HTTP e verificare le risposte.
- I test del servizio utilizzano direttamente il servizio UtenteService per verificare la logica di business.
