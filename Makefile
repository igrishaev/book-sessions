
repl:
	lein repl

docker-run:
	docker-compose up

docker-clear:
	rm -rf /tmp/docker/book

docker-psql:
	psql -h 127.0.0.1 -U book book
