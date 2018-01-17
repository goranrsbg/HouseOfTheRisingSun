CREATE TABLE SETTLEMENTS (
    ID INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    NAME VARCHAR(23) UNIQUE,
	INITIAL CHAR(2) NOT NULL
);
INSERT INTO SETTLEMENTS VALUES
(DEFAULT, 'Kolari', 'KL'),
(DEFAULT, 'Landol', 'LA'),
(DEFAULT, 'Binovac', 'BI'),
(DEFAULT, 'Suvodol', 'SU'),
(DEFAULT, 'Lunjevac', 'LU');
CREATE TABLE STREETS (
    PAK INTEGER PRIMARY KEY CONSTRAINT PAK_CH CHECK (PAK < 10000000),
    NAME VARCHAR(47) NOT NULL,
    SETTLEMENT_ID INTEGER,
    CONSTRAINT SETTLEMENTS_FK
    FOREIGN KEY (SETTLEMENT_ID)
    REFERENCES SETTLEMENTS (ID)
);
INSERT INTO STREETS VALUES
(234432, 'Gornja livada', 1),
(234450, 'Grobljanska', 1),
(234403, 'Kepinac', 1),
(234401, 'Kolarčeva - istok parna strana', 1),
(234427, 'Kolarčeva - zapad parna strana', 1),
(234483, 'Kolarčeva - istok neparna strana', 1),
(234484, 'Kolarčeva - zapad neparna strana', 1),
(234438, 'Ljubavno sokače', 1),
(234451, 'Mlinska', 1),
(234413, 'Narodnog fronta', 1),
(234405, '1. maja', 1),
(234416, 'Save Grujića', 1),
(234408, '17. oktobra', 1),
(234424, 'Smederevska', 1),
(234406, 'Vašarska', 1),
(234415, 'Vojvode Vuleta', 1),
(234404, 'Železnička', 1),
(234480, 'Branislava Nušića', 2),
(234434, 'Branka Radičevića - druga', 2),
(234435, 'Jeremije Jeremića - treća', 2),
(234439, 'Karabinovo sokače', 2),
(234433, 'Karađorđeva - prva', 2),
(234442, 'Kolarski put', 2),
(234481, 'Mileta Kostadinovića Žutog', 2),
(234479, 'Miloša Obilića', 2),
(234478, 'Nikole Tesle', 2),
(234482, 'Selište', 2),
(234436, 'Svetog Save - četvrta', 2),
(234440, 'Tanjino sokače', 2),
(234441, 'Veljino sokače', 2),
(234437, 'Vuka Karadžića', 2),
(234417, 'Binovački put', 3),
(234461, 'Bogosava Stepanovića Boleta', 3),
(234458, 'Čuburska', 3),
(234466, 'Dobrivoja Jankovića Lake', 3),
(234460, 'Dragoljuba Pajića', 3),
(234456, 'Drajinačka', 3),
(234463, 'Đurđa Lukića Đure', 3),
(234457, 'Kostadina Dimitrijevića Koce', 3),
(234467, 'Kostadina Mijailovića Koje', 3),
(234459, 'Mitra Gajića', 3),
(234419, 'Nastasa Nikolića', 3),
(234465, 'Omladinska', 3),
(234464, 'Radomira Matejića Rače', 3),
(234418, 'Šumadijska', 3),
(234462, 'Tanasija Ilića Tane', 3),
(234475, 'Balkanska', 4),
(234468, 'Despota Đurđa', 4),
(234472, 'Dositeja Obradovića', 4),
(234473, 'Karađorđeva', 4),
(234476, 'Knez Mihajlova', 4),
(234422, 'Nikole Tesle', 4),
(234423, 'Palanački put', 4),
(234420, 'Pilota Đorđa Stevanovića', 4),
(234470, 'Solunskih ratnika', 4),
(234474, 'Šumadijska', 4),
(234421, 'Svetog Save', 4),
(234477, 'Vojvode Stepe', 4),
(234471, 'Vuka Karadžića', 4),
(234469, 'Živojina Mišića', 4),
(234449, 'Karađorđeva', 5),
(234444, 'Lunjevačka - zapadna strana', 5),
(234485, 'Lunjevačka - istočna strana', 5),
(234448, 'Omladinska', 5),
(234443, 'Palanački put - Konjska', 5),
(234447, 'Stanoja Glavaša', 5),
(234446, 'Vojvode Putnika', 5),
(234445, 'Vojvode Stepe', 5);
CREATE TABLE LOCATIONS (
    ID INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    X DOUBLE NOT NULL,
    Y DOUBLE NOT NULL,
    HOUSE_NUMBER VARCHAR(10) DEFAULT NULL,
    STREET_PAK INTEGER,
    NOTE VARCHAR(512) DEFAULT NULL,
    CONSTRAINT STREETS_FK
    FOREIGN KEY(STREET_PAK)
    REFERENCES STREETS (PAK)
);
CREATE TABLE RECIPIENTS (
    ID INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    LAST_NAME VARCHAR(51) NOT NULL,
    FIRST_NAME VARCHAR(51) DEFAULT NULL,
	DETAILS VARCHAR(51) DEFAULT NULL,
    LOCATION_ID INTEGER,
    ID_NUMBER BIGINT DEFAULT NULL,
    RETIREE BOOLEAN DEFAULT FALSE,
    CONSTRAINT LOCATIONS_FK
    FOREIGN KEY (LOCATION_ID)
    REFERENCES LOCATIONS (ID)
);
CREATE INDEX LAST_NAME_IND ON RECIPIENTS (LAST_NAME);
CREATE INDEX FIRST_NAME_IND ON RECIPIENTS (FIRST_NAME);