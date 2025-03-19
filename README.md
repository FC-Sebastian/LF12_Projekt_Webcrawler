## LF12 Projektarbeit Energiedaten-Crawler
Ein Webcrawler der Daten von "https://www.agora-energiewende.de/daten-tools/agorameter" sammelt und auswertet.

### Testen:
Um den aktuellen Stand zu testen muss das Projekt heruntergeladen werden, und in einem IDE geöffnet werden.
Nachdem die Maven-Dependencies geladen wurden kann das Programm durch das ausführen von WebCrawlerFrame.java getestet werden.

Sollte seit dem letzten commit eine neue Version vom ChromeDriver veröffentlicht worden sein,
dann muss die "stable" version über diesen Link heruntergeladen werden, https://googlechromelabs.github.io/chrome-for-testing/ 
und der Inhalt des Archives in den ChromeDriver Ordner extrahiert werden.

Damit das speichern in die Datenbank getestet werden kann, muss docker/docker-compose.yml mit dem Befehl:
```
docker-compose up --build
```
gestartet werden (Docker Desktop muss installiert sein und der Befehl muss im docker ordner ausgeführt werden)

Nachdem ein von und bis Datum gewählt wurden lädt das Programm die relevanten Daten vom Agorameter und zeigt die Stundensätze als Tabelle an.

Scheinbar gab es ein Update der Seite, ich hab mich gefragt warum ich das mit dem Wind vor 2015 vergessen hatte, 
ich muss das getestet haben und dann als "erledigt" abgeschrieben haben. Wind-Daten vor 2015 werden als On- und Off-Shore gegeben.