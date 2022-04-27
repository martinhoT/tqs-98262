CREATE TABLE book (
    id          bigint,
    title       varchar(256)    NOT NULL,
    author      varchar(64)     NOT NULL,

    PRIMARY KEY (id)
);

INSERT INTO book VALUES
(0, 'I have no mouth and I must scream', 'Harlan Ellison'),
(1, 'The Art of War', 'Sun Tzu'),
(2, 'Os Sapatos do Pai Natal', 'José Fanha')
;