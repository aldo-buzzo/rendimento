-- Aggiorna la password dell'utente admin con l'hash BCrypt corrispondente a "password"
UPDATE utente 
SET password = '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG' 
WHERE username = 'admin';

-- Se l'utente admin non esiste, lo crea con la password "password"
INSERT INTO utente (username, password, email, nome, cognome, data_registrazione, is_system_user)
SELECT 'admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'admin@example.com', 'Admin', 'User', NOW(), true
WHERE NOT EXISTS (SELECT 1 FROM utente WHERE username = 'admin');
