-- Modifica dei campi decimal nella tabella profilo_calcolo per aumentare la precisione a 6 decimali
ALTER TABLE profilo_calcolo 
    ALTER COLUMN percentuale_bollo TYPE DECIMAL(7,6),
    ALTER COLUMN percentuale_bollo SET DEFAULT 0.0020,
    ALTER COLUMN commissione_btp TYPE DECIMAL(7,6),
    ALTER COLUMN commissione_btp SET DEFAULT 0.0009,
    ALTER COLUMN commissione_bot_120gg TYPE DECIMAL(7,6),
    ALTER COLUMN commissione_bot_120gg SET DEFAULT 0.0009,
    ALTER COLUMN commissione_bot_240gg TYPE DECIMAL(7,6),
    ALTER COLUMN commissione_bot_240gg SET DEFAULT 0.0009,
    ALTER COLUMN commissione_bot_oltre TYPE DECIMAL(7,6),
    ALTER COLUMN commissione_bot_oltre SET DEFAULT 0.0009,
    ALTER COLUMN commissione_cct TYPE DECIMAL(7,6),
    ALTER COLUMN commissione_cct SET DEFAULT 0.0009,
    ALTER COLUMN commissione_ctz TYPE DECIMAL(7,6),
    ALTER COLUMN commissione_ctz SET DEFAULT 0.0009;