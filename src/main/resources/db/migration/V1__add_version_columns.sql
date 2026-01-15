-- Script SQL per aggiungere le colonne version alle tabelle per il controllo ottimistico della concorrenza

-- Aggiunta della colonna version alla tabella titolo
ALTER TABLE titolo ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;

-- Aggiunta della colonna version alla tabella simulazione
ALTER TABLE simulazione ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;

-- Aggiunta della colonna version alla tabella app_metadata
ALTER TABLE app_metadata ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;

-- Nota: Questo script è fornito per riferimento e per l'esecuzione manuale se necessario.
-- Se l'applicazione è configurata con spring.jpa.hibernate.ddl-auto=update,
-- Hibernate aggiungerà automaticamente queste colonne al riavvio dell'applicazione.
