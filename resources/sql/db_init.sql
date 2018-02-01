CREATE TABLE SETTLEMENTS (
    ID INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1),
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
(234432, 'Gornja livada', 0),
(234450, 'Grobljanska', 0),
(234403, 'Kepinac', 0),
(234401, 'Kolarčeva - istok parna strana', 0),
(234427, 'Kolarčeva - zapad parna strana', 0),
(234483, 'Kolarčeva - istok neparna strana', 0),
(234484, 'Kolarčeva - zapad neparna strana', 0),
(234438, 'Ljubavno sokače', 0),
(234451, 'Mlinska', 0),
(234413, 'Narodnog fronta', 0),
(234405, '1. maja', 0),
(234416, 'Save Grujića', 0),
(234408, '17. oktobra', 0),
(234424, 'Smederevska', 0),
(234406, 'Vašarska', 0),
(234415, 'Vojvode Vuleta', 0),
(234404, 'Železnička', 0),
(234480, 'Branislava Nušića', 1),
(234434, 'Branka Radičevića', 1),
(234435, 'Jeremije Jeremića', 1),
(234439, 'Karabinovo sokače', 1),
(234433, 'Karađorđeva', 1),
(234442, 'Kolarski put', 1),
(234481, 'Mileta Kostadinovića Žutog', 1),
(234479, 'Miloša Obilića', 1),
(234478, 'Nikole Tesle', 1),
(234482, 'Selište', 1),
(234436, 'Svetog Save', 1),
(234440, 'Tanjino sokače', 1),
(234441, 'Veljino sokače', 1),
(234437, 'Vuka Karadžića', 1),
(234417, 'Binovački put', 2),
(234461, 'Bogosava Stepanovića Boleta', 2),
(234458, 'Čuburska', 2),
(234466, 'Dobrivoja Jankovića Lake', 2),
(234460, 'Dragoljuba Pajića', 2),
(234456, 'Drajinačka', 2),
(234463, 'Đurđa Lukića Đure', 2),
(234457, 'Kostadina Dimitrijevića Koce', 2),
(234467, 'Kostadina Mijailovića Koje', 2),
(234459, 'Mitra Gajića', 2),
(234419, 'Nastasa Nikolića', 2),
(234465, 'Omladinska', 2),
(234464, 'Radomira Matejića Rače', 2),
(234418, 'Šumadijska', 2),
(234462, 'Tanasija Ilića Tane', 2),
(234475, 'Balkanska', 3),
(234468, 'Despota Đurđa', 3),
(234472, 'Dositeja Obradovića', 3),
(234473, 'Karađorđeva', 3),
(234476, 'Knez Mihajlova', 3),
(234422, 'Nikole Tesle', 3),
(234423, 'Palanački put', 3),
(234420, 'Pilota Đorđa Stevanovića', 3),
(234470, 'Solunskih ratnika', 3),
(234474, 'Šumadijska', 3),
(234421, 'Svetog Save', 3),
(234477, 'Vojvode Stepe', 3),
(234471, 'Vuka Karadžića', 3),
(234469, 'Živojina Mišića', 3),
(234449, 'Karađorđeva', 4),
(234444, 'Lunjevačka - zapadna strana', 4),
(234485, 'Lunjevačka - istočna strana', 4),
(234448, 'Omladinska', 4),
(234443, 'Palanački put - Konjska', 4),
(234447, 'Stanoja Glavaša', 4),
(234446, 'Vojvode Putnika', 4),
(234445, 'Vojvode Stepe', 4);
CREATE TABLE LOCATIONS (
    ID INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    X DOUBLE NOT NULL,
    Y DOUBLE NOT NULL,
    HOUSE_NUMBER VARCHAR(10) NOT NULL,
    STREET_PAK INTEGER NOT NULL,
    NOTE VARCHAR(512) DEFAULT NULL,
	UNIQUE (STREET_PAK, X, Y),
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