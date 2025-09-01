<img width="300" height="512" alt="DigitAIR" src="https://github.com/user-attachments/assets/1e9a23a8-a4c2-46bd-b907-5d0fec6c5426" />

## Descrizione del progetto
Il controllo della qualità dell’aria rappresenta una delle priorità ambientali più importanti del nostro tempo. Con l’intensificarsi dell’urbanizzazione, 
dell’industrializzazione e dei cambiamenti climatici, il monitoraggio preciso e tempestivo degli inquinanti atmosferici è diventato fondamentale per la tutela della
salute pubblica e la sostenibilità ambientale.  
Per affrontare questa sfida, l’evoluzione tecnologica degli ultimi decenni ha reso possibile lo sviluppo di sistemi di monitoraggio più capillari e
in grado di fornire i dati in tempo reale. Nel contesto territoriale pugliese, il progetto **"DigitAIR"** è stato sviluppato per superare i limiti dei tradizionali 
sistemi di monitoraggio, grazie all’implementazione di tecnologie innovative come l’Internet of Things (IoT) e l’Intelligenza Artificiale (AI).
Il progetto è stato concepito con un’architettura che consente la realizzazione di una rete di monitoraggio ad alta densità, offrendo una soluzione integrata, 
intuitiva, economicamente sostenibile e capace di garantire la massima trasparenza dei dati.  
Un aspetto fondamentale della soluzione proposta riguarda lo sviluppo di un sistema di visualizzazione dei dati accessibile a tutti. La piattaforma, caratterizzata da
un’interfaccia intuitiva e user-friendly, offre strumenti di visualizzazione intuitivi, mappe interattive per ogni inquinante e funzionalità di analisi storica
dei dati, rendendo le informazioni ambientali comprensibili e utilizzabili anche da utenti non specializzati. Particolare attenzione è stata dedicata al settore
accademico e alla pubblica amministrazione: per gli studenti universitari, i ricercatori e gli enti pubblici non solo è garantito l’accesso gratuito dei dati
raccolti consultando le mappe, ma anche l’utilizzo di funzionalità avanzate di simulazione. Queste includono modelli predittivi per valutare l’impatto 
attuale e futuro dell’inquinamento sulla salute e strumenti per stimare la riduzione degli inquinanti in seguito ad interventi di forestazione urbana.

---

## Architettura del sistema
Il sistema è progettato con un approccio modulare per garantire robustezza,scalabilità e manutenibilità, suddividendo le responsabilità tra Edge Device,
Front-end Web e Back-end, il quale è stato sviluppato secondo un’architettura a microservizi per favorire indipendenza dei componenti.

<img width="2083" height="915" alt="Architettura" src="https://github.com/user-attachments/assets/7fe33895-a981-4fd1-a204-28a7747bb4cb" />  

Le principali componenti in cui si articola in sistema sono:  
#### User Service
Il microservizio User Service gestisce gli utenti del sistema, occupandosi di registrazione, autenticazione, gestione degli account e dei domini affiliati 
da parte dell’Admin. Utilizza token JWT per garantire un accesso sicuro alle risorse e interagisce con il Notification Service per l’invio di email.

#### Notification
Il microservizio gestisce l’invio delle email e comunica con il microservizio UserService tramite il protocollo AMQP, implementato con RabbitMQ.

#### Smartbox
La Smartbox è un dispositivo basato su ESP32 DevKit-C con sensori per il rilevamento degli inquinanti. Il microcontrollore, connesso alla rete, pubblica 
sul relativo topic le misure al minuto 50 di ogni ora affichè vengano salvate su un microservizio registrato al broker MQTT e gestisce eventuali disconnessioni
o malfunzionamenti tramite messaggi e API dedicate.

#### Device Indexer
Il microservizio Device Indexer mantiene il registro dei dispositivi, traccia lo stato di connessione, riceve e inoltra le misurazioni al Data Processor. 
Inoltre espone API per la gestione dei dispositivi e la loro disconnessione in caso di anomalie.

#### Data Processor
Il Data Processor si occupa direttamente del post processing dei dati, della generazione delle immagini e della gestione delle simulazioni. Esegue interpolazioni 
per generare mappe di distribuzione degli inquinanti e supporta simulazioni ambientali e sanitarie, fornendo inoltre API per l’accesso alle immagini e metriche.

#### Client per simulazioni
Un client Python simula l’invio concorrente di misurazioni da più dispositivi, generando dati realistici basati sulla distribuzione geografica delle province pugliesi.

#### Gateway
I microservizi sono stati connessi al frontend utilizzando KrakenD, un gateway con configurazione basata su JSON

#### Frontend
L’interfaccia web permette agli utenti di visualizzare le mappe degli inquinanti sull’intero territorio pugliese o, a scelta, solo su Lecce. In base al ruolo, gli utenti
possono eseguire simulazioni, mentre la gestione dell’applicazione è riservata esclusivamente all’Admin.

---

Di seguito viene fornita una descrizione dettagliata della componente implementata nella repository corrente.
## User Service
Il microservizio si interessa della gestione degli utenti all’interno del sistema, prevedendo l’esistenza di tre attori: Admin, Regular User e Researcher. Nello
specifico i diversi ambiti di cui si interessa sono:
- registrazione di un nuovo utente
- autenticazione e autorizzazione
- gestione profilo
- gestione dei domini accreditati
- interazione con Notification
#### Registrazione
Durante la fase di registrazione, l’utente deve compilare un form indicando, oltre al nome e al cognome, anche l’indirizzo email e una password, che diventeranno 
le credenziali necessarie per accedere al sistema. L’utente può scegliere di registrarsi come “Utente Affiliato”, ottenendo così l’accesso a funzionalità
riservate a questa categoria. Tuttavia, la richiesta verrà approvata solo se il dominio dell’email utilizzata per la registrazione risulta tra quelli accreditati. 
A questo punto, l’utente riceve un’email di conferma contenente un codice OTP, da inserire nell’apposita schermata per verificare l’account e completare così la procedura di registrazione.
#### Autenticazione e Autorizzazione
L’utente, specificando email e password, può effettuare l’accesso al sistema. In questa fase avviene la generazione del token JWT, una stringa in base64 essenziale per gestire 
l’accesso a risorse protette, per le quali è richiesta un’autenticazione/autorizzazione. Per accedere a queste risorse è necessario effettuare una richiesta HTTP inserendo un 
campo header “Authorization” nella richiesta, il cui valore sarà “Bearer [token]”. Il token che viene utilizzato è infatti di tipo Bearer, “al portatore”; ciò significa che chiunque ne sia in possesso
si può autenticare con le informazioni contenute all’interno di questo. In fase di autenticazione l’utente può richiedere il reset della password specificando l’email nell’apposita sezione.
#### Gestione Profilo
L’utente autenticato può accedere alle funzionalità riservate in base al proprio ruolo e gestire il proprio account, modificando nome, cognome, email, tipologia di utente (soggetta a verifica dei domini 
affiliati) e password, oltre a poter richiedere l’eliminazione dell’account.
