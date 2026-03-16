.PHONY: up down build logs restart

up:
	BUILDX_BAKE_ENTITLEMENTS_FS=0 docker compose up --build -d

down:
	docker compose down

build:
	BUILDX_BAKE_ENTITLEMENTS_FS=0 docker compose build

logs:
	docker compose logs -f

restart:
	docker compose restart
