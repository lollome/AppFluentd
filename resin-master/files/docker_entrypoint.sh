#!/bin/sh
set -e

# Non possiamo montare un secret folder da kubernetes sopra
# una cartella che contiene file dall'immagine conservando l'accessibilità
# di questi ultimi

# Ergo montiamo il secret folder da un'altra parte e copiamo i relativi file
# nella cartella contenente i file dall'immagine

mkdir -p /etc/polij

find /etc/polij.d -type f -print0 | \
	sort -z | \
	xargs -0 -n1 -I% cp -avf "%" /etc/polij/

exec "$@"
