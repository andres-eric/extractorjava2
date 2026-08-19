#!/bin/bash
set -e

# Nombre del proyecto para logs
PROYECTO="SqlExtractor"

echo "📥 Actualizando código desde Bitbucket..."
git pull origin $(git branch --show-current)

# 1. Detectar rama
CURRENT_BRANCH=$(git branch --show-current)
export SPRING_PROFILES_ACTIVE=$([ "$CURRENT_BRANCH" = "main" ] && echo "prod" || echo "dev")

echo "🚀 Desplegando $PROYECTO en puerto 8080..."
echo "📍 Rama: $CURRENT_BRANCH | Perfil: $SPRING_PROFILES_ACTIVE"

# 2. Reemplazar SOLO el contenedor nuevo
# --build: recompila tu código Java
# --force-recreate: asegura que se levante la versión más reciente
# app: es el nombre del servicio definido en tu docker-compose.yml
docker compose up -d --build --force-recreate app

echo "✅ Despliegue de $PROYECTO finalizado."
