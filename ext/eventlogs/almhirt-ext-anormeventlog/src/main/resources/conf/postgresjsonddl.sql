CREATE TABLE IF NOT EXISTS %tblname% 
(	internalId SERIAL PRIMARY KEY,
    id UUID UNIQUE,
	aggId UUID NOT NULL,
	aggVersion BIGINT NOT NULL,
	channel VARCHAR(16) NOT NULL,
	timestamp TIMESTAMP NOT NULL,
	payload TEXT NOT NULL);
	
	
CREATE OR REPLACE FUNCTION inline_0() RETURNS void AS '

DECLARE v_exists integer;
BEGIN
SELECT INTO v_exists count(*) FROM pg_class WHERE relname = ''%tblname%_aggId_idx'';
IF v_exists = 0 THEN
CREATE INDEX %tblname%_aggId_idx ON %tblname%(aggId);
END IF;
END;' LANGUAGE 'plpgsql';


SELECT inline_0();
DROP function inline_0();

CREATE OR REPLACE FUNCTION inline_1() RETURNS void AS '

DECLARE v_exists integer;
BEGIN
SELECT INTO v_exists count(*) FROM pg_class WHERE relname = ''%tblname%_aggIdAggVersion_idx'';
IF v_exists = 0 THEN
CREATE INDEX %tblname%_aggIdAggVersion_idx ON %tblname%(aggId, aggVersion);
END IF;
END;' LANGUAGE 'plpgsql';


SELECT inline_1();
DROP function inline_1();


select 1



