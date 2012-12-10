CREATE TABLE IF NOT EXISTS %tblname%
(	internalId BIGINT IDENTITY,
    id UUID UNIQUE,
	aggId UUID NOT NULL,
	aggVersion BIGINT NOT NULL,
	channel VARCHAR(16) NOT NULL,
	timestamp TIMESTAMP NOT NULL,
	payload CLOB NOT NULL);
	
CREATE INDEX IF NOT EXISTS idx_aggId ON %tblname%(aggId);
CREATE INDEX IF NOT EXISTS idx_aggIdAggVersion ON %tblname%(aggId, aggVersion);