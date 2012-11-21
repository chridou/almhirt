CREATE TABLE IF NOT EXISTS %tblname%
(	internalId SERIAL NOT NULL,
    id UUID UNIQUE,
	aggId UUID NOT NULL,
	aggVersion BIGINT NOT NULL,
	timestamp TIMESTAMP NOT NULL,
	payload TEXT NOT NULL);
	
CREATE INDEX IF NOT EXISTS idx_aggId ON %tblname%(aggId);
CREATE INDEX IF NOT EXISTS idx_aggIdAggVersion ON %tblname%(aggId, aggVersion);