CREATE TABLE IF NOT EXISTS  patogeno(
    id  int auto_increment NOT NULL UNIQUE,
    tipo VARCHAR(255) NOT NULL UNIQUE,
    cantidadDeEspecies int NOT NULL,
    primary key (id)
)