#!/bin/bash
psql -c "create user bssunituser with password 'bssunituser';" -U postgres;
psql -c "create user bssunituser with password 'bssunituser';" -U postgres;
psql -c "create database bssunittests with owner bssunituser TEMPLATE=template0 ENCODING='UTF8'" -U postgres;
psql -c "create schema bssunittests" -U postgres;
psql -c "grant all privileges on schema bssunittests to bssunituser" -U postgres;
