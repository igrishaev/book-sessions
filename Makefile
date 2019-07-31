
repl:
	lein repl

docker-run:
	docker-compose up

docker-clear:
	rm -rf /tmp/docker/book

docker-psql:
	docker run -it --rm --network book_default postgres psql -h 172.18.0.2 -U book book

psql:
	psql -h 127.0.0.1 -U book book
