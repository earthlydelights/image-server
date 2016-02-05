set HTTP_PROXY_HOST=proxy
set HTTP_PROXY_PORT=8080
set HTTPS_PROXY_HOST=proxy
set HTTPS_PROXY_PORT=8080
set HTTP_NON_PROXY_HOSTS="localhost"

rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem 

rem ./neo.sh list-dbms -a p1630844092trial -h hanatrial.ondemand.com -u p1630844092 -p USUAL_PASSWORD
rem ./neo.sh display-db-info -a p1630844092trial -h hanatrial.ondemand.com -u p1630844092 -p USUAL_PASSWORD -i imageserverdb
rem ./neo.sh open-db-tunnel -a p1630844092trial -h hanatrial.ondemand.com -u p1630844092 -p USUAL_PASSWORD -i imageserverdb
rem C:\_\hdbclient
rem hdbsql ->
rem 	\c  -n localhost:30015 -u system
rem 	create user bigchief password USUAL_PASSWORD
rem 	grant content_admin to bigchief with admin option
rem ./neo.sh bind-db -h hanatrial.ondemand.com -a p1630844092trial -b imageserver -i imageserverdb --db-user bigchief --db-password USUAL_PASSWORD# -u p1630844092 -p USUAL_PASSWORD
rem bigchief 1941866044 = USUAL_PASSWORD

rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem 

rem ./neo.sh list-dbms -a p1941866044trial -h hanatrial.ondemand.com -u p1941866044 -p USUAL_PASSWORD
rem ./neo.sh display-db-info -a p1941866044trial -h hanatrial.ondemand.com -u p1941866044 -p USUAL_PASSWORD -i imageserverdb
rem ./neo.sh open-db-tunnel -a p1941866044trial -h hanatrial.ondemand.com -u p1941866044 -p USUAL_PASSWORD -i imageserverdb
rem C:\_\hdbclient
rem hdbsql ->
rem 	\c  -n localhost:30015 -u system
rem 	create user bigchief password USUAL_PASSWORD
rem 	grant content_admin to bigchief with admin option
rem ./neo.sh bind-db -h hanatrial.ondemand.com -a p1941866044trial -b imageserver -i imageserverdb --db-user bigchief --db-password USUAL_PASSWORD -u p1941866044 -p USUAL_PASSWORD
rem bigchief 1941866044 = USUAL_PASSWORD

rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem rem 

rem ./neo.sh list-dbms -a p1941872433trial -h hanatrial.ondemand.com -u p1941872433 -p USUAL_PASSWORD
rem ./neo.sh display-db-info -a p1941872433trial -h hanatrial.ondemand.com -u p1941872433 -p USUAL_PASSWORD -i imageserverdb
rem ./neo.sh open-db-tunnel -a p1941872433trial -h hanatrial.ondemand.com -u p1941872433 -p USUAL_PASSWORD# -i imageserverdb
rem C:\_\hdbclient
rem hdbsql ->
rem 	\c  -n localhost:30015 -u system
rem 	create user bigchief password USUAL_PASSWORD
rem 	grant content_admin to bigchief with admin option
rem ./neo.sh bind-db -h hanatrial.ondemand.com -a p1941872433trial -b imageserver -i imageserverdb --db-user bigchief --db-password USUAL_PASSWORD% -u p1941872433 -p USUAL_PASSWORD
rem bigchief 1941872433 = USUAL_PASSWORD%

