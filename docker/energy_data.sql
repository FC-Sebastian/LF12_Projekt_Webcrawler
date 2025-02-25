--
-- Tabellenstruktur für Tabelle `energy_data`
--

CREATE TABLE `energy_data` (
  `datetime` datetime NOT NULL,
  `type` varchar(255) CHARACTER SET utf8 NOT NULL,
  `gwh_per_hour` double NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Indizes für die Tabelle `energy_data`
--
ALTER TABLE `energy_data`
  ADD PRIMARY KEY (`datetime`,`type`);
COMMIT;
