#!/bin/bash

echo $POSTGRESQL_USER
echo $POSTGRESQL_DATABASE
echo $(pwd)
export SQL_DIR=$(pwd)/sql

psql -d $POSTGRESQL_DATABASE -w -c "grant all privileges on database ${POSTGRESQL_DATABASE} to ${POSTGRESQL_USER};"

envsubst < $SQL_DIR/create.sql > /tmp/create.sql

psql -d $POSTGRESQL_DATABASE -w < /tmp/create.sql
