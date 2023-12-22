create table candidates
(
    id            serial primary key,
    name          varchar not null,
    description   varchar not null,
    creation_date timestamp,
    visible       boolean not null,
    city_id       int references cities(id),
    file_id       int references files(id)
);