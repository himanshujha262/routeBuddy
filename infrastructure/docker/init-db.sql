-- Enable PostGIS and UUID Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis";

-- Verify PostGIS Version
SELECT PostGIS_Full_Version();
