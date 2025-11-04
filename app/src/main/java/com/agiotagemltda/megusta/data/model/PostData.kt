package com.agiotagemltda.megusta.data

import com.agiotagemltda.megusta.domain.model.Post

fun generatorMovies(): List<Post> {
    return listOf(
        Post(
            id = 1,
            name = "Pulp Fiction",
            tags = listOf("quentin tarantino", "clássico", "1994", "crime", "drama"),
            notes = "Uma obra-prima do cinema independente com narrativa não linear. Uma obra-prima do cinema independente com narrativa não linear.",
            url = "https://www.imdb.com/title/tt0110912/",
            image = "https://m.media-amazon.com/images/M/MV5BNGNhMDIzZTUtZmE2MC00M2Q4LWEwYWMtZjE4ZWI1N2Q3NmFiXkEyXkFqcGdeQXVyMTMxODk2OTU@._V1_.jpg"
        ),
        Post(
            id = 2,
            name = "Kill Bill: Volume 1",
            tags = listOf("quentin tarantino", "2003", "ação", "vingança", "uma thurman"),
            notes = "A Noiva retorna em busca de vingança contra seu antigo esquadrão.",
            url = "https://www.imdb.com/title/tt0266697/",
            image = "https://m.media-amazon.com/images/M/MV5BNzM3NDFhYTAtYmU5Mi00ZmRhLTk0Y2EtZjVjODM2YzcyZTEyXkEyXkFqcGdeQXVyNzkwMjQ5NzM@._V1_.jpg"
        ),
        Post(
            id = 3,
            name = "Kill Bill: Volume 2",
            tags = listOf("quentin tarantino", "2004", "ação", "drama", "vingança"),
            notes = "O desfecho épico da jornada da Noiva contra Bill.",
            url = "https://www.imdb.com/title/tt0378194/",
            image = "https://m.media-amazon.com/images/M/MV5BNmNiMTQyMjAtMWRkMy00ZDIwLTkxYzItNTY2NjVkNzBiNWEyXkEyXkFqcGdeQXVyMTMxODk2OTU@._V1_.jpg"
        ),
        Post(
            id = 4,
            name = "Bastardos Inglórios",
            tags = listOf(
                "quentin tarantino",
                "2009",
                "guerra",
                "história alternativa",
                "brad pitt"
            ),
            notes = "Judeus americanos caçam nazistas na França ocupada.",
            url = "https://www.imdb.com/title/tt0361748/",
            image = "https://m.media-amazon.com/images/M/MV5BOTJiNDEzOWYtMmYxMS00OWZmLWEwZTktN2NiZmRhNWEwZTZiXkEyXkFqcGdeQXVyNjU0OTQ0OTY@._V1_.jpg"
        ),
        Post(
            id = 5,
            name = "Django Livre",
            tags = listOf("quentin tarantino", "2012", "western", "escravidão", "jamie foxx"),
            notes = "Um escravo liberto caça recompensas e busca sua esposa.",
            url = "https://www.imdb.com/title/tt1853728/",
            image = "https://m.media-amazon.com/images/M/MV5BMjIyNTQ5NjQ1OV5BMl5BanBnXkFtZTcwODg1MDU4OQ@@._V1_.jpg"
        ),
        Post(
            id = 6,
            name = "Os Oito Odiados",
            tags = listOf("quentin tarantino", "2015", "western", "mistério", "suspense"),
            notes = "Oito estranhos presos numa nevasca... nem todos sobreviverão.",
            url = "https://www.imdb.com/title/tt3460252/",
            image = "https://m.media-amazon.com/images/M/MV5BMjA1Nzk0OTM3OV5BMl5BanBnXkFtZTgwNjU2NjAwNzE@._V1_.jpg"
        ),
        Post(
            id = 7,
            name = "Era Uma Vez em... Hollywood",
            tags = listOf("quentin tarantino", "2019", "drama", "comédia", "leonardo dicaprio"),
            notes = "Los Angeles, 1969. Um ator e seu dublê enfrentam uma Hollywood em mudança.",
            url = "https://www.imdb.com/title/tt7131622/",
            image = "https://m.media-amazon.com/images/M/MV5BOTg4ZTNkZmUtMzNlZi00YmFjLWE1NjEtMzBiZWFlZTY0Njg0XkEyXkFqcGdeQXVyMTA3MDk4ODIy._V1_.jpg"
        )
    )
}