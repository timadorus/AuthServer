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

Der Authserver ist als RESTful Webservice auf HTTPS-Basis realisiert und ein Client schickt
entsprechende HTTP Anfragen an den Server. Da HTTPS (also HTTP über SSL bzw. TLS) genutzt wird,
ist die Verbindung zwischen Client und Authserver verschlüsselt und (zum. für unsere Zwecke
ausreichend) abhörsicher.

Um sich beim Authserver zu authentifizieren, sendet ein Client bei einer HTTP-GET Anfrage
seine Benutzerkennung (username) und Passwort mit (Basic access authentication,
HTTP-Authentifizierung). Benutzerkennung und Passwort werden hierbei im HTTP-Header des
Requests mitgesendet und können vom Server aus dem HTTP-Header wieder extrahiert
werden. Die eigentliche HTTP-Anfrage wird dann nur vom Server bearbeitet, wenn Benutzername
und Passwort auch korrekt sind.

Der durch eine Authentifizierunganfrage erhaltene auth-token wird im Zuge der
Login-Prozedur des Timadorus Message Protocols (TMP) vom Clienten an den Gameserver
weitergereicht. Der Auth-Token ist AES verschlüsselt mit einem geheimen Schlüssel, den
nur der Authserver und der Gameserver kennen (shared-secret-key) und enthält Daten
(u.a. einen Zeitstempel u. Namen), um die Korrektheit der Anmeldung zu prüfen.



REST API
########

Alle Rückgabewerte und alle Parameter werden als JSON übertragen. Der Authserver nimmt
folgende HTTP Anfragen für folgende Resourcen entgegen:

  [HTTP Methode]            [Resource]           [Parameter]       [Privilegiert]
  
      GET                    /users                match                Ja
       -> Liefert eine Auflistung aller Benutzerkontennamen als JSON-Array.
       
      GET                  /users/name                -                 Nein *
       -> Liefert Statusinformationen über den Benutzer mit namen 'name', also
          z.b. /users/hallo liefert Statusinformationen für hallo. Ein normaler
          Benutzer kann lediglich seinen eigenen Account abfragen. Ein Administrator
          kann die Anfrage für jeden beliebigen Account stellen. Je nachdem, ob
          ein normaler Benutzer, oder ein Administrator die Anfrage stellt, sind im
          zurückgelieferten JSON-Objekt mehr oder weniger Felder enthalten.
          In jedem Fall ist das Attribut 'name' enthalten und das Attribut 'entities',
          welches ein JSON-Array mit den Namen der Charaktere des Benutzers darstellt.
          
      GET                /users/name/charakter        -                  Nein *
       -> Liefert Statusinformationen über den Charakter mit namen 'charakter' des
          Benutzers 'name' zurück, also z.b. /users/hallo/welt liefert Statusinformationen
          für den Charakter 'welt' des Benutzers 'hallo' zurück.
          Ein normaler Benutzer kann lediglich seine eigenen Charaktere abfragen. Ein
          Administrator kann die Anfrage für jeden beliebigen Charakter jedes beliebigen
          Accounts stellen. Je nachdem, ob ein normaler Benutzer, oder ein Administrator
          die Anfrage stellt, sind im zurückgelieferten JSON-Objekt mehr oder weniger
          Felder enthalten.
          In jedem Fall ist das Attribut 'name' enthalten und das Attribut 'authToken',
          welches den auth-token für den jeweiligen Charakter enthält.
          
      GET              /users/name/charakter/stats    -                   Nein *
       -> Liefert die Attribute des Charakters 'charakters' des Benutzers 'name' zurück,
          also z.b. /users/hallo/welt/stats liefert alle Attribute des Charaketers
          'welt' des Benutzers 'hallo' zurück.
          Ein normaler Benutzer kann lediglich seine eigenen Charaktere abfragen. Ein
          Administrator kann die Anfrage für jeden beliebigen Charakter jedes beliebigen
          Accounts stellen.
          
      PUT             /users/name                     JSON                  Ja
       -> Legt einen neuen Benutzer mit dem Namen 'name' an. Als Parameter wird ein
          JSON-Objekt erwartet, welches mindestens das Attribut 'password' enthält.
          Optionale weitere Attribute sind 'admin' (bool), und 'flags' (integer).
          
      PUT            /users/name/charakter            JSON                  Nein *
       -> Legt einen neuen Charakter mit Namen 'charakter' für den Benutzer 'name' an.
          Ein normaler Benutzer kann lediglich unter seinem eigenen Benutzernamen
          neue Charaktere anlegen. Ein Administrator kann für beliebige Accounts neue
          Charaktere anlegen. Als Parameter kann ein JSON-Objekt übergeben werden,
          welches das Attribut 'flags' (integer) enthält.
          
      DELETE        /users/name                        -                     Ja
       -> Löscht den Benutzer mit namen 'name'. Alle etwaigen Charaktere des Benutzers
          werden hierbei ebenfalls gelöscht.
      
      DELETE        /users/name/charakter              -                    Nein *
       -> Löscht den Charakter mit Namen 'charakter' des Benutzers 'name'. Ein normaler
          Benutzer kann lediglich seine eigenen Charaktere löschen. Ein Administrator
          kann beliebige Charaktere beliebiger Benutzer löschen.
          
      POST          /users/name                        JSON                 Nein *
       -> Editiert einen bestehenden Benutzer mit Namen 'name'. Als Parameter wird
          ein JSON-Objekt mit den zu ändernden Attributen erwartet. Mögliche Attribute
          sind: 'admin' (boolean), 'password' (string) und 'flags' (integer). Ein
          normaler Benutzer kann lediglich das Passwort ändern, ein Administrator
          kann jedes der angegebenen Attribute editieren.
          
      POST          /users/name/charakter              JSON                  Ja
       -> Editiert einen bestehenden Charakter mit Namen 'charakter' des Benutzers
          'name'. Als Parameter wird ein JSON-Objekt mit den zu ändernen Attributen
          erwartet. Mögliche Attribute sind: 'name' (string) und 'flags' (integer).
      



Konfigurieren und Starten
#########################

Der Authserver kann über die server-config.xml Datei konfiguriert werden. Hier
wird die Datenbankverbindung eingetragen, sowie der geheime Schlüssel, den sich
Auth- und Gameserver teilen, um auth-token ver- bzw. entschlüsseln zu können.

Zum Starten ins gleiche Verzeichnis wie die auth-server.jar Datei wechseln und
"java -jar auth-server" eingeben. Die server-config.xml sollte sich im gleichen
Verzeichnis befinden. Beim ersten Start legt der Authserver die benötigten
Datenbanktabellen neu an und erstellt den Standardnutzer 'admin' mit Passwort
'password'. Das Passwort sollte nach dem ersten Start geändert werden.



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

