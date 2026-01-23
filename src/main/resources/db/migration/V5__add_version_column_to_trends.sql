-- Script SQL per aggiungere la colonna version alla tabella trends per il controllo ottimistico della concorrenza

-- Aggiunta della colonna version alla tabella trends
ALTER TABLE trends ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;

-- Nota: Questo script è fornito per riferimento e per l'esecuzione manuale se necessario.
-- Se l'applicazione è configurata con spring.jpa.hibernate.ddl-auto=update,
-- Hibernate aggiungerà automaticamente questa colonna al riavvio dell'applicazione.
