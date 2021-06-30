
repl:
	lein repl

docker-up:
	docker-compose up

docker-clear:
	rm -rf /tmp/docker/book

psql:
	psql -h 127.0.0.1 -U book book
