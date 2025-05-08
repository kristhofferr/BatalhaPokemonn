// kristhoffer RA:60300916, Leonardo Dezenisk RA:60000321,João Pedro Placido RA:60000589
fun main() {
    println("Digite o nick do jogador 1:")
    System.out.flush()
    val nome1 = readLine()?.takeIf { it.isNotBlank() } ?: "Jogador1"
    val jogador1 = criarJogador(nome1)
    montarTime(jogador1)

    println("\nDigite o nick do jogador 2:")
    System.out.flush()
    val nome2 = readLine()?.takeIf { it.isNotBlank() } ?: "Jogador2"
    val jogador2 = criarJogador(nome2)
    montarTime(jogador2)

    println("\n--- Time de ${jogador1["nickname"]} ---")
    mostrarTime(jogador1)

    println("\n--- Time de ${jogador2["nickname"]} ---")
    mostrarTime(jogador2)

    println("\nPressione ENTER para batalhar...")
    readLine()

    batalhar(jogador1, jogador2)
}

fun criarJogador(nickname: String): MutableMap<String, Any> {
    return mutableMapOf(
        "nickname" to nickname,
        "time" to mutableListOf<Map<String, Any?>>()
    )
}

fun montarTime(jogador: MutableMap<String, Any>, limite: Int = 3) {
    println("Montando time para ${jogador["nickname"]}")
    val time = jogador["time"] as MutableList<Map<String, Any?>>
    val listaPokedex = pokedex.toList().sortedBy { it.first }

    repeat(limite) {
        println("\nEscolha o número do Pokémon para adicionar:")
        for ((index, entry) in listaPokedex.withIndex()) {
            val pokemon = entry.second
            val tipo2 = pokemon["tipo2"]?.let { "/$it" } ?: ""
            println("${index + 1}. ${pokemon["nome"]} (${pokemon["tipo1"]}$tipo2) - Vida: ${pokemon["vida"]}")
        }

        var escolhido: MutableMap<String, Any?>? = null
        while (escolhido == null) {
            print("Digite o número correspondente: ")
            val input = readLine()?.trim()
            val numero = input?.toIntOrNull()

            if (numero != null && numero in 1..listaPokedex.size) {
                val pokemonOriginal = listaPokedex[numero - 1].second
                escolhido = pokemonOriginal.toMutableMap()
                escolhido["vidaAtual"] = escolhido["vida"]
                escolhido["experiencia"] = 0
                time.add(escolhido)
            } else {
                println("Número inválido. Tente novamente.")
            }
        }
    }
}

fun mostrarTime(jogador: Map<String, Any>) {
    val time = jogador["time"] as List<Map<String, Any?>>
    for ((index, pokemon) in time.withIndex()) {
        val tipo2 = pokemon["tipo2"]?.let { "/$it" } ?: ""
        println("${index + 1}. ${pokemon["nome"]} (${pokemon["tipo1"]}$tipo2)")
    }
}

fun calcularDano(atacante: Map<String, Any?>, defensor: Map<String, Any?>): Int {
    val ataque = atacante["ataque"] as Int
    val defesa = defensor["defesa"] as Int
    return (ataque - defesa / 2).coerceAtLeast(5)
}

fun batalhar(jogador1: Map<String, Any>, jogador2: Map<String, Any>) {
    val time1 = (jogador1["time"] as List<Map<String, Any?>>).map { it.toMutableMap() }
    val time2 = (jogador2["time"] as List<Map<String, Any?>>).map { it.toMutableMap() }

    var index1 = 0
    var index2 = 0

    while (index1 < time1.size && index2 < time2.size) {
        val p1 = time1[index1]
        val p2 = time2[index2]

        println("\nRodada: ${index1 + 1}")
        println("${jogador1["nickname"]}: ${p1["nome"]} (${p1["vidaAtual"]}) vs ${jogador2["nickname"]}: ${p2["nome"]} (${p2["vidaAtual"]})")

        while ((p1["vidaAtual"] as Int) > 0 && (p2["vidaAtual"] as Int) > 0) {
            val danoP1 = calcularDano(p1, p2)
            p2["vidaAtual"] = (p2["vidaAtual"] as Int - danoP1).coerceAtLeast(0)
            println("${p1["nome"]} atacou ${p2["nome"]} causando $danoP1 de dano. Vida restante: ${p2["vidaAtual"]}")

            if ((p2["vidaAtual"] as Int) <= 0) break

            val danoP2 = calcularDano(p2, p1)
            p1["vidaAtual"] = (p1["vidaAtual"] as Int - danoP2).coerceAtLeast(0)
            println("${p2["nome"]} atacou ${p1["nome"]} causando $danoP2 de dano. Vida restante: ${p1["vidaAtual"]}")
        }

        if ((p1["vidaAtual"] as Int) <= 0) {
            println("${p1["nome"]} foi derrotado!")
            index1++
            ganharExperiencia(p2)
        }
        if ((p2["vidaAtual"] as Int) <= 0) {
            println("${p2["nome"]} foi derrotado!")
            index2++
            ganharExperiencia(p1)
        }

        println("Pressione ENTER para a próxima rodada...")
        readLine()
    }

    when {
        index1 == time1.size && index2 == time2.size -> println("Empate!")
        index1 == time1.size -> println("${jogador2["nickname"]} venceu a batalha!")
        else -> println("${jogador1["nickname"]} venceu a batalha!")
    }
}

fun ganharExperiencia(pokemon: MutableMap<String, Any?>) {
    val novaExp = (pokemon["experiencia"] as? Int ?: 0) + 1
    pokemon["experiencia"] = novaExp
    println("${pokemon["nome"]} ganhou experiência! Total: $novaExp")
    if (novaExp >= 2) {
        evoluirPokemon(pokemon)
    }
}

fun evoluirPokemon(pokemon: MutableMap<String, Any?>) {
    val nomeAtual = pokemon["nome"] as String
    val proximaEvolucao = evolucoes[nomeAtual]
    if (proximaEvolucao != null) {
        println("${nomeAtual} está evoluindo para ${proximaEvolucao}!")
        val novoPokemon = pokedex.values.find { it["nome"] == proximaEvolucao }?.toMutableMap()
        if (novoPokemon != null) {
            novoPokemon["vidaAtual"] = novoPokemon["vida"]
            novoPokemon["experiencia"] = 0
            novoPokemon["ataque"] = (novoPokemon["ataque"] as Int) + 20
            pokemon.clear()
            pokemon.putAll(novoPokemon)
            println("${pokemon["nome"]} evoluiu e agora tem ataque ${pokemon["ataque"]}!")
        }
    }
}
fun toPokemon(
    nome: String,
    tipo1: String,
    tipo2: String? = null,
    ataque: Int = (40..100).random(),
    defesa: Int = (30..90).random(),
    vida: Int = (100..200).random()
): Map<String, Any?> {
    return mapOf(
        "nome" to nome,
        "tipo1" to tipo1,
        "tipo2" to tipo2,
        "ataque" to ataque,
        "defesa" to defesa,
        "vida" to vida,
        "vidaAtual" to vida
    )
}

val evolucoes = mapOf(
    "Bulbasaur"    to "Ivysaur",
    "Ivysaur"      to "Venusaur",
    "Charmander"   to "Charmeleon",
    "Charmeleon"   to "Charizard",
    "Squirtle"     to "Wartortle",
    "Wartortle"    to "Blastoise",
    "Caterpie"     to "Metapod",
    "Metapod"      to "Butterfree",
    "Weedle"       to "Kakuna",
    "Kakuna"       to "Beedrill",
    "Pidgey"       to "Pidgeotto",
    "Pidgeotto"    to "Pidgeot",
    "Rattata"      to "Raticate",
    "Spearow"      to "Fearow",
    "Ekans"        to "Arbok",
    "Sandshrew"    to "Sandslash",
    "Nidoran♀"     to "Nidorina",
    "Nidorina"     to "Nidoqueen",
    "Nidoran♂"     to "Nidorino",
    "Nidorino"     to "Nidoking",
    "Clefairy"     to "Clefable",
    "Vulpix"       to "Ninetales",
    "Jigglypuff"   to "Wigglytuff",
    "Zubat"        to "Golbat",
    "Oddish"       to "Gloom",
    "Gloom"        to "Vileplume",
    "Paras"        to "Parasect",
    "Venonat"      to "Venomoth",
    "Diglett"      to "Dugtrio",
    "Meowth"       to "Persian",
    "Psyduck"      to "Golduck",
    "Mankey"       to "Primeape",
    "Growlithe"    to "Arcanine",
    "Poliwag"      to "Poliwhirl",
    "Poliwhirl"    to "Poliwrath",
    "Abra"         to "Kadabra",
    "Kadabra"      to "Alakazam",
    "Machop"       to "Machoke",
    "Machoke"      to "Machamp",
    "Bellsprout"   to "Weepinbell",
    "Weepinbell"   to "Victreebel",
    "Tentacool"    to "Tentacruel",
    "Geodude"      to "Graveler",
    "Graveler"     to "Golem",
    "Ponyta"       to "Rapidash",
    "Slowpoke"     to "Slowbro",
    "Magnemite"    to "Magneton",
    "Doduo"        to "Dodrio",
    "Seel"         to "Dewgong",
    "Grimer"       to "Muk",
    "Shellder"     to "Cloyster",
    "Gastly"       to "Haunter",
    "Haunter"      to "Gengar",
    "Drowzee"      to "Hypno",
    "Krabby"       to "Kingler",
    "Voltorb"      to "Electrode",
    "Exeggcute"    to "Exeggutor",
    "Cubone"       to "Marowak",
    "Koffing"      to "Weezing",
    "Rhyhorn"      to "Rhydon",
    "Horsea"       to "Seadra",
    "Goldeen"      to "Seaking",
    "Staryu"       to "Starmie",
    "Magikarp"     to "Gyarados",
    "Eevee"        to "Vaporeon",
    "Omanyte"      to "Omastar",
    "Kabuto"       to "Kabutops",
    "Dratini"      to "Dragonair",
    "Dragonair"    to "Dragonite"
)
val pokedex: Map<Int, Map<String, Any?>> = mapOf(
    1 to toPokemon("Bulbasaur", "Grama", "Veneno"),
    2 to toPokemon("Ivysaur", "Grama", "Veneno"),
    3 to toPokemon("Venusaur", "Grama", "Veneno"),
    4 to toPokemon("Charmander", "Fogo"),
    5 to toPokemon("Charmeleon", "Fogo"),
    6 to toPokemon("Charizard", "Fogo", "Voador"),
    7 to toPokemon("Squirtle", "Água"),
    8 to toPokemon("Wartortle", "Água"),
    9 to toPokemon("Blastoise", "Água"),
    10 to toPokemon("Caterpie", "Inseto"),
    11 to toPokemon("Metapod", "Inseto"),
    12 to toPokemon("Butterfree", "Inseto", "Voador"),
    13 to toPokemon("Weedle", "Inseto", "Veneno"),
    14 to toPokemon("Kakuna", "Inseto", "Veneno"),
    15 to toPokemon("Beedrill", "Inseto", "Veneno"),
    16 to toPokemon("Pidgey", "Normal", "Voador"),
    17 to toPokemon("Pidgeotto", "Normal", "Voador"),
    18 to toPokemon("Pidgeot", "Normal", "Voador"),
    19 to toPokemon("Rattata", "Normal"),
    20 to toPokemon("Raticate", "Normal"),
    21 to toPokemon("Spearow", "Normal", "Voador"),
    22 to toPokemon("Fearow", "Normal", "Voador"),
    23 to toPokemon("Ekans", "Veneno"),
    24 to toPokemon("Arbok", "Veneno"),
    25 to toPokemon("Pikachu", "Elétrico"),
    26 to toPokemon("Raichu", "Elétrico"),
    27 to toPokemon("Sandshrew", "Terrestre"),
    28 to toPokemon("Sandslash", "Terrestre"),
    29 to toPokemon("Nidoran♀", "Veneno"),
    30 to toPokemon("Nidorina", "Veneno"),
    31 to toPokemon("Nidoqueen", "Veneno", "Terrestre"),
    32 to toPokemon("Nidoran♂", "Veneno"),
    33 to toPokemon("Nidorino", "Veneno"),
    34 to toPokemon("Nidoking", "Veneno", "Terrestre"),
    35 to toPokemon("Clefairy", "Fada"),
    36 to toPokemon("Clefable", "Fada"),
    37 to toPokemon("Vulpix", "Fogo"),
    38 to toPokemon("Ninetales", "Fogo"),
    39 to toPokemon("Jigglypuff", "Normal", "Fada"),
    40 to toPokemon("Wigglytuff", "Normal", "Fada"),
    41 to toPokemon("Zubat", "Veneno", "Voador"),
    42 to toPokemon("Golbat", "Veneno", "Voador"),
    43 to toPokemon("Oddish", "Grama", "Veneno"),
    44 to toPokemon("Gloom", "Grama", "Veneno"),
    45 to toPokemon("Vileplume", "Grama", "Veneno"),
    46 to toPokemon("Paras", "Inseto", "Grama"),
    47 to toPokemon("Parasect", "Inseto", "Grama"),
    48 to toPokemon("Venonat", "Inseto", "Veneno"),
    49 to toPokemon("Venomoth", "Inseto", "Veneno"),
    50 to toPokemon("Diglett", "Terrestre"),
    51 to toPokemon("Dugtrio", "Terrestre"),
    52 to toPokemon("Meowth", "Normal"),
    53 to toPokemon("Persian", "Normal"),
    54 to toPokemon("Psyduck", "Água"),
    55 to toPokemon("Golduck", "Água"),
    56 to toPokemon("Mankey", "Lutador"),
    57 to toPokemon("Primeape", "Lutador"),
    58 to toPokemon("Growlithe", "Fogo"),
    59 to toPokemon("Arcanine", "Fogo"),
    60 to toPokemon("Poliwag", "Água"),
    61 to toPokemon("Poliwhirl", "Água"),
    62 to toPokemon("Poliwrath", "Água", "Lutador"),
    63 to toPokemon("Abra", "Psíquico"),
    64 to toPokemon("Kadabra", "Psíquico"),
    65 to toPokemon("Alakazam", "Psíquico"),
    66 to toPokemon("Machop", "Lutador"),
    67 to toPokemon("Machoke", "Lutador"),
    68 to toPokemon("Machamp", "Lutador"),
    69 to toPokemon("Bellsprout", "Grama", "Veneno"),
    70 to toPokemon("Weepinbell", "Grama", "Veneno"),
    71 to toPokemon("Victreebel", "Grama", "Veneno"),
    72 to toPokemon("Tentacool", "Água", "Veneno"),
    73 to toPokemon("Tentacruel", "Água", "Veneno"),
    74 to toPokemon("Geodude", "Pedra", "Terrestre"),
    75 to toPokemon("Graveler", "Pedra", "Terrestre"),
    76 to toPokemon("Golem", "Pedra", "Terrestre"),
    77 to toPokemon("Ponyta", "Fogo"),
    78 to toPokemon("Rapidash", "Fogo"),
    79 to toPokemon("Slowpoke", "Água", "Psíquico"),
    80 to toPokemon("Slowbro", "Água", "Psíquico"),
    81 to toPokemon("Magnemite", "Elétrico", "Aço"),
    82 to toPokemon("Magneton", "Elétrico", "Aço"),
    83 to toPokemon("Farfetch'd", "Normal", "Voador"),
    84 to toPokemon("Doduo", "Normal", "Voador"),
    85 to toPokemon("Dodrio", "Normal", "Voador"),
    86 to toPokemon("Seel", "Água"),
    87 to toPokemon("Dewgong", "Água", "Gelo"),
    88 to toPokemon("Grimer", "Veneno"),
    89 to toPokemon("Muk", "Veneno"),
    90 to toPokemon("Shellder", "Água"),
    91 to toPokemon("Cloyster", "Água", "Gelo"),
    92 to toPokemon("Gastly", "Fantasma", "Veneno"),
    93 to toPokemon("Haunter", "Fantasma", "Veneno"),
    94 to toPokemon("Gengar", "Fantasma", "Veneno"),
    95 to toPokemon("Onix", "Pedra", "Terrestre"),
    96 to toPokemon("Drowzee", "Psíquico"),
    97 to toPokemon("Hypno", "Psíquico"),
    98 to toPokemon("Krabby", "Água"),
    99 to toPokemon("Kingler", "Água"),
    100 to toPokemon("Voltorb", "Elétrico"),
    101 to toPokemon("Electrode", "Elétrico"),
    102 to toPokemon("Exeggcute", "Grama", "Psíquico"),
    103 to toPokemon("Exeggutor", "Grama", "Psíquico"),
    104 to toPokemon("Cubone", "Terrestre"),
    105 to toPokemon("Marowak", "Terrestre"),
    106 to toPokemon("Hitmonlee", "Lutador"),
    107 to toPokemon("Hitmonchan", "Lutador"),
    108 to toPokemon("Lickitung", "Normal"),
    109 to toPokemon("Koffing", "Veneno"),
    110 to toPokemon("Weezing", "Veneno"),
    111 to toPokemon("Rhyhorn", "Terrestre", "Pedra"),
    112 to toPokemon("Rhydon", "Terrestre", "Pedra"),
    113 to toPokemon("Chansey", "Normal"),
    114 to toPokemon("Tangela", "Grama"),
    115 to toPokemon("Kangaskhan", "Normal"),
    116 to toPokemon("Horsea", "Água"),
    117 to toPokemon("Seadra", "Água"),
    118 to toPokemon("Goldeen", "Água"),
    119 to toPokemon("Seaking", "Água"),
    120 to toPokemon("Staryu", "Água"),
    121 to toPokemon("Starmie", "Água", "Psíquico"),
    122 to toPokemon("Mr. Mime", "Psíquico", "Fada"),
    123 to toPokemon("Scyther", "Inseto", "Voador"),
    124 to toPokemon("Jynx", "Gelo", "Psíquico"),
    125 to toPokemon("Electabuzz", "Elétrico"),
    126 to toPokemon("Magmar", "Fogo"),
    127 to toPokemon("Pinsir", "Inseto"),
    128 to toPokemon("Tauros", "Normal"),
    129 to toPokemon("Magikarp", "Água"),
    130 to toPokemon("Gyarados", "Água", "Voador"),
    131 to toPokemon("Lapras", "Água", "Gelo"),
    132 to toPokemon("Ditto", "Normal"),
    133 to toPokemon("Eevee", "Normal"),
    134 to toPokemon("Vaporeon", "Água"),
    135 to toPokemon("Jolteon", "Elétrico"),
    136 to toPokemon("Flareon", "Fogo"),
    137 to toPokemon("Porygon", "Normal"),
    138 to toPokemon("Omanyte", "Pedra", "Água"),
    139 to toPokemon("Omastar", "Pedra", "Água"),
    140 to toPokemon("Kabuto", "Pedra", "Água"),
    141 to toPokemon("Kabutops", "Pedra", "Água"),
    142 to toPokemon("Aerodactyl", "Pedra", "Voador"),
    143 to toPokemon("Snorlax", "Normal"),
    144 to toPokemon("Articuno", "Gelo", "Voador"),
    145 to toPokemon("Zapdos", "Elétrico", "Voador"),
    146 to toPokemon("Moltres", "Fogo", "Voador"),
    147 to toPokemon("Dratini", "Dragão"),
    148 to toPokemon("Dragonair", "Dragão"),
    149 to toPokemon("Dragonite", "Dragão", "Voador"),
    150 to toPokemon("Mewtwo", "Psíquico"),
    151 to toPokemon("Mew", "Psíquico")
)

