-- Aggiunta dei nuovi campi alla tabella simulazione per il calcolo avanzato dei rendimenti

ALTER TABLE simulazione
ADD COLUMN nominale DECIMAL(10,2),
ADD COLUMN prezzo_riferimento_bollo DECIMAL(10,4),
ADD COLUMN capitale_investito DECIMAL(10,2),
ADD COLUMN capitale_con_commissioni DECIMAL(10,2),
ADD COLUMN cedole_nette_annue DECIMAL(10,4),
ADD COLUMN guadagno_netto_senza_costi DECIMAL(10,4),
ADD COLUMN rendimento_senza_costi DECIMAL(10,6),
ADD COLUMN rendimento_con_commissioni DECIMAL(10,6),
ADD COLUMN rendimento_con_bollo_mensile DECIMAL(10,6),
ADD COLUMN bollo_totale_mensile DECIMAL(10,4);

COMMENT ON COLUMN simulazione.nominale IS 'Valore nominale dell''investimento';
COMMENT ON COLUMN simulazione.prezzo_riferimento_bollo IS 'Prezzo utilizzato per il calcolo del bollo';
COMMENT ON COLUMN simulazione.capitale_investito IS 'Capitale effettivamente investito';
COMMENT ON COLUMN simulazione.capitale_con_commissioni IS 'Capitale investito più commissioni';
COMMENT ON COLUMN simulazione.cedole_nette_annue IS 'Cedole annue al netto della tassazione';
COMMENT ON COLUMN simulazione.guadagno_netto_senza_costi IS 'Guadagno netto prima di commissioni e bollo';
COMMENT ON COLUMN simulazione.rendimento_senza_costi IS 'Rendimento prima di commissioni e bollo';
COMMENT ON COLUMN simulazione.rendimento_con_commissioni IS 'Rendimento al netto delle commissioni';
COMMENT ON COLUMN simulazione.rendimento_con_bollo_mensile IS 'Rendimento con bollo calcolato mensilmente';
COMMENT ON COLUMN simulazione.bollo_totale_mensile IS 'Importo totale del bollo calcolato mensilmente';
