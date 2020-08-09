package models

import com.google.gson.annotations.SerializedName

data class ZipCode (
    @SerializedName("cod_distrito") var codDistrito: String,
    @SerializedName("cod_concelho") var codConcelho: String,
    @SerializedName("cod_localidade") var codLocalidade: String,
    @SerializedName("nome_localidade") var nomeLocalidade: String,
    @SerializedName("cod_arteria") var codArteria: String,
    @SerializedName("tipo_arteria") var tipoArteria: String,
    var prep1: String,
    @SerializedName("titulo_arteria") var tituloArteria: String,
    var prep2: String,
    @SerializedName("nome_arteria") var nomeArteria: String,
    @SerializedName("local_arteria") var localArteria: String,
    var troco: String,
    var porta: String,
    var cliente: String,
    @SerializedName("num_cod_postal") var numCodPostal: String,
    @SerializedName("ext_cod_postal") var extCodPostal: String,
    @SerializedName("desig_postal") var desigPostal: String
)