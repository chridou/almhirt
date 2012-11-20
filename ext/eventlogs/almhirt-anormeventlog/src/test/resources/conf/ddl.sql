CREATE TABLE IF NOT EXISTS eventlog
(	id UUID PRIMARY KEY,
	version BIGINT,
	timestamp TIMESTAMP,
	payload CLOB)