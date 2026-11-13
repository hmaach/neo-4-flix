// Neo4flix demo graph. Idempotent: every statement uses MERGE or IF NOT EXISTS, so re-running changes nothing.
// Graph model (CLAUDE.md):
//   (:User {id, username, email, passwordHash, totpSecretEnc, totpEnabled, roles, createdAt})
//   (:Movie {id, title, releaseDate, overview})-[:IN_GENRE]->(:Genre {name})
//   (:User)-[:RATED {score 1..5, ratedAt}]->(:Movie)
//   (:User)-[:WATCHLISTED {addedAt}]->(:Movie)
// Ids are app-style UUID strings. Seed ids follow a readable pattern; the services generate random UUIDs.

// 1. Constraints
CREATE CONSTRAINT user_id_unique IF NOT EXISTS FOR (u:User) REQUIRE u.id IS UNIQUE;
CREATE CONSTRAINT user_username_unique IF NOT EXISTS FOR (u:User) REQUIRE u.username IS UNIQUE;
CREATE CONSTRAINT user_email_unique IF NOT EXISTS FOR (u:User) REQUIRE u.email IS UNIQUE;
CREATE CONSTRAINT movie_id_unique IF NOT EXISTS FOR (m:Movie) REQUIRE m.id IS UNIQUE;
CREATE CONSTRAINT genre_name_unique IF NOT EXISTS FOR (g:Genre) REQUIRE g.name IS UNIQUE;

// 2. Genres
UNWIND ['Action', 'Adventure', 'Animation', 'Comedy', 'Drama', 'Horror', 'Romance', 'Science Fiction'] AS name
MERGE (:Genre {name: name});

// 3. Movies and their genres
UNWIND [
  {id: '10000000-0000-4000-8000-000000000001', title: 'The Matrix', releaseDate: date('1999-03-31'), genres: ['Science Fiction', 'Action'], overview: 'A hacker learns that reality is a simulation and joins the fight against the machines.'},
  {id: '10000000-0000-4000-8000-000000000002', title: 'Inception', releaseDate: date('2010-07-16'), genres: ['Science Fiction', 'Action'], overview: 'A thief who steals secrets through dreams is hired to plant an idea instead.'},
  {id: '10000000-0000-4000-8000-000000000003', title: 'Interstellar', releaseDate: date('2014-11-07'), genres: ['Science Fiction', 'Drama'], overview: 'Explorers travel through a wormhole to find a new home for humanity.'},
  {id: '10000000-0000-4000-8000-000000000004', title: 'Blade Runner 2049', releaseDate: date('2017-10-06'), genres: ['Science Fiction', 'Drama'], overview: 'A young blade runner uncovers a secret that could plunge society into chaos.'},
  {id: '10000000-0000-4000-8000-000000000005', title: 'Arrival', releaseDate: date('2016-11-11'), genres: ['Science Fiction', 'Drama'], overview: 'A linguist is recruited to communicate with alien visitors.'},
  {id: '10000000-0000-4000-8000-000000000006', title: 'Dune', releaseDate: date('2021-10-22'), genres: ['Science Fiction', 'Adventure'], overview: 'A noble family takes control of the desert planet Arrakis and its precious spice.'},
  {id: '10000000-0000-4000-8000-000000000007', title: 'The Martian', releaseDate: date('2015-10-02'), genres: ['Science Fiction', 'Adventure'], overview: 'An astronaut stranded on Mars must survive until a rescue is possible.'},
  {id: '10000000-0000-4000-8000-000000000008', title: 'Ex Machina', releaseDate: date('2015-01-21'), genres: ['Science Fiction', 'Drama'], overview: 'A programmer evaluates the human qualities of a humanoid artificial intelligence.'},
  {id: '10000000-0000-4000-8000-000000000009', title: 'Alien', releaseDate: date('1979-05-25'), genres: ['Science Fiction', 'Horror'], overview: 'The crew of a commercial spaceship encounters a deadly life form.'},
  {id: '10000000-0000-4000-8000-000000000010', title: 'Star Wars', releaseDate: date('1977-05-25'), genres: ['Science Fiction', 'Adventure'], overview: 'A farm boy joins a rebellion against the Galactic Empire.'},
  {id: '10000000-0000-4000-8000-000000000011', title: 'Mad Max: Fury Road', releaseDate: date('2015-05-15'), genres: ['Action', 'Adventure'], overview: 'In a post-apocalyptic wasteland, a drifter and a rebel flee a tyrant.'},
  {id: '10000000-0000-4000-8000-000000000012', title: 'Edge of Tomorrow', releaseDate: date('2014-06-06'), genres: ['Science Fiction', 'Action'], overview: 'A soldier relives the same day of an alien war, getting better each time.'},
  {id: '10000000-0000-4000-8000-000000000013', title: 'The Shawshank Redemption', releaseDate: date('1994-09-23'), genres: ['Drama'], overview: 'Two imprisoned men bond over the years and find hope.'},
  {id: '10000000-0000-4000-8000-000000000014', title: 'The Godfather', releaseDate: date('1972-03-24'), genres: ['Drama'], overview: 'The aging patriarch of a crime dynasty transfers control to his reluctant son.'},
  {id: '10000000-0000-4000-8000-000000000015', title: 'Forrest Gump', releaseDate: date('1994-07-06'), genres: ['Drama', 'Romance'], overview: 'A kind man witnesses decades of American history.'},
  {id: '10000000-0000-4000-8000-000000000016', title: "Schindler's List", releaseDate: date('1993-12-15'), genres: ['Drama'], overview: 'A businessman saves more than a thousand Jewish refugees during the Holocaust.'},
  {id: '10000000-0000-4000-8000-000000000017', title: 'The Green Mile', releaseDate: date('1999-12-10'), genres: ['Drama'], overview: 'A death row guard discovers that one inmate has a mysterious gift.'},
  {id: '10000000-0000-4000-8000-000000000018', title: 'Good Will Hunting', releaseDate: date('1997-12-05'), genres: ['Drama', 'Romance'], overview: 'A janitor with a gift for mathematics is helped by a therapist.'},
  {id: '10000000-0000-4000-8000-000000000019', title: 'A Beautiful Mind', releaseDate: date('2001-12-21'), genres: ['Drama'], overview: 'A brilliant mathematician struggles with schizophrenia.'},
  {id: '10000000-0000-4000-8000-000000000020', title: 'The Pianist', releaseDate: date('2002-09-25'), genres: ['Drama'], overview: 'A Polish Jewish musician struggles to survive the Warsaw ghetto.'},
  {id: '10000000-0000-4000-8000-000000000021', title: 'Manchester by the Sea', releaseDate: date('2016-11-18'), genres: ['Drama'], overview: 'A man returns to his hometown to care for his nephew.'},
  {id: '10000000-0000-4000-8000-000000000022', title: 'Marriage Story', releaseDate: date('2019-11-06'), genres: ['Drama', 'Romance'], overview: 'A couple goes through a divorce that pushes them to their limits.'},
  {id: '10000000-0000-4000-8000-000000000023', title: 'Nomadland', releaseDate: date('2021-02-19'), genres: ['Drama'], overview: 'A woman in her sixties travels the American West living in a van.'},
  {id: '10000000-0000-4000-8000-000000000024', title: 'The Pursuit of Happyness', releaseDate: date('2006-12-15'), genres: ['Drama'], overview: 'A struggling salesman takes custody of his son as he begins a new career.'},
  {id: '10000000-0000-4000-8000-000000000025', title: 'Toy Story', releaseDate: date('1995-11-22'), genres: ['Animation', 'Comedy', 'Adventure'], overview: 'A cowboy doll feels threatened when a new spaceman toy arrives.'},
  {id: '10000000-0000-4000-8000-000000000026', title: 'Finding Nemo', releaseDate: date('2003-05-30'), genres: ['Animation', 'Adventure'], overview: 'A clownfish crosses the ocean to find his captured son.'},
  {id: '10000000-0000-4000-8000-000000000027', title: 'Spirited Away', releaseDate: date('2001-07-20'), genres: ['Animation', 'Adventure'], overview: 'A girl wanders into a world of spirits and must work to free her parents.'},
  {id: '10000000-0000-4000-8000-000000000028', title: 'Up', releaseDate: date('2009-05-29'), genres: ['Animation', 'Adventure', 'Comedy'], overview: 'An old man ties balloons to his house and flies to South America.'},
  {id: '10000000-0000-4000-8000-000000000029', title: 'Inside Out', releaseDate: date('2015-06-19'), genres: ['Animation', 'Comedy'], overview: 'The emotions inside a young girl try to guide her through a move.'},
  {id: '10000000-0000-4000-8000-000000000030', title: 'Coco', releaseDate: date('2017-11-22'), genres: ['Animation', 'Adventure'], overview: 'A boy enters the Land of the Dead to find his great-great-grandfather.'},
  {id: '10000000-0000-4000-8000-000000000031', title: 'Shrek', releaseDate: date('2001-05-18'), genres: ['Animation', 'Comedy'], overview: 'An ogre sets out to rescue a princess to get his swamp back.'},
  {id: '10000000-0000-4000-8000-000000000032', title: 'Ratatouille', releaseDate: date('2007-06-29'), genres: ['Animation', 'Comedy'], overview: 'A rat who dreams of becoming a chef teams up with a kitchen worker in Paris.'},
  {id: '10000000-0000-4000-8000-000000000033', title: 'Spider-Man: Into the Spider-Verse', releaseDate: date('2018-12-14'), genres: ['Animation', 'Action'], overview: 'A teenager becomes Spider-Man and meets his counterparts from other dimensions.'},
  {id: '10000000-0000-4000-8000-000000000034', title: 'The Incredibles', releaseDate: date('2004-11-05'), genres: ['Animation', 'Action'], overview: 'A family of undercover superheroes is forced into action.'},
  {id: '10000000-0000-4000-8000-000000000035', title: 'Get Out', releaseDate: date('2017-02-24'), genres: ['Horror'], overview: "A young man visits his girlfriend's family estate and uncovers a disturbing secret."},
  {id: '10000000-0000-4000-8000-000000000036', title: 'The Conjuring', releaseDate: date('2013-07-19'), genres: ['Horror'], overview: 'Paranormal investigators help a family terrorized by a dark presence.'},
  {id: '10000000-0000-4000-8000-000000000037', title: 'Hereditary', releaseDate: date('2018-06-08'), genres: ['Horror', 'Drama'], overview: 'After the family matriarch dies, her descendants are haunted by their ancestry.'},
  {id: '10000000-0000-4000-8000-000000000038', title: 'La La Land', releaseDate: date('2016-12-09'), genres: ['Romance', 'Drama', 'Comedy'], overview: 'A jazz pianist and an aspiring actress fall in love in Los Angeles.'},
  {id: '10000000-0000-4000-8000-000000000039', title: 'The Notebook', releaseDate: date('2004-06-25'), genres: ['Romance', 'Drama'], overview: 'A poor young man and a rich young woman fall in love in the 1940s.'},
  {id: '10000000-0000-4000-8000-000000000040', title: 'The Grand Budapest Hotel', releaseDate: date('2014-03-07'), genres: ['Comedy', 'Drama'], overview: 'A hotel concierge and his lobby boy are framed for a murder.'},
  {id: '10000000-0000-4000-8000-000000000041', title: 'Superbad', releaseDate: date('2007-08-17'), genres: ['Comedy'], overview: 'Two high school friends try to buy alcohol for a party before graduation.'},
  {id: '10000000-0000-4000-8000-000000000042', title: 'John Wick', releaseDate: date('2014-10-24'), genres: ['Action'], overview: 'A retired hitman seeks vengeance against the gangsters who wronged him.'}
] AS m
MERGE (mv:Movie {id: m.id})
SET mv.title = m.title, mv.releaseDate = m.releaseDate, mv.overview = m.overview
WITH mv, m
UNWIND m.genres AS genreName
MATCH (g:Genre {name: genreName})
MERGE (mv)-[:IN_GENRE]->(g);

// 4. Users. Same demo password for all: Neo4flix-Demo-2026!  (BCrypt, local demo only, see README)
// Taste clusters for the recommendation demo: neo/trinity/ripley/deckard like science fiction,
// andy/red/vito/forrest like drama, woody/nemo like animation, newbie has no ratings (cold start).
UNWIND [
  {id: '20000000-0000-4000-8000-000000000001', username: 'admin', roles: ['USER', 'ADMIN']},
  {id: '20000000-0000-4000-8000-000000000002', username: 'neo', roles: ['USER']},
  {id: '20000000-0000-4000-8000-000000000003', username: 'trinity', roles: ['USER']},
  {id: '20000000-0000-4000-8000-000000000004', username: 'ripley', roles: ['USER']},
  {id: '20000000-0000-4000-8000-000000000005', username: 'deckard', roles: ['USER']},
  {id: '20000000-0000-4000-8000-000000000006', username: 'andy', roles: ['USER']},
  {id: '20000000-0000-4000-8000-000000000007', username: 'red', roles: ['USER']},
  {id: '20000000-0000-4000-8000-000000000008', username: 'vito', roles: ['USER']},
  {id: '20000000-0000-4000-8000-000000000009', username: 'forrest', roles: ['USER']},
  {id: '20000000-0000-4000-8000-000000000010', username: 'woody', roles: ['USER']},
  {id: '20000000-0000-4000-8000-000000000011', username: 'nemo', roles: ['USER']},
  {id: '20000000-0000-4000-8000-000000000012', username: 'newbie', roles: ['USER']}
] AS u
MERGE (usr:User {id: u.id})
ON CREATE SET usr.createdAt = datetime()
SET usr.username = u.username,
    usr.email = u.username + '@neo4flix.local',
    usr.passwordHash = '$2b$10$.z7Pjyn7w6rFpEvsQ/AuhOdkA8PLOmQQLmryAG2cKv8UrGLYQgwgm',
    usr.totpEnabled = false,
    usr.roles = u.roles;

// 5. Ratings: [username, movie title, score]. Each cluster rates its genre 4 or 5 and a few others 1 to 3.
UNWIND [
  ['admin', 'The Matrix', 5], ['admin', 'Inception', 5], ['admin', 'The Godfather', 4], ['admin', 'Toy Story', 4],
  ['admin', 'Get Out', 4], ['admin', 'La La Land', 4], ['admin', 'John Wick', 3], ['admin', 'The Grand Budapest Hotel', 4],

  ['neo', 'The Matrix', 5], ['neo', 'Inception', 5], ['neo', 'Interstellar', 5], ['neo', 'Blade Runner 2049', 4],
  ['neo', 'Arrival', 4], ['neo', 'Alien', 4], ['neo', 'Star Wars', 5], ['neo', 'Edge of Tomorrow', 4],
  ['neo', 'The Notebook', 1], ['neo', 'The Shawshank Redemption', 2], ['neo', 'Toy Story', 2],

  ['trinity', 'The Matrix', 5], ['trinity', 'Inception', 4], ['trinity', 'Dune', 5], ['trinity', 'The Martian', 5],
  ['trinity', 'Ex Machina', 4], ['trinity', 'Mad Max: Fury Road', 5], ['trinity', 'Blade Runner 2049', 5], ['trinity', 'Star Wars', 4],
  ['trinity', 'Forrest Gump', 2], ['trinity', 'The Godfather', 2], ['trinity', 'Shrek', 2],

  ['ripley', 'Alien', 5], ['ripley', 'Arrival', 5], ['ripley', 'Dune', 4], ['ripley', 'The Martian', 4],
  ['ripley', 'Ex Machina', 5], ['ripley', 'Interstellar', 4], ['ripley', 'The Matrix', 4], ['ripley', 'Edge of Tomorrow', 5],
  ['ripley', 'La La Land', 1], ['ripley', 'The Pianist', 2],

  ['deckard', 'Blade Runner 2049', 5], ['deckard', 'Inception', 4], ['deckard', 'Dune', 5], ['deckard', 'Mad Max: Fury Road', 4],
  ['deckard', 'Ex Machina', 4], ['deckard', 'Star Wars', 4], ['deckard', 'Interstellar', 5], ['deckard', 'Alien', 4],
  ['deckard', 'Superbad', 1], ['deckard', 'Finding Nemo', 2],

  ['andy', 'The Shawshank Redemption', 5], ['andy', 'The Godfather', 5], ['andy', 'The Green Mile', 5], ['andy', 'Forrest Gump', 4],
  ['andy', 'Good Will Hunting', 4], ['andy', "Schindler's List", 5], ['andy', 'A Beautiful Mind', 4],
  ['andy', 'The Matrix', 2], ['andy', 'Shrek', 2], ['andy', 'The Conjuring', 1],

  ['red', 'The Shawshank Redemption', 5], ['red', 'The Pianist', 5], ['red', 'Manchester by the Sea', 4], ['red', 'Marriage Story', 4],
  ['red', 'The Godfather', 4], ['red', 'Good Will Hunting', 5], ['red', 'Nomadland', 4],
  ['red', 'Alien', 2], ['red', 'Toy Story', 3],

  ['vito', 'The Godfather', 5], ['vito', "Schindler's List", 5], ['vito', 'The Pianist', 4], ['vito', 'A Beautiful Mind', 5],
  ['vito', 'The Pursuit of Happyness', 4], ['vito', 'The Green Mile', 4], ['vito', 'Forrest Gump', 5],
  ['vito', 'Star Wars', 2], ['vito', 'Mad Max: Fury Road', 1],

  ['forrest', 'Forrest Gump', 5], ['forrest', 'The Green Mile', 4], ['forrest', 'The Pursuit of Happyness', 5], ['forrest', 'Marriage Story', 4],
  ['forrest', 'Nomadland', 5], ['forrest', 'Manchester by the Sea', 4], ['forrest', 'The Shawshank Redemption', 4], ['forrest', 'Good Will Hunting', 4],
  ['forrest', 'Edge of Tomorrow', 2], ['forrest', 'Up', 3],

  ['woody', 'Toy Story', 5], ['woody', 'Finding Nemo', 5], ['woody', 'Up', 4], ['woody', 'Inside Out', 5],
  ['woody', 'Shrek', 4], ['woody', 'The Incredibles', 4], ['woody', 'Coco', 4],
  ['woody', 'Hereditary', 1], ['woody', 'The Godfather', 2],

  ['nemo', 'Finding Nemo', 5], ['nemo', 'Spirited Away', 5], ['nemo', 'Coco', 5], ['nemo', 'Ratatouille', 4],
  ['nemo', 'Spider-Man: Into the Spider-Verse', 5], ['nemo', 'Up', 5], ['nemo', 'Toy Story', 4],
  ['nemo', 'Get Out', 1], ['nemo', 'Interstellar', 3]
] AS r
MATCH (u:User {username: r[0]}), (m:Movie {title: r[1]})
MERGE (u)-[rel:RATED]->(m)
ON CREATE SET rel.ratedAt = datetime()
SET rel.score = r[2];

// 6. Watchlist: [username, movie title]
UNWIND [
  ['neo', 'Dune'], ['neo', 'The Martian'],
  ['andy', 'Nomadland'],
  ['woody', 'Spider-Man: Into the Spider-Verse'],
  ['newbie', 'The Matrix']
] AS w
MATCH (u:User {username: w[0]}), (m:Movie {title: w[1]})
MERGE (u)-[rel:WATCHLISTED]->(m)
ON CREATE SET rel.addedAt = datetime();
