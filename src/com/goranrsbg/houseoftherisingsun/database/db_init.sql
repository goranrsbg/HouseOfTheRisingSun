CREATE TABLE SETTLEMENTS (
    SETTLEMENT_ID INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    SETTLEMENT_NAME VARCHAR(47) UNIQUE,
    SETTLEMENT_INITIALS CHAR(2) NOT NULL,
    SETTLEMENT_FILE_NAME VARCHAR(47) NOT NULL
);
CREATE TABLE STREETS (
    STREET_ID INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    STREET_PAK INTEGER NOT NULL,
    STREET_NAME VARCHAR(47) NOT NULL,
    SETTLEMENT_ID INTEGER REFERENCES SETTLEMENTS (SETTLEMENT_ID),
    UNIQUE (STREET_PAK)
);
CREATE TABLE LOCATIONS (
    LOCATION_ID INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    LOCATION_POINT_X DOUBLE NOT NULL,
    LOCATION_POINT_Y DOUBLE NOT NULL,
    LOCATION_ADDRESS_NO VARCHAR(23) NOT NULL,
    LOCATION_POSTMAN_PATH_STEP INTEGER,
    LOCATION_NOTE VARCHAR(51) DEFAULT NULL,
    STREET_ID INTEGER REFERENCES STREETS (STREET_ID),
    UNIQUE (STREET_ID, LOCATION_ADDRESS_NO)
);
CREATE TABLE RECIPIENTS (
    RECIPIENT_ID INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    RECIPIENT_LAST_NAME VARCHAR(51) NOT NULL,
    RECIPIENT_FIRST_NAME VARCHAR(51) DEFAULT NULL,
    RECIPIENT_DETAILS VARCHAR(51) DEFAULT NULL,
    RECIPIENT_IS_RETIREE BOOLEAN DEFAULT NULL,
    RECIPIENT_ID_CARD_NUMBER BIGINT DEFAULT NULL,
    RECIPIENT_ID_CARD_POLICE_DEPARTMENT VARCHAR(51) DEFAULT NULL,
    LOCATION_ID INTEGER REFERENCES LOCATIONS (LOCATION_ID)
);
CREATE INDEX LAST_NAME_INDEX ON RECIPIENTS (RECIPIENT_LAST_NAME);
CREATE INDEX FIRST_NAME_INDEX ON RECIPIENTS (RECIPIENT_FIRST_NAME);
CREATE TABLE USERS (
    USER_ID INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY(START WITH 1, INCREMENT BY 1),
    USER_NAME CHAR(64) NOT NULL UNIQUE,
    USER_PASSWORD CHAR(64) NOT NULL
);
INSERT INTO USERS VALUES
(DEFAULT, '6199aecf23aba7e87b2dafb8b4915260da85e3cf53568197b7e451982392fb8e', '4fc82b26aecb47d2868c4efbe3581732a3e7cbcc6c2efb32062c08170a05eeb8');
INSERT INTO SETTLEMENTS VALUES
(DEFAULT, 'default', '@', 'default_map.jpg'),
(DEFAULT, 'Kolari', 'KL', 'kolari_map.jpg'),
(DEFAULT, 'Landol', 'LA', 'landol_map.jpg'),
(DEFAULT, 'Binovac', 'BI', 'binovac_map.jpg'),
(DEFAULT, 'Suvodol', 'SU', 'suvodol_map.jpg'),
(DEFAULT, 'Lunjevac', 'LU', 'lunjevac_map.jpg');
INSERT INTO STREETS VALUES
(DEFAULT, 234432, 'Gornja livada', 2),
(DEFAULT, 234450, 'Grobljanska', 2),
(DEFAULT, 234403, 'Kepinac', 2),
(DEFAULT, 234401, 'Kolarčeva - istok parna strana', 2),
(DEFAULT, 234427, 'Kolarčeva - zapad parna strana', 2),
(DEFAULT, 234483, 'Kolarčeva - istok neparna strana', 2),
(DEFAULT, 234484, 'Kolarčeva - zapad neparna strana', 2),
(DEFAULT, 234438, 'Ljubavno sokače', 2),
(DEFAULT, 234451, 'Mlinska', 2),
(DEFAULT, 234413, 'Narodnog fronta', 2),
(DEFAULT, 234405, '1. maja', 2),
(DEFAULT, 234416, 'Save Grujića', 2),
(DEFAULT, 234408, '17. oktobra', 2),
(DEFAULT, 234424, 'Smederevska', 2),
(DEFAULT, 234406, 'Vašarska', 2),
(DEFAULT, 234415, 'Vojvode Vuleta', 2),
(DEFAULT, 234404, 'Železnička', 2),
(DEFAULT, 234480, 'Branislava Nušića', 3),
(DEFAULT, 234434, 'Branka Radičevića', 3),
(DEFAULT, 234435, 'Jeremije Jeremića', 3),
(DEFAULT, 234439, 'Karabinovo sokače', 3),
(DEFAULT, 234433, 'Karađorđeva', 3),
(DEFAULT, 234442, 'Kolarski put', 3),
(DEFAULT, 234481, 'Mileta Kostadinovića Žutog', 3),
(DEFAULT, 234479, 'Miloša Obilića', 3),
(DEFAULT, 234478, 'Nikole Tesle', 3),
(DEFAULT, 234482, 'Selište', 3),
(DEFAULT, 234436, 'Svetog Save', 3),
(DEFAULT, 234440, 'Tanjino sokače', 3),
(DEFAULT, 234441, 'Veljino sokače', 3),
(DEFAULT, 234437, 'Vuka Karadžića', 3),
(DEFAULT, 234417, 'Binovački put', 4),
(DEFAULT, 234461, 'Bogosava Stepanovića Boleta', 4),
(DEFAULT, 234458, 'Čuburska', 4),
(DEFAULT, 234466, 'Dobrivoja Jankovića Lake', 4),
(DEFAULT, 234460, 'Dragoljuba Pajića', 4),
(DEFAULT, 234456, 'Drajinačka', 4),
(DEFAULT, 234463, 'Đurđa Lukića Đure', 4),
(DEFAULT, 234457, 'Kostadina Dimitrijevića Koce', 4),
(DEFAULT, 234467, 'Kostadina Mijailovića Koje', 4),
(DEFAULT, 234459, 'Mitra Gajića', 4),
(DEFAULT, 234419, 'Nastasa Nikolića', 4),
(DEFAULT, 234465, 'Omladinska', 4),
(DEFAULT, 234464, 'Radomira Matejića Rače', 4),
(DEFAULT, 234418, 'Šumadijska', 4),
(DEFAULT, 234462, 'Tanasija Ilića Tane', 4),
(DEFAULT, 234475, 'Balkanska', 5),
(DEFAULT, 234468, 'Despota Đurđa', 5),
(DEFAULT, 234472, 'Dositeja Obradovića', 5),
(DEFAULT, 234473, 'Karađorđeva', 5),
(DEFAULT, 234476, 'Knez Mihajlova', 5),
(DEFAULT, 234422, 'Nikole Tesle', 5),
(DEFAULT, 234423, 'Palanački put', 5),
(DEFAULT, 234420, 'Pilota Đorđa Stevanovića', 5),
(DEFAULT, 234470, 'Solunskih ratnika', 5),
(DEFAULT, 234474, 'Šumadijska', 5),
(DEFAULT, 234421, 'Svetog Save', 5),
(DEFAULT, 234477, 'Vojvode Stepe', 5),
(DEFAULT, 234471, 'Vuka Karadžića', 5),
(DEFAULT, 234469, 'Živojina Mišića', 5),
(DEFAULT, 234449, 'Karađorđeva', 5),
(DEFAULT, 234444, 'Lunjevačka - zapadna strana', 6),
(DEFAULT, 234485, 'Lunjevačka - istočna strana', 6),
(DEFAULT, 234448, 'Omladinska', 6),
(DEFAULT, 234443, 'Palanački put - Konjska', 6),
(DEFAULT, 234447, 'Stanoja Glavaša', 6),
(DEFAULT, 234446, 'Vojvode Putnika', 6),
(DEFAULT, 234445, 'Vojvode Stepe', 6);