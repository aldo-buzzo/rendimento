# Documentazione REST Endpoints e Pagine HTML

Questo documento contiene una tabella di tutti gli URL REST presenti nei controller dell'applicazione e una tabella che associa ogni pagina HTML e gli eventi presenti nella pagina ai relativi URL REST.

## Tabella degli URL REST

| Controller | Endpoint | Metodo HTTP | Descrizione |
|------------|----------|-------------|-------------|
| **FrontendApiController** | `/api/frontend/app-info` | GET | Restituisce le informazioni sull'applicazione |
| **FrontendApiController** | `/api/frontend/enum/periodicita-cedole` | GET | Restituisce i valori dell'enum PeriodicitaCedole |
| **FrontendApiController** | `/api/frontend/enum/periodicita-bollo` | GET | Restituisce i valori dell'enum PeriodicitaBollo |
| **FrontendApiController** | `/api/frontend/enum/tipo-titolo` | GET | Restituisce i valori dell'enum TipoTitolo |
| **FrontendApiController** | `/api/frontend/simulazioni/latest` | GET | Recupera l'ultima simulazione per ogni titolo |
| **SimulazioneController** | `/api/simulazioni/calcola-rendimento` | POST | Calcola il rendimento di un titolo |
| **SimulazioneController** | `/api/simulazioni` | POST | Salva una simulazione |
| **SimulazioneController** | `/api/simulazioni/calcola-e-salva` | POST | Calcola e salva una simulazione in un'unica operazione |
| **SimulazioneController** | `/api/simulazioni` | GET | Recupera tutte le simulazioni |
| **SimulazioneController** | `/api/simulazioni/{id}` | GET | Recupera una simulazione per ID |
| **SimulazioneController** | `/api/simulazioni/{id}` | DELETE | Elimina una simulazione per ID |
| **SimulazioneController** | `/api/simulazioni/titolo/{idTitolo}` | GET | Recupera i dettagli di simulazione per un titolo specifico |
| **SimulazioneController** | `/api/simulazioni/titolo/{idTitolo}/all` | GET | Recupera tutte le simulazioni per un titolo specifico |
| **SimulazioneController** | `/api/simulazioni/{id}/ricalcola` | GET | Recupera i dettagli di una simulazione con i valori ricalcolati |
| **SimulazioneController** | `/api/simulazioni/calcola-rendimenti-tutti-titoli` | POST | Calcola il rendimento di tutti i titoli con scadenza futura |
| **SimulazioneController** | `/api/simulazioni/{id}/calcolo-dettagliato` | GET | Recupera i dati dettagliati di calcolo per una simulazione |
| **SimulazioneController** | `/api/simulazioni/trends/{periodo}` | GET | Recupera i dati di trend dei rendimenti per un determinato periodo |
| **SimulazioneController** | `/api/simulazioni/recupera-dati` | GET | Recupera i dati storici dei titoli BTP, elabora simulazioni e salva trend |
| **SimulazioneController** | `/api/simulazioni/recupera-dati/{idTitolo}` | GET | Recupera i dati storici di un titolo specifico, elabora simulazioni e salva trend |
| **AuthController** | `/login` | GET | Mostra la pagina di login |
| **AuthController** | `/registrazione` | GET | Mostra la pagina di registrazione |
| **AuthController** | `/api/auth/registrazione` | POST | Registra un nuovo utente |
| **AuthController** | `/api/auth/utente-corrente` | GET | Ottiene i dati dell'utente corrente |
| **AuthController** | `/api/auth/logout` | POST | Effettua il logout dell'utente corrente |
| **AuthController** | `/api/auth/cambia-password` | POST | Cambia la password dell'utente corrente |
| **AuthController** | `/api/auth/test-admin` | GET | Endpoint di test per verificare se l'utente admin esiste |
| **AuthController** | `/api/auth/create-test-admin` | GET | Endpoint di test per creare un nuovo utente admin |
| **BorsaItalianaController** | `/api/borsa-italiana/corso/btp/{isin}` | GET | Restituisce il corso ufficiale di un BTP dato l'ISIN |
| **BorsaItalianaController** | `/api/borsa-italiana/corso/bot/{isin}` | GET | Restituisce il corso ufficiale di un BOT dato l'ISIN |
| **BorsaItalianaController** | `/api/borsa-italiana/corso/{tipo}/{isin}` | GET | Restituisce il corso ufficiale di un titolo dato l'ISIN e il tipo |
| **BorsaItalianaController** | `/api/borsa-italiana/{tipo}/{isin}` | GET | Restituisce tutte le informazioni di un titolo dato l'ISIN e il tipo |
| **BorsaItalianaController** | `/api/borsa-italiana/lista/{tipoTitolo}` | GET | Recupera la lista dei titoli da Borsa Italiana in base al tipo |
| **BorsaItalianaController** | `/api/borsa-italiana/lista-paginata/{tipoTitolo}` | GET | Recupera la lista paginata dei titoli da Borsa Italiana in base al tipo |
| **TitoloController** | `/api/titolo` | GET | Recupera tutti i titoli dell'utente corrente |
| **TitoloController** | `/api/titolo/{id}` | GET | Trova un titolo per ID |
| **TitoloController** | `/api/titolo/isin/{codiceIsin}` | GET | Trova un titolo per codice ISIN |
| **TitoloController** | `/api/titolo/scadenza/{periodo}` | GET | Recupera i titoli con scadenza entro un determinato periodo |
| **TitoloController** | `/api/titolo/{id}` | DELETE | Elimina un titolo per ID |
| **TitoloController** | `/api/titolo/importa` | POST | Importa un titolo da Borsa Italiana dato il codice ISIN e il tipo |
| **TitoloController** | `/api/titolo` | POST | Crea un nuovo titolo o aggiorna un titolo esistente con lo stesso codice ISIN |
| **TitoloController** | `/api/titolo/importa-multipli` | POST | Importa più titoli contemporaneamente da Borsa Italiana |
| **TrendController** | `/api/trend/aggregati` | GET | Restituisce i trend aggregati dei tassi di interesse |
| **TrendController** | `/api/trend/periodo/{periodo}` | GET | Restituisce i trend aggregati dei tassi di interesse filtrati per periodo di scadenza |
| **TrendController** | `/api/trend/andamenti/{periodo}` | GET | Restituisce gli andamenti dei tassi di interesse nel tempo per un periodo specifico |
| **ProfiloCalcoloController** | `/api/profili-calcolo` | GET | Recupera tutti i profili di calcolo dell'utente autenticato |
| **ProfiloCalcoloController** | `/api/profili-calcolo/{id}` | GET | Recupera un profilo di calcolo specifico |
| **ProfiloCalcoloController** | `/api/profili-calcolo` | POST | Crea un nuovo profilo di calcolo |
| **ProfiloCalcoloController** | `/api/profili-calcolo/{id}` | PUT | Aggiorna un profilo di calcolo esistente |
| **ProfiloCalcoloController** | `/api/profili-calcolo/{id}/predefinito` | PUT | Imposta un profilo di calcolo come predefinito |
| **ProfiloCalcoloController** | `/api/profili-calcolo/{id}` | DELETE | Elimina un profilo di calcolo |

## Tabella delle Pagine HTML e degli Eventi

| Pagina HTML | Evento | Elemento | URL REST |
|-------------|--------|----------|----------|
| **login.html** | Caricamento pagina | - | `/api/auth/utente-corrente` |
| **login.html** | Submit form | Form di login | `/login` (gestito da Spring Security) |
| **registrazione.html** | Submit form | Form di registrazione | `/api/auth/registrazione` |
| **index.html** | Caricamento pagina | - | `/api/auth/utente-corrente` |
| **index.html** | Caricamento pagina | - | `/api/frontend/app-info` |
| **index.html** | Click | Pulsante logout | `/api/auth/logout` |
| **index.html** | Caricamento pagina | - | `/api/frontend/simulazioni/latest` |
| **dettaglio-simulazione.html** | Caricamento pagina | - | `/api/simulazioni/titolo/{idTitolo}` |
| **dettaglio-simulazione.html** | Caricamento pagina | - | `/api/simulazioni/titolo/{idTitolo}/all` |
| **dettaglio-simulazione.html** | Caricamento pagina | - | `/api/frontend/app-info` |
| **dettaglio-simulazione.html** | Click | Pulsante "Ricalcola" | `/api/simulazioni/calcola-rendimento` |
| **lista-titoli.html** | Caricamento pagina | - | `/api/titolo` |
| **lista-titoli.html** | Click | Pulsante "Aggiungi Titolo" | `/api/titolo` (POST) |
| **lista-titoli.html** | Click | Pulsante "Lista BTP" | `/api/borsa-italiana/lista/BTP` |
| **lista-titoli.html** | Click | Pulsante "Lista BOT" | `/api/borsa-italiana/lista/BOT` |
| **lista-titoli.html** | Click | Pulsante "Calcola Rendimenti" | `/api/simulazioni/calcola-rendimenti-tutti-titoli` |
| **lista-titoli.html** | Click | Pulsante "Elimina" | `/api/titolo/{id}` (DELETE) |
| **nuova-simulazione.html** | Submit form | Form nuova simulazione | `/api/simulazioni/calcola-e-salva` |
| **info-rendimenti.html** | Caricamento pagina | - | `/api/simulazioni/trends/{periodo}` |
| **info-titolo-rendimenti.html** | Caricamento pagina | - | `/api/simulazioni/{id}/calcolo-dettagliato` |
| **trends.html** | Caricamento pagina | - | `/api/trend/aggregati` |
| **trends-tassi.html** | Caricamento pagina | - | `/api/trend/andamenti/{periodo}` |
| **profili-utente.html** | Caricamento pagina | - | `/api/profili-calcolo` |
| **profili-utente.html** | Submit form | Form nuovo profilo | `/api/profili-calcolo` (POST) |
| **profili-utente.html** | Submit form | Form modifica profilo | `/api/profili-calcolo/{id}` (PUT) |
| **profili-utente.html** | Click | Pulsante "Imposta come predefinito" | `/api/profili-calcolo/{id}/predefinito` |
| **profili-utente.html** | Click | Pulsante "Elimina" | `/api/profili-calcolo/{id}` (DELETE) |