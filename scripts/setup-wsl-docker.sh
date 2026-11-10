#!/usr/bin/env bash
# Installe le moteur Docker dans Ubuntu WSL2 et démarre PostgreSQL + Mailpit pour NovaCore.
# À lancer DANS WSL : wsl -d Ubuntu puis  bash setup-wsl-docker.sh
# Demande le mot de passe sudo une fois. Ne touche pas à Windows ni à Device Guard.
set -euo pipefail

echo "==> Installation du moteur Docker (docker.io)"
sudo apt-get update -qq
sudo apt-get install -y -qq docker.io

echo "==> Autoriser l'utilisateur courant à utiliser Docker sans sudo"
sudo usermod -aG docker "$USER"

echo "==> Démarrage du démon Docker (WSL n'a pas systemd par défaut)"
sudo service docker start || sudo dockerd >/tmp/dockerd.log 2>&1 &
sleep 3

echo "==> PostgreSQL 17 (base/user/mot de passe : novacore)"
sudo docker rm -f novacore-pg 2>/dev/null || true
sudo docker run -d --name novacore-pg --restart unless-stopped \
  -e POSTGRES_DB=novacore -e POSTGRES_USER=novacore -e POSTGRES_PASSWORD=novacore \
  -p 5432:5432 -v novacore-pgdata:/var/lib/postgresql/data \
  postgres:17-alpine

echo "==> Mailpit (capture des e-mails de dev)"
sudo docker rm -f novacore-mail 2>/dev/null || true
sudo docker run -d --name novacore-mail --restart unless-stopped \
  -p 1025:1025 -p 8025:8025 axllent/mailpit

echo
echo "Terminé. Conteneurs actifs :"
sudo docker ps --format '  {{.Names}}  {{.Image}}  {{.Status}}  {{.Ports}}'
echo
echo "PostgreSQL est joignable depuis Windows sur localhost:5432"
echo "Interface Mailpit : http://localhost:8025"
