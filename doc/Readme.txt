Architektur Überblick
#####################

Der Authserver ist ein eigenständiger Server, der unabhängig vom eigentlichen Gameserver
läuft und der die Authentifizierung von Benutzerkonten (user accounts) sowie die Auswahl
eines Spielcharakters (entity) des jeweiligen Benutzers durchführt.

Der Gameclient führt also zuerst beim Authserver seine Authentisierung durch und wählt
seinen Spielcharakter aus. Er erhält vom Authserver daraufhin einen sog. Auth-Token,
den er anschließend an den Gameserver weiterreicht. Der Auth-Token dient dem Gameserver
dann als Beweis, dass der Benutzer sich angemeldet hat.



Funktionsweise
##############

Der Authserver ist als HTTPS-Server realisiert und ein Client schickt entsprechende
HTTP-GET Anfragen an den Server. Da HTTPS (also HTTP über SSL bzw. TLS) genutzt wird,
ist die Verbindung zwischen Client und Authserver verschlüsselt und (zum. für unsere Zwecke
ausreichend) abhörsicher.

Um sich beim Authserver zu authentifizieren, sendet ein Client bei einer HTTP-GET
Anfrage seine Benutzerkennung (username) und Passwort mit (Basic access authentication,
HTTP-Authentifizierung). Benutzerkennung und Passwort werden hierbei im HTTP-Header
des Requests mitgesendet und können vom Server aus dem HTTP-Header wieder extrahiert
werden. Die eigentliche HTTP-Anfrage wird dann nur vom Server bearbeitet, wenn
Benutzername und Passwort auch korrekt sind.

Der Authserver nimmt nur 2 verschiedene HTTP-GET Anfragen entgegen, und zwar:

 * listEntities
     Liefert eine Liste aller Spiel-Charaktere (entities) des jeweiligen Benutzers
     zurück.
 
 * getAuthToken/entity
     Liefert einen Auth-token String für den angeforderten Spiel-Charaketer (entity)
     zurück.
     
Der durch eine getAuthToken-Anfrage erhaltene auth-token wird dann im Zuge der
Login-Prozedur des Timadorus Message Protocols (TMP) vom Clienten an den Gameserver
weitergereicht. Der Auth-Token ist AES verschlüsselt mit einem geheimen Schlüssel, den
nur der Authserver und der Gameserver kennen (shared-secret-key) und enthält Daten
(u.a. einen Zeitstempel u. Namen), um die Korrektheit der Anmeldung zu prüfen.



Konfigurieren und Starten
#########################

Der Authserver kann über die server-config.xml Datei konfiguriert werden. Hier
wird die Datenbankverbindung eingetragen, sowie der geheime Schlüssel, den sich
Auth- und Gameserver teilen, um auth-token ver- bzw. entschlüsseln zu können.

Zum Starten ins gleiche Verzeichnis wie die auth-server.jar Datei wechseln und
"java -jar auth-server" eingeben. Die server-config.xml sollte sich im gleichen
Verzeichnis befinden.



Logging
#######

Logging erfolgt mit der Standard Logging API von Java.



Zertifikate für SSL-Betrieb erstellen
#####################################

Damit der Authserver als HTTPS-Server fungieren kann, benötigt er ein SSL Zertifikat.
Dies kann mit dem 'keytool' Programm, welches mit Java mitgeliefert wird, erstellt
werden. 




Jar Packages Überblick
######################

Es gibt folgende Komponenten im Projekt:
 * auth-server
    Dies ist der eigentliche Authserver.
 * auth-client-lib
    Dies ist die Bibliothek, die vom Spielclient eingebunden kann und eine
    einfache Schnittstelle anbietet, um die Authentifizierung durchzuführen.
 * example-client
    Eine Beispiel-Clientanwendung, die die auth-client-lib benutzt und demonstriert
    wie man sich authentifiziert.
 * example-gameserver
    Ein dummy "Gameserver", der auth-token entgegennimmt und validiert.



Kompilieren
###########

Die 'build.xml' Datei im Hauptverzeichnis mit Ant (Java Build Tool) aufrufen.
Hierzu sollte es genügen ins Hauptverzeichnis zu wechseln und 'ant.exe' bzw
'./ant' auszuführen.

Das Buildskript kompiliert alle Quelldateien, führt alle Unittests aus und
erstellt JAR Pakete im ./release unterordner. Benötigte Bibliotheken werden
mit Hilfe von ivy heruntergeladen. Der erste Buildvorgang dauert lange,
da erst alle Bibliotheken heruntergeladen werden müssen. Bei weiteren
Buildvorgängen werden die Bibliotheken dann natürlich aus einem Cache
geladen und das Bauen geht schnell.




Erlang Beispielcode
###################

Im Verzeichnis ./doc/erlang/ befindet sich Erlang Beispielcode, der demonstriert,
wie man einen auth-token mit Erlang entschlüsselt.

Da der geheime Schlüssel zum Entschlüsseln von Auth-Token vom festgelegten Passwort
über die PBKDF2-Funktion abgeleitet wird und diese (bisher) in Erlang nicht enthalten
ist, muss hierfür eine gesonderte Thirdparty Bibliothek benutzt werden (pbkdf2.erl von
Apache CouchDB).



Datenbank
#########

Als Datenbank für das Hinterlegen von Benutzerkennungen und Passwörtern wird
Apache Derby benutzt. Der Datenbanktreiber und Connectionstring können in der
Konfigurationsdatei des Authservers (server-config.xml) aber angepasst werden.

