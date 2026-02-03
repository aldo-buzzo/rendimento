-- Rimozione dei campi rendimentoLordo e rendimentoNettoCedole dalla tabella simulazione
-- Questi campi sono stati sostituiti rispettivamente da rendimentoSenzaCosti e rendimentoConCommissioni

ALTER TABLE simulazione DROP COLUMN IF EXISTS rendimento_lordo;
ALTER TABLE simulazione DROP COLUMN IF EXISTS rendimento_netto_cedole;

-- Nota: l'uso di IF EXISTS previene errori se i campi sono già stati rimossi
-- o se i nomi delle colonne nel database sono diversi