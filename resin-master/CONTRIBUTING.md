# Come rilasciare Resin
Per rilasciare una nuova versione dell'immagine Resin
è necessario seguire i seguenti passi.

## 1. Modifica i file
Modifica tutti i file necessari (es: gli XML di configurazione).

## 2. Apri un branch di release
Il branch deve avere il numero di versione (es: 4.0.62.12)
che indica il numero di versione di Resin (le prime tre cifre) e il numero
di revisione interno (quarta cifra).

Per esempio, il comando da usare per aprire la release 4.0.62.12 è  
`git checkout -b release/4.0.62.12`

## 3. Cambia il numero di versione
Apri il file __.gitlab-ci.yml__ e
cambia le variabili necessarie.

**RESIN_VERSION** indica il numero di versione di Resin  
**REVISION** indica il numero progressivo di rilasci fatti con
la versione di Resin corrente.  
Quest'ultimo va **azzerato** quando si rilascia una nuova versione di Resin e va
**aumentato** quando si rilascia una nuova revisione interna (della stessa versione di Resin).

Nota: la versione presente nel Dockerfile è solo un _default_ e viene sovrascritto
durante la _build_.

## 4. Aggiorna le pagine del sito
Vanno aggiornato le pagine per aggiungere il numero di versione da rilasciare.

Per testarle: `jekyll serve -b '' -s docs`


## 5. Merge in master e tag
Va effettuato il _merge_ nel branch __master__ e taggato.

`git checkout master`

`git merge --no-ff release/4.0.62.12`

`git tag -m "Resin 4.0.62 - JDK11.0.4_11" v4.0.62.12`

## 6. Backmerge su develop
`git checkout develop && git merge --no-ff v4.0.62.12`

## 7. Push
`git push origin master develop v4.0.62.12`

Questo farà partire la pipeline di build.  
Su Gitlab è possibile verificarne l'esito e la presente delle nuove immagini.
