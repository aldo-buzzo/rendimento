-- Aggiunta dei campi per il bollo annuale alla tabella simulazione

ALTER TABLE simulazione
ADD COLUMN rendimento_con_bollo_annuale DECIMAL(10,6),
ADD COLUMN bollo_totale_annuale DECIMAL(10,4);

COMMENT ON COLUMN simulazione.rendimento_con_bollo_annuale IS 'Rendimento con bollo calcolato annualmente';
COMMENT ON COLUMN simulazione.bollo_totale_annuale IS 'Importo totale del bollo calcolato annualmente';
