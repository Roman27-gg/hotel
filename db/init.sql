CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE huesped (
    id             BIGSERIAL PRIMARY KEY,
    documento      VARCHAR(20)  NOT NULL UNIQUE,
    nombre         VARCHAR(60)  NOT NULL,
    apellido       VARCHAR(60)  NOT NULL,
    telefono       VARCHAR(20)  NOT NULL,
    email          VARCHAR(120),
    fecha_registro TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE habitacion (
    id           BIGSERIAL PRIMARY KEY,
    numero       VARCHAR(10)   NOT NULL UNIQUE,
    tipo         VARCHAR(20)   NOT NULL CHECK (tipo IN ('SENCILLA','SUITE')),
    capacidad    SMALLINT      NOT NULL CHECK (capacidad BETWEEN 1 AND 6),
    tarifa_noche NUMERIC(10,2) NOT NULL CHECK (tarifa_noche > 0),
    estado       VARCHAR(20)   NOT NULL DEFAULT 'DISPONIBLE'
                 CHECK (estado IN ('DISPONIBLE','RESERVADA','OCUPADA','MANTENIMIENTO'))
);

CREATE TABLE reserva (
    id                BIGSERIAL PRIMARY KEY,
    habitacion_id     BIGINT        NOT NULL REFERENCES habitacion(id) ON DELETE RESTRICT,
    huesped_id        BIGINT        NOT NULL REFERENCES huesped(id)    ON DELETE RESTRICT,
    fecha_entrada     DATE          NOT NULL,
    fecha_salida      DATE          NOT NULL,
    num_huespedes     SMALLINT      NOT NULL CHECK (num_huespedes > 0),
    estado            VARCHAR(20)   NOT NULL DEFAULT 'CONFIRMADA'
                      CHECK (estado IN ('CONFIRMADA','CANCELADA','FINALIZADA')),
    valor_total       NUMERIC(12,2) NOT NULL CHECK (valor_total >= 0),
    penalizacion      NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (penalizacion >= 0),
    fecha_creacion    TIMESTAMP     NOT NULL DEFAULT now(),
    fecha_cancelacion TIMESTAMP,
    fecha_check_in    TIMESTAMP,
    fecha_check_out   TIMESTAMP,
    CONSTRAINT ck_reserva_fechas CHECK (fecha_salida > fecha_entrada),
    CONSTRAINT ex_reserva_sin_cruce EXCLUDE USING gist (
        habitacion_id WITH =,
        daterange(fecha_entrada, fecha_salida, '[)') WITH &&
    ) WHERE (estado = 'CONFIRMADA')
);

CREATE INDEX idx_reserva_habitacion ON reserva(habitacion_id);
CREATE INDEX idx_reserva_huesped    ON reserva(huesped_id);
CREATE INDEX idx_reserva_estado     ON reserva(estado);

INSERT INTO huesped (documento, nombre, apellido, telefono, email) VALUES
    ('12345678', 'Juan', 'Perez', '3001112233', 'juan.perez@email.com'),
    ('87654321', 'Maria', 'Gomez', '3004445566', 'maria.gomez@email.com'),
    ('11223344', 'Carlos', 'Ruiz', '3007778899', NULL),
    ('55667788', 'Ana', 'Diaz', '3009990011', 'ana.diaz@email.com');

INSERT INTO habitacion (numero, tipo, capacidad, tarifa_noche, estado) VALUES
    ('101', 'SENCILLA', 2, 80000.00, 'DISPONIBLE'),
    ('102', 'SENCILLA', 2, 80000.00, 'DISPONIBLE'),
    ('103', 'SENCILLA', 2, 80000.00, 'DISPONIBLE'),
    ('104', 'SENCILLA', 2, 80000.00, 'DISPONIBLE'),
    ('105', 'SENCILLA', 2, 80000.00, 'DISPONIBLE'),
    ('106', 'SENCILLA', 2, 80000.00, 'DISPONIBLE'),
    ('107', 'SENCILLA', 2, 80000.00, 'DISPONIBLE'),
    ('108', 'SENCILLA', 2, 80000.00, 'DISPONIBLE'),
    ('109', 'SENCILLA', 2, 80000.00, 'DISPONIBLE'),
    ('110', 'SENCILLA', 2, 80000.00, 'DISPONIBLE'),
    ('111', 'SENCILLA', 2, 80000.00, 'DISPONIBLE'),
    ('112', 'SENCILLA', 2, 80000.00, 'DISPONIBLE'),
    ('113', 'SENCILLA', 2, 80000.00, 'DISPONIBLE'),
    ('114', 'SENCILLA', 2, 80000.00, 'DISPONIBLE'),
    ('201', 'SUITE', 4, 200000.00, 'DISPONIBLE'),
    ('202', 'SUITE', 4, 200000.00, 'DISPONIBLE'),
    ('203', 'SUITE', 4, 200000.00, 'DISPONIBLE'),
    ('204', 'SUITE', 4, 200000.00, 'DISPONIBLE'),
    ('205', 'SUITE', 4, 200000.00, 'DISPONIBLE'),
    ('206', 'SUITE', 4, 200000.00, 'DISPONIBLE');

INSERT INTO reserva (habitacion_id, huesped_id, fecha_entrada, fecha_salida, num_huespedes, estado, valor_total) VALUES
    (1, 1, CURRENT_DATE + 5, CURRENT_DATE + 8, 2, 'CONFIRMADA', 240000.00),
    (2, 2, CURRENT_DATE + 10, CURRENT_DATE + 12, 1, 'CONFIRMADA', 160000.00),
    (16, 3, CURRENT_DATE + 2, CURRENT_DATE + 7, 3, 'CONFIRMADA', 1000000.00),
    (17, 4, CURRENT_DATE - 2, CURRENT_DATE + 1, 2, 'CANCELADA', 400000.00);
