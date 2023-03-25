# JavaChannelExample
Esercizio laboratorio sistemi distribuiti - java selector e channels

Il client si connette al server e manda un codice identificativo (1)
Il server se riceve il codice corretto, ovvero 1, risponde con lo stesso codice
in seguito il client fa inserire all'utente un codice identificativo per il giorno di cui si è
interessati guardare la programmazione dei film.
Il client invia il codice al server il quale risponde con tutti i film che vengono proiettati in quel
giorno.


Esempio a scopo didattico per la gestione di più richieste da parte del server utilizzando un selector.
