#!/usr/bin/perl -w
#-
#+ Script zum Extrahieren von AOK-Parameter-Beschreibungen aus dem Wiki 
#+
#+ Die Verschachtelung der Schleifen ist dem normalen Verständnis
#+ entgegengesetzt, damit die HTTP-Anfrage nur einmal über das Netz
#+ gesendet werden muss. Die entsprechenden Parameterdaten werden 
#+ dann lokal gesichert.
#+
#+ Im Wiki werden innerhalb einer === ... === Überschrift die Kennzeichnungen
#+ mittels Parameter-Nummern erwartet. Diese sind wie folgt aufgebaut:
#+\begin{verbatim}
#+=== Startset [DEFAULT=4, PNR=0] ===
#+=== ROLL90, NICK90, YAW90 [DEFAULT=9800, PNR=1,2,3] ===
#+\end{verbatim}
#+ Es wird auf |PNR=..]| geparst. Dieses Syntax ist zwingend. Der Bereich des
#+ Parameters endet, sobald eine Überschrift gleicher oder höherer Wertigkeit
#+ erreicht wird.
#+
#+ Wir benutzen eine HTTP-Verbindung, um direkt aus dem Wiki zu lesen.
use Net::HTTP;
#+ 
#+ Es werden maximal 127 Parameter geholt. und die Ergebnisdateien unter dem
#+ hier angegebenen Muster gesichert:
$maxnr=127;
$muster="AOK-ParmDoku/AOK-ParmDoku_%d.html";
#+ Hier holen wir die HTML-Seite ab...
$host="www.armokopter.at";
$page="/wiki/doku.php?id=aok:einstieg:parameter";
#+
#+ HTTP-Objekt erzeugen und Seite holen
my $s = Net::HTTP->new(Host => $host) || die $@;
$s->write_request(GET => $page, 'User-Agent' => "Parm doku extractor 0.1") || die $@;

#+ Unser Index für die Parameternummer:
my $nr;

#+ In der Array-Variablen |@copy| speichern wir die Startzeile des zugehörigen 
#+ Parameters ab, in |@erg| sichern wir den entsprechenden HTML-Text.
my @copy;
my @erg;
#+ Die Startzeilen müssen mit 0 initialisiert werden.
for ($nr=0; $nr<=$maxnr; $nr++) {
	$copy[$nr]=0;
}

#+ unser Zeilenzähler:
my $line = 0;

#+ Nun lesen wir Zeile für Zeile der HTML-Seite und parsen auf Infos zu all
#+ den gewünschten Parametern.
while (readline $s) {
	$line++;

	for ($nr=0; $nr<=$maxnr; $nr++) {
#+ |PNR| mit passender Zahl suchen und falls gefunden, Sicherung einschalten.
		if (/PNR=(\d*,)*$nr(,\d*)*\]/) {
			$copy[$nr]=$line;
		}
#+ Sichern wieder ausschalten, falls wir sichern, nicht mehr in der aktuellen 
#+ Startzeile stehen und eine Überschrift mit gleicher oder höherer Wertigkeit
#+ gefunden wurde. 
		if ($copy[$nr] && $copy[$nr] != $line && /\<\/h[4321]\>/i) {
			$copy[$nr]=0;
		}
#+ Wenn Sichern eingeschaltet ist, den passenden Text merken.
		if ($copy[$nr]) {
			$erg[$nr].=$_;
		}
	}
}

#+ Parameter-Namen lesen, falls |legends_parameters.txt| vorhanden ist:
my @parms;
$i=0;
if (open $LEG, "legend_parameters.txt") { 
	while (<$LEG>) {
		s/\r*\n*//g;
		$parms[$i++]=$_;
	}
	close $LEG;
}

#+ Nun werden für alle gefundenen Parameter HTML-Dateien geschrieben. Das Muster
#+ von oben wird hier im |open| benutzt.
for ($nr=0; $nr<=$maxnr; $nr++) {
	if (defined($erg[$nr])) {
		my $name = sprintf $muster, $nr;
		open $OUT, ">$name" or die $@;
		$erg[$nr] =~ s/<img .*FIXME\" \/>/FIXME/mg;
		print $OUT "<html>\n$erg[$nr]\n</html>" or die $@;
		close $OUT or die $@;
	} else {
		if (defined $parms[$nr] and $parms[$nr] ne "unused") {
			print "Doku für Parameter $nr ($parms[$nr]) fehlt.\n";
		}
	}
}
